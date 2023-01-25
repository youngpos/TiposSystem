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
import tipsystem.utils.MSSQL2;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class ManageCustomerListActivity extends Activity{

	JSONArray m_results;
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

	String m_customer;
	String m_customername;
	String m_customergubun;
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
		setContentView(R.layout.activity_manage_customer_list);
		
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
        
        m_customer = getIntent().getStringExtra("customer");
        m_customername = getIntent().getStringExtra("customername");
        m_customergubun = "0";
        if(getIntent().hasExtra("customerGubun")){
        	m_customergubun = getIntent().getStringExtra("customerGubun");
        }
		m_cusList= (ListView)findViewById(R.id.listviewCustomerList);
		m_cusList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	            returnResultData(position);
            }
        });
		
		m_cusList.setOnScrollListener(customScrollListener);

		String[] from = new String[] {"Office_Code", "Office_Name", "Office_Sec"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3 };
        m_adapter = new SimpleAdapter(this, mfillMaps, R.layout. activity_listview_customer_list, from, to);
        m_cusList.setAdapter(m_adapter);
        
		doSearch();
	}

	private void returnResultData(int position) {

		Intent intent = new Intent();
		intent.putExtra("fillmaps", mfillMaps.get(position)); //HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra("fillmaps");
		this.setResult(RESULT_OK, intent);
		finish();
	}

	private void updateListView(JSONArray results) {
        
        for (int i = 0; i < results.length(); i++) {
        	
        	try {
            	JSONObject json = results.getJSONObject(i);
            	HashMap<String, String> map = JsonHelper.toStringHashMap(json);
	            String section = map.get("Office_Sec");	            
	            if(section.equals("1")) section = "매입거래처";
	            else if(section.equals("2")) section = "수수료거래처";
	            else if(section.equals("3")) section = "매출거래처";
	            map.put("Office_Sec", section);
	            
	            mfillMaps.add(map);
		 
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
        m_adapter.notifyDataSetChanged();
    }

    //목록에서 재검색 시 필요한 함수
    public void doListSearch(View view){

		mfillMaps.clear();

		EditText cus_code = (EditText)findViewById(R.id.edittextofficecode);
		EditText cus_name = (EditText)findViewById(R.id.edittextofficename);

		m_customer = cus_code.getText().toString();
		m_customername = cus_name.getText().toString();

		doSearch();

	}


	//목록에서 재검색 시 필요한 함수
	public void doReNew(View view){

		EditText cus_name = (EditText)findViewById(R.id.edittextofficecode);
		EditText cus_code = (EditText)findViewById(R.id.edittextofficename);

		cus_name.setText("");
		cus_code.setText("");

	}



	// 조회 실행 함수 
    public void doSearch() {

    	// 쿼리 작성하기
    	String index = String.valueOf(mfillMaps.size());
		String query = "";
		//수수료매장 매출분석시 수수료 거래처만 검색 되어지게
		String query1 = " ";
		if(m_customergubun.equals("1")){
			query1 += "and office_sec='2' ";
		}
		
    	query = "SELECT TOP 50 * FROM Office_Manage "
    			+ " WHERE Office_Use = '1' AND Office_Code like '%"+ m_customer+"%' AND Office_Name like '%"+m_customername+"%' "+query1+" AND "
    			+ " Office_Code NOT IN(SELECT TOP " + index + " Office_Code FROM Office_Manage "
    			+ " WHERE Office_Use = '1' AND Office_Code like '%"+ m_customer+"%' And Office_Name like '%"+m_customername+"%' "+query1
    			+ " Order By Office_Code ASC) Order By Office_Code ASC;";

    	// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.show();
 		
	    // 콜백함수와 함께 실행
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
					m_adapter.notifyDataSetChanged();
					Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();							
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				m_adapter.notifyDataSetChanged();
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
			LinearLayout custom_action_bar = (LinearLayout) View.inflate(this, R.layout.activity_custom_actionbar, null);
			actionbar.setCustomView(custom_action_bar);
	
			actionbar.setDisplayShowHomeEnabled(false);
			actionbar.setDisplayShowTitleEnabled(false);
			actionbar.setDisplayShowCustomEnabled(true);
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
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
