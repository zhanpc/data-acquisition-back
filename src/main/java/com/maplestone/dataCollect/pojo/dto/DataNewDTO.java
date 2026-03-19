package com.maplestone.dataCollect.pojo.dto;

import lombok.Data;

import java.util.List;

@Data
public class DataNewDTO {
    private String createTime;
    private List<DataDTO> dataDTOList;
}
