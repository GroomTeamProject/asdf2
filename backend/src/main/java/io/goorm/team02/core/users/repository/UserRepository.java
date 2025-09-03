package io.goorm.team02.core.users.repository;

import io.goorm.team02.core.users.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email); //이메일로 사용자 조회
    Optional<User> findByPhone(String phone); //전화번호로 사용자 조회
}