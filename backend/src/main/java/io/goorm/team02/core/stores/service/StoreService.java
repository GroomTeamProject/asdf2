package io.goorm.team02.core.stores.service;

import io.goorm.team02.core.stores.controller.dto.StoreContactRequest;
import io.goorm.team02.core.stores.controller.dto.StoreCreateRequest;
import io.goorm.team02.core.stores.controller.dto.StoreDeliveryRequest;
import io.goorm.team02.core.stores.controller.dto.StoreHolidayRequest;
import io.goorm.team02.core.stores.controller.dto.StoreHolidayResponse;
import io.goorm.team02.core.stores.controller.dto.StoreHourRequest;
import io.goorm.team02.core.stores.controller.dto.StoreHourResponse;
import io.goorm.team02.core.stores.controller.dto.StoreLocationRequest;
import io.goorm.team02.core.stores.controller.dto.StoreUpdateRequest;
import io.goorm.team02.core.stores.domain.Store;
import io.goorm.team02.core.stores.domain.StoreHoliday;
import io.goorm.team02.core.stores.domain.StoreHour;
import io.goorm.team02.core.stores.repository.StoreHolidayRepository;
import io.goorm.team02.core.stores.repository.StoreHourRepository;
import io.goorm.team02.core.stores.domain.TempUser;
import io.goorm.team02.core.stores.repository.StoreRepository;
import io.goorm.team02.core.stores.repository.UserRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j; // 추가
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j // 추가
public class StoreService {

    private final StoreRepository storeRepository;
    private final UserRepository userRepository;
    private final StoreHourRepository storeHourRepository;
    private final StoreHolidayRepository storeHolidayRepository;

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
        TempUser owner = userRepository.findById(currentUserId)
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

    @Transactional
    public Store updateContact(StoreContactRequest request) {
        log.info("가게 연락처 변경");
        Store store = getMyStore();
        store.setPhone(request.getPhone());
        return store;
    }

    @Transactional
    public Store updateDelivery(StoreDeliveryRequest request) {
        log.info("배달비/최소주문금액 변경");
        Store store = getMyStore();
        store.setDeliveryFee(request.getDeliveryFee());
        store.setMinOrderAmount(request.getMinOrderAmount());
        store.setDeliveryTimeMin(request.getDeliveryTimeMin());
        store.setDeliveryTimeMax(request.getDeliveryTimeMax());
        return store;
    }

    @Transactional
    public Store updateLocation(StoreLocationRequest request) {
        log.info("가게 위치 정보 변경");
        Store store = getMyStore();
        store.setAddress(request.getAddress());
        store.setDetailAddress(request.getDetailAddress());
        //store.setLatitude(request.getLatitude());
        //store.setLongitude(request.getLongitude());
        return store;
    }

    @Transactional
    public String uploadImage(MultipartFile file) {
        log.info("가게 이미지 수정");
        Store store = getMyStore();
        //S3 url 로직 필요
        store.setImageUrl(file.getOriginalFilename());
        return storeRepository.save(store).getImageUrl();
    }

    @Transactional
    public Store deleteImage(Long id) {
        log.info("가게 이미지 삭제");
        Store store = getMyStore();
        store.setImageUrl(null);
        return storeRepository.save(store);
    }

    public List<StoreHour> getStoreHours() {
        log.info("가게 운영 시간 조회");
        return getMyStore().getStoreHours();
    }

    public List<StoreHourResponse> updateStoreHours(List<StoreHourRequest> requests) {
        log.info("가게 운영 시간 설정");
        Store store = getMyStore();

        // 업데이트 대상 요일 수집
        Set<Integer> targetDays = new HashSet<>();

        for (StoreHourRequest request : requests) {
            if (request.getDayOfWeek() == 7) {
                // 전체 요일 (0~6)
                for (int day = 0; day <= 6; day++) {
                    targetDays.add(day);
                }
            } else {
                // 특정 요일
                targetDays.add(request.getDayOfWeek());
            }
        }

        // 해당 요일들만 DB에서 삭제
        storeHourRepository.deleteByStoreIdAndDayOfWeekIn(store.getId(), targetDays);

        // 메모리에서도 해당 요일만 삭제
        store.getStoreHours().removeIf(hour -> targetDays.contains(hour.getDayOfWeek()));

        // 새로운 운영시간 설정
        List<StoreHour> updatedHours = new ArrayList<>();

        for (StoreHourRequest request : requests) {
            // dayOfWeek가 7이면 일주일 모든 요일에 적용 (0~6)
            if (request.getDayOfWeek() == 7) {
                for (int day = 0; day <= 6; day++) {
                    StoreHour storeHour = StoreHour.builder()
                        .store(store)
                        .dayOfWeek(day)
                        .openTime(request.getOpenTime())
                        .closeTime(request.getCloseTime())
                        .isClosed(request.getIsClosed())
                        .build();

                    updatedHours.add(storeHour);
                }
            } else {
                // 특정 요일만 설정
                StoreHour storeHour = StoreHour.builder()
                    .store(store)
                    .dayOfWeek(request.getDayOfWeek())
                    .openTime(request.getOpenTime())
                    .closeTime(request.getCloseTime())
                    .isClosed(request.getIsClosed())
                    .build();

                updatedHours.add(storeHour);
            }
        }

        // 새로운 운영시간들을 DB에 저장
        List<StoreHour> savedHours = storeHourRepository.saveAll(updatedHours);

        // DTO로 변환해서 리턴
        return savedHours.stream()
            .map(StoreHourResponse::from)
            .collect(Collectors.toList());
    }

    public ResponseEntity<String> createHoliday(StoreHolidayRequest request) {
        log.info("휴무일 설정");

        try {
            Store store = getMyStore();

            if (request.getDate() == null) {
                return ResponseEntity.badRequest().body("휴무일을 입력해주세요");
            }

            if (request.getDate().isBefore(LocalDate.now())) {
                return ResponseEntity.badRequest().body("과거 날짜는 휴무일로 설정할 수 없습니다");
            }

            boolean exists = storeHolidayRepository.existsByStoreIdAndDate(
                store.getId(),
                request.getDate()
            );

            if (exists) {
                return ResponseEntity.badRequest().body("이미 등록된 휴무일입니다: " + request.getDate());
            }

            StoreHoliday holiday = StoreHoliday.builder()
                .store(store)
                .date(request.getDate())
                .reason(request.getReason())
                .isRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false)
                .build();

            storeHolidayRepository.save(holiday);

            log.info("휴무일 등록 완료: {} - {} (매년반복: {})",
                request.getDate(),
                request.getReason(),
                holiday.getIsRecurring());

            return ResponseEntity.ok("휴무일이 성공적으로 등록되었습니다");

        } catch (IllegalArgumentException e) {
            log.error("휴무일 등록 실패 - 잘못된 입력: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (Exception e) {
            log.error("휴무일 등록 실패 - 서버 오류: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("휴무일 등록 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    public List<StoreHolidayResponse> getHolidays() {
        log.info("휴무일 조회");

        try {
            Store store = getMyStore();

            // 현재 날짜 이후의 휴무일들만 조회
            List<StoreHoliday> holidays = storeHolidayRepository
                .findByStoreIdAndDateGreaterThanEqualOrderByDateAsc(store.getId(), LocalDate.now());

            // Entity -> DTO 변환 (간단하게)
            List<StoreHolidayResponse> result = holidays.stream()
                .map(StoreHolidayResponse::from)
                .collect(Collectors.toList());

            log.info("휴무일 조회 완료: {}개", result.size());
            return result;

        } catch (Exception e) {
            log.error("휴무일 조회 실패: {}", e.getMessage());
            throw new RuntimeException("휴무일 조회 중 오류가 발생했습니다", e);
        }
    }

    @Transactional
    public ResponseEntity<String> deleteHoliday(Long id) {
        log.info("휴무일 삭제 요청: ID {}", id);

        try {
            Store store = getMyStore();

            // 휴무일 존재 및 권한 확인
            StoreHoliday holiday = storeHolidayRepository.findById(id)
                .orElse(null);

            if (holiday == null) {
                return ResponseEntity.badRequest().body("존재하지 않는 휴무일입니다");
            }

            if (!holiday.getStore().getId().equals(store.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("다른 가게의 휴무일은 삭제할 수 없습니다");
            }

            // 삭제
            storeHolidayRepository.delete(holiday);

            log.info("휴무일 삭제 완료: {} - {}", holiday.getDate(), holiday.getReason());
            return ResponseEntity.ok("휴무일이 성공적으로 삭제되었습니다");

        } catch (Exception e) {
            log.error("휴무일 삭제 실패: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("휴무일 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}