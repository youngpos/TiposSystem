package tipsystem.tips;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.TIPOS;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class ReadQuestionActivity extends Activity {

	// loading bar
	private ProgressDialog dialog; 
	//Spinner m_spin;
	Button m_cancelButton;
	Button m_commentButton;

	JSONObject m_shop;
	JSONObject m_userProfile;
	String m_office_code;
	
	String m_Mode;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_read_question);
		
		//샵정보 불러오기
		m_shop = LocalStorage.getJSONObject(this, "currentShopData"); 
		m_userProfile = LocalStorage.getJSONObject(this, "userProfile"); 
		
        Log.i("currentShopData", m_shop.toString() );
        Log.i("userProfile", m_userProfile.toString() );
			
        try {
        	//오피스 코드 불러오기
			m_office_code = m_shop.getString("OFFICE_CODE");
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
		Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
		
		TextView textView =  (TextView) findViewById(R.id.textView3);
		textView.setTypeface(typeface);
		
		textView =  (TextView) findViewById(R.id.textView4);
		textView.setTypeface(typeface);
		
		TextView textViewShopTitle = (TextView) findViewById(R.id.textViewShopTitle);
		textViewShopTitle.setTypeface(typeface);
		
		TextView textViewContentTitle = (TextView) findViewById(R.id.content_title);
		textViewContentTitle.setTypeface(typeface);
		
		//m_spin = (Spinner)findViewById(R.id.spinnerQuestionType);
		//m_spin.setOnItemSelectedListener(this);
		
		//댓글 버튼 선언하기
		m_commentButton = (Button) findViewById(R.id.buttonComments);
		
		m_commentButton.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {			
				//답글보기 인테트 실행
				String data = getIntent().getStringExtra("NOTICE");
				
            	Intent intent = new Intent(ReadQuestionActivity.this, ComentQuestionActivity.class);	            	
            	intent.putExtra("DATA", data.toString());
            	startActivity(intent);				
			}
		});
			
		
		//닫기 버튼 선언하기
		m_cancelButton = (Button) findViewById(R.id.buttonCancel);
		m_cancelButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		/*m_saveButton = (Button) findViewById(R.id.buttonSave);
        m_saveButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				save();
			}
		});*/

		//EditText editTextManagerName = (EditText) findViewById(R.id.editTextManagerName);
		//EditText editTextContectNumber = (EditText) findViewById(R.id.editTextContectNumber);
		//EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
		WebView webviewcontent = (WebView)findViewById(R.id.webviewContent);

		//String phoneNumber = LocalStorage.getString(this, "phoneNumber");
		//editTextContectNumber.setText(phoneNumber);
		
        m_Mode = getIntent().getStringExtra("MODE");
		if (m_Mode.equals("UPDATE")) {

			String data = getIntent().getStringExtra("NOTICE");
			String total;
			
			try {
				JSONObject json = new JSONObject(data);					
				
				Log.w("data", json.toString());
				
				//editTextManagerName.setText(json.getString("APP_USERNAME"));			
				//editTextManagerName.setText(json.getString("APP_HP"));							
				//editTextContent.setText(json.getString("B_Content"));								
				webviewcontent.loadData(json.getString("B_Content"), "text/html; charset=UTF-8", null);				
				textViewContentTitle.setText(json.getString("B_Title"));
				
				//댓글버튼 댓글수 등록하기
				total = json.getString("total");
				
				switch(Integer.parseInt(total)){
				case 0: break;
				case 1: m_commentButton.setBackgroundResource(R.drawable.btn_basic_1); break;
				case 2: m_commentButton.setBackgroundResource(R.drawable.btn_basic_2); break;
				case 3: m_commentButton.setBackgroundResource(R.drawable.btn_basic_3); break;
				default : 
					m_commentButton.setBackgroundResource(R.drawable.btn_basic_4);
					break;
				}
				
				
				String APP_GUBUN = json.getString("APP_GUBUN");
							
				APP_GUBUN = json.getString("APP_GUBUN");
				
				int gubun = 0;
				if (APP_GUBUN.equals("0")) gubun = 0;
				if (APP_GUBUN.equals("카드결재")) gubun = 1;
				if (APP_GUBUN.equals("프로그램")) gubun = 2;
				if (APP_GUBUN.equals("POS")) gubun = 3;
				if (APP_GUBUN.equals("AS")) gubun = 4;
				if (APP_GUBUN.equals("기타")) gubun = 5;
				//m_spin.setSelection(gubun);
				
				//AS/카드결제/기타
				//int gubun = Integer.parseInt(APP_GUBUN);
				String strGubun = "기타";
				switch (gubun) {				
				case 0: strGubun ="공지사항"; break;
				case 1: strGubun ="카드결재 관련"; break;
				case 2: strGubun ="프로그램 관련"; break;
				case 3: strGubun ="POS장비 관련"; break;
				case 4: strGubun ="A/S 신청"; break;
				case 5: strGubun ="기타"; break;
				}
				
				textView.setText(strGubun.toString()); 
				//m_saveButton.setVisibility(View.INVISIBLE);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}

		JSONObject currentShopData = LocalStorage.getJSONObject(this, "currentShopData"); 

		String Office_Name= null;	
        try {
			Office_Name = currentShopData.getString("Office_Name");
			textViewShopTitle.setText(Office_Name);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	// private methos
	/*private void save() {

		//EditText editTextManagerName = (EditText) findViewById(R.id.editTextManagerName);
		EditText editTextContectNumber = (EditText) findViewById(R.id.editTextContectNumber);
		EditText editTextContent = (EditText) findViewById(R.id.editTextContent);
		WebView webviewcontent = (WebView)findViewById(R.id.webviewContent);
		
		
		//AS/카드결제/기타
		//int gubun = m_spin.getSelectedItemPosition();
		String strGubun = "기타";
		switch (gubun) {
		case 0: strGubun ="카드결재"; break;
		case 1: strGubun ="프로그램"; break;
		case 2: strGubun ="POS"; break;
		case 3: strGubun ="AS"; break;
		case 4: strGubun ="기타"; break;
		}
		// ..
		addNewNotice("ip", m_office_code, webviewcontent.getContext().toString(), editTextManagerName.getText().toString(),
				editTextContectNumber.getText().toString(), strGubun);
		
	}*/

	// 새로운 공지사항 추가 함수 
    public void addNewNotice(String b_ip, String code, String content, String name, String phone, String gubun) {

    	// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.show();
 		
    	// 쿼리 작성하기
	    String query =  "";
	    
	    if (m_Mode.equals("UPDATE")) {
		    query = "INSERT INTO Mult_BBS(b_gubun,b_pass,b_html,b_title,b_content,b_ip,b_writerID,OFFICE_CODE,APP_USERNAME,APP_HP,APP_GUBUN)" 
		    		+ " VALUES('3','','1',CONVERT(VARCHAR(8000),'" + content+"'),'"+content+"','"
		    		+ b_ip +"','" + code+"','" + code+"','" + name+"','" + phone+"','" + gubun+"');";

	    	query += "UPDATE MULT_BBS SET B_REGROUP= B_IDX"; 
	    	query += " WHERE B_REGROUP = 0 ;";
	    }
	    else {
	    	query = "UPDATE Mult_BBS SET(b_gubun,b_pass,b_html,b_title,b_content,b_ip,b_writerID,OFFICE_CODE,APP_USERNAME,APP_HP,APP_GUBUN)" 
		    		+ " VALUES('3','','1',CONVERT(VARCHAR(8000),'" + content+"'),'"+content+"','"
		    		+ b_ip +"','" + code+"','" + code+"','" + name+"','" + phone+"','" + gubun+"');";

	    	query = "UPDATE MULT_BBS SET B_REGROUP= B_IDX"; 
	    	query += " WHERE B_REGROUP = 0 ;";
	    }
	    
	    // 콜백함수와 함께 실행
	    new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				didAddOrUpdateNewNotice(results);
			}
			// 2022.05.26.본사서버 IP변경
		}).execute(TIPOS.HOST_SERVER_IP+":"+TIPOS.HOST_SERVER_PORT, TIPOS.HOST_SERVER_DB, TIPOS.HOST_SERVER_ID, TIPOS.HOST_SERVER_PW, query);
    }

    // DB에 접속후 호출되는 함수
    public void didAddOrUpdateNewNotice(JSONArray results) {

		AlertDialog.Builder builder = new AlertDialog.Builder(ReadQuestionActivity.this,AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle("알림");
        builder.setMessage("저장이 완료되었습니다.");
        builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.show();
    }
    
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	
			getActionBar().setDisplayHomeAsUpEnabled(true);		
			ActionBar actionbar = getActionBar();         
			//LinearLayout custom_action_bar = (LinearLayout) View.inflate(this, R.layout.activity_custom_actionbar, null);
			//actionbar.setCustomView(custom_action_bar);
			actionbar.setDisplayShowHomeEnabled(false);
			actionbar.setDisplayShowTitleEnabled(true);
			actionbar.setTitle("문의하기");		
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.input_question, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}
