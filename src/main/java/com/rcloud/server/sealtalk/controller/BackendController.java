package com.rcloud.server.sealtalk.controller;

import com.rcloud.server.sealtalk.constant.Constants;
import com.rcloud.server.sealtalk.constant.ErrorCode;
import com.rcloud.server.sealtalk.controller.param.PageBeanRes;
import com.rcloud.server.sealtalk.domain.*;
import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.interceptor.ServerApiParamHolder;
import com.rcloud.server.sealtalk.manager.*;
import com.rcloud.server.sealtalk.model.ServerApiParams;
import com.rcloud.server.sealtalk.model.response.APIResult;
import com.rcloud.server.sealtalk.model.response.APIResultWrap;
import com.rcloud.server.sealtalk.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
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
    private UserManager userManager;

    @Resource
    private BackendUserManager  backendUserManager;

    @Resource
    private BackendSystemConfigManager backendSystemConfigManager;

    @Resource
    private BackendIPWhiteListManager backendIPWhiteListManager;

    @Resource
    private UserIPBlackListManager userIPBlackListManager;

    @Resource
    private UserBlackListManager userBlackListManager;

    @Resource
    private GroupManager groupManager;

    @Resource
    private MiscManager miscManager;

    @ApiOperation(value = "后台管理-登陆")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public APIResult<Object> login(
            @ApiParam(name = "account", value = "用户名", required = true, type = "String", example = "admin")
            @RequestParam String account,
            @ApiParam(name = "password", value = "密码", required = true, type = "String", example = "123456")
            @RequestParam String password){

        log.info("BackendController login account:"+account+" password:"+password);
        try {
            ValidateUtils.notEmpty(account);
            ValidateUtils.checkPassword(password);

            BackendUsers backendUsers = backendUserManager.login(account, password);

            //设置cookie  userId加密存入cookie
            //登录成功后的其他请求，当前登录用户useId获取从cookie中获取
            //setCookie(response, backendUsers.getId());

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", backendUsers.getId());
            resultMap.put("authToken", createAuthToken(backendUsers.getId()));
            resultMap.put("roleType", backendUsers.getRoleType());

//        Map<String, Object> resultMap = new HashMap<>();
//        resultMap.put("id", "1");
//        resultMap.put("token", "abcdefg");
//        // 1-超级管理员 2-普通管理员
//        resultMap.put("roleType", "超级管理员");

            //对result编码
            return APIResultWrap.ok(MiscUtils.encodeResults(resultMap));
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }



    @ApiOperation(value = "后台管理-获取角色列表")
    @RequestMapping(value = "/Role/list", method = RequestMethod.POST)
    public APIResult<Object> roleList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) {

        log.info("BackendController roleList currentPage:"+currentPage+" pageSize:"+pageSize);
        try {
            PageBeanRes<BackendUsers> pageBeanRes = new PageBeanRes();
            int page = TypeConversionUtils.StringToInt(currentPage);
            int pagesize = TypeConversionUtils.StringToInt(pageSize);
            log.info("BackendController roleList page:"+page+" pageSize:"+pagesize);
            List<BackendUsers> backendUsersList = backendUserManager.getPageBackendUsers(page, pagesize);
            if(backendUsersList == null || backendUsersList.isEmpty()) {
                pageBeanRes.setPage(1);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(0);
                pageBeanRes.setData(null);
            }
            else {
                int total = backendUserManager.getTotalCount();
                log.info("BackendController roleList total:"+total);
                pageBeanRes.setPage(page);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(total);
                pageBeanRes.setData(backendUsersList);
            }

            //对result编码
            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "后台管理-编辑/添加角色")
    @RequestMapping(value = "/Role/save", method = RequestMethod.POST)
    public APIResult<Object> saveRole(
            @ApiParam(name = "account", value = "角色用户名", required = true, type = "String", example = "admin")
            @RequestParam String account,
            @ApiParam(name = "password", value = "密码", required = true, type = "String", example = "123456")
            @RequestParam String password,
            @ApiParam(name = "roleType", value = "角色类型", required = true, type = "String", example = "123456")
            @RequestParam String roleType) {

        log.info("BackendController saveRole account:"+account+" password:"+password+" roleType:"+roleType);
        try {
            Integer id = backendUserManager.saveRole(account, password, roleType);

            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("id", N3d.encode(id));

            return APIResultWrap.ok(resultMap);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "后台管理-根据账户搜索用户")
    @RequestMapping(value = "/Role/search", method = RequestMethod.POST)
    public APIResult<Object> searchUser(
            @ApiParam(name = "account", value = "用户名", required = true, type = "String", example = "admin")
            @RequestParam String account) {

        log.info("BackendController searchUser account:"+account);
        try {
            ValidateUtils.notEmpty(account);

            List<BackendUsers> backendUsersList = backendUserManager.getBackendUsersByAccount(account);

            PageBeanRes pageBeanRes = new PageBeanRes();
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(10);
            pageBeanRes.setTotal(1);
            pageBeanRes.setData(backendUsersList);

            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "后台管理-删除角色")
    @RequestMapping(value = "/Role/delete", method = RequestMethod.POST)
    public APIResult<Object> delete(
            @ApiParam(name = "account", value = "用户名", required = true, type = "String", example = "admin")
            @RequestParam String account) {

        log.info("BackendController delete account:"+account);
        try {
            ValidateUtils.notEmpty(account);

            backendUserManager.delete(account);

            //对result编码
            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            //对result编码
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "后台管理-获取变量列表")
    @RequestMapping(value = "/Variable/list", method = RequestMethod.POST)
    public APIResult<Object> variableList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) {

        log.info("BackendController variableList currentPage:"+currentPage+" pageSize:"+pageSize);

        try {
            PageBeanRes<BackendSystemConfig> pageBeanRes = new PageBeanRes<>();
            int page = TypeConversionUtils.StringToInt(currentPage);
            int pagesize = TypeConversionUtils.StringToInt(pageSize);
            log.info("BackendController variableList page:"+page+" pageSize:"+pagesize);
            List<BackendSystemConfig> backendSystemConfigList = backendSystemConfigManager.getPageBackendSystemConfig(page, pagesize);
            if(backendSystemConfigList == null || backendSystemConfigList.isEmpty()) {
                pageBeanRes.setPage(1);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(0);
                pageBeanRes.setData(null);
            }
            else {
                int total = backendSystemConfigManager.getTotalCount();
                log.info("BackendController variableList total:"+total);
                pageBeanRes.setPage(page);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(total);
                pageBeanRes.setData(backendSystemConfigList);
            }

            //对result编码
            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-编辑/添加角色")
    @RequestMapping(value = "/Variable/save", method = RequestMethod.POST)
    public APIResult<Object> saveVariable(
            @ApiParam(name = "varName", value = "变量名", required = true, type = "String", example = "admin")
            @RequestParam String varName,
            @ApiParam(name = "varValue", value = "变量值", required = true, type = "String", example = "123456")
            @RequestParam String varValue,
            @ApiParam(name = "varDes", value = "变量描述", required = true, type = "String", example = "123456")
            @RequestParam String varDes,
            @ApiParam(name = "description", value = "描述", required = true, type = "String", example = "123456")
            @RequestParam String description) {

        log.info("BackendController saveVariable varName:"+varName+" varValue:"+varValue+" varDes:"+varDes+" description:"+description);
        try {
            ValidateUtils.notEmpty(varName);

            backendSystemConfigManager.saveVariable(varName, varValue, varDes, description);

            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-根据账户搜索用户")
    @RequestMapping(value = "/Variable/search", method = RequestMethod.POST)
    public APIResult<Object> searchVariable(
            @ApiParam(name = "varName", value = "变量名", required = true, type = "String", example = "admin")
            @RequestParam String varName) {

        log.info("BackendController searchVariable varName:"+varName);
        try {
            ValidateUtils.notEmpty(varName);

            List<BackendSystemConfig> backendSystemConfigList = backendSystemConfigManager.getBackendSystemConfigByAccount(varName);

            PageBeanRes pageBeanRes = new PageBeanRes();
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(10);
            pageBeanRes.setTotal(1);
            pageBeanRes.setData(backendSystemConfigList);

            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "后台管理-获取后台白名单列表")
    @RequestMapping(value = "/IpBackendWhite/list", method = RequestMethod.POST)
    public APIResult<Object> ipBackendWhiteList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) {


        log.info("BackendController ipBackendWhiteList currentPage:"+currentPage+" pageSize:"+pageSize);
        try {
            PageBeanRes<BackendIPWhite> pageBeanRes = new PageBeanRes<>();
            int page = TypeConversionUtils.StringToInt(currentPage);
            int pagesize = TypeConversionUtils.StringToInt(pageSize);
            log.info("BackendController ipBackendWhiteList page:"+page+" pageSize:"+pagesize);
            List<BackendIPWhite> backendIPWhites = backendIPWhiteListManager.getPageBackendIPWhiteList(page, pagesize);
            if(backendIPWhites == null || backendIPWhites.isEmpty()) {
                pageBeanRes.setPage(1);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(0);
                pageBeanRes.setData(null);
            }
            else {
                int total = backendIPWhiteListManager.getTotalCount();
                log.info("BackendController ipBackendWhiteList total:"+total);
                pageBeanRes.setPage(page);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(total);
                pageBeanRes.setData(backendIPWhites);
            }

            //对result编码
            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "后台管理-编辑/添加后台白名单")
    @RequestMapping(value = "/IpBackendWhite/save", method = RequestMethod.POST)
    public APIResult<Object> saveIpBackendWhite(
            @ApiParam(name = "ip", value = "ip", required = true, type = "String", example = "172.0.0.1")
            @RequestParam String ip,
            @ApiParam(name = "description", value = "ip描述", required = true, type = "String", example = "暂无描述")
            @RequestParam String description) {

        log.info("BackendController saveIpBackendWhite ip:"+ip+" description:"+description);
        try {
            ValidateUtils.notEmpty(ip);

            backendIPWhiteListManager.saveIP(ip, description);

            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "后台管理-根据IP搜索后台白名单")
    @RequestMapping(value = "/IpBackendWhite/search", method = RequestMethod.POST)
    public APIResult<Object> searchIpBackendWhite(
            @ApiParam(name = "ip", value = "ip", required = true, type = "String", example = "admin")
            @RequestParam String ip) {

        log.info("BackendController searchIpBackendWhite ip:"+ip);
        try {

            ValidateUtils.notEmpty(ip);

            List<BackendIPWhite> backendIPWhites = backendIPWhiteListManager.getBackendIPWhiteListByAccount(ip);

            PageBeanRes pageBeanRes = new PageBeanRes();
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(10);
            pageBeanRes.setTotal(1);
            pageBeanRes.setData(backendIPWhites);

            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-删除白名单IP")
    @RequestMapping(value = "/IpBackendWhite/delete", method = RequestMethod.POST)
    public APIResult<Object> deleteBackenWhiteIP(
            @ApiParam(name = "ip", value = "ip", required = true, type = "String", example = "172.1.1.1")
            @RequestParam String ip) {

        log.info("BackendController deleteBackenWhiteIP ip:"+ip);
        try {
            ValidateUtils.notEmpty(ip);

            ServerApiParams serverApiParams = ServerApiParamHolder.get();
            String requestIp = serverApiParams.getRequestUriInfo().getIp();
            if(ip.equals(requestIp)) {
                throw new ServiceException(ErrorCode.IP_NOT_DELETE_SELF);
            }

            backendIPWhiteListManager.delete(ip);

            //对result编码
            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }



    @ApiOperation(value = "后台管理-获取用户黑名单列表")
    @RequestMapping(value = "/IpBlack/list", method = RequestMethod.POST)
    public APIResult<Object> ipBlackList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) {

        log.info("BackendController ipBlackList currentPage:"+currentPage+" pageSize:"+pageSize);
        try {
            PageBeanRes<UserIPBlack> pageBeanRes = new PageBeanRes<>();
            int page = TypeConversionUtils.StringToInt(currentPage);
            int pagesize = TypeConversionUtils.StringToInt(pageSize);
            log.info("BackendController ipBlackList page:"+page+" pagesize:"+pagesize);
            List<UserIPBlack> userIPBlacks = userIPBlackListManager.getPageUserIPBlackList(page, pagesize);
            if(userIPBlacks == null || userIPBlacks.isEmpty()) {
                pageBeanRes.setPage(1);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(0);
                pageBeanRes.setData(null);
            }
            else {
                int total = userIPBlackListManager.getTotalCount();
                log.info("BackendController ipBlackList total:"+total);
                pageBeanRes.setPage(page);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(total);
                pageBeanRes.setData(userIPBlacks);
            }

            //对result编码
            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-编辑/添加用户IP黑名单")
    @RequestMapping(value = "/IpBlack/save", method = RequestMethod.POST)
    public APIResult<Object> saveIpBlack(
            @ApiParam(name = "ip", value = "ip", required = true, type = "String", example = "172.0.0.1")
            @RequestParam String ip,
            @ApiParam(name = "description", value = "ip描述", required = true, type = "String", example = "暂无描述")
            @RequestParam String description) {

        log.info("BackendController saveIpBlack ip:"+ip+" description:"+description);
        try {
            ValidateUtils.notEmpty(ip);

            userIPBlackListManager.saveIP(ip, description);

            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }

    }

    @ApiOperation(value = "后台管理-搜索用户IP")
    @RequestMapping(value = "/IpBlack/search", method = RequestMethod.POST)
    public APIResult<Object> searchIpBlack(
            @ApiParam(name = "ip", value = "ip", required = true, type = "String", example = "admin")
            @RequestParam String ip) {

        log.info("BackendController searchIpBlack ip:"+ip);
        try {
            ValidateUtils.notEmpty(ip);

            List<UserIPBlack> userIPBlacks = userIPBlackListManager.getUserIPBlackListByAccount(ip);

            PageBeanRes pageBeanRes = new PageBeanRes();
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(10);
            pageBeanRes.setTotal(1);
            pageBeanRes.setData(userIPBlacks);

            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-解禁用户IP")
    @RequestMapping(value = "/IpBlack/delete", method = RequestMethod.POST)
    public APIResult<Object> deleteIpBlack(
            @ApiParam(name = "ip", value = "ip", required = true, type = "String", example = "172.1.1.1")
            @RequestParam String ip) {

        log.info("BackendController deleteIpBlack ip:"+ip);
        try {
            ValidateUtils.notEmpty(ip);

            userIPBlackListManager.delete(ip);

            //对result编码
            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }



    @ApiOperation(value = "后台管理-获取用户黑名单列表")
    @RequestMapping(value = "/UserBlack/list", method = RequestMethod.POST)
    public APIResult<Object> userBlackList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) {

        log.info("BackendController userBlackList currentPage:"+currentPage+" pageSize:"+pageSize);
        try {
            PageBeanRes<UserBlack> pageBeanRes = new PageBeanRes<>();
            int page = TypeConversionUtils.StringToInt(currentPage);
            int pagesize = TypeConversionUtils.StringToInt(pageSize);
            log.info("BackendController userBlackList page:"+page+" pageSize:"+pagesize);
            List<UserBlack> userBlacks = userBlackListManager.getPageUserBlackList(page, pagesize);
            if(userBlacks == null || userBlacks.isEmpty()) {
                pageBeanRes.setPage(1);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(0);
                pageBeanRes.setData(null);
            }
            else {
                int total = userIPBlackListManager.getTotalCount();
                log.info("BackendController userBlackList total:"+total);
                pageBeanRes.setPage(page);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(total);
                pageBeanRes.setData(userBlacks);
            }

            //对result编码
            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-搜索用户IP")
    @RequestMapping(value = "/UserBlack/search", method = RequestMethod.POST)
    public APIResult<Object> searchUserBlack(
            @ApiParam(name = "region", value = "区号", type = "String", example = "86")
            @RequestParam String region,
            @ApiParam(name = "phone", value = "手机号", required = true, type = "String", example = "138xxxxxxxx")
            @RequestParam String phone) {

        log.info("BackendController searchUserBlack region:"+region +" phone:"+phone);
        try {
            String regionT;
            if(StringUtils.isEmpty(region)) {
                regionT = "86";
            }
            else {
                regionT = MiscUtils.removeRegionPrefix(region);
            }

            ValidateUtils.notEmpty(phone);

            List<UserBlack> userBlacks = userBlackListManager.getUserBlackListByAccount(regionT, phone);

            PageBeanRes pageBeanRes = new PageBeanRes();
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(10);
            pageBeanRes.setTotal(1);
            pageBeanRes.setData(userBlacks);

            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-解禁用户IP")
    @RequestMapping(value = "/UserBlack/delete", method = RequestMethod.POST)
    public APIResult<Object> deleteUserBlack(
            @ApiParam(name = "region", value = "区号", required = true, type = "String", example = "86")
            @RequestParam String region,
            @ApiParam(name = "phone", value = "手机号", required = true, type = "String", example = "138xxxxxxxx")
            @RequestParam String phone) {

        log.info("BackendController deleteUserBlack region:"+region);
        try {
            ValidateUtils.notEmpty(region);

            ValidateUtils.notEmpty(phone);

            // 从用户黑名单中删除
            userBlackListManager.delete(region, phone);

            // 设置用户表中的数据为可用
            userManager.setStatus(region, phone, 0);

            //对result编码
            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }



    @ApiOperation(value = "后台管理-获取注册用户列表")
    @RequestMapping(value = "/User/list", method = RequestMethod.POST)
    public APIResult<Object> userList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) {

        log.info("BackendController userList currentPage:"+currentPage+" pageSize:"+pageSize);
        try {
            PageBeanRes<Users> pageBeanRes = new PageBeanRes<>();
            int page = TypeConversionUtils.StringToInt(currentPage);
            int pagesize = TypeConversionUtils.StringToInt(pageSize);
            log.info("BackendController userList page:"+page+" pageSize:"+pagesize);
            List<Users> usersList = userManager.getPageUserList(page, pagesize);
            if(usersList == null || usersList.isEmpty()) {
                pageBeanRes.setPage(1);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(0);
                pageBeanRes.setData(null);
            }
            else {
                int total = userManager.getTotalCount();
                log.info("BackendController userList total:"+total);
                pageBeanRes.setPage(page);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(total);
                pageBeanRes.setData(usersList);
            }

            //对result编码
            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-重制密码")
    @RequestMapping(value = "/User/resetPwd", method = RequestMethod.POST)
    public APIResult<Object> resetPwd(
            @ApiParam(name = "region", value = "区号", required = true, type = "String", example = "86")
            @RequestParam String region,
            @ApiParam(name = "phone", value = "手机号", required = true, type = "String", example = "86")
            @RequestParam String phone) {

        log.info("BackendController resetPwd region:"+region+" phone:"+phone);
        try {
            ValidateUtils.notEmpty(region);

            ValidateUtils.notEmpty(phone);

            userManager.resetPwd(region, phone);

            //对result编码
            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-设置账号状态")
    @RequestMapping(value = "/User/disableOrEnable", method = RequestMethod.POST)
    public APIResult<Object> disableOrEnableUser(
            @ApiParam(name = "region", value = "区号", required = true, type = "String", example = "86")
            @RequestParam String region,
            @ApiParam(name = "phone", value = "手机号", required = true, type = "String", example = "138xxxxxxxx")
            @RequestParam String phone,
            @ApiParam(name = "isDisable", value = "是否禁用", required = true, type = "String", example = "0")
            @RequestParam String isDisable) {

        log.info("BackendController resetPwd region:"+region+" phone:"+phone+" isDisable:"+isDisable);
        try {
            ValidateUtils.notEmpty(region);

            ValidateUtils.notEmpty(phone);

            userManager.disableOrEnableUser(region, phone, isDisable);

            //对result编码
            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-搜索注册用户")
    @RequestMapping(value = "/User/search", method = RequestMethod.POST)
    public APIResult<Object> searchUser(
            @ApiParam(name = "region", value = "区号", required = true, type = "String", example = "86")
            @RequestParam String region,
            @ApiParam(name = "phone", value = "手机号", required = true, type = "String", example = "138xxxxxxxx")
            @RequestParam String phone) {

        log.info("BackendController searchUser region:"+region+" phone:"+phone);
        try {
            ValidateUtils.notEmpty(region);

            ValidateUtils.notEmpty(phone);

            List<Users> userList = userManager.getUserByAccount(region, phone);

            PageBeanRes pageBeanRes = new PageBeanRes();
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(10);
            pageBeanRes.setTotal(1);
            pageBeanRes.setData(userList);

            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-添加账户")
    @RequestMapping(value = "/User/add", method = RequestMethod.POST)
    public APIResult<Object> addUser(
            @ApiParam(name = "region", value = "区号", required = true, type = "String", example = "86")
            @RequestParam String region,
            @ApiParam(name = "phone", value = "手机号", required = true, type = "String", example = "138xxxxxxxx")
            @RequestParam String phone,
            @ApiParam(name = "nickname", value = "昵称", required = true, type = "String", example = "138xxxxxxxx")
            @RequestParam String nickname,
            @ApiParam(name = "password", value = "密码", required = true, type = "String", example = "abc123")
            @RequestParam String password) {

        log.info("BackendController addUser region:"+region+" phone:"+phone+" password:"+password);
        try {
            String regionT = "86";
            if(!StringUtils.isEmpty(region)) {
                regionT = region;
            }

            ValidateUtils.notEmpty(phone);

            ValidateUtils.checkPassword(password);

            userManager.addUser(regionT, phone, nickname, password);

            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-获取群列表")
    @RequestMapping(value = "/Group/list", method = RequestMethod.POST)
    public APIResult<Object> groupList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) {

        log.info("BackendController groupList currentPage:"+currentPage+" pageSize:"+pageSize);
        try {
            PageBeanRes<Groups> pageBeanRes = new PageBeanRes<>();
            int page = TypeConversionUtils.StringToInt(currentPage);
            int pagesize = TypeConversionUtils.StringToInt(pageSize);
            log.info("BackendController groupList page:"+page+" pageSize:"+pagesize);
            List<Groups> groupsList = groupManager.getPageGroupsList(page, pagesize);
            if(groupsList == null || groupsList.isEmpty()) {
                pageBeanRes.setPage(1);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(0);
                pageBeanRes.setData(null);
            }
            else {
                int total = groupManager.getTotalCount();
                log.info("BackendController groupList total:"+total);
                pageBeanRes.setPage(page);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(total);
                pageBeanRes.setData(groupsList);
            }

            //对result编码
            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "后台管理-查询群")
    @RequestMapping(value = "/Group/search", method = RequestMethod.POST)
    public APIResult<Object> searchGroup(
            @ApiParam(name = "name", value = "群名", type = "String", example = "86")
            @RequestParam String name) {

        log.info("BackendController searchGroup name:"+name);
        try {
            ValidateUtils.notEmpty(name);

            PageBeanRes<Groups> pageBeanRes = new PageBeanRes<>();

            List<Groups> groupsList = groupManager.getGroupsByName(name);
            if(groupsList == null || groupsList.isEmpty()) {
                pageBeanRes.setPage(1);
                pageBeanRes.setPageSize(10);
                pageBeanRes.setTotal(0);
                pageBeanRes.setData(null);
            }
            else {
                pageBeanRes.setPage(1);
                pageBeanRes.setPageSize(groupsList.size());
                pageBeanRes.setTotal(groupsList.size());
                pageBeanRes.setData(groupsList);
            }

            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }



    @ApiOperation(value = "后台管理-获取变量列表")
    @RequestMapping(value = "/Version/list", method = RequestMethod.POST)
    public APIResult<Object> versionList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) {

        log.info("BackendController versionList currentPage:"+currentPage+" pageSize:"+pageSize);

        try {
            PageBeanRes<VersionUpdate> pageBeanRes = new PageBeanRes<>();
            int page = TypeConversionUtils.StringToInt(currentPage);
            int pagesize = TypeConversionUtils.StringToInt(pageSize);
            log.info("BackendController versionList page:"+page+" pagesize:"+pagesize);
            List<VersionUpdate> versionUpdateList = miscManager.getPageVersionUpdateList(page, pagesize);
            if(versionUpdateList == null || versionUpdateList.isEmpty()) {
                pageBeanRes.setPage(1);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(0);
                pageBeanRes.setData(null);
            }
            else {
                int total = miscManager.getTotalCount();
                log.info("BackendController versionList total:"+total);
                pageBeanRes.setPage(page);
                pageBeanRes.setPageSize(pagesize);
                pageBeanRes.setTotal(total);
                pageBeanRes.setData(versionUpdateList);
            }

            //对result编码
            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-编辑/添加角色")
    @RequestMapping(value = "/Version/save", method = RequestMethod.POST)
    public APIResult<Object> saveVersion(
            @ApiParam(name = "clientType", value = "变量名", required = true, type = "String", example = "ios")
            @RequestParam String clientType,
            @ApiParam(name = "version", value = "变量值", required = true, type = "String", example = "5.0.0")
            @RequestParam String version,
            @ApiParam(name = "channel", value = "变量描述", required = true, type = "String", example = "official")
            @RequestParam String channel,

            @ApiParam(name = "isShowUpdate", value = "变量描述", required = true, type = "String", example = "0")
            @RequestParam String isShowUpdate,
            @ApiParam(name = "isForce", value = "变量描述", required = true, type = "String", example = "0")
            @RequestParam String isForce,
            @ApiParam(name = "isPlist", value = "变量描述", required = true, type = "String", example = "0")
            @RequestParam String isPlist,

            @ApiParam(name = "content", value = "变量描述", required = true, type = "String", example = "更新内容")
            @RequestParam String content,
            @ApiParam(name = "url", value = "变量描述", required = true, type = "String", example = "http://")
            @RequestParam String url,

            @ApiParam(name = "description", value = "备注", required = true, type = "String", example = "暂无备注")
            @RequestParam String description) {

        log.info("BackendController saveVersion clientType:"+clientType+" version:"+version+" channel:"+channel);
        log.info("BackendController saveVersion isShowUpdate:"+isShowUpdate+" isForce:"+isForce+" isPlist:"+isPlist);
        log.info("BackendController saveVersion content:"+content);
        log.info("BackendController saveVersion url:"+url);
        log.info("BackendController saveVersion description:"+description);

        try {
            ValidateUtils.notEmpty(clientType);
            ValidateUtils.notEmpty(version);
            ValidateUtils.notEmpty(channel);

            ValidateUtils.notEmpty(isShowUpdate);
            ValidateUtils.notEmpty(isForce);
            ValidateUtils.notEmpty(isPlist);

            ValidateUtils.notEmpty(content);
            ValidateUtils.notEmpty(url);

            miscManager.saveVersionUpdate(clientType, version, channel, isShowUpdate, isForce, isPlist, content, url, description);
            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }

    @ApiOperation(value = "后台管理-根据账户搜索用户")
    @RequestMapping(value = "/Version/search", method = RequestMethod.POST)
    public APIResult<Object> searchVersion(
            @ApiParam(name = "version", value = "版本号", required = true, type = "String", example = "5.0.0")
            @RequestParam String version) {

        log.info("BackendController searchVersion version:"+version);

        try {
            ValidateUtils.notEmpty(version);

            List<VersionUpdate> backendSystemConfigList = miscManager.getVersionUpdateByVersion(version);

            PageBeanRes pageBeanRes = new PageBeanRes();
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(10);
            pageBeanRes.setTotal(1);
            pageBeanRes.setData(backendSystemConfigList);
            return APIResultWrap.ok(pageBeanRes);
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    @ApiOperation(value = "后台管理-根据账户搜索用户")
    @RequestMapping(value = "/Version/enableOrDisable", method = RequestMethod.POST)
    public APIResult<Object> enableOrDisableVersionUpdate(
            @ApiParam(name = "clientType", value = "客户端类型", required = true, type = "String", example = "ios")
            @RequestParam String clientType,
            @ApiParam(name = "version", value = "版本号", required = true, type = "String", example = "5.0.0")
            @RequestParam String version,
            @ApiParam(name = "channel", value = "渠道号", required = true, type = "String", example = "official")
            @RequestParam String channel,
            @ApiParam(name = "isShowUpdate", value = "是否更新", required = true, type = "String", example = "0")
            @RequestParam String isShowUpdate) {

        log.info("BackendController enableOrDisableVersionUpdate clientType:"+clientType+" version:"+version+" channel:"+channel+" isShowUpdate:"+isShowUpdate);

        try {
            ValidateUtils.notEmpty(clientType);
            ValidateUtils.notEmpty(version);
            ValidateUtils.notEmpty(channel);

            ValidateUtils.notEmpty(isShowUpdate);

            miscManager.enableOrDisableVersionUpdate(clientType, version, channel, isShowUpdate);
            return APIResultWrap.ok();
        }
        catch (ServiceException e) {
            return APIResultWrap.error(e);
        }
    }


    /**
     * createAuthToken
     *
     * @param userId
     */
    private String createAuthToken (int userId) {
        int salt = RandomUtil.randomBetween(1000, 9999);
        String text = salt + Constants.SEPARATOR_NO + userId + Constants.SEPARATOR_NO + System.currentTimeMillis();
        byte[] value = AES256.encrypt(text, sealtalkConfig.getAuthCookieKey());
        String authToken = new String(value);
        log.info("createAuthToken authToken:"+authToken);
        return authToken;
    }
}
