package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.LOGIN_USER_TTL;

public class RefreshTokenInterceptor implements HandlerInterceptor {

    private StringRedisTemplate stringRedisTemplate;

    public RefreshTokenInterceptor(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1. 获取session
        //HttpSession session = request.getSession();
        //2. 获取session中的用户
        //Object user = session.getAttribute("user");

        //3. 判断用户是否存在
        /*if(user == null){
            //4. 不存在，拦截，返回401状态码
            response.setStatus(401);
            return false;
        }*/
        //5. 存在，保存用户信息到threadlocal
        //UserHolder.saveUser((UserDTO) user);
        //6. 放行

        //1. 获取请求头里的token
        String token = request.getHeader("authorization");
        if(StrUtil.isBlank(token)){
            /*//4. 不存在，拦截，返回401状态码
            response.setStatus(401);
            return false;*/
            return true;
        }

        //2.基于token获取redis中的用户
        String key = RedisConstants.LOGIN_USER_KEY + token;
        Map<Object, Object> userMap = stringRedisTemplate.opsForHash()
                .entries(key);
        //3. 判断用户是否存在
        if(userMap.isEmpty()){
            /*//4. 不存在，拦截，返回401状态码
            response.setStatus(401);
            return false;*/
            return true;
        }

        //5. 将查询到的hash数据转化为userDTO对象
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);

        //6. 存在，保存用户信息到threadlocal
        UserHolder.saveUser(userDTO);

        //7. 刷新token有效期
        stringRedisTemplate.expire(key, LOGIN_USER_TTL, TimeUnit.MINUTES);

        return true;

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {
        UserHolder.removeUser();
    }
}
