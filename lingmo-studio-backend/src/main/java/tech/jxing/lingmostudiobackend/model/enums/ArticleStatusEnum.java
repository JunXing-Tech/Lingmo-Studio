package tech.jxing.lingmostudiobackend.model.enums;

import lombok.Getter;

/**
 * 文章生成任务的总体执行状态枚举。
 * 用于描述一个异步生成任务从启动到结束的宏观状态。
 */
@Getter
public enum ArticleStatusEnum {

    /** 任务已创建，等待进入线程池执行 */
    PENDING("PENDING", "等待处理"),
    
    /** 任务正在执行中（可能处于标题生成、大纲生成或正文生成等子阶段） */
    PROCESSING("PROCESSING", "处理中"),
    
    /** 任务顺利完成，最终文章内容已持久化 */
    COMPLETED("COMPLETED", "已完成"),
    
    /** 任务执行过程中发生异常，执行中断 */
    FAILED("FAILED", "失败");

    /**
     * 状态的内部持久化值（存储到数据库的字符串）
     */
    private final String value;

    /**
     * 状态的友好描述文字
     */
    private final String description;

    ArticleStatusEnum(String value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据状态字符串值获取对应的枚举实例。
     *
     * @param value 状态值
     * @return 枚举实例，未找到则返回 null
     */
    public static ArticleStatusEnum getByValue(String value) {
        if (value == null) {
            return null;
        }
        for (ArticleStatusEnum statusEnum : values()) {
            if (statusEnum.getValue().equals(value)) {
                return statusEnum;
            }
        }
        return null;
    }
}