package ru.hh.school;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.stream.Stream;

public class TestHelper {
  private static final Path SCRIPTS_DIR = Path.of("src","main", "resources", "scripts");

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
