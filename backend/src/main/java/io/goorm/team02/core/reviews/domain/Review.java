package io.goorm.team02.core.reviews.domain;

import io.goorm.team02.core.common.domain.BaseEntity;
import io.goorm.team02.core.orders.domain.Order;
import io.goorm.team02.core.stores.domain.Store;
import io.goorm.team02.core.users.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@ManyToOne
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@Column(nullable = false)
	private Integer rating; // 1~5 점수

	@Column(columnDefinition = "TEXT")
	private String content;

	@Column(columnDefinition = "JSON")
	private String imageUrls; // ["url1", "url2"]

	private String ownerReply;
	private LocalDateTime ownerRepliedAt;

	@Column(nullable = false)
	private Boolean isReported = false;

}
