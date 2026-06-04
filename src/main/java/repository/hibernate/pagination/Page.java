package repository.hibernate.pagination;

import java.util.List;

public class Page<T> {
    private final List<T> content;
    private final boolean hasNext;
    private final int pageNumber;

    public Page(List<T> content, boolean hasNext, int pageNumber) {
        this.content = content;
        this.hasNext = hasNext;
        this.pageNumber = pageNumber;
    }

    public List<T> getContent() {
        return content;
    }

    public boolean hasNext() {
        return hasNext;
    }

    public int getPageNumber() {
        return pageNumber;
    }
}
