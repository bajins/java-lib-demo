package com.bajins.demo;

import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 请求示例
 */
@CrossOrigin
@RestController
@RequestMapping("/request")
public class HttpRequestLearnController {


    /**
     * 请求参数接收
     * <ul>
     *     <li>@RequestBody 只支持请求头content-Type为application/json的请求
     *     <a href="https://blog.csdn.net/justry_deng/article/details/80972817"></a>
     *     </li>
     *     <li>@RequestParam 支持请求头content-Type为multipart/form-data的请求body参数，或url上的参数</li>
     * </ul>
     *
     * @param map
     * @param a        后端RPC调用时，不能省略@RequestParam，否则RPC会找不到参数报错
     * @param b
     * @param c        不加@RequestParam前端的参数名需要和后端控制器的变量名保持一致才能生效，参数为非必传
     * @param request
     * @param response
     * @return
     */
    @RequestMapping({"/test"})
    public String file(@RequestBody Map<String, Object> map, @RequestParam("a") String a,
                       @RequestParam(value = "b", required = false) String b, String c, HttpServletRequest request,
                       HttpServletResponse response) {
        return "success";
    }

}
