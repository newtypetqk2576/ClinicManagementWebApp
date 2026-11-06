# Kiểm thử thanh toán và chống trùng lịch

Hai chương trình Java chạy độc lập, không cần JUnit và không kết nối database thật.
JDBC được mô phỏng để kiểm tra kết quả, dữ liệu được lưu, commit/rollback và đóng kết nối.
Các kiểm thử này không thay thế việc kiểm tra truy vấn và khóa đồng thời trên SQL Server thật.

Chạy PowerShell tại thư mục `ClinicManagementWebApp`, sửa đường dẫn JDK/Tomcat nếu cần:

```powershell
$jdkPath = 'C:\Program Files\Java\jdk-17'
$tomcatPath = 'D:\Tomcat 10.1'
$testOutput = 'build\regression-tests'
$testClasspath = "$tomcatPath\lib\servlet-api.jar;lib\*"
New-Item -ItemType Directory -Path $testOutput -Force | Out-Null
$testSources = @(Get-ChildItem src\java,test\regression -Filter '*.java' -Recurse |
    ForEach-Object { $_.FullName })
& "$jdkPath\bin\javac.exe" -encoding UTF-8 --release 17 -cp $testClasspath -d $testOutput $testSources
if ($LASTEXITCODE -ne 0) { throw 'Biên dịch thất bại.' }
& "$jdkPath\bin\java.exe" -cp "$testOutput;$testClasspath" regression.PaymentRegressionTest
if ($LASTEXITCODE -ne 0) { throw 'Kiểm thử thanh toán thất bại.' }
& "$jdkPath\bin\java.exe" -cp "$testOutput;$testClasspath" regression.AppointmentRegressionTest
if ($LASTEXITCODE -ne 0) { throw 'Kiểm thử lịch hẹn thất bại.' }
```

Quy tắc chống trùng: cùng thời điểm `AppointmentDate`, cùng bác sĩ **hoặc** cùng bệnh nhân,
với lịch ở trạng thái `BOOKED` hoặc `CONFIRMED`. Chưa có thời lượng ca khám nên không kiểm
khoảng thời gian chồng lấn. Lịch đã hủy không giữ chỗ.

Khi có môi trường SQL Server thử nghiệm riêng, kiểm tra thêm:

1. Hai phiên cùng đặt một giờ cho cùng bác sĩ: chỉ một lịch được tạo.
2. Hai phiên đặt cùng giờ cho cùng bệnh nhân nhưng khác bác sĩ: chỉ một lịch được tạo.
3. Hủy lịch rồi đặt lại giờ đó: tạo được lịch mới.
4. Sửa chẩn đoán rồi bấm **Lưu và xác nhận thanh toán**: nội dung mới được lưu, hóa đơn
   chuyển sang đã thanh toán và hồ sơ hoàn thành; gửi lại không ghi đè nội dung đã chốt.

Các thao tác thử nghiệm trên chỉ thực hiện với dữ liệu thử, không dùng dữ liệu bệnh nhân thật.
