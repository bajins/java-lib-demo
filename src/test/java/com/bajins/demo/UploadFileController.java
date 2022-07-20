package com.bajins.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 文件上传：单文件、多文件、携带参数、分片上传、断点续传、追加上传、表单上传、流式上传
 *
 * @link <a href="https://help.aliyun.com/document_detail/32013.html">参考OSS文件上传</a>
 */
@CrossOrigin
@RestController
@RequestMapping("/upload")
public class UploadFileController {


    /**
     * 上传文件
     *
     * @param file
     * @param response
     * @return
     */
    @RequestMapping({"/file"})
    public String file(@RequestParam("file") MultipartFile file, HttpServletRequest request,
                       HttpServletResponse response) {
        return file.getOriginalFilename();
    }

    /**
     * 上传同时携带参数，指定 from 表单属性 enctype="multipart/form-data"
     * <p>
     * 实现的方式是：在表单中一次提交多个参数（类型只能是文件或字符串）。非文件参数：如果是json，只能先取值再转换
     *
     * @param files
     * @param id
     * @param request
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping("/fileParam1")
    public String fileParam(@RequestParam("files") MultipartFile[] files, String id, HttpServletRequest request,
                            HttpServletResponse response) {

        return null;
    }

    /**
     * 上传同时携带参数
     * <p>
     * 实现的方式是：在表单中一次提交多个参数（类型只能是文件或字符串）。非文件参数：如果是json，只能先取值再转换
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping({"/fileParam2"})
    public String fileParam(MultipartHttpServletRequest request, HttpServletResponse response) {
        // 通过前后端约定的参数名获取参数
        String ticketInfos = request.getParameter("map"); // json字符串
        List<MultipartFile> files = request.getFiles("files"); // 文件列表，getFile获取单个文件，getFiles获取多个文件

        String filePath = "D:\\Download\\file\\";
        List<String> paths = new ArrayList<>();
        for (MultipartFile file : files) {
            String path;
            try {
                if (file.isEmpty() && file.getSize() == 0) {

                }
                // 获取文件名
                String fileName = file.getOriginalFilename();
                // 获取文件的后缀名
                String suffixName = null;
                if (fileName != null) {
                    suffixName = fileName.substring(fileName.lastIndexOf("."));
                }
                // 文件上传后的路径
                path = filePath + fileName;
                File dest = new File(path);
                // 检测是否存在目录
                if (!dest.getParentFile().exists()) {
                    dest.getParentFile().mkdirs();
                }
                // 写入文件，此处只能存放绝对路径，否则会导致读取异常，可以用传统方式来替换。
                file.transferTo(dest);
            } catch (IOException exception) {
                exception.printStackTrace();
                return null;
            }
            path = path.replace("\\", "\\\\");
            paths.add(path);
        }
        return String.join(",", paths);
    }

    /**
     * 上传同时携带参数，用在multipart/form-data表单提交请求的方法上
     * <p>
     * 实现的方式是：在表单中一次提交多个参数（类型只能是文件或字符串）。非文件参数：如果是json，只能先取值再转换
     *
     * @param map
     * @param files
     * @param request
     * @param response
     * @return
     */
    @RequestMapping({"/fileParam3"})
    public String fileParam(@RequestPart @Valid final Map<String, Object> map, // json字符串
                            @RequestPart("files") List<MultipartFile> files, HttpServletRequest request,
                            HttpServletResponse response) {
        return map.toString();
    }

}
