package com.example.dp4coruna.ml;

import android.util.Log;
import org.deeplearning4j.datasets.datavec.RecordReaderDataSetIterator;
import org.nd4j.common.io.ClassPathResource;




import java.io.IOException;

public class MLModel {
    public MLModel(){

    }

    /**
     * Will read provided data (from csv for now) and format it to be used by machine learning model
     */
    public void obtainDataSet(){
        int linesToSkip = 1;
        char delimiter = ',';
        String log_tag = "FromObtainData"; //(!!!)

        Log.i(log_tag,"Got here at leaste");
        RecordReader rr = new CSVRecordReader(linesToSkip,delimiter);

        try {
            rr.initialize(new FileSplit(new ClassPathResource("dl4jsampledata.csv").getFile()));
        } catch (IOException e) {
            Log.i(log_tag,"IO exception");
            e.printStackTrace();
        } catch (InterruptedException e) {
            Log.i(log_tag,"InterruptException");
            e.printStackTrace();
        }

        DataSetIterator dsi = new RecordReaderDataSetIterator.Builder(rr,5).regression(0,2).build(); //working on it
        DataSet ds = dsi.next();
        INDArray ia = ds.getFeatures();
        Log.i(log_tag,String.valueOf(ia.length()));
    }


}
