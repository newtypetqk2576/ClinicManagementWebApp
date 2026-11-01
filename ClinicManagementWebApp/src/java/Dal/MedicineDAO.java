package Dal;

import Model.Medicine;
import jakarta.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MedicineDAO extends DBContext {
    public MedicineDAO(ServletContext servletContext) {
        super(servletContext);
    }

    public List<Medicine> getAll() throws SQLException {
        String sql = "SELECT MedicineID, MedicineName, ActiveIngredient, Unit, Price, "
                + "StockQuantity, IsActive, CreatedAt FROM Medicines "
                + "ORDER BY MedicineID";
        List<Medicine> medicines = new ArrayList<>();
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                medicines.add(mapMedicine(resultSet));
            }
        }
        return medicines;
    }

    public Medicine getByID(int medicineID) throws SQLException {
        String sql = "SELECT MedicineID, MedicineName, ActiveIngredient, Unit, Price, "
                + "StockQuantity, IsActive, CreatedAt FROM Medicines WHERE MedicineID = ?";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, medicineID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapMedicine(resultSet) : null;
            }
        }
    }

    public List<Medicine> searchActiveByName(String searchTerm) throws SQLException {
        String term = searchTerm == null ? "" : searchTerm.trim();
        if (term.isEmpty()) {
            return new ArrayList<>();
        }
        String escapedTerm = term.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("[", "\\[");
        String sql = "SELECT TOP (10) MedicineID, MedicineName, ActiveIngredient, Unit, Price, "
                + "StockQuantity, IsActive, CreatedAt FROM Medicines "
                + "WHERE IsActive = 1 AND MedicineName LIKE ? ESCAPE '\\' "
                + "ORDER BY MedicineName, MedicineID";
        List<Medicine> medicines = new ArrayList<>();
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setNString(1, "%" + escapedTerm + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    medicines.add(mapMedicine(resultSet));
                }
            }
        }
        return medicines;
    }

    public void create(Medicine medicine) throws SQLException {
        String sql = "INSERT INTO Medicines (MedicineName, ActiveIngredient, Unit, Price, "
                + "StockQuantity, IsActive) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            setMedicineFields(statement, medicine);
            statement.executeUpdate();
        }
    }

    public boolean update(Medicine medicine) throws SQLException {
        String sql = "UPDATE Medicines SET MedicineName = ?, ActiveIngredient = ?, Unit = ?, "
                + "Price = ?, StockQuantity = ?, IsActive = ? WHERE MedicineID = ?";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            setMedicineFields(statement, medicine);
            statement.setInt(7, medicine.getMedicineID());
            return statement.executeUpdate() == 1;
        }
    }

    private Medicine mapMedicine(ResultSet resultSet) throws SQLException {
        Medicine medicine = new Medicine();
        medicine.setMedicineID(resultSet.getInt("MedicineID"));
        medicine.setMedicineName(resultSet.getString("MedicineName"));
        medicine.setActiveIngredient(resultSet.getString("ActiveIngredient"));
        medicine.setUnit(resultSet.getString("Unit"));
        medicine.setPrice(resultSet.getBigDecimal("Price"));
        medicine.setStockQuantity(resultSet.getInt("StockQuantity"));
        medicine.setActive(resultSet.getBoolean("IsActive"));
        medicine.setCreatedAt(resultSet.getTimestamp("CreatedAt").toLocalDateTime());
        return medicine;
    }

    private void setMedicineFields(PreparedStatement statement, Medicine medicine) throws SQLException {
        statement.setNString(1, medicine.getMedicineName());
        if (medicine.getActiveIngredient() == null) {
            statement.setNull(2, Types.NVARCHAR);
        } else {
            statement.setNString(2, medicine.getActiveIngredient());
        }
        statement.setNString(3, medicine.getUnit());
        statement.setBigDecimal(4, medicine.getPrice());
        statement.setInt(5, medicine.getStockQuantity());
        statement.setBoolean(6, medicine.isActive());
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection could not be established.");
        }
        return connection;
    }
}
