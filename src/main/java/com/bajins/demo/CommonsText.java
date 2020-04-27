package com.bajins.demo;

import org.apache.commons.text.StringEscapeUtils;

/**
 * org.apache.commons.commons-text
 */
public class CommonsText {

    public static void main(String[] args) {
        // Html 转码.
        String html = StringEscapeUtils.escapeHtml4("html");

        // Html 解码.
        String htmlEscaped = StringEscapeUtils.unescapeHtml4("htmlEscaped");

        // Xml 转码.
        String xml = StringEscapeUtils.escapeXml11("xml");

        // Xml 解码.
        String xmlEscaped = StringEscapeUtils.unescapeXml("xmlEscaped");
    }
}
