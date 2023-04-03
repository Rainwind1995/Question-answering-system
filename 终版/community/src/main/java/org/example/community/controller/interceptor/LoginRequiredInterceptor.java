package org.example.community.controller.interceptor;


import org.example.community.annotaion.LoginRequired;
import org.example.community.util.HostHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class LoginRequiredInterceptor implements HandlerInterceptor {
    @Autowired
    private HostHolder hostHolder;


    /**
     *
     * @param request
     * @param response
     * @param handler 拦截的目标
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 首先判断下我们拦截的目标是否为一个方法
        if(handler instanceof HandlerMethod){
            // 将Object对象类型转为HandlerMethod
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            // 获取当前方法类型
            LoginRequired method = handlerMethod.getMethodAnnotation(LoginRequired.class);
            // 判断当前的方法是否存在以及用户是否登录
            if(method != null && hostHolder.getUser() == null){
                // 用户没登陆则无法访问当前被注释的方法,重定向到登录界面
                response.sendRedirect(request.getContextPath() + "/login");
                // 拒绝后续的请求
                return false;
            }
        }

        return true;
    }

}
