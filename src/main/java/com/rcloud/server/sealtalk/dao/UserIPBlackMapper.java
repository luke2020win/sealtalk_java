package com.rcloud.server.sealtalk.dao;

import com.rcloud.server.sealtalk.domain.UserIPBlack;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserIPBlackMapper extends Mapper<UserIPBlack> {
    // 分页获取用户IP黑名单数据
    List<UserIPBlack> getPageUserIPBlack(@Param("offset") Integer offset, @Param("limit") Integer limit);
    // 分页获取用户IP黑名单总数
    Integer getTotalCount();
}
