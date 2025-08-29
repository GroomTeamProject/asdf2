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
import java.math.BigDecimal;
import java.util.List;

@Entity
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

}
