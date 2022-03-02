package com.example.bookjihc.models;

public class ModelCategory {
    String id, category, uid;
    long timestammp;

    public ModelCategory() {


    }

    public ModelCategory(String id, String category, String uid, long timestammp) {
        this.id = id;
        this.category = category;
        this.timestammp = timestammp;
        this.uid = uid;


    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public long getTimestammp() {
        return timestammp;
    }

    public void setTimestammp(long timestammp) {
        this.timestammp = timestammp;
    }
}
