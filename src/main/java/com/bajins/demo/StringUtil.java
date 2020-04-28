package com.bajins.demo;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class StringUtil {

    public static void main(String[] args) {
        // 使用ApacheCommonLang的工具类来生成有边界的随机Int
        RandomUtils.nextInt(1, 10);
        // 利用Apache Commons的StringUtils把list转String
        //StringUtils.join(list.toArray(), ",");

        // 利用Guava的Joiner把list转String
        //Joiner.on(",").join(list);
        // 利用Guava的Splitter将逗号分隔的字符串转换为List
        //Splitter.on(",").trimResults().splitToList("s,t,r");

        // 利用Apache Commons的StringUtils （只是用了split)将逗号分隔的字符串转换为数组
        String[] split = StringUtils.split("s,t,r", ",");
        // 使用org.apache.commons.lang3.StringUtils.countMatches查找并统计子串出现在字符串中的次数
        int i1 = StringUtils.countMatches("abba", "a");

        // 使用org.springframework.wechatutil.StringUtils.countOccurrencesOf查找并统计子串出现在字符串中的次数
        int i = org.springframework.util.StringUtils.countOccurrencesOf("srcStr", "findStr");


        // 利用Spring Framework的StringUtils将逗号分隔的字符串转换为数组
        String[] strings = org.springframework.util.StringUtils.commaDelimitedListToStringArray("s,t,r");
        // 利用Spring Framework的StringUtils把list转String
        String s = org.springframework.util.StringUtils.collectionToDelimitedString(Arrays.asList(strings), ",");
    }
}
