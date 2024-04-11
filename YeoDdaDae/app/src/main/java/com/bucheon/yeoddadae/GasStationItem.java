package com.bucheon.yeoddadae;

import android.graphics.Bitmap;

public class GasStationItem {
    private String name;
    private String radius;
    private String gasolinePrice; // 휘발유가
    private String dieselPrice; // 경유가
    private String highGasolinePrice;
    private String highDieselPrice;
    private String phone;
    private String brand;
    private String addition;
    private int starRate; // 0:☆☆☆☆☆, 5: ★★★★★
    private double lat;
    private double lon;

    public GasStationItem(String name, String radius, String gasolinePrice, String dieselPrice, String highGasolinePrice, String highDieselPrice, String phone, String brand,String addition, int starRate, String lat, String lon) {
        this.name = name;
        this.radius = radius;
        this.gasolinePrice = gasolinePrice;
        this.dieselPrice = dieselPrice;
        this.highGasolinePrice = highGasolinePrice;
        this.highDieselPrice = highDieselPrice;
        this.phone = phone;
        this.brand = brand;
        this.addition = addition;
        this.starRate = starRate;
        this.lat = Double.parseDouble(lat);
        this.lon = Double.parseDouble(lon);
    }

    public String getName() {
        return name;
    }

    public String getRadius() {
        return radius;
    }

    public String getGasolinePrice() {
        return gasolinePrice;
    }

    public String getDieselPrice() {
        return dieselPrice;
    }
    public String getHighGasolinePrice() {return highGasolinePrice;}

    public String getHighDieselPrice() {return highDieselPrice;}

    public String getPhone() {
        return phone;
    }

    public String getBrand() { return brand; }

    public String getAddition() {
        return addition;
    }

    public int getStarRate() {
        return starRate;
    }

    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }
}
