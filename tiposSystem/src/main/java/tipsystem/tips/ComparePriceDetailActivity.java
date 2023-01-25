package tipsystem.tips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.JsonHelper;
import tipsystem.utils.MSSQL;
import tipsystem.utils.StringFormat;
import tipsystem.utils.TIPOS;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class ComparePriceDetailActivity extends Activity {

	ListView m_listPriceDetail;
	TextView m_barcodeTxt;
	TextView m_gNameTxt;
	TextView m_purPriTxt;
	TextView m_sellPriTxt;

	CompareListAdapter m_adapter; 
	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();	
	
	double m_Pur_Pri =0;
	double m_Sell_Pri =0;
	String m_Shop_Area ="";
	String m_customer_code ="";
	
	private ProgressDialog dialog;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_compare_price_detail);
		
		Intent intent = getIntent();
		HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra("fillMaps");   
		String BarCode = hashMap.get("BarCode");
		String G_Name = hashMap.get("G_Name");
		String Pur_Pri = hashMap.get("o_Pur_Pri");
		String Sell_Pri = hashMap.get("o_Sell_Pri");
		m_Shop_Area = intent.getStringExtra("Shop_Area");
		m_customer_code = intent.getStringExtra("OFFICE_CODE");
		m_Pur_Pri = (double)Double.valueOf(Pur_Pri);
		m_Sell_Pri = (double)Double.valueOf(Sell_Pri);
				
		m_listPriceDetail= (ListView)findViewById(R.id.listviewPriceDetailList);

		Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
		TextView textView = (TextView) findViewById(R.id.textView1);
		textView.setTypeface(typeface);
		textView = (TextView) findViewById(R.id.textView3);
		textView.setTypeface(typeface);
		      
		m_barcodeTxt = (TextView) findViewById(R.id.textView4);
		m_barcodeTxt.setTypeface(typeface);
		m_barcodeTxt.setText(BarCode);
		
		m_gNameTxt = (TextView) findViewById(R.id.textView2);
		m_gNameTxt.setTypeface(typeface);
		m_gNameTxt.setText(G_Name);
		
		m_purPriTxt = (TextView) findViewById(R.id.textView6);
		m_purPriTxt.setTypeface(typeface);
		m_purPriTxt.setText(StringFormat.convertToNumberFormat(Pur_Pri));
		
		m_sellPriTxt = (TextView) findViewById(R.id.textView8);
		m_sellPriTxt.setTypeface(typeface);
		m_sellPriTxt.setText(StringFormat.convertToNumberFormat(Sell_Pri));
		
		comparePrice(BarCode, G_Name);
	}
	
	private void comparePrice(String BarCode, String G_Name) {
		
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();

 		String query = "";
 		query = "SELECT A.Pur_Pri, A.Sell_Pri, B.Shop_Area, B.Shop_Size FROM Goods A inner join V_OFFICE_USER B "
 				+ " on A.Office_Code = B.Sto_CD"
 				+ " WHERE A.BarCode = '" + BarCode + "' ;";

 		query = "SELECT A.Shop_Area, A.Shop_Size, B.Pur_Pri, B.Sell_Pri"
 				+ " FROM V_OFFICE_USER A, GOODS B"
 				+ " WHERE A.STO_CD=B.OFFICE_CODE" 
 				//+ " AND STO_CD<>'0000001'" 
 				+ " AND B.BARCODE='" + BarCode + "'"
 				+ " AND B.Pur_Pri>0 AND B.Sell_Pri>0";
 		
 		if (!m_Shop_Area.equals("전체")) query	+= " AND A.SHOP_AREA='"+m_Shop_Area+"'";
 		query	+= " ORDER BY EDITDATE DESC";
 		
 		new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				if (results.length() > 0) {
					updateListView(results);
		            Toast.makeText(getApplicationContext(), "조회 완료", Toast.LENGTH_SHORT).show();
				}
				else {
		            Toast.makeText(getApplicationContext(), "상품을 검색할수 없습니다", Toast.LENGTH_SHORT).show();					
				}
			}
			// 2022.05.26.본사서버 IP변경
		}).execute(TIPOS.HOST_SERVER_IP+":"+TIPOS.HOST_SERVER_PORT, TIPOS.HOST_SERVER_DB, TIPOS.HOST_SERVER_ID, TIPOS.HOST_SERVER_PW, query);
 		
	}
	
	public void updateListView(JSONArray results) {
		
        if (!mfillMaps.isEmpty()) mfillMaps.clear();
        
        for (int i = 0; i < results.length(); i++) {
        	try {
            	JSONObject json = results.getJSONObject(i);

    			double Pur_Pri1 = m_Pur_Pri;
    			double Pur_Pri2 = json.getDouble("Pur_Pri");
    			double Sell_Pri1 = m_Sell_Pri;
    			double Sell_Pri2 = json.getDouble("Sell_Pri");

            	HashMap<String, String> map = JsonHelper.toStringHashMap(json);

	            String Pur_Pri = map.get("Pur_Pri");	
	            if(Pur_Pri1>Pur_Pri2) Pur_Pri = "+";
	            else if(Pur_Pri1==Pur_Pri2) Pur_Pri = "=";
	            else Pur_Pri = "-";

	            double Pur_Pri_dif = Pur_Pri1-Pur_Pri2;
	            if (Pur_Pri_dif<0) Pur_Pri_dif =Pur_Pri_dif*-1.0;
	            
	            map.put("Pur_Pri_dif", String.format("%.2f", Pur_Pri_dif));
	            map.put("Pur_Pri_inc", Pur_Pri);
	            
	            String Sell_Pri = map.get("Sell_Pri");
	            if(Sell_Pri1>Sell_Pri2) Sell_Pri = "+";
	            else if(Sell_Pri1==Sell_Pri2) Sell_Pri = "=";
	            else Sell_Pri = "-";
	            
	            double Sell_Pri_dif = Sell_Pri1-Sell_Pri2;
	            if (Sell_Pri_dif<0) Sell_Pri_dif =Sell_Pri_dif*-1.0;
	            
	            map.put("Sell_Pri_dif", String.format("%.0f", Sell_Pri_dif));
	            map.put("Sell_Pri_inc", Sell_Pri);
	            
	            map.put("BarCode", json.getString("Shop_Area"));
	            map.put("G_Name", json.getString("Shop_Size"));
	            
	            
	            mfillMaps.add(map);
		 
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
        
		String[] from = new String[] {"Shop_Area", "Shop_Size", "Pur_Pri", "Sell_Pri"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4};
        
        m_adapter = new CompareListAdapter(this,  R.layout. activity_listview_compare_list, mfillMaps);
        m_listPriceDetail.setAdapter(m_adapter);
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
			actionbar.setTitle("가격비교");
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.compare_price_detail, menu);
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
