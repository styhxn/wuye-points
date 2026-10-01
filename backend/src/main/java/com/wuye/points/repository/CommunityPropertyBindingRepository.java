package com.wuye.points.repository;

import com.wuye.points.entity.CommunityPropertyBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommunityPropertyBindingRepository extends JpaRepository<CommunityPropertyBinding, String> {

    List<CommunityPropertyBinding> findByCommunityEntityId(String communityEntityId);

    List<CommunityPropertyBinding> findByPropertyCompanyId(String propertyCompanyId);

    Optional<CommunityPropertyBinding> findByCommunityEntityIdAndPropertyCompanyId(
            String communityEntityId, String propertyCompanyId);

    boolean existsByCommunityEntityIdAndPropertyCompanyId(String communityEntityId, String propertyCompanyId);

    void deleteByCommunityEntityIdAndPropertyCompanyId(String communityEntityId, String propertyCompanyId);
}
