package com.example.dp4coruna;

/**
 * Stores the necessary cell data: cell signal strength, cell tower id, local/tracking area code
 */
public class CellData {
    public double cellSignalStrength;
    public double cellTowerId;
    public double areaCode;
    public String type;

    public CellData(){}

    public CellData(double cellSignalStrength, double cellTowerId, double areaCode, String type){
        this.cellSignalStrength = cellSignalStrength;
        this.cellTowerId = cellTowerId;
        this.areaCode = areaCode;
        this.type = type;
    }

    public void setFields(double cellSignalStrength, double cellTowerId, double areaCode, String type){
        this.cellSignalStrength = cellSignalStrength;
        this.cellTowerId = cellTowerId;
        this.areaCode = areaCode;
        this.type = type;
    }

    public void copyDataOf(Object srcCellData){
        if((!(srcCellData instanceof CellData))||(srcCellData == null)){
            return;
        }

        CellData cellDataSrc = (CellData)srcCellData;

        this.setFields(cellDataSrc.cellSignalStrength,cellDataSrc.cellTowerId,cellDataSrc.areaCode,cellDataSrc.type);
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
