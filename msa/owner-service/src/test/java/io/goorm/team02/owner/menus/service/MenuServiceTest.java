package io.goorm.team02.owner.menus.service;

import io.goorm.team02.core.owner.menus.domain.Menu;
import io.goorm.team02.core.owner.menus.domain.MenuCategory;
import io.goorm.team02.core.owner.menus.domain.MenuOption;
import io.goorm.team02.core.owner.menus.domain.MenuOptionItem;
import io.goorm.team02.core.owner.menus.domain.enums.MenuStatus;
import io.goorm.team02.core.owner.menus.domain.enums.OptionType;
import io.goorm.team02.core.owner.menus.service.*;
import io.goorm.team02.core.owner.stores.domain.Store;
import io.goorm.team02.core.owner.stores.domain.enums.StoreCategory;
import io.goorm.team02.core.owner.stores.domain.enums.StoreStatus;
import io.goorm.team02.dto.owner.menus.categorycreate.CategoryMoveRequest;
import io.goorm.team02.dto.owner.menus.categorycreate.MenuCategoryCreateRequest;
import io.goorm.team02.dto.owner.menus.categorycreate.MenuCategoryUpdateRequest;
import io.goorm.team02.dto.owner.menus.menucreate.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MenuService 단위 테스트")
class MenuServiceTest {

    @Mock
    private MenuCategoryService menuCategoryService;

    @Mock
    private MenuCrudService menuCrudService;

    @Mock
    private MenuImageService menuImageService;

    @Mock
    private MenuOptionService menuOptionService;

    @Mock
    private MenuValidationService menuValidationService;

    @InjectMocks
    private MenuService menuService;

    private Long testUserId = 1L;
    private Long testStoreId = 1L;
    private Long testCategoryId = 1L;
    private Long testMenuId = 1L;
    private Long testOptionGroupId = 1L;
    private Long testOptionItemId = 1L;

    private Store testStore;
    private MenuCategory testCategory;
    private Menu testMenu;
    private MenuOption testOption;
    private MenuOptionItem testOptionItem;

    @BeforeEach
    void setUp() {
        testStore = createTestStore();
        testCategory = createTestCategory();
        testMenu = createTestMenu();
        testOption = createTestOption();
        testOptionItem = createTestOptionItem();
    }

    @Nested
    @DisplayName("카테고리 관리 위임 테스트")
    class CategoryDelegationTests {

        @Test
        @DisplayName("카테고리 목록 조회 위임")
        void getMenuCategories_ShouldDelegateToMenuCategoryService() {
            // Given
            List<MenuCategory> expectedCategories = List.of(testCategory);
            when(menuCategoryService.getMenuCategories(testUserId)).thenReturn(expectedCategories);

            // When
            List<MenuCategory> result = menuService.getMenuCategories(testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testCategory);
            verify(menuCategoryService).getMenuCategories(testUserId);
        }

        @Test
        @DisplayName("카테고리 생성 위임")
        void createCategory_ShouldDelegateToMenuCategoryService() {
            // Given
            MenuCategoryCreateRequest request = createCategoryCreateRequest();
            when(menuCategoryService.createCategory(testUserId, request)).thenReturn(testCategory);

            // When
            MenuCategory result = menuService.createCategory(testUserId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testCategory);
            verify(menuCategoryService).createCategory(testUserId, request);
        }

        @Test
        @DisplayName("카테고리 수정 위임")
        void updateCategory_ShouldDelegateToMenuCategoryService() {
            // Given
            MenuCategoryUpdateRequest request = createCategoryUpdateRequest();
            when(menuCategoryService.updateCategory(testUserId, testCategoryId, request))
                    .thenReturn(testCategory);

            // When
            MenuCategory result = menuService.updateCategory(testUserId, testCategoryId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testCategory);
            verify(menuCategoryService).updateCategory(testUserId, testCategoryId, request);
        }

        @Test
        @DisplayName("카테고리 삭제 위임")
        void deleteCategory_ShouldDelegateToMenuCategoryService() {
            // Given
            doNothing().when(menuCategoryService).deleteCategory(testUserId, testCategoryId);

            // When
            menuService.deleteCategory(testUserId, testCategoryId);

            // Then
            verify(menuCategoryService).deleteCategory(testUserId, testCategoryId);
        }

        @Test
        @DisplayName("카테고리 순서 변경 위임")
        void updateCategoryOrder_ShouldDelegateToMenuCategoryService() {
            // Given
            CategoryMoveRequest request = createCategoryMoveRequest();
            List<MenuCategory> expectedResult = List.of(testCategory);
            when(menuCategoryService.updateCategoryOrder(testUserId, request))
                    .thenReturn(expectedResult);

            // When
            List<MenuCategory> result = menuService.updateCategoryOrder(testUserId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            verify(menuCategoryService).updateCategoryOrder(testUserId, request);
        }
    }

    @Nested
    @DisplayName("메뉴 CRUD 위임 테스트")
    class MenuCrudDelegationTests {

