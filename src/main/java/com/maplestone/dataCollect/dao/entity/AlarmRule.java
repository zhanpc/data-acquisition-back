package com.maplestone.dataCollect.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * 告警规则实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("alarm_rule")
public class AlarmRule implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer stationId;
    private Integer pointId;
    private Double upperLimit;
    private Double lowerLimit;
    private String alarmLevel;
    private Integer enabled;
    private Date createdAt;
    private Date updatedAt;
}
