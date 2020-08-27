package com.rcloud.server.sealtalk.service;

import com.rcloud.server.sealtalk.dao.BackendUsersMapper;
import com.rcloud.server.sealtalk.domain.BackendUsers;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;
import java.util.List;


/**
 *
 */
@Service
@Slf4j
public class BackendUsersService extends AbstractBaseService<BackendUsers, Integer> {

    @Resource
    private BackendUsersMapper mapper;

    @Override
    protected Mapper getMapper() {
        return mapper;
    }

    public List<BackendUsers> getAllBackendUsers() {
        return mapper.getAllBackendUsers();
    }

    public List<BackendUsers> getPageBackendUsers(Integer offset, Integer limit) {
        log.info("BackendUsersService getPageBackendUsers offset:"+offset+" limit:"+limit);
        return mapper.getPageBackendUsers(offset, limit);
    }


    public Integer getTotalCount() {
        return mapper.getTotalCount();
    }
}
