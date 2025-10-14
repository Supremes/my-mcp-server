package com.dododo.mymcpserver.controller;

import com.dododo.mymcpserver.model.SqlParamsDTO;
import com.dododo.mymcpserver.service.SQLService;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.SQLSelect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class SQLController {
    @Autowired
    private SQLService sqlService;

    @PostMapping("/executeSQL")
    public String executeSQL(SqlParamsDTO sqlParamsDTO) {
        log.info("params: {}", sqlParamsDTO);
        sqlService.executeQuery(sqlParamsDTO.getSql());
        return "";
    }
}
