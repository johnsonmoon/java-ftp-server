package com.github.johnsonmoon.java.ftp.server.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * 文件操作工具类。
 */
public class FileUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);

    /**
     * 写入文件
     *
     * @param file        文件
     * @param inputStream 输入流
     * @return true/false
     */
    public static boolean writeFile(File file, InputStream inputStream) {
        boolean result = true;
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            byte[] buffer = new byte[32];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
            fileOutputStream.flush();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            result = false;
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (Exception e) {
                    logger.warn(e.getMessage(), e);
                }
            }
        }
        return result;
    }

    /**
     * 删除指定的文件。
     *
     * @param file 待删除的文件
     * @return 如果删除成功，返回 true，否则返回 false
     */
    public static boolean deleteFile(File file) {
        try {
            return Files.deleteIfExists(file.toPath());
        } catch (IOException e) {
            logger.error("Failed to delete file", e);
            return false;
        }
    }

    /**
     * 创建文件
     */
    public static boolean createFile(File file) {
        if (!file.exists()) {
            try {
                if (!createDir(file.getParentFile()))
                    return false;
                if (!file.createNewFile())
                    return false;
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return true;
    }

    /**
     * 创建路径
     */
    public static boolean createDir(File dir) throws Exception {
        if (!dir.exists()) {
            File parent = dir.getParentFile();
            if (parent != null)
                createDir(parent);
            return dir.mkdir();
        }
        return true;
    }
}
