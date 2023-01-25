package tipsystem.tips;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;

public class PurchasePaymentStatusActivity extends Activity implements OnItemClickListener, 
																		//OnDateChangeListener,
																		OnTabChangeListener,
																		DatePickerDialog.OnDateSetListener {
	private static final int ZBAR_SCANNER_REQUEST = 0;
	private static final int BARCODE_MANAGER_REQUEST = 2;
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

	/* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
	int m_KeypadType = 0;

	TabHost m_tabHost;
	
	ListView m_listPurchaseTab1;
	ListView m_listPurchaseTab2;
	ListView m_listPurchaseTab3;

	SimpleAdapter adapter1;
	SimpleAdapter adapter2;
	SimpleAdapter adapter3;
	List<HashMap<String, String>> mfillMaps1 = new ArrayList<HashMap<String, String>>();
	List<HashMap<String, String>> mfillMaps2 = new ArrayList<HashMap<String, String>>();
	List<HashMap<String, String>> mfillMaps3 = new ArrayList<HashMap<String, String>>();
	Button m_period1;
	Button m_period2;
	TextView m_barCode;
	TextView m_productName;
	TextView m_customerCode;
	TextView m_customerName;
	
	CalendarView m_calendar;
		
	DatePicker m_datePicker;
	
	SimpleDateFormat m_dateFormatter;
	Calendar m_dateCalender1;
	Calendar m_dateCalender2;
	
	int m_dateMode=0;
	String m_rBarCode = null;
	
	int m_queryCount1 = 0;
	int m_queryCount2 = 0;
	int m_queryCount3 = 0;
	
	Spinner m_spinnerOfficeGT;	
	
	ProgressDialog dialog;
	NumberFormat m_numberFormat;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_purchase_payment_status);
		
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


			String OFFICE_CODE = m_shop.getString("OFFICE_CODE");

			/* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
			m_KeypadType = LocalStorage.getInt(this, "KeypadType:" + OFFICE_CODE);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		m_period1 = (Button) findViewById(R.id.buttonSetDate1); 
		m_period2 = (Button) findViewById(R.id.buttonSetDate2);
		m_barCode = (TextView) findViewById(R.id.editTextBarcode);

		// 김영목 2020.09.14. 키패드입력 옵션 설정에 따라 적용 추가
		if (m_KeypadType == 0) { // 숫자키패드
			m_barCode.setInputType(2);
		}
		if (m_KeypadType == 1) { // 문자키패드
			m_barCode.setInputType(145); //textVisiblePassword ) 0x00000091
		}

		m_productName = (TextView) findViewById(R.id.editTextProductName);
		m_customerCode = (TextView) findViewById(R.id.editTextCustomerCode);
		m_customerName = (TextView) findViewById(R.id.editTextCustomerName);
		
		m_listPurchaseTab1= (ListView)findViewById(R.id.listviewPurchaseListTab1);
		m_listPurchaseTab2= (ListView)findViewById(R.id.listviewPurchaseListTab2);
		m_listPurchaseTab3= (ListView)findViewById(R.id.listviewPurchaseListTab3);
		
		m_listPurchaseTab1.setOnItemClickListener(this);
		m_listPurchaseTab2.setOnItemClickListener(this);
		m_listPurchaseTab3.setOnItemClickListener(this);		
		
		String[] from1 = new String[] {"In_Num", "In_Date", "Office_Name", "In_Pri"};
		int[] to1 = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4};
			
		adapter1 = new SimpleAdapter(this, mfillMaps1, R.layout.activity_listview_item4_7, from1, to1);		
		m_listPurchaseTab1.setAdapter(adapter1);	
		
		String[] from2= new String[] {"Office_Code", "Office_Name", "이월", "지급금액", "미지급금액"};
		int[] to2 = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5};
			
		adapter2 = new SimpleAdapter(this, mfillMaps2, R.layout.activity_listview_item5_6, from2, to2);		
		m_listPurchaseTab2.setAdapter(adapter2);	

	    String[] from3 = new String[] {"Office_Code", "Office_Name", "순매입", "순매출"};
		int[] to3 = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4};

		adapter3 = new SimpleAdapter(this, mfillMaps3, R.layout.activity_listview_item4_3, from3, to3);		
		m_listPurchaseTab3.setAdapter(adapter3);

		m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
		m_numberFormat = NumberFormat.getInstance();		
		
		m_dateCalender1 = Calendar.getInstance();
		m_dateCalender2 = Calendar.getInstance();
				
		m_period1.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
		m_period2.setText(m_dateFormatter.format(m_dateCalender2.getTime()));		
		
		//그룹목록 설정		
		m_spinnerOfficeGT = (Spinner)findViewById(R.id.spinnerOfficeGroupType);
				
		
		m_tabHost = (TabHost) findViewById(R.id.tabhostPurchasePaymentStatus);
        m_tabHost.setup();
        m_tabHost.setOnTabChangedListener(this);
             
        TabHost.TabSpec spec = m_tabHost.newTabSpec("tag1");
        
        spec.setContent(R.id.tab1);
        spec.setIndicator("매입목록");
        m_tabHost.addTab(spec);
        
        spec = m_tabHost.newTabSpec("tag2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("결제현황");
        m_tabHost.addTab(spec);
        
        spec = m_tabHost.newTabSpec("tag3");
        spec.setContent(R.id.tab3);
        spec.setIndicator("매입/매출");
        m_tabHost.addTab(spec);

		//----------------------------------------//
		// 탭 속성 정의
		//----------------------------------------//
		//https://www.tabnine.com/code/java/methods/android.widget.TabHost/getTabWidget
		// 2022.04.06. 탭 텍스트 색상 변경
		for(int i=0;i<m_tabHost.getTabWidget().getChildCount();i++){
			TextView tv = (TextView) m_tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
			tv.setTextColor(Color.parseColor("#ffffff"));
		}
		//----------------------------------------//

        m_tabHost.setCurrentTab(0);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
        TextView textView = (TextView) findViewById(R.id.textView2);
        textView.setTypeface(typeface);
        
        textView = (TextView) findViewById(R.id.textView3);
        textView.setTypeface(typeface);
        
        /*textView = (TextView) findViewById(R.id.textView4);
        textView.setTypeface(typeface);
        
        textView = (TextView) findViewById(R.id.textView5);
        textView.setTypeface(typeface);
        
        textView = (TextView) findViewById(R.id.textView6);
        textView.setTypeface(typeface);*/
        
        setGroupType(); //그룹설정
        
	}	
	
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		if ( m_listPurchaseTab1.getId() == arg0.getId() )
		{
			Intent intent = new Intent(this, PurchaseListDetailViewActivity.class);
			
			String In_Num = mfillMaps1.get(arg2).get("In_Num");
			String In_Date = mfillMaps1.get(arg2).get("In_Date");
			String name = ((TextView) arg1.findViewById(R.id.item3)).getText().toString();

	    	intent.putExtra("In_Num", In_Num);
	    	intent.putExtra("In_Date", In_Date);
	    	intent.putExtra("OFFICE_NAME", name);
	    	startActivity(intent);	
		}
		else if ( m_listPurchaseTab2.getId() == arg0.getId() )
		{
			Intent intent = new Intent(this, PaymentDetailViewActivity.class);
			
			String period1 = m_period1.getText().toString();
			String period2 = m_period2.getText().toString();

			String officeCode = ((TextView) arg1.findViewById(R.id.item1)).getText().toString();
			String officeName = ((TextView) arg1.findViewById(R.id.item2)).getText().toString();
			
	    	intent.putExtra("PERIOD1", period1);
	    	intent.putExtra("PERIOD2", period2);
	    	intent.putExtra("OFFICE_NAME", officeName);
	    	intent.putExtra("OFFICE_CODE", officeCode);
	    	
	    	startActivity(intent);	
		}
		else if ( m_listPurchaseTab3.getId() == arg0.getId() )
		{
			Intent intent = new Intent(this, CustomerPurchasePaymentDetailActivity.class);
			String period1 = m_period1.getText().toString();
			String period2 = m_period2.getText().toString();

			String officeCode = ((TextView) arg1.findViewById(R.id.item1)).getText().toString();
			String officeName = ((TextView) arg1.findViewById(R.id.item2)).getText().toString();
			String pur = ((TextView) arg1.findViewById(R.id.item3)).getText().toString();
			String sale = ((TextView) arg1.findViewById(R.id.item4)).getText().toString();
			
	    	intent.putExtra("PERIOD1", period1);
	    	intent.putExtra("PERIOD2", period2);
	    	intent.putExtra("OFFICE_NAME", officeName);
	    	intent.putExtra("OFFICE_CODE", officeCode);
	    	intent.putExtra("PUR", pur);
	    	intent.putExtra("SALE", sale);
	    	
	    	startActivity(intent);	
		}
	}
	
	public void OnClickRenew(View v) {

		m_barCode.setText("");
		m_productName.setText("");
		m_customerCode.setText("");
		m_customerName.setText("");
		
		m_spinnerOfficeGT.setSelection(0);
	}
	
	public void OnClickSearch(View v) {
		
		doQuery();
	};
	
	public void doQuery() {

 		switch (m_tabHost.getCurrentTab())
 		{
	 		case 0:
	 			queryListForTab1(); break;
	 		case 1:
	 			queryListForTab2(); break;
	 		case 2:
	 			queryListForTab3(); break;
 		}
	}

	@Override
	public void onTabChanged(String tabId) {
		
	}

	public void onClickSetDate1(View v) {
		
		DatePickerDialog newDlg = new DatePickerDialog(this, this,
				m_dateCalender1.get(Calendar.YEAR),
				m_dateCalender1.get(Calendar.MONTH),
				m_dateCalender1.get(Calendar.DAY_OF_MONTH));
		 newDlg.show();
		
		 m_dateMode = 1;
	};
	
	public void onClickSetDate2(View v) {

		DatePickerDialog newDlg = new DatePickerDialog(this, this, 
				m_dateCalender2.get(Calendar.YEAR),
				m_dateCalender2.get(Calendar.MONTH),
				m_dateCalender2.get(Calendar.DAY_OF_MONTH));		
		newDlg.show();
		
		m_dateMode = 2;
	};
	
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		
		if ( m_dateMode == 1 ) {
			m_dateCalender1.set(year, monthOfYear, dayOfMonth);
			m_period1.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
		}
		else if ( m_dateMode == 2 ) {
			m_dateCalender2.set(year, monthOfYear, dayOfMonth);
			m_period2.setText(m_dateFormatter.format(m_dateCalender2.getTime()));
		}
		
		m_dateMode = 0;		
	}

	private void queryListForTab1()
	{
		mfillMaps1.removeAll(mfillMaps1);
		
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();
		String barCode = m_barCode.getText().toString();
		String productName = m_productName.getText().toString();
		String customerCode = m_customerCode.getText().toString();
		String customerName = m_customerName.getText().toString();
		
		//그룹검색 추가
		int officeGroup = m_spinnerOfficeGT.getSelectedItemPosition();
		String officeGT = "";
		if( 0 != officeGroup ){ // 그룹
			/*int a = 65; // A그룹
			for(int i = 1; i <= 12 ; i++){				
				if ( i == officeGroup){
					officeGT = "And A.O_Grade = '"+(char)a+"' ";
				}
				a++;
			}*/
			officeGT = (String)m_spinnerOfficeGT.getSelectedItem();
			officeGT = "And E.O_Grade = '"+officeGT+"' ";		
		}
		
		
		String query = "";
	    
		int year1 = Integer.parseInt(period1.substring(0, 4));
		int month1 = Integer.parseInt(period1.substring(5, 7));
		
		int year2 = Integer.parseInt(period2.substring(0, 4));
		int month2 = Integer.parseInt(period2.substring(5, 7));

		query = "SELECT In_Num, In_Date, OFFICE_CODE, Office_Name, In_Pri"
				+ " FROM ( ";
		for ( int y = year1; y <= year2; y++ ) {
			int m1 = 1, m2 = 12;
			if (y == year1) m1 = month1;
			if (y == year2) m2 = month2;
			for ( int m = m1; m <= m2; m++ ) {
				
				String tableName = String.format("%04d%02d", y, m);
				
				query += " SELECT B.In_Num, B.In_Date, B.OFFICE_CODE,B.Office_Name, B.In_Pri"
						+ " FROM INT_"+tableName+" B INNER JOIN IND_"+tableName+" C "
						+ " ON B.IN_NUM=C.IN_NUM "
						+ " LEFT JOIN GOODS D "
						+ "  ON C.BARCODE=D.BARCODE "
						+ " LEFT JOIN OFFICE_Manage E "
						+ "  ON C.OFFICE_CODE=E.OFFICE_CODE "
						+ "  WHERE B.IN_DATE >= '" + period1 + "' AND B.IN_DATE <= '" + period2 + "'  "
						+ "  AND B.OFFICE_CODE LIKE '%" + customerCode + "%' AND B.OFFICE_NAME LIKE '%" + customerName + "%' "						
						+ "  AND C.BARCODE LIKE '%" + barCode + "%' AND D.G_NAME LIKE '%" + productName + "%' "
						+ officeGT;
						
 
				query += " union all ";
			}			
		}
		query = query.substring(0, query.length()-11);		
		query += ") A "
				+ " GROUP BY IN_NUM, IN_DATE, OFFICE_CODE, OFFICE_NAME, IN_PRI "
				+ " ORDER BY IN_NUM DESC";
		
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();

		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {

				dialog.dismiss();
				dialog.cancel();
				if ( results.length() > 0 ) 
					updateListForTab1(results);	
				adapter1.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();	
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				adapter1.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}

	private void updateListForTab1(JSONArray results) {		
		try { 			
			for(int index = 0; index < results.length() ; index++) {
				HashMap<String, String> map = JsonHelper.toStringHashMap(results.getJSONObject(index));	
				map.put("In_Pri", StringFormat.convertToNumberFormat(map.get("In_Pri")));			
				mfillMaps1.add(map);			
			}
		} catch(JSONException e) {
			e.printStackTrace();			
		}
	}
	
	private void queryListForTab2()
	{
		mfillMaps2.removeAll(mfillMaps2);
		
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();
		String barCode = m_barCode.getText().toString();
		String customerCode = m_customerCode.getText().toString();
		String customerName = m_customerName.getText().toString();
		
		int officeGroup = m_spinnerOfficeGT.getSelectedItemPosition();
		String officeGT = "";
		if( 0 != officeGroup ){ // 그룹
			/*int a = 65; // A그룹
			for(int i = 1; i <= 12 ; i++){				
				if ( i == officeGroup){
					officeGT = "And A.O_Grade = '"+(char)a+"' ";
				}
				a++;
			}*/
			officeGT = (String)m_spinnerOfficeGT.getSelectedItem();
			officeGT = "And A.O_Grade = '"+officeGT+"' ";			
			
		}
		
		
		String query = "";

		query = "SELECT Office_Code, Office_Name, 이월, 지급금액, (이월+매입+입금)-(반품+할인액+장려금+지급금액) 미지급금액"
				+ " FROM (" 
					+ " SELECT A.OFFICE_CODE,A.OFFICE_NAME, " 
					+ " ISNULL(이월,0) 이월, " 
					+ " ISNULL(매입,0) 매입, " 
					+ " ISNULL(반품,0) 반품, " 
					+ " ISNULL(할인액,0) 할인액, " 
					+ " ISNULL(장려금,0) 장려금, " 
					+ " ISNULL(지급금액,0) 지급금액, "
					+ " ISNULL(입금,0) 입금 "
					+ " FROM OFFICE_MANAGE A LEFT JOIN ( " 
	 					+ " SELECT Office_Code, "  
	 					+ " Sum ((Buy_Pri + in_pri) - (Pay_Pri + Buy_RePri + Sale_Pri + Sub_Pri)) '이월' " 
	 					+ " From Office_Settlement " 
	 					+ " Where Pro_Date< '" + period1 + "' " 
	     				+ " Group by Office_Code " 
	 				+ " ) B ON A.OFFICE_CODE=B.OFFICE_CODE " 
					+ " LEFT JOIN ( " 
	 					+ " Select  Office_Code, Sum (Buy_Pri) '매입', Sum (Buy_RePri) '반품', Sum (Sale_Pri) '할인액', "
	 					+ " Sum (Sub_Pri) '장려금', Sum (Pay_Pri) '지급금액', Sum (in_pri) '입금' " 
	 					+ " From Office_Settlement " 
	 					+ " Where Pro_Date between '" + period1 + "' AND '" + period2 + "' " 
	 					+ " Group by Office_Code " 
					+ " ) C ON A.OFFICE_CODE=C.OFFICE_CODE " 
	 					+ "Where 1=1 "+officeGT
				+ " ) X " 
					+ " where Office_Code like '%"+customerCode+"%'"
					+ " AND Office_Name like '%"+customerName+"%'"
				+ " ORDER BY OFFICE_CODE ;";
		
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();

		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {

				dialog.dismiss();
				dialog.cancel();
				if ( results.length() > 0 ) updateListForTab2(results);
				adapter2.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();	
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				adapter2.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
		
	private void updateListForTab2(JSONArray results) {		
		try { 			
			for(int index = 0; index < results.length() ; index++) {
				HashMap<String, String> map = JsonHelper.toStringHashMap(results.getJSONObject(index));	
				map.put("이월", StringFormat.convertToNumberFormat(map.get("이월")));					
				map.put("지급금액", StringFormat.convertToNumberFormat(map.get("지급금액")));		
				map.put("미지급금액", StringFormat.convertToNumberFormat(map.get("미지급금액")));		
				mfillMaps2.add(map);				
			}
		} catch(JSONException e) {
			e.printStackTrace();
		}
	}
	
	private void queryListForTab3()
	{
		mfillMaps3.removeAll(mfillMaps3);
		
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();
		String barCode = m_barCode.getText().toString();
		String productName = m_productName.getText().toString();
		String customerCode = m_customerCode.getText().toString();
		String customerName = m_customerName.getText().toString();
		
		String query = "";
	    
		int year1 = Integer.parseInt(period1.substring(0, 4));
		int month1 = Integer.parseInt(period1.substring(5, 7));
		
		int year2 = Integer.parseInt(period2.substring(0, 4));
		int month2 = Integer.parseInt(period2.substring(5, 7));
		
		query = "select" 
				+ " a.Office_Code,a.Office_Name,  "
				+ " isnull(In_Pri,0) '순매입',  "
				+ " isnull(sell_pri,0) '순매출' "
				+ " from office_manage a  "
				+ " left join (  "
				+ "  select office_code,  "
				+ "  sum(In_Pri) In_Pri  "
				+ "  from (          ";
				
				for ( int y = year1; y <= year2; y++ ) {
					int m1 = 1, m2 = 12;
					if (y == year1) m1 = month1;
					if (y == year2) m2 = month2;
					for ( int m = m1; m <= m2; m++ ) {

						String tableDate = String.format("%04d%02d", y, m);
						query += "   select office_code,  "
								+ "        sum(In_Pri) In_Pri  "
								+ "        From InD_"+tableDate+" "
						 		+ "       where in_date between '" + period1 + "' and '" + period2 + "' "
						 		+ "       group by office_code  "
		  
								+ " union all ";
					}
				}

		query = query.substring(0, query.length()-11);
		query += "  ) x  "
				+ "  group by  office_code  "
				+ " ) b  on a.office_code=b.office_code  "
				+ " left join (  "
				+ "  select office_code,  "
				+ "  sum(sell_pri) sell_pri "
				+ "  from (          ";
				
				for ( int y = year1; y <= year2; y++ ) {
					int m1 = 1, m2 = 12;
					if (y == year1) m1 = month1;
					if (y == year2) m2 = month2;
					for ( int m = m1; m <= m2; m++ ) {

						String tableDate = String.format("%04d%02d", y, m);
						query += "   select office_code,  "
						 		+ "       sum(TSell_Pri-TSell_RePri) sell_pri "
						 		+ "       From SaD_"+tableDate+" "
						 		+ "       where sale_date between '" + period1 + "' and '" + period2 + "' AND Card_YN='0'  "
						 		+ "       group by office_code  "

								+ " union all ";
					}
				}

				query = query.substring(0, query.length()-11);
				query += "  ) x  "
				+ "  group by  office_code  "
				+ " ) c on a.office_code=c.office_code  "
				+ " Where 1=1  "
				+ " AND a.Office_Code like '%"+customerCode+"%' AND a.Office_Name like '%"+customerName+"%' ";
		
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();

		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {

				dialog.dismiss();
				dialog.cancel();
				if ( results.length() > 0 ) 
					updateListForTab3(results);
				adapter3.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();	
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				adapter3.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
		
	private void updateListForTab3(JSONArray results) {		
		try { 			
			for(int index = 0; index < results.length() ; index++) {
				HashMap<String, String> map = JsonHelper.toStringHashMap(results.getJSONObject(index));	
				map.put("순매입", StringFormat.convertToNumberFormat(map.get("순매입")));	
				map.put("순매출", StringFormat.convertToNumberFormat(map.get("순매출")));				
				mfillMaps3.add(map);				
			}
		} catch(JSONException e) {
			e.printStackTrace();			
		}
	}
    
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{    
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){
			// 카메라 스캔을 통한 바코드 검색
			case ZBAR_SCANNER_REQUEST :
				if (resultCode == RESULT_OK) {
			        // Scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT)
			        // Type of the scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
			        Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();
			        //Toast.makeText(this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE), Toast.LENGTH_SHORT).show();
			        // The value of type indicates one of the symbols listed in Advanced Options below.
			       
			        String barcode = data.getStringExtra(ZBarConstants.SCAN_RESULT);
			        m_barCode.setText(barcode);
					doQueryWithBarcode();
					
			    } else if(resultCode == RESULT_CANCELED) {
			        Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
			    }
				break;
			// 목록 검색을 통한 바코드 검색				
			case BARCODE_MANAGER_REQUEST :
				if(resultCode == RESULT_OK && data != null) {
					HashMap<String, String> hashMap = (HashMap<String, String>)data.getSerializableExtra("fillmaps");        	
					m_barCode.setText(hashMap.get("BarCode"));
		        	doQueryWithBarcode();
		        }
				break;
			case CUSTOMER_MANAGER_REQUEST :
				if(resultCode == RESULT_OK && data != null) {
					HashMap<String, String> hashMap = (HashMap<String, String>)data.getSerializableExtra("fillmaps");     	
					m_customerCode.setText(hashMap.get("Office_Code"));
					m_customerName.setText(hashMap.get("Office_Name"));
		        }
				break;
			}
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
	
	public void onBarcodeSearch(View view)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String list_value = pref.getString("prefSearchMethod", "");
        if (list_value.equals("camera")) {
			startCameraSearch();
        }
        else if (list_value.equals("list")) {
        	startProductList();
        }
        else {
        	// 바코드 검색 버튼 클릭시 나오는 목록 셋팅
    		final String[] option = new String[] { "목록", "카메라"};
    		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.user_select_dialog_item, option);
    		AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
    		builder.setTitle("Select Option");
    		
    		// 목록 선택시 이벤트 처리
    		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {

    				if(which == 0){ // 목록으로 조회할 경우
    					startProductList();
    				} else { // 스캔할 경우	
    					startCameraSearch();
    				}
    			}
    		}); 
    		builder.show();
        }
	}
	
	private void startProductList() {
		String barcode = m_barCode.getText().toString();
		String gname = m_productName.getText().toString();
		String officecode = m_customerCode.getText().toString();
		Intent intent = new Intent(this, ManageProductListActivity.class);
		intent.putExtra("barcode", barcode);
		intent.putExtra("gname", gname);
		intent.putExtra("Office_Code", officecode);	
    	startActivityForResult(intent, BARCODE_MANAGER_REQUEST);
	}
	
	private void startCameraSearch() {
		Intent intent = new Intent(this, ZBarScannerActivity.class);
    	startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
	} 
		
	// MSSQL
	// SQL QUERY 실행
	public void doQueryWithBarcode () {
		
		String query = "";
		String barcode = m_barCode.getText().toString();
		query = "SELECT * FROM Goods WHERE Barcode = '" + barcode + "';";
	
		if (barcode.equals("")) return;
		
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();

	    // 콜백함수와 함께 실행
	    new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				
				if (results.length() > 0) {
					try {						
						m_productName.setText(results.getJSONObject(0).getString("G_Name"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				else {
					Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
				}
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}    
	
	//그룹정보 불러오기
	private void setGroupType(){
		
		//거래처 그룹정보를 불러옵니다.
		String query = "Select distinct(O_grade) GRADE From OFFICE_MANAGE WHERE NOT O_grade IS NULL AND O_grade<>'' ";
		
	    // 콜백함수와 함께 실행
	    new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				
				ArrayList<String> group_list = new ArrayList<String>();
				group_list.add("그룹");				
				if(results.length()>0){
					try{
						int count = results.length();
						for(int i =0; i < count; i++){
							group_list.add(results.getJSONObject(i).getString("GRADE"));
						}
					}catch(JSONException e){
						e.printStackTrace();
					}
				}				
				ArrayAdapter <String>adapter = new ArrayAdapter<String>(
		                getApplicationContext(), // 현재화면의 제어권자						
		                android.R.layout.simple_spinner_item, // 레이아웃                
		                group_list); // 데이터
		        m_spinnerOfficeGT.setAdapter(adapter);		        
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
			actionbar.setTitle("매입/대금 결제현황");
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.purchase_payment_status, menu);
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
