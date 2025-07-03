package com.example.videopokerfx;

import java.util.Objects;

public class Kaart {
    private String mast;
    private String number;

    public String getMast() {
        return mast;
    }

    public String getNumber() {
        return number;
    }

    public Kaart(String mast, String number) {
        this.mast = mast;
        this.number = number;
    }

    public String toString() {
        return number + " of " + mast;
    }

    public String getPildiTee() {
        return "/images/" + number + "_of_" + mast + ".png";
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Kaart kaart = (Kaart) o;
        return mast.equals(kaart.mast) && number.equals(kaart.number);
    }

    public int hashCode() {
        return Objects.hash(mast, number);
    }
}
