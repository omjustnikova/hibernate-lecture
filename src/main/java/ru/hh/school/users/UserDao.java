package ru.hh.school.users;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import javax.persistence.criteria.CriteriaBuilder;
import java.util.Optional;
import java.util.Set;

public class UserDao {

    private final SessionFactory sessionFactory;

    public UserDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public Set<User> getAll() {
        //TODO: implement
        return null;
    }

    public void saveNew(User user) {
        //TODO: implement
    }

    public Optional<User> getBy(int id) {
        return Optional.ofNullable(
                session().get(User.class, id)
        );
    }

    public void deleteBy(int id) {
        // jpa2.1 criteria builder

        CriteriaBuilder builder = session().getCriteriaBuilder();

        var query = builder.createCriteriaDelete(User.class);
        query.where(
                builder.equal(query.from(User.class).get("id"), id)
        );

        session().createQuery(query).executeUpdate();

        // + есть ещё 2 способа это сделать
    }

    public void deleteAll() {
        session().createQuery("delete from User").executeUpdate();
    }

    public void update(User user) {
        //TODO: implement
    }

    private Session session() {
        return sessionFactory.getCurrentSession();
    }
}
