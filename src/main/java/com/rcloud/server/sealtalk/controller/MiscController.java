package com.rcloud.server.sealtalk.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rcloud.server.sealtalk.constant.ErrorCode;
import com.rcloud.server.sealtalk.controller.param.ClientVersionParam;
import com.rcloud.server.sealtalk.controller.param.ScreenCaptureParam;
import com.rcloud.server.sealtalk.controller.param.SendMessageParam;
import com.rcloud.server.sealtalk.domain.BackendSystemConfig;
import com.rcloud.server.sealtalk.domain.Groups;
import com.rcloud.server.sealtalk.domain.ScreenStatuses;
import com.rcloud.server.sealtalk.domain.VersionUpdate;
import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.manager.BackendSystemConfigManager;
import com.rcloud.server.sealtalk.manager.GroupManager;
import com.rcloud.server.sealtalk.manager.MiscManager;
import com.rcloud.server.sealtalk.model.dto.DemoSquareDTO;
import com.rcloud.server.sealtalk.model.response.APIResult;
import com.rcloud.server.sealtalk.model.response.APIResultWrap;
import com.rcloud.server.sealtalk.util.*;
import io.micrometer.core.instrument.util.IOUtils;
import io.micrometer.core.instrument.util.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: xiuwei.nie
 * @Author: Jianlu.Yu
 * @Date: 2020/7/6
 * @Description:
 * @Copyright (c) 2020, rongcloud.cn All Rights Reserved
 */
@Api(tags = "其他相关")
@RestController
@RequestMapping("/misc")
@Slf4j
public class MiscController extends BaseController {


    @Value("classpath:squirrel.json")
    private org.springframework.core.io.Resource squirrelResource;

    @Value("classpath:client_version.json")
    private org.springframework.core.io.Resource clientResource;

    @Value("classpath:demo_square.json")
    private org.springframework.core.io.Resource demoSquareResource;

    @Autowired
    private GroupManager groupManager;

    @Autowired
    private MiscManager miscManager;

    @Resource
    private BackendSystemConfigManager backendSystemConfigManager;


    @ApiOperation(value = "获取配置信息")
    @RequestMapping(value = "/app_config_info", method = RequestMethod.GET)
    public APIResult appConfigInfo() {
        List<BackendSystemConfig> backendSystemConfigList = backendSystemConfigManager.getAllBackendSystemConfig();
        return APIResultWrap.ok(backendSystemConfigList);
    }

