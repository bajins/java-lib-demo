package com.bajins.demo;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Properties;

/**
 * 发送邮件
 *
 * @author claer admin@bajins.com
 */
public class JavaxEmail {


    /**
     * 发送邮件
     *
     * @param session   邮件服务器会话
     * @param fromEmail 发送人
     * @param toEmail   接收人
     * @param subject   主题
     * @param content   内容
     * @throws MessagingException
     */
    public static void sendEmail(Session session, String fromEmail, String toEmail, String subject, String content) throws MessagingException, UnsupportedEncodingException, FileNotFoundException {
        //根据已有的eml邮件文件创建 MimeMessage 对象
        //MimeMessage message = new MimeMessage(session, new FileInputStream(subject + ".eml"));

        MimeMessage message = new MimeMessage(session);
        // 2. From: 发件人
        // 其中 InternetAddress 的三个参数分别为: 邮箱, 显示的昵称(只用于显示, 没有特别的要求), 昵称的字符集编码
        message.setFrom(new InternetAddress(fromEmail, "USER_AA", "UTF-8"));
        /*
         * 3. RecipientType.TO||BCC||CC
         *     TO表示主要接收人
         *     BCC表示秘密抄送人
         *     CC表示抄送人
         * InternetAddress  接收者的邮箱地址
         */
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail, "USER_CC", "UTF-8"));
        // To: 增加收件人（可选）
        //message.addRecipient(MimeMessage.RecipientType.TO, new InternetAddress("dd@receive.com", "USER_DD", "UTF-8"));
        // Cc: 抄送（可选）
        //message.setRecipient(MimeMessage.RecipientType.CC, new InternetAddress("ee@receive.com", "USER_EE", "UTF-8"));
        // Bcc: 密送（可选）
        //message.setRecipient(MimeMessage.RecipientType.BCC, new InternetAddress("ff@receive.com", "USER_FF",
        // "UTF-8"));
        // 4. Subject: 邮件主题
        message.setSubject(subject, "UTF-8");
        // 5. Content: 邮件正文（可以使用html标签）
        //message.setContent(content, "text/html;charset=UTF-8");

        /*
         * 下面是复杂邮件内容的创建:
         */
        // 5.1. 创建图片“节点”
        MimeBodyPart image = new MimeBodyPart();
        DataHandler dh = new DataHandler(new FileDataSource("FairyTail.jpg")); // 读取本地文件
        image.setDataHandler(dh);		            // 将图片数据添加到“节点”
        image.setContentID("image_fairy_tail");	    // 为“节点”设置一个唯一编号（在文本“节点”将引用该ID）

        // 5.2. 创建文本“节点”
        MimeBodyPart text = new MimeBodyPart();
        //    这里添加图片的方式是将整个图片包含到邮件内容中, 实际上也可以以 http 链接的形式添加网络图片
        text.setContent("这是一张图片<br/><img src='cid:image_fairy_tail'/>", "text/html;charset=UTF-8");

        // 5.3. （文本+图片）设置 文本 和 图片 “节点”的关系（将 文本 和 图片 “节点”合成一个混合“节点”）
        MimeMultipart mm_text_image = new MimeMultipart();
        mm_text_image.addBodyPart(text);
        mm_text_image.addBodyPart(image);
        mm_text_image.setSubType("related");	// 关联关系

        // 5.4. 将 文本+图片 的混合“节点”封装成一个普通“节点”
        //    最终添加到邮件的 Content 是由多个 BodyPart 组成的 Multipart, 所以我们需要的是 BodyPart,
        //    上面的 mm_text_image 并非 BodyPart, 所有要把 mm_text_image 封装成一个 BodyPart
        MimeBodyPart text_image = new MimeBodyPart();
        text_image.setContent(mm_text_image);

        // 5.5. 创建附件“节点”
        MimeBodyPart attachment = new MimeBodyPart();
        DataHandler dh2 = new DataHandler(new FileDataSource("妖精的尾巴目录.doc"));  // 读取本地文件
        attachment.setDataHandler(dh2);			                                    // 将附件数据添加到“节点”
        attachment.setFileName(MimeUtility.encodeText(dh2.getName()));	            // 设置附件的文件名（需要编码）

        // 5.6. 设置（文本+图片）和 附件 的关系（合成一个大的混合“节点” / Multipart ）
        MimeMultipart mm = new MimeMultipart();
        mm.addBodyPart(text_image);
        mm.addBodyPart(attachment);		// 如果有多个附件，可以创建多个多次添加
        mm.setSubType("mixed");			// 混合关系

        // 5.7. 设置整个邮件的关系（将最终的混合“节点”作为邮件的内容添加到邮件对象）
        message.setContent(mm);

        // 6. 设置显示的发件时间
        message.setSentDate(new Date());
        // 7. 保存前面的设置
        message.saveChanges();

        // 8. 将该邮件保存到本地
        try (OutputStream out = Files.newOutputStream(Paths.get(subject + ".eml"));) {
            message.writeTo(out);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Transport.send(message);
        /*
        // 根据 Session 获取邮件传输对象
        Transport transport = session.getTransport();
        // 使用 邮箱账号 和 密码 连接邮件服务器
        // 这里认证的邮箱必须与 message 中的发件人邮箱一致，否则报错
        transport.connect(fromEmail, password);
        // 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(message, message.getAllRecipients());
        // 关闭连接
        transport.close();
        */
    }

    /**
     * 获取邮件服务器session会话
     *
     * @param protocol  指定发送的邮箱的邮箱协议
     * @param host      指定SMTP服务器地址
     * @param port      指定SMTP服务器端口
     * @param auth      是否需要SMTP验证
     * @param tls       是否开启tls
     * @param ssl       是否开启ssl
     * @param fromEmail 邮件服务器账户
     * @param password  密码
     * @return
     */
    public static Session getSession(String protocol, String host, String port, String auth, String tls, String ssl,
                                     String fromEmail, String password) {
        Properties props = new Properties(); // 用于连接邮件服务器的参数配置（发送邮件时才需要用到）
        props.setProperty("mail.transport.protocol", protocol);// 指定发送的邮箱的邮箱协议
        /*
         * 接收邮件 (IMAP) 服务器
         * imap.gmail.com
         * 要求 SSL：是
         * 端口：993
         * ---------------------
         * 发送邮件 (SMTP) 服务器
         * smtp.gmail.com
         * 要求 SSL：是
         * 要求 TLS：是（如适用）
         * 使用身份验证：是
         * SSL 端口：465
         * TLS/STARTTLS 端口：587
         *
         * ***************************************
         *
         * 接收邮件 (IMAP) 服务器
         * 服务器名称: outlook.office365.com
         * 端口: 993
         * 加密方法: TLS
         * ---------------------
         * 发送邮件 (SMTP) 服务器
         * 服务器名称: smtp.office365.com
         * 端口: 587
         * 加密方法: STARTTLS
         */
        props.setProperty("mail.smtp.host", host);// 指定SMTP服务器
        props.setProperty("mail.smtp.port", port);// smtp是发信邮件服务器,端口是25
        props.setProperty("mail.smtp.auth", auth);// 指定是否需要SMTP验证
        props.setProperty("mail.smtp.starttls.enable", tls);
        props.setProperty("mail.smtp.ssl", ssl);

        /*MailSSLSocketFactory sf = new MailSSLSocketFactory();
        sf.setTrustAllHosts(true);
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.ssl.socketFactory", sf);*/

        //props.setProperty("mail.smtp.socketFactory.fallback", "false");
        //Gmail需加上以下配置
        /*props.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");*/

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, password);
            }
        });
        // 开启 Session debugger 模式, 可以看到邮件发送的运行状态
        session.setDebug(true);

        return session;
    }

    public static void main(String[] args) {

    }

}
