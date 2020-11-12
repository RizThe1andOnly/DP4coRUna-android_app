package com.example.dp4coruna.localLearning.location.dataHolders;

import java.security.InvalidParameterException;

public class CosSimLabel implements Comparable {

    public double cosSimVal;
    public AreaLabel arealabel;

    public CosSimLabel(AreaLabel arealabel, double cosSimVal){
        this.cosSimVal = cosSimVal;
        this.arealabel = arealabel;
    }

    public void setCosSimVal(double cosSimVal) {
        this.cosSimVal = cosSimVal;
    }

    public AreaLabel getArealabel() {
        return arealabel;
    }

    public double getCosSimVal() {
        return cosSimVal;
    }


    @Override
    public int compareTo(Object o) {
        if((!(o instanceof CosSimLabel))||(o == null)) throw new ClassCastException();

        CosSimLabel target = (CosSimLabel) o;

        if(this.cosSimVal < target.cosSimVal) return -1;
        if(this.cosSimVal > target.cosSimVal) return 1;
        return 0;
    }
}
