package com.dododo.mymcpserver.controller;

import com.dododo.mymcpserver.model.SqlParamsDTO;
import com.dododo.mymcpserver.service.ConnectionPoolSQLService;
import com.dododo.mymcpserver.service.JPASQLFacadeService;
import com.dododo.mymcpserver.service.MybatisSQLService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SQLController {
    @Autowired
    private JPASQLFacadeService JPASQLFacadeService;
    @Autowired
    private MybatisSQLService mybatisSQLService;
    @Autowired
    private ConnectionPoolSQLService connectionPoolSQLService;

    @PostMapping("/executeSQL/JPA")
    public String executeSQLJPA(SqlParamsDTO sqlParamsDTO) {
        log.info("params: {}", sqlParamsDTO);
        return JPASQLFacadeService.executeSQL(sqlParamsDTO.getSql());
    }

    @PostMapping("/executeSQL/ConnectionPool")
    public String executeSQLCP(SqlParamsDTO sqlParamsDTO) {
        log.info("params: {}", sqlParamsDTO);
        connectionPoolSQLService.executeSQL(sqlParamsDTO.getSql());
        return "";
    }

    @PostMapping("/executeSQL/Mybatis")
    public String executeSQLMybatis(SqlParamsDTO sqlParamsDTO) {
        log.info("params: {}", sqlParamsDTO);
        mybatisSQLService.executeSQL(sqlParamsDTO.getSql());
        return "";
    }
}
