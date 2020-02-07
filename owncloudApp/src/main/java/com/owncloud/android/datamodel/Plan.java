package com.owncloud.android.datamodel;

import com.google.gson.annotations.SerializedName;

public class Plan {
    private int id;
    private float cost;
    private int days;
    private String name;
    private Boolean active;

    @SerializedName("cta_text")
    private String ctaText;
    @SerializedName("android_product_id")
    private String androidProductId;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getCost() {
        return cost;
    }

    public void setCost(float cost) {
        this.cost = cost;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAndroidProductId() {
        return androidProductId;
    }

    public void setAndroidProductId(String androidProductId) {
        this.androidProductId = androidProductId;
    }

    public String getCtaText() {
        return ctaText;
    }

    public void setCtaText(String ctaText) {
        this.ctaText = ctaText;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
