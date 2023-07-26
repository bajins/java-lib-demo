package com.bajins.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import io.fury.Fury;
import io.fury.Language;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.values.ValuesStatement;

import javax.swing.filechooser.FileSystemView;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 查找数据库中所有表并生成插入sql
 *
 * @version V1.0
 * @author: dingdehang
 * @Title: JdbcGenInsertSql.java
 * @Package com.pangus.ims
 * @Description:
 * @date: 2023-7-11 14:44:46
 * @Copyright: 2023 pangus.com Inc. All rights reserved.
 */
public class JdbcGenInsertSql {

    private static Connection getConnection() throws SQLException {
        Connection connection2 = getConnection2();
        connection2.setSchema("test1");
        return connection2;
    }

    private static Connection getConnection2() throws SQLException {
        return DriverManager.getConnection("jdbc:oracle:thin:@//192.168.100.110:1521/test", "test", "test");
    }

    public static void main(String[] args) {
        /*try {
            dbToSerialize(getConnection(), 1, "ser.bin");
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }*/
        try {
            deserializeToDb(getConnection2(), 1, new File("ser.bin"));
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
        /*try {
            dbToDb(getConnection(), getConnection2());
        } catch (SQLException e) {
            e.printStackTrace();
        }*/
    }

    /**
     * 序列化数据库数据到文件中
     *
     * @param connection 读取数据库连接信息
     * @param toMethod   序列化方式：1kryo、2fury、3jdk、4json、5xml
     * @param filename   123是二进制文件：ser.bin；4可以为ser.json;5可以为ser.xml。最后会输出到桌面
     * @throws SQLException
     * @throws IOException
     * @throws FileNotFoundException
     * @date: 2023-7-21 10:17:34
     */
    public static void dbToSerialize(Connection connection, int toMethod, String filename)
            throws SQLException, FileNotFoundException, IOException {
        // ORA-00001: 违反唯一约束条件
        // ORA-00942: 表或视图不存在
        Pattern pattern = Pattern.compile(".*(ORA-00001|ORA-00942).*", Pattern.CASE_INSENSITIVE);
        // 获取所有表信息
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getTables(null, connection.getSchema(), "%", new String[]{"TABLE"});
        List<String> tableNameList = new ArrayList<>();
        while (resultSet.next()) {
            String tableName = resultSet.getString("TABLE_NAME");
            String tableType = resultSet.getString("TABLE_TYPE");
            if (tableType.equals("TABLE")) {
                tableNameList.add(tableName);
            }
        }
        resultSet.close(); // 资源使用完成必须关闭

        Map<String, List<Map<String, Object>>> serializMap = new HashMap<>(); // 序列化到文件数据

        // 遍历表生成插入sql
        for (String tableName : tableNameList) {
            ResultSet colResultSet = metaData.getColumns(null, connection.getSchema(), tableName, "TEST");
            if (!colResultSet.next()) { // 判断字段是否存在
                continue;
            }
            // System.out.println(tableName);
            colResultSet.close(); // 资源使用完成必须关闭

            /*while (colResultSet.next()) {
                String colName = colResultSet.getString("COLUMN_NAME");
                String colType = colResultSet.getString("TYPE_NAME");
            }*/
            // 查询数据
            PreparedStatement pstmt = connection.prepareStatement("select * from " + tableName);
            ResultSet dataResultSet = null;
            try {
                dataResultSet = pstmt.executeQuery();
            } catch (Exception e) {
                if (!pattern.matcher(e.getMessage()).find()) {
                    e.printStackTrace();
                }
                pstmt.close(); // 资源使用完成必须关闭
                continue;
            }
            while (dataResultSet.next()) {
                try {
                    List<Column> columns = new ArrayList<>();
                    List<Expression> values1 = new ArrayList<>(); // 插入数据的?占位符
                    List<Map<String, Object>> values_ = new ArrayList<>(); // 待插入的数据

                    ResultSetMetaData rsmd = dataResultSet.getMetaData(); // 获取元数据
                    int count = rsmd.getColumnCount();
                    for (int j = 0; j < count; j++) {
                        String columnName = rsmd.getColumnLabel(j + 1);
                        String columnType = rsmd.getColumnTypeName(j + 1);

                        Object value = dataResultSet.getObject(columnName);
                        if (dataResultSet.wasNull() || value == null) { // 如果数字数据库是null会返回0，wasNull判断会返回true
                            // values.add(new NullValue());
                            continue;
                        }
                        columns.add(new Column(columnName));

                        values1.add(new StringValue(" ? "));

                        Map<String, Object> valMap = new HashMap<>();

                        String lowColType = columnType.toLowerCase();
                        valMap.put("type", lowColType);
                        if (columnName.equals("test")) {
                            valMap.put("value", "test");
                            continue;
                        }
                        if (lowColType.equals("blob")) {
                            Blob blob = dataResultSet.getBlob(columnName);
                            /*try (InputStream is = blob.getBinaryStream();
                                BufferedInputStream bis = new BufferedInputStream(is);) {
                                byte[] bytes = new byte[(int) blob.length()]; // is.available()
                                int len = bytes.length;
                                int offset = 0;
                                int read = 0;
                                while (offset < len && (read = bis.read(bytes, offset, len - offset)) >= 0) {
                                    offset += read;
                                }
                                String bs = new String(bytes));
                            }*/
                            String bs = new String(blob.getBytes(1, (int) blob.length()), StandardCharsets.UTF_8);
                            // 转16进制
                            // String hex = Hex.encodeHexString(blob.getBytes(1, (int) blob.length())).toUpperCase();
                            // String hex = DatatypeConverter.printHexBinary(blob.getBytes(1, (int) blob.length()));
                            valMap.put("value", bs);
                        } else if (lowColType.equals("binary") || lowColType.equals("bytea")) {
                            try (InputStream is = dataResultSet.getBinaryStream(columnName);) {
                                byte[] bytes = is.readAllBytes();
                                String bs = new String(bytes, StandardCharsets.UTF_8);
                                valMap.put("value", bs);
                            }
                        } else if (lowColType.equals("clob")) {
                            Clob clob = dataResultSet.getClob(columnName);
                            valMap.put("value", clob.getSubString(1L, (int) clob.length()));
                        } else if (lowColType.equals("nclob")) {
                            NClob nclob = dataResultSet.getNClob(columnName);
                            valMap.put("value", nclob.getSubString(1L, (int) nclob.length()));
                        } else if (lowColType.equals("date") || lowColType.equals("datetime")
                                || lowColType.contains("timestamp")) {
                            valMap.put("value", dataResultSet.getTimestamp(columnName));
                        } else if (lowColType.equals("time")) {
                            valMap.put("value", dataResultSet.getTime(columnName));
                        } else if (lowColType.equals("number") || lowColType.equals("numeric")) {
                            BigDecimal number = dataResultSet.getBigDecimal(columnName);
                            valMap.put("value", number);
                        } else { // varchar
                            valMap.put("value", value);
                        }
                        values_.add(valMap);
                    }
                    Table table = new Table(tableName);

                    ExpressionList expressionList = new ExpressionList(values1);
                    // https://github.com/JSQLParser/JSqlParser/issues/1802
                    ValuesStatement valuesStatement = new ValuesStatement().withExpressions(expressionList);
                    Select select = new Select().withSelectBody(valuesStatement);

                    Insert insert = new Insert().withTable(table).withColumns(columns).withSelect(select);

                    String sql = insert.toString().replaceAll("\\(\\)|'", "");
                    // System.out.println(sql);
                    serializMap.put(sql, values_);
                } catch (Exception e) {
                    if (!pattern.matcher(e.getMessage()).find()) {
                        e.printStackTrace();
                    }
                }
            }
            dataResultSet.close(); // 资源使用完成必须关闭
            pstmt.clearBatch(); // 清空容器中的SQL脚本
            pstmt.clearParameters(); // 清空参数
            pstmt.close(); // 关闭游标
        }
        resultSet.close(); // 资源使用完成必须关闭
        connection.close(); // 关闭连接

        File homeDirectory = FileSystemView.getFileSystemView().getHomeDirectory();
        File file = Paths.get(homeDirectory.getAbsolutePath(), filename).toFile();
        // 序列化为二进制
        if (toMethod == 1) {
            try (FileOutputStream fos = new FileOutputStream(file); Output output = new Output(fos);) {
                // MapSerializer<Map<String, List<Map<String, Object>>>> mapSerializer = new MapSerializer<>();
                Kryo kryo = new Kryo();
                // kryo.register(HashMap.class, mapSerializer);
                kryo.register(HashMap.class);
                kryo.register(ArrayList.class);
                kryo.register(BigDecimal.class);
                kryo.register(Date.class);
                kryo.register(Time.class);
                kryo.register(Timestamp.class);
                kryo.register(Boolean.class);
                kryo.register(Byte.class);
                kryo.register(Short.class);
                kryo.register(Float.class);
                kryo.register(Double.class);
                kryo.register(Integer.class);
                kryo.register(Long.class);
                kryo.register(String.class);
                kryo.setReferences(false);
                kryo.setRegistrationRequired(true);
                // kryo.setWarnUnregisteredClasses(true);
                // kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
                // mapSerializer.setKeyClass(String.class, kryo.getSerializer(String.class));
                kryo.writeObject(output, serializMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (toMethod == 2) {
            Fury fury = Fury.builder().withLanguage(Language.JAVA).withRefTracking(false).withSecureMode(false).build();
            // fury.register(String.class);
            byte[] serialize = fury.serialize(serializMap);
            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bis = new BufferedOutputStream(fos);) {
                bis.write(serialize);
            }
        } else if (toMethod == 3) {
            try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                 // ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                 ObjectOutputStream oos = new ObjectOutputStream(fileOutputStream)) {
                oos.writeObject(serializMap);
                oos.flush();
            }
        } else if (toMethod == 4) {
            try (FileOutputStream fos = new FileOutputStream(file);
                 OutputStreamWriter bis = new OutputStreamWriter(fos);) {
                // FileWriter fileWriter = new FileWriter(file);
                bis.write(JSON.toJSONString(serializMap));
            }
        } else if (toMethod == 5) {
            try (FileOutputStream fos = new FileOutputStream(file);
                 BufferedOutputStream bis = new BufferedOutputStream(fos);
                 XMLEncoder xe = new XMLEncoder(bis);) {
                xe.writeObject(serializMap);
                /*JAXBContext newInstance = JAXBContext.newInstance(HashMap.class);
                Marshaller marshaller = newInstance.createMarshaller();
                marshaller.marshal(serializMap, file);*/
            }
        }
    }

    /**
     * 反序列化文件数据写入到数据库
     *
     * @param connection 写入数据库连接信息
     * @param toMethod   反序列化方式：1kryo、2fury、3jdk、4json、5xml
     * @param file       数据文件
     * @throws IOException
     * @throws FileNotFoundException
     * @throws SQLException
     * @date: 2023-7-21 10:49:20
     */
    @SuppressWarnings("unchecked")
    private static void deserializeToDb(Connection connection, int toMethod, File file)
            throws FileNotFoundException, IOException, SQLException {
        // 读取文件内容
        Map<String, List<Map<String, Object>>> dataMap = null;
        // 反序列化
        if (toMethod == 1) {
            try (FileInputStream fis = new FileInputStream(file); Input input = new Input(fis);) {
                // MapSerializer<Map<String, List<Map<String, Object>>>> mapSerializer = new MapSerializer<>();
                Kryo kryo = new Kryo();
                // kryo.register(HashMap.class, mapSerializer);
                kryo.register(HashMap.class);
                kryo.register(ArrayList.class);
                kryo.register(BigDecimal.class);
                kryo.register(Date.class);
                kryo.register(Time.class);
                kryo.register(Timestamp.class);
                kryo.register(Boolean.class);
                kryo.register(Byte.class);
                kryo.register(Short.class);
                kryo.register(Float.class);
                kryo.register(Double.class);
                kryo.register(Integer.class);
                kryo.register(Long.class);
                kryo.register(String.class);
                kryo.setReferences(false);
                kryo.setRegistrationRequired(true);
                // kryo.setWarnUnregisteredClasses(true);
                // kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
                // mapSerializer.setKeyClass(String.class, kryo.getSerializer(String.class));
                dataMap = (Map<String, List<Map<String, Object>>>) kryo.readObject(input, HashMap.class);
            }
        } else if (toMethod == 2) {
            Fury fury = Fury.builder().withLanguage(Language.JAVA).withRefTracking(false).withSecureMode(false).build();
            // fury.register(String.class);
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis);) {
                byte[] bytes = bis.readAllBytes();
                dataMap = (Map<String, List<Map<String, Object>>>) fury.deserialize(bytes);
            }
        } else if (toMethod == 3) {
            try (FileInputStream fis = new FileInputStream(file); ObjectInputStream ois = new ObjectInputStream(fis)) {
                dataMap = (Map<String, List<Map<String, Object>>>) ois.readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else if (toMethod == 4) {
            try (FileReader fileReader = new FileReader(file); BufferedReader br = new BufferedReader(fileReader);) {
                String json = br.lines().collect(Collectors.joining());
                TypeReference<Map<String, List<Map<String, Object>>>> typeReference = new TypeReference<Map<String,
                        List<Map<String, Object>>>>() {
                };
                dataMap = JSON.parseObject(json, typeReference);
            }
        } else if (toMethod == 5) {
            try (FileInputStream fis = new FileInputStream(file);
                 BufferedInputStream bis = new BufferedInputStream(fis);
                 XMLDecoder xd = new XMLDecoder(bis);) {
                dataMap = (Map<String, List<Map<String, Object>>>) xd.readObject();
                /*JAXBContext newInstance = JAXBContext.newInstance(HashMap.class);
                Marshaller marshaller = newInstance.createMarshaller();
                marshaller.marshal(serializMap, file);*/
            }
        }
        if (dataMap != null) {
            // ORA-00001: 违反唯一约束条件
            // ORA-00942: 表或视图不存在
            Pattern pattern = Pattern.compile(".*(ORA-00001|ORA-00942).*", Pattern.CASE_INSENSITIVE);

            for (Entry<String, List<Map<String, Object>>> entry : dataMap.entrySet()) {
                String sql = entry.getKey();
                List<Map<String, Object>> values = entry.getValue();
                // 开始执行插入数据
                PreparedStatement pst = connection.prepareStatement(sql); // 打开一个游标

                for (int j = 0; j < values.size(); j++) {
                    Map<String, Object> map = values.get(j);

                    String type = (String) map.get("type");
                    Object value = map.get("value");

                    if (type.equals("clob")) {
                        Clob clob = connection.createClob();
                        clob.setString(1, (String) value);
                        pst.setClob(j + 1, clob);
                        // pst.setClob(j + 1, Reader);
                        /*try (ByteArrayInputStream bis = new ByteArrayInputStream(((String) value).getBytes());
                                InputStreamReader isr = new InputStreamReader(bis);) {
                            pst.setCharacterStream(j + 1, isr);
                            // pst.setNCharacterStream(j + 1, Reader);
                        }*/
                    } else if (type.equals("nclob")) {
                        NClob nClob = connection.createNClob();
                        nClob.setString(1, (String) value);
                        pst.setNClob(j + 1, nClob);
                        // pst.setNClob(j + 1, Reader);
                    } else if (type.equals("blob")) {
                        Blob blob = connection.createBlob();
                        blob.setBytes(1, ((String) value).getBytes(StandardCharsets.UTF_8));
                        pst.setBlob(j + 1, blob);
                        // pst.setBlob(j + 1, InputStream);
                    } else if (type.equals("binary") || type.equals("bytea")) {
                        byte[] bytes = ((String) value).getBytes(StandardCharsets.UTF_8);
                        pst.setBinaryStream(j + 1, new ByteArrayInputStream(bytes));
                    } else if (type.equals("bigdecimal") || type.equals("number") || type.equals("numeric")) {
                        pst.setBigDecimal(j + 1, (BigDecimal) value);
                    } else if (type.equals("array")) {
                        pst.setArray(j + 1, (Array) value);
                    } else if (type.equals("date")) {
                        pst.setDate(j + 1, (Date) value);
                    } else if (type.equals("time")) {
                        pst.setTime(j + 1, (Time) value);
                    } else if (type.equals("timestamp")) {
                        pst.setTimestamp(j + 1, (Timestamp) value);
                    } else if (type.equals("boolean")) {
                        pst.setBoolean(j + 1, (Boolean) value);
                    } else if (type.equals("byte")) {
                        pst.setByte(j + 1, (Byte) value);
                    } else if (type.equals("byte[]")) {
                        pst.setBytes(j + 1, (byte[]) value);
                    } else if (type.equals("short")) {
                        pst.setShort(j + 1, (Short) value);
                    } else if (type.equals("float")) {
                        pst.setFloat(j + 1, (Float) value);
                    } else if (type.equals("double")) {
                        pst.setDouble(j + 1, (Double) value);
                    } else if (type.equals("integer")) {
                        pst.setInt(j + 1, (Integer) value);
                    } else if (type.equals("long")) {
                        pst.setLong(j + 1, (Long) value);
                    } else if (type.equals("string")) {
                        pst.setString(j + 1, (String) value);
                    } else {
                        pst.setObject(j + 1, value);
                        // pst.setObject(j + 1, value, Types.VARCHAR); // java.sql.Types java.sql.JDBCType
                    }
                    // pst.setAsciiStream(j + 1, InputStream);
                    // pst.setNString(j + 1, String);
                    // pst.setNull(j + 1, int);
                    // pst.setRef(j + 1, Ref);
                    // pst.setRowId(j + 1, RowId);
                    // pst.setSQLXML(j + 1, SQLXML);
                    // pst.setUnicodeStream(j + 1, InputStream, int);
                    // pst.setURL(j + 1, URL);
                }
                try {
                    // pst.addBatch(); // 向容器中添加SQL脚本
                    // int[] rows = pst.executeBatch(); // 执行容器中的SQL脚本
                    // System.out.println(rows.length);
                    // boolean execute = pst.execute();
                    // System.err.println(execute);
                    int row = pst.executeUpdate();
                    System.err.println(row);
                    // connection2.commit();
                } catch (Exception e) {
                    if (!pattern.matcher(e.getMessage()).find()) {
                        e.printStackTrace();
                    }
                } finally {
                    pst.clearBatch(); // 清空容器中的SQL脚本
                    pst.clearParameters(); // 清空参数
                    pst.close(); // 关闭游标
                }
            }
        }
        connection.close(); // 关闭连接
    }

    /**
     * 直接从数据库读取数据转储到另一个数据库
     *
     * @param connection  读取数据库连接信息
     * @param connection2 写入数据库连接信息
     * @throws SQLException
     * @date: 2023-7-21 10:33:39
     */
    public static void dbToDb(Connection connection, Connection connection2) throws SQLException {
        // ORA-00001: 违反唯一约束条件
        // ORA-00942: 表或视图不存在
        Pattern pattern = Pattern.compile(".*(ORA-00001|ORA-00942).*", Pattern.CASE_INSENSITIVE);
        // 获取所有表信息
        DatabaseMetaData metaData = connection.getMetaData();
        ResultSet resultSet = metaData.getTables(null, connection.getSchema(), "%", new String[]{"TABLE"});
        List<String> tableNameList = new ArrayList<>();
        while (resultSet.next()) {
            String tableName = resultSet.getString("TABLE_NAME");
            String tableType = resultSet.getString("TABLE_TYPE");
            if (tableType.equals("TABLE")) {
                tableNameList.add(tableName);
            }
        }
        resultSet.close(); // 资源使用完成必须关闭

        // 遍历表生成插入sql
        for (String tableName : tableNameList) {
            ResultSet colResultSet = metaData.getColumns(null, connection.getSchema(), tableName, "TEST");
            if (!colResultSet.next()) { // 判断字段是否存在
                continue;
            }
            System.out.println(tableName);
            colResultSet.close(); // 资源使用完成必须关闭

            /*while (colResultSet.next()) {
                String colName = colResultSet.getString("COLUMN_NAME");
                String colType = colResultSet.getString("TYPE_NAME");
            }*/
            // 查询数据
            PreparedStatement pstmt = connection
                    .prepareStatement("select * from " + tableName);
            ResultSet dataResultSet = pstmt.executeQuery();

            while (dataResultSet.next()) {
                try {
                    List<Column> columns = new ArrayList<>();
                    List<Object> values = new ArrayList<>();
                    List<Expression> values1 = new ArrayList<>(); // 插入数据的?占位符

                    ResultSetMetaData rsmd = dataResultSet.getMetaData(); // 获取元数据
                    int count = rsmd.getColumnCount();
                    for (int j = 0; j < count; j++) {
                        String columnName = rsmd.getColumnLabel(j + 1);
                        // String columnType = rsmd.getColumnTypeName(j + 1);

                        Object value = dataResultSet.getObject(columnName);
                        if (dataResultSet.wasNull() || value == null) { // 如果数字数据库是null会返回0，wasNull判断会返回true
                            // values.add(new NullValue());
                            continue;
                        }
                        columns.add(new Column(columnName));

                        values1.add(new StringValue(" ? "));

                        if (columnName.equals("test")) {
                            values.add("test");
                            continue;
                        }
                        values.add(value);
                    }
                    Table table = new Table(tableName);

                    ExpressionList expressionList = new ExpressionList(values1);
                    ValuesStatement valuesStatement = new ValuesStatement().withExpressions(expressionList);
                    Select select = new Select().withSelectBody(valuesStatement);

                    Insert insert = new Insert().withTable(table).withColumns(columns).withSelect(select);

                    String sql = insert.toString().replaceAll("\\(\\)|'", "");
                    // System.out.println(sql);

                    // 开始执行插入数据
                    PreparedStatement pst = connection2.prepareStatement(sql); // 打开一个游标
                    for (int j = 0; j < values.size(); j++) {
                        Object obj = values.get(j);
                        if (obj instanceof Clob) {
                            pst.setClob(j + 1, (Clob) obj);
                            // pst.setClob(j + 1, Reader);
                        } else if (obj instanceof NClob) {
                            pst.setNClob(j + 1, (NClob) obj);
                            // pst.setNClob(j + 1, Reader);
                        } else if (obj instanceof Blob) {
                            pst.setBlob(j + 1, (Blob) obj);
                            // pst.setBlob(j + 1, InputStream);
                        } else if (obj instanceof BigDecimal) {
                            pst.setBigDecimal(j + 1, (BigDecimal) obj);
                        } else if (obj instanceof Array) {
                            pst.setArray(j + 1, (Array) obj);
                        } else if (obj instanceof Date) {
                            pst.setDate(j + 1, (Date) obj);
                        } else if (obj instanceof Time) {
                            pst.setTime(j + 1, (Time) obj);
                        } else if (obj instanceof Timestamp) {
                            pst.setTimestamp(j + 1, (Timestamp) obj);
                        } else if (obj instanceof Boolean) {
                            pst.setBoolean(j + 1, (Boolean) obj);
                        } else if (obj instanceof Byte) {
                            pst.setByte(j + 1, (Byte) obj);
                        } else if (obj instanceof byte[]) {
                            pst.setBytes(j + 1, (byte[]) obj);
                        } else if (obj instanceof Short) {
                            pst.setShort(j + 1, (Short) obj);
                        } else if (obj instanceof Float) {
                            pst.setFloat(j + 1, (Float) obj);
                        } else if (obj instanceof Double) {
                            pst.setDouble(j + 1, (Double) obj);
                        } else if (obj instanceof Integer) {
                            pst.setInt(j + 1, (Integer) obj);
                        } else if (obj instanceof Long) {
                            pst.setLong(j + 1, (Long) obj);
                        } else if (obj instanceof String) {
                            pst.setString(j + 1, (String) obj);
                        } else {
                            pst.setObject(j + 1, obj);
                            // pst.setObject(j + 1, obj, SQLType); // java.sql.Types java.sql.JDBCType
                        }
                        // pst.setAsciiStream(j + 1, InputStream);
                        // pst.setBinaryStream(j + 1, InputStream);
                        // pst.setCharacterStream(j + 1, Reader);
                        // pst.setNCharacterStream(j + 1, Reader);
                        // pst.setNString(j + 1, String);
                        // pst.setNull(j + 1, int);
                        // pst.setRef(j + 1, Ref);
                        // pst.setRowId(j + 1, RowId);
                        // pst.setSQLXML(j + 1, SQLXML);
                        // pst.setUnicodeStream(j + 1, InputStream, int);
                        // pst.setURL(j + 1, URL);
                    }
                    try {
                        // pst.addBatch(); // 向容器中添加SQL脚本
                        // int[] rows = pst.executeBatch(); // 执行容器中的SQL脚本
                        // System.out.println(rows.length);
                        // boolean execute = pst.execute();
                        // System.err.println(execute);
                        int row = pst.executeUpdate();
                        System.err.println(row);
                        // connection2.commit();
                    } catch (Exception e) {
                        if (!pattern.matcher(e.getMessage()).find()) {
                            e.printStackTrace();
                        }
                    } finally {
                        pst.clearBatch(); // 清空容器中的SQL脚本
                        pst.clearParameters(); // 清空参数
                        pst.close(); // 关闭游标
                    }
                } catch (Exception e) {
                    if (!pattern.matcher(e.getMessage()).find()) {
                        e.printStackTrace();
                    }
                }
            }
            dataResultSet.close(); // 资源使用完成必须关闭
            pstmt.clearBatch(); // 清空容器中的SQL脚本
            pstmt.clearParameters(); // 清空参数
            pstmt.close(); // 关闭游标
        }
        resultSet.close(); // 资源使用完成必须关闭
        connection.close(); // 关闭连接
        connection2.close(); // 关闭连接
    }
}
