package com.bajins.demo;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

/**
 * 如果是通过命令：java -jar test.jar，则直接ctrl+c，可以执行到下边的退出方法。<br/>
 * 如果是用IDEA启动，则按下Exit（注意不是红色的Stop）可以执行到下边的退出方法。<br/>
 * 如果你用的mvn spring-boot:run来启动运行的话，可能不会执行销毁的操作。
 */
@Component
public class DisposableBeanTest implements DisposableBean {
    @Override
    public void destroy() throws Exception {

        System.out.println("springboot程序结束  implements DisposableBean ");
    }
}
