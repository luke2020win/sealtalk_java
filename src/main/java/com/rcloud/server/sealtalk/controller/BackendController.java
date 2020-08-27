package com.rcloud.server.sealtalk.controller;

import com.rcloud.server.sealtalk.constant.Constants;
import com.rcloud.server.sealtalk.domain.BackendUsers;
import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.manager.BackendUserManager;
import com.rcloud.server.sealtalk.manager.UserManager;
import com.rcloud.server.sealtalk.model.response.APIResult;
import com.rcloud.server.sealtalk.model.response.APIResultWrap;
import com.rcloud.server.sealtalk.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@Api(tags = "后台管理系统相关接口")
@RestController
@RequestMapping("/api")
@Slf4j
public class BackendController extends BaseController {

    @Resource
    private BackendUserManager  backendUserManager;

    @ApiOperation(value = "后台管理-登陆")
    @CrossOrigin("http://localhost:9999")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public APIResult<Object> login(
            @ApiParam(name = "account", value = "用户名", required = true, type = "String", example = "admin")
            @RequestParam String account,
            @ApiParam(name = "password", value = "密码", required = true, type = "String", example = "123456")
            @RequestParam String password,
            HttpServletResponse response) throws ServiceException {

        log.info("BackendController login account:"+account+" password:"+password);

        ValidateUtils.notEmpty(account);
        ValidateUtils.checkPassword(password);

        BackendUsers backendUsers = backendUserManager.login(account, password);

        //设置cookie  userId加密存入cookie
        //登录成功后的其他请求，当前登录用户useId获取从cookie中获取
        setCookie(response, backendUsers.getId());

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", backendUsers.getId());
        resultMap.put("token", backendUsers.getToken());
        resultMap.put("roleType", backendUsers.getRoleType());


//        Map<String, Object> resultMap = new HashMap<>();
//        resultMap.put("id", "1");
//        resultMap.put("token", "abcdefg");
//        // 1-超级管理员 2-普通管理员
//        resultMap.put("roleType", "超级管理员");

        //对result编码
        return APIResultWrap.ok(MiscUtils.encodeResults(resultMap));
    }

    @ApiOperation(value = "后台管理-获取角色列表")
    @CrossOrigin("http://localhost:9999")
    @RequestMapping(value = "/Role/list", method = RequestMethod.POST)
    public APIResult<Object> roleList(
            @ApiParam(name = "page", value = "页码", required = true, type = "String", example = "admin")
            @RequestParam String page,
            @ApiParam(name = "limit", value = "每页数", required = true, type = "String", example = "123456")
            @RequestParam String limit
    ) throws ServiceException {
        log.info("BackendController roleList page:"+page+" limit:"+limit);
        //对result编码
        return APIResultWrap.ok("");
    }

    @ApiOperation(value = "后台管理-编辑/添加角色")
    @CrossOrigin("http://localhost:9999")
    @RequestMapping(value = "/Role/save", method = RequestMethod.POST)
    public APIResult<Object> saveRole(
            @ApiParam(name = "roleId", value = "角色id", required = true, type = "String", example = "1")
            @RequestParam String roleId,
            @ApiParam(name = "roleAccount", value = "角色用户名", required = true, type = "String", example = "admin")
            @RequestParam String roleAccount,
            @ApiParam(name = "password", value = "密码", required = true, type = "String", example = "123456")
            @RequestParam String password,
            @ApiParam(name = "roleType", value = "角色类型", required = true, type = "String", example = "123456")
            @RequestParam String roleType) throws ServiceException {

        log.info("BackendController saveRole roleAccount:"+roleAccount+" password:"+password+" roleType:"+roleType +" roleId:"+roleId);

        String accountTemp = MiscUtils.xss(roleAccount, ValidateUtils.NICKNAME_MAX_LENGTH);

        Integer id = backendUserManager.register(accountTemp, password, roleType);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", N3d.encode(id));

        return APIResultWrap.ok(resultMap);
    }

    @ApiOperation(value = "后台管理-编辑角色")
    @CrossOrigin("http://localhost:9999")
    @RequestMapping(value = "/Role/delete", method = RequestMethod.DELETE)
    public APIResult<Object> deleteRole(
            @ApiParam(name = "account", value = "用户名", required = true, type = "String", example = "admin")
            @RequestParam String account,
            @ApiParam(name = "password", value = "密码", required = true, type = "String", example = "123456")
            @RequestParam String password,
            HttpServletResponse response) throws ServiceException {

        //log.info("BackendController roleList account:"+account+" password:"+password+" roleType:"+roleType);

        //对result编码
        return APIResultWrap.ok("");
    }

    /**
     * 设置AuthCookie
     *
     * @param response
     * @param userId
     */
    private void setCookie(HttpServletResponse response, int userId) {
        int salt = RandomUtil.randomBetween(1000, 9999);
        String text = salt + Constants.SEPARATOR_NO + userId + Constants.SEPARATOR_NO + System.currentTimeMillis();
        byte[] value = AES256.encrypt(text, sealtalkConfig.getAuthCookieKey());
        Cookie cookie = new Cookie(sealtalkConfig.getAuthCookieName(), new String(value));
        cookie.setHttpOnly(true);
        cookie.setDomain(sealtalkConfig.getAuthCookieDomain());
        cookie.setMaxAge(Integer.valueOf(sealtalkConfig.getAuthCookieMaxAge()));
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
