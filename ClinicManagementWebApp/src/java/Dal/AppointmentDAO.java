package Dal;

import Model.Appointment;
import jakarta.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class AppointmentDAO extends DBContext {
    public AppointmentDAO(ServletContext servletContext) {
        super(servletContext);
    }

    public List<Appointment> getAll() throws SQLException {
        return queryAppointments(null);
    }

    public List<Appointment> getAllForDoctor(int doctorID) throws SQLException {
        if (doctorID <= 0) {
            throw new IllegalArgumentException("DoctorID must be positive.");
        }
        String sql = "SELECT a.AppointmentID, a.PatientID, p.FullName AS PatientName, a.DoctorID, "
                + "d.FullName AS DoctorName, a.AppointmentDate, a.Reason, a.Status, a.CreatedAt "
                + "FROM Appointments a JOIN Patients p ON p.PatientID = a.PatientID "
                + "JOIN Doctors d ON d.DoctorID = a.DoctorID WHERE a.DoctorID = ? "
                + "ORDER BY a.AppointmentDate DESC";
        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, doctorID);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Appointment appointment = new Appointment();
                    appointment.setAppointmentID(resultSet.getInt("AppointmentID"));
                    appointment.setPatientID(resultSet.getInt("PatientID"));
                    appointment.setPatientName(resultSet.getString("PatientName"));
                    appointment.setDoctorID(resultSet.getInt("DoctorID"));
                    appointment.setDoctorName(resultSet.getString("DoctorName"));
                    appointment.setAppointmentDate(resultSet.getTimestamp("AppointmentDate").toLocalDateTime());
                    appointment.setReason(resultSet.getString("Reason"));
                    appointment.setStatus(resultSet.getString("Status"));
                    appointment.setCreatedAt(resultSet.getTimestamp("CreatedAt").toLocalDateTime());
                    appointments.add(appointment);
                }
            }
        }
        return appointments;
    }

    public Appointment getDetails(int appointmentID) throws SQLException {
        return getDetails(appointmentID, null);
    }

    public Appointment getDetailsForDoctor(int appointmentID, int doctorID) throws SQLException {
        if (doctorID <= 0) {
            throw new IllegalArgumentException("DoctorID must be positive.");
        }
        return getDetails(appointmentID, doctorID);
    }

    private Appointment getDetails(int appointmentID, Integer doctorID) throws SQLException {
        String sql = "SELECT a.AppointmentID, a.PatientID, a.DoctorID, d.FullName AS DoctorName, "
                + "a.AppointmentDate, a.Reason, a.Status, a.CreatedAt, "
                + "p.FullName AS PatientName, p.DateOfBirth, p.Gender, p.Phone, p.Email, p.Address, "
                + "p.CreatedAt AS PatientCreatedAt, p.UserID "
                + "FROM Appointments a JOIN Patients p ON p.PatientID = a.PatientID "
                + "JOIN Doctors d ON d.DoctorID = a.DoctorID WHERE a.AppointmentID = ? "
                + (doctorID == null ? "" : "AND a.DoctorID = ?");
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, appointmentID);
            if (doctorID != null) {
                statement.setInt(2, doctorID);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Appointment appointment = new Appointment();
                appointment.setAppointmentID(resultSet.getInt("AppointmentID"));
                appointment.setPatientID(resultSet.getInt("PatientID"));
                appointment.setDoctorID(resultSet.getInt("DoctorID"));
                appointment.setDoctorName(resultSet.getString("DoctorName"));
                appointment.setAppointmentDate(resultSet.getTimestamp("AppointmentDate").toLocalDateTime());
                appointment.setReason(resultSet.getString("Reason"));
                appointment.setStatus(resultSet.getString("Status"));
                appointment.setCreatedAt(resultSet.getTimestamp("CreatedAt").toLocalDateTime());

                Model.Patient patient = new Model.Patient();
                patient.setPatientID(resultSet.getInt("PatientID"));
                patient.setFullName(resultSet.getString("PatientName"));
                if (resultSet.getDate("DateOfBirth") != null) {
                    patient.setDateOfBirth(resultSet.getDate("DateOfBirth").toLocalDate());
                }
                patient.setGender(resultSet.getString("Gender"));
                patient.setPhone(resultSet.getString("Phone"));
                patient.setEmail(resultSet.getString("Email"));
                patient.setAddress(resultSet.getString("Address"));
                patient.setCreatedAt(resultSet.getTimestamp("PatientCreatedAt").toLocalDateTime());
                int userID = resultSet.getInt("UserID");
                patient.setUserID(resultSet.wasNull() ? null : userID);
                appointment.setPatient(patient);
                return appointment;
            }
        }
    }

    public boolean confirmForDoctor(int appointmentID, int doctorID) throws SQLException {
        String sql = "UPDATE Appointments SET Status = 'CONFIRMED' "
                + "WHERE AppointmentID = ? AND DoctorID = ? AND Status = 'BOOKED'";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, appointmentID);
            statement.setInt(2, doctorID);
            return statement.executeUpdate() == 1;
        }
    }

    public List<Appointment> getByPatientID(int patientID) throws SQLException {
        if (patientID <= 0) {
            throw new IllegalArgumentException("PatientID must be positive.");
        }
        return queryAppointments(patientID);
    }

    private List<Appointment> queryAppointments(Integer patientID) throws SQLException {
        String sql = "SELECT a.AppointmentID, a.PatientID, a.DoctorID, d.FullName AS DoctorName, "
                + "a.AppointmentDate, a.Reason, a.Status, a.CreatedAt, "
                + "CASE WHEN a.Status IN ('BOOKED', 'CONFIRMED') "
                + "AND a.AppointmentDate > SYSDATETIME() THEN CAST(1 AS BIT) ELSE CAST(0 AS BIT) "
                + "END AS Cancellable "
                + "FROM Appointments a JOIN Doctors d ON d.DoctorID = a.DoctorID "
                + (patientID == null ? "" : "WHERE a.PatientID = ? ")
                + "ORDER BY a.AppointmentDate DESC";
        List<Appointment> appointments = new ArrayList<>();
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            if (patientID != null) {
                statement.setInt(1, patientID);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Appointment appointment = new Appointment();
                    appointment.setAppointmentID(resultSet.getInt("AppointmentID"));
                    appointment.setPatientID(resultSet.getInt("PatientID"));
                    appointment.setDoctorID(resultSet.getInt("DoctorID"));
                    appointment.setDoctorName(resultSet.getString("DoctorName"));
                    appointment.setAppointmentDate(resultSet.getTimestamp("AppointmentDate").toLocalDateTime());
                    appointment.setReason(resultSet.getString("Reason"));
                    appointment.setStatus(resultSet.getString("Status"));
                    appointment.setCreatedAt(resultSet.getTimestamp("CreatedAt").toLocalDateTime());
                    appointment.setCancellable(resultSet.getBoolean("Cancellable"));
                    appointments.add(appointment);
                }
            }
        }
        return appointments;
    }

    public boolean createForPatient(int patientID, int doctorID, LocalDateTime appointmentDate, String reason)
            throws SQLException {
        String sql = "INSERT INTO Appointments (PatientID, DoctorID, AppointmentDate, Reason) "
                + "SELECT ?, d.DoctorID, ?, ? FROM Doctors d "
                + "WHERE d.DoctorID = ? AND d.IsActive = 1";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, patientID);
            statement.setTimestamp(2, Timestamp.valueOf(appointmentDate));
            if (reason == null) {
                statement.setNull(3, Types.NVARCHAR);
            } else {
                statement.setNString(3, reason);
            }
            statement.setInt(4, doctorID);
            return statement.executeUpdate() == 1;
        }
    }

    public boolean cancelForPatient(int appointmentID, int patientID) throws SQLException {
        String sql = "UPDATE Appointments SET Status = 'CANCELLED' "
                + "WHERE AppointmentID = ? AND PatientID = ? "
                + "AND Status IN ('BOOKED', 'CONFIRMED') "
                + "AND AppointmentDate > SYSDATETIME()";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, appointmentID);
            statement.setInt(2, patientID);
            return statement.executeUpdate() == 1;
        }
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection could not be established.");
        }
        return connection;
    }
}
