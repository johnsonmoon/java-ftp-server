package com.github.johnsonmoon.java.ftp.server.common;

import com.github.johnsonmoon.java.ftp.server.common.entity.AjaxResponse;
import com.github.johnsonmoon.java.ftp.server.common.ext.FtpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletRequest;

/**
 * 全局异常处理类。
 */
@RestControllerAdvice(basePackages = "com.github.johnsonmoon.java.ftp.server.controller")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Throwable.class)
    @ResponseBody
    public ResponseEntity<AjaxResponse<?>> handleControllerException(HttpServletRequest request, Throwable ex) {
        Integer errorCode;
        String message;
        if (FtpException.class.isInstance(ex)) {
            errorCode = 200;
            message = ex.getMessage();
            logger.debug(message, ex);
        } else if (IllegalArgumentException.class.isInstance(ex)) {
            errorCode = 400;
            message = ex.getMessage();
            logger.debug(message, ex);
        } else {
            errorCode = 500;
            message = "An unexpected error occurred, please check the logger for details.";
            logger.error(message, ex);
        }
        AjaxResponse<?> ajaxResponse = new AjaxResponse<>();
        ajaxResponse.setMsg(message);
        ajaxResponse.setStatus(errorCode);
        ajaxResponse.setSuccess(false);
        return new ResponseEntity<>(ajaxResponse, HttpStatus.BAD_REQUEST);
    }
}