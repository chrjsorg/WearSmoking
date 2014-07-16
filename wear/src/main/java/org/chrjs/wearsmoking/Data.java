package org.chrjs.wearsmoking;

import java.util.Date;

public class Data {

    private float moneyPerPackage;
    private int cigsPerPackage;
    private Date sinceDate;
    private int cigsPerDay;

    private DateDiff diff;

    public Data(int cigsPerDay, int cigsPerPackage, float moneyPerPackage, long sinceDate) {
        this.moneyPerPackage = moneyPerPackage;
        this.sinceDate = new Date(sinceDate);
        this.cigsPerDay = cigsPerDay;
        this.cigsPerPackage = cigsPerPackage;

        diff = GetDateDiff(this.sinceDate.getTime());
    }

    private static DateDiff GetDateDiff(long timeStartMillis) {
        DateDiff dateDiff = new DateDiff();
        Date now = new Date();
        long diff = now.getTime() - timeStartMillis;
        dateDiff.Days = diff / (24 * 60 * 60 * 1000);
        dateDiff.Hours = diff / (60 * 60 * 1000) % 24;
        if (dateDiff.Days == 0) {

            if (dateDiff.Hours >= 12) {
                dateDiff.Days = 1;
            }
        }
        return dateDiff;
    }

    public String getTimeSinceQuit() {
        return diff.Days + "d, " + diff.Hours + "h";
    }

    public int getAvoidedCigs() {
        return (int) ((diff.Days * this.cigsPerDay) + (diff.Hours * (this.cigsPerDay / 24)));
    }

    public int getSavedMoney() {
        float hours = (diff.Days * 24) + diff.Hours;
        return (int) ((this.moneyPerPackage / this.cigsPerPackage * this.cigsPerDay / 24) * hours);
    }

    public static class DateDiff {
        public long Hours;
        public long Days;
    }
}
