package io.goorm.team02.core.menus.domain;

import io.goorm.team02.core.common.domain.BaseEntity;
import io.goorm.team02.core.menus.domain.enums.MenuStatus;
import io.goorm.team02.core.stores.domain.Store;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "menus")
public class Menu extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "store_id", nullable = false)
	private Store store;

	@ManyToOne
	@JoinColumn(name = "category_id")
	private MenuCategory category;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Column(length = 500)
	private String imageUrl;

	@Column(nullable = false)
	private Boolean isPopular = false;

	@Column(nullable = false)
	private Boolean isRecommended = false;

	@Enumerated(EnumType.STRING)
	private MenuStatus status = MenuStatus.AVAILABLE;

	private Integer displayOrder = 0;

	@OneToMany(mappedBy = "menu", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<MenuOption> options;

	@Builder
	public Menu(Store store, MenuCategory category, String name, String description,
				BigDecimal price, String imageUrl, Boolean isPopular, Boolean isRecommended,
				MenuStatus status, Integer displayOrder) {
		this.store = store;
		this.category = category;
		this.name = name;
		this.description = description;
		this.price = price;
		this.imageUrl = imageUrl;
		this.isPopular = isPopular != null ? isPopular : false;
		this.isRecommended = isRecommended != null ? isRecommended : false;
		this.status = status != null ? status : MenuStatus.AVAILABLE;
		this.displayOrder = displayOrder != null ? displayOrder : 0;
	}

	// 필요한 경우 setter 메서드들 추가
	public void updateName(String name) {
		this.name = name;
	}

	public void updateDescription(String description) {
		this.description = description;
	}

	public void updatePrice(BigDecimal price) {
		this.price = price;
	}

	public void updateImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public void updateStatus(MenuStatus status) {
		this.status = status;
	}

	public void updateDisplayOrder(Integer displayOrder) {
		this.displayOrder = displayOrder;
	}

	public void updateIsPopular(Boolean isPopular) {
		this.isPopular = isPopular;
	}

	public void updateIsRecommended(Boolean isRecommended) {
		this.isRecommended = isRecommended;
	}
}