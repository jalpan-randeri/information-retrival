package model;

import java.util.Set;

/**
 * PageModel contains
 *   - Page id,
 *   - inlinks Set of Page that refer to page
 *   - outlinks Set of Pages to which this page refer
 */
public class PageModel {
    private String pageId;
    private double rank;
    private long inlinkCount;

    public PageModel() {
    }

    public PageModel(String pageId, double rank) {
        this.pageId = pageId;
        this.rank = rank;
    }

    public Long getInlinkCount() {
        return inlinkCount;
    }

    public void setInlinkCount(long inlinkCount) {
        this.inlinkCount = inlinkCount;
    }

    public String getPageId() {
        return pageId;
    }

    public void setPageId(String pageId) {
        this.pageId = pageId;
    }

    public Double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PageModel pageModel = (PageModel) o;

        return pageId.equals(pageModel.pageId);

    }

    @Override
    public int hashCode() {
        return pageId.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s  ->  %.20f",pageId, rank);
    }
}
