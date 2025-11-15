package com.microservice.skeleton.user.controller;




import com.microservice.skeleton.user.annotation.IgnoreSecurityCheck;
import com.microservice.skeleton.user.domain.BusinessException;
import com.microservice.skeleton.user.domain.entity.User.CurrentUser;
import com.microservice.skeleton.user.domain.entity.User.UserLoginToken;
import com.microservice.skeleton.user.error.BusinessError;
import com.microservice.skeleton.user.res.CommonRes;
import com.microservice.skeleton.user.service.UserService;
import com.microservice.skeleton.user.util.CommonUtil;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Map;


@RestController
@Api(tags = "用户接口")
@RequestMapping("/user")
@CrossOrigin
@Slf4j
public class UserController {
    @Resource
    private UserService userService;




}
