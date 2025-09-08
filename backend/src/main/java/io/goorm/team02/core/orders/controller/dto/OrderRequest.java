package io.goorm.team02.core.orders.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class OrderRequest {

	@JsonProperty("delivery_address")
	private String deliveryAddress;

	@JsonProperty("delivery_detail_address")
	private String deliveryDetailAddress;

	private String phone;

	@JsonProperty("orderMemo")
	private String orderMemo;

	private List<ItemRequest> items;

	@JsonProperty("totalAmount")
	private int totalAmount;

	private CustomerRequest customer;

	@Getter
	@Setter
	public static class ItemRequest {
		@JsonProperty("menu_id")
		private Long menuId;
		@JsonProperty("menu_name")
		private String menuName;
		private int quantity;
		@JsonProperty("total_price")
		private int totalPrice;
		private List<String> options;
	}

	@Getter
	@Setter
	public static class CustomerRequest {
		private String name;
		@JsonProperty("cardNumber")
		private String cardNumber;
		private String expiry;
		private String cvc;
	}
}
