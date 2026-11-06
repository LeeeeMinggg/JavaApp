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
);

CREATE TABLE IF NOT EXISTS movies (
    movie_id VARCHAR(36) PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description CLOB NOT NULL,
    genre VARCHAR(80) NOT NULL,
    duration INT NOT NULL,
    release_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    poster_url VARCHAR(500),
    CONSTRAINT chk_movie_status CHECK (status IN ('COMING_SOON','NOW_SHOWING','INACTIVE')),
    CONSTRAINT chk_movie_duration CHECK (duration > 0)
);

CREATE TABLE IF NOT EXISTS cinemas (
    cinema_id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    address VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS rooms (
    room_id VARCHAR(36) PRIMARY KEY,
    cinema_id VARCHAR(36) NOT NULL,
    room_name VARCHAR(60) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT uq_room_name UNIQUE (cinema_id, room_name),
    CONSTRAINT fk_room_cinema FOREIGN KEY (cinema_id) REFERENCES cinemas(cinema_id)
);

CREATE TABLE IF NOT EXISTS seats (
    seat_id VARCHAR(36) PRIMARY KEY,
    room_id VARCHAR(36) NOT NULL,
    seat_number VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    CONSTRAINT uq_room_seat UNIQUE (room_id, seat_number),
    CONSTRAINT chk_seat_type CHECK (type IN ('STANDARD','VIP','COUPLE')),
    CONSTRAINT fk_seat_room FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);

CREATE TABLE IF NOT EXISTS showtimes (
    showtime_id VARCHAR(36) PRIMARY KEY,
    movie_id VARCHAR(36) NOT NULL,
    room_id VARCHAR(36) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    base_price DECIMAL(12,2) NOT NULL,
    CONSTRAINT chk_showtime_status CHECK (status IN ('SCHEDULED','CANCELLED','COMPLETED')),
    CONSTRAINT fk_showtime_movie FOREIGN KEY (movie_id) REFERENCES movies(movie_id),
    CONSTRAINT fk_showtime_room FOREIGN KEY (room_id) REFERENCES rooms(room_id)
);
CREATE INDEX IF NOT EXISTS idx_showtime_movie_time ON showtimes(movie_id, start_time);
CREATE INDEX IF NOT EXISTS idx_showtime_room_time ON showtimes(room_id, start_time);

CREATE TABLE IF NOT EXISTS showtime_seats (
    showtime_seat_id VARCHAR(80) PRIMARY KEY,
    showtime_id VARCHAR(36) NOT NULL,
    seat_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    held_until TIMESTAMP NULL,
    price DECIMAL(12,2) NOT NULL,
    version INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_showtime_seat UNIQUE (showtime_id, seat_id),
    CONSTRAINT chk_showtime_seat_status CHECK (status IN ('AVAILABLE','HELD','CONFIRMED')),
    CONSTRAINT fk_ss_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(showtime_id),
    CONSTRAINT fk_ss_seat FOREIGN KEY (seat_id) REFERENCES seats(seat_id)
);
CREATE INDEX IF NOT EXISTS idx_ss_availability ON showtime_seats(showtime_id, status, held_until);

CREATE TABLE IF NOT EXISTS bookings (
    booking_id VARCHAR(36) PRIMARY KEY,
    customer_id VARCHAR(36) NOT NULL,
    showtime_id VARCHAR(36) NOT NULL,
    booking_time TIMESTAMP NOT NULL,
    total_amount DECIMAL(12,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT chk_booking_status CHECK (status IN ('PENDING','CONFIRMED','CANCELLED','EXPIRED')),
    CONSTRAINT fk_booking_customer FOREIGN KEY (customer_id) REFERENCES users(user_id),
    CONSTRAINT fk_booking_showtime FOREIGN KEY (showtime_id) REFERENCES showtimes(showtime_id)
);
CREATE INDEX IF NOT EXISTS idx_booking_customer_time ON bookings(customer_id, booking_time);

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
);

CREATE TABLE IF NOT EXISTS payments (
    payment_id VARCHAR(36) PRIMARY KEY,
    booking_id VARCHAR(36) NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    payment_time TIMESTAMP NOT NULL,
    method VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    gateway_name VARCHAR(100) NOT NULL,
    CONSTRAINT chk_payment_status CHECK (status IN ('PENDING','SUCCESS','FAILED','REFUNDED')),
    CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
);
CREATE INDEX IF NOT EXISTS idx_payment_booking ON payments(booking_id);
CREATE INDEX IF NOT EXISTS idx_payment_report ON payments(status, payment_time);

CREATE TABLE IF NOT EXISTS etickets (
    eticket_id VARCHAR(36) PRIMARY KEY,
    ticket_id VARCHAR(36) NOT NULL UNIQUE,
    qr_code VARCHAR(255) NOT NULL UNIQUE,
    issue_date TIMESTAMP NOT NULL,
    status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_eticket_ticket FOREIGN KEY (ticket_id) REFERENCES tickets(ticket_id)
);
