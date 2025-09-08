package io.goorm.team02.core.menus.service;

import io.goorm.team02.core.menus.controller.dto.categorycreate.CategoryMoveRequest;
import io.goorm.team02.core.menus.controller.dto.categorycreate.CategoryOrderUpdateRequest;
import io.goorm.team02.core.menus.controller.dto.categorycreate.MenuCategoryCreateRequest;
import io.goorm.team02.core.menus.controller.dto.categorycreate.MenuCategoryUpdateRequest;
import io.goorm.team02.core.menus.controller.dto.menucreate.MenuCreateRequest;
import io.goorm.team02.core.menus.controller.dto.menucreate.MenuOptionCreateRequest;
import io.goorm.team02.core.menus.controller.dto.menucreate.MenuOptionItemCreateRequest;
import io.goorm.team02.core.menus.domain.Menu;
import io.goorm.team02.core.menus.domain.MenuCategory;
import io.goorm.team02.core.menus.domain.MenuOption;
import io.goorm.team02.core.menus.domain.MenuOptionItem;
import io.goorm.team02.core.menus.domain.enums.MenuStatus;
import io.goorm.team02.core.menus.repository.MenuCategoryRepository;
import io.goorm.team02.core.menus.repository.MenuOptionItemRepository;
import io.goorm.team02.core.menus.repository.MenuOptionRepository;
import io.goorm.team02.core.menus.repository.MenuRepository;
import io.goorm.team02.core.stores.domain.Store;
import io.goorm.team02.core.stores.repository.StoreRepository;
//import io.goorm.team02.core.stores.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class MenuService {

    private final MenuRepository menuRepository;
    private final MenuCategoryRepository menuCategoryRepository;
    private final MenuOptionRepository menuOptionRepository;      // 추가
    private final MenuOptionItemRepository menuOptionItemRepository; // 추가
    private final StoreRepository storeRepository;
