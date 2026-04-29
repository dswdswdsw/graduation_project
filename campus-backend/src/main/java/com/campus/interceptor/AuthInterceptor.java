package com.campus.interceptor;

import com.campus.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.campus.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT认证拦截器 - 用于验证请求中的JWT令牌
 * 
 * <p>在Controller方法执行之前拦截HTTP请求，
 * 验证请求头中是否携带有效的JWT令牌。
 * 只有通过认证的请求才能继续访问受保护的资源。</p>
 * 
 * <h2>工作流程：</h2>
 * <ol>
 *   <li>接收HTTP请求</li>
 *   <li>检查是否为OPTIONS预检请求（直接放行）</li>
 *   <li>从Authorization请求头提取Token</li>
 *   <li>使用JwtUtil验证Token有效性</li>
 *   <li>验证通过则放行，否则返回401未授权错误</li>
 * </ol>
 * 
 * <h2>配置说明：</h2>
 * <p>此拦截器需要在WebMvcConfig中进行注册和路径匹配配置。
 * 通常对/api/user/**等需要登录的路径生效。</p>
 * 
 * @see JwtUtil JWT工具类
 * @see WebMvcConfig Web MVC配置类（注册拦截器）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    /**
     * JWT工具类
     * 用于验证Token的有效性
     */
    private final JwtUtil jwtUtil;

    /**
     * JSON序列化工具
     * 用于将Result对象转换为JSON字符串写入响应
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 预处理方法 - 在Controller方法执行之前调用
     * 
     * <p>实现JWT Token的验证逻辑：</p>
     * <ul>
     *   <li>OPTIONS请求直接放行（用于CORS预检）</li>
     *   <li>检查Authorization头是否存在</li>
     *   <li>验证Token格式和有效性</li>
     *   <li>无效时返回401状态码和错误信息</li>
     * </ul>
     * 
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param handler 处理器对象（Controller方法）
     * @return true表示通过验证，false表示验证失败
     * @throws Exception 可能抛出的异常
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.debug("认证拦截: {} {}", request.getMethod(), request.getRequestURI());
        
        // 1. OPTIONS预检请求直接放行（CORS）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 2. 获取Authorization头中的Token
        String token = request.getHeader("Authorization");
        
        // 3. 验证Token是否存在且有效
        if (token == null || !jwtUtil.validateToken(token)) {
            log.warn("认证失败: 无效或缺失Token - {}", request.getRequestURI());
            
            // 4. 设置401未授权状态码
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            
            // 5. 返回统一的错误响应格式
            Result<Void> result = Result.fail(401, "未登录或登录已过期");
            response.getWriter().write(objectMapper.writeValueAsString(result));
            return false;
        }

        log.debug("认证通过: {}", request.getRequestURI());
        return true;
    }
}
