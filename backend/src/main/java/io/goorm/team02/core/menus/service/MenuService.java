package io.goorm.team02.core.menus.service;

import io.goorm.team02.core.menus.controller.dto.categorycreate.MenuCategoryCreateRequest;
import io.goorm.team02.core.menus.controller.dto.menucreate.MenuCreateRequest;
import io.goorm.team02.core.menus.controller.dto.menucreate.MenuOptionCreateRequest;
import io.goorm.team02.core.menus.controller.dto.menucreate.MenuOptionItemCreateRequest;
import io.goorm.team02.core.menus.domain.Menu;
import io.goorm.team02.core.menus.domain.MenuCategory;
import io.goorm.team02.core.menus.domain.MenuOption;
import io.goorm.team02.core.menus.domain.MenuOptionItem;
import io.goorm.team02.core.menus.repository.MenuCategoryRepository;
import io.goorm.team02.core.menus.repository.MenuOptionItemRepository;
import io.goorm.team02.core.menus.repository.MenuOptionRepository;
import io.goorm.team02.core.menus.repository.MenuRepository;
import io.goorm.team02.core.stores.domain.Store;
import io.goorm.team02.core.stores.repository.StoreRepository;
import io.goorm.team02.core.stores.repository.UserRepository;
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
    private final UserRepository userRepository;

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
     * 현재 로그인한 사용자 ID 조회
     */
    private Long getCurrentUserId() {
        // TODO: Spring Security Context에서 현재 사용자 ID 조회
        // 임시로 1L 반환
        return 1L;
    }
}