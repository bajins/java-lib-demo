package com.bajins.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
     * @param joinPoint 切点
     * @param clazz     被使用注解的类
     * @return
     * @throws Exception
     */
    public static <T extends Annotation> T getMethodAnnotate(JoinPoint joinPoint, Class<T> clazz) throws ClassNotFoundException {
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
                annotation = method.getAnnotation(clazz);
            }
            /**
             * 返回 Class 对象所表示的类或接口所声明的所有字段，
             * 包括公共、保护、默认（包）访问和私有字段，但不包括继承的字段。
             */
            /*Field[] declaredFields = clazz.getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                String attributeName = declaredFields[i].getName();
                System.out.println(attributeName);
            }*/
        }
        return annotation;
    }

    /**
     * 获取切点位置的参数
     *
     * @param joinPoint 切点
     * @param request   从切面传入的当前HttpServletRequest
     * @return java.lang.String
     */
    public static String getParam(JoinPoint joinPoint, HttpServletRequest request) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 获取切点位置的方法上的参数名
        String[] parameterNames = methodSignature.getParameterNames();
        // 获取切点位置的方法上的参数值
        Object[] args = joinPoint.getArgs();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        String parameterName;
        Object arg;
        for (int i = 0; i < parameterNames.length; i++) {
            parameterName = parameterNames[i];
            arg = args[i];
            if (arg instanceof HttpServletRequest) {
                // 获取HttpServletRequest中的请求标准参数
                Map<String, String[]> properties = request.getParameterMap();
                Map<String, String> returnMap = new HashMap<>();
                for (Map.Entry<String, String[]> entry : properties.entrySet()) {
                    returnMap.put(entry.getKey(), String.join(",", entry.getValue()));
                }
                if (returnMap != null && returnMap.size() > 0) {
                    objectNode.putPOJO(parameterName, returnMap);
                }
            } else if (arg instanceof HttpServletResponse) {
                continue;
            } else {
                objectNode.putPOJO(parameterName, arg);
            }
        }
        return objectNode.toString();
    }

    /**
     * 获取切点位置的参数
     *
     * @param joinPoint 切点
     * @return
     */
    public static String getParam(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 获取切点位置的方法上的参数名
        String[] parameterNames = methodSignature.getParameterNames();
        // 获取切点位置的方法上的参数值
        Object[] args = joinPoint.getArgs();

        ObjectMapper objectMapper = new ObjectMapper();
        ObjectNode objectNode = objectMapper.createObjectNode();
        String parameterName;
        Object arg;
        for (int i = 0; i < parameterNames.length; i++) {
            parameterName = parameterNames[i];
            arg = args[i];
            if (arg instanceof HttpServletRequest) {
                HttpServletRequest request = (HttpServletRequest) arg;
                // 获取HttpServletRequest中的请求标准参数
                Map<String, String[]> properties = request.getParameterMap();
                Map<String, String> returnMap = new HashMap<>();
                for (Map.Entry<String, String[]> entry : properties.entrySet()) {
                    returnMap.put(entry.getKey(), String.join(",", entry.getValue()));
                }
                if (returnMap != null && returnMap.size() > 0) {
                    objectNode.putPOJO(parameterName, returnMap);
                }
            } else if (arg instanceof HttpServletResponse) {
                continue;
            } else {
                objectNode.putPOJO(parameterName, arg);
            }
        }
        return objectNode.toString();
    }

    /**
     * 借助了 Mybatis 通过切点获取方法上的参数名
     *
     * @param joinPoint
     * @return
     * @throws NotFoundException
     */
    /*private Map<String, Object> getFieldsName(JoinPoint joinPoint) throws NotFoundException {
        Map<String, Object> map = new HashMap<>();
        // 获取切点位置的类
        Class<?> aClass = joinPoint.getTarget().getClass();
        // 获取类对象表示的实体名称
        String clazzName = aClass.getName();
        // 获取切点位置的方法名称
        String methodName = joinPoint.getSignature().getName();
        // 获取切点位置的方法上的参数值
        Object[] args = joinPoint.getArgs();

        // 类池子
        ClassPool pool = ClassPool.getDefault();
        // 获取类路径
        ClassClassPath classPath = new ClassClassPath(aClass);
        // 装入类路径
        pool.insertClassPath(classPath);
        // 从池子中获取类
        CtClass cc = pool.get(clazzName);
        CtMethod cm = cc.getDeclaredMethod(methodName);
        // 获取方法信息
        MethodInfo methodInfo = cm.getMethodInfo();
        // 获取方法上的参数名
        CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
        LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
        // 判断是否为静态
        int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
        for (int i = 0; i < cm.getParameterTypes().length; i++) {
            map.put(attr.variableName(i + pos), args[i]);
        }
        return map;
    }*/

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