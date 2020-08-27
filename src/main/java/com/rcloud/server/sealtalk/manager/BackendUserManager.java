package com.rcloud.server.sealtalk.manager;
import com.rcloud.server.sealtalk.constant.Constants;
import com.rcloud.server.sealtalk.constant.ErrorCode;
import com.rcloud.server.sealtalk.domain.*;
import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.rongcloud.RongCloudClient;
import com.rcloud.server.sealtalk.service.*;
import com.rcloud.server.sealtalk.util.*;
import io.rong.models.response.TokenResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
@Slf4j
public class BackendUserManager extends BaseManager {

    @Resource
    private RongCloudClient rongCloudClient;

    @Resource
    private BackendUsersService backendUsersService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 注册
     * @param account
     * @param password
     * @param roleType
     * @return
     * @throws ServiceException
     */
    public Integer register(String account, String password, String roleType) throws ServiceException {
        BackendUsers param = new BackendUsers();
        param.setAccount(account);
        BackendUsers backendUsers = backendUsersService.getOne(param);

        if (backendUsers != null) {
            throw new ServiceException(ErrorCode.PHONE_ALREADY_REGIESTED);
        }

        //如果没有注册过，密码hash
        int salt = RandomUtil.randomBetween(1000, 9999);
        String hashStr = MiscUtils.hash(password, salt);

        BackendUsers u = register0(account, salt, hashStr, roleType);

        return u.getId();
    }

    /**
     * 注册插入user 表、dataversion表
     * 同一事务
     *
     * @param account
     * @param salt
     * @param hashStr
     * @param roleType
     * @return
     */
    private BackendUsers register0(String account, int salt, String hashStr, String roleType) {
        return transactionTemplate.execute(transactionStatus -> {
            //插入user表
            BackendUsers backendUsers = new BackendUsers();
            backendUsers.setAccount(account);
            backendUsers.setPasswordHash(hashStr);
            backendUsers.setRoleType(roleType);
            backendUsers.setPasswordSalt(String.valueOf(salt));
            backendUsers.setIp("0.0.0.0");
            backendUsers.setCreatedAt(new Date());
            backendUsers.setUpdatedAt(backendUsers.getCreatedAt());
            backendUsers.setPortraitUri(sealtalkConfig.getRongcloudDefaultPortraitUrl());
            backendUsersService.saveSelective(backendUsers);
            return backendUsers;
        });
    }

    /**
     * 用户登录
     *
     * @param account
     * @param password
     * @return Pair<L, R> L=用户ID，R=融云token
     * @throws ServiceException
     */
    public BackendUsers login(String account, String password) throws ServiceException {

        BackendUsers param = new BackendUsers();
        param.setAccount(account);
        BackendUsers backendUsers = backendUsersService.getOne(param);

        //判断用户是否存在
        if (backendUsers == null) {
            throw new ServiceException(ErrorCode.USER_NOT_EXIST);
        }

        //校验密码是否正确
        String passwordHash = MiscUtils.hash(password, Integer.valueOf(backendUsers.getPasswordSalt()));
        if (!passwordHash.equals(backendUsers.getPasswordHash())) {
            throw new ServiceException(ErrorCode.USER_PASSWORD_WRONG);
        }

        String token = backendUsers.getToken();
        log.info("login id:" + backendUsers.getId());
        log.info("login account:" + backendUsers.getAccount());
        log.info("login token:" + token);
        log.info("login portraitUri:" + backendUsers.getPortraitUri());


        BackendUsers users = new BackendUsers();
        if (StringUtils.isEmpty(token)) {
            //如果user表中的融云token为空，调用融云sdk 获取token
            //如果用户头像地址为空，采用默认头像地址
            String portraitUri = StringUtils.isEmpty(backendUsers.getPortraitUri()) ? sealtalkConfig.getRongcloudDefaultPortraitUrl() : backendUsers.getPortraitUri();
            TokenResult tokenResult = rongCloudClient.register(backendUsers.getAccount(), backendUsers.getRoleType(), portraitUri);
            if (!Constants.CODE_OK.equals(tokenResult.getCode())) {
                throw new ServiceException(ErrorCode.SERVER_ERROR, "'RongCloud Server API Error Code: " + tokenResult.getCode());
            }

            token = tokenResult.getToken();

            //获取后根据userId更新表中token
            users.setId(backendUsers.getId());
            users.setRoleType(backendUsers.getRoleType());
            users.setToken(token);
            users.setUpdatedAt(new Date());
            backendUsersService.updateByPrimaryKeySelective(users);
        }
        else {
            users.setId(backendUsers.getId());
            users.setRoleType(backendUsers.getRoleType());
            users.setToken(token);
            users.setUpdatedAt(backendUsers.getUpdatedAt());
        }

        //返回userId、token
        return users;
    }

    /**
     * 更新密码或者注册用户
     * @param account
     * @param password
     * @param roleType
     * @return
     */
    public Integer saveRole(String account, String password, String roleType) {
        // 查询参数
        BackendUsers param = new BackendUsers();
        param.setAccount(account);
        // 查询记录
        BackendUsers users = backendUsersService.getOne(param);

        if(users != null) {
            resetPassword(account, password);
            return users.getId();
        }
        else {
            String accountTemp = MiscUtils.xss(account, ValidateUtils.NICKNAME_MAX_LENGTH);
            //如果没有注册过，密码hash
            int salt = RandomUtil.randomBetween(1000, 9999);
            String hashStr = MiscUtils.hash(password, salt);
            BackendUsers u = register0(accountTemp, salt, hashStr, roleType);
            return u.getId();
        }
    }

    /**
     * 删除用户
     * @param account
     */
    public void delete(String account) {
        Example example = new Example(BackendUsers.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("account", account);
        backendUsersService.deleteByExample(example);
    }

    /**
     * 设置当前用户头像
     *
     * @param portraitUri
     * @param account
     * @throws ServiceException
     */
    public void setPortraitUri(String account, String portraitUri) throws ServiceException {
        long timestamp = System.currentTimeMillis();
        BackendUsers param = new BackendUsers();
        param.setAccount(account);
        BackendUsers backendUsers = backendUsersService.getOne(param);
        if (backendUsers == null) {
            throw new ServiceException(ErrorCode.REQUEST_ERROR);
        }

        //修改头像
        BackendUsers users = new BackendUsers();
        users.setId(backendUsers.getId());
        users.setPortraitUri(portraitUri);
        users.setTimestamp(timestamp);
        users.setUpdatedAt(new Date());
        backendUsersService.updateByPrimaryKeySelective(users);
        return;
    }

    /**
     * @return
     */
    public List<BackendUsers> getAllBackendUsers() {
        return backendUsersService.getAllBackendUsers();
    }

    /**
     * 获取记录数目
     */
    public Integer getTotalCount() {
        return backendUsersService.getTotalCount();
    }

    /**
     * @return
     */
    public List<BackendUsers> getPageBackendUsers(Integer pageNum, Integer pageSize) {
        log.info("BackendUserManager getPageBackendUsers pageNum:"+pageNum+" pageSize:"+pageSize);
        int offset = (pageNum - 1) * pageSize;
        int limit = pageSize;
        return backendUsersService.getPageBackendUsers(offset, limit);
    }

    /**
     * 更新密码
     * @param account
     * @param salt
     * @param hashStr
     */
    private void updatePassword(String account, int salt, String hashStr) {
        BackendUsers backendUsers = new BackendUsers();
        backendUsers.setPasswordHash(hashStr);
        backendUsers.setPasswordSalt(String.valueOf(salt));
        backendUsers.setUpdatedAt(new Date());

        Example example = new Example(BackendUsers.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("account", account);
        backendUsersService.updateByExampleSelective(backendUsers, example);
    }

    /**
     * 修改密码
     *
     * @param account
     * @param password
     */
    public void resetPassword(String account, String password) {
        //新密码hash,修改user表密码字段
        int salt = RandomUtil.randomBetween(1000, 9999);
        String hashStr = MiscUtils.hash(password, salt);
        updatePassword(account, salt, hashStr);
    }

    public List<BackendUsers> getBackendUsersByAccount(String account) throws ServiceException {
        BackendUsers param = new BackendUsers();
        param.setAccount(account);

        BackendUsers backendUsers = backendUsersService.getOne(param);
        if (backendUsers == null) {
            throw new ServiceException(ErrorCode.USER_NOT_EXIST);
        }

        List<BackendUsers> backendUsersList = new ArrayList<>();
        backendUsersList.add(backendUsers);

        return backendUsersList;
    }
}

