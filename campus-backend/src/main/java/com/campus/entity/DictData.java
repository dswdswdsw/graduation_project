package com.campus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("dict_data")
public class DictData extends BaseEntity {

    private Long typeId;
    private String dataLabel;
    private String dataValue;
    private Integer sort;
    private Integer status;
    private String remark;
}
