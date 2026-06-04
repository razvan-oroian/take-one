package service.dtos;

public class MovieFilter {
    private String title;
    private Integer minRuntime;
    private Integer maxRuntime;
    private Integer minYear;
    private Integer maxYear;
    private String genre;

    public MovieFilter(String title, Integer minRuntime, Integer maxRuntime, Integer minYear, Integer maxYear, String genre) {
        this.title = title;
        this.minRuntime = minRuntime;
        this.maxRuntime = maxRuntime;
        this.minYear = minYear;
        this.maxYear = maxYear;
        this.genre = genre;
    }

    public MovieFilter() {}

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getMinRuntime() {
        return minRuntime;
    }

    public void setMinRuntime(Integer minRuntime) {
        this.minRuntime = minRuntime;
    }

    public Integer getMaxRuntime() {
        return maxRuntime;
    }

    public void setMaxRuntime(Integer maxRuntime) {
        this.maxRuntime = maxRuntime;
    }

    public Integer getMinYear() {
        return minYear;
    }

    public void setMinYear(Integer minYear) {
        this.minYear = minYear;
    }

    public Integer getMaxYear() {
        return maxYear;
    }

    public void setMaxYear(Integer maxYear) {
        this.maxYear = maxYear;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
