import axios from 'axios';

const API_URL = 'http://localhost:3000/api/payment';

export async function makePayment(cart, customerInfo) {
    try {
        const response = await axios.post(API_URL, { cart, customer: customerInfo });
        return response.data;
    } catch (err) {
        console.error('결제 실패:', err);
        throw err;
    }
}
