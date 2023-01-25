package tipsystem.tips;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.StringFormat;

public class PurchaseListActivity extends Activity implements OnItemClickListener,DatePickerDialog.OnDateSetListener{
	
	//검색 결과 돌려 받기
	private static final int CUSTOMER_MANAGER_REQUEST = 3;
	
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

	//날자 설정관련
	DatePicker m_datePicker;
	Button m_buttonSetDate;
		 
	SimpleDateFormat m_dateFormatter;
	Calendar m_dateCalender1;
	Calendar m_dateCalender2;
	NumberFormat m_numberFormat;
	
	//목록
	ListView m_listPurchaseList;
	
	//거래처 검색 버튼
	Button bottnSearch;
	EditText m_customerCode;
	EditText m_customerName;
	
	//신규 매입 등록 버튼
	Button buttonNewRegister;
	
	//리스트목록 필요한 것들
	SimpleAdapter adapter;
	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();
	HashMap<String, String> m_tempInNum = new HashMap<String, String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_purchase_list);
		
		// 매장 정보 불러 오기
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
        
        //목록 할당 하기
        m_listPurchaseList= (ListView)findViewById(R.id.listviewPurchaseList);
		
		//날자 설정 관련
		m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
		m_numberFormat = NumberFormat.getInstance();		
		m_dateCalender1 = Calendar.getInstance();
		m_dateCalender2 = Calendar.getInstance();
		
		m_buttonSetDate = (Button)findViewById(R.id.buttonSetDate);
		m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
		//날자 설정 관련 끝
		
		//거래처 검색 버튼 할당하기
		bottnSearch = (Button)findViewById(R.id.buttonSearch);		
		
		m_customerCode = (EditText)findViewById(R.id.editTextCustomerCode);
		m_customerName = (EditText)findViewById(R.id.editTextCustomerName);
		
		//신규 등록 버튼
		/*buttonNewRegister = (Button)findViewById(R.id.buttonSendRegister);
		buttonNewRegister.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				//매입신규 등록 화면
				Intent intent = new Intent(getApplicationContext(), PurchaseRegistActivity.class);				
		    	startActivity(intent);
			}
		});*/

		Button buttonsendclose = (Button)findViewById(R.id.buttonSendClose);
		buttonsendclose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		//리스트뷰 목록 생성 어댑터
		String[] from = new String[] {"In_Date", "In_Num", "Office_Code", "Office_Name", "In_Pri", "In_RePri", "Profit_Pri", "Profit_Rate" };		
		int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8,};
		
		adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_item4_16, from, to);		
		m_listPurchaseList.setAdapter(adapter);	
		
		m_listPurchaseList.setOnItemClickListener(this);


		
		/*if(m_customerCode.isFocusable()){
			//목록 검색 하기 
			serachPurchaseList();
		}	*/	
	}
	
	//목록 검색 하기 함수
	private void serachPurchaseList(){
		mfillMaps.removeAll(mfillMaps);
		
		String period = m_buttonSetDate.getText().toString();
		String customerCode = m_customerCode.getText().toString();
		
		if(customerCode.equals("")){
			customerCode = " ";
		}else{
			customerCode = " And Office_Code='"+customerCode+"' ";
		}
		
		String query = "";
	    
		int year1 = Integer.parseInt(period.substring(0, 4));
		int month1 = Integer.parseInt(period.substring(5, 7));
		
		String tableName = String.format("%04d%02d", year1, month1);		
		
		query = "Select * From int_"+tableName+" Where in_date='"+period+"' "+customerCode+" order by In_Seq ASC ";
		
		new MSSQL(new MSSQL.MSSQLCallbackInterface() {
			@Override
			public void onRequestCompleted(JSONArray results) {				
				updateCustomerList(results);
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	//리스트에 뿌리기
	private void updateCustomerList(JSONArray results){
		try {		
			if ( results.length() > 0 ) {
				HashMap<String, String> map = new HashMap<String, String>();
				for(int i = 0; i < results.length() ; i++) {
					JSONObject son = results.getJSONObject(i);
					map =JsonHelper.toStringHashMap(son);
					//"In_Date", "In_Num", "Office_Code", "Office_Name", "In_Pri", "In_RePri", "Profit_Pri", "Profit_Rate" 
					map.put("Profit_Pri", StringFormat.convertToIntNumberFormat(map.get("Profit_Pri")));					
					mfillMaps.add(map);
				}	
			}else{
				Toast.makeText(getApplicationContext(), "조회 결과가 없습니다.", Toast.LENGTH_SHORT);
			}
			adapter.notifyDataSetChanged();
		} catch (JSONException e) {
			e.printStackTrace();
		}		
	}	

	//날자 변경설정
	public void onClickSetDate(View view) {
		DatePickerDialog newDlg = new DatePickerDialog(this, this,
				m_dateCalender1.get(Calendar.YEAR),
				m_dateCalender1.get(Calendar.MONTH),
				m_dateCalender1.get(Calendar.DAY_OF_MONTH));
		 newDlg.show();
	}
	
	//다음 날자 선택
	public void onClickSetDatePrevious(View view) {
		m_dateCalender1.add(Calendar.DAY_OF_MONTH, -1);	
		m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);
		m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));	
				
		serachPurchaseList();
	}
	
	//이전날자 선택
	public void onClickSetDateNext(View view) {
		m_dateCalender1.add(Calendar.DAY_OF_MONTH, 1);
		m_dateCalender2.add(Calendar.DAY_OF_MONTH, 1);	
		m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));			
 		
		serachPurchaseList();
	}
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		
		m_dateCalender1.set(year, monthOfYear, dayOfMonth);
		m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));		
		m_dateCalender2.set(year, monthOfYear, dayOfMonth);
		m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);
		
		serachPurchaseList();
	}
	
	// 리스트 클릭시 넘길 값들
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		//"In_Date", "In_Num", "Office_Code", "Office_Name", "In_Pri", "In_RePri", "Profit_Pri", "Profit_Rate"
		
		m_tempInNum.putAll(mfillMaps.get(arg2));
				
		String in_num = m_tempInNum.get("In_Num");
		String in_date =  m_tempInNum.get("In_Date");
		String office_code = m_tempInNum.get("Office_Code");
		String office_name = m_tempInNum.get("Office_Name");
		String in_pri = m_tempInNum.get("In_Pri");
		String in_repri = m_tempInNum.get("In_RePri");
		String profit_pri = m_tempInNum.get("Profit_Pri");
		String profit_rate = m_tempInNum.get("Profit_Rate");
		
		Log.i("전표번호", in_num);
		
		Intent intent = new Intent(this, PurchaseDetailActivity.class);
		intent.putExtra("In_Num", in_num);
		intent.putExtra("Office_Code", office_code);
		intent.putExtra("In_Date", in_date);
		intent.putExtra("Office_Name", office_name);
		intent.putExtra("In_Pri", in_pri);
		intent.putExtra("In_RePri", in_repri);
		intent.putExtra("Profit_Pri", profit_pri);
		intent.putExtra("Profit_Rate", profit_rate);
		intent.putExtra("Gubun", "InT");
		startActivity(intent);
		
	}	
		
	public void onCustomerSearch(View view)
	{
		String customer = m_customerCode.getText().toString();
		String customername = m_customerName.getText().toString();
		Intent intent = new Intent(this, ManageCustomerListActivity.class);
		intent.putExtra("customer", customer);
		intent.putExtra("customername", customername);
    	startActivityForResult(intent, CUSTOMER_MANAGER_REQUEST);
	}
	
	//검색 결과 돌려 받기 입니다.
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{    
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){
			// 카메라 스캔을 통한 바코드 검색
			/*case ZBAR_SCANNER_REQUEST :
				if (resultCode == RESULT_OK) {
			        // Scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT)
			        // Type of the scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
			        Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();
			        //Toast.makeText(this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE), Toast.LENGTH_SHORT).show();
			        // The value of type indicates one of the symbols listed in Advanced Options below.
			       
			        String barcode = data.getStringExtra(ZBarConstants.SCAN_RESULT);
					m_textBarcode.setText(barcode);
					doQueryWithBarcode();
					
			    } else if(resultCode == RESULT_CANCELED) {
			        Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
			    }
				break;*/
			// 목록 검색을 통한 바코드 검색				
			/*case BARCODE_MANAGER_REQUEST :
				if(resultCode == RESULT_OK && data != null) {
					HashMap<String, String> hashMap = (HashMap<String, String>)data.getSerializableExtra("fillmaps");        	
					m_textBarcode.setText(hashMap.get("BarCode"));
		        	doQueryWithBarcode();
		        }
				break;*/
			case CUSTOMER_MANAGER_REQUEST :
				if(resultCode == RESULT_OK && data != null) {
					HashMap<String, String> hashMap = (HashMap<String, String>)data.getSerializableExtra("fillmaps");     	
					m_customerCode.setText(hashMap.get("Office_Code"));
					m_customerName.setText(hashMap.get("Office_Name"));
		        }
				break;
			}
	}
	
	
	public void showDialog(String msg) {

		AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();        
        
        /*new AlertDialog.Builder(MainActivity.this)
        .setTitle("암호를 입력하세요")
        .setMessage("문의:1600-1883")
        .setView(input)
        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String value = input.getText().toString();
                Log.i("passwd", value);
                if (value.equals("1883")) {

	                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
			    	startActivity(intent);	
                }
                else {
		            Toast.makeText(MainActivity.this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show();
                }
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Do nothing.
            }
        }).show();
        */
    }	
		
	@Override
	public void onResume(){
		
		serachPurchaseList();
		super.onResume();
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
			actionbar.setTitle("매입목록");
			
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
