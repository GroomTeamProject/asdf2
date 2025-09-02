package io.goorm.team02.core.stores.service;

import io.goorm.team02.core.stores.controller.dto.StoreCreateRequest;
import io.goorm.team02.core.stores.controller.dto.StoreUpdateRequest;
import io.goorm.team02.core.stores.domain.Store;
import io.goorm.team02.core.users.domain.User;
import io.goorm.team02.core.stores.repository.StoreRepository;
import io.goorm.team02.core.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 추가
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j // 추가
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    /**
     * 가게 등록 (최초 1회)
     */
    @Transactional
    public Store createStore(StoreCreateRequest request) {
        log.info("=== 가게 등록 시작 ===");

        // 현재 로그인한 사용자 ID 가져오기
        Long currentUserId = getCurrentUserId();
        log.info("현재 사용자 ID: {}", currentUserId);

        // 입력받은 값들 로그 출력
        log.info("입력받은 가게 정보:");
        log.info("  - 사업자등록번호: {}", request.getBusinessNumber());
        log.info("  - 가게명: {}", request.getName());
        log.info("  - 설명: {}", request.getDescription());
        log.info("  - 전화번호: {}", request.getPhone());
        log.info("  - 주소: {}", request.getAddress());
        log.info("  - 상세주소: {}", request.getDetailAddress());
        log.info("  - 위도: {}", request.getLatitude());
        log.info("  - 경도: {}", request.getLongitude());
        log.info("  - 카테고리: {}", request.getCategory());
        log.info("  - 최소주문금액: {}", request.getMinOrderAmount());
        log.info("  - 배달료: {}", request.getDeliveryFee());
        log.info("  - 최소배달시간: {}분", request.getDeliveryTimeMin());
        log.info("  - 최대배달시간: {}분", request.getDeliveryTimeMax());
        log.info("  - 이미지URL: {}", request.getImageUrl());

        // 사용자 조회 및 검증
        log.info("사용자 조회 중...");
        User owner = userRepository.findById(currentUserId)
            .orElseThrow(() -> {
                log.error("사용자를 찾을 수 없습니다. ID: {}", currentUserId);
                return new RuntimeException("사용자를 찾을 수 없습니다");
            });
        //log.info("사용자 조회 성공: {} ({})", owner.getName(), owner.getEmail());

        // 사용자가 이미 가게를 가지고 있는지 확인
        log.info("기존 가게 존재 여부 확인 중...");
        if (storeRepository.existsByOwnerIdAndIsActiveTrue(currentUserId)) {
            log.warn("이미 등록된 가게가 있습니다. 사용자 ID: {}", currentUserId);
            throw new RuntimeException("이미 등록된 가게가 있습니다");
        }
        log.info("기존 가게 없음 - 새 가게 등록 진행");

        // Store 엔티티 생성
        log.info("Store 엔티티 생성 중...");
        Store store = new Store();

        store.setOwner(owner);
        store.setBusinessNumber(request.getBusinessNumber());
        store.setName(request.getName());
        store.setDescription(request.getDescription());
        store.setPhone(request.getPhone());
        store.setAddress(request.getAddress());
        store.setDetailAddress(request.getDetailAddress());
        store.setLatitude(request.getLatitude());
        store.setLongitude(request.getLongitude());
        store.setCategory(request.getCategory());
        store.setMinOrderAmount(request.getMinOrderAmount());
        store.setDeliveryFee(request.getDeliveryFee());
        store.setDeliveryTimeMin(request.getDeliveryTimeMin());
        store.setDeliveryTimeMax(request.getDeliveryTimeMax());
        store.setImageUrl(request.getImageUrl());

        log.info("Store 엔티티 생성 완료");
        log.info("데이터베이스 저장 중...");

        Store savedStore = storeRepository.save(store);

        log.info("가게 등록 완료! 생성된 가게 ID: {}, 가게명: {}", savedStore.getId(), savedStore.getName());
        log.info("=== 가게 등록 종료 ===");

        return savedStore;
    }

    /**
     * 내 가게 정보 조회
     */
    public Store getMyStore() {
        Long currentUserId = getCurrentUserId();
        log.debug("내 가게 정보 조회 - 사용자 ID: {}", currentUserId);

        return storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)
            .orElseThrow(() -> {
                log.warn("등록된 가게가 없습니다. 사용자 ID: {}", currentUserId);
                return new RuntimeException("등록된 가게가 없습니다");
            });
    }

    /**
     * 가게 기본 정보 수정
     */
    @Transactional
    public Store updateStore(StoreUpdateRequest request) {
        log.info("가게 정보 수정 시작");
        Store store = getMyStore();

        // 필드 업데이트
        if (request.getName() != null) {
            log.info("가게명 변경: {} -> {}", store.getName(), request.getName());
            store.setName(request.getName());
        }
        if (request.getDescription() != null) {
            log.info("설명 변경: {} -> {}", store.getDescription(), request.getDescription());
            store.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            log.info("카테고리 변경: {} -> {}", store.getCategory(), request.getCategory());
            store.setCategory(request.getCategory());
        }
        if (request.getImageUrl() != null) {
            log.info("이미지URL 변경: {} -> {}", store.getImageUrl(), request.getImageUrl());
            store.setImageUrl(request.getImageUrl());
        }

        log.info("가게 정보 수정 완료");
        return store;
    }

    /**
     * 가게 삭제 (비활성화)
     */
    @Transactional
    public void deleteStore() {
        log.info("가게 삭제(비활성화) 시작");
        Store store = getMyStore();
        store.setIsActive(false);
        log.info("가게 삭제 완료 - 가게 ID: {}, 가게명: {}", store.getId(), store.getName());
    }

    /**
     * 현재 로그인한 사용자 ID 조회
     */
    private Long getCurrentUserId() {
        // TODO: Spring Security Context에서 현재 사용자 ID 조회
        // 임시로 1L 반환
        return 1L;
    }
}