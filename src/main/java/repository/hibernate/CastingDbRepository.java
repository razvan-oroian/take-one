package repository.hibernate;

import model.Casting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import repository.CastingRepository;

import java.util.List;

public class CastingDbRepository implements CastingRepository {
    private static Logger logger = LogManager.getLogger(CastingDbRepository.class);

    @Override
    public List<Casting> findCastByMovieId(Integer movieId) {
        logger.trace("{}", movieId);

        try (var em = HibernateUtils.getEntityManagerFactory().createEntityManager()) {
            String query_str = "SELECT c FROM Casting c JOIN FETCH c.actor WHERE c.movie.id = :movieId";

            var query = em.createQuery(query_str, Casting.class);
            query.setParameter("movieId", movieId);

            var results = query.getResultList();
            logger.traceExit("{} elems", results.size());
            return results;
        }
    }

    @Override
    public Casting add(Casting elem) {
        return null;
    }

    @Override
    public void update(Integer integer, Casting elem) {

    }

    @Override
    public void delete(Integer integer) {

    }

    @Override
    public Casting findOne(Integer integer) {
        return null;
    }

    @Override
    public Iterable<Casting> findAll() {
        return null;
    }
}
