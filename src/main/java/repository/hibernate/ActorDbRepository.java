package repository.hibernate;

import model.Actor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.ActorRepository;
import repository.RepositoryException;

import java.util.List;

public class ActorDbRepository implements ActorRepository {
    private static Logger logger = LogManager.getLogger(ActorDbRepository.class);

    public ActorDbRepository() {}

    @Override
    public Actor add(Actor elem) {
        logger.traceEntry("{}", elem);

        if (elem == null) {
            throw new RepositoryException("Actor is null");
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
    public void update(Integer id, Actor elem) {
        logger.traceEntry("{}, {}", id, elem);

        if (elem == null)
            throw new RepositoryException("Actor is null");
        if (id == null)
            throw new RepositoryException("Id is null");
        if (!id.equals(elem.getId())) {
            throw new RepositoryException("Id doesn't match the actor");
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
                var actor = em.find(Actor.class, id);
                if (actor != null) {
                    em.remove(actor);
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
    public Actor findOne(Integer id) {
        logger.traceEntry("{}", id);

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var actor = em.find(Actor.class, id);
            logger.traceExit("{}", actor);
            return actor;
        }
    }

    @Override
    public Iterable<Actor> findAll() {
        logger.traceEntry();

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var actors = em.createQuery("select a from Actor a", Actor.class)
                    .getResultList();
            logger.traceExit("{}", actors.size());
            return actors;
        }
    }
}