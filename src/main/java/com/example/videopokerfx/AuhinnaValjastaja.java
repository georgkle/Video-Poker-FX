package com.example.videopokerfx;




public class AuhinnaValjastaja {

    public static double arvutatasu(double panus, String tulemus) {

        return switch (tulemus) {
            // Lisa vastav väärtus või loogika
            case "Royal Flush" -> 800 * panus;
            case "Straight Flush" -> 50 * panus;
            case "Four of a Kind" -> 30 * panus;
            case "Full House" -> 10 * panus;
            case "Flush" -> 8 * panus;
            case "Straight" -> 5 * panus;
            case "Three of a Kind" -> 3 * panus;
            case "Two Pair" -> 1.5 * panus;
            case "Jacks or Better" -> panus;
            case "One Pair" -> panus * 0.5;
            case "High Card" -> 0;
            default -> 0; // Kui pole vastet, tagasta 0
        };
    }}






