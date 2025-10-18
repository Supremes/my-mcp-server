package com.dododo.mymcpserver.service;

import com.dododo.mymcpserver.utils.SQLUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JPASQLFacadeService {

    private final JPAService jpaService;

    /**
     * 分发并执行 SQL（只负责 JPA 部分）。
     * 只做语义路由，不做安全校验，可在此处增加白名单/权限控制。
     */
    public String executeSQL(String sql) {
        if (sql == null || sql.isBlank()) {
            log.info("SQL不能为空");
            return "{\"error\":\"SQL不能为空\"}";
        }
        log.info("Facade 分发 SQL: {}", sql);
        if (SQLUtils.isQuerySQL(sql)) {
            return jpaService.executeSQLQuery(sql);
        } else {
            return jpaService.executeSQLUpdate(sql);
        }
    }
}
