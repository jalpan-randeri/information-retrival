package model;

/**
 * Document score contains
 * - document id
 * - score of document
 */
public class DocumentScore {
    private String documentId;
    private double score;

    public DocumentScore(String documentId, double score) {
        this.documentId = documentId;
        this.score = score;
    }


    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DocumentScore score = (DocumentScore) o;

        return !(documentId != null ? !documentId.equals(score.documentId) : score.documentId != null);

    }

    @Override
    public int hashCode() {
        return documentId != null ? documentId.hashCode() : 0;
    }
}
