package io.goorm.team02.core.owner.stores.repository;

import io.goorm.team02.core.owner.stores.domain.TempUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<TempUser, Long> {
    Optional<TempUser> findByEmail(String email);
}