package com.bajins.demo;


import com.google.common.base.CaseFormat;
import com.google.common.collect.Lists;

import java.util.List;

public class GavaUtil {
    public static void main(String[] args) {
        List<Integer> intList = Lists.newArrayList(1, 2, 3, 4, 5, 6, 7, 8);
        // 将一个list按三个一组分成N个小的list，
        // partition返回的是原list的subview视图，即原list改变后，partition之后的结果也会随着改变
        List<List<Integer>> subSets = Lists.partition(intList, 3);
        List<Integer> lastPartition = subSets.get(2);
        List<Integer> firstPartition = subSets.iterator().next();
        List<Integer> expectedLastPartition = Lists.newArrayList(7, 8);

        // 字符串大写字母转下划线 https://blog.csdn.net/u014082714/article/details/101701372
        String name = "TestStringName";
        System.out.println(CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, name));

        // List转Map
        /*Map<Long, User> maps = Maps.uniqueIndex(userList, new Function<User, Long>() {
            @Override
            public Long apply(User user) {
                return user.getId();
            }
        });*/

    }
}
