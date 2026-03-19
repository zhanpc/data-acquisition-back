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
 * 测点配置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("point_config")
public class PointConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer stationId;
    private Integer pointId;
    private String pointName;
    private String dataType;
    private String unit;
    private String tableName;
    private Integer ioa;
    private Integer address;
    private String registerType;
    private Date createdAt;
    private Date updatedAt;
}
