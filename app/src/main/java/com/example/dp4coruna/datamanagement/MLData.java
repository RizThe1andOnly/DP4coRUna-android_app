package com.example.dp4coruna.datamanagement;

import java.util.List;
import java.util.Map;

public class MLData {
    public float[][] features;
    public int[] encodedLabels;
    public Map<String,Integer> labelMappings;
    public List<String> labels;

    public MLData(float[][] features, int[] encodedLabels, Map<String,Integer> labelMap, List<String> labels){
        this.features = features;
        this.encodedLabels = encodedLabels;
        this.labelMappings = labelMap;
        this.labels = labels;
    }

    /**
     * If this constructor is used then setter methods have to be called to set the fields of this object.
     */
    public MLData(){
        this.features = null;
        this.labels = null;
        this.labelMappings = null;
        this.encodedLabels = null;
    }

    //setters:
    public boolean setEncodedLabels(int[] encodedLabels) {
        if(encodedLabels == null) return false;

        this.encodedLabels = encodedLabels;

        return true;
    }

    public boolean setFeatures(float[][] features){
        if(features == null) return false;
        this.features = features;
        return true;
    }

    public boolean setLabelMappings(Map<String,Integer> map){
        if(map == null) return false;
        this.labelMappings = map;
        return true;
    }

    public boolean setLables(List<String> labels){
        if(labels == null) return false;
        this.labels = labels;
        return false;
    }
}
