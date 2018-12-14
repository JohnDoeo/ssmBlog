package com.we.weblog.web.controller.admin;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.kisso.SSOHelper;
import com.baomidou.kisso.annotation.Action;
import com.baomidou.kisso.annotation.Login;
import com.baomidou.kisso.security.token.SSOToken;
import com.we.weblog.domain.Log;
import com.we.weblog.domain.User;
import com.we.weblog.domain.modal.LogActions;
import com.we.weblog.domain.util.BaseConfigUtil;
import com.we.weblog.service.LogsService;
import com.we.weblog.service.UserService;
import com.we.weblog.domain.util.AddressUtil;
import com.we.weblog.web.controller.core.BaseController;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;


/**
 * <pre>
 *     登陆控制界面
 * </pre>
 *
 */
@Controller
public class LoginController extends BaseController {

    private UserService userService;
    private LogsService logService;

     @Autowired
     LoginController(UserService userService,LogsService logService) {
        this.userService = userService;
        this.logService = logService;
    }


    /**
     * 处理跳转到登录页的请求
     *
     * @param session session
     * @return 模板路径admin/admin_login
     */
    @GetMapping(value = "/login")
    public String login(HttpSession session) {
        User user = (User) session.getAttribute(BaseConfigUtil.USER_SESSION_KEY);
        //如果session存在，跳转到后台首页
        if (null != user) {
            return "redirect:/admin";
        }
        return "admin/admin_login";
    }

    /**
     * 登录 （注解跳过权限验证）
     */
    @Login(action = Action.Skip)
    @PostMapping("/doLogin")
    public String getLogin(HttpServletRequest request) throws Exception {

//        WafRequestWrapper wafRequest = new WafRequestWrapper(request);
        String loginName = request.getParameter("username");
        String password = request.getParameter("password");
        //管理员只有一个
        User adminUser = userService.findUser();
        Date loginLast = DateUtil.date();
        if (null != adminUser.getLoginLast()) {
            loginLast = adminUser.getLoginLast();
        }
        Long between = DateUtil.between(loginLast, DateUtil.date(), DateUnit.MINUTE);
        //登陆次数限制
        if (StringUtils.equals(adminUser.getLoginEnable(), "true") && (between < 10)) {
            return "登陆限制";
        }
        //验证用户名和密码
        User user = null;
        if (Validator.isEmail(loginName)) {
            user = userService.userLoginByEmail(loginName, SecureUtil.md5(password));
        } else {
            user = userService.userLoginByName(loginName, password);
        }
        userService.updateUserLoginLast(DateUtil.date());
        //判断User对象是否相等 TODO 这里的逻辑还要修改 比较弱
        if (user != null && adminUser.getUserId() == user.getUserId() ) {
            //重置用户的登录状态为正常
            //创建日志
            Log loginLog = new Log(LogActions.LOGIN,loginName, AddressUtil.getIpAddress(request),1);
            if (logService.saveByLogs(loginLog) < 0)
                throw new Exception("loginLog add error");
            userService.updateUserNormal();
            SSOHelper.setCookie(request, response, SSOToken.create().setIp(request).setId(1000).setIssuer(loginName), false);
            return "redirect:index.html/#/admin/admin_index.html";
        } else{
            //更新失败次数
            Integer errorCount = userService.updateUserLoginError();
            //超过五次禁用账户
            Log loginLog = new Log(LogActions.LOGIN,loginName, AddressUtil.getIpAddress(request),1);
            logService.saveByLogs(loginLog);
            if (errorCount >= 5) {
                userService.updateUserLoginEnable("5");
            }
            return redirectTo("/admin/login.html");
        }

    }

    /**
     *  登出页面
     * @return
     */
    @GetMapping("/logout")
    public String logout() {
        SSOHelper.clearLogin(request, response);
        logService.saveByLogs(new Log(LogActions.LOGOUT,null, AddressUtil.getIpAddress(request),1));
        return "redirect:/" + THEME + "/index";
    }

}
