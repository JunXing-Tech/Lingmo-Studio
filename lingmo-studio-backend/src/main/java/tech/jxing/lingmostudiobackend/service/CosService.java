package tech.jxing.lingmostudiobackend.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.http.HttpProtocol;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import tech.jxing.lingmostudiobackend.config.CosConfig;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 腾讯云对象存储（COS）服务实现类。
 * 负责将外部网络图片下载并转存到腾讯云 COS 中，以保证图片的持久化和访问稳定性。
 */
@Service
@Slf4j
public class CosService {

    @Resource
    private CosConfig cosConfig;

    private COSClient cosClient;

    // 用于从外部 URL 下载图片
    private final OkHttpClient httpClient = new OkHttpClient();

    /**
     * Spring Bean 初始化后，根据配置信息初始化腾讯云 COS 客户端。
     */
    @PostConstruct
    public void init() {
        // 使用 SecretId 和 SecretKey 进行身份验证
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        // 设置存储桶所在的区域
        Region region = new Region(cosConfig.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        // 强制使用 HTTPS 协议，保证传输安全
        clientConfig.setHttpProtocol(HttpProtocol.https);
        // 构造 COS 客户端
        cosClient = new COSClient(cred, clientConfig);
    }

    /**
     * 将指定的网络图片 URL 下载并上传到 COS 存储桶中。
     *
     * @param imageUrl 原始网络图片 URL
     * @param folder   存储在 COS 中的目标文件夹名称
     * @return 上传后的 COS 访问 URL；如果上传失败，则返回原始 URL 以进行降级处理。
     */
    public String uploadImage(String imageUrl, String folder) {
        try {
            // 1. 发起网络请求下载图片
            Request request = new Request.Builder().url(imageUrl).build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("下载图片失败, URL={}", imageUrl);
                    // 降级：下载失败则直接返回原始 URL
                    return imageUrl;
                }

                // 获取图片二进制数据
                byte[] imageBytes = response.body().bytes();
                
                // 2. 构造存储路径和文件名（使用 UUID 防止文件名冲突）
                String fileName = folder + "/" + UUID.randomUUID() + ".jpg";
                
                // 3. 将字节数组转换为输入流并上传到 COS
                try (InputStream inputStream = new ByteArrayInputStream(imageBytes)) {
                    ObjectMetadata metadata = new ObjectMetadata();
                    // 设置内容长度和内容类型，有助于浏览器正确识别和缓存
                    metadata.setContentLength(imageBytes.length);
                    metadata.setContentType("image/jpeg");
                    
                    PutObjectRequest putObjectRequest = new PutObjectRequest(
                            cosConfig.getBucket(), fileName, inputStream, metadata);
                    
                    // 执行上传操作
                    cosClient.putObject(putObjectRequest);
                    
                    // 4. 构造并返回 COS 的公网访问 URL
                    return String.format("https://%s.cos.%s.myqcloud.com/%s", 
                            cosConfig.getBucket(), cosConfig.getRegion(), fileName);
                }
            }
        } catch (IOException e) {
            log.error("上传图片到 COS 发生异常, URL={}", imageUrl, e);
            // 降级：异常情况下直接返回原始 URL
            return imageUrl; 
        }
    }

    /**
     * 直接返回图片原始 URL，不执行转存操作。
     * 用于不需要持久化或测试场景。
     *
     * @param imageUrl 图片 URL
     * @return 图片原始 URL
     */
    public String useDirectUrl(String imageUrl) {
        return imageUrl;
    }
}