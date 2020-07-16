package com.bajins.demo;

import org.apache.commons.collections4.comparators.ComparableComparator;
import org.apache.commons.collections4.ComparatorUtils;

import java.util.Comparator;

public class CommonsCollections {
    public static void main(String[] args) {
        // 使用Apache Collections对List集合中的泛型中的bean某一个字段排序
        //Comparator<?> mycmp = ComparableComparator.getInstance(); // collections4之前版本的使用方式
        Comparator<?> mycmp = new ComparableComparator();
        // 允许null
        mycmp = ComparatorUtils.nullLowComparator(mycmp);
        // 逆序，默认为正序
        mycmp = ComparatorUtils.reversedComparator(mycmp);

        //Collections.sort(list, new BeanComparator("fieldName", mycmp));

    }
}
