package com.rcloud.server.sealtalk.manager;

import com.rcloud.server.sealtalk.constant.ErrorCode;
import com.rcloud.server.sealtalk.domain.BackendSystemConfig;
import com.rcloud.server.sealtalk.domain.BackendUsers;
import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.service.*;
import com.rcloud.server.sealtalk.util.MiscUtils;
import com.rcloud.server.sealtalk.util.RandomUtil;
import com.rcloud.server.sealtalk.util.ValidateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
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
public class BackendSystemConfigManager extends BaseManager {

    @Resource
    private BackendSystemConfigService backendSystemConfigService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * @return
     */
    public List<BackendSystemConfig> getAllBackendSystemConfig() {
        return backendSystemConfigService.getAllBackendSystemConfig();
    }

    /**
     * 获取记录数目
     */
    public Integer getTotalCount() {
        return backendSystemConfigService.getTotalCount();
    }

    /**
     * @return
     */
    public List<BackendSystemConfig> getPageBackendSystemConfig(Integer pageNum, Integer pageSize) {
        log.info("BackendSystemConfigManager getPageBackendSystemConfig pageNum:"+pageNum+" pageSize:"+pageSize);
        int offset = (pageNum - 1) * pageSize;
        int limit = pageSize;
        return backendSystemConfigService.getPageBackendSystemConfig(offset, limit);
    }

    /**
     * 更新变量
     *
     * @param varName
     * @param varValue
     * @param varDes
     * @return
     */
    public void saveVariable(String varName, String varValue, String varDes, String description) {
        // 查询参数
        BackendSystemConfig param = new BackendSystemConfig();
        param.setVarName(varName);
        // 查询记录
        BackendSystemConfig users = backendSystemConfigService.getOne(param);

        if(users != null) {
            updateBackendSystemConfig(varName, varValue, varDes, description);
        }
        else {
            insertBackendSystemConfig(varName, varValue, varDes, description);
        }
    }

    /**
     * 注册插入变量 表、dataversion表
     * 同一事务
     *
     * @param varName
     * @param varValue
     * @param varDes
     * @return
     */
    private BackendSystemConfig insertBackendSystemConfig(String varName, String varValue, String varDes, String description) {
        return transactionTemplate.execute(transactionStatus -> {
            //插入user表
            BackendSystemConfig backendSystemConfig = new BackendSystemConfig();
            backendSystemConfig.setVarName(varName);
            backendSystemConfig.setVarValue(varValue);
            backendSystemConfig.setVarDes(varDes);
            backendSystemConfig.setDescription(description);
            backendSystemConfig.setCreatedAt(new Date());
            backendSystemConfig.setUpdatedAt(new Date());
            backendSystemConfigService.saveSelective(backendSystemConfig);
            return backendSystemConfig;
        });
    }

    /**
     * 更新密码
     * @param varName
     * @param varValue
     * @param varDes
     */
    private void updateBackendSystemConfig(String varName, String varValue, String varDes, String description) {
        BackendSystemConfig backendSystemConfig = new BackendSystemConfig();
        backendSystemConfig.setVarValue(varValue);
        backendSystemConfig.setVarDes(varDes);
        backendSystemConfig.setDescription(description);
        backendSystemConfig.setUpdatedAt(new Date());

        Example example = new Example(BackendSystemConfig.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("varName", varName);
        backendSystemConfigService.updateByExampleSelective(backendSystemConfig, example);
    }

    /**
     * 根据变量名搜索
     * @param varName
     * @return
     * @throws ServiceException
     */
    public List<BackendSystemConfig> getBackendSystemConfigByAccount(String varName) throws ServiceException {
        BackendSystemConfig param = new BackendSystemConfig();
        param.setVarName(varName);

        BackendSystemConfig backendSystemConfig = backendSystemConfigService.getOne(param);
        if (backendSystemConfig == null) {
            throw new ServiceException(ErrorCode.VAR_NOT_EXIST);
        }

        List<BackendSystemConfig> backendSystemConfigList = new ArrayList<>();
        backendSystemConfigList.add(backendSystemConfig);

        return backendSystemConfigList;
    }
}
