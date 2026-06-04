package repository.hibernate;

import model.WatchlistEntry;
import repository.RepositoryException;
import repository.WatchlistEntryRepository;

import java.util.List;

public class WatchlistEntryDbRepository implements WatchlistEntryRepository {
    @Override
    public List<WatchlistEntry> findByUserId(Integer userId) {
        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            return em.createQuery(
                            "select w from WatchlistEntry w join fetch w.movie where w.user.id = :userId order by w.addedAt desc",
                            WatchlistEntry.class)
                    .setParameter("userId", userId)
                    .getResultList();
        }
    }

    @Override
    public boolean existsByUserIdAndMovieId(Integer userId, Integer movieId) {
        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            Long count = em.createQuery(
                            "select count(w) from WatchlistEntry w where w.user.id = :userId and w.movie.id = :movieId",
                            Long.class)
                    .setParameter("userId", userId)
                    .setParameter("movieId", movieId)
                    .getSingleResult();
            return count > 0;
        }
    }
    @Override
    public void deleteByUserIdAndMovieId(Integer userId, Integer movieId) {
        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                em.createQuery("delete from WatchlistEntry w where w.user.id = :userId and w.movie.id = :movieId")
                        .setParameter("userId", userId)
                        .setParameter("movieId", movieId)
                        .executeUpdate();
                tx.commit();
            } catch (Exception ex) {
                if (tx != null && tx.isActive()) tx.rollback();
                throw new RepositoryException(ex);
            }
        }
    }

    @Override
    public WatchlistEntry add(WatchlistEntry elem) {
        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            var tx = em.getTransaction();
            try {
                tx.begin();
                em.persist(elem);
                tx.commit();
                return elem;
            } catch (Exception ex) {
                if (tx != null && tx.isActive()) tx.rollback();
                throw new RepositoryException(ex);
            }
        }
    }

    @Override
    public void update(Integer integer, WatchlistEntry elem) {

    }

    @Override
    public void delete(Integer integer) {

    }

    @Override
    public WatchlistEntry findOne(Integer integer) {
        return null;
    }

    @Override
    public Iterable<WatchlistEntry> findAll() {
        return null;
    }
}
