package ru.hh.school.users;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class UserDao {

  private final DataSource dataSource;

  public UserDao(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public Set<User> getAll() {
    // TODO Implement
    Set<User> result = new HashSet<>();
    try (
        Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from hhuser");
    ) {
      while (resultSet.next()) {
        result.add(User.existing(
            resultSet.getInt("user_id"),
            resultSet.getString("first_name"),
            resultSet.getString("last_name")
        ));
      }
    } catch (SQLException e) {
      throw new RuntimeException("Fail getting all users");
    }
    return result;
  }

  public void saveNew(User user) {
    if (user.getId() != null) {
      throw new IllegalArgumentException("User " + user + " already exists");
    }
    // TODO Implement om prepared statement
    try (
        Connection connection = dataSource.getConnection();
        PreparedStatement statement = connection.prepareStatement(
            "insert into hhuser (first_name, last_name) " +
            "values (?, ?)", PreparedStatement.RETURN_GENERATED_KEYS
        );
    ) {
      statement.setString(1, user.getFirstName());
      statement.setString(2, user.getLastName());
      statement.executeUpdate();

      ResultSet generatedKeys = statement.getGeneratedKeys();
      while (generatedKeys.next()) {
        user.setId(generatedKeys.getInt("user_id"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new RuntimeException("Fail saving a new user");
    }
  }

  public void deleteAll() {
    try (Connection connection = dataSource.getConnection()) {

      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate("delete from hhuser");
      }

    } catch (SQLException e) {
      throw new RuntimeException("Can't delete all users", e);
    }
  }

  public Optional<User> getBy(int userId) {
    try (Connection connection = dataSource.getConnection()) {

      try (PreparedStatement statement = connection.prepareStatement(
        "SELECT user_id, first_name, last_name FROM hhuser WHERE user_id = ?")) {

        statement.setInt(1, userId);

        try (ResultSet resultSet = statement.executeQuery()) {

          boolean userExists = resultSet.next();
          if (!userExists) {
            return Optional.empty();
          }
          return Optional.of(
            User.existing(
              userId,
              resultSet.getString("first_name"),
              resultSet.getString("last_name")
            )
          );
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("failed to get user by id " + userId, e);
    }
  }

  public void deleteBy(int userId) {
    try (Connection connection = dataSource.getConnection()) {

      try(PreparedStatement statement = connection.prepareStatement(
        "DELETE FROM hhuser WHERE user_id = ?")) {

        statement.setInt(1, userId);

        // TODO: нужное раскомментить
        statement.executeUpdate();
//      statement.executeQuery();
      }

    } catch (SQLException e) {
      throw new RuntimeException("failed to remove user by id " + userId, e);
    }
  }

  public void update(User user) {
    if (user.getId() == null) {
      throw new IllegalArgumentException("can not update " + user + " without id");
    }

    try(Connection connection = dataSource.getConnection()) {

      try (PreparedStatement statement = connection.prepareStatement(
        "update hhuser set first_name=?, last_name=? where user_id=?")) {

        //TODO:
        //реализовать установку параметров и вызов запроса
        statement.setString(1, user.getFirstName());
        statement.setString(2, user.getLastName());
        statement.setInt(3,  user.getId());

        statement.executeUpdate();
      }

    } catch (SQLException e) {
      throw new RuntimeException("failed to update " + user, e);
    }
  }


}
