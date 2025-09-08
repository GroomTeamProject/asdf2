/*
import express from "express";

const app = express();
app.use(express.json());

app.post("/api/create-payment", async (req, res) => {
    try {
        const secretKey = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6"; // 시크릿 키
        const encrypted = "Basic " + Buffer.from(secretKey + ":").toString("base64");

        const response = await fetch("https://api.tosspayments.com/v1/payments", {
            method: "POST",
            headers: {
                Authorization: encrypted,
                "Content-Type": "application/json",
            },
            body: JSON.stringify({
                amount: 1000,
                orderId: "order-" + new Date().getTime(),
                orderName: "테스트 결제",
                successUrl: "http://localhost:5173/success",
                failUrl: "http://localhost:5173/fail",
            }),
        });

        const data = await response.json();
        if (!response.ok) {
            console.error("결제 생성 실패:", data);
            return res.status(response.status).json(data);
        }

        console.log("결제 생성 성공:", data);
        res.json(data);
    } catch (err) {
        console.error("서버 오류:", err);
        res.status(500).json({ error: "서버 오류 발생" });
    }
});

app.listen(3000, () => console.log("✅ Server running on http://localhost:3000"));

*/

// server.js
import express from "express";
import fetch from "node-fetch"; // Node.js v18 이상이면 내장 fetch 가능
import cors from "cors";

const app = express();
app.use(cors());
app.use(express.json());

const SECRET_KEY = "test_gsk_docs_OaPz8L5KdmQXkzRz3y47BMw6"; // TossPayments 서버 비밀키

// 결제 요청 생성 API
app.post("/api/payments", async (req, res) => {
  const { orderId, orderName, amount } = req.body;

  try {
    const response = await fetch("https://api.tosspayments.com/v1/payments", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Basic ${Buffer.from(SECRET_KEY + ":").toString("base64")}`,
      },
      body: JSON.stringify({
        orderId,
        orderName,
        amount,
        customerEmail: "customer123@gmail.com",
        successUrl: "http://localhost:5173/success",
        failUrl: "http://localhost:5173/fail",
      }),
    });

    const data = await response.json();
    res.json(data);
  } catch (err) {
    console.error("서버 결제 요청 실패:", err);
    res.status(500).json({ error: err.message });
  }
});

app.listen(3000, () => {
  console.log("Server running on http://localhost:3000");
});
