package io.goorm.team02.core.reviews.service;

import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.orders.repository.OrderRepository;
import io.goorm.team02.core.reviews.controller.dto.ReviewRequest;
import io.goorm.team02.core.reviews.controller.dto.ReviewResponse;
import io.goorm.team02.core.reviews.domain.Review;
import io.goorm.team02.core.reviews.repository.ReviewRepository;
import io.goorm.team02.core.stores.domain.Store;
import io.goorm.team02.core.users.domain.User;
import io.goorm.team02.core.users.repository.UserinfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final OrderRepository orderRepository;
    private final UserinfoRepository userRepository;

    // ================================
    // API Methods
    // ================================

    /**
     * 리뷰 생성
     */
    @Transactional
    public ReviewResponse create(ReviewRequest reviewRequest) {
        // 1. 사용자 조회 및 검증
        User user = userRepository.findById(reviewRequest.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + reviewRequest.userId()));

        // 2. 주문 조회 및 검증
        Order order = orderRepository.findById(reviewRequest.orderId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다: " + reviewRequest.orderId()));

        // 3. 주문이 해당 사용자의 것인지 확인
        if (!order.getUser().getId().equals(reviewRequest.userId())) {
            throw new IllegalStateException("본인의 주문에만 리뷰를 작성할 수 있습니다");
        }

        // 4. 이미 리뷰가 작성되었는지 확인
        if (reviewRepository.findByOrderId(reviewRequest.orderId()).isPresent()) {
            throw new IllegalStateException("이미 리뷰가 작성된 주문입니다");
        }

        // 5. 엔티티 참조 조회
        Store store = order.getStore();

        // 6. Review 도메인에 위임
        Review review = Review.create(order, user, store, reviewRequest.rating(), reviewRequest.content());

        // 7. 저장
        Review savedReview = reviewRepository.save(review);
        return ReviewResponse.from(savedReview);
    }

    /**
     * 가게별 리뷰 목록 조회
     */
    public List<ReviewResponse> getAllByStoreId(Long storeId) {
        List<Review> reviews = reviewRepository.findAllByStoreIdWithDetails(storeId);
        return reviews.stream()
                .map(ReviewResponse::from)
                .toList();
    }

    /**
     * 사용자별 리뷰 목록 조회
     */
    public List<ReviewResponse> getAllByUserId(Long userId) {
        List<Review> reviews = reviewRepository.findAllByUserIdWithDetails(userId);
        return reviews.stream()
                .map(ReviewResponse::from)
                .toList();
    }

    /**
     * 리뷰 상세 조회
     */
    public ReviewResponse getById(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다: " + reviewId));
        return ReviewResponse.from(review);
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public ReviewResponse update(Long reviewId, ReviewRequest reviewRequest) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다: " + reviewId));

        // 리뷰 수정
        review.setRating(reviewRequest.rating());
        review.setContent(reviewRequest.content());

        Review savedReview = reviewRepository.save(review);
        return ReviewResponse.from(savedReview);
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public void delete(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다: " + reviewId));

        reviewRepository.delete(review);
    }

    /**
     * 사장님 답글 작성
     */
    @Transactional
    public ReviewResponse addOwnerReply(Long reviewId, String reply) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다: " + reviewId));

        review.addOwnerReply(reply);
        Review savedReview = reviewRepository.save(review);
        return ReviewResponse.from(savedReview);
    }

    /**
     * 리뷰 신고
     */
    @Transactional
    public ReviewResponse report(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다: " + reviewId));

        review.report();
        Review savedReview = reviewRepository.save(review);
        return ReviewResponse.from(savedReview);
    }

    /**
     * 가게별 평균 평점 조회
     */
    public Double getAverageRatingByStoreId(Long storeId) {
        return reviewRepository.findAverageRatingByStoreId(storeId).doubleValue();
    }

    /**
     * 가게별 리뷰 개수 조회
     */
    public Long getReviewCountByStoreId(Long storeId) {
        return reviewRepository.countByStoreId(storeId);
    }
}
