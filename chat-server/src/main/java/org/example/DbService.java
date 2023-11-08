package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class DbService {

    private final String url;
    private Connection connection;
    private final String query = "SELECT * FROM users u where u.name = 'nmn'";
    private final String queryUpdateName = "update users set \"name\" = '??' where \"name\" = '!!'";

    public DbService(String url) {
        this.url = url;
    }

    public void connect() {
        try {
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(url);
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        } finally {
            try {
                connection.close();
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    public boolean checkUserInDb(String userName, String password) {

        try (Statement statement = connection.createStatement()) {

            ResultSet resultSet = statement.executeQuery(query.replace("nmn", userName));

            while (resultSet.next()) {
                if (password.equals(resultSet.getString(3))) {
                    return true;
                }
            }

        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }

        return false;
    }

    public boolean updateLogin(String oldUserName, String newUserName) {

        try (Statement statement = connection.createStatement()) {

            int resultSet = statement.executeUpdate(queryUpdateName.replace("!!", oldUserName).replace("??", newUserName));

            if (resultSet > 0) {
                return true;
            }

        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }

        return false;

    }
}
