package Dal;

import Model.MedicalRecord;
import Model.MedicalRecord.Invoice;
import Model.MedicalRecord.MedicineLine;
import Model.MedicalRecord.Prescription;
import Model.MedicalRecord.ServiceLine;
import jakarta.servlet.ServletContext;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MedicalRecordDAO extends DBContext {
    public enum InvoicePaymentResult {
        CONFIRMED,
        ALREADY_PAID,
        NOT_PAYABLE,
        NOT_FOUND
    }

    public MedicalRecordDAO(ServletContext servletContext) {
        super(servletContext);
    }

    public List<MedicalRecord> getByPatientID(int patientID) throws SQLException {
        if (patientID <= 0) {
            throw new IllegalArgumentException("PatientID must be positive.");
        }
        String sql = "SELECT mr.MedicalRecordID, d.FullName AS DoctorName, mr.VisitDate, "
                + "mr.Symptoms, mr.Diagnosis, mr.Status FROM MedicalRecords mr "
                + "JOIN Doctors d ON d.DoctorID = mr.DoctorID "
                + "WHERE mr.PatientID = ? ORDER BY mr.VisitDate DESC, mr.MedicalRecordID DESC";
        List<MedicalRecord> records = new ArrayList<>();
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, patientID);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    MedicalRecord record = new MedicalRecord();
                    record.setMedicalRecordID(resultSet.getInt("MedicalRecordID"));
                    record.setDoctorName(resultSet.getString("DoctorName"));
                    record.setVisitDate(resultSet.getTimestamp("VisitDate").toLocalDateTime());
                    record.setSymptoms(resultSet.getString("Symptoms"));
                    record.setDiagnosis(resultSet.getString("Diagnosis"));
                    record.setStatus(resultSet.getString("Status"));
                    records.add(record);
                }
            }
        }
        return records;
    }

    public MedicalRecord getDetailsForPatient(int medicalRecordID, int patientID) throws SQLException {
        if (medicalRecordID <= 0 || patientID <= 0) {
            throw new IllegalArgumentException("Record and Patient IDs must be positive.");
        }
        String sql = "SELECT mr.MedicalRecordID, mr.PatientID, mr.DoctorID, mr.AppointmentID, "
                + "p.FullName AS PatientName, d.FullName AS DoctorName, mr.VisitDate, "
                + "mr.Symptoms, mr.Diagnosis, mr.TreatmentNotes, mr.Status, mr.CreatedAt "
                + "FROM MedicalRecords mr JOIN Doctors d ON d.DoctorID = mr.DoctorID "
                + "JOIN Patients p ON p.PatientID = mr.PatientID "
                + "WHERE mr.MedicalRecordID = ? AND mr.PatientID = ?";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, medicalRecordID);
            statement.setInt(2, patientID);
            MedicalRecord record;
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                record = new MedicalRecord();
                record.setMedicalRecordID(resultSet.getInt("MedicalRecordID"));
                record.setPatientID(resultSet.getInt("PatientID"));
                record.setDoctorID(resultSet.getInt("DoctorID"));
                int appointmentID = resultSet.getInt("AppointmentID");
                record.setAppointmentID(resultSet.wasNull() ? null : appointmentID);
                record.setPatientName(resultSet.getString("PatientName"));
                record.setDoctorName(resultSet.getString("DoctorName"));
                record.setVisitDate(resultSet.getTimestamp("VisitDate").toLocalDateTime());
                record.setSymptoms(resultSet.getString("Symptoms"));
                record.setDiagnosis(resultSet.getString("Diagnosis"));
                record.setTreatmentNotes(resultSet.getString("TreatmentNotes"));
                record.setStatus(resultSet.getString("Status"));
                record.setCreatedAt(resultSet.getTimestamp("CreatedAt").toLocalDateTime());
            }
            loadServices(conn, record);
            loadPrescriptions(conn, record);
            record.setInvoice(loadInvoice(conn, medicalRecordID));
            return record;
        }
    }

    public List<MedicalRecord> getByDoctorID(int doctorID) throws SQLException {
        requirePositiveID(doctorID, "DoctorID");
        String sql = "SELECT mr.MedicalRecordID, mr.PatientID, mr.DoctorID, "
                + "p.FullName AS PatientName, d.FullName AS DoctorName, mr.VisitDate, "
                + "mr.Symptoms, mr.Status FROM MedicalRecords mr "
                + "JOIN Patients p ON p.PatientID = mr.PatientID "
                + "JOIN Doctors d ON d.DoctorID = mr.DoctorID "
                + "WHERE mr.DoctorID = ? ORDER BY mr.VisitDate DESC, mr.MedicalRecordID DESC";
        List<MedicalRecord> records = new ArrayList<>();
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, doctorID);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    MedicalRecord record = new MedicalRecord();
                    record.setMedicalRecordID(resultSet.getInt("MedicalRecordID"));
                    record.setPatientID(resultSet.getInt("PatientID"));
                    record.setDoctorID(resultSet.getInt("DoctorID"));
                    record.setPatientName(resultSet.getString("PatientName"));
                    record.setDoctorName(resultSet.getString("DoctorName"));
                    record.setVisitDate(resultSet.getTimestamp("VisitDate").toLocalDateTime());
                    record.setSymptoms(resultSet.getString("Symptoms"));
                    record.setStatus(resultSet.getString("Status"));
                    records.add(record);
                }
            }
        }
        return records;
    }

    public List<MedicalRecord> getAllForManager() throws SQLException {
        return getForManagerOn(null);
    }

    public List<MedicalRecord> getForManagerOn(LocalDate date) throws SQLException {
        String sql = "SELECT mr.MedicalRecordID, mr.PatientID, mr.DoctorID, "
                + "p.FullName AS PatientName, d.FullName AS DoctorName, mr.VisitDate, "
                + "mr.Symptoms, mr.Status FROM MedicalRecords mr "
                + "JOIN Patients p ON p.PatientID = mr.PatientID "
                + "JOIN Doctors d ON d.DoctorID = mr.DoctorID "
                + (date == null ? "" : "WHERE mr.VisitDate >= ? AND mr.VisitDate < ? ")
                + "ORDER BY mr.VisitDate DESC, mr.MedicalRecordID DESC";
        List<MedicalRecord> records = new ArrayList<>();
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            if (date != null) {
                statement.setDate(1, Date.valueOf(date));
                statement.setDate(2, Date.valueOf(date.plusDays(1)));
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    MedicalRecord record = new MedicalRecord();
                    record.setMedicalRecordID(resultSet.getInt("MedicalRecordID"));
                    record.setPatientID(resultSet.getInt("PatientID"));
                    record.setDoctorID(resultSet.getInt("DoctorID"));
                    record.setPatientName(resultSet.getString("PatientName"));
                    record.setDoctorName(resultSet.getString("DoctorName"));
                    record.setVisitDate(resultSet.getTimestamp("VisitDate").toLocalDateTime());
                    record.setSymptoms(resultSet.getString("Symptoms"));
                    record.setStatus(resultSet.getString("Status"));
                    records.add(record);
                }
            }
        }
        return records;
    }

    public Map<LocalDate, BigDecimal> getDailyRevenue(LocalDate start, LocalDate endExclusive)
            throws SQLException {
        Map<LocalDate, BigDecimal> totals = new LinkedHashMap<>();
        try (Connection conn = requireConnection()) {
            String revenueDate = hasPaidAtColumn(conn) ? "COALESCE(PaidAt, InvoiceDate)" : "InvoiceDate";
            String sql = "SELECT CAST(" + revenueDate + " AS date) AS RevenueDate, "
                    + "SUM(TotalAmount) AS Revenue FROM Invoices WHERE PaymentStatus = 'PAID' "
                    + "AND " + revenueDate + " >= ? AND " + revenueDate + " < ? "
                    + "GROUP BY CAST(" + revenueDate + " AS date)";
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setDate(1, Date.valueOf(start));
                statement.setDate(2, Date.valueOf(endExclusive));
                try (ResultSet rows = statement.executeQuery()) {
                    while (rows.next()) {
                        totals.put(rows.getDate("RevenueDate").toLocalDate(), rows.getBigDecimal("Revenue"));
                    }
                }
            }
        }
        return totals;
    }

    private boolean hasPaidAtColumn(Connection conn) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement("SELECT COL_LENGTH('dbo.Invoices', 'PaidAt')");
                ResultSet result = statement.executeQuery()) {
            return result.next() && result.getObject(1) != null;
        }
    }

    public MedicalRecord getDetailsForDoctor(int medicalRecordID, int doctorID) throws SQLException {
        requirePositiveID(medicalRecordID, "MedicalRecordID");
        requirePositiveID(doctorID, "DoctorID");
        return getDetailsByScope(medicalRecordID, doctorID);
    }

    public MedicalRecord getDetailsForManager(int medicalRecordID) throws SQLException {
        requirePositiveID(medicalRecordID, "MedicalRecordID");
        return getDetailsByScope(medicalRecordID, null);
    }

    private MedicalRecord getDetailsByScope(int medicalRecordID, Integer doctorID) throws SQLException {
        String sql = "SELECT mr.MedicalRecordID, mr.PatientID, mr.DoctorID, mr.AppointmentID, "
                + "p.FullName AS PatientName, d.FullName AS DoctorName, mr.VisitDate, "
                + "mr.Symptoms, mr.Diagnosis, mr.TreatmentNotes, mr.Status, mr.CreatedAt "
                + "FROM MedicalRecords mr JOIN Doctors d ON d.DoctorID = mr.DoctorID "
                + "JOIN Patients p ON p.PatientID = mr.PatientID "
                + "WHERE mr.MedicalRecordID = ? "
                + (doctorID == null ? "" : "AND mr.DoctorID = ?");
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, medicalRecordID);
            if (doctorID != null) {
                statement.setInt(2, doctorID);
            }
            MedicalRecord record;
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                record = new MedicalRecord();
                record.setMedicalRecordID(resultSet.getInt("MedicalRecordID"));
                record.setPatientID(resultSet.getInt("PatientID"));
                record.setDoctorID(resultSet.getInt("DoctorID"));
                int appointmentID = resultSet.getInt("AppointmentID");
                record.setAppointmentID(resultSet.wasNull() ? null : appointmentID);
                record.setPatientName(resultSet.getString("PatientName"));
                record.setDoctorName(resultSet.getString("DoctorName"));
                record.setVisitDate(resultSet.getTimestamp("VisitDate").toLocalDateTime());
                record.setSymptoms(resultSet.getString("Symptoms"));
                record.setDiagnosis(resultSet.getString("Diagnosis"));
                record.setTreatmentNotes(resultSet.getString("TreatmentNotes"));
                record.setStatus(resultSet.getString("Status"));
                record.setCreatedAt(resultSet.getTimestamp("CreatedAt").toLocalDateTime());
            }
            loadServices(conn, record);
            loadPrescriptions(conn, record);
            record.setInvoice(loadInvoice(conn, medicalRecordID));
            return record;
        }
    }

    public Integer getIDByAppointmentForDoctor(int appointmentID, int doctorID) throws SQLException {
        requirePositiveID(appointmentID, "AppointmentID");
        requirePositiveID(doctorID, "DoctorID");
        String sql = "SELECT MedicalRecordID FROM MedicalRecords "
                + "WHERE AppointmentID = ? AND DoctorID = ?";
        try (PreparedStatement statement = requireConnection().prepareStatement(sql)) {
            statement.setInt(1, appointmentID);
            statement.setInt(2, doctorID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("MedicalRecordID") : null;
            }
        }
    }

    public Integer getIDByAppointmentForManager(int appointmentID) throws SQLException {
        requirePositiveID(appointmentID, "AppointmentID");
        String sql = "SELECT MedicalRecordID FROM MedicalRecords WHERE AppointmentID = ?";
        try (PreparedStatement statement = requireConnection().prepareStatement(sql)) {
            statement.setInt(1, appointmentID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt("MedicalRecordID") : null;
            }
        }
    }

    public boolean saveForDoctor(MedicalRecord record, boolean createInvoice, boolean replaceItems)
            throws SQLException {
        requirePositiveID(record.getDoctorID(), "DoctorID");
        return save(record, createInvoice, replaceItems, record.getDoctorID());
    }

    public boolean saveForManager(MedicalRecord record, boolean createInvoice, boolean replaceItems)
            throws SQLException {
        requirePositiveID(record.getDoctorID(), "DoctorID");
        return save(record, createInvoice, replaceItems, null);
    }

    public InvoicePaymentResult confirmInvoicePaymentForDoctor(MedicalRecord record, int doctorID)
            throws SQLException {
        requirePositiveID(record.getMedicalRecordID(), "MedicalRecordID");
        requirePositiveID(doctorID, "DoctorID");
        return confirmInvoicePayment(record, doctorID);
    }

    public InvoicePaymentResult confirmInvoicePaymentForManager(MedicalRecord record) throws SQLException {
        requirePositiveID(record.getMedicalRecordID(), "MedicalRecordID");
        return confirmInvoicePayment(record, null);
    }

    private InvoicePaymentResult confirmInvoicePayment(MedicalRecord record, Integer doctorID)
            throws SQLException {
        int medicalRecordID = record.getMedicalRecordID();
        String scope = doctorID == null ? "" : "AND mr.DoctorID = ?";
        String recordSql = "UPDATE mr SET Symptoms = ?, Diagnosis = ?, TreatmentNotes = ?, "
                + "Status = 'COMPLETED' FROM MedicalRecords mr "
                + "JOIN Invoices i ON i.MedicalRecordID = mr.MedicalRecordID "
                + "WHERE mr.MedicalRecordID = ? AND i.PaymentStatus = 'PAID' "
                + "AND mr.Status NOT IN ('COMPLETED', 'CANCELLED') " + scope;
        try (Connection conn = requireConnection()) {
            String paidAt = hasPaidAtColumn(conn) ? ", PaidAt = SYSDATETIME()" : "";
            String invoiceSql = "UPDATE i SET PaymentStatus = 'PAID'" + paidAt + " FROM Invoices i "
                    + "JOIN MedicalRecords mr ON mr.MedicalRecordID = i.MedicalRecordID "
                    + "WHERE i.MedicalRecordID = ? AND i.PaymentStatus = 'UNPAID' "
                    + "AND mr.Status NOT IN ('COMPLETED', 'CANCELLED') " + scope;
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // Lock the visit before the invoice, as the normal save flow does.
                // A simultaneous save or payment must finish before this one proceeds.
                String lockSql = "SELECT mr.MedicalRecordID FROM MedicalRecords mr WITH (UPDLOCK, HOLDLOCK) "
                        + "WHERE mr.MedicalRecordID = ? " + scope;
                try (PreparedStatement statement = conn.prepareStatement(lockSql)) {
                    statement.setInt(1, medicalRecordID);
                    if (doctorID != null) {
                        statement.setInt(2, doctorID);
                    }
                    try (ResultSet rows = statement.executeQuery()) {
                        if (!rows.next()) {
                            conn.rollback();
                            return InvoicePaymentResult.NOT_FOUND;
                        }
                    }
                }
                int updated;
                try (PreparedStatement statement = conn.prepareStatement(invoiceSql)) {
                    statement.setInt(1, medicalRecordID);
                    if (doctorID != null) {
                        statement.setInt(2, doctorID);
                    }
                    updated = statement.executeUpdate();
                }
                if (updated == 1) {
                    try (PreparedStatement statement = conn.prepareStatement(recordSql)) {
                        setNullableNString(statement, 1, record.getSymptoms());
                        setNullableNString(statement, 2, record.getDiagnosis());
                        setNullableNString(statement, 3, record.getTreatmentNotes());
                        statement.setInt(4, medicalRecordID);
                        if (doctorID != null) {
                            statement.setInt(5, doctorID);
                        }
                        if (statement.executeUpdate() != 1) {
                            throw new SQLException("The medical record could not be completed after payment.");
                        }
                    }
                    conn.commit();
                    return InvoicePaymentResult.CONFIRMED;
                }

                String statusSql = "SELECT i.PaymentStatus, mr.Status FROM Invoices i "
                        + "JOIN MedicalRecords mr ON mr.MedicalRecordID = i.MedicalRecordID "
                        + "WHERE mr.MedicalRecordID = ? " + scope;
                InvoicePaymentResult result;
                try (PreparedStatement statusStatement = conn.prepareStatement(statusSql)) {
                    statusStatement.setInt(1, medicalRecordID);
                    if (doctorID != null) {
                        statusStatement.setInt(2, doctorID);
                    }
                    try (ResultSet resultSet = statusStatement.executeQuery()) {
                        if (!resultSet.next()) {
                            result = InvoicePaymentResult.NOT_FOUND;
                        } else if ("PAID".equals(resultSet.getString("PaymentStatus"))) {
                            result = InvoicePaymentResult.ALREADY_PAID;
                        } else {
                            result = InvoicePaymentResult.NOT_PAYABLE;
                        }
                    }
                }

                conn.commit();
                return result;
            } catch (SQLException | RuntimeException ex) {
                conn.rollback();
                throw ex;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        }
    }

    private boolean save(MedicalRecord record, boolean createInvoice, boolean replaceItems, Integer doctorScope)
            throws SQLException {
        if (!"IN_PROGRESS".equals(record.getStatus()) && !"CANCELLED".equals(record.getStatus())) {
            throw new IllegalArgumentException("Select a valid medical record status.");
        }
        Connection conn = requireConnection();
        boolean originalAutoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            int recordID;
            if (record.getMedicalRecordID() <= 0) {
                validateNewRecordRelations(conn, record, doctorScope);
                String sql = "INSERT INTO MedicalRecords "
                        + "(AppointmentID, PatientID, DoctorID, Symptoms, Diagnosis, TreatmentNotes, Status) "
                        + "OUTPUT INSERTED.MedicalRecordID VALUES (?, ?, ?, ?, ?, ?, 'IN_PROGRESS')";
                try (PreparedStatement statement = conn.prepareStatement(sql)) {
                    setNullableInt(statement, 1, record.getAppointmentID());
                    statement.setInt(2, record.getPatientID());
                    statement.setInt(3, record.getDoctorID());
                    setNullableNString(statement, 4, record.getSymptoms());
                    setNullableNString(statement, 5, record.getDiagnosis());
                    setNullableNString(statement, 6, record.getTreatmentNotes());
                    try (ResultSet resultSet = statement.executeQuery()) {
                        if (!resultSet.next()) {
                            throw new SQLException("Medical record insert did not return an ID.");
                        }
                        recordID = resultSet.getInt(1);
                    }
                }
            } else {
                recordID = record.getMedicalRecordID();
                String sql = "UPDATE MedicalRecords SET Symptoms = ?, Diagnosis = ?, "
                        + "TreatmentNotes = ?, Status = ? "
                        + "WHERE MedicalRecordID = ? AND Status <> 'COMPLETED' "
                        + (doctorScope == null ? "" : "AND DoctorID = ?");
                try (PreparedStatement statement = conn.prepareStatement(sql)) {
                    setNullableNString(statement, 1, record.getSymptoms());
                    setNullableNString(statement, 2, record.getDiagnosis());
                    setNullableNString(statement, 3, record.getTreatmentNotes());
                    statement.setString(4, record.getStatus());
                    statement.setInt(5, recordID);
                    if (doctorScope != null) {
                        statement.setInt(6, doctorScope);
                    }
                    if (statement.executeUpdate() != 1) {
                        if (isCompletedRecord(conn, recordID, doctorScope)) {
                            throw new IllegalStateException("Completed medical records are read-only.");
                        }
                        conn.rollback();
                        return false;
                    }
                }

            }

            boolean hasInvoice = invoiceExists(conn, recordID);
            if (replaceItems) {
                if (hasInvoice || hasIssuedPrescription(conn, recordID)) {
                    throw new IllegalStateException(
                            "Services or an issued prescription cannot be changed after billing or issue.");
                }
                replaceServices(conn, recordID, record.getServices());
                replacePrescriptions(conn, recordID, record.getPrescriptions());
            }
            if (createInvoice && !hasInvoice) {
                createInvoice(conn, recordID);
            }
            conn.commit();
            return true;
        } catch (SQLException | RuntimeException ex) {
            conn.rollback();
            throw ex;
        } finally {
            try {
                conn.setAutoCommit(originalAutoCommit);
            } finally {
                conn.close();
            }
        }
    }

    private boolean isCompletedRecord(Connection conn, int recordID, Integer doctorScope)
            throws SQLException {
        String sql = "SELECT 1 FROM MedicalRecords WHERE MedicalRecordID = ? "
                + "AND Status = 'COMPLETED' " + (doctorScope == null ? "" : "AND DoctorID = ?");
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, recordID);
            if (doctorScope != null) {
                statement.setInt(2, doctorScope);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void validateNewRecordRelations(Connection conn, MedicalRecord record, Integer doctorScope)
            throws SQLException {
        requirePositiveID(record.getPatientID(), "PatientID");
        if (doctorScope != null && doctorScope != record.getDoctorID()) {
            throw new IllegalArgumentException("The selected Doctor does not match the signed-in Doctor.");
        }
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT 1 FROM Doctors WHERE DoctorID = ? AND IsActive = 1")) {
            statement.setInt(1, record.getDoctorID());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("The selected Doctor account is no longer active.");
                }
            }
        }
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT 1 FROM Patients WHERE PatientID = ?")) {
            statement.setInt(1, record.getPatientID());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("The selected patient does not exist.");
                }
            }
        }
        if (record.getAppointmentID() != null) {
            try (PreparedStatement statement = conn.prepareStatement(
                    "SELECT 1 FROM Appointments a "
                    + "WHERE a.AppointmentID = ? AND a.DoctorID = ? AND a.PatientID = ? "
                    + "AND NOT EXISTS (SELECT 1 FROM MedicalRecords mr "
                    + "WHERE mr.AppointmentID = a.AppointmentID)")) {
                statement.setInt(1, record.getAppointmentID());
                statement.setInt(2, record.getDoctorID());
                statement.setInt(3, record.getPatientID());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new IllegalArgumentException(
                                "The appointment is not assigned to this Doctor and patient, or already has a record.");
                    }
                }
            }
        }
    }

    private void replaceServices(Connection conn, int recordID, List<ServiceLine> services)
            throws SQLException {
        Map<Integer, List<java.math.BigDecimal>> previousPrices = loadServicePrices(conn, recordID);
        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM MedicalRecordServices WHERE MedicalRecordID = ?")) {
            statement.setInt(1, recordID);
            statement.executeUpdate();
        }
        String sql = "INSERT INTO MedicalRecordServices "
                + "(MedicalRecordID, ServiceID, Quantity, UnitPrice, Notes) VALUES (?, ?, ?, ?, ?)";
        for (ServiceLine line : services) {
            List<java.math.BigDecimal> savedPrices = previousPrices.get(line.getServiceID());
            java.math.BigDecimal price = savedPrices == null || savedPrices.isEmpty()
                    ? null : savedPrices.remove(0);
            if (price == null) {
                price = activeCatalogPrice(conn,
                        "SELECT Price FROM Services WHERE ServiceID = ? AND IsActive = 1",
                        line.getServiceID(), "service");
            }
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, recordID);
                statement.setInt(2, line.getServiceID());
                statement.setInt(3, line.getQuantity());
                statement.setBigDecimal(4, price);
                setNullableNString(statement, 5, line.getNotes());
                statement.executeUpdate();
            }
        }
    }

    private void replacePrescriptions(Connection conn, int recordID, List<Prescription> prescriptions)
            throws SQLException {
        Map<Integer, List<java.math.BigDecimal>> previousPrices = loadMedicinePrices(conn, recordID);
        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM PrescriptionDetails WHERE PrescriptionID IN "
                + "(SELECT PrescriptionID FROM Prescriptions WHERE MedicalRecordID = ?)")) {
            statement.setInt(1, recordID);
            statement.executeUpdate();
        }
        try (PreparedStatement statement = conn.prepareStatement(
                "DELETE FROM Prescriptions WHERE MedicalRecordID = ?")) {
            statement.setInt(1, recordID);
            statement.executeUpdate();
        }
        String sql = "INSERT INTO Prescriptions (MedicalRecordID, Notes) "
                + "OUTPUT INSERTED.PrescriptionID VALUES (?, ?)";
        for (Prescription prescription : prescriptions) {
            if (prescription.getMedicines().isEmpty()
                    && (prescription.getNotes() == null || prescription.getNotes().isBlank())) {
                continue;
            }
            int prescriptionID;
            try (PreparedStatement statement = conn.prepareStatement(sql)) {
                statement.setInt(1, recordID);
                setNullableNString(statement, 2, prescription.getNotes());
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.next()) {
                        throw new SQLException("Prescription insert did not return an ID.");
                    }
                    prescriptionID = resultSet.getInt(1);
                }
            }
            String detailSql = "INSERT INTO PrescriptionDetails "
                    + "(PrescriptionID, MedicineID, Dosage, Frequency, DurationDays, "
                    + "Quantity, UnitPrice, Instructions) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            for (MedicineLine medicine : prescription.getMedicines()) {
                List<java.math.BigDecimal> savedPrices = previousPrices.get(medicine.getMedicineID());
                java.math.BigDecimal price = savedPrices == null || savedPrices.isEmpty()
                        ? null : savedPrices.remove(0);
                if (price == null) {
                    price = activeCatalogPrice(conn,
                            "SELECT Price FROM Medicines WHERE MedicineID = ? AND IsActive = 1",
                            medicine.getMedicineID(), "medicine");
                }
                try (PreparedStatement statement = conn.prepareStatement(detailSql)) {
                    statement.setInt(1, prescriptionID);
                    statement.setInt(2, medicine.getMedicineID());
                    setNullableNString(statement, 3, medicine.getDosage());
                    setNullableNString(statement, 4, medicine.getFrequency());
                    if (medicine.getDurationDays() == null) {
                        statement.setNull(5, Types.INTEGER);
                    } else {
                        statement.setInt(5, medicine.getDurationDays());
                    }
                    statement.setInt(6, medicine.getQuantity());
                    statement.setBigDecimal(7, price);
                    setNullableNString(statement, 8, medicine.getInstructions());
                    statement.executeUpdate();
                }
            }
        }
    }

    private Map<Integer, List<java.math.BigDecimal>> loadServicePrices(Connection conn, int recordID)
            throws SQLException {
        Map<Integer, List<java.math.BigDecimal>> prices = new LinkedHashMap<>();
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT ServiceID, UnitPrice FROM MedicalRecordServices "
                + "WHERE MedicalRecordID = ? ORDER BY MedicalRecordServiceID")) {
            statement.setInt(1, recordID);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    prices.computeIfAbsent(resultSet.getInt("ServiceID"), ignored -> new ArrayList<>())
                            .add(resultSet.getBigDecimal("UnitPrice"));
                }
            }
        }
        return prices;
    }

    private Map<Integer, List<java.math.BigDecimal>> loadMedicinePrices(Connection conn, int recordID)
            throws SQLException {
        Map<Integer, List<java.math.BigDecimal>> prices = new LinkedHashMap<>();
        String sql = "SELECT pd.MedicineID, pd.UnitPrice FROM PrescriptionDetails pd "
                + "JOIN Prescriptions p ON p.PrescriptionID = pd.PrescriptionID "
                + "WHERE p.MedicalRecordID = ? "
                + "ORDER BY p.PrescriptionDate, p.PrescriptionID, pd.PrescriptionDetailID";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, recordID);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    prices.computeIfAbsent(resultSet.getInt("MedicineID"), ignored -> new ArrayList<>())
                            .add(resultSet.getBigDecimal("UnitPrice"));
                }
            }
        }
        return prices;
    }

    private java.math.BigDecimal activeCatalogPrice(
            Connection conn, String sql, int itemID, String itemType) throws SQLException {
        requirePositiveID(itemID, itemType + "ID");
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, itemID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalArgumentException("The selected " + itemType + " does not exist.");
                }
                return resultSet.getBigDecimal("Price");
            }
        }
    }

    private boolean invoiceExists(Connection conn, int recordID) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT 1 FROM Invoices WHERE MedicalRecordID = ?")) {
            statement.setInt(1, recordID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean hasIssuedPrescription(Connection conn, int recordID) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(
                "SELECT 1 FROM Prescriptions WHERE MedicalRecordID = ? "
                + "AND Status IN ('ISSUED', 'CANCELLED')")) {
            statement.setInt(1, recordID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void createInvoice(Connection conn, int recordID) throws SQLException {
        String amountsSql = "SELECT "
                + "COALESCE((SELECT SUM(Quantity * UnitPrice) FROM MedicalRecordServices "
                + "WHERE MedicalRecordID = ?), 0) AS ServiceAmount, "
                + "COALESCE((SELECT SUM(pd.Quantity * pd.UnitPrice) FROM PrescriptionDetails pd "
                + "JOIN Prescriptions p ON p.PrescriptionID = pd.PrescriptionID "
                + "WHERE p.MedicalRecordID = ? AND p.Status <> 'CANCELLED'), 0) AS MedicineAmount";
        java.math.BigDecimal serviceAmount;
        java.math.BigDecimal medicineAmount;
        try (PreparedStatement statement = conn.prepareStatement(amountsSql)) {
            statement.setInt(1, recordID);
            statement.setInt(2, recordID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Unable to calculate invoice amounts.");
                }
                serviceAmount = resultSet.getBigDecimal("ServiceAmount");
                medicineAmount = resultSet.getBigDecimal("MedicineAmount");
            }
        }
        java.math.BigDecimal total = serviceAmount.add(medicineAmount);
        String sql = "INSERT INTO Invoices "
                + "(MedicalRecordID, ServiceAmount, MedicineAmount, DiscountAmount, TotalAmount) "
                + "VALUES (?, ?, ?, 0, ?)";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, recordID);
            statement.setBigDecimal(2, serviceAmount);
            statement.setBigDecimal(3, medicineAmount);
            statement.setBigDecimal(4, total);
            statement.executeUpdate();
        }
    }

    private void setNullableInt(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private void setNullableNString(PreparedStatement statement, int index, String value) throws SQLException {
        if (value == null || value.isBlank()) {
            statement.setNull(index, Types.NVARCHAR);
        } else {
            statement.setNString(index, value);
        }
    }

    private void requirePositiveID(int id, String label) {
        if (id <= 0) {
            throw new IllegalArgumentException(label + " must be positive.");
        }
    }

    private void loadServices(Connection conn, MedicalRecord record) throws SQLException {
        String sql = "SELECT mrs.ServiceID, s.ServiceName, mrs.Quantity, mrs.UnitPrice, mrs.Notes "
                + "FROM MedicalRecordServices mrs JOIN Services s ON s.ServiceID = mrs.ServiceID "
                + "WHERE mrs.MedicalRecordID = ? ORDER BY mrs.MedicalRecordServiceID";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, record.getMedicalRecordID());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    ServiceLine line = new ServiceLine();
                    line.setServiceID(resultSet.getInt("ServiceID"));
                    line.setServiceName(resultSet.getString("ServiceName"));
                    line.setQuantity(resultSet.getInt("Quantity"));
                    line.setUnitPrice(resultSet.getBigDecimal("UnitPrice"));
                    line.setNotes(resultSet.getString("Notes"));
                    record.getServices().add(line);
                }
            }
        }
    }

    private void loadPrescriptions(Connection conn, MedicalRecord record) throws SQLException {
        String sql = "SELECT p.PrescriptionID, p.PrescriptionDate, p.Notes AS PrescriptionNotes, "
                + "p.Status AS PrescriptionStatus, pd.PrescriptionDetailID, pd.MedicineID, m.MedicineName, "
                + "m.ActiveIngredient, m.Unit, pd.Dosage, pd.Frequency, pd.DurationDays, "
                + "pd.Quantity, pd.UnitPrice, pd.Instructions "
                + "FROM Prescriptions p LEFT JOIN PrescriptionDetails pd "
                + "ON pd.PrescriptionID = p.PrescriptionID "
                + "LEFT JOIN Medicines m ON m.MedicineID = pd.MedicineID "
                + "WHERE p.MedicalRecordID = ? "
                + "ORDER BY p.PrescriptionDate, p.PrescriptionID, pd.PrescriptionDetailID";
        Map<Integer, Prescription> prescriptions = new LinkedHashMap<>();
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, record.getMedicalRecordID());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int prescriptionID = resultSet.getInt("PrescriptionID");
                    Prescription prescription = prescriptions.get(prescriptionID);
                    if (prescription == null) {
                        prescription = new Prescription();
                        prescription.setPrescriptionID(prescriptionID);
                        prescription.setPrescriptionDate(
                                resultSet.getTimestamp("PrescriptionDate").toLocalDateTime());
                        prescription.setNotes(resultSet.getString("PrescriptionNotes"));
                        prescription.setStatus(resultSet.getString("PrescriptionStatus"));
                        prescriptions.put(prescriptionID, prescription);
                    }
                    if (resultSet.getObject("PrescriptionDetailID") != null) {
                        MedicineLine medicine = new MedicineLine();
                        medicine.setMedicineID(resultSet.getInt("MedicineID"));
                        medicine.setMedicineName(resultSet.getString("MedicineName"));
                        medicine.setActiveIngredient(resultSet.getString("ActiveIngredient"));
                        medicine.setUnit(resultSet.getString("Unit"));
                        medicine.setDosage(resultSet.getString("Dosage"));
                        medicine.setFrequency(resultSet.getString("Frequency"));
                        int durationDays = resultSet.getInt("DurationDays");
                        medicine.setDurationDays(resultSet.wasNull() ? null : durationDays);
                        medicine.setQuantity(resultSet.getInt("Quantity"));
                        medicine.setUnitPrice(resultSet.getBigDecimal("UnitPrice"));
                        medicine.setInstructions(resultSet.getString("Instructions"));
                        prescription.getMedicines().add(medicine);
                    }
                }
            }
        }
        record.getPrescriptions().addAll(prescriptions.values());
    }

    private Invoice loadInvoice(Connection conn, int medicalRecordID) throws SQLException {
        String sql = "SELECT InvoiceID, InvoiceDate, ServiceAmount, MedicineAmount, DiscountAmount, "
                + "TotalAmount, PaymentMethod, PaymentStatus, Notes "
                + "FROM Invoices WHERE MedicalRecordID = ?";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, medicalRecordID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Invoice invoice = new Invoice();
                invoice.setInvoiceID(resultSet.getInt("InvoiceID"));
                invoice.setInvoiceDate(resultSet.getTimestamp("InvoiceDate").toLocalDateTime());
                invoice.setServiceAmount(resultSet.getBigDecimal("ServiceAmount"));
                invoice.setMedicineAmount(resultSet.getBigDecimal("MedicineAmount"));
                invoice.setDiscountAmount(resultSet.getBigDecimal("DiscountAmount"));
                invoice.setTotalAmount(resultSet.getBigDecimal("TotalAmount"));
                invoice.setPaymentMethod(resultSet.getString("PaymentMethod"));
                invoice.setPaymentStatus(resultSet.getString("PaymentStatus"));
                invoice.setNotes(resultSet.getString("Notes"));
                return invoice;
            }
        }
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection could not be established.");
        }
        return connection;
    }
}
