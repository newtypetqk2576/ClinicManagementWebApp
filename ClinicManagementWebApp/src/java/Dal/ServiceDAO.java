package Dal;

import Model.Service;
import jakarta.servlet.ServletContext;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class ServiceDAO extends DBContext {
    public ServiceDAO(ServletContext servletContext) {
        super(servletContext);
    }

    public List<Service> getAll() throws SQLException {
        String sql = "SELECT ServiceID, ServiceName, Description, Price, IsActive, CreatedAt "
                + "FROM Services ORDER BY ServiceID";
        List<Service> services = new ArrayList<>();
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                services.add(mapService(resultSet));
            }
        }
        return services;
    }

    public Service getByID(int serviceID) throws SQLException {
        String sql = "SELECT ServiceID, ServiceName, Description, Price, IsActive, CreatedAt "
                + "FROM Services WHERE ServiceID = ?";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setInt(1, serviceID);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? mapService(resultSet) : null;
            }
        }
    }

    public void create(Service service) throws SQLException {
        String sql = "INSERT INTO Services (ServiceName, Description, Price, IsActive) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            setServiceFields(statement, service);
            statement.executeUpdate();
        }
    }

    public boolean update(Service service) throws SQLException {
        String sql = "UPDATE Services SET ServiceName = ?, Description = ?, Price = ?, IsActive = ? "
                + "WHERE ServiceID = ?";
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            setServiceFields(statement, service);
            statement.setInt(5, service.getServiceID());
            return statement.executeUpdate() == 1;
        }
    }

    public List<Service> getActiveServices() throws SQLException {
        String sql = "SELECT ServiceID, ServiceName, Description, Price, IsActive, CreatedAt "
                + "FROM Services WHERE IsActive = 1 ORDER BY ServiceName";
        List<Service> services = new ArrayList<>();
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql);
                ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                services.add(mapService(resultSet));
            }
        }
        return services;
    }

    public List<Service> searchActiveByName(String searchTerm) throws SQLException {
        String term = searchTerm == null ? "" : searchTerm.trim();
        if (term.isEmpty()) {
            return new ArrayList<>();
        }
        String escapedTerm = term.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_")
                .replace("[", "\\[");
        String sql = "SELECT TOP (10) ServiceID, ServiceName, Description, Price, IsActive, CreatedAt "
                + "FROM Services WHERE IsActive = 1 AND ServiceName LIKE ? ESCAPE '\\' "
                + "ORDER BY ServiceName, ServiceID";
        List<Service> services = new ArrayList<>();
        try (Connection conn = requireConnection();
                PreparedStatement statement = conn.prepareStatement(sql)) {
            statement.setNString(1, "%" + escapedTerm + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    services.add(mapService(resultSet));
                }
            }
        }
        return services;
    }

    private Service mapService(ResultSet resultSet) throws SQLException {
        Service service = new Service();
        service.setServiceID(resultSet.getInt("ServiceID"));
        service.setServiceName(resultSet.getString("ServiceName"));
        service.setDescription(resultSet.getString("Description"));
        service.setPrice(resultSet.getBigDecimal("Price"));
        service.setActive(resultSet.getBoolean("IsActive"));
        service.setCreatedAt(resultSet.getTimestamp("CreatedAt").toLocalDateTime());
        return service;
    }

    private void setServiceFields(PreparedStatement statement, Service service) throws SQLException {
        statement.setNString(1, service.getServiceName());
        if (service.getDescription() == null) {
            statement.setNull(2, Types.NVARCHAR);
        } else {
            statement.setNString(2, service.getDescription());
        }
        statement.setBigDecimal(3, service.getPrice());
        statement.setBoolean(4, service.isActive());
    }

    private Connection requireConnection() throws SQLException {
        if (connection == null) {
            throw new SQLException("Database connection could not be established.");
        }
        return connection;
    }
}
