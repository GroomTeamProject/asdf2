import mariadb from 'mariadb';

// DB 커넥션 풀 생성
const pool = mariadb.createPool({
    host: 'localhost',
    user: 'root',
    password: 'Wnsrns3224!',
    database: 'quickdeliver',
    connectionLimit: 5
});

export default pool;
