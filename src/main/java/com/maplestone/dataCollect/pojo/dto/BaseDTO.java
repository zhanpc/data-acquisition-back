package com.maplestone.dataCollect.pojo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @description:
 * @Author hmx
 * @CreateTime 2021-07-14 18:26
 */
@Data
@Schema(name = "BaseDTO", description = "查询基类")
@NoArgsConstructor
public class BaseDTO {
    @Schema(name = "page", description = "当前页")
    private int page = 1;

    @Schema(name = "size", description = "页大小")
    private int size = 10;

    @Schema(name = "like", description = "搜索内容")
    private String like;

    public static final int MAX_SIZE = 1000; // 限制最大页大小

    public int getPage() {
        return page;
    }

    public int getSize() {
        return Math.min(size, MAX_SIZE);
    }

    public BaseDTO(int page, int size) {
        this.page = page;
        this.size = size;
    }

}
