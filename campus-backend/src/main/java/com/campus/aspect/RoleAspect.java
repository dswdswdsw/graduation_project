package com.campus.aspect;

import com.campus.annotation.RequireRole;
import com.campus.exception.BusinessException;
import com.campus.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@RequiredArgsConstructor
public class RoleAspect {

    private final JwtUtil jwtUtil;

    @Around("@annotation(requireRole)")
    public Object checkRole(ProceedingJoinPoint joinPoint, RequireRole requireRole) throws Throwable {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(401, "无法获取请求信息");
        }

        HttpServletRequest request = attributes.getRequest();
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token == null || token.isEmpty()) {
            throw new BusinessException(401, "未提供认证令牌");
        }

        String userRole = jwtUtil.getRoleFromToken(token);
        if (userRole == null) {
            throw new BusinessException(401, "无效的认证令牌");
        }

        String[] allowedRoles = requireRole.value();
        for (String role : allowedRoles) {
            if (role.equals(userRole)) {
                return joinPoint.proceed();
            }
        }

        throw new BusinessException(403, "权限不足，无法访问该资源");
    }
}
