package ru.hh.school.jdbc;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public class TestHelper {

  private final static int USERS_COUNT = 5;
  private final static int RESUMES_PER_USER = 2;

  private final static String USER_INSERT_QUERY = "insert into hhuser(first_name, last_name) values (?, ?)";
  private final static String RESUME_INSERT_QUERY = "insert into resume(description, user_id) values (?, ?)";

  private static final Path SCRIPTS_DIR = Path.of("src","main", "resources", "scripts");

  public static List<Integer> insertTestData(DataSource dataSource) {
    List<Integer> result = new ArrayList<>();
    try (Connection connection = dataSource.getConnection()) {

      try (
        PreparedStatement userStatement = connection.prepareStatement(USER_INSERT_QUERY, Statement.RETURN_GENERATED_KEYS);
        PreparedStatement resumeStatement = connection.prepareStatement(RESUME_INSERT_QUERY);
        ) {

        for (int i = 0; i < USERS_COUNT; i++) {
          Integer userId = createUser(userStatement, i);
          result.add(userId);

          for (int j = 0; j< RESUMES_PER_USER; j++) {
            createResume(resumeStatement, userId, j);
          }
        }
      }


    } catch (SQLException e) {
      throw new RuntimeException("can't prepare test data", e);
    }

    return result;
  }

  private static Integer createUser(PreparedStatement userStatement, int userIndex) throws SQLException {
    userStatement.setString(1, "name" + userIndex);
    userStatement.setString(2, "familyName" + userIndex);
    userStatement.executeUpdate();
    Integer userId = null;
    try (ResultSet generatedKeys = userStatement.getGeneratedKeys()) {
      if (generatedKeys.next()) {
        userId = generatedKeys.getInt("user_id");
      }
    }
    return userId;
  }

  private static void createResume(PreparedStatement resumeStatement, Integer userId, int resumeIndex) throws SQLException {
    resumeStatement.setString(1, "description" + resumeIndex);
    resumeStatement.setInt(2, userId);
    resumeStatement.executeUpdate();
  }

  public static void clearUsers(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate("delete from hhuser");
      }
    } catch (SQLException e) {
      throw new RuntimeException("can't clear hhuser table", e);
    }
  }

  public static void clearResumes(DataSource dataSource) {
    try (Connection connection = dataSource.getConnection()) {
      try (Statement statement = connection.createStatement()) {
        statement.executeUpdate("delete from resume");
      }
    } catch (SQLException e) {
      throw new RuntimeException("can't clear resume table", e);
    }
  }

  /**
   * Файл должен лежать в resources/scripts
   */
  public static void executeScript(DataSource dataSource, String scriptFileName) {
    splitToQueries(SCRIPTS_DIR.resolve(scriptFileName))
            .forEach((query) -> execute(dataSource, query));
  }

  public static void execute(DataSource dataSource, String query) {
    try (Connection connection = dataSource.getConnection();
         Statement statement = connection.createStatement()) {
      statement.executeUpdate(query);
    } catch (SQLException e) {
      throw new RuntimeException("Can't execute query " + query, e);
    }
  }

  private static Stream<String> splitToQueries(Path path) {
    try {
      return Arrays.stream(Files.readString(path).split(";"));
    } catch (IOException e) {
      throw new RuntimeException("Can't read file " + path, e);
    }
  }



}
