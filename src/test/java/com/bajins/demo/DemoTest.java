package com.bajins.demo;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.web.WebAppConfiguration;

import javax.sql.DataSource;

@SpringBootTest
//@RunWith(SpringRunner.class)
//@RunWith(SpringJUnit4ClassRunner.class)
@RunWith(JUnitRunner.class)
//@AutoConfigureMockMvc
@WebAppConfiguration
@ContextConfiguration(locations = {
        "classpath:application.properties",
        "classpath:application-dev.properties",
        /*"file:WebContent/META-INF/datasource-test.xml"*/
})
//@TransactionConfiguration(transactionManager = "transactionManager", defaultRollback = true)
public class DemoTest extends AbstractTransactionalJUnit4SpringContextTests {


    @Before
    public static void before() {
        System.out.println("运行测试之前，会运行多次");
    }

    @BeforeClass
    public static void beforeClass() {
        System.out.println("运行测试之前，且只运行一次");
    }

    /**
     * 比@BeforeClass后执行
     */
    @Before
    public void init() {
        System.out.println("ehcache缓存目录:" + System.getProperty("java.io.tmpdir"));
        try {
            ClassPathXmlApplicationContext app = new ClassPathXmlApplicationContext(
                    "classpath:spring/springtest/InitJndi.xml");
            DataSource ds = (DataSource) app.getBean("dataSource");
            /*SimpleNamingContextBuilder builder = new SimpleNamingContextBuilder();
            builder.bind("java:comp/env/jdbc/db1", ds);
            builder.activate();

            ClassPathXmlApplicationContext appcontext = new ClassPathXmlApplicationContext(
            "classpath:spring/spring.xml");

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
