package com.example.dp4coruna.ml;

import android.content.Context;
import com.example.dp4coruna.location.LocationObject;

/**
 * Handles location features/labels requests from network to the database.
 */
public class QueryLocation {
    /*
        Acts as an intermediary between the Network classes and AppDatabase/MachineLearning. Will get the
        requests from network for features or labels and then call the necessary methods in the database/ml
        classes based on the request.

        Currently working off of assumption: Network group will extract LocationObject object from the network
        transmission and will send it here for processing.
     */

    /**
     * Will query device database with the request by the location object, if necessary machine learning model will be
     * run for results. The returned data will be different based on type of request, types of request can be found
     * under LocationObject class.
     * @param locationObject structure containing data to be used for query
     * @return JSON rep of location object if request is SEND_LABEL_REQUEST_FEATURES, string of probabilites if
     *          request is SEND_FEATURES_REQUEST_LABEL.
     */
    public static String queryDevice(LocationObject locationObject, Context context){
        if(locationObject.requestType == LocationObject.SEND_FEATURES_REQUEST_LABEL){
            return queryLabel(locationObject,context);
        }
        else if(locationObject.requestType == LocationObject.SEND_LABEL_REQUEST_FEATURES){
            return queryFeatures(locationObject,context);
        }

        return "Request Code Not Recognized";
    }


    /**
     * Specifically for querying features of a given label.
     * @param locationObject
     * @param context
     * @return JSON representation of locationobject if label exists in the device database or "not found" if not.
     */
    private static String queryFeatures(LocationObject locationObject, Context context){
        return "";
    }


    /**
     * Query labels that the given set of features may belong to. This will run the machine learning model and return
     * a list of probabilities on which label the features may belong to.
     * @param locationObject
     * @param context
     * @return String: list of probabilities
     */
    private static String queryLabel(LocationObject locationObject, Context context){
        //see MLModel class to see how the below functions work and how the rest of the model operates:
        MLModel mlm = new MLModel(context,MLModel.LOAD_MODEL_FROM_DEVICE);
        String outputString = mlm.getOutput(locationObject);

        return outputString;
    }


}
