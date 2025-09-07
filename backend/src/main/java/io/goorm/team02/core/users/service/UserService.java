package io.goorm.team02.core.users.service;

import io.goorm.team02.core.users.domain.User;
import io.goorm.team02.core.users.domain.enums.UserType;
import io.goorm.team02.core.users.controller.dto.SignupRequest;
import io.goorm.team02.core.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User registerUser(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // 전화번호 중복 체크
        if (request.getPhone() != null && userRepository.findByPhone(request.getPhone()).isPresent()) {
            throw new RuntimeException("Phone number already exists");
        }

        //  비밀번호 일치 확인
        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // User 생성
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        //user.setUserType(UserType.CUSTOMER);  // 기본값
        user.setUserType(request.getUserType());
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        return userRepository.save(user);
    }
}
