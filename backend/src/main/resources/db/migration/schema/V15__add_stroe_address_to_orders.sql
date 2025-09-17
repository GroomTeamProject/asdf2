-- V15__add_stroe_address_to_orders.sql
-- orders 테이블에 store_address, store_detail_address 칼럼 추가

ALTER TABLE orders ADD COLUMN store_address VARCHAR(200) NOT NULL;
ALTER TABLE orders ADD COLUMN store_detail_address VARCHAR(100) NOT NULL;
    