package com.bajins.demo;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
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
    public static void sendEmail(Session session, String fromEmail, String toEmail, String subject, String content) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setSubject(subject);
        message.setSentDate(new Date());
        //setFrom 表示用哪个邮箱发送邮件
        message.setFrom(new InternetAddress(fromEmail));
        /**
         * RecipientType.TO||BCC||CC
         *     TO表示主要接收人
         *     BCC表示秘密抄送人
         *     CC表示抄送人
         * InternetAddress  接收者的邮箱地址
         */
        message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmail));

        message.setContent(content, "text/html;charset=utf-8");

        Transport.send(message);

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
        Properties props = new Properties();
        props.setProperty("mail.transport.protocol", protocol);// 指定发送的邮箱的邮箱协议
        /**
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
         * */
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
        return session;
    }

}
