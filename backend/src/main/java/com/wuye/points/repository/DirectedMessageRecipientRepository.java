package com.wuye.points.repository;

import com.wuye.points.entity.DirectedMessageRecipient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface DirectedMessageRecipientRepository extends JpaRepository<DirectedMessageRecipient, String>,
        JpaSpecificationExecutor<DirectedMessageRecipient> {

    Optional<DirectedMessageRecipient> findByMessageId(String messageId);

    List<DirectedMessageRecipient> findByMessageIdInAndReadStatus(Collection<String> messageIds, String readStatus);

    @Query("""
            SELECT COUNT(dmr) FROM DirectedMessageRecipient dmr
            JOIN DirectedMessageTask dmt ON dmr.taskId = dmt.id
            WHERE dmt.senderId = :senderId
              AND dmr.recipientId = :recipientId
              AND dmr.createdAt >= :start
              AND dmr.createdAt < :end
            """)
    long countBySenderAndRecipientToday(
            @Param("senderId") String senderId,
            @Param("recipientId") String recipientId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);
}
