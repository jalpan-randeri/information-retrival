package model;

import java.util.Map;

/**
 * Inverted Index contains
 *  - word / term
 *  - map of Document id -> term frequency
 */
public class InvertedIndex {
    private String term;
    private Map<String, Long> documentFrequency;

    public InvertedIndex(String term, Map<String, Long> documentFrequency) {
        this.term = term;
        this.documentFrequency = documentFrequency;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Map<String, Long> getDocumentFrequency() {
        return documentFrequency;
    }

    public void setDocumentFrequency(Map<String, Long> documentFrequency) {
        this.documentFrequency = documentFrequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvertedIndex that = (InvertedIndex) o;

        if (term != null ? !term.equals(that.term) : that.term != null) return false;
        return !(documentFrequency != null ? !documentFrequency.equals(that.documentFrequency) : that.documentFrequency != null);

    }

    @Override
    public int hashCode() {
        int result = term != null ? term.hashCode() : 0;
        result = 31 * result + (documentFrequency != null ? documentFrequency.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "InvertedIndex{" +
                "term='" + term + '\'' +
                ", documentFrequency=" + documentFrequency +
                '}';
    }
}
