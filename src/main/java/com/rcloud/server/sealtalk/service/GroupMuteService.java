package com.rcloud.server.sealtalk.service;

import com.rcloud.server.sealtalk.dao.GroupMuteMapper;
import com.rcloud.server.sealtalk.domain.GroupMute;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;

@Service
public class GroupMuteService extends AbstractBaseService<GroupMute, Integer> {

    @Resource
    private GroupMuteMapper mapper;

    @Override
    protected Mapper<GroupMute> getMapper() {
        return mapper;
    }
}
