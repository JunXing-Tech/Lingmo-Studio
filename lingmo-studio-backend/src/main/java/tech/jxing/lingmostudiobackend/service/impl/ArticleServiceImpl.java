package tech.jxing.lingmostudiobackend.service.impl;

import cn.hutool.core.util.IdUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tech.jxing.lingmostudiobackend.exception.BusinessException;
import tech.jxing.lingmostudiobackend.exception.ErrorCode;
import tech.jxing.lingmostudiobackend.exception.ThrowUtils;
import tech.jxing.lingmostudiobackend.mapper.ArticleMapper;
import tech.jxing.lingmostudiobackend.model.dto.article.ArticleQueryRequest;
import tech.jxing.lingmostudiobackend.model.dto.article.ArticleState;
import tech.jxing.lingmostudiobackend.model.entity.Article;
import tech.jxing.lingmostudiobackend.model.entity.User;
import tech.jxing.lingmostudiobackend.model.enums.ArticleStatusEnum;
import tech.jxing.lingmostudiobackend.model.vo.ArticleVO;
import tech.jxing.lingmostudiobackend.service.ArticleService;
import tech.jxing.lingmostudiobackend.utils.GsonUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static tech.jxing.lingmostudiobackend.constant.UserConstant.ADMIN_ROLE;

/**
 * 文章业务服务实现类。
 * 负责文章任务的创建、状态维护、内容持久化以及分页查询等核心数据库操作。
 * 该类继承自 MyBatis-Flex 的 ServiceImpl，具备基础的 CRUD 能力。
 */
@Service
@Slf4j
public class ArticleServiceImpl extends ServiceImpl<ArticleMapper, Article> implements ArticleService {

    /**
     * 创建一个新的文章生成任务记录。
     *
     * @param topic     用户输入的选题
     * @param loginUser 当前登录用户
     * @return 生成的任务 ID (taskId)
     */
    @Override
    public String createArticleTask(String topic, User loginUser) {
        // 1. 生成全局唯一的任务 ID (使用 UUID)
        String taskId = IdUtil.simpleUUID();

        // 2. 构造初始文章记录
        Article article = new Article();
        article.setTaskId(taskId);
        article.setUserId(loginUser.getId());
        article.setTopic(topic);
        // 初始状态设置为“等待中”
        article.setStatus(ArticleStatusEnum.PENDING.getValue());
        article.setCreateTime(LocalDateTime.now());

        // 3. 保存到数据库
        this.save(article);

        log.info("文章任务已成功创建, taskId={}, userId={}", taskId, loginUser.getId());
        return taskId;
    }

    /**
     * 根据任务 ID 获取文章记录。
     */
    @Override
    public Article getByTaskId(String taskId) {
        return this.getOne(
                QueryWrapper.create().eq("taskId", taskId)
        );
    }

    /**
     * 更新文章任务的执行状态及错误信息。
     *
     * @param taskId       任务 ID
     * @param status       目标状态枚举
     * @param errorMessage 错误信息（仅在状态为 FAILED 时有值）
     */
    @Override
    public void updateArticleStatus(String taskId, ArticleStatusEnum status, String errorMessage) {
        Article article = getByTaskId(taskId);

        if (article == null) {
            log.error("更新状态失败：文章记录不存在, taskId={}", taskId);
            return;
        }

        article.setStatus(status.getValue());
        article.setErrorMessage(errorMessage);
        this.updateById(article);

        log.info("文章状态已更新, taskId={}, status={}", taskId, status.getValue());
    }

