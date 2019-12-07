package ru.hh.school.users;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.hh.school.TestHelper;

import javax.persistence.PersistenceException;
import java.io.IOException;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class UserServiceTest {

    private static UserService userService;
    private static EmbeddedPostgres embeddedPostgres = null;


    @BeforeClass
    public static void setUp() {
        try {
            embeddedPostgres = EmbeddedPostgres.builder()
                    .setPort(5433)
                    .start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        SessionFactory sessionFactory = createSessionFactory();

        userService = new UserService(
                sessionFactory,
                new UserDao(sessionFactory)
        );

        if (embeddedPostgres != null) {
            TestHelper.executeScript(embeddedPostgres.getPostgresDatabase(), "create_hhuser.sql");
        }
    }

    @AfterClass
    public static void shutdown(){
      try {
        embeddedPostgres.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    private static SessionFactory createSessionFactory() {
        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .loadProperties("hibernate.properties")
                .build();

        Metadata metadata = new MetadataSources(serviceRegistry)
                .addAnnotatedClass(User.class)
                .buildMetadata();

        return metadata.buildSessionFactory();
    }


    @Before
    public void cleanUpDb() {
        userService.deleteAll();
    }

    public void insert_users() {
        TestHelper.executeScript(embeddedPostgres.getPostgresDatabase(), "insert_hhusers.sql");
    }

    @Test
    public void getAllUsersShouldReturnEmptySet() {
        insert_users();
        final Set<User> all = userService.getAll();
        assertEquals(2, all.size());
        assertTrue(all.stream().anyMatch(u->u.getFirstName().equals("Sarah")));
    }

    @Test
    public void saveNewUserShouldInsertDbRow() {
        User user = new User("John", "Lennon");
        userService.saveNew(user);
        assertEquals(Set.of(user), userService.getAll());
    }

    @Test(expected = PersistenceException.class)
    public void savingOfExistingUserShouldBePrevented() {
        User user = new User("John", "Lennon");
        userService.saveNew(user);
        userService.saveNew(user);

        fail();
    }

    @Test
    public void updateFirstNameShouldSucceed() {
        User user = new User("John", "Lennon");
        userService.saveNew(user);
        userService.changeFullName(user.getId(), "Paul", "McCartney");

        assertEquals(
                "Paul",
                userService.getBy(user.getId()).map(User::getFirstName).get()
        );
    }

    @Test
    public void getByIdShouldReturnUserIfRowExists() {
        User user = new User("John", "Lennon");
        userService.saveNew(user);

        Optional<User> extractedUser = userService.getBy(user.getId());

        assertTrue(extractedUser.isPresent());
        assertEquals(user, extractedUser.get());
    }


    @Test
    public void getByIdShouldReturnEmptyIfRowDoesntExist() {
        assertFalse(userService.getBy(-1).isPresent());
    }

    @Test
    public void deleteUserShouldDeleteDbRow() {
        User user = new User("John", "Lennon");
        userService.saveNew(user);

        Optional<User> extractedUser = userService.getBy(user.getId());
        assertTrue(extractedUser.isPresent());

        userService.deleteBy(user.getId());

        extractedUser = userService.getBy(user.getId());
        assertFalse(extractedUser.isPresent());
    }

    @Test
    public void updateShouldUpdateDbRowOfExistingUser() {
        User user = new User("Ringo", "Lennon");
        userService.saveNew(user);

        user.setFirstName("John");
        userService.update(user);

        assertEquals(
                "John",
                userService.getBy(user.getId()).map(User::getFirstName).orElse(null)
        );
    }

}
