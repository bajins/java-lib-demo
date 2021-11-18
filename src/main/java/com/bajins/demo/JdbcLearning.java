package com.bajins.demo;

import java.sql.SQLException;

public class JdbcLearning {
    public static void main(String[] args) {

    }

    /**
     * 获取执行sql
     * https://my.oschina.net/ouminzy/blog/4952167
     * https://blog.csdn.net/lqzkcx3/article/details/80367097
     * https://blog.csdn.net/lqzkcx3/article/details/79259117
     * https://www.cnblogs.com/wggj/p/12762648.html
     *
     * @param ps PreparedStatement
     * @return
     * @throws SQLException
     * @date: 2021-11-18 11:54:36
     */
    /*public static String getSql(PreparedStatement ps) throws SQLException {
        if (ps instanceof DruidPooledPreparedStatement) {
            DruidPooledPreparedStatement dpps = (DruidPooledPreparedStatement) ps;
            PreparedStatementHolder psh = dpps.getPreparedStatementHolder();
            // return psh.key.getSql();

            PreparedStatementProxyImpl psp = (PreparedStatementProxyImpl) psh.statement;
            // System.out.println(psp.getBatchSqlList());
            // System.out.println(psp.getLastExecuteSql());
            // System.out.println(JSON.toJSONString(psp.getParameters()));
            // return psp.getSql();
            return psp.getBatchSql();
        }
        String driveType = ps.getConnection().getMetaData().getDatabaseProductName().toUpperCase();
        switch (driveType) {
            case "ORACLE":
                oracle.jdbc.internal.OraclePreparedStatement ops = (oracle.jdbc.internal.OraclePreparedStatement) ps;
                return ops.getOriginalSql();
            case "MYSQL":
                String sql = ps.toString();
                return sql.substring(sql.indexOf(":") + 1);
            case "POSTGRESQL":
                return ps.toString();
        }
        return ps.toString();
    }*/
}
