package com.dododo.mymcpserver.service;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;

@Service
public class SQLService {
    @Autowired
    private EntityManager entityManager;

    // 原生 SQL 查询
    public List<?> executeQuery(String sql) {
        return entityManager.createNativeQuery(sql).getResultList();
    }

    // 原生SQL - update/insert/delete
    @Transactional
    public int executeUpdate(String sql) {
        return entityManager.createNativeQuery(sql).executeUpdate();
    }

    @Transactional
    public int executeUpdateWithParams(String sql, HashMap<String, String> params) {
        return entityManager.createNativeQuery(sql).executeUpdate();
    }
}
