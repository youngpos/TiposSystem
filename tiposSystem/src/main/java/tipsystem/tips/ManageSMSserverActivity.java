package tipsystem.tips;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.GCMServer;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.MSSQL3;
import tipsystem.utils.StringFormat;

import kr.co.tipos.tips.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;


public class ManageSMSserverActivity extends Activity implements DatePickerDialog.OnDateSetListener {
	
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

	String m_OfficeCode = "";
	
	
	//돌려받기 위해서 정의
	int MESSAGE_SET=0;
	
	DatePicker m_datePicker;
	
	SimpleDateFormat m_dateFormatter;
	Calendar m_dateCalender1;
	Calendar m_dateCalender2;
	int m_dateMode=0;
	
	Spinner m_spinnerDate;
	Spinner m_spinnerGubun;
	Spinner m_spinnerClass;
	Spinner m_spinnerSMSonOff;
	
	ArrayAdapter<CharSequence>  adspin1;
	ArrayAdapter<CharSequence>  adspin2;
	ArrayAdapter<CharSequence>  adspin3;
	ArrayAdapter<CharSequence>  adspin4;
	
	Button m_period1;
	Button m_period2;
	
	EditText m_edittextGubun1;
	EditText m_edittextGubun2;
	EditText m_edittextAddress;
	
	Button m_mseeageSelect;
	Button m_reNew;
	Button m_search;
	
	String m_messageNumber;
	EditText m_message;
	
	//상단 검색 문잘 발송건수
	TextView m_totalSMS;
	//하단 전송 가능건수
	TextView m_totalSMScount;
	
	ListView m_listSMS;
	
	Button m_sendMessage;	
	
	String junpyo_Number;
	String posID;
	int m_smsTotal = 0;
	
	//전표번호 전송을 위해서
	ArrayList<String> list_junpyo = new ArrayList<String>();
	
	// loading bar
	private ProgressDialog dialog; 		
	
	//휴대폰 정보 담겨 있는곳 (총발송수량, 매장당 발송 댓수)
	List<HashMap<String, String>> m_handPhoneList = new ArrayList<HashMap<String, String>>();
	//전송할 메세지를 담는 곳( 각 휴대폰별 발행 가능 메세지및 회원정보 )	
	List<HashMap<String, String>> m_SMSList = new ArrayList<HashMap<String, String>>();
	//목록에 표시할 전송 가능 건수들
	List<HashMap<String, String>> m_SendList = new ArrayList<HashMap<String, String>>();
	SimpleAdapter adapter;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_smsserver);			
						
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

			m_OfficeCode = m_shop.getString("OFFICE_CODE");
	        posID = LocalStorage.getString(this, "currentPosID:"+m_OfficeCode);	
	    } catch (JSONException e) {
			e.printStackTrace();
		}
        
		//첫번째 조회구분 스피너설정
		m_spinnerDate = (Spinner) findViewById(R.id.spinner1);
		adspin1 = ArrayAdapter.createFromResource(this, R.array.sms_spinner1,    android.R.layout.simple_spinner_item);		  
		adspin1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_spinnerDate.setAdapter(adspin1);
		m_spinnerDate.setOnItemSelectedListener(new OnItemSelectedListener() {          
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
		
			}
		});
		
		
		//두번째 구분 스피너 설정
		m_spinnerGubun = (Spinner) findViewById(R.id.spinner2);		
		adspin2 = ArrayAdapter.createFromResource(this, R.array.sms_spinner2,    android.R.layout.simple_spinner_item);		  
		adspin2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_spinnerGubun.setAdapter(adspin2);
		m_spinnerGubun.setOnItemSelectedListener(new OnItemSelectedListener() {          
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub		
			}
		});
		
		//세번째 스피너
		m_spinnerClass = (Spinner)findViewById(R.id.spinner3);			
		adspin3 = ArrayAdapter.createFromResource(this, R.array.sms_spinner3,    android.R.layout.simple_spinner_item);		  
		adspin3.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_spinnerClass.setAdapter(adspin3);
		m_spinnerClass.setOnItemSelectedListener(new OnItemSelectedListener() {          
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
		
			}
		});		
		
		m_spinnerSMSonOff = (Spinner)findViewById(R.id.spinner4);		
		adspin4 = ArrayAdapter.createFromResource(this, R.array.sms_spinner4,    android.R.layout.simple_spinner_item);		  
		adspin4.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		m_spinnerSMSonOff.setAdapter(adspin4);
		m_spinnerSMSonOff.setOnItemSelectedListener(new OnItemSelectedListener() {          
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
		
			}
		});
		m_spinnerSMSonOff.setSelection(1);
		
		//날자 구성 하기
		m_period1 = (Button) findViewById(R.id.button_date1); 
		m_period2 = (Button) findViewById(R.id.button_date2);
		
		//오늘 날짜 리셋 하기 입니다.
		onClickDateReSet();		
		
		//객체 선언 합니다.
		m_edittextGubun1 = (EditText)findViewById(R.id.editText_search01);
		m_edittextGubun2 = (EditText)findViewById(R.id.editText_search02);
		m_edittextAddress = (EditText)findViewById(R.id.editText_address);
		
		
		//메세지 박스 입니다.
      	m_message = (EditText)findViewById(R.id.edittext_smsmessage);
        Intent intent = getIntent();
        intent.getStringExtra("SMS_Num");
        intent.getStringExtra("SMS_Memo");
        m_messageNumber = intent.getStringExtra("SMS_Num");
        String str = intent.getStringExtra("SMS_Memo");
        m_message.setText(str);
        
        //전송 목록 리스트뷰
        m_listSMS = (ListView)findViewById(R.id.listView_smsList);
        
        String[] from = new String[] { "Num", "CallBack", "SendTotal", "TotalSMSCount" };
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4 };
                
        // TODO : 거래처관리 첫번째 탭 리스트뷰
        adapter = new SimpleAdapter(this, m_SendList, R.layout.activity_listview_item2, from, to);		
        m_listSMS.setAdapter(adapter);	
        
        m_totalSMScount = (TextView)findViewById(R.id.textView_smstotalcount);
        m_totalSMS = (TextView)findViewById(R.id.textview_smstotal);
        //전송 가능건수 가져오기
        doHandPhoneCount();
        
        m_sendMessage = (Button)findViewById(R.id.button_smssendmessage);
        m_sendMessage.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {				
				if(m_SMSList.isEmpty()){
					Toast.makeText(getApplicationContext(), "회원 검색 후 사용해 주세요", Toast.LENGTH_SHORT).show();
					return;
				}				
				
				if( m_totalSMScount.getText().equals("0") ){
					Toast.makeText(getApplicationContext(), "발송 가능 건수가 0건 입니다. 내일 다시 시도해 주세요!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				//EditText et = (EditText)findViewById(R.id.edittext_smsmessage); 메세지 박스
				if(m_message.getText().toString().equals("")){
					Toast.makeText(getApplicationContext(), "메세지를 선택해 주세요!", Toast.LENGTH_SHORT).show();
					return;
				}
				
				//전표번호 생성하기
				junpyoCreate();		
				
				Toast.makeText(getApplicationContext(), "발송 완료", Toast.LENGTH_SHORT).show();				
			}
		});
       
	}
	
	public void memberSearch(View v) {	
		//목록 초기화
		m_SendList.removeAll(m_SendList);
		m_totalSMS.setText("");
		String query = "";
		
		String dateGubun =  m_spinnerDate.getSelectedItem().toString();
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();
		
		if(dateGubun.equals("최종방문일")){
			dateGubun = " And Vis_Date Between '"+period1+"' And '"+period2+"' " ;
		}else{
			dateGubun = "";
		}
		
		String searchGubun = m_spinnerGubun.getSelectedItem().toString();
		String edittext_search01 = m_edittextGubun1.getText().toString();
		if(!searchGubun.equals("조회구분")){
			if(edittext_search01.equals("")){
				Toast.makeText(getApplicationContext(), "첫번째 조건을 입력해 주세요!", Toast.LENGTH_SHORT).show();
				return;
			}
			String edittext_search02 = m_edittextGubun2.getText().toString();
			if(edittext_search02.equals("")){
				Toast.makeText(getApplicationContext(), "두번째 조건을 입력해 주세요!", Toast.LENGTH_SHORT).show();
				return;
			}
			
			if(searchGubun.equals("구매금액")){
				searchGubun = " And (Pur_Pri Between "+edittext_search01+" And "+edittext_search02+") ";
			}else if(searchGubun.equals("미수금액")){
				searchGubun = " And (Dec_Pri Between "+edittext_search01+" And "+edittext_search02+") ";
			}else if(searchGubun.equals("포인트")){
				searchGubun = " And (Cus_Point Between "+edittext_search01+" And "+edittext_search02+") ";
			}
		}else{
			searchGubun = "";
		}
				
		String memberClass = m_spinnerClass.getSelectedItem().toString();
			if(memberClass.equals("일반")){
				memberClass = " And Cus_Class = '1' ";
			}else if(memberClass.equals("실버")){
				memberClass = " And Cus_Class = '2' ";
			}else if(memberClass.equals("골드")){
				memberClass = " And Cus_Class = '3' ";
			}else if(memberClass.equals("VIP")){
				memberClass = " And Cus_Class = '4' ";
			}else if(memberClass.equals("기타")){
				memberClass = " And Cus_Class = '5' ";
			}else{
				memberClass = "";
			}
		String smsOnOff = m_spinnerSMSonOff.getSelectedItem().toString();
			if(smsOnOff.equals("적용")){
				smsOnOff = " And HPSend_YN = '1' ";
			}else if(smsOnOff.equals("미적용")){
				smsOnOff = " And HPSend_YN = '0' ";
			}else{
				smsOnOff = "";
			}
		String address = m_edittextAddress.getText().toString();		
			if(!address.equals("")){
				address = " And (ADDRESS1 Like '%"+address+"%' Or ADDRESS2 Like '%"+address+"%') ";
			}else{
				address = ""; 
			}
			
			//전화 번호에 휴대폰 번호가 등록 되어 있어도 불러 지는 쿼리
			query += " Select Cus_Class, Cus_Name, Cus_Code, " 
					+ " case when Cus_Mobile = '' then convert(varchar(15),CUS_Tel) else convert(varchar(15),CUS_Mobile) end Cus_Moblie "	
					+ " From Customer_Info "  
					+ " Where Cus_Code In ( "  
					+ " Select Max(Cus_Code) "  
					+ " From Customer_Info "  
					+ " Where ( ( Ltrim(Cus_Mobile) <> '' AND Left(Ltrim(Cus_Mobile), 2) = '01' ) or ( Ltrim(Cus_Mobile) = '' AND Ltrim(Cus_Tel) <> '' AND Left(Ltrim(Cus_Tel), 2) = '01') ) "
					+ " AND Cus_Use = '1' "  
					+ smsOnOff
					+ memberClass
					+ dateGubun
					+ searchGubun
					+ address
					+ " Group By Cus_Code ) " //Cus_Mobile) " 정식 버젼에선 중복 방지를 위해서 풀어야 합니다. 
					+ " Order by Cus_Code ";
		
			//전화 번호에 휴대폰 번호가 저장 되있으면 안불러 지는 쿼리	
		/*query += "Select Cus_Class, Cus_Name, Cus_Code, convert(varchar(15),CUS_Mobile) CUS_Mobile " 
				+ " From Customer_Info " 
				+ " Where Cus_Code In ( "     
				+ " Select Max(Cus_Code) " 
				+ " From Customer_Info "     
				+ " Where Ltrim(Cus_Mobile) <> '' And Left(Ltrim(Cus_Mobile), 2) = '01' "
				+ " AND Cus_Use = '1' "
				+ smsOnOff
				+ memberClass
				+ dateGubun
				+ searchGubun
				+ address
				+ " Group By  Cus_Mobile) "  
				+ " Order by Cus_Code ";*/
		
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
				if (results.length()>0) {
					//조회된 수량을 표시해줍니다.
					m_totalSMS.setText(String.valueOf((results.length())));
					
					//조회된 회원 휴대폰 정보를 가지고 있는 휴대폰 별로 구분 합니다.
					updateSMSsendMessage(results);
					adapter.notifyDataSetChanged();	
				}
				else {
		            Toast.makeText(getApplicationContext(), "조회 결과가 없습니다.", Toast.LENGTH_SHORT).show();
		            adapter.notifyDataSetChanged();
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
		    	Toast.makeText(getApplicationContext(), "회원정보를 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);

	}		
	
	
	// 문자전송 건수별 휴대폰 분리 하기
	private void updateSMSsendMessage(JSONArray results){
			// Start time
			long startTime = System.currentTimeMillis();
			
			m_SMSList.clear();
			//전송건수 : totalsmsCount   휴대폰댓수 : m_handPhoneList.size()
			//휴대폰별 전송가능건수 : m_handPhoneList.get("Send_Total")
			//전송대기건수 : m_totalSMS.getText(), 
			
			//회전한 수량 만큼 증가하기
			int totalsmsCount = 0;
			
			String phoneNumber = LocalStorage.getString(this, "phoneNumber");	
				//휴대폰 댓수별 회전
				for( int i = 0; i < m_handPhoneList.size(); i++){
					JSONObject json;
					HashMap<String, String> map = new HashMap<String, String>();
					//전송가능 나눈 건수 별 표시하기
					HashMap<String, String> map_sendList = new HashMap<String, String>();								
							
					
					int totalsms = 0;
					
					//전송 가능 휴대폰 정보 
					map.putAll(m_handPhoneList.get(i));
					//휴대폰별 전송가능 건수
					int sendtotal = Integer.parseInt(map.get("SendTotal"));
								
					//(목록)순번
					map_sendList.put("Num", String.valueOf((i+1)));
					
					map_sendList.put("CallBack", map.get("APP_HP"));
					map_sendList.put("SendTotal", map.get("SendTotal"));
					map_sendList.put("REGID", map.get("REGID"));
				
					for(int j = 0; j < sendtotal; j++ ){
						HashMap<String, String> smsphone = new HashMap<String, String>();
						try{
							HashMap<String, String> member_map = new HashMap<String, String>();
							
							json = results.getJSONObject(totalsmsCount);
							member_map = JsonHelper.toStringHashMap(json);						
							//발송요청한 휴대폰 번호
							smsphone.put("Pos_Num", phoneNumber);
							//회원 정보 삽입
							smsphone.put("Cus_Code", member_map.get("Cus_Code"));
							smsphone.put("Cus_Name", member_map.get("Cus_Name"));
							smsphone.put("Phone_Number", member_map.get("Cus_Moblie").trim().replaceAll("-", ""));
							//보낼분 정보 삽입
							smsphone.put("CallBack", map.get("APP_HP"));						 
						}catch(JSONException e){
							e.printStackTrace();
						}
						totalsms++;
						totalsmsCount++;					
						m_SMSList.add(smsphone);
						if(totalsmsCount == results.length() ) break;
					}
					map_sendList.put("TotalSMSCount", String.valueOf(totalsms));
					m_SendList.add(map_sendList);
					
					if(totalsmsCount == results.length() ) break;
				}
				
				// End time
				long endTime = System.currentTimeMillis();
				 
				// Total time
				long lTime = endTime - startTime;
				Log.i("endtime", "TIME : " + lTime + "(ms)");				
		}
	
	
	//날자 리셋 하기 함수
	private void onClickDateReSet() {
		m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
		
		m_dateCalender1 = Calendar.getInstance();
		m_dateCalender2 = Calendar.getInstance();
		
		m_period1.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
		m_period2.setText(m_dateFormatter.format(m_dateCalender2.getTime()));
	}
	
	//새로입력 버튼 리스너함수
	public void onClickReNew(View v) {
		m_spinnerDate.setSelection(0);
		m_spinnerGubun.setSelection(0);
		m_spinnerClass.setSelection(0);
		m_spinnerSMSonOff.setSelection(1);	
	
		m_edittextGubun1.setText("");
		m_edittextGubun2.setText("");
		m_edittextAddress.setText("");
		
		m_message.setText("");
		m_message.setHint("전송하실 문자 메세지를 선택해 주세요!");
		
		m_totalSMScount.setText("0");
		m_totalSMS.setText("0");
		
		m_SendList.removeAll(m_SendList);
		adapter.notifyDataSetChanged();
		
		m_SMSList.removeAll(m_SMSList);
		
		onClickDateReSet();
		doHandPhoneCount();
	}
	
	
	//새로입력 버튼 리스너함수
		public void onClickReNewRe() {
			m_spinnerDate.setSelection(0);
			m_spinnerGubun.setSelection(0);
			m_spinnerClass.setSelection(0);
			m_spinnerSMSonOff.setSelection(1);	
		
			m_edittextGubun1.setText("");
			m_edittextGubun2.setText("");
			m_edittextAddress.setText("");
			
			m_message.setText("");
			m_message.setHint("전송하실 문자 메세지를 선택해 주세요!");
			
			m_totalSMScount.setText("0");
			m_totalSMS.setText("0");
			
			m_SendList.removeAll(m_SendList);
			adapter.notifyDataSetChanged();
			
			m_SMSList.removeAll(m_SMSList);
			
			onClickDateReSet();
			doHandPhoneCount();
		}
	
	//첫번째 날자 설정 리스너
	public void onClickSetDate1(View v) {
		
		DatePickerDialog newDlg = new DatePickerDialog(this, this,
				m_dateCalender1.get(Calendar.YEAR),
				m_dateCalender1.get(Calendar.MONTH),
				m_dateCalender1.get(Calendar.DAY_OF_MONTH));
		 newDlg.show();
		
		 m_dateMode = 1;
	};
	
	//두번째 날자 설정 리스너
	public void onClickSetDate2(View v) {
		
		DatePickerDialog newDlg = new DatePickerDialog(this, this, 
				m_dateCalender2.get(Calendar.YEAR),
				m_dateCalender2.get(Calendar.MONTH),
				m_dateCalender2.get(Calendar.DAY_OF_MONTH));
		
		newDlg.show();
		
		m_dateMode = 2;
	};
	
	//달력 선택 화면 콜백 리스너
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
	
	public void onClickMessageSelected(View v) {
		Intent intent = new Intent(this, ManageCodeSmsMessageBoxActivity.class);		
		intent.putExtra("MODE", "sendMsg");
		startActivityForResult(intent, MESSAGE_SET);		
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode==MESSAGE_SET ) // 액티비티가 정상적으로 종료되었을 경우
        {   
            m_messageNumber = data.getStringExtra("SMS_Num");
            String str = data.getStringExtra("SMS_Memo");
            m_message.setText(str);        
        }
    }   

	//전송 가능 건수 하단 표시 하기 및 계산
	private void doHandPhoneCount(){
		//초기화
		m_handPhoneList.clear();		
		m_smsTotal = 0;
		String date = m_dateFormatter.format(m_dateCalender1.getTime()).toString();
		String query = "";
		JSONArray shopsPhones= LocalStorage.getJSONArray(ManageSMSserverActivity.this, "shopsPhone");
		
		Log.d("shopsPhones -> 휴대폰저장수", String.valueOf(shopsPhones.length()));
		for(int i = 0 ; i < shopsPhones.length(); i++ ){
			HashMap<String, String> m_handPhone =new HashMap<String, String>();
			try {					
	        	JSONObject json = shopsPhones.getJSONObject(i);
	        	m_handPhone = JsonHelper.toStringHashMap(json);   
	        	m_handPhone.put("SendTotal", "0");
			}catch(JSONException e){
				e.printStackTrace();
			}
				m_handPhoneList.add(m_handPhone);
		}
		
		Log.d("휴대폰 대수", String.format("%d", m_handPhoneList.size()));
		
		//select  callback, ISNULL( 500-sum(sms_count), 0) Total from hphone_sms_send where CONVERT(char(10), date, 23)='2014-06-13' group by callback
		query = " select  CallBack, ISNULL(sum(sms_count), 0) Total from hphone_sms_send "
				+ "where CONVERT(char(10), date, 23)='"+date+"' group by CallBack ";	
		
		//로딩 다이알로그 
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
					JSONObject json;
					//전체 휴대폰번호를 불러와서 비교 합니다.
					for(int i = 0; i < m_handPhoneList.size(); i++){					
					HashMap<String, String> map_TotalPhone = m_handPhoneList.get(i);	
					//String totalPhone_Count = map_TotalPhone.get("Total");
					
					m_smsTotal += 500;
					map_TotalPhone.put("SendTotal", "500");
					
						for(int j = 0 ; j < results.length(); j++ ){
							//문자 전송 수량 계산하기							
							
							try {
								json = results.getJSONObject(j);
								HashMap<String, String> map_SendPhone = JsonHelper.toStringHashMap(json);
								String sendSMS_total = map_SendPhone.get("Total");
								//두 번호를 비교해서 보낸 것이 있다면 전송 건수를 차감해 주세요
								if(map_TotalPhone.get("APP_HP").equals(map_SendPhone.get("CallBack"))){
									
									//휴대폰 값이 둘이 같다면 총보낸것에 더하기를 하고 전송 건수를 추가해줍니다.
									int send = Integer.parseInt(map_TotalPhone.get("SendTotal")) - Integer.parseInt(sendSMS_total);
									
									//전송 수량 만큰 빼야 합니다.
									m_smsTotal -= Integer.parseInt(sendSMS_total);
									
									//전송가능건수 입력									
									map_TotalPhone.put("SendTotal", String.valueOf(send));									
								}
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						//휴대폰 목록에 전송 가능건수 삽입 합니다.
						m_handPhoneList.set(i, map_TotalPhone);
					}
					m_totalSMScount.setText(String.valueOf(m_smsTotal));
				} else {
					//문자 전송 건수 검색시 아무 것도 보낸 것이 없을때 나옵니다.
					for(int i = 0 ; i < m_handPhoneList.size() ; i++ ){
						HashMap<String, String> map1 = m_handPhoneList.get(i);
						Log.i("map:Total", map1.get("APP_HP"));
						m_smsTotal += 500;
						map1.put("SendTotal", "500");
						m_handPhoneList.set(i, map1);
					}
					m_totalSMScount.setText(String.valueOf(m_smsTotal));
				}
				
				Log.w("종료시 자료", m_handPhoneList.toString());
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();	
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	

	//전표 생성 하기
	private synchronized void  junpyoCreate(){
		String date = m_dateFormatter.format(m_dateCalender1.getTime()).toString();
		String query = "select  top 1 right(SMS_Num, 3)+1 SMS_Num " 
						+ " from hphone_sms_send " 
						+ " where CONVERT(char(10), date, 23)='"+date+"' " 
						+ " group by SMS_Num order by sms_num desc ";
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.show();
 		
		// 콜백함수와 함께 실행
	    new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {				
				if (results.length()>0) {					
					for(int i = 0 ; i < results.length(); i++ ){
						JSONObject json;
						try {
							json = results.getJSONObject(i);
							HashMap<String, String> map = JsonHelper.toStringHashMap(json);		
							junpyo_Number = map.get("SMS_Num").toString();
						} catch (JSONException e) {							 
							e.printStackTrace();
						}
					}
				}
				else {
					junpyo_Number = "1";		            
				}				
				//문자 전송 함수 
				sendSMSMessage();
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
		    	Toast.makeText(getApplicationContext(), "전표번호 생성을 실패하였습니다! ", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);

	}
	
	
	@SuppressWarnings("unchecked")
	private void sendSMSMessage(){
		
		String date = m_dateFormatter.format(m_dateCalender1.getTime()).toString();
		String query = "";					
		String phoneNumber = LocalStorage.getString(getApplicationContext(), "phoneNumber");
		//EditText et = (EditText)findViewById(R.id.edittext_smsmessage);
		String msg = m_message.getText().toString();
		String msgAll  = msg.replaceAll("'", "''");
		String type = "1";
		if ( msg.length() <= 140 && msg.length() >= 0){
			type = "0";
		}
		msg = msg.replaceAll("'", "''").substring(0,  20);		
		
		ArrayList<String> db = new ArrayList<String>();
		db.add(m_ip+":"+m_port);
		//db.add("TIPS");
		//db.add("sa");
		//db.add("tips");
		db.add(m_uudb);
		db.add(m_uuid);
		db.add(m_uupw);
		int year = Integer.parseInt(date.substring(0, 4));
		int month = Integer.parseInt(date.substring(5, 7));
		int day = Integer.parseInt(date.substring(8, 10));
		int junpyo_number = Integer.parseInt(junpyo_Number);	
		
	    String junpyo = String.format("%04d%02d%02d", year, month, day) + posID + String.format("%03d", junpyo_number);
	    Log.d("ManageSMSserver -> ", junpyo.toString());
	    list_junpyo.add(junpyo);
		ArrayList<String> queryList = new ArrayList<String>();		
		//발송 정보 불러 오기 입니다.
		Log.w("r리스트 저장 시작", String.valueOf(m_SMSList.size()));
		//HashMap<String, String> map = new HashMap<String, String>();
		for(int i = 0; i < m_SMSList.size(); i++) {			
			HashMap<String, String> map = new HashMap<String, String>();
			map.putAll(m_SMSList.get(i));
			query = " insert into Hphone_SMS (SMS_Num, Pos_No, Cus_Code, Cus_Name, Phone_Number, CallBack, Msg, type) values ";
			//Pos_Num, Cus_Code, Cus_Name, Phone_Number, CallBack,
			query += " ( '"+junpyo+"', '"+map.get("Pos_Num")+"', '"+map.get("Cus_Code")+"', '"+map.get("Cus_Name")+"', '" ;
			query += map.get("Phone_Number")+"', '"+map.get("CallBack")+"', '"+msg+"', '"+type+"' ) ";		
			queryList.add(query);
		
		}	
		Log.w("r리스트 저장 완료", String.valueOf(queryList.size()));
		
		
		// hphone_sms_send에 등록 합니다.
		for(int i = 0 ; i < m_SendList.size(); i++ ){
			HashMap<String, String> map = new HashMap<String, String>();
			map.putAll(m_SendList.get(i));
			String smscount = map.get("TotalSMSCount");			
			if(!smscount.equals("0")){			
				query = " insert into hphone_sms_send (sms_num, pos_no, callback, msg, sms_count, type ) values ";
				//SMS_Num, Pos_No, CallBack, Msg, Sms_Count, Type
				query += " ('"+junpyo+"', '"+phoneNumber+"', '"+map.get("CallBack")+"', '"+msgAll+"', '"+map.get("TotalSMSCount")+"', '"+type+"' ) ";
				queryList.add(query);
			}
		}		
			
		//Log.i("query", query.toString());	
		
 		
	    // 콜백함수와 함께 실행
	    new MSSQL3(new MSSQL3.MSSQL3CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();				
				sendOK();				
		        Toast.makeText(getApplicationContext(), "전송 완료 했습니다.", Toast.LENGTH_SHORT).show();				
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
		    	Toast.makeText(getApplicationContext(), "등록을 실패하였습니다! ", Toast.LENGTH_SHORT).show();
			}
	    }).execute(db, queryList );
	    //}).execute(m_ip+":"+m_port, "TIPS", "sa", "tips", query);
	}
	
	@SuppressWarnings("unchecked")
	private void sendOK(){
		//푸시 날리기 입니다 네트워크 오류로 인해 AsyncTask 에서 날립니다.
		GCMServer gcm = new GCMServer();
		
		List<String> list = new ArrayList<String>();
		String regId = "";
		for(int i = 0 ; i < m_SendList.size(); i++ ){
			HashMap<String, String> map = m_SendList.get(i);
			regId = map.get("REGID");
			Log.i("regId", regId);
			list.add(regId);
		}
		gcm.execute(list, list_junpyo);
				
		onClickReNew(new View(getApplicationContext()));
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(ManageSMSserverActivity.this,AlertDialog.THEME_HOLO_LIGHT)
	    .setTitle("메세지 전송 완료")
	    .setMessage("메세지 발송 작업이 완료 되었습니다. 전송결과메뉴에서 확인 하세요! ")
	    .setPositiveButton("확인", new DialogInterface.OnClickListener() {			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();				
			}
		});
    	dialog.show();		
	}
	
	
	//사용안합니다.  Task 로 호출 합니다. 	
	public void sendMessage() throws IOException {	 
		Log.d("시작합니다.", "발송 했습니다.");
		//GCM 등록 아이디
		Sender sender = new Sender("AIzaSyBA9BUJnmDP8JU-doB5NLT2VQTpnRPtIAA");
		
		boolean SHOW_ON_IDLE = false;    //기기가 활성화 상태일때 보여줄것인지
		int LIVE_TIME = 1800;  //이건 시간이 아니고 초를 의미..
		//int RETRY = 5;  //메시지 전송실패시 재시도 횟수
		
		//테스트용 핸드폰별 등록 아이디
		//String regId = "APA91bFQ7zXF6_LIJ44u4PXQQOQUSVpoFhO6f2FTueLNI9SVegbYEkxLzrnrwRt12DIL3KEPqSonWXL3Hus1kEdQCamJ7IhaPTSC8p-H2z8hA5xd9z_cCkZfUbV8GPStjMaAl-JDICUtTjGLuQsx8xdx4n5IwLHMV17Eyb0fxw7fK-QePVxt56Y";	 
		String regId = "";
		
		Message message = new Message.Builder()
				.addData("TiposSMS", "SEND")
				.delayWhileIdle(SHOW_ON_IDLE)
                .timeToLive(LIVE_TIME)
                .build();		 

		List<String> list = new ArrayList<String>();
		
		for(int i = 0 ; i < m_SendList.size(); i++ ){
			HashMap<String, String> map = m_SendList.get(i);
			regId = map.get("REGID");
			Log.i("regId", regId);
			list.add(regId);
		}

		MulticastResult multiResult = sender.send(message, list, 5);
		
		if (multiResult != null) {
			List<Result> resultList = multiResult.getResults();	 

			for (Result result : resultList) {
					//System.out.println(result.getMessageId());
				Log.e("PUSH errer", result.getMessageId());
			}
		}
		Log.d("종료합니다.", "발송 완료 했습니다.");
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
