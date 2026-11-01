package Dal;

import jakarta.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author FPT University - PRJ30X
 */
public class DBContext {
    protected Connection connection;
    public DBContext(ServletContext servletContext) {
        //@Students: You are not allowed to edit this method  
        try {
            Properties properties = new Properties();
            try (InputStream inputStream = servletContext.getResourceAsStream("/WEB-INF/ConnectDB.properties")) {
                if (inputStream == null) {
                    throw new IOException("Unable to find /WEB-INF/ConnectDB.properties");
                }
                properties.load(inputStream);
            }
            String user = properties.getProperty("userID");
            String pass = properties.getProperty("password");
            String url = properties.getProperty("url");
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            connection = DriverManager.getConnection(url, user, pass);
        } catch (IOException | ClassNotFoundException | SQLException ex) {
            Logger.getLogger(DBContext.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
