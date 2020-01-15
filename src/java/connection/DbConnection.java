package connection;

import org.springframework.beans.factory.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

@Component
public class DbConnection  {
    public DbConnection(){}
    public Connection getConnection() throws IOException {
        Properties properties = new Properties();
        Connection connection = null;
        try (InputStream in = Files.newInputStream(Paths.get("connection.properties"))) {
            properties.load(in);
        }
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");

        try{
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(url,username,password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}