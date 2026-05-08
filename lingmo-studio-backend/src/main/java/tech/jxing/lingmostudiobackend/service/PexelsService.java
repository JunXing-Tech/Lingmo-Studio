package tech.jxing.lingmostudiobackend.service;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;
import tech.jxing.lingmostudiobackend.config.PexelsConfig;
import tech.jxing.lingmostudiobackend.model.enums.ImageMethodEnum;

import java.io.IOException;

import static tech.jxing.lingmostudiobackend.constant.ArticleConstant.*;

/**
 * Pexels 图片检索服务实现类。
 * 通过对接 Pexels 开放 API，根据关键词搜索高质量的免费图片。
 */
@Service
@Slf4j
public class PexelsService implements ImageSearchService {

    @Resource
    private PexelsConfig pexelsConfig;

    // 使用 OkHttpClient 发起网络请求
    private final OkHttpClient httpClient = new OkHttpClient();

    /**
     * 根据关键词在 Pexels 平台搜索图片。
     *
     * @param keywords 搜索关键词
     * @return 匹配的第一张图片的 URL，如果未找到或调用失败则返回 null
     */
    @Override
    public String searchImage(String keywords) {
        try {
            // 1. 构建搜索请求的 URL
            String url = buildSearchUrl(keywords);
            
            // 2. 创建 HTTP 请求，并在 Header 中添加 API Key 进行鉴权
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("Authorization", pexelsConfig.getApiKey())
                    .build();

            // 3. 执行请求并处理响应
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("Pexels API 调用失败: 状态码={}, 关键词={}", response.code(), keywords);
                    return null;
                }

                // 4. 解析响应体，提取图片 URL
                String responseBody = response.body().string();
                return extractImageUrl(responseBody, keywords);
            }
        } catch (IOException e) {
            log.error("Pexels API 调用发生网络异常, 关键词={}", keywords, e);
            return null;
        }
    }

    /**
     * 返回当前服务使用的图片获取方式。
     */
    @Override
    public ImageMethodEnum getMethod() {
        return ImageMethodEnum.PEXELS;
    }

    /**
     * 当 API 调用失败或无结果时的备选方案。
     * 使用 Picsum 提供的占位图片服务。
     *
     * @param position 图片在文章中的位置索引（用于生成不同的随机图）
     * @return 备选图片 URL
     */
    @Override
    public String getFallbackImage(int position) {
        return String.format(PICSUM_URL_TEMPLATE, position);
    }

    /**
     * 构建 Pexels 搜索 URL。
     * 包含查询词、每页结果数、图片方向等参数。
     *
     * @param keywords 搜索关键词
     * @return 完整的 API 搜索地址
     */
    private String buildSearchUrl(String keywords) {
        return String.format("%s?query=%s&per_page=%d&orientation=%s",
                PEXELS_API_URL,
                keywords,
                PEXELS_PER_PAGE,
                PEXELS_ORIENTATION_LANDSCAPE);
    }

    /**
     * 从 Pexels API 返回的 JSON 响应中提取第一张图片的 large 尺寸 URL。
     *
     * @param responseBody JSON 响应体
     * @param keywords     用于日志记录的关键词
     * @return 图片的 URL 地址
     */
    private String extractImageUrl(String responseBody, String keywords) {
        // 使用 Gson 解析 JSON 字符串
        JsonObject jsonObject = JsonParser.parseString(responseBody).getAsJsonObject();
        JsonArray photos = jsonObject.getAsJsonArray("photos");
        
        // 检查搜索结果是否为空
        if (photos.isEmpty()) {
            log.warn("Pexels 未能为关键词 [{}] 检索到任何图片", keywords);
            return null;
        }

        // 提取第一张照片的 'large' 规格 URL
        JsonObject photo = photos.get(0).getAsJsonObject();
        JsonObject src = photo.getAsJsonObject("src");
        return src.get("large").getAsString();
    }
}
