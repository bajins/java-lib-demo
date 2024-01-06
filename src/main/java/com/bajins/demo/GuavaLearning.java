package com.bajins.demo;


import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import java.util.List;
import java.util.concurrent.*;


/**
 * @see StopWatch 计算执行时间差
 */
public class GuavaLearning {
    /**
     * 判断是否为驼峰
     *
     * @param str
     * @return
     */
    public static boolean isCamel(String str) {
        boolean lower = false;
        boolean upper = false;
        for (int i = 0; i < str.length(); i++) {
            char charAt = str.charAt(i);
            if (Character.isLowerCase(charAt)) {
                lower = true;
            } else if (Character.isUpperCase(charAt)) {
                upper = true;
            }
            if (lower && upper) {
                break;
            }
        }
        return lower && upper;
    }

    public static void main(String[] args) {
        /* https://github.com/google/guava http://www.ibloger.net/article/3294.html
        LOWER_CAMEL	        Java变量的命名规则	        lowerCamel
        LOWER_HYPHEN	    连字符连接变量的命名规则	    lower-hyphen
        LOWER_UNDERSCORE	C ++变量命名规则	        lower_underscore
        UPPER_CAMEL	        Java和C++类的命名规则	    UpperCamel
        UPPER_UNDERSCORE	Java和C++常量的命名规则	UPPER_UNDERSCORE
         */
        List<Integer> intList = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8);
        // 将一个list按三个一组分成N个小的list，
        // partition返回的是原list的subview视图，即原list改变后，partition之后的结果也会随着改变
        List<List<Integer>> subSets = Lists.partition(intList, 3);
        List<Integer> lastPartition = subSets.get(2);
        List<Integer> firstPartition = subSets.iterator().next();
        List<Integer> expectedLastPartition = Lists.newArrayList(7, 8);

        // 字符串大写字母转下划线
        String name = "TestStringName";
        if (!name.contains("_") && isCamel(name)) { // 如果有下划线则不进行驼峰转下划线，且有小写同时也有大写才进行转换
            System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name));
        }

        // List转Map
        /*Map<Long, User> maps = Maps.uniqueIndex(userList, new Function<User, Long>() {
            @Override
            public Long apply(User user) {
                return user.getId();
            }
        });*/

        // 线程池中创建 ThreadFactory 设置线程名称
        ThreadFactory guavaThreadFactory = new ThreadFactoryBuilder().setNameFormat("retryClient-pool-").build();
        ExecutorService exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10), guavaThreadFactory);
    }
}
