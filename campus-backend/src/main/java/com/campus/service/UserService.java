package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.dto.PasswordDTO;
import com.campus.entity.User;
import com.campus.vo.LoginVO;

public interface UserService extends IService<User> {

    LoginVO login(String username, String password);

    void register(User user);

    User getUserInfo(Long userId);

    void changePassword(Long userId, PasswordDTO dto);

    Page<User> listUsers(String role, String keyword, int page, int size);

    void updateUser(User user);

    void resetPassword(Long userId);
}
