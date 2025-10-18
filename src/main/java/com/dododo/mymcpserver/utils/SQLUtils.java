package com.dododo.mymcpserver.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String executeSQLWithStatement(String sql, Statement stmt) throws SQLException, JsonProcessingException {
        String trimmed = sql.trim();
        if (isQuerySQL(sql)) {
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
    }

    public static boolean isQuerySQL(String sql) {
        String upperSQL = sql.trim().toUpperCase();
        if (upperSQL.startsWith("SELECT") || upperSQL.startsWith("SHOW") ||
                upperSQL.startsWith("DESCRIBE") || upperSQL.startsWith("DESC")) {
            return true;
        }
        return false;
    }
}
