package tipsystem.tips;

import java.text.NumberFormat;
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

import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class CustomerProductDetailInNewsActivity extends Activity {

	ListView m_listPurchaseDetail;
	
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

	NumberFormat m_numberFormat;
	ProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customer_product_detail_in_news);
		
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
		
		m_listPurchaseDetail= (ListView)findViewById(R.id.listviewCustomerProductDetailInNews);		
		m_numberFormat = NumberFormat.getInstance();
		
		Intent intent = getIntent();
		
		String period1 = intent.getExtras().getString("PERIOD1");
		String customerCode = intent.getExtras().getString("OFFICE_CODE");
		String customerName = intent.getExtras().getString("OFFICE_NAME");
		String card_yn = intent.getExtras().getString("CARDYN");
		 		
		executeQuery(period1, customerCode, customerName, card_yn);
	}


	private void setTabList1(List<HashMap<String, String>> fillMaps)
	{		
		 // create the grid item mapping
		String[] from = new String[] {"BarCode", "G_Name", "순매출수량", "순매출"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4 };
		
		// fill in the grid_item layout
		SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.activity_listview_item4_2, from, to);
		
		m_listPurchaseDetail.setAdapter(adapter);
	}
	
	private void executeQuery(String... urls)
	{
	    String period1 = urls[0];
		String customerCode = urls[1];
		String customerName = urls[2];
		String card_yn = urls[3];
		
		String query = "";
	    
		int year1 = Integer.parseInt(period1.substring(0, 4));
		int month1 = Integer.parseInt(period1.substring(5, 7));

		String tableName = String.format("%04d%02d", year1, month1);
		
//		query = "Select A.BarCode, A.G_Name, IsNull(Sum(A.Sale_Count), 0) '순매출수량', IsNull(Sum(A.TSell_Pri-A.TSell_RePri), 0) '순매출'"
//				+ " From SaD_"+tableName+" A "
//				+ " Where A.Sale_Date='" + period1 + "' And A.Office_Code = '" + customerCode + "' "+card_yn
//				+ " Group By A.BarCode, A.G_Name "
//				+ " Order by A.순매출수량 DESC ";

		/* 2021.10.07.김영목. SQL문 에러 수정 */
		query = "SELECT * FROM ( "
				+ "Select A.BarCode, A.G_Name, IsNull(Sum(A.Sale_Count), 0) '순매출수량', IsNull(Sum(A.TSell_Pri-A.TSell_RePri), 0) '순매출'"
				+ " From SaD_"+tableName+" A "
				+ " Where A.Sale_Date='" + period1 + "' And A.Office_Code = '" + customerCode + "' "+card_yn
				+ " Group By A.BarCode, A.G_Name "
				+ " ) A "
				+ " Order by A.순매출수량 DESC ";

		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();
 		
		new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				try {
					if ( results.length() > 0 ) {
				        List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
			 			
						for(int i = 0; i < results.length() ; i++) {
							JSONObject son = results.getJSONObject(i);
							HashMap<String, String> map = JsonHelper.toStringHashMap(son);

							map.put("순매출", StringFormat.convertToNumberFormat(map.get("순매출")));
							fillMaps.add(map);
						}	
						
						setTabList1(fillMaps);
						Toast.makeText(getApplicationContext(), "조회 완료" + results.length(), Toast.LENGTH_SHORT).show();
					
					}
	    		} catch (JSONException e) {
	    			e.printStackTrace();
	    		}
				
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}

	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		
			ActionBar actionbar = getActionBar();         
	//		LinearLayout custom_action_bar = (LinearLayout) View.inflate(this, R.layout.activity_custom_actionbar, null);
	//		actionbar.setCustomView(custom_action_bar);
	
			actionbar.setDisplayShowHomeEnabled(false);
			actionbar.setDisplayShowTitleEnabled(true);
			actionbar.setDisplayShowCustomEnabled(true);
			actionbar.setTitle("거래처 상품 상세보기");
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.customer_product_detail_in_news, menu);
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
