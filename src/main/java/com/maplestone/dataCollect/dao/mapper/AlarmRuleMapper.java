package com.maplestone.dataCollect.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.maplestone.dataCollect.dao.entity.AlarmRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 告警规则 Mapper
 */
@Mapper
public interface AlarmRuleMapper extends BaseMapper<AlarmRule> {
}
