package Dal;

import Model.User;
import Model.Doctor;
import Model.Patient;
import jakarta.servlet.ServletContext;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserDAO extends DBContext {
    public static class DuplicateRegistrationException extends SQLException {
        private final String field;

        public DuplicateRegistrationException(String field) {
            super(field);
            this.field = field;
        }

        public DuplicateRegistrationException(String field, SQLException cause) {
            super(field, cause);
            this.field = field;
        }

        public String getField() {
            return field;
        }
    }

    public static class UnlinkedProfileUnavailableException extends SQLException {
        public UnlinkedProfileUnavailableException() {
            super("The selected patient or doctor is no longer available for account linking.");
        }
    }

    public UserDAO(ServletContext servletContext) {
        super(servletContext);
    }

    public User login(String username, String password) throws SQLException {
        String sql = "SELECT u.UserID, u.Username, u.Role, u.IsActive, "
                + "p.PatientID, d.DoctorID FROM Users u "
                + "LEFT JOIN Patients p ON p.UserID = u.UserID "
                + "LEFT JOIN Doctors d ON d.UserID = u.UserID "
                + "WHERE u.Username = ? AND u.PasswordHash = ? AND u.IsActive = 1";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, password);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                User user = new User();
                user.setUserID(resultSet.getInt("UserID"));
                user.setUsername(resultSet.getString("Username"));
                user.setRole(resultSet.getString("Role"));
                user.setActive(resultSet.getBoolean("IsActive"));
                int patientID = resultSet.getInt("PatientID");
                user.setPatientID(resultSet.wasNull() ? null : patientID);
                int doctorID = resultSet.getInt("DoctorID");
                user.setDoctorID(resultSet.wasNull() ? null : doctorID);
                return user;
            }
        }
    }

    public void registerPatient(String username, String password, String fullName, String gender,
            String phone, String email, String address) throws SQLException {
        try (Connection conn = requireConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                try {
                    if (exists(conn, "SELECT 1 FROM Users WHERE Username = ?", username)) {
                        throw new DuplicateRegistrationException("username");
                    }
                    if (exists(conn, "SELECT 1 FROM Patients WHERE Phone = ?", phone)) {
                        throw new DuplicateRegistrationException("phone");
                    }

                    int userID;
                    String insertUser = "INSERT INTO Users (Username, PasswordHash, Role) "
                            + "VALUES (?, ?, 'PATIENT')";
                    try (PreparedStatement statement = conn.prepareStatement(
                            insertUser, Statement.RETURN_GENERATED_KEYS)) {
                        statement.setString(1, username);
                        statement.setString(2, password);
                        statement.executeUpdate();
                        try (ResultSet keys = statement.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException("User insert did not return a UserID.");
                            }
                            userID = keys.getInt(1);
                        }
                    }

                    String insertPatient = "INSERT INTO Patients "
                            + "(FullName, Gender, Phone, Email, Address, UserID) "
                            + "VALUES (?, ?, ?, ?, ?, ?)";
                    try (PreparedStatement statement = conn.prepareStatement(insertPatient)) {
                        statement.setString(1, fullName);
                        statement.setNString(2, gender);
                        statement.setString(3, phone);
                        statement.setString(4, email);
                        statement.setString(5, address);
                        statement.setInt(6, userID);
                        statement.executeUpdate();
                    }
                    conn.commit();
                } catch (SQLException | RuntimeException ex) {
                    rollback(conn, ex);
                    if (ex instanceof SQLException sqlException && isUniqueViolation(sqlException)) {
                        throw new DuplicateRegistrationException("usernameOrPhone", sqlException);
                    }
                    throw ex;
                }
            } finally {
                if (conn.getAutoCommit() != originalAutoCommit) {
                    conn.setAutoCommit(originalAutoCommit);
                }
            }
        }
    }

    public boolean changePassword(int userID, String currentPassword, String newPassword)
            throws SQLException {
        String sql = "UPDATE Users SET PasswordHash = ?, UpdatedAt = SYSDATETIME() "
                + "WHERE UserID = ? AND PasswordHash = ? AND IsActive = 1";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, newPassword);
            statement.setInt(2, userID);
            statement.setString(3, currentPassword);
            return statement.executeUpdate() == 1;
        }
    }

    public List<User> getUsersForManager() throws SQLException {
        String sql = "SELECT UserID, Username, Role, IsActive, CreatedAt, UpdatedAt "
                + "FROM Users ORDER BY Username, UserID";
        List<User> users = new ArrayList<>();
        try (PreparedStatement statement = requireConnection().prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                User user = new User();
                user.setUserID(resultSet.getInt("UserID"));
                user.setUsername(resultSet.getString("Username"));
                user.setRole(resultSet.getString("Role"));
                user.setActive(resultSet.getBoolean("IsActive"));
                user.setCreatedAt(resultSet.getTimestamp("CreatedAt").toLocalDateTime());
                java.sql.Timestamp updatedAt = resultSet.getTimestamp("UpdatedAt");
                user.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());
                users.add(user);
            }
        }
        return users;
    }

    public User getUserDetailsForManager(int userID) throws SQLException {
        String sql = "SELECT u.UserID, u.Username, u.PasswordHash, u.Role, u.IsActive, "
                + "u.CreatedAt, u.UpdatedAt, "
                + "p.PatientID AS LinkedPatientID, p.FullName AS PatientFullName, "
                + "p.DateOfBirth AS PatientDateOfBirth, p.Gender AS PatientGender, "
                + "p.Phone AS PatientPhone, p.Email AS PatientEmail, "
                + "p.Address AS PatientAddress, p.CreatedAt AS PatientCreatedAt, "
                + "d.DoctorID AS LinkedDoctorID, d.FullName AS DoctorFullName, "
                + "d.Specialty AS DoctorSpecialty, d.Phone AS DoctorPhone, "
                + "d.IsActive AS DoctorIsActive "
                + "FROM Users u LEFT JOIN Patients p ON p.UserID = u.UserID "
                + "LEFT JOIN Doctors d ON d.UserID = u.UserID WHERE u.UserID = ?";
        try (PreparedStatement statement = requireConnection().prepareStatement(sql)) {
            statement.setInt(1, userID);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                User user = new User();
                user.setUserID(resultSet.getInt("UserID"));
                user.setUsername(resultSet.getString("Username"));
                user.setPasswordHash(resultSet.getString("PasswordHash"));
                user.setRole(resultSet.getString("Role"));
                user.setActive(resultSet.getBoolean("IsActive"));
                user.setCreatedAt(resultSet.getTimestamp("CreatedAt").toLocalDateTime());
                java.sql.Timestamp updatedAt = resultSet.getTimestamp("UpdatedAt");
                user.setUpdatedAt(updatedAt == null ? null : updatedAt.toLocalDateTime());

                int patientID = resultSet.getInt("LinkedPatientID");
                if (!resultSet.wasNull()) {
                    Patient patient = new Patient();
                    patient.setPatientID(patientID);
                    patient.setFullName(resultSet.getString("PatientFullName"));
                    java.sql.Date dateOfBirth = resultSet.getDate("PatientDateOfBirth");
                    patient.setDateOfBirth(dateOfBirth == null ? null : dateOfBirth.toLocalDate());
                    patient.setGender(resultSet.getString("PatientGender"));
                    patient.setPhone(resultSet.getString("PatientPhone"));
                    patient.setEmail(resultSet.getString("PatientEmail"));
                    patient.setAddress(resultSet.getString("PatientAddress"));
                    patient.setCreatedAt(resultSet.getTimestamp("PatientCreatedAt").toLocalDateTime());
                    patient.setUserID(userID);
                    user.setPatientID(patientID);
                    user.setPatient(patient);
                }

                int doctorID = resultSet.getInt("LinkedDoctorID");
                if (!resultSet.wasNull()) {
                    Doctor doctor = new Doctor();
                    doctor.setDoctorID(doctorID);
                    doctor.setFullName(resultSet.getString("DoctorFullName"));
                    doctor.setSpecialty(resultSet.getString("DoctorSpecialty"));
                    doctor.setPhone(resultSet.getString("DoctorPhone"));
                    doctor.setActive(resultSet.getBoolean("DoctorIsActive"));
                    doctor.setUserID(userID);
                    user.setDoctorID(doctorID);
                    user.setDoctor(doctor);
                }
                return user;
            }
        }
    }

    public List<Patient> searchUnlinkedPatientsForManager(String searchTerm) throws SQLException {
        String pattern = searchPattern(searchTerm);
        String sql = "SELECT TOP (20) PatientID, FullName, Phone FROM Patients "
                + "WHERE UserID IS NULL AND (FullName LIKE ? ESCAPE '\\' "
                + "OR Phone LIKE ? ESCAPE '\\') "
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

    public List<Doctor> searchUnlinkedDoctorsForManager(String searchTerm) throws SQLException {
        String pattern = searchPattern(searchTerm);
        String sql = "SELECT TOP (20) DoctorID, FullName, Phone FROM Doctors "
                + "WHERE UserID IS NULL AND (FullName LIKE ? ESCAPE '\\' "
                + "OR Phone LIKE ? ESCAPE '\\') "
                + "ORDER BY FullName, DoctorID";
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

    public void createUserForManager(String username, String password, String role,
            Integer patientID, Integer doctorID) throws SQLException {
        String table = null;
        String idColumn = null;
        Integer profileID = null;
        if ("PATIENT".equals(role) && patientID != null && doctorID == null) {
            table = "Patients";
            idColumn = "PatientID";
            profileID = patientID;
        } else if ("DOCTOR".equals(role) && doctorID != null && patientID == null) {
            table = "Doctors";
            idColumn = "DoctorID";
            profileID = doctorID;
        } else if (!("MANAGER".equals(role) && patientID == null && doctorID == null)) {
            throw new IllegalArgumentException("Select a valid role and matching profile.");
        }

        try (Connection conn = requireConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            try {
                conn.setAutoCommit(false);
                try {
                    if (profileID != null
                            && !lockUnlinkedProfile(conn, table, idColumn, profileID)) {
                        throw new UnlinkedProfileUnavailableException();
                    }

                    int userID;
                    String insertUser = "INSERT INTO Users (Username, PasswordHash, Role) "
                            + "OUTPUT INSERTED.UserID VALUES (?, ?, ?)";
                    try (PreparedStatement statement = conn.prepareStatement(insertUser)) {
                        statement.setString(1, username);
                        statement.setString(2, password);
                        statement.setString(3, role);
                        try (ResultSet keys = statement.executeQuery()) {
                            if (!keys.next()) {
                                throw new SQLException("User insert did not return a UserID.");
                            }
                            userID = keys.getInt(1);
                        } catch (SQLException ex) {
                            if (isUniqueViolation(ex)) {
                                throw new DuplicateRegistrationException("username", ex);
                            }
                            throw ex;
                        }
                    }

                    if (profileID != null) {
                        String linkProfile = "UPDATE " + table + " SET UserID = ? "
                                + "WHERE " + idColumn + " = ? AND UserID IS NULL";
                        try (PreparedStatement statement = conn.prepareStatement(linkProfile)) {
                            statement.setInt(1, userID);
                            statement.setInt(2, profileID);
                            if (statement.executeUpdate() != 1) {
                                throw new UnlinkedProfileUnavailableException();
                            }
                        }
                    }
                    conn.commit();
                } catch (SQLException | RuntimeException ex) {
                    rollback(conn, ex);
                    throw ex;
                }
            } finally {
                if (conn.getAutoCommit() != originalAutoCommit) {
                    conn.setAutoCommit(originalAutoCommit);
                }
            }
        }
    }

    public boolean updateUserForManager(int userID, String username, String password,
            boolean active) throws SQLException {
        String sql = password == null
                ? "UPDATE Users SET Username = ?, IsActive = ?, UpdatedAt = SYSDATETIME() WHERE UserID = ?"
                : "UPDATE Users SET Username = ?, PasswordHash = ?, IsActive = ?, "
                        + "UpdatedAt = SYSDATETIME() WHERE UserID = ?";
        try (PreparedStatement statement = requireConnection().prepareStatement(sql)) {
            statement.setString(1, username);
            if (password == null) {
                statement.setBoolean(2, active);
                statement.setInt(3, userID);
            } else {
                statement.setString(2, password);
                statement.setBoolean(3, active);
                statement.setInt(4, userID);
            }
            try {
                return statement.executeUpdate() == 1;
            } catch (SQLException ex) {
                if (isUniqueViolation(ex)) {
                    throw new DuplicateRegistrationException("username", ex);
                }
                throw ex;
            }
        }
    }

    private boolean lockUnlinkedProfile(Connection conn, String table, String idColumn, int profileID)
            throws SQLException {
        String sql = "SELECT " + idColumn + " FROM " + table
                + " WITH (UPDLOCK, HOLDLOCK) WHERE " + idColumn + " = ? AND UserID IS NULL";
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, profileID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private String searchPattern(String searchTerm) {
        String term = searchTerm == null ? "" : searchTerm.trim();
        String escapedTerm = term.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("[", "\\[");
        return "%" + escapedTerm + "%";
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection could not be established.");
        }
        return connection;
    }

    private boolean exists(Connection conn, String sql, String value) throws SQLException {
        try (PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setString(1, value);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private void rollback(Connection conn, Throwable cause) {
        try {
            conn.rollback();
        } catch (SQLException rollbackException) {
            cause.addSuppressed(rollbackException);
        }
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
