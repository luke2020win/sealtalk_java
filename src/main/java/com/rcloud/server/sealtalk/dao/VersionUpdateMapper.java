package com.rcloud.server.sealtalk.dao;

import com.rcloud.server.sealtalk.domain.VersionUpdate;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface VersionUpdateMapper extends Mapper<VersionUpdate> {
    // 分页获取注册用户数据
    List<VersionUpdate> getPageVersionUpdateList(@Param("offset") Integer offset, @Param("limit") Integer limit);

    // 获取总记录数
    Integer getTotalCount();

    // 获取某个客户端最新版本
    VersionUpdate getLastVersionUpdateByClientType(@Param("clientType") String clientType);

    // 获取某端某个渠道的最新版本
    VersionUpdate getLastVersionUpdateByClientTypeAndChannel(@Param("clientType") String clientType, @Param("channel") String channel);
}