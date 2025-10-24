package com.dododo.mymcpserver.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.query.NativeQuery;
import org.hibernate.transform.ResultTransformer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class JPAService {
    // 其默认实现是hibernate
    @Autowired
    private EntityManager entityManager;
    private final ObjectMapper objectMapper = new ObjectMapper();


    private NativeQuery<Map<String, Object>> createNativeHibernateQuery(String sql) {
        // 1. 创建原生查询
        Query query = entityManager.createNativeQuery(sql);
        // 2.【关键步骤】使用Hibernate API来获取结果并映射到List<Map>
        // 我们将Query解包为Hibernate的NativeQuery
        NativeQuery<Map<String, Object>> hibernateQuery = query.unwrap(NativeQuery.class);

        // 3. 设置结果转换器
        // AliasesToEntityMapResultTransformer会将每一行转为一个Map，key是列的别名(或名称)
        hibernateQuery.setResultTransformer(new AliasesToEntityMapResultTransformer());
        return hibernateQuery;
    }

    // 读操作可选开启只读事务（有助于某些数据库优化, 也避免误写）
    @Transactional(readOnly = true)
    public String executeSQLQuery(String sql) {
        if (sql == null || sql.isBlank()) {
            log.info("SQL不能为空");
            return "{\"error\":\"SQL不能为空\"}";
        }
        NativeQuery<Map<String, Object>> hibernateQuery = createNativeHibernateQuery(sql);
        // 获取结果
        List<Map<String, Object>> resultList = hibernateQuery.getResultList();

        // 使用Jackson将List<Map>转换为JSON字符串
        try {
            return objectMapper.writeValueAsString(resultList);
        } catch (JsonProcessingException e) {
            // 在实际应用中，这里应该有更完善的异常处理
            ObjectNode err = objectMapper.createObjectNode();
            err.put("error", e.getMessage());
            return err.toString();
        }
    }

    // 必须是 public/protected 才能被 Spring AOP 代理织入事务；private 调用不会走代理
    @Transactional
    public String executeSQLUpdate(String sql) {
        if (sql == null || sql.isBlank()) {
            return "{\"error\":\"SQL不能为空\"}";
        }
        NativeQuery<Map<String, Object>> hibernateQuery = createNativeHibernateQuery(sql);
        int result = hibernateQuery.executeUpdate();
        // 使用Jackson将List<Map>转换为JSON字符串
        try {
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            // 在实际应用中，这里应该有更完善的异常处理
            ObjectNode err = objectMapper.createObjectNode();
            err.put("error", e.getMessage());
            return err.toString();
        }
    }

    /**
     * 自定义一个ResultTransformer，因为Hibernate 6中废弃了旧的 AliasesToEntityMapResultTransformer。
     * 这个实现是兼容的，并且功能相同。
     */
    public static class AliasesToEntityMapResultTransformer implements ResultTransformer {
        @Override
        public Object transformTuple(Object[] tuple, String[] aliases) {
            Map<String, Object> result = new HashMap<>(aliases.length);
            for (int i = 0; i < aliases.length; i++) {
                String alias = aliases[i];
                if (alias != null) {
                    // Clob/Blob等特殊类型可能需要额外处理，这里简化
                    result.put(alias, tuple[i]);
                }
            }
            return result;
        }
        @Override
        public List transformList(List collection) {
            return collection;
        }
    }
}
