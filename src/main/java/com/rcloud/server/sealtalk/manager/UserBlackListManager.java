package com.rcloud.server.sealtalk.manager;

import com.rcloud.server.sealtalk.constant.ErrorCode;
import com.rcloud.server.sealtalk.domain.UserBlack;
import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.service.UserBlackListService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class UserBlackListManager extends BaseManager {

    @Resource
    private UserBlackListService userBlackListService;

    /**
     * 获取记录数目
     */
    public Integer getTotalCount() {
        return userBlackListService.getTotalCount();
    }

    /**
     * @return
     */
    public List<UserBlack> getPageUserBlackList(Integer pageNum, Integer pageSize) {
        log.info("UserBlackListManager getPageUserBlackList pageNum:"+pageNum+" pageSize:"+pageSize);
        int offset = (pageNum - 1) * pageSize;
        int limit = pageSize;
        return userBlackListService.getPageUserBlackList(offset, limit);
    }


    /**
     * 根据账号名搜索
     * @param region
     * @param phone
     * @return
     * @throws ServiceException
     */
    public List<UserBlack> getUserBlackListByAccount(String region, String phone) throws ServiceException {
        UserBlack param = new UserBlack();
        param.setRegion(region);
        param.setPhone(phone);

        UserBlack userBlack = userBlackListService.getOne(param);
        if (userBlack == null) {
            throw new ServiceException(ErrorCode.USER_NOT_EXIST);
        }

        List<UserBlack> blackListArrayList = new ArrayList<>();
        blackListArrayList.add(userBlack);

        return blackListArrayList;
    }

    /**
     * 删除用户
     * @param region
     * @param phone
     */
    public void delete(String region, String phone) {
        Example example = new Example(UserBlack.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("region", region);
        criteria.andEqualTo("phone", phone);
        userBlackListService.deleteByExample(example);
    }
}
