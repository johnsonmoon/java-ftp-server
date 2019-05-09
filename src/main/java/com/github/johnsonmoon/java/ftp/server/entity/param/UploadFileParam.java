package com.github.johnsonmoon.java.ftp.server.entity.param;

import org.springframework.web.multipart.MultipartFile;

/**
 * Create by xuyh at 2019/5/9 11:34.
 */
public class UploadFileParam {
    private String name;
    private String targetDir;
    private MultipartFile file;

    public String getTargetDir() {
        return targetDir;
    }

    public void setTargetDir(String targetDir) {
        this.targetDir = targetDir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    @Override
    public String toString() {
        return "UploadFileParam{" +
                "name='" + name + '\'' +
                ", targetDir='" + targetDir + '\'' +
                ", file=" + file +
                '}';
    }
}
