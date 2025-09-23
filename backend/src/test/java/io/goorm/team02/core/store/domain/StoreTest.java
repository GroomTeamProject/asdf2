package io.goorm.team02.core.store.domain;

import io.goorm.team02.core.auth.security.SecurityUtils;
import io.goorm.team02.core.common.service.S3Service;
import io.goorm.team02.core.orders.service.OrderService;
import io.goorm.team02.core.reviews.repository.ReviewRepository;
import io.goorm.team02.core.stores.controller.dto.storemanagement.*;
import io.goorm.team02.core.stores.domain.Store;
import io.goorm.team02.core.stores.domain.StoreHour;
import io.goorm.team02.core.stores.domain.StoreHoliday;
import io.goorm.team02.core.stores.domain.TempUser;
import io.goorm.team02.core.stores.domain.enums.StoreCategory;
import io.goorm.team02.core.stores.domain.enums.StoreStatus;
import io.goorm.team02.core.stores.domain.enums.UserType;
import io.goorm.team02.core.stores.repository.StoreHolidayRepository;
import io.goorm.team02.core.stores.repository.StoreHourRepository;
import io.goorm.team02.core.stores.repository.StoreRepository;
import io.goorm.team02.core.stores.repository.UserRepository;
import io.goorm.team02.core.stores.service.StoreService;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static io.goorm.team02.core.auth.security.SecurityUtils.getCurrentUserId;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService 테스트")
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreHourRepository storeHourRepository;

    @Mock
    private StoreHolidayRepository storeHolidayRepository;

    @Mock
    private OrderService orderService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private StoreService storeService;

    private TempUser testUser;
    private Store testStore;
    private StoreCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성
        testUser = new TempUser();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setName("테스트 사장님");
        testUser.setUserType(UserType.OWNER);
        testUser.setPhone("010-1234-5678");
        testUser.setIsActive(true);

        // 테스트용 가게 생성
        testStore = new Store();
        testStore.setId(1L);
        testStore.setOwner(testUser);
        testStore.setBusinessNumber("123-45-67890");
        testStore.setName("테스트 가게");
        testStore.setDescription("테스트 가게 설명");
        testStore.setPhone("02-1234-5678");
        testStore.setAddress("서울시 강남구");
        testStore.setDetailAddress("123번지");
        testStore.setLatitude(new BigDecimal("37.5665"));
        testStore.setLongitude(new BigDecimal("126.9780"));
        testStore.setCategory(StoreCategory.KOREAN);
        testStore.setMinOrderAmount(new BigDecimal("15000"));
        testStore.setDeliveryFee(new BigDecimal("3000"));
        testStore.setDeliveryTimeMin(30);
        testStore.setDeliveryTimeMax(50);
        testStore.setStatus(StoreStatus.CLOSED);
        testStore.setIsActive(true);
        testStore.setStoreHours(new ArrayList<>());

        // 테스트용 가게 생성 요청 DTO
        createRequest = new StoreCreateRequest();
        createRequest.setBusinessNumber("123-45-67890");
        createRequest.setName("테스트 가게");
        createRequest.setDescription("테스트 가게 설명");
        createRequest.setPhone("02-1234-5678");
        createRequest.setAddress("서울시 강남구");
        createRequest.setDetailAddress("123번지");
        createRequest.setLatitude(new BigDecimal("37.5665"));
        createRequest.setLongitude(new BigDecimal("126.9780"));
        createRequest.setCategory(StoreCategory.KOREAN);
        createRequest.setMinOrderAmount(new BigDecimal("15000"));
        createRequest.setDeliveryFee(new BigDecimal("3000"));
        createRequest.setDeliveryTimeMin(30);
        createRequest.setDeliveryTimeMax(50);
        createRequest.setImageUrl("https://example.com/image.jpg");
    }

    @Nested
    @DisplayName("가게 등록 테스트")
    class CreateStoreTest {

        @Test
        @DisplayName("정상적인 가게 등록 성공")
        void createStore_Success() {
            // Given
            Long currentUserId = 1L;

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);

                when(userRepository.findById(currentUserId)).thenReturn(Optional.of(testUser));
                when(storeRepository.existsByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(false);
                when(storeRepository.save(any(Store.class))).thenReturn(testStore);

                // When
                Store result = storeService.createStore(createRequest);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getName()).isEqualTo("테스트 가게");
                assertThat(result.getOwner()).isEqualTo(testUser);
                assertThat(result.getBusinessNumber()).isEqualTo("123-45-67890");

                verify(userRepository).findById(currentUserId);
                verify(storeRepository).existsByOwnerIdAndIsActiveTrue(currentUserId);
                verify(storeRepository).save(any(Store.class));
            }
        }

        @Test
        @DisplayName("사용자를 찾을 수 없는 경우 예외 발생")
        void createStore_UserNotFound_ThrowsException() {
            // Given
            Long currentUserId = 999L;

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

                // When & Then
                ResponseStatusException exception = assertThrows(
                        ResponseStatusException.class,
                        () -> storeService.createStore(createRequest)
                );

                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                assertThat(exception.getReason()).isEqualTo("인증 실패");
            }
        }

        @Test
        @DisplayName("이미 가게가 등록된 경우 예외 발생")
        void createStore_StoreAlreadyExists_ThrowsException() {
            // Given
            Long currentUserId = 1L;

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(userRepository.findById(currentUserId)).thenReturn(Optional.of(testUser));
                when(storeRepository.existsByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(true);

                // When & Then
                ResponseStatusException exception = assertThrows(
                        ResponseStatusException.class,
                        () -> storeService.createStore(createRequest)
                );

                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                assertThat(exception.getReason()).isEqualTo("이미 등록된 가게가 있음");
            }
        }

        @Test
        @DisplayName("필수 필드가 없는 경우 예외 발생 - 가게명")
        void createStore_MissingName_ThrowsException() {
            // Given
            Long currentUserId = 1L;
            createRequest.setName(null);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(userRepository.findById(currentUserId)).thenReturn(Optional.of(testUser));
                when(storeRepository.existsByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(false);

                // When & Then
                ResponseStatusException exception = assertThrows(
                        ResponseStatusException.class,
                        () -> storeService.createStore(createRequest)
                );

                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(exception.getReason()).isEqualTo("잘못된 요청");
            }
        }

        @Test
        @DisplayName("데이터 무결성 위반 시 예외 발생")
        void createStore_DataIntegrityViolation_ThrowsException() {
            // Given
            Long currentUserId = 1L;

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(userRepository.findById(currentUserId)).thenReturn(Optional.of(testUser));
                when(storeRepository.existsByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(false);
                when(storeRepository.save(any(Store.class))).thenThrow(new DataIntegrityViolationException("중복 사업자등록번호"));

                // When & Then
                ResponseStatusException exception = assertThrows(
                        ResponseStatusException.class,
                        () -> storeService.createStore(createRequest)
                );

                assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(exception.getReason()).isEqualTo("잘못된 요청");
            }
        }
    }

    @Nested
    @DisplayName("내 가게 정보 조회 테스트")
    class GetMyStoreTest {

        @Test
        @DisplayName("가게 정보 조회 성공")
        void getMyStore_Success() {
            // Given
            Long currentUserId = 1L;

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));

                // When
                Store result = storeService.getMyStore();

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getId()).isEqualTo(1L);
                assertThat(result.getName()).isEqualTo("테스트 가게");

                verify(storeRepository).findByOwnerIdAndIsActiveTrue(currentUserId);
            }
        }

        @Test
        @DisplayName("등록된 가게가 없는 경우 null 반환")
        void getMyStore_NoStore_ReturnsNull() {
            // Given
            Long currentUserId = 1L;

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.empty());

                // When
                Store result = storeService.getMyStore();

                // Then
                assertThat(result).isNull();

                verify(storeRepository).findByOwnerIdAndIsActiveTrue(currentUserId);
            }
        }
    }

    @Nested
    @DisplayName("가게 정보 수정 테스트")
    class UpdateStoreTest {

        @Test
        @DisplayName("가게 정보 수정 성공")
        void updateStore_Success() {
            // Given
            Long currentUserId = 1L;
            StoreUpdateRequest updateRequest = new StoreUpdateRequest();
            updateRequest.setName("수정된 가게명");
            updateRequest.setDescription("수정된 설명");
            updateRequest.setCategory(StoreCategory.CHINESE);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeRepository.save(any(Store.class))).thenReturn(testStore);

                // When
                Store result = storeService.updateStore(updateRequest);

                // Then
                assertThat(result).isNotNull();
                verify(storeRepository).save(any(Store.class));
            }
        }

        @Test
        @DisplayName("변경된 정보가 없는 경우")
        void updateStore_NoChanges() {
            // Given
            Long currentUserId = 1L;
            StoreUpdateRequest updateRequest = new StoreUpdateRequest();
            // 모든 필드를 null로 두어 변경사항이 없도록 함

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));

                // When
                Store result = storeService.updateStore(updateRequest);

                // Then
                assertThat(result).isNotNull();
                verify(storeRepository, never()).save(any(Store.class));
            }
        }
    }

    @Nested
    @DisplayName("운영시간 설정 테스트")
    class UpdateStoreHoursTest {

        @Test
        @DisplayName("운영시간 설정 성공")
        void updateStoreHours_Success() {
            // Given
            Long currentUserId = 1L;
            List<StoreHourRequest> hourRequests = new ArrayList<>();

            StoreHourRequest hourRequest = new StoreHourRequest();
            hourRequest.setDayOfWeek(1); // 월요일
            hourRequest.setOpenTime(LocalTime.of(9, 0));
            hourRequest.setCloseTime(LocalTime.of(22, 0));
            hourRequest.setIsClosed(false);
            hourRequests.add(hourRequest);

            StoreHour savedHour = StoreHour.builder()
                    .id(1L)
                    .store(testStore)
                    .dayOfWeek(1)
                    .openTime(LocalTime.of(9, 0))
                    .closeTime(LocalTime.of(22, 0))
                    .isClosed(false)
                    .build();

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeHourRepository.saveAll(anyList())).thenReturn(List.of(savedHour));

                // When
                List<StoreHourResponse> result = storeService.updateStoreHours(hourRequests);

                // Then
                assertThat(result).isNotEmpty();
                assertThat(result).hasSize(1);

                verify(storeHourRepository).deleteByStoreIdAndDayOfWeekIn(eq(1L), any());
                verify(storeHourRepository).saveAll(anyList());
            }
        }

        @Test
        @DisplayName("전체 요일 설정 (dayOfWeek = 7)")
        void updateStoreHours_AllDays_Success() {
            // Given
            Long currentUserId = 1L;
            List<StoreHourRequest> hourRequests = new ArrayList<>();

            StoreHourRequest hourRequest = new StoreHourRequest();
            hourRequest.setDayOfWeek(7); // 모든 요일
            hourRequest.setOpenTime(LocalTime.of(9, 0));
            hourRequest.setCloseTime(LocalTime.of(22, 0));
            hourRequest.setIsClosed(false);
            hourRequests.add(hourRequest);

            List<StoreHour> savedHours = new ArrayList<>();
            for (int day = 0; day <= 6; day++) {
                StoreHour hour = StoreHour.builder()
                        .id((long) (day + 1))
                        .store(testStore)
                        .dayOfWeek(day)
                        .openTime(LocalTime.of(9, 0))
                        .closeTime(LocalTime.of(22, 0))
                        .isClosed(false)
                        .build();
                savedHours.add(hour);
            }

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeHourRepository.saveAll(anyList())).thenReturn(savedHours);

                // When
                List<StoreHourResponse> result = storeService.updateStoreHours(hourRequests);

                // Then
                assertThat(result).hasSize(7); // 7개 요일 모두 생성

                verify(storeHourRepository).deleteByStoreIdAndDayOfWeekIn(eq(1L), any());
                verify(storeHourRepository).saveAll(anyList());
            }
        }
    }

    @Nested
    @DisplayName("영업 상태 변경 테스트")
    class UpdateStoreStatusTest {

        @Test
        @DisplayName("영업 상태 변경 성공")
        void updateStoreStatus_Success() {
            // Given
            Long currentUserId = 1L;
            testStore.setStatus(StoreStatus.CLOSED); // 현재 상태

            StoreStatusRequest statusRequest = new StoreStatusRequest();
            statusRequest.setStatus(StoreStatus.OPEN); // 다른 상태로 변경
            statusRequest.setMessage("영업 시작");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeRepository.save(any(Store.class))).thenReturn(testStore);

                // When
                StoreStatusModifyResponse result = storeService.updateStoreStatus(statusRequest);

                // Then
                assertThat(result).isNotNull();

                verify(storeRepository).save(any(Store.class));
            }
        }

        @Test
        @DisplayName("동일한 상태로 변경 시도 - 저장하지 않고 메시지 반환")
        void updateStoreStatus_SameStatus_ReturnsMessageWithoutSaving() {
            // Given
            Long currentUserId = 1L;
            testStore.setStatus(StoreStatus.CLOSED); // 현재 상태를 CLOSED로 설정

            StoreStatusRequest statusRequest = new StoreStatusRequest();
            statusRequest.setStatus(StoreStatus.CLOSED); // 동일한 상태로 변경 시도
            statusRequest.setMessage("동일한 상태 설정");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));

                // When
                StoreStatusModifyResponse result = storeService.updateStoreStatus(statusRequest);

                // Then
                assertThat(result).isNotNull();
                assertThat(result.getMessage()).contains("이미 동일한 상태입니다");

                // 동일한 상태인 경우 save()가 호출되지 않음
                verify(storeRepository, never()).save(any(Store.class));
            }
        }

        @Test
        @DisplayName("인증되지 않은 사용자인 경우 SecurityUtils에서 예외 발생")
        void updateStoreStatus_UnauthenticatedUser_ThrowsSecurityException() {
            // Given
            StoreStatusRequest statusRequest = new StoreStatusRequest();
            statusRequest.setStatus(null); // 상태를 null로 설정

            // SecurityUtils mocking 없이 실행 - IllegalStateException 발생

            // When & Then
            IllegalStateException exception = assertThrows(
                    IllegalStateException.class, // SecurityUtils에서 발생하는 예외
                    () -> storeService.updateStoreStatus(statusRequest)
            );

            assertThat(exception.getMessage()).isEqualTo("인증된 사용자 정보를 찾을 수 없습니다.");

            // repository 호출이 없어야 함
            verifyNoInteractions(storeRepository);
        }
    }

    @Nested
    @DisplayName("가게 삭제 테스트")
    class DeleteStoreTest {

        @Test
        @DisplayName("가게 삭제(비활성화) 성공")
        void deleteStore_Success() {
            // Given
            Long currentUserId = 1L;

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeRepository.save(any(Store.class))).thenReturn(testStore);

                // When
                assertDoesNotThrow(() -> storeService.deleteStore());

                // Then
                verify(storeRepository).save(any(Store.class));
            }
        }
    }


    @Nested
    @DisplayName("휴무일 관리 테스트")
    class StoreHolidayTest {

        @Test
        @DisplayName("휴무일 등록 성공")
        void createHoliday_Success() {
            // Given
            Long currentUserId = 1L;
            StoreHolidayRequest holidayRequest = new StoreHolidayRequest();
            holidayRequest.setDate(LocalDate.now().plusDays(1));
            holidayRequest.setReason("정기 휴무");
            holidayRequest.setIsRecurring(false);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeHolidayRepository.existsByStoreIdAndDate(testStore.getId(), holidayRequest.getDate())).thenReturn(false);

                StoreHoliday savedHoliday = StoreHoliday.builder()
                        .id(1L)
                        .store(testStore)
                        .date(holidayRequest.getDate())
                        .reason(holidayRequest.getReason())
                        .isRecurring(holidayRequest.getIsRecurring())
                        .build();
                when(storeHolidayRepository.save(any(StoreHoliday.class))).thenReturn(savedHoliday);

                // When
                ResponseEntity<String> result = storeService.createHoliday(holidayRequest);

                // Then
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody()).isEqualTo("휴무일이 성공적으로 등록되었습니다");

                verify(storeHolidayRepository).existsByStoreIdAndDate(testStore.getId(), holidayRequest.getDate());
                verify(storeHolidayRepository).save(any(StoreHoliday.class));
            }
        }

        @Test
        @DisplayName("과거 날짜 휴무일 등록 시 실패")
        void createHoliday_PastDate_Failure() {
            // Given
            Long currentUserId = 1L;
            StoreHolidayRequest holidayRequest = new StoreHolidayRequest();
            holidayRequest.setDate(LocalDate.now().minusDays(1)); // 어제 날짜
            holidayRequest.setReason("과거 휴무");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));

                // When
                ResponseEntity<String> result = storeService.createHoliday(holidayRequest);

                // Then
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(result.getBody()).isEqualTo("과거 날짜는 휴무일로 설정할 수 없습니다");

                verify(storeHolidayRepository, never()).save(any(StoreHoliday.class));
            }
        }

        @Test
        @DisplayName("중복 휴무일 등록 시 실패")
        void createHoliday_DuplicateDate_Failure() {
            // Given
            Long currentUserId = 1L;
            StoreHolidayRequest holidayRequest = new StoreHolidayRequest();
            holidayRequest.setDate(LocalDate.now().plusDays(1));
            holidayRequest.setReason("중복 휴무");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeHolidayRepository.existsByStoreIdAndDate(testStore.getId(), holidayRequest.getDate())).thenReturn(true);

                // When
                ResponseEntity<String> result = storeService.createHoliday(holidayRequest);

                // Then
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(result.getBody()).contains("이미 등록된 휴무일입니다");

                verify(storeHolidayRepository, never()).save(any(StoreHoliday.class));
            }
        }

        @Test
        @DisplayName("휴무일 날짜가 null인 경우 실패")
        void createHoliday_NullDate_Failure() {
            // Given
            Long currentUserId = 1L;
            StoreHolidayRequest holidayRequest = new StoreHolidayRequest();
            holidayRequest.setDate(null); // 날짜를 null로 설정
            holidayRequest.setReason("날짜 없는 휴무");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));

                // When
                ResponseEntity<String> result = storeService.createHoliday(holidayRequest);

                // Then
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(result.getBody()).isEqualTo("휴무일을 입력해주세요");

                verify(storeHolidayRepository, never()).save(any(StoreHoliday.class));
            }
        }

        @Test
        @DisplayName("휴무일 목록 조회 성공")
        void getHolidays_Success() {
            // Given
            Long currentUserId = 1L;
            LocalDate futureDate1 = LocalDate.now().plusDays(7);
            LocalDate futureDate2 = LocalDate.now().plusDays(14);

            List<StoreHoliday> holidays = List.of(
                    StoreHoliday.builder()
                            .id(1L)
                            .store(testStore)
                            .date(futureDate1)
                            .reason("정기 휴무")
                            .isRecurring(false)
                            .build(),
                    StoreHoliday.builder()
                            .id(2L)
                            .store(testStore)
                            .date(futureDate2)
                            .reason("창립기념일")
                            .isRecurring(true)
                            .build()
            );

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeHolidayRepository.findByStoreIdAndDateGreaterThanEqualOrderByDateAsc(
                        eq(testStore.getId()), any(LocalDate.class))).thenReturn(holidays);

                // When
                List<StoreHolidayResponse> result = storeService.getHolidays();

                // Then
                assertThat(result).hasSize(2);
                assertThat(result.get(0).getDate()).isEqualTo(futureDate1);
                assertThat(result.get(0).getReason()).isEqualTo("정기 휴무");
                assertThat(result.get(0).getIsRecurring()).isFalse();
                assertThat(result.get(1).getDate()).isEqualTo(futureDate2);
                assertThat(result.get(1).getReason()).isEqualTo("창립기념일");
                assertThat(result.get(1).getIsRecurring()).isTrue();

                verify(storeHolidayRepository).findByStoreIdAndDateGreaterThanEqualOrderByDateAsc(
                        eq(testStore.getId()), any(LocalDate.class));
            }
        }

        @Test
        @DisplayName("휴무일이 없는 경우 빈 목록 반환")
        void getHolidays_EmptyList() {
            // Given
            Long currentUserId = 1L;

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeHolidayRepository.findByStoreIdAndDateGreaterThanEqualOrderByDateAsc(
                        eq(testStore.getId()), any(LocalDate.class))).thenReturn(new ArrayList<>());

                // When
                List<StoreHolidayResponse> result = storeService.getHolidays();

                // Then
                assertThat(result).isEmpty();

                verify(storeHolidayRepository).findByStoreIdAndDateGreaterThanEqualOrderByDateAsc(
                        eq(testStore.getId()), any(LocalDate.class));
            }
        }

        @Test
        @DisplayName("휴무일 삭제 성공")
        void deleteHoliday_Success() {
            // Given
            Long currentUserId = 1L;
            Long holidayId = 1L;

            StoreHoliday holiday = StoreHoliday.builder()
                    .id(holidayId)
                    .store(testStore)
                    .date(LocalDate.now().plusDays(1))
                    .reason("삭제할 휴무일")
                    .isRecurring(false)
                    .build();

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeHolidayRepository.findById(holidayId)).thenReturn(Optional.of(holiday));

                // When
                ResponseEntity<String> result = storeService.deleteHoliday(holidayId);

                // Then
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody()).isEqualTo("휴무일이 성공적으로 삭제되었습니다");

                verify(storeHolidayRepository).findById(holidayId);
                verify(storeHolidayRepository).delete(holiday);
            }
        }

        @Test
        @DisplayName("존재하지 않는 휴무일 삭제 시 실패")
        void deleteHoliday_NotFound_Failure() {
            // Given
            Long currentUserId = 1L;
            Long holidayId = 999L;

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeHolidayRepository.findById(holidayId)).thenReturn(Optional.empty());

                // When
                ResponseEntity<String> result = storeService.deleteHoliday(holidayId);

                // Then
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                assertThat(result.getBody()).isEqualTo("존재하지 않는 휴무일입니다");

                verify(storeHolidayRepository, never()).delete(any(StoreHoliday.class));
            }
        }

        @Test
        @DisplayName("다른 가게의 휴무일 삭제 시 실패")
        void deleteHoliday_UnauthorizedStore_Failure() {
            // Given
            Long currentUserId = 1L;
            Long holidayId = 1L;

            // 다른 가게의 휴무일 생성
            Store anotherStore = new Store();
            ReflectionTestUtils.setField(anotherStore, "id", 999L);

            StoreHoliday holiday = StoreHoliday.builder()
                    .id(holidayId)
                    .store(anotherStore) // 다른 가게의 휴무일
                    .date(LocalDate.now().plusDays(1))
                    .reason("다른 가게 휴무일")
                    .isRecurring(false)
                    .build();

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeHolidayRepository.findById(holidayId)).thenReturn(Optional.of(holiday));

                // When
                ResponseEntity<String> result = storeService.deleteHoliday(holidayId);

                // Then
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
                assertThat(result.getBody()).isEqualTo("다른 가게의 휴무일은 삭제할 수 없습니다");

                verify(storeHolidayRepository, never()).delete(any(StoreHoliday.class));
            }
        }
    }


    @Nested
    @DisplayName("Response DTO 테스트")
    class ResponseDtoTest {

        @Test
        @DisplayName("StoreStatusModifyResponse 생성 테스트")
        void storeStatusModifyResponse_Creation() {
            // Given
            String message = "영업 상태가 성공적으로 변경되었습니다.";

            // When
            StoreStatusModifyResponse response = StoreStatusModifyResponse.of(testStore, message);

            // Then
            assertThat(response.getStoreId()).isEqualTo(testStore.getId());
            assertThat(response.getStoreName()).isEqualTo(testStore.getName());
            assertThat(response.getStatus()).isEqualTo(testStore.getStatus());
            assertThat(response.getMessage()).isEqualTo(message);
        }

        @Test
        @DisplayName("StoreHolidayResponse 생성 테스트")
        void storeHolidayResponse_Creation() {
            // Given
            StoreHoliday holiday = StoreHoliday.builder()
                    .id(1L)
                    .store(testStore)
                    .date(LocalDate.of(2024, 12, 25))
                    .reason("크리스마스")
                    .isRecurring(true)
                    .build();

            // When
            StoreHolidayResponse response = StoreHolidayResponse.from(holiday);

            // Then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getDate()).isEqualTo(LocalDate.of(2024, 12, 25));
            assertThat(response.getReason()).isEqualTo("크리스마스");
            assertThat(response.getIsRecurring()).isTrue();
        }

        @Test
        @DisplayName("StoreHourResponse 생성 테스트")
        void storeHourResponse_Creation() {
            // Given
            StoreHour storeHour = StoreHour.builder()
                    .id(1L)
                    .store(testStore)
                    .dayOfWeek(1)
                    .openTime(LocalTime.of(9, 0))
                    .closeTime(LocalTime.of(22, 0))
                    .isClosed(false)
                    .build();

            // When
            StoreHourResponse response = StoreHourResponse.from(storeHour);

            // Then
            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getDayOfWeek()).isEqualTo(1);
            assertThat(response.getOpenTime()).isEqualTo(LocalTime.of(9, 0));
            assertThat(response.getCloseTime()).isEqualTo(LocalTime.of(22, 0));
            assertThat(response.getIsClosed()).isFalse();
        }
    }

    @Nested
    @DisplayName("배달 정보 수정 테스트")
    class UpdateDeliveryTest {

        @Test
        @DisplayName("배달 정보 수정 성공")
        void updateDelivery_Success() {
            // Given
            Long currentUserId = 1L;
            StoreDeliveryRequest deliveryRequest = new StoreDeliveryRequest();
            deliveryRequest.setDeliveryFee(new BigDecimal("2500"));
            deliveryRequest.setMinOrderAmount(new BigDecimal("12000"));
            deliveryRequest.setDeliveryTimeMin(25);
            deliveryRequest.setDeliveryTimeMax(45);

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeRepository.save(any(Store.class))).thenReturn(testStore);

                // When
                Store result = storeService.updateDelivery(deliveryRequest);

                // Then
                assertThat(result).isNotNull();
                verify(storeRepository).save(any(Store.class));
            }
        }
    }

    @Nested
    @DisplayName("연락처 정보 수정 테스트")
    class UpdateContactTest {

        @Test
        @DisplayName("연락처 정보 수정 성공")
        void updateContact_Success() {
            // Given
            Long currentUserId = 1L;
            StoreContactRequest contactRequest = new StoreContactRequest();
            contactRequest.setPhone("02-9999-8888");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeRepository.save(any(Store.class))).thenReturn(testStore);

                // When
                Store result = storeService.updateContact(contactRequest);

                // Then
                assertThat(result).isNotNull();
                verify(storeRepository).save(any(Store.class));
            }
        }

        @Test
        @DisplayName("변경된 연락처 정보가 없는 경우")
        void updateContact_NoChanges() {
            // Given
            Long currentUserId = 1L;
            StoreContactRequest contactRequest = new StoreContactRequest();
            contactRequest.setPhone(testStore.getPhone()); // 동일한 연락처

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));

                // When
                Store result = storeService.updateContact(contactRequest);

                // Then
                assertThat(result).isNotNull();
                verify(storeRepository, never()).save(any(Store.class));
            }
        }
    }

    @Nested
    @DisplayName("위치 정보 수정 테스트")
    class UpdateLocationTest {

        @Test
        @DisplayName("위치 정보 수정 성공")
        void updateLocation_Success() {
            // Given
            Long currentUserId = 1L;
            StoreLocationRequest locationRequest = new StoreLocationRequest();
            locationRequest.setAddress("서울시 서초구");
            locationRequest.setDetailAddress("456번지");

            try (MockedStatic<SecurityUtils> securityUtilsMock = mockStatic(SecurityUtils.class)) {
                securityUtilsMock.when(SecurityUtils::getCurrentUserId).thenReturn(currentUserId);
                when(storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)).thenReturn(Optional.of(testStore));
                when(storeRepository.save(any(Store.class))).thenReturn(testStore);

                // When
                Store result = storeService.updateLocation(locationRequest);

                // Then
                assertThat(result).isNotNull();
                verify(storeRepository).save(any(Store.class));
            }
        }
    }


}
