package com.bajins.demo;

public class SqlUtilsLearning {

    public static void main(String[] args) {
        // https://github.com/499636235/Druid-SqlParser/blob/main/src/Demo/Demo.java
        // 解析sql，生成 AST(抽象语法树)
        /*List<SQLStatement> stmtList = SQLUtils.parseStatements(sql, dbType);
        if (CollectionUtils.isEmpty(stmtList)) {
            System.out.printf("stmtList为空");
        }
        for (SQLStatement sqlStatement : stmtList) {
            // 使用 visitor 访问 AST(抽象语法树)
            OracleSchemaStatVisitor visitor = new OracleSchemaStatVisitor();
            sqlStatement.accept(visitor);
            // map的key为表名，value为该表使用到的所有列名
            Map<String, ArrayList<String>> map = new HashMap<>();
            // visitor.getColumns() 即是所有用到的表和字段
            for (TableStat.Column c : visitor.getColumns()) {
                // 没有添加的表，新建list
                if (!map.containsKey(c.getTable())) {
                    ArrayList<String> colList1 = new ArrayList<>();
                    colList1.add(c.getName());
                    map.put(c.getTable(), colList1);
                } else {
                    // 添加过的表，直接add
                    ArrayList<String> colList2 = map.get(c.getTable());
                    colList2.add(c.getName());
                }
                //                System.out.println(c);
            }
        }
        // parser得到AST
        SQLStatementParser parser = SQLParserUtils.createSQLStatementParser(
                sql, jdbcType);
        //只接受select 语句
        if (!Token.SELECT.equals(parser.getExprParser().getLexer().token())) {
            throw new RuntimeException("不支持 " + parser.getExprParser().getLexer().token() + " 语法,仅支持 SELECT 语法");
        }
        List<SQLStatement> stmtList = parser.parseStatementList();
        //接收查询字段
        List<SQLSelectItem> items = null;
        for (SQLStatement stmt : stmtList) {
            if (stmt instanceof SQLSelectStatement) {
                SQLSelectStatement sstmt = (SQLSelectStatement) stmt;
                SQLSelect sqlselect = sstmt.getSelect();
                SQLSelectQueryBlock query = (SQLSelectQueryBlock) sqlselect.getQuery();
                items = query.getSelectList();
                SQLTableSource tableSource = query.getFrom();
            }
        }
        for (SQLSelectItem s : items) {
            String column = s.getAlias();
            //            String column = StringUtils.isEmpty(s.getAlias()) ? expr.toString() : s.getAlias();
            //防止字段重复
            if (!columns.contains(column)) {
                columns.add(column);
            }
        }*/
    }
}