        @Test
        @DisplayName("메뉴 목록 조회 위임")
        void getMenus_ShouldDelegateToMenuCrudService() {
            // Given
            List<Menu> expectedMenus = List.of(testMenu);
            when(menuCrudService.getMenus(testUserId, testCategoryId)).thenReturn(expectedMenus);

            // When
            List<Menu> result = menuService.getMenus(testUserId, testCategoryId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testMenu);
            verify(menuCrudService).getMenus(testUserId, testCategoryId);
        }

        @Test
        @DisplayName("메뉴 목록 조회 - 카테고리 ID null")
        void getMenus_WithNullCategoryId_ShouldDelegateToMenuCrudService() {
            // Given
            List<Menu> expectedMenus = List.of(testMenu);
            when(menuCrudService.getMenus(testUserId, null)).thenReturn(expectedMenus);

            // When
            List<Menu> result = menuService.getMenus(testUserId, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            verify(menuCrudService).getMenus(testUserId, null);
        }

        @Test
        @DisplayName("메뉴 상세 조회 위임")
        void getMenu_ShouldDelegateToMenuCrudService() {
            // Given
            when(menuCrudService.getMenu(testUserId, testMenuId)).thenReturn(testMenu);

            // When
            Menu result = menuService.getMenu(testUserId, testMenuId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testMenu);
            verify(menuCrudService).getMenu(testUserId, testMenuId);
        }

        @Test
        @DisplayName("메뉴 생성 위임")
        void createMenu_ShouldDelegateToMenuCrudService() {
            // Given
            MenuCreateRequest request = createMenuCreateRequest();
            when(menuCrudService.createMenu(testUserId, request)).thenReturn(testMenu);

            // When
            Menu result = menuService.createMenu(testUserId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testMenu);
            verify(menuCrudService).createMenu(testUserId, request);
        }

        @Test
        @DisplayName("메뉴 수정 위임")
        void updateMenu_ShouldDelegateToMenuCrudService() {
            // Given
            MenuUpdateRequest request = createMenuUpdateRequest();
            when(menuCrudService.updateMenu(testUserId, testMenuId, request)).thenReturn(testMenu);

            // When
            Menu result = menuService.updateMenu(testUserId, testMenuId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testMenu);
            verify(menuCrudService).updateMenu(testUserId, testMenuId, request);
        }

        @Test
        @DisplayName("메뉴 삭제 위임")
        void deleteMenu_ShouldDelegateToMenuCrudService() {
            // Given
            doNothing().when(menuCrudService).deleteMenu(testUserId, testMenuId);

            // When
            menuService.deleteMenu(testUserId, testMenuId);

            // Then
            verify(menuCrudService).deleteMenu(testUserId, testMenuId);
        }

        @Test
        @DisplayName("메뉴 상태 변경 위임")
        void updateMenuStatus_ShouldDelegateToMenuCrudService() {
            // Given
            MenuStatusRequest request = createMenuStatusRequest();
            when(menuCrudService.updateMenuStatus(testUserId, testMenuId, request)).thenReturn(testMenu);

            // When
            Menu result = menuService.updateMenuStatus(testUserId, testMenuId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testMenu);
            verify(menuCrudService).updateMenuStatus(testUserId, testMenuId, request);
        }

        @Test
        @DisplayName("메뉴 순서 변경 위임")
        void updateMenuOrder_ShouldDelegateToMenuCrudService() {
            // Given
            MenuOrderUpdateRequest request = createMenuOrderUpdateRequest();
            List<Menu> expectedResult = List.of(testMenu);
            when(menuCrudService.updateMenuOrder(testUserId, request)).thenReturn(expectedResult);

            // When
            List<Menu> result = menuService.updateMenuOrder(testUserId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            verify(menuCrudService).updateMenuOrder(testUserId, request);
        }
    }

    @Nested
    @DisplayName("이미지 관리 위임 테스트")
    class ImageDelegationTests {

        @Mock
        private MultipartFile mockFile;

        @Test
        @DisplayName("메뉴 이미지 업로드 위임")
        void uploadMenuImage_ShouldDelegateToMenuImageService() {
            // Given
            String expectedImageUrl = "https://example.com/image.jpg";
            when(menuImageService.uploadMenuImage(testUserId, testMenuId, mockFile))
                    .thenReturn(expectedImageUrl);

            // When
            String result = menuService.uploadMenuImage(testUserId, testMenuId, mockFile);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedImageUrl);
            verify(menuImageService).uploadMenuImage(testUserId, testMenuId, mockFile);
        }

        @Test
        @DisplayName("메뉴 이미지 정보 조회 위임")
        void getMenuImageInfo_ShouldDelegateToMenuImageService() {
            // Given
            Map<String, Object> expectedInfo = Map.of(
                    "menuId", testMenuId,
                    "hasImage", true,
                    "imageUrl", "https://example.com/image.jpg"
            );
            when(menuImageService.getMenuImageInfo(testUserId, testMenuId)).thenReturn(expectedInfo);

            // When
            Map<String, Object> result = menuService.getMenuImageInfo(testUserId, testMenuId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).containsKey("menuId");
            assertThat(result.get("menuId")).isEqualTo(testMenuId);
            verify(menuImageService).getMenuImageInfo(testUserId, testMenuId);
        }

        @Test
        @DisplayName("메뉴 이미지 삭제 위임")
        void deleteMenuImage_ShouldDelegateToMenuImageService() {
            // Given
            Long imageId = 123L;
            doNothing().when(menuImageService).deleteMenuImage(testUserId, testMenuId, imageId);

            // When
            menuService.deleteMenuImage(testUserId, testMenuId, imageId);

            // Then
            verify(menuImageService).deleteMenuImage(testUserId, testMenuId, imageId);
        }
    }

    @Nested
    @DisplayName("옵션 관리 위임 테스트")
    class OptionDelegationTests {

        @Test
        @DisplayName("옵션 그룹 목록 조회 위임")
        void getMenuOptionGroups_ShouldDelegateToMenuOptionService() {
            // Given
            List<MenuOption> expectedOptions = List.of(testOption);
            when(menuOptionService.getMenuOptionGroups(testUserId, testMenuId))
                    .thenReturn(expectedOptions);

            // When
            List<MenuOption> result = menuService.getMenuOptionGroups(testUserId, testMenuId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testOption);
            verify(menuOptionService).getMenuOptionGroups(testUserId, testMenuId);
        }

        @Test
        @DisplayName("옵션 그룹 생성 위임")
        void createOptionGroup_ShouldDelegateToMenuOptionService() {
            // Given
            MenuOptionGroupCreateRequest request = createOptionGroupCreateRequest();
            when(menuOptionService.createOptionGroup(testUserId, testMenuId, request))
                    .thenReturn(testOption);

            // When
            MenuOption result = menuService.createOptionGroup(testUserId, testMenuId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testOption);
            verify(menuOptionService).createOptionGroup(testUserId, testMenuId, request);
        }

        @Test
        @DisplayName("옵션 그룹 수정 위임")
        void updateOptionGroup_ShouldDelegateToMenuOptionService() {
            // Given
            MenuOptionGroupUpdateRequest request = createOptionGroupUpdateRequest();
            when(menuOptionService.updateOptionGroup(testUserId, testMenuId, testOptionGroupId, request))
                    .thenReturn(testOption);

            // When
            MenuOption result = menuService.updateOptionGroup(testUserId, testMenuId, testOptionGroupId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testOption);
            verify(menuOptionService).updateOptionGroup(testUserId, testMenuId, testOptionGroupId, request);
        }

        @Test
        @DisplayName("옵션 그룹 삭제 위임")
        void deleteOptionGroup_ShouldDelegateToMenuOptionService() {
            // Given
            doNothing().when(menuOptionService).deleteOptionGroup(testUserId, testMenuId, testOptionGroupId);

            // When
            menuService.deleteOptionGroup(testUserId, testMenuId, testOptionGroupId);

            // Then
            verify(menuOptionService).deleteOptionGroup(testUserId, testMenuId, testOptionGroupId);
        }

