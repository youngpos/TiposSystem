package tipsystem.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.json.JSONArray;

import android.content.Context;
import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 업데이트 용 * 
 * @author 2017-07-01 김현수
 *
 */
public class MSSQL4 extends AsyncTask<String, Integer, Integer> {
	
	public interface MSSQL4CallbackInterface {
		public void onRequestCompleted(Integer results);
		public void onRequestFailed(int code, String msg);
    }

    private MSSQL4CallbackInterface mCallback;
    int errCode =0;
    String errMsg;   
    
    
    public MSSQL4(MSSQL4CallbackInterface callback) {
        mCallback = callback;
    }

    // doInBackground 메소드가 실행되기 전에 실행되는 메소드
	@Override
	protected void onPreExecute() {
	    super.onPreExecute();	    
	}

    protected Integer doInBackground(String... params) {
    	Log.i("MSSQL4"," MSSQL4 Connect Example.");
    	Connection conn = null;    	
    	 int result = 0;
    	
    	String ip = params[0];
    	String dbname = params[1];
    	String dbid = params[2];
    	String dbpw = params[3];
    	String query = params[4];
        Log.e("MSSQL4","query: " + query );
    	
    	try {
    	    Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
    	    Log.i("MSSQL4","MSSQL4 driver load");

    	    conn = DriverManager.getConnection("jdbc:jtds:sqlserver://" +ip +"/"+ dbname, dbid, dbpw);
    	    Log.i("MSSQL4","MSSQL4 open");
    	    Statement stmt = conn.createStatement();
    	    
    	    Log.w("MSSQL","query: " + query );
            result = stmt.executeUpdate(query);
                        
            stmt.close();        	
    	    conn.close();

   	 	 } catch (SQLException e) {
    	    Log.w("Error connection","" + e.getMessage());	
    	    errCode = 1;
    	    errMsg = e.getMessage();
    	 } catch (Exception e) {
    	    Log.w("Error connection","" + e.getMessage());		
    	    errCode = 2;
    	    errMsg = e.getMessage();
    	 }
    	 
    	 return result;	 
    }

    protected void onPostExecute(Integer results) {
    	super.onPostExecute(results);
    	switch (errCode) {
		case 0 :
			Log.w("MSSQL4:", "onPostExecute: " +results);
	        mCallback.onRequestCompleted(results);
			break;
		case 1 :
		case 2 :
	        mCallback.onRequestFailed(errCode, errMsg);
			break;
		}
    }
}

