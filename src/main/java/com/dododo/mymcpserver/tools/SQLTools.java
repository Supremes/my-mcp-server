package com.dododo.mymcpserver.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;

import javax.sql.DataSource;
import java.sql.*;

public class SQLTools {
    @Autowired
    private DataSource dataSource;

    private final ObjectMapper mapper = new ObjectMapper();

    @Tool(name = "execute-sql", description = "支持CRUD各类操作")
    public String executeSQL(String sql) {
        return "";
    }

    @Tool(name = "list-tables", description = "支持罗列库中的表单")
    public String listTables(String sql) {
        return "";
    }

    /**
     * 执行任意SQL:
     * - SELECT: 返回结果集(JSON数组)
     * - 其他: 返回 {"updated": 影响行数}
     * 安全提示: 外部输入请自行做白名单/参数化处理，避免 SQL 注入。
     */
    @Tool(name = "run-sql", description = "执行任意SQL(SELECT返回结果集, 其他语句返回影响行数)")
    public String runSql(String sql) {
        if (sql == null || sql.isBlank()) {
            return "{\"error\":\"SQL不能为空\"}";
        }
        String trimmed = sql.trim();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // 简单判定是否为查询
            if (trimmed.regionMatches(true, 0, "SELECT", 0, 6)) {
                try (ResultSet rs = stmt.executeQuery(trimmed)) {
                    ResultSetMetaData meta = rs.getMetaData();
                    int cols = meta.getColumnCount();
                    ArrayNode arr = mapper.createArrayNode();
                    while (rs.next()) {
                        ObjectNode row = mapper.createObjectNode();
                        for (int i = 1; i <= cols; i++) {
                            String colName = meta.getColumnLabel(i);
                            Object val = rs.getObject(i);
                            if (val == null) {
                                row.putNull(colName);
                            } else if (val instanceof Number) {
                                row.putPOJO(colName, val);
                            } else {
                                row.put(colName, val.toString());
                            }
                        }
                        arr.add(row);
                    }
                    return mapper.writeValueAsString(arr);
                }
            } else {
                int affected = stmt.executeUpdate(trimmed);
                ObjectNode result = mapper.createObjectNode();
                result.put("updated", affected);
                return mapper.writeValueAsString(result);
            }
        } catch (SQLException e) {
            ObjectNode err = mapper.createObjectNode();
            err.put("error", e.getMessage());
            return err.toString();
        } catch (Exception ex) {
            return "{\"error\":\"执行异常: " + ex.getMessage() + "\"}";
        }
    }
}