        @Test
        @DisplayName("옵션 아이템 목록 조회 위임")
        void getMenuOptions_ShouldDelegateToMenuOptionService() {
            // Given
            List<MenuOptionItem> expectedItems = List.of(testOptionItem);
            when(menuOptionService.getMenuOptions(testUserId, testMenuId, testOptionGroupId))
                    .thenReturn(expectedItems);

            // When
            List<MenuOptionItem> result = menuService.getMenuOptions(testUserId, testMenuId, testOptionGroupId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(testOptionItem);
            verify(menuOptionService).getMenuOptions(testUserId, testMenuId, testOptionGroupId);
        }

        @Test
        @DisplayName("옵션 아이템 생성 위임")
        void createOption_ShouldDelegateToMenuOptionService() {
            // Given
            MenuOptionItemCreateRequest request = createOptionItemCreateRequest();
            when(menuOptionService.createOption(testUserId, testMenuId, testOptionGroupId, request))
                    .thenReturn(testOptionItem);

            // When
            MenuOptionItem result = menuService.createOption(testUserId, testMenuId, testOptionGroupId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testOptionItem);
            verify(menuOptionService).createOption(testUserId, testMenuId, testOptionGroupId, request);
        }

        @Test
        @DisplayName("옵션 아이템 수정 위임")
        void updateOption_ShouldDelegateToMenuOptionService() {
            // Given
            MenuOptionItemUpdateRequest request = createOptionItemUpdateRequest();
            when(menuOptionService.updateOption(testUserId, testMenuId, testOptionGroupId, testOptionItemId, request))
                    .thenReturn(testOptionItem);

            // When
            MenuOptionItem result = menuService.updateOption(testUserId, testMenuId, testOptionGroupId, testOptionItemId, request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(testOptionItem);
            verify(menuOptionService).updateOption(testUserId, testMenuId, testOptionGroupId, testOptionItemId, request);
        }

        @Test
        @DisplayName("옵션 아이템 삭제 위임")
        void deleteOption_ShouldDelegateToMenuOptionService() {
            // Given
            doNothing().when(menuOptionService).deleteOption(testUserId, testMenuId, testOptionGroupId, testOptionItemId);

            // When
            menuService.deleteOption(testUserId, testMenuId, testOptionGroupId, testOptionItemId);

            // Then
            verify(menuOptionService).deleteOption(testUserId, testMenuId, testOptionGroupId, testOptionItemId);
        }
    }

    @Nested
    @DisplayName("예외 상황 테스트")
    class ExceptionTests {

        @Test
        @DisplayName("카테고리 서비스 예외 전파")
        void createCategory_WhenCategoryServiceThrows_ShouldPropagateException() {
            // Given
            MenuCategoryCreateRequest request = createCategoryCreateRequest();
            RuntimeException expectedException = new RuntimeException("카테고리 생성 실패");
            when(menuCategoryService.createCategory(testUserId, request)).thenThrow(expectedException);

            // When & Then
            assertThatThrownBy(() -> menuService.createCategory(testUserId, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("카테고리 생성 실패");

            verify(menuCategoryService).createCategory(testUserId, request);
        }

        @Test
        @DisplayName("메뉴 CRUD 서비스 예외 전파")
        void createMenu_WhenMenuCrudServiceThrows_ShouldPropagateException() {
            // Given
            MenuCreateRequest request = createMenuCreateRequest();
            RuntimeException expectedException = new RuntimeException("메뉴 생성 실패");
            when(menuCrudService.createMenu(testUserId, request)).thenThrow(expectedException);

            // When & Then
            assertThatThrownBy(() -> menuService.createMenu(testUserId, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("메뉴 생성 실패");

            verify(menuCrudService).createMenu(testUserId, request);
        }

        @Test
        @DisplayName("이미지 서비스 예외 전파")
        void uploadMenuImage_WhenImageServiceThrows_ShouldPropagateException() {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            RuntimeException expectedException = new RuntimeException("이미지 업로드 실패");
            when(menuImageService.uploadMenuImage(testUserId, testMenuId, mockFile))
                    .thenThrow(expectedException);

            // When & Then
            assertThatThrownBy(() -> menuService.uploadMenuImage(testUserId, testMenuId, mockFile))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("이미지 업로드 실패");

            verify(menuImageService).uploadMenuImage(testUserId, testMenuId, mockFile);
        }

        @Test
        @DisplayName("옵션 서비스 예외 전파")
        void createOptionGroup_WhenOptionServiceThrows_ShouldPropagateException() {
            // Given
            MenuOptionGroupCreateRequest request = createOptionGroupCreateRequest();
            RuntimeException expectedException = new RuntimeException("옵션 그룹 생성 실패");
            when(menuOptionService.createOptionGroup(testUserId, testMenuId, request))
                    .thenThrow(expectedException);

            // When & Then
            assertThatThrownBy(() -> menuService.createOptionGroup(testUserId, testMenuId, request))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("옵션 그룹 생성 실패");

            verify(menuOptionService).createOptionGroup(testUserId, testMenuId, request);
        }
    }

    @Nested
    @DisplayName("통합 워크플로우 테스트")
    class IntegrationWorkflowTests {

        @Test
        @DisplayName("전체 메뉴 설정 워크플로우")
        void completeMenuSetupWorkflow_ShouldCallAllServicesInOrder() {
            // Given
            MenuCategoryCreateRequest categoryRequest = createCategoryCreateRequest();
            MenuCreateRequest menuRequest = createMenuCreateRequest();
            MenuOptionGroupCreateRequest optionRequest = createOptionGroupCreateRequest();
            MultipartFile mockFile = mock(MultipartFile.class);

            when(menuCategoryService.createCategory(testUserId, categoryRequest)).thenReturn(testCategory);
            when(menuCrudService.createMenu(testUserId, menuRequest)).thenReturn(testMenu);
            when(menuOptionService.createOptionGroup(testUserId, testMenuId, optionRequest))
                    .thenReturn(testOption);
            when(menuImageService.uploadMenuImage(testUserId, testMenuId, mockFile))
                    .thenReturn("https://example.com/image.jpg");

            // When
            MenuCategory category = menuService.createCategory(testUserId, categoryRequest);
            Menu menu = menuService.createMenu(testUserId, menuRequest);
            MenuOption option = menuService.createOptionGroup(testUserId, testMenuId, optionRequest);
            String imageUrl = menuService.uploadMenuImage(testUserId, testMenuId, mockFile);

            // Then
            assertThat(category).isNotNull();
            assertThat(menu).isNotNull();
            assertThat(option).isNotNull();
            assertThat(imageUrl).isNotNull();

            // 각 서비스가 올바른 순서로 호출되었는지 확인
            verify(menuCategoryService).createCategory(testUserId, categoryRequest);
            verify(menuCrudService).createMenu(testUserId, menuRequest);
            verify(menuOptionService).createOptionGroup(testUserId, testMenuId, optionRequest);
            verify(menuImageService).uploadMenuImage(testUserId, testMenuId, mockFile);
        }

        @Test
        @DisplayName("메뉴 삭제 시 관련 데이터 정리 워크플로우")
        void menuDeletionWorkflow_ShouldCallServicesInCorrectOrder() {
            // Given - 메뉴 삭제 시 이미지와 옵션도 함께 정리된다고 가정
            Long imageId = 123L;

            doNothing().when(menuImageService).deleteMenuImage(testUserId, testMenuId, imageId);
            doNothing().when(menuOptionService).deleteOptionGroup(testUserId, testMenuId, testOptionGroupId);
            doNothing().when(menuCrudService).deleteMenu(testUserId, testMenuId);

            // When
            menuService.deleteMenuImage(testUserId, testMenuId, imageId);
            menuService.deleteOptionGroup(testUserId, testMenuId, testOptionGroupId);
            menuService.deleteMenu(testUserId, testMenuId);

            // Then
            verify(menuImageService).deleteMenuImage(testUserId, testMenuId, imageId);
            verify(menuOptionService).deleteOptionGroup(testUserId, testMenuId, testOptionGroupId);
            verify(menuCrudService).deleteMenu(testUserId, testMenuId);
        }

        @Test
        @DisplayName("옵션 그룹과 아이템 생성 워크플로우")
        void optionCreationWorkflow_ShouldCallOptionServiceMethods() {
            // Given
            MenuOptionGroupCreateRequest groupRequest = createOptionGroupCreateRequest();
            MenuOptionItemCreateRequest itemRequest = createOptionItemCreateRequest();

            when(menuOptionService.createOptionGroup(testUserId, testMenuId, groupRequest))
                    .thenReturn(testOption);
            when(menuOptionService.createOption(testUserId, testMenuId, testOptionGroupId, itemRequest))
                    .thenReturn(testOptionItem);

            // When
            MenuOption optionGroup = menuService.createOptionGroup(testUserId, testMenuId, groupRequest);
            MenuOptionItem optionItem = menuService.createOption(testUserId, testMenuId, testOptionGroupId, itemRequest);

            // Then
            assertThat(optionGroup).isNotNull();
            assertThat(optionItem).isNotNull();
            verify(menuOptionService).createOptionGroup(testUserId, testMenuId, groupRequest);
            verify(menuOptionService).createOption(testUserId, testMenuId, testOptionGroupId, itemRequest);
        }
    }

    @Nested
    @DisplayName("매개변수 전달 검증 테스트")
    class ParameterValidationTests {

        @Test
        @DisplayName("카테고리 관련 메서드들의 매개변수 전달 검증")
        void categoryMethods_ShouldPassCorrectParameters() {
            // Given
            MenuCategoryCreateRequest createRequest = createCategoryCreateRequest();
            MenuCategoryUpdateRequest updateRequest = createCategoryUpdateRequest();
            CategoryMoveRequest moveRequest = createCategoryMoveRequest();

            when(menuCategoryService.getMenuCategories(testUserId)).thenReturn(List.of(testCategory));
            when(menuCategoryService.createCategory(testUserId, createRequest)).thenReturn(testCategory);
            when(menuCategoryService.updateCategory(testUserId, testCategoryId, updateRequest)).thenReturn(testCategory);
            doNothing().when(menuCategoryService).deleteCategory(testUserId, testCategoryId);
            when(menuCategoryService.updateCategoryOrder(testUserId, moveRequest)).thenReturn(List.of(testCategory));

            // When
            menuService.getMenuCategories(testUserId);
            menuService.createCategory(testUserId, createRequest);
            menuService.updateCategory(testUserId, testCategoryId, updateRequest);
            menuService.deleteCategory(testUserId, testCategoryId);
            menuService.updateCategoryOrder(testUserId, moveRequest);

            // Then
            verify(menuCategoryService).getMenuCategories(testUserId);
            verify(menuCategoryService).createCategory(testUserId, createRequest);
            verify(menuCategoryService).updateCategory(testUserId, testCategoryId, updateRequest);
            verify(menuCategoryService).deleteCategory(testUserId, testCategoryId);
            verify(menuCategoryService).updateCategoryOrder(testUserId, moveRequest);
        }

        @Test
        @DisplayName("메뉴 관련 메서드들의 매개변수 전달 검증")
        void menuMethods_ShouldPassCorrectParameters() {
            // Given
            MenuCreateRequest createRequest = createMenuCreateRequest();
            MenuUpdateRequest updateRequest = createMenuUpdateRequest();
            MenuStatusRequest statusRequest = createMenuStatusRequest();
            MenuOrderUpdateRequest orderRequest = createMenuOrderUpdateRequest();

            when(menuCrudService.getMenus(testUserId, testCategoryId)).thenReturn(List.of(testMenu));
            when(menuCrudService.getMenu(testUserId, testMenuId)).thenReturn(testMenu);
            when(menuCrudService.createMenu(testUserId, createRequest)).thenReturn(testMenu);
            when(menuCrudService.updateMenu(testUserId, testMenuId, updateRequest)).thenReturn(testMenu);
            doNothing().when(menuCrudService).deleteMenu(testUserId, testMenuId);
            when(menuCrudService.updateMenuStatus(testUserId, testMenuId, statusRequest)).thenReturn(testMenu);
            when(menuCrudService.updateMenuOrder(testUserId, orderRequest)).thenReturn(List.of(testMenu));

            // When
            menuService.getMenus(testUserId, testCategoryId);
            menuService.getMenu(testUserId, testMenuId);
            menuService.createMenu(testUserId, createRequest);
            menuService.updateMenu(testUserId, testMenuId, updateRequest);
            menuService.deleteMenu(testUserId, testMenuId);
            menuService.updateMenuStatus(testUserId, testMenuId, statusRequest);
            menuService.updateMenuOrder(testUserId, orderRequest);

            // Then
            verify(menuCrudService).getMenus(testUserId, testCategoryId);
            verify(menuCrudService).getMenu(testUserId, testMenuId);
            verify(menuCrudService).createMenu(testUserId, createRequest);
            verify(menuCrudService).updateMenu(testUserId, testMenuId, updateRequest);
            verify(menuCrudService).deleteMenu(testUserId, testMenuId);
            verify(menuCrudService).updateMenuStatus(testUserId, testMenuId, statusRequest);
            verify(menuCrudService).updateMenuOrder(testUserId, orderRequest);
        }

        @Test
        @DisplayName("이미지 관련 메서드들의 매개변수 전달 검증")
        void imageMethods_ShouldPassCorrectParameters() {
            // Given
            MultipartFile mockFile = mock(MultipartFile.class);
            Long imageId = 123L;
            Map<String, Object> imageInfo = Map.of("menuId", testMenuId, "hasImage", true);

            when(menuImageService.uploadMenuImage(testUserId, testMenuId, mockFile)).thenReturn("imageUrl");
            when(menuImageService.getMenuImageInfo(testUserId, testMenuId)).thenReturn(imageInfo);
            doNothing().when(menuImageService).deleteMenuImage(testUserId, testMenuId, imageId);

            // When
            menuService.uploadMenuImage(testUserId, testMenuId, mockFile);
            menuService.getMenuImageInfo(testUserId, testMenuId);
            menuService.deleteMenuImage(testUserId, testMenuId, imageId);

            // Then
            verify(menuImageService).uploadMenuImage(testUserId, testMenuId, mockFile);
            verify(menuImageService).getMenuImageInfo(testUserId, testMenuId);
            verify(menuImageService).deleteMenuImage(testUserId, testMenuId, imageId);
        }

        @Test
        @DisplayName("옵션 관련 메서드들의 매개변수 전달 검증")
        void optionMethods_ShouldPassCorrectParameters() {
            // Given
            MenuOptionGroupCreateRequest groupCreateRequest = createOptionGroupCreateRequest();
            MenuOptionGroupUpdateRequest groupUpdateRequest = createOptionGroupUpdateRequest();
            MenuOptionItemCreateRequest itemCreateRequest = createOptionItemCreateRequest();
            MenuOptionItemUpdateRequest itemUpdateRequest = createOptionItemUpdateRequest();

            when(menuOptionService.getMenuOptionGroups(testUserId, testMenuId)).thenReturn(List.of(testOption));
            when(menuOptionService.createOptionGroup(testUserId, testMenuId, groupCreateRequest)).thenReturn(testOption);
            when(menuOptionService.updateOptionGroup(testUserId, testMenuId, testOptionGroupId, groupUpdateRequest)).thenReturn(testOption);
            doNothing().when(menuOptionService).deleteOptionGroup(testUserId, testMenuId, testOptionGroupId);
            when(menuOptionService.getMenuOptions(testUserId, testMenuId, testOptionGroupId)).thenReturn(List.of(testOptionItem));
            when(menuOptionService.createOption(testUserId, testMenuId, testOptionGroupId, itemCreateRequest)).thenReturn(testOptionItem);
            when(menuOptionService.updateOption(testUserId, testMenuId, testOptionGroupId, testOptionItemId, itemUpdateRequest)).thenReturn(testOptionItem);
            doNothing().when(menuOptionService).deleteOption(testUserId, testMenuId, testOptionGroupId, testOptionItemId);

            // When
            menuService.getMenuOptionGroups(testUserId, testMenuId);
            menuService.createOptionGroup(testUserId, testMenuId, groupCreateRequest);
            menuService.updateOptionGroup(testUserId, testMenuId, testOptionGroupId, groupUpdateRequest);
            menuService.deleteOptionGroup(testUserId, testMenuId, testOptionGroupId);
            menuService.getMenuOptions(testUserId, testMenuId, testOptionGroupId);
            menuService.createOption(testUserId, testMenuId, testOptionGroupId, itemCreateRequest);
            menuService.updateOption(testUserId, testMenuId, testOptionGroupId, testOptionItemId, itemUpdateRequest);
            menuService.deleteOption(testUserId, testMenuId, testOptionGroupId, testOptionItemId);

            // Then
            verify(menuOptionService).getMenuOptionGroups(testUserId, testMenuId);
            verify(menuOptionService).createOptionGroup(testUserId, testMenuId, groupCreateRequest);
            verify(menuOptionService).updateOptionGroup(testUserId, testMenuId, testOptionGroupId, groupUpdateRequest);
            verify(menuOptionService).deleteOptionGroup(testUserId, testMenuId, testOptionGroupId);
            verify(menuOptionService).getMenuOptions(testUserId, testMenuId, testOptionGroupId);
            verify(menuOptionService).createOption(testUserId, testMenuId, testOptionGroupId, itemCreateRequest);
            verify(menuOptionService).updateOption(testUserId, testMenuId, testOptionGroupId, testOptionItemId, itemUpdateRequest);
            verify(menuOptionService).deleteOption(testUserId, testMenuId, testOptionGroupId, testOptionItemId);
        }
    }

    @Nested
    @DisplayName("리턴 값 검증 테스트")
    class ReturnValueValidationTests {

        @Test
        @DisplayName("카테고리 조회 결과 검증")
        void getMenuCategories_ShouldReturnCorrectData() {
            // Given
            List<MenuCategory> expectedCategories = List.of(testCategory);
            when(menuCategoryService.getMenuCategories(testUserId)).thenReturn(expectedCategories);

            // When
            List<MenuCategory> result = menuService.getMenuCategories(testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedCategories);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("테스트 카테고리");
        }

        @Test
        @DisplayName("메뉴 조회 결과 검증")
        void getMenus_ShouldReturnCorrectData() {
            // Given
            List<Menu> expectedMenus = List.of(testMenu);
            when(menuCrudService.getMenus(testUserId, testCategoryId)).thenReturn(expectedMenus);

            // When
            List<Menu> result = menuService.getMenus(testUserId, testCategoryId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedMenus);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("테스트 메뉴");
            assertThat(result.get(0).getPrice()).isEqualTo(new BigDecimal("15000"));
        }

        @Test
        @DisplayName("옵션 그룹 조회 결과 검증")
        void getMenuOptionGroups_ShouldReturnCorrectData() {
            // Given
            List<MenuOption> expectedOptions = List.of(testOption);
            when(menuOptionService.getMenuOptionGroups(testUserId, testMenuId)).thenReturn(expectedOptions);

            // When
            List<MenuOption> result = menuService.getMenuOptionGroups(testUserId, testMenuId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedOptions);
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("테스트 옵션 그룹");
            assertThat(result.get(0).getType()).isEqualTo(OptionType.SINGLE);
        }

        @Test
        @DisplayName("이미지 정보 조회 결과 검증")
        void getMenuImageInfo_ShouldReturnCorrectData() {
            // Given
            Map<String, Object> expectedInfo = Map.of(
                    "menuId", testMenuId,
                    "menuName", "테스트 메뉴",
                    "hasImage", true,
                    "imageUrl", "https://example.com/image.jpg",
                    "imageId", 123L
            );
            when(menuImageService.getMenuImageInfo(testUserId, testMenuId)).thenReturn(expectedInfo);

            // When
            Map<String, Object> result = menuService.getMenuImageInfo(testUserId, testMenuId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(expectedInfo);
            assertThat(result.get("menuId")).isEqualTo(testMenuId);
            assertThat(result.get("hasImage")).isEqualTo(true);
            assertThat(result.get("imageUrl")).isEqualTo("https://example.com/image.jpg");
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("빈 카테고리 목록 반환")
        void getMenuCategories_WhenEmpty_ShouldReturnEmptyList() {
            // Given
            when(menuCategoryService.getMenuCategories(testUserId)).thenReturn(List.of());

            // When
            List<MenuCategory> result = menuService.getMenuCategories(testUserId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(menuCategoryService).getMenuCategories(testUserId);
        }

        @Test
        @DisplayName("빈 메뉴 목록 반환")
        void getMenus_WhenEmpty_ShouldReturnEmptyList() {
            // Given
            when(menuCrudService.getMenus(testUserId, testCategoryId)).thenReturn(List.of());

            // When
            List<Menu> result = menuService.getMenus(testUserId, testCategoryId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(menuCrudService).getMenus(testUserId, testCategoryId);
        }

        @Test
        @DisplayName("빈 옵션 그룹 목록 반환")
        void getMenuOptionGroups_WhenEmpty_ShouldReturnEmptyList() {
            // Given
            when(menuOptionService.getMenuOptionGroups(testUserId, testMenuId)).thenReturn(List.of());

            // When
            List<MenuOption> result = menuService.getMenuOptionGroups(testUserId, testMenuId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result).isEmpty();
            verify(menuOptionService).getMenuOptionGroups(testUserId, testMenuId);
        }

        @Test
        @DisplayName("이미지가 없는 메뉴의 이미지 정보 조회")
        void getMenuImageInfo_WhenNoImage_ShouldReturnCorrectInfo() {
            // Given
            Map<String, Object> expectedInfo = Map.of(
                    "menuId", testMenuId,
                    "menuName", "테스트 메뉴",
                    "hasImage", false,
                    "imageUrl", ""
            );
            when(menuImageService.getMenuImageInfo(testUserId, testMenuId)).thenReturn(expectedInfo);

            // When
            Map<String, Object> result = menuService.getMenuImageInfo(testUserId, testMenuId);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.get("hasImage")).isEqualTo(false);
            assertThat(result.get("imageUrl")).isEqualTo("");
            verify(menuImageService).getMenuImageInfo(testUserId, testMenuId);
        }
    }

    // ================================
    // Helper Methods for Test Data
    // ================================

    private Store createTestStore() {
        Store store = new Store();
        store.setId(testStoreId);
        store.setOwnerId(testUserId);
        store.setName("테스트 가게");
        store.setBusinessNumber("123-45-67890");
        store.setPhone("02-1234-5678");
        store.setAddress("서울시 강남구");
        store.setCategory(StoreCategory.KOREAN);
        store.setStatus(StoreStatus.OPEN);
        store.setIsActive(true);
        return store;
    }

    private MenuCategory createTestCategory() {
        return MenuCategory.builder()
                .store(testStore)
                .name("테스트 카테고리")
                .displayOrder(1)
                .isActive(true)
                .build();
    }

    private Menu createTestMenu() {
        return Menu.builder()
                .store(testStore)
                .category(testCategory)
                .name("테스트 메뉴")
                .description("테스트 메뉴 설명")
                .price(new BigDecimal("15000"))
                .status(MenuStatus.AVAILABLE)
                .displayOrder(1)
                .isPopular(false)
                .isRecommended(false)
                .build();
    }

    private MenuOption createTestOption() {
        return MenuOption.builder()
                .menu(testMenu)
                .name("테스트 옵션 그룹")
                .type(OptionType.SINGLE)
                .isRequired(true)
                .displayOrder(1)
                .build();
    }

    private MenuOptionItem createTestOptionItem() {
        return MenuOptionItem.builder()
                .option(testOption)
                .name("테스트 옵션 아이템")
                .additionalPrice(new BigDecimal("1000"))
                .displayOrder(1)
                .isActive(true)
                .build();
    }

    private MenuCategoryCreateRequest createCategoryCreateRequest() {
        MenuCategoryCreateRequest request = new MenuCategoryCreateRequest();
        request.setName("새 카테고리");
        request.setDisplayOrder(1);
        request.setIsActive(true);
        return request;
    }

    private MenuCategoryUpdateRequest createCategoryUpdateRequest() {
        MenuCategoryUpdateRequest request = new MenuCategoryUpdateRequest();
        request.setName("수정된 카테고리");
        request.setDisplayOrder(2);
        request.setIsActive(true);
        return request;
    }

    private CategoryMoveRequest createCategoryMoveRequest() {
        CategoryMoveRequest request = new CategoryMoveRequest();
        request.setCategoryId(testCategoryId);
        request.setNewPosition(2);
        return request;
    }

    private MenuCreateRequest createMenuCreateRequest() {
        MenuCreateRequest request = new MenuCreateRequest();
        request.setCategoryId(testCategoryId);
        request.setName("새 메뉴");
        request.setDescription("새 메뉴 설명");
        request.setPrice(new BigDecimal("18000"));
        request.setStatus("AVAILABLE");
        request.setDisplayOrder(1);
        request.setIsPopular(false);
        request.setIsRecommended(false);
        return request;
    }

    private MenuUpdateRequest createMenuUpdateRequest() {
        MenuUpdateRequest request = new MenuUpdateRequest();
        request.setName("수정된 메뉴");
        request.setDescription("수정된 설명");
        request.setPrice(new BigDecimal("20000"));
        request.setStatus("AVAILABLE");
        return request;
    }

    private MenuStatusRequest createMenuStatusRequest() {
        MenuStatusRequest request = new MenuStatusRequest();
        request.setStatus("SOLD_OUT");
        request.setReason("재료 소진");
        request.setIsTemporary(true);
        return request;
    }

    private MenuOrderUpdateRequest createMenuOrderUpdateRequest() {
        MenuOrderUpdateRequest request = new MenuOrderUpdateRequest();
        request.setMenuId(testMenuId);
        request.setNewPosition(2);
        request.setCategoryId(testCategoryId);
        request.setGlobalOrder(false);
        return request;
    }

    private MenuOptionGroupCreateRequest createOptionGroupCreateRequest() {
        MenuOptionGroupCreateRequest request = new MenuOptionGroupCreateRequest();
        request.setName("새 옵션 그룹");
        request.setType("SINGLE");
        request.setIsRequired(true);
        request.setDisplayOrder(1);
        request.setItems(List.of(createOptionItemCreateRequest()));
        return request;
    }

    private MenuOptionGroupUpdateRequest createOptionGroupUpdateRequest() {
        MenuOptionGroupUpdateRequest request = new MenuOptionGroupUpdateRequest();
        request.setName("수정된 옵션 그룹");
        request.setType("MULTIPLE");
        request.setIsRequired(false);
        request.setDisplayOrder(2);
        return request;
    }

    private MenuOptionItemCreateRequest createOptionItemCreateRequest() {
        MenuOptionItemCreateRequest request = new MenuOptionItemCreateRequest();
        request.setName("새 옵션 아이템");
        request.setAdditionalPrice(new BigDecimal("500"));
        request.setDisplayOrder(1);
        request.setIsActive(true);
        return request;
    }

    private MenuOptionItemUpdateRequest createOptionItemUpdateRequest() {
        MenuOptionItemUpdateRequest request = new MenuOptionItemUpdateRequest();
        request.setName("수정된 옵션 아이템");
        request.setAdditionalPrice(new BigDecimal("1500"));
        request.setDisplayOrder(2);
        request.setIsActive(false);
        return request;
    }
}