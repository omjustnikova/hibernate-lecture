package ru.hh.school.users;

import org.hibernate.SessionFactory;

import java.util.Optional;

public class ResumeService {
    private final ResumeDao resumeDao;
    private final TransactionHelper th;

    public ResumeService(
            SessionFactory sessionFactory,
            ResumeDao resumeDao) {
        this.resumeDao = resumeDao;
        this.th = new TransactionHelper(sessionFactory);
    }

    public void saveNew(Resume resume) {
        th.inTransaction(() -> resumeDao.saveNew(resume));
    }

    public Optional<Resume> getBy(int resumeId) {
        return th.inTransaction(() -> resumeDao.getBy(resumeId));
    }
}
