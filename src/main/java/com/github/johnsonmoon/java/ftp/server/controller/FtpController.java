package com.github.johnsonmoon.java.ftp.server.controller;

import com.github.johnsonmoon.java.ftp.server.common.ext.FtpException;
import com.github.johnsonmoon.java.ftp.server.entity.FileDefine;
import com.github.johnsonmoon.java.ftp.server.entity.FileDefineTypeEnum;
import com.github.johnsonmoon.java.ftp.server.entity.param.UploadFileParam;
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
                fileDefine.setSize(f.length());
                fileDefines.add(fileDefine);
            }
        }
        return fileDefines;
    }

    @ApiOperation("Upload file.")
    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public Boolean uploadFile(UploadFileParam uploadFileParam) {
        if (uploadFileParam.getName() == null || uploadFileParam.getName().isEmpty()
                || uploadFileParam.getTargetDir() == null || uploadFileParam.getTargetDir().isEmpty()
                || uploadFileParam.getFile() == null) {
            throw new FtpException("Name or targetDir or file is null!");
        }
        File dir = new File(uploadFileParam.getTargetDir());
        if (!dir.exists()) {
            throw new FtpException("TargetDir not exists!");
        }
        if (!dir.isDirectory()) {
            throw new FtpException(String.format("%s is not a directory!", dir));
        }
        String filePathName = uploadFileParam.getTargetDir() + File.separator + uploadFileParam.getName();
        File newFile = new File(filePathName);
        if (newFile.exists()) {
            if (!newFile.delete()) {
                throw new FtpException("Old file in target directory exist with same fileName and could not be deleted.");
            }
        }
        try {
            if (!newFile.createNewFile()) {
                throw new FtpException("Create new file failed.");
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException("Create new file failed.");
        }
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = uploadFileParam.getFile().getInputStream();
            outputStream = new FileOutputStream(newFile);
            byte[] bytes = new byte[32];
            int length;
            while ((length = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException("Upload file failed.");
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return true;
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

    @ApiOperation("Move file into target directory")
    @GetMapping("/move")
    public Boolean moveFile(@RequestParam("file") String file,
                            @RequestParam("targetDir") String targetDir) {
        if (file == null || file.isEmpty() || targetDir == null || targetDir.isEmpty()) {
            throw new FtpException("file or targetDir is null!");
        }
        File f = new File(file);
        File dir = new File(targetDir);
        if (!f.exists() || !dir.exists()) {
            throw new FtpException("file not exist or targetDir not exist.");
        }
        if (!f.isFile() || !dir.isDirectory()) {
            throw new FtpException("file is not a file or targetDir is not directory.");
        }
        File newFile = new File(dir.getPath() + File.separator + f.getName());
        if (newFile.exists()) {
            if (newFile.delete()) {
                throw new FtpException("Old file in target directory exist with same fileName and could not be deleted.");
            }
        }
        try {
            if (!newFile.createNewFile()) {
                throw new FtpException("Create new file failed.");
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException("Create new file failed.");
        }
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(f);
            outputStream = new FileOutputStream(newFile);
            byte[] bytes = new byte[32];
            int length;
            while ((length = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException("Move file failed.");
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return f.delete();
    }

    @ApiOperation("Rename file")
    @GetMapping("/rename")
    public Boolean renameFile(@RequestParam("file") String file,
                              @RequestParam("targetFile") String targetFile) {
        if (file == null || file.isEmpty() || targetFile == null || targetFile.isEmpty()) {
            throw new FtpException("file or targetDir is null!");
        }
        File f = new File(file);
        File targetF = new File(targetFile);
        if (!f.exists()) {
            throw new FtpException("file not exist.");
        }
        if (!f.isFile()) {
            throw new FtpException("file is not a file or targetDir is not directory.");
        }
        if (targetF.exists()) {
            throw new FtpException("targetFile already exist.");
        }
        try {
            if (!targetF.createNewFile()) {
                throw new FtpException("Create new file failed.");
            }
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException("Create new file failed.");
        }
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(f);
            outputStream = new FileOutputStream(targetF);
            byte[] bytes = new byte[32];
            int length;
            while ((length = inputStream.read(bytes)) > 0) {
                outputStream.write(bytes, 0, length);
            }
            outputStream.flush();
        } catch (Exception e) {
            logger.warn(e.getMessage(), e);
            throw new FtpException("Move file failed.");
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                logger.warn(e.getMessage(), e);
            }
        }
        return f.delete();
    }

    @ApiOperation("Delete file.")
    @GetMapping("/delete")
    public Boolean deleteFile(@RequestParam("file") String file) {
        if (file == null || file.isEmpty()) {
            throw new FtpException("file is null!");
        }
        File f = new File(file);
        if (!f.exists()) {
            throw new FtpException("file not exist.");
        }
        if (!f.isFile()) {
            throw new FtpException("file is not a file.");
        }
        return f.delete();
    }
}
