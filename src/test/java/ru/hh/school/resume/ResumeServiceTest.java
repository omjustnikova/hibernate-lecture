package ru.hh.school.resume;

import com.opentable.db.postgres.embedded.EmbeddedPostgres;
import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.service.ServiceRegistry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.hh.school.TestHelper;
import ru.hh.school.users.*;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ResumeServiceTest {

    private static ResumeService resumeService;
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

        resumeService = new ResumeService(
                sessionFactory,
                new ResumeDao(sessionFactory)
        );

        if (embeddedPostgres != null) {
            TestHelper.executeScript(embeddedPostgres.getPostgresDatabase(), "create_resume.sql");
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
                .addAnnotatedClass(Resume.class)
                .buildMetadata();

        return metadata.buildSessionFactory();
    }

    @Test
    public void saveNewResumeShouldInsertDbRow() {
//        Resume resume = new Resume(1, "ResumeDescription");
//        resumeService.saveNew(resume);
//
//        Optional<Resume> result = resumeService.getBy(resume.getResumeId);
//        assertFalse(result.isEmpty());
//        assertEquals(resume, result.get());
    }

}
