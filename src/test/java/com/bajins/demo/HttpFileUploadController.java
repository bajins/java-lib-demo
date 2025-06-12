package com.bajins.demo;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import org.springframework.web.multipart.support.*;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 文件上传（含携带参数）：单文件、多文件、携带参数、分片上传、断点续传、追加上传、表单上传、流式上传
 * https://mp.weixin.qq.com/s/J55UtidL_WI0zSJM1C7kXQ
 *
 * @link <a href="https://help.aliyun.com/document_detail/32013.html">参考OSS文件上传</a>
 * @see MultipartFile
 * @see MultipartHttpServletRequest
 * @see AbstractMultipartHttpServletRequest
 * @see DefaultMultipartHttpServletRequest
 * @see StandardMultipartHttpServletRequest
 * @see RequestPartServletServerHttpRequest
 * @see MultipartResolver
 * @see CommonsMultipartResolver 使用 Apache 的 commons-fileupload
 * @see StandardServletMultipartResolver Servlet 3 以上的版本使用
 */
@CrossOrigin
@RestController
@RequestMapping("/upload")
public class HttpFileUploadController {

    @Autowired
    private ResourceLoader resourceLoader;
    @Autowired
    private ServletContext servletContext;
    @Autowired
    private WebApplicationContext context;

    /**
     * 上传同时携带参数，指定 from 表单属性 enctype="multipart/form-data"
     * <p>
     * 实现的方式是：在表单中一次提交多个参数（类型只能是文件或字符串）。非文件参数：如果是json，只能先取值再转换
     * <p>
     * https://docs.spring.io/spring-boot/docs/current/reference/html/howto.html#howto.spring-mvc.multipart-file-uploads
     *
     * @param files    类型为List<MultipartFile>/MultipartFile[]是文件列表，如果类型为MultipartFile是单个文件
     * @param id       字符串参数，使用@RequestParam注解或不使用注解，都只能接收字符串类型，不能转换为指定类型
     * @param request
     * @param response
     * @return
     */
    @ResponseBody
    @RequestMapping("/fileParam1")
    public String fileParam(@RequestParam("files") MultipartFile[] files, String id, HttpServletRequest request,
                            HttpServletResponse response) {
        for (MultipartFile file : files) {
            System.out.println(file.getOriginalFilename());
        }
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
        //request.getFile("file"); // 获取单个文件
        //request.getFileMap()
        //request.getMultiFileMap()
        List<MultipartFile> files = request.getFiles("files"); // 文件列表


        // 获取本地静态文件
        /*String path_temp = "common/img/user_head_img.png";
        ClassPathResource resource = new ClassPathResource(path_temp); // 获取类路径下的文件
        System.out.println(resource.getPath());
        FileSystemResource resource1 = new FileSystemResource(path_temp);
        System.out.println(resource1.getPath());
        String filePath0 = servletContext.getRealPath("/" + path_temp);
        System.out.println(filePath0);
        String filePath1 = context.getServletContext().getRealPath("/" + path_temp);
        System.out.println(filePath1);
        Resource resource2 = resourceLoader.getResource(path_temp);
        System.out.println(resource2);*/

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
                path = Paths.get(filePath, fileName).toString();
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

    @RequestMapping({"/fileParam21"})
    public String fileParam(HttpServletRequest request, HttpServletResponse response) {

        CommonsMultipartResolver multipartResolver =
                new CommonsMultipartResolver(request.getSession().getServletContext());
        if (multipartResolver.isMultipart(request)) {
            throw new IllegalArgumentException("消息主体无效!");
        }
        MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;

        Set<Map.Entry<String, MultipartFile>> entityMaps = multiRequest.getFileMap().entrySet();
        for (Map.Entry<String, MultipartFile> entity : entityMaps) {
            System.out.println(entity.getKey());
        }
        MultiValueMap<String, MultipartFile> multiFileMap = multiRequest.getMultiFileMap();

        return "";
    }

    /**
     * 上传同时携带参数，用在multipart/form-data表单提交请求的方法上
     * <p>
     * 实现的方式是：在表单中一次提交多个参数（类型只能是文件或字符串）。
     *
     * @param map      json字符串转换为指定类型，需要单独针对此参数设置Content-Type: application/json，且使用@RequestPart注解
     * @param files    类型为List<MultipartFile>/MultipartFile[]是文件列表，如果类型为MultipartFile是单个文件
     * @param request
     * @param response
     * @return
     */
    @RequestMapping({"/fileParam3"})
    public String fileParam(@RequestPart @Valid final Map<String, Object> map,
                            @RequestPart("files") List<MultipartFile> files, HttpServletRequest request,
                            HttpServletResponse response) {
        return map.toString();
    }

    @RequestMapping({"/fileParam4"})
    public String fileParamJsonFile(@RequestParam("json") @Valid final String json,
                                    @RequestParam("files") List<MultipartFile> files, HttpServletRequest request,
                                    HttpServletResponse response) {
        return JSON.parse(json).toString();
    }

    @RequestMapping({"/fileParam5"})
    public String fileParamModelAttribute(@ModelAttribute @Valid final Map<String, Object> map,
                                          @RequestParam("files") List<MultipartFile> files, HttpServletRequest request,
                                          HttpServletResponse response) {
        return map.toString();
    }

}
