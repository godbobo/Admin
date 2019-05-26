package com.aqzscn.www.global.config.security;

import com.aqzscn.www.global.domain.dto.IErrorCode;
import com.aqzscn.www.global.domain.dto.ReturnVo;
import com.aqzscn.www.global.domain.dto.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * 资源服务配置
 *
 * @author Godbobo
 * @date 2019/5/26
 */
@Configuration
@EnableResourceServer
public class ResourceConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
        // 和授权服务配置中的资源id保持一致 并设置资源仅基于令牌认证
        resources.resourceId("rid").stateless(true);
        // 设置自己的异常处理类
        resources.authenticationEntryPoint((httpServletRequest, httpServletResponse, e) -> {
            // 在这里处理用户没有授权信息或者授权信息错误的情况
            // 组装返回信息
            List<IErrorCode> errorCodes = new ArrayList<>(1);
            Throwable cause = e.getCause();
            if(cause instanceof InvalidTokenException) {
                errorCodes.add(new ValidationResult(401L,"token无效"));
            }else{
                errorCodes.add(new ValidationResult(401L,"没有授权信息"));
            }
            ReturnVo vo = new ReturnVo();
            vo.setErrors(errorCodes);
            vo.setMsg("认证失败");
            // 设置响应体信息
            httpServletResponse.setContentType("application/json;charset=utf-8");
            PrintWriter out = httpServletResponse.getWriter();
            httpServletResponse.setStatus(401);
            ObjectMapper mapper = new ObjectMapper();
            out.write(mapper.writeValueAsString(vo));
            out.flush();
            out.close();
        }).accessDeniedHandler((httpServletRequest, httpServletResponse, e) -> {
            // 组装返回信息
            List<IErrorCode> errorCodes = new ArrayList<>(1);
            errorCodes.add(new ValidationResult(403L, "403 Forbidden"));
            ReturnVo vo = new ReturnVo();
            vo.setErrors(errorCodes);
            vo.setMsg("权限不足，禁止访问");
            // 设置响应体信息
            httpServletResponse.setContentType("application/json;charset=utf-8");
            PrintWriter out = httpServletResponse.getWriter();
            httpServletResponse.setStatus(403);
            ObjectMapper mapper = new ObjectMapper();
            out.write(mapper.writeValueAsString(vo));
            out.flush();
            out.close();
        });
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/g/**").hasRole("ADMIN")
                .antMatchers("/blog/**").hasRole("USER")
                .anyRequest().authenticated();
    }
}
