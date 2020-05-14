package com.aueb.urbanarts;

public class item {
    int profilePhoto, likeCount, commentCount;
    String artistName, eventType, address;
    boolean liveEvent;

    public item() {

    }

    public item(item item) {
        this.profilePhoto = item.profilePhoto;
        this.artistName = item.artistName;
        this.eventType = item.eventType;
        this.address = item.address;
        this.likeCount = item.likeCount;
        this.commentCount = item.commentCount;
        this.liveEvent = item.liveEvent;
    }

    public item(int profilePhoto, String artistName, String eventType, String address) {
        this.profilePhoto = profilePhoto;
        this.artistName = artistName;
        this.eventType = eventType;
        this.address = address;
        this.likeCount = 0;
        this.commentCount = 0;
        this.liveEvent = true;
    }

    public item(int profilePhoto, String artistName, String eventType, String address, int likeCount, int commentCount, boolean liveEvent) {
        this.profilePhoto = profilePhoto;
        this.artistName = artistName;
        this.eventType = eventType;
        this.address = address;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.liveEvent = liveEvent;
    }

    public int getProfilePhoto() {
        return profilePhoto;
    }

    public String getArtistName() {
        return this.artistName;
    }

    public String getEventType() {
        return this.eventType;
    }

    public String getAddress() {
        return this.address;
    }

    public int getLikeCount() {
        return this.likeCount;
    }

    public int getCommentCount() {
        return this.commentCount;
    }

    public boolean getLiveEvent() {
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

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
