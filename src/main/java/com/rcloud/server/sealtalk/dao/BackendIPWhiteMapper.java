package com.rcloud.server.sealtalk.dao;

import com.rcloud.server.sealtalk.domain.BackendIPWhite;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BackendIPWhiteMapper extends Mapper<BackendIPWhite> {
    // 分页获取后台IP白名单数据
    List<BackendIPWhite> getPageBackendIPWhite(@Param("offset") Integer offset, @Param("limit") Integer limit);
    // 分页获取后台IP白名单总数
    Integer getTotalCount();
}
