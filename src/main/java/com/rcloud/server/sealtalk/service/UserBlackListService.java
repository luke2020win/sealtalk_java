package com.rcloud.server.sealtalk.service;

import com.rcloud.server.sealtalk.dao.UserBlackMapper;
import com.rcloud.server.sealtalk.domain.UserBlack;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.common.Mapper;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
public class UserBlackListService extends AbstractBaseService<UserBlack, Integer> {
    @Resource
    private UserBlackMapper mapper;

    @Override
    protected Mapper getMapper() {
        return mapper;
    }

    public List<UserBlack> getPageUserBlackList(Integer offset, Integer limit) {
        log.info("UserBlackListService getPageUserBlackList offset:"+offset+" limit:"+limit);
        return mapper.getPageUserBlack(offset, limit);
    }


    public Integer getTotalCount() {
        return mapper.getTotalCount();
    }
}
