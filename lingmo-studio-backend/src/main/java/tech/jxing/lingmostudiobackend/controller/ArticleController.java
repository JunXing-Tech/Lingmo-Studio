package tech.jxing.lingmostudiobackend.controller;

import com.mybatisflex.core.paginate.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import tech.jxing.lingmostudiobackend.annotation.AuthCheck;
import tech.jxing.lingmostudiobackend.common.BaseResponse;
import tech.jxing.lingmostudiobackend.common.DeleteRequest;
import tech.jxing.lingmostudiobackend.common.ResultUtils;
import tech.jxing.lingmostudiobackend.exception.ErrorCode;
import tech.jxing.lingmostudiobackend.exception.ThrowUtils;
import tech.jxing.lingmostudiobackend.manager.SseEmitterManager;
import tech.jxing.lingmostudiobackend.model.dto.article.ArticleCreateRequest;
import tech.jxing.lingmostudiobackend.model.dto.article.ArticleQueryRequest;
import tech.jxing.lingmostudiobackend.model.entity.User;
import tech.jxing.lingmostudiobackend.model.vo.ArticleVO;
import tech.jxing.lingmostudiobackend.service.ArticleAsyncService;
import tech.jxing.lingmostudiobackend.service.ArticleService;
import tech.jxing.lingmostudiobackend.service.UserService;

/**
 * 文章业务接口控制器。
 * 核心功能：处理文章生成任务的创建、建立 SSE 实时进度连接、文章列表及详情查询、文章删除。
 */
@RestController
@RequestMapping("/article")
@Tag(name = "文章接口")
@Slf4j
public class ArticleController {

    @Resource
    private ArticleService articleService;

    @Resource
    private ArticleAsyncService articleAsyncService;

    @Resource
    private SseEmitterManager sseEmitterManager;

    @Resource
    private UserService userService;

    /**
     * 创建文章任务。
     * 用户提交选题后，后端会立即返回任务 ID，并在后台异步启动 AI 生成流程。
     */
    @PostMapping("/create")
    @Operation(summary = "创建文章任务")
    public BaseResponse<String> createArticle(@RequestBody ArticleCreateRequest request, HttpServletRequest httpServletRequest) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getTopic() == null || request.getTopic().trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "选题不能为空");

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(httpServletRequest);

        // 1. 同步操作：在数据库中创建任务记录，并获取任务 ID
        String taskId = articleService.createArticleTask(request.getTopic(), loginUser);

        // 2. 异步操作：触发后台 AI 智能体生成流程（非阻塞）
        articleAsyncService.executeArticleGeneration(taskId, request.getTopic());

        // 3. 立即返回任务 ID 给前端
        return ResultUtils.success(taskId);
    }

    /**
     * 建立 SSE (Server-Sent Events) 长连接。
     * 前端通过任务 ID 订阅该接口，以实时接收 AI 生成过程中的进度消息和流式文本。
     */
    @GetMapping("/progress/{taskId}")
    @Operation(summary = "获取文章生成进度(SSE)")
    public SseEmitter getProgress(@PathVariable String taskId, HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "任务ID不能为空");

        // 1. 权限与安全校验：确保任务存在且当前用户有权访问
        User loginUser = userService.getLoginUser(httpServletRequest);
        articleService.getArticleDetail(taskId, loginUser);

        // 2. 通过管理器创建一个 SSE 连接实例
        SseEmitter emitter = sseEmitterManager.createEmitter(taskId);

        log.info("SSE 进度推送连接已建立, taskId={}", taskId);
        return emitter;
    }

    /**
     * 获取文章完整详情。
     */
    @GetMapping("/{taskId}")
    @Operation(summary = "获取文章详情")
    @AuthCheck(mustRole = "user")
    public BaseResponse<ArticleVO> getArticle(@PathVariable String taskId,
                                              HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(taskId == null || taskId.trim().isEmpty(),
                ErrorCode.PARAMS_ERROR, "任务ID不能为空");

        User loginUser = userService.getLoginUser(httpServletRequest);
        // 调用业务层获取文章详情（内部包含权限校验）
        ArticleVO articleVO = articleService.getArticleDetail(taskId, loginUser);

        return ResultUtils.success(articleVO);
    }

    /**
     * 分页查询当前用户的文章列表。
     */
    @PostMapping("/list")
    @Operation(summary = "分页查询文章列表")
    @AuthCheck(mustRole = "user")
    public BaseResponse<Page<ArticleVO>> listArticle(@RequestBody ArticleQueryRequest request,
                                                     HttpServletRequest httpServletRequest) {
        User loginUser = userService.getLoginUser(httpServletRequest);
        // 执行分页查询逻辑
        Page<ArticleVO> articleVOPage = articleService.listArticleByPage(request, loginUser);

        return ResultUtils.success(articleVOPage);
    }

    /**
     * 删除指定的文章。
     */
    @PostMapping("/delete")
    @Operation(summary = "删除文章")
    @AuthCheck(mustRole = "user")
    public BaseResponse<Boolean> deleteArticle(@RequestBody DeleteRequest deleteRequest,
                                               HttpServletRequest httpServletRequest) {
        ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() == null,
                ErrorCode.PARAMS_ERROR);

        User loginUser = userService.getLoginUser(httpServletRequest);
        // 执行逻辑删除
        boolean result = articleService.deleteArticle(deleteRequest.getId(), loginUser);

        return ResultUtils.success(result);
    }
}
