package com.bajins.demo;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.springframework.util.StringUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.util.StreamReaderDelegate;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * https://stackoverflow.com/questions/1537207/how-to-convert-xml-to-java-util-map-and-vice-versa
 */
public class XmlUtils {

    //public static void main(String[] args) throws DocumentException, IOException {
    //    String textFromFile = FileUtils.readFileToString(new File("ehcache3.xml"), "UTF-8");
    //    Map<String, Object> map = xml2map(textFromFile, false);
    //    // long begin = System.currentTimeMillis();
    //    // for(int i=0; i<1000; i++){
    //    // map = (Map<String, Object>) xml2mapWithAttr(doc.getRootElement());
    //    // }
    //    // System.out.println("耗时:"+(System.currentTimeMillis()-begin));
    //    JSON json = JSONObject.fromObject(map);
    //    System.out.println(json.toString(1)); // 格式化输出
    //
    //    Document doc = map2xml(map, "root");
    //    //Document doc = map2xml(map); //map中含有根节点的键
    //    System.out.println(formatXml(doc));
    //}
    //
    ///**
    // * xml转map 不带属性
    // *
    // * @param xmlStr
    // * @param needRootKey 是否需要在返回的map里加根节点键
    // * @return
    // * @throws DocumentException
    // */
    //public static Map xml2map(String xmlStr, boolean needRootKey) throws DocumentException {
    //    Document doc = DocumentHelper.parseText(xmlStr);
    //    Element root = doc.getRootElement();
    //    Map<String, Object> map = (Map<String, Object>) xml2map(root);
    //    if (root.elements().size() == 0 && root.attributes().size() == 0) {
    //        return map;
    //    }
    //    if (needRootKey) {
    //        //在返回的map里加根节点键（如果需要）
    //        Map<String, Object> rootMap = new HashMap<String, Object>();
    //        rootMap.put(root.getName(), map);
    //        return rootMap;
    //    }
    //    return map;
    //}
    //
    ///**
    // * xml转map 带属性
    // *
    // * @param xmlStr
    // * @param needRootKey 是否需要在返回的map里加根节点键
    // * @return
    // * @throws DocumentException
    // */
    //public static Map xml2mapWithAttr(String xmlStr, boolean needRootKey) throws DocumentException {
    //    Document doc = DocumentHelper.parseText(xmlStr);
    //    Element root = doc.getRootElement();
    //    Map<String, Object> map = (Map<String, Object>) xml2mapWithAttr(root);
    //    if (root.elements().size() == 0 && root.attributes().size() == 0) {
    //        return map; //根节点只有一个文本内容
    //    }
    //    if (needRootKey) {
    //        //在返回的map里加根节点键（如果需要）
    //        Map<String, Object> rootMap = new HashMap<String, Object>();
    //        rootMap.put(root.getName(), map);
    //        return rootMap;
    //    }
    //    return map;
    //}
    //
    ///**
    // * xml转map 不带属性
    // *
    // * @param e
    // * @return
    // */
    //private static Map xml2map(Element e) {
    //    Map map = new LinkedHashMap<>();
    //    List list = e.elements();
    //    if (list.size() > 0) {
    //        for (int i = 0; i < list.size(); i++) {
    //            Element iter = (Element) list.get(i);
    //            List mapList = new ArrayList<>();
    //
    //            if (iter.elements().size() > 0) {
    //                Map m = xml2map(iter);
    //                if (map.get(iter.getName()) != null) {
    //                    Object obj = map.get(iter.getName());
    //                    if (!(obj instanceof List)) {
    //                        mapList = new ArrayList<>();
    //                        mapList.add(obj);
    //                        mapList.add(m);
    //                    }
    //                    if (obj instanceof List) {
    //                        mapList = (List) obj;
    //                        mapList.add(m);
    //                    }
    //                    map.put(iter.getName(), mapList);
    //                } else
    //                    map.put(iter.getName(), m);
    //            } else {
    //                if (map.get(iter.getName()) != null) {
    //                    Object obj = map.get(iter.getName());
    //                    if (!(obj instanceof List)) {
    //                        mapList = new ArrayList();
    //                        mapList.add(obj);
    //                        mapList.add(iter.getText());
    //                    }
    //                    if (obj instanceof List) {
    //                        mapList = (List) obj;
    //                        mapList.add(iter.getText());
    //                    }
    //                    map.put(iter.getName(), mapList);
    //                } else
    //                    map.put(iter.getName(), iter.getText());
    //            }
    //        }
    //    } else
    //        map.put(e.getName(), e.getText());
    //    return map;
    //}
    //
    ///**
    // * xml转map 带属性
    // *
    // * @param e
    // * @return
    // */
    //private static Map xml2mapWithAttr(Element element) {
    //    Map<String, Object> map = new LinkedHashMap<String, Object>();
    //
    //    List<Element> list = element.elements();
    //    List<Attribute> listAttr0 = element.attributes(); // 当前节点的所有属性的list
    //    for (Attribute attr : listAttr0) {
    //        map.put("@" + attr.getName(), attr.getValue());
    //    }
    //    if (list.size() > 0) {
    //
    //        for (int i = 0; i < list.size(); i++) {
    //            Element iter = list.get(i);
    //            List mapList = new ArrayList<>();
    //
    //            if (iter.elements().size() > 0) {
    //                Map m = xml2mapWithAttr(iter);
    //                if (map.get(iter.getName()) != null) {
    //                    Object obj = map.get(iter.getName());
    //                    if (!(obj instanceof List)) {
    //                        mapList = new ArrayList<>();
    //                        mapList.add(obj);
    //                        mapList.add(m);
    //                    }
    //                    if (obj instanceof List) {
    //                        mapList = (List) obj;
    //                        mapList.add(m);
    //                    }
    //                    map.put(iter.getName(), mapList);
    //                } else
    //                    map.put(iter.getName(), m);
    //            } else {
    //
    //                List<Attribute> listAttr = iter.attributes(); // 当前节点的所有属性的list
    //                Map<String, Object> attrMap = null;
    //                boolean hasAttributes = false;
    //                if (listAttr.size() > 0) {
    //                    hasAttributes = true;
    //                    attrMap = new LinkedHashMap<String, Object>();
    //                    for (Attribute attr : listAttr) {
    //                        attrMap.put("@" + attr.getName(), attr.getValue());
    //                    }
    //                }
    //
    //                if (map.get(iter.getName()) != null) {
    //                    Object obj = map.get(iter.getName());
    //                    if (!(obj instanceof List)) {
    //                        mapList = new ArrayList<>();
    //                        mapList.add(obj);
    //                        // mapList.add(iter.getText());
    //                        if (hasAttributes) {
    //                            attrMap.put("#text", iter.getText());
    //                            mapList.add(attrMap);
    //                        } else {
    //                            mapList.add(iter.getText());
    //                        }
    //                    }
    //                    if (obj instanceof List) {
    //                        mapList = (List) obj;
    //                        // mapList.add(iter.getText());
    //                        if (hasAttributes) {
    //                            attrMap.put("#text", iter.getText());
    //                            mapList.add(attrMap);
    //                        } else {
    //                            mapList.add(iter.getText());
    //                        }
    //                    }
    //                    map.put(iter.getName(), mapList);
    //                } else {
    //                    // map.put(iter.getName(), iter.getText());
    //                    if (hasAttributes) {
    //                        attrMap.put("#text", iter.getText());
    //                        map.put(iter.getName(), attrMap);
    //                    } else {
    //                        map.put(iter.getName(), iter.getText());
    //                    }
    //                }
    //            }
    //        }
    //    } else {
    //        // 根节点的
    //        if (listAttr0.size() > 0) {
    //            map.put("#text", element.getText());
    //        } else {
    //            map.put(element.getName(), element.getText());
    //        }
    //    }
    //    return map;
    //}
    //
    ///**
    // * map转xml map中没有根节点的键
    // *
    // * @param map
    // * @param rootName
    // * @throws DocumentException
    // * @throws IOException
    // */
    //public static Document map2xml(Map<String, Object> map, String rootName) throws DocumentException, IOException {
    //    Document doc = DocumentHelper.createDocument();
    //    Element root = DocumentHelper.createElement(rootName);
    //    doc.add(root);
    //    map2xml(map, root);
    //    //System.out.println(doc.asXML());
    //    //System.out.println(formatXml(doc));
    //    return doc;
    //}
    //
    ///**
    // * map转xml map中含有根节点的键
    // *
    // * @param map
    // * @throws DocumentException
    // * @throws IOException
    // */
    //public static Document map2xml(Map<String, Object> map) throws DocumentException, IOException {
    //    Iterator<Map.Entry<String, Object>> entries = map.entrySet().iterator();
    //    if (entries.hasNext()) { //获取第一个键创建根节点
    //        Map.Entry<String, Object> entry = entries.next();
    //        Document doc = DocumentHelper.createDocument();
    //        Element root = DocumentHelper.createElement(entry.getKey());
    //        doc.add(root);
    //        map2xml((Map) entry.getValue(), root);
    //        //System.out.println(doc.asXML());
    //        //System.out.println(formatXml(doc));
    //        return doc;
    //    }
    //    return null;
    //}
    //
    ///**
    // * map转xml
    // *
    // * @param map
    // * @param body xml元素
    // * @return
    // */
    //private static Element map2xml(Map<String, Object> map, Element body) {
    //    Iterator<Map.Entry<String, Object>> entries = map.entrySet().iterator();
    //    while (entries.hasNext()) {
    //        Map.Entry<String, Object> entry = entries.next();
    //        String key = entry.getKey();
    //        Object value = entry.getValue();
    //        if (key.startsWith("@")) {    //属性
    //            body.addAttribute(key.substring(1, key.length()), value.toString());
    //        } else if (key.equals("#text")) { //有属性时的文本
    //            body.setText(value.toString());
    //        } else {
    //            if (value instanceof java.util.List) {
    //                List list = (List) value;
    //                Object obj;
    //                for (int i = 0; i < list.size(); i++) {
    //                    obj = list.get(i);
    //                    //list里是map或String，不会存在list里直接是list的，
    //                    if (obj instanceof java.util.Map) {
    //                        Element subElement = body.addElement(key);
    //                        map2xml((Map) list.get(i), subElement);
    //                    } else {
    //                        body.addElement(key).setText((String) list.get(i));
    //                    }
    //                }
    //            } else if (value instanceof java.util.Map) {
    //                Element subElement = body.addElement(key);
    //                map2xml((Map) value, subElement);
    //            } else {
    //                body.addElement(key).setText(value.toString());
    //            }
    //        }
    //        //System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue());
    //    }
    //    return body;
    //}
    //
    ///**
    // * 格式化输出xml
    // *
    // * @param xmlStr
    // * @return
    // * @throws DocumentException
    // * @throws IOException
    // */
    //public static String formatXml(String xmlStr) throws DocumentException, IOException {
    //    Document document = DocumentHelper.parseText(xmlStr);
    //    return formatXml(document);
    //}
    //
    ///**
    // * 格式化输出xml
    // *
    // * @param document
    // * @return
    // * @throws DocumentException
    // * @throws IOException
    // */
    //public static String formatXml(Document document) throws DocumentException, IOException {
    //    // 格式化输出格式
    //    OutputFormat format = OutputFormat.createPrettyPrint();
    //    //format.setEncoding("UTF-8");
    //    StringWriter writer = new StringWriter();
    //    // 格式化输出流
    //    XMLWriter xmlWriter = new XMLWriter(writer, format);
    //    // 将document写入到输出流
    //    xmlWriter.write(document);
    //    xmlWriter.close();
    //    return writer.toString();
    //}
    //
    //
    ///**
    // * xml字符串转换成Map
    // * 获取标签内属性值和text值
    // *
    // * @param xml
    // * @return
    // * @throws Exception
    // */
    //public static Map<String, String> xmlToMap(String xml) throws Exception {
    //    StringReader reader = new StringReader(xml);
    //    InputSource source = new InputSource(reader);
    //    SAXReader sax = new SAXReader(); // 创建一个SAXReader对象
    //    Document document = sax.read(source); // 获取document对象,如果文档无节点，则会抛出Exception提前结束
    //    Element root = document.getRootElement(); // 获取根节点
    //    Map<String, String> map = XmlUtil.getNodes(root); // 从根节点开始遍历所有节点
    //    return map;
    //}
    //
    ///**
    // * 从指定节点开始,递归遍历所有子节点
    // *
    // */
    //public static Map<String, String> getNodes(Element node) {
    //    xmlMap.put(node.getName().toLowerCase(), node.getTextTrim());
    //    List<Attribute> listAttr = node.attributes(); // 当前节点的所有属性的list
    //    for (Attribute attr : listAttr) { // 遍历当前节点的所有属性
    //        String name = attr.getName(); // 属性名称
    //        String value = attr.getValue(); // 属性的值
    //        xmlMap.put(name, value.trim());
    //    }
    //
    //    // 递归遍历当前节点所有的子节点
    //    List<Element> listElement = node.elements(); // 所有一级子节点的list
    //    for (Element e : listElement) { // 遍历所有一级子节点
    //        XmlUtil.getNodes(e); // 递归
    //    }
    //    return xmlMap;
    //}
    //
    //
    //private static void recursionXmlToMap(Element element, Map<String, Object> outmap) {
    //    // 得到根元素下的子元素列表
    //    List<Element> list = element.elements();
    //    int size = list.size();
    //    if (size == 0) {
    //        // 如果没有子元素,则将其存储进map中
    //        outmap.put(element.getName(), element.getTextTrim());
    //    } else {
    //        // innermap用于存储子元素的属性名和属性值
    //        Map<String, Object> innermap = new HashMap<>();
    //        // 遍历子元素
    //        list.forEach(childElement -> recursionXmlToMap(childElement, innermap));
    //        outmap.put(element.getName(), innermap);
    //    }
    //}


