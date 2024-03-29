package com.bajins.demo;

import com.alibaba.fastjson.support.spring.FastJsonJsonView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * https://blog.csdn.net/user2025/article/details/107300831
 * https://blog.csdn.net/j080624/article/details/70815120
 *
 * @see FileSystemResource
 * @see ResponseEntity
 * @see FileOutputView
 * @see FastJsonJsonView
 */
@CrossOrigin
@RestController
@RequestMapping("/down")
public class HttpFileDownloadController {

    /**
     * 使用
     *
     * @param response HttpServletResponse 返回
     */
    @RequestMapping("/f1")
    public void file(HttpServletResponse response) {

        Map<String, Object> resMap = new HashMap<>();
        File file = new File("D:\\test.png");
        try (ServletOutputStream outputStream = response.getOutputStream();
             //InputStream inputStream = new FileInputStream(file);// 文件的存放路径
             InputStream inputStream = Files.newInputStream(file.toPath());// 文件的存放路径
        ) { //获取页面输出流
            // 编码避免中文乱码等情况
            String filename = UriUtils.encode(file.getName(), StandardCharsets.UTF_8.displayName());
            //String filename = URLEncoder,encode(file.getName(), StandardCharsets.UTF_8.displaName());// 会把空转续+号
            //String filename = StringEscapeUtils.escapeEcmaScript(file.getName());
            //String filename = Base64.getEncoder().encodeToString(file.getName().getBytes());
            // 写之前设置响应流以附件的形式打开返回值,这样可以保证前边打开文件出错时异常可以返回给前台
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);

            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, len);
            }
        } catch (IOException e) {
            try {
                response.setContentType(MediaType.TEXT_HTML_VALUE);
                response.getWriter().write(e.getMessage());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * 使用
     *
     * @param response HttpServletResponse 返回
     */
    @RequestMapping("/f1")
    public void file1(HttpServletResponse response) {

        Map<String, Object> resMap = new HashMap<>();
        File file = new File("D:\\test.png");
        try (ServletOutputStream outputStream = response.getOutputStream();) { //获取页面输出流
            // 编码避免中文乱码等情况
            String filename = UriUtils.encode(file.getName(), StandardCharsets.UTF_8.displayName());
            // 会把空转续+号
            //String filename = URLEncoder,encode(file.getName(), StandardCharsets.UTF_8.displaName());
            //String filename = StringEscapeUtils.escapeEcmaScript(file.getName());
            //String filename = Base64.getEncoder().encodeToString(file.getName().getBytes());
            // 写之前设置响应流以附件的形式打开返回值,这样可以保证前边打开文件出错时异常可以返回给前台
            response.setHeader("Content-Disposition", "attachment;filename=" + filename);

            byte[] bytes = FileUtils.readFileToByteArray(file); //向输出流写文件
            outputStream.write(bytes);
            outputStream.flush();
        } catch (IOException e) {
            try {
                response.setContentType(MediaType.TEXT_HTML_VALUE);
                response.getWriter().write(e.getMessage());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    /**
     * 使用
     *
     * @param response HttpServletResponse 返回
     */
    @RequestMapping({"/f2"})
    public void file(HttpServletRequest request, HttpServletResponse response, File file) {
        response.setCharacterEncoding(request.getCharacterEncoding());
        try (FileInputStream fis = new FileInputStream(file)) {
            response.setContentType("application/octet-stream");
            // 编码避免中文乱码等情况
            String filename = UriUtils.encode(file.getName(), StandardCharsets.UTF_8.displayName());
            // 会把空转续+号
            //String filename = URLEncoder,encode(file.getName(), StandardCharsets.UTF_8.displaName());
            //String filename = StringEscapeUtils.escapeEcmaScript(file.getName());
            //String filename = Base64.getEncoder().encodeToString(file.getName().getBytes());
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            IOUtils.copy(fis, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            try {
                response.setContentType(MediaType.TEXT_HTML_VALUE);
                response.getWriter().write(e.getMessage());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    @RequestMapping({"/f3"})
    public ModelAndView file(HttpServletRequest request, HttpServletResponse response, ModelAndView mav) {
        //mav.setView(new FileOutputView());
        return mav;
    }

    @RequestMapping({"/f4"})
    public ResponseEntity<FileSystemResource> file(HttpServletRequest request, HttpServletResponse response) {

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));

        File file = new File("D:\\test.png");
        // 编码避免中文乱码等情况
        String filename = UriUtils.encode(file.getName(), StandardCharsets.UTF_8.displayName());
        // 会把空转续+号
        //String filename = URLEncoder,encode(file.getName(), StandardCharsets.UTF_8.displaName());
        //String filename = StringEscapeUtils.escapeEcmaScript(file.getName());
        //String filename = Base64.getEncoder().encodeToString(file.getName().getBytes());
        headers.add("Content-Disposition", "attachment; filename=" + filename);
        return ResponseEntity.ok()//
                .headers(headers)//
                .contentLength(file.length())//
                .contentType(MediaType.parseMediaType("application/octet-stream"))//
                .body(new FileSystemResource(file));
    }

    /**
     * 用 HttpServletRequest 获取整个URL，然后使用Spring的 AntPathMatcher 工具来提取URL中的路径部分
     *
     * @param request
     * @return
     */
    @GetMapping("/files/**")
    public ResponseEntity<FileSystemResource> getFileContent(HttpServletRequest request) {
        String restOfTheUrl = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        String bestMatchingPattern = (String) request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);

        AntPathMatcher antPathMatcher = new AntPathMatcher();
        String filePath = antPathMatcher.extractPathWithinPattern(bestMatchingPattern, restOfTheUrl);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
        headers.add("Pragma", "no-cache");
        headers.add("Expires", "0");
        headers.add("Last-Modified", new Date().toString());
        headers.add("ETag", String.valueOf(System.currentTimeMillis()));

        File file = new File(filePath);
        // 编码避免中文乱码等情况
        String filename = UriUtils.encode(file.getName(), StandardCharsets.UTF_8.displayName());
        // 会把空转续+号
        //String filename = URLEncoder,encode(file.getName(), StandardCharsets.UTF_8.displaName());
        //String filename = StringEscapeUtils.escapeEcmaScript(file.getName());
        //String filename = Base64.getEncoder().encodeToString(file.getName().getBytes());
        headers.add("Content-Disposition",
                "attachment; filename=" + filename);
        return ResponseEntity.ok()//
                .headers(headers)//
                .contentLength(file.length())//
                .contentType(MediaType.parseMediaType("application/octet-stream"))//
                .body(new FileSystemResource(file));
    }
}
