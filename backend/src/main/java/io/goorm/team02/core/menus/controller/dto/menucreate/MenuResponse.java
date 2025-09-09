// MenuResponse.java
package io.goorm.team02.core.menus.controller.dto.menucreate;

import io.goorm.team02.core.menus.domain.Menu;
import io.goorm.team02.core.menus.domain.enums.MenuStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "메뉴 응답")
public class MenuResponse {

    @Schema(description = "메뉴 ID", example = "1")
    private Long id;

    @Schema(description = "메뉴명", example = "김치찌개")
    private String name;

    @Schema(description = "메뉴 설명", example = "집에서 직접 담근 김치로 끓인 얼큰한 김치찌개")
    private String description;

    @Schema(description = "메뉴 가격", example = "8000.00")
    private BigDecimal price;

    @Schema(description = "카테고리 ID", example = "1")
    private Long categoryId;

    @Schema(description = "카테고리명", example = "메인")
    private String categoryName;

    @Schema(description = "메뉴 이미지 URL", example = "https://example.com/images/kimchi-jjigae.jpg")
    private String imageUrl;

    @Schema(description = "인기 메뉴 여부", example = "false")
    private Boolean isPopular;

    @Schema(description = "추천 메뉴 여부", example = "false")
    private Boolean isRecommended;

    @Schema(description = "메뉴 판매 상태", example = "AVAILABLE")
    private MenuStatus status;

    @Schema(description = "메뉴 표시 순서", example = "1")
    private Integer displayOrder;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Schema(description = "메뉴 옵션 목록")
    private List<MenuOptionResponse> options;

    public static MenuResponse from(Menu menu) {
        return MenuResponse.builder()
                .id(menu.getId())
                .name(menu.getName())
                .description(menu.getDescription())
                .price(menu.getPrice())
                .categoryId(menu.getCategory() != null ? menu.getCategory().getId() : null)
                .categoryName(menu.getCategory() != null ? menu.getCategory().getName() : null)
                .imageUrl(menu.getImageUrl())
                .isPopular(menu.getIsPopular())
                .isRecommended(menu.getIsRecommended())
                .status(menu.getStatus())
                .displayOrder(menu.getDisplayOrder())
                .createdAt(menu.getCreatedAt())
                .updatedAt(menu.getUpdatedAt())
                .options(menu.getOptions() != null ?
                        menu.getOptions().stream()
                                .map(MenuOptionResponse::from)
                                .toList() : null)
                .build();
    }
}