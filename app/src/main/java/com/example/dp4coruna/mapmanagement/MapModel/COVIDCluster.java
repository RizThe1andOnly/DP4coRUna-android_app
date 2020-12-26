package com.example.dp4coruna.mapmanagement.MapModel;

import com.google.android.gms.maps.model.LatLng;

/**This Object is designed to hold the information on up-to-date COVID cases, by county
 * Data is updated every hour
 * HTTP request is sent to database and returned and parsed into these objects
 * Info from: https://coronavirus-resources.esri.com/datasets/628578697fb24d8ea4c32fa0c5ae1843_0/data?geometry=161.214%2C-0.075%2C23.226%2C52.222&selectedAttribute=Active&where=(Confirmed%20%3E%200)
 *Attribute Info: https://coronavirus-resources.esri.com/datasets/628578697fb24d8ea4c32fa0c5ae1843_0?geometry=161.214%2C-0.075%2C23.226%2C52.222&orderBy=Last_Update&orderByAsc=false&selectedAttribute=Active&where=(Confirmed%20%3E%200)
 */

public class COVIDCluster {
    private String county;
    private String state;
    private String country;
    private String location; //full location. usually formatted: county, state, country
    private LatLng coordinates;
    private int confirmed;
    private int recovered; //sometimes zero, depending on data
    private int deaths;
    private int active; //number of active cases: (confirmed-recovered-deaths) not all tuples contain this value, but most do
    private String risklevel;

    public void setCounty(String county){
    this.county = county;
}

    public void setState(String state){
        this.state = state;
    }

    public void setCountry(String country){
        this.country = country;
    }

    public void setLocation(String location){
        this.location = location;
    }

    public void setCoordinates(LatLng coordinates){
        this.coordinates = coordinates;
    }

    public void setConfirmed(int confirmed){
        this.confirmed = confirmed;
    }

    public void setRecovered(int recovered){
        this.recovered = recovered;
    }

    public void setDeaths(int deaths){
        this.deaths = deaths;
    }

    public void setActive(int active){
        this.active = active;
    }

    /**Sets risk level based on number of active cases
     * Active cases per county range from 0-325,000
     * Though the upper limit is only comprised of a few outliers
     *For now, I have split it into three tiers which show a good gradient in the
     * Tri-State area. We can alter these if need be.
     *
     * 3 Tiers:
     * High Risk - 50,000+ Active Cases
     * Medium Risk: 10,000 - 50,000 Active Cases
     * Low Risk: <10,000 Active Cases
     * Based on data here: https://coronavirus-resources.esri.com/datasets/628578697fb24d8ea4c32fa0c5ae1843_0/data?geometry=161.214%2C-0.075%2C23.226%2C52.222&selectedAttribute=Active&where=(Confirmed%20%3E%200)
     *Note: This must be set after active cases has been set.
     */
    public void setRiskLevel(){
        if(this.active<10000){
            this.risklevel = "Low";
        }
        else if(this.active>10000 && this.active<50000){
            this.risklevel = "Medium";
        }
        else{
            this.risklevel = "High";
        }


    }


    public String getCounty(){
        return this.county;
    }

    public String getState(){
        return this.state;
    }

    public String getCountry(){
        return this.country;
    }

    public String getLocation(){
        return this.location;
    }

    public LatLng getCoordinates(){
        return this.coordinates;
    }

    public int getConfirmed(){
        return this.confirmed;
    }

    public int getRecovered(){
        return this.recovered;
    }

    public int getDeaths(){
        return this.deaths;
    }

    public int getActive(){
        return this.active;
    }

    public String getRisklevel(){
        return this.risklevel;
    }

}
