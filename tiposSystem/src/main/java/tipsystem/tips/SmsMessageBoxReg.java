package tipsystem.tips;

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
import tipsystem.utils.MSSQL2;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class SmsMessageBoxReg extends Activity{

	JSONArray m_results;
	JSONObject m_shop;
	JSONObject userProfile;

	String userid = "";
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

	EditText m_edittextNumber;
	EditText m_edittextComment;
	TextView m_textviewStrLength;
	Button m_buttonSave;	
	
	String m_modify = "y";
	HashMap<String, String> hashMap = new HashMap<String, String>(); 
	
	// loading bar
	private ProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_code_smsmessagereg);
		//linearlayout_messagebox
		m_shop = LocalStorage.getJSONObject(this, "currentShopData");
		
	    //등록수정자 불러오기
		userProfile = LocalStorage.getJSONObject(this, "userProfile");
				
        try {
			userid = userProfile.getString("User_ID");

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
        
        Intent intent = getIntent();
        try{
        	m_modify = hashMap.put("Modify", intent.getStringExtra("Modify").toString());
	        hashMap.put("SMS_Num", intent.getStringExtra("SMS_Num").toString());
	        hashMap.put("SMS_Memo", intent.getStringExtra("SMS_Memo").toString());
	        hashMap.put("Write_Date", intent.getStringExtra("Write_Date").toString());
	        hashMap.put("Edit_Date", intent.getStringExtra("Edit_Date").toString());
	        hashMap.put("Writer", intent.getStringExtra("Writer").toString());
	        hashMap.put("Editor", intent.getStringExtra("Editor").toString());
        }catch(NullPointerException e){
        	
        }      
        
        m_edittextComment = (EditText)findViewById(R.id.editText_comment);
        m_edittextNumber= (EditText)findViewById(R.id.editText_number);
        m_textviewStrLength= (TextView)findViewById(R.id.textview_strlength);
        m_buttonSave= (Button)findViewById(R.id.button_save);
        
        m_buttonSave.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {			
				if(v.getTag().equals("등록")) {
					doSaveMessage();
				}else if(v.getTag().equals("수정")){
					doModify();
				}
			}
		});
        
        //텍스트 변경시 변경된 숫자 계산해서 넣기
        m_edittextComment.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { 
				if(s.length() >= 140 ){					
					m_textviewStrLength.setText("MMS \n" + String.valueOf(s.length()) );
					m_textviewStrLength.setTextColor(Color.GREEN);
				}else{
					m_textviewStrLength.setText(String.format("%d", s.length()));
					m_textviewStrLength.setTextColor(Color.WHITE);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				
			}
		});
                 
       //mfillMaps = intent.getExtras().getString("fillmaps");
       if(hashMap.isEmpty()){
    	   doSearchNumber();
       }else{
    	  doSetting();
       }
	}
	
	public void doSetting() {
		m_edittextNumber.setText(hashMap.get("SMS_Num").toString());
		m_edittextComment.setText(hashMap.get("SMS_Memo").toString());		
		//m_buttonSave.setBackgroundResource(R.drawable.change_btn);
		m_buttonSave.setTag("수정");
		m_buttonSave.setBackgroundResource(R.drawable.change_btn);		
		Log.i("변경여부", "변경완료되었습니다.");
	}	
	
	public void doSaveMessage(){
		
		SimpleDateFormat m_dateFormatter;	
		Calendar m_dateCalender1;
		
		m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
		m_dateCalender1 = Calendar.getInstance();
		String period = m_dateFormatter.format(m_dateCalender1.getTime());
		
		String number = m_edittextNumber.getText().toString();
		
		// 쿼리 작성하기    	
		String comments = m_edittextComment.getText().toString();
		comments = comments.replace("'", "''");
		String query = "";
    	
		query = " Insert Into SMS_Msg (SMS_Num, SMS_Memo, Write_Date, Edit_Date, Writer, Editor) "
				+ "Values ("+number+", '"+comments+"', '"+period+"','"+period+"','"+userid+"','"+userid+"'); " 
				+ " Select Max(SMS_Num) As 최대메세지함번호 From SMS_Msg ";

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
					finish();
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
		
	public void doModify(){
		
		SimpleDateFormat m_dateFormatter;	
		Calendar m_dateCalender1;
		
		m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
		m_dateCalender1 = Calendar.getInstance();
		String period = m_dateFormatter.format(m_dateCalender1.getTime());
		
		String number = m_edittextNumber.getText().toString();
		
		// 쿼리 작성하기    	
		String comments = m_edittextComment.getText().toString();
		comments = comments.replaceAll("'", "''");
		String query = "";
    	
		query = " update sms_msg set sms_memo = '"+comments+"', edit_date = '"+period+"', "
				+ " editor='"+userid+"' where sms_num = '"+number+"' "				
				+ " Select Max(SMS_Num) As 최대메세지함번호 From SMS_Msg ";

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
					finish();
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
	
	// 메세지 번호 불러 오기
    public void doSearchNumber() {
    	    	
    	// 쿼리 작성하기    	
		String query = "";
    	query = "Select Max(SMS_Num) As 최대메세지함번호 From SMS_Msg";

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
					JSONObject json;
					try {
						json = results.getJSONObject(0);
						HashMap<String, String> map = JsonHelper.toStringHashMap(json);
						int a = Integer.parseInt(map.get("최대메세지함번호")) + 1 ;
						m_edittextNumber.setText(String.format("%d", a));
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				else {
					m_edittextNumber.setText("1");
					//Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();							
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
