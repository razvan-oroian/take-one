package repository;

public interface Repository<ID, T> {
    T add(T elem);
    void update(ID id, T elem);
    void delete(ID id);
    T findOne(ID id);
    Iterable<T> findAll();
}