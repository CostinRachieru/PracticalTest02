package ro.pub.cs.systems.eim.practicaltest02.network;

import android.os.Build;
import android.support.annotation.RequiresApi;

import java.time.LocalDateTime;
import java.util.Date;

public class BitcoinPriceModel {

    private String eurRate;
    private long eurTime;
    private String usdRate;
    private long usdTime;
    Date date = new Date();

    public BitcoinPriceModel() {
        this.usdRate = null;
        this.eurRate = null;
    }

    public long getEurTime() {
        return eurTime;
    }

    public long getUsdTime() {
        return usdTime;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public BitcoinPriceModel(String usdRate, String eurRate) {
        if (usdRate != null) {
            this.usdRate = usdRate;
            this.usdTime = date.getTime();
        }
        if (eurRate != null) {
            this.eurRate = eurRate;
            this.eurTime = date.getTime();
        }
    }

    public String getEurRate() {
        return eurRate;
    }

    public void setEurRate(String eurRate) {
        this.eurRate = eurRate;
    }

    public String getUsdRate() {
        return usdRate;
    }

    public void setUsdRate(String usdRate) {
        this.usdRate = usdRate;
    }
}
