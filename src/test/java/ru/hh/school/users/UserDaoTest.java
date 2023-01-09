package ru.hh.school.users;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.postgresql.ds.PGSimpleDataSource;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

public class UserDaoTest {

  private static UserDao userDao;
  private static PGSimpleDataSource ds;

  @Before
  public void cleanUpDb() {
    userDao.deleteAll();
  }

  @Test
  public void getAllUsersShouldReturnTwoEnyties() {
    TestHelper.executeScript(ds, "insert_some_users.sql");
    Set<User> users = userDao.getAll();
    assertEquals(2, users.size());
    assertTrue(
        users
            .stream()
            .anyMatch(u -> u.getFirstName().equals("John"))
    );
  }

  @Test
  public void saveNewUserShouldInsertDbRow() {
    User user = User.newUser("John", "Lennon");
    userDao.saveNew(user);
    assertEquals(Set.of(user), userDao.getAll());
  }

  @Test(expected = IllegalArgumentException.class)
  public void savingOfExistingUserShouldBePrevented() {
    User user = User.newUser("John", "Lennon");
    userDao.saveNew(user);
    userDao.saveNew(user);

    fail();
  }

  @Test
  public void getByIdShouldReturnUserIfRowExists() {
    User user = User.newUser("John", "Lennon");
    userDao.saveNew(user);

    Optional<User> extractedUser = userDao.getBy(user.getId());

    assertTrue(extractedUser.isPresent());
    assertEquals(user, extractedUser.get());
  }


  @Test
  public void getByIdShouldReturnEmptyIfRowDoesntExist() {
    assertFalse(userDao.getBy(-1).isPresent());
  }

  @Test
  public void deleteUserShouldDeleteDbRow() {
    User user = User.newUser("John", "Lennon");
    userDao.saveNew(user);

    Optional<User> extractedUser = userDao.getBy(user.getId());
    assertTrue(extractedUser.isPresent());

    userDao.deleteBy(user.getId());

    extractedUser = userDao.getBy(user.getId());
    assertFalse(extractedUser.isPresent());
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateShouldThrowExceptionForNewUsers() {
    User user = User.newUser("John", "Lennon");
    userDao.update(user);

    fail();
  }

  @Test(expected = IllegalArgumentException.class)
  public void updateShouldThrowExceptionForUsersMissingInDB() {
    User user = User.newUser("John", "Lennon");
    userDao.update(user);

    fail();
  }

  @Test
  public void updateShouldUpdateDbRowOfExistingUser() {
    User user = User.newUser("Ringo", "Lennon");
    userDao.saveNew(user);

    user.setFirstName("John");
    userDao.update(user);

    assertEquals(
      "John",
      userDao.getBy(user.getId()).map(User::getFirstName).orElse(null)
    );
  }

  @BeforeClass
  public static void setUpDatasource() {
    try {
      EmbeddedPostgres.builder()
          .setPort(5433)
          .start();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    ds = new PGSimpleDataSource();
    ds.setUser("postgres");
    ds.setPassword("postgres");
    ds.setUrl("jdbc:postgresql://localhost:5433/postgres");
    userDao = new UserDao(ds);

    TestHelper.executeScript(ds, "create_hhuser.sql");
  }


}
