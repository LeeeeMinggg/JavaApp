# Bản đồ 13 màn/khối giao diện

Do 13 ảnh gốc không còn trong bản ghi cuộc trò chuyện được chuyển sang workspace, giao diện được dựng lại theo cùng ngôn ngữ thị giác CineTicket: nền navy, điểm nhấn đỏ, card trắng, poster lớn, sơ đồ ghế và dashboard.

| # | Màn/khối | FXML hoặc tab |
|---|---|---|
| 1 | Đăng nhập | `login.fxml` |
| 2 | Đăng ký | `register.fxml` |
| 3 | Trang chủ / danh sách phim | `home.fxml` |
| 4 | Tìm kiếm phim | thanh tìm kiếm trong `home.fxml` |
| 5 | Chi tiết phim | `movie-details.fxml` |
| 6 | Chọn lịch chiếu | danh sách suất trong `movie-details.fxml` |
| 7 | Chọn ghế | `seat-selection.fxml` |
| 8 | Checkout / thanh toán mô phỏng | `checkout.fxml` |
| 9 | Vé điện tử QR | `eticket.fxml` |
| 10 | Lịch sử booking | `booking-history.fxml` |
| 11 | Staff quản lý phim | tab Movie trong `staff-dashboard.fxml` |
| 12 | Staff quản lý phòng/suất chiếu | tab Showtime trong `staff-dashboard.fxml` |
| 13 | Admin tài khoản + doanh thu | hai tab trong `admin-dashboard.fxml` |

Tất cả màn dùng một stylesheet `theme.css`, có cùng header, màu, typography, card radius và khoảng cách để giữ hình ảnh nhất quán.
