package com.rcloud.server.sealtalk.dao;

import com.rcloud.server.sealtalk.domain.Users;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface UsersMapper extends Mapper<Users> {
    // 分页获取注册用户数据
    List<Users> getPageUserList(@Param("offset") Integer offset, @Param("limit") Integer limit);

    Integer getTotalCount();
}