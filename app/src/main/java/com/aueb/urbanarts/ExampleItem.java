package com.aueb.urbanarts;

public class ExampleItem {
    private int imageResource;
    private String text1;
    private String text2;
    private String ID;
    public ExampleItem(){

    }
    public ExampleItem(int imageResource, String text1, String text2,String ID) {
        this.imageResource = imageResource;
        this.text1 = text1;
        this.text2 = text2;
        this.ID=ID;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public void setText1(String text1) {
        this.text1 = text1;
    }

    public void setText2(String text2) {
        this.text2 = text2;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getImageResource() {
        return imageResource;
    }
    public String getText1() {
        return text1;
    }
    public String getText2() {
        return text2;
    }
    public String getID(){return ID;}
}
