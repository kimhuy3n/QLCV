# QLCV - Quản lý công việc cá nhân

Ứng dụng Java Swing sử dụng mô hình MVC, JDBC và MySQL.

## Chuẩn bị MySQL bằng XAMPP/phpMyAdmin

1. Khởi động module **MySQL** trong XAMPP.
2. Mở `http://localhost/phpmyadmin`.
3. Chọn **Import**, chọn file `database/qlcv.sql`, rồi bấm **Import**.
4. Mặc định ứng dụng kết nối `localhost:3306`, database `qlcv`, tài khoản `root`, mật khẩu rỗng.

Nếu cấu hình MySQL khác, đặt các biến môi trường trước khi chạy:

```text
QLCV_DB_URL=jdbc:mysql://localhost:3306/qlcv?useUnicode=true&characterEncoding=UTF-8
QLCV_DB_USER=root
QLCV_DB_PASSWORD=mat_khau
```

## Chạy trong IntelliJ IDEA

1. Mở `pom.xml` và chọn **Load Maven Project** nếu IntelliJ chưa tự nhận diện.
2. Chọn JDK 21.
3. Chạy lớp `vn.qlcv.Main`.

Hoặc chạy bằng Maven:

```shell
mvn clean compile
mvn exec:java
```

Mật khẩu được băm bằng PBKDF2-HMAC-SHA256 của JDK, không lưu trực tiếp trong database. Mỗi tài khoản có danh mục và công việc riêng.
