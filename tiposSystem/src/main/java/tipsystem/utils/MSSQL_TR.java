package tipsystem.utils;

import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

public class MSSQL_TR extends AsyncTask<String, Integer, JSONArray> {

	public interface MSSQLCallbackInterface {
		public void onRequestCompleted(JSONArray results);
		public void onRequestFailed(int code, String msg);
	}

    private MSSQLCallbackInterface mCallback;
	int errCode =0;
	String errMsg;

    public MSSQL_TR(MSSQLCallbackInterface callback) {
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

			conn.setAutoCommit(false);	// tran 추가

			try {
				rs = stmt.executeQuery(query);
				json = ResultSetConverter.convert(rs);
				conn.commit();
			}
			catch (SQLException ex){
				conn.rollback();
			}

			//conn.setAutoCommit(true);
    	    //conn.close();

   	 	 } catch (SQLException e) {
    	    //Log.w("Error connection","" + e.getMessage());
			Log.e("Error connection","" + e.getMessage());
			errCode = 1;
			errMsg = e.getMessage();
			if (conn != null){
				try{
					conn.rollback();
				} catch (java.sql.SQLException ex) {
					ex.printStackTrace();
				}
			}
    	 } catch (Exception e) {
    	    //Log.w("Error connection","" + e.getMessage());
			Log.e("Error connection","" + e.getMessage());
			errCode = 2;
			errMsg = e.getMessage();
			if (conn != null){
				try {
					conn.rollback();
				} catch (java.sql.SQLException ex) {
					ex.printStackTrace();
				}
			}
    	 }

		if (conn != null){
			try {
				conn.setAutoCommit(true);
				conn.close();
			} catch (java.sql.SQLException e) {
				e.printStackTrace();
			}
		}

    	 return json;
    }

    protected void onPostExecute(JSONArray results) {
    	super.onPostExecute(results);
		//Log.w("MSSQL:", "onPostExecute: " +results.toString());
        //mCallback.onRequestCompleted(results);
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

