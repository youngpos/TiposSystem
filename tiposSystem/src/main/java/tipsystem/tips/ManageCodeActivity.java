package tipsystem.tips;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;

import android.opengl.Visibility;
import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import kr.co.tipos.tips.R;

public class ManageCodeActivity extends Activity {

	JSONObject m_shop;
	JSONObject m_userProfile;

	String m_OfficeCode = "";

	// loading bar
	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_code);

		//----------------------------------------//
		// Toolbar
		//----------------------------------------//
		ImageView homeIcon = findViewById(R.id.toolbar_home_icon);
		TextView toolbarTitle = findViewById(R.id.toolbar_title_text);
		//Button toobarSetting = findViewById(R.id.toolbar_setting_button);
		ImageView configIcon = findViewById(R.id.toolbar_config_icon);


//		leftIcon.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				finish();
//			}
//		});
		homeIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent intent = new Intent(ManageCodeActivity.this, MainMenuActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 스택 중간 지우기
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 스택 맨처음 확인
				startActivity(intent);
			}
		});
//		toobarSetting.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				startActivity(new Intent(ManageCodeActivity.this,TIPSPreferences.class));
//			}
//		});
		configIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(ManageCodeActivity.this,TIPSPreferences.class));
			}
		});

		toolbarTitle.setText("기본 관리");
		//----------------------------------------//

		Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
		//TextView textView = (TextView) findViewById(R.id.textView1);
		//textView.setTypeface(typeface);

		m_shop = LocalStorage.getJSONObject(this, "currentShopData");
		m_userProfile = LocalStorage.getJSONObject(this, "userProfile");

		try {
			m_OfficeCode = m_shop.getString("OFFICE_CODE");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//테스트를 위해선 주석처리 후 실행 해야 합니다.
		//당분간 기능을 사용하지 않습니다.
		//doHandPhoneYN();
		//doHandPhoneSearch();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manage_code, menu);
		return true;
	}
	
	/*private void doHandPhoneYN(){
		String phoneNumber = LocalStorage.getString(ManageCodeActivity.this, "phoneNumber");
		String query = "select * from APP_USER "
						+ " where OFFICE_CODE='"+m_OfficeCode+"' "
						+ "And APP_USERNAME = '문자발송폰' "
						+ "And APP_HP = '"+phoneNumber+"' ";
		
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
					Button btn_sms2 = (Button)findViewById(R.id.button02);
					Button btn_sms1 = (Button)findViewById(R.id.button01);
					Button btn_sms3 = (Button)findViewById(R.id.button03);
					btn_sms1.setVisibility(View.VISIBLE);
					btn_sms2.setVisibility(View.VISIBLE);
					btn_sms3.setVisibility(View.VISIBLE);	
					doHandPhoneSearch();
				}
				else {
					Button btn_sms3 = (Button)findViewById(R.id.button03);
					Button btn_sms2 = (Button)findViewById(R.id.button02);
					Button btn_sms1 = (Button)findViewById(R.id.button01);
					btn_sms1.setVisibility(View.GONE);
					btn_sms2.setVisibility(View.GONE);
					btn_sms3.setVisibility(View.GONE);		     
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
		    	Toast.makeText(getApplicationContext(), "휴대폰번호를 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
			}
	    }).execute(m_ip+":"+m_port, "Trans", "app_tips", "app_tips", query);	    
	}*/
		
/*	private void doHandPhoneSearch(){
		
		//REGID가 등록이 안되있다면 한번도 전송 프로그램을 실행 하지 않았다는 겁니다!
		String query = "select * from APP_USER where OFFICE_CODE='"+m_OfficeCode+"' And APP_USERNAME = '문자전송폰' And REGID <> '' ";
				
	    // 콜백함수와 함께 실행
	    new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				if (results.length()>0) {
					//매장 휴대폰 문자전송 번호 호출하기
					LocalStorage.setJSONArray(ManageCodeActivity.this, "shopsPhone", results);					
				}
				else {
		            Toast.makeText(getApplicationContext(), " 등록된 휴대폰이 없습니다. ", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
		    	Toast.makeText(getApplicationContext(), "휴대폰번호를 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
			}
	    }).execute(m_ip+":"+m_port, "Trans", "app_tips", "app_tips", query);
	    
	}*/

	//거래처 관리
	public void onClickManageCustomer(View view)
	{
		Intent intent = new Intent(this, ManageCustomerActivity.class);
		startActivity(intent);
		overridePendingTransition(0, 0);

	}

	//상품관리
	public void onClickManageProduct(View view)
	{

		//2017-06-23  M3모바일 사용으로 인해 봉인됨
		/*Intent intent = new Intent(this, ManageProductActivity.class);
    	intent.putExtra("barcode", "");
    	startActivity(intent);*/

		Intent intent = new Intent(this, ManageProductActivityModfiy.class);
		intent.putExtra("barcode", "");
		startActivity(intent);
		overridePendingTransition(0, 0);


	}

	//가격비교
	public void onClickComparePrice(View view)
	{
		Intent intent = new Intent(this, ComparePriceActivity.class);
		startActivity(intent);
		overridePendingTransition(0, 0);

	}

	//바코드발행관리
	public void onClickBarcodePrint(View view)
	{
		Intent intent = new Intent(this, BarcodePrinterActivity.class);
// test		Intent intent = new Intent(this, MobilePinterTestActivity.class);
		startActivity(intent);
		overridePendingTransition(0, 0);

	}

//	//문자발행관리
//	public void onClickSMSMessageBox(View view)
//	{
//		Intent intent = new Intent(this, ManageCodeSmsMessageBoxActivity.class);
//		startActivity(intent);
//	}
//
//	//문자발행리스트
//	public void onClickSMSMessageSendList(View view)
//	{
//		Intent intent = new Intent(this, ManageSMSserverActivity.class);
//		startActivity(intent);
//	}
//
//	/*
//	public void onClickSMSTranBox(View view)
//	{
//		Intent intent = new Intent(this, SmsTranBox.class);
//		startActivity(intent);
//	}
//	*/
//	//문자 발행결과 목록
//	public void onClickSMSResultBox(View view)
//	{
//		Intent intent = new Intent(this, ManageCodeSMSResults.class);
//		startActivity(intent);
//	}

	//이미지관리
	public void onClickImageFtp_local(View view)
	{
		Intent intent = new Intent(this, ManageImageFtpActivity_local.class);
		startActivity(intent);
	}

	//이미지관리
	public void onClickImageFtp(View view)
	{
		Intent intent = new Intent(this, ManageImageFtpActivity.class);
		startActivity(intent);
	}



}
