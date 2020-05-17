package com.aueb.urbanarts;

public class item {
    private int profilePhoto, likeCount, commentCount;
    private String artistName, typeOfArt, location;
    private boolean liveEvent;

    public item(item item) {
        this.profilePhoto = item.profilePhoto;
        this.artistName = item.artistName;
        this.typeOfArt = item.typeOfArt;
        this.location = item.location;
        this.likeCount = item.likeCount;
        this.commentCount = item.commentCount;
        this.liveEvent = item.liveEvent;
    }

    public item(int profilePhoto, String typeOfArt, String location, boolean liveEvent) {
        this.profilePhoto = profilePhoto;
        this.typeOfArt = typeOfArt;
        this.location = location;
        this.likeCount = 0;
        this.commentCount = 0;
        this.liveEvent = liveEvent;
    }

    public item(int profilePhoto, String artistName, String typeOfArt, String location, boolean liveEvent) {
        this.profilePhoto = profilePhoto;
        this.artistName = artistName;
        this.typeOfArt = typeOfArt;
        this.location = location;
        this.likeCount = 0;
        this.commentCount = 0;
        this.liveEvent = liveEvent;
    }

    int getProfilePhoto() {
        return profilePhoto;
    }

    String getArtistName() {
        return this.artistName;
    }

    String getTypeOfArt() {
        return this.typeOfArt;
    }

    String getLocation() {
        return this.location;
    }

    int getLikeCount() {
        return this.likeCount;
    }

    int getCommentCount() {
        return this.commentCount;
    }

    boolean getLiveEvent() {
        return this.liveEvent;
    }

    public void setProfilePhoto(int profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public void setLiveEvent(boolean liveEvent) {
        this.liveEvent = liveEvent;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setTypeOfArt(String genre) {
        this.typeOfArt = genre;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
