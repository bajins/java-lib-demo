package com.bajins.demo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


/**
 * 当数据量大时，同步响应数据会超时，可以利用分块传输响应
 * .net6 https://mp.weixin.qq.com/s/rB6DxvmSkoaXcgZXF9pPDQ
 * https://github.com/c0ny1/chunked-coding-converter
 * https://blog.csdn.net/qiaotinger/article/details/62216838
 * https://juejin.cn/post/6960581319944306719
 */
@CrossOrigin // https://www.pangjian.me/2021/05/20/cors-failed-with-transfer-encoding-chunked
@Controller
@RequestMapping("/app")
public class HttpChunkedController {

    /**
     * 使用 Callable 接口异步调用任务方法，StreamingResponseBody 输出流对象
     *
     * @return
     */
    @RequestMapping({"/testCallable"})
    @ResponseBody
    public Callable<StreamingResponseBody> testCallable() {
        return new Callable<StreamingResponseBody>() {
            @Override
            public StreamingResponseBody call() {
                // TODO:调用Service方法处理业务逻辑
                List<Integer> integers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

                return new StreamingResponseBody() {
                    @Override
                    public void writeTo(OutputStream outputStream) {
                        try {
                            for (int i = 0; i < integers.size(); i++) {
                                // TODO: 根据实际情况输出数据
                                outputStream.write(i);
                                outputStream.flush(); // Transfer-Encoding: chunked 会触发分块传输
                                System.out.println(i);
                                try {
                                    TimeUnit.SECONDS.sleep(5);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            try {
                                outputStream.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                };
            }
        };
    }

    @RequestMapping({"/test"})
    @ResponseBody
    public String test(HttpServletResponse response) {
        response.setContentType("text/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        System.out.println(response.getLocale());
        //response.setLocale(Locale.CHINA);
        System.out.println(response.getBufferSize());
        response.setBufferSize(0); // 将缓冲区的大小设置为0，则表示不缓冲
        try (PrintWriter writer = response.getWriter();) {
            //byte[] b = new byte[4];
            //try (ServletOutputStream outputStream = response.getOutputStream();) {
            for (int i = 0; i < 10; i++) {
                //writer.write("test");
                writer.print(i);
                //response.resetBuffer(); // 重置buffer区
                //response.reset();
                writer.flush(); // Transfer-Encoding: chunked 会触发分块传输
                System.out.println(response.getBufferSize());
                //response.flushBuffer(); // 强制把buffer数据发送给前端，同flush()
                System.out.println(response.isCommitted()); // 检测服务器端是否已经把数据写入到了客户端
                //b[0] = (byte) (i & 0xff);
                //b[1] = (byte) (i >> 8 & 0xff);
                //b[2] = (byte) (i >> 16 & 0xff);
                //b[3] = (byte) (i >> 24 & 0xff);
                //outputStream.write(b);
                //outputStream.flush(); // Transfer-Encoding: chunked 会触发分块传输
                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
