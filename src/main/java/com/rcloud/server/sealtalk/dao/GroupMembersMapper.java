package com.rcloud.server.sealtalk.dao;

import com.rcloud.server.sealtalk.domain.GroupMembers;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface GroupMembersMapper extends Mapper<GroupMembers> {

    List<GroupMembers> queryGroupMembersWithGroupByMemberId(@Param("memberId") Integer memberId);

    List<GroupMembers> queryGroupMembersWithUsersByGroupId(@Param("groupId") Integer groupId);

    GroupMembers queryGroupMembersWithGroupByGroupIdAndMemberId(@Param("groupId") Integer groupId, @Param("memberId") Integer memberId);

}