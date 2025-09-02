import db from './db.js'; // MariaDB pool 연결 파일

/**
 * cart = [{id:1, name:'상품 A', price:10000, quantity:2}, ...]
 * customerInfo = { name: '홍길동' }
 */
export async function createOrder(cart, customerInfo) {
    const conn = await db.getConnection();
    try {
        await conn.beginTransaction(); // 트랜잭션 시작

        // 총 금액 계산
        const totalPrice = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

        // 주문 생성
        const orderResult = await conn.query(
            'INSERT INTO orders(total_price, status) VALUES (?, ?)',
            [totalPrice, 'paid'] // 가짜 결제이므로 바로 'paid'로
        );
        const orderId = orderResult.insertId;

        // 주문 상세 기록
        for (const item of cart) {
            await conn.query(
                'INSERT INTO order_items(order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)',
                [orderId, item.id, item.quantity, item.price]
            );
        }

        await conn.commit(); // 트랜잭션 커밋
        return { success: true, orderId, totalPrice };
    } catch (err) {
        await conn.rollback(); // 오류 시 롤백
        console.error('주문 생성 실패:', err);
        throw err;
    } finally {
        conn.release();
    }
}
