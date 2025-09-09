package io.goorm.team02.core.stores.repository;

import io.goorm.team02.core.stores.domain.TempUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoreUserRepository extends JpaRepository<TempUser, Long> {
}