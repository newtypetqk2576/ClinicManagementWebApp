CREATE DATABASE ClinicManagement;
GO

USE ClinicManagement;
GO

CREATE TABLE Patients (
    PatientID INT IDENTITY(1,1) PRIMARY KEY,
    FullName NVARCHAR(100) NOT NULL,
    DateOfBirth DATE NULL,
    Gender NVARCHAR(10) NULL,
    Phone VARCHAR(15) NOT NULL UNIQUE,
    Email VARCHAR(100) NULL,
    Address NVARCHAR(255) NULL,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME()
);

CREATE TABLE Doctors (
    DoctorID INT IDENTITY(1,1) PRIMARY KEY,
    FullName NVARCHAR(100) NOT NULL,
    Specialty NVARCHAR(100) NOT NULL,
    Phone VARCHAR(15) NULL,
    IsActive BIT NOT NULL DEFAULT 1
);




CREATE TABLE Appointments (
    AppointmentID INT IDENTITY(1,1) PRIMARY KEY,
    PatientID INT NOT NULL,
    DoctorID INT NOT NULL,
    AppointmentDate DATETIME2 NOT NULL,
    Reason NVARCHAR(255) NULL,
    Status VARCHAR(20) NOT NULL
        DEFAULT 'BOOKED',

    CreatedAt DATETIME2 NOT NULL
        DEFAULT SYSDATETIME(),

    CONSTRAINT FK_Appointments_Patients
        FOREIGN KEY (PatientID)
        REFERENCES Patients(PatientID),

    CONSTRAINT FK_Appointments_Doctors
        FOREIGN KEY (DoctorID)
        REFERENCES Doctors(DoctorID),

    CONSTRAINT CK_Appointments_Status
        CHECK (Status IN (
            'BOOKED',
            'CONFIRMED',
            'COMPLETED',
            'CANCELLED'
        ))
);



     SELECT
    a.AppointmentID,
    p.FullName AS PatientName,
    p.Phone,
    d.FullName AS DoctorName,
    d.Specialty,
    a.AppointmentDate,
    a.Reason,
    a.Status
FROM Appointments a
JOIN Patients p
    ON a.PatientID = p.PatientID
JOIN Doctors d
    ON a.DoctorID = d.DoctorID
ORDER BY a.AppointmentDate DESC;

USE ClinicManagement;
GO

CREATE TABLE Services (
    ServiceID INT IDENTITY(1,1) PRIMARY KEY,
    ServiceName NVARCHAR(150) NOT NULL,
    Description NVARCHAR(500) NULL,
    Price DECIMAL(12,2) NOT NULL,
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT CK_Services_Price
        CHECK (Price >= 0)
);
GO

CREATE TABLE Medicines (
    MedicineID INT IDENTITY(1,1) PRIMARY KEY,
    MedicineName NVARCHAR(150) NOT NULL,
    ActiveIngredient NVARCHAR(150) NULL,
    Unit NVARCHAR(30) NOT NULL,
    Price DECIMAL(12,2) NOT NULL,
    StockQuantity INT NOT NULL DEFAULT 0,
    IsActive BIT NOT NULL DEFAULT 1,
    CreatedAt DATETIME2 NOT NULL DEFAULT SYSDATETIME(),

    CONSTRAINT CK_Medicines_Price
        CHECK (Price >= 0),

    CONSTRAINT CK_Medicines_Stock
        CHECK (StockQuantity >= 0)
);
GO
   

   USE ClinicManagement;
GO

-- =========================================
-- 1. MEDICAL RECORDS
-- Hồ sơ khám bệnh
-- =========================================
CREATE TABLE MedicalRecords (
    MedicalRecordID INT IDENTITY(1,1) PRIMARY KEY,

    AppointmentID INT NULL,
    PatientID INT NOT NULL,
    DoctorID INT NOT NULL,

    VisitDate DATETIME2 NOT NULL
        DEFAULT SYSDATETIME(),

    Symptoms NVARCHAR(1000) NULL,
    Diagnosis NVARCHAR(1000) NULL,
    TreatmentNotes NVARCHAR(2000) NULL,

    Status VARCHAR(20) NOT NULL
        DEFAULT 'IN_PROGRESS',

    CreatedAt DATETIME2 NOT NULL
        DEFAULT SYSDATETIME(),

    CONSTRAINT FK_MedicalRecords_Appointments
        FOREIGN KEY (AppointmentID)
        REFERENCES Appointments(AppointmentID),

    CONSTRAINT FK_MedicalRecords_Patients
        FOREIGN KEY (PatientID)
        REFERENCES Patients(PatientID),

    CONSTRAINT FK_MedicalRecords_Doctors
        FOREIGN KEY (DoctorID)
        REFERENCES Doctors(DoctorID),

    CONSTRAINT UQ_MedicalRecords_Appointment
        UNIQUE (AppointmentID),

    CONSTRAINT CK_MedicalRecords_Status
        CHECK (Status IN (
            'IN_PROGRESS',
            'COMPLETED',
            'CANCELLED'
        ))
);
GO


-- =========================================
-- 2. MEDICAL RECORD SERVICES
-- Dịch vụ trong từng lần khám
-- =========================================
CREATE TABLE MedicalRecordServices (
    MedicalRecordServiceID INT IDENTITY(1,1)
        PRIMARY KEY,

    MedicalRecordID INT NOT NULL,
    ServiceID INT NOT NULL,

    Quantity INT NOT NULL DEFAULT 1,
    UnitPrice DECIMAL(12,2) NOT NULL,

    Notes NVARCHAR(500) NULL,

    CONSTRAINT FK_MedicalRecordServices_Records
        FOREIGN KEY (MedicalRecordID)
        REFERENCES MedicalRecords(MedicalRecordID),

    CONSTRAINT FK_MedicalRecordServices_Services
        FOREIGN KEY (ServiceID)
        REFERENCES Services(ServiceID),

    CONSTRAINT CK_MedicalRecordServices_Quantity
        CHECK (Quantity > 0),

    CONSTRAINT CK_MedicalRecordServices_Price
        CHECK (UnitPrice >= 0)
);
GO


-- =========================================
-- 3. PRESCRIPTIONS
-- Thông tin đơn thuốc
-- =========================================
CREATE TABLE Prescriptions (
    PrescriptionID INT IDENTITY(1,1) PRIMARY KEY,

    MedicalRecordID INT NOT NULL,

    PrescriptionDate DATETIME2 NOT NULL
        DEFAULT SYSDATETIME(),

    Notes NVARCHAR(1000) NULL,

    Status VARCHAR(20) NOT NULL
        DEFAULT 'DRAFT',

    CreatedAt DATETIME2 NOT NULL
        DEFAULT SYSDATETIME(),

    CONSTRAINT FK_Prescriptions_MedicalRecords
        FOREIGN KEY (MedicalRecordID)
        REFERENCES MedicalRecords(MedicalRecordID),

    CONSTRAINT CK_Prescriptions_Status
        CHECK (Status IN (
            'DRAFT',
            'ISSUED',
            'CANCELLED'
        ))
);
GO


-- =========================================
-- 4. PRESCRIPTION DETAILS
-- Chi tiết các loại thuốc trong đơn
-- =========================================
CREATE TABLE PrescriptionDetails (
    PrescriptionDetailID INT IDENTITY(1,1)
        PRIMARY KEY,

    PrescriptionID INT NOT NULL,
    MedicineID INT NOT NULL,

    Dosage NVARCHAR(100) NULL,
    Frequency NVARCHAR(200) NULL,
    DurationDays INT NULL,

    Quantity INT NOT NULL,
    UnitPrice DECIMAL(12,2) NOT NULL,

    Instructions NVARCHAR(500) NULL,

    CONSTRAINT FK_PrescriptionDetails_Prescriptions
        FOREIGN KEY (PrescriptionID)
        REFERENCES Prescriptions(PrescriptionID),

    CONSTRAINT FK_PrescriptionDetails_Medicines
        FOREIGN KEY (MedicineID)
        REFERENCES Medicines(MedicineID),

    CONSTRAINT CK_PrescriptionDetails_Quantity
        CHECK (Quantity > 0),

    CONSTRAINT CK_PrescriptionDetails_Price
        CHECK (UnitPrice >= 0),

    CONSTRAINT CK_PrescriptionDetails_Duration
        CHECK (DurationDays IS NULL OR DurationDays > 0)
);
GO