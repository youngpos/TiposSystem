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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class CustomerPurchasePaymentDetailActivity extends Activity {

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

	ListView m_listDetailView;
	
	TextView m_period1;
	TextView m_period2;
	TextView m_customerCode;
	TextView m_customerName;
	TextView m_realPurchase;
	TextView m_realPayment;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customer_purchase_payment_detail);
		
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
        
		m_listDetailView= (ListView)findViewById(R.id.listviewCustomerPurchasePaymentDetailViewList);
		
		Intent intent = getIntent();
		
		String period1 = intent.getExtras().getString("PERIOD1");
		String period2 = intent.getExtras().getString("PERIOD2");

		String officeCode = intent.getExtras().getString("OFFICE_CODE");
		String customerName = intent.getExtras().getString("OFFICE_NAME");
		String PUR = intent.getExtras().getString("PUR");
		String SALE = intent.getExtras().getString("SALE");
		
		m_period1 = (TextView)findViewById(R.id.textViewPeriod1);
		m_period2 = (TextView)findViewById(R.id.textViewPeriod2);
		m_customerName = (TextView)findViewById(R.id.textViewCustomerName);
		m_realPurchase = (TextView)findViewById(R.id.textViewRealPurchase);
		m_realPayment = (TextView)findViewById(R.id.textViewRealPayment);
		
		m_period1.setText(period1);
		m_period2.setText(period2);
		
		m_customerName.setText(customerName);
		
		int year1 = Integer.parseInt(period1.substring(0, 4));
 		int year2 = Integer.parseInt(period2.substring(0, 4));
 		
 		int month1 = Integer.parseInt(period1.substring(5, 7));
 		int month2 = Integer.parseInt(period2.substring(5, 7));
 		
		m_realPurchase.setText(PUR);
		m_realPayment.setText(SALE);

    	String query = "";

		query = "select "
				+ " a.BARCODE,a.G_NAME, "
				+ " isnull(b.In_count,0) '매입수량',  "
				+ " isnull(b.In_Pri,0) '순매입',  "
				+ " isnull(c.sale_count,0) '매출수량',  "
				+ " isnull(c.sell_pri,0) '순매출' "
				+ " from Goods a  "
				+ " left join (  "
				+ "  select barcode,  "
				+ "  sum(In_count) In_count,  "
				+ "  sum(In_Pri) In_Pri  "
				+ "  from (  ";

		for ( int y = year1; y <= year2; y++ ) {
			int m1 = 1, m2 = 12;
			if (y == year1) m1 = month1;
			if (y == year2) m2 = month2;
			for ( int m = m1; m <= m2; m++ ) {

				String tableDate = String.format("%04d%02d", y, m);
				query += "   select barcode,  "
						+ "        sum(In_count) In_count,  "
						+ "        sum(In_Pri) In_Pri  "
						+ "        From InD_"+tableDate+" "
						+ "        where in_date between '" + period1 + "' and '" + period2 + "' "
						+ "        AND office_code='"+officeCode+"'  "
						+ "        group by barcode ";

				query += " union all ";
			}
		}
		query = query.substring(0, query.length()-11);
		query += "  ) x  "
				+ "  group by barcode  "
				+ " ) b  on a.barcode=b.barcode  "
				+ " left join (  "
				+ "  select barcode,  "
				+ "  sum(sale_count) sale_count,  "
				+ "  sum(sell_pri) sell_pri "
				+ "  from (         ";

		for ( int y = year1; y <= year2; y++ ) {
			int m1 = 1, m2 = 12;
			if (y == year1) m1 = month1;
			if (y == year2) m2 = month2;
			for ( int m = m1; m <= m2; m++ ) {

				String tableDate = String.format("%04d%02d", y, m);
				query += "   select barcode,  "
				+ "        sum(sale_count) sale_count,  "
				+ "        sum(TSell_Pri-TSell_RePri) sell_pri "
				+ "        From SaD_"+tableDate+" "
				+ "        where sale_date between '" + period1 + "' and '" + period2 + "' AND Card_YN='0'  "
				+ "        AND office_code='"+officeCode+"' "
				+ "        group by barcode ";

				query += " union all ";
			}
		}
		query = query.substring(0, query.length()-11);
		query += "  ) x  "
				+ "  group by barcode  "
				+ " ) c on a.barcode=c.barcode  "
				+ " where 1=1 "
				+ " and isnull(b.In_count,0)<>0 or isnull(c.sale_count,0)<>0 or isnull(b.In_Pri,0)<>0 or isnull(c.sell_pri,0)<>0";

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
			if ( results.length() > 0 ) { 
				// create the grid item mapping
				String[] from = new String[] {"BARCODE", "G_NAME", "순매입", "순매출"};
				int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4 };
				
				// prepare the list of all records
				List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
 			
				for(int i = 0; i < results.length() ; i++) {

					JSONObject son = results.getJSONObject(i);
					HashMap<String, String> map = JsonHelper.toStringHashMap(son);
					map.put("순매입", StringFormat.convertToNumberFormat(map.get("순매입")));	
					map.put("순매출", StringFormat.convertToNumberFormat(map.get("순매출")));	
					fillMaps.add(map);
				}
			
				// fill in the grid_item layout
				SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.activity_listview_item4_3, from, to);
				
				m_listDetailView.setAdapter(adapter);
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
	//		LinearLayout custom_action_bar = (LinearLayout) View.inflate(this, R.layout.activity_custom_actionbar, null);
	//		actionbar.setCustomView(custom_action_bar);
	
			actionbar.setDisplayShowHomeEnabled(false);
			actionbar.setDisplayShowTitleEnabled(true);
			actionbar.setDisplayShowCustomEnabled(true);
			actionbar.setTitle("거래처 매입/매출 상세보기");
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.customer_purchase_payment_detail_view,
				menu);
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
