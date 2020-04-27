package com.bajins.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.JoinPoint;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * 切面拦截工具
 * Object[] getArgs：返回目标方法的参数
 * Signature getSignature：返回目标方法的签名
 * Object getTarget：返回被织入增强处理的目标对象
 * Object getThis：返回AOP框架为目标对象生成的代理对象
 *
 * @author claer admin@bajins.com
 */
public class Aspectj {

    /**
     * 获取注解中对方法的描述信息
     *
     * @param joinPoint       切点
     * @param annotationClass 被使用注解的类
     * @return
     * @throws Exception
     */
    public static <T extends Annotation> T getMethodAnnotate(JoinPoint joinPoint, Class<T> annotationClass) throws Exception {
        // 获取目标类名
        String targetName = joinPoint.getTarget().getClass().getName();
        // 获取方法名
        String methodName = joinPoint.getSignature().getName();
        // 获取相关参数
        Object[] arguments = joinPoint.getArgs();
        // 使用反射生成类对象
        Class targetClass = Class.forName(targetName);
        // 获取该类中的方法
        Method[] methods = targetClass.getMethods();

        T annotation = null;
        for (Method method : methods) {
            // 判断使用反射生成类对象中的方法与使用注解切点进入的方法是否一致
            // 获取使用反射生成类对象中的所有参数类型,进行判断是否一致
            if (method.getName().equals(methodName) && method.getParameterTypes().length == arguments.length) {
                // 获取使用反射生成类对象中使用自定义注解方法上的描述
                annotation = method.getAnnotation(annotationClass);
            }
        }
        /**
         * 返回 Class 对象所表示的类或接口所声明的所有字段，
         * 包括公共、保护、默认（包）访问和私有字段，但不包括继承的字段。
         */
        /*Field[] declaredFields = annotationClass.getDeclaredFields();
        for (int i = 0; i < declaredFields.length; i++) {
            String attributeName = declaredFields[i].getName();
            System.out.println(attributeName);
        }*/
        return annotation;
    }

    /**
     * 获取切点的参数，根据请求方法使用不同的方式获取参数
     *
     * @param joinPoint 切点
     * @param request
     * @return java.lang.String
     */
    public static String getParam(JoinPoint joinPoint, HttpServletRequest request) throws JsonProcessingException {
        String method = request.getMethod();

        // 判断请求方式是否为POST
        if (RequestMethod.POST.toString().equals(method)) {
            Object[] args = joinPoint.getArgs();
            if (args.length <= 0) {
                return "";
            }
            // 如果切点拿到的第一个参数是HttpServletRequest并且不为空
            if (args[0] instanceof HttpServletRequest && args[0] != null) {
                // POST请求方式的真正HttpServletRequest在joinPoint.getArgs()[0]中
                request = (HttpServletRequest) args[0];
            }
        }
        // 判断HttpServletRequest是否为空
        if (request == null) {
            return "";
        }
        // 获取HttpServletRequest中的参数map
        Map<String, String[]> properties = request.getParameterMap();
        Map<String, String> returnMap = new HashMap<String, String>();
        for (Map.Entry<String, String[]> entry : properties.entrySet()) {
            returnMap.put(entry.getKey(), String.join(",",entry.getValue()));
        }
        return new ObjectMapper().writeValueAsString(returnMap);
    }

    /**
     * 获取 类.方法
     *
     * @param joinPoint
     * @return java.lang.String
     */
    public static String getMethodByTarget(JoinPoint joinPoint) {
        // 类全路径
        return joinPoint.getTarget().getClass().getName() + "." + joinPoint.getSignature().getName();
    }

    /**
     * 获取 类.方法
     *
     * @param joinPoint
     * @return java.lang.String
     */
    public static String getMethodBySignature(JoinPoint joinPoint) {
        // 类全路径
        return joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
    }

}