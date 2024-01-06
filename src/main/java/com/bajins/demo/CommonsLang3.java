package com.bajins.demo;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.apache.commons.lang3.tuple.*;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.commons.text.WordUtils;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @see DurationFormatUtils
 * @see FastDateFormat
 * @see ComparatorChain 多元素排序
 * @see BeanComparator 实现自然排序（根据ASCII码排序）
 * @see FixedOrderComparator 指定排序规则
 * @see SerializationUtils 序列化，深拷贝对象
 * @see IOUtils
 * @see ImmutablePair
 * @see ImmutableTriple
 * @see MutablePair
 * @see MutableTriple
 * @see Pair
 * @see Triple
 * @see StopWatch 计算执行时间差
 */
public class CommonsLang3 {

    /**
     * covert field name to column name userName --> user_name
     * covert class name to column name UserName -- > user_name
     *
     * @param propertyName
     * @return
     */
    public static String getUnderlineName(String propertyName) {
        if (null == propertyName) {
            return "";
        }
        StringBuilder sbl = new StringBuilder(propertyName);
        sbl.setCharAt(0, Character.toLowerCase(sbl.charAt(0)));
        propertyName = sbl.toString();

        char[] chars = propertyName.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (char c : chars) {
            if (CharUtils.isAsciiAlphaUpper(c)) {
                sb.append("_" + StringUtils.lowerCase(CharUtils.toString(c)));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * covert field name to column name
     *
     * @param fieldName
     * @return
     */
    public static String getHumnName(String fieldName) {
        if (null == fieldName) {
            return "";
        }
        fieldName = fieldName.toLowerCase();
        char[] chars = fieldName.toCharArray();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '_') {
                int j = i + 1;
                if (j < chars.length) {
                    sb.append(StringUtils.upperCase(CharUtils.toString(chars[j])));
                    i++;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        // 缩短到某长度,用...结尾.其实就是(substring(str, 0, max-3) + "...")
        StringUtils.abbreviate("abcdefg", 6);// "abc..."

        // 字符串结尾的后缀是否与你要结尾的后缀匹配，若不匹配则添加后缀
        StringUtils.appendIfMissing("abc", "xyz");// "abcxyz"
        StringUtils.appendIfMissingIgnoreCase("abcXYZ", "xyz");// "abcXYZ"

        // 首字母大小写转换
        StringUtils.capitalize("cat");// "Cat"
        StringUtils.uncapitalize("Cat");// "cat"

        // 驼峰转下划线
        //String str = StringUtils.join(StringUtils.splitByCharacterTypeCamelCase("userName"), "_").toLowerCase();
        String underscoreCase = StringUtils.uncapitalize("userName").replaceAll("([A-Z])", "_$1").toLowerCase();
        System.out.println(underscoreCase);

        System.out.println(WordUtils.capitalizeFully(underscoreCase, '_').replaceAll("_", ""));

        // 字符串扩充至指定大小且居中（若扩充大小少于原字符大小则返回原字符，若扩充大小为 负数则为0计算 ）
        StringUtils.center("abcd", 2);//  "abcd"
        StringUtils.center("ab", -1);//  "ab"
        StringUtils.center("ab", 4);// " ab "
        StringUtils.center("a", 4, "yz");// "yayz"
        StringUtils.center("abc", 7, "");// "  abc  "

        // 去除字符串中的"\n", "\r", or "\r\n"
        StringUtils.chomp("abc\r\n");// "abc"

        // 判断一字符串是否包含另一字符串
        StringUtils.contains("abc", "z");// false
        StringUtils.containsIgnoreCase("abc", "A");// true

        // 统计一字符串在另一字符串中出现次数
        StringUtils.countMatches("abba", "a");// 2

        // 删除字符串中的梭有空格
        StringUtils.deleteWhitespace("   ab  c  ");// "abc"

        // 比较两字符串，返回不同之处。确切的说是返回第二个参数中与第一个参数所不同的字符串
        StringUtils.difference("abcde", "abxyz");// "xyz"

        // 检查字符串结尾后缀是否匹配
        StringUtils.endsWith("abcdef", "def");// true
        StringUtils.endsWithIgnoreCase("ABCDEF", "def");// true
        StringUtils.endsWithAny("abcxyz", new String[]{null, "xyz", "abc"});// true

        // 检查起始字符串是否匹配
        StringUtils.startsWith("abcdef", "abc");// true
        StringUtils.startsWithIgnoreCase("ABCDEF", "abc");// true
        StringUtils.startsWithAny("abcxyz", new String[]{null, "xyz", "abc"});// true

        // 判断两字符串是否相同
        StringUtils.equals("abc", "abc");// true
        StringUtils.equalsIgnoreCase("abc", "ABC");// true

        // 比较字符串数组内的所有元素的字符序列，起始一致则返回一致的字符串，若无则返回""
        StringUtils.getCommonPrefix("abcde", "abxyz");// "ab"

        // 正向查找字符在字符串中第一次出现的位置
        StringUtils.indexOf("aabaabaa", "b");// 2
        StringUtils.indexOf("aabaabaa", "b", 3);// 5(从角标3后查找)
        StringUtils.ordinalIndexOf("aabaabaa", "a", 3);// 1(查找第n次出现的位置)

        // 反向查找字符串第一次出现的位置
        StringUtils.lastIndexOf("aabaabaa", 'b');// 5
        StringUtils.lastIndexOf("aabaabaa", 'b', 4);// 2
        StringUtils.lastOrdinalIndexOf("aabaabaa", "ab", 2);// 1

        // 判断字符串大写、小写
        StringUtils.isAllUpperCase("ABC");// true
        StringUtils.isAllLowerCase("abC");// false

        // 判断是否为空(注：isBlank与isEmpty 区别)
        StringUtils.isBlank(null);
        StringUtils.isBlank("");
        StringUtils.isBlank(" ");// true
        StringUtils.isNoneBlank(" ", "bar");// false

        StringUtils.isEmpty(null);
        StringUtils.isEmpty("");// true
        StringUtils.isEmpty(" ");// false
        StringUtils.isNoneEmpty(" ", "bar");// true

        // 判断字符串数字
        StringUtils.isNumeric("123");// false
        StringUtils.isNumeric("12 3");// false (不识别运算符号、小数点、空格……)
        StringUtils.isNumericSpace("12 3");// true

        // 数组中加入分隔符号
        //StringUtils.join([1, 2, 3], ';');// "1;2;3"

        // 大小写转换
        StringUtils.upperCase("aBc");// "ABC"
        StringUtils.lowerCase("aBc");// "abc"
        StringUtils.swapCase("The dog has a BONE");// "tHE DOG HAS A bone"

        // 替换字符串内容……（replacePattern、replceOnce）
        StringUtils.replace("aba", "a", "z");// "zbz"
        StringUtils.overlay("abcdef", "zz", 2, 4);// "abzzef"(指定区域)
        StringUtils.replaceEach("abcde", new String[]{"ab", "d"},
                new String[]{"w", "t"});// "wcte"(多组指定替换ab->w，d->t)

        // 重复字符
        StringUtils.repeat('e', 3);// "eee"

        // 反转字符串
        StringUtils.reverse("bat");// "tab"

        // 删除某字符
        StringUtils.remove("queued", 'u');// "qeed"

        // 分割字符串
        StringUtils.split("a..b.c", '.');// ["a", "b", "c"]
        StringUtils.split("ab:cd:ef", ":", 2);// ["ab", "cd:ef"]
        StringUtils.splitByWholeSeparator("ab-!-cd-!-ef", "-!-", 2);// ["ab", "cd-!-ef"]
        StringUtils.splitByWholeSeparatorPreserveAllTokens("ab::cd:ef", ":");// ["ab"," ","cd","ef"]

        // 去除首尾空格，类似trim……（stripStart、stripEnd、stripAll、stripAccents）
        StringUtils.strip(" ab c ");// "ab c"
        StringUtils.stripToNull(null);// null
        StringUtils.stripToEmpty(null);// ""

        // 截取字符串
        StringUtils.substring("abcd", 2);// "cd"
        StringUtils.substring("abcdef", 2, 4);// "cd"

        // left、right从左(右)开始截取n位字符
        StringUtils.left("abc", 2);// "ab"
        StringUtils.right("abc", 2);// "bc"
        // 从第n位开始截取m位字符       n  m
        StringUtils.mid("abcdefg", 2, 4);// "cdef"

        StringUtils.substringBefore("abcba", "b");// "a"
        StringUtils.substringBeforeLast("abcba", "b");// "abc"
        StringUtils.substringAfter("abcba", "b");// "cba"
        StringUtils.substringAfterLast("abcba", "b");// "a"

        StringUtils.substringBetween("tagabctag", "tag");// "abc"
        StringUtils.substringBetween("yabczyabcz", "y", "z");// "abc"

        String randomAlphanumeric = RandomStringUtils.randomAlphanumeric(5);

        // 检查所有元素是否为空
        // 如果有一个元素为空返回false，所有元素不为空或元素为empty返回true
        ObjectUtils.allNotNull("values");

        // 检查元素是否为空
        // 如果有一个元素不为空返回true
        ObjectUtils.anyNotNull("values");

        // 比较两个对象,返回一个int值
        //ObjectUtils.compare(c1, c2);

        // 如果对象为空返回一个默认值
        ObjectUtils.defaultIfNull("object", "defaultValue");

        // 返回数组中第一个不为空的值
        ObjectUtils.firstNonNull("values");

        // 判断两个对象不相等，返回一个boolean
        ObjectUtils.notEqual("object1", "object2");


        // StringEscapeUtils类可以对html、js、xml、sql等代码进行转义来防止SQL注入及XSS注入
        System.out.println(StringEscapeUtils.escapeHtml4("<a>abc</a>"));// 转义html脚本
        System.out.println(StringEscapeUtils.unescapeHtml4("&lt;a&gt;abc&lt;/a&gt;"));// 反转义html脚本

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


        /*
         * org.apache.commons.lang3.tuple包下有：
         * <p>
         * Pair 封装一对键值对，实现类：可变 MutablePair<L,R>，不可变 ImmutablePair<L,R>
         * Triple 封装3个值的类，实现类：可变 MutableTriple<L,M,R>，不可变 ImmutableTriple<L,M,R>
         * </p>
         * https://github.com/vavr-io/vavr 也有元组实现
         */
        Pair<String, String> of = Pair.of("goodsBeans", "totalProperty");
        System.out.println(of.getLeft());

        ImmutableTriple<String, String, String> three = ImmutableTriple.of("1", "2", "3");
        System.out.println(three.left);

        /*
         * 动态根据字段排序
         */
        //Collections.<Student> sort(list, new BeanComparator<Student>("score"));

        // 线程池中创建 ThreadFactory 设置线程名称
        ThreadFactory basicThreadFactory = new BasicThreadFactory.Builder().daemon(true).namingPattern(
                "basicThreadFactory-").build();
        ExecutorService exec = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(10), basicThreadFactory);
    }

    /**
     * 比较两个对象指定的属性值是否相等
     * <p>
     * 忽略指定字段并比较两个对象：EqualsBuilder.reflectionEquals(lhs, rhs, "id","code")
     *
     * @param lhs    第一个对象
     * @param rhs    第二个对象
     * @param fields 需要比较的属性字段
     * @return 相同返回true，不同则返回false
     * @throws IntrospectionException
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static boolean equalsFields(Object lhs, Object rhs, String... fields) throws IntrospectionException,
            InvocationTargetException, IllegalAccessException {
        Class<?> lhsClazz = lhs.getClass();
        Class<?> rhsClazz = rhs.getClass();
        if (lhsClazz != rhsClazz) {
            return false;
        }
        // 数组转Map
        Map<String, String> fieldMap = Arrays.stream(fields).collect(Collectors.toMap(e -> e, Function.identity()));
        EqualsBuilder equalsBuilder = new EqualsBuilder();
        // 获取JavaBean的所有属性
        PropertyDescriptor[] pds = Introspector.getBeanInfo(lhsClazz, Object.class).getPropertyDescriptors();
        for (PropertyDescriptor pd : pds) {
            // 遍历获取属性名
            String name = pd.getName();
            if (name.equals(fieldMap.get(name))) {
                // 获取属性的get方法
                Method readMethod = pd.getReadMethod();
                // 调用get方法获得属性值
                Object lhsValue = readMethod.invoke(lhs);
                Object rhsValue = readMethod.invoke(rhs);
                // 添加到比较
                equalsBuilder.append(lhsValue, rhsValue);
            }
        }
        return equalsBuilder.isEquals();
    }

    @Override
    public boolean equals(Object obj) {
        /*CommonsLang3 rhs = (CommonsLang3) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(field1, rhs.field1)
                .append(field2, rhs.field2)
                .append(field3, rhs.field3)
                .isEquals();*/
        return EqualsBuilder.reflectionEquals(this, obj, "id,age,userName");
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public String toString() {
        // return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
        // 如果需要排除某些字段， 可以使用ReflectionToStringBuilder.toStringExclude方法
        return ReflectionToStringBuilder.toString(this);
    }
}
