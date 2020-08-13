package com.example.dp4coruna.ml;

import android.app.Activity;
import android.util.Log;

import com.example.dp4coruna.R;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
//import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.DataSet;

import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Will create machine learning model with required properties and train it by fitting it to given data.
 * The model will be able to generate probability model for the input features based on training.
 *
 * (!!! 8/12/2020) Changes will be made to class when ready to integrate to:
 *  - Database code
 *  - Whole app
 * For now will be working with this as a standalone class which is not dependent on any other class in this app
 */
public class MLModel {

    //constants:
    private final int NUMBER_OF_FEATURES = 7;

    //call vars:
    public MultiLayerNetwork mln;
    private DataSet trainingData;
    private NDArray obtainedDataset;
    private float[][] obtainedDataValues;


    /**
     * (!!!) Constructor will be updated later.
     */
    public MLModel(){

    }

    /**
     * Will read provided data (from csv for now) and format it to be used by machine learning model
     */
    public void obtainDataSet(Activity activity){
        CSVReader cr = new CSVReader(new InputStreamReader(activity.getResources().openRawResource(R.raw.dl4jsampledata)));
        List<List<String>> dataString = new ArrayList<>();

        int k = 0;
        try {
            cr.readNext(); //read first line of titles for data

            String[] rowString;
            while((rowString = cr.readNext())!=null){
                k++;
                List<String> tempStringList = new ArrayList<>();
                for(int i=1;i< rowString.length;i++){ //(!!!) i=1 for start because we don't want the id and want to start at "label" column
                    tempStringList.add(rowString[i]);
                }
                dataString.add(tempStringList);
            }

        } catch (IOException e) {
            Log.i("FromObtainData","Problem reading");
            e.printStackTrace();
        } catch (CsvValidationException e) {
            Log.i("FromObtainData","Problem reading csv");
            e.printStackTrace();
        }

        float[][] dataMatrix = generateFloatMatrix(dataString);

        //separate dataMatrix into labels and features:
        float[][] featuresMatrix = new float[dataMatrix.length][NUMBER_OF_FEATURES-1];
        for(int i=0;i< dataMatrix.length;i++){
            for(int j=0;j<NUMBER_OF_FEATURES-1;j++){
                featuresMatrix[i][j] = dataMatrix[i][j+1];
            }
        }
        float[] labelArray = new float[dataMatrix.length];
        for(int i=0;i< dataMatrix.length;i++){
            labelArray[i] = dataMatrix[i][0];
        }

        this.trainingData = new DataSet(new NDArray(labelArray),new NDArray(featuresMatrix));

    }


    /**
     * Convert matrix of string data into a matrix of float data
     * @param stringData
     * @return float[][]
     */
    private float[][] generateFloatMatrix(List<List<String>> stringData){
        int numberOfSamples = stringData.size();

        float[][] matrix = new float[numberOfSamples][NUMBER_OF_FEATURES];

        for(int i=0;i<numberOfSamples;i++){
            List<String> sampleRow = stringData.get(i);
            for(int j=0;j<NUMBER_OF_FEATURES;j++){
                matrix[i][j] = Float.parseFloat(sampleRow.get(j));
            }
        }

        return matrix;
    }

    /**
     * Will convert provided string into a sample/row that can be added to Dataset to be processed by
     * machine learning model.
     *
     * @param rowString features for a single sample in string format
     */
    private void createRow(String[] rowString){

    }


    /**
     * Will create and adjust a multinomial logistic regression (softmax) model.
     */
    public void createMlModel(){
        //vars to be used in model adjustments:
        long seed = (new Date().getTime());
        double learningRate = 0.1;
        int numberOfEpochs = 3;

        //create model configurations then initialize model with configuration:
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .updater(new Nesterovs(learningRate,0.9))
                .list()
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nOut(NUMBER_OF_FEATURES).build())
                .build();

        this.mln = new MultiLayerNetwork(config);
        this.mln.init();

        /**
         * Fit model to data below.
         *
         * Note(s): (!!!)
         *  - Don't know what proper number for epochs should be so will use 3 for now
         */
        for(int i=0;i<numberOfEpochs;i++){
            this.mln.fit(this.trainingData);
        }
    }



}
