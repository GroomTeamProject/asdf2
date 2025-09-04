package io.goorm.team02.core.stores.repository;

import io.goorm.team02.core.stores.domain.StoreHour;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

public interface StoreHourRepository extends JpaRepository<StoreHour, Long> {

    @Modifying
    @Transactional
    @Query("DELETE FROM StoreHour sh WHERE sh.store.id = :storeId")
    void deleteByStoreId(@Param("storeId") Long storeId);
}