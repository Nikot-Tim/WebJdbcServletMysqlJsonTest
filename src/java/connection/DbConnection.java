package connection;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DbConnection  {
    public  DbConnection(){}
    public static Connection getConnection() throws IOException {
        Properties properties = new Properties();
        Connection connection = null;
        try (InputStream in = Files.newInputStream(Paths.get("D:\\WebJdbcServletMysqlJsonTest\\src\\resources\\connection.properties"))) {
            properties.load(in);
        }
        String url = properties.getProperty("url");
        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        String driver = properties.getProperty("driver");

        try{
            Class.forName(driver);
            connection = DriverManager.getConnection(url,username,password);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return connection;
    }
}
