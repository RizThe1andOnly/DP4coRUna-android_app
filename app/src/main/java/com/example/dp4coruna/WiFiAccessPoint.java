package com.example.dp4coruna;

import java.util.List;

/**
 * Class holds Wifi-AccessPoint data, specifically the ssid and rssi value measured.
 * As of now these are the only value req. This class purpose is to simplify wifi-ap
 * data since ScanResult returned by wi-fi scans contain a lot more stuff, this class
 * only has bare essentials.
 */
public class WiFiAccessPoint {
   public String ssid;
   public double rssi;
   private static final int NUMBER_OF_TABS = 1;

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

    public static String getListStringRepresent(List<WiFiAccessPoint> list){
       String stringRep = "Wifi AccessPoints: { \n";
       for(WiFiAccessPoint elem : list){
           String elemString = "";
           for(int i=0;i<NUMBER_OF_TABS;i++){
               elemString += "\t";
           }
           elemString += elem.getSsid() + " : " + String.valueOf(elem.getRssi());
           elemString += "\n";
           stringRep += elemString;
       }
       stringRep += "}";
       return stringRep;
    }
}
