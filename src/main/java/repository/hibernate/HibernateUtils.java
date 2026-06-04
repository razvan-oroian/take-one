package repository.hibernate;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HibernateUtils {
    private static EntityManagerFactory emf;
    public static EntityManagerFactory getEntityManagerFactory() {
        if (emf == null) {
            try {
                Map<String, String> localOverrides = new HashMap<>();

                try (InputStream is = HibernateUtils.class.getClassLoader().getResourceAsStream("hibernate.properties")) {
                    if (is != null) {
                        Properties props = new Properties();
                        props.load(is);
                        for (String key : props.stringPropertyNames()) {
                            localOverrides.put(key, props.getProperty(key));
                        }
                        System.out.println("Loaded local database credentials from hibernate.properties!");
                    } else {
                        System.out.println("No hibernate.properties found. Using persistence.xml defaults.");
                    }
                } catch (Exception e) {
                    System.err.println("Error reading hibernate.properties: " + e.getMessage());
                }

                emf = Persistence.createEntityManagerFactory("myPersistenceUnit", localOverrides);

            } catch (Exception ex) {
                System.err.println("Initial SessionFactory creation failed." + ex);
                throw new ExceptionInInitializerError(ex);
            }
        }
        return emf;
    }

    public static void closeEntityManagerFactory() {
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }
}