    @ApiOperation(value = "Android、iOS 获取更新版本")
    @RequestMapping(value = "/client_version", method = RequestMethod.POST)
    public APIResult<Object>  getClientVersion(@RequestBody ClientVersionParam clientVersionParam) {
        try {

            VersionUpdate versionUpdate;
            String clientType = clientVersionParam.getClientType();
            String channel = clientVersionParam.getChannel();
            String version = clientVersionParam.getVersion();
            Integer versionCode = clientVersionParam.getVersionCode();

            log.info("MiscController getClientVersion clientType:"+clientType+" channel:"+channel+" version:"+version);
            ValidateUtils.notEmpty(channel);
            ValidateUtils.notEmpty(version);
            ValidateUtils.notEmpty(clientType);

            versionUpdate = miscManager.getClientVersion(version, versionCode, channel, clientType);
            return APIResultWrap.ok(versionUpdate);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "Server API 发送消息")
    @RequestMapping(value = "/send_message", method = RequestMethod.POST)
    public APIResult sendMessage(@RequestBody SendMessageParam sendMessageParam) {
        try {
            String conversationType = sendMessageParam.getConversationType();
            String targetId = sendMessageParam.getConversationType();
            String objectName = sendMessageParam.getConversationType();
            String content = sendMessageParam.getConversationType();
            String pushContent = sendMessageParam.getPushContent();

            ValidateUtils.notEmpty(conversationType);
            ValidateUtils.notEmpty(targetId);
            ValidateUtils.notEmpty(objectName);
            ValidateUtils.notEmpty(content);

            Integer currentUserId = getCurrentUserId();
            miscManager.sendMessage(currentUserId, conversationType, N3d.decode(targetId), objectName, content, pushContent, targetId);
            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "截屏通知状态设置")
    @RequestMapping(value = "/set_screen_capture", method = RequestMethod.POST)
    public APIResult setScreenCapture(@RequestBody ScreenCaptureParam screenCaptureParam) {
        try {
            Integer conversationType = screenCaptureParam.getConversationType();
            String targetId = screenCaptureParam.getTargetId();
            Integer noticeStatus = screenCaptureParam.getNoticeStatus();

            ValidateUtils.notNull(conversationType);
            ValidateUtils.notEmpty(targetId);
            ValidateUtils.notNull(noticeStatus);

            Integer currentUserId = getCurrentUserId();

            miscManager.setScreenCapture(currentUserId, N3d.decode(targetId), conversationType, noticeStatus);
            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "获取截屏通知状态")
    @RequestMapping(value = "/get_screen_capture", method = RequestMethod.POST)
    public APIResult<Object> getScreenCapture(@RequestBody ScreenCaptureParam screenCaptureParam) {
        try {
            Integer conversationType = screenCaptureParam.getConversationType();
            String targetId = screenCaptureParam.getTargetId();

            ValidateUtils.notNull(conversationType);
            ValidateUtils.notEmpty(targetId);

            Integer currentUserId = getCurrentUserId();

            ScreenStatuses screenStatuses = miscManager.getScreenCapture(currentUserId, N3d.decode(targetId), conversationType);
            Map<String, Object> result = new HashMap<>();
            if (screenStatuses == null) {
                result.put("status", 0);
            } else {
                result.put("status", screenStatuses.getStatus());
            }
            return APIResultWrap.ok(MiscUtils.encodeResults(result));
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "发送截屏通知消息")
    @RequestMapping(value = "/send_sc_msg", method = RequestMethod.POST)
    public APIResult sendScreenCaptureMsg(@RequestBody ScreenCaptureParam screenCaptureParam) {
        try {
            Integer conversationType = screenCaptureParam.getConversationType();
            String targetId = screenCaptureParam.getTargetId();

            ValidateUtils.notNull(conversationType);
            ValidateUtils.notEmpty(targetId);

            Integer currentUserId = getCurrentUserId();

            miscManager.sendScreenCaptureMsg(currentUserId, N3d.decode(targetId), conversationType);

            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "获取客户端最新版本（ Desktop 使用 ）")
    @RequestMapping(value = "/latest_update", method = RequestMethod.GET)
    public void getLatestUpdateVersion(
            @ApiParam(name = "version", value = "版本号", required = true, type = "String", example = "xxx")
            @RequestParam("version") String version,
            HttpServletResponse response) throws ServiceException, IOException {

        try {
            response.setCharacterEncoding("utf8");
            if (StringUtils.isEmpty(version)) {
                response.setStatus(400);
                response.getWriter().write("Invalid version.");
                return;
            }

            String result = CacheUtil.get(CacheUtil.LAST_UPDATE_VERSION_INFO);
            if (StringUtils.isEmpty(result)) {
                String jsonData = IOUtils
                        .toString(squirrelResource.getInputStream(), StandardCharsets.UTF_8);
                result = jsonData;

            }

            String jsonVersion = "";
            JsonNode jsonNode = JacksonUtil.getJsonNode(result);
            if (jsonNode != null) {
                JsonNode v = jsonNode.get("version");
                if (v.isNull()) {
                    response.setStatus(400);
                    response.getWriter().write("Invalid version.");
                    return;
                } else {
                    jsonVersion = v.asText();
                }
            }

            if (version.compareTo(jsonVersion) > 0) {
                response.setStatus(204);
                return;
            }

            CacheUtil.set(CacheUtil.LAST_UPDATE_VERSION_INFO, result);
            response.getWriter().write(result);
            return;
        } catch (IOException e) {
            response.setStatus(500);
            response.getWriter().write("server error.");
        }
    }

    /**
     * Android、iOS 获取更新版本, 返回 sealtalk 标准数据格式
     *
     * @param response
     * @return
     */
    @ApiOperation(value = "Android、iOS 获取更新版本")
    @RequestMapping(value = "/mobile_version", method = RequestMethod.GET)
    public APIResult<?> getMobileVersion(HttpServletResponse response) {
        try {
            response.setCharacterEncoding("utf8");

            String result = CacheUtil.get(CacheUtil.MOBILE_VERSION_INFO);
            if (StringUtils.isEmpty(result)) {
                String jsonData = IOUtils.toString(clientResource.getInputStream(), StandardCharsets.UTF_8);
                result = jsonData;

            }
            CacheUtil.set(CacheUtil.MOBILE_VERSION_INFO, result);

            return APIResultWrap.ok(JacksonUtil.getJsonNode(result));
        } catch (Exception e) {
            return APIResultWrap.error(ErrorCode.SERVER_ERROR);
        }
    }


    @ApiOperation(value = "Android、iOS 获取更新版本")
    @RequestMapping(value = "/demo_square", method = RequestMethod.GET)
    public APIResult<?> getDemoSquare() {
        try {
            String jsonData = IOUtils.toString(demoSquareResource.getInputStream(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            List<DemoSquareDTO> demoSquareDTOList = objectMapper.readValue(jsonData, new TypeReference<List<DemoSquareDTO>>() {});

            List<Integer> groupIds = new ArrayList<>();
            if (CollectionUtils.isEmpty(demoSquareDTOList)) {
                return APIResultWrap.ok(null);
            }

            for (DemoSquareDTO demoSquareDTO : demoSquareDTOList) {
                groupIds.add(demoSquareDTO.getId());
            }
            List<Groups> groupsList = groupManager.getGroupList(groupIds);

            Map<Integer, Groups> groupsMap = new HashMap<>();
            if (groupsList != null) {
                for (Groups groups : groupsList) {
                    groupsMap.put(groups.getId(), groups);
                }
            }

            for (DemoSquareDTO demoSquareDTO : demoSquareDTOList) {
                if ("group".equals(demoSquareDTO.getType())) {
                    Groups groups = groupsMap.get(demoSquareDTO.getId());
                    if (groups == null) {
                        demoSquareDTO.setName("Unknown");
                        demoSquareDTO.setPortraitUri("");
                        demoSquareDTO.setMemberCount(0);
                    } else {
                        demoSquareDTO.setName(groups.getName());
                        demoSquareDTO.setPortraitUri(groups.getPortraitUri());
                        demoSquareDTO.setMemberCount(groups.getMemberCount());
                        demoSquareDTO.setMaxMemberCount(groups.getMaxMemberCount());
                    }
                }
            }
            return APIResultWrap.ok(MiscUtils.encodeResults(demoSquareDTOList));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return APIResultWrap.error(ErrorCode.SERVER_ERROR);
        }
    }
}
