package com.wuye.points.repository;

import com.wuye.points.entity.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

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

    @Query("SELECT COUNT(m) FROM Message m WHERE m.toId = :receiverId AND m.readStatus = 'unread'")
    long countUnreadByReceiverId(@Param("receiverId") String receiverId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.toId = :receiverId AND m.readStatus = 'unread' AND m.messageType = :messageType")
    long countUnreadByReceiverIdAndMessageType(@Param("receiverId") String receiverId, @Param("messageType") String messageType);

    // 邻居私聊固定为 chatType=private + messageType=resident_message（schema 中 chat_type 仅 private/group/broadcast）
    @Query("SELECT COUNT(m) FROM Message m WHERE m.toId = :receiverId AND m.readStatus = 'unread' "
           + "AND m.chatType = 'private' AND m.messageType = 'resident_message'")
    long countUnreadSingleByReceiverId(@Param("receiverId") String receiverId);

    List<Message> findByRelatedTaskId(String relatedTaskId);

    interface SenderUnreadCount {
        String getSenderId();
        long getCount();
    }

    @Query("SELECT m.fromId AS senderId, COUNT(m) AS count FROM Message m " +
           "WHERE m.toId = :receiverId AND m.readStatus = 'unread' GROUP BY m.fromId")
    List<SenderUnreadCount> countUnreadGroupBySender(@Param("receiverId") String receiverId);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.toId = :receiverId AND m.fromId = :senderId AND m.readStatus = 'unread'")
    long countUnreadByReceiverIdAndSenderId(@Param("receiverId") String receiverId, @Param("senderId") String senderId);

    /** 管理端会话消息：某物业下指定 chatType 的全部消息（按时间倒序，便于按对端聚合） */
    @Query("SELECT m FROM Message m WHERE (:pc IS NULL OR m.propertyCompanyId = :pc) " +
           "AND m.chatType IN :chatTypes ORDER BY m.createdAt DESC")
    List<Message> findConversationMessages(@Param("pc") String propertyCompanyId,
                                            @Param("chatTypes") List<String> chatTypes);

    /** 管理端与指定住户的 1:1 会话消息（按时间倒序分页） */
    @Query("SELECT m FROM Message m WHERE ((m.fromId = :a AND m.toId = :b) OR (m.fromId = :b AND m.toId = :a)) " +
           "AND (:pc IS NULL OR m.propertyCompanyId = :pc) ORDER BY m.createdAt DESC")
    Page<Message> findConversationBetween(@Param("a") String a, @Param("b") String b,
                                          @Param("pc") String pc, Pageable pageable);

    /** 当前用户参与的全部会话消息（本人发出或接收，排除群聊，按时间倒序供按对端聚合） */
    @Query("SELECT m FROM Message m WHERE (:pc IS NULL OR m.propertyCompanyId = :pc) " +
           "AND (m.fromId = :uid OR m.toId = :uid) AND m.chatType <> 'group' ORDER BY m.createdAt DESC")
    List<Message> findMyConversationMessages(@Param("pc") String propertyCompanyId,
                                             @Param("uid") String currentUserId);
}
