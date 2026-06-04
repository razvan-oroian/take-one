package repository.hibernate;

import model.Genre;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.GenreRepository;
import repository.RepositoryException;

import java.util.List;

public class GenreDbRepository implements GenreRepository {
    private static Logger logger = LogManager.getLogger(GenreDbRepository.class);

    public GenreDbRepository() {}

    @Override
    public Genre add(Genre elem) {
        logger.traceEntry("{}", elem);

        if (elem == null) {
            throw new RepositoryException("Genre is null");
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
    public void update(Integer id, Genre elem) {
        logger.traceEntry("{}, {}", id, elem);

        if (elem == null)
            throw new RepositoryException("Genre is null");
        if (id == null)
            throw new RepositoryException("Id is null");
        if (!id.equals(elem.getId())) {
            throw new RepositoryException("Id doesn't match the genre");
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
                var genre = em.find(Genre.class, id);
                if (genre != null) {
                    em.remove(genre);
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
    public Genre findOne(Integer id) {
        logger.traceEntry("{}", id);

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var genre = em.find(Genre.class, id);
            logger.traceExit("{}", genre);
            return genre;
        }
    }

    @Override
    public Iterable<Genre> findAll() {
        logger.traceEntry();

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var genres = em.createQuery("select g from Genre g", Genre.class)
                    .getResultList();
            logger.traceExit("{}", genres.size());
            return genres;
        }
    }

    @Override
    public List<Genre> findDistinct() {
        logger.traceEntry();

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var genres =  em.createQuery("select distinct g from Genre g", Genre.class)
                            .getResultList();
            logger.traceExit("{}", genres.size());
            return genres;
        }
    }
}
