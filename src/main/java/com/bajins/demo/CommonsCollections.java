package com.bajins.demo;

import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.collections4.comparators.ComparableComparator;
import org.apache.commons.compress.utils.Lists;

import java.util.Comparator;
import java.util.List;

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

        List<Integer> intList = Lists.newArrayList();
        intList.add(1);
        intList.add(2);
        intList.add(3);
        intList.add(4);
        // 将一个list按三个一组分成N个小的list，
        // 没有对应的Iterable.partions方法，类似guava那样，partition后的结果同样是原集合的视图
        List<List<Integer>> subSets = ListUtils.partition(intList, 3);
        List<Integer> lastPartition = subSets.get(2);

    }
}
