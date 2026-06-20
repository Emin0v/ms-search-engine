package com.company.searchengine.interceptor;

import static com.company.searchengine.model.constant.ApplicationConstants.HttpAttribute.REQUEST_START_TIME;

import com.company.searchengine.util.WebUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class RequestInterceptor implements HandlerInterceptor {

    private final WebUtil webUtil;

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) throws Exception {
        log.trace("CommonRequestInterceptor is calling for uri: {} {}", req.getMethod(), req.getRequestURI());

        req.setAttribute(REQUEST_START_TIME, System.currentTimeMillis());
        final var projectBasedHeaders = webUtil.getAcHeaders();
        log.debug("Project Based Headers in CommonRequestInterceptor: {}", projectBasedHeaders);
        log.trace("Non Project Based Headers in CommonRequestInterceptor: {}", webUtil.getNonAcHeaders());
        projectBasedHeaders.forEach((header, value) -> MDC.put(header.toLowerCase(), value));

        return HandlerInterceptor.super.preHandle(req, res, handler);
    }

    @Override
    public void afterCompletion(HttpServletRequest req, HttpServletResponse res, Object handler, Exception ex) {
        MDC.clear();
        req.removeAttribute(REQUEST_START_TIME);
        log.trace("CommonRequestInterceptor ended for uri: {} {}. MDC cleared", req.getMethod(), req.getRequestURI());
    }

}