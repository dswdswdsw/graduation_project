package com.campus.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    private String username;
    private String password;
    private String realName;
    private String idCard;
    private String userNo;
    private String college;
    private String major;
    private String className;
    private Integer firstLogin;
    private String role;
    private String phone;
    private String email;
    private String avatar;
    private Integer status;
}
