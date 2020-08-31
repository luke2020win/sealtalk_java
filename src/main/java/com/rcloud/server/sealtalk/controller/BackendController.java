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

    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-登陆")
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

    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-获取角色列表")
    @RequestMapping(value = "/Role/list", method = RequestMethod.POST)
    public APIResult<Object> roleList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) throws ServiceException {

        log.info("BackendController roleList currentPage:"+currentPage+" pageSize:"+pageSize);

        int pageTemp = 1;
        try {
            pageTemp = Integer.valueOf(currentPage);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        int pageSizeT = 10;
        try {
            pageSizeT = Integer.valueOf(pageSize);
        }
        catch (NumberFormatException numberFormatException) {
           numberFormatException.printStackTrace();
        }

        PageBeanRes<BackendUsers> pageBeanRes = new PageBeanRes();

        log.info("BackendController roleList pageTemp:"+pageTemp+" pageSize:"+pageSizeT);
        List<BackendUsers> backendUsersList = backendUserManager.getPageBackendUsers(pageTemp, pageSizeT);
        if(backendUsersList == null || backendUsersList.isEmpty()) {
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(0);
            pageBeanRes.setData(null);
        }
        else {
            int total = backendUserManager.getTotalCount();
            log.info("BackendController roleList total:"+total);
            pageBeanRes.setPage(pageTemp);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(total);
            pageBeanRes.setData(backendUsersList);
        }

        //对result编码
        return APIResultWrap.ok(pageBeanRes);
    }

    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-编辑/添加角色")
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

    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-根据账户搜索用户")
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

    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-删除角色")
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

    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-获取变量列表")
    @RequestMapping(value = "/Variable/list", method = RequestMethod.POST)
    public APIResult<Object> variableList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) {

        log.info("BackendController variableList currentPage:"+currentPage+" pageSize:"+pageSize);

        int pageT = 1;
        try {
            pageT = Integer.valueOf(currentPage);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        int pageSizeT = 10;
        try {
            pageSizeT = Integer.valueOf(pageSize);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        PageBeanRes<BackendSystemConfig> pageBeanRes = new PageBeanRes<>();

        log.info("BackendController variableList pageTemp:"+pageT+" pageSize:"+pageSizeT);
        List<BackendSystemConfig> backendSystemConfigList = backendSystemConfigManager.getPageBackendSystemConfig(pageT, pageSizeT);
        if(backendSystemConfigList == null || backendSystemConfigList.isEmpty()) {
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(0);
            pageBeanRes.setData(null);
        }
        else {
            int total = backendSystemConfigManager.getTotalCount();
            log.info("BackendController variableList total:"+total);
            pageBeanRes.setPage(pageT);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(total);
            pageBeanRes.setData(backendSystemConfigList);
        }

        //对result编码
        return APIResultWrap.ok(pageBeanRes);
    }


    @CrossOrigin("http://web.hotchatvip.com")
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
            @RequestParam String description) throws ServiceException {

        log.info("BackendController saveVariable varName:"+varName+" varValue:"+varValue+" varDes:"+varDes+" description:"+description);

        ValidateUtils.notEmpty(varName);

        backendSystemConfigManager.saveVariable(varName, varValue, varDes, description);

        return APIResultWrap.ok();
    }


    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-根据账户搜索用户")
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



    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-获取后台白名单列表")
    @RequestMapping(value = "/IpBackendWhite/list", method = RequestMethod.POST)
    public APIResult<Object> ipBackendWhiteList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) throws ServiceException {


        log.info("BackendController ipBackendWhiteList currentPage:"+currentPage+" pageSize:"+pageSize);

        int pageT = 1;
        try {
            pageT = Integer.valueOf(currentPage);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        int pageSizeT = 10;
        try {
            pageSizeT = Integer.valueOf(pageSize);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        PageBeanRes<BackendIPWhite> pageBeanRes = new PageBeanRes<>();

        log.info("BackendController ipBackendWhiteList pageTemp:"+pageT+" pageSize:"+pageSizeT);
        List<BackendIPWhite> backendIPWhites = backendIPWhiteListManager.getPageBackendIPWhiteList(pageT, pageSizeT);
        if(backendIPWhites == null || backendIPWhites.isEmpty()) {
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(0);
            pageBeanRes.setData(null);
        }
        else {
            int total = backendIPWhiteListManager.getTotalCount();
            log.info("BackendController ipBackendWhiteList total:"+total);
            pageBeanRes.setPage(pageT);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(total);
            pageBeanRes.setData(backendIPWhites);
        }

        //对result编码
        return APIResultWrap.ok(pageBeanRes);
    }


    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-编辑/添加后台白名单")
    @RequestMapping(value = "/IpBackendWhite/save", method = RequestMethod.POST)
    public APIResult<Object> saveIpBackendWhite(
            @ApiParam(name = "ip", value = "ip", required = true, type = "String", example = "172.0.0.1")
            @RequestParam String ip,
            @ApiParam(name = "description", value = "ip描述", required = true, type = "String", example = "暂无描述")
            @RequestParam String description) throws ServiceException {

        log.info("BackendController saveIpBackendWhite ip:"+ip+" description:"+description);

        ValidateUtils.notEmpty(ip);

        backendIPWhiteListManager.saveIP(ip, description);

        return APIResultWrap.ok();
    }


    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-根据IP搜索后台白名单")
    @RequestMapping(value = "/IpBackendWhite/search", method = RequestMethod.POST)
    public APIResult<Object> searchIpBackendWhite(
            @ApiParam(name = "ip", value = "ip", required = true, type = "String", example = "admin")
            @RequestParam String ip) throws ServiceException {

        log.info("BackendController searchIpBackendWhite ip:"+ip);


        ValidateUtils.notEmpty(ip);

        List<BackendIPWhite> backendIPWhites = backendIPWhiteListManager.getBackendIPWhiteListByAccount(ip);

        PageBeanRes pageBeanRes = new PageBeanRes();
        pageBeanRes.setPage(1);
        pageBeanRes.setPageSize(10);
        pageBeanRes.setTotal(1);
        pageBeanRes.setData(backendIPWhites);

        return APIResultWrap.ok(pageBeanRes);
    }

    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-删除白名单IP")
    @RequestMapping(value = "/IpBackendWhite/delete", method = RequestMethod.POST)
    public APIResult<Object> deleteBackenWhiteIP(
            @ApiParam(name = "ip", value = "ip", required = true, type = "String", example = "172.1.1.1")
            @RequestParam String ip) throws ServiceException {

        log.info("BackendController deleteBackenWhiteIP ip:"+ip);

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




    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-获取用户黑名单列表")
    @RequestMapping(value = "/IpBlack/list", method = RequestMethod.POST)
    public APIResult<Object> ipBlackList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) throws ServiceException {

        log.info("BackendController ipBlackList currentPage:"+currentPage+" pageSize:"+pageSize);

        int pageT = 1;
        try {
            pageT = Integer.valueOf(currentPage);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        int pageSizeT = 10;
        try {
            pageSizeT = Integer.valueOf(pageSize);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        PageBeanRes<UserIPBlack> pageBeanRes = new PageBeanRes<>();

        log.info("BackendController ipBlackList pageTemp:"+pageT+" pageSize:"+pageSizeT);
        List<UserIPBlack> userIPBlacks = userIPBlackListManager.getPageUserIPBlackList(pageT, pageSizeT);
        if(userIPBlacks == null || userIPBlacks.isEmpty()) {
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(0);
            pageBeanRes.setData(null);
        }
        else {
            int total = userIPBlackListManager.getTotalCount();
            log.info("BackendController ipBlackList total:"+total);
            pageBeanRes.setPage(pageT);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(total);
            pageBeanRes.setData(userIPBlacks);
        }

        //对result编码
        return APIResultWrap.ok(pageBeanRes);
    }


    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-编辑/添加用户IP黑名单")
    @RequestMapping(value = "/IpBlack/save", method = RequestMethod.POST)
    public APIResult<Object> saveIpBlack(
            @ApiParam(name = "ip", value = "ip", required = true, type = "String", example = "172.0.0.1")
            @RequestParam String ip,
            @ApiParam(name = "description", value = "ip描述", required = true, type = "String", example = "暂无描述")
            @RequestParam String description) throws ServiceException {

        log.info("BackendController saveIpBlack ip:"+ip+" description:"+description);

        ValidateUtils.notEmpty(ip);

        userIPBlackListManager.saveIP(ip, description);

        return APIResultWrap.ok();
    }


    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-搜索用户IP")
    @RequestMapping(value = "/IpBlack/search", method = RequestMethod.POST)
    public APIResult<Object> searchIpBlack(
            @ApiParam(name = "ip", value = "ip", required = true, type = "String", example = "admin")
            @RequestParam String ip) throws ServiceException {

        log.info("BackendController searchIpBlack ip:"+ip);

        ValidateUtils.notEmpty(ip);

        List<UserIPBlack> userIPBlacks = userIPBlackListManager.getUserIPBlackListByAccount(ip);

        PageBeanRes pageBeanRes = new PageBeanRes();
        pageBeanRes.setPage(1);
        pageBeanRes.setPageSize(10);
        pageBeanRes.setTotal(1);
        pageBeanRes.setData(userIPBlacks);

        return APIResultWrap.ok(pageBeanRes);
    }

    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-解禁用户IP")
    @RequestMapping(value = "/IpBlack/delete", method = RequestMethod.POST)
    public APIResult<Object> deleteIpBlack(
            @ApiParam(name = "ip", value = "ip", required = true, type = "String", example = "172.1.1.1")
            @RequestParam String ip) throws ServiceException {

        log.info("BackendController deleteIpBlack ip:"+ip);

        ValidateUtils.notEmpty(ip);

        userIPBlackListManager.delete(ip);

        //对result编码
        return APIResultWrap.ok();
    }




    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-获取用户黑名单列表")
    @RequestMapping(value = "/UserBlack/list", method = RequestMethod.POST)
    public APIResult<Object> userBlackList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) throws ServiceException {

        log.info("BackendController userBlackList currentPage:"+currentPage+" pageSize:"+pageSize);

        int pageT = 1;
        try {
            pageT = Integer.valueOf(currentPage);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        int pageSizeT = 10;
        try {
            pageSizeT = Integer.valueOf(pageSize);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        PageBeanRes<UserBlack> pageBeanRes = new PageBeanRes<>();

        log.info("BackendController userBlackList pageTemp:"+pageT+" pageSize:"+pageSizeT);
        List<UserBlack> userBlacks = userBlackListManager.getPageUserBlackList(pageT, pageSizeT);
        if(userBlacks == null || userBlacks.isEmpty()) {
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(0);
            pageBeanRes.setData(null);
        }
        else {
            int total = userIPBlackListManager.getTotalCount();
            log.info("BackendController userBlackList total:"+total);
            pageBeanRes.setPage(pageT);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(total);
            pageBeanRes.setData(userBlacks);
        }

        //对result编码
        return APIResultWrap.ok(pageBeanRes);
    }


    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-搜索用户IP")
    @RequestMapping(value = "/UserBlack/search", method = RequestMethod.POST)
    public APIResult<Object> searchUserBlack(
            @ApiParam(name = "region", value = "区号", type = "String", example = "86")
            @RequestParam String region,
            @ApiParam(name = "phone", value = "手机号", required = true, type = "String", example = "138xxxxxxxx")
            @RequestParam String phone) throws ServiceException {

        log.info("BackendController searchUserBlack region:"+region +" phone:"+phone);

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

    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-解禁用户IP")
    @RequestMapping(value = "/UserBlack/delete", method = RequestMethod.POST)
    public APIResult<Object> deleteUserBlack(
            @ApiParam(name = "region", value = "区号", required = true, type = "String", example = "86")
            @RequestParam String region,
            @ApiParam(name = "phone", value = "手机号", required = true, type = "String", example = "138xxxxxxxx")
            @RequestParam String phone) throws ServiceException {

        log.info("BackendController deleteUserBlack region:"+region);

        ValidateUtils.notEmpty(region);

        ValidateUtils.notEmpty(phone);

        // 从用户黑名单中删除
        userBlackListManager.delete(region, phone);

        // 设置用户表中的数据为可用
        userManager.setStatus(region, phone, 0);

        //对result编码
        return APIResultWrap.ok();
    }




    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-获取注册用户列表")
    @RequestMapping(value = "/User/list", method = RequestMethod.POST)
    public APIResult<Object> userList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) throws ServiceException {

        log.info("BackendController userList currentPage:"+currentPage+" pageSize:"+pageSize);

        int pageT = 1;
        try {
            pageT = Integer.valueOf(currentPage);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        int pageSizeT = 10;
        try {
            pageSizeT = Integer.valueOf(pageSize);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        PageBeanRes<Users> pageBeanRes = new PageBeanRes<>();

        log.info("BackendController userList pageTemp:"+pageT+" pageSize:"+pageSizeT);
        List<Users> usersList = userManager.getPageUserList(pageT, pageSizeT);
        if(usersList == null || usersList.isEmpty()) {
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(0);
            pageBeanRes.setData(null);
        }
        else {
            int total = userManager.getTotalCount();
            log.info("BackendController userList total:"+total);
            pageBeanRes.setPage(pageT);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(total);
            pageBeanRes.setData(usersList);
        }

        //对result编码
        return APIResultWrap.ok(pageBeanRes);
    }


    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-重制密码")
    @RequestMapping(value = "/User/resetPwd", method = RequestMethod.POST)
    public APIResult<Object> resetPwd(
            @ApiParam(name = "region", value = "区号", required = true, type = "String", example = "86")
            @RequestParam String region,
            @ApiParam(name = "phone", value = "手机号", required = true, type = "String", example = "86")
            @RequestParam String phone) throws ServiceException {

        log.info("BackendController resetPwd region:"+region+" phone:"+phone);

        ValidateUtils.notEmpty(region);

        ValidateUtils.notEmpty(phone);

        userManager.resetPwd(region, phone);

        //对result编码
        return APIResultWrap.ok();
    }


    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-设置账号状态")
    @RequestMapping(value = "/User/disableOrEnable", method = RequestMethod.POST)
    public APIResult<Object> disableOrEnableUser(
            @ApiParam(name = "region", value = "区号", required = true, type = "String", example = "86")
            @RequestParam String region,
            @ApiParam(name = "phone", value = "手机号", required = true, type = "String", example = "138xxxxxxxx")
            @RequestParam String phone,
            @ApiParam(name = "isDisable", value = "是否禁用", required = true, type = "String", example = "0")
            @RequestParam String isDisable) throws ServiceException {

        log.info("BackendController resetPwd region:"+region+" phone:"+phone+" isDisable:"+isDisable);

        ValidateUtils.notEmpty(region);

        ValidateUtils.notEmpty(phone);

        userManager.disableOrEnableUser(region, phone, isDisable);

        //对result编码
        return APIResultWrap.ok();
    }

    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-搜索注册用户")
    @RequestMapping(value = "/User/search", method = RequestMethod.POST)
    public APIResult<Object> searchUser(
            @ApiParam(name = "region", value = "区号", required = true, type = "String", example = "86")
            @RequestParam String region,
            @ApiParam(name = "phone", value = "手机号", required = true, type = "String", example = "138xxxxxxxx")
            @RequestParam String phone) throws ServiceException {

        log.info("BackendController searchUser region:"+region+" phone:"+phone);

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


    @CrossOrigin("http://web.hotchatvip.com")
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
            @RequestParam String password) throws ServiceException {

        log.info("BackendController addUser region:"+region+" phone:"+phone+" password:"+password);

        String regionT = "86";
        if(!StringUtils.isEmpty(region)) {
            regionT = region;
        }

        ValidateUtils.notEmpty(phone);

        ValidateUtils.checkPassword(password);

        userManager.addUser(regionT, phone, nickname, password);

        return APIResultWrap.ok();
    }


    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-获取群列表")
    @RequestMapping(value = "/Group/list", method = RequestMethod.POST)
    public APIResult<Object> groupList(
            @ApiParam(name = "currentPage", value = "页码", required = true, type = "String", example = "1")
            @RequestParam String currentPage,
            @ApiParam(name = "pageSize", value = "每页数", required = true, type = "String", example = "10")
            @RequestParam String pageSize) throws ServiceException {

        log.info("BackendController groupList currentPage:"+currentPage+" pageSize:"+pageSize);

        int pageT = 1;
        try {
            pageT = Integer.valueOf(currentPage);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        int pageSizeT = 10;
        try {
            pageSizeT = Integer.valueOf(pageSize);
        }
        catch (NumberFormatException numberFormatException) {
            numberFormatException.printStackTrace();
        }

        PageBeanRes<Groups> pageBeanRes = new PageBeanRes<>();

        log.info("BackendController groupList pageTemp:"+pageT+" pageSize:"+pageSizeT);
        List<Groups> groupsList = groupManager.getPageGroupsList(pageT, pageSizeT);
        if(groupsList == null || groupsList.isEmpty()) {
            pageBeanRes.setPage(1);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(0);
            pageBeanRes.setData(null);
        }
        else {
            int total = groupManager.getTotalCount();
            log.info("BackendController groupList total:"+total);
            pageBeanRes.setPage(pageT);
            pageBeanRes.setPageSize(pageSizeT);
            pageBeanRes.setTotal(total);
            pageBeanRes.setData(groupsList);
        }

        //对result编码
        return APIResultWrap.ok(pageBeanRes);
    }



    @CrossOrigin("http://web.hotchatvip.com")
    @ApiOperation(value = "后台管理-查询群")
    @RequestMapping(value = "/Group/search", method = RequestMethod.POST)
    public APIResult<Object> searchGroup(
            @ApiParam(name = "name", value = "群名", type = "String", example = "86")
            @RequestParam String name) throws ServiceException {

        log.info("BackendController searchGroup name:"+name);

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
