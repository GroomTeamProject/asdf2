package io.goorm.team02.core.menus.controller.dto.categorycreate;

import io.goorm.team02.core.menus.domain.MenuCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "메뉴 카테고리 응답")
public class MenuCategoryResponse {

    @Schema(description = "카테고리 ID", example = "1")
    private Long id;

    @Schema(description = "가게 ID", example = "1")
    private Long storeId;

    @Schema(description = "카테고리명", example = "메인메뉴")
    private String name;

    @Schema(description = "카테고리 표시 순서", example = "1")
    private Integer displayOrder;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "메뉴 개수", example = "5")
    private Integer menuCount;

    public static MenuCategoryResponse from(MenuCategory category) {
        return MenuCategoryResponse.builder()
                .id(category.getId())
                .storeId(category.getStore() != null ? category.getStore().getId() : null)
                .name(category.getName())
                .displayOrder(category.getDisplayOrder())
                .isActive(category.getIsActive())
                .menuCount(category.getMenus() != null ? category.getMenus().size() : 0)
                .build();
    }
}