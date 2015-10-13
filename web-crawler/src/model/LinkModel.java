package model;

/**
 * LinkModel Model
 *   - link  String url of link
 *   - level Integer as depth of the document
 */
public class LinkModel {
    private String link;
    private int level;

    public LinkModel(int level, String link) {
        this.level = level;
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public int getLevel() {
        return level;
    }

    @Override
    public String toString() {
        return "LinkModel{" +
                "link='" + link + '\'' +
                ", level=" + level +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LinkModel link1 = (LinkModel) o;

        return link.replaceFirst("http(s)?","").equals(link1.link.replaceFirst("http(s)?",""));

    }

    @Override
    public int hashCode() {
        return link.hashCode();
    }
}
