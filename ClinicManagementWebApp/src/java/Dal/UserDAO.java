package Dal;

import Model.User;
import jakarta.servlet.ServletContext;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public void registerPatient(String username, String password, String fullName,
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
                            + "(FullName, Phone, Email, Address, UserID) VALUES (?, ?, ?, ?, ?)";
                    try (PreparedStatement statement = conn.prepareStatement(insertPatient)) {
                        statement.setString(1, fullName);
                        statement.setString(2, phone);
                        statement.setString(3, email);
                        statement.setString(4, address);
                        statement.setInt(5, userID);
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
