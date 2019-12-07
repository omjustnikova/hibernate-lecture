package ru.hh.school.users;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ResumeDao {
    private final SessionFactory sessionFactory;

    public ResumeDao(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void saveNew(Resume resume) {
        // TODO Implement
    }

    public Optional<Resume> getBy(int id) {
        // TODO Implement
        return Optional.empty();
    }

    public Set<Resume> getActiveResumesForUserId(int userId) {
        // TODO Implement
        return new HashSet<>();
    }

    private Session session() {
        return sessionFactory.getCurrentSession();
    }
}
