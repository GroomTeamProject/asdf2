package io.goorm.team02.review.controller.dto;

import io.goorm.team02.review.entity.Review;

import java.time.LocalDateTime;

/**
 * 리뷰 응답 DTO
 */
public record ReviewResponse(
        Long id,
        Long orderId,
        Long userId,
        Long storeId,
        Integer rating,
        String content,
        String ownerReply,
        LocalDateTime ownerRepliedAt,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getOrderId(),
                review.getUserId(),
                review.getStoreId(),
                review.getRating(),
                review.getContent(),
                review.getOwnerReply(),
                review.getOwnerRepliedAt(),
                review.getCreatedAt(),
                review.getUpdatedAt());
    }
}
