package com.example.myapplicationbb.model;

public class HeightData {
    private String age;
    private float p3;
    private float p10;
    private float p25;
    private float p50;
    private float p75;
    private float p90;
    private float p97;

    public HeightData(String age, float p3, float p10, float p25, float p50,
                      float p75, float p90, float p97) {
        this.age = age;
        this.p3 = p3;
        this.p10 = p10;
        this.p25 = p25;
        this.p50 = p50;
        this.p75 = p75;
        this.p90 = p90;
        this.p97 = p97;
    }

    // Getters
    public String getAgeLabel() { return age; }
    public float getP3() { return p3; }
    public float getP10() { return p10; }
    public float getP25() { return p25; }
    public float getP50() { return p50; }
    public float getP75() { return p75; }
    public float getP90() { return p90; }
    public float getP97() { return p97; }
}

