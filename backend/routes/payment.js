import express from 'express';
import db from '../db.js'; // MariaDB 연결 모듈

const router = express.Router();

// POST /api/payment
router.post('/', async (req, res) => {
    const { cart, customer } = req.body;

    if (!cart || cart.length === 0) {
        return res.status(400).json({ message: '장바구니가 비어있습니다.' });
    }

    if (!customer || !customer.name) {
        return res.status(400).json({ message: '고객 정보가 없습니다.' });
    }

    try {
        // 결제 로직 (DB 테스트용)
        const totalPrice = cart.reduce((sum, item) => sum + item.price * item.quantity, 0);

        // 트랜잭션 예제
        const conn = await db.getConnection();
        await conn.beginTransaction();

        // orders 테이블에 주문 생성
        const orderResult = await conn.query(
            'INSERT INTO orders(name, total_price) VALUES(?, ?)',
            [customer.name, totalPrice]
        );
        const orderId = orderResult.insertId;

        // order_items 테이블에 상품 추가
        for (let item of cart) {
            await conn.query(
                'INSERT INTO order_items(order_id, product_id, quantity, price) VALUES(?, ?, ?, ?)',
                [orderId, item.id, item.quantity, item.price]
            );
        }

        await conn.commit();
        conn.release();

        res.json({ success: true, paymentId: `TEST-${orderId}` });
    } catch (err) {
        console.error(err);
        res.status(500).json({ success: false, message: '서버 에러 발생!' });
    }
});

export default router;
