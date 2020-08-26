package com.rcloud.server.sealtalk.service;

import com.rcloud.server.sealtalk.dao.BackendUsersMapper;
import com.rcloud.server.sealtalk.domain.BackendUsers;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;


/**
 *
 */
@Service
public class BackendUsersService extends AbstractBaseService<BackendUsers, Integer> {

    @Resource
    private BackendUsersMapper mapper;

    @Override
    protected Mapper getMapper() {
        return mapper;
    }
}
