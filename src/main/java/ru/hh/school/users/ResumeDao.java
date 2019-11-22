package ru.hh.school.users;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.Optional;

public class ResumeDao {
    private final SessionFactory sessionFactory;

    public ResumeDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void saveNew(Resume resume) {
        // TODO Implement
    }

    public Optional<Resume> getBy(int id) {
        return Optional.empty();
        // TODO Implement
    }

    private Session session() {
        return sessionFactory.getCurrentSession();
    }
}
