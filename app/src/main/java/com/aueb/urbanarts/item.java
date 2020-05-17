package com.aueb.urbanarts;

public class item {
    private int likeCount, commentCount;
    private String artistName, typeOfArt, location, eventPhoto="none", profilePhoto="none";
    private boolean liveEvent;

    public item(String profilePhoto, String eventPhoto, String artistName, String typeOfArt, String location, boolean liveEvent, int likeCount, int commentCount) {
        this.profilePhoto = profilePhoto;
        this.eventPhoto = eventPhoto;
        this.artistName = artistName;
        this.typeOfArt = typeOfArt;
        this.location = location;
        this.liveEvent = liveEvent;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
    }

    public String getProfilePhoto() {
        return profilePhoto;
    }

    public void setProfilePhoto(String profilePhoto) {
        this.profilePhoto = profilePhoto;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getTypeOfArt() {
        return typeOfArt;
    }

    public void setTypeOfArt(String typeOfArt) {
        this.typeOfArt = typeOfArt;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getEventPhoto() {
        return eventPhoto;
    }

    public void setEventPhoto(String eventPhoto) {
        this.eventPhoto = eventPhoto;
    }

    public boolean isLiveEvent() {
        return liveEvent;
    }

    public void setLiveEvent(boolean liveEvent) {
        this.liveEvent = liveEvent;
    }
}
