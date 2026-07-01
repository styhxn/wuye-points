package com.wuye.points.repository;

import com.wuye.points.entity.AnnouncementCollect;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementCollectRepository extends JpaRepository<AnnouncementCollect, String> {

    List<AnnouncementCollect> findByAnnouncementId(String announcementId);

    Page<AnnouncementCollect> findByAnnouncementId(String announcementId, Pageable pageable);

    Optional<AnnouncementCollect> findByAnnouncementIdAndResidentId(String announcementId, String residentId);

    boolean existsByAnnouncementIdAndResidentId(String announcementId, String residentId);

    long countByAnnouncementId(String announcementId);
}
