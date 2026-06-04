package repository.hibernate;

import jakarta.persistence.TypedQuery;
import model.Movie;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.MovieRepository;
import repository.RepositoryException;
import repository.hibernate.pagination.Page;
import repository.hibernate.pagination.Pageable;
import service.dtos.MovieFilter;

import java.util.ArrayList;
import java.util.List;

public class MovieDbRepository implements MovieRepository {
    private static Logger logger = LogManager.getLogger(MovieDbRepository.class);

    public MovieDbRepository() {}

    @Override
    public Movie add(Movie elem) {
        logger.traceEntry("{}", elem);

        if (elem == null) {
            throw new RepositoryException("Movie is null");
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
    public void update(Integer id, Movie elem) {
        logger.traceEntry("{}, {}", id, elem);

        if (elem == null)
            throw new RepositoryException("Movie is null");
        if (id == null)
            throw new RepositoryException("Id is null");
        if (!id.equals(elem.getId())) {
            throw new RepositoryException("Id doesn't match the movie");
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
                var movie = em.find(Movie.class, id);
                if (movie != null) {
                    em.remove(movie);
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
    public Movie findOne(Integer id) {
        logger.traceEntry("{}", id);

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var movie = em.find(Movie.class, id);
            logger.traceExit("{}", movie);
            return movie;
        }
    }

    @Override
    public Iterable<Movie> findAll() {
        logger.traceEntry();

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var movies = em.createQuery("select m from Movie m", Movie.class)
                    .getResultList();
            logger.traceExit("{}", movies.size());
            return movies;
        }
    }

    @Override
    public List<Movie> findByFilter(MovieFilter filter) {
        logger.traceEntry("{}", filter);

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            String query_str = buildFilterQuery(filter);

            var query = em.createQuery(query_str,  Movie.class);
            addFilterQueryParameters(filter, query);

            System.out.println(query_str);
            return query.getResultList();
        }

    }

    private static void addFilterQueryParameters(MovieFilter filter, TypedQuery<Movie> query) {
        if (filter.getTitle() != null &&  !filter.getTitle().trim().isEmpty()) {
            query.setParameter("title", "%" + filter.getTitle() + "%");
        }
        if (filter.getGenre() != null) {
            query.setParameter("genre", filter.getGenre());
        }
        if (filter.getMinRuntime() != null) {
            query.setParameter("minRuntime", filter.getMinRuntime());
        }
        if (filter.getMaxRuntime() != null) {
            query.setParameter("maxRuntime", filter.getMaxRuntime());
        }
        if (filter.getMinYear() != null) {
            query.setParameter("minYear", filter.getMinYear());
        }
        if (filter.getMaxYear() != null) {
            query.setParameter("maxYear", filter.getMaxYear());
        }
    }

    @Override
    public Page<Movie> findPageByFilter(MovieFilter filter, Pageable pageable) {
        logger.traceEntry("{}, {}", filter,  pageable);

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var query_str = buildFilterQuery(filter);

            var query =  em.createQuery(query_str.toString(),  Movie.class);
            addFilterQueryParameters(filter, query);

            query.setFirstResult(pageable.getOffset());
            query.setMaxResults(pageable.getPageSize() + 1);

            var movies = query.getResultList();
            boolean hasNext = false;
            if (movies.size() > pageable.getPageSize()) {
                hasNext = true;
                movies.removeLast();
            }

            logger.traceExit();
            return new Page<>(movies, hasNext, pageable.getPageNumber());
        }
    }

    @Override
    public List<Movie> findMoviesByActorId(Integer actorId) {
        logger.traceEntry("{}", actorId);

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var movies = em.createQuery("select c.movie from Casting c where c.actor.id = :actorId", Movie.class)
                    .setParameter("actorId", actorId)
                    .getResultList();

            logger.traceExit("{}", movies.size());
            return movies;

        } catch (Exception ex) {
            logger.error("{}", ex);
        }

        return List.of();
    }

    @Override
    public List<Movie> findMoviesByDirectorId(Integer directorId) {
        logger.traceEntry("{}", directorId);

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var movies = em.createQuery("select m from Movie m join m.directors d where d.id = :directorId", Movie.class)
                    .setParameter("directorId", directorId)
                    .getResultList();

            logger.traceExit("{}", movies.size());
            return movies;
        } catch (Exception ex) {
            logger.error("{}", ex);
        }

        return List.of();
    }

    private static String buildFilterQuery(MovieFilter filter) {
        StringBuilder query_str = new StringBuilder("select m from Movie m");
        if (filter.getGenre() != null) {
            query_str.append(" join m.genres g ");
        }

        query_str.append(" where 1 = 1");

        if (filter.getTitle() != null && !filter.getTitle().trim().isEmpty()) {
            query_str.append(" and m.title ilike :title");
        }

        if (filter.getGenre() != null) {
            query_str.append(" and g.name = :genre");
        }

        if (filter.getMinRuntime() != null) {
            query_str.append(" and m.runtime >= :minRuntime");
        }
        if (filter.getMaxRuntime() != null) {
            query_str.append(" and m.runtime <= :maxRuntime");
        }

        if (filter.getMinYear() != null) {
            query_str.append(" and year(m.releaseDate) >= :minYear");
        }
        if  (filter.getMaxYear() != null) {
            query_str.append(" and year(m.releaseDate) <= :maxYear");
        }
        return query_str.toString();
    }
}
