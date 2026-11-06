-- CineTicket Desktop - MySQL 8 setup and demo data
-- Safe for a fresh database. Existing rows with the same demo IDs are kept.

CREATE DATABASE IF NOT EXISTS cineticket_desktop
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE cineticket_desktop;

CREATE TABLE IF NOT EXISTS users (
    user_id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(30) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    email VARCHAR(120) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_user_role CHECK (role IN ('CUSTOMER','STAFF','ADMIN'))
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS movies (
    movie_id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description TEXT NOT NULL,
    genre VARCHAR(80) NOT NULL,
    duration INT NOT NULL,
    release_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    poster_url VARCHAR(500),
    CONSTRAINT chk_movie_status CHECK (status IN ('COMING_SOON','NOW_SHOWING','INACTIVE')),
    CONSTRAINT chk_movie_duration CHECK (duration > 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cinemas (
    cinema_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    address VARCHAR(255) NOT NULL
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS rooms (
    room_id VARCHAR(36) PRIMARY KEY,
    cinema_id VARCHAR(36) NOT NULL,
    room_name VARCHAR(60) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uq_room_name UNIQUE (cinema_id, room_name),
    CONSTRAINT fk_room_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas(cinema_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS seats (
    seat_id VARCHAR(36) PRIMARY KEY,
    room_id VARCHAR(36) NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    CONSTRAINT uq_room_seat UNIQUE (room_id, seat_number),
    CONSTRAINT chk_seat_type CHECK (type IN ('STANDARD','VIP','COUPLE')),
    CONSTRAINT fk_seat_room FOREIGN KEY (room_id) REFERENCES rooms(room_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS showtimes (
    showtime_id VARCHAR(36) PRIMARY KEY,
    movie_id VARCHAR(36) NOT NULL,
    room_id VARCHAR(36) NOT NULL,
    start_time DATETIME NOT NULL,
    end_time DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    base_price DECIMAL(12,2) NOT NULL,
    CONSTRAINT chk_showtime_status CHECK (status IN ('SCHEDULED','CANCELLED','COMPLETED')),
    CONSTRAINT fk_showtime_movie FOREIGN KEY (movie_id) REFERENCES movies(movie_id),
    CONSTRAINT fk_showtime_room FOREIGN KEY (room_id) REFERENCES rooms(room_id),
    INDEX idx_showtime_movie_time (movie_id, start_time),
    INDEX idx_showtime_room_time (room_id, start_time)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS showtime_seats (
    showtime_seat_id VARCHAR(80) PRIMARY KEY,
    showtime_id VARCHAR(36) NOT NULL,
    seat_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    held_until DATETIME NULL,
    price DECIMAL(12,2) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_showtime_seat UNIQUE (showtime_id, seat_id),
    CONSTRAINT chk_showtime_seat_status CHECK (status IN ('AVAILABLE','HELD','CONFIRMED')),
    CONSTRAINT fk_ss_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(showtime_id),
    CONSTRAINT fk_ss_seat FOREIGN KEY (seat_id) REFERENCES seats(seat_id),
    INDEX idx_ss_availability (showtime_id, status, held_until)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS bookings (
    booking_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    showtime_id VARCHAR(36) NOT NULL,
    booking_time DATETIME NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT chk_booking_status CHECK (status IN ('PENDING','CONFIRMED','CANCELLED','EXPIRED')),
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES users(user_id),
    CONSTRAINT fk_booking_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(showtime_id),
    INDEX idx_booking_customer_time (customer_id, booking_time)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS tickets (
    ticket_id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL,
    showtime_seat_id VARCHAR(80) NOT NULL,
    ticket_code VARCHAR(30) NOT NULL UNIQUE,
    price DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT chk_ticket_status CHECK (status IN ('PENDING','ISSUED','USED','CANCELLED')),
    CONSTRAINT fk_ticket_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    CONSTRAINT fk_ticket_showtime_seat FOREIGN KEY (showtime_seat_id) REFERENCES showtime_seats(showtime_seat_id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS payments (
    payment_id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_time DATETIME NOT NULL,
    method VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    gateway_name VARCHAR(100) NOT NULL,
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING','SUCCESS','FAILED','REFUNDED')),
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id),
    INDEX idx_payment_booking (booking_id),
    INDEX idx_payment_report (status, payment_time)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS etickets (
    eticket_id VARCHAR(36) PRIMARY KEY,
    ticket_id VARCHAR(36) NOT NULL UNIQUE,
    qr_code VARCHAR(255) NOT NULL UNIQUE,
    issue_date DATETIME NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_eticket_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id)
) ENGINE=InnoDB;

INSERT IGNORE INTO users(user_id,username,password_hash,full_name,email,role,active) VALUES
('user-customer-demo','customer',SHA2('customer123',256),'Nguyễn Minh Anh','customer@cineticket.local','CUSTOMER',TRUE),
('user-staff-demo','staff',SHA2('staff123',256),'Trần Nhân Viên','staff@cineticket.local','STAFF',TRUE),
('user-admin-demo','admin',SHA2('admin123',256),'Lê Quản Trị','admin@cineticket.local','ADMIN',TRUE);

INSERT IGNORE INTO cinemas(cinema_id,name,address) VALUES
('cinema-01','CineTicket Hà Nội','12 Nguyễn Trãi, Thanh Xuân, Hà Nội'),
('cinema-02','CineTicket Hồ Gươm','88 Tràng Tiền, Hoàn Kiếm, Hà Nội');

INSERT IGNORE INTO rooms(room_id,cinema_id,room_name,status) VALUES
('room-01','cinema-01','Phòng 01','ACTIVE'),
('room-02','cinema-01','Phòng 02','ACTIVE'),
('room-03','cinema-02','Phòng Premium','ACTIVE');

INSERT IGNORE INTO seats(seat_id,room_id,seat_number,type) VALUES
('r1-a1','room-01','A1','STANDARD'),('r1-a2','room-01','A2','STANDARD'),('r1-a3','room-01','A3','STANDARD'),('r1-a4','room-01','A4','STANDARD'),
('r1-a5','room-01','A5','STANDARD'),('r1-a6','room-01','A6','STANDARD'),('r1-a7','room-01','A7','STANDARD'),('r1-a8','room-01','A8','STANDARD'),
('r1-b1','room-01','B1','VIP'),('r1-b2','room-01','B2','VIP'),('r1-b3','room-01','B3','VIP'),('r1-b4','room-01','B4','VIP'),
('r1-b5','room-01','B5','VIP'),('r1-b6','room-01','B6','VIP'),('r1-b7','room-01','B7','VIP'),('r1-b8','room-01','B8','VIP'),
('r1-c1','room-01','C1','COUPLE'),('r1-c2','room-01','C2','COUPLE'),('r1-c3','room-01','C3','COUPLE'),('r1-c4','room-01','C4','COUPLE');

INSERT IGNORE INTO seats(seat_id,room_id,seat_number,type)
SELECT REPLACE(seat_id,'r1-','r2-'),'room-02',seat_number,type FROM seats WHERE room_id='room-01';
INSERT IGNORE INTO seats(seat_id,room_id,seat_number,type)
SELECT REPLACE(seat_id,'r1-','r3-'),'room-03',seat_number,type FROM seats WHERE room_id='room-01';

INSERT IGNORE INTO movies(movie_id,title,description,genre,duration,release_date,status,poster_url) VALUES
('movie-01','Dune: Hành Tinh Cát','Cuộc chiến giành quyền kiểm soát hành tinh Arrakis và tương lai của các gia tộc.','Khoa học viễn tưởng',166,'2024-03-01','NOW_SHOWING',''),
('movie-02','Mai','Câu chuyện tình cảm và gia đình trong nhịp sống hiện đại.','Tâm lý, Tình cảm',131,'2024-02-10','NOW_SHOWING',''),
('movie-03','Đào, Phở và Piano','Một câu chuyện về tình yêu và lòng dũng cảm giữa những ngày Hà Nội khói lửa.','Lịch sử, Tình cảm',100,'2024-02-10','NOW_SHOWING',''),
('movie-04','Kung Fu Panda 4','Po bước vào hành trình mới để tìm người kế nhiệm và đối đầu một kẻ biến hình.','Hoạt hình, Hài',94,'2024-03-08','NOW_SHOWING',''),
('movie-05','Godzilla x Kong','Hai biểu tượng khổng lồ hợp lực trước mối đe dọa ẩn sâu trong Trái Đất.','Hành động, Phiêu lưu',115,'2024-03-29','NOW_SHOWING',''),
('movie-06','Inside Out 2','Riley bước vào tuổi thiếu niên cùng những cảm xúc hoàn toàn mới.','Hoạt hình, Gia đình',96,'2024-06-14','COMING_SOON','');

INSERT IGNORE INTO showtimes(showtime_id,movie_id,room_id,start_time,end_time,status,base_price) VALUES
('show-01','movie-01','room-01',DATE_ADD(CURRENT_DATE,INTERVAL 34 HOUR),DATE_ADD(CURRENT_DATE,INTERVAL 37 HOUR),'SCHEDULED',90000),
('show-02','movie-01','room-02',DATE_ADD(CURRENT_DATE,INTERVAL 42 HOUR),DATE_ADD(CURRENT_DATE,INTERVAL 45 HOUR),'SCHEDULED',100000),
('show-03','movie-02','room-03',DATE_ADD(CURRENT_DATE,INTERVAL 35 HOUR),DATE_ADD(CURRENT_DATE,INTERVAL 38 HOUR),'SCHEDULED',85000),
('show-04','movie-03','room-01',DATE_ADD(CURRENT_DATE,INTERVAL 58 HOUR),DATE_ADD(CURRENT_DATE,INTERVAL 60 HOUR),'SCHEDULED',75000),
('show-05','movie-04','room-02',DATE_ADD(CURRENT_DATE,INTERVAL 59 HOUR),DATE_ADD(CURRENT_DATE,INTERVAL 61 HOUR),'SCHEDULED',80000),
('show-06','movie-05','room-03',DATE_ADD(CURRENT_DATE,INTERVAL 66 HOUR),DATE_ADD(CURRENT_DATE,INTERVAL 69 HOUR),'SCHEDULED',95000);

INSERT IGNORE INTO showtime_seats(showtime_seat_id,showtime_id,seat_id,status,price,version)
SELECT CONCAT(st.showtime_id,'-',s.seat_id),st.showtime_id,s.seat_id,'AVAILABLE',
       CASE s.type WHEN 'VIP' THEN ROUND(st.base_price*1.4) WHEN 'COUPLE' THEN ROUND(st.base_price*2) ELSE st.base_price END,0
FROM showtimes st JOIN seats s ON s.room_id=st.room_id;

SELECT 'CineTicket database is ready.' AS setup_status;
