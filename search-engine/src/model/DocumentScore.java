package model;

/**
 *
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
}
