package tech.jxing.lingmostudiobackend.service;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import tech.jxing.lingmostudiobackend.constant.PromptConstant;
import tech.jxing.lingmostudiobackend.model.dto.article.ArticleState;
import tech.jxing.lingmostudiobackend.model.enums.ImageMethodEnum;
import tech.jxing.lingmostudiobackend.model.enums.SseMessageTypeEnum;
import tech.jxing.lingmostudiobackend.utils.GsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * 智能体调用逻辑
 */
@Service
@Slf4j
public class ArticleAgentService {

    @Resource
    private DashScopeChatModel chatModel;

    @Resource
    private ImageSearchService imageSearchService;

    @Resource
    private CosService cosService;

    /**
     * 智能体执行完整的文章生成流程
     *
     * @param state 生成文章状态
     * @param streamHandler 文章生成流式处理
     */
    public void executeArticleGeneration(ArticleState state, Consumer<String> streamHandler) {
        try {
            // 智能体1：生成文章标题
            log.info("智能体1开始生成标题, taskId={}", state.getTaskId());
            // 调用标题生成逻辑，结果将存入 state 对象
            agent1GenerateTitle(state);
            // 通过 streamHandler 推送“标题生成完成”的消息到前端
            streamHandler.accept(SseMessageTypeEnum.AGENT1_COMPLETE.getValue());

            // 智能体2：生成大纲（流式输出）
            log.info("智能体2：开始生成大纲, taskId={}", state.getTaskId());
            agent2GenerateOutline(state, streamHandler);
            streamHandler.accept(SseMessageTypeEnum.AGENT2_COMPLETE.getValue());

            // 智能体3：生成正文（流式输出）
            log.info("智能体3：开始生成正文, taskId={}", state.getTaskId());
            agent3GenerateContent(state, streamHandler);
            streamHandler.accept(SseMessageTypeEnum.AGENT3_COMPLETE.getValue());

            // 智能体4：分析配图需求
            log.info("智能体4：开始分析配图需求, taskId={}", state.getTaskId());
            agent4AnalyzeImageRequirements(state);
            streamHandler.accept(SseMessageTypeEnum.AGENT4_COMPLETE.getValue());

            // 智能体5：生成配图
            log.info("智能体5：开始生成配图, taskId={}", state.getTaskId());
            agent5GenerateImages(state, streamHandler);
            streamHandler.accept(SseMessageTypeEnum.AGENT5_COMPLETE.getValue());

            // 图文合成
            log.info("开始图文合成, taskId={}", state.getTaskId());
            mergeImagesIntoContent(state);
            streamHandler.accept(SseMessageTypeEnum.MERGE_COMPLETE.getValue());

            log.info("文章生成完成, taskId={}", state.getTaskId());
        } catch (Exception e) {
            log.error("文章生成失败, taskId={}", state.getTaskId(), e);
            throw new RuntimeException("文章生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 智能体1：生成标题
     * @param state 文章生成状态
     */
    private void agent1GenerateTitle(ArticleState state) {
        // 1. 准备 Prompt：从常量类获取模板，并将 {topic} 替换为用户输入的选题
        String prompt = PromptConstant.AGENT1_TITLE_PROMPT
                .replace("{topic}", state.getTopic());
        // 2. 调用 LLM：使用非流式方式请求 AI
        String content = callLLM(prompt);
        // 3. 解析 JSON：AI 返回的是 JSON 字符串，将其解析为 TitleResult 实体类
        ArticleState.TitleResult titleResult = parseJsonResponse(content, ArticleState.TitleResult.class, "标题");
        // 4. 更新状态：将生成的主副标题存入全局状态对象 state 中，供后续步骤使用
        state.setTitle(titleResult);
        log.info("智能体1：标题生成成功, mainTitle={}", titleResult.getMainTitle());
    }

    /**
     * 智能体2：根据标题生成文章大纲。
     * 使用流式调用 LLM，并将生成的大纲解析为结构化对象存储在状态中。
     *
     * @param state         文章生成状态上下文
     * @param streamHandler 用于实时推送大纲生成进度的流式处理器
     */
    private void agent2GenerateOutline(ArticleState state, Consumer<String> streamHandler) {
        // 1. 填充提示词模板，注入主标题和副标题
        String prompt = PromptConstant.AGENT2_OUTLINE_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{subTitle}", state.getTitle().getSubTitle());

        // 2. 流式调用 LLM，并获取最终完整的响应内容
        String content = callLlmWithStreaming(prompt, streamHandler, SseMessageTypeEnum.AGENT2_STREAMING);

        // 3. 将 LLM 返回的 JSON 字符串解析为大纲结果对象
        ArticleState.OutlineResult outlineResult = parseJsonResponse(content, ArticleState.OutlineResult.class, "大纲");

        // 4. 更新状态上下文中的大纲信息
        state.setOutline(outlineResult);

        log.info("智能体2：大纲生成成功, sections={}", outlineResult.getSections().size());
    }

    /**
     * 智能体3：根据生成的大纲编写文章正文。
     * 采用流式输出，将生成的正文内容实时推送给用户。
     *
     * @param state         文章生成状态上下文，包含已生成的标题和大纲
     * @param streamHandler 用于实时推送正文生成进度的流式处理器
     */
    private void agent3GenerateContent(ArticleState state, Consumer<String> streamHandler) {
        // 1. 将大纲章节列表转换为 JSON 字符串，以便嵌入到提示词中
        String outlineText = GsonUtils.toJson(state.getOutline().getSections());

        // 2. 填充提示词模板，注入标题和大纲信息
        String prompt = PromptConstant.AGENT3_CONTENT_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{subTitle}", state.getTitle().getSubTitle())
                .replace("{outline}", outlineText);

        // 3. 流式调用 LLM 进行正文创作，并获取最终完整的文章内容
        String content = callLlmWithStreaming(prompt, streamHandler, SseMessageTypeEnum.AGENT3_STREAMING);

        // 4. 将生成的正文保存到状态上下文中
        state.setContent(content);

        log.info("智能体3：正文生成成功, length={}", content.length());
    }

    /**
     * 智能体4：分析文章内容并生成配图需求。
     * 该方法会分析文章的主题和各章节内容，确定哪些地方需要插入图片，并给出每张图片的搜索关键词或生成描述。
     *
     * @param state 文章生成状态上下文，包含已生成的正文
     */
    private void agent4AnalyzeImageRequirements(ArticleState state) {
        // 1. 构造提示词，将主标题和文章正文注入到配图分析模板中
        String prompt = PromptConstant.AGENT4_IMAGE_REQUIREMENTS_PROMPT
                .replace("{mainTitle}", state.getTitle().getMainTitle())
                .replace("{content}", state.getContent());

        // 2. 调用 LLM（非流式），获取配图需求的 JSON 列表字符串
        String content = callLLM(prompt);

        // 3. 将返回的 JSON 字符串解析为结构化的配图需求列表
        // 使用 TypeToken 处理泛型集合的解析
        List<ArticleState.ImageRequirement> imageRequirements = parseJsonListResponse(
                content,
                new TypeToken<List<ArticleState.ImageRequirement>>(){},
                "配图需求"
        );

        // 4. 将分析出的配图需求保存到状态上下文中，供后续步骤使用
        state.setImageRequirements(imageRequirements);

        log.info("智能体4：配图需求分析成功, count={}", imageRequirements.size());
    }

    /**
     * 智能体5：根据配图需求生成/搜索配图。
     * 该方法采用串行方式，逐个处理配图需求，并具备自动降级策略。
     * 每处理完一张图片，都会通过 streamHandler 实时通知前端。
     *
     * @param state         文章生成状态上下文，包含配图需求列表
     * @param streamHandler 用于实时推送配图完成进度的流式处理器
     */
    private void agent5GenerateImages(ArticleState state, Consumer<String> streamHandler) {
        // 用于存储所有成功的配图结果
        List<ArticleState.ImageResult> imageResults = new ArrayList<>();

        // 遍历智能体4分析出的每一个配图需求
        for (ArticleState.ImageRequirement requirement : state.getImageRequirements()) {
            log.info("智能体5：开始检索配图, position={}, keywords={}",
                    requirement.getPosition(), requirement.getKeywords());

            // 1. 调用图片检索服务（如 Pexels）尝试搜索图片
            String imageUrl = imageSearchService.searchImage(requirement.getKeywords());

            // 2. 降级策略处理
            ImageMethodEnum method = imageSearchService.getMethod();
            if (imageUrl == null) {
                // 如果主搜索服务失败或无结果，使用备选方案
                imageUrl = imageSearchService.getFallbackImage(requirement.getPosition());
                method = ImageMethodEnum.PICSUM;
                log.warn("智能体5：图片检索失败, 使用降级方案, position={}", requirement.getPosition());
            }

            // 3. 处理图片 URL（当前版本直接使用原始 URL，不上传到 COS）
            String finalImageUrl = cosService.useDirectUrl(imageUrl);

            // 4. 构造配图结果对象，包含图片 URL、获取方式等信息
            ArticleState.ImageResult imageResult = buildImageResult(requirement, finalImageUrl, method);
            imageResults.add(imageResult);

            // 5. 实时推送进度：每完成一张配图，就向前端发送一个 IMAGE_COMPLETE 消息
            String imageCompleteMessage = SseMessageTypeEnum.IMAGE_COMPLETE.getStreamingPrefix() + GsonUtils.toJson(imageResult);
            streamHandler.accept(imageCompleteMessage);

            log.info("智能体5：配图检索成功, position={}, method={}",
                    requirement.getPosition(), method.getValue());
        }

        // 6. 将所有配图结果保存到状态上下文中
        state.setImages(imageResults);
        log.info("智能体5：所有配图生成完成, count={}", imageResults.size());
    }

    /**
     * 图文合成：将之前生成的配图插入到正文对应的章节位置。
     * 该方法会遍历正文的每一行，识别 Markdown 格式的二级标题（## ），并在其下方插入对应的图片。
     *
     * @param state 文章生成状态上下文，包含生成的正文和配图结果
     */
    private void mergeImagesIntoContent(ArticleState state) {
        String content = state.getContent();
        List<ArticleState.ImageResult> images = state.getImages();

        // 如果没有配图，直接将原始正文设为最终内容并返回
        if (images == null || images.isEmpty()) {
            state.setFullContent(content);
            return;
        }

        StringBuilder fullContent = new StringBuilder();

        // 1. 将正文按行切分，以便逐行处理
        String[] lines = content.split("\n");
        for (String line : lines) {
            // 先将当前行内容追加到结果中
            fullContent.append(line).append("\n");

            // 2. 检查当前行是否为 Markdown 的二级标题（通常代表一个章节的开始）
            if (line.startsWith("## ")) {
                // 提取章节标题文字（去除 "## " 前缀和首尾空格）
                String sectionTitle = line.substring(3).trim();
                
                // 3. 尝试在该章节标题后插入匹配的图片
                insertImageAfterSection(fullContent, images, sectionTitle);
            }
        }

        // 4. 将合成后的完整内容保存到状态上下文中
        state.setFullContent(fullContent.toString());
        log.info("图文合成完成, fullContentLength={}", fullContent.length());
    }

    /**
     * 调用 LLM - 非流式
     *
     * @param prompt 提示
     * @return 响应结果
     */
    private String callLLM(String prompt) {
        ChatResponse response = chatModel.call(new Prompt(new UserMessage( prompt)));
        return response.getResult().getOutput().getText();
    }

    /**
     * 解析 JSON 响应
     * @param content 待解析的 JSON 字符串
     * @param clazz 目标类型的 Class 对象
     * @param name 业务名称
     * @return 解析结果
     */
    private <T> T parseJsonResponse(String content, Class<T> clazz, String name) {
        try {
            return GsonUtils.fromJson(content, clazz);
        } catch (JsonSyntaxException e) {
            log.error("{}解析失败, content={}", name, content, e);
            throw new RuntimeException(name + "解析失败");
        }
    }

    /**
     * 调用 LLM - 流式输出
     * @param prompt 提示
     * @param streamHandler 流式处理
     * @param messageType 标识当前消息的类型，并为流式输出添加统一的前缀
     * @return 响应内容
     */
    private String callLlmWithStreaming(String prompt, Consumer<String> streamHandler, SseMessageTypeEnum messageType) {
        // 用于累积完整的响应内容，以便方法最后返回
        StringBuilder contentBuilder = new StringBuilder();

        // 构造提示词并启动流式调用，返回一个响应流 (Flux)
        Flux<ChatResponse> streamResponse = chatModel.stream(new Prompt(new UserMessage(prompt)));

        // 订阅并处理流中的每一个响应块
        streamResponse
                .doOnNext(response -> {
                    // 提取当前数据块的文本内容
                    String chunk = response.getResult().getOutput().getText();
                    if (chunk != null && !chunk.isEmpty()) {
                        // 1. 累积到完整内容中
                        contentBuilder.append(chunk);
                        // 2. 通过回调函数将带有特定前缀的数据块推送出去（如 SSE）
                        streamHandler.accept(messageType.getStreamingPrefix() + chunk);
                    }
                })
                .doOnError(error -> log.error("LLM 流式调用失败, messageType={}", messageType, error))
                // 阻塞当前线程，直到流结束（最后一个元素到达），确保方法返回时已获取全部内容
                .blockLast();

        return contentBuilder.toString();
    }

    /**
     * 解析 JSON 列表格式的响应内容。
     * 将 JSON 字符串转换为指定的泛型集合类型。
     *
     * @param content   待解析的 JSON 字符串内容
     * @param typeToken 捕获复杂的泛型类型信息（如 List<T>）
     * @param name      业务名称（用于在解析失败时生成更友好的错误日志）
     * @param <T>       目标解析类型
     * @return 解析后的 Java 对象
     */
    private <T> T parseJsonListResponse(String content, TypeToken<T> typeToken, String name) {
        try {
            return GsonUtils.fromJson(content, typeToken);
        } catch (JsonSyntaxException e) {
            log.error("{}解析失败, content={}", name, content, e);
            throw new RuntimeException(name + "解析失败");
        }
    }

    /**
     * 构建单张图片的配图结果对象。
     * 将 AI 分析的需求信息与实际检索到的图片 URL、方法等信息进行封装。
     *
     * @param requirement 配图需求（包含位置、关键词、章节标题等）
     * @param imageUrl    检索到的图片公网访问 URL
     * @param method      图片获取方法（如 PEXELS 或 PICSUM 降级）
     * @return 封装好的 ImageResult 对象
     */
    private ArticleState.ImageResult buildImageResult(ArticleState.ImageRequirement requirement,
                                                      String imageUrl,
                                                      ImageMethodEnum method) {
        ArticleState.ImageResult imageResult = new ArticleState.ImageResult();
        // 设置图片在文章中的顺序位置
        imageResult.setPosition(requirement.getPosition());
        // 设置图片访问地址
        imageResult.setUrl(imageUrl);
        // 设置获取方式（用于前端展示或统计）
        imageResult.setMethod(method.getValue());
        // 记录搜索时使用的关键词
        imageResult.setKeywords(requirement.getKeywords());
        // 记录所属章节标题，用于后续的图文合成
        imageResult.setSectionTitle(requirement.getSectionTitle());
        // 设置图片描述（通常是 AI 生成的配图类型说明）
        imageResult.setDescription(requirement.getType());
        return imageResult;
    }

    /**
     * 在正文的章节标题行之后，尝试插入匹配的图片。
     * 遍历所有已生成的图片，如果图片的所属章节标题与当前正文行匹配，则按 Markdown 语法插入图片。
     *
     * @param fullContent  正在构建的文章完整内容（StringBuilder）
     * @param images       所有已生成的配图结果列表
     * @param sectionTitle 当前正文扫描到的章节标题文字
     */
    private void insertImageAfterSection(StringBuilder fullContent,
                                         List<ArticleState.ImageResult> images,
                                         String sectionTitle) {
        for (ArticleState.ImageResult image : images) {
            // 排除 position 为 1 的图片（通常是封面图，处理逻辑不同）
            // 且图片的 sectionTitle 不能为空，并且当前章节标题包含图片预设的章节标题
            if (image.getPosition() > 1 &&
                    image.getSectionTitle() != null &&
                    sectionTitle.contains(image.getSectionTitle().trim())) {
                
                // 按照 Markdown 语法格式：![描述](链接) 插入图片，并换行
                fullContent.append("\n![").append(image.getDescription())
                        .append("](").append(image.getUrl()).append(")\n");
                
                // 找到匹配的图片并插入后，跳出当前循环（一个标题后通常只插一张图）
                break;
            }
        }
    }
}
