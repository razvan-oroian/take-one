package repository.hibernate;

import model.Movie;
import model.Review;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.RepositoryException;
import repository.ReviewRepository;

import java.util.List;

public class ReviewDbRepository implements ReviewRepository {
    private static Logger logger = LogManager.getLogger(ReviewDbRepository.class);

    public ReviewDbRepository() {}

    @Override
    public Review add(Review elem) {
        logger.traceEntry("{}", elem);

        if (elem == null) {
            throw new RepositoryException("Review is null");
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
    public void update(Integer id, Review elem) {
        logger.traceEntry("{}, {}", id, elem);

        if (elem == null)
            throw new RepositoryException("Review is null");
        if (id == null)
            throw new RepositoryException("Id is null");
        if (!id.equals(elem.getId())) {
            throw new RepositoryException("Id doesn't match the review");
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
                var review = em.find(Review.class, id);
                if (review != null) {
                    em.remove(review);
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
    public Review findOne(Integer id) {
        logger.traceEntry("{}", id);

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var review = em.find(Review.class, id);
            logger.traceExit("{}", review);
            return review;
        }
    }

    @Override
    public Iterable<Review> findAll() {
        logger.traceEntry();

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var reviews = em.createQuery("select r from Review r", Review.class)
                    .getResultList();
            logger.traceExit("{}", reviews.size());
            return reviews;
        }
    }

    @Override
    public Review findByUserIdAndMovieId(Integer userId, Integer movieId) {
        logger.traceEntry("{}, {}", userId,  movieId);

        try  (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var review = em.createQuery("select r from Review r where r.user.id=:userId and r.movie.id=:movieId", Review.class)
                    .setParameter("userId", userId)
                    .setParameter("movieId", movieId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);
            logger.traceExit("{}", review);

            return review;
        } catch (Exception ex) {
            logger.error("{}", ex);
        }

        return null;
    }

    public Long countMoviesWatched(Integer userId) {
        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("select count(r) from Review r where r.user.id = :userId", Long.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
        }
    }

    public Long sumRuntimeMinutes(Integer userId) {
        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            Long total = em.createQuery("select sum(r.movie.runtime) from Review r where r.user.id = :userId", Long.class)
                    .setParameter("userId", userId)
                    .getSingleResult();
            return total == null ? 0L : total;
        }
    }

    public List<Movie> findRecentMovies(Integer userId, int limit) {
        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery("select r.movie from Review r where r.user.id = :userId order by r.createdAt desc", model.Movie.class)
                    .setParameter("userId", userId)
                    .setMaxResults(limit)
                    .getResultList();
        }
    }

    public List<Object[]> getGenreStats(Integer userId) {
        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery(
                            "select g.name, count(r) from Review r join r.movie m join m.genres g " +
                                    "where r.user.id = :userId group by g.name order by count(r) desc", Object[].class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    public List<Object[]> getReleaseYearStats(Integer userId) {
        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery(
                            "select year(r.movie.releaseDate), count(r) from Review r " +
                                    "where r.user.id = :userId group by year(r.movie.releaseDate) order by year(r.movie.releaseDate)", Object[].class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }
}