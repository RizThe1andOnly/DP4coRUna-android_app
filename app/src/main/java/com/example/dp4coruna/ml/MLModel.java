package com.example.dp4coruna.ml;

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.util.Log;

import android.widget.Toast;
import com.example.dp4coruna.R;
import com.example.dp4coruna.dataManagement.AppDatabase;
import com.example.dp4coruna.localLearning.location.LocationObject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;

import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import org.nd4j.linalg.cpu.nativecpu.NDArray;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Will create machine learning model with required properties and train it by fitting it to given data.
 * The model will be able to generate probability model for the input features based on training.
 */
public class MLModel {
    /*                          -------------Class Notes-----------------------
        (!!!) Changes will be made to class when ready to integrate to:
    - Database code : Somewhat implemented (8/17/2020)
    - Whole app
   For now will be working with this as a standalone class which is not dependent on any other class in this app

   Currently this section uses random data that is stored in app->res->raw->dp4sampledata.csv


        --How the data needs to be formatted to be used in the model (as i know so far)--
    DeepLearning4J (DL4J) provides its own classes for machine learning and for holding data, so we need to setup data
    in a way to utilize them.

    - The model in DL4J takes in a DataSet object for the fit method.
        - The DataSet object takes in NDArray objects as parameters, one for labels(will mention below) and one for
          features.
            - NDArray is the DL4J analogue of a numpy array and takes in arrays as parameters for constructor and builds
              an NDArray object.

    - Note this for the labels: They need to be in one-hot-encoded format. This will be achieved through
                                creating an array for each label and having all zeros except for one element which will
                                be '1' corresponding to the label. It will be a matrix with rows and columns equal to
                                number of labels/samples. Each row will represent a label and the column with column
                                index matching the row index will be value '1' otherwise '0'.


   (!!!Guide)
                                -- TO USE THIS CLASS --
       This class will have two broad functionalities: to train model and to use model.

       Train model:
         This class will be used to train a softmax model based on the currently available data in the device database.
         IMPORTANT: should only be called when new location data is added to the device otherwise unnecessary.

         Steps for training:
            - Create instance of this class.
            - Call the method trainAndSaveModel using the instance.
                -- This will take care of training the model and then saving the model to device storage, this way
                won't have to train model everytime there is a need to use it, only when new data is added.

         Steps for using this model:
            - Create instance of this class
            - Call the method loadModel or create the instance using the constructor MLModel(context,reqLoadModel)
               -- This step will load model from device storage into the model instance (this.mln) that belongs to this
                class.
            - From the instance do: objectName.mln.output(NDArray inputData, false) to get list of probabilities that
            make up the softmax output. The output will be type INDArray.
     */



    //                  ------------------ Class Code ---------------------

    private Context context;

    private final String model_filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MlModel.zip";
    private final String dataset_filePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MlDataset.zip";

    //static constants to be used by other classes to access functionalities:
    public static final int LOAD_MODEL_FROM_DEVICE = 0;
    public static final int TRAIN_MODEL_AND_SAVE_IN_DEVICE = 1;

    //ml training constants:
    private final int NUMBER_OF_FEATURES = 6;

    // vars:
    public MultiLayerNetwork mln;
    public DataSet trainingData;
    private int numberOfLocations; //will act as the number of classes.
    private List<String> labels;


    /**
     * (!!!) Constructor will be updated later.
     */
    public MLModel(Context context){
        this.context = context;
    }

    /**
     * Constructor for using the model right away. This will automatically load the model stored in device to the class
     * ready for use. Note: there has to be a model saved in the device for this constructor to function properly.
     *
     *
     * @param context application context obtained through getApplicationContext in an Activity that extends AppCompatActivity
     * @param request int value indicating the action to be taken by the machine learning model.
     */
    public MLModel(Context context, int request){
        this.context = context;
        if(request == LOAD_MODEL_FROM_DEVICE){
            loadModel();
        }
        else if(request == TRAIN_MODEL_AND_SAVE_IN_DEVICE){
            trainAndSaveModel();
        }

    }


    /**
     * Will load the model from storage and run it with given input to return a set of probabilities.
     */
    public void loadModel(){
        try {
            this.mln = MultiLayerNetwork.load(new File(this.model_filePath),true);
            this.trainingData = new DataSet();
            (this.trainingData).load(new File(this.dataset_filePath));
            (this.trainingData).setLabelNames(MLData.getLabelNames(this.context));
            (this.labels) = MLData.getLabelNames(this.context);
        } catch (IOException e) {
            Log.i("FromMlModel","IOException when trying load model; perhaps not found?");
            e.printStackTrace();

            //error message:
            Toast.makeText(this.context , "File does not exist; please make model/dataset", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Calls createMlModel() method to create and fit softmax network, then saves it to device so training doesn't have
     * to take place everytime.
     */
    public void trainAndSaveModel(){
        this.getDataAndCompileDataset();
        this.createMlModel();

        //save model and dataset to device:
        try {
            //delete old version of the file:
            File model_fileToBeDeleted = new File(this.model_filePath);
            File dataset_fileToBeDeleted = new File(this.dataset_filePath);

            if(model_fileToBeDeleted.exists()){
                model_fileToBeDeleted.delete();
            }
            if(dataset_fileToBeDeleted.exists()){
                dataset_fileToBeDeleted.delete();
            }

            //save the new version:
            this.mln.save(new File(this.model_filePath));
            this.trainingData.save(new File(this.dataset_filePath));

        } catch (IOException e) {
            Log.i("FromMlModel","IOException when trying to save model");
            e.printStackTrace();
        }
    }

    /**
     * Will run model.output on given data and return the output. A method that abstracts
     * running of machine learning model then formatting of output data and just returning the data.
     *
     * @param locationObject given data; input features that will be put into model to be run
     * @return String: list of the probabilities of each class being the label which belongs to given features.
     */
    public String getOutput(LocationObject locationObject){
        String toBeReturned = "[";

        INDArray outputVals = getOutputVals(locationObject);
        for(int i=0;i< outputVals.length();i++){
            toBeReturned += (this.labels).get(i) + " : " + outputVals.getDouble(i) + ", ";
        }
        toBeReturned += "]";

        return toBeReturned;
    }

    /**
     * Helper method called by getOutput(). This will just get the values, the labels for which the values belong with
     * will be added in getOutput.
     * @param locationObject
     * @return
     */
    private INDArray getOutputVals(LocationObject locationObject){
        INDArray input = formatInput(locationObject);
        INDArray output = this.mln.output(input);
        return output;
    }


    /**
     * Takes a location object with feature fields filled in and constructs an INDArray of features out of it for
     * feeding into machine learning model.
     * @param locationObject structure holding feature data
     * @return INDArray of features
     */
    private INDArray formatInput(LocationObject locationObject){
        float[] featuresFloatArray = new float[NUMBER_OF_FEATURES];

        //set each feature into the float array based on the index/feature mapping:
        featuresFloatArray[0] = (float)locationObject.getLightLevel();
        featuresFloatArray[1] = (float)locationObject.getSoundLevel();
        featuresFloatArray[2] = (float)locationObject.getGeoMagneticFieldStrength();
        featuresFloatArray[3] = (float)locationObject.getCellId();
        featuresFloatArray[4] = (float)locationObject.getAreaCode();
        featuresFloatArray[5] = (float)locationObject.getCellSignalStrength();

        //create NDArray (implementation of INDArray interface) to return:
        NDArray toBeReturned = new NDArray(featuresFloatArray);

        return toBeReturned;
    }

    /**
     * Calls DatabaseTest.getMLDataFromDatabase to obtain already formatted data from database in MLData object. Then
     * create DL4J dataset from said data.
     */
    private void getDataAndCompileDataset(){
        MLData obtainedData = (new AppDatabase(this.context)).getMLDataFromDatabase();
        this.trainingData = new DataSet(new NDArray(obtainedData.features), new NDArray(obtainedData.encodedLabels));
        (this.trainingData).setLabelNames(obtainedData.labels);
        this.numberOfLocations = obtainedData.numberOfLocations;
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
     */
    private void createMlModel(){
        //vars to be used in model adjustments:
        long seed = (new Date().getTime());
        double learningRate = 0.1;
        int numberOfEpochs = 3;

        /* - create model configurations then initialize model with configuration
           - The nOut value = number of classes for regression or number of locations we have, this will be set when
           obtaining data inside of the numberOfLocations class variable.
           - nIn is the number of features which right now is constant at 6 features.
         */
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Nesterovs(learningRate,0.9))
                .list()
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(NUMBER_OF_FEATURES)
                        .activation(Activation.SOFTMAX)
                        .nOut(this.numberOfLocations).build())
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





    /*
     * The below section is used for obtaining random data that is stored within the app's directory. IT IS USED TO
     * TEST FUNCTIONALITY OF THE ML MODEL CODE NOT FOR ACTUAL USE AND SHOULD BE DISREGARDED FOR MOST PART, KEPT JUST
     * IN CASE FUNCTIONALITY NEEDS TO BE TEST AGAIN. If using this then un-comment lines with (!!!) at the end.
     */

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
        float[][] featuresMatrix = new float[dataMatrix.length][NUMBER_OF_FEATURES];
        for(int i=0;i< dataMatrix.length;i++){
            for(int j=0;j<NUMBER_OF_FEATURES;j++){
                featuresMatrix[i][j] = dataMatrix[i][j+1];
            }
        }

        // one-hot-encoding the labels (usually we would have to create a mapping between these and labels but since
        // this is all random we won't do it until later)
        float[][] labelArray = new float[dataMatrix.length][dataMatrix.length];
        for(int i=0;i< dataMatrix.length;i++){
            labelArray[i][i] = 1;
        }

        //this.trainingData = new DataSet(new NDArray(featuresMatrix),new NDArray(labelArray)); //(!!!)
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

}
