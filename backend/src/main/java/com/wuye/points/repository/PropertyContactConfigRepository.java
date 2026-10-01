package com.wuye.points.repository;

import com.wuye.points.entity.PropertyContactConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyContactConfigRepository extends JpaRepository<PropertyContactConfig, String> {

    Optional<PropertyContactConfig> findFirstByOrderByIdAsc();
}
