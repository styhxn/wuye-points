package com.wuye.points.repository;

import com.wuye.points.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 消息 Repository
 * 注：from_id / to_id 隐含 propertyCompanyId（用户归属唯一物业）
 * 数据库字段 from_id/to_id 对应实体属性 fromId/toId
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, String>, JpaSpecificationExecutor<Message> {

    Page<Message> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.fromId = :senderId")
    Page<Message> findBySenderId(@Param("senderId") String senderId, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.toId = :receiverId")
    Page<Message> findByReceiverId(@Param("receiverId") String receiverId, Pageable pageable);
}
