package com.rcloud.server.sealtalk.manager;

import com.rcloud.server.sealtalk.constant.ErrorCode;
import com.rcloud.server.sealtalk.domain.BackendIPWhite;
import com.rcloud.server.sealtalk.domain.UserIPBlack;
import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.interceptor.ServerApiParamHolder;
import com.rcloud.server.sealtalk.model.ServerApiParams;
import com.rcloud.server.sealtalk.service.UserIPBlackListService;
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
public class UserIPBlackListManager extends BaseManager {

    @Resource
    private UserIPBlackListService userIPBlackListService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    /**
     * 获取记录数目
     */
    public Integer getTotalCount() {
        return userIPBlackListService.getTotalCount();
    }

    /**
     * @return
     */
    public List<UserIPBlack> getPageUserIPBlackList(Integer pageNum, Integer pageSize) {
        log.info("UserIPBlackListManager getPageUserIPBlackList pageNum:"+pageNum+" pageSize:"+pageSize);
        int offset = (pageNum - 1) * pageSize;
        int limit = pageSize;
        return userIPBlackListService.getPageUserIPBlackList(offset, limit);
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
        UserIPBlack param = new UserIPBlack();
        param.setIp(ip);
        // 查询记录
        UserIPBlack userIPBlack = userIPBlackListService.getOne(param);

        if(userIPBlack != null) {
            updateUserIPBlackList(ip, description);
        }
        else {
            insertUserIPBlackList(ip, description);
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
    private UserIPBlack insertUserIPBlackList(String ip, String description) {
        return transactionTemplate.execute(transactionStatus -> {
            //插入user表
            UserIPBlack userIPBlack = new UserIPBlack();
            userIPBlack.setIp(ip);
            if(StringUtils.isEmpty(description)) {
                userIPBlack.setDescription("暂无备注");
            }

            userIPBlack.setCreatedAt(new Date());
            userIPBlack.setUpdatedAt(new Date());
            userIPBlackListService.saveSelective(userIPBlack);
            return userIPBlack;
        });
    }

    /**
     * 更新密码
     * @param ip
     * @param description
     */
    private void updateUserIPBlackList(String ip, String description) {
        UserIPBlack userIPBlack = new UserIPBlack();
        if(StringUtils.isEmpty(description)) {
            userIPBlack.setDescription("暂无备注");
        }
        else {
            userIPBlack.setDescription(description);
        }

        userIPBlack.setUpdatedAt(new Date());

        Example example = new Example(UserIPBlack.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("ip", ip);
        userIPBlackListService.updateByExampleSelective(userIPBlack, example);
    }

    /**
     * 根据IP地址搜索
     * @param ip
     * @return
     * @throws ServiceException
     */
    public List<UserIPBlack> getUserIPBlackListByAccount(String ip) throws ServiceException {
        UserIPBlack param = new UserIPBlack();
        param.setIp(ip);

        UserIPBlack userIPBlack = userIPBlackListService.getOne(param);
        if (userIPBlack == null) {
            throw new ServiceException(ErrorCode.IP_NOT_EXIST);
        }

        List<UserIPBlack> userIPBlackListArray = new ArrayList<>();
        userIPBlackListArray.add(userIPBlack);

        return userIPBlackListArray;
    }

    /**
     * 删除ip
     * @param ip
     */
    public void delete(String ip) {
        Example example = new Example(UserIPBlack.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("ip", ip);
        userIPBlackListService.deleteByExample(example);
    }

    public boolean checkBlackIp(String ip) {
        log.info("UserIPBlackListManager checkBlackIp ip:"+ip);

        UserIPBlack param = new UserIPBlack();
        param.setIp(ip);

        UserIPBlack userIPBlack = userIPBlackListService.getOne(param);
        if (userIPBlack != null) {
            return true;
        }

        return false;
    }
}
