package Dal;

import Model.Doctor;
import jakarta.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO extends DBContext {
    public DoctorDAO(ServletContext servletContext) {
        super(servletContext);
    }

    public List<Doctor> getAll() throws SQLException {
        String sql = "SELECT DoctorID, FullName, Specialty, Phone, IsActive, UserID "
                + "FROM Doctors ORDER BY FullName, DoctorID";
        List<Doctor> doctors = new ArrayList<>();
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorID(resultSet.getInt("DoctorID"));
                doctor.setFullName(resultSet.getString("FullName"));
                doctor.setSpecialty(resultSet.getString("Specialty"));
                doctor.setPhone(resultSet.getString("Phone"));
                doctor.setActive(resultSet.getBoolean("IsActive"));
                int userID = resultSet.getInt("UserID");
                doctor.setUserID(resultSet.wasNull() ? null : userID);
                doctors.add(doctor);
            }
        }
        return doctors;
    }

    public int create(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO Doctors (FullName, Specialty, Phone, IsActive) "
                + "OUTPUT INSERTED.DoctorID VALUES (?, ?, ?, ?)";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            setDoctorFields(statement, doctor);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new SQLException("Doctor insert did not return a DoctorID.");
                }
                return resultSet.getInt(1);
            }
        }
    }

    public boolean update(Doctor doctor) throws SQLException {
        String sql = "UPDATE Doctors SET FullName = ?, Specialty = ?, Phone = ?, IsActive = ? "
                + "WHERE DoctorID = ?";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            setDoctorFields(statement, doctor);
            statement.setInt(5, doctor.getDoctorID());
            return statement.executeUpdate() == 1;
        }
    }

    public List<Doctor> getActive() throws SQLException {
        String sql = "SELECT DoctorID, FullName, Specialty FROM Doctors "
                + "WHERE IsActive = 1 ORDER BY FullName";
        List<Doctor> doctors = new ArrayList<>();
        if (connection == null) {
            throw new SQLException("Database connection could not be established.");
        }
        try (PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                Doctor doctor = new Doctor();
                doctor.setDoctorID(resultSet.getInt("DoctorID"));
                doctor.setFullName(resultSet.getString("FullName"));
                doctor.setSpecialty(resultSet.getString("Specialty"));
                doctors.add(doctor);
            }
        }
        return doctors;
    }

    public List<Doctor> searchActiveByNameOrPhone(String searchTerm) throws SQLException {
        String term = searchTerm == null ? "" : searchTerm.trim();
        if (term.isEmpty()) {
            return new ArrayList<>();
        }
        String escapedTerm = term.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("[", "\\[");
        String pattern = "%" + escapedTerm + "%";
        String sql = "SELECT TOP (10) DoctorID, FullName, Phone FROM Doctors "
                + "WHERE IsActive = 1 AND (FullName LIKE ? ESCAPE '\\' "
                + "OR Phone LIKE ? ESCAPE '\\') ORDER BY FullName, DoctorID";
        List<Doctor> doctors = new ArrayList<>();
        try (PreparedStatement statement = requireConnection().prepareStatement(sql)) {
            statement.setNString(1, pattern);
            statement.setString(2, pattern);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Doctor doctor = new Doctor();
                    doctor.setDoctorID(resultSet.getInt("DoctorID"));
                    doctor.setFullName(resultSet.getString("FullName"));
                    doctor.setPhone(resultSet.getString("Phone"));
                    doctors.add(doctor);
                }
            }
        }
        return doctors;
    }

    public Doctor getByID(int doctorID) throws SQLException {
        String sql = "SELECT DoctorID, FullName, Specialty, Phone, IsActive, UserID "
                + "FROM Doctors WHERE DoctorID = ?";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, doctorID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }
                Doctor doctor = new Doctor();
                doctor.setDoctorID(resultSet.getInt("DoctorID"));
                doctor.setFullName(resultSet.getString("FullName"));
                doctor.setSpecialty(resultSet.getString("Specialty"));
                doctor.setPhone(resultSet.getString("Phone"));
                doctor.setActive(resultSet.getBoolean("IsActive"));
                int userID = resultSet.getInt("UserID");
                doctor.setUserID(resultSet.wasNull() ? null : userID);
                return doctor;
            }
        }
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection could not be established.");
        }
        return connection;
    }

    private void setDoctorFields(PreparedStatement statement, Doctor doctor) throws SQLException {
        statement.setNString(1, doctor.getFullName());
        statement.setNString(2, doctor.getSpecialty());
        statement.setString(3, doctor.getPhone());
        statement.setBoolean(4, doctor.isActive());
    }
}
