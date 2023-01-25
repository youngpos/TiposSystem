package tipsystem.tips;

/*
 * 기본관리 -> 상품관리 -> 검색버튼 (상품목록)
 * TODO : 매출관리 상품검색 -> 검색 목록
 * */

 
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;
import android.support.v4.app.NavUtils;

public class ManageProductListActivity extends Activity {
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

	ListView m_cusList;
	SimpleAdapter m_adapter; 
	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

	String m_barcode = "";
	String m_gname = "";
	String m_office_code = "";
	String m_good_use = "";
	
    // loading bar
	private ProgressDialog dialog;

    // loading more in listview
    int currentVisibleItemCount;
    private boolean isEnd = false;
    private OnScrollListener customScrollListener = new OnScrollListener() {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            currentVisibleItemCount = visibleItemCount;

            if((firstVisibleItem + visibleItemCount) == totalItemCount && firstVisibleItem != 0) 
            	isEnd = true;            
            else 
            	isEnd = false;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (isEnd && currentVisibleItemCount > 0 && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				doSearch();
		    }
        }
    };
      
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_product_list);

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

        if(getIntent().hasExtra("barcode")){
        	m_barcode = getIntent().getStringExtra("barcode");
        }
        if(getIntent().hasExtra("good_use")){
        	m_good_use = getIntent().getStringExtra("good_use");
        }
        if(getIntent().hasExtra("gname")){
        	m_gname = getIntent().getStringExtra("gname");
        }        
        
        //if(getIntent().getStringExtra("Office_Code") != null) {
        if(getIntent().hasExtra("Office_Code")){
        	m_office_code = getIntent().getStringExtra("Office_Code");
        }
        
		m_cusList= (ListView)findViewById(R.id.listviewManageProductList);
		m_cusList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	returnResultData(position);
            }
        });
		
		m_cusList.setOnScrollListener(customScrollListener);

    	String[] from = new String[] {"BarCode", "G_Name", "Bus_Name", "Pur_Pri", "Sell_Pri", "Sale_Sell", "Profit_Rate", "면과세", "Real_Sto"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8, R.id.item9 };
        m_adapter = new SimpleAdapter(this, mfillMaps, R.layout. activity_listview_product_list, from, to);
        m_cusList.setAdapter(m_adapter);
        
        doSearch();
	}

	private void returnResultData(int position) {

		Intent intent = new Intent();
		intent.putExtra("fillmaps", mfillMaps.get(position)); 
		this.setResult(RESULT_OK, intent);
		finish();
	}
    
	private void updateListView(JSONArray results) {
       
        for (int i = 0; i < results.length(); i++) {
        	
        	try {
            	JSONObject json = results.getJSONObject(i);
            	HashMap<String, String> map = JsonHelper.toStringHashMap(json);            	
				map.put("Pur_Pri", StringFormat.convertToNumberFormat(map.get("Pur_Pri")));
				map.put("Sell_Pri", StringFormat.convertToNumberFormat(map.get("Sell_Pri")));
				//면과세 표시							
            	if(map.get("Tax_YN").equals("0")){
            		map.put("면과세", "면세");
            	}else{
            		map.put("면과세", "과세");
            	}
            	map.put("Sale_Sell", StringFormat.convertToIntNumberFormat(map.get("Sale_Sell")));
	            mfillMaps.add(map);
		 
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
        m_adapter.notifyDataSetChanged();
    }
    
	private void doSearch() {

    	String index = String.valueOf(mfillMaps.size());
		String query = "";    
		query = " SELECT TOP 50 BarCode, G_Name, Tax_YN, Pur_Pri, Sell_Pri, S_Name, Bus_Name, Profit_Rate, Sale_Sell, Real_Sto FROM Goods "
			      + " WHERE Goods_Use='1' AND Pur_Use='1' AND "
			      + " BarCode like '"+m_barcode+"%' AND G_Name like '%"+m_gname+"%' AND Bus_Code like '%"+m_office_code+"%' AND "
			      + " BarCode NOT IN(SELECT TOP "+index+" BarCode FROM Goods "
			      + " where BarCode like '%"+m_barcode +"%' AND G_Name like '%"+m_gname+"%' AND Bus_Code like '%"+m_office_code+"%' "
			      + " Order By BarCode ASC) Order By BarCode ASC ";

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
			actionbar.setTitle("전체 상품 조회");
			
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
}

