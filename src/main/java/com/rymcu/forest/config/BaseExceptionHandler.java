package com.rymcu.forest.config;

import com.alibaba.fastjson.support.spring.annotation.FastJsonView;
import com.rymcu.forest.core.exception.BusinessException;
import com.rymcu.forest.core.exception.ServiceException;
import com.rymcu.forest.core.exception.TransactionException;
import com.rymcu.forest.core.result.GlobalResult;
import com.rymcu.forest.core.result.ResultCode;
import org.apache.commons.lang.StringUtils;
import org.apache.shiro.authc.AccountException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器
 *
 * @author ronger
 */
@RestControllerAdvice
public class BaseExceptionHandler {
    Map<Class<? extends Exception>, GlobalResult<ResultCode>> mapper = new HashMap<Class<? extends Exception>, GlobalResult<ResultCode>>();

    {
        mapper.put(UnauthenticatedException.class, new GlobalResult<>(ResultCode.UNAUTHENTICATED));
        mapper.put(UnauthorizedException.class, new GlobalResult<>(ResultCode.UNAUTHORIZED));
        mapper.put(UnknownAccountException.class, new GlobalResult<>(ResultCode.UNKNOWN_ACCOUNT));
        mapper.put(AccountException.class, new GlobalResult<>(ResultCode.INCORRECT_ACCOUNT_OR_PASSWORD));

        GlobalResult<ResultCode> noHandler = new GlobalResult<>(ResultCode.NOT_FOUND);
        noHandler.setMessage(ResultCode.NOT_FOUND.getMessage());
        mapper.put(NoHandlerFoundException.class, noHandler);

        mapper.put(ServletException.class, new GlobalResult<>(ResultCode.FAIL));
        mapper.put(TransactionException.class, new GlobalResult<>());
        mapper.put(BusinessException.class, new GlobalResult<>(ResultCode.INVALID_PARAM));

    }

    private final Logger logger = LoggerFactory.getLogger(BaseExceptionHandler.class);

    @SuppressWarnings("Duplicates")
    @ExceptionHandler(Exception.class)
    public Object errorHandler(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        GlobalResult<ResultCode> res = mapper.computeIfAbsent(ex.getClass(),
                e -> {
                    String message;
                    if (handler instanceof HandlerMethod) {
                        HandlerMethod handlerMethod = (HandlerMethod) handler;
                        message = String.format("接口 [%s] 出现异常，方法：%s.%s，异常摘要：%s",
                                request.getRequestURI(),
                                handlerMethod.getBean().getClass().getName(),
                                handlerMethod.getMethod().getName(),
                                ex.getMessage());
                    } else {
                        message = ex.getMessage();
                    }
                    logger.error(message, ex);
                    return new GlobalResult<>(ResultCode.INTERNAL_SERVER_ERROR);
                });
        if (isAjax(request)) {
            res.setMessage(ex.getMessage());
            res.setSuccess(false);
            return res;
        } else {
            ModelAndView mv = new ModelAndView();
            FastJsonView view = new FastJsonView();
            Map<String, Object> attributes = new HashMap(2);
            attributes.put("code", res.getCode());
            attributes.put("message", res.getMessage());
            attributes.put("success", false);
            view.setAttributesMap(attributes);
            mv.setView(view);
            return mv;
        }
    }

    private boolean isAjax(HttpServletRequest request) {
        String requestedWith = request.getHeader("x-requested-with");
        if (requestedWith != null && "XMLHttpRequest".equalsIgnoreCase(requestedWith)) {
            return true;
        }
        String contentType = request.getContentType();
        return StringUtils.isNotBlank(contentType) && contentType.contains("application/json");
    }
}
