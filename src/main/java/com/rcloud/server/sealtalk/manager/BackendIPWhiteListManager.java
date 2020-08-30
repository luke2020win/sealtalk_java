package com.rcloud.server.sealtalk.manager;

import com.rcloud.server.sealtalk.constant.ErrorCode;
import com.rcloud.server.sealtalk.domain.BackendIPWhite;
import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.interceptor.ServerApiParamHolder;
import com.rcloud.server.sealtalk.model.ServerApiParams;
import com.rcloud.server.sealtalk.service.BackendIPWhiteListService;
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
public class BackendIPWhiteListManager extends BaseManager {

    @Resource
    private BackendIPWhiteListService backendIPWhiteListService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 获取记录数目
     */
    public Integer getTotalCount() {
        return backendIPWhiteListService.getTotalCount();
    }

    /**
     * @return
     */
    public List<BackendIPWhite> getPageBackendIPWhiteList(Integer pageNum, Integer pageSize) {
        log.info("BackendIPWhiteListManager getPageBackendIPWhiteList pageNum:"+pageNum+" pageSize:"+pageSize);
        int offset = (pageNum - 1) * pageSize;
        int limit = pageSize;
        return backendIPWhiteListService.getPageBackendIPWhiteList(offset, limit);
    }

    /**
     * 添加/修改ip
     *
     * @param ip
     * @param description
     * @return
     */
    public void saveIP(String ip, String description) {
        // 查询参数
        BackendIPWhite param = new BackendIPWhite();
        param.setIp(ip);
        // 查询记录
        BackendIPWhite backendIPWhite = backendIPWhiteListService.getOne(param);

        if(backendIPWhite != null) {
            updateBackendIPWhiteList(ip, description);
        }
        else {
            insertBackendIPWhiteList(ip, description);
        }
    }

    /**
     * 注册插入变量 表、dataversion表
     * 同一事务
     *
     * @param ip
     * @param description
     * @return
     */
    private BackendIPWhite insertBackendIPWhiteList(String ip, String description) {
        return transactionTemplate.execute(transactionStatus -> {
            //插入BackendIPWhiteList表
            BackendIPWhite backendIPWhite = new BackendIPWhite();
            backendIPWhite.setIp(ip);
            if(StringUtils.isEmpty(description)) {
                backendIPWhite.setDescription("暂无备注");
            }
            else {
                backendIPWhite.setDescription(description);
            }
            backendIPWhite.setCreatedAt(new Date());
            backendIPWhite.setUpdatedAt(new Date());
            backendIPWhiteListService.saveSelective(backendIPWhite);
            return backendIPWhite;
        });
    }

    /**
     * 更新密码
     * @param ip
     * @param description
     */
    private void updateBackendIPWhiteList(String ip, String description) {
        BackendIPWhite backendIPWhite = new BackendIPWhite();
        if(StringUtils.isEmpty(description)) {
            backendIPWhite.setDescription("暂无备注");
        }
        else {
            backendIPWhite.setDescription(description);
        }
        backendIPWhite.setUpdatedAt(new Date());

        Example example = new Example(BackendIPWhite.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("ip", ip);
        backendIPWhiteListService.updateByExampleSelective(backendIPWhite, example);
    }

    /**
     * 根据IP地址搜索
     * @param ip
     * @return
     * @throws ServiceException
     */
    public List<BackendIPWhite> getBackendIPWhiteListByAccount(String ip) throws ServiceException {
        BackendIPWhite param = new BackendIPWhite();
        param.setIp(ip);

        BackendIPWhite backendIPWhite = backendIPWhiteListService.getOne(param);
        if (backendIPWhite == null) {
            throw new ServiceException(ErrorCode.IP_NOT_EXIST);
        }

        List<BackendIPWhite> backendIPWhiteListArray = new ArrayList<>();
        backendIPWhiteListArray.add(backendIPWhite);

        return backendIPWhiteListArray;
    }

    /**
     * 删除ip
     * @param ip
     */
    public void delete(String ip) {
        Example example = new Example(BackendIPWhite.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("ip", ip);
        backendIPWhiteListService.deleteByExample(example);
    }

    public boolean checkWhiteIp(String ip) {
        log.info("BackendIPWhiteListManager checkWhiteIp ip:"+ip);
        BackendIPWhite param = new BackendIPWhite();
        param.setIp(ip);

        BackendIPWhite backendIPWhite = backendIPWhiteListService.getOne(param);
        if (backendIPWhite != null) {
            return true;
        }

        return false;
    }
}
