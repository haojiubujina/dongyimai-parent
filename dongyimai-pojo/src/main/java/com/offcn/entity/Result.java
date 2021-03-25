package com.offcn.entity;

import java.io.Serializable;

public class Result implements Serializable {

    /**
     *
     * 返回结果封装
     */
    //操作是否成功
    private boolean success;
    //操作的展示信息
    private String message;

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
