package io.goorm.team02.core.stores.controller;

import io.goorm.team02.core.stores.controller.dto.StoreContactRequest;
import io.goorm.team02.core.stores.controller.dto.StoreCreateRequest;
import io.goorm.team02.core.stores.controller.dto.StoreDeliveryRequest;
import io.goorm.team02.core.stores.controller.dto.StoreHolidayRequest;
import io.goorm.team02.core.stores.controller.dto.StoreHourRequest;
import io.goorm.team02.core.stores.controller.dto.StoreLocationRequest;
import io.goorm.team02.core.stores.controller.dto.StoreStatusRequest;
import io.goorm.team02.core.stores.controller.dto.StoreUpdateRequest;
import io.goorm.team02.core.stores.domain.Store;
import io.goorm.team02.core.stores.domain.StoreHour;
import io.goorm.team02.core.stores.service.StoreService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/owner/store")
@RequiredArgsConstructor
public class StoreController {

    private final StoreService storeService;

    @GetMapping("/test")
    public String test() {
        return "StoreController is working!";
    }

    // ================================
    // 2.1 가게 기본 정보
    // ================================

    /**
     * 가게 등록 (최초 1회)
     */
    @PostMapping
    public Store createStore(@RequestBody StoreCreateRequest request) {
        return storeService.createStore(request);
    }

    /**
     * 내 가게 정보 조회
     */
    @GetMapping
    public Store getMyStore() {
        return storeService.getMyStore();
    }

    /**
     * 가게 기본 정보 수정
     */
    @PutMapping
    public Store updateStore(@RequestBody StoreUpdateRequest request) {
        return storeService.updateStore(request);
    }

    /**
     * 가게 삭제 (비활성화)
     */
    @DeleteMapping
    public void deleteStore() {
        storeService.deleteStore();
    }

    // ================================
    // 2.2 가게 상세 설정
    // ================================
    /**
     * 연락처 정보 수정
     */
    @PutMapping("/contact")
    public Store updateContact(@RequestBody StoreContactRequest request) {
        return storeService.updateContact(request);
    }

     /**
     * 배달 설정 (배달비, 최소주문금액)
     */
    @PutMapping("/delivery")
    public Store updateDelivery(@RequestBody StoreDeliveryRequest request) {
        return storeService.updateDelivery(request);
    }

    /**
     * 주소 및 배달 가능 지역 설정
     */
    @PutMapping("/location")
    public Store updateLocation(@RequestBody StoreLocationRequest request) {
        return storeService.updateLocation(request);
    }

    /**
     * 가게 이미지 업로드
     */
    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "가게 이미지 업로드", description = "가게 대표 이미지를 업로드합니다")
    public ResponseEntity<String> uploadImage(
        @Parameter(description = "업로드할 이미지 파일", required = true)
        @RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("파일이 비어있습니다");
        }

        String imageUrl = storeService.uploadImage(file);
        return ResponseEntity.ok(imageUrl);
    }

    /**
     * 가게 이미지 삭제
     */
    @DeleteMapping("/images/{id}")
    public void deleteImage(@PathVariable Long id) {
        storeService.deleteImage(id);
    }

//    // ================================
//    // 2.3 운영시간 관리
//    // ================================
//
//    /**
//     * 운영시간 조회
//     */
//    @GetMapping("/hours")
//    public List<StoreHour> getStoreHours() {
//        return storeService.getStoreHours();
//    }
//
//    /**
//     * 운영시간 설정 (요일별 일괄)
//     */
//    @PutMapping("/hours")
//    public List<StoreHour> updateStoreHours(@RequestBody List<StoreHourRequest> requests) {
//        return storeService.updateStoreHours(requests);
//    }
//
//    /**
//     * 특정 요일 운영시간 수정
//     */
//    @PutMapping("/hours/{day}")
//    public StoreHour updateStoreHour(@PathVariable Integer day, @RequestBody StoreHourRequest request) {
//        return storeService.updateStoreHour(day, request);
//    }
//
//    /**
//     * 휴무일 등록
//     */
//    @PostMapping("/holidays")
//    public void createHoliday(@RequestBody StoreHolidayRequest request) {
//        storeService.createHoliday(request);
//    }
//
//    /**
//     * 휴무일 목록 조회
//     */
//    @GetMapping("/holidays")
//    public List<StoreHolidayRequest> getHolidays() {
//        return storeService.getHolidays();
//    }
//
//    /**
//     * 휴무일 삭제
//     */
//    @DeleteMapping("/holidays/{id}")
//    public void deleteHoliday(@PathVariable Long id) {
//        storeService.deleteHoliday(id);
//    }
//
//    // ================================
//    // 2.4 영업 상태 관리
//    // ================================
//
//    /**
//     * 현재 영업 상태 조회
//     */
//    @GetMapping("/status")
//    public Store getStoreStatus() {
//        return storeService.getStoreStatus();
//    }
//
//    /**
//     * 영업 상태 변경
//     */
//    @PutMapping("/status")
//    public Store updateStoreStatus(@RequestBody StoreStatusRequest request) {
//        return storeService.updateStoreStatus(request);
//    }
//
//    /**
//     * 영업 상태 변경 이력
//     */
//    @GetMapping("/status/history")
//    public List<String> getStatusHistory() {
//        return storeService.getStatusHistory();
//    }
}