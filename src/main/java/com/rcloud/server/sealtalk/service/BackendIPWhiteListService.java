package com.rcloud.server.sealtalk.service;

import com.rcloud.server.sealtalk.dao.BackendIPWhiteMapper;
import com.rcloud.server.sealtalk.domain.BackendIPWhite;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class BackendIPWhiteListService extends AbstractBaseService<BackendIPWhite, Integer> {
    @Resource
    private BackendIPWhiteMapper mapper;

    @Override
    protected Mapper getMapper() {
        return mapper;
    }

    public List<BackendIPWhite> getPageBackendIPWhiteList(Integer offset, Integer limit) {
        log.info("BackendIPWhiteListService getPageBackendIPWhiteList offset:"+offset+" limit:"+limit);
        return mapper.getPageBackendIPWhite(offset, limit);
    }


    public Integer getTotalCount() {
        return mapper.getTotalCount();
    }
}
