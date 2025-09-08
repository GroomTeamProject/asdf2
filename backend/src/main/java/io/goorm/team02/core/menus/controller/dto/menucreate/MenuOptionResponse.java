// MenuOptionResponse.java
package io.goorm.team02.core.menus.controller.dto.menucreate;

import io.goorm.team02.core.menus.domain.MenuOption;
import io.goorm.team02.core.menus.domain.enums.OptionType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "메뉴 옵션 응답")
public class MenuOptionResponse {

    @Schema(description = "옵션 ID", example = "1")
    private Long id;

    @Schema(description = "옵션명", example = "사이즈")
    private String name;

    @Schema(description = "옵션 타입", example = "SINGLE")
    private OptionType type;

    @Schema(description = "필수 옵션 여부", example = "true")
    private Boolean isRequired;

    @Schema(description = "옵션 표시 순서", example = "1")
    private Integer displayOrder;

    @Schema(description = "옵션 아이템 목록")
    private List<MenuOptionItemResponse> items;

    public static MenuOptionResponse from(MenuOption option) {
        return MenuOptionResponse.builder()
                .id(option.getId())
                .name(option.getName())
                .type(option.getType())
                .isRequired(option.getIsRequired())
                .displayOrder(option.getDisplayOrder())
                .items(option.getItems() != null ?
                        option.getItems().stream()
                                .map(MenuOptionItemResponse::from)
                                .toList() : null)
                .build();
    }
}