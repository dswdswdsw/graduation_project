package com.campus.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户数据访问层接口 (Mapper)
 * 
 * <p>继承MyBatis-Plus的BaseMapper，获得强大的CRUD操作能力。
 * 负责User实体与数据库表sys_user之间的映射和交互。</p>
 * 
 * <h2>继承自BaseMapper的方法：</h2>
 * <ul>
 *   <li><b>insert</b> - 插入记录</li>
 *   <li><b>deleteById</b> - 根据ID删除</li>
 *   <li><b>updateById</b> - 根据ID更新</li>
 *   <li><b>selectById</b> - 根据ID查询</li>
 *   <li><b>selectList</b> - 查询列表</li>
 *   <li><b>selectOne</b> - 查询单条记录</li>
 *   <li><b>selectCount</b> - 统计记录数</li>
 *   <li>... 等更多方法</li>
 * </ul>
 * 
 * <h2>MyBatis-Plus特性：</h2>
 * <ul>
 *   <li>无需编写XML即可完成基本CRUD</li>
 *   <li>支持Lambda表达式类型安全查询</li>
 *   <li>自动处理逻辑删除字段</li>
 *   <li>支持分页查询插件</li>
 * </ul>
 * 
 * <h2>使用方式：</h2>
 * <p>此Mapper通常不直接在Controller中使用，
 * 而是通过Service层（如UserService）间接调用。</p>
 * 
 * @see User 用户实体类
 * @see UserService 用户服务层
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 如果需要自定义SQL查询，可以在此处添加方法
     * 并在resources/mapper/UserMapper.xml中实现
     * 
     * 示例：
     * @Select("SELECT * FROM sys_user WHERE role = #{role}")
     * List<User> selectByRole(@Param("role") String role);
     */
}
