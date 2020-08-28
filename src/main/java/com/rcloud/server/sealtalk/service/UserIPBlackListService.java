package com.rcloud.server.sealtalk.service;

import com.rcloud.server.sealtalk.dao.UserIPBlackMapper;
import com.rcloud.server.sealtalk.domain.UserIPBlack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class UserIPBlackListService extends AbstractBaseService<UserIPBlack, Integer> {
    @Resource
    private UserIPBlackMapper mapper;

    @Override
    protected Mapper getMapper() {
        return mapper;
    }

    public List<UserIPBlack> getPageUserIPBlackList(Integer offset, Integer limit) {
        log.info("UserIPBlackListService getPageUserIPBlackList offset:"+offset+" limit:"+limit);
        return mapper.getPageUserIPBlack(offset, limit);
    }


    public Integer getTotalCount() {
        return mapper.getTotalCount();
    }
}
