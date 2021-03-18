package com.burcak.mybank.entities;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Rates {

    @JsonProperty("TRY")
    private double tr;

    @JsonProperty("USD")
    private double usd;

    @JsonProperty("EUR")
    private double eur;

    public double getTr() {
        return tr;
    }

    public void setTr(double tr) {
        this.tr = tr;
    }

    public double getUsd() {
        return usd;
    }

    public void setUsd(double usd) {
        this.usd = usd;
    }

    public double getEur() {
        return eur;
    }

    public void setEur(double eur) {
        this.eur = eur;
    }
}
