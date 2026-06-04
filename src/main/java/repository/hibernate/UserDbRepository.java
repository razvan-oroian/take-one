package repository.hibernate;

import model.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.RepositoryException;
import repository.UserRepository;

public class UserDbRepository implements UserRepository {
    private static Logger logger = LogManager.getLogger(UserDbRepository.class);

    public UserDbRepository() {}


    @Override
    public User add(User elem) {
        logger.traceEntry("{}", elem);

        if (elem == null) {
            throw new RepositoryException("User is null");
        }

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var tx = em.getTransaction();

            try {
                tx.begin();
                em.persist(elem);
                tx.commit();
            } catch (Exception ex) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw new RepositoryException(ex);
            }
        }

        logger.traceExit("{}", elem);
        return elem;
    }

    @Override
    public void update(Integer id, User elem) {
        logger.traceEntry("{}, {}", id, elem);

        if (elem == null)
            throw new RepositoryException("User is null");
        if (id == null)
            throw new RepositoryException("Id is null");
        if (!id.equals(elem.getId())) {
            throw new RepositoryException("Id doesn't match the user");
        }

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                em.merge(elem);
                tx.commit();
            } catch (Exception ex) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw new RepositoryException(ex);
            }
        }

        logger.traceExit();
    }

    @Override
    public void delete(Integer id) {
        logger.traceEntry("{}", id);

        if (id == null)
            throw new RepositoryException("Id is null");

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var tx = em.getTransaction();

            try {
                tx.begin();
                var user =  em.find(User.class, id);
                if (user != null) {
                    em.remove(user);
                }
                tx.commit();
            } catch (Exception ex) {
                if (tx != null && tx.isActive()) {
                    tx.rollback();
                }
                throw new RepositoryException(ex);
            }
        }

        logger.traceExit();
    }

    @Override
    public User findOne(Integer id) {
        logger.traceEntry("{}", id);

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var user =  em.find(User.class, id);
            logger.traceExit("{}", user);
            return user;
        }
    }

    @Override
    public Iterable<User> findAll() {
        logger.traceEntry();

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var users =  em.createQuery("select u from User u", User.class)
                    .getResultList();
            logger.traceExit("{}", users.size());
            return users;
        }
    }

    @Override
    public User findByEmail(String email) {
        logger.traceEntry("{}", email);

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var user = em.createQuery("select u from User u where u.email = :email", User.class)
                    .setParameter("email", email)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            logger.traceExit("{}", user);
            return user;
        }
    }
}
