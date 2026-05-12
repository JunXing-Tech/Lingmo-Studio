package tech.jxing.lingmostudiobackend.model.dto.article;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tech.jxing.lingmostudiobackend.common.PageRequest;

import java.io.Serializable;

/**
 * 查询文章请求
 */
@EqualsAndHashCode(callSuper = false)
@Data
public class ArticleQueryRequest extends PageRequest implements Serializable {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 状态
     */
    private String status;

    private static final long serialVersionUID = 1L;
}