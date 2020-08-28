package com.rcloud.server.sealtalk.dao;

import com.rcloud.server.sealtalk.domain.UserBlack;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UserBlackMapper extends Mapper<UserBlack> {
    // 分页获取用户黑名单数据
    List<UserBlack> getPageUserBlack(@Param("offset") Integer offset, @Param("limit") Integer limit);
    // 分页获取用户黑名单总数
    Integer getTotalCount();
}
