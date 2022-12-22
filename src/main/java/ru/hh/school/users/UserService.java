package ru.hh.school.users;

import org.hibernate.SessionFactory;
import java.util.Optional;
import java.util.Set;

public class UserService {

  private final UserDao userDao;
  private final TransactionHelper th;

  public UserService(
          SessionFactory sessionFactory,
          UserDao userDao) {
    this.userDao = userDao;
    this.th = new TransactionHelper(sessionFactory);
  }

  public Set<User> getAll() {
    return th.inTransaction(userDao::getAll);
  }

  public void deleteAll() {
    th.inTransaction(userDao::deleteAll);
  }

  public void saveNew(User user) {
    th.inTransaction(() -> userDao.saveNew(user));
  }

  public Optional<User> getBy(int userId) {
    return th.inTransaction(() -> userDao.getBy(userId));
  }

  public void deleteBy(int userId) {
    th.inTransaction(() -> userDao.deleteBy(userId));
  }

  public void update(User user) {
    th.inTransaction(() -> userDao.update(user));
  }

  public void changeFullName(int userId, String firstName, String lastName) {
    th.inTransaction(() -> {
      final User user = userDao.getBy(userId).orElse(null);
      if (user == null){
        return;
      }
      user.setFirstName(firstName);
      user.setLastName(lastName);
      // хибер отслеживает изменения сущностей и выполняет sql update перед коммитом транзакции
    });
  }

}
