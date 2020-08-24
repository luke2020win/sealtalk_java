package com.rcloud.server.sealtalk.controller.param;

import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.util.JacksonUtil;
import lombok.Data;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/8/24
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Data
public class FriendshipParam {

    private String friendId;

    private String message;

    private String displayName;

    private String[] contactList;

    private String[] friendIds;

    private String region;
    private String phone;
    private String description;
    private String imageUri;


    public static void main(String[] args) throws ServiceException {
        FriendshipParam friendshipParam = new FriendshipParam();
        String[] contactList = {"18810183286","18810183285"};
        friendshipParam.setContactList(contactList);
        System.out.println(JacksonUtil.toJson(contactList));
    }


}
