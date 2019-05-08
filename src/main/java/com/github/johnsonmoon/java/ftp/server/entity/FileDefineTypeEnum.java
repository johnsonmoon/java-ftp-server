package com.github.johnsonmoon.java.ftp.server.entity;

/**
 * Create by xuyh at 2019/5/8 13:14.
 */
public enum FileDefineTypeEnum {
    FILE("file"),
    DIRECTORY("directory");
    private String type;

    FileDefineTypeEnum(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return "FileDefineTypeEnum{" +
                "type='" + type + '\'' +
                '}';
    }
}
