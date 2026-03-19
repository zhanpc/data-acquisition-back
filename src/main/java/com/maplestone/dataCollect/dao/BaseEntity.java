package com.maplestone.dataCollect.dao;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.io.Serializable;
import java.util.Date;

/**
 * @author
 * @date
 */
@Data
@Schema(name = "BaseEntity", description = "入库基类")
public class BaseEntity implements Serializable {

    private static final long serialVersionUID = -1342729126453968851L;

    @Id
    @TableId(type = IdType.ASSIGN_UUID)
    @Schema(name = "id", description = "主键")
    private String id;

    @Schema(name = "createdUser", description = "创建人")
    private String createdUser;

    @Schema(name = "createdTime", description = "创建时间")
    private Date createdTime;

    @Schema(name = "lastModifiedUser", description = "修改人")
    private String lastModifiedUser;

    @Schema(name = "lastModifiedTime", description = "修改时间")
    private Date lastModifiedTime;
}
