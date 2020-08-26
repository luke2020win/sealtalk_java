package com.rcloud.server.sealtalk.manager;

import com.rcloud.server.sealtalk.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/8/11
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Service
@Slf4j
public class BackendSystemConfigManager extends BaseManager {

    @Resource
    private BackendSystemConfigService backendSystemConfigService;
}
