package com.example.dp4coruna.ml;

import android.util.Log;
import org.datavec.api.records.reader.RecordReader;
import org.datavec.api.records.reader.impl.csv.CSVRecordReader;
import org.datavec.api.split.FileSplit;
import org.datavec.api.writable.Writable;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.dataset.api.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
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
    private final int NUMBER_OF_FEATURES = 6;

    //call vars:
    public MultiLayerNetwork mln;
    private DataSet trainingData;

    /**
     * (!!!) Constructor will be updated later.
     */
    public MLModel(){

    }

    /**
     * Goal for 8/12/2020 11:48 AM - ? : Build and train softmax model and be able to get probabilities for input with random data
     * Specific Tasks:
     *  - Create method(s) to create the model and specify its properties and parameters. This method should also train
     *    model with given data.
     *
     *  - Create method(s) to read data from sql database and format it for use by above task.
     *
     */

    /**
     * Will create and adjust a multinomial logistic regression (softmax) model.
     */
    private void createMlModel(){
        //vars to be used in model adjustments:
        long seed = (new Date().getTime());
        double learningRate = 0.1;
        int numberOfEpochs = 10;

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
         *  - Don't know what proper number for epochs should be so will use 10 for now
         */
        for(int i=0;i<numberOfEpochs;i++){
            this.mln.fit(this.trainingData);
        }
    }


    /**
     * Will read provided data (from csv for now) and format it to be used by machine learning model
     */
    public void obtainDataSet(){
        int linesToSkip = 0;
        char delimiter = ',';
        String log_tag = "FromObtainData"; //(!!!)

        RecordReader rr = new CSVRecordReader(linesToSkip,delimiter);
        try {
            rr.initialize(new FileSplit(new ClassPathResource("dl4jsampledata.csv").getFile()));
        } catch (IOException e) {
            Log.e(log_tag,"IO exception");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.e(log_tag,"InterruptException");
            e.printStackTrace();
        }

        //DataSetIterator dsi = new RecordReaderDataSetIterator.Builder(rr,5).regression().build(); //working on it
    }

}
