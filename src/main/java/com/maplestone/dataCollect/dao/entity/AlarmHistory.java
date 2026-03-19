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
 * 告警历史实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("alarm_history")
public class AlarmHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;
    private Integer stationId;
    private Integer pointId;
    private Date alarmTime;
    private Double alarmValue;
    private String alarmLevel;
    private String alarmMessage;
    private Date createdAt;
}
