package tipsystem.tips;

//import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import com.dm.zbar.android.scanner.ZBarConstants;
//import com.dm.zbar.android.scanner.ZBarScannerActivity;


import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
//import android.preference.PreferenceManager;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
//import android.content.SharedPreferences;
//import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

import android.util.Log;

public class ManageSale_CategoriActivity extends Activity implements OnItemClickListener, 
															OnItemSelectedListener, 
															OnTabChangeListener,
															DatePickerDialog.OnDateSetListener{
	// TODO : ???????????? ????????????
	private static final int ZBAR_SCANNER_REQUEST = 0;
	//private static final int BARCODE_MANAGER_REQUEST = 2;
	private static final int CUSTOMER_MANAGER_REQUEST = 3;

	JSONObject m_shop;
	JSONObject m_userProfile;

	//----------------------------------------//
	// 2022.05.26.???????????? IP??????
	//----------------------------------------//
	String m_ip = "";
	String m_port = "";
	//----------------------------------------//
	// 2021.12.21. ??????DB IP,PW,DB ??????
	//----------------------------------------//
	String m_uuid = "";
	String m_uupw = "";
	String m_uudb = "";
	//----------------------------------------//

	String m_APP_USER_GRADE;
	String m_OFFICE_CODE;	// ????????????????????? ????????? ???????????????
	String m_OFFICE_NAME;	// ????????????????????? ????????? ???????????????
	private String Card_Yn = "A.Card_Yn = '0' AND"; // ?????? ????????? ?????? ?????? ??????
	
	TabHost m_tabHost;	
	ListView m_listSalesTab1;
	ListView m_listSalesTab2;
	ListView m_listSalesTab3;
	ListView m_listSalesTab4;
	
	SimpleAdapter adapter1;
	SimpleAdapter adapter2;
	SimpleAdapter adapter3;
	SimpleAdapter adapter4;

	List<HashMap<String, String>> mfillMaps1 = new ArrayList<HashMap<String, String>>();
	List<HashMap<String, String>> mfillMaps2 = new ArrayList<HashMap<String, String>>();
	List<HashMap<String, String>> mfillMaps3 = new ArrayList<HashMap<String, String>>();
	List<HashMap<String, String>> mfillMaps4 = new ArrayList<HashMap<String, String>>();
	
	Button m_period1;
	Button m_period2;
	
	Spinner m_spinClassification1;
	Spinner m_spinClassification2;
	Spinner m_spinClassification3;
	
	/*TextView m_barCode;
	TextView m_productName;*/
	TextView m_customerCode;
	TextView m_customerName;
	
	CheckBox m_checkBox1Day;
	
	String m_CalendarDay;
	String m_viewOption;
	
	String L_Name;
	String M_Name;
	String S_Name;

	DatePicker m_datePicker;
	
	SimpleDateFormat m_dateFormatter;
	Calendar m_dateCalender1;
	Calendar m_dateCalender2;
	
	int m_dateMode=0;
	
	NumberFormat m_numberFormat;
	
	ProgressDialog dialog;
	
    int index = 0;
    int size = 100;
    int firstPosition = 0;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_managesale_categori);
		
		 //???????????? ?????? ??????
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
      	m_viewOption = pref.getString("prefSaleViewMethod", "All");
      	
		m_shop = LocalStorage.getJSONObject(this, "currentShopData");
		m_userProfile = LocalStorage.getJSONObject(this, "userProfile"); 
		
        try {
			m_ip = m_shop.getString("SHOP_IP");
	        m_port = m_shop.getString("SHOP_PORT");
			//----------------------------------------//
			// 2021.12.21. ??????DB IP,PW,DB ??????
			//----------------------------------------//
			m_uuid = m_shop.getString("shop_uuid");
			m_uupw = m_shop.getString("shop_uupass");
			m_uudb = m_shop.getString("shop_uudb");
			//----------------------------------------//

			m_APP_USER_GRADE =m_userProfile.getString("APP_USER_GRADE");
			m_OFFICE_CODE = m_userProfile.getString("OFFICE_CODE");			
			//m_OFFICE_NAME = m_userProfile.getString("Office_Name");			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		m_period1 = (Button) findViewById(R.id.buttonSetDate1); 
		m_period2 = (Button) findViewById(R.id.buttonSetDate2);
		
		/*m_barCode = (TextView) findViewById(R.id.editTextBarcode);
		m_productName = (TextView) findViewById(R.id.editTextProductName);*/
		m_customerCode = (TextView) findViewById(R.id.editTextCustomerCode);
		m_customerName = (TextView) findViewById(R.id.editTextCustomerName);
				
		m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
		m_numberFormat = NumberFormat.getInstance();
				
		m_dateCalender1 = Calendar.getInstance();
		m_dateCalender2 = Calendar.getInstance();
		
		m_CalendarDay = m_dateFormatter.format(m_dateCalender1.getTime());
				
		m_period1.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
		m_period2.setText(m_dateFormatter.format(m_dateCalender2.getTime()));
		
		
		m_listSalesTab1= (ListView)findViewById(R.id.listviewSalesListTab1);
		m_listSalesTab2= (ListView)findViewById(R.id.listviewSalesListTab2);
		m_listSalesTab3= (ListView)findViewById(R.id.listviewSalesListTab3);
		m_listSalesTab4= (ListView)findViewById(R.id.listviewSalesListTab4);
		
		String[] from1 = new String[] {"L_Code", "L_Name", "?????????", "????????????", "?????????", "?????????", "????????????", "????????????", "?????????"};
        int[] to1 = new int[] { R.id.item1, R.id.item2, R.id.item7, R.id.item8, R.id.item9, R.id.item10, R.id.item11, R.id.item12, R.id.item13 };
        
        // TODO : ??????????????? ????????? ??? ????????????
		adapter1 = new SimpleAdapter(this, mfillMaps1, R.layout.activity_listview_item4_12, from1, to1);		
		m_listSalesTab1.setAdapter(adapter1);
		
		String[] from2 = new String[] {"L_Code", "L_Name", "M_Code", "M_Name", "?????????", "????????????", "?????????", "?????????", "????????????", "????????????", "?????????"};
        int[] to2 = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item7, R.id.item8, R.id.item9, R.id.item10, R.id.item11, R.id.item12, R.id.item13 };
        
		adapter2 = new SimpleAdapter(this, mfillMaps2, R.layout.activity_listview_item4_12, from2, to2);		
		m_listSalesTab2.setAdapter(adapter2);

		String[] from3 = new String[] {"L_Code", "L_Name", "M_Code", "M_Name", "S_Code", "S_Name", "?????????", "????????????", "?????????", "?????????", "????????????", "????????????", "?????????"};
        int[] to3 = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8, R.id.item9, R.id.item10, R.id.item11, R.id.item12, R.id.item13 };
        
		adapter3 = new SimpleAdapter(this, mfillMaps3, R.layout.activity_listview_item4_12, from3, to3);		
		m_listSalesTab3.setAdapter(adapter3);

		String[] from4 = new String[] {"L_Code", "L_Name", "M_Code", "M_Name", "S_Code", "S_Name", "Barcode",  "?????????",  "?????????", "G_Name", "????????????", "??????", "?????????" };
	    int[] to4 = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8, R.id.item9, R.id.item10, R.id.item11, R.id.item12, R.id.item13  };
        
		adapter4 = new SimpleAdapter(this, mfillMaps4, R.layout.activity_listview_item4_9, from4, to4);		
		m_listSalesTab4.setAdapter(adapter4);
		
		m_tabHost = (TabHost) findViewById(R.id.tabhostManageSales);
        m_tabHost.setup();        
        
        TabHost.TabSpec spec = m_tabHost.newTabSpec("tag1");
        
        spec.setContent(R.id.tab1);
        spec.setIndicator("?????????");
        m_tabHost.addTab(spec);

        spec = m_tabHost.newTabSpec("tag2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("?????????");
        m_tabHost.addTab(spec);
        
        spec = m_tabHost.newTabSpec("tag3");
        spec.setContent(R.id.tab3);
        spec.setIndicator("?????????");
        m_tabHost.addTab(spec);
        
        spec = m_tabHost.newTabSpec("tag4");
        spec.setContent(R.id.tab4);
        spec.setIndicator("?????????");
        m_tabHost.addTab(spec);

		//----------------------------------------//
		// ??? ?????? ??????
		//----------------------------------------//
		//https://www.tabnine.com/code/java/methods/android.widget.TabHost/getTabWidget
		// 2022.04.06. ??? ????????? ?????? ??????
		for(int i=0;i<m_tabHost.getTabWidget().getChildCount();i++){
			TextView tv = (TextView) m_tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
			tv.setTextColor(Color.parseColor("#ffffff"));
		}
		//----------------------------------------//

        // ??????????????? ??????
        m_listSalesTab1.setOnItemClickListener(this);
        m_listSalesTab2.setOnItemClickListener(this);        
        m_listSalesTab3.setOnItemClickListener(this);
        
        
        m_spinClassification1 = (Spinner)findViewById(R.id.spinnerClassificationType1);
		m_spinClassification1.setOnItemSelectedListener(this);
				
		m_spinClassification2 = (Spinner)findViewById(R.id.spinnerClassificationType2);
		m_spinClassification2.setOnItemSelectedListener(this);
				
		m_spinClassification3 = (Spinner)findViewById(R.id.spinnerClassificationType3);
		m_spinClassification3.setOnItemSelectedListener(this);
		
        
				
    /*    // ????????? ?????? ??? ????????? ??? ?????? ?????? ??????
        m_barCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 			
        	@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 			    if(!hasFocus){
 			    	String barcode = null; 
 			    	barcode = m_barCode.getText().toString();
 			    	if(!barcode.equals("")) // ????????? Barcode??? ?????? ?????? ?????????
 			    		doQueryWithBarcode();
 			    }
 			}
 		});      */
        
                
 		// ????????? ?????? ?????? ??? ????????? ??? ?????? ?????? ??????
        m_customerCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 			    if(!hasFocus){
 			    	String customerCode = m_customerCode.getText().toString();
 			    	if(!customerCode.equals("")) // ????????? customerCode??? ?????? ?????? ?????????
 			    		fillBusNameFromBusCode(customerCode);	    	
 			    }
 			}
 		});	       
    
        
    	m_checkBox1Day =(CheckBox)findViewById(R.id.checkBox1Day);
    	m_checkBox1Day.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if (isChecked) {
					m_period2.setEnabled(false);
					m_period2.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
				}
				else {
					m_period2.setEnabled(true);
				}
			}
		});
        
        // TODO : ?????????????????? ?????? ??????????????? ?????? ??????
        if (m_APP_USER_GRADE.equals("2")  && !m_OFFICE_CODE.equals("") && null != m_OFFICE_CODE) {
        	m_customerCode.setEnabled(false);
        	m_customerName.setEnabled(false);
        	m_customerCode.setText(m_OFFICE_CODE); 
        	m_customerName.setText(m_OFFICE_NAME);         
        }
        
        // ????????? ??? ??????        
        L_Name = "??????";
		M_Name = "??????";
		S_Name = "??????";
		
        //?????? ????????? ?????? ????????????        
        queryComboBoxIn(0, "", "", "");
      	
	}
	
	//????????? ????????? ?????? ??????
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		if ( m_listSalesTab1.getId() == arg0.getId() )
		{	
			m_tabHost.setCurrentTab(1);
			//String ltype = ((TextView) arg1.findViewById(R.id.item1)).getText().toString();
			String lname = ((TextView) arg1.findViewById(R.id.item2)).getText().toString();
			L_Name = lname;			
			doQuery();			
		}
		else if ( m_listSalesTab2.getId() == arg0.getId() )
		{
			m_tabHost.setCurrentTab(2);
			//String ltype = ((TextView) arg1.findViewById(R.id.item1)).getText().toString();
			String lname = ((TextView) arg1.findViewById(R.id.item2)).getText().toString();
			String mname = ((TextView) arg1.findViewById(R.id.item4)).getText().toString();
			L_Name = lname;
			M_Name = mname;			
			doQuery();	
	    	
		} else if ( m_listSalesTab3.getId() == arg0.getId() )		{
			m_tabHost.setCurrentTab(3);
			//String ltype = ((TextView) arg1.findViewById(R.id.item1)).getText().toString();
			String lname = ((TextView) arg1.findViewById(R.id.item2)).getText().toString();
			String mname = ((TextView) arg1.findViewById(R.id.item4)).getText().toString();
			String sname = ((TextView) arg1.findViewById(R.id.item6)).getText().toString();
			L_Name = lname;
			M_Name = mname;
			S_Name = sname;
			doQuery();	
		}
	}

	public void OnClickRenew(View v) {

        if (!m_APP_USER_GRADE.equals("2")  && !m_OFFICE_CODE.equals("") && null != m_OFFICE_CODE) {
    		m_customerCode.setText("");
    		m_customerName.setText("");        	
        }

//----------------------------------------//
//		2022.07.20. ????????? ?????????
//----------------------------------------//
		m_customerCode.setText("");
		m_customerName.setText("");
//----------------------------------------//

        mfillMaps1.removeAll(mfillMaps1);
        mfillMaps2.removeAll(mfillMaps2);
        mfillMaps3.removeAll(mfillMaps3);
        mfillMaps4.removeAll(mfillMaps4);
        
        // ????????? ??? ??????        
        //spinnerReset();
        //?????? ????????? ?????? ????????????        
        queryComboBoxIn(0, "", "", "");
        
        adapter1.notifyDataSetChanged();
        adapter2.notifyDataSetChanged();
        adapter3.notifyDataSetChanged();
        adapter4.notifyDataSetChanged();
        
	/*	m_barCode.setText("");
		m_productName.setText("");*/
	}
	/*
	public void OnClickCalendar(View v) {

		Intent intent = new Intent(this, ManageSalesCalendarActivity.class);			
    	intent.putExtra("PERIOD1", m_period1.getText().toString());
    	intent.putExtra("PERIOD2", m_period2.getText().toString());
    	//13???12??? ?????? ?????? ????????? ????????? ?????? ?????? ??????
    	intent.putExtra("OFFICE_CODE", m_customerCode.getText().toString());
    	intent.putExtra("OFFICE_NAME", m_customerName.getText().toString());
    	startActivity(intent);	
	}
	*/
	@Override
	public void onTabChanged(String tabId) {
	
 		//doQuery();
	}

	public void OnClickSearch(View v) {
		/*// ?????? ?????? ?????? ?????? ?????? ??????  12??? 27??? ???????????? ?????? ?????? ????????????
 		SharedPreferences pref1 = PreferenceManager.getDefaultSharedPreferences(this);
 	    String list_value1 = pref1.getString("MultiCardYnTypeChoose", "");
 	    if (list_value1.equals("cardY")) {
 	    	Card_Yn = " A.Card_YN = '0' AND ";
 	    }else if (list_value1.equals("cardN")) {
 	    	Card_Yn = " A.Card_YN = '1' AND ";
 	    }else {
 	    	Card_Yn = " " ;
 	    }*/
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
	 		case 3:
	 			queryListForTab4(); break;
 		}
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

		// ???????????? ????????? ?????? 2?????? ????????? ????????????
		if (m_checkBox1Day.isChecked()) {
			m_period2.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
		}
		
		m_dateMode = 0;
	}
	
	//?????????
	private void queryListForTab1()
	{		
		//??????????????? ?????? ?????? ?????? ???????????? ??????
		chooseCardYN();
		
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();		
		
		String customerCode = m_customerCode.getText().toString();
		//String customerName = m_customerName.getText().toString();
		
		String typeL = L_Name;
		String typeM = M_Name;
		String typeS = S_Name;
		
		String query = "";
		String constraint = "";
	    
		int year1 = Integer.parseInt(period1.substring(0, 4));		
		int month1 = Integer.parseInt(period1.substring(5, 7));
		
		int year2 = Integer.parseInt(period2.substring(0, 4));
		int month2 = Integer.parseInt(period2.substring(5, 7));
		
		mfillMaps1.removeAll(mfillMaps1);
				
		if ( typeL.equals("") != true && typeL.equals("??????") != true) {
			constraint = setConstraint(constraint, "L_Name", "=", typeL);
		}

		/**
		 * 2019-11-14 ?????? ?????? ??????
		if ( typeM.equals("") != true && typeM.equals("??????") != true) {
			constraint = setConstraint(constraint, "M_Name", "=", typeM);
		}

		
		if ( typeS.equals("") != true && typeS.equals("??????") != true) {
			constraint = setConstraint(constraint, "S_Name", "=", typeS);
		}
		 **/
			
		query = " 	Select G.L_Code, G.L_Name, " 
			 + " ISNULL(G.?????????,0) ?????????, ISNULL(G.????????????,0) ????????????, ISNULL(G.????????????,0) ????????????, ISNULL(G.????????????,0) ????????????, ISNULL(G.?????????,0) ?????????, ISNULL(G.?????????,0) ?????????, "
			 + " '?????????'=CASE WHEN ISNULL(G.???????????????,0) <> 0 Then (ISNULL(?????????,0)/G.???????????????)*100  ELSE 0 End "
			 + " From ( "
			 + " Select G.L_Code,G.L_Name, Sum(G.?????????) '?????????', Sum(G.????????????) '????????????', Sum(G.????????????) '????????????', Sum(G.????????????) '????????????',     Sum(G.?????????) '?????????', "
			 + " '?????????'=Case When Sum(G.?????????)=0 Or Sum(G.?????????)=0 Then 0 Else (Sum(G.?????????)/Sum(G.?????????))*100 End, Sum(G.???????????????) '???????????????' "
			 + " From ( ";
			 for ( int y = year1; y <= year2; y++ ) {
					int m1 = 1, m2 = 12;
					if (y == year1) m1 = month1;
					if (y == year2) m2 = month2;
					for ( int m = m1; m <= m2; m++ ) {
						String tableName = String.format("%04d%02d", y, m);

			 			query += " Select A.L_Code, A.L_Name, Sum(A.TSell_Pri - A.TSell_RePri) '?????????', Sum(A.CaRd_Pri) '????????????', Sum((A.TSell_Pri - A.TSell_RePri) - A.Card_Pri) '????????????', "
				 	  		    + "Sum(A.Pur_Pri * A.Sale_Count) '????????????', Sum(A.Profit_Pri) '?????????' , Sum(B.TSell_Pri - B.TSell_RePri) '???????????????' "
				 	  			+ " From Sad_"+tableName+" A left join Sat_"+tableName+" B on A.sale_num=B.sale_num "
				 	  			+ " Where A.Sale_Date >= '"+period1+"' AND A.Sale_Date <= '"+period2+"' And  "+ Card_Yn +" Office_Code  LIKE '"+customerCode+"%' ";

			 		//?????? ?????? ????????????
			 		if ( constraint.equals("") != true ) {
			 			query = query + " and " + constraint;
					}

			 		query += " Group By A.L_Code,A.L_Name " ;

			 		query += " UNION ALL ";	
					}
			 }
			 query = query.substring(0, query.length()-11);

			 query += " ) G " ;
			 query +=" Group By G.L_Code, G.L_Name ";
			 query +=" ) G Order By G.L_CODE ";
		
				
		// ?????? ??????????????? 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();
 		
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {

				dialog.dismiss();
				dialog.cancel();
				if ( results.length() > 0 ) updateListForTab1(results);
				adapter1.notifyDataSetChanged();	
				Toast.makeText(getApplicationContext(), "?????? ??????: " + results.length(), Toast.LENGTH_SHORT).show();	
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				adapter1.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}

	private void updateListForTab1(JSONArray results)
	{		
		for(int index = 0; index < results.length() ; index++) {					
					try {
						JSONObject son = results.getJSONObject(index);
						HashMap<String, String> map = JsonHelper.toStringHashMap(son);						
						map.put("?????????", StringFormat.convertToNumberFormat(map.get("?????????")));
						map.put("????????????", StringFormat.convertToRoundFormat(map.get("????????????")));
						map.put("????????????", StringFormat.convertToRoundFormat(map.get("????????????")));
						map.put("????????????", StringFormat.convertToNumberFormat(map.get("????????????")));
						map.put("?????????", StringFormat.convertToNumberFormat(map.get("?????????")));
						map.put("?????????", StringFormat.convertToRoundFormat(map.get("?????????"))+"%");
						map.put("?????????", StringFormat.convertToRoundFormat(map.get("?????????")) +"%");
						//map.put("?????????", StringFormat.convertToNumberFormat(String.format("%.2f",Double.parseDouble(map.get("?????????")))) +"%");
						//map.put("?????????", StringFormat.convertToNumberFormat(String.format("%.2f",Double.parseDouble(map.get("?????????")))) +"%");
						
						mfillMaps1.add(map);	
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
	}
	
	//?????????
	private void queryListForTab2()
	{
		mfillMaps2.removeAll(mfillMaps2);
		
		//??????????????? ?????? ?????? ?????? ???????????? ??????
		chooseCardYN();
		
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();		
		
		String customerCode = m_customerCode.getText().toString();
		//String customerName = m_customerName.getText().toString();
		
		String typeL = L_Name;
		String typeM = M_Name;
		String typeS = S_Name;
		
		String query = "";
		String constraint = "";
	    
		int year1 = Integer.parseInt(period1.substring(0, 4));		
		int month1 = Integer.parseInt(period1.substring(5, 7));
		
		int year2 = Integer.parseInt(period2.substring(0, 4));
		int month2 = Integer.parseInt(period2.substring(5, 7));		
		
				
		if ( typeL.equals("") != true && typeL.equals("??????") != true) {
			constraint = setConstraint(constraint, "L_Name", "=", typeL);
		}
		
		if ( typeM.equals("") != true && typeM.equals("??????") != true) {
			constraint = setConstraint(constraint, "M_Name", "=", typeM);
		}

		/**
		 * 2019-11-04 ?????? ?????? ??????
		if ( typeS.equals("") != true && typeS.equals("??????") != true) {
			constraint = setConstraint(constraint, "S_Name", "=", typeS);
		}
		 **/
			
		query = " 	Select G.L_Code, G.L_Name, G.M_Code, G.M_Name, " 
			 + " ISNULL(G.?????????,0) ?????????, ISNULL(G.????????????,0) ????????????, ISNULL(G.????????????,0) ????????????, ISNULL(G.????????????,0) ????????????, ISNULL(G.?????????,0) ?????????, ISNULL(G.?????????,0) ?????????, "
			 + " '?????????'=CASE WHEN ISNULL(G.???????????????,0) <> 0 Then (ISNULL(?????????,0)/G.???????????????)*100  ELSE 0 End "
			 + " From ( "
			 + " Select G.L_Code,G.L_Name,  G.M_Code, G.M_Name, Sum(G.?????????) '?????????', Sum(G.????????????) '????????????', Sum(G.????????????) '????????????', Sum(G.????????????) '????????????',     Sum(G.?????????) '?????????', "
			 + " '?????????'=Case When Sum(G.?????????)=0 Or Sum(G.?????????)=0 Then 0 Else (Sum(G.?????????)/Sum(G.?????????))*100 End, Sum(G.???????????????) '???????????????' "
			 + " From ( ";
			 for ( int y = year1; y <= year2; y++ ) {
					int m1 = 1, m2 = 12;
					if (y == year1) m1 = month1;
					if (y == year2) m2 = month2;
					for ( int m = m1; m <= m2; m++ ) {
						String tableName = String.format("%04d%02d", y, m);
			 			query += " Select A.L_Code, A.L_Name,  A.M_Code, A.M_Name , Sum(A.TSell_Pri - A.TSell_RePri) '?????????', Sum(A.CaRd_Pri) '????????????', Sum((A.TSell_Pri - A.TSell_RePri) - A.Card_Pri) '????????????', "
				 	  		+ "Sum(A.Pur_Pri * A.Sale_Count) '????????????', Sum(A.Profit_Pri) '?????????' , Sum(B.TSell_Pri - B.TSell_RePri) '???????????????' "
				 	  		+ " From Sad_"+tableName+" A left join Sat_"+tableName+" B on A.sale_num=B.sale_num "
				 	  		+ " Where A.Sale_Date >= '"+period1+"' AND A.Sale_Date <= '"+period2+"' And  "+ Card_Yn +" Office_Code  LIKE '"+customerCode+"%' ";

			 				//?????? ?????? ????????????
			 				if ( constraint.equals("") != true ) {
								query = query + " and " + constraint;
			 				}

						query += " Group By A.L_Code, A.L_Name, A.M_Code, A.M_Name " ;

			 			query += " UNION ALL ";
					}
			 }
			 query = query.substring(0, query.length()-11);

			 query +=" ) G " ;
			 query +=" Group By G.L_Code, G.L_Name,  G.M_Code, G.M_Name  " ;
			 query +=" ) G Order By G.L_CODE,  G.M_Code, G.M_Name  ";
		
				
		// ?????? ??????????????? 
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
				Toast.makeText(getApplicationContext(), "?????? ??????: " + results.length(), Toast.LENGTH_SHORT).show();	
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				adapter2.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	private void updateListForTab2(JSONArray results)
	{				
		for(int index = 0; index < results.length() ; index++) {					
			try {
				JSONObject son = results.getJSONObject(index);
				HashMap<String, String> map = JsonHelper.toStringHashMap(son);						
				map.put("?????????", StringFormat.convertToNumberFormat(map.get("?????????")));
				map.put("????????????", StringFormat.convertToRoundFormat(map.get("????????????")));
				map.put("????????????", StringFormat.convertToRoundFormat(map.get("????????????")));
				map.put("????????????", StringFormat.convertToNumberFormat(map.get("????????????")));
				map.put("?????????", StringFormat.convertToNumberFormat(map.get("?????????")));
				map.put("?????????", StringFormat.convertToRoundFormat(map.get("?????????")) +"%");
				map.put("?????????", StringFormat.convertToRoundFormat(map.get("?????????")) +"%");
				//map.put("?????????", StringFormat.convertToNumberFormat(String.format("%.2f",Double.parseDouble(map.get("?????????")))) +"%");
				//map.put("?????????", StringFormat.convertToNumberFormat(String.format("%.2f",Double.parseDouble(map.get("?????????")))) +"%");
				
				mfillMaps2.add(map);	
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	//?????????
	private void queryListForTab3()
	{
		mfillMaps3.removeAll(mfillMaps3);
		
		//??????????????? ?????? ?????? ?????? ???????????? ??????
		chooseCardYN();
		
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();		
		
		String customerCode = m_customerCode.getText().toString();
		//String customerName = m_customerName.getText().toString();
		
		String typeL = L_Name;
		String typeM = M_Name;
		String typeS = S_Name;
		
		String query = "";
		String constraint = "";
	    
		int year1 = Integer.parseInt(period1.substring(0, 4));		
		int month1 = Integer.parseInt(period1.substring(5, 7));
		
		int year2 = Integer.parseInt(period2.substring(0, 4));
		int month2 = Integer.parseInt(period2.substring(5, 7));		
		
				
		if ( typeL.equals("") != true && typeL.equals("??????") != true) {
			constraint = setConstraint(constraint, "L_Name", "=", typeL);
		}
		
		if ( typeM.equals("") != true && typeM.equals("??????") != true) {
			constraint = setConstraint(constraint, "M_Name", "=", typeM);
		}
		
		if ( typeS.equals("") != true && typeS.equals("??????") != true) {
			constraint = setConstraint(constraint, "S_Name", "=", typeS);
		}
			
		query = " 	Select G.L_Code, G.L_Name, G.M_Code, G.M_Name, G.S_Code, G.S_Name, " 
			 + " ISNULL(G.?????????,0) ?????????, ISNULL(G.????????????,0) ????????????, ISNULL(G.????????????,0) ????????????, ISNULL(G.????????????,0) ????????????, ISNULL(G.?????????,0) ?????????, ISNULL(G.?????????,0) ?????????, "
			 + " '?????????'=CASE WHEN ISNULL(G.???????????????,0) <> 0 Then (ISNULL(?????????,0)/G.???????????????)*100  ELSE 0 End "
			 + " From ( "
			 + " Select G.L_Code,G.L_Name,  G.M_Code, G.M_Name, G.S_Code, G.S_Name, Sum(G.?????????) '?????????', Sum(G.????????????) '????????????', Sum(G.????????????) '????????????', Sum(G.????????????) '????????????',     Sum(G.?????????) '?????????', "
			 + " '?????????'=Case When Sum(G.?????????)=0 Or Sum(G.?????????)=0 Then 0 Else (Sum(G.?????????)/Sum(G.?????????))*100 End, Sum(G.???????????????) '???????????????' "
			 + " From ( ";
			 for ( int y = year1; y <= year2; y++ ) {
					int m1 = 1, m2 = 12;
					if (y == year1) m1 = month1;
					if (y == year2) m2 = month2;
					for ( int m = m1; m <= m2; m++ ) {
						String tableName = String.format("%04d%02d", y, m);

						query += " Select A.L_Code, A.L_Name,  A.M_Code, A.M_Name , A.S_Code, A.S_Name, Sum(A.TSell_Pri - A.TSell_RePri) '?????????', Sum(A.CaRd_Pri) '????????????', Sum((A.TSell_Pri - A.TSell_RePri) - A.Card_Pri) '????????????', "
				 	  		+ "Sum(A.Pur_Pri * A.Sale_Count) '????????????', Sum(A.Profit_Pri) '?????????' , Sum(B.TSell_Pri - B.TSell_RePri) '???????????????' "
				 	  		+ " From Sad_"+tableName+" A left join Sat_"+tableName+" B on A.sale_num=B.sale_num "
				 	  		+ " Where A.Sale_Date >= '"+period1+"' AND A.Sale_Date <= '"+period2+"' And  "+ Card_Yn +" Office_Code  LIKE '"+customerCode+"%' ";

						//?????? ?????? ????????????
			 			if ( constraint.equals("") != true ) {
			 				query = query + " and " + constraint;
						}

						query += " Group By A.L_Code, A.L_Name, A.M_Code, A.M_Name, A.S_Code, A.S_Name ";

			 			query += " UNION ALL ";
					}
			 }
			 query = query.substring(0, query.length()-11);

			 query +=" ) G " ;
			 query +=" Group By G.L_Code, G.L_Name,  G.M_Code, G.M_Name, G.S_Code, G.S_Name ";
			 query +=" ) G Order By G.L_CODE,  G.M_Code, G.M_Name, G.S_Code, G.S_Name  ";
		
				
		// ?????? ??????????????? 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();
 		
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {

				dialog.dismiss();
				dialog.cancel();
				if ( results.length() > 0 ) updateListForTab3(results);
				adapter3.notifyDataSetChanged();	
				Toast.makeText(getApplicationContext(), "?????? ??????: " + results.length(), Toast.LENGTH_SHORT).show();	
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				adapter3.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
		
	private void updateListForTab3(JSONArray results)
	{		
		for(int index = 0; index < results.length() ; index++) {					
			try {
				JSONObject son = results.getJSONObject(index);
				HashMap<String, String> map = JsonHelper.toStringHashMap(son);						
				map.put("?????????", StringFormat.convertToNumberFormat(map.get("?????????")));
				map.put("????????????", StringFormat.convertToRoundFormat(map.get("????????????")));
				map.put("????????????", StringFormat.convertToRoundFormat(map.get("????????????")));
				map.put("????????????", StringFormat.convertToNumberFormat(map.get("????????????")));
				map.put("?????????", StringFormat.convertToNumberFormat(map.get("?????????")));
				map.put("?????????", StringFormat.convertToRoundFormat(map.get("?????????")) +"%");
				map.put("?????????", StringFormat.convertToRoundFormat(map.get("?????????")) +"%");
				
				mfillMaps3.add(map);	
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	//?????????
	private void queryListForTab4()
	{
		mfillMaps4.removeAll(mfillMaps4);
		
		//??????????????? ?????? ?????? ?????? ???????????? ??????
		chooseCardYN();
		
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();		
		
		String customerCode = m_customerCode.getText().toString();
		//String customerName = m_customerName.getText().toString();
		
		String typeL = L_Name;
		String typeM = M_Name;
		String typeS = S_Name;
		
		String query = "";
		String constraint = "";
	    
		int year1 = Integer.parseInt(period1.substring(0, 4));		
		int month1 = Integer.parseInt(period1.substring(5, 7));
		
		int year2 = Integer.parseInt(period2.substring(0, 4));
		int month2 = Integer.parseInt(period2.substring(5, 7));		
		
		if(m_viewOption.equals("cardN")){ // ???????????? ????????? ??????
			Card_Yn = " A.Card_Yn = '0' AND ";
		}else if(m_viewOption.equals("cardY")) { // ????????? ??????	
			Card_Yn = " A.Card_Yn = '1' AND ";
		}else{
			Card_Yn = "";
		}
				
		if ( typeL.equals("") != true && typeL.equals("??????") != true) {
			constraint = setConstraint(constraint, "L_Name", "=", typeL);
		}
		
		if ( typeM.equals("") != true && typeM.equals("??????") != true) {
			constraint = setConstraint(constraint, "M_Name", "=", typeM);
		}
		
		if ( typeS.equals("") != true && typeS.equals("??????") != true) {
			constraint = setConstraint(constraint, "S_Name", "=", typeS);
		}
			
		query = " 	Select G.L_Code, G.L_Name, G.M_Code, G.M_Name, G.S_Code, G.S_Name, G.Barcode, G.G_Name," 
			 + " ISNULL(G.?????????,0) ?????????, ISNULL(G.????????????,0) ????????????, ISNULL(G.????????????,0) ????????????, ISNULL(G.????????????,0) ????????????, ISNULL(G.?????????,0) ?????????, ISNULL(G.??????,0) '??????', ISNULL(G.?????????,0) ?????????, "
			 + " '?????????'=CASE WHEN ISNULL(G.???????????????,0) <> 0 Then (ISNULL(?????????,0)/G.???????????????)*100  ELSE 0 End "
			 + " From ( "
			 + " Select G.L_Code,G.L_Name,  G.M_Code, G.M_Name, G.S_Code, G.S_Name, G.Barcode, G.G_Name, Sum(G.?????????) '?????????', Sum(G.????????????) '????????????', Sum(G.????????????) '????????????', Sum(G.????????????) '????????????',     Sum(G.?????????) '?????????', "
			 + " Sum(G.??????) '??????', '?????????'=Case When Sum(G.?????????)=0 Or Sum(G.?????????)=0 Then 0 Else (Sum(G.?????????)/Sum(G.?????????))*100 End, Sum(G.???????????????) '???????????????' "
			 + " From ( ";
			 for ( int y = year1; y <= year2; y++ ) {
					int m1 = 1, m2 = 12;
					if (y == year1) m1 = month1;
					if (y == year2) m2 = month2;
					for ( int m = m1; m <= m2; m++ ) {
						String tableName = String.format("%04d%02d", y, m);

			 			query += " Select A.L_Code, A.L_Name,  A.M_Code, A.M_Name , A.S_Code, A.S_Name, A.Barcode, A.G_Name, Sum(A.TSell_Pri - A.TSell_RePri) '?????????', Sum(A.CaRd_Pri) '????????????', Sum((A.TSell_Pri - A.TSell_RePri) - A.Card_Pri) '????????????', "
				 	  		+ "Sum(A.Pur_Pri * A.Sale_Count) '????????????', Sum(A.Profit_Pri) '?????????' , Sum(A.Sale_Count) '??????', Sum(B.TSell_Pri - B.TSell_RePri) '???????????????'  "
				 	  		+ " From Sad_"+tableName+" A left join Sat_"+tableName+" B on A.sale_num=B.sale_num "
				 	  		+ " Where A.Sale_Date >= '"+period1+"' AND A.Sale_Date <= '"+period2+"' And  "+ Card_Yn +" Office_Code  LIKE '"+customerCode+"%' ";
				 	  			 		 
			 			//?????? ?????? ????????????
			 			if ( constraint.equals("") != true ) {
							query = query + " and " + constraint;
						}

						query += " Group by A.L_Code, A.L_Name, A.M_Code, A.M_Name, A.S_Code, A.S_Name, A.Barcode, A.G_Name ";

			 			query += " UNION ALL ";
					}
			 }
			 query = query.substring(0, query.length()-11);

			 query +=" ) G ";
			 query +=" Group By G.L_Code, G.L_Name,  G.M_Code, G.M_Name, G.S_Code, G.S_Name, G.Barcode, G.G_Name  ";
			 query +=" ) G Order By G.?????? DESC  ";
		
				
		// ?????? ??????????????? 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();
 		
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {

				dialog.dismiss();
				dialog.cancel();
				if ( results.length() > 0 ) updateListForTab4(results);
				adapter4.notifyDataSetChanged();	
				Toast.makeText(getApplicationContext(), "?????? ??????: " + results.length(), Toast.LENGTH_SHORT).show();	
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				adapter4.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
		
	private void updateListForTab4(JSONArray results)
	{
		for(int index = 0; index < results.length() ; index++) {					
			try {
				JSONObject son = results.getJSONObject(index);
				HashMap<String, String> map = JsonHelper.toStringHashMap(son);						
				map.put("?????????", StringFormat.convertToNumberFormat(map.get("?????????")));
				map.put("????????????", StringFormat.convertToRoundFormat(map.get("????????????")));
				map.put("????????????", StringFormat.convertToRoundFormat(map.get("????????????")));
				map.put("????????????", StringFormat.convertToNumberFormat(map.get("????????????")));
				map.put("?????????", StringFormat.convertToNumberFormat(map.get("?????????")));
				map.put("?????????", StringFormat.convertToRoundFormat(map.get("?????????")) +"%");
				map.put("?????????", StringFormat.convertToRoundFormat(map.get("?????????")) +"%");
				
				mfillMaps4.add(map);	
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{    
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){
			// ????????? ????????? ?????? ????????? ??????
			case ZBAR_SCANNER_REQUEST :
				/*if (resultCode == RESULT_OK) {
			        // Scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT)
			        // Type of the scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
			        Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();
			        //Toast.makeText(this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE), Toast.LENGTH_SHORT).show();
			        // The value of type indicates one of the symbols listed in Advanced Options below.
			       
			        String barcode = data.getStringExtra(ZBarConstants.SCAN_RESULT);
			      //  m_barCode.setText(barcode);
					doQueryWithBarcode();
					
			    } else if(resultCode == RESULT_CANCELED) {
			        Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
			    }
				break;
			// ?????? ????????? ?????? ????????? ??????				
			case BARCODE_MANAGER_REQUEST :
				if(resultCode == RESULT_OK && data != null) {
		        	HashMap<String, String> hashMap = (HashMap<String, String>)data.getSerializableExtra("fillmaps");        	
		      //  	m_barCode.setText(hashMap.get("BarCode"));
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
	
	public void onCustomerSearch(View view)
	{
		String customer = m_customerCode.getText().toString();
		String customername = m_customerName.getText().toString();
		Intent intent = new Intent(this, ManageCustomerListActivity.class);
		intent.putExtra("customer", customer);
		intent.putExtra("customername", customername);
    	startActivityForResult(intent, CUSTOMER_MANAGER_REQUEST);
	}
	
	/*public void onBarcodeSearch(View view)
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
        	// ????????? ?????? ?????? ????????? ????????? ?????? ??????
    		final String[] option = new String[] { "??????", "?????????"};
    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option);
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("Select Option");
    		
    		// ?????? ????????? ????????? ??????
    		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {

    				if(which == 0){ // ???????????? ????????? ??????
    					startProductList();
    				} else { // ????????? ??????	
    					startCameraSearch();
    				}
    			}
    		}); 
    		builder.show();
        }
	}*/
	
	/*private void startProductList() {
		String barcode = m_barCode.getText().toString();
		String gname = m_productName.getText().toString();
		String officecode = m_customerCode.getText().toString();
		Intent intent = new Intent(this, ManageProductListActivity.class);					
		intent.putExtra("barcode", barcode);
		intent.putExtra("gname", gname);
		intent.putExtra("Office_Code", officecode);	
    	startActivityForResult(intent, BARCODE_MANAGER_REQUEST);
	}*/
	
	/*private void startCameraSearch() {
		Intent intent = new Intent(this, ZBarScannerActivity.class);
    	startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
	} */
	
	// TODO : ChooseCardYN?????? ?????? ???????????? ????????? ????????? ?????? ?????? ?????? ?????? 
	public void chooseCardYN() {
	/*final String[] option1 = new String[] { "????????????", "????????????", "????????????"};
	ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option1);
	AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
	builder1.setTitle("Select Option");
	
	// ?????? ????????? ????????? ??????
	builder1.setAdapter(adapter1, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {

			if(which == 1){ // ???????????? ????????? ??????
				Card_Yn = " A.Card_Yn = '0' AND ";
			}else if(which == 2) { // ????????? ??????	
				Card_Yn = " A.Card_Yn = '1' AND ";
			}else{
				Card_Yn = " A.Card_Yn Like '%' AND ";
			}
		}
	}); 
	builder1.show();	*/
		if(m_viewOption.equals("cardN")){ // ???????????? ????????? ??????
			Card_Yn = " A.Card_Yn = '0' AND ";
		}else if(m_viewOption.equals("cardY")) { // ????????? ??????	
			Card_Yn = " A.Card_Yn = '1' AND ";
		}else{
			Card_Yn = "";
		}
	}
	
	
	// MSSQL
	// SQL QUERY ??????
	/*public void doQueryWithBarcode () {
		
		String query = "";
		String barcode = m_barCode.getText().toString();		
		query = "SELECT * FROM Goods WHERE Barcode = '"+barcode+"';";
	
		if (barcode.equals("")) return;
		
	    // ??????????????? ?????? ??????
	    new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				
				if (results.length() > 0) {
					try {						
						m_productName.setText(results.getJSONObject(0).getString("G_Name"));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				else {
					Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
				}
			}
	    }).execute(m_ip+":"+m_port, "TIPS", "sa", "tips", query);		
	}
	*/
	// ????????? ????????? ???????????? ?????? ??????
	private void fillBusNameFromBusCode(String customerCode) {
		
		String query = "";
		query = "SELECT Office_Name From Office_Manage WHERE Office_Code = '" + customerCode + "';";
	    new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {

				if (results.length() > 0) {
					try {
						JSONObject json = results.getJSONObject(0);
						String bus_name = json.getString("Office_Name");
						m_customerName.setText(bus_name);
					} catch (JSONException e) {						
						e.printStackTrace();
					}
		            Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
				}
				else {
		            Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();					
				}
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	

	@Override
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
			long arg3) {
		// TODO Auto-generated method stub
		if ( arg0.getId() == m_spinClassification1.getId() )
		{
			String typeL = m_spinClassification1.getItemAtPosition(arg2).toString();
			L_Name = typeL;
			if(typeL.equals("??????")) {				
				 M_Name = "";
				 S_Name = "";
				 queryComboBoxIn(2, "", "", "");
			}
			queryComboBoxIn(1, typeL, "", "");
		}
		else if ( arg0.getId() == m_spinClassification2.getId() )
		{
			String typeL = m_spinClassification1.getItemAtPosition(m_spinClassification1.getSelectedItemPosition()).toString();
			String typeM = m_spinClassification2.getItemAtPosition(arg2).toString();
			if ( typeL == "??????"){
				Toast.makeText( ManageSale_CategoriActivity.this, "???????????? ?????? ????????? ?????????!! ", Toast.LENGTH_SHORT).show();
				return;
			}else if( typeM == "??????" ){
				S_Name = "";
				L_Name=typeL;
				M_Name=typeM;
				queryComboBoxIn(2, typeL, typeM, "");
			}else{
				L_Name=typeL;
				M_Name=typeM;
				queryComboBoxIn(2, typeL, typeM, "");	
			}
		}
		else if ( arg0.getId() == m_spinClassification3.getId() )
		{			
			String typeL = m_spinClassification1.getItemAtPosition(m_spinClassification1.getSelectedItemPosition()).toString();
			String typeM = m_spinClassification2.getItemAtPosition(m_spinClassification2.getSelectedItemPosition()).toString();
			String typeS = m_spinClassification3.getItemAtPosition(arg2).toString();
			if ( typeM == "??????" ){
				Toast.makeText(this, "???????????? ?????? ????????? ?????????!! ", Toast.LENGTH_SHORT).show();
				return;
			}else{
			L_Name=typeL;
			M_Name=typeM;
			S_Name=typeS;		
			}
			//queryComboBoxIn(typeL, typeM, typeS);
		}
	}

	
	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
	
	// ?????? ????????????
	private void queryComboBoxIn(final int iCombo, String lName, String mName, String sName) {
		
		if ( iCombo == 0 ) {
			
			String query = " Select L_Code, L_Name From L_Branch Where L_Code <> 'AA' Order By L_Code ";
			
			new MSSQL(new MSSQL.MSSQLCallbackInterface() {
	
				@Override
				public void onRequestCompleted(JSONArray results) {
					
					updateComboBox(results, 0);
				}
			}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
		}
		else if( iCombo == 1 ){
			
			String query = " Select M_Code, M_Name From M_Branch Where L_Name='"+ lName +"' AND M_Code <> 'AA' Order By M_Code ";
			
			new MSSQL(new MSSQL.MSSQLCallbackInterface() {
	
				@Override
				public void onRequestCompleted(JSONArray results) {
					
					updateComboBox(results, 1);
				}
			}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
			
		}
		else if ( iCombo == 2 ){
			
			String query = " Select S_Code, S_Name From S_Branch Where L_Name='"+ lName +"' AND  M_Name='"+ mName +"' AND S_Code <> 'AA' Order By S_Code ";
			
			new MSSQL(new MSSQL.MSSQLCallbackInterface() {
	
				@Override
				public void onRequestCompleted(JSONArray results) {
					
					updateComboBox(results, 2);
				}
			}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
		}		
	}
	
	// ?????? ????????? ?????????
	private void updateComboBox(JSONArray results, int iCombo ) {
		
		ArrayList<String> lSpList = new ArrayList<String>();       
		
		try {
			
			if ( results.length() > 0 )
			{
				 		   	        
				String lName = null;
				
				lSpList.add("??????");
		        
				for(int index = 0; index < results.length() ; index++)
				{
					JSONObject son = results.getJSONObject(index);
					
					if ( iCombo == 0 )
					{
						lName = son.getString("L_Name");
					}
					else if ( iCombo == 1 )
					{
						lName = son.getString("M_Name");
					}
					else if ( iCombo == 2 )
					{
						lName = son.getString("S_Name");
					}
					
	        		boolean isExist = false;
	        		
	        		for ( int i = 0; i < lSpList.size(); i++ )
	        		{
	        			if ( lSpList.get(i).toString().equals(lName) == true )
	        			{
	        				isExist = true;
	        				break;
	        			}
	        		}
	        		
	        		if ( isExist == true )
	        		{
	        			continue;
	        		}

		            lSpList.add(lName);
				}
				
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(ManageSale_CategoriActivity.this, android.R.layout.simple_spinner_item, lSpList);
				
				if ( iCombo == 0 )
				{
					m_spinClassification1.setAdapter(adapter);
				}
				else if ( iCombo == 1 )
				{
					m_spinClassification2.setAdapter(adapter);
				}
				else if ( iCombo == 2 )
				{
					m_spinClassification3.setAdapter(adapter);
				}
			}
			else 
			{
				ArrayAdapter<String> adapter = new ArrayAdapter<String>(ManageSale_CategoriActivity.this, android.R.layout.simple_spinner_item, lSpList);
				
				if ( iCombo == 0 )
				{
					m_spinClassification1.setAdapter(adapter);
				}
				else if ( iCombo == 1 )
				{
					m_spinClassification2.setAdapter(adapter);
				}
				else if ( iCombo == 2 )
				{
					m_spinClassification3.setAdapter(adapter);
				}				
			}
			
			//Toast.makeText(getApplicationContext(), "?????? ??????: " + results.length(), Toast.LENGTH_SHORT).show();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}		
	}
	
	private String setConstraint(String str, String field, String op, String value)
    {
    	if ( str.equals("") != true )
    	{
    		str = str + " and ";
    	}
    	
    	str = str + field + " " + op + " '" + value + "'";
    	
    	return str;
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
			actionbar.setTitle("????????? ????????????");
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		
		 //???????????? ?????? ??????
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
      	m_viewOption = pref.getString("prefSaleViewMethod", "");      	
      	
	};
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manage_payment, menu);
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