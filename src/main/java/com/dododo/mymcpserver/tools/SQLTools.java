package com.dododo.mymcpserver.tools;

import com.dododo.mymcpserver.service.ConnectionPoolSQLService;
import com.dododo.mymcpserver.service.JPASQLFacadeService;
import com.dododo.mymcpserver.service.JPAService;
import com.dododo.mymcpserver.service.MybatisSQLService;
import com.dododo.mymcpserver.utils.SQLUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;

import static com.dododo.mymcpserver.constants.SQLConstants.INSERT;

@Service
public class SQLTools {
    @Autowired
    private ConnectionPoolSQLService connectionPoolSQLService;

    @Autowired
    private MybatisSQLService mybatisSQLService;

    @Autowired
    private JPASQLFacadeService jpasqlFacadeService;

    /**
     * 执行任意SQL:
     * - SELECT: 返回结果集(JSON数组)
     * - 其他: 返回 {"updated": 影响行数}
     * 安全提示: 外部输入请自行做白名单/参数化处理，避免 SQL 注入。
     */
    @Tool(name = "run-sql-connection-pool", description = "执行任意SQL(SELECT返回结果集, 其他语句返回影响行数), 该实现基于底层连接池的datasource")
    public String runSqlWithConnectionPool(String sql) {
        return connectionPoolSQLService.executeSQL(sql);
    }

    @Tool(name = "run-sql-mybatis-orm", description = "执行任意SQL(SELECT返回结果集, 其他语句返回影响行数), 该实现基于mybatis提供的SqlSessionTemplate")
    public String runSqlWithMybatisOrm(String sql) {
        return mybatisSQLService.executeSQL(sql);
    }

    @Tool(name = "run-sql-jpa", description = "执行任意SQL(SELECT返回结果集, 其他语句返回影响行数), 该实现基于JPA提供的entityManager")
    public String runSqlWithJPA(String sql) {
        return jpasqlFacadeService.executeSQL(sql);
    }

}
