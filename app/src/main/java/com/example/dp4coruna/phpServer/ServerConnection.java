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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.os.*;
import com.example.dp4coruna.TempResultsActivity;

/**
 * Connects to the AWS PHP Server.
 *
 * Requires the DP4SERVERURL to be set in the secure.properties file
 *
 * General Workflow of the class:
 *  - Calling class will create object using 1 of several constructors. The constructor picked depends on how output will
 *  be processed. At this point, I recommend using Handlers for which there are many examples in this project. A direct
 *  example of a handler for this class's output can be found in the TempResults class called PrintOutputHandler. The
 *  handler has to be in the main looper which it is by default.
 *
 *  - Calling class will either call queryDatabase or updateDatabase (hasn't yet been implemented). When calling these
 *  methods a fully prepared sql statement needs to be provided in string format. That sql statement should NOT have a
 *  SEMICOLON at the end like usual.
 *
 *  - The 2 methods mentioned above will call sendRequest() method. This method will use the Google Volley API to send
 *  a post request to our server. The address of the server is defined in the file secure.properties. The post request will
 *  contain the fully prepared sql statement within in it. The Volley api will wait for the response.
 *
 *  - Once the response arrives it will be one large string that is essentially a html page. The data, being a html page,
 *  will have html tags and elements other than just the data we are looking for. That is when processHtmlForData() is
 *  called. This method extracts all of our required data as lines of strings and stores them in a List<String> object.
 *
 *  - After that the outputResults() method is called which based on whether a handler or a broadcast receiver or both
 *  have been set up will forward that list to them. It is up to the calling class to then process the list of strings
 *  into usable data.
 *
 *  - Each string represents a row with each column separated by a single space (" "). The programmer has to know before
 *  hand what the query returns and work based on that.
 *
 *  - The above 3 steps describe a query. An update will simply send a request and do nothing else. The programmer has to
 *  check the aws database to see if there were any updates.
 */
public class ServerConnection {
    private String SERVERURL;

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

    //declaration of broadcast destination:
    private String broadcastDestination;


    /**
     * Create an instance of object to connect with the DP4coRUna database.
     *
     * Requires the DP4SERVERURL to be set in the secure.properties file to function properly.
     *
     * @param context Application context from an AppCompatActivity/Service class (basic android activity)
     */
    public ServerConnection(Context context){
        generalConstructorOps(context);
        this.externalHandler = null;
        this.broadcastDestination = null;
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
        generalConstructorOps(context);
        this.externalHandler = handler;
        this.broadcastDestination = null;
    }

    /**
     * Create an instance of object to connect with the DP4coRUna database.
     * Requires the DP4SERVERURL to be set in the secure.properties file to function properly.
     *
     * This constructor is used if there is a broadcast receiver setup on the calling class to interpret the
     * results but no handler.
     *
     * @param context
     * @param broadCastDesination The package string associated with calling class. See Android BroadcastReceiver for details.
     */
    public ServerConnection(Context context, String broadCastDesination){
        generalConstructorOps(context);
        this.externalHandler = null;
        this.broadcastDestination = broadCastDesination;
    }

    /**
     * Create an instance of object to connect with the DP4coRUna database.
     * Requires the DP4SERVERURL to be set in the secure.properties file to function properly.
     *
     * This constructor accepts both a handler and a broadcast destination string for its argument and will
     * process both things.
     *
     * @param context
     * @param externalHandler Handler to handle string results when they are available.
     * @param broadcastDestination BroadcastReceiver to handle results when they are available.
     */
    public ServerConnection(Context context, Handler externalHandler, String broadcastDestination){
        generalConstructorOps(context);
        this.externalHandler = externalHandler;
        this.broadcastDestination = broadcastDestination;
    }

    /**
     * Carries out the operations that all constructors of this class are required to do.
     * @param context
     */
    private void generalConstructorOps(Context context){
        SERVERURL = context.getString(R.string.dp4_server_url);
        this.context = context;
        this.result = new ArrayList<>();
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
            result = processHtmlForData(response,requestType);
            outputResults(requestType);
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
     * Since the response from our server is an html page it needs to be processed.
     * This method will extract only the data and return it as a list of strings.
     * @param html
     * @return
     */
    private List<String> processHtmlForData(String html,int requestType){
        List<String> toBeReturned = new ArrayList<>();

        if(requestType == UPDATE_REQUEST){
            toBeReturned.add(html);
            return toBeReturned;
        }

        /*
            Parsing html string for results. Will do so by using regex and java Pattern and Match class.
         */
        String regexTarget = "<p>.*?<\\/p>";
        Pattern pattern = Pattern.compile(regexTarget);
        Matcher matcher = pattern.matcher(html);
        int nextIndex = 0;

        /*
            Lengths of <p> and </p>. We need this because the regex will match a string with these characters in it.
            So we need to get rid of them.
         */
        int beginingSectionLen = 3;
        int endSectionLen = 4; // </p> has 4 chars

        while(matcher.find(nextIndex)){
            String line = matcher.group();
            String strippedLine = line.substring(beginingSectionLen,(line.length()-endSectionLen));
            toBeReturned.add(strippedLine);
            nextIndex = matcher.end();
        }

        
        return toBeReturned;
    }


    /**
     * Method to send results back to calling class asynchronously.
     *
     * Will check if there is a handler and if there is send message to it.
     *
     * Will send broadcast with results if there is a destination to send to, i.e. this.broadcastDestination is not null.
     */
    private void outputResults(int requestType){
        Log.i("AreaLabelFeat", "in output results");
        //Comment out the line below if you want to test out the code
       // if(requestType == UPDATE_REQUEST) return;

        if(this.externalHandler != null){
            Log.i("AreaLabelFeat", "in external handler");
            Message msg = (this.externalHandler).obtainMessage();
            msg.obj = this.result;
            (this.externalHandler).sendMessage(msg);
        }

        //send the broadcast with results:
        if(this.broadcastDestination != null){
            Intent outputResultIntent = new Intent(this.broadcastDestination);
            outputResultIntent.putStringArrayListExtra("results",(ArrayList<String>) result);
            LocalBroadcastManager.getInstance(this.context).sendBroadcast(outputResultIntent);
        }
    }

    public List<String> getResults(){
        return this.result;
    }



}