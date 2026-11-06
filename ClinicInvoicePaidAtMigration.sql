USE ClinicManagement;
GO

IF COL_LENGTH('dbo.Invoices', 'PaidAt') IS NULL
    ALTER TABLE dbo.Invoices ADD PaidAt DATETIME2 NULL;
GO
