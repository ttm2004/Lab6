package com.trantrongmanh.lab6.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Photo model representing a single image in the gallery.
 * Implements Parcelable so it can be passed between Activities via Intent.
 */
public class Photo implements Parcelable {

    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String category;
    private long dateAdded;
    private boolean isFavorite;

    public Photo() {
    }

    public Photo(String id, String title, String description, String imageUrl,
                 String category, long dateAdded, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.category = category;
        this.dateAdded = dateAdded;
        this.isFavorite = isFavorite;
    }

    protected Photo(Parcel in) {
        id = in.readString();
        title = in.readString();
        description = in.readString();
        imageUrl = in.readString();
        category = in.readString();
        dateAdded = in.readLong();
        isFavorite = in.readByte() != 0;
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(description);
        dest.writeString(imageUrl);
        dest.writeString(category);
        dest.writeLong(dateAdded);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
    }
}