    /**
     * 将 AI 智能体生成的最终文章内容及元数据保存到数据库。
     *
     * @param taskId 任务 ID
     * @param state  包含 AI 生成结果的上下文状态对象
     */
    @Override
    public void saveArticleContent(String taskId, ArticleState state) {
        Article article = getByTaskId(taskId);

        if (article == null) {
            log.error("保存内容失败：文章记录不存在, taskId={}", taskId);
            return;
        }

        // 1. 设置文章基本内容
        article.setMainTitle(state.getTitle().getMainTitle());
        article.setSubTitle(state.getTitle().getSubTitle());
        // 将大纲对象列表序列化为 JSON 字符串存储
        article.setOutline(GsonUtils.toJson(state.getOutline().getSections()));
        article.setContent(state.getContent());
        article.setFullContent(state.getFullContent());

        // 2. 提取并设置封面图 URL（约定 position=1 的为封面图）
        if (state.getImages() != null && !state.getImages().isEmpty()) {
            ArticleState.ImageResult cover = state.getImages().stream()
                    .filter(img -> img.getPosition() != null && img.getPosition() == 1)
                    .findFirst()
                    .orElse(null);
            if (cover != null && cover.getUrl() != null) {
                article.setCoverImage(cover.getUrl());
            }
        }
        // 3. 将所有配图结果序列化为 JSON 字符串存储
        article.setImages(GsonUtils.toJson(state.getImages()));
        // 4. 设置任务完成时间
        article.setCompletedTime(LocalDateTime.now());

        this.updateById(article);
        log.info("文章内容持久化成功, taskId={}", taskId);
    }

    /**
     * 获取文章详情，并进行权限校验。
     */
    @Override
    public ArticleVO getArticleDetail(String taskId, User loginUser) {
        Article article = getByTaskId(taskId);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR, "文章不存在");

        // 权限校验：非管理员只能查看自己的文章
        checkArticlePermission(article, loginUser);

        return ArticleVO.objToVo(article);
    }

    /**
     * 分页查询文章列表。
     * 管理员可查看全部，普通用户只能查看自己的。
     */
    @Override
    public Page<ArticleVO> listArticleByPage(ArticleQueryRequest request, User loginUser) {
        long current = request.getPageNum();
        long size = request.getPageSize();

        // 1. 构建查询条件
        QueryWrapper queryWrapper = QueryWrapper.create()
                .eq("isDelete", 0)
                .orderBy("createTime", false);

        // 2. 权限过滤：非管理员只能看自己的数据
        if (!ADMIN_ROLE.equals(loginUser.getUserRole())) {
            queryWrapper.eq("userId", loginUser.getId());
        } else if (request.getUserId() != null) {
            // 管理员可以按用户 ID 筛选
            queryWrapper.eq("userId", request.getUserId());
        }

        // 3. 状态过滤
        if (request.getStatus() != null && !request.getStatus().trim().isEmpty()) {
            queryWrapper.eq("status", request.getStatus());
        }

        // 4. 执行 MyBatis-Flex 分页查询
        Page<Article> articlePage = this.page(new Page<>(current, size), queryWrapper);

        // 5. 将 Entity 分页转换为 VO 分页
        return convertToVOPage(articlePage);
    }

    /**
     * 逻辑删除文章。
     */
    @Override
    public boolean deleteArticle(Long id, User loginUser) {
        Article article = this.getById(id);
        ThrowUtils.throwIf(article == null, ErrorCode.NOT_FOUND_ERROR);

        // 校验删除权限
        checkArticlePermission(article, loginUser);

        // MyBatis-Flex 的逻辑删除（根据配置会自动更新 isDelete 字段）
        return this.removeById(id);
    }

    /**
     * 私有辅助方法：校验当前用户是否有权操作目标文章。
     *
     * @param article   文章实体
     * @param loginUser 当前登录用户
     */
    private void checkArticlePermission(Article article, User loginUser) {
        if (!article.getUserId().equals(loginUser.getId()) &&
                !ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权操作该文章");
        }
    }

    /**
     * 私有辅助方法：将 MyBatis-Flex 的 Page<Article> 转换为 Page<ArticleVO>。
     */
    private Page<ArticleVO> convertToVOPage(Page<Article> articlePage) {
        Page<ArticleVO> articleVOPage = new Page<>();
        articleVOPage.setPageNumber(articlePage.getPageNumber());
        articleVOPage.setPageSize(articlePage.getPageSize());
        articleVOPage.setTotalRow(articlePage.getTotalRow());

        List<ArticleVO> articleVOList = articlePage.getRecords().stream()
                .map(ArticleVO::objToVo)
                .collect(Collectors.toList());
        articleVOPage.setRecords(articleVOList);

        return articleVOPage;
    }
}
