package services;
import org.bson.types.ObjectId;

import java.util.Date;
import java.util.Objects;

public class NewsArticle{
    private String author;
    private String title;
    private String description;

    private String chatgptdescription;

    private String midJourneyImageURL;

    private ObjectId midjourneyMongoImageID;
    private String url;
    private String source;
    private String image;
    private String category;
    private String language;
    private String country;
    private Date published_at;

    // Konstruktoren, Getter und Setter hier hinzufügen

    @Override
    public String toString() {
        return "NewsArticle{" +
                "author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", chatgptdescription='" + chatgptdescription +"\'" +
                ", url='" + url + '\'' +
                ", source='" + source + '\'' +
                ", image='" + image + '\'' +
                ", category='" + category + '\'' +
                ", language='" + language + '\'' +
                ", country='" + country + '\'' +
                ", published_at=" + published_at +
                '}';
    }

    public String getMidJourneyImageURL() {
        return midJourneyImageURL;
    }

    public void setMidJourneyImageURL(String midJourneyImageURL) {
        this.midJourneyImageURL = midJourneyImageURL;
    }

    public ObjectId getMidjourneyMongoImageID() {
        return midjourneyMongoImageID;
    }

    public void setMidjourneyMongoImageID(ObjectId midjourneyMongoImageID) {
        this.midjourneyMongoImageID = midjourneyMongoImageID;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }
    public void setChatGPTdescription(String chatgptdescription) {
        this.chatgptdescription = chatgptdescription;
    }

    public String getChatGPTdescription() {
        return chatgptdescription;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getPublished_at() {
        return published_at;
    }

    public void setPublished_at(Date published_at) {
        this.published_at = published_at;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NewsArticle newsArticle = (NewsArticle) o;
        return published_at.equals(newsArticle.getPublished_at()) &&
                title.equals(newsArticle.getTitle());
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, published_at);
    }
}
