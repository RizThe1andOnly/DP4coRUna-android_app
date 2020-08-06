package com.example.dp4coruna;

/**
 * Class holds Wifi-AccessPoint data, specifically the ssid and rssi value measured.
 * As of now these are the only value req. This class purpose is to simplify wifi-ap
 * data since ScanResult returned by wi-fi scans contain a lot more stuff, this class
 * only has bare essentials.
 */
public class WiFiAccessPoint {
   public String ssid;
   public double rssi;

   public WiFiAccessPoint(String ssid, double rssi){
       this.ssid = ssid;
       this.rssi = rssi;
   }

    public double getRssi() {
        return this.rssi;
    }

    public String getSsid() {
        return this.ssid;
    }
}
