package com.maplestone.dataCollect.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import com.maplestone.dataCollect.common.constant.FilePathConst;
import com.maplestone.dataCollect.common.utils.DateUtils;
import com.maplestone.dataCollect.common.utils.FileUtils;
import com.maplestone.dataCollect.common.utils.UUIDUtils;
import com.maplestone.dataCollect.pojo.RspVo;
import com.maplestone.dataCollect.pojo.vo.FileDocumentVO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @description:
 * @Author hmx
 * @CreateTime 2021-06-25 12:56
 */

@Slf4j
@Service
public class CommonService {

    @Value("${web.file.root}")
    private String fileRootPath;
    @Value("${web.file.path}")
    private String webFilePath;

    /**
     * MultipartFile 上传文件
     *
     * @param file
     * @param filePath
     * @return
     */
    public FileDocumentVO uploadFile(MultipartFile file, String filePath, String suffix) {
        FileDocumentVO fileDocumentVO = new FileDocumentVO();
        // 新文件名
        String newFileName = UUIDUtils.getUUID() + "." + suffix;
        // 源文件名称
        String oldFileName = file.getOriginalFilename();

        // 创建输出文件夹路径
        String date = DateUtils.dateToString(new Date(), "yyyyMMdd");
        String directory = fileRootPath + FilePathConst.PATH_UPLOAD_DOCUMENT + filePath + "/" + date;
        File newFile = new File(directory);
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        String realPath = directory + "/" + newFileName;
        String path = FilePathConst.PATH_UPLOAD_DOCUMENT + filePath + "/" + date + "/" + newFileName;
        try {
            // 输出
            FileUtils.writeFromStream(file.getInputStream(), realPath);
            fileDocumentVO.setFilePath(path);
            fileDocumentVO.setOriginalName(oldFileName);
            fileDocumentVO.setPresentName(newFileName);
            fileDocumentVO.setSize(file.getSize() + "");
            fileDocumentVO.setFileFormat(suffix);
            return fileDocumentVO;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * MultipartFile 上传文件
     *
     * @param file
     * @param filePath
     * @return
     */
    public FileDocumentVO uploadFile(MultipartFile file, String filePath, String suffix, String fileName) {
        FileDocumentVO fileDocumentVO = new FileDocumentVO();
        // 源文件名称
        String oldFileName = file.getOriginalFilename();

        Long timeStamp = System.currentTimeMillis();
        if (StringUtils.isEmpty(fileName)) {
            fileName = FileUtils.getFileNameNotDot(oldFileName);
        }
        // 新文件名 源文件名+时间戳 避免重复, 加入分割符号
        String newFileName = fileName + "_" + timeStamp + "." + suffix;

        // 创建输出文件夹路径
        String directory = fileRootPath + webFilePath + "/" + filePath;
        File newFile = new File(directory);
        if (!newFile.exists()) {
            newFile.mkdirs();
        }
        String realPath = directory + "/" + newFileName;
        String path = webFilePath + "/" + filePath + "/" + newFileName;
        try {
            // 输出
            FileUtils.writeFromStream(file.getInputStream(), realPath);
            fileDocumentVO.setFilePath(path);
            fileDocumentVO.setOriginalName(oldFileName);
            fileDocumentVO.setPresentName(newFileName);
            fileDocumentVO.setSize(file.getSize() + "");
            fileDocumentVO.setFileFormat(suffix);
            return fileDocumentVO;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 删除文件
     */
    public void removeFile(String filePath) {
        try {
            if (StringUtils.isNotBlank(filePath)) {
                File file = new File(fileRootPath + filePath);
                deleteFolder(file);
            }
        } catch (Exception e) {
            log.error("删除本地文件失败 {}", e);
        }
    }

    /**
     * 删除文件夹
     *
     * @param folder
     */
    public static void deleteFolder(File folder) {
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteFolder(file);
                }
            }
        }
        folder.delete();
    }

    /**
     * 下载文件
     *
     * @param request
     * @param response
     * @param filePath
     * @param fileName
     */
    public void download(HttpServletRequest request, HttpServletResponse response, String filePath, String fileName) {
        if (filePath.startsWith("http")) {
            InputStream is = null;
            try {
                URL httpUrl = new URL(filePath);
                HttpURLConnection connection = (HttpURLConnection) httpUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5 * 1000);
                is = connection.getInputStream();
            } catch (Exception e) {
                e.printStackTrace();
                throw new IllegalArgumentException("文件不存在~");
            }
            RspVo.response(request, response, is, fileName);
        } else {
            File file = new File(fileRootPath + filePath);
            if (file.exists()) {
                RspVo.response(request, response, file, fileName);
            } else {
                throw new IllegalArgumentException("文件不存在~");
            }
        }
    }

    /**
     * 请求体中获取图片
     */
    public MultipartFile[] getFileFromRequest(HttpServletRequest request) {
        MultipartFile[] files = new MultipartFile[0];
        try {
            List<MultipartFile> multipartFiles = new ArrayList<>();
            StandardMultipartHttpServletRequest httpServletRequest = (StandardMultipartHttpServletRequest) request;
            Iterator<String> iterator = httpServletRequest.getFileNames();
            while (iterator.hasNext()) {
                MultipartFile file = httpServletRequest.getFile(iterator.next());
                multipartFiles.add(file);
            }
            files = new MultipartFile[multipartFiles.size()];
            for (int i = 0; i < multipartFiles.size(); i++) {
                files[i] = multipartFiles.get(i);
            }
        } catch (Exception e) {
            log.error("文件或图片解析失败~");
            return null;
        }
        return files;
    }

    /**
     * 创建文件夹，判断文件夹是否存在，不存在则创建
     *
     * @param filePath
     * @return
     */
    public boolean createFolder(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return true;
        } else {
            return file.mkdirs();
        }
    }

}
