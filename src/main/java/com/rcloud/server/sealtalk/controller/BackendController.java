package com.rcloud.server.sealtalk.controller;

import com.rcloud.server.sealtalk.constant.Constants;
import com.rcloud.server.sealtalk.controller.param.PageBeanRes;
import com.rcloud.server.sealtalk.domain.BackendSystemConfig;
import com.rcloud.server.sealtalk.domain.BackendUsers;
import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.manager.BackendSystemConfigManager;
import com.rcloud.server.sealtalk.manager.BackendUserManager;
import com.rcloud.server.sealtalk.model.response.APIResult;
import com.rcloud.server.sealtalk.model.response.APIResultWrap;
import com.rcloud.server.sealtalk.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "后台管理系统相关接口")
@RestController
@RequestMapping("/api")
@Slf4j
public class BackendController extends BaseController {

    @Resource
    private BackendUserManager  backendUserManager;

    @Resource
    private BackendSystemConfigManager backendSystemConfigManager;

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
            @ApiParam(name = "page", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String page,
            @ApiParam(name = "limit", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String limit) {

        log.info("BackendController roleList page:"+page+" limit:"+limit);

        int pageTemp = 1;
        try {
            pageTemp = Integer.valueOf(page);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        int pageSize = 10;
        try {
            pageSize = Integer.valueOf(limit);
        }
        catch (NumberFormatException numberFormatException) {
           numberFormatException.printStackTrace();
        }

        PageBeanRes<BackendUsers> pageBeanRes = new PageBeanRes();

        log.info("BackendController roleList pageTemp:"+pageTemp+" pageSize:"+pageSize);
        List<BackendUsers> backendUsersList = backendUserManager.getPageBackendUsers(pageTemp, pageSize);
        if(backendUsersList == null || backendUsersList.isEmpty()) {
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(pageSize);
            pageBeanRes.setTotal(0);
            pageBeanRes.setData(null);
        }
        else {
            int total = backendUserManager.getTotalCount();
            log.info("BackendController roleList total:"+total);
            pageBeanRes.setPage(pageTemp);
            pageBeanRes.setPageSize(pageSize);
            pageBeanRes.setTotal(total);
            pageBeanRes.setData(backendUsersList);
        }

        //对result编码
        return APIResultWrap.ok(pageBeanRes);
    }

    @ApiOperation(value = "后台管理-编辑/添加角色")
    @CrossOrigin("http://localhost:9999")
    @RequestMapping(value = "/Role/save", method = RequestMethod.POST)
    public APIResult<Object> saveRole(
            @ApiParam(name = "account", value = "角色用户名", required = true, type = "String", example = "admin")
            @RequestParam String account,
            @ApiParam(name = "password", value = "密码", required = true, type = "String", example = "123456")
            @RequestParam String password,
            @ApiParam(name = "roleType", value = "角色类型", required = true, type = "String", example = "123456")
            @RequestParam String roleType) throws ServiceException {

        log.info("BackendController saveRole account:"+account+" password:"+password+" roleType:"+roleType);

        Integer id = backendUserManager.saveRole(account, password, roleType);

        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", N3d.encode(id));

        return APIResultWrap.ok(resultMap);
    }

    @ApiOperation(value = "后台管理-根据账户搜索用户")
    @CrossOrigin("http://localhost:9999")
    @RequestMapping(value = "/Role/search", method = RequestMethod.POST)
    public APIResult<Object> searchUser(
            @ApiParam(name = "account", value = "用户名", required = true, type = "String", example = "admin")
            @RequestParam String account) throws ServiceException {

        log.info("BackendController searchUser account:"+account);

        ValidateUtils.notEmpty(account);

        List<BackendUsers> backendUsersList = backendUserManager.getBackendUsersByAccount(account);

        PageBeanRes pageBeanRes = new PageBeanRes();
        pageBeanRes.setPage(1);
        pageBeanRes.setPageSize(10);
        pageBeanRes.setTotal(1);
        pageBeanRes.setData(backendUsersList);

        return APIResultWrap.ok(pageBeanRes);
    }

    @ApiOperation(value = "后台管理-编辑角色")
    @CrossOrigin("http://localhost:9999")
    @RequestMapping(value = "/Role/delete", method = RequestMethod.DELETE)
    public APIResult<Object> delete(
            @ApiParam(name = "account", value = "用户名", required = true, type = "String", example = "admin")
            @RequestParam String account) throws ServiceException {

        log.info("BackendController delete account:"+account);

        ValidateUtils.notEmpty(account);

        backendUserManager.delete(account);

        //对result编码
        return APIResultWrap.ok();
    }

    @ApiOperation(value = "后台管理-获取变量列表")
    @CrossOrigin("http://localhost:9999")
    @RequestMapping(value = "/Variable/list", method = RequestMethod.POST)
    public APIResult<Object> variableList(
            @ApiParam(name = "page", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String page,
            @ApiParam(name = "limit", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String limit) {


        log.info("BackendController variableList page:"+page+" limit:"+limit);

        int pageT = 1;
        try {
            pageT = Integer.valueOf(page);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        int pageSize = 10;
        try {
            pageSize = Integer.valueOf(limit);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        PageBeanRes<BackendSystemConfig> pageBeanRes = new PageBeanRes<>();

        log.info("BackendController variableList pageTemp:"+pageT+" pageSize:"+pageSize);
        List<BackendSystemConfig> backendSystemConfigList = backendSystemConfigManager.getPageBackendSystemConfig(pageT, pageSize);
        if(backendSystemConfigList == null || backendSystemConfigList.isEmpty()) {
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(pageSize);
            pageBeanRes.setTotal(0);
            pageBeanRes.setData(null);
        }
        else {
            int total = backendSystemConfigManager.getTotalCount();
            log.info("BackendController variableList total:"+total);
            pageBeanRes.setPage(pageT);
            pageBeanRes.setPageSize(pageSize);
            pageBeanRes.setTotal(total);
            pageBeanRes.setData(backendSystemConfigList);
        }

        //对result编码
        return APIResultWrap.ok(pageBeanRes);
    }


    @ApiOperation(value = "后台管理-编辑/添加角色")
    @CrossOrigin("http://localhost:9999")
    @RequestMapping(value = "/Variable/save", method = RequestMethod.POST)
    public APIResult<Object> saveVariable(
            @ApiParam(name = "varName", value = "变量名", required = true, type = "String", example = "admin")
            @RequestParam String varName,
            @ApiParam(name = "varValue", value = "变量值", required = true, type = "String", example = "123456")
            @RequestParam String varValue,
            @ApiParam(name = "varDes", value = "变量描述", required = true, type = "String", example = "123456")
            @RequestParam String varDes) throws ServiceException {

        log.info("BackendController saveVariable varName:"+varName+" varValue:"+varValue+" varDes:"+varDes);

        ValidateUtils.notEmpty(varName);

        backendSystemConfigManager.saveVariable(varName, varValue, varDes);

        return APIResultWrap.ok();
    }


    @ApiOperation(value = "后台管理-根据账户搜索用户")
    @CrossOrigin("http://localhost:9999")
    @RequestMapping(value = "/Variable/search", method = RequestMethod.POST)
    public APIResult<Object> searchVariable(
            @ApiParam(name = "varName", value = "变量名", required = true, type = "String", example = "admin")
            @RequestParam String varName) throws ServiceException {

        log.info("BackendController searchVariable varName:"+varName);

        ValidateUtils.notEmpty(varName);

        List<BackendSystemConfig> backendSystemConfigList = backendSystemConfigManager.getBackendSystemConfigByAccount(varName);

        PageBeanRes pageBeanRes = new PageBeanRes();
        pageBeanRes.setPage(1);
        pageBeanRes.setPageSize(10);
        pageBeanRes.setTotal(1);
        pageBeanRes.setData(backendSystemConfigList);

        return APIResultWrap.ok(pageBeanRes);
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
