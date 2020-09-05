package com.rcloud.server.sealtalk.manager;

import com.rcloud.server.sealtalk.constant.Constants;
import com.rcloud.server.sealtalk.constant.ConversationType;
import com.rcloud.server.sealtalk.constant.ErrorCode;
import com.rcloud.server.sealtalk.domain.*;
import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.rongcloud.RongCloudClient;
import com.rcloud.server.sealtalk.rongcloud.message.CustomerConNtfMessage;
import com.rcloud.server.sealtalk.service.*;
import com.rcloud.server.sealtalk.util.*;
import io.rong.messages.InfoNtfMessage;
import io.rong.messages.TxtMessage;
import io.rong.models.message.GroupMessage;
import io.rong.models.message.PrivateMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

/**
 * @Author: Jianlu.Yu
 * @Date: 2020/8/11
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Service
@Slf4j
public class MiscManager extends BaseManager {

    @Resource
    private RongCloudClient rongCloudClient;

    @Resource
    private FriendshipsService friendshipsService;

    @Resource
    private GroupMembersService groupMembersService;

    @Resource
    private UsersService usersService;

    @Resource
    private ScreenStatusesService screenStatusesService;

    @Resource
    private VersionUpdateService versionUpdateService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 调用Server api发送消息
     *
     * @param conversationType
     * @param targetId
     * @param objectName
     * @param content
     * @param pushContent
     * @param encodedTargetId
     */
    public void sendMessage(Integer currentUserId, String conversationType, Integer targetId, String objectName, String content, String pushContent, String encodedTargetId) throws ServiceException {
        if (Constants.CONVERSATION_TYPE_PRIVATE.equals(conversationType)) {
            //如果会话类型是单聊
            Example example = new Example(Friendships.class);
            example.createCriteria().andEqualTo("userId", currentUserId)
                    .andEqualTo("friendId", targetId)
                    .andEqualTo("status", Friendships.FRIENDSHIP_AGREED);
            Friendships friendships = friendshipsService.getOneByExample(example);

            if (friendships != null) {
                //调用融云接口发送单聊消息
                PrivateMessage privateMessage = new PrivateMessage()
                        .setSenderId(N3d.encode(currentUserId))
                        .setTargetId(new String[]{encodedTargetId})
                        .setObjectName(objectName)
                        .setContent(new TxtMessage(content, ""))
                        .setPushContent(pushContent);
                rongCloudClient.sendPrivateMessage(privateMessage);
                return;
            } else {
                throw new ServiceException(ErrorCode.NOT_YOUR_FRIEND);
            }
        }
        else if (Constants.CONVERSATION_TYPE_GROUP.equals(conversationType)) {
            //如果会话类型是群组
            Example example = new Example(GroupMembers.class);
            example.createCriteria().andEqualTo("groupId", targetId)
                    .andEqualTo("memberId", currentUserId);
            GroupMembers groupMembers = groupMembersService.getOneByExample(example);

            if (groupMembers != null) {
                GroupMessage groupMessage = new GroupMessage();
                groupMessage.setSenderId(N3d.encode(currentUserId))
                        .setTargetId(new String[]{encodedTargetId})
                        .setObjectName(objectName)
                        .setContent(new TxtMessage(content, ""))
                        .setPushContent(pushContent);
                //发送群组消息
                rongCloudClient.sendGroupMessage(groupMessage);
            }
            else {
                throw new ServiceException(ErrorCode.NOT_YOUR_GROUP.getErrorCode(), "Your are not member of Group " + encodedTargetId + ".", ErrorCode.NOT_YOUR_GROUP.getErrorCode());
            }
        }
        else {
            throw new ServiceException(ErrorCode.UNSUPPORTED_CONVERSATION_TYPE);
        }
    }

    /**
     * 设置截屏通知状态
     *
     * @param currentUserId
     * @param targetId
     * @param conversationType
     * @param noticeStatus
     */
    public void setScreenCapture(Integer currentUserId, Integer targetId, Integer conversationType, Integer noticeStatus) throws ServiceException {

        String operateId = String.valueOf(targetId);
        String statusContent = noticeStatus == 0 ? "closeScreenNtf" : "openScreenNtf";
        if (conversationType == 1) {
            operateId = currentUserId < targetId ? currentUserId + "_" + targetId : targetId + "_" + currentUserId;
        }

        Users users = usersService.getByPrimaryKey(currentUserId);
        if (users != null) {
            Example example = new Example(ScreenStatuses.class);
            example.createCriteria().andEqualTo("conversationType", conversationType)
                    .andEqualTo("operateId", operateId);

            ScreenStatuses screenStatuses = screenStatusesService.getOneByExample(example);

            if (screenStatuses != null) {
                //如果存在截屏设置记录则更新状态
                screenStatuses.setStatus(noticeStatus);
                screenStatusesService.updateByPrimaryKeySelective(screenStatuses);
                //发送截屏消息
                sendScreenMsg0(currentUserId, targetId, conversationType, statusContent);
            } else {
                //如果不存在，则创建
                ScreenStatuses ss = new ScreenStatuses();
                ss.setOperateId(operateId);
                ss.setConversationType(conversationType);
                ss.setStatus(noticeStatus);
                ss.setCreatedAt(new Date());
                ss.setUpdatedAt(ss.getCreatedAt());
                screenStatusesService.saveSelective(ss);
                //发送截屏通知消息
                sendScreenMsg0(currentUserId, targetId, conversationType, statusContent);
            }
        } else {
            throw new ServiceException(ErrorCode.ILLEGAL_PARAMETER);
        }


    }

    /**
     * 发送截屏通知消息
     *
     * @param currentUserId
     * @param targetId
     * @param conversationType
     * @param operation
     */

    private void sendScreenMsg0(Integer currentUserId, Integer targetId, Integer conversationType, String operation) throws ServiceException {

        if (ConversationType.PRIVATE.getCode().equals(conversationType)) {
            String encodeUserId = N3d.encode(currentUserId);
            String encodeTargetId = N3d.encode(targetId);

            CustomerConNtfMessage customerConNtfMessage = new CustomerConNtfMessage();
            customerConNtfMessage.setOperatorUserId(encodeUserId);
            customerConNtfMessage.setOperation(operation);

            PrivateMessage privateMessage = new PrivateMessage()
                    .setSenderId(encodeUserId)
                    .setTargetId(new String[]{encodeTargetId})
                    .setObjectName(customerConNtfMessage.getType())
                    .setContent(customerConNtfMessage);

            rongCloudClient.sendPrivateMessage(privateMessage);
        }
        else if (ConversationType.GROUP.getCode().equals(conversationType)) {
            rongCloudClient.sendCustomerConNtfMessage(N3d.encode(currentUserId), N3d.encode(targetId), operation);
        }
        else {
            throw new ServiceException(ErrorCode.REQUEST_ERROR);
        }

    }

    /**
     * 获取截屏通知状态
     *
     * @param currentUserId
     * @param targetId
     * @param conversationType
     * @return
     */
    public ScreenStatuses getScreenCapture(Integer currentUserId, Integer targetId, Integer conversationType) {
        String operateId = String.valueOf(targetId);
        if (conversationType == 1) {
            operateId = currentUserId < targetId ? currentUserId + "_" + targetId : targetId + "_" + currentUserId;
        }

        Example example = new Example(ScreenStatuses.class);
        example.createCriteria().andEqualTo("operateId", operateId)
                .andEqualTo("conversationType", conversationType);
        return screenStatusesService.getOneByExample(example);
    }


    /**
     * 发送截屏消息
     *
     * @param currentUserId
     * @param targetId
     * @param conversationType
     */
    public void sendScreenCaptureMsg(Integer currentUserId, Integer targetId, Integer conversationType) throws ServiceException{
        sendScreenMsg0(currentUserId, targetId, conversationType, "sendScreenNtf");
    }

    /**
     * 获取版本信息
     * @param version
     * @param channel
     * @param clientType
     * @return
     * @throws ServiceException
     */
    public VersionUpdate getClientVersion(String version, Integer versionCode, String channel, String clientType) throws ServiceException {
        log.info("MiscManager getClientVersion version:"+version);
        log.info("MiscManager getClientVersion channel:"+channel);
        log.info("MiscManager getClientVersion clientype:"+clientType);

        Integer versioncode = null;
        if(versionCode == null || versioncode < 0) {
            versioncode = VersionUtils.toVersionCode(version);
        }
        String channelStr = VersionUtils.handleClientype(channel);
        String clienttype = VersionUtils.handleClientype(clientType);

        log.info("MiscManager getClientVersion channelStr:"+channelStr);
        log.info("MiscManager getClientVersion clientypeStr:"+clienttype);

        VersionUpdate versionUpdate = versionUpdateService.getLastVersionUpdateByClientTypeAndChannel(clienttype, channelStr);
        if(versionUpdate != null) {
            if(versionUpdate.getVersionCode() <= versioncode && versionUpdate.getChannel().equals(channelStr)) {
                versionUpdate.setIsShowUpdate(VersionUpdate.NO_SHOW_UPDATE);
            }
        }

        return versionUpdate;
    }

    public List<VersionUpdate> getPageVersionUpdateList(int pageNum, int pageSize) {
        log.info("UserBlackListManager getPageVersionUpdateList pageNum:"+pageNum+" pageSize:"+pageSize);
        int offset = (pageNum - 1) * pageSize;
        int limit = pageSize;
        return versionUpdateService.getPageVersionUpdateList(offset, pageSize);
    }

    public int getTotalCount() {
        return versionUpdateService.getTotalCount();
    }

    public void saveVersionUpdate(String clientType, String version, String channel, String isShowUpdate, String isForce, String isPlist, String content, String url, String description) throws ServiceException {

        String clientT = VersionUtils.handleClientype(clientType);
        String chan = VersionUtils.handleChannel(channel);
        Integer versionC = VersionUtils.toVersionCode(version);
        Integer isS = TypeConversionUtils.StringToInt(isShowUpdate);
        Integer isF = TypeConversionUtils.StringToInt(isForce);
        Integer isp = TypeConversionUtils.StringToInt(isPlist);

        // 查询参数
        VersionUpdate param = new VersionUpdate();
        param.setClientType(clientT);
        param.setVersionCode(versionC);
        param.setChannel(chan);
        // 查询记录
        VersionUpdate versionUpdate = versionUpdateService.getOne(param);

        if(versionUpdate != null) {
            versionUpdate.setIsShowUpdate(isS);
            versionUpdate.setIsForce(isF);
            versionUpdate.setIsPlist(isp);
            versionUpdate.setContent(content);
            versionUpdate.setUrl(url);
            if(StringUtils.isEmpty(description)) {
                versionUpdate.setDescription("暂无备注");
            }
            else {
                versionUpdate.setDescription(description);
            }
            updateVersionUpdate(clientT, versionC, chan, versionUpdate);
        }
        else {
            insertVersionUpdate(clientT, version, versionC, chan, isS, isF, isp, content, url, description);
        }
    }

    public List<VersionUpdate> getVersionUpdateByVersion(String version) throws ServiceException {
        Integer versionC = VersionUtils.toVersionCode(version);

        Example example = new Example(VersionUpdate.class);
        example.createCriteria().andEqualTo("versionCode", versionC);
        return versionUpdateService.getByExample(example);
    }

    public void enableOrDisableVersionUpdate(String clientType, String version, String channel, String isShowUpdate) throws ServiceException {
        String clientT = VersionUtils.handleClientype(clientType);
        String chan = VersionUtils.handleChannel(channel);
        Integer versionC = VersionUtils.toVersionCode(version);
        Integer isS = TypeConversionUtils.StringToInt(isShowUpdate);

        // 查询参数
        VersionUpdate param = new VersionUpdate();
        param.setClientType(clientT);
        param.setVersionCode(versionC);
        param.setChannel(chan);
        // 查询记录
        VersionUpdate versionUpdate = versionUpdateService.getOne(param);
        if(versionUpdate == null) {
            throw new ServiceException(ErrorCode.VERSION_UPDATE_NOT_EXIST);
        }
        else {
            versionUpdate.setIsShowUpdate(isS);
            versionUpdate.setUpdatedAt(new Date());
            Example example = new Example(VersionUpdate.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("clientType", clientT)
                    .andEqualTo("versionCode", versionC)
                    .andEqualTo("channel", chan);
            versionUpdateService.updateByExampleSelective(versionUpdate, example);
        }
    }

    /**
     * @param clientType
     * @param version
     * @param channel
     * @param isShowUpdate
     * @param isForce
     * @param isPlist
     * @param content
     * @param url
     * @param description
     * @return
     */
    private VersionUpdate insertVersionUpdate(String clientType, String version, Integer versionCode, String channel, Integer isShowUpdate, Integer isForce, Integer isPlist, String content, String url, String description) {
        return transactionTemplate.execute(transactionStatus -> {
            //插入user表
            VersionUpdate versionUpdate = new VersionUpdate();
            versionUpdate.setClientType(clientType);
            versionUpdate.setVersion(version);
            versionUpdate.setVersionCode(versionCode);
            versionUpdate.setChannel(channel);
            versionUpdate.setIsShowUpdate(isShowUpdate);
            versionUpdate.setIsForce(isForce);
            versionUpdate.setIsPlist(isPlist);
            versionUpdate.setContent(content);
            versionUpdate.setUrl(url);
            if(StringUtils.isEmpty(description)) {
                versionUpdate.setDescription("暂无备注");
            }
            else {
                versionUpdate.setDescription(description);
            }
            versionUpdate.setCreatedAt(new Date());
            versionUpdate.setUpdatedAt(new Date());
            versionUpdateService.saveSelective(versionUpdate);
            return versionUpdate;
        });
    }

    /**
     * @param versionUpdate
     */
    private void updateVersionUpdate(String clientType, Integer versionCode, String channel, VersionUpdate versionUpdate) {
        versionUpdate.setUpdatedAt(new Date());
        Example example = new Example(VersionUpdate.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("clientType", clientType)
                .andEqualTo("versionCode", versionCode)
                .andEqualTo("channel", channel);

        versionUpdateService.updateByExampleSelective(versionUpdate, example);
    }
}
