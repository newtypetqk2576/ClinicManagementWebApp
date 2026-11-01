package Dal;

import Model.Patient;
import jakarta.servlet.ServletContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class PatientDAO extends DBContext {
    public static class DuplicatePhoneException extends SQLException {
        public DuplicatePhoneException(SQLException cause) {
            super("A patient with this phone number already exists.", cause);
        }
    }

    public PatientDAO(ServletContext servletContext) {
        super(servletContext);
    }

    public List<Patient> getAll() throws SQLException {
        String sql = "SELECT PatientID, FullName, DateOfBirth, Gender, Phone, Email, Address, CreatedAt, UserID "
                + "FROM Patients ORDER BY FullName, PatientID";
        List<Patient> patients = new ArrayList<>();
        try (PreparedStatement statement = requireConnection().prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                patients.add(mapPatient(resultSet));
            }
        }
        return patients;
    }

    public Patient getByID(int patientID) throws SQLException {
        String sql = "SELECT PatientID, FullName, DateOfBirth, Gender, Phone, Email, Address, CreatedAt, UserID "
                + "FROM Patients WHERE PatientID = ?";
        try (PreparedStatement statement = requireConnection().prepareStatement(sql)) {
            statement.setInt(1, patientID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapPatient(resultSet) : null;
            }
        }
    }

    public List<Patient> searchByNameOrPhone(String searchTerm) throws SQLException {
        String term = searchTerm == null ? "" : searchTerm.trim();
        if (term.isEmpty()) {
            return new ArrayList<>();
        }
        String escapedTerm = term.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("[", "\\[");
        String pattern = "%" + escapedTerm + "%";
        String sql = "SELECT TOP (10) PatientID, FullName, Phone FROM Patients "
                + "WHERE FullName LIKE ? ESCAPE '\\' OR Phone LIKE ? ESCAPE '\\' "
                + "ORDER BY FullName, PatientID";
        List<Patient> patients = new ArrayList<>();
        try (PreparedStatement statement = requireConnection().prepareStatement(sql)) {
            statement.setNString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Patient patient = new Patient();
                    patient.setPatientID(resultSet.getInt("PatientID"));
                    patient.setFullName(resultSet.getString("FullName"));
                    patient.setPhone(resultSet.getString("Phone"));
                    patients.add(patient);
                }
            }
        }
        return patients;
    }

    public boolean existsByID(int patientID) throws SQLException {
        String sql = "SELECT 1 FROM Patients WHERE PatientID = ?";
        try (PreparedStatement statement = requireConnection().prepareStatement(sql)) {
            statement.setInt(1, patientID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    public int create(Patient patient) throws SQLException {
        String sql = "INSERT INTO Patients (FullName, DateOfBirth, Gender, Phone, Email, Address) "
                + "OUTPUT INSERTED.PatientID VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = requireConnection().prepareStatement(sql)) {
            setPatientFields(statement, patient);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Patient insert did not return a PatientID.");
                }
                return resultSet.getInt(1);
            }
        } catch (SQLException ex) {
            if (isUniqueViolation(ex)) {
                throw new DuplicatePhoneException(ex);
            }
            throw ex;
        }
    }

    public boolean update(Patient patient) throws SQLException {
        String sql = "UPDATE Patients SET FullName = ?, DateOfBirth = ?, Gender = ?, "
                + "Phone = ?, Email = ?, Address = ? WHERE PatientID = ?";
        try (PreparedStatement statement = requireConnection().prepareStatement(sql)) {
            setPatientFields(statement, patient);
            statement.setInt(7, patient.getPatientID());
            try {
                return statement.executeUpdate() == 1;
            } catch (SQLException ex) {
                if (isUniqueViolation(ex)) {
                    throw new DuplicatePhoneException(ex);
                }
                throw ex;
            }
        }
    }

    private Patient mapPatient(ResultSet resultSet) throws SQLException {
        Patient patient = new Patient();
        patient.setPatientID(resultSet.getInt("PatientID"));
        patient.setFullName(resultSet.getString("FullName"));
        if (resultSet.getDate("DateOfBirth") != null) {
            patient.setDateOfBirth(resultSet.getDate("DateOfBirth").toLocalDate());
        }
        patient.setGender(resultSet.getString("Gender"));
        patient.setPhone(resultSet.getString("Phone"));
        patient.setEmail(resultSet.getString("Email"));
        patient.setAddress(resultSet.getString("Address"));
        patient.setCreatedAt(resultSet.getTimestamp("CreatedAt").toLocalDateTime());
        int userID = resultSet.getInt("UserID");
        patient.setUserID(resultSet.wasNull() ? null : userID);
        return patient;
    }

    private void setPatientFields(PreparedStatement statement, Patient patient) throws SQLException {
        statement.setNString(1, patient.getFullName());
        if (patient.getDateOfBirth() == null) {
            statement.setNull(2, Types.DATE);
        } else {
            statement.setDate(2, java.sql.Date.valueOf(patient.getDateOfBirth()));
        }
        if (patient.getGender() == null) {
            statement.setNull(3, Types.NVARCHAR);
        } else {
            statement.setNString(3, patient.getGender());
        }
        statement.setString(4, patient.getPhone());
        statement.setString(5, patient.getEmail());
        statement.setNString(6, patient.getAddress());
    }

    private java.sql.Connection requireConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection could not be established.");
        }
        return connection;
    }

    private boolean isUniqueViolation(SQLException exception) {
        for (SQLException current = exception; current != null; current = current.getNextException()) {
            if (current.getErrorCode() == 2601 || current.getErrorCode() == 2627) {
                return true;
            }
        }
        return false;
    }
}
