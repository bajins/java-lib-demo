package com.bajins.demo;

import org.apache.commons.io.FileUtils;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.MultipleFailureException;
import org.junit.runners.model.Statement;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 解决内网情况下，DTD验证失败<br/>
 * 参考 https://www.jianshu.com/p/6c279b4d7f5c<br/>
 * 在spring里面存在spring.schemas spring在解析的时候会自动映射，mybatis也是同样道理<br/>
 * 当解析到dtd时自动映射成对应jar下面的文件，这样无需去网络获取<br/>
 * <p>
 * 需修改spring-mybatis.xml中的mybatis-3-config.dtd为本地绝对路径，解决报错：Connection refused: connect <br/>
 * 修改spring-context-web.xml中/WEB-INF/views.xml为file:webapp/WEB-INF/views.xml，<br/>
 * 解决报错：Could not open ServletContext resource [/WEB-INF/views.xml]
 * </p>
 *
 * @see org.apache.ibatis.builder.xml.XMLMapperEntityResolver mybatis默认解析类
 */
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class JUnitRunner extends SpringJUnit4ClassRunner {

    /*@Resource
    private DataSource dataSource;*/
    /*private static String[] springFiles = { "beans/my-beans-config.xml" };
    private ClassPathXmlApplicationContext context;*/

    static {
        String projectDir = System.getProperty("user.dir"); // 当前项目目录
        String tmpDir = System.getProperty("java.io.tmpdir"); // 缓存目录
        Path tempDirPath = Paths.get(tmpDir);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Path destPath = Paths.get(projectDir, "webapp", "WEB-INF", "classes");
        Path configTestDir = Paths.get(projectDir, "configtest");

        Path dtd = configTestDir.resolve("mybatis-3-config.dtd");
        Path dtdTempPath = tempDirPath.resolve(dtd.getFileName());
        File dtdFile = dtd.toFile();
        if (dtdFile.exists()) {
            try {
                FileUtils.copyFile(dtdFile, dtdTempPath.toFile()); // 把dtd文件复制到缓存目录
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File configTestDirFile = configTestDir.toFile();
        File[] listFiles = configTestDirFile.listFiles((dir, name) -> name.endsWith(".xml") || name.endsWith(".dtd"));

        for (File file : listFiles) {

            File destFile = destPath.resolve(file.getName()).toFile();

            if (file.getName().equals("spring-mybatis.xml")) {
                try {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    builder.setEntityResolver((publicId, systemId) -> {
                        // return new InputSource(new ByteArrayInputStream("".getBytes()));
                        return new InputSource(new StringReader(""));
                    });
                    Document document = builder.parse(file);
                    document.setXmlStandalone(false);
                    // document.setXmlVersion("1.0");

                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes"); // 换行
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "-//mybatis.org//DTD Config 3.0//EN");
                    // 把xml中的dtd指向到缓存目录
                    transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, dtdTempPath.toAbsolutePath().toString());
                    transformer.transform(new DOMSource(document), new StreamResult(destFile));

                } catch (ParserConfigurationException | SAXException | IOException | TransformerException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    FileUtils.copyFile(file, destFile);

                    // 此方式：文件或目录被其他进程占用无法复制
                    // Files.copy(Paths.get(file.getAbsolutePath()), destPath, StandardCopyOption.REPLACE_EXISTING);

                    /*Process exec = Runtime.getRuntime().exec("cmd /c copy " + file.getAbsolutePath() + " " +
                    destPath);
                    if (exec.isAlive()) { // 运行结束
                        System.out.println(exec.exitValue());
                        try (InputStream inputStream = exec.getInputStream();
                                InputStreamReader isr = new InputStreamReader(inputStream, "GBK");
                                BufferedReader br = new BufferedReader(isr)) {
                            String line = null;
                            while ((line = br.readLine()) != null) {
                                System.out.println(line);
                            }
                        }
                    }*/
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * @param clazz
     * @throws Exception
     */
    public JUnitRunner(Class<?> clazz) throws Exception {
        super(clazz);

        /**
         * 服务器(比如tomcat)启动时,它有自己的容器加载JNDI,而在junit里，没有这个JNDI
         */

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

    @Override
    public void run(RunNotifier notifier) {
        super.run(notifier);
    }

    @Override
    protected Statement withBeforeClasses(Statement statement) {
        /*if (statement instanceof RunBefores) {

        } else if (statement instanceof RunBeforeTestClassCallbacks) {

        }*/
        return super.withBeforeClasses(statement);
    }

    @Override
    protected Statement withAfterClasses(Statement statement) {
        statement = super.withAfterClasses(statement);
        /*if (statement instanceof RunAfters) {

        } else if (statement instanceof RunAfterTestClassCallbacks) {

        } else if (statement instanceof ExpectException) {

        }*/
        return new RunAfterFinally(statement);
    }

    @Override
    protected Statement withBefores(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        return super.withBefores(frameworkMethod, testInstance, statement);
    }

    @Override
    protected Statement withAfters(FrameworkMethod frameworkMethod, Object testInstance, Statement statement) {
        return super.withAfters(frameworkMethod, testInstance, statement);
    }

    @Override
    protected Statement withPotentialRepeat(FrameworkMethod frameworkMethod, Object testInstance, Statement next) {
        return super.withPotentialRepeat(frameworkMethod, testInstance, next);
    }

    /**
     * 最后只执行一次：finally<br/>
     * https://www.cnblogs.com/jinsdu/p/4709270.html<br/>
     * https://www.cnblogs.com/linkworld/p/9061967.html
     *
     * @see ParentRunner#withAfterClasses(org.junit.runners.model.Statement)
     * @see org.junit.internal.runners.statements.RunAfters#evaluate()
     */
    public static class RunAfterFinally extends Statement {

        private final Statement next;

        public RunAfterFinally(Statement next) {
            this.next = next;
        }

        @Override
        public void evaluate() throws Throwable {
            List<Throwable> errors = new ArrayList<>();
            try {
                next.evaluate();
            } catch (Throwable e) {
                errors.add(e);
            } finally { // 最终执行
                System.out.println();
            }
            MultipleFailureException.assertEmpty(errors);
        }
    }

}
