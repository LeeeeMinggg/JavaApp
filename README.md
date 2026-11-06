# CineTicket Desktop Application

Ứng dụng đặt vé xem phim chạy trên Windows, viết mới bằng **Java 17 + JavaFX + FXML/CSS + JDBC + H2 embedded**. Project bám thiết kế 22 class/interface trong báo cáo Milestone 3, ưu tiên code ngắn gọn và dễ giải thích khi bảo vệ OOP.

**Trạng thái kiểm chứng:** Maven build thành công; 9/9 test pass trên JDK 17. Xem [BUILD-VERIFICATION.md](BUILD-VERIFICATION.md).

> Thanh toán trong project là **mô phỏng**. Ứng dụng không kết nối ngân hàng, Stripe, AWS, email hoặc dịch vụ trả phí nào.

## 1. Chức năng đã có

### Customer

- Đăng ký, đăng nhập và đăng xuất.
- Tìm phim theo tên hoặc thể loại, xem chi tiết và lịch chiếu.
- Xem sơ đồ ghế Standard/VIP/Couple và giá tương ứng.
- Giữ ghế trong 5 phút; hai người không thể giữ cùng một ghế.
- Tạo booking, thanh toán mô phỏng thành công hoặc thất bại.
- Chỉ phát hành Ticket và ETicket/QR sau Payment thành công.
- Xem lịch sử booking, mở vé QR, hủy booking và giải phóng ghế.

### Staff

- Xem, thêm và cập nhật Movie.
- Xem danh sách phòng và lịch chiếu.
- Tạo Showtime, kiểm tra trùng lịch phòng.
- Tự tạo ShowtimeSeat cho toàn bộ ghế của phòng và áp dụng giá Standard/VIP/Couple.

### Admin

- Dashboard số người dùng, phim, suất chiếu, booking, vé và doanh thu.
- Xem tài khoản theo vai trò Customer/Staff/Admin.
- Khóa hoặc mở khóa tài khoản (không thể tự khóa tài khoản đang dùng).
- Báo cáo doanh thu theo ngày; chỉ tính Payment `SUCCESS`, loại `FAILED` và `REFUNDED`.

## 2. Cấu trúc dễ học

```text
src/main/java/com/cineticket/desktop
├── model/       22 class/interface nghiệp vụ + enum trạng thái
├── dao/         Câu lệnh JDBC đọc/ghi database
├── service/     Luồng đăng nhập, booking, payment, quản trị
├── ui/          Chuyển màn hình và tiện ích giao diện
├── ui/controller/ Controller của các file FXML
└── config/      Cấu hình và tạo Connection

src/main/resources/com/cineticket/desktop
├── view/        Các màn hình FXML
├── styles/      theme.css (navy - red - white)
└── images/      Banner và poster phim thật, chạy offline

database/
├── setup.sql                Bản thiết kế MySQL tham khảo
└── reset-demo-bookings.sql  Xóa riêng dữ liệu booking để demo lại
```

Chi tiết đối chiếu OOP nằm tại [docs/OOP_MAPPING.md](docs/OOP_MAPPING.md). Danh sách 13 màn/khối giao diện nằm tại [docs/SCREEN_MAP.md](docs/SCREEN_MAP.md).

## 3. Yêu cầu máy

- Windows 10/11.
- JDK 17 hoặc 21 (khuyên dùng Temurin 17).
- Apache Maven 3.9+.
- Apache NetBeans 20+ nếu muốn chạy bằng IDE.

Không cần cài JavaFX SDK, MySQL hay MySQL Workbench; Maven tải JavaFX và H2 theo `pom.xml`.

## 4. Database tự động

Ứng dụng dùng H2 dạng file. Lần chạy đầu tiên, chương trình tự:

- Tạo thư mục `data/`.
- Tạo toàn bộ bảng và index.
- Thêm phim, phòng, ghế, suất chiếu và ba tài khoản demo.
- Lưu dữ liệu tại `data/cineticket.mv.db`.

Không cần chạy SQL hoặc nhập mật khẩu. Thư mục `data/` đã nằm trong `.gitignore`, vì vậy mỗi thành viên có database riêng sau khi clone.

Hai file `src/main/resources/database/h2-schema.sql` và `h2-seed.sql` là schema/seed thực tế. File `database/setup.sql` được giữ làm bản thiết kế MySQL tham khảo cho báo cáo.

## 5. Chạy từ dòng lệnh

Tại thư mục project:

```powershell
mvn clean test
mvn javafx:run
```

Hoặc nhấp đúp `run-windows.bat` sau khi `java` và `mvn` đã có trong `PATH`.

## 6. Mở bằng NetBeans

1. Chọn **File → Open Project**.
2. Chọn thư mục `CineTicket-Desktop` (thư mục có `pom.xml`).
3. Chờ NetBeans tải Maven dependencies.
4. Trong cửa sổ Projects, bấm phải project → **Run Maven → Goals...**.
5. Nhập `clean javafx:run`, sau đó bấm **Run**.

Để chạy test: bấm phải project → **Test** hoặc chạy goal `clean test`.

## 7. Tài khoản demo

| Vai trò | Username | Password |
|---|---|---|
| Customer | `customer` | `customer123` |
| Staff | `staff` | `staff123` |
| Admin | `admin` | `admin123` |

Mật khẩu được seed dưới dạng SHA-256 để source dễ học và đồng nhất với SQL demo. Với sản phẩm thật nên thay bằng BCrypt/Argon2 có salt; README ghi rõ giới hạn này để tránh hiểu nhầm đây là cấu hình production.

## 8. Cách chống đặt ghế trùng

Luồng `BookingService.createPendingBooking()` chạy trong một transaction:

1. Giải phóng hold đã hết hạn.
2. `SELECT ... FOR UPDATE` khóa các dòng `showtime_seats` được chọn.
3. Chỉ chấp nhận đủ số ghế và tất cả còn `AVAILABLE`.
4. Cập nhật `status`, `held_until` và tăng `version`.
5. Tạo Booking/Ticket và commit cùng lúc.

Database còn có unique constraint `(showtime_id, seat_id)`. Vì vậy việc chống trùng không phụ thuộc vào nút ghế trên UI.

## 9. Thanh toán mô phỏng

- `MOCK_SUCCESS`: tạo một Payment `SUCCESS`, xác nhận Booking/ShowtimeSeat, phát hành Ticket và ETicket QR.
- `MOCK_FAIL`: lưu một Payment `FAILED`; booking vẫn `PENDING` để thử lại.
- Hủy booking đã thanh toán: Payment chuyển `REFUNDED`, vé bị hủy và ghế được giải phóng.

Trong màn checkout có checkbox để cố ý mô phỏng thất bại khi demo.

## 10. Test tự động

Test dùng H2 ở chế độ tương thích MySQL, không cần MySQL đang chạy:

- Strategy tính giá Standard/VIP/Couple.
- Booking không thể confirm khi chưa Payment thành công.
- Observer nhận sự kiện xác nhận booking.
- Hai thread cùng giữ một ghế: chỉ một thread thành công.
- Payment thành công phát hành Ticket và QR.
- Payment thất bại được giữ thành một lần thử riêng.

Chạy:

```powershell
mvn clean test
```

## 11. Source tham khảo

Source CineTicket Spring Boot cũ chỉ được dùng để tham khảo luồng chức năng. Project này không copy kiến trúc Spring, không cần JWT, AWS S3, Stripe, Resilience4j hay server REST.

## 12. Lưu ý giao diện

Giao diện khách hàng bám phong cách rạp phim hiện đại: trang chủ navy/charcoal với hero lớn, poster cards và nút đỏ; màn chọn ghế nền sáng với bảng tóm tắt tối bên phải. Poster/banner đều là ảnh phim thật lưu trong project, **không dùng ảnh AI**; nguồn được ghi tại [docs/ASSET_SOURCES.md](docs/ASSET_SOURCES.md).
