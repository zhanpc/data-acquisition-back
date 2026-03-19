package com.maplestone.dataCollect.pojo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(name = "TreeNode", description = "结构树节点")
public class TreeNode {
    @Schema(name = "id", description = "id")
    private String id;
    @Schema(name = "name", description = "名称")
    private String name;
    @Schema(name = "name", description = "模型名称")
    private String modelName;
    @Schema(name = "hasChildren", description = "是否有子元素")
    private Boolean hasChildren;
    @Schema(name = "isBind", description = "是否绑定 第二级  节段信息")
    private Boolean isBind;
    @Schema(name = "isDetail", description = "是否绑定 第三级  焊缝信息")
    private Boolean isDetail;
    @Schema(name = "progress", description = "进度 第二级 节段信息")
    private Double progress;
    private List<TreeNode> nodes;

    public TreeNode(String id, String name) {
        this.id = id;
        this.name = name;
        this.nodes = new ArrayList<>();
    }
}
