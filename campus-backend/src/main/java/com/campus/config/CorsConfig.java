package com.campus.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * CORS跨域请求配置类
 *
 * <p>用于解决前后端分离架构中的跨域资源共享问题。
 * 当前端应用（如Vue）运行在不同的域名、端口或协议上时，
 * 浏览器会出于安全考虑阻止跨域请求。本配置类通过设置
 * CORS（Cross-Origin Resource Sharing）策略，允许前端
 * 正常访问后端API接口。</p>
 *
 * <h2>主要功能：</h2>
 * <ul>
 *   <li>允许指定的来源发起跨域请求</li>
 *   <li>支持携带认证信息（如Cookie和Authorization头）</li>
 *   <li>定义允许的HTTP方法和请求头</li>
 *   <li>暴露自定义响应头给前端</li>
 * </ul>
 *
 * <h2>使用场景：</h2>
 * <pre>
 * 前端：http://localhost:5173 (Vue开发服务器)
 * 后端：http://localhost:8080 (Spring Boot)
 * 浏览器会自动拦截跨域请求，需要此配置来放行
 * </pre>
 *
 * <h2>安全注意事项：</h2>
 * <p>当前配置使用通配符"*"允许所有来源，适用于开发环境。
 * 在生产环境中，应该限制为具体的前端域名以提高安全性。</p>
 *
 * @author 智慧校园项目组
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
public class CorsConfig {

    /**
     * 创建并配置CORS过滤器Bean
     *
     * <p>该方法创建一个CorsFilter实例，用于处理所有进入应用的HTTP请求，
     * 并根据CORS规则决定是否允许跨域访问。过滤器会在请求到达Controller
     * 之前执行，检查请求的Origin头并根据配置返回相应的CORS响应头。</p>
     *
     * <h3>CORS响应头说明：</h3>
     * <ul>
     *   <li><b>Access-Control-Allow-Origin</b>: 允许的请求来源域名</li>
     *   <li><b>Access-Control-Allow-Credentials</b>: 是否允许携带凭证</li>
     *   <li><b>Access-Control-Allow-Methods</b>: 允许的HTTP方法</li>
     *   <li><b>Access-Control-Allow-Headers</b>: 允许的请求头字段</li>
     *   <li><b>Access-Control-Expose-Headers</b>: 可暴露给前端的响应头</li>
     * </ul>
     *
     * <h3>预检请求处理：</h3>
     * <p>浏览器在发送非简单请求（如PUT、DELETE或包含自定义头的请求）
     * 时，会先发送OPTIONS预检请求。本过滤器会自动处理这些预检请求，
     * 返回适当的CORS头，让浏览器知道是否允许实际请求。</p>
     *
     * @return 配置好的CorsFilter实例，已注册到Spring容器中
     */
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOriginPattern("*");
        config.setAllowCredentials(true);
        config.addAllowedMethod("*");
        config.addAllowedHeader("*");

        config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }
}
