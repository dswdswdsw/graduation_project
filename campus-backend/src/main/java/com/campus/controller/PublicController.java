package com.campus.controller;

import com.campus.common.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公共控制器 - 提供无需认证的公共API接口
 * 
 * <p>提供系统级别的公共接口，所有用户（包括未登录用户）都可以访问。
 * 主要用于系统健康检查、版本信息等基础功能。</p>
 * 
 * <h2>API端点：</h2>
 * <ul>
 *   <li>GET /api/public/health - 系统健康检查</li>
 * </ul>
 * 
 * <h2>特点：</h2>
 * <ul>
 *   <li><b>无需认证</b>：不需要JWT Token即可访问</li>
 *   <li><b>始终可用</b>：不依赖数据库，在default profile下也可用</li>
 *   <li><b>轻量级</b>：接口简单，响应快速</li>
 * </ul>
 * 
 * <h2>使用场景：</h2>
 * <ul>
 *   <li>负载均衡器健康检查</li>
 *   <li>Docker容器健康探针</li>
 *   <li>前端检测后端服务是否可用</li>
 *   <li>监控系统状态</li>
 * </ul>
 */
@RestController
@RequestMapping("/api/public")
public class PublicController {

    /**
     * 系统健康检查接口
     * 
     * <p>用于检测后端服务是否正常运行。此接口不依赖任何外部资源
     * （如数据库、Redis等），因此可以准确反映应用本身的状态。</p>
     * 
     * <h3>请求示例：</h3>
     * <pre>
     * GET /api/public/health
     * </pre>
     * 
     * <h3>成功响应：</h3>
     * <pre>
     * {
     *   "code": 200,
     *   "message": "操作成功",
     *   "data": "系统运行正常"
     * }
     * </pre>
     * 
     * <h3>典型用途：</h3>
     * <ul>
     *   <li>Nginx/Traefik等反向代理的健康检查配置</li>
     *   <li>Kubernetes/Docker的liveness和readiness探针</li>
     *   <li>前端应用的启动前检测</li>
     *   <li>监控系统的定时检测</li>
     * </ul>
     * 
     * @return Result包含健康状态消息
     */
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("系统运行正常");
    }
}
