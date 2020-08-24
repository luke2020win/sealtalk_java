package com.rcloud.server.sealtalk.controller;

import com.rcloud.server.sealtalk.exception.ServiceException;
import com.rcloud.server.sealtalk.model.response.APIResult;
import com.rcloud.server.sealtalk.model.response.APIResultWrap;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Api(tags = "后台管理系统相关接口")
@RestController
@RequestMapping("/api")
@Slf4j
public class BackendController extends BaseController {

    @ApiOperation(value = "后台管理-登陆")
    @CrossOrigin("http://localhost:9999")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public APIResult<Object> login(
            @ApiParam(name = "username", value = "用户名", required = true, type = "String", example = "admin")
            @RequestParam String username,
            @ApiParam(name = "password", value = "密码", required = true, type = "String", example = "123456")
            @RequestParam String password
    ) throws ServiceException {
        log.info("BackendController login username:"+username+" password:"+password);
        return APIResultWrap.ok("");
    }
}
