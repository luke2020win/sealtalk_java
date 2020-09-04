package com.rcloud.server.sealtalk.service;

import com.rcloud.server.sealtalk.dao.VersionUpdateMapper;
import com.rcloud.server.sealtalk.domain.UserIPBlack;
import com.rcloud.server.sealtalk.domain.VersionUpdate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;
import java.util.List;

/**
 * @Author: xiuwei.nie
 * @Date: 2020/7/6
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Service
@Slf4j
public class VersionUpdateService extends AbstractBaseService<VersionUpdate, Integer> {

    @Resource
    private VersionUpdateMapper mapper;

    @Override
    protected Mapper getMapper() {
        return mapper;
    }

    // 获取某个客户端最新版本
    public VersionUpdate getLastVersionUpdateByClientType(String clientType) {
        return mapper.getLastVersionUpdateByClientType(clientType);
    }

    // 获取某端某个渠道的最新版本
    public VersionUpdate getLastVersionUpdateByClientTypeAndChannel(String clientType, String channel) {
        return mapper.getLastVersionUpdateByClientTypeAndChannel(clientType, channel);
    }

    public List<VersionUpdate> getPageVersionUpdateList(Integer offset, Integer limit) {
        log.info("VersionUpdateService getPageVersionUpdateList offset:"+offset+" limit:"+limit);
        return mapper.getPageVersionUpdateList(offset, limit);
    }

    public Integer getTotalCount() {
        return mapper.getTotalCount();
    }
}
