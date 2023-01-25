package tipsystem.tips;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class ManageEventListActivity extends Activity {
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

	CheckBox m_checkBoxCurrent;
	ListView m_cusList;
	SimpleAdapter m_adapter; 
	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();
	JSONArray m_eventList;
	
    // loading bar
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_event_list);
		
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

		m_cusList= (ListView)findViewById(R.id.listviewManageProductList);
		m_cusList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	returnResultData(position);
            }
        });
				
		m_checkBoxCurrent =(CheckBox)findViewById(R.id.checkBoxCurrent);
		m_checkBoxCurrent.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {

				fetchDate();
			}
		});

    	String[] from = new String[] {"Evt_Name", "행사구분", "Evt_Date"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3};
        m_adapter = new SimpleAdapter(this, mfillMaps, R.layout. activity_event_list, from, to);
        m_cusList.setAdapter(m_adapter);
	}
	
	@Override
	protected void onResume() {
		super.onResume();

		fetchDate();
	}

	private void returnResultData(int position) {
		String Evt_CD = mfillMaps.get(position).get("Evt_CD");
  		Intent intent = new Intent(this, ManageEventActivity.class);
		intent.putExtra("Evt_CD", Evt_CD);
		Log.i("Evt_CD" , Evt_CD);
		startActivity(intent);
	}
	    
	private void fetchDate() {

		String query = "";    	
		query = "Select Finish_date From Finish;";

	    new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				if (results.length() > 0) {
					try {
						JSONObject json = results.getJSONObject(0);
						String Finish_date = json.getString("Finish_date");	
						doSearch(Finish_date);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				else {
					Toast.makeText(getApplicationContext(), "종료기간을 알수 없습니다.", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
				finish();
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	private void doSearch(String Finish_date) {
	    SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd");
    	String nextDay = Finish_date;
	    try {
	    	Date d = form.parse(Finish_date);
	    	Calendar c = Calendar.getInstance();
	    	c.setTime(d);
	    	c.add(Calendar.DATE, -1);
	    	nextDay = String.format("%04d-%02d-%02d", c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
	    } catch (java.text.ParseException e) {
	        e.printStackTrace();
	    }
		
		String query = "";
		query = "Select '0', A.Evt_CD, A.Evt_Name, COUNT(A.BarCode) AS Goods_Cnt,"
				+ " 행사구분=Case When A.Evt_Gubun=1 Then '기간행사' When Evt_Gubun=2 "
				+ " Then '연속행사' When Evt_Gubun=2 Then '시간행사' Else '' End ," 
				+ " 적용구분=Case When A.Evt_SDate<='"+Finish_date+"' AND Evt_EDate > '"+nextDay+"' Then '적용' ELSE '미적용' End, " 
				+ " A.Evt_SDate, A.Evt_EDate, A.Evt_STime , A.Evt_ETime, A.INEVT_SDATE, A.INEVT_EDATE"
				+ " From Evt_Mst A inner join  Goods B On A.Barcode=B.Barcode "
				+ " Where A.Evt_Name Like '%%' And A.Evt_CD Like '%%' And A.BarCode Like "
				+ " '%%' AND B.G_Name Like '%%' And (A.Writer Like '%%' Or A.Editor Like '%%') "
				+ " Group By A.Evt_CD, A.Evt_Name, A.Evt_Gubun, A.Evt_SDate, A.Evt_EDate, A.Evt_STime, A.Evt_ETime, A.INEVT_SDATE, A.INEVT_EDATE  Order by A.Evt_EDate DESC ";
		
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.show();

	    new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				if (results.length() > 0) {
					updateListView(results);
					
		            Toast.makeText(getApplicationContext(), "조회 완료", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();							
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	

	private void updateListView(JSONArray results) {

		if (!mfillMaps.isEmpty()) {
			mfillMaps.removeAll(mfillMaps);
			m_adapter.notifyDataSetChanged();	
		}
		boolean isCurrent = m_checkBoxCurrent.isChecked();
		
        for (int i = 0; i < results.length(); i++) {
        	
        	try {
            	JSONObject json = results.getJSONObject(i);
            	HashMap<String, String> map = JsonHelper.toStringHashMap(json);
            	
            	String Evt_SDate = map.get("Evt_SDate");
            	String Evt_EDate = map.get("Evt_EDate");
            	map.put("Evt_Date", Evt_SDate + " ~ " + Evt_EDate);
            	
            	if (isCurrent&& map.get("적용구분").equals("미적용")) continue;
            	
	            mfillMaps.add(map);
	            		 
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

        m_adapter.notifyDataSetChanged();
    }
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		
			ActionBar actionbar = getActionBar();         
	
			actionbar.setDisplayShowHomeEnabled(false);
			actionbar.setDisplayShowTitleEnabled(true);
			actionbar.setDisplayShowCustomEnabled(true);
			actionbar.setTitle("전체 행사 조회");
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.customer_product_detail_view, menu);
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
	
	public void onNewEvent(View v) {

  		Intent intent = new Intent(this, ManageEventActivity.class);
		intent.putExtra("Evt_CD", "");
      	startActivity(intent);
	};
}