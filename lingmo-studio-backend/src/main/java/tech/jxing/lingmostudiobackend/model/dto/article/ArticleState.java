package tech.jxing.lingmostudiobackend.model.dto.article;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 文章生成状态及结果对象
 */
@Data
public class ArticleState implements Serializable {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 选题
     */
    private String topic;

    /**
     * 标题结果（智能体1输出）
     */
    private TitleResult title;

    /**
     * 大纲结果（智能体2输出）
     */
    private OutlineResult outline;

    /**
     * 正文内容（智能体3输出）
     */
    private String content;

    /**
     * 配图需求列表（智能体4输出）
     */
    private List<ImageRequirement> imageRequirements;

    /**
     * 封面图 URL（单独存储，同时 images 列表中的 position=1 也是封面图）
     */
    private String coverImage;

    /**
     * 配图结果列表（智能体5输出）
     */
    private List<ImageResult> images;

    /**
     * 完整图文内容（合成后）
     */
    private String fullContent;

    private static final long serialVersionUID = 1L;

    /**
     * 标题结果
     */
    @Data
    public static class TitleResult implements Serializable {
        /** 主标题 */
        private String mainTitle;
        /** 副标题 /引言 */
        private String subTitle;
    }

    /**
     * 大纲结果
     */
    @Data
    public static class OutlineResult implements Serializable {
        /** 章节列表 */
        private List<OutlineSection> sections;
    }

    /**
     * 大纲章节
     */
    @Data
    public static class OutlineSection implements Serializable {
        /** 章节序号 */
        private Integer section;
        /** 章节标题 */
        private String title;
        /** 章节核心要点列表 */
        private List<String> points;
    }

    /**
     * 配图需求
     */
    @Data
    public static class ImageRequirement implements Serializable {
        /** 配图位置序号 */
        private Integer position;
        /** 配图类型 */
        private String type;
        /** 章节标题 */
        private String sectionTitle;
        /** 关键词/提示词 */
        private String keywords;
    }

    /**
     * 配图结果
     */
    @Data
    public static class ImageResult implements Serializable {
        /** 配图位置序号 */
        private Integer position;
        /** 图片 URL */
        private String url;
        /** 获取方式 */
        private String method;
        /** 关键词 */
        private String keywords;
        /** 章节标题 */
        private String sectionTitle;
        /** 图片描述 */
        private String description;
    }
}
