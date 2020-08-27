package com.example.dp4coruna.ml;

import android.content.Context;
import com.example.dp4coruna.dataManagement.AppDatabase;

import java.util.List;
import java.util.Map;

public class MLData {
    public float[][] features;
    public float[][] encodedLabels;
    public Map<String,float[]> labelMappings;
    public List<String> labels;
    public int numberOfLocations; //set through the size of the labels list since that reflects the number of locations; acts as number of classes for softmax

    public MLData(float[][] features, float[][] encodedLabels, Map<String,float[]> labelMap, List<String> labels){
        this.features = features;
        this.encodedLabels = encodedLabels;
        this.labelMappings = labelMap;
        this.labels = labels;
        this.numberOfLocations = labels.size();
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
    public boolean setEncodedLabels(float[][] encodedLabels) {
        if(encodedLabels == null) return false;

        this.encodedLabels = encodedLabels;

        return true;
    }

    public boolean setFeatures(float[][] features){
        if(features == null) return false;
        this.features = features;
        return true;
    }

    public boolean setLabelMappings(Map<String,float[]> map){
        if(map == null) return false;
        this.labelMappings = map;
        return true;
    }

    public boolean setLables(List<String> labels){
        if(labels == null) return false;
        this.labels = labels;
        this.numberOfLocations = labels.size();
        return false;
    }

    public static List<String> getLabelNames(Context context){
        AppDatabase ad = new AppDatabase(context);
        List<String> labelNames = ad.getAllLocationLabels();
        return labelNames;
    }


    /*
                                                File save and retrieval operations.
             - Required to save the MLData object which contains actual label and encoded label mappings to device.
             - The mappings will be retrieved when model is run to have the output layer's data be labeled accordingly,
             since the output only returns an array of probabilities and no actual labels to along with them.
             - Assumption is that the probabilities are in order of the labels provided when training.

             - To save file to device internal storage:
                - Will convert MLData object to a JSON string
                - Write JSON string file to newly created file, or update if file already exists.
                - Conversion to json will be done through google GSON api
     */






}
