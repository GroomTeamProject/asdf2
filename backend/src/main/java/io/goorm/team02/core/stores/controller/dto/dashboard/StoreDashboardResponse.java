package io.goorm.team02.core.stores.controller.dto.dashboard;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "가게 대시보드 응답")
public record StoreDashboardResponse(
        @Schema(description = "오늘 주문 개수", example = "15")
        Long todayOrderCount,

        @Schema(description = "오늘 매출", example = "450000")
        BigDecimal todayRevenue,

        @Schema(description = "가게 평점", example = "4.5")
        BigDecimal storeRating,

        @Schema(description = "리뷰 개수", example = "127")
        Long reviewCount,

        @Schema(description = "오늘 운영시간", example = "11:00 - 23:00")
        String todayOperatingHours,

        @Schema(description = "총 주문 개수", example = "1,234")
        Long totalOrderCount,

        @Schema(description = "최근 주문 정보")
        List<RecentOrderInfo> recentOrders
) {}