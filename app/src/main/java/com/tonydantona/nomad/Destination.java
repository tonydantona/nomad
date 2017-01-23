package com.tonydantona.nomad;

/**
 * Created by rti1ajd on 1/23/2017.
 */

public class Destination {
    private String consignee;
    private double napLat;
    private double napLon;
    private String HIN;

    public String getHIN() {
        return HIN;
    }

    public void setHIN(String HIN) {
        this.HIN = HIN;
    }

    public double getNapLat() {
        return napLat;
    }

    public void setNapLat(double napLat) {
        this.napLat = napLat;
    }

    public double getNapLon() {
        return napLon;
    }

    public void setNapLon(double napLon) {
        this.napLon = napLon;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }


}
