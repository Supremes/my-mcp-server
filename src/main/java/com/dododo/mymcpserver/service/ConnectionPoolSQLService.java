package com.dododo.mymcpserver.service;

import com.dododo.mymcpserver.utils.SQLUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Service
public class ConnectionPoolSQLService {
    @Autowired
    private DataSource dataSource;
    private final ObjectMapper mapper = new ObjectMapper();

    public String executeSQL(String sql) {
        if (sql == null || sql.isBlank()) {
            log.info("SQL不能为空");
            return "{\"error\":\"SQL不能为空\"}";
        }
        log.info("执行SQL: {}", sql);
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            return SQLUtils.executeSQLWithStatement(sql, statement);
        } catch (SQLException sqlException) {
            ObjectNode errNode = mapper.createObjectNode();
            errNode.put("error", sqlException.getMessage());
            return errNode.toString();
        } catch (Exception ex) {
            return "{\"error\":\"执行异常: " + ex.getMessage() + "\"}";
        }
    }
}
