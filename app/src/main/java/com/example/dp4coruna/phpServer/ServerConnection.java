package com.example.dp4coruna.phpServer;

import android.content.Context;
import android.content.Intent;
import android.os.Message;
import android.util.Log;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.dp4coruna.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.*;
import com.example.dp4coruna.TempResultsActivity;

/**
 * Connects to the AWS PHP Server.
 *
 * Requires the DP4SERVERURL to be set in the secure.properties file
 */
public class ServerConnection {
    private final String SERVERURL;

    private Context context;

    //constants for each type of request:
    private final String QUERY_REQUEST_STRING = "/SamplePage.php";
    private final String UPDATE_REQUEST_STRING = "";
    private final int QUERY_REQUEST = 0;
    private final int UPDATE_REQUEST = 1;

    //this will be updated everytime there is a network request
    private List<String> result;

    //thread definition for network request:
    private Thread networkRequestThread;

    //declare external handler:
    private Handler externalHandler;


    /**
     * Create an instance of object to connect with the DP4coRUna database.
     *
     * Requires the DP4SERVERURL to be set in the secure.properties file to function properly.
     *
     * @param context Application context from an AppCompatActivity/Service class (basic android activity)
     */
    public ServerConnection(Context context){
        SERVERURL = context.getString(R.string.dp4_server_url);
        this.context = context;

        this.result = new ArrayList<>();
        this.externalHandler = null;
    }

    /**
     * Create an instance of object to connect with the DP4coRUna database.
     * Requires the DP4SERVERURL to be set in the secure.properties file to function properly.
     *
     * This constructor takes a handler as an argument which will be sent a List<String> obj with the results.
     * The list will be the msg.obj property in the handleMessage() method of the handler. The handler must be
     * defined to be able to handle this.
     *
     * @param context
     * @param handler Handler object that can use List<String> object.
     */
    public ServerConnection(Context context, Handler handler){
        SERVERURL = context.getString(R.string.dp4_server_url);
        this.context = context;

        this.result = new ArrayList<>();
        this.externalHandler = handler;
    }


    /**
     * Queries the AWS database and returns the result.
     *
     * The result is returned as a list of string with each column separated by a whitespace (" ").
     * The returned data must be processed by the caller.
     *
     * @param sqlStatement
     *
     * @return List of Strings; each String is a row returned by query
     */
    public void queryDatabase(String sqlStatement){
        sendRequest(sqlStatement,this.QUERY_REQUEST);

        //uncomment below try/catch block if using thread version of code
        // wait for the network thread to finish (be interrupted). this way the answer is sent back.
//        try {
//            (this.networkRequestThread).join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    /**
     * Sends the http request to the AWS server.
     *
     * The server returns an html page ( I didn't know much about aws server or databases so this
     * how I set it up). The html page will be processed with another function.
     *
     * Asynchronous version. Will use broadcast receiver/sender to communicate with other classes. Will
     * also contain the capacity to utilize handlers if present.
     *
     * @param sqlStatement Fully prepared sql statement
     * @param requestType Constant defined in this class, see section labeled "constants for each type of request"
     */
    private void sendRequest(String sqlStatement, int requestType){
        Context localContext = this.context;
        String url = localContext.getString(R.string.dp4_server_url);

        //set type of request:
        switch (requestType){
            case UPDATE_REQUEST: url += UPDATE_REQUEST_STRING; break;
            case QUERY_REQUEST: url += QUERY_REQUEST_STRING; break;
        }

        String fullUrl = url;

        RequestQueue rq = Volley.newRequestQueue(localContext);
        StringRequest sr = new StringRequest(Request.Method.POST,fullUrl,response->{
                /*
                    This lambda function is triggered when the server returns a page.

                    response is a String value.

                    response will be processed by processHtmlForData() to get only the data.
                 */
            result = processHtmlForData(response);
            outputResults();
        },error ->{
            Log.i("FromServerConnection",error.toString());
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map paramMap = new HashMap<String,String>();
                paramMap.put("sqlQuery",sqlStatement);
                return paramMap;
            }
        };

        rq.add(sr);
    }


    /**
     * Sends the http request to the AWS server.
     *
     * The server returns an html page ( I didn't know much about aws server or databases so this
     * how I set it up). The html page will be processed with another function.
     *
     * Synchronous version using threads. uses thread interrupt here and thread join on calling method for synchronization.
     *
     * @param sqlStatement Fully prepared sql statement
     * @param requestType Constant defined in this class, see section labeled "constants for each type of request"
     */
    private void sendRequest_threadVersion(String sqlStatement, int requestType){
        final Context localContext = this.context;
        String url = localContext.getString(R.string.dp4_server_url);

        //set type of request:
        switch (requestType){
            case UPDATE_REQUEST: url += UPDATE_REQUEST_STRING; break;
            case QUERY_REQUEST: url += QUERY_REQUEST_STRING; break;
        }

        // the final vars are required to use the local variables in another class like the one being defined by Thread below.
        final String fullUrl = url;
        final String sqlStatementForThread = sqlStatement;

        //start new thread for network request. Thread is used to make operation synchronous.
        this.networkRequestThread = new Thread(()->{
            /*
                Lambda Runnable.

                Uses Goolge Volley api to send network request to the proper php page.
             */

            RequestQueue rq = Volley.newRequestQueue(localContext);
            StringRequest sr = new StringRequest(Request.Method.POST,fullUrl,response->{
                /*
                    This lambda function is triggered when the server returns a page.

                    response is a String value.

                    response will be processed by processHtmlForData() to get only the data.
                 */
                result = processHtmlForData(response);
                Thread.currentThread().interrupt(); //stop the thread when response is available
            },error ->{
                Log.i("FromServerConnection",error.toString());
                Thread.currentThread().interrupt(); //stop the thread when error is available
            });

            rq.add(sr);
        },"NetworkReqThread");


    }


    /**
     * Since the response from our server is an html page it needs to be processed.
     * This method will extract only the data and return it as a list of strings.
     * @param html
     * @return
     */
    private List<String> processHtmlForData(String html){
        List<String> toBeReturned = new ArrayList<>();
        toBeReturned.add(html);

        /*
            Parsing html string for results. Will do so by simply looking for <p> and </p> keeping everything
            in between for each of these sets.
         */
        String regexTarget = "";




        return toBeReturned;
    }


    /**
     * Method to send results back to calling class asynchronously.
     *
     * Will check if there is a handler and if there is send message to it.
     *
     * Will send broadcast with results even if there are no classes waiting to receive.
     */
    private void outputResults(){
        if(this.externalHandler != null){
            Message msg = (this.externalHandler).obtainMessage();
            msg.obj = this.result;
            (this.externalHandler).sendMessage(msg);
        }

        //send the broadcast with results:
        Intent outputResultIntent = new Intent(TempResultsActivity.BROADCAST_RECEIVE_ACTION);
        outputResultIntent.putStringArrayListExtra("results",(ArrayList<String>) result);
        LocalBroadcastManager.getInstance(this.context).sendBroadcast(outputResultIntent);
    }
}
