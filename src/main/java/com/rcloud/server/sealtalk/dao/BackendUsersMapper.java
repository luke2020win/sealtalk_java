package com.rcloud.server.sealtalk.dao;

import com.rcloud.server.sealtalk.domain.BackendUsers;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BackendUsersMapper extends Mapper<BackendUsers> {
    // 获取全部管理员数据
    List<BackendUsers> getAllBackendUsers();

    // 分页获取管理员数据
    List<BackendUsers> getPageBackendUsers(@Param("offset") Integer offset, @Param("limit") Integer limit);

    Integer getTotalCount();
}