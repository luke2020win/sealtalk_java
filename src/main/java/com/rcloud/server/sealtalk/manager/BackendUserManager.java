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
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StringUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;


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
     * 判断用户是否已经存在
     *
     * @param account
     * @return true 存在，false 不存在
     */
    public boolean isExistUser(String account) {
        BackendUsers param = new BackendUsers();
        param.setAccout(account);
        BackendUsers users = backendUsersService.getOne(param);
        return users != null;
    }

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
        param.setAccout(account);
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
            backendUsers.setAccout(account);
            backendUsers.setPasswordHash(hashStr);
            backendUsers.setRoleType(roleType);
            backendUsers.setPasswordSalt(String.valueOf(salt));
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
    public Pair<Integer, String> login(String account, String password) throws ServiceException {
        BackendUsers param = new BackendUsers();
        param.setAccout(account);
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
        log.info("login account:" + backendUsers.getAccout());
        log.info("login token:" + token);
        log.info("login portraitUri:" + backendUsers.getPortraitUri());

        if (StringUtils.isEmpty(token)) {
            //如果user表中的融云token为空，调用融云sdk 获取token
            //如果用户头像地址为空，采用默认头像地址
            String portraitUri = StringUtils.isEmpty(backendUsers.getPortraitUri()) ? sealtalkConfig.getRongcloudDefaultPortraitUrl() : backendUsers.getPortraitUri();
            TokenResult tokenResult = rongCloudClient.register(backendUsers.getAccout(), backendUsers.getRoleType(), portraitUri);
            if (!Constants.CODE_OK.equals(tokenResult.getCode())) {
                throw new ServiceException(ErrorCode.SERVER_ERROR, "'RongCloud Server API Error Code: " + tokenResult.getCode());
            }

            token = tokenResult.getToken();

            //获取后根据userId更新表中token
            BackendUsers users = new BackendUsers();
            users.setId(backendUsers.getId());
            users.setToken(token);
            users.setUpdatedAt(new Date());
            backendUsersService.updateByPrimaryKeySelective(users);
        }

        //返回userId、token
        return Pair.of(backendUsers.getId(), token);
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

    /**
     * 更新密码
     * @param account
     * @param salt
     * @param hashStr
     */
    private void updatePassword(String account, int salt, String hashStr) {
        BackendUsers user = new BackendUsers();
        user.setPasswordHash(hashStr);
        user.setPasswordSalt(String.valueOf(salt));
        user.setUpdatedAt(new Date());

        Example example = new Example(Users.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("account", account);
        backendUsersService.updateByExampleSelective(user, example);
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
        param.setAccout(account);
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
     * 根据account查询用户信息
     *
     * @param account
     * @return
     */
    public BackendUsers getBackendUserByStAccount(String account) {
        BackendUsers u = new BackendUsers();
        u.setAccout(account);
        return backendUsersService.getOne(u);
    }
}

