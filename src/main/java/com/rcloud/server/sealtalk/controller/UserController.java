package com.rcloud.server.sealtalk.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.rcloud.server.sealtalk.constant.Constants;
import com.rcloud.server.sealtalk.constant.ErrorCode;
import com.rcloud.server.sealtalk.constant.SmsServiceType;
import com.rcloud.server.sealtalk.controller.param.UserParam;
import com.rcloud.server.sealtalk.domain.BlackLists;
import com.rcloud.server.sealtalk.domain.Groups;
import com.rcloud.server.sealtalk.domain.Users;
import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.manager.UserManager;
import com.rcloud.server.sealtalk.model.ServerApiParams;
import com.rcloud.server.sealtalk.model.dto.*;
import com.rcloud.server.sealtalk.model.dto.sync.SyncInfoDTO;
import com.rcloud.server.sealtalk.model.response.APIResult;
import com.rcloud.server.sealtalk.model.response.APIResultWrap;
import com.rcloud.server.sealtalk.util.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
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
@Api(tags = "用户相关")
@RestController
@RequestMapping("/user")
@Slf4j
public class UserController extends BaseController {

    @Resource
    private UserManager userManager;

    @ApiOperation(value = "向手机发送验证码(RongCloud)")
    @RequestMapping(value = "/send_code", method = RequestMethod.POST)
    public APIResult sendCode(
            @ApiParam(name = "region", value = "区号", required = true, type = "String", example = "86")
            @RequestParam String region,
            @ApiParam(name = "phone", value = "电话号", required = true, type = "String", example = "188xxxxxxxx")
            @RequestParam String phone){

            try {
                // 万能验证，不验证手机和区域
                if (sealtalkConfig.getIsOpenPassCode() == 1) {
                    ValidateUtils.notEmpty(region);
                    ValidateUtils.notEmpty(phone);
                }
                else {
                    ValidateUtils.checkRegion(region);
                    ValidateUtils.checkCompletePhone(phone);
                }

                // 判断账户黑名单
                userManager.checkBlackUser(region, phone);

                ServerApiParams serverApiParams = getServerApiParams();
                userManager.sendCode(region, phone, SmsServiceType.RONGCLOUD, serverApiParams);
                return APIResultWrap.ok();
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "向手机发送验证码(云片服务)")
    @RequestMapping(value = "/send_code_yp", method = RequestMethod.POST)
    public APIResult sendCodeYp(@RequestBody UserParam userParam){

            try {
                String region = userParam.getRegion();
                String phone = userParam.getPhone();

                region = MiscUtils.removeRegionPrefix(region);

                // 万能验证，不验证手机和区域
                if (sealtalkConfig.getIsOpenPassCode() == 1) {
                    ValidateUtils.notEmpty(region);
                    ValidateUtils.notEmpty(phone);
                }
                else {
                    ValidateUtils.checkRegion(region);
                    ValidateUtils.checkCompletePhone(phone);
                }

                // 判断账户黑名单
                userManager.checkBlackUser(region, phone);

                ServerApiParams serverApiParams = getServerApiParams();
                userManager.sendCode(region, phone, SmsServiceType.YUNPIAN, serverApiParams);
                return APIResultWrap.ok();
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    /**
     * 校验验证码(融云)
     * 1、根据region ,phone 查询验证码
     * -》如果没查询到，返回404，Unknown phone number
     * -》获取当前时间然后减去2分钟，和token的updateAt修改时间比较，判断是否在2分钟有效期内
     * 如果过期，返回2000，Verification code expired
     * -》如果是开发环境或者RONGCLOUD_SMS_REGISTER_TEMPLATE_ID为空，并且验证码参数 code==9999，直接返回成功200，并返回token {verification_token: verification.token}
     * <p>
     * 2、调用融云校验验证码接口
     * -》如果调用接口失败，返回融云接口的错误码和错误信息
     * -》如果调用接口成功，判断返回码
     * -》如果返回码不等于200，返回融云内部错误码和错误信息
     * -》如果返回码等于200，说明校验成功，返回token
     */
    @ApiOperation(value = "校验验证码")
    @RequestMapping(value = "/verify_code", method = RequestMethod.POST)
    public APIResult<Object> verifyCode(@ApiParam(name = "region", value = "区号", required = true, type = "String", example = "86")
                                        @RequestParam String region,
                                        @ApiParam(name = "phone", value = "电话号", required = true, type = "String", example = "188xxxxxxxx")
                                        @RequestParam String phone,
                                        @ApiParam(name = "code", value = "验证码", required = true, type = "String", example = "xxxxxx")
                                        @RequestParam String code){

            try {

                // 万能验证，不验证手机和区域
                if (sealtalkConfig.getIsOpenPassCode() == 1  && Constants.DEFAULT_VERIFY_CODE.equals(code)) {
                    ValidateUtils.notEmpty(region);
                    ValidateUtils.notEmpty(phone);
                    ValidateUtils.notEmpty(code);
                }
                else {
                    ValidateUtils.checkRegion(region);
                    ValidateUtils.checkCompletePhone(phone);
                }

                // 判断账户黑名单
                userManager.checkBlackUser(region, phone);

                String token = userManager.verifyCode(region, phone, code, SmsServiceType.RONGCLOUD);
                Map<String, String> result = new HashMap<>();
                result.put(Constants.VERIFICATION_TOKEN_KEY, token);
                return APIResultWrap.ok(token);
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    /**
     * 校验验证码(云片)
     * 1、region处理，去掉前缀 + 号
     * 2、根据region ,phone 查询验证码
     * -》如果没查询到，返回404，Unknown phone number
     * -》获取当前时间然后减去2分钟，和token的updateAt修改时间比较，判断是否在2分钟有效期内
     * 如果过期，返回2000，Verification code expired
     * -》如果是开发环境，并且验证码参数 code==9999，直接返回成功200，并返回token {verification_token: verification.token}
     * 3、判断验证码是否正确  verification.sessionId == code
     * -》正确，返回200，token verification.token
     * -》错误，返回1000，Invalid verification code.
     */
    @ApiOperation(value = "校验验证码(云片服务)")
    @RequestMapping(value = "/verify_code_yp", method = RequestMethod.POST)
    public APIResult<Object> verifyCodeYP(@RequestBody UserParam userParam){

            try {
                String region = userParam.getRegion();
                String phone = userParam.getPhone();
                String code = userParam.getCode();
                region = MiscUtils.removeRegionPrefix(region);

                // 万能验证，不验证手机和区域
                if (sealtalkConfig.getIsOpenPassCode() == 1 && Constants.DEFAULT_VERIFY_CODE.equals(code)) {
                    ValidateUtils.notEmpty(region);
                    ValidateUtils.notEmpty(phone);
                    ValidateUtils.notEmpty(code);
                }
                else {
                    ValidateUtils.checkRegion(region);
                    ValidateUtils.checkCompletePhone(phone);
                }

                // 判断账户黑名单
                userManager.checkBlackUser(region, phone);

                String token = userManager.verifyCode(region, phone, code, SmsServiceType.YUNPIAN);
                Map<String, String> result = new HashMap<>();
                result.put(Constants.VERIFICATION_TOKEN_KEY, token);
                return APIResultWrap.ok(result);
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    @ApiOperation(value = "获取所有区域信息")
    @RequestMapping(value = "/regionlist", method = RequestMethod.GET)
    public APIResult<Object> regionlist(){

            try {
                JsonNode jsonNode = userManager.getRegionList();
                return APIResultWrap.ok(jsonNode);
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    @ApiOperation(value = "检查手机号是否可以注册")
    @RequestMapping(value = "/check_phone_available", method = RequestMethod.POST)
    public APIResult<Boolean> checkPhoneAvailable(@RequestBody UserParam userParam){

            try {
                String region = userParam.getRegion();
                String phone = userParam.getPhone();

                region = MiscUtils.removeRegionPrefix(region);

                // 万能验证，不验证手机和区域
                if (sealtalkConfig.getIsOpenPassCode() == 1) {
                    ValidateUtils.notEmpty(region);
                    ValidateUtils.notEmpty(phone);
                }
                else {
                    ValidateUtils.checkRegion(region);
                    ValidateUtils.checkCompletePhone(phone);
                }

                if (userManager.isExistUser(region, phone)) {
                    return APIResultWrap.ok(false, "Phone number has already existed.");
                } else {
                    return APIResultWrap.ok(true);
                }
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    /**
     * 0、xss 转义处理 昵称字段
     * 1、密码不能有空格
     * 2、昵称长度[1,32]
     * 3、密码长度[6,20]
     * 4、verificationToken token是uuid格式
     * 5、verificationToken 是否在verification_codes表中存在
     * 6、检查该手机号(Region+phone)是否已经注册过，已经注册过，返回400
     * 7、如果没有注册过，hash生成密码，插入user表
     * 8、然后插入DataVersion表，然后设置cookie，缓存nickname，
     * 9、然后上报管理后台
     * 10、返回注册成功，200，用户主键Id编码
     */
    @ApiOperation(value = "注册新用户")
    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public APIResult<Object> register(@RequestBody UserParam userParam, HttpServletResponse response){

            try {
                String nickname = userParam.getNickname();
                String password = userParam.getPassword();
                String verification_token = userParam.getVerification_token();

                nickname = MiscUtils.xss(nickname, ValidateUtils.NICKNAME_MAX_LENGTH);
                checkRegisterParam(nickname, password, verification_token);

                ServerApiParams serverApiParams = getServerApiParams();
                Integer id = userManager.register(nickname, password, verification_token, serverApiParams);

                //设置cookie
                setCookie(response, id);
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("id", N3d.encode(id));

                return APIResultWrap.ok(resultMap);
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    private void checkRegisterParam(String nickname, String password, String verificationToken) throws ServiceException {
        ValidateUtils.checkPassword(password);
        ValidateUtils.checkNickName(nickname);
        ValidateUtils.checkUUID(verificationToken);
    }

    /**
     * 1、 判断phone、regionName合法性，不合法返回400
     * 2、 根据phone、region查询用户，查询不到返回1000，提示phone不存在
     * 3、 对明文密码加盐hash，验证密码是否正确，密码错误返回1001，提示错误的密码
     * 4、 埋cookie，缓存userid-》nickname
     * 5、 查询该用户所属的所有组
     * 6、 将登录用户的userid、groupIdName信息同步到融云
     * 7、 如果融云token为空，从融云获取token，如果融云token不为空，将userid、融云token返回给前端
     */
    @ApiOperation(value = "用户登录")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public APIResult<Object> login(@RequestBody UserParam userParam, HttpServletResponse response){

            try {
                String region = userParam.getRegion();
                String phone = userParam.getPhone();
                String password = userParam.getPassword();
                region = MiscUtils.removeRegionPrefix(region);

                // 万能验证，不验证手机和区域
                if (sealtalkConfig.getIsOpenPassCode() == 1) {
                    ValidateUtils.notEmpty(region);
                    ValidateUtils.notEmpty(phone);
                }
                else {
                    ValidateUtils.checkRegionName(MiscUtils.getRegionName(region));
                    ValidateUtils.checkCompletePhone(phone);
                }

                // 判断账户黑名单
                userManager.checkBlackUser(region, phone);

                ServerApiParams serverApiParams = getServerApiParams();
                Pair<Integer, String> pairResult = userManager.login(region, phone, password, serverApiParams);

                //设置cookie  userId加密存入cookie
                //登录成功后的其他请求，当前登录用户useId获取从cookie中获取
                setCookie(response, pairResult.getLeft());

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("id", pairResult.getLeft());
                resultMap.put("token", pairResult.getRight());
                resultMap.put("authToken", createAuthToken(pairResult.getLeft()));

                //对result编码
                return APIResultWrap.ok(MiscUtils.encodeResults(resultMap));
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    @ApiOperation(value = "用户注销")
    @RequestMapping(value = "/logout", method = RequestMethod.POST)
    public APIResult logout(HttpServletResponse response) {

        Cookie newCookie = new Cookie(getSealtalkConfig().getAuthCookieName(), null);
        newCookie.setMaxAge(0);
        newCookie.setPath("/");
        response.addCookie(newCookie);
        return APIResultWrap.ok();

    }

    @ApiOperation(value = "重置密码")
    @RequestMapping(value = "/reset_password", method = RequestMethod.POST)
    public APIResult resetPassword(@RequestBody UserParam userParam){

            try {
                String password = userParam.getPassword();
                String verificationToken = userParam.getVerification_token();

                ValidateUtils.checkPassword(password);
                ValidateUtils.checkUUID(verificationToken);

                ServerApiParams serverApiParams = getServerApiParams();
                userManager.resetPassword(password, verificationToken, serverApiParams);

                return APIResultWrap.ok();
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "修改密码")
    @RequestMapping(value = "/change_password", method = RequestMethod.POST)
    public APIResult changePassword(@RequestBody UserParam userParam){

            try {
                String newPassword = userParam.getNewPassword();
                String oldPassword = userParam.getOldPassword();

                ValidateUtils.checkPassword(newPassword);
                ValidateUtils.notEmpty(oldPassword);

                Integer currentUserId = getCurrentUserId();
                ServerApiParams serverApiParams = getServerApiParams();

                userManager.changePassword(newPassword, oldPassword, currentUserId, serverApiParams);
                return APIResultWrap.ok();
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    /**
     * 设置当前用户昵称
     * <p>
     * 1、xss处理nickname
     * 2、校验nickname参数合法性
     * 3、从cookie中获取当前用户id，根据id查询用户信息
     * 4、更新user表中的nickname,timestamp
     * 5、调用融云接口刷新昵称、缓存nickname到本地缓存
     * 6、更新DataVersion表的UserVersion、AllFriendshipVersion
     * 7、移除缓存信息
     * -》Cache.del("user_" + currentUserId);
     * -》Cache.del("friendship_profile_user_" + currentUserId);
     * 8、根据currentUserId 查询所有的friendId，然后清除缓存Cache.del("friendship_all_" + friend.friendId)
     * 9、根据currentUserId查询所有的groupId，然后清除缓存Cache.del("group_members_" + groupMember.groupId)
     * 10、成功后返回200
     */

    @ApiOperation(value = "设置昵称")
    @RequestMapping(value = "/set_nickname", method = RequestMethod.POST)
    public APIResult setNickName(@RequestBody UserParam userParam){

            try {
                String nickname = userParam.getNickname();

                nickname = MiscUtils.xss(nickname, ValidateUtils.NICKNAME_MAX_LENGTH);
                ValidateUtils.checkNickName(nickname);

                Integer currentUserId = getCurrentUserId();
                userManager.setNickName(nickname, currentUserId);

                return APIResultWrap.ok();
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "设置头像")
    @RequestMapping(value = "/set_portrait_uri", method = RequestMethod.POST)
    public APIResult setPortraitUri(@RequestBody UserParam userParam){

            try {
                String portraitUri = userParam.getPortraitUri();

                portraitUri = MiscUtils.xss(portraitUri, ValidateUtils.PORTRAIT_URI_MAX_LENGTH);

                ValidateUtils.checkURLFormat(portraitUri);
                ValidateUtils.checkPortraitUri(portraitUri);

                Integer currentUserId = getCurrentUserId();
                userManager.setPortraitUri(portraitUri, currentUserId);
                return APIResultWrap.ok();
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    @ApiOperation(value = "获取融云token")
    @RequestMapping(value = "/get_token", method = RequestMethod.GET)
    public APIResult<Object> getToken(){

            try {
                Integer currentUserId = getCurrentUserId();
                Pair<Integer, String> pairResult = userManager.getToken(currentUserId);

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("id", pairResult.getLeft());
                resultMap.put("token", pairResult.getRight());

                return APIResultWrap.ok(MiscUtils.encodeResults(resultMap));
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    @ApiOperation(value = "获取当前用户黑名单列表")
    @RequestMapping(value = "/blacklist", method = RequestMethod.GET)
    public APIResult<Object> blacklist(){

            try {
                Integer currentUserId = getCurrentUserId();

                List<BlackLists> resultList = userManager.getBlackList(currentUserId);

                List<BlackListDTO> BlackListDTOList = new ArrayList<>();

                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMATR_PATTERN);

                if (!CollectionUtils.isEmpty(resultList)) {

                    for (BlackLists blackLists : resultList) {
                        BlackListDTO blackListDTO = new BlackListDTO();
                        Users users = blackLists.getUsers();
                        BlackListsUserDTO dto = new BlackListsUserDTO();
                        dto.setId(N3d.encode(users.getId()));
                        dto.setGender(users.getGender());
                        dto.setNickname(users.getNickname());
                        dto.setPortraitUri(users.getPortraitUri());
                        dto.setPhone(users.getPhone());
                        dto.setStAccount(users.getStAccount());
                        dto.setUpdatedAt(sdf.format(users.getUpdatedAt()));
                        dto.setUpdatedTime(users.getUpdatedAt().getTime());
                        blackListDTO.setUser(dto);
                        BlackListDTOList.add(blackListDTO);
                    }
                }
                return APIResultWrap.ok(BlackListDTOList);
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    @ApiOperation(value = "将好友加入黑名单")
    @RequestMapping(value = "/add_to_blacklist", method = RequestMethod.POST)
    public APIResult addBlackList(@RequestBody UserParam userParam){

            try {
                String friendId = userParam.getFriendId();
                ValidateUtils.notEmpty(friendId);

                Integer currentUserId = getCurrentUserId();
                userManager.addBlackList(currentUserId, N3d.decode(friendId), friendId);
                return APIResultWrap.ok();
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    @ApiOperation(value = "将好友移除黑名单")
    @RequestMapping(value = "/remove_from_blacklist", method = RequestMethod.POST)
    public APIResult removeBlacklist(@RequestBody UserParam userParam){

            try {
                String friendId = userParam.getFriendId();
                ValidateUtils.notEmpty(friendId);

                Integer currentUserId = getCurrentUserId();
                userManager.removeBlackList(currentUserId, N3d.decode(friendId), friendId);
                return APIResultWrap.ok();
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    @ApiOperation(value = "获取七牛云存储token")
    @RequestMapping(value = "/get_image_token", method = RequestMethod.GET)
    public APIResult<Object> getImageToken() {

        String token = userManager.getImageToken();

        Map<String, Object> map = new HashMap<>();
        map.put("target", "qiniu");
        map.put("domain", sealtalkConfig.getQiniuBucketDomain());
        map.put("token", token);
        return APIResultWrap.ok(map);
    }


    @ApiOperation(value = "获取短信图片验证码")
    @RequestMapping(value = "/get_sms_img_code", method = RequestMethod.POST)
    public APIResult<Object> getSmsImgCode(){

            try {
                String result = userManager.getSmsImgCode();

                JsonNode jsonNode = JacksonUtil.getJsonNode(result);

                if (jsonNode.get("code").toString().equals("200")) {
                    Map<String, Object> resultMap = new HashMap<>();
                    resultMap.put("url", jsonNode.get("url"));
                    resultMap.put("verifyId", jsonNode.get("verifyId"));
                    return APIResultWrap.ok(resultMap);
                } else {
                    throw new ServiceException(ErrorCode.SERVER_ERROR, "RongCloud Server API Error Code: " + jsonNode.get("code"));
                }
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "获取当前用户所属群组")
    @RequestMapping(value = "/groups", method = RequestMethod.GET)
    public APIResult<Object> getGroups(){

            try {
                Integer currentUserId = getCurrentUserId();
                log.info("getGroups currentUserId:" + currentUserId);
                List<Groups> groupsList = userManager.getGroups(currentUserId);
                log.info("getGroups groupsList size:" + groupsList.size());

                return APIResultWrap.ok(MiscUtils.encodeResults(groupsList, "id", "creatorId"));
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "同步用户的好友、黑名单、群组、群组成员数据")
    @RequestMapping(value = "/sync/{version}", method = RequestMethod.GET)
    public APIResult<Object> syncInfo(@ApiParam(name = "version", value = "请求的版本号(时间戳)", required = true, type = "String", example = "xxx")
                                      @PathVariable("version") String version){

            try {
                ValidateUtils.checkTimeStamp(version);

                Integer currentUserId = getCurrentUserId();

                SyncInfoDTO syncInfoDTO = userManager.getSyncInfo(currentUserId, Long.valueOf(version));
                return APIResultWrap.ok(syncInfoDTO);
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "根据手机号查找用户信息")
    @RequestMapping(value = "/find/{region}/{phone}", method = RequestMethod.GET)
    public APIResult<Object> getUserByPhone(@ApiParam(name = "region", value = "region", required = true, type = "String", example = "xxx")
                                            @PathVariable("region") String region,
                                            @ApiParam(name = "phone", value = "phone", required = true, type = "String", example = "xxx")
                                            @PathVariable("phone") String phone){

            try {
                ValidateUtils.checkRegion(region);
                ValidateUtils.checkCompletePhone(phone);

                Users users = userManager.getUser(region, phone);
                if (users != null) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", users.getId());
                    map.put("nickname", users.getNickname());
                    map.put("portraitUri", users.getPortraitUri());
                    return APIResultWrap.ok(MiscUtils.encodeResults(map));
                } else {
                    throw new ServiceException(ErrorCode.UNKNOW_USER);
                }
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "根据手机号查找用户信息")
    @RequestMapping(value = "/find_user", method = RequestMethod.GET)
    public APIResult<Object> getUserByPhoneOrAccount(@ApiParam(name = "region", value = "region", type = "String", example = "xxx")
                                                     @RequestParam(value = "region", required = false) String region,
                                                     @ApiParam(name = "phone", value = "phone", type = "String", example = "xxx")
                                                     @RequestParam(value = "phone", required = false) String phone,
                                                     @ApiParam(name = "st_account", value = "account", type = "String", example = "xxx")
                                                     @RequestParam(value = "st_account", required = false) String account){

            try {
                if ((!Constants.REGION_NUM.equals(region) || !RegexUtils.checkMobile(phone)) && StringUtils.isEmpty(account)) {
                    throw new ServiceException(ErrorCode.EMPTY_PARAMETER);
                }

                Map<String, Object> map = new HashMap<>();
                if (Constants.REGION_NUM.equals(region) && RegexUtils.checkMobile(phone)) {
                    Users users = userManager.getUser(region, phone);
                    if (users != null && Users.PHONE_VERIFY_NO_NEED.equals(users.getPhoneVerify())) {
                        //用户存在，并且用户允许通过手机号搜索到我
                        map.put("id", users.getId());
                        map.put("nickname", users.getNickname());
                        map.put("portraitUri", users.getPortraitUri());
                        return APIResultWrap.ok(MiscUtils.encodeResults(map));
                    }
                }

                if (StringUtils.isNotEmpty(account)) {
                    Users users = userManager.getUserByStAccount(account);
                    if (users != null && Users.ST_SEARCH_VERIFY_NO_NEED.equals(users.getPhoneVerify())) {
                        // 用户存在并且 用户允许通过st账号搜索到我
                        map.put("id", users.getId());
                        map.put("nickname", users.getNickname());
                        map.put("portraitUri", users.getPortraitUri());
                        return APIResultWrap.ok(MiscUtils.encodeResults(map));
                    }
                }

                return APIResultWrap.error(ErrorCode.USER_NOT_EXIST);
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "获取用户信息")
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public APIResult<Object> getUserInfo(@ApiParam(name = "id", value = "用户ID", required = true, type = "Integer", example = "xxx")
                                         @PathVariable("id") String id){

            try {
                Integer userId = N3d.decode(id);
                log.info("getUserInfo userId:" + userId);
                Users users = userManager.getUser(userId);
                if (users != null) {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setId(N3d.encode(users.getId()));
                    userDTO.setRegion(users.getRegion());
                    userDTO.setNickname(users.getNickname());
                    userDTO.setPortraitUri(users.getPortraitUri());
                    userDTO.setGender(users.getGender());
                    userDTO.setStAccount(users.getStAccount());
                    userDTO.setPhone(users.getPhone());
                    return APIResultWrap.ok(userDTO);
                } else {
                    return APIResultWrap.error(ErrorCode.UNKNOW_USER);
                }
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "获取通讯录群组")
    @RequestMapping(value = "/favgroups", method = RequestMethod.GET)
    public APIResult<Object> getFavGroups(@ApiParam(name = "limit", value = "limit", required = false, type = "Integer", example = "xxx")
                                          @RequestParam(value = "limit", required = false) Integer limit,
                                          @ApiParam(name = "offset", value = "offset", required = false, type = "Integer", example = "xxx")
                                          @RequestParam(value = "offset", required = false) Integer offset){

            try {

                if (limit == null && offset != null) {
                    return APIResultWrap.error(ErrorCode.REQUEST_ERROR);
                }

                Integer currentUserId = getCurrentUserId();
                Pair<Integer, List<Groups>> result = userManager.getFavGroups(currentUserId, limit, offset);

                Integer count = result.getLeft();
                List<Groups> groupsList = result.getRight();

                SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMATR_PATTERN);

                List<FavGroupInfoDTO> favGroupInfoDTOS = new ArrayList<>();
                if (!CollectionUtils.isEmpty(groupsList)) {
                    for (Groups groups : groupsList) {
                        FavGroupInfoDTO favGroupInfoDTO = new FavGroupInfoDTO();
                        favGroupInfoDTO.setId(N3d.encode(groups.getId()));
                        favGroupInfoDTO.setName(groups.getName());
                        favGroupInfoDTO.setPortraitUri(groups.getPortraitUri());
                        favGroupInfoDTO.setMemberCount(groups.getMemberCount());
                        favGroupInfoDTO.setMaxMemberCount(groups.getMaxMemberCount());
                        favGroupInfoDTO.setMemberProtection(groups.getMemberProtection());
                        favGroupInfoDTO.setCreatorId(N3d.encode(groups.getCreatorId()));
                        favGroupInfoDTO.setIsMute(groups.getIsMute());
                        favGroupInfoDTO.setCertiStatus(groups.getCertiStatus());
                        favGroupInfoDTO.setCreatedAt(sdf.format(groups.getCreatedAt()));
                        favGroupInfoDTO.setUpdatedAt(sdf.format(groups.getUpdatedAt()));
                        favGroupInfoDTO.setCreatedTime(groups.getCreatedAt().getTime());
                        favGroupInfoDTO.setUpdatedTime(groups.getUpdatedAt().getTime());
                        favGroupInfoDTOS.add(favGroupInfoDTO);
                    }
                }

                FavGroupsDTO favGroupsDTO = new FavGroupsDTO();
                favGroupsDTO.setLimit(limit);
                favGroupsDTO.setOffset(offset);
                favGroupsDTO.setTotal(count);
                favGroupsDTO.setList(favGroupInfoDTOS);

                return APIResultWrap.ok(favGroupsDTO);
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    @ApiOperation(value = "设置 SealTalk 号")
    @RequestMapping(value = "/set_st_account", method = RequestMethod.POST)
    public APIResult setStAccount(@RequestBody UserParam userParam){

            try {
                String stAccount = userParam.getStAccount();

                ValidateUtils.checkStAccount(stAccount);

                Integer currentUserId = getCurrentUserId();
                userManager.setStAccount(currentUserId, stAccount);
                return APIResultWrap.ok();
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    @ApiOperation(value = "设置性别")
    @RequestMapping(value = "/set_gender", method = RequestMethod.POST)
    public APIResult setGender(@RequestBody UserParam userParam){

            try {
                String gender = userParam.getGender();

                ValidateUtils.checkGender(gender);
                Integer currentUserId = getCurrentUserId();
                Users u = new Users();
                u.setId(currentUserId);
                u.setGender(gender);

                userManager.updateUserById(u);
                return APIResultWrap.ok();
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "设置个人隐私设置")
    @RequestMapping(value = "/set_privacy", method = RequestMethod.POST)
    public APIResult setPrivacy(@RequestBody UserParam userParam){

            try {
                Integer phoneVerify = userParam.getPhoneVerify();
                Integer stSearchVerify = userParam.getStSearchVerify();
                Integer friVerify = userParam.getFriVerify();
                Integer groupVerify = userParam.getGroupVerify();

                ValidateUtils.checkPrivacy(phoneVerify, stSearchVerify, friVerify, groupVerify);
                Integer currentUserId = getCurrentUserId();

                Users users = userManager.getUser(currentUserId);

                Users u = new Users();
                u.setId(users.getId());
                u.setPhoneVerify(phoneVerify);
                u.setStSearchVerify(stSearchVerify);
                u.setFriVerify(friVerify);
                u.setGroupVerify(groupVerify);
                userManager.updateUserById(u);
                return APIResultWrap.ok();
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "获取个人隐私设置")
    @RequestMapping(value = "/get_privacy", method = RequestMethod.GET)
    public APIResult<Object> getPrivacy(){

            try {
                Integer currentUserId = getCurrentUserId();

                Users users = userManager.getUser(currentUserId);

                Map<String, Object> result = new HashMap<>();
                result.put("phoneVerify", users.getPhoneVerify());
                result.put("stSearchVerify", users.getStSearchVerify());
                result.put("friVerify", users.getFriVerify());
                result.put("groupVerify", users.getGroupVerify());
                return APIResultWrap.ok(MiscUtils.encodeResults(result));
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "设置接收戳一下消息状态")
    @RequestMapping(value = "/set_poke", method = RequestMethod.POST)
    public APIResult setPokeStatus(@RequestBody UserParam userParam){

            try {
                Integer pokeStatus = userParam.getPokeStatus();
                ValidateUtils.checkPokeStatus(pokeStatus);

                Integer currentUserId = getCurrentUserId();
                Users u = new Users();
                u.setId(currentUserId);
                u.setPokeStatus(pokeStatus);
                userManager.updateUserById(u);
                return APIResultWrap.ok();
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }

    @ApiOperation(value = "获取接收戳一下消息状态")
    @RequestMapping(value = "/get_poke", method = RequestMethod.GET)
    public APIResult<Object> getPokeStatus(){

            try {
                Integer currentUserId = getCurrentUserId();
                Users users = userManager.getUser(currentUserId);
                Map<String, Object> result = new HashMap<>();
                result.put("pokeStatus", users.getPokeStatus());
                return APIResultWrap.ok(MiscUtils.encodeResults(result));
            }
            catch (ServiceException e) {
                return APIResultWrap.error(e);
            }
    }


    /**
     * 接口文档中没有此接口，nodejs版本代码里存在。
     *
     * @return
     * @throws ServiceException
     */
    @ApiOperation(value = "batch")
    @RequestMapping(value = "/batch", method = RequestMethod.GET)
    public APIResult<Object> batch(@RequestParam("id") String[] id){

            try {
                ValidateUtils.notEmpty(id);
                Integer[] userIds = MiscUtils.decodeIds(id);
                List<Users> userList = userManager.getBatchUser(CollectionUtils.arrayToList(userIds));
                List<Map<String, Object>> resultMap = new ArrayList<>();
                if (!CollectionUtils.isEmpty(userList)) {
                    for (Users u : userList) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("id", N3d.encode(u.getId()));
                        map.put("nickname", u.getNickname());
                        map.put("portraitUri", u.getPortraitUri());
                        resultMap.add(map);
                    }
                }
                return APIResultWrap.ok(resultMap);
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
        int salt = getSalt();
        String text = salt + Constants.SEPARATOR_NO + userId + Constants.SEPARATOR_NO + System.currentTimeMillis();
        byte[] value = AES256.encrypt(text, sealtalkConfig.getAuthCookieKey());
        String authToken = new String(value);
        log.info("createAuthToken authToken:"+authToken);
        return authToken;
    }

    /**
     * 设置AuthCookie
     *
     * @param response
     * @param userId
     */
    private void setCookie(HttpServletResponse response, int userId) {
        int salt = getSalt();
        String text = salt + Constants.SEPARATOR_NO + userId + Constants.SEPARATOR_NO + System.currentTimeMillis();
        byte[] value = AES256.encrypt(text, sealtalkConfig.getAuthCookieKey());
        Cookie cookie = new Cookie(sealtalkConfig.getAuthCookieName(), new String(value));
        cookie.setHttpOnly(true);
        cookie.setDomain(sealtalkConfig.getAuthCookieDomain());
        cookie.setMaxAge(Integer.valueOf(sealtalkConfig.getAuthCookieMaxAge()));
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private int getSalt() {
        int salt = RandomUtil.randomBetween(1000, 9999);
        return salt;
    }
}
