package model;

/**
 *
 */
public class Term {
    private String word;
    private double posToNegRatio;
    private double negToPosRatio;

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public Double getPosToNegRatio() {
        return posToNegRatio;
    }

    public void setPosToNegRatio(double posToNegRatio) {
        this.posToNegRatio = posToNegRatio;
    }

    public Double getNegToPosRatio() {
        return negToPosRatio;
    }

    public void setNegToPosRatio(double negToPosRatio) {
        this.negToPosRatio = negToPosRatio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Term term = (Term) o;

        return word.equals(term.word);

    }

    @Override
    public int hashCode() {
        return word.hashCode();
    }
}
