package io.goorm.team02.owner.stores.service;

import io.goorm.team02.core.owner.common.service.S3Service;
import io.goorm.team02.core.owner.stores.client.OrderServiceClient;
import io.goorm.team02.core.owner.stores.domain.Store;
import io.goorm.team02.core.owner.stores.domain.StoreHoliday;
import io.goorm.team02.core.owner.stores.domain.StoreHour;
import io.goorm.team02.core.owner.stores.domain.enums.StoreCategory;
import io.goorm.team02.core.owner.stores.domain.enums.StoreStatus;
import io.goorm.team02.core.owner.stores.events.ImageCleanupEvent;
import io.goorm.team02.core.owner.stores.mapper.StoreMapper;
import io.goorm.team02.core.owner.stores.repository.StoreHolidayRepository;
import io.goorm.team02.core.owner.stores.repository.StoreHourRepository;
import io.goorm.team02.core.owner.stores.repository.StoreRepository;
import io.goorm.team02.core.owner.stores.service.StoreService;
import io.goorm.team02.dto.orders.OrderDashboardDto;
import io.goorm.team02.dto.orders.OrderItemDto;
import io.goorm.team02.dto.orders.RecentOrderDto;
import io.goorm.team02.dto.owner.stores.dashboard.StoreDashboardResponse;
import io.goorm.team02.dto.owner.stores.storemanagement.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StoreService 단위 테스트")
class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private StoreHourRepository storeHourRepository;

    @Mock
    private StoreHolidayRepository storeHolidayRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private StoreMapper storeMapper;

    @Mock
    private OrderServiceClient orderServiceClient;

    @InjectMocks
    private StoreService storeService;

    private Store testStore;
    private StoreCreateRequest createRequest;
    private StoreResponse storeResponse;
    private Long testUserId = 1L;

    @BeforeEach
    void setUp() {
        testStore = createTestStore();
        createRequest = createTestCreateRequest();
        storeResponse = createTestStoreResponse();
    }

    @Nested
    @DisplayName("가게 등록 테스트")
    class CreateStoreTests {

        @Test
        @DisplayName("가게 등록 성공")
        void createStore_Success() {
            // Given
            when(storeRepository.existsByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(false);
            doNothing().when(storeMapper).validateStoreCreateRequest(createRequest);
            when(storeMapper.convertStringToStoreCategory(createRequest.getCategory())).thenReturn(StoreCategory.KOREAN);
            when(storeRepository.save(any(Store.class))).thenReturn(testStore);
            when(storeMapper.toStoreResponse(testStore)).thenReturn(storeResponse);

            // When
            StoreResponse result = storeService.createStore(testUserId, createRequest);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testStore.getId());
            assertThat(result.getName()).isEqualTo(testStore.getName());

            verify(storeRepository).existsByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeMapper).validateStoreCreateRequest(createRequest);
            verify(storeRepository).save(any(Store.class));
            verify(storeMapper).toStoreResponse(testStore);
        }

        @Test
        @DisplayName("가게 등록 실패 - 이미 가게가 존재함")
        void createStore_Fail_AlreadyExists() {
            // Given
            when(storeRepository.existsByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(true);

            // When & Then
            assertThatThrownBy(() -> storeService.createStore(testUserId, createRequest))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("이미 등록된 가게가 있습니다");

            verify(storeRepository).existsByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeRepository, never()).save(any(Store.class));
        }

        @Test
        @DisplayName("가게 등록 실패 - 데이터 무결성 위반")
        void createStore_Fail_DataIntegrityViolation() {
            // Given
            when(storeRepository.existsByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(false);
            doNothing().when(storeMapper).validateStoreCreateRequest(createRequest);
            when(storeMapper.convertStringToStoreCategory(createRequest.getCategory())).thenReturn(StoreCategory.KOREAN);
            when(storeRepository.save(any(Store.class))).thenThrow(new DataIntegrityViolationException("Duplicate entry"));

            // When & Then
            assertThatThrownBy(() -> storeService.createStore(testUserId, createRequest))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("중복된 사업자등록번호입니다");
        }
    }

    @Nested
    @DisplayName("가게 조회 테스트")
    class GetStoreTests {

        @Test
        @DisplayName("내 가게 정보 조회 성공")
        void getMyStore_Success() {
            // Given
            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeMapper.toStoreResponse(testStore)).thenReturn(storeResponse);

            // When
            StoreResponse result = storeService.getMyStore(testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(testStore.getId());
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeMapper).toStoreResponse(testStore);
        }

        @Test
        @DisplayName("내 가게 정보 조회 - 가게 없음")
        void getMyStore_NotFound() {
            // Given
            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.empty());

            // When
            StoreResponse result = storeService.getMyStore(testUserId);

            // Then
            assertThat(result).isNull();
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeMapper, never()).toStoreResponse(any());
        }
    }

    @Nested
    @DisplayName("가게 정보 수정 테스트")
    class UpdateStoreTests {

        @Test
        @DisplayName("가게 정보 수정 성공")
        void updateStore_Success() {
            // Given
            StoreUpdateRequest updateRequest = new StoreUpdateRequest();
            updateRequest.setName("수정된 가게명");
            updateRequest.setDescription("수정된 설명");

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            doNothing().when(storeMapper).validateStoreUpdateRequest(updateRequest);
            when(storeRepository.save(testStore)).thenReturn(testStore);
            when(storeMapper.toStoreResponse(testStore)).thenReturn(storeResponse);

            // When
            StoreResponse result = storeService.updateStore(testUserId, updateRequest);

            // Then
            assertThat(result).isNotNull();
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeMapper).validateStoreUpdateRequest(updateRequest);
            verify(storeRepository).save(testStore);
        }

        @Test
        @DisplayName("연락처 정보 수정 성공")
        void updateContact_Success() {
            // Given
            StoreContactRequest contactRequest = new StoreContactRequest();
            contactRequest.setPhone("02-9999-8888");

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeRepository.save(testStore)).thenReturn(testStore);
            when(storeMapper.toStoreResponse(testStore)).thenReturn(storeResponse);

            // When
            StoreResponse result = storeService.updateContact(testUserId, contactRequest);

            // Then
            assertThat(result).isNotNull();
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeRepository).save(testStore);
        }

        @Test
        @DisplayName("배달 정보 수정 성공")
        void updateDelivery_Success() {
            // Given
            StoreDeliveryRequest deliveryRequest = new StoreDeliveryRequest();
            deliveryRequest.setDeliveryFee(new BigDecimal("3000"));
            deliveryRequest.setMinOrderAmount(new BigDecimal("20000"));

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeRepository.save(testStore)).thenReturn(testStore);
            when(storeMapper.toStoreResponse(testStore)).thenReturn(storeResponse);

            // When
            StoreResponse result = storeService.updateDelivery(testUserId, deliveryRequest);

            // Then
            assertThat(result).isNotNull();
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeRepository).save(testStore);
        }

        @Test
        @DisplayName("위치 정보 수정 성공")
        void updateLocation_Success() {
            // Given
            StoreLocationRequest locationRequest = new StoreLocationRequest();
            locationRequest.setAddress("서울시 서초구 강남대로 123");
            locationRequest.setDetailAddress("2층 201호");

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeRepository.save(testStore)).thenReturn(testStore);
            when(storeMapper.toStoreResponse(testStore)).thenReturn(storeResponse);

            // When
            StoreResponse result = storeService.updateLocation(testUserId, locationRequest);

            // Then
            assertThat(result).isNotNull();
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeRepository).save(testStore);
        }
    }

    @Nested
    @DisplayName("가게 삭제 테스트")
    class DeleteStoreTests {

        @Test
        @DisplayName("가게 삭제 성공")
        void deleteStore_Success() {
            // Given
            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeRepository.save(testStore)).thenReturn(testStore);

            // When
            storeService.deleteStore(testUserId);

            // Then
            assertThat(testStore.getIsActive()).isFalse();
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeRepository).save(testStore);
        }

        @Test
        @DisplayName("가게 삭제 실패 - 가게 없음")
        void deleteStore_Fail_NotFound() {
            // Given
            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> storeService.deleteStore(testUserId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("가게를 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("이미지 관리 테스트")
    class ImageManagementTests {

        @Mock
        private MultipartFile mockFile;

        @Test
        @DisplayName("이미지 업로드 성공")
        void uploadImage_Success() {
            // Given
            String tempImageUrl = "https://bucket.s3.region.amazonaws.com/temp/1/image.jpg";
            String finalImageUrl = "https://bucket.s3.region.amazonaws.com/store/1/image.jpg";

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(s3Service.uploadFileWithTransaction(mockFile, testStore.getId())).thenReturn(tempImageUrl);
            when(s3Service.moveToFinalPath(tempImageUrl, testStore.getId())).thenReturn(finalImageUrl);
            when(storeRepository.save(testStore)).thenReturn(testStore);

            // When
            String result = storeService.uploadImage(testUserId, mockFile);

            // Then
            assertThat(result).isEqualTo(finalImageUrl);
            assertThat(testStore.getImageUrl()).isEqualTo(finalImageUrl);
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(s3Service).uploadFileWithTransaction(mockFile, testStore.getId());
            verify(s3Service).moveToFinalPath(tempImageUrl, testStore.getId());
            verify(storeRepository).save(testStore);
        }

        @Test
        @DisplayName("이미지 삭제 성공")
        void deleteImage_Success() {
            // Given
            String currentImageUrl = "https://bucket.s3.region.amazonaws.com/store/1/image.jpg";
            testStore.setImageUrl(currentImageUrl);

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeRepository.save(testStore)).thenReturn(testStore);

            // When
            storeService.deleteImage(testUserId, 1L);

            // Then
            assertThat(testStore.getImageUrl()).isNull();
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeRepository).save(testStore);
            verify(eventPublisher).publishEvent(any(ImageCleanupEvent.class));
        }
    }

    @Nested
    @DisplayName("운영시간 관리 테스트")
    class StoreHourTests {

        @Test
        @DisplayName("운영시간 조회 성공")
        void getStoreHours_Success() {
            // Given
            List<StoreHour> storeHours = createTestStoreHours();
            List<StoreHourResponse> storeHourResponses = createTestStoreHourResponses();
            testStore.setStoreHours(storeHours);

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeMapper.toHourResponseList(storeHours)).thenReturn(storeHourResponses);

            // When
            List<StoreHourResponse> result = storeService.getStoreHours(testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(storeHourResponses.size());
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeMapper).toHourResponseList(storeHours);
        }

        @Test
        @DisplayName("운영시간 설정 성공")
        void updateStoreHours_Success() {
            // Given
            List<StoreHourRequest> requests = createTestStoreHourRequests();
            List<StoreHour> savedHours = createTestStoreHours();
            List<StoreHourResponse> responses = createTestStoreHourResponses();

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeHourRepository.saveAll(anyList())).thenReturn(savedHours);
            when(storeMapper.toHourResponseList(savedHours)).thenReturn(responses);

            // When
            List<StoreHourResponse> result = storeService.updateStoreHours(testUserId, requests);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(responses.size());
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeHourRepository).deleteByStoreIdAndDayOfWeekIn(eq(testStore.getId()), any());
            verify(storeHourRepository).saveAll(anyList());
        }
    }

    @Nested
    @DisplayName("휴무일 관리 테스트")
    class HolidayTests {

        @Test
        @DisplayName("휴무일 등록 성공")
        void createHoliday_Success() {
            // Given
            StoreHolidayRequest request = new StoreHolidayRequest();
            request.setDate(LocalDate.now().plusDays(1));
            request.setReason("정기휴무");
            request.setIsRecurring(false);

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeHolidayRepository.existsByStoreIdAndDate(testStore.getId(), request.getDate())).thenReturn(false);
            when(storeHolidayRepository.save(any(StoreHoliday.class))).thenReturn(new StoreHoliday());

            // When
            ResponseEntity<String> result = storeService.createHoliday(testUserId, request);

            // Then
            assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(result.getBody()).contains("성공적으로 등록되었습니다");
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeHolidayRepository).existsByStoreIdAndDate(testStore.getId(), request.getDate());
            verify(storeHolidayRepository).save(any(StoreHoliday.class));
        }

        @Test
        @DisplayName("휴무일 등록 실패 - 중복된 날짜")
        void createHoliday_Fail_Duplicate() {
            // Given
            StoreHolidayRequest request = new StoreHolidayRequest();
            request.setDate(LocalDate.now().plusDays(1));

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeHolidayRepository.existsByStoreIdAndDate(testStore.getId(), request.getDate())).thenReturn(true);

            // When
            ResponseEntity<String> result = storeService.createHoliday(testUserId, request);

            // Then
            assertThat(result.getStatusCode().is4xxClientError()).isTrue();
            assertThat(result.getBody()).contains("이미 등록된 휴무일입니다");
        }

        @Test
        @DisplayName("휴무일 조회 성공")
        void getHolidays_Success() {
            // Given
            List<StoreHoliday> holidays = createTestStoreHolidays();
            List<StoreHolidayResponse> responses = createTestStoreHolidayResponses();

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeHolidayRepository.findByStoreIdAndDateGreaterThanEqualOrderByDateAsc(eq(testStore.getId()), any(LocalDate.class)))
                    .thenReturn(holidays);
            when(storeMapper.toHolidayResponseList(holidays)).thenReturn(responses);

            // When
            List<StoreHolidayResponse> result = storeService.getHolidays(testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(responses.size());
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeHolidayRepository).findByStoreIdAndDateGreaterThanEqualOrderByDateAsc(eq(testStore.getId()), any(LocalDate.class));
        }

        @Test
        @DisplayName("휴무일 삭제 성공")
        void deleteHoliday_Success() {
            // Given
            Long holidayId = 1L;
            StoreHoliday holiday = createTestStoreHoliday();

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeHolidayRepository.findByIdAndStoreId(holidayId, testStore.getId())).thenReturn(Optional.of(holiday));

            // When
            ResponseEntity<String> result = storeService.deleteHoliday(testUserId, holidayId);

            // Then
            assertThat(result.getStatusCode().is2xxSuccessful()).isTrue();
            assertThat(result.getBody()).contains("성공적으로 삭제되었습니다");
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeHolidayRepository).findByIdAndStoreId(holidayId, testStore.getId());
            verify(storeHolidayRepository).delete(holiday);
        }
    }

    @Nested
    @DisplayName("가게 상태 관리 테스트")
    class StoreStatusTests {

        @Test
        @DisplayName("가게 상태 조회 성공")
        void getStoreStatus_Success() {
            // Given
            StoreStatusResponse statusResponse = createTestStoreStatusResponse();

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeMapper.toStoreStatusResponse(eq(testStore), anyBoolean(), anyString())).thenReturn(statusResponse);

            // When
            StoreStatusResponse result = storeService.getStoreStatus(testUserId);

            // Then
            assertThat(result).isNotNull();
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeMapper).toStoreStatusResponse(eq(testStore), anyBoolean(), anyString());
        }

        @Test
        @DisplayName("가게 상태 변경 성공")
        void updateStoreStatus_Success() {
            // Given
            StoreStatusRequest request = new StoreStatusRequest();
            request.setStatus("OPEN");
            request.setMessage("정상 영업 시작");

            // 가게의 현재 상태를 CLOSED로 설정 (다른 상태로 변경되도록)
            testStore.setStatus(StoreStatus.CLOSED);

            StoreStatusModifyResponse modifyResponse = new StoreStatusModifyResponse(
                    testStore.getId(), testStore.getName(), "OPEN", "영업 상태가 성공적으로 변경되었습니다.");

            when(storeMapper.validateAndConvertStoreStatus(request)).thenReturn(StoreStatus.OPEN);
            doNothing().when(storeMapper).validateStatusChangeMessage(request);
            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeRepository.save(testStore)).thenReturn(testStore);
            when(storeMapper.toStatusModifyResponse(testStore, "영업 상태가 성공적으로 변경되었습니다."))
                    .thenReturn(modifyResponse);

            // When
            StoreStatusModifyResponse result = storeService.updateStoreStatus(testUserId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("OPEN");
            verify(storeMapper).validateAndConvertStoreStatus(request);
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeRepository).save(testStore);
        }

        // 동일한 상태로 변경하는 경우에 대한 별도 테스트 추가
        @Test
        @DisplayName("가게 상태 변경 - 동일한 상태")
        void updateStoreStatus_SameStatus() {
            // Given
            StoreStatusRequest request = new StoreStatusRequest();
            request.setStatus("OPEN");
            request.setMessage("정상 영업 시작");

            // 가게의 현재 상태를 OPEN으로 설정 (동일한 상태)
            testStore.setStatus(StoreStatus.OPEN);

            StoreStatusModifyResponse modifyResponse = new StoreStatusModifyResponse(
                    testStore.getId(), testStore.getName(), "OPEN", "이미 동일한 상태입니다.");

            when(storeMapper.validateAndConvertStoreStatus(request)).thenReturn(StoreStatus.OPEN);
            doNothing().when(storeMapper).validateStatusChangeMessage(request);
            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(storeMapper.toStatusModifyResponse(testStore, "이미 동일한 상태입니다."))
                    .thenReturn(modifyResponse);

            // When
            StoreStatusModifyResponse result = storeService.updateStoreStatus(testUserId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo("OPEN");
            assertThat(result.getMessage()).isEqualTo("이미 동일한 상태입니다.");
            verify(storeMapper).validateAndConvertStoreStatus(request);
            verify(storeRepository).findByOwnerIdAndIsActiveTrue(testUserId);
            verify(storeRepository, never()).save(testStore); // save는 호출되지 않아야 함
        }
    }

    @Nested
    @DisplayName("대시보드 조회 테스트")
    class DashboardTests {

        @Test
        @DisplayName("대시보드 조회 성공")
        void getDashboard_Success() {
            // Given
            OrderDashboardDto orderData = createTestOrderDashboardDto();
            StoreDashboardResponse expectedResponse = createTestStoreDashboardResponse();

            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.of(testStore));
            when(orderServiceClient.getDashboardData(testStore.getId())).thenReturn(orderData);

            // When
            StoreDashboardResponse result = storeService.getDashboard(testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.todayStats()).isNotNull();
            assertThat(result.restaurant()).isNotNull();
            verify(orderServiceClient).getDashboardData(testStore.getId());
        }

        @Test
        @DisplayName("대시보드 조회 실패 - 가게 없음")
        void getDashboard_Fail_StoreNotFound() {
            // Given
            when(storeRepository.findByOwnerIdAndIsActiveTrue(testUserId)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> storeService.getDashboard(testUserId))
                    .isInstanceOf(ResponseStatusException.class)
                    .hasMessageContaining("가게를 찾을 수 없습니다");
        }
    }

    // ================================
    // Helper Methods for Test Data
    // ================================

    private Store createTestStore() {
        Store store = new Store();
        store.setId(1L);
        store.setOwnerId(testUserId);
        store.setBusinessNumber("123-45-67890");
        store.setName("테스트 가게");
        store.setDescription("테스트 설명");
        store.setPhone("02-1234-5678");
        store.setAddress("서울시 강남구 테헤란로 123");
        store.setDetailAddress("1층 101호");
        store.setLatitude(new BigDecimal("37.5665"));
        store.setLongitude(new BigDecimal("126.9780"));
        store.setCategory(StoreCategory.KOREAN);
        store.setMinOrderAmount(new BigDecimal("15000"));
        store.setDeliveryFee(new BigDecimal("2000"));
        store.setDeliveryTimeMin(30);
        store.setDeliveryTimeMax(45);
        store.setStatus(StoreStatus.OPEN);
        store.setRating(new BigDecimal("4.5"));
        store.setReviewCount(100);
        store.setIsActive(true);
        store.setStoreHours(new ArrayList<>());
        return store;
    }

    private StoreCreateRequest createTestCreateRequest() {
        StoreCreateRequest request = new StoreCreateRequest();
        request.setBusinessNumber("123-45-67890");
        request.setName("테스트 가게");
        request.setDescription("테스트 설명");
        request.setPhone("02-1234-5678");
        request.setAddress("서울시 강남구 테헤란로 123");
        request.setDetailAddress("1층 101호");
        request.setLatitude(new BigDecimal("37.5665"));
        request.setLongitude(new BigDecimal("126.9780"));
        request.setCategory("KOREAN");
        request.setMinOrderAmount(new BigDecimal("15000"));
        request.setDeliveryFee(new BigDecimal("2000"));
        request.setDeliveryTimeMin(30);
        request.setDeliveryTimeMax(45);
        return request;
    }

    private StoreResponse createTestStoreResponse() {
        return StoreResponse.builder()
                .id(1L)
                .businessNumber("123-**-***90")
                .name("테스트 가게")
                .description("테스트 설명")
                .phone("02-1234-5678")
                .address("서울시 강남구 테헤란로 123")
                .detailAddress("1층 101호")
                .latitude(new BigDecimal("37.5665"))
                .longitude(new BigDecimal("126.9780"))
                .category("KOREAN")
                .minOrderAmount(new BigDecimal("15000"))
                .deliveryFee(new BigDecimal("2000"))
                .deliveryTimeMin(30)
                .deliveryTimeMax(45)
                .status("OPEN")
                .rating(new BigDecimal("4.5"))
                .reviewCount(100)
                .isActive(true)
                .build();
    }

    private List<StoreHour> createTestStoreHours() {
        return List.of(
                StoreHour.builder()
                        .id(1L)
                        .store(testStore)
                        .dayOfWeek(1)
                        .openTime(LocalTime.of(9, 0))
                        .closeTime(LocalTime.of(22, 0))
                        .isClosed(false)
                        .build()
        );
    }

    private List<StoreHourResponse> createTestStoreHourResponses() {
        return List.of(
                StoreHourResponse.builder()
                        .id(1L)
                        .dayOfWeek(1)
                        .openTime(LocalTime.of(9, 0))
                        .closeTime(LocalTime.of(22, 0))
                        .isClosed(false)
                        .build()
        );
    }

    private List<StoreHourRequest> createTestStoreHourRequests() {
        StoreHourRequest request = new StoreHourRequest();
        request.setDayOfWeek(1);
        request.setOpenTime(LocalTime.of(9, 0));
        request.setCloseTime(LocalTime.of(22, 0));
        request.setIsClosed(false);
        return List.of(request);
    }

    private List<StoreHoliday> createTestStoreHolidays() {
        return List.of(
                StoreHoliday.builder()
                        .id(1L)
                        .store(testStore)
                        .date(LocalDate.now().plusDays(1))
                        .reason("정기휴무")
                        .isRecurring(false)
                        .build()
        );
    }

    private List<StoreHolidayResponse> createTestStoreHolidayResponses() {
        return List.of(
                StoreHolidayResponse.builder()
                        .id(1L)
                        .date(LocalDate.now().plusDays(1))
                        .reason("정기휴무")
                        .isRecurring(false)
                        .build()
        );
    }

    private StoreHoliday createTestStoreHoliday() {
        return StoreHoliday.builder()
                .id(1L)
                .store(testStore)
                .date(LocalDate.now().plusDays(1))
                .reason("정기휴무")
                .isRecurring(false)
                .build();
    }

    private StoreStatusResponse createTestStoreStatusResponse() {
        return StoreStatusResponse.builder()
                .storeId(1L)
                .storeName("테스트 가게")
                .status("OPEN")
                .isActive(true)
                .isCurrentlyOpen(true)
                .currentDayStatus("09:00 - 22:00 영업중")
                .lastUpdated(LocalDateTime.now())
                .build();
    }

    private OrderDashboardDto createTestOrderDashboardDto() {
        return OrderDashboardDto.builder()
                .todayOrderCount(15L)
                .todayRevenue(new BigDecimal("450000"))
                .totalOrderCount(1234L)
                .averageRating(new BigDecimal("4.5"))
                .reviewCount(127L)
                .recentOrders(createTestRecentOrders())
                .build();
    }

    private List<RecentOrderDto> createTestRecentOrders() {
        return List.of(
                RecentOrderDto.builder()
                        .id(1L)
                        .orderNumber("ORD-20241211-001")
                        .customerName("김철수")
                        .total(new BigDecimal("25000"))
                        .status("PENDING")
                        .orderTime(LocalDateTime.now())
                        .items(createTestOrderItems())
                        .build()
        );
    }

    private List<OrderItemDto> createTestOrderItems() {
        return List.of(
                OrderItemDto.builder()
                        .name("양념치킨")
                        .quantity(2)
                        .price(new BigDecimal("18000"))
                        .subtotal(new BigDecimal("36000"))
                        .options("순한맛")
                        .build()
        );
    }

    private StoreDashboardResponse createTestStoreDashboardResponse() {
        return new StoreDashboardResponse(
                new StoreDashboardResponse.TodayStats(15L, new BigDecimal("450000")),
                new StoreDashboardResponse.RestaurantInfo(1L, "테스트 가게", new BigDecimal("4.5"), 127L, 1234L),
                List.of(new StoreDashboardResponse.StoreHourInfo(1, "09:00:00", "22:00:00", false)),
                List.of(new StoreDashboardResponse.RecentOrderInfo(
                        1L, "ORD-20241211-001", "김철수", new BigDecimal("25000"),
                        "PENDING", LocalDateTime.now().toString(),
                        List.of(new StoreDashboardResponse.OrderItemInfo("양념치킨", 2))
                ))
        );
    }
}