package tipsystem.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import org.json.JSONArray;

import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;

public class MSSQL2 extends AsyncTask<String, Integer, JSONArray> {

	public interface MSSQL2CallbackInterface {
		public void onRequestCompleted(JSONArray results);
		public void onRequestFailed(int code, String msg);
    }

    private MSSQL2CallbackInterface mCallback;
    int errCode =0;
    String errMsg;

    public MSSQL2(MSSQL2CallbackInterface callback) {
        mCallback = callback;
    }

    // doInBackground 메소드가 실행되기 전에 실행되는 메소드
	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	}

    protected JSONArray doInBackground(String... params) {
    	Connection conn = null;
    	JSONArray json = new JSONArray();
    	
    	String ip = params[0];
    	String dbname = params[1];
    	String dbid = params[2];
    	String dbpw = params[3];
    	String query = params[4];

    	//2019-11-14 IP/PORT 정보 확인
    	Log.i("MSSQL","IP:" + ip);

    	try {
    	    Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
    	    Log.i("MSSQL","MSSQL driver load");

    	    String url = "jdbc:jtds:sqlserver://" +ip +"/"+ dbname;
    	    conn = DriverManager.getConnection(url, dbid, dbpw);
    	    Log.i("MSSQL","MSSQL open: " + url);
    	    
    	    Statement stmt = conn.createStatement();       
    	    ResultSet rs =null;
        	Log.w("MSSQL","query: " + query );
            rs = stmt.executeQuery(query);
            
        	json = ResultSetConverter.convert(rs);

			conn.close();
   	 	} catch (SQLException e) {
    	    Log.e("Error connection","" + e.getMessage());
    	    errCode = 1;
    	    errMsg = e.getMessage();
    	} catch (Exception e) {
    	    Log.e("Error connection","" + e.getMessage());	
    	    errCode = 2;
    	    errMsg = e.getMessage();
    	}
    	
    	return json;        	 
    }

    protected void onPostExecute(JSONArray results) {
    	super.onPostExecute(results);
		switch (errCode) {
		case 0 :
			Log.w("MSSQL:", "onPostExecute: " +results.toString());
	        mCallback.onRequestCompleted(results);
			break;
		case 1 :
		case 2 :
	        mCallback.onRequestFailed(errCode, errMsg);
			break;
		}
    }
}

