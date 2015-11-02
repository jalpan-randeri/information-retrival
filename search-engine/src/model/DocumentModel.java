package model;

/**
 * Document Model contains,
 *  - name String
 *  - term frequency count long
 */
public class DocumentModel {
    private String name;
    private long termFrequencyCount;

    public DocumentModel(String name, long termFrequencyCount) {
        this.name = name;
        this.termFrequencyCount = termFrequencyCount;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getTermFrequencyCount() {
        return termFrequencyCount;
    }

    public void setTermFrequencyCount(long termFrequencyCount) {
        this.termFrequencyCount = termFrequencyCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentModel that = (DocumentModel) o;

        return !(name != null ? !name.equals(that.name) : that.name != null);

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return '{' + name + ", " + termFrequencyCount + '}';
    }
}

