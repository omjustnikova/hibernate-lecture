package ru.hh.school.jdbc;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.hibernate.LazyInitializationException;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class RelationsTest {

  private static DataSource dataSource;
  private static SessionFactory sessionFactory;
  private static TransactionHelper transactionHelper;

  private static List<Integer> expectedUserIds;

  @BeforeClass
  public static void setUpTestSuit() {
    EmbeddedPostgres embeddedPostgres = null;

    try {
      embeddedPostgres = EmbeddedPostgres.builder()
              .setPort(5433)
              .start();
    } catch (IOException e) {
      e.printStackTrace();
    }

    dataSource = createDataSource();
    sessionFactory = createSessionFactory();
    transactionHelper = new TransactionHelper(sessionFactory);

      if (embeddedPostgres != null) {
          TestHelper.executeScript(embeddedPostgres.getPostgresDatabase(), "prepare_data.sql");
      }
  }

  private static DataSource createDataSource() {
    PGSimpleDataSource ds = new PGSimpleDataSource();
    ds.setUser("postgres");
    ds.setPassword("postgres");
    ds.setUrl("jdbc:postgresql://localhost:5433/postgres");
    return ds;
  }

  private static SessionFactory createSessionFactory() {
    Metadata metadata = new MetadataSources()
      .addAnnotatedClass(User.class)
      .addAnnotatedClass(Resume.class)
      .buildMetadata();

    return metadata.buildSessionFactory();
  }

  @Before
  public void prepareTestData() {
    TestHelper.clearResumes(dataSource);
    TestHelper.clearUsers(dataSource);
    expectedUserIds = TestHelper.insertTestData(dataSource);
  }


  @Test
  public void fetchAssociatedEntitiesWithJdbc() {
    try (Connection connection = dataSource.getConnection()) {
      try (PreparedStatement statement = connection.prepareStatement(
        "SELECT * " +
          "FROM hhuser AS u " +
          "JOIN resume AS r ON u.user_id = r.user_id " +
          "WHERE u.user_id BETWEEN ? AND ? "
      )) {
        statement.setInt(1, expectedUserIds.get(0));
        statement.setInt(2, expectedUserIds.get(expectedUserIds.size() - 1));

        try (ResultSet resultSet = statement.executeQuery()) {

          Map<Integer, User> userMap = new LinkedHashMap<>();
          while (resultSet.next()) {
            Integer userId = resultSet.getInt(1);
            User user = userMap.get(userId);
            if (user == null) {
              user = new User();
              user.setId(userId);
              user.setFirstName(resultSet.getString(2));
              user.setLastName(resultSet.getString(3));
              userMap.put(userId, user);
            }
            Resume resume = new Resume();
            resume.setId(resultSet.getInt(4));
            resume.setDescription(resultSet.getString(5));
            user.addResume(resume);
          }

          List<User> users = new ArrayList<>(userMap.values());


          assertEquals(expectedUserIds.size(), users.size());
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }


  @Test
  public void fetchAssociatedEntitiesWithHibernate() {
    List<User> users = transactionHelper.inTransaction(this::fetchUsersWithResumes);

    assertEquals(expectedUserIds.size(), users.size());
    assertEquals("description0", users.get(0).getResumes().get(0).getDescription());
  }

  private List<User> fetchUsersWithResumes() {
    return sessionFactory.getCurrentSession().createQuery(
      "select distinct u " +
        "from User u " +
        "join fetch u.resumes " +
        "where u.id between :lo and :hi", User.class)
      .setParameter("lo", expectedUserIds.get(0))
      .setParameter("hi", expectedUserIds.get(expectedUserIds.size() - 1))
      .list();
  }


  @Test(expected = LazyInitializationException.class)
  public void fetchAssociatedEntitiesWithHibernateLazy() {
    List<User> users = transactionHelper.inTransaction(this::fetchUsersWithResumesLazy);

    assertEquals(expectedUserIds.size(), users.size());
    assertEquals("description0",
        users
            .get(0)
            .getResumes()
            .get(0)
            .getDescription());
  }

  private List<User> fetchUsersWithResumesLazy() {
    return sessionFactory.getCurrentSession().createQuery(
        "select distinct u " +
            "from User u " +
            "where u.id between :lo and :hi", User.class)
        .setParameter("lo", expectedUserIds.get(0))
        .setParameter("hi", expectedUserIds.get(expectedUserIds.size() - 1))
        .list();
  }


  @Test
  public void getDescriptionFromLazyFieldInTransaction() {
    String description = transactionHelper.inTransaction(this::getDescriptionFromLazyField);

    assertEquals("description0", description);
  }

  private String getDescriptionFromLazyField() {
    List<User> users = fetchUsersWithResumesLazy();
    return users.get(0).getResumes().get(0).getDescription();
  }


  @Test
  public void fetchAssociatedEntitiesWithHibernateLazyButThenMerge() {
    List<User> users = transactionHelper.inTransaction(this::fetchUsersWithResumesLazy);

    String description = transactionHelper.inTransaction(()->getFirstResumeDescriptionFromLazyInitUser(users.get(0)));
    assertEquals("description0", description);
  }

  private String getFirstResumeDescriptionFromLazyInitUser(User user) {
    sessionFactory
        .getCurrentSession()
        .update(user);
    return user
        .getResumes()
        .get(0)
        .getDescription();
  }


}
