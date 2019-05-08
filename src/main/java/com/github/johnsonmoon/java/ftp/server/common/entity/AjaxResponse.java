package com.github.johnsonmoon.java.ftp.server.common.entity;


import java.io.Serializable;
import java.util.Map;

public class AjaxResponse<T> implements Serializable {
    private static final long serialVersionUID = 45387487319877474L;
    private int status;
    private String msg;
    private T data;
    private boolean success;

    public AjaxResponse() {
        status = 200;
    }

    public AjaxResponse(int status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public static <T> AjaxResponse<T> getFailResponse(String errorMsg) {
        AjaxResponse<T> resp = new AjaxResponse<T>();
        resp.setStatus(500);
        resp.setMsg(errorMsg);
        return resp;
    }

    public static class AjaxResponse300 {
        private String redirect;

        public String getRedirect() {
            return redirect;
        }

        public void setRedirect(String redirect) {
            this.redirect = redirect;
        }
    }

    public AjaxResponse(T data) {
        this();
        this.data = data;
    }

    public AjaxResponse addToMap(String key, Map map) {
        if (data instanceof Map) {
            Map dataMap = (Map) data;
            dataMap.put(key, map.get(key));
        }
        return this;
    }

    public int getStatus() {
        return status;
    }

    public int getCode() {
        return status;
    }

    public AjaxResponse setStatus(int status) {
        this.status = status;
        return this;
    }

    public String getMsg() {
        return msg;
    }

    public AjaxResponse setMsg(String msg) {
        this.msg = msg;
        return this;
    }

    public T getData() {
        return data;
    }

    public AjaxResponse setData(T data) {
        this.data = data;
        return this;
    }

    public boolean isSuccess() {
        return success;
    }

    public AjaxResponse setSuccess(boolean success) {
        this.success = success;
        return this;
    }

    @Override
    public String toString() {
        return "AjaxResponse{" +
                "status=" + status +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                ", success=" + success +
                '}';
    }
}
