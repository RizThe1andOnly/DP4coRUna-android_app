package com.example.dp4coruna.localLearning.location.learner;

import android.content.Context;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.location.dataHolders.AreaLabel;
import com.example.dp4coruna.localLearning.location.dataHolders.CosSimLabel;
import com.example.dp4coruna.localLearning.location.dataHolders.WiFiAccessPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Implements the Cosine Similarity Formula outlined in the CollabLoc Paper.
 */
public class CosSimilarity {

    /*
        This class is started through checkCosSim_vs_allLocations(). This will run the algorithm on all available
        wifi access point lists (one list per room in the database).

        The equation found in the CollabLoc paper has its implementation start in the getCosineSimiliarity()
        method. This is where actual implementation of the formula starts. There are some other helper functions
        set up to carryout tasks.

        Currently the formula has been broken down as follows:
         - The broad functions are handled in the getCosineSimilarity() method. This includes:
            - Obtaining the numerator and denominator values and carrying out the division.
         - The numerator is the multiplication of each rssi value of the input list with that of the database element
         list. This also includes the check to see if the two wifi access points share the same MAC address.
            - The mac address check is handled by the compareMacAddress() method.
            - Each pair of rssi value multiplication is handled within the getCosineSimilarity() method, within
            the loop.
         - The Denominator is the product of the root sum squares of both lists that are being processed. This is done
         by calling rootSumSquaredR() method on both lists and simply getting a value for each list.

        Note: for every location stored in the database getCosineSimilarity() method will be called, since the input
        needs to be checked against every available location. This is handled by the checkCosSim_vs_allLocations()
        method.
     */

    private Context context;

    /**
     * Default Constructor to be used if not using checkCosSim_vs_allLocations()
     */
    public CosSimilarity(){}

    /**
     * Constructor for using checkCosSim_vs_allLocations()
     * @param context
     */
    public CosSimilarity(Context context){
        this.context = context;
    }


    /**
     * Gets all available wifi access points in database divided into their respective locations and then tests each
     * list with the given list to check for similarity.
     * @return String results for each room label and the similarity value
     */
    public String checkCosSim_vs_allLocations(List<WiFiAccessPoint> start){
        /*
            - Will achieve function by first obtaining all of the available labels from the database.
            - All of the wifi access points for each room will also be obtained from the database.
            - Then cosSimValues_forEachLocation() will be called
            - A string will be created with formatting roomName : cosSimValue for each room
         */

        AppDatabase ad = new AppDatabase(this.context);
        List<String> roomLabels = ad.getAllLocationLabels();
        List<List<WiFiAccessPoint>> wapLists = ad.getWifiAPListByLocation();

        List<Double> vals = cosSimValues_forEachLocation(start,wapLists);

        //break into room:value string list:
        String toBeReturned = "";
        for(int i=0;i<roomLabels.size();i++){
            toBeReturned += roomLabels.get(i) + ":" + vals.get(i) + ", ";
        }

        return toBeReturned;
    }

    public CosSimLabel checkCosSin_vs_allLocations_v2(List<WiFiAccessPoint> start){
        AppDatabase ad = new AppDatabase(this.context);
        List<AreaLabel> als = ad.getAllAreaLabels();
        List<List<WiFiAccessPoint>> wapLists = ad.getWifiAPListByLocation();

        List<Double> vals = cosSimValues_forEachLocation(start,wapLists);

        //prep output
        List<CosSimLabel> labeledOutputVals = new ArrayList<>();
        for(int i=0;i< vals.size();i++){
            labeledOutputVals.add(new CosSimLabel(als.get(i),vals.get(i)));
        }

        return Collections.max(labeledOutputVals);
    }

    /**
     * Helper method called by checkCosSim_vs_allLocations(). This will call getCosineSimilarity() for each wifi access
     * point list in endList (so once for every location available currently in the database, since every location has
     * its own wifi access point list).
     * @param start wifi access point list for the room user is currently in
     * @param endList all of the wifi access point lists available in the device database.
     * @return list of cosine similarity values ordered same as the room labels
     */
    private List<Double> cosSimValues_forEachLocation(List<WiFiAccessPoint> start, List<List<WiFiAccessPoint>> endList){
        List<Double> simVals = new ArrayList<>();

        for(List<WiFiAccessPoint> endPoint : endList){
            simVals.add(getCosineSimilarity(start,endPoint));
        }

        return simVals;
    }


