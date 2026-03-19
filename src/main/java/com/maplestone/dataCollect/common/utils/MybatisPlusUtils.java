package com.maplestone.dataCollect.common.utils;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.maplestone.dataCollect.pojo.dto.BaseDTO;

public class MybatisPlusUtils {

    public static <T> IPage<T> getPage(BaseDTO baseDTO) {
        return new Page<>(baseDTO.getPage(), baseDTO.getSize());
    }

    public static <T> QueryWrapper<T> orderByAsc(QueryWrapper<T> queryWrapper, String column) {
        queryWrapper.orderByAsc(column);
        return queryWrapper;
    }
}
