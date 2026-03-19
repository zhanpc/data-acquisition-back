package com.maplestone.dataCollect.pojo.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 数据点实体 - 统一的数据采集模型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer stationId;
    private Integer pointId;
    private String pointName;
    private Long timestamp;
    private Double value;
    private Integer quality;
    private String tableName;

    private Integer coa;
    private Integer ioa;
    private Integer typeId;

    private Integer slaveId;
    private Integer address;
    private String registerType;
}
