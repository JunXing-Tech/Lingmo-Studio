package tech.jxing.lingmostudiobackend.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import tech.jxing.lingmostudiobackend.manager.SseEmitterManager;
import tech.jxing.lingmostudiobackend.model.dto.article.ArticleState;
import tech.jxing.lingmostudiobackend.model.enums.ArticleStatusEnum;
import tech.jxing.lingmostudiobackend.model.enums.SseMessageTypeEnum;
import tech.jxing.lingmostudiobackend.utils.GsonUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class ArticleAsyncService {

    @Resource
    private ArticleAgentService articleAgentService;

    @Resource
    private SseEmitterManager sseEmitterManager;

    @Resource
    private ArticleService articleService;

    /**
     * 异步执行文章生成的全流程编排。
     *
     * @param taskId 任务唯一标识 ID
     * @param topic  用户输入的选题
     */
    @Async("articleExecutor")
    public void executeArticleGeneration(String taskId, String topic) {
        log.info("异步文章生成任务开始执行, taskId={}, topic={}", taskId, topic);
        
        try {
            // 1. 初始化阶段：将数据库中的文章状态更新为“处理中”
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.PROCESSING, null);
            
            // 2. 环境准备：创建任务状态上下文对象，用于在各个智能体之间传递数据
            ArticleState state = new ArticleState();
            state.setTaskId(taskId);
            state.setTopic(topic);
            
            // 3. 核心生成阶段：调用智能体编排服务
            // 传入一个回调函数 handlesAgentMessage，每当 AI 产生流式输出或进度更新时，都会触发该回调并通过 SSE 推送给前端
            articleAgentService.executeArticleGeneration(state, message -> {
                handleAgentMessage(taskId, message, state);
            });
            
            // 4. 持久化阶段：AI 生成完毕，将文章内容、标题、配图等完整信息保存到数据库
            articleService.saveArticleContent(taskId, state);
            
            // 5. 状态收尾阶段：更新数据库状态为“已完成”
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.COMPLETED, null);
            
            // 6. 实时通知阶段：通过 SSE 发送“全部完成”的信号，告知前端生成结束
            sendSseMessage(taskId, SseMessageTypeEnum.ALL_COMPLETE, Map.of("taskId", taskId));
            
            // 7. 资源释放阶段：关闭该任务对应的 SSE 连接通道
            sseEmitterManager.complete(taskId);
            
            log.info("异步文章生成任务顺利完成, taskId={}", taskId);
        } catch (Exception e) {
            // 异常处理机制：确保在任何步骤出错时，都能正确更新状态并通知用户
            log.error("异步任务执行发生异常, taskId={}", taskId, e);
            
            // 将数据库状态更新为“失败”，并记录错误原因
            articleService.updateArticleStatus(taskId, ArticleStatusEnum.FAILED, e.getMessage());
            
            // 通过 SSE 推送错误信息给前端，以便展示给用户
            sendSseMessage(taskId, SseMessageTypeEnum.ERROR, Map.of("message", e.getMessage()));
            
            // 即使失败，也需要关闭 SSE 通道，防止连接泄漏
            sseEmitterManager.complete(taskId);
        }
    }

    /**
     * 处理并推送来自智能体的原始流式消息。
     * 该方法作为回调函数，被 ArticleAgentService 频繁调用。
     * 它负责将 AI 生成的原始文本片段或进度指令，包装成结构化的 JSON 数据，并通过 SSE 发送给前端。
     *
     * @param taskId  任务唯一标识 ID
     * @param message 智能体产生的原始消息字符串（通常包含特定前缀）
     * @param state   当前文章生成的中间状态上下文
     */
    private void handleAgentMessage(String taskId, String message, ArticleState state) {
        // 1. 将原始字符串消息解析并构建为包含类型、内容和元数据的 Map 结构
        Map<String, Object> data = buildMessageData(message, state);
        
        // 2. 如果消息有效（不为空），则将其序列化为 JSON 字符串并通过 SSE 管理器推送
        if (data != null) {
            // 使用 Gson 将 Map 转换为 JSON 格式字符串发送
            sseEmitterManager.send(taskId, GsonUtils.toJson(data));
        }
    }

    /**
     * 根据智能体返回的原始消息内容，构建用于 SSE 推送的结构化数据。
     * 该方法负责识别消息的“前缀”，从而判断消息的类型（是流式文本、配图完成还是阶段结束）。
     *
     * @param message 原始消息字符串
     * @param state   当前文章生成的中间状态
     * @return 包含类型和内容的 Map 对象，若无法识别则返回 null
     */
    private Map<String, Object> buildMessageData(String message, ArticleState state) {
        // 1. 获取不同类型消息的识别前缀（如 "AGENT2_STREAMING:", "IMAGE_COMPLETE:" 等）
        String streamingPrefix2 = SseMessageTypeEnum.AGENT2_STREAMING.getStreamingPrefix();
        String streamingPrefix3 = SseMessageTypeEnum.AGENT3_STREAMING.getStreamingPrefix();
        String imageCompletePrefix = SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix();

        // 2. 识别并处理：智能体2（生成大纲）的流式文本片段
        if (message.startsWith(streamingPrefix2)) {
            return buildStreamingData(SseMessageTypeEnum.AGENT2_STREAMING,
                    message.substring(streamingPrefix2.length()));
        }

        // 3. 识别并处理：智能体3（生成正文）的流式文本片段
        if (message.startsWith(streamingPrefix3)) {
            return buildStreamingData(SseMessageTypeEnum.AGENT3_STREAMING,
                    message.substring(streamingPrefix3.length()));
        }

        // 4. 识别并处理：单张配图检索/生成完成的消息
        if (message.startsWith(imageCompletePrefix)) {
            // 提取前缀之后的 JSON 内容并解析
            String imageJson = message.substring(imageCompletePrefix.length());
            return buildImageCompleteData(imageJson);
        }

        // 5. 如果不带特殊前缀，则视为“阶段性完成”消息（如 AGENT1_COMPLETE）
        // 这类消息通常用于通知前端某个步骤已彻底结束，并同步该阶段产生的所有数据
        return buildCompleteMessageData(message, state);
    }

    /**
     * 构建用于流式文本传输的消息对象。
     * 将 AI 生成的字符片段包装成统一的 JSON 结构，便于前端解析并实现打字机效果。
     *
     * @param type    消息类型（如大纲流或正文流）
     * @param content 当前产生的文本内容片段
     * @return 包含类型和内容的 Map 结构
     */
    private Map<String, Object> buildStreamingData(SseMessageTypeEnum type, String content) {
        Map<String, Object> data = new HashMap<>();
        // 消息类型标识，前端根据此标识决定将 content 渲染到哪个区域
        data.put("type", type.getValue());
        // 实际的文本片段
        data.put("content", content);
        return data;
    }

    /**
     * 构建单张配图完成后的推送数据对象。
     * 将智能体返回的图片 JSON 字符串解析为 Java 对象，并包装成统一的 SSE 消息格式。
     *
     * @param imageJson 包含图片详情（URL、位置、描述等）的 JSON 字符串
     * @return 包含类型标识和图片对象的 Map 结构
     */
    private Map<String, Object> buildImageCompleteData(String imageJson) {
        Map<String, Object> data = new HashMap<>();
        // 设置消息类型为图片完成
        data.put("type", SseMessageTypeEnum.IMAGE_COMPLETE.getValue());
        // 将 JSON 字符串反序列化为 ImageResult 对象，确保发送给前端的是结构化数据而非原始字符串
        data.put("image", GsonUtils.fromJson(imageJson, ArticleState.ImageResult.class));
        return data;
    }

    /**
     * 构建阶段性任务完成的消息数据。
     * 当某个智能体（Agent）完成其所有工作时，该方法会从任务状态（state）中抓取该阶段生成的完整数据，
     * 并封装成统一的 SSE 消息包发送给前端，以便前端进行最终的界面同步。
     *
     * @param message 智能体发送的原始完成指令（如 "AGENT1_COMPLETE"）
     * @param state   当前文章生成的完整状态上下文
     * @return 包含阶段结果数据的 Map 结构，若指令无法匹配则返回 null
     */
    private Map<String, Object> buildCompleteMessageData(String message, ArticleState state) {
        Map<String, Object> data = new HashMap<>();

        // 1. 智能体1完成：同步生成的标题（主标题和副标题）
        if (SseMessageTypeEnum.AGENT1_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT1_COMPLETE.getValue());
            data.put("title", state.getTitle());
        } 
        // 2. 智能体2完成：同步生成的文章大纲列表
        else if (SseMessageTypeEnum.AGENT2_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT2_COMPLETE.getValue());
            data.put("outline", state.getOutline().getSections());
        } 
        // 3. 智能体3完成：正文生成结束（正文已在流式输出中实时推送，此处仅发送完成信号）
        else if (SseMessageTypeEnum.AGENT3_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT3_COMPLETE.getValue());
        } 
        // 4. 智能体4完成：同步分析出的配图需求列表
        else if (SseMessageTypeEnum.AGENT4_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT4_COMPLETE.getValue());
            data.put("imageRequirements", state.getImageRequirements());
        } 
        // 5. 智能体5完成：同步最终获取到的所有图片结果
        else if (SseMessageTypeEnum.AGENT5_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.AGENT5_COMPLETE.getValue());
            data.put("images", state.getImages());
        } 
        // 6. 图文合成完成：同步最终生成的图文并茂的文章全内容
        else if (SseMessageTypeEnum.MERGE_COMPLETE.getValue().equals(message)) {
            data.put("type", SseMessageTypeEnum.MERGE_COMPLETE.getValue());
            data.put("fullContent", state.getFullContent());
        } 
        // 7. 未知指令：返回 null，不进行推送
        else {
            return null;
        }

        return data;
    }

    /**
     * 向前端发送统一格式的 SSE 消息。
     * 该方法封装了消息的构建过程，确保每条推送消息都包含类型标识，并能灵活携带额外数据。
     *
     * @param taskId         任务唯一标识 ID
     * @param type           消息类型枚举
     * @param additionalData 需要携带的额外业务数据
     */
    private void sendSseMessage(String taskId, SseMessageTypeEnum type, Map<String, Object> additionalData) {
        // 创建用于推送的数据容器
        Map<String, Object> data = new HashMap<>();
        // 1. 设置消息类型，这是前端路由处理的关键依据
        data.put("type", type.getValue());
        // 2. 将传入的额外业务数据合并到推送包中
        if (additionalData != null) {
            data.putAll(additionalData);
        }
        // 3. 通过 SSE 管理器将数据对象序列化为 JSON 并发送给指定的客户端
        sseEmitterManager.send(taskId, GsonUtils.toJson(data));
    }

}
