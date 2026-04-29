package com.campus.service.impl;

import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.common.ResultCode;
import com.campus.dto.PasswordDTO;
import com.campus.entity.User;
import com.campus.exception.BusinessException;
import com.campus.mapper.UserMapper;
import com.campus.service.UserService;
import com.campus.util.JwtUtil;
import com.campus.vo.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(String username, String password) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        User user = getOne(wrapper);

        if (user == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "用户不存在");
        }

        String encryptedPwd = SecureUtil.md5(password);
        if (!encryptedPwd.equals(user.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED, "密码错误");
        }

        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "账号已被禁用");
        }

        LoginVO vo = new LoginVO();
        vo.setUserId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setRealName(user.getRealName());
        vo.setRole(user.getRole());
        vo.setToken(jwtUtil.generateToken(user.getId(), user.getRole()));
        vo.setFirstLogin(user.getFirstLogin());

        return vo;
    }

    @Override
    public void register(User user) {
        Long count = lambdaQuery().eq(User::getUsername, user.getUsername()).count();
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        user.setPassword(SecureUtil.md5(user.getPassword()));
        user.setStatus(1);
        user.setFirstLogin(1);
        save(user);
    }

    @Override
    public User getUserInfo(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    @Override
    public void changePassword(Long userId, PasswordDTO dto) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        String oldEncrypted = SecureUtil.md5(dto.getOldPassword());
        if (!oldEncrypted.equals(user.getPassword())) {
            throw new BusinessException("旧密码错误");
        }
        user.setPassword(SecureUtil.md5(dto.getNewPassword()));
        user.setFirstLogin(0);
        updateById(user);
    }

    @Override
    public Page<User> listUsers(String role, String keyword, int page, int size) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword)
                    .or().like(User::getUserNo, keyword));
        }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> result = page(pageParam, wrapper);
        result.getRecords().forEach(u -> u.setPassword(null));
        return result;
    }

    @Override
    public void updateUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(SecureUtil.md5(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        updateById(user);
    }

    @Override
    public void resetPassword(Long userId) {
        User user = getById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "用户不存在");
        }
        String defaultPwd = "123456";
        if (user.getIdCard() != null && user.getIdCard().length() >= 6) {
            defaultPwd = user.getIdCard().substring(user.getIdCard().length() - 6);
        }
        user.setPassword(SecureUtil.md5(defaultPwd));
        user.setFirstLogin(1);
        updateById(user);
    }
}
