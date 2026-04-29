package com.campus.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 实体基类 - 提供所有实体的公共字段
 * 
 * <p>使用MyBatis-Plus的自动填充功能，在插入和更新数据时自动处理时间戳和逻辑删除字段。
 * 所有业务实体类都应该继承此基类以获得统一的数据管理能力。</p>
 * 
 * <h2>包含的公共字段：</h2>
 * <ul>
 *   <li><b>id</b> - 主键ID，自增策略</li>
 *   <li><b>createTime</b> - 创建时间，插入时自动填充</li>
 *   <li><b>updateTime</b> - 更新时间，插入和更新时自动填充</li>
 *   <li><b>deleted</b> - 逻辑删除标记（0:未删除, 1:已删除）</li>
 * </ul>
 * 
 * <h2>设计模式：</h2>
 * <p>采用模板方法模式的变体，通过继承实现代码复用，
 * 确保所有实体都具有一致的基础字段和行为。</p>
 * 
 * @see MyMetaObjectHandler 自动填充处理器
 */
@Data
public class BaseEntity {

    /**
     * 主键ID
     * 使用数据库自增策略生成唯一标识符
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 创建时间
     * 在执行INSERT操作时自动填充当前时间
     * @TableField(fill = FieldFill.INSERT) 表示仅在插入时填充
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 更新时间
     * 在执行INSERT和UPDATE操作时都会自动更新为当前时间
     * @TableField(fill = FieldFill.INSERT_UPDATE) 表示插入和更新时都填充
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 逻辑删除标记
     * 0表示未删除（正常状态），1表示已删除
     * 使用@TableLogic注解实现逻辑删除，查询时会自动过滤已删除记录
     */
    @TableLogic
    private Integer deleted;
}
