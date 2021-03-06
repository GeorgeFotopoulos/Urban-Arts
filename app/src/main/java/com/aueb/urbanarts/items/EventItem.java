package com.aueb.urbanarts.items;

public class EventItem {
    private String artistName, typeOfArt, location, eventPhoto, profilePhoto, artistDescription, year, artistType, livetime;
    private int likeCount, commentCount;
    private boolean liveEvent, liked;

    public EventItem(String profilePhoto, String eventPhoto, String artistName, String typeOfArt, String location, boolean liveEvent, int likeCount, int commentCount, boolean liked, String livetime) {
        this.profilePhoto = profilePhoto;
        this.eventPhoto = eventPhoto;
        this.artistName = artistName;
        this.typeOfArt = typeOfArt;
        this.location = location;
        this.liveEvent = liveEvent;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.liked = liked;
        this.livetime = livetime;
    }

    public EventItem(String profilePhoto, String artistName, String artistDescription, String typeOfArt, String year, String artistType) {
        this.profilePhoto = profilePhoto;
        this.artistName = artistName;
        this.artistDescription = artistDescription;
        this.typeOfArt = typeOfArt;
        this.year = year;
        this.artistType = artistType;
    }

    public String getLivetime() {
        return livetime;
    }

    public void setLivetime(String livetime) {
        this.livetime = livetime;
    }

    public String getArtistType() {
        return artistType;
    }

    public void setArtistType(String artistType) {
        this.artistType = artistType;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getArtistDescription() {
        return artistDescription;
    }

    public void setArtistDescription(String artistDescription) {
        this.artistDescription = artistDescription;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
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
