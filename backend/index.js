import express from 'express';
import cors from 'cors';
import bodyParser from 'body-parser';
import paymentRouter from './routes/payment.js';

const app = express();

// CORS 설정: 프론트 주소 허용
app.use(cors({
    origin: 'http://localhost:5173', // 프론트 포트
    methods: ['GET', 'POST'],
    credentials: true
}));

app.use(bodyParser.json());

// 결제 라우트 연결
app.use('/api/payment', paymentRouter);

const PORT = 3000;
app.listen(PORT, () => {
    console.log(`Server running on http://localhost:${PORT}`);
});
