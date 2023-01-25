package tipsystem.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.json.JSONArray;

import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;

public class MSSQL extends AsyncTask<String, Integer, JSONArray> {

	public interface MSSQLCallbackInterface {
		public void onRequestCompleted(JSONArray results);
    }

    private MSSQLCallbackInterface mCallback;

    public MSSQL(MSSQLCallbackInterface callback) {
        mCallback = callback;
    }

    // doInBackground 메소드가 실행되기 전에 실행되는 메소드
	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	}

    protected JSONArray doInBackground(String... params) {
    	Log.i("MSSQL"," MSSQL Connect Example.");
    	Connection conn = null;
    	JSONArray json = new JSONArray();
    	String ip = params[0];
    	String dbname = params[1];
    	String dbid = params[2];
    	String dbpw = params[3];
    	String query = params[4];
        Log.e("MSSQL","query: " + query );
    	
    	try {
    	    Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
    	    Log.i("MSSQL","MSSQL driver load");

    	    conn = DriverManager.getConnection("jdbc:jtds:sqlserver://" +ip +"/"+ dbname, dbid, dbpw);
    	    Log.i("MSSQL","MSSQL open");
    	    Statement stmt = conn.createStatement();
        	ResultSet rs =null;
            rs = stmt.executeQuery(query);	            
        	json = ResultSetConverter.convert(rs);
    	   
    	    conn.close();

   	 	 } catch (SQLException e) {
    	    Log.w("Error connection","" + e.getMessage());	
    	 } catch (Exception e) {
    	    Log.w("Error connection","" + e.getMessage());		   
    	 }
    	 
    	 return json;        	 
    }

    protected void onPostExecute(JSONArray results) {
    	super.onPostExecute(results);
		Log.w("MSSQL:", "onPostExecute: " +results.toString());

        mCallback.onRequestCompleted(results);
    }
}

