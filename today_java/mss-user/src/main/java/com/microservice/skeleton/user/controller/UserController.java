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


    @RequestMapping(value = "/LoginByCAS")
    @IgnoreSecurityCheck
    public void loginByCAS(HttpServletRequest request, HttpServletResponse response) throws Exception {
        userService.loginByCAS(request, response);
    }

    @PostMapping("/LoginByToken")
    @IgnoreSecurityCheck
    public CommonRes loginByToken(@RequestBody @Valid UserLoginToken userLoginVo, BindingResult bindingResult) {
        // 检查参数是否验证错误
        if (bindingResult.hasErrors()) {
            throw new BusinessException(BusinessError.PARAMETER_VALIDATION_ERROR,
                    CommonUtil.processErrorString(bindingResult));
        }
        // 执行登录操作
        return CommonRes.create(userService.loginByToken(userLoginVo));
    }

    @RestController
    @RequestMapping("/cas")
    public class CasProbeController {
        @GetMapping("/whoami")
        public Map<String, Object> whoami(HttpServletRequest req) {
            return Map.of(
                    "remoteUser", req.getRemoteUser(),
                    "principal", req.getUserPrincipal()
            );
        }
    }

}
