package com.github.johnsonmoon.java.ftp.server.controller;

import com.github.johnsonmoon.java.ftp.server.common.ext.FtpException;
import com.github.johnsonmoon.java.ftp.server.entity.FileDefine;
import com.github.johnsonmoon.java.ftp.server.entity.FileDefineTypeEnum;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Create by johnsonmoon at 2018/11/28 15:55.
 */
@Api("FTP-controller")
@RestController
public class FtpController {
    private static Logger logger = LoggerFactory.getLogger(FtpController.class);

    @ApiOperation("List files of directory.")
    @GetMapping("/list")
    public List<FileDefine> listFiles(@RequestParam("dir") String dir) {
        if (dir == null || dir.isEmpty()) {
            throw new FtpException("Dir must not be null!");
        }
        File file = new File(dir);
        if (!file.exists()) {
            throw new FtpException("Dir not exists!");
        }
        if (!file.isDirectory()) {
            throw new FtpException(String.format("%s is not a directory!", dir));
        }
        File[] files = file.listFiles();
        List<FileDefine> fileDefines = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                FileDefine fileDefine = new FileDefine();
                fileDefine.setName(f.getName());
                fileDefine.setPath(f.getPath());
                fileDefine.setAbsolutePath(f.getAbsolutePath());
                if (f.isDirectory()) {
                    fileDefine.setType(FileDefineTypeEnum.DIRECTORY.getType());
                }
                if (f.isFile()) {
                    fileDefine.setType(FileDefineTypeEnum.FILE.getType());
                }
                fileDefines.add(fileDefine);
            }
        }
        return fileDefines;
    }

    @ApiOperation("Download file.")
    @GetMapping("/download")
    public void downloadFile(@RequestParam("file") String file,
                             HttpServletResponse httpServletResponse) {
        if (file == null || file.isEmpty()) {
            throw new FtpException("File path name is null!");
        }
        File fileF = new File(file);
        if (!fileF.exists()) {
            throw new FtpException("File not exists!");
        }
        FileInputStream fileInputStream = null;
        OutputStream outputStream = null;
        httpServletResponse.setHeader("Content-Type", "application/octet-stream");
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + fileF.getName());
        try {
            fileInputStream = new FileInputStream(fileF);
            outputStream = httpServletResponse.getOutputStream();
            byte[] bytes = new byte[32];
            int length;
            while ((length = fileInputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException(e.getMessage(), e);
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
    }
}
