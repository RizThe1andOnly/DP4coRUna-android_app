package com.example.dp4coruna.dataManagement.remoteDatabase;


import android.content.Context;
import android.os.Handler;
import android.util.Log;
import com.example.dp4coruna.R;

import java.sql.*;
import java.util.Properties;

public class DbConnection {
    private Handler responseHandler;
    private Context localContext;

    /**
     * Accepts a handler that will process the response which will contain
     * the data retrieved from the database.
     *
     * The handler should be run on the main looper!!
     * @param responseHandler obj that extends handler class for specific task with response
     * @param context Context of the calling activity
     */
    public DbConnection(Handler responseHandler, Context context){
        this.responseHandler = responseHandler;
        this.localContext = context;
    }

    private Connection getConnection() throws SQLException {

        Connection mysqlConnection = null;

        //generate the connection to the database and return it
        try{
            mysqlConnection = DriverManager.getConnection("jdbc:mysql://cs336-g12-db.cah5sjf8kxme.us-east-2.rds.amazonaws.com:3306/TrainApp",
                    "g12",
                    "cs336Password");
        } catch (Exception e){
            Log.i("DbConnection",e.toString());
        }

        // for the very first time. to create the database to be used for our project.
        if(mysqlConnection != null){
            String databaseCreationString = "create database if not exists dp4coruna;" ;

            try(Statement stmnt = mysqlConnection.createStatement()){
                //stmnt.executeUpdate(databaseCreationString);
            } catch (Exception e){
                Log.i("DbConnection",e.toString());
            }
        }

        return mysqlConnection;
    }

    /**
     * Doesn't have to return anything i just did it like this.
     * @return
     */
    public boolean createTableExample(){
        //table creation sql statement:
        String creationStatement = "create table if not exists exampletable ("
                                    + "exmplUser varchar(255),"
                                    + "examplPassword int,"
                                    + "PRIMARY KEY(exmplUser)"
                                    + ");";

        //execution of statement:
        int result = 0;
        try(Statement statement = getConnection().createStatement()){
            result = statement.executeUpdate(creationStatement);
        } catch (Exception e){
            Log.i("DbConnection",e.toString());
        }

        if(result == -1) return false;
        return true;
    }


    /**
     * Example of adding data to an existing table. This method is for specifically
     * adding data to the exampleTable i created earlier. However, there are other
     * ways to approach this, it all depends on how you want to design it.
     * @param username
     * @param password
     * @return
     */
    public boolean addDataToTable(String username, int password){
        String addString = "insert into exampletable (exmplUser,exmplPassword) values (?,?);";

        int results = 0;
        try(PreparedStatement ps = getConnection().prepareStatement(addString)){
            ps.setString(1,username); //note prepared statment index starts at 1.
            ps.setInt(2,password);
            results = ps.executeUpdate();
        }catch (Exception e){
            Log.i("DbConnection",e.toString());
        }

        if(results == -1) return false;
        return true;
    }

    /**
     * Example query. I have the query corresponding to the table created in createTableExample().
     * I also have the prepared statement as part of the comments inside of the method.
     *
     * I have the exception thrown here instead of doing try catch. Its for convenience there is no
     * actual reason for it.
     *
     * The return depends on how you want to process it. I'm doing void beacuse i won't return
     * anything from here.
     *
     * @throws SQLException
     */
    public void queryTable() throws SQLException{
        String queryString = "select exmplUser, "
                            +       "exmplPassword "
                            + "from exampletable;";

        /*
            Prepared statement: used with variables. place a question mark where the variable should go in the
                                query and then in the PreparedStatement code we will replace the ?.

            String psString = "select exmplUser, "
                                +    "exmplPassword "
                                + "from exampletable "
                                + "where exmplUser = ? "
                                + ";";

            String questionMarkReplacement = someUsername;

            PreparedStatement pstatement = setupConnection().prepareStatement(psString);
            pstatement.setString(1,questionMarkReplacement);

            everything else is the same.

         */


        ResultSet result = null;
        Statement statement = getConnection().createStatement();
        result = statement.executeQuery(queryString);

        /*
            ResultSet is an object that is gone through using while loop and next().
            next() goes through rows and columns are based on your query statement and
            the data types of the table. You have to manually put in the correct column
            indexes based on your query. For the current query exmplUser is index = 0 and
            exmplPassword is 1. The type for exmplUser is string and exmplPassword is int so
            getString and getInt are used.
         */
        while(result.next()){//next returns boolean; false after all rows examined
            String usrName = result.getString(0);
            int password = result.getInt(1);
            Log.i("QueryAns",usrName + " : " + password);
        }
    }


}
