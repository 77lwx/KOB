package com.kob.backend.config.filter;

import com.kob.backend.mapper.UserMapper;
import com.kob.backend.pojo.User;
import com.kob.backend.service.impl.utils.UserDetailsImpl;
import com.kob.backend.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {
    @Autowired
    private UserMapper userMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull FilterChain filterChain) throws ServletException, IOException {
        String token = request.getHeader("Authorization");
        //检查令牌是否非空，并且使用 token.startsWith("Bearer ") 检查令牌是否以 Bearer 开头。
        // 如果令牌不符合要求，则直接调用 filterChain.doFilter(request, response) 继续处理请求，不执行后续逻辑。
        if (!StringUtils.hasText(token) || !token.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        token = token.substring(7);
        String userid;
        try {
            Claims claims = JwtUtil.parseJWT(token);
            userid = claims.getSubject();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        //查询用户信息：使用解析出的用户ID（假设它是整型的）作为参数，调用 userMapper.selectById(Integer.parseInt(userid))
        // 来查询用户信息。如果查询结果为空，则抛出异常，表示用户未登录或令牌无效。
        User user = userMapper.selectById(Integer.parseInt(userid));
        if (user == null) {
            throw new RuntimeException("用户名未登录");
        }
        UserDetailsImpl loginUser = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginUser, null, null);
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        //继续处理请求，这允许后续的 Spring Security 过滤器或控制器接收到经过身份验证的用户信息。
        filterChain.doFilter(request, response);

//        过滤器链中的最后一个过滤器调用 filterChain.doFilter(request, response); 后，
//        请求会被传递给 Spring 的 DispatcherServlet，它是请求处理的核心调度器。
//        DispatcherServlet 将请求映射到具体的控制器（Controller）方法。
//        映射是基于请求的 URL 和控制器中的@RequestMapping 注解完成的。
    }
}

