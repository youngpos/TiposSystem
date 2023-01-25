package tipsystem.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import org.json.JSONArray;

import android.database.SQLException;
import android.os.AsyncTask;
import android.util.Log;

/**
 * 
 * @author Administrator 다량의 쿼리 전송용 메소드
 *
 */
public class MSSQL3 extends AsyncTask<ArrayList<String>, Integer, JSONArray > {

	public interface MSSQL3CallbackInterface {
		public void onRequestCompleted(JSONArray results);
		public void onRequestFailed(int code, String msg);
    }

    private MSSQL3CallbackInterface mCallback;
    int errCode =0;
    String errMsg;

    public MSSQL3(MSSQL3CallbackInterface callback) {
        mCallback = callback;
    }

    // doInBackground 메소드가 실행되기 전에 실행되는 메소드
	@Override
	protected void onPreExecute() {
	    super.onPreExecute();
	}

    protected JSONArray doInBackground(ArrayList<String>... params) {
    	Connection conn = null;
    	JSONArray json = new JSONArray();    	
    	int[] updateCounts;
    	
    	String ip="";
    	String dbname="";
    	String dbid="";
    	String dbpw="";
    	ArrayList<String> queryList = params[1];
    	String query="";
    	
    	
    	
    		ip = params[0].get(0);
        	dbname = params[0].get(1);
        	dbid = params[0].get(2);
        	dbpw = params[0].get(3);        	
    	    	
    	Log.i("MSSQL",ip+dbname+dbid+dbpw);
    	
    	
    	try {
    	    Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
    	    Log.i("MSSQL","MSSQL driver load");

    	    String url = "jdbc:jtds:sqlserver://" +ip +"/"+ dbname;
    	    conn = DriverManager.getConnection(url, dbid, dbpw);
    	    Log.i("MSSQL","MSSQL open: " + url);
    	    
    	    Statement stmt = conn.createStatement();           
    	    
        	//ResultSet rs =null;
        	Log.w("MSSQL","query: " + query );
        	
        	conn.setAutoCommit(false);
        	
        	for(int i = 0 ; i < queryList.size(); i++ ){        		
        		stmt.addBatch(queryList.get(i));        		
        		//오류 방지용 천개 씩 끊어서 실행
        		Log.i("MSSQL","query: " + queryList.get(i) );
        		if( i % 1000 == 0) {
        	         updateCounts = stmt.executeBatch();        			
        	    }         		
        	}        	
        	
        	updateCounts = stmt.executeBatch();
        	//rs = stmt.executeQuery(query);	            
        	//json = ResultSetConverter.convert(rs);
        	
        	conn.setAutoCommit(true);
        	stmt.close();
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

