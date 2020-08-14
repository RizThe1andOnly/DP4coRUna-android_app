package com.example.dp4coruna.ml;

import android.app.Activity;
import android.util.Log;

import com.example.dp4coruna.R;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
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
 * (!!!) Changes will be made to class when ready to integrate to:
 *  - Database code
 *  - Whole app
 * For now will be working with this as a standalone class which is not dependent on any other class in this app
 *
 * Currently this section uses random data that is stored in app->res->raw->dp4sampledata.csv
 *
 *
 *      --How the data needs to be formatted to be used in the model (as i know so far)--
 *  DeepLearning4J (DL4J) provides its own classes for machine learning and for holding data, so we need to setup data
 *  in a way to utilize them.
 *
 *  - The model in DL4J takes in a DataSet object for the fit method.
 *      - The DataSet object takes in NDArray objects as parameters, one for labels(will mention below) and one for
 *        features.
 *          - NDArray is the DL4J analogue of a numpy array and takes in arrays as parameters for constructor and builds
 *            an NDArray object.
 *
 *  - Note this for the labels: They essentially need to be in one-hot-encoded format. This will be achieved through
 *                              creating an array for each label and having all zeros except for one element which will
 *                              be '1' corresponding to the label. It will be a matrix with rows and columns equal to
 *                              number of labels/samples. Each row will represent a label and the column with column
 *                              index matching the row index will be value '1' otherwise '0'.
 *
 *
 *   (!!!Guide) -- To use this class --
 *   1- Create instance in another class or activity.
 *   2- Call the obtain data method, passing in activity as parameter.
 *   3- Call createMlModel
 *   4- Use the "mln" field then to carry out tasks with trained model.
 */
public class MLModel {

    //constants:
    private final int NUMBER_OF_FEATURES = 7;

    //call vars:
    public MultiLayerNetwork mln;
    private DataSet trainingData;


    /**
     * (!!!) Constructor will be updated later.
     */
    public MLModel(){

    }

    /**
     * Will read provided data (from csv for now) and format it to be used by machine learning model
     *
     * This is the random data that was generated before, this is only for testing purposes. We will eventually
     * obtain data from the database and then process them.
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

        // one-hot-encoding the labels (usually we would have to create a mapping between these and labels but since
        // this is all random we won't do it until later)
        float[][] labelArray = new float[dataMatrix.length][dataMatrix.length];
        for(int i=0;i< dataMatrix.length;i++){
            labelArray[i][i] = 1;
        }

        this.trainingData = new DataSet(new NDArray(featuresMatrix),new NDArray(labelArray));
        //Log.i("FromMLModel",this.trainingData.toString());
    }


    /**
     * Convert matrix of string data into a matrix of float data.
     *
     * Currently used by the obtain data testing method. (!!!)
     *
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
     * Will create and adjust a multinomial logistic regression (softmax) model.
     *
     * (!!!) Core component of our current machine learning model. It uses the the DeepLearning4J's MultiLayerConfiguration
     * and MultiLayerNetwork objects for the machine learning.
     *
     * The three variables under "//vars to be used in model adjustments" may need to be adjusted based on algo. Aside
     * from that at the moment (unless there are changes in number of features) there are no need for changes here.
     *
     * Note the for loop that call model.fit() on this model, this is the training for the model with data on location
     * features available in the device database. The for loop has the condition of the epochs, so that needs to be
     * adjusted based on how many epoch we want for this data.
     *
     *  --Look at top of class for description on how data needs to be setup--
     *
     *  --- To Use this method --- (!!!Guide)
     *   1- Create a MLModel instance.
     *   2- Use the MLModel instance created to call obtain data to get data to train this model
     *   3- Use the MLModel instance to then call this method.
     *          --Doing this will set up and train the model
     *   4- Use the "mln" field of the MLModel instance/object to then use the model functionalities like predictions.
     *
     */
    public void createMlModel(){
        //vars to be used in model adjustments:
        long seed = (new Date().getTime());
        double learningRate = 0.1;
        int numberOfEpochs = 3;

        //create model configurations then initialize model with configuration:
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(learningRate,0.9))
                .list()
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(NUMBER_OF_FEATURES-1)
                        .activation(Activation.SOFTMAX)
                        .nOut(9).build())
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
