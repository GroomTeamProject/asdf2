package io.goorm.team02.core.menus.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "menu_option_items")
public class MenuOptionItem {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "option_id", nullable = false)
	private MenuOption option;

	@Column(nullable = false, length = 50)
	private String name;

	@Column(precision = 10, scale = 2)
	private BigDecimal additionalPrice = BigDecimal.ZERO;

	private Integer displayOrder = 0;

	@Column(nullable = false)
	private Boolean isActive = true;

}
