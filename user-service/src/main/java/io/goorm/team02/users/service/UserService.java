
package io.goorm.team02.users.service;

import io.goorm.team02.users.domain.User;
import io.goorm.team02.users.domain.enums.UserType;
import io.goorm.team02.common.dto.auth.SignupRequest;
import io.goorm.team02.common.dto.auth.SignupResponse;
//import io.goorm.team02.auth.service.RefreshTokenService;
import io.goorm.team02.common.exception.dto.client.ConflictException;
import io.goorm.team02.users.repository.UserinfoRepository;
import io.goorm.team02.users.domain.UserAddress;
import io.goorm.team02.users.repository.UserAddressRepository;
import io.goorm.team02.users.controller.dto.UserUpdateRequest;
import io.goorm.team02.users.controller.dto.UserAddressRequest;

import java.util.List;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
// 회원가입 로직, user 정보/ user_adress 정보 각각의 db에 저장
public class UserService {

    private final UserinfoRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserAddressRepository userAddressRepository;

    public SignupResponse registerUser(SignupRequest request) {
        // 이메일 중복 체크
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            //throw new RuntimeException("Email already exists"); // 500에러로 넘어감
            throw new ConflictException("Email already exists");  //409에러로 처리
        }

        // 전화번호 전처리 (-, 공백 등 제거 후 숫자만 남김)
        String cleanedPhone = null;
        if (request.getPhone() != null) {
            cleanedPhone = request.getPhone().replaceAll("[^0-9]", "");
            // 전화번호 중복 체크 (전처리된 값 기준)
            if (userRepository.findByPhone(cleanedPhone).isPresent()) {
                throw new ConflictException("Phone number already exists");
            }
        }

        // 비밀번호 일치 확인
        if (!request.getPassword().equals(request.getPasswordCheck())) {
            throw new ConflictException("비밀번호가 일치하지 않습니다.");
        }

        // User 생성
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        //user.setPhone(request.getPhone());
        user.setPhone(cleanedPhone);
        user.setUserType(UserType.valueOf(request.getUserType()));
        user.setIsActive(true);
        user.setEmailVerified(false);
        user.setPhoneVerified(false);

        User savedUser = userRepository.save(user);

        // UserAddress 저장
        UserAddress address = new UserAddress();
        address.setUser(savedUser);
        address.setAddressName(request.getAddressName());
        address.setAddress(request.getAddress());
        address.setDetailAddress(request.getDetailAddress());
        address.setZipcode(request.getZipcode());
        address.setIsDefault(request.getIsDefault());

        userAddressRepository.save(address);

        // ✅ Response DTO 반환 : email, name, user_type
        return new SignupResponse(savedUser.getEmail(), savedUser.getName(), savedUser.getUserType().name());
    }

}
