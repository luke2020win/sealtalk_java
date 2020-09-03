package com.rcloud.server.sealtalk.model.dto.sync;

import lombok.Data;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/9/3
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Data
public class SyncGroupDTO {
    private String groupId;
    private String displayName;
    private Integer role;
    private Boolean isDeleted;

    private SyncGroupInnerDTO group;



}
