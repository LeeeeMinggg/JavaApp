INSERT INTO users(user_id,username,password_hash,full_name,email,role,active) VALUES
('user-customer-demo','customer','b041c0aeb35bb0fa4aa668ca5a920b590196fdaf9a00eb852c9b7f4d123cc6d6','Nguyễn Minh Anh','customer@cineticket.local','CUSTOMER',TRUE),
('user-staff-demo','staff','10176e7b7b24d317acfcf8d2064cfd2f24e154f7b5a96603077d5ef813d6a6b6','Trần Nhân Viên','staff@cineticket.local','STAFF',TRUE),
('user-admin-demo','admin','240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9','Lê Quản Trị','admin@cineticket.local','ADMIN',TRUE);

INSERT INTO cinemas(cinema_id,name,address) VALUES
('cinema-01','CineTicket Hà Nội','12 Nguyễn Trãi, Thanh Xuân, Hà Nội'),
('cinema-02','CineTicket Hồ Gươm','88 Tràng Tiền, Hoàn Kiếm, Hà Nội');

INSERT INTO rooms(room_id,cinema_id,room_name,status) VALUES
('room-01','cinema-01','Phòng 01','ACTIVE'),
('room-02','cinema-01','Phòng 02','ACTIVE'),
('room-03','cinema-02','Phòng Premium','ACTIVE');

INSERT INTO seats(seat_id,room_id,seat_number,type) VALUES
('r1-a1','room-01','A1','STANDARD'),('r1-a2','room-01','A2','STANDARD'),('r1-a3','room-01','A3','STANDARD'),('r1-a4','room-01','A4','STANDARD'),
('r1-a5','room-01','A5','STANDARD'),('r1-a6','room-01','A6','STANDARD'),('r1-a7','room-01','A7','STANDARD'),('r1-a8','room-01','A8','STANDARD'),
('r1-b1','room-01','B1','VIP'),('r1-b2','room-01','B2','VIP'),('r1-b3','room-01','B3','VIP'),('r1-b4','room-01','B4','VIP'),
('r1-b5','room-01','B5','VIP'),('r1-b6','room-01','B6','VIP'),('r1-b7','room-01','B7','VIP'),('r1-b8','room-01','B8','VIP'),
('r1-c1','room-01','C1','COUPLE'),('r1-c2','room-01','C2','COUPLE'),('r1-c3','room-01','C3','COUPLE'),('r1-c4','room-01','C4','COUPLE');

INSERT INTO seats(seat_id,room_id,seat_number,type)
SELECT REPLACE(seat_id,'r1-','r2-'),'room-02',seat_number,type FROM seats WHERE room_id='room-01';
INSERT INTO seats(seat_id,room_id,seat_number,type)
SELECT REPLACE(seat_id,'r1-','r3-'),'room-03',seat_number,type FROM seats WHERE room_id='room-01';

INSERT INTO movies(movie_id,title,description,genre,duration,release_date,status,poster_url) VALUES
('movie-01','Dune: Hành Tinh Cát','Cuộc chiến giành quyền kiểm soát hành tinh Arrakis và tương lai của các gia tộc.','Khoa học viễn tưởng',166,DATE '2024-03-01','NOW_SHOWING',''),
('movie-02','Mai','Câu chuyện tình cảm và gia đình trong nhịp sống hiện đại.','Tâm lý, Tình cảm',131,DATE '2024-02-10','NOW_SHOWING',''),
('movie-03','Đào, Phở và Piano','Một câu chuyện về tình yêu và lòng dũng cảm giữa những ngày Hà Nội khói lửa.','Lịch sử, Tình cảm',100,DATE '2024-02-10','NOW_SHOWING',''),
('movie-04','Kung Fu Panda 4','Po bước vào hành trình mới để tìm người kế nhiệm và đối đầu một kẻ biến hình.','Hoạt hình, Hài',94,DATE '2024-03-08','NOW_SHOWING',''),
('movie-05','Godzilla x Kong','Hai biểu tượng khổng lồ hợp lực trước mối đe dọa ẩn sâu trong Trái Đất.','Hành động, Phiêu lưu',115,DATE '2024-03-29','NOW_SHOWING',''),
('movie-06','Inside Out 2','Riley bước vào tuổi thiếu niên cùng những cảm xúc hoàn toàn mới.','Hoạt hình, Gia đình',96,DATE '2024-06-14','COMING_SOON','');

INSERT INTO showtimes(showtime_id,movie_id,room_id,start_time,end_time,status,base_price) VALUES
('show-01','movie-01','room-01',DATEADD('HOUR',34,CURRENT_DATE),DATEADD('HOUR',37,CURRENT_DATE),'SCHEDULED',90000),
('show-02','movie-01','room-02',DATEADD('HOUR',42,CURRENT_DATE),DATEADD('HOUR',45,CURRENT_DATE),'SCHEDULED',100000),
('show-03','movie-02','room-03',DATEADD('HOUR',35,CURRENT_DATE),DATEADD('HOUR',38,CURRENT_DATE),'SCHEDULED',85000),
('show-04','movie-03','room-01',DATEADD('HOUR',58,CURRENT_DATE),DATEADD('HOUR',60,CURRENT_DATE),'SCHEDULED',75000),
('show-05','movie-04','room-02',DATEADD('HOUR',59,CURRENT_DATE),DATEADD('HOUR',61,CURRENT_DATE),'SCHEDULED',80000),
('show-06','movie-05','room-03',DATEADD('HOUR',66,CURRENT_DATE),DATEADD('HOUR',69,CURRENT_DATE),'SCHEDULED',95000);

INSERT INTO showtime_seats(showtime_seat_id,showtime_id,seat_id,status,price,version)
SELECT CONCAT(st.showtime_id,'-',s.seat_id),st.showtime_id,s.seat_id,'AVAILABLE',
       CASE s.type WHEN 'VIP' THEN ROUND(st.base_price*1.4) WHEN 'COUPLE' THEN ROUND(st.base_price*2) ELSE st.base_price END,0
FROM showtimes st JOIN seats s ON s.room_id=st.room_id;
