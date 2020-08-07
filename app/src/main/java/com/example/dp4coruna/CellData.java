package com.example.dp4coruna;

/**
 * Stores the necessary cell data: cell signal strength, cell tower id, local/tracking area code
 */
public class CellData {
    public double cellSignalStrength;
    public double cellTowerId;
    public double areaCode;
    public String type;

    public CellData(double cellSignalStrength, double cellTowerId, double areaCode, String type){
        this.cellSignalStrength = cellSignalStrength;
        this.cellTowerId = cellTowerId;
        this.areaCode = areaCode;
        this.type = type;
    }

    @Override
    public String toString() {
        return "CellData{ \n" +
                "   Cell Id: " + this.cellTowerId + "\n" +
                "   Area Code: " + this.areaCode + "\n" +
                "   Cell Signal Strength: " + this.cellSignalStrength + "\n"+
                "   Type: " + this.type + "\n" +
                "}";
    }
}
