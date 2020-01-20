package dao;

import connection.DbConnection;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import model.User;
import org.springframework.beans.factory.stereotype.Component;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@Component
public class UserDaoImpl implements UserDao{

    private static final String INSERT_USERS_SQL = "INSERT INTO users" + "  (name, email, country) VALUES " + " (?, ?, ?);";
    private static final String SELECT_USER_BY_ID = "select id,name,email,country from users where id =?";
    private static final String SELECT_ALL_USERS = "select * from users";
    private static final String DELETE_USER_SQL = "delete from users where id = ?;";
    private static final String UPDATE_USER_SQL = "update users set name = ?,email= ?, country =? where id = ?;";

    public UserDaoImpl(){}

    @Override
    public boolean saveUser(User user) throws SQLException, IOException {
        try(PreparedStatement preparedStatement = DbConnection.getConnection().prepareStatement(INSERT_USERS_SQL)) {
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getCountry());
            return preparedStatement.execute();
        }
    }

    @Override
    public User findUserById(int id) throws SQLException, IOException {
        try(PreparedStatement preparedStatement = DbConnection.getConnection().prepareStatement(SELECT_USER_BY_ID)) {
            preparedStatement.setInt(1, id);

            ResultSet resultSet = preparedStatement.executeQuery();
            User user = null;
            while (resultSet.next()) {
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String country = resultSet.getString("country");
                user = new User(id, name, email, country);

            }
            return user;
        }
    }

    @Override
    public List<User> findAll() throws SQLException, IOException {
        try(PreparedStatement preparedStatement = DbConnection.getConnection().prepareStatement(SELECT_ALL_USERS)) {
            ResultSet resultSet = preparedStatement.executeQuery();
            List<User> users = new ArrayList<>();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String name = resultSet.getString("name");
                String email = resultSet.getString("email");
                String country = resultSet.getString("country");
                users.add(new User(id, name, email, country));
            }
            return users;
        }
    }

    @Override
    public boolean deleteUser(int id) throws IOException, SQLException {
        try(PreparedStatement preparedStatement = DbConnection.getConnection().prepareStatement(DELETE_USER_SQL)){
            preparedStatement.setInt(1,id);
            return preparedStatement.executeUpdate() > 0;
        }
    }

    @Override
    public boolean updateUser(User user) throws IOException, SQLException {
        try(PreparedStatement preparedStatement = DbConnection.getConnection().prepareStatement(UPDATE_USER_SQL)){
            preparedStatement.setString(1, user.getName());
            preparedStatement.setString(2, user.getEmail());
            preparedStatement.setString(3, user.getCountry());
            preparedStatement.setInt(4, user.getId());
            return preparedStatement.executeUpdate() > 1;
        }
    }

}
