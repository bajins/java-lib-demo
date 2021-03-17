package com.bajins.demo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.naming.NamingException;
import javax.sql.DataSource;

/*@SpringBootTest
@RunWith(SpringRunner.class)
//@RunWith(SpringJUnit4ClassRunner.class)
@AutoConfigureMockMvc
@ContextConfiguration(locations = {"classpath:spring-*.xml", "file:WebContent/META-INF/datasource-test.xml"})*/
public class DemoTest {

    /*@Resource
    private DataSource dataSource;*/
    /*private static String[] springFiles = { "beans/my-beans-config.xml" };
    private ClassPathXmlApplicationContext context;*/


    @BeforeClass
    public static void initClass() throws NamingException {
        // 服务器(比如tomcat)启动时,它有自己的容器加载JNDI,而在junit里，没有这个JNDI
        // org.springframework.mock.jndi软件包中提供的功能来模拟数据源
        /*context = new ClassPathXmlApplicationContext(springFiles);
        DataSource dataSource = (DataSource) context.getBean("dataSource");*/

        // 使用org.springframework.jdbc.datasource.DriverManagerDataSource创建数据源
        /*DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        ds.setUrl("jdbc:mysql://localhost:8088/test");
        ds.setUsername("root");
        ds.setPassword("1234");*/

        // alibaba的druid获取 datasource
        /*Map<String, String> map = new HashMap<>();
        map.put(DruidDataSourceFactory.PROP_DRIVERCLASSNAME, drive);
        map.put(DruidDataSourceFactory.PROP_URL, url);
        map.put(DruidDataSourceFactory.PROP_USERNAME, username);
        map.put(DruidDataSourceFactory.PROP_PASSWORD, password);
        DataSource ds = DruidDataSourceFactory.createDataSource(map);*/

        // org.springframework.jndi.JndiObjectFactoryBean获取JNDI数据源配置信息
        // InitialContext 在应用服务器（如Tomcat）的上下文里才存在。
        // 不能用main函数直接测试，只能放到tomcat或者servlet、jsp显示
        /*Tomcat tomcat = new Tomcat();
        tomcat.enableNaming(); // 启用tomcat的JNDI
        tomcat.start();
        tomcat.getServer().await();*/
        /*Context context = new InitialContext();
        DataSource ds = (DataSource)context.lookup("java:comp/env/myDataSourceJNDI");*/
        //DataSource ds = (DataSource)InitialContext.doLookup("java:comp/env/myDataSourceJNDI");

        //SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
        /*SimpleNamingContextBuilder builder = SimpleNamingContextBuilder.emptyActivatedContextBuilder();
        //builder.bind("java:comp/env/myDataSourceJNDI", dataSource);
        builder.activate();*/
    }

    /**
     * 比@BeforeClass后执行
     */
    @Before
    public void init() {
        System.out.println("ehcache缓存目录:" + System.getProperty("java.io.tmpdir"));
        try {
            ClassPathXmlApplicationContext app = new ClassPathXmlApplicationContext("classpath:spring/springtest" +
                    "/InitJndi.xml");
            DataSource ds = (DataSource) app.getBean("dataSource");
            /*SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
            builder.bind("java:comp/env/jdbc/db1", ds);
            builder.activate();

            ClassPathXmlApplicationContext appcontext = new ClassPathXmlApplicationContext("classpath:spring/spring.xml");

            //这里提取测试bean
            testCacheService = (TestCacheService) appcontext.getBean("testCacheService");
            sqlService = (SqlService) appcontext.getBean("sqlService");

            baseDao = (BaseDao) appcontext.getBean("baseDao");
            hibernateTemplate = (HibernateTemplate) appcontext.getBean("hibernateTemplate");*/

            /*SimpleNamingContextBuilder.emptyActivatedContextBuilder();
            this.initContext = new InitialContext();
            this.initContext.bind("java:comp/env/jdbc/datasource", new DriverManagerDataSource("jdbc:h2:mem:testdb"));
            DataSource ds = (DataSource) this.initContext.lookup("java:comp/env/jdbc/datasource");*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
