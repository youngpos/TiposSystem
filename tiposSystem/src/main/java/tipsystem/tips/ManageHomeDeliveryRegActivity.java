package tipsystem.tips;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.M3MoblieBarcodeScanBroadcast;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.MSSQL4;
import tipsystem.utils.StringFormat;
import tipsystem.utils.UserPublicUtils;

public class ManageHomeDeliveryRegActivity extends Activity implements OnClickListener, OnItemClickListener{
		
	String TAG = "배달등록관리";
	
	private static final int ZBAR_SCANNER_REQUEST = 0;
	private static final int BARCODE_MANAGER_REQUEST = 2;
	private static final int CUSTOMER_MANAGER_REQUEST = 3;
	private static final int PURCHASE_REGIST_REQUEST = 4;
	private static final int PRODUCT_REGIST_REQUEST = 5;
	
	//2017-04 m3mobile
	private static final String SCANNER_ACTION_SETTING_CHANGE = "com.android.server.scannerservice.settingchange";
	//public static final String SCANNER_ACTION_PARAMETER = "android.intent.action.SCANNER_PARAMETER";
	//public static final String SCANNER_ACTION_ENABLE = "com.android.server.scannerservice.m3onoff";
	//public static final String SCANNER_EXTRA_ENABLE = "scanneronoff";
	
	public static final String SCANNER_ACTION_START = "android.intent.action.M3SCANNER_BUTTON_DOWN";
	//public static final String SCANNER_ACTION_CANCEL = "android.intent.action.M3SCANNER_BUTTON_UP";
	
	//public static final String SCANNER_ACTION_BARCODE = "com.android.server.scannerservice.broadcast";

	
		
	UserPublicUtils upu;
	
	JSONObject m_shop;
	JSONObject m_userProfile;
		
	// 전표번호 생성
	String posID = "P";
	String userID = "1";
	
	//임시부여 아이피
	String m_ip = "127.0.0.1";
	String m_port = "1433";
	//----------------------------------------//
	// 2021.12.21. 매장DB IP,PW,DB 추가
	//----------------------------------------//
	String m_uuid = "sa";
	String m_uupw = "tips";
	String m_uudb = "tips";
	//----------------------------------------//

	Context mContext;
	
	private ProgressDialog dialog;
	
	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>(); // 배달상품목록 리스트
	
	//회원정보
	EditText edittext_MemberCode;
	EditText edittext_MemberName;
	EditText edittext_HDNum;	//전표번호
		
	//상품정보
	TextView textview_goodsInfo;
	EditText edittext_Barcode;
	EditText edittext_Gname;
	EditText edittext_SellPri;
	EditText edittext_Amount;
	
	CheckBox checkbox_CFix;

	Button button_keyboard;
	
	//리스트
	ListView listview_GoodsList;
	TextView textview_TCount;
	SimpleAdapter m_adapter;
	
	//2017-04 m3mobile
	SharedPreferences pref;
	boolean m3Mobile;
	
	
	//저울상품 환경설정
	int setCodeLength=-1;		//저울상품 코드 길이
	String setSnumber="";				//저울상품 시작 문자
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_managehomedeliveryreg);
		
		mContext = this;
		m_shop = LocalStorage.getJSONObject(this, "currentShopData");
		m_userProfile = LocalStorage.getJSONObject(this, "userProfile");

		//사용자 함수
		upu = new UserPublicUtils(mContext);
		
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
			posID = LocalStorage.getString(this, "currentPosID:" + OFFICE_CODE);
			userID = m_userProfile.getString("User_ID");
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		
		// 환경설정 관련 옵션 아이템 불러오기
		pref = PreferenceManager.getDefaultSharedPreferences(this);		
		m3Mobile = pref.getBoolean("m3mobile", false);

		//시작점
		init();
		
		//초기값 조회
		start();

		//최초 목록 조회 합니다.
		getHomeDileveryList();
	}
	
	
	private void start(){
		
		Intent intent = getIntent();
		boolean gubun = intent.getBooleanExtra("구분", false); 
		//gubun 회원배달 ture // 비회원 배달 false
		if(gubun){
			
			String member_code = intent.getStringExtra("회원번호");
			String member_name = intent.getStringExtra("회원명");
			
			edittext_MemberCode.setText(member_code);
			edittext_MemberName.setText(member_name);
			
			//회원 코드번호로 테이블에 등록된 배달 내역이 있는지 확인 합니다.
			getHomeDocumentNumber(member_code);
			
		}else{
			
			//edittext_MemberCode.setText("00000000");
			//edittext_MemberName.setText("비회원");
			
			edittext_MemberCode.setText("");
			edittext_MemberName.setText("비회원");
			
			//회원 코드번호로 테이블에 등록된 배달 내역이 있는지 확인 합니다.
			getHomeDocumentNewNumber();
			
		}
	}
		
	private void init(){
		
		//회원정보
		edittext_MemberCode = (EditText)findViewById(R.id.editMemberCode);
		edittext_MemberName = (EditText)findViewById(R.id.editTextMemberName);
		edittext_HDNum = (EditText)findViewById(R.id.editTextHomeNum);	//전표번호
		
		//상품정보
		textview_goodsInfo = (TextView)findViewById(R.id.textview_goodsinfo);
		edittext_Barcode = (EditText)findViewById(R.id.editTextBarcode);
		edittext_Gname = (EditText)findViewById(R.id.editTextProductName);
		edittext_SellPri = (EditText)findViewById(R.id.editTextSalePrice);
		edittext_Amount = (EditText)findViewById(R.id.editTextAmount);
		
		checkbox_CFix = (CheckBox)findViewById(R.id.checkBox_amountfix);

		button_keyboard = (Button)findViewById(R.id.buttonKeyboard);
		button_keyboard.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(button_keyboard.getTag().equals("on")){
					upu.hideSoftKeyboard(true);
					button_keyboard.setTag("off");
				}else{
					upu.hideSoftKeyboard(false);
					button_keyboard.setTag("on");
				}

			}
		});
		
		//리스트
		listview_GoodsList = (ListView)findViewById(R.id.listviewReadyToSendList);
		textview_TCount = (TextView)findViewById(R.id.textviewTCount);
		
		listview_GoodsList.setOnItemClickListener(this);
		
		
		//목록 리스트
		String[] from = new String[] { "바코드", "상품명", "판매가", "수량" };
		int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4 };
		m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_item8, from, to);
		listview_GoodsList.setAdapter(m_adapter);
				
		//버튼정보		
		Button button_Search = (Button)findViewById(R.id.buttonSearch);	//상품목록검색
		Button button_Renew = (Button)findViewById(R.id.buttonRenew);	//새로입력
		final Button button_Save = (Button)findViewById(R.id.buttonSave);
//		Button button_Scan = (Button)findViewById(R.id.buttonScan);
				
		button_Search.setOnClickListener(this);
		button_Renew.setOnClickListener(this);
		button_Save.setOnClickListener(this);
//		button_Scan.setOnClickListener(this);
		
		//상품 수량 고정
		checkbox_CFix.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				if(isChecked){
					String amount = edittext_Amount.getText().toString();

					if("".equals(amount)){
						edittext_Amount.setText("1");
					}
					edittext_Amount.setEnabled(false);
				}else{
					edittext_Amount.setEnabled(true);
				}
			}
		});

		edittext_Barcode.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(!hasFocus){
					String barcode = edittext_Barcode.getText().toString();
					if(!"".equals(barcode)){
						doQueryWithBarcode();
					}
					upu.setChangeBackGroundColor(0, edittext_Barcode);
				}else{
					upu.setChangeBackGroundColor(1, edittext_Barcode);
				}

			}
		});
				
		edittext_Gname.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					upu.setChangeBackGroundColor(1, edittext_Gname);
				}else{
					upu.setChangeBackGroundColor(0, edittext_Gname);
				}
			}
		});
		
		edittext_SellPri.setImeOptions(EditorInfo.IME_ACTION_NEXT);
		edittext_SellPri.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){									
					upu.setChangeBackGroundColor(1, edittext_SellPri);
				}else{
					upu.setChangeBackGroundColor(0, edittext_SellPri);
				}
			}
		});
		
		edittext_SellPri.setOnKeyListener(new OnKeyListener() {
				
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					// TODO Auto-generated method stub
					
					if(event.getAction() == KeyEvent.ACTION_DOWN){				
						if(keyCode == event.KEYCODE_ENTER || keyCode == EditorInfo.IME_ACTION_DONE){
							
							if(checkbox_CFix.isChecked()){
								if(!onCheckData()){
					    			return false;
					    		}  
								
								doSend();
							}
						}
					}
					return false;
				}
			});
		
				
		edittext_Amount.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(hasFocus){
					upu.setChangeBackGroundColor(1, edittext_Amount);

					if(checkbox_CFix.isChecked()){	//체크여부 확인
						//수량 1개로 강제 등록 합니다.
						
						if(!onCheckData()){
			    			return;
			    		}  
						
						doSend();					
						
					}else{
						InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
						if (imm.isAcceptingText()) {
							imm.showSoftInput(getCurrentFocus(), InputMethodManager.SHOW_FORCED);
						}
					}

				}else{
					upu.setChangeBackGroundColor(0, edittext_Amount);
				}
			}
		});

		edittext_Amount.setImeOptions(EditorInfo.IME_ACTION_DONE);
		//입력수량 완료버튼 입력 시 자동저장 기능 리스너
		edittext_Amount.setOnEditorActionListener(new OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				//완료 버튼을 눌렀습니다.
				if(actionId == EditorInfo.IME_ACTION_DONE){
					if(!onCheckData()){
						return false;
					}
					doSend();
				}

				return false;
			}
		});

				
		if(m3Mobile){			
			M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, edittext_Barcode, edittext_Amount);				
		}
		
		//저울상품 정보셋팅
		posSet();
	}
	
	//저장
	public void doSend(){
		
		String barcode = edittext_Barcode.getText().toString();
		String home_num = edittext_HDNum.getText().toString();
		String cus_code = edittext_MemberCode.getText().toString();
		String cus_name = edittext_MemberName.getText().toString();
		String sell_pri = edittext_SellPri.getText().toString();
		sell_pri = sell_pri.replace(",", "");
		String amount = edittext_Amount.getText().toString();
		
		String query = "Insert Into Temp_DeliveryPDA(barcode, home_num, cus_code, cus_name, sell_pri, amount, writer) "
				+ "values( '"+barcode+"', '"+home_num+"', '"+cus_code+"', '"+cus_name+"', '"+sell_pri+"', '"+amount+"', '"+posID+"' ) ";
				
		// 로딩 다이알로그
		
		if(!dialog.isShowing()){
			dialog = new ProgressDialog(this);
			dialog.setMessage("Loading....");
			dialog.setCancelable(false);
			dialog.show();
		}
		// 콜백함수와 함께 실행
		new MSSQL4(new MSSQL4.MSSQL4CallbackInterface() {

			@Override
			public void onRequestCompleted(Integer results) {
				dialog.dismiss();
				dialog.cancel();
				
				Log.d(TAG, "배달상품 등록 결과 : "+results);		
				
				onReNew();
				//Toast.makeText(getApplicationContext(), "저장완료.", Toast.LENGTH_SHORT).show();

				getHomeDileveryList();

				//edittext_Barcode.requestFocus();
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "전송실패(" + String.valueOf(code) + "):" + msg, Toast.LENGTH_SHORT)
						.show();
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
		
		
	}
	
	//상품 검색 하기 SQL QUERY 실행
	public void doQueryWithBarcode() {

		String query = "";
		
		//저울상품 체크
		doCheckScale();
		
		// 상품명 원레 이름 등록		
		String barcode = edittext_Barcode.getText().toString();		
		query = "Select * From goods where barcode='"+barcode+"' and Goods_Use='1' ";

		if (barcode.equals(""))
			return;
		
		// 로딩 다이알로그
		if(!dialog.isShowing()){
			dialog = new ProgressDialog(this);
			dialog.setMessage("Loading....");
			dialog.show();
		}
		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
																	
				if (results.length() > 0) { // 검색결과 확인
					try {
						
						HashMap<String,String> m_tempProduct = JsonHelper.toStringHashMap(results.getJSONObject(0));												
						edittext_Gname.setText(m_tempProduct.get("G_Name"));
						
						if("1".equals(m_tempProduct.get("Scale_Use"))){
							textview_goodsInfo.setText("상품정보 [저울상품]");
							edittext_Amount.requestFocus();
							return;
						}
						
						String sDate = m_tempProduct.get("Sale_SDate");
						String eDate = m_tempProduct.get("Sale_EDate");
						
						//행사 기간 체크 체크 오늘날자와 비교						
						if( !"".equals(sDate)){
							if(upu.DateCompare(sDate, eDate)){
								textview_goodsInfo.setText("상품정보 [행사]");
								edittext_SellPri.setText(StringFormat.convertToNumberFormat(m_tempProduct.get("Sale_Sell")));
							}else{
								textview_goodsInfo.setText("상품정보 [일반]");
								edittext_SellPri.setText(StringFormat.convertToNumberFormat(m_tempProduct.get("Sell_Pri")));
							}
						}else{
							textview_goodsInfo.setText("상품정보 [일반]");
							edittext_SellPri.setText(StringFormat.convertToNumberFormat(m_tempProduct.get("Sell_Pri")));
						}
						
						edittext_Amount.requestFocus();
												
					} catch (JSONException e) {
						e.printStackTrace();
					}
					
				}else{					
					Toast.makeText(mContext, "조회된 상품이 없습니다.", Toast.LENGTH_SHORT).show();
					edittext_Barcode.requestFocus();
				}					
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(mContext, "상품검색 실패\r\n" + msg, Toast.LENGTH_SHORT).show();
				edittext_Barcode.requestFocus();
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	
	//전표검색해서 전표 번호 불러오기
	private void getHomeDocumentNumber(String memcode){
				
		String member_code = memcode;	//회원번호
		
		String query = "Select isnull(Max(right(home_num, 3)), '000') as 최종전표,  "
						+" isnull((Select top 1 isnull(home_num, '') From Temp_DeliveryPda "
						+ " where cus_code='"+member_code+"'), '') as 회원전표 "
						+ " From Temp_DeliveryPda ";
		
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
				
				//처리결과
				if(results.length() > 0){															
					
					try {
						
						JSONObject obj = results.getJSONObject(0);
						String mlastjp = (String) obj.get("최종전표");
						String memejp = (String) obj.get("회원전표");
						
						//회원으로 검색된 전표가 없습니다.
						if(!"".equals(memejp)){
							edittext_HDNum.setText(memejp);
							return;
						}
						
						//신규번호 생성
						String num = upu.getTodayData().replace("-", "")+posID+upu.getJunPyoNumStyle(mlastjp);
						edittext_HDNum.setText(num);
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}						
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	
	//비회원 전표를 생성합니다.
	private void getHomeDocumentNewNumber(){
		
		String query = "Select isnull(Max(right(home_num, 3)), '000') as 최종전표 From Temp_DeliveryPda";
		
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
				
				//처리결과
				if(results.length() > 0){															
					
					try {
						
						JSONObject obj = results.getJSONObject(0);
						String mlastjp = (String) obj.get("최종전표");						
						
						//신규번호 생성
						String num = upu.getTodayData().replace("-", "")+posID+upu.getJunPyoNumStyle(mlastjp);
						edittext_HDNum.setText(num);
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	
	
	//유효성 체크
	public boolean onCheckData(){
		
		String barcode = edittext_Barcode.getText().toString();
		String g_name = edittext_Gname.getText().toString();
		String sell_pri = edittext_SellPri.getText().toString();
		String amount = edittext_Amount.getText().toString();
		
		String home_num = edittext_HDNum.getText().toString();
		
		//전표번호를 체크 합니다.
		if("".equals(home_num)){
			Toast.makeText(mContext, "전표번호가 없습니다. 등록창을 다시 실행해 주세요!!", Toast.LENGTH_SHORT).show();
			return false;
		}
				
		if("".equals(barcode)){
			Toast.makeText(mContext, "바코드를 입력해 주세요~!", Toast.LENGTH_SHORT).show();
			edittext_Barcode.requestFocus();
			return false;
		}
		
		if("".equals(sell_pri)){
			Toast.makeText(mContext, "판매가를 입력해 주세요~!", Toast.LENGTH_SHORT).show();
			edittext_SellPri.requestFocus();
			return false;
		}
		
		try{			
			sell_pri = sell_pri.replace(",", "");
			int a = Integer.parseInt(sell_pri);
			if(a <= 0){
				Toast.makeText(mContext, "판매가를 입력해 주세요~!", Toast.LENGTH_SHORT).show();
				edittext_SellPri.requestFocus();
				return false;
			}			
		}catch(NumberFormatException e){
			Toast.makeText(mContext, "판매가는 숫자만 입력해 주세요~!", Toast.LENGTH_SHORT).show();
			edittext_SellPri.requestFocus();
			e.printStackTrace();
			return false;
		}
		
		if("".equals(amount)){
			Toast.makeText(mContext, "수량을 입력해 주세요~!", Toast.LENGTH_SHORT).show();
			edittext_Amount.requestFocus();
			return false;
		}		
		
		int a = 0;
		try{
			a = Integer.parseInt(amount);
		}catch(NumberFormatException e){
			e.printStackTrace();
			return false;
		}
		
		if( a <= 0 ){
			Toast.makeText(mContext, "수량을 0개 이상 입력해 주세요~!", Toast.LENGTH_SHORT).show();
			edittext_Amount.requestFocus();
			return false;
		}		
		
		return true;
	}
	
	
	//새로입력
	public void onReNew(){
				
		textview_goodsInfo.setText("상품정보");
		edittext_Barcode.setText("");;
		edittext_Gname.setText("");;
		edittext_SellPri.setText("");
		
		if(!checkbox_CFix.isChecked()){
			edittext_Amount.setText("");
		}

		edittext_Barcode.requestFocus();

		upu.hideSoftKeyboard(true);

	}
	
	/**
	 * 2020-06-15  팅김 문제 발생
	//목록 초기화	
	private void setReNewList(){
	    mfillMaps.clear();
	    textview_TCount.setText("0");
	}
	 **/
	
	
	//상품검색
	public void onBarcodeSearch(View v){
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String list_value = pref.getString("prefSearchMethod", "");
		if (list_value.equals("camera")) {
			startCameraSearch();
		} else if (list_value.equals("list")) {
			startProductList();
		} else {
			// 바코드 검색 버튼 클릭시 나오는 목록 셋팅
			final String[] option = new String[] { "목록", "카메라" };
			//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.user_select_dialog_item, option);
			AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
			builder.setTitle("Select Option");

			// 목록 선택시 이벤트 처리
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					if (which == 0) { // 목록으로 조회할 경우
						startProductList();
					} else { // 스캔할 경우
						startCameraSearch();
					}
				}
			});
			builder.show();
		}		
	}

	
	// 상품 목록검색
	private void startProductList() {
		String barcode = edittext_Barcode.getText().toString();
		String gname = edittext_Gname.getText().toString();		
		Intent intent = new Intent(this, ManageProductListActivity.class);
		intent.putExtra("barcode", barcode);
		intent.putExtra("gname", gname);
		intent.putExtra("Office_Code", "");
		startActivityForResult(intent, BARCODE_MANAGER_REQUEST);
	}
	
	// 상품 카메라 검색
	private void startCameraSearch() {
		Intent intent = new Intent(this, ZBarScannerActivity.class);
		startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
	}
	
	//저울상품 셋
	private void posSet(){
		
		String query = "Select * From Pos_Set";
		
			
		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
			
				//처리결과
				if(results.length() > 0){															
					
					try {
						
						JSONObject obj = results.getJSONObject(0);
						String setCode = (String)obj.get("106");
						setCode = setCode.split(",")[1];
						
						setSnumber = (String)obj.get("107");
						Log.d(TAG, "시작문자 :"+setSnumber +" 저울코드 길이 : "+setCode);
						
						try{							
							setCodeLength = Integer.parseInt(setCode);														
						}catch(NumberFormatException err){
							err.printStackTrace();
						}
						
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();										
					}						
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				Toast.makeText(getApplicationContext(), "저울상품 셋팅 실패", Toast.LENGTH_SHORT).show();
				
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
		
		
		
	}
	
	
	//저울상품 확인
	private void doCheckScale(){
		
		String barcode = edittext_Barcode.getText().toString();
		
		//다섯자리 바코드는 저울상품이 아닙니다.
		if(barcode.length() < 13){
			return;
		}
		
		//저울상품 코드길이 셋팅이 안되있다면
		if(setCodeLength <= 0 && setSnumber.length() <= 0 ){
			return;
		}
		
		//앞두자리가 저울코드라면 저울상품으로 인정
		if(!setSnumber.equals(barcode.substring(0, 2))){
			return;
		}
		
		String code = barcode.substring(0, setCodeLength+setSnumber.length());
		String price = barcode.substring(setCodeLength+setSnumber.length(), barcode.length()-1);
		
		edittext_Barcode.setText(code);
		edittext_SellPri.setText(StringFormat.convertToNumberFormat(price));
		Log.d(TAG, "code : "+code+"  price : "+price);
		
		
	}
	
	
	//배달상품 목록 조회
	private void getHomeDileveryList(){
		
		//목록 초기화  ??2020-06-15 문제 발생
		//setReNewList();
		
		String home_num = edittext_HDNum.getText().toString();
		
		if("".equals(home_num)) return;
		
		String query = "Select a.idx, a.Home_Num, a.Barcode, b.G_Name, a.Amount, a.Sell_Pri "
						 + " From Temp_DeliveryPDA a left join goods b "
						 + " on a.barcode=b.barcode "
						 + " Where Home_Num = '"+home_num+"' "
						 + " Order By idx ";
		
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

				//2020-06-15 초기화 처리 이동
				mfillMaps.clear();
				textview_TCount.setText("0");

				//처리결과
				if(results.length() > 0){										
					
					try{								
						for(int i=0; i < results.length(); i++){
							
							JSONObject map = results.getJSONObject(i);
							
							HashMap<String, String> temp = new HashMap<String, String>();
							temp.put("고유값", map.get("idx").toString());
							temp.put("전표번호", map.get("Home_Num").toString());
							temp.put("바코드", map.get("Barcode").toString());
							temp.put("상품명", map.get("G_Name").toString());
							temp.put("판매가", map.get("Sell_Pri").toString());
							temp.put("수량", map.get("Amount").toString());
							
							mfillMaps.add(temp);												
						}					
						
						//총상품수량
						textview_TCount.setText(mfillMaps.size()+"");
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}else{
					Toast.makeText(getApplicationContext(), "등록된 상품이 없습니다.", Toast.LENGTH_SHORT).show();
					
				}
				
				m_adapter.notifyDataSetChanged();
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
				m_adapter.notifyDataSetChanged();
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
		
	}
	
	
	//목록 삭제
	private void onDataDelete(int position){
		final int pos = position;
		
		HashMap<String, String> map = mfillMaps.get(pos);
		
		String idx = map.get("고유값");
		String query = "Delete From Temp_DeliveryPDA Where idx='"+idx+"' ";
		
		// 로딩 다이알로그
		dialog = new ProgressDialog(this);
		dialog.setMessage("Loading....");
		dialog.setCancelable(false);
		dialog.show();

		// 콜백함수와 함께 실행
		new MSSQL4(new MSSQL4.MSSQL4CallbackInterface() {

			@Override
			public void onRequestCompleted(Integer results) {
				dialog.dismiss();
				dialog.cancel();
				
				Log.d(TAG, "삭제 결과 : "+results);	
				
				//원 상품을 목록에서 삭제해 줍니다.
				mfillMaps.remove(pos);
				
				//총 수량재입력
				textview_TCount.setText(mfillMaps.size()+"");
				m_adapter.notifyDataSetChanged();
				 upu.hideSoftKeyboard(false);
				Toast.makeText(getApplicationContext(), "삭제완료.", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				 upu.hideSoftKeyboard(false);
				Toast.makeText(getApplicationContext(), "삭제실패(" + String.valueOf(code) + "):" + msg, Toast.LENGTH_SHORT)
						.show();
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
		
		
	}
	
	
	//목록전체 삭제 c
	public void onAllDataDelete(View v){
		
		//삭제할 목록이 없다면 리턴
		if( mfillMaps.size() <= 0) {
			upu.showDialog("삭제할 상품이 없습니다.", 0);
			return;
		}
		
		HashMap<String, String> map = mfillMaps.get(0);
		
		String home_num = map.get("전표번호");
		String query = "Delete From Temp_DeliveryPDA Where home_num='"+home_num+"' ";
		
		// 로딩 다이알로그
		dialog = new ProgressDialog(this);
		dialog.setMessage("Loading....");
		dialog.setCancelable(false);
		dialog.show();

		// 콜백함수와 함께 실행
		new MSSQL4(new MSSQL4.MSSQL4CallbackInterface() {

			@Override
			public void onRequestCompleted(Integer results) {
				dialog.dismiss();
				dialog.cancel();
				
				Log.d(TAG, "삭제 결과 : "+results);	
				
				//원 상품을 목록에서 삭제해 줍니다.
				mfillMaps.clear();
				
				//총 수량재입력
				textview_TCount.setText("0");
				m_adapter.notifyDataSetChanged();
				
				Toast.makeText(getApplicationContext(), "삭제완료.", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "삭제실패(" + String.valueOf(code) + "):" + msg, Toast.LENGTH_SHORT)
						.show();
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
		
		
	}
	
	//수량변경 d
	public void setChangeAmount(int position, String amount){
		
		final int pos = position;
		
		HashMap<String, String> map = mfillMaps.get(pos);
		
		String idx = map.get("고유값");
		final String count = amount.trim();
		
		String query = "Update Temp_DeliveryPDA Set Amount='"+count+"' Where idx='"+idx+"' ";
		
		// 로딩 다이알로그
		dialog = new ProgressDialog(this);
		dialog.setMessage("Loading....");
		dialog.setCancelable(false);
		dialog.show();

		// 콜백함수와 함께 실행
		new MSSQL4(new MSSQL4.MSSQL4CallbackInterface() {

			@Override
			public void onRequestCompleted(Integer results) {
				dialog.dismiss();
				dialog.cancel();
				
				Log.d(TAG, "수량변경 결과 : "+results);
				//원 상품을 목록에서 삭제해 줍니다.				
				mfillMaps.get(pos).put("수량", count);
				
				m_adapter.notifyDataSetChanged();
				upu.hideSoftKeyboard(false);
				Toast.makeText(getApplicationContext(), "변경완료.", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "변경실패(" + String.valueOf(code) + "):" + msg, Toast.LENGTH_SHORT)
						.show();
				upu.hideSoftKeyboard(false);
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
			
		
	}



	// 스캐너 또는 목록 검색에서 돌려받은값 처리 하기
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		// 카메라 스캔을 통한 바코드 검색
		case ZBAR_SCANNER_REQUEST:
			if (resultCode == RESULT_OK) {
				// Scan result is available by making a call to
				// data.getStringExtra(ZBarConstants.SCAN_RESULT)
				// Type of the scan result is available by making a call to
				// data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
				Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT),
						Toast.LENGTH_SHORT).show();
				Toast.makeText(this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE),
						Toast.LENGTH_SHORT).show();
				// The value of type indicates one of the symbols listed in
				// Advanced Options below.

				String barcode = data.getStringExtra(ZBarConstants.SCAN_RESULT);
				edittext_Barcode.setText(barcode);
				edittext_Amount.requestFocus();

				//yWithBarcode(); 검색을 두번씩 해서 변경함

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
			}
			break;
		// 목록 검색을 통한 바코드 검색
		case BARCODE_MANAGER_REQUEST:
			if (resultCode == RESULT_OK && data != null) {
				HashMap<String, String> hashMap = (HashMap<String, String>) data.getSerializableExtra("fillmaps");
				edittext_Barcode.setText(hashMap.get("BarCode"));
				doQueryWithBarcode();
			}
			break;	
		case PRODUCT_REGIST_REQUEST:
			if (resultCode == RESULT_OK) {
				doQueryWithBarcode();
			}
			break;
		}
	}
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch(v.getId()){
        case R.id.buttonSearch:
            //배달상품 목록 검색
        	Log.d(TAG, "배달상품 목록");
        	getHomeDileveryList();
        	
        	//upu.hideSoftKeyboard(false);
            break;
        case R.id.buttonRenew:
        	//새로입력
        	Log.d(TAG, "새로입력");
        	onReNew();
        	break;
        	
        case R.id.buttonSave:        	
        	//저장하기
        	Log.d(TAG, "저장하기");        	

    		if(!onCheckData()){
    			break;
    		}    		
        	
        	doSend();
        	break;
        case R.id.buttonScan:        	
        	//스캔
        	Log.d(TAG, "스캔");
        	if(m3Mobile){
				Intent intent = new Intent(SCANNER_ACTION_SETTING_CHANGE);
				intent.putExtra("setting", "read_mode");
				intent.putExtra("read_mode_value", 1);
				mContext.sendOrderedBroadcast(intent, null);
				
				intent = new Intent(SCANNER_ACTION_START, null);
				mContext.sendOrderedBroadcast(intent, null);
			}
        	break;
        }		
	}
	
	//2017-04 m3mobile 추가 
	@Override
	protected void onResume() {
	    super.onResume();
	    if(m3Mobile){
	    	M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, edittext_Barcode, edittext_Amount);
		    M3MoblieBarcodeScanBroadcast.setOnResume();  
	    }	    
	}
	
	//2017-04 m3mobile 추가
	@Override
	protected void onDestroy() {		
		super.onDestroy();		
		if(m3Mobile){			
			M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, edittext_Barcode, edittext_Amount);
			M3MoblieBarcodeScanBroadcast.setOnDestory();
		}		
	}

	@Override
	public void onBackPressed() {

		Log.d(TAG, "총수량 : "+mfillMaps.size()+" 회원명 : "+edittext_MemberName.getText());
		if(mfillMaps.size() > 0 && edittext_MemberName.getText().toString().equals("비회원")){
			new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT).setTitle("판매상품 등록완료").setMessage("비회원 판매상품 등록의 경우\r\n현재 화면을 나가면 상품을 추가등록 할 수 없습니다.\r\n나가시겠습니까?")
					.setPositiveButton("예", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							finish();
						}
					}).setNegativeButton("아니요", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					return;
				}
			}).show();
		}else{
			finish();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		
		final int pos = position;
		
		HashMap<String, String> map = mfillMaps.get(pos);
		final String count = map.get("수량");

		//----------------------------------------//
		// 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
		//----------------------------------------//
		//AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
		AlertDialog.Builder ad = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
		//----------------------------------------//

		ad.setTitle("배달 상품 목록");       // 제목 설정
		ad.setMessage("수량변경 및 삭제");   // 내용 설정
	
		// EditText 삽입하기
		final TextView ev = new TextView(getApplicationContext());
		ev.setText("수량변경");
		final EditText et = new EditText(getApplicationContext());

		//----------------------------------------//
		// 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
		//----------------------------------------//
		et.setTextColor(Color.BLACK);
		et.setTextSize(30);
		//----------------------------------------//

		et.setGravity(Gravity.CENTER);
		et.setText(count);
		et.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
		et.selectAll();		
		ad.setView(et);
		
		// 수량변경버튼 설정
		ad.setPositiveButton("수량변경", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        Log.v(TAG, "Yes Btn Click");
	
		        // Text 값 받아서 로그 남기기
		        String value = et.getText().toString();
		        Log.v(TAG, value);
		        
		        try{
		        	//수량 입력 없을 시
		        	int a = Integer.parseInt(value);
		        	
		        	if(a <= 0){
		        		Toast.makeText(mContext, "수량을 0개 이상 입력해 주세요!", Toast.LENGTH_SHORT).show();
		        		dialog.dismiss();
		        		upu.hideSoftKeyboard(false);
		        		return;
		        	}
		        	
		        }catch(NumberFormatException e){
		        	dialog.dismiss();		   
		        	upu.hideSoftKeyboard(false);
		        	Log.d(TAG, "수량오류");
		        	return;		        	
		        }
		        
		    	//수량변경 없을 시
		        if( count.equals(value.trim())){
		        	dialog.dismiss();		   
		        	upu.hideSoftKeyboard(false);
		        	Log.d(TAG, "입력변경없슴");
	        		return;
	        	}
		        
		        //수량변경
		        setChangeAmount(pos, value);
						    
		        Log.d(TAG, "정상변경");
		        dialog.dismiss();     //닫기
		        
		        // Event
		    }
		});
	
		//삭제버튼 설정
		ad.setNeutralButton("삭제", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        Log.v(TAG,"Neutral Btn Click");
		        
		        //전표 삭제하기
		        onDataDelete(pos);
		        dialog.dismiss();     //닫기
		    }
		});
	
		// 취소 버튼 설정
		ad.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        Log.v(TAG,"No Btn Click");
		        dialog.dismiss();     //닫기
		        upu.hideSoftKeyboard(false);
		        // Event
		    }
		});
		
		//창 띄우기
		ad.show();
		
		upu.hideSoftKeyboard(true);
				
	}
	
}
