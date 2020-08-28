package com.rcloud.server.sealtalk.dao;

import com.rcloud.server.sealtalk.domain.Groups;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface GroupsMapper extends Mapper<Groups> {
    // 分页获取用户黑名单数据
    List<Groups> getPageGroupsList(@Param("offset") Integer offset, @Param("limit") Integer limit);
    // 分页获取用户黑名单总数
    Integer getTotalCount();
    // 根据群名查询群
    List<Groups> getGroupsByName(@Param("name") String name);
}