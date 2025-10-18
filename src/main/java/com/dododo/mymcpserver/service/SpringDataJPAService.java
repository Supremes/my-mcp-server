package com.dododo.mymcpserver.service;

import com.dododo.mymcpserver.entity.About;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataJPAService extends JpaRepository<About, Long> {
    // 当你转向操作实体对象和使用参数化查询时，EntityManager和JpaRepository才能真正发挥其简化开发、提升安全性的巨大优势。
}
