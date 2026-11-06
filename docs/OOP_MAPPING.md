# Đối chiếu Milestone 3 với source code

## 22 class/interface

| # | Class/interface | Nơi dùng thật |
|---|---|---|
| 1 | `User` | Lớp cha chứa tài khoản, hồ sơ, login/logout |
| 2 | `Customer` | Tìm phim, tạo/hủy booking, lịch sử |
| 3 | `Staff` | Vai trò quản lý dữ liệu vận hành |
| 4 | `Admin` | Vai trò quản lý tài khoản và báo cáo |
| 5 | `Movie` | Phim, mô tả, thể loại, thời lượng, trạng thái |
| 6 | `Cinema` | Rạp sở hữu danh sách Room |
| 7 | `Room` | Phòng thuộc Cinema, chứa Seat |
| 8 | `Seat` | Ghế vật lý và loại ghế |
| 9 | `Showtime` | Một lần chiếu Movie trong Room; phát sự kiện thay đổi |
| 10 | `ShowtimeSeat` | Trạng thái ghế theo suất, hold/confirm/release/version |
| 11 | `Booking` | Tổng Ticket, Payment, confirm/cancel; phát sự kiện |
| 12 | `Ticket` | Vé của đúng một ShowtimeSeat |
| 13 | `ETicket` | Bản điện tử và dữ liệu QR |
| 14 | `Payment` | Một lần thử thanh toán; SUCCESS/FAILED/REFUNDED |
| 15 | `PaymentGateway` | Cổng mô phỏng process/verify transaction |
| 16 | `RevenueReport` | Tổng hợp Payment SUCCESS và Ticket ISSUED |
| 17 | `PricingStrategy` | Interface `calculatePrice(ShowtimeSeat)` |
| 18 | `StandardPricing` | Giá cơ bản |
| 19 | `VIPPricing` | Giá cơ bản × 1,40 |
| 20 | `CouplePricing` | Giá cơ bản × 2,00 |
| 21 | `NotificationSubject` | Hợp đồng subscribe/unsubscribe/notify |
| 22 | `NotificationObserver` | Hợp đồng `update(event)` |

## Bốn tính chất OOP

### Encapsulation

Field nghiệp vụ là `private`. Trạng thái chỉ đổi qua method có điều kiện như `holdSeat()`, `confirmSeat()`, `Booking.confirm()` và `Payment.refund()`.

### Inheritance

`Customer`, `Staff`, `Admin` kế thừa `User`. Thuộc tính đăng nhập và hồ sơ chỉ viết một lần ở lớp cha; hành vi theo vai trò nằm ở lớp con.

### Abstraction

`PricingStrategy`, `NotificationSubject`, `NotificationObserver` là interface. DAO cũng dùng interface (`UserDao`, `MovieDao`, `ShowtimeDao`) để tách nghiệp vụ khỏi JDBC.

### Polymorphism

Nơi tính giá gọi `PricingStrategy.calculatePrice()` mà không cần biết implementation cụ thể. User đăng nhập được xử lý qua kiểu `User`, sau đó điều hướng theo `getRole()`.

## Design patterns

### Strategy

Ba implementation giá có cùng hợp đồng. Thêm giá sinh viên hoặc giá cuối tuần chỉ cần thêm implementation mới, không sửa `Ticket`.

### Observer

`Booking`, `Payment`, `Showtime` thực thi `NotificationSubject`. `ConsoleNotificationObserver` là kênh cụ thể dùng khi demo. Có thể thêm email/app notification mà không sửa publisher.

### MVC + Service/DAO

- Model: 22 class/interface nghiệp vụ.
- View: FXML + CSS.
- Controller: nhận thao tác UI.
- Service: điều phối use case và transaction.
- DAO: câu SQL và ánh xạ dữ liệu.

## Ràng buộc dữ liệu

- Unique `(showtime_id, seat_id)` trong `showtime_seats`.
- Booking total bằng tổng Ticket price.
- Payment success phải có amount bằng booking total.
- Vé và QR chỉ được issue sau thanh toán thành công.
- Payment thất bại không ghi đè Payment cũ.
- Doanh thu chỉ tính Payment `SUCCESS`.
- `SELECT ... FOR UPDATE` + `version` chống hai người đặt trùng ghế.
