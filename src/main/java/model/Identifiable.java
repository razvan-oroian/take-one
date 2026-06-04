package model;

public interface Identifiable<TId> {
    TId  getId();
    void setId(TId id);
}
