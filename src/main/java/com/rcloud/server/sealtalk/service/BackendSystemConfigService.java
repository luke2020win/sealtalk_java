package com.rcloud.server.sealtalk.service;

import com.rcloud.server.sealtalk.dao.BackendSystemConfigMapper;
import com.rcloud.server.sealtalk.dao.BackendUsersMapper;
import com.rcloud.server.sealtalk.domain.BackendSystemConfig;
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
public class BackendSystemConfigService extends AbstractBaseService<BackendSystemConfig, Integer> {

    @Resource
    private BackendSystemConfigMapper mapper;

    @Override
    protected Mapper getMapper() {
        return mapper;
    }


    public List<BackendSystemConfig> getAllBackendSystemConfig() {
        return mapper.getAllBackendSystemConfig();
    }

    public List<BackendSystemConfig> getPageBackendSystemConfig(Integer offset, Integer limit) {
        log.info("BackendSystemConfigService getPageBackendSystemConfig offset:"+offset+" limit:"+limit);
        return mapper.getPageBackendSystemConfig(offset, limit);
    }

    public Integer getTotalCount() {
        return mapper.getTotalCount();
    }
}
