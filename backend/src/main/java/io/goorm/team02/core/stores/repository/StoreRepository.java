package io.goorm.team02.core.stores.repository;

import io.goorm.team02.core.stores.domain.Store;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {

    /**
     * 사업자번호로 가게 존재 여부 확인
     */
    boolean existsByBusinessNumber(String businessNumber);

    /**
     * 사장님 ID로 활성화된 가게 존재 여부 확인
     */
    boolean existsByOwnerIdAndIsActiveTrue(Long ownerId);

    /**
     * 사장님 ID로 활성화된 가게 조회
     */
    Optional<Store> findByOwnerIdAndIsActiveTrue(Long ownerId);
}