package com.github.johnsonmoon.java.ftp.server.common.ext;

/**
 * Create by xuyh at 2019/5/8 11:49.
 */
public class FtpException extends RuntimeException {
    public FtpException() {
    }

    public FtpException(String message) {
        super(message);
    }

    public FtpException(String message, Throwable cause) {
        super(message, cause);
    }

    public FtpException(Throwable cause) {
        super(cause);
    }

    public FtpException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
