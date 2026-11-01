Project PRJ301 : Clinic Management WebApp 
Description: 1 web để quản lý bệnh nhân, lịch hẹn, thuốc , dịch vụ khám , hồ sơ khám và thanh toán
Table : User(Account), Patient, Doctor, Service, Medicine, MedicalRecord - MedicalRecordService(các dịch vụ có trong hồ sơ), Prescription(đơn thuốc), PrescriptionDetail(chi tiết đơn thuốc), Invoice(hóa đơn)

Authentication routes in the web application:
- `/login` signs in an existing account.
- `/register` creates a patient account and linked patient record in one database transaction.
- `/account/change-password` requires an authenticated session.
- `/logout` signs out via POST.

Passwords are stored as plaintext in the existing `Users.PasswordHash` column as requested; this is not suitable for production use.
