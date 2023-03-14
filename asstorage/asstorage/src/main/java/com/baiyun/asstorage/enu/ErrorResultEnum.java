package com.baiyun.asstorage.enu;

public enum ErrorResultEnum {

    ADJACENT_REPEAT("连续重复AS异常"),
    AS_LOOP("AS环路异常"),
    NATION_LEAK("国内流量外泄"),
    TAMPER_AS("路径篡改异常"),
    FAKE_AS("路径伪造异常"),
    NORMAL("无异常"),
    ILLEGAL_INPUT("非法输入"),
    UNKNOW("邻居关系异常");

    String msg;

    ErrorResultEnum(String msg){
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

}
