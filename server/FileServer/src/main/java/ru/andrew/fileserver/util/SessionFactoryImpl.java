package ru.andrew.fileserver.util;

import lombok.Getter;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.springframework.stereotype.Component;

@Component
public class SessionFactoryImpl {
    @Getter
    private final SessionFactory sessionFactory;

    public SessionFactoryImpl() {
        Configuration config = new Configuration();
        config.configure();
        sessionFactory = config.buildSessionFactory();
    }
}
