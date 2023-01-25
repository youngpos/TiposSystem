package tipsystem.tips;

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

import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class ManageSaleCommissionActivity extends Activity implements OnItemClickListener, 
															OnTabChangeListener,
															DatePickerDialog.OnDateSetListener{
	// TODO : 매출관리 메인화면
	//private static final int ZBAR_SCANNER_REQUEST = 0;
	//private static final int BARCODE_MANAGER_REQUEST = 2;
	private static final int CUSTOMER_MANAGER_REQUEST = 3;

	JSONObject m_shop;
	JSONObject m_userProfile;

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

	String m_APP_USER_GRADE;
	String m_OFFICE_CODE;	// 수수료매장일때 고정될 오피스코드
	String m_OFFICE_NAME;	// 수수료매장일때 고정될 오피스네임
	private String Card_Yn = " A.Card_Yn = '0' AND "; // 다중 매장시 사용 여부 결정
	
	TabHost m_tabHost;	
	ListView m_listSalesTab1;
	ListView m_listSalesTab2;
	ListView m_listSalesTab3;
	//ListView m_listSalesTab4;
	
	SimpleAdapter adapter1;
	SimpleAdapter adapter2;
	SimpleAdapter adapter3;
	//SimpleAdapter adapter4;

	List<HashMap<String, String>> mfillMaps1 = new ArrayList<HashMap<String, String>>();
	List<HashMap<String, String>> mfillMaps2 = new ArrayList<HashMap<String, String>>();
	List<HashMap<String, String>> mfillMaps3 = new ArrayList<HashMap<String, String>>();
	//List<HashMap<String, String>> mfillMaps4 = new ArrayList<HashMap<String, String>>();
	
	Button m_period1;
	Button m_period2;
	
	TextView m_paper;
	TextView m_cardnumber;
	TextView m_customerCode;
	TextView m_customerName;
	
	CheckBox m_checkBox1Day;
	
	String m_CalendarDay;
	String m_viewOption;
	
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
		setContentView(R.layout.activity_managesale_commission);
		
		m_shop = LocalStorage.getJSONObject(this, "currentShopData");
		m_userProfile = LocalStorage.getJSONObject(this, "userProfile"); 
						
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

			m_APP_USER_GRADE =m_userProfile.getString("APP_USER_GRADE");
			m_OFFICE_CODE = m_userProfile.getString("OFFICE_CODE");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}

		m_period1 = (Button) findViewById(R.id.buttonSetDate1); 
		m_period2 = (Button) findViewById(R.id.buttonSetDate2);
		
		//카드번호 및, 전표번호 검색
		m_paper = (EditText) findViewById(R.id.editTextJunNumber);
		m_cardnumber = (EditText) findViewById(R.id.editTextCardNumber);
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
		//m_listSalesTab4= (ListView)findViewById(R.id.listviewSalesListTab4);
		
		String[] from1 = new String[] {"Office_Code", "Office_Name", "순매출", "수카드금액", "과세매출", "면세매출", "현금매출", "카드매출"};
	    int[] to1 = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8 };
        
		adapter1 = new SimpleAdapter(this, mfillMaps1, R.layout.activity_listview_item3, from1, to1);		
		m_listSalesTab1.setAdapter(adapter1);	
		
		// Sale_Date, Sale_Time, Pos_No, Sale_Num, 회원, 판매금액, 반품금액, 할인금액, 순매출, 현금금액, 카드금액, 외상금액, 포인트사용, 배달구분		
		String[] from2 = new String[] {"Sale_Date", "Sale_Time", "Pos_No", "Sale_Num", "판매금액", "반품금액", "할인금액", "포인트사용", "외상금액", "순매출", "현금금액", "카드금액", "회원","배달구분" };
        int[] to2 = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8,  R.id.item9, R.id.item10, R.id.item11, R.id.item12, R.id.item13 , R.id.item14};
        
		adapter2 = new SimpleAdapter(this, mfillMaps2, R.layout.activity_listview_item5_5, from2, to2);		
		m_listSalesTab2.setAdapter(adapter2);
		
		//거래일자, 승인일자, 전표번호, 포스번호, 승인구분, 회원번호, 할부, 카드번호, 승인금액, 승인번호, 카드사명		
		String[] from3 = new String[] {"거래일자", "승인일자", "전표번호", "포스번호", "승인구분", "할부", "회원번호", "카드번호", "승인금액", "승인번호", "카드사명"};
        int[] to3 = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8, R.id.item9, R.id.item10, R.id.item11 };
        
		adapter3 = new SimpleAdapter(this, mfillMaps3, R.layout.activity_listview_item4_13, from3, to3);		
		m_listSalesTab3.setAdapter(adapter3);

		/*String[] from4 = new String[] {"Office_Code", "Office_Name", "순매출", "수카드금액", "과세매출", "면세매출", "현금매출", "카드매출"};
	    int[] to4 = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8 };
        
		adapter4 = new SimpleAdapter(this, mfillMaps4, R.layout.activity_listview_item3, from4, to4);		
		m_listSalesTab4.setAdapter(adapter4);*/
		
		m_tabHost = (TabHost) findViewById(R.id.tabhostManageSales);
        m_tabHost.setup();        
        
        TabHost.TabSpec spec = m_tabHost.newTabSpec("tag1");
        
        spec.setContent(R.id.tab1);
        spec.setIndicator("매출");
        m_tabHost.addTab(spec);

        spec = m_tabHost.newTabSpec("tag2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("전표검색");
        m_tabHost.addTab(spec);
        
        spec = m_tabHost.newTabSpec("tag3");
        spec.setContent(R.id.tab3);
        spec.setIndicator("카드검색");
        m_tabHost.addTab(spec);

		//----------------------------------------//
		// 탭 속성 정의
		//----------------------------------------//
		//https://www.tabnine.com/code/java/methods/android.widget.TabHost/getTabWidget
		// 2022.04.04. 탭 텍스트 색상 변경
		for(int i=0;i<m_tabHost.getTabWidget().getChildCount();i++){
			TextView tv = (TextView) m_tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
			tv.setTextColor(Color.parseColor("#ffffff"));
		}
		//----------------------------------------//

       /* spec = m_tabHost.newTabSpec("tag4");
        spec.setContent(R.id.tab4);
        spec.setIndicator("공제금액");
        m_tabHost.addTab(spec);*/
        
        // 클릭리스너 설정
        m_listSalesTab1.setOnItemClickListener(this);
        m_listSalesTab2.setOnItemClickListener(this);
        m_listSalesTab3.setOnItemClickListener(this);
        
      /*  // 바코드 입력 후 포커스 딴 곳을 옮길 경우
        m_barCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 			
        	@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 			    if(!hasFocus){
 			    	String barcode = null; 
 			    	barcode = m_barCode.getText().toString();
 			    	if(!barcode.equals("")) // 입력한 Barcode가 값이 있을 경우만
 			    		doQueryWithBarcode();
 			    }
 			}
 		});      */
        
                
 		// 거래처 코드 입력 후 포커스 딴 곳을 옮길 경우
        m_customerCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
 			@Override
 			public void onFocusChange(View v, boolean hasFocus) {
 			    if(!hasFocus){
 			    	String customerCode = m_customerCode.getText().toString();
 			    	if(!customerCode.equals("")) // 입력한 customerCode가 값이 있을 경우만
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
        
        // TODO : 수수료매장의 경우 오피스코드 고정 설정
        if (m_APP_USER_GRADE.equals("2")  && !m_OFFICE_CODE.equals("") && null != m_OFFICE_CODE) {
        	m_customerCode.setEnabled(false);
        	m_customerName.setEnabled(false);
        	m_customerCode.setText(m_OFFICE_CODE); 
        	m_customerName.setText(m_OFFICE_NAME);         
        }
	}
		
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		if ( m_listSalesTab1.getId() == arg0.getId() ){
			String code = ((TextView) arg1.findViewById(R.id.item1)).getText().toString();
			String name = ((TextView) arg1.findViewById(R.id.item2)).getText().toString();
			
			Intent intent = new Intent(this, ChargeCustomerDetailActivity.class);
			String period1 = m_period1.getText().toString();
			String period2 = m_period2.getText().toString();
			
	    	intent.putExtra("PERIOD1", period1);
	    	intent.putExtra("PERIOD2", period2);
	    	intent.putExtra("OFFICE_CODE", code);
	    	intent.putExtra("OFFICE_NAME", name);
	    	
	    	startActivity(intent);
		}
		else if ( m_listSalesTab2.getId() == arg0.getId() )
		{
			String sale_date = ((TextView) arg1.findViewById(R.id.item1)).getText().toString();
			String sale_time = ((TextView) arg1.findViewById(R.id.item2)).getText().toString();
			String pos_number = ((TextView) arg1.findViewById(R.id.item3)).getText().toString();
			String sale_num = ((TextView) arg1.findViewById(R.id.item4)).getText().toString();
			
			Intent intent = new Intent(this, ManageSaleCommissionDetailActivity.class);
			
	    	intent.putExtra("SALE_DATE", sale_date);
	    	intent.putExtra("SALE_TIME", sale_time);
	    	intent.putExtra("POS_NUMBER", pos_number);
	    	intent.putExtra("SALE_NUM", sale_num);
	    	//intent.putExtra("CARD_YN", Card_Yn);
	    	intent.putExtra("GUBUN", "1"); //전표검색으로 넘긴값
	    	
	    	startActivity(intent);
		}
		else if ( m_listSalesTab3.getId() == arg0.getId() )
		{
			String sale_date = ((TextView) arg1.findViewById(R.id.item1)).getText().toString();
			String sale_time = "";
			String pos_number = ((TextView) arg1.findViewById(R.id.item4)).getText().toString();
			String sale_num = ((TextView) arg1.findViewById(R.id.item3)).getText().toString();
			
			Intent intent = new Intent(this, ManageSaleCommissionDetailActivity.class);
			
	    	intent.putExtra("SALE_DATE", sale_date);
	    	intent.putExtra("SALE_TIME", sale_time);
	    	intent.putExtra("POS_NUMBER", pos_number);
	    	intent.putExtra("SALE_NUM", sale_num.substring(12));
	    	//intent.putExtra("CARD_YN", Card_Yn);
	    	intent.putExtra("GUBUN", "2"); //카드검색으로 넘긴값
	    	
	    	startActivity(intent);
		}
	}

	//새로입력
	public void OnClickRenew(View v) {

        if (!m_APP_USER_GRADE.equals("2")  && !m_OFFICE_CODE.equals("") && null != m_OFFICE_CODE) {
    		m_customerCode.setText("");
    		m_customerName.setText("");        	
        }
		m_paper.setText("");
		m_cardnumber.setText("");

		//----------------------------------------//
		//		2022.07.20. 무조건 지우기
		//----------------------------------------//
		m_customerCode.setText("");
		m_customerName.setText("");
		//----------------------------------------//

		//----------------------------------------//
		//		2022.07.20. 지우기 추가
		//----------------------------------------//
		mfillMaps1.removeAll(mfillMaps1);
		mfillMaps2.removeAll(mfillMaps2);
		mfillMaps3.removeAll(mfillMaps3);
		adapter1.notifyDataSetChanged();
		adapter2.notifyDataSetChanged();
		adapter3.notifyDataSetChanged();
		//----------------------------------------//

	}
	
	//달력매출
	public void OnClickCalendar(View v) {

		Intent intent = new Intent(this, ManageSalesCalendarActivity.class);			
    	intent.putExtra("PERIOD1", m_period1.getText().toString());
    	intent.putExtra("PERIOD2", m_period2.getText().toString());
    	//13년12월 추가 내용 화면에 거래처 내용 전송 하기
    	intent.putExtra("OFFICE_CODE", m_customerCode.getText().toString());
    	intent.putExtra("OFFICE_NAME", m_customerName.getText().toString());
    	startActivity(intent);	
	}
	
	//탭변경 리스너
	@Override
	public void onTabChanged(String tabId) {
	
		int tab = m_tabHost.getCurrentTab();
		if( tab > 0 && 3 > tab){
			m_paper.setEnabled(false);
			m_cardnumber.setEnabled(false);
		}else{
			m_paper.setEnabled(true);
			m_cardnumber.setEnabled(true);
		}
 		//doQuery();
	}

	//조회버튼
	public void OnClickSearch(View v) {
		/*// 매출 보기 메뉴 옵션 설정 하기  12월 27일 버튼옵션 으로 변경 처리완료
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
	
	//탭변경시 실행되는 쿼리
	public void doQuery() {

 		switch (m_tabHost.getCurrentTab())
 		{
	 		case 0:
	 			queryListForTab1(); break;
	 		case 1:
	 			queryListForTab2(); break;
	 		case 2:
	 			queryListForTab3(); break;
	 /*		case 3:
	 			queryListForTab4(); break;*/
 		}
	}

	//첫번째 날자 변경
	public void onClickSetDate1(View v) {
				
		DatePickerDialog newDlg = new DatePickerDialog(this, this,
				m_dateCalender1.get(Calendar.YEAR),
				m_dateCalender1.get(Calendar.MONTH),
				m_dateCalender1.get(Calendar.DAY_OF_MONTH));
		 newDlg.show();
		
		 m_dateMode = 1;
	};
	
	//두번째 날자 변경
	public void onClickSetDate2(View v) {
		
		DatePickerDialog newDlg = new DatePickerDialog(this, this, 
				m_dateCalender2.get(Calendar.YEAR),
				m_dateCalender2.get(Calendar.MONTH),
				m_dateCalender2.get(Calendar.DAY_OF_MONTH));
		
		newDlg.show();
		
		m_dateMode = 2;
	};

	//날자 하루 고정 체크
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

		// 체크되어 있으면 날짜 2개를 똑같이 바꾸어줌
		if (m_checkBox1Day.isChecked()) {
			m_period2.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
		}
		
		m_dateMode = 0;
	}
		
	//첫번째 탭
	private void queryListForTab1()
	{
		mfillMaps1.removeAll(mfillMaps1);
		
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();		
		String customerCode = m_customerCode.getText().toString();
		String customerName = m_customerName.getText().toString();
		
		String query = "";
	    
		int year1 = Integer.parseInt(period1.substring(0, 4));
		int month1 = Integer.parseInt(period1.substring(5, 7));
		
		int year2 = Integer.parseInt(period2.substring(0, 4));
		int month2 = Integer.parseInt(period2.substring(5, 7));
		
		//수수료매장 매출 전체 보기 나눠보기 옵션
		//chooseCardYN();
		
		query = " Select G.Office_Code, G.Office_Name, "
			      + "       Sum (G.순매출) '순매출', Sum(G.과세매출) '과세매출', Sum(G.면세매출) '면세매출', "
			      + "       Sum (G.현금매출) '현금매출', Sum (G.카드매출) '카드매출', Sum (G.수_카드금액) '수_카드금액', "
			      + "       Sum (G.매장수수료) '매장수수료', Sum (G.카드수수료) '카드수수료', Sum (G.S_Point) '포인트', "
			      + "       Sum (G.S_CashBackPoint) '캐쉬백', Sum (G.이익금) '이익금', "
			      + "       '이익률'=Case When Sum(G.이익금)=0 Or Sum(G.순매출)=0 Then 0 Else (Sum(G.이익금)/Sum(G.순매출))*100 End "
			+ " From ( ";

			for ( int y = year1; y <= year2; y++ ) {
						int m1 = 1, m2 = 12;
						if (y == year1) m1 = month1;
						if (y == year2) m2 = month2;
						for ( int m = m1; m <= m2; m++ ) {
							
			String tableName = String.format("%04d%02d", y, m);

			query += "     Select A.Office_Code, A.Office_Name,  Sum (a.TSell_Pri - a.TSell_RePri) '순매출', "
			+"           '과세매출'=Sum(Case When A.Tax_YN='1' Then A.TSell_Pri-A.TSell_RePri Else 0 End),"
			+"           '면세매출'=Sum(Case When A.Tax_YN='0' Then A.TSell_Pri-A.TSell_RePri Else 0 End),"
			+"           Sum ((a.TSell_Pri - a.TSell_RePri) - a.Card_Pri) '현금매출',"
			+"           Sum (a.Card_Pri) '카드매출', '수_카드금액'=Sum(Case When A.Card_YN='1' Then A.Card_Pri Else 0 End),"
			+"           '매장수수료'=Sum(Case When A.Sale_YN='1' Then (A.TSell_Pri+A.Dc_Pri)*(A.Fee/100) Else ((A.TSell_RePri+A.Dc_Pri)*(A.Fee/100))*-1 End),"
			+"           '카드수수료'=Sum(Case When A.Card_YN='0' Then a.Card_Pri * (a.Card_Fee / 100) Else 0 End),"
			+"           Sum(A.S_Point) S_Point, Sum(A.S_CashBackPoint) S_CashBackPoint , Sum (a.Profit_Pri) '이익금'"
			+"     From SaD_"+tableName+" A "
			+"          LEFT JOIN  SaT_"+tableName+" C ON A.Sale_Num=C.Sale_Num "
			+"          LEFT JOIN Office_Manage B ON A.Office_Code=B.Office_Code "	   
			+"     Where B.Office_Sec = '2' And A.Office_Code Like '%"+customerCode+"%' And "
			+"            A.Office_Name Like '%"+customerName+"%' AND A.Sale_Date >= '"+period1+"' AND "
			+"            A.Sale_Date <= '"+period2+"' Group By A.Office_Code, A.Office_Name "
			+" UNION ALL ";
						}
			}		
			query = query.substring(0, query.length()-11);
			query +="     ) G "
			+"     Group By G.Office_Code,G.Office_Name ";
		
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
				if (results.length()>0)
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
	
	//첫번째 탭
	private void updateListForTab1(JSONArray results)
	{
		for(int index = 0; index < results.length() ; index++) {
			
			try {
				JSONObject son = results.getJSONObject(index);
				HashMap<String, String> map = JsonHelper.toStringHashMap(son);
				map.put("순매출", StringFormat.convertToNumberFormat(map.get("순매출")));
				map.put("수카드금액", StringFormat.convertToNumberFormat(map.get("수_카드금액")));
				map.put("과세매출", StringFormat.convertToNumberFormat(map.get("과세매출")));
				map.put("면세매출", StringFormat.convertToNumberFormat(map.get("면세매출")));
				map.put("현금매출", StringFormat.convertToIntNumberFormat(map.get("현금매출")));
				map.put("카드매출", StringFormat.convertToIntNumberFormat(map.get("카드매출")));
				mfillMaps1.add(map);	
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
		
	//두번째 탭	
	private void queryListForTab2()
	{
		mfillMaps2.removeAll(mfillMaps2);
		
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();
		String paper = m_paper.getText().toString();
		//String cardnumber = m_cardnumber.getText().toString();
		String customerCode = m_customerCode.getText().toString();
		String customerName = m_customerName.getText().toString();
		
		String query = "";
	    
		int year1 = Integer.parseInt(period1.substring(0, 4));
		int month1 = Integer.parseInt(period1.substring(5, 7));
		
		int year2 = Integer.parseInt(period2.substring(0, 4));
		int month2 = Integer.parseInt(period2.substring(5, 7));
		
		// Sale_Date, Sale_Time, Pos_No, Sale_Num, 회원, 판매금액, 반품금액, 할인금액, 순매출, 현금금액, 카드금액, 외상금액, 포인트사용, 배달구분
		query = " Select G.Sale_Date, MAX(G.Sale_Time) Sale_Time, G.Pos_No, G.Sale_Num, G.회원, Sum(G.판매금액) '판매금액', "
				+ " Sum(G.반품금액) '반품금액', Sum(G.할인금액) '할인금액', Sum(G.순매출) '순매출', "
				+ " Sum(G.현금금액) '현금금액', Sum(G.카드금액) '카드금액', case When Sum(G.외상금액) = '0' Then '' else '외상' end '외상금액', " 
				+ " Sum(G.포인트사용) '포인트사용', G.배달구분 "
				+ " From ( ";
				for ( int y = year1; y <= year2; y++ ) {
					int m1 = 1, m2 = 12;
					if (y == year1) m1 = month1;
					if (y == year2) m2 = month2;
					for ( int m = m1; m <= m2; m++ ) {				
				String tableName = String.format("%04d%02d", y, m);
		query += " Select A.Sale_Date, A.Sale_Time, A.Pos_No, A.Sale_Num, case When A.Cus_Code = '' Then '비회원' Else '회원' end '회원', Case When A.Sale_Yn='1' Then A.TSell_Pri+A.Dc_Pri Else 0 End '판매금액', "
				+ " Case When A.Sale_Yn='0' Then A.TSell_RePri+A.Dc_Pri Else 0 End '반품금액', Case When A.Sale_Yn='1' Then A.Dc_Pri Else A.Dc_Pri*-1 End '할인금액', "
				+ " A.TSell_Pri -A.TSell_RePri '순매출', " 
				+ " (A.TSell_Pri-A.TSell_RePri)-A.Card_Pri-Round(A.Money_Per*B.Dec_Pri,2)-Round(A.Money_Per*B.CMS_Pri,2)-Round(A.Money_Per*B.Cus_PointUse,2)-Round(A.Money_Per*B.Sub_Pri,2)-Round(A.Money_Per*B.Gift_Pri,2)-Round(A.Money_Per*B.CashBack_PointUse,2)-Round(A.Money_Per*B.Cut_Pri,2)-Round(A.Money_Per*B.BC_Coupon_DC,2)-Round(A.Money_Per*B.BC_Card_DC,2) '현금금액', " 
				+ " A.Card_Pri '카드금액', Round(A.Money_Per*B.Dec_Pri,2) '외상금액',  Round(A.Money_Per*B.Cus_PointUse,2) '포인트사용', "
				+ " '배달구분'=Case When B.Delivery_YN='0' Then'일반' When B.Delivery_YN='1' Then'내방' Else '전화' End "	
				+ " From SaD_"+tableName+" A Right Join SaT_"+tableName+" B On A.Sale_Num=B.Sale_Num Left Join Customer_Info C On A.Cus_Code = C.Cus_Code " 
				+ " Where "+Card_Yn+" A.Sale_Num IN ( "
				+ " Select Sale_Num " 
				+ " From SaD_"+tableName+" " 
				+ " Where "+Card_Yn+" Office_Code Like '%"+customerCode+"%' And Office_Name Like '%"+customerName+"%'  AND Sale_Date >= '"+period1+"' AND Sale_Date <= '"+period2+"' " 
				+ " AND Sale_Num LIKE '%"+paper+"%' "
				+ " Group By Sale_Num "
				+ "	 ) ";
		query +=" UNION ALL ";
					}
				}
		query = query.substring(0, query.length()-11);	
		query += " ) G " 
				+ " Group BY G.Sale_Num, G.Sale_Date, G.Pos_No, G.회원, G.배달구분 "
				+ " Order by G.Sale_Num ";

		
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
					updateListForTab2(results);
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
	
	//두번째탭
	private void updateListForTab2(JSONArray results)
	{				
		for(int index = 0; index < results.length() ; index++) {
			// Sale_Date, Sale_Time, Pos_No, Sale_Num, 회원, 판매금액, 반품금액, 할인금액,
			// 순매출, 현금금액, 카드금액, 외상금액, 포인트사용, 배달구분
			try {
				JSONObject son = results.getJSONObject(index);
				HashMap<String, String> map = JsonHelper.toStringHashMap(son);												
				map.put("Sale_Num", map.get("Sale_Num").substring(12).toString());
				map.put("회원", map.get("회원").toString());
				map.put("판매금액", StringFormat.convertToNumberFormat(map.get("판매금액")));
				map.put("반품금액", StringFormat.convertToNumberFormat(map.get("반품금액")));
				map.put("할인금액", StringFormat.convertToNumberFormat(map.get("할인금액")));
				map.put("순매출", StringFormat.convertToIntNumberFormat(map.get("순매출")));				
				map.put("현금금액", StringFormat.convertToIntNumberFormat(map.get("현금금액")));
				map.put("카드금액", StringFormat.convertToIntNumberFormat(map.get("카드금액")));
				map.put("포인트사용", StringFormat.convertToIntNumberFormat(map.get("포인트사용")));				
				map.put("외상금액", map.get("외상금액").toString());
				
				mfillMaps2.add(map);	
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	//세번째 탭
	private void queryListForTab3()
	{
		mfillMaps3.removeAll(mfillMaps3);
				
		String query = "";
		
		//수수료매장 매출 전체 보기 나눠보기 옵션
		//chooseCardYN();
		
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();
		String office_code = m_customerCode.getText().toString();
		
		TextView joenpyonumber = (TextView)findViewById(R.id.editTextJunNumber);
		String m_joenpyonumber = joenpyonumber.getText().toString();
		TextView cardnumber = (TextView)findViewById(R.id.editTextCardNumber);
		String m_cardnumber = cardnumber.getText().toString();		
		//거래일자, 승인일자, 전표번호, 포스번호, 승인구분, 회원번호, 할부, 카드번호, 승인금액, 승인번호, 카드사명		
		query = "select C_SaleDate '거래일자', C_AppDate '승인일자', C_JeonPyo '전표번호',  C_PosNo '포스번호', " 
				+ " '승인구분' = case When C_SaleType='0' Then '승인' else '취소' end, C_Month '할부', "
				+ " C_MemID '회원번호', C_CardNumber '카드번호', C_Price '승인금액', "	
				+ " C_AppNumber '승인번호', C_CardName '카드사명' "
				+ " from Card_log " 
				+ " where C_SaleDate <= '"+period1+"'  and C_SaleDate >= '"+period2+"' and Office_Code Like '%"+office_code+"%' "
				+ " and C_CardNumber Like '%"+m_cardnumber+"%' and C_JeonPyo Like '%"+m_joenpyonumber+"%' ";
		
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
		
	//세번째 탭
	private void updateListForTab3(JSONArray results)
	{	
		
		for(int index = 0; index < results.length() ; index++) {			
			try {
				JSONObject son = results.getJSONObject(index);
				HashMap<String, String> map = JsonHelper.toStringHashMap(son);
				map.put("승인금액", StringFormat.convertToIntNumberFormat(map.get("승인금액")));

/* 2021.09.27.김영목 카드번호 트리밍 에러 생략 */
//				String cardnum = map.get("카드번호").toString();
//				if ( cardnum.length() > 1 && cardnum.length() < 16 ){
//					map.put("카드번호", cardnum.substring(0, 11)+"****" );
//				}else if ( cardnum.length() == 16 ) {
//					map.put("카드번호", cardnum.substring(0, 12)+"****" );
//				}else{
//					map.put("카드번호", cardnum);
//				}

				mfillMaps3.add(map);	
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	/*
	private void queryListForTab4()

	{
		mfillMaps4.removeAll(mfillMaps4);
		
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
		
		query = " Select G.Office_Code, G.Office_Name, "
			      + "       Sum (G.순매출) '순매출', Sum(G.과세매출) '과세매출', Sum(G.면세매출) '면세매출', "
			      + "       Sum (G.현금매출) '현금매출', Sum (G.카드매출) '카드매출', Sum (G.수_카드금액) '수_카드금액', "
			      + "       Sum (G.매장수수료) '매장수수료', Sum (G.카드수수료) '카드수수료', Sum (G.S_Point) '포인트', "
			      + "       Sum (G.S_CashBackPoint) '캐쉬백', Sum (G.이익금) '이익금', "
			      + "       '이익률'=Case When Sum(G.이익금)=0 Or Sum(G.순매출)=0 Then 0 Else (Sum(G.이익금)/Sum(G.순매출))*100 End "
			+ " From ( ";

			for ( int y = year1; y <= year2; y++ ) {
						int m1 = 1, m2 = 12;
						if (y == year1) m1 = month1;
						if (y == year2) m2 = month2;
						for ( int m = m1; m <= m2; m++ ) {
							
			String tableName = String.format("%04d%02d", y, m);

			query += "     Select A.Office_Code, A.Office_Name,  Sum (a.TSell_Pri - a.TSell_RePri) '순매출', "
			+"           '과세매출'=Sum(Case When A.Tax_YN='1' Then A.TSell_Pri-A.TSell_RePri Else 0 End),"
			+"           '면세매출'=Sum(Case When A.Tax_YN='0' Then A.TSell_Pri-A.TSell_RePri Else 0 End),"
			+"           Sum ((a.TSell_Pri - a.TSell_RePri) - a.Card_Pri) '현금매출',"
			+"           Sum (a.Card_Pri) '카드매출', '수_카드금액'=Sum(Case When A.Card_YN='1' Then A.Card_Pri Else 0 End),"
			+"           '매장수수료'=Sum(Case When A.Sale_YN='1' Then (A.TSell_Pri+A.Dc_Pri)*(A.Fee/100) Else ((A.TSell_RePri+A.Dc_Pri)*(A.Fee/100))*-1 End),"
			+"           '카드수수료'=Sum(Case When A.Card_YN='0' Then a.Card_Pri * (a.Card_Fee / 100) Else 0 End),"
			+"           Sum(A.S_Point) S_Point, Sum(A.S_CashBackPoint) S_CashBackPoint , Sum (a.Profit_Pri) '이익금'"
			+"     From SaD_"+tableName+" A "
			+"          LEFT JOIN  SaT_"+tableName+" C ON A.Sale_Num=C.Sale_Num "
			+"          LEFT JOIN Office_Manage B ON A.Office_Code=B.Office_Code "
			+"          LEFT JOIN Cash_Receip_Log F ON A.SALE_NUM=F.C_JEONPYO "
			+"     Where B.Office_Sec = '2' And A.Office_Code Like '%"+customerCode+"%' And "
			+"            A.Office_Name Like '%"+customerName+"%' AND A.Sale_Date >= '"+period1+"' AND "
			+"            A.Sale_Date <= '"+period2+"' Group By A.Office_Code, A.Office_Name "
			+" UNION ALL ";
						}
			}		
			query = query.substring(0, query.length()-11);
			query +="     ) G "
			+"     Group By G.Office_Code,G.Office_Name ";
		
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
				if (results.length()>0)
					updateListForTab4(results);
				adapter4.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();	
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				adapter4.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
				
			}
	    }).execute(m_ip+":"+m_port, "TIPS", "sa", "tips", query);
	}
		
	private void updateListForTab4(JSONArray results)
	{
		for(int index = 0; index < results.length() ; index++) {
			
			try {
				JSONObject son = results.getJSONObject(index);
				HashMap<String, String> map = JsonHelper.toStringHashMap(son);
				map.put("순매출", StringFormat.convertToNumberFormat(map.get("순매출")));
				map.put("수카드금액", StringFormat.convertToNumberFormat(map.get("수_카드금액")));
				map.put("과세매출", StringFormat.convertToNumberFormat(map.get("과세매출")));
				map.put("면세매출", StringFormat.convertToNumberFormat(map.get("면세매출")));
				map.put("현금매출", StringFormat.convertToIntNumberFormat(map.get("현금매출")));
				map.put("카드매출", StringFormat.convertToIntNumberFormat(map.get("카드매출")));
				mfillMaps4.add(map);	
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	*/
	//바코드 스캐너 목록 검색 값 돌려 받기
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
	
	//거래처 검색
	public void onCustomerSearch(View view)
	{
		String customer = m_customerCode.getText().toString();
		String customername = m_customerName.getText().toString();
		Intent intent = new Intent(this, ManageCustomerListActivity.class);
		intent.putExtra("customer", customer);
		intent.putExtra("customername", customername);
		intent.putExtra("customerGubun", "1");
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
        	// 바코드 검색 버튼 클릭시 나오는 목록 셋팅
    		final String[] option = new String[] { "목록", "카메라"};
    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option);
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
	
	// TODO : ChooseCardYN함수 생성 매장매출 검색시 다매장 매출 옵션 설정 변경 
	public void chooseCardYN() {
	/*final String[] option1 = new String[] { "전체보기", "매장매출", "다중매출"};
	ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option1);
	AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
	builder1.setTitle("Select Option");
	
	// 목록 선택시 이벤트 처리
	builder1.setAdapter(adapter1, new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int which) {

			if(which == 1){ // 목록으로 조회할 경우
				Card_Yn = " A.Card_Yn = '0' AND ";
			}else if(which == 2) { // 스캔할 경우	
				Card_Yn = " A.Card_Yn = '1' AND ";
			}else{
				Card_Yn = " A.Card_Yn Like '%' AND ";
			}
		}
	}); 
	builder1.show();	*/
		if(m_viewOption.equals("cardY")){ // 목록으로 조회할 경우
			Card_Yn = " A.Card_Yn = '0' AND ";
		}else if(m_viewOption.equals("cardN")) { // 스캔할 경우	
			Card_Yn = " A.Card_Yn = '1' AND ";
		}else{
			Card_Yn = " ";
		}
	}
		
	// MSSQL
	// SQL QUERY 실행
	/*public void doQueryWithBarcode () {
		
		String query = "";
		String barcode = m_barCode.getText().toString();		
		query = "SELECT * FROM Goods WHERE Barcode = '"+barcode+"';";
	
		if (barcode.equals("")) return;
		
	    // 콜백함수와 함께 실행
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
					Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
				}
			}
	    }).execute(m_ip+":"+m_port, "TIPS", "sa", "tips", query);		
	}*/
	
	// 거래처 코드로 거래처명 자동 완성
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
		            Toast.makeText(getApplicationContext(), "조회 완료", Toast.LENGTH_SHORT).show();
				}
				else {
		            Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();					
				}
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		 //매출보기 옵션 설정
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
      	m_viewOption = pref.getString("prefSaleViewMethod", "");		
      	
      //수수료매장 매출 전체 보기 나눠보기 옵션
      chooseCardYN();
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
			actionbar.setTitle("매출관리");
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

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