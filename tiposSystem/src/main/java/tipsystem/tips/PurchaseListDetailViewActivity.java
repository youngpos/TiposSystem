package tipsystem.tips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.StringFormat;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class PurchaseListDetailViewActivity extends Activity {

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

	ListView m_listPurchaseDetail;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_purchase_list_detail_view);
		
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
        
		m_listPurchaseDetail= (ListView)findViewById(R.id.listviewPurchaseDetailViewList);
		
		Intent intent = getIntent();
		
		String customerName = intent.getExtras().getString("OFFICE_NAME");
		String In_Date = intent.getExtras().getString("In_Date");
		String In_Num = intent.getExtras().getString("In_Num");

		int y = Integer.parseInt(In_Date.substring(0, 4));
		int m = Integer.parseInt(In_Date.substring(5, 7));
		
    	String query ="";

		String tableName = String.format("InD_%04d%02d", y, m);
		query = "select A.BARCODE, B.G_NAME, A.PUR_PRI, A.SELL_PRI, A.IN_COUNT "
	    		+ " from " + tableName + " as A inner join GOODS as B on A.BARCODE = B.BARCODE " 
	    		+ " where A.In_Num = '" + In_Num + "' and A.IN_DATE = '" + In_Date + "'" // and OFFICE_NAME = '" + customerName + "'"
				+ " ORDER BY A.IN_SEQ";
				
		// 콜백함수와 함께 실행
		new MSSQL(new MSSQL.MSSQLCallbackInterface() {
		
			@Override
			public void onRequestCompleted(JSONArray results) {
				setListItems(results);
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}

	void setListItems(JSONArray results)
	{
		try {
		
			if ( results.length() > 0 )
			{
				// create the grid item mapping
				String[] from = new String[] {"BARCODE", "G_NAME", "PUR_PRI", "SELL_PRI", "IN_COUNT"};
				int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5};
				
				// prepare the list of all records
				List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
 			
				for(int i = 0; i < results.length() ; i++)
				{
					JSONObject son = results.getJSONObject(i);
					
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("BARCODE", son.getString("BARCODE") );
					map.put("G_NAME", son.getString("G_NAME"));

					map.put("PUR_PRI", String.format("%d", son.getInt("PUR_PRI")) );
					map.put("SELL_PRI", String.format("%d", son.getInt("SELL_PRI")) );
					map.put("IN_COUNT", String.format("%d", son.getInt("IN_COUNT")) );
					map.put("PUR_PRI", StringFormat.convertToNumberFormat(map.get("PUR_PRI")));	
					map.put("SELL_PRI", StringFormat.convertToNumberFormat(map.get("SELL_PRI")));	
					
					fillMaps.add(map);
				}	
			
				// fill in the grid_item layout
				SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.activity_listview_item5_4, from, to);
				m_listPurchaseDetail.setAdapter(adapter);								
			}
			
			Toast.makeText(getApplicationContext(), "조회 완료 : " + results.length(), Toast.LENGTH_SHORT).show();

		} catch (JSONException e) {
			e.printStackTrace();
		}
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
			actionbar.setTitle("매입목록 상세보기");
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.purchase_list_detail_view, menu);
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
