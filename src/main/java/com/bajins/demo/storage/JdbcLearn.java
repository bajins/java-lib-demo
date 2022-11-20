package com.bajins.demo.storage;

import org.apache.commons.lang3.ArrayUtils;

import java.sql.Blob;

public class JdbcLearn {

    // https://docs.oracle.com/en/database/oracle/oracle-database/21/jajdb/oracle/sql/BLOB.html
    /*public static Blob bytes2Blob(byte[] b) throws Exception {
        if (System.getProperty("oracle.server.version") != null) {
            Connection con = DriverManager.getConnection("jdbc:default:connection");
            CallableStatement cStmt = con.prepareCall("{ call DBMS_LOB.createtemporary(?,true,DBMS_LOB.SESSION) }");
            cStmt.registerOutParameter(1, OracleTypes.BLOB);
            cStmt.execute();
            Blob blob = ((OracleCallableStatement) cStmt).getBLOB(1);
            cStmt.close();
            OutputStream out = blob.setBinaryStream(1L);
            out.write(b);
            out.flush();
            return blob;
        } else {
            return new javax.sql.rowset.serial.SerialBlob(b);
        }
    }*/

    private String getTextFromBlob(Blob blob) {
        int i = 1;
        byte[] btArr = new byte[0];
        try {
            while (i < blob.length()) {
                byte[] bytes = blob.getBytes(i, 1024);
                btArr = ArrayUtils.addAll(btArr, bytes);
                i += 1024;
            }
            return new String(btArr, "GB2312");
        } catch (Exception e) {
            return null;
        }
    }

    public static void main(String[] args) {
        /*conn = new OracleDriver().defaultConnection();
        Blob desBlob = BLOB.createTemporary(conn, false, BLOB.DURATION_SESSION);*/
    }
}
