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
        String sql = "SELECT DoctorID, FullName, Specialty, Phone, IsActive, UserID FROM Doctors";
        List<Doctor> doctors = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
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
}
