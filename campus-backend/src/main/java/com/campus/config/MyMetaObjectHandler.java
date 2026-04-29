package com.campus.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus元数据对象处理器
 *
 * <p>实现MyBatis-Plus的MetaObjectHandler接口，用于在数据库操作时
 * 自动填充实体类的公共字段。当实体类继承BaseEntity并在字段上添加
 * @TableField(fill = FieldFill.INSERT)或
 * @TableField(fill = FieldFill.INSERT_UPDATE)注解时，
 * 本处理器会在执行插入或更新操作时自动填充对应字段的值。</p>
 *
 * <h2>自动填充的字段：</h2>
 * <ul>
 *   <li><b>createTime</b>: 数据创建时间，仅在INSERT操作时填充</li>
 *   <li><b>updateTime</b>: 数据更新时间，在INSERT和UPDATE操作时都会填充</li>
 * </ul>
 *
 * <h2>工作原理：</h2>
 * <ol>
 *   <li>实体类字段标注@TableField注解并指定fill策略</li>
 *   <li>MyBatis-Plus在执行SQL前检测到需要填充的字段</li>
 *   <li>调用本处理器的insertFill()或updateFill()方法</li>
 *   <li>通过反射机制将当前时间值设置到目标字段中</li>
 *   <li>继续执行正常的数据库操作</li>
 * </ol>
 *
 * <h2>使用示例：</h2>
 * <pre>
 * // 实体类定义
 * public class User extends BaseEntity {
 *     // createTime和updateTime会由本处理器自动填充
 * }
 *
 * // 业务代码 - 无需手动设置时间字段
 * user.setName("张三");
 * userMapper.insert(user);  // 自动填充createTime和updateTime
 *
 * user.setAge(20);
 * userMapper.updateById(user);  // 自动更新updateTime
 * </pre>
 *
 * <h2>条件激活：</h2>
 * <p>通过@ConditionalOnProperty注解控制，仅在spring.profiles.active=dev
 * 配置文件激活时生效。这确保了自动填充功能只在开发环境启用，
 * 生产环境可能采用数据库默认值或其他策略。</p>
 *
 * @see com.baomidou.mybatisplus.core.handlers.MetaObjectHandler
 * @see com.campus.entity.BaseEntity
 * @author 智慧校园项目组
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "spring.profiles.active", havingValue = "dev")
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 处理插入操作的自动填充
     *
     * <p>当执行INSERT语句插入新记录到数据库时，MyBatis-Plus会调用此方法。
     * 方法会检查目标实体类是否存在createTime和updateTime字段（通过注解标记），
     * 如果存在且当前值为null，则自动填充当前的系统时间。</p>
     *
     * <h3>填充逻辑：</h3>
     * <ul>
     *   <li>createTime: 使用strictInsertFill严格填充，仅当字段值为null时填充</li>
     *   <li>updateTime: 同样使用strictInsertFill，确保新记录也有更新时间</li>
     * </ul>
     *
     * <h3>strictInsertFill vs fill：</h3>
     * <p>使用strictInsertFill方法进行严格模式填充，这意味着只有当字段当前值为null时
     * 才会执行填充操作。如果业务代码已经手动设置了时间值，则不会覆盖。这提供了更灵活的
     * 控制，允许特殊场景下使用自定义时间戳。</p>
     *
     * @param metaObject MyBatis的元对象，封装了实体类的属性信息和反射操作方法
     *                   通过该对象可以获取和设置实体类中的字段值
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 处理更新操作的自动填充
     *
     * <p>当执行UPDATE语句更新数据库记录时，MyBatis-Plus会调用此方法。
     * 方法会自动将updateTime字段更新为当前系统时间，确保每次修改记录后
     * 都能追踪到最后一次更新的时间点。</p>
     *
     * <h3>填充逻辑：</h3>
     * <ul>
     *   <li>updateTime: 使用strictUpdateFill严格填充更新时间</li>
     *   <li>注意：此处不填充createTime，因为创建时间应该在首次插入时确定</li>
     * </ul>
     *
     * <h3>业务价值：</h3>
     * <p>自动维护更新时间对于以下场景非常重要：
     * <ul>
     *   <li>数据审计：追踪记录的最后修改时间</li>
     *   <li>缓存失效：基于更新时间判断缓存是否过期</li>
     *   <li>并发控制：乐观锁实现的时间戳基准</li>
     *   <li>数据同步：增量同步时的变更检测依据</li>
     * </ul>
     * </p>
     *
     * @param metaObject MyBatis的元对象，表示正在被更新的实体对象
     *                   包含了实体的当前状态和可修改的属性映射
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
