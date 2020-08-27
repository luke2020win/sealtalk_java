package com.rcloud.server.sealtalk.dao;

import com.rcloud.server.sealtalk.domain.BackendSystemConfig;
import com.rcloud.server.sealtalk.domain.BackendUsers;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BackendSystemConfigMapper extends Mapper<BackendSystemConfig> {
    // 获取全部管理员数据
    List<BackendSystemConfig> getAllBackendSystemConfig();

    // 分页获取管理员数据
    List<BackendSystemConfig> getPageBackendSystemConfig(@Param("offset") Integer offset, @Param("limit") Integer limit);

    Integer getTotalCount();
}
