
INSERT INTO Patients
    (FullName, DateOfBirth, Gender, Phone, Email, Address)
VALUES
    (N'Nguyễn Khánh Chi', '2005-04-17',
     N'Nữ', '0901000001', 'minhanh@example.com',
     N'Hà Nội'),

    (N'Trần Quang Khải', '2006-07-25',
     N'Nam', '0901000002', 'quocbao@example.com',
     N'Hà Nội'),

    (N'Lê Thu Hà', '1995-03-08',
     N'Nữ', '0901000003', NULL,
     N'Bắc Ninh');



     INSERT INTO Doctors (FullName, Specialty, Phone)
VALUES
    (N'Phạm Khánh Linh', N'Nội tổng quát', '0912000001'),
    (N'Trần q Khải', N'Da liễu', '0912000002'),
    (N'Lê Thanh Loan', N'Tai mũi họng', '0912000003');
    SELECT * FROM Patients;
SELECT * FROM Doctors;



INSERT INTO Appointments
    (PatientID, DoctorID, AppointmentDate, Reason, Status)
VALUES
    (1, 1, '2026-10-13T09:00:00',
     N'Khám sức khỏe tổng quát', 'CONFIRMED'),

    (2, 2, '2026-10-13T10:30:00',
     N'Khám da liễu', 'BOOKED'),

    (3, 3, '2026-10-14T14:00:00',
     N'Đau họng', 'BOOKED');


      INSERT INTO Services
    (ServiceName, Description, Price, IsActive)
VALUES
    (N'Khám tổng quát',
     N'Khám và đánh giá sức khỏe tổng quát',
     150000, 1),

    (N'Khám nội khoa',
     N'Khám các bệnh lý nội khoa',
     200000, 1),

    (N'Khám da liễu',
     N'Khám và tư vấn các bệnh về da',
     180000, 1),

    (N'Khám tai mũi họng',
     N'Khám tai, mũi và họng',
     180000, 1),

    (N'Xét nghiệm máu',
     N'Dịch vụ xét nghiệm máu cơ bản',
     250000, 1),

    (N'Đo điện tim',
     N'Đo và ghi nhận điện tâm đồ',
     120000, 1),

    (N'Siêu âm ổ bụng',
     N'Dịch vụ siêu âm ổ bụng',
     300000, 1),

    (N'Đo huyết áp',
     N'Kiểm tra chỉ số huyết áp',
     30000, 1),

    (N'Tư vấn dinh dưỡng',
     N'Tư vấn chế độ dinh dưỡng',
     100000, 1),

    (N'Khám chuyên khoa mắt',
     N'Kiểm tra và đánh giá sức khỏe mắt',
     180000, 1);


GO
INSERT INTO Medicines
    (MedicineName, ActiveIngredient, Unit,
     Price, StockQuantity, IsActive)
VALUES
    (N'Paracetamol 500mg',
     N'Paracetamol', N'Viên', 1500, 500, 1),

    (N'Vitamin C 500mg',
     N'Ascorbic acid', N'Viên', 2000, 300, 1),

    (N'Viên bổ sung kẽm',
     N'Zinc', N'Viên', 3000, 200, 1),

    (N'Nước muối sinh lý 0.9%',
     N'Sodium chloride', N'Chai', 10000, 100, 1),

    (N'ORS',
     N'Glucose và điện giải', N'Gói', 5000, 150, 1),

    (N'Gel bôi ngoài da mẫu',
     N'Thành phần giả lập', N'Tuýp', 25000, 50, 1),

    (N'Viên bổ sung vitamin B',
     N'Vitamin B complex', N'Viên', 2500, 250, 1),

    (N'Siro ho mẫu',
     N'Thành phần giả lập', N'Chai', 35000, 40, 1),

    (N'Dung dịch sát khuẩn mẫu',
     N'Thành phần giả lập', N'Chai', 18000, 60, 1),

    (N'Viên bổ sung canxi',
     N'Calcium', N'Viên', 4000, 180, 1);
GO


-- 1. Tạo hồ sơ khám
INSERT INTO MedicalRecords (
    AppointmentID,
    PatientID,
    DoctorID,
    VisitDate,
    Symptoms,
    Diagnosis,
    TreatmentNotes,
    Status
)
VALUES (
    1,
    1,
    1,
    SYSDATETIME(),
    N'Mệt mỏi, cần kiểm tra sức khỏe',
    N'Chưa có kết luận chẩn đoán',
    N'Hồ sơ dùng để kiểm thử CRUD',
    'IN_PROGRESS'
);
GO

-- 2. Thêm dịch vụ vào hồ sơ khám
INSERT INTO MedicalRecordServices (
    MedicalRecordID,
    ServiceID,
    Quantity,
    UnitPrice,
    Notes
)
VALUES (
    1,
    1,
    1,
    150000,
    N'Dịch vụ khám tổng quát mẫu'
);
GO

-- 3. Tạo đơn thuốc nháp
INSERT INTO Prescriptions (
    MedicalRecordID,
    Notes,
    Status
)
VALUES (
    1,
    N'Đơn thuốc minh họa cho demo',
    'DRAFT'
);
GO

-- 4. Thêm thuốc vào đơn
INSERT INTO PrescriptionDetails (
    PrescriptionID,
    MedicineID,
    Dosage,
    Frequency,
    DurationDays,
    Quantity,
    UnitPrice,
    Instructions
)
VALUES (
    1,
    1,
    N'Theo chỉ định của bác sĩ',
    N'Theo chỉ định của bác sĩ',
    3,
    6,
    1500,
    N'Dữ liệu minh họa, không phải chỉ định điều trị'
);
GO

-- 1. Tạo tài khoản bệnh nhân
INSERT INTO Users (Username, PasswordHash, Role)
VALUES
('nguyenkhanhchi', '123456', 'PATIENT'),
('tranquangkhai',  '123456', 'PATIENT');


-- 2. Tạo tài khoản bác sĩ
INSERT INTO Users (Username, PasswordHash, Role)
VALUES
('phamkhanhlinh', '123456', 'MANAGER'),
('tranqkhai',     '123456', 'DOCTOR'),
('lethanhloan',   '123456', 'DOCTOR');


-- 3. Liên kết tài khoản với bệnh nhân
UPDATE Patients
SET UserID = (
    SELECT UserID FROM Users
    WHERE Username = 'nguyenkhanhchi'
)
WHERE FullName = N'Nguyễn Khánh Chi';

UPDATE Patients
SET UserID = (
    SELECT UserID FROM Users
    WHERE Username = 'tranquangkhai'
)
WHERE FullName = N'Trần Quang Khải';


-- 4. Liên kết tài khoản với bác sĩ
UPDATE Doctors
SET UserID = (
    SELECT UserID FROM Users
    WHERE Username = 'phamkhanhlinh'
)
WHERE FullName = N'Phạm Khánh Linh';

UPDATE Doctors
SET UserID = (
    SELECT UserID FROM Users
    WHERE Username = 'tranqkhai'
)
WHERE FullName = N'Trần q Khải';

UPDATE Doctors
SET UserID = (
    SELECT UserID FROM Users
    WHERE Username = 'lethanhloan'
)
WHERE FullName = N'Lê Thanh Loan';


-- 5. Kiểm tra kết quả
SELECT * FROM Users;
SELECT * FROM Patients;
SELECT * FROM Doctors;
