// 由于请求和响应都使用了http://tempuri.org/作为命名空间，我们可以在包级别定义这个命名空间，这样就不必在每个JAXB注解上重复。
@XmlSchema(
        namespace = "http://tempuri.org/", // 定义包的默认命名空间
        xmlns = {
                @XmlNs(prefix = "", namespaceURI = "http://tempuri.org/")
        },
        elementFormDefault = XmlNsForm.QUALIFIED // 确保元素带命名空间前缀
)
package com.bajins.demo.soapws;

