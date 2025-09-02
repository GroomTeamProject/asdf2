import mariadb from 'mariadb';

// DB 커넥션 풀 생성
const pool = mariadb.createPool({
    host: 'localhost',
    user: 'root',        // DB 사용자
    password: 'Wnsrns3224!', // 설정한 비밀번호
    database: 'quickdeliver', // 사용할 DB
    connectionLimit: 5
});

export default pool;
