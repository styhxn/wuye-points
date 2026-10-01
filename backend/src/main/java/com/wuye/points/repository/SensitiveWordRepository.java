package com.wuye.points.repository;

import com.wuye.points.entity.SensitiveWord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensitiveWordRepository extends JpaRepository<SensitiveWord, String> {
}
