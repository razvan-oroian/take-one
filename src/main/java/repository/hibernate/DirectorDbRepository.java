package repository.hibernate;

import model.Director;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.DirectorRepository;
import repository.RepositoryException;

import java.util.List;

public class DirectorDbRepository implements DirectorRepository {
    private static Logger logger = LogManager.getLogger(DirectorDbRepository.class);

    public DirectorDbRepository() {}

    @Override
    public Director add(Director elem) {
        logger.traceEntry("{}", elem);

        if (elem == null) {
            throw new RepositoryException("Director is null");
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
    public void update(Integer id, Director elem) {
        logger.traceEntry("{}, {}", id, elem);

        if (elem == null)
            throw new RepositoryException("Director is null");
        if (id == null)
            throw new RepositoryException("Id is null");
        if (!id.equals(elem.getId())) {
            throw new RepositoryException("Id doesn't match the director");
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
                var director = em.find(Director.class, id);
                if (director != null) {
                    em.remove(director);
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
    public Director findOne(Integer id) {
        logger.traceEntry("{}", id);

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var director = em.find(Director.class, id);
            logger.traceExit("{}", director);
            return director;
        }
    }

    @Override
    public Iterable<Director> findAll() {
        logger.traceEntry();

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var directors = em.createQuery("select d from Director d", Director.class)
                    .getResultList();
            logger.traceExit("{}", directors.size());
            return directors;
        }
    }

    @Override
    public Director findDirectorsByMovieId(Integer movieId) {
        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            String query_str = "SELECT d FROM Movie m JOIN m.directors d WHERE m.id = :movieId";

            var query = em.createQuery(query_str, Director.class);
            query.setParameter("movieId", movieId);

            return query.getSingleResult();
        }
    }
}
