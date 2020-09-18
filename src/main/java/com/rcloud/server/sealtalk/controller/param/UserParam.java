package com.rcloud.server.sealtalk.controller.param;

import lombok.Data;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/8/23
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Data
public class UserParam {
    public static Integer  TYPE_WECHAT = 1;
    public static Integer  TYPE_QQ = 2;
    public static Integer  TYPE_DOUYIN = 3;

    private String region;
    private String phone;
    private String code;

    private String nickname;
    private String password;
    private String verification_token;

    private Integer openType;
    private String openId;

    private String oldPassword;
    private String newPassword;

    private String portraitUri;

    private String stAccount;

    private String friendId;
    private String version;
    private String gender;

    private Integer phoneVerify;
    private Integer stSearchVerify;
    private Integer friVerify;
    private Integer groupVerify;

    private Integer pokeStatus;

}

