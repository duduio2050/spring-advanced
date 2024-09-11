package org.example.expert.config;


import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.example.expert.domain.user.enums.UserRole;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Aspect
@Slf4j(topic = "AspectCheckUser")
public class AspectCheckUser {

    @Pointcut("execution(* org.example.expert.domain.comment.controller.CommentAdminController..*(..))")
    private void commentControllerLayer(){

    }

    @Pointcut("execution(* org.example.expert.domain.user.controller.UserAdminController..*(..))")
    private void userControllerLayer(){

    }

    @Before("commentControllerLayer() || userControllerLayer()")
    // 어드바이스들
    public void before(JoinPoint joinPoint) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();

        Long userId = (Long) request.getAttribute("userId");
        String userEmail = (String) request.getAttribute("email");

        String userRoleStr = (String) request.getAttribute("userRole");
        UserRole userRole = UserRole.valueOf(userRoleStr);


        String url = request.getRequestURL().toString();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


        log.info("요청한 사용자의 ID : {}", userId);
        log.info("API 요청 시각 : {}", now.format(formatter));
        log.info("API 요청 URL : {}", url);
    }

}
