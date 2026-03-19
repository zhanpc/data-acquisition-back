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
 * 场站配置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("station_config")
public class StationConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer stationId;
    private String stationName;
    private String protocol;
    private String host;
    private Integer port;
    private Integer status;
    private String extraParams; // JSON格式，存协议特有参数，如 RTU 的串口配置
    private Date createdAt;
    private Date updatedAt;
}