    /**
     * Calculate cosine similarity between two lists of wifi access points as per the equation given in the
     * CollabLoc paper.
     * @param start List at point A or starting point
     * @param end List at point B or end (current location) point
     * @return double the cosine similarity
     */
    public double getCosineSimilarity(List<WiFiAccessPoint> start, List<WiFiAccessPoint> end){
        //for normalization (if req) see the helper method cosineSimilarity_RssiProcessing:
        List<Double> start_processedRssiList = cosineSimilarity_RssiProcessing(start);
        List<Double> end_processedRssiList = cosineSimilarity_RssiProcessing(end);

        // rss = root sum squared; denominator section of the cosine similarity equation
        double rss_start = cosineSimilarity_RootSumSquaredR(start_processedRssiList);
        double rss_end = cosineSimilarity_RootSumSquaredR(end_processedRssiList);

        //numerator section of the cosine similarity equation
        double outerSum = 0;
        double innerSum = 0;
        for(int i=0;i<start.size();i++){
            WiFiAccessPoint a_AP = start.get(i);
            double a_Rssi = start_processedRssiList.get(i);
            for(int j=0;j<end.size();j++){
                WiFiAccessPoint b_AP = end.get(j);
                double b_Rssi = end_processedRssiList.get(j);
                int delta = cosineSimilarity_CompareMacAddress(a_AP,b_AP);
                double abdelta = 0;
                if(delta == 1) abdelta = a_Rssi * b_Rssi;
                innerSum += abdelta;
            }
            outerSum += innerSum;
            innerSum = 0;
        }

        double similarity = outerSum / (rss_start * rss_end);

        return similarity;
    }

    /**
     * Helper method to be used by cosineSimilarity() to get length of wifi access point list
     * length. Length being the square root of the sum of each Rssi value squared of the given list.
     *
     * Has the 'W' at the end to indicate it takes WifiAccessPoint List
     *
     * @param arr
     * @return
     */
    private double cosineSimilarity_RootSumSquaredW(List<WiFiAccessPoint> arr){
        double squaredSum = 0;
        for(WiFiAccessPoint w : arr){
            squaredSum = squaredSum + Math.pow(w.getRssi(),2);
        }
        return Math.sqrt(squaredSum);
    }

    /**
     * Helper method to be used by cosineSimilarity() to get length of wifi access point list
     * length. Length being the square root of the sum of each Rssi value squared of the given list.
     *
     * Has the 'R' at the end to indicate it takes list of doubles or rssi values as the argument.
     *
     * @param arr
     * @return
     */
    private double cosineSimilarity_RootSumSquaredR(List<Double> arr){
        double squaredSum = 0;
        for(double r : arr){
            squaredSum = squaredSum + Math.pow(r,2);
        }
        return Math.sqrt(squaredSum);
    }

    /**
     * Helper method used by cosineSimilarity() to determine if two wifi access points have the same MAC address which
     * would mean they are the same wifi access point.
     * @param a Access point a
     * @param b Access point b
     * @return 1 if Mac address matches, 0 otherwise.
     */
    private int cosineSimilarity_CompareMacAddress(WiFiAccessPoint a, WiFiAccessPoint b){
        if((a.getBssid()).equals(b.getBssid())) return 1;
        return 0;
    }

    /**
     * Helper method called by cosineSimilarity to process Rssi values of a list. Currently maybe
     * normalize rssi values.
     * @param arr
     */
    private List<Double> cosineSimilarity_RssiProcessing(List<WiFiAccessPoint> arr){
        /*
            Normalization would happen by extracting all of the rssi values into
            separate list then getting normalized values.
         */
        List<Double> rssiVals = new ArrayList<>();
        for(WiFiAccessPoint w : arr){
            rssiVals.add(w.getRssi());
        }

        //further process of the rssiVals list here if necessary:

        return rssiVals;
    }
}
