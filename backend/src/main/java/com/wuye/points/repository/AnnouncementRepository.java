package com.wuye.points.repository;

import com.wuye.points.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, String>,
        JpaSpecificationExecutor<Announcement> {

    Page<Announcement> findByPropertyCompanyId(String propertyCompanyId, Pageable pageable);

    Optional<Announcement> findByIdAndPropertyCompanyId(String id, String propertyCompanyId);

    @Query("""
            SELECT COUNT(a) FROM Announcement a
            WHERE a.merchantId = :merchantId
              AND a.announcementType = 'merchant'
              AND a.createdAt >= :since
              AND a.status <> 'archived'
            """)
    long countRecentMerchantAnnouncements(@Param("merchantId") String merchantId,
                                          @Param("since") LocalDateTime since);

    /** 楼长当日已发条数（单元通知 / 商品分享分 contentType 限频） */
    long countByPublisherIdAndContentTypeAndCreatedAtGreaterThanEqual(
            String publisherId, String contentType, LocalDateTime since);

    /** 楼长「我的运营记录」列表：按发布者 + 内容类型取 */
    Page<Announcement> findByPublisherIdAndContentType(
            String publisherId, String contentType, Pageable pageable);
}