    /**
     * XML转Map
     *
     * @param xmlStr
     * @return
     * @throws JDOMException
     * @throws IOException
     */
    public static Map<String, Object> xml2Map(String xmlStr) throws JDOMException, IOException {
        if (!StringUtils.hasText(xmlStr)) {
            return null;
        }
        xmlStr = xmlStr.replaceAll("\\n", "");
        xmlStr = xmlStr.replaceAll("&#02;", "#");

        byte[] xml = xmlStr.getBytes(StandardCharsets.UTF_8);
        Map<String, Object> json = new HashMap<>();
        try (InputStream is = new ByteArrayInputStream(xml);) {
            SAXBuilder sb = new SAXBuilder();
            Document doc = sb.build(is);
            Element root = doc.getRootElement();
            json.put(root.getName(), iterateElement(root));
        }
        return json;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> iterateElement(Element element) {
        List<Element> node = element.getChildren();
        Map<String, Object> obj = new HashMap<>();
        List<Object> list = null;
        for (Element child : node) {
            list = new LinkedList<>();
            String text = child.getTextTrim();
            if (!StringUtils.hasText(text)) {
                if (child.getChildren().size() == 0) {
                    continue;
                }
                if (obj.containsKey(child.getName())) {
                    list = (List<Object>) obj.get(child.getName());
                }
                list.add(iterateElement(child)); // 遍历child的子节点
                obj.put(child.getName(), list);
            } else {
                if (obj.containsKey(child.getName())) {
                    Object value = obj.get(child.getName());
                    try {
                        list = (List<Object>) value;
                    } catch (ClassCastException e) {
                        list.add(value);
                    }
                }
                if (child.getChildren().size() == 0) { // child无子节点时直接设置text
                    obj.put(child.getName(), text);
                } else {
                    list.add(text);
                    obj.put(child.getName(), list);
                }
            }
        }
        return obj;
    }

    /**
     * 对象转XML
     *
     * @param obj
     * @return
     * @throws JAXBException
     * @throws IOException
     */
    public static <T> String bean2Xml(Object obj) throws JAXBException, IOException {
        //import javax.xml.bind.JAXBContext;
        //import javax.xml.bind.JAXBException;
        //import javax.xml.bind.Marshaller;
        //import javax.xml.bind.Unmarshaller;
        // 实参中包含需要解析的类
        JAXBContext jaxbContext = JAXBContext.newInstance(obj.getClass());
        // javaBean序列化xml文件器
        Marshaller marshaller = jaxbContext.createMarshaller();
        // 序列化后的xml是否需要格式化输出
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        // 取消这个标签的显示<?xml version="1.0" encoding="utf-8" standalone="yes"?>
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        // 编码格式
        marshaller.setProperty(Marshaller.JAXB_ENCODING, StandardCharsets.UTF_8);
        // 序列化
        try (StringWriter sw = new StringWriter();) {
            marshaller.marshal(obj, sw);
            return sw.toString();
        }
    }

    /**
     * XML反序列化为对象
     *
     * @param <T>
     * @param xml
     * @param clazz
     * @return
     * @throws JAXBException
     * @throws UnsupportedEncodingException
     * @throws IOException
     * @throws FactoryConfigurationError
     * @throws XMLStreamException
     */
    public static <T> T xml2Bean(String xml, Class<T> clazz) throws JAXBException, UnsupportedEncodingException,
            IOException, FactoryConfigurationError, XMLStreamException {
        //import javax.xml.bind.JAXBContext;
        //import javax.xml.bind.JAXBException;
        //import javax.xml.bind.Marshaller;
        //import javax.xml.bind.Unmarshaller;
        // 实参中包含需要解析的类
        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
        // xml文件解析成JavaBean对象器
        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        try (InputStream is = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8))) {
            XMLStreamReader xmlReader = XMLInputFactory.newFactory().createXMLStreamReader(is);
            // 序列化
            //return unmarshaller.unmarshal(new ToLowerCaseNamesStreamReaderDelegate(xmlReader), clazz).getValue();
            return unmarshaller.unmarshal(xmlReader, clazz).getValue();
        }
        /*try (StringReader reader = new StringReader(xml);) {
            // 序列化
            return unmarshaller.unmarshal(reader);
        }*/
    }

    /**
     * 自定义解析XML处理
     */
    private static class ToLowerCaseNamesStreamReaderDelegate extends StreamReaderDelegate {

        public ToLowerCaseNamesStreamReaderDelegate(XMLStreamReader xsr) {
            super(xsr);
        }

        @Override
        public QName getAttributeName(int index) {
            return super.getAttributeName(index);
        }

        @Override
        public String getAttributeNamespace(int index) {
            return super.getAttributeNamespace(index);
        }

        @Override
        public String getAttributeLocalName(int index) { // 获取标签元素上的属性名称
            return super.getAttributeLocalName(index);
        }

        @Override
        public String getNamespacePrefix(int index) {
            return super.getNamespacePrefix(index);
        }

        @Override
        public QName getName() {
            return super.getName();
        }

        @Override
        public String getLocalName() { // 获取所有标签元素名称
            return super.getLocalName().toLowerCase();
        }

        @Override
        public Object getProperty(String name) {
            return super.getProperty(name);
        }
    }


}
