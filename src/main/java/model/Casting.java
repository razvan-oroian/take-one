package model;

import jakarta.persistence.*;

@Entity
@Table(name = "movie_actors")
public class Casting implements Identifiable<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id")
    private Actor actor;

    @Column(name = "role", nullable = false,  length = 255)
    private String role;

    public Casting() {}

    public Casting(String role, Movie movie, Actor actor) {
        this.role = role;
        this.movie = movie;
        this.actor = actor;
    }

    public Movie getMovie() {
        return movie;
    }

    public Actor getActor() {
        return actor;
    }

    public String getRole() {
        return role;
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public void setId(Integer integer) {
        id = integer;
    }

}
