package io.goorm.team02.core.users.service;

import io.goorm.team02.core.users.controller.dto.UserAddressRequest;
import io.goorm.team02.core.users.controller.dto.UserAddressResponse;
import io.goorm.team02.core.users.domain.User;
import io.goorm.team02.core.users.domain.UserAddress;
import io.goorm.team02.core.users.repository.UserAddressRepository;
import io.goorm.team02.core.users.repository.UserinfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAddressService {

    private final UserinfoRepository userRepository;
    private final UserAddressRepository userAddressRepository;

    @Transactional
    public UserAddressResponse addAddress(Long userId, UserAddressRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserAddress address = UserAddress.builder()
                .user(user)
                .addressName(request.getAddressName())
                .address(request.getAddress())
                .detailAddress(request.getDetailAddress())
                .zipcode(request.getZipcode())
                .isDefault(request.isDefault())
                .build();

        UserAddress saved = userAddressRepository.save(address);

        return new UserAddressResponse(
                saved.getId(),
                saved.getAddressName(),
                saved.getAddress(),
                saved.getDetailAddress(),
                saved.getZipcode(),
                saved.getIsDefault()
        );
    }
}
