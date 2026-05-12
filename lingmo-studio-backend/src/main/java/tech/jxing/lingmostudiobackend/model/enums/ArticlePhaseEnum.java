package tech.jxing.lingmostudiobackend.model.enums;

import lombok.Getter;

/**
 * 文章生成阶段枚举类。
 * 定义了文章从选题到最终生成的完整生命周期，并规定了各个阶段之间的流转规则（状态机）。
 */
@Getter
public enum ArticlePhaseEnum {

    /** 等待开始 */
    PENDING("PENDING", "等待处理"),
    
    /** 智能体1正在工作：生成备选标题 */
    TITLE_GENERATING("TITLE_GENERATING", "生成标题中"),
    
    /** 标题已生成，等待用户从中选择一个 */
    TITLE_SELECTING("TITLE_SELECTING", "等待选择标题"),
    
    /** 智能体2正在工作：根据选定标题生成文章大纲 */
    OUTLINE_GENERATING("OUTLINE_GENERATING", "生成大纲中"),
    
    /** 大纲已生成，等待用户查看或微调大纲内容 */
    OUTLINE_EDITING("OUTLINE_EDITING", "等待编辑大纲"),
    
    /** 智能体3正在工作：根据大纲扩写正文内容 */
    CONTENT_GENERATING("CONTENT_GENERATING", "生成正文中");

    /**
     * 阶段的内部唯一标识值
     */
    private final String value;

    /**
     * 阶段的友好描述文字，用于前端 UI 展示
     */
    private final String description;

    ArticlePhaseEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据阶段值（String）获取对应的枚举实例。
     *
     * @param value 阶段值
     * @return 枚举实例，若未找到则返回 null
     */
    public static ArticlePhaseEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ArticlePhaseEnum phaseEnum : values()) {
            if (phaseEnum.getValue().equals(value)) {
                return phaseEnum;
            }
        }
        return null;
    }

    /**
     * 核心业务逻辑：校验当前阶段是否允许转换到目标阶段。
     * 方法定义了文章生成的严谨工作流，防止状态跳变或逆向操作。
     *
     * @param targetPhase 想要转换到的下一个阶段
     * @return true 表示允许转换；false 表示转换非法
     */
    public boolean canTransitionTo(ArticlePhaseEnum targetPhase) {
        if (targetPhase == null) {
            return false;
        }

        return switch (this) {
            // 等待处理 -> 只能进入生成标题阶段
            case PENDING -> targetPhase == TITLE_GENERATING;
            // 标题生成中 -> 完成后进入标题选择阶段
            case TITLE_GENERATING -> targetPhase == TITLE_SELECTING;
            // 标题已选定 -> 接下来生成大纲
            case TITLE_SELECTING -> targetPhase == OUTLINE_GENERATING;
            // 大纲生成中 -> 完成后进入大纲编辑阶段
            case OUTLINE_GENERATING -> targetPhase == OUTLINE_EDITING;
            // 大纲确认/编辑完 -> 最后进入正文生成阶段
            case OUTLINE_EDITING -> targetPhase == CONTENT_GENERATING;
            // 正文生成已经是目前的最终环节
            case CONTENT_GENERATING -> false; 
        };
    }
}