package tipsystem.tips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.tips.ManageCodeSMSResults.SmsListAdapter;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;

import kr.co.tipos.tips.R;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ManageCodeSMSResultsListActivity extends Activity {
	
	String m_smsNum;
	String m_callBack;
	
	JSONObject m_shop;

	//----------------------------------------//
	// 2022.05.26.본사서버 IP변경
	//----------------------------------------//
	String m_ip = "";
	String m_port = "";
	//----------------------------------------//
	// 2021.12.21. 매장DB IP,PW,DB 추가
	//----------------------------------------//
	String m_uuid = "";
	String m_uupw = "";
	String m_uudb = "";
	//----------------------------------------//

	//전송결과 리스트 목록
	SimpleAdapter adapter;
	ListView m_resultsList;
	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();
	
	ProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_code_smsresults_list);
		
			m_shop = LocalStorage.getJSONObject(this, "currentShopData");		 		
			
	       	 try {
				m_ip = m_shop.getString("SHOP_IP");
		       	m_port = m_shop.getString("SHOP_PORT");
				 //----------------------------------------//
				 // 2021.12.21. 매장DB IP,PW,DB 추가
				 //----------------------------------------//
				 m_uuid = m_shop.getString("shop_uuid");
				 m_uupw = m_shop.getString("shop_uupass");
				 m_uudb = m_shop.getString("shop_uudb");
				 //----------------------------------------//

		   	} catch (JSONException e) {
				e.printStackTrace();
			}
			
			Intent intent = getIntent();
			
			m_smsNum = intent.getExtras().getString("SMS_Num");
			String posno = intent.getExtras().getString("Pos_No");
			String smscount = intent.getExtras().getString("SMS_Count");
			m_callBack = intent.getExtras().getString("CallBack");
			String msg = intent.getExtras().getString("Msg");
		
			TextView tv1 = (TextView)findViewById(R.id.textView2);
			tv1.setText(m_smsNum);
			
			TextView tv2 = (TextView)findViewById(R.id.textView4);
			tv2.setText(posno);
			
			TextView tv3 = (TextView)findViewById(R.id.textView6);
			tv3.setText(m_callBack);
			
			TextView tv4 = (TextView)findViewById(R.id.textView8);
			tv4.setText(smscount);
			
			TextView tv5 = (TextView)findViewById(R.id.editText_smsMsg);
			tv5.setText(msg);
			
			m_resultsList = (ListView)findViewById(R.id.listview_smsResultsDetail);
			
			String[] from = new String[] {"Cus_Code", "Cus_Name", "Phone_Number", "Result_In", "Send_Date", "End_Date" };
	        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6 };
	        
			adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_item7, from, to);		
			m_resultsList.setAdapter(adapter);
			
			sendSmsResultsList();
	}
	
	
	private void sendSmsResultsList(){
		
		mfillMaps.removeAll(mfillMaps);		
		
		String query = "";
		
		query = " Select * From Hphone_sms_history Where SMS_Num='"+m_smsNum+"' And CallBack='"+m_callBack+"' ";
		
		
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();
				
		// 콜백함수와 함께 실행
	    new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				
				if (results.length()>0) {							
					resultsUpdate(results);
					Toast.makeText(getApplicationContext(), "조회완료. 휴대폰정보 조회 시작  ", Toast.LENGTH_SHORT).show();
				} else {
										
		            Toast.makeText(getApplicationContext(), "조회 결과가 없습니다.", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
												
		    	Toast.makeText(getApplicationContext(), " 네트워크 및 서버를 찾을수 없습니다. ", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	
	private void resultsUpdate(JSONArray results){
		
		for(int i = 0 ; i < results.length(); i++ ){
		HashMap<String, String> map = new HashMap<String, String>();
		JSONObject son;								
			try {
				son = results.getJSONObject(i);
				map =JsonHelper.toStringHashMap(son);
				mfillMaps.add(map);
			} catch (JSONException e) {
				e.printStackTrace();
			}								
		}
		adapter.notifyDataSetChanged();
	}
	
	@Override	
	public boolean onCreateOptionsMenu(Menu menu) {
			// Inflate the menu; this adds items to the action bar if it is present.
			getMenuInflater().inflate(R.menu.manage_customer, menu);
			return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//				
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
				
			NavUtils.navigateUpFromSameTask(this);
			return true;

			case R.id.action_settings:	 
				startActivity(new Intent(this, TIPSPreferences.class));
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