//    private final UserRepository userRepository;

    /**
     * 현재 로그인한 사용자 ID 조회
     */
    private Long getCurrentUserId() {
        // TODO: Spring Security Context에서 현재 사용자 ID 조회
        // 임시로 1L 반환
        return 1L;
    }
    /**
     * 메뉴 카테고리 등록
     */
    @Transactional
    public MenuCategory createCategory(MenuCategoryCreateRequest request) {
        log.info("=== 메뉴 카테고리 등록 시작 ===");

        // 현재 로그인한 사용자 ID 가져오기
        Long currentUserId = getCurrentUserId();
        log.info("현재 사용자 ID: {}", currentUserId);

        // 입력받은 값들 로그 출력
        log.info("입력받은 카테고리 정보:");
        log.info("  - 카테고리명: {}", request.getName());
        log.info("  - 표시순서: {}", request.getDisplayOrder());
        log.info("  - 활성화여부: {}", request.getIsActive());

        // 내 가게 조회
        log.info("가게 정보 조회 중...");
        Store store = getMyStore();
        log.info("가게 조회 완료 - 가게 ID: {}, 가게명: {}", store.getId(), store.getName());

        // 입력값 검증
        validateCategoryCreateRequest(request, store.getId());

        // 중복 카테고리명 체크
        log.info("중복 카테고리명 확인 중...");
        if (menuCategoryRepository.existsByStoreIdAndName(store.getId(), request.getName())) {
            log.warn("이미 존재하는 카테고리명입니다. 가게 ID: {}, 카테고리명: {}", store.getId(), request.getName());
            throw new RuntimeException("이미 존재하는 카테고리명입니다: " + request.getName());
        }
        log.info("중복 카테고리명 확인 완료 - 사용 가능한 카테고리명");

        // 표시 순서 자동 설정 (요청에 없는 경우)
        Integer displayOrder = request.getDisplayOrder();
        if (displayOrder == null || displayOrder == 0) {
            // 가게의 마지막 카테고리 순서 + 1로 설정
            Integer maxOrder = getMaxCategoryOrder(store.getId());
            displayOrder = maxOrder + 1;
            log.info("표시 순서 자동 설정: {}", displayOrder);
        }

        // MenuCategory 엔티티 생성
        log.info("MenuCategory 엔티티 생성 중...");
        MenuCategory category = MenuCategory.builder()
                .store(store)
                .name(request.getName().trim())
                .displayOrder(displayOrder)
                .isActive(request.getIsActive())
                .build();

        log.info("MenuCategory 엔티티 생성 완료");

        // 카테고리 저장
        log.info("메뉴 카테고리 저장 중...");
        MenuCategory savedCategory = menuCategoryRepository.save(category);

        log.info("메뉴 카테고리 등록 완료! 생성된 카테고리 ID: {}, 카테고리명: {}",
                savedCategory.getId(), savedCategory.getName());
        log.info("=== 메뉴 카테고리 등록 종료 ===");

        return savedCategory;
    }

    /**
     * 카테고리 생성 요청 검증
     */
    private void validateCategoryCreateRequest(MenuCategoryCreateRequest request, Long storeId) {
        log.debug("카테고리 생성 요청 유효성 검증 시작");

        // 필수 필드 검증
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("카테고리명은 필수입니다");
        }

        if (request.getName().trim().length() > 50) {
            throw new IllegalArgumentException("카테고리명은 50자를 초과할 수 없습니다");
        }

        if (request.getDisplayOrder() != null && request.getDisplayOrder() < 0) {
            throw new IllegalArgumentException("표시 순서는 0 이상이어야 합니다");
        }

        log.debug("카테고리 생성 요청 유효성 검증 완료");
    }

    /**
     * 가게의 카테고리 최대 순서 조회
     */
    private Integer getMaxCategoryOrder(Long storeId) {
        log.debug("가게의 카테고리 최대 순서 조회 - 가게 ID: {}", storeId);

        List<MenuCategory> categories = menuCategoryRepository.findByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(storeId);

        if (categories.isEmpty()) {
            log.debug("기존 카테고리가 없음 - 순서 0 반환");
            return 0;
        }

        Integer maxOrder = categories.stream()
                .mapToInt(MenuCategory::getDisplayOrder)
                .max()
                .orElse(0);

        log.debug("최대 순서: {}", maxOrder);
        return maxOrder;
    }



    /**
     * 메뉴 등록
     */
    @Transactional
    public Menu createMenu(MenuCreateRequest request) {
        log.info("=== 메뉴 등록 시작 ===");

        // 현재 로그인한 사용자 ID 가져오기
        Long currentUserId = getCurrentUserId();
        log.info("현재 사용자 ID: {}", currentUserId);

        // 입력받은 값들 로그 출력
        log.info("입력받은 메뉴 정보:");
        log.info("  - 메뉴명: {}", request.getName());
        log.info("  - 설명: {}", request.getDescription());
        log.info("  - 가격: {}", request.getPrice());
        log.info("  - 카테고리 ID: {}", request.getCategoryId());
        log.info("  - 이미지URL: {}", request.getImageUrl());
        log.info("  - 인기메뉴: {}", request.getIsPopular());
        log.info("  - 추천메뉴: {}", request.getIsRecommended());
        log.info("  - 상태: {}", request.getStatus());
        log.info("  - 표시순서: {}", request.getDisplayOrder());
        if (request.getOptions() != null) {
            log.info("  - 옵션 개수: {}개", request.getOptions().size());
        }

        // 내 가게 조회
        log.info("가게 정보 조회 중...");
        Store store = getMyStore();
        log.info("가게 조회 완료 - 가게 ID: {}, 가게명: {}", store.getId(), store.getName());

        // 카테고리 조회 및 검증
        log.info("카테고리 조회 중... 카테고리 ID: {}", request.getCategoryId());
        MenuCategory category = menuCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.error("카테고리를 찾을 수 없습니다. ID: {}", request.getCategoryId());
                    return new RuntimeException("카테고리를 찾을 수 없습니다");
                });

        // 카테고리가 해당 가게의 것인지 확인
        if (!category.getStore().getId().equals(store.getId())) {
            log.error("다른 가게의 카테고리입니다. 카테고리 소유 가게 ID: {}, 현재 가게 ID: {}",
                    category.getStore().getId(), store.getId());
            throw new RuntimeException("다른 가게의 카테고리입니다");
        }
        log.info("카테고리 조회 완료 - 카테고리명: {}", category.getName());

        // 입력값 검증
        validateMenuCreateRequest(request);

        // Menu 엔티티 생성
        log.info("Menu 엔티티 생성 중...");
        Menu menu = Menu.builder()
                .store(store)
                .category(category)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .imageUrl(request.getImageUrl())
                .isPopular(request.getIsPopular() != null ? request.getIsPopular() : false)
                .isRecommended(request.getIsRecommended() != null ? request.getIsRecommended() : false)
                .status(request.getStatus())
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .build();

        log.info("Menu 엔티티 생성 완료");

        // 메뉴 저장 (옵션은 아직 저장하지 않음)
        log.info("메뉴 기본 정보 저장 중...");
        Menu savedMenu = menuRepository.save(menu);
        log.info("메뉴 기본 정보 저장 완료 - 메뉴 ID: {}", savedMenu.getId());

        // 메뉴 옵션 처리
        if (request.getOptions() != null && !request.getOptions().isEmpty()) {
            log.info("메뉴 옵션 처리 시작 - 옵션 그룹 수: {}", request.getOptions().size());
            List<MenuOption> menuOptions = createMenuOptions(savedMenu, request.getOptions());

            // 메뉴에 옵션 설정 (양방향 관계 설정)
            if (savedMenu.getOptions() == null) {
                savedMenu = Menu.builder()
                        .store(savedMenu.getStore())
                        .category(savedMenu.getCategory())
                        .name(savedMenu.getName())
                        .description(savedMenu.getDescription())
                        .price(savedMenu.getPrice())
                        .imageUrl(savedMenu.getImageUrl())
                        .isPopular(savedMenu.getIsPopular())
                        .isRecommended(savedMenu.getIsRecommended())
                        .status(savedMenu.getStatus())
                        .displayOrder(savedMenu.getDisplayOrder())
                        .build();
            }

            log.info("메뉴 옵션 처리 완료 - 생성된 옵션 그룹 수: {}", menuOptions.size());
        }

        log.info("메뉴 등록 완료! 생성된 메뉴 ID: {}, 메뉴명: {}", savedMenu.getId(), savedMenu.getName());
        log.info("=== 메뉴 등록 종료 ===");

        return savedMenu;
    }

    /**
     * 메뉴 옵션들 생성 및 저장
     */
    private List<MenuOption> createMenuOptions(Menu menu, List<MenuOptionCreateRequest> optionRequests) {
        List<MenuOption> menuOptions = new ArrayList<>();

        for (int i = 0; i < optionRequests.size(); i++) {
            MenuOptionCreateRequest optionRequest = optionRequests.get(i);
            log.info("옵션 그룹 {}번 생성 중... 옵션명: {}, 타입: {}",
                    i + 1, optionRequest.getName(), optionRequest.getType());

            // 옵션 그룹 생성
            MenuOption menuOption = MenuOption.builder()
                    .menu(menu)
                    .name(optionRequest.getName())
                    .type(optionRequest.getType())
                    .isRequired(optionRequest.getIsRequired() != null ? optionRequest.getIsRequired() : false)
                    .displayOrder(optionRequest.getDisplayOrder() != null ? optionRequest.getDisplayOrder() : i)
                    .build();

            // 옵션 그룹을 먼저 저장 (ID가 필요하기 때문)
            MenuOption savedOption = menuOptionRepository.save(menuOption);
            log.info("옵션 그룹 저장 완료 - 옵션 ID: {}", savedOption.getId());

            // 옵션 아이템들 생성 및 저장
            if (optionRequest.getItems() != null && !optionRequest.getItems().isEmpty()) {
                log.info("옵션 아이템 생성 중... 아이템 수: {}", optionRequest.getItems().size());
                List<MenuOptionItem> optionItems = createMenuOptionItems(savedOption, optionRequest.getItems());
                log.info("옵션 아이템 생성 완료 - {}개 생성됨", optionItems.size());
            }

            menuOptions.add(savedOption);
            log.info("옵션 그룹 {}번 생성 완료", i + 1);
        }

        return menuOptions;
    }

    /**
     * 메뉴 옵션 아이템들 생성 및 저장
     */
    private List<MenuOptionItem> createMenuOptionItems(MenuOption option, List<MenuOptionItemCreateRequest> itemRequests) {
        List<MenuOptionItem> optionItems = new ArrayList<>();

        for (int i = 0; i < itemRequests.size(); i++) {
            MenuOptionItemCreateRequest itemRequest = itemRequests.get(i);
            log.debug("옵션 아이템 {}번 생성: {}, 추가금액: {}",
                    i + 1, itemRequest.getName(), itemRequest.getAdditionalPrice());

            MenuOptionItem optionItem = MenuOptionItem.builder()
                    .option(option)
                    .name(itemRequest.getName())
                    .additionalPrice(itemRequest.getAdditionalPrice() != null ?
                            itemRequest.getAdditionalPrice() : BigDecimal.ZERO)
                    .displayOrder(itemRequest.getDisplayOrder() != null ? itemRequest.getDisplayOrder() : i)
                    .isActive(itemRequest.getIsActive() != null ? itemRequest.getIsActive() : true)
                    .build();

            // 옵션 아이템 저장
            MenuOptionItem savedItem = menuOptionItemRepository.save(optionItem);
            optionItems.add(savedItem);

            log.debug("옵션 아이템 저장 완료 - 아이템 ID: {}", savedItem.getId());
        }

        return optionItems;
    }
    /**
     * 메뉴 생성 요청 검증
     */
    private void validateMenuCreateRequest(MenuCreateRequest request) {
        log.debug("메뉴 생성 요청 유효성 검증 시작");

        // 필수 필드 검증
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("메뉴명은 필수입니다");
        }

        if (request.getPrice() == null) {
            throw new IllegalArgumentException("가격은 필수입니다");
        }

        if (request.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 0원 이상이어야 합니다");
        }

        if (request.getCategoryId() == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다");
        }

        // 옵션 검증
        if (request.getOptions() != null) {
            for (MenuOptionCreateRequest option : request.getOptions()) {
                validateMenuOption(option);
            }
        }

        log.debug("메뉴 생성 요청 유효성 검증 완료");
    }

    /**
     * 메뉴 옵션 검증
     */
    private void validateMenuOption(MenuOptionCreateRequest option) {
        if (option.getName() == null || option.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("옵션명은 필수입니다");
        }

        if (option.getType() == null) {
            throw new IllegalArgumentException("옵션 타입은 필수입니다");
        }

        if (option.getItems() == null || option.getItems().isEmpty()) {
            throw new IllegalArgumentException("옵션 아이템은 최소 1개 이상이어야 합니다");
        }

        // 옵션 아이템 검증
        for (MenuOptionItemCreateRequest item : option.getItems()) {
            validateMenuOptionItem(item);
        }
    }

    /**
     * 메뉴 옵션 아이템 검증
     */
    private void validateMenuOptionItem(MenuOptionItemCreateRequest item) {
        if (item.getName() == null || item.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("옵션 아이템명은 필수입니다");
        }

        if (item.getAdditionalPrice() != null &&
                item.getAdditionalPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("추가 금액은 0원 이상이어야 합니다");
        }
    }

    /**
     * 내 가게 정보 조회
     */
    private Store getMyStore() {
        Long currentUserId = getCurrentUserId();
        log.debug("내 가게 정보 조회 - 사용자 ID: {}", currentUserId);

        return storeRepository.findByOwnerIdAndIsActiveTrue(currentUserId)
                .orElseThrow(() -> {
                    log.warn("등록된 가게가 없습니다. 사용자 ID: {}", currentUserId);
                    return new RuntimeException("등록된 가게가 없습니다");
                });
    }

    /**
     * 메뉴 카테고리 목록 조회
     */

    public List<MenuCategory> getMenuCategories() {
        log.info("=== 메뉴 카테고리 목록 조회 시작 ===");

        Long currentUserId = getCurrentUserId();
        Store store = getMyStore();

        log.info("메뉴 카테고리 전체 목록 조회 중... (비활성화 포함)");
        List<MenuCategory> categories = menuCategoryRepository.findByStoreIdOrderByDisplayOrderAsc(store.getId());

        log.info("메뉴 카테고리 전체 조회 완료 - 총 {}개 카테고리", categories.size());

        // 활성/비활성 상태별 통계
        long activeCount = categories.stream().filter(c -> c.getIsActive()).count();
        long inactiveCount = categories.size() - activeCount;
        log.info("  - 활성화: {}개, 비활성화: {}개", activeCount, inactiveCount);

        log.info("=== 메뉴 카테고리 전체 목록 조회 종료 ===");
        return categories;
    }





    /**
     * 메뉴 카테고리 수정
     */
    @Transactional
    public MenuCategory updateCategory(Long categoryId, MenuCategoryUpdateRequest request) {
        log.info("=== 메뉴 카테고리 수정 시작 ===");

        // 현재 로그인한 사용자 ID 가져오기
        Long currentUserId = getCurrentUserId();
        log.info("현재 사용자 ID: {}", currentUserId);
        log.info("수정할 카테고리 ID: {}", categoryId);

        // 입력받은 값들 로그 출력
        log.info("수정 요청 정보:");
        log.info("  - 카테고리명: {}", request.getName());
        log.info("  - 표시순서: {}", request.getDisplayOrder());
        log.info("  - 활성화여부: {}", request.getIsActive());
        log.info("  - 강제비활성화: {}", request.getForceDeactivate());

        // 내 가게 조회
        log.info("가게 정보 조회 중...");
        Store store = getMyStore();
        log.info("가게 조회 완료 - 가게 ID: {}, 가게명: {}", store.getId(), store.getName());

        // 카테고리 조회 및 권한 확인
        log.info("카테고리 조회 중... 카테고리 ID: {}", categoryId);
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("카테고리를 찾을 수 없습니다. ID: {}", categoryId);
                    return new RuntimeException("카테고리를 찾을 수 없습니다");
                });

        // 카테고리가 해당 가게의 것인지 확인
        if (!category.getStore().getId().equals(store.getId())) {
            log.error("다른 가게의 카테고리입니다. 카테고리 소유 가게 ID: {}, 현재 가게 ID: {}",
                    category.getStore().getId(), store.getId());
            throw new RuntimeException("다른 가게의 카테고리입니다");
        }

        log.info("카테고리 조회 완료 - 기존 카테고리명: {}", category.getName());

        // 입력값 검증
        validateCategoryUpdateRequest(request, store.getId(), categoryId);

        // 변경사항 적용
        boolean hasChanges = false;

        // 카테고리명 변경
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String newName = request.getName().trim();
            if (!newName.equals(category.getName())) {
                log.info("카테고리명 변경: {} -> {}", category.getName(), newName);
                category.updateName(newName);
                hasChanges = true;
            }
        }

        // 표시 순서 변경
        if (request.getDisplayOrder() != null && !request.getDisplayOrder().equals(category.getDisplayOrder())) {
            log.info("표시 순서 변경: {} -> {}", category.getDisplayOrder(), request.getDisplayOrder());
            category.updateDisplayOrder(request.getDisplayOrder());
            hasChanges = true;
        }

        // 활성화 상태 변경
        if (request.getIsActive() != null && !request.getIsActive().equals(category.getIsActive())) {
            log.info("활성화 상태 변경: {} -> {}", category.getIsActive(), request.getIsActive());

            // 비활성화할 때 처리
            if (!request.getIsActive()) {
                handleCategoryDeactivation(category, request.getForceDeactivate());
            }

            category.updateIsActive(request.getIsActive());
            hasChanges = true;
        }

        if (!hasChanges) {
            log.info("변경된 정보가 없습니다.");
            return category;
        }

        // 카테고리 저장
        log.info("메뉴 카테고리 저장 중...");
        MenuCategory savedCategory = menuCategoryRepository.save(category);

        log.info("메뉴 카테고리 수정 완료! 카테고리 ID: {}, 카테고리명: {}",
                savedCategory.getId(), savedCategory.getName());
        log.info("=== 메뉴 카테고리 수정 종료 ===");

        return savedCategory;
    }

    /**
     * 카테고리 수정 요청 검증
     */
    private void validateCategoryUpdateRequest(MenuCategoryUpdateRequest request, Long storeId, Long categoryId) {
        log.debug("카테고리 수정 요청 유효성 검증 시작");

        // 카테고리명 중복 체크 (다른 카테고리와)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            String trimmedName = request.getName().trim();

            if (trimmedName.length() > 50) {
                throw new IllegalArgumentException("카테고리명은 50자를 초과할 수 없습니다");
            }

            // 현재 카테고리가 아닌 다른 카테고리에서 같은 이름이 사용되는지 확인
            List<MenuCategory> existingCategories = menuCategoryRepository.findByStoreIdOrderByDisplayOrderAsc(storeId);
            boolean isDuplicate = existingCategories.stream()
                    .anyMatch(cat -> !cat.getId().equals(categoryId) &&
                            cat.getName().equals(trimmedName) &&
                            cat.getIsActive());

            if (isDuplicate) {
                throw new IllegalArgumentException("이미 존재하는 카테고리명입니다: " + trimmedName);
            }
        }

        // 표시 순서 검증
        if (request.getDisplayOrder() != null && request.getDisplayOrder() < 0) {
            throw new IllegalArgumentException("표시 순서는 0 이상이어야 합니다");
        }

        log.debug("카테고리 수정 요청 유효성 검증 완료");
    }

    /**
     * 카테고리 비활성화 처리
     */
    private void handleCategoryDeactivation(MenuCategory category, Boolean forceDeactivate) {
        log.info("카테고리 비활성화 처리 시작...");

        if (category.getMenus() != null && !category.getMenus().isEmpty()) {
            long activeMenuCount = category.getMenus().stream()
                    .filter(menu -> menu.getStatus() == MenuStatus.AVAILABLE)
                    .count();

            long soldOutMenuCount = category.getMenus().stream()
                    .filter(menu -> menu.getStatus() == MenuStatus.SOLD_OUT)
                    .count();

            if (activeMenuCount > 0 || soldOutMenuCount > 0) {
                log.warn("카테고리에 활성/품절 메뉴가 {}개 있습니다 (판매중: {}개, 품절: {}개)",
                        activeMenuCount + soldOutMenuCount, activeMenuCount, soldOutMenuCount);

                // 강제 비활성화 옵션이 true인 경우 메뉴들을 자동으로 숨김 처리
                if (forceDeactivate != null && forceDeactivate) {
                    log.info("강제 비활성화 옵션으로 메뉴들을 숨김 처리합니다");

                    List<Menu> menusToUpdate = new ArrayList<>();
                    category.getMenus().forEach(menu -> {
                        if (menu.getStatus() == MenuStatus.AVAILABLE || menu.getStatus() == MenuStatus.SOLD_OUT) {
                            log.info("메뉴 숨김 처리: {} (기존 상태: {} -> HIDDEN)", menu.getName(), menu.getStatus());
                            menu.updateStatus(MenuStatus.HIDDEN);
                            menusToUpdate.add(menu);
                        }
                    });

                    // 변경된 메뉴들 저장
                    if (!menusToUpdate.isEmpty()) {
                        menuRepository.saveAll(menusToUpdate);
                        log.info("총 {}개 메뉴가 숨김 처리되었습니다", menusToUpdate.size());
                    }

                } else {
                    // 강제 옵션이 없으면 예외 발생
                    throw new IllegalArgumentException(
                            String.format("카테고리에 판매중인 메뉴가 %d개 있습니다 (판매중: %d개, 품절: %d개). " +
                                            "강제 비활성화를 원하면 forceDeactivate=true 옵션을 사용하거나 " +
                                            "메뉴를 먼저 숨김 처리해주세요.",
                                    activeMenuCount + soldOutMenuCount, activeMenuCount, soldOutMenuCount)
                    );
                }
            }
        }

        log.info("카테고리 비활성화 처리 완료");
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        log.info("=== 메뉴 카테고리 삭제 시작 ===");

        // 현재 로그인한 사용자 ID 가져오기
        Long currentUserId = getCurrentUserId();
        log.info("현재 사용자 ID: {}", currentUserId);
        log.info("삭제할 카테고리 ID: {}", categoryId);

        // 내 가게 조회
        log.info("가게 정보 조회 중...");
        Store store = getMyStore();
        log.info("가게 조회 완료 - 가게 ID: {}, 가게명: {}", store.getId(), store.getName());

        // 카테고리 조회 및 권한 확인
        log.info("카테고리 조회 중... 카테고리 ID: {}", categoryId);
        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> {
                    log.error("카테고리를 찾을 수 없습니다. ID: {}", categoryId);
                    return new RuntimeException("카테고리를 찾을 수 없습니다");
                });

        // 카테고리가 해당 가게의 것인지 확인
        if (!category.getStore().getId().equals(store.getId())) {
            log.error("다른 가게의 카테고리입니다. 카테고리 소유 가게 ID: {}, 현재 가게 ID: {}",
                    category.getStore().getId(), store.getId());
            throw new RuntimeException("다른 가게의 카테고리입니다");
        }

        log.info("카테고리 조회 완료 - 카테고리명: {}, 활성상태: {}", category.getName(), category.getIsActive());

        // 삭제 전 검증
        validateCategoryDeletion(category);

        // 카테고리에 속한 메뉴들 확인 및 처리
        handleMenusBeforeCategoryDeletion(category);

        // 카테고리 삭제
        log.info("카테고리 삭제 실행 중...");
        menuCategoryRepository.delete(category);

        log.info("메뉴 카테고리 삭제 완료! 삭제된 카테고리: {} (ID: {})", category.getName(), categoryId);
        log.info("=== 메뉴 카테고리 삭제 종료 ===");
    }

    /**
     * 카테고리 삭제 전 검증
     */
    private void validateCategoryDeletion(MenuCategory category) {
        log.info("카테고리 삭제 가능 여부 검증 중...");

        // 1. 카테고리에 메뉴가 있는지 확인
        if (category.getMenus() != null && !category.getMenus().isEmpty()) {
            int totalMenus = category.getMenus().size();
            long activeMenus = category.getMenus().stream()
                    .filter(menu -> menu.getStatus() == MenuStatus.AVAILABLE)
                    .count();
            long hiddenMenus = category.getMenus().stream()
                    .filter(menu -> menu.getStatus() == MenuStatus.HIDDEN)
                    .count();
            long soldOutMenus = category.getMenus().stream()
                    .filter(menu -> menu.getStatus() == MenuStatus.SOLD_OUT)
                    .count();

            log.warn("카테고리에 메뉴가 {}개 존재합니다", totalMenus);
            log.warn("  - 판매중: {}개, 품절: {}개, 숨김: {}개", activeMenus, soldOutMenus, hiddenMenus);

            // 활성 메뉴나 품절 메뉴가 있으면 삭제 불가
            if (activeMenus > 0 || soldOutMenus > 0) {
                throw new IllegalArgumentException(
                        String.format("카테고리에 메뉴가 %d개 있습니다 (판매중: %d개, 품절: %d개). " +
                                        "메뉴를 모두 숨김 처리하거나 다른 카테고리로 이동한 후 삭제해주세요.",
                                activeMenus + soldOutMenus, activeMenus, soldOutMenus)
                );
            }

            // 숨김 메뉴만 있는 경우 경고 로그만 출력하고 진행
            if (hiddenMenus > 0) {
                log.warn("숨김 처리된 메뉴 {}개가 함께 삭제됩니다", hiddenMenus);
            }
        }

        // 2. 마지막 남은 카테고리인지 확인
        List<MenuCategory> activeCategories = menuCategoryRepository
                .findByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(category.getStore().getId());

        if (activeCategories.size() == 1 && activeCategories.get(0).getId().equals(category.getId())) {
            log.warn("마지막 남은 활성 카테고리입니다");
            throw new IllegalArgumentException("최소 1개 이상의 카테고리가 있어야 합니다. 다른 카테고리를 먼저 생성해주세요.");
        }

        log.info("카테고리 삭제 가능 여부 검증 완료");
    }

    /**
     * 카테고리 삭제 전 메뉴들 처리
     */
    private void handleMenusBeforeCategoryDeletion(MenuCategory category) {
        if (category.getMenus() != null && !category.getMenus().isEmpty()) {
            log.info("카테고리에 속한 메뉴들 처리 중...");

            // 숨김 처리된 메뉴들과 관련 옵션들을 모두 삭제
            for (Menu menu : category.getMenus()) {
                log.info("메뉴 삭제 처리: {} (상태: {})", menu.getName(), menu.getStatus());

                // 메뉴 옵션들 삭제
                if (menu.getOptions() != null && !menu.getOptions().isEmpty()) {
                    log.info("메뉴 옵션 삭제 중... 옵션 그룹 수: {}", menu.getOptions().size());

                    for (MenuOption option : menu.getOptions()) {
                        // 옵션 아이템들 삭제
                        if (option.getItems() != null && !option.getItems().isEmpty()) {
                            log.debug("옵션 아이템 삭제: {} ({}개)", option.getName(), option.getItems().size());
                            menuOptionItemRepository.deleteByOptionId(option.getId());
                        }
                    }

                    // 옵션 그룹 삭제
                    menuOptionRepository.deleteByMenuId(menu.getId());
                }
            }

            log.info("메뉴들 처리 완료");
        }
    }

    /**
     * 카테고리 삭제 (소프트 삭제 버전) - 선택사항
     */
    @Transactional
    public void softDeleteCategory(Long categoryId) {
        log.info("=== 메뉴 카테고리 소프트 삭제 시작 ===");

        Long currentUserId = getCurrentUserId();
        Store store = getMyStore();

        MenuCategory category = menuCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다"));

        if (!category.getStore().getId().equals(store.getId())) {
            throw new RuntimeException("다른 가게의 카테고리입니다");
        }

        log.info("카테고리 비활성화 처리: {}", category.getName());

        // 비활성화로 처리 (실제 삭제하지 않음)
        category.updateIsActive(false);
        menuCategoryRepository.save(category);

        log.info("메뉴 카테고리 소프트 삭제 완료! 카테고리: {} (ID: {})", category.getName(), categoryId);
        log.info("=== 메뉴 카테고리 소프트 삭제 종료 ===");
    }

    /**
     * 카테고리 순서 변경 (드래그 앤 드롭 방식)
     */
    @Transactional
    public List<MenuCategory> updateCategoryOrder(CategoryMoveRequest request) {
        log.info("=== 카테고리 순서 변경 시작 ===");

        // 현재 로그인한 사용자 ID 가져오기
        Long currentUserId = getCurrentUserId();
        log.info("현재 사용자 ID: {}", currentUserId);

        // 내 가게 조회
        log.info("가게 정보 조회 중...");
        Store store = getMyStore();
        log.info("가게 조회 완료 - 가게 ID: {}, 가게명: {}", store.getId(), store.getName());

        // 이동할 카테고리 조회 및 권한 확인
        log.info("이동할 카테고리 조회 중... 카테고리 ID: {}", request.getCategoryId());
        MenuCategory targetCategory = menuCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> {
                    log.error("카테고리를 찾을 수 없습니다. ID: {}", request.getCategoryId());
                    return new IllegalArgumentException("카테고리를 찾을 수 없습니다");
                });

        // 카테고리가 해당 가게의 것인지 확인
        if (!targetCategory.getStore().getId().equals(store.getId())) {
            log.error("다른 가게의 카테고리입니다. 카테고리 소유 가게 ID: {}, 현재 가게 ID: {}",
                    targetCategory.getStore().getId(), store.getId());
            throw new IllegalArgumentException("다른 가게의 카테고리입니다");
        }

        // 현재 가게의 모든 활성 카테고리를 순서대로 조회
        List<MenuCategory> allCategories = menuCategoryRepository.findByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(store.getId());

        if (allCategories.isEmpty()) {
            log.warn("활성 카테고리가 없습니다");
            throw new IllegalArgumentException("활성 카테고리가 없습니다");
        }

        // 새로운 위치 유효성 검증
        if (request.getNewPosition() < 1 || request.getNewPosition() > allCategories.size()) {
            log.error("잘못된 위치입니다. 요청 위치: {}, 총 카테고리 수: {}", request.getNewPosition(), allCategories.size());
            throw new IllegalArgumentException(
                    String.format("위치는 1부터 %d 사이여야 합니다", allCategories.size())
            );
        }

        // 현재 위치 찾기
        int currentPosition = -1;
        for (int i = 0; i < allCategories.size(); i++) {
            if (allCategories.get(i).getId().equals(request.getCategoryId())) {
                currentPosition = i + 1; // 1부터 시작
                break;
            }
        }

        if (currentPosition == -1) {
            log.error("카테고리를 목록에서 찾을 수 없습니다. ID: {}", request.getCategoryId());
            throw new IllegalArgumentException("카테고리를 목록에서 찾을 수 없습니다");
        }

        log.info("카테고리 이동: {} (ID: {}) - 위치 {} -> {}",
                targetCategory.getName(), targetCategory.getId(),
                currentPosition, request.getNewPosition());

        // 위치가 같으면 변경하지 않음
        if (currentPosition == request.getNewPosition()) {
            log.info("이미 같은 위치입니다. 변경하지 않습니다.");
            return allCategories;
        }

        // 카테고리 리스트에서 이동할 카테고리 제거
        MenuCategory movingCategory = allCategories.remove(currentPosition - 1);

        // 새로운 위치에 삽입 (인덱스는 0부터 시작하므로 -1)
        allCategories.add(request.getNewPosition() - 1, movingCategory);

        // 모든 카테고리의 displayOrder를 새로 설정
        List<MenuCategory> updatedCategories = new ArrayList<>();
        for (int i = 0; i < allCategories.size(); i++) {
            MenuCategory category = allCategories.get(i);
            int newDisplayOrder = i + 1;

            if (!category.getDisplayOrder().equals(newDisplayOrder)) {
                log.info("카테고리 순서 업데이트: {} (ID: {}) - {} -> {}",
                        category.getName(), category.getId(),
                        category.getDisplayOrder(), newDisplayOrder);
                category.updateDisplayOrder(newDisplayOrder);
                updatedCategories.add(category);
            }
        }

        // 변경된 카테고리들만 저장
        if (!updatedCategories.isEmpty()) {
            menuCategoryRepository.saveAll(updatedCategories);
            log.info("총 {}개 카테고리 순서가 업데이트되었습니다", updatedCategories.size());
        }

        log.info("=== 카테고리 순서 변경 완료 ===");

        // 최종 결과를 순서대로 반환
        return menuCategoryRepository.findByStoreIdAndIsActiveTrueOrderByDisplayOrderAsc(store.getId());
    }

    /**
     * 카테고리 순서 변경 요청 검증
     */
    private void validateCategoryOrderRequest(CategoryOrderUpdateRequest request, Long storeId) {
        log.debug("카테고리 순서 변경 요청 유효성 검증 시작");

        if (request.getCategoryOrders() == null || request.getCategoryOrders().isEmpty()) {
            throw new IllegalArgumentException("카테고리 순서 목록은 필수입니다");
        }

        // 중복된 카테고리 ID 확인
        List<Long> categoryIds = request.getCategoryOrders().stream()
                .map(CategoryOrderUpdateRequest.CategoryOrderItem::getCategoryId)
                .toList();

        long uniqueCount = categoryIds.stream().distinct().count();
        if (uniqueCount != categoryIds.size()) {
            throw new IllegalArgumentException("중복된 카테고리 ID가 있습니다");
        }

        // 중복된 순서 확인
        List<Integer> orders = request.getCategoryOrders().stream()
                .map(CategoryOrderUpdateRequest.CategoryOrderItem::getDisplayOrder)
                .toList();

        long uniqueOrderCount = orders.stream().distinct().count();
        if (uniqueOrderCount != orders.size()) {
            throw new IllegalArgumentException("중복된 표시 순서가 있습니다");
        }

        // 순서 값 유효성 검증
        for (CategoryOrderUpdateRequest.CategoryOrderItem item : request.getCategoryOrders()) {
            if (item.getCategoryId() == null) {
                throw new IllegalArgumentException("카테고리 ID는 필수입니다");
            }
            if (item.getDisplayOrder() == null || item.getDisplayOrder() < 0) {
                throw new IllegalArgumentException("표시 순서는 0 이상이어야 합니다");
            }
        }

        log.debug("카테고리 순서 변경 요청 유효성 검증 완료");
    }
}