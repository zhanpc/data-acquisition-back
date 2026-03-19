package com.maplestone.dataCollect.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maplestone.dataCollect.dao.entity.AlarmHistory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警历史 Mapper
 */
@Mapper
public interface AlarmHistoryMapper extends BaseMapper<AlarmHistory> {
}
