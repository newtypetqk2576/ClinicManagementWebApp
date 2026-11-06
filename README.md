Project PRJ301 : Clinic Management WebApp 
Description: 1 web để quản lý bệnh nhân, lịch hẹn, thuốc , dịch vụ khám , hồ sơ khám và thanh toán
Table : User(Account), Patient, Doctor, Service, Medicine, MedicalRecord - MedicalRecordService(các dịch vụ có trong hồ sơ), Prescription(đơn thuốc), PrescriptionDetail(chi tiết đơn thuốc), Invoice(hóa đơn)

## Tổng quan doanh thu

Với cơ sở dữ liệu đã có, chạy `ClinicInvoicePaidAtMigration.sql` trên SQL Server để lưu thời điểm xác nhận thanh toán. Cơ sở dữ liệu khởi tạo bằng `ClinicTableGenerate.sql` đã có cột này.

Mục **Tổng quan** của quản lý tính doanh thu từ hóa đơn đã thanh toán, theo ngày và theo tuần từ thứ Hai đến Chủ nhật, giờ Việt Nam. Hóa đơn thiếu thời điểm thanh toán được tính theo ngày lập hóa đơn.

