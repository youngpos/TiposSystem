package tipsystem.tips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.StringFormat;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class PaymentDetailViewActivity extends Activity {
	
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

	ListView m_listPaymentView;
	
	TextView m_period1;
	TextView m_period2;
	TextView m_customerCode;
	TextView m_customerName;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_payment_detail_view);
		
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
		
		m_listPaymentView = (ListView)findViewById(R.id.listviewPaymentDetailViewList);
		
		Intent intent = getIntent();
		
		String period1 = intent.getExtras().getString("PERIOD1");
		String period2 = intent.getExtras().getString("PERIOD2");
		
		String customerName = intent.getExtras().getString("OFFICE_NAME");
		String customerCode = intent.getExtras().getString("OFFICE_CODE");
		
		m_period1 = (TextView)findViewById(R.id.textViewPeriod1);
		m_period2 = (TextView)findViewById(R.id.textViewPeriod2);
		m_customerName = (TextView)findViewById(R.id.textViewCustomerName);
		
		m_period1.setText(period1);
		m_period2.setText(period2);
		
		m_customerName.setText(customerName);

    	String query ="";
		query = "select * " 
    	    		+ " from OFFICE_SETTLEMENT " 
    	    		+ " where Office_Code='"+customerCode+"' and PRO_DATE between '" + period1 + "' and '" + period2 + "'";

		query = "Select In_Seq, Pro_Date, Sale_Code, '구분'=CASE WHEN GUBUN='1' THEN '입금' " 
				+ " WHEN GUBUN='2' THEN '매입'" 
				+ " WHEN GUBUN='3' THEN '지급'" 
				+ " WHEN GUBUN='4' THEN '반품'" 
				+ " WHEN GUBUN='5' THEN '선입금' END," 
				+ " 0 '이월', Buy_Pri '매입', Buy_RePri '반품', Sale_Pri '할인액', Sub_PrI '장려금', Pay_Pri '지급액', in_pri '입금', 0 '미지급액', Bigo" 
				+ " From OFFICE_SETTLEMENT " 
				+ " Where Pro_Date between '" + period1 + "'  AND '" + period2 + "' " 
				+ " AND Office_Code='"+customerCode+"' "
				+ " ORDER BY Pro_Date, In_Seq ;";
		
		// 콜백함수와 함께 실행
		new MSSQL(new MSSQL.MSSQLCallbackInterface() {
		
			@Override
			public void onRequestCompleted(JSONArray results) {
				setListItems(results);
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
		
		Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
        TextView textView = (TextView) findViewById(R.id.textView1);
        textView.setTypeface(typeface);
	}

	void setListItems(JSONArray results)
	{
		try {
			if ( results.length() > 0 ) {
				// create the grid item mapping
				String[] from = new String[] {"In_Seq", "Pro_Date", "Sale_Code", "구분", "이월", "매입", "반품", "할인액", "장려금", "지급액", "입금", "미지급액", "Bigo"};
				int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8,
										R.id.item9, R.id.item10, R.id.item11, R.id.item12, R.id.item13};
				
				// prepare the list of all records
				List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
 			
				for(int i = 0; i < results.length() ; i++) {

					JSONObject son = results.getJSONObject(i);
					HashMap<String, String> map = JsonHelper.toStringHashMap(son);
					
					//미지급액=(이월 +매입 +입금) - (반품 + 할인액 + 장려금 + 지급액 )
					double a = Double.valueOf(map.get("매입"));
					double b = Double.valueOf(map.get("반품"));
					double c = Double.valueOf(map.get("할인액"));
					double d = Double.valueOf(map.get("장려금"));
					double e = Double.valueOf(map.get("지급액"));
					double f = Double.valueOf(map.get("입금"));
					double g = Double.valueOf(map.get("이월"));					
					map.put("미지급액", String.valueOf(g+a+f-b-c-d-e));

					map.put("미지급액", StringFormat.convertToNumberFormat(map.get("미지급액")));	
					map.put("이월", StringFormat.convertToNumberFormat(map.get("이월")));	
					map.put("매입", StringFormat.convertToNumberFormat(map.get("매입")));	
					map.put("반품", StringFormat.convertToNumberFormat(map.get("반품")));	
					map.put("할인액", StringFormat.convertToNumberFormat(map.get("할인액")));	
					map.put("지급액", StringFormat.convertToNumberFormat(map.get("지급액")));	
					map.put("입금", StringFormat.convertToNumberFormat(map.get("입금")));	
					fillMaps.add(map);
				}	
				// fill in the grid_item layout
				SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.activity_listview_item13, from, to);				
				m_listPaymentView.setAdapter(adapter);		
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
			actionbar.setTitle("대금결제 상세보기");
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.payment_detail_view, menu);
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
