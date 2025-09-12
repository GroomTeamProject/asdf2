package io.goorm.team02.core.reviews.controller;

import io.goorm.team02.core.reviews.controller.dto.OwnerReplyRequest;
import io.goorm.team02.core.reviews.controller.dto.ReviewRequest;
import io.goorm.team02.core.reviews.controller.dto.ReviewResponse;
import io.goorm.team02.core.reviews.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController implements ReviewControllerDocs {

    private final ReviewService reviewService;

    @PostMapping
    public ReviewResponse create(@RequestBody ReviewRequest reviewRequest) {
        return reviewService.create(reviewRequest);
    }

    @GetMapping("/store")
    public List<ReviewResponse> getAllByStoreId(@RequestParam("storeId") Long storeId) {
        return reviewService.getAllByStoreId(storeId);
    }

    @GetMapping("/user")
    public List<ReviewResponse> getAllByUserId(@RequestParam("userId") Long userId) {
        return reviewService.getAllByUserId(userId);
    }

    @GetMapping("/{reviewId}")
    public ReviewResponse getById(@PathVariable Long reviewId) {
        return reviewService.getById(reviewId);
    }

    @PutMapping("/{reviewId}")
    public ReviewResponse update(@PathVariable Long reviewId, @RequestBody ReviewRequest reviewRequest) {
        return reviewService.update(reviewId, reviewRequest);
    }

    @DeleteMapping("/{reviewId}")
    public void delete(@PathVariable Long reviewId) {
        reviewService.delete(reviewId);
    }

    @PostMapping("/{reviewId}/reply")
    public ReviewResponse addOwnerReply(@PathVariable Long reviewId, @RequestBody OwnerReplyRequest request) {
        return reviewService.addOwnerReply(reviewId, request.reply());
    }

    @PostMapping("/{reviewId}/report")
    public ReviewResponse report(@PathVariable Long reviewId) {
        return reviewService.report(reviewId);
    }

    @GetMapping("/store/rating")
    public Double getAverageRatingByStoreId(@RequestParam("storeId") Long storeId) {
        return reviewService.getAverageRatingByStoreId(storeId);
    }

    @GetMapping("/store/count")
    public Long getReviewCountByStoreId(@RequestParam("storeId") Long storeId) {
        return reviewService.getReviewCountByStoreId(storeId);
    }
}
