# Build verification

Ngày kiểm tra: 06/11/2026 (Asia/Saigon)

## Môi trường kiểm tra

- Eclipse Temurin JDK `17.0.20.1`
- Apache Maven `3.10.0`
- Windows 11 x64

## Kết quả

```text
Main sources compiled: 71
Tests run: 9
Failures: 0
Errors: 0
Skipped: 0
Result: BUILD SUCCESS
```

Nhóm test đã chạy:

- `BookingDomainTest`: 2 test.
- `PricingStrategyTest`: 3 test.
- `BookingServiceIntegrationTest`: 3 test.
- `FxmlContractTest`: 1 test kiểm tra XML, controller, `fx:id` và event handler của toàn bộ FXML.

Đóng gói Maven cũng thành công:

```text
target/cineticket-desktop-1.0.0.jar
Result: BUILD SUCCESS
```

JAR chuẩn của Maven không tự chứa JavaFX native libraries; cách chạy được hỗ trợ là `mvn javafx:run` như README hướng dẫn.

## Kiểm tra sau khi nâng cấp giao diện

- Biên dịch lại 71 source Java: thành công.
- Đọc/kiểm tra cú pháp toàn bộ 10 file FXML: hợp lệ.
- Đóng gói lại `target/cineticket-desktop-1.0.0.jar`: `BUILD SUCCESS`.
- 7 ảnh phim thật đã được nhúng vào resources và nguồn ảnh được ghi tại `docs/ASSET_SOURCES.md`.
