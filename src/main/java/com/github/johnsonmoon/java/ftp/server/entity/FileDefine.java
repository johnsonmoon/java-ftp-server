package com.github.johnsonmoon.java.ftp.server.entity;

/**
 * Create by xuyh at 2019/5/8 11:54.
 */
public class FileDefine {
    private String name;
    private String path;
    private String absolutePath;
    private String type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "FileDefine{" +
                "name='" + name + '\'' +
                ", path='" + path + '\'' +
                ", absolutePath='" + absolutePath + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
