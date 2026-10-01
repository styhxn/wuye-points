package com.wuye.points.repository;

import com.wuye.points.entity.CommunityEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityEntityRepository extends JpaRepository<CommunityEntity, String>,
        JpaSpecificationExecutor<CommunityEntity> {
}
