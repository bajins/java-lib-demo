package com.bajins.demo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateSerializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import com.google.common.base.CaseFormat;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class JacksonLearning {

    /**
     * 如果有内部类，在进行绑定时是采用的静态类调用方式创建内部类实例，即 Demo.new InnerDemo()，所以需要在内部类上使用static关键字
     * <p>
     * JackSon拒绝尝试使用非静态内部类的基本原因（序列化是可以正常工作的）：<br/>
     * 是因为这样的类没有实例化的一般方法，没有无参数的构造函数，
     * 也没有@jsoncreator注释其他构造函数或工厂方法（或单个字符串参数的构造函数）
     * </p>
     * 非静态内部类（包括匿名内部类）被编译器通过隐藏的构造器传递了一组隐藏变量，直接结果就是：<br/>
     * 无默认构造函数，即使代码里面有一个显式声明的无参构造函数
     *
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        //这个特性，决定了解析器是否将自动关闭那些不属于parser自己的输入源。
        // 如果禁止，则调用应用不得不分别去关闭那些被用来创建parser的基础输入流InputStream和reader；
        //默认是true
        objectMapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        //是否允许解析使用Java/C++ 样式的注释（包括'/'+'*' 和'//' 变量）
        objectMapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        //设置为true时，属性名称不带双引号
        objectMapper.configure(JsonGenerator.Feature.QUOTE_FIELD_NAMES, false);
        //反序列化是是否允许属性名称不带双引号（可以进一步减小json体积）
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        //是否允许单引号来包住属性名称和字符串值
        objectMapper.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        //是否允许JSON字符串包含非引号控制字符（值小于32的ASCII字符，包含制表符和换行符）
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        //是否允许JSON整数以多个0开始
        objectMapper.configure(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
        //按字母顺序排序属性,默认false
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY,true);
        //是否以类名作为根元素，可以通过@JsonRootName来自定义根元素名称,默认false
        objectMapper.configure(SerializationFeature.WRAP_ROOT_VALUE,true);
        //是否缩放排列输出,默认false
        objectMapper.configure(SerializationFeature.INDENT_OUTPUT,false);
        //序列化Date日期时以timestamps输出，默认true 取消时间的转化格式,默认是时间戳,可以取消,同时需要设置要表现的时间格式
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS,true);
        //序列化枚举是否以toString()来输出，默认false，即默认以name()来输出
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_TO_STRING, true);
        //序列化枚举是否以ordinal()来输出，默认false
        objectMapper.configure(SerializationFeature.WRITE_ENUMS_USING_INDEX,false);
        //序列化单元素数组时不以数组来输出，默认false
        objectMapper.configure(SerializationFeature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED,true);
        //序列化Map时对key进行排序操作，默认false
        objectMapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS,true);
        //序列化char[]时以json数组输出，默认false
        objectMapper.configure(SerializationFeature.WRITE_CHAR_ARRAYS_AS_JSON_ARRAYS,true);
        //序列化BigDecimal时是输出原始数字还是科学计数，默认false，即以toPlainString()科学计数方式来输出
        objectMapper.configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN,true);
        // 排除json字符串中实体类没有的字段 @JsonIgnore
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        //objectMapper.configure(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS.mappedFeature(), true);
        // 序列化的时候序列对象的所有属性
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        // 如果是空对象的时候,不抛异常
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        //GMT+8
        //map.put("CTT", "Asia/Shanghai");
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // 日期格式
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        // https://zhuanlan.zhihu.com/p/277834439
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(dateTimeFormatter));
        javaTimeModule.addSerializer(LocalDate.class, new LocalDateSerializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(dateTimeFormatter));
        javaTimeModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(dateTimeFormatter));

        objectMapper.configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, false); // true忽略已被注册的 module
        objectMapper.registerModule(javaTimeModule); // 避免无效，先把IGNORE_DUPLICATE_MODULE_REGISTRATIONS设为false
        objectMapper.configure(MapperFeature.IGNORE_DUPLICATE_MODULE_REGISTRATIONS, true);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
        // Include.NON_NULL 属性为NULL 不序列化
        // ALWAYS // 默认策略，任何情况都执行序列化
        // NON_EMPTY // null、集合数组等没有内容、空字符串等，都不会被序列化
        // NON_DEFAULT // 如果字段是默认值，就不会被序列化
        // NON_ABSENT // null的不会序列化，但如果类型是AtomicReference，依然会被序列化
        // @JsonInclude(value = Include.NON_NULL)
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        // 序列化结果格式化，美化输出
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 枚举输出成字符串
        // 输出索引
        objectMapper.enable(SerializationFeature.WRITE_ENUMS_USING_INDEX);
        //空对象不要抛出异常：
        objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        //Date、Calendar等序列化为时间格式的字符串(如果不执行以下设置，就会序列化成时间戳格式)：
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //反序列化时，遇到未知属性不要抛出异常：
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //反序列化时，遇到忽略属性不要抛出异常：
        objectMapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
        //反序列化时，空字符串对于的实例属性为null：
        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        // 命名策略：使用属性名称，可以为不同字段设置不同的名称
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CAMEL_CASE);// 驼峰命名，字段的首字母小写
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);// 驼峰命名，字段的首字母大写
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);// 字段小写，多个单词以下划线_分隔
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.KEBAB_CASE);// 字段小写，多个单词以中横线-分隔
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.LOWER_CASE);// 字段小写，多个单词间无分隔符

        System.out.println(new PropertyNamingStrategy.SnakeCaseStrategy().translate("userName")); // 驼峰转下划线
        // 驼峰转下划线
        PropertyNamingStrategy.UpperCamelCaseStrategy upperCamelCaseStrategy =
                new PropertyNamingStrategy.UpperCamelCaseStrategy();
        System.out.println(upperCamelCaseStrategy.translate("user_Name").replaceAll("_", ""));

        /*SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        // Person类的属性过滤器（只序列化car,house,name字段）
        filterProvider.addFilter("person", SimpleBeanPropertyFilter.filterOutAllExcept(Sets.newHashSet("car",
        "house", "name")));*/


        // HashMap<String,String> 转json
        HashMap<String, String> map = new HashMap<>();
        map.put("name", "jack");
        map.put("city", "beijin");
        String mjson = objectMapper.writeValueAsString(map);
        System.out.println(mjson);

        // json转 HashMap<String,String>
        HashMap<String, String> mmap = objectMapper.readValue(mjson, HashMap.class);
        System.out.println(mmap);

        // json转 Map<String, Object>
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(HashMap.class, String.class,
                Object.class);
        JavaType javaType1 = objectMapper.getTypeFactory().constructMapType(HashMap.class, String.class,
                Object.class);
        Map<String, Object> map1 = objectMapper.readValue(mjson, javaType);
        System.out.println(map1);

        // 转换为 Map<String, Object>
        Map<String, Object> map2 = objectMapper.readValue(mjson, new TypeReference<HashMap<String, Object>>() {
        });
        System.out.println(map2);


        // List<Map<String, String>> 转json
        List<Map<String, String>> list = new ArrayList<>();
        list.add(map);
        SerializerProvider serializerProvider = objectMapper.getSerializerProvider();
        serializerProvider.setDefaultKeySerializer(new JsonSerializer<Object>() {// 处理属性名称，特别是Map的KEY是下划线风格时
            @Override
            public void serialize(Object o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
                    throws IOException {
                String id = (String) o;
                String to = CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, id);
                jsonGenerator.writeFieldName(to);
            }
        });
        String ljson = objectMapper.writeValueAsString(list);
        System.out.println(ljson);

        // json转 转换为 ArrayList<Map<String,Object>>
        List<Map<String, Object>> lm = objectMapper.readValue(ljson, ArrayList.class);
        System.out.println(lm);

        // 转换为 ArrayList<Map<String,Object>>
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(List.class,
                HashMap.class);
        CollectionType collectionType1 = objectMapper.getTypeFactory().constructCollectionType(List.class,
                javaType);
        ArrayList<HashMap<String, Object>> list1 = objectMapper.readValue(ljson, collectionType);
        System.out.println(list1);

        // 转换为 ArrayList<Map<String,Object>>
        TypeReference<ArrayList<HashMap<String, Object>>> typeReference = new TypeReference<ArrayList<HashMap<String,
                Object>>>() {
        };
        ArrayList<Map<String, Object>> list2 = objectMapper.readValue(ljson, typeReference);
        System.out.println(list2);

        /*
         * xml和对象互转
         */
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        // 反序列化时，若实体类没有对应的属性，是否抛出JsonMappingException异常，false忽略掉
        xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        //xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        xmlMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        xmlMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        xmlMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        // XML标签名:使用骆驼命名的属性名，
        xmlMapper.setPropertyNamingStrategy(PropertyNamingStrategy.UPPER_CAMEL_CASE);
        // 设置转换模式
        xmlMapper.enable(MapperFeature.USE_STD_BEAN_NAMING);

        String xml = xmlMapper.writeValueAsString(list2);

        MappingJackson2XmlHttpMessageConverter mappingJackson2XmlHttpMessageConverter =
                new MappingJackson2XmlHttpMessageConverter();
        String xml2 = mappingJackson2XmlHttpMessageConverter.getObjectMapper().writeValueAsString(list2);
    }

    /**
     * 自定义命名策略，为字段添加下划线前缀，其中setter方法使用默认的字段名
     */
    public static class AppendPrefixStrategyForSetter extends PropertyNamingStrategy.PropertyNamingStrategyBase {
        @Override
        public String translate(String input) {
            return '_' + input;
        }

        @Override
        public String nameForSetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
            return defaultName;
        }
    }

    /**
     * 出参保留两位小数
     * <pre>
     * &#064;JsonDeserialize(using  = DeserializerBigDecimal.class)
     * </pre>
     */
    public static class DeserializerBigDecimal extends JsonDeserializer<BigDecimal> {
        @Override
        public BigDecimal deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            String value = jsonParser.getValueAsString();
            if (!StringUtils.hasText(value)) {
                return null;
            }
            // 这里取floor
            return new BigDecimal(value).setScale(2, RoundingMode.FLOOR);
        }
    }

    /**
     * 入参保留两位小数
     * <pre>
     * &#064;JsonSerialize(using  = SerializerBigDecimal.class)
     * </pre>
     */
    public static class SerializerBigDecimal extends JsonSerializer<BigDecimal> {
        @Override
        public void serialize(BigDecimal bigDecimal, JsonGenerator jsonGenerator,
                              SerializerProvider serializerProvider) throws IOException {
            // 注意这里不能使用jsonGenerator.writeNumber(result);方法，不然会把.00去掉
            jsonGenerator.writeString(bigDecimal.setScale(2, RoundingMode.FLOOR).toPlainString());
        }
    }
}