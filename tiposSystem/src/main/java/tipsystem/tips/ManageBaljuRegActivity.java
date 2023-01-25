package tipsystem.tips;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import kr.co.tipos.tips.R;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.M3MoblieBarcodeScanBroadcast;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.UserPublicUtils;

public class ManageBaljuRegActivity extends Activity implements OnDateSetListener, android.widget.AdapterView.OnItemClickListener{
	
	private static final int ZBAR_SCANNER_REQUEST = 0;
	private static final int BARCODE_MANAGER_REQUEST = 2;
	private static final int CUSTOMER_MANAGER_REQUEST = 3;
	//private static final int PURCHASE_REGIST_REQUEST = 4;
	private static final int PRODUCT_REGIST_REQUEST = 5;
	
	//2017-04 m3mobile
	private static final String SCANNER_ACTION_SETTING_CHANGE = "com.android.server.scannerservice.settingchange";
	//public static final String SCANNER_ACTION_PARAMETER = "android.intent.action.SCANNER_PARAMETER";
	//public static final String SCANNER_ACTION_ENABLE = "com.android.server.scannerservice.m3onoff";
	//public static final String SCANNER_EXTRA_ENABLE = "scanneronoff";

	public static final String SCANNER_ACTION_START = "android.intent.action.M3SCANNER_BUTTON_DOWN";
	//public static final String SCANNER_ACTION_CANCEL = "android.intent.action.M3SCANNER_BUTTON_UP";
	
	//public static final String SCANNER_ACTION_BARCODE = "com.android.server.scannerservice.broadcast";
		
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

	/* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
	int m_KeypadType = 0;

	// 전표번호 생성
	String posID = "P";
	String userID = "1";	
		
	DatePicker m_datePicker;
	SimpleDateFormat m_dateFormatter;
	Calendar m_dateCalender1;
	
	CheckBox m_purdataFix;		//매입일 고정
	Button m_period;					//발주일	
	String m_period1;					//입고예정일
	
	EditText m_customerCode;		//거래처코드
	EditText m_customerName;		//거래처명
	EditText m_textBarcode;			//바코드
	EditText m_textProductName;	//상품명
	
	EditText m_et_purchasePrice;		//매입가
	EditText m_et_salePrice;			//행사가
	EditText m_et_purchaseCost; 	//매입원가
	EditText m_et_addTax;				//부가세
	CheckBox m_purpriTaxyn; 		//과세상품 부가세 포함 별도 옵션
	EditText m_amount;				//발주수량
	EditText m_profitRatio;			//이익률
	
	TextView m_textviewProSto;		//안전재고
	TextView m_textviewRealSto;		//현재고
	
	TextView m_totalPurpri; 			//전체 매입가 
	
	// 매입 합계 금액을 계산해 주세요~
	Double totalPurPri = 0.0;
	
	TextView m_goodsGubun; 		//행사상품 표시
	TextView m_inDate;				//입고예정일 표시
	
	Button m_bt_barcodeSearch;		//상품검색
	Button saveButton; 				//저장/수정버튼
	CheckBox m_checkBoxOfficeFix; //거래처코드 고정
	
	CheckBox m_checkBoxRejectedProduct;	//반품유무
		 
	//매입목록 선택 포지션
	int m_selectedListIndex = -1;
	ListView m_listReadyToSend;				//전송대기목록
	SimpleAdapter m_adapter;				//전송대기목록
		
	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>(); // 발주목록 리스트	
	HashMap<String, String> m_tempProduct = new HashMap<String, String>(); // 현재 불러온 상품정보	
	
	//m3pda 스캐너 사용
	Context mContext;
	SharedPreferences pref;
	boolean m3Mobile;
	
	boolean m_saveYN = false;			//해당 발주장에서 상품 등록 여부
	boolean mAllSend;
	
	UserPublicUtils upu;
	
	
	private ProgressDialog dialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_baljureg);
		
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

			String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
			posID = LocalStorage.getString(this, "currentPosID:" + OFFICE_CODE);
			userID = m_userProfile.getString("User_ID");

			/* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
			m_KeypadType = LocalStorage.getInt(this, "KeypadType:" + OFFICE_CODE);

		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		/**********************   개체선언 *********************************************************/

		mContext = this;
		
		upu = new UserPublicUtils(mContext);
		
		// 환경설정 관련 옵션 아이템 불러오기
		pref = PreferenceManager.getDefaultSharedPreferences(this);		
		m3Mobile = pref.getBoolean("m3mobile", false);
		
		m_purdataFix = (CheckBox)findViewById(R.id.checkboxPurDataFix);		//매입일 고정
		
		m_period = (Button)findViewById(R.id.buttonDateSet1);
		m_customerCode = (EditText)findViewById(R.id.editTextCustomerCode);		//거래처코드
		m_customerName = (EditText)findViewById(R.id.editTextCustomerName);		//거래처명

		m_textBarcode = (EditText)findViewById(R.id.editTextBarcode);					//바코드

		// 김영목 2020.09.14. 키패드입력 옵션 설정에 따라 적용 추가
		if (m_KeypadType == 0) { // 숫자키패드
			m_textBarcode.setInputType(2);
		}
		if (m_KeypadType == 1) { // 문자키패드
			m_textBarcode.setInputType(145); //textVisiblePassword ) 0x00000091
		}

		m_textProductName = (EditText)findViewById(R.id.editTextProductName);		//상품명
		m_textProductName.setNextFocusDownId(R.id.editTextPurchasePrice);
		
		m_et_purchasePrice = (EditText)findViewById(R.id.editTextPurchasePrice);		//매입가
		m_et_purchasePrice.setNextFocusDownId(R.id.editTextSalePrice);
		m_et_salePrice = (EditText)findViewById(R.id.editTextSalePrice);					//행사가
		m_et_salePrice.setNextFocusDownId(R.id.editTextAmount);
		m_et_purchaseCost = (EditText)findViewById(R.id.editTextPurchaseCost); 		//매입원가
		m_et_addTax = (EditText)findViewById(R.id.editTextAddTax);						//부가세
		m_purpriTaxyn = (CheckBox)findViewById(R.id.checkBoxTaxProduct); 			//과세상품 부가세 포함 별도 옵션
		m_amount = (EditText)findViewById(R.id.editTextAmount);						//발주수량
		m_profitRatio = (EditText)findViewById(R.id.editTextProfitRatio);					//이익률		
		
		m_textviewProSto = (TextView)findViewById(R.id.textViewProSto);				//안전재고
		m_textviewRealSto = (TextView)findViewById(R.id.textViewRealSto);				//현재고
				
		m_checkBoxRejectedProduct = (CheckBox)findViewById(R.id.checkBoxRejectedProduct);		//반품
		
		m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
		m_dateCalender1 = Calendar.getInstance();
		m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

		
		// 전체매입가 선언
		m_totalPurpri = (TextView) findViewById(R.id.textviewTPurPri);
		
		m_goodsGubun = (TextView)findViewById(R.id.textViewGoodsGubun); 		//행사상품 표시
		m_inDate = (TextView)findViewById(R.id.textViewinDate);							//입고예정일 표시		
		setInDate();																					//입고예정일
		
		m_bt_barcodeSearch = (Button)findViewById(R.id.buttonBarcode);				//상품검색
		saveButton = (Button)findViewById(R.id.buttonSave);				 				//저장/수정버튼
		m_checkBoxOfficeFix = (CheckBox)findViewById(R.id.checkboxOfiiceFix);	 	//거래처코드 고정
		
		m_listReadyToSend = (ListView) findViewById(R.id.listviewReadyToSendList);

		//목록 리스트
		String[] from = new String[] { "Or_Date", "Office_Code", "Office_Name", "nCount", "Price_Sum" };
		int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5 };
		m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_item4_15, from, to);
		m_listReadyToSend.setAdapter(m_adapter);
		
		m_listReadyToSend.setOnItemClickListener(this);
		
		
		/********************* 리스너 모음 *****************************************************/
		
		//입력수량 완료버튼 입력 시 자동저장 기능 리스너
		m_amount.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				//완료 버튼을 눌렀습니다.
				if(actionId == EditorInfo.IME_ACTION_DONE){				
					//단품 저장	
					//sendOrderData();
					//오류 검사후 저장
					checkData();

				}
				
				return false;
			}
		});
		
		m_amount.setImeOptions(EditorInfo.IME_ACTION_DONE);
		m_amount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					upu.setChangeBackGroundColor(0, m_amount);
				}else{
					upu.setChangeBackGroundColor(1, m_amount);
				}
			}
		});
		
		//거래처 고정 체크박스
		m_checkBoxOfficeFix.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub
				//거래처코드 고정
				if(isChecked){					
					m_customerCode.setEnabled(false);
					m_customerName.setEnabled(false);
					
				}else{
					m_customerCode.setEnabled(true);
					m_customerName.setEnabled(true);
				}
			}
		});
				
		
		// 바코드 입력 후 포커스 딴 곳을 옮길 경우
		m_textBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					upu.setChangeBackGroundColor(0, m_textBarcode);
					String barcode = m_textBarcode.getText().toString();
					//if (!barcode.equals("") && modify_position == 0) // 입력한 Barcode가 값이 있을 경우만
					if (!barcode.equals("")) 
						doQueryWithBarcode();
				}else{
					upu.setChangeBackGroundColor(1, m_textBarcode);
				}
			}
		});
		
		//새로고침 버튼
		Button reNew = (Button)findViewById(R.id.buttonRenew);
		reNew.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				setRenew();		
			
			}
		});
		
		
		//조회버튼
		Button btn_Search = (Button)findViewById(R.id.buttonSearch);
		btn_Search.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//전체 초기화 				
				
				//키보드 내리기
				//hideSoftKeyboard(false);
								
				//목록 불러오기(포스번호로)
				getTempInDPDAList();
				
				//매입합계금액표시								
			}
		});
		

		//저장버튼
		Button btn_Save = (Button)findViewById(R.id.buttonSave);
		btn_Save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub				
				//sendOrderData();
				//오류검사후 저장
				checkData();
				
			}
		});
		
		
		//스캔버튼 2017-04 m3mobile
		Button btn_Scanner = (Button)findViewById(R.id.buttonScan);
		btn_Scanner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				if(m3Mobile){
					Intent intent = new Intent(SCANNER_ACTION_SETTING_CHANGE);
					intent.putExtra("setting", "read_mode");		
					intent.putExtra("read_mode_value", 1);	
					mContext.sendOrderedBroadcast(intent, null);
					
					intent = new Intent(SCANNER_ACTION_START, null);
					mContext.sendOrderedBroadcast(intent, null);
				}
			}
		});
		
		
		// 매입가 + 이익률로 판매가
		m_et_purchasePrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					upu.setChangeBackGroundColor(0, m_et_purchasePrice);
					// 이익률 변경하기
					calculateProfitRatio();
					// 매입원가, 부가세 계산하기
					calculatePurpri();
				}else{
					upu.setChangeBackGroundColor(1, m_et_purchasePrice);
				}
			}
		});

		// 매입원가계산기 (매입원가로 매입가및 이익률 계산)
		m_et_purchaseCost.setEnabled(false);
		m_et_purchaseCost.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub

				if (!hasFocus) {
					
					upu.setChangeBackGroundColor(0, m_et_purchaseCost);
					// 매입원가를 이용해서 매입가를 만들어 줍니다.
					purpricostTopurpri();
					// 매입가를 이용해서 판매가와 계산해 이익률 만들어 줍니다.
					calculateProfitRatio();
					// 매입가를 다시 가공해서 매입원가와 부가세를 뿌려줍니다.
					calculatePurpri();

				}else{
					upu.setChangeBackGroundColor(1, m_et_purchaseCost);
				}
			}
		});

		// 매입원가 + 이익률로 판매가
		m_et_salePrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					upu.setChangeBackGroundColor(0, m_et_salePrice);
					String sell = m_et_salePrice.getText().toString();

					// 판매가 원단위 절사 하기
					if (!sell.equals("") || !sell.equals("0")) {
						m_et_salePrice.setText(sellpriceRound(sell));
					}
					calculateProfitRatio();
				}else{
					upu.setChangeBackGroundColor(1, m_et_salePrice);
				}
			}
		});

		// 이익률 계산기
		m_profitRatio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					
					upu.setChangeBackGroundColor(0, m_profitRatio);
					String ratio = m_profitRatio.getText().toString(); // 이익률
					String purchasePrice = m_et_purchasePrice.getText().toString(); // 매입가
					// String salePrice = m_et_salePrice.getText().toString();
					// //판매가

					if (!ratio.equals("") && !ratio.equals("0") && !purchasePrice.equals("0")
							&& !purchasePrice.equals("")) {
						float f_ratio = Float.valueOf(ratio).floatValue() / 100; // 퍼센트값
																					// 구함
						float f_purchasePrice = Float.parseFloat(purchasePrice); // 매입가
																					// float
																					// casting
						float f_salesPrice = f_purchasePrice / (1 - f_ratio); // 판매가
																				// 구하기

						// 판매가 반올림 수행
						int rest = ((int) (f_salesPrice / 10.0f)) * 10;
						int one = (int) f_salesPrice - rest;
						if (one >= 5)
							rest += 10;

						// 반올림 수행해서 역으로 이익률 계산
						f_ratio = (rest - f_purchasePrice) / rest;

						m_profitRatio.setText(String.format("%.2f", f_ratio * 100));
						m_et_salePrice.setText(String.valueOf(rest));
					}
				}else{
					upu.setChangeBackGroundColor(1, m_profitRatio);
				}
			}
		});
		
		
		// intent filter 2017-04 m3mobile
		if(m3Mobile){
			/*IntentFilter filter = new IntentFilter();
			filter.addAction(SCANNER_ACTION_BARCODE);
			registerReceiver(BarcodeIntentBroadcast, filter);*/			
			M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textBarcode, m_amount);			
		}		
		
		/***************** 생성자  ***************************************************************/
	}
	
	
	
	
	/**************	사용자 검색 함수   **************************************************************************/
		
	
	// 거래처 검색
	public void onCustomerSearch(View view) {
		
		if(m_checkBoxOfficeFix.isChecked() && m_saveYN ){
			Toast.makeText(this, "발주된 상품이 있습니다. \r\n거래처를 변경하시려면 체크를 해제해 주세요.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String customer = m_customerCode.getText().toString();
		String customername = m_customerName.getText().toString();
		Intent intent = new Intent(this, ManageCustomerListActivity.class);
		intent.putExtra("customer", customer);
		intent.putExtra("customername", customername);
		startActivityForResult(intent, CUSTOMER_MANAGER_REQUEST);
	}

	// 바코드 검색
	public void onBarcodeSearch(View view) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String list_value = pref.getString("prefSearchMethod", "");
		if (list_value.equals("camera")) {
			startCameraSearch();
		} else if (list_value.equals("list")) {
			startProductList();
		} else {
			//바코드 검색 버튼 클릭시 나오는 목록 셋팅
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
	
	// 상품 카메라 검색
	private void startCameraSearch() {
		Intent intent = new Intent(this, ZBarScannerActivity.class);
		startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
	}
	
	// 상품 목록검색
	private void startProductList() {
		String barcode = m_textBarcode.getText().toString();
		String gname = m_textProductName.getText().toString();
		String officecode = m_customerCode.getText().toString();
		Intent intent = new Intent(this, ManageProductListActivity.class);
		intent.putExtra("barcode", "");
		intent.putExtra("gname", gname);
		intent.putExtra("Office_Code", "");
		startActivityForResult(intent, BARCODE_MANAGER_REQUEST);
	}


	// 상품 검색 하기 SQL QUERY 실행
	public void doQueryWithBarcode() {

		String query = "";
		String barcode = m_textBarcode.getText().toString();
		String orDate = m_period.getText().toString();
		//m_period1 = "2017-06-11";
				
		if(barcode.equals("")){			
			return;
		}
		
		//query = "exec sp_executesql N'EXEC str_PDA_005Q ''"+orDate+"'', ''"+m_period1+"'', ''"+barcode+"'', '''', ''"+posID+"'', ''BC''' ";
		//query = "EXEC str_PDA_005Q '"+orDate+"', '"+m_period1+"', '"+barcode+"', '', '"+posID+"', 'BC' ";
		
		query = "Select A.*, ISNULL(B.Sale_Barcode, '') AS Sale_BarCode, ISNULL(B.Sale_Pur, 0) AS Sale_Pur, ISNULL(B.Sale_Sell, 0) AS Sale_Sell, ISNULL(B.Sale_ProfitRate, 0) AS Sale_ProfitRate, ISNULL(B.Sale_ProfitPri, 0) AS Sale_ProfitPri "
					+" From ( "
					+" Select  ISNULL(BarCode,'')    AS BarCode "
					+"	, ISNULL(BarCode1,'')   AS BarCode1 "
			        +" , ISNULL(G_Name,'')     AS G_Name "
			        +" , ISNULL(Pur_Pri,0)     AS Pur_Pri "
			        +" , ISNULL(Sell_Pri,0)    AS Sell_Pri "
			        +" , ISNULL(Profit_Rate,0) AS Profit_Rate "
			        +" , ISNULL(Tax_YN,'')     AS Tax_YN "
			        +" , ISNULL(Obtain,0)      AS Obtain "
			        +" , ISNULL(Pur_Cost,0)    AS Pur_Cost "
			        +" , ISNULL(Add_Tax,0)     AS Add_Tax "
			        +" , ISNULL(Pro_Sto,0)     AS Pro_Sto "
			        +" , ISNULL(Real_Sto,0)    AS Real_Sto "
			        +" , ISNULL(Bot_Code,'')   AS Bot_Code "
			        +" , ISNULL(Bot_Name,'')   AS Bot_Name " 
			        +" , ISNULL(Bot_Pur,0)     AS Bot_Pur "
			        +" , ISNULL(Bot_Sell,0)    AS Bot_Sell "
			        +" , ISNULL(Box_Use,'0')   AS Box_Use "
			        +" , ISNULL(Scale_Use,'0') AS Scale_Use "
			        +" , ISNULL(Bus_Code,'')   AS Bus_Code "
			        +" , ISNULL(Bus_Name,'')   AS Bus_Name "
			        +" , ISNULL(VAT_CHK,'1')     AS VAT_CHK "  
					+"	From Goods "
					+"	Where Barcode='"+barcode+"' "
					+") A Left Join ( "
					+"	Select TOP 1 Barcode AS Sale_Barcode, ISNULL(Sale_Pur, 0) AS Sale_Pur, ISNULL(Sale_Sell, 0) AS Sale_Sell, ISNULL(Profit_Rate, 0) AS Sale_ProfitRate, ISNULL(Profit_Pri, 0) AS Sale_ProfitPri "
					+"	From Evt_Mst " 
					+"	Where Barcode='"+barcode+"' And  (inEvt_SDate <= '"+m_period1+"' AND inEvt_EDate >= '"+m_period1+"' ) "
					+") B "
					+" on A.Barcode=B.Sale_Barcode";	
		
		
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
								
				if (results.length() > 0) { // 검색결과 확인
					try {
						
						JSONObject obj = results.getJSONObject(0);
						
						//검색된 상품 원정보 기록하기
						setGoodsInfo(obj);						
						
						// 상품명 원레 이름 등록
						m_textProductName.setText(obj.getString("G_Name"));
						
						//행사상품일때 행사 매입가로 출력합니다.
						if( "".equals(obj.getString("Sale_BarCode"))){
							
							System.out.println("일반상품");
							setSaleGubun("일반");
							String s = obj.getString("Pur_Pri");
							m_et_purchasePrice.setText(s);
							m_et_purchaseCost.setText(obj.getString("Pur_Cost"));
							m_et_addTax.setText(obj.getString("Add_Tax"));
							m_et_salePrice.setText(obj.getString("Sell_Pri"));
							m_profitRatio.setText(obj.getString("Profit_Rate"));
							
							// 상품장 부가세 별도 포함 상품
							String m_vatchk = obj.getString("VAT_CHK").toString();
							setTaxSeparate(obj.getString("Tax_YN").toString(), m_vatchk.toString());
							
						}else{
							
							System.out.println("행사상품");		
							setSaleGubun("행사");
							String p = obj.getString("Sale_Pur");
							String s = obj.getString("Sale_Sell");
							
							m_et_purchasePrice.setText(p);
							
							//행사매입가로 매입원가 및 부가세 계산해서 표시하기
							calculatePurpri();							
							
							m_et_salePrice.setText(s);
							m_profitRatio.setText(obj.getString("Sale_ProfitRate"));
							
							// 상품장 부가세 별도 포함 상품
							String m_vatchk = obj.getString("VAT_CHK").toString();
							setTaxSeparate(obj.getString("Tax_YN").toString(), m_vatchk.toString());
							
							//원상품장에 행사상품 매입가/판매가/부가세를 입력합니다.
							m_tempProduct.put("Pur_Pri", p);
							m_tempProduct.put("Pur_Cost", m_et_purchaseCost.getText().toString());
							m_tempProduct.put("Add_Tax", m_et_addTax.getText().toString());
							m_tempProduct.put("Sell_Pri", s);
														
						}					
						
						//안전재고 현재고 입력
						String proSto = "안전재고 : " + obj.get("Pro_Sto");
						String realSto = "현재고 : " + obj.get("Real_Sto"); 
						m_textviewProSto.setText(proSto);
						m_textviewRealSto.setText(realSto);						
						
						m_amount.requestFocus();

						//Log.i("거래처코드 비어있지?", m_customerCode.getText().toString());
						// 거래처 선택 되어 있을시에 거래처 변경 안되게 설정 됩니다.						
						if(m_checkBoxOfficeFix.isChecked()){
							if (m_customerCode.getText().toString().equals("")) {
								m_customerCode.setText(obj.getString("Bus_Code"));
								m_customerName.setText(obj.getString("Bus_Name"));								
							}							
						}else{
							m_customerCode.setText(obj.getString("Bus_Code"));
							m_customerName.setText(obj.getString("Bus_Name"));
						}
																		
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}else{
					Toast.makeText(getApplicationContext(), "등록된 상품이 없습니다.", Toast.LENGTH_SHORT).show();
					return;
				}
				
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				Toast.makeText(ManageBaljuRegActivity.this, "상품검색 실패\r\n" + msg, Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
	}

	
	//저장된 발주목록을 불러옵니다.
	private void getTempInDPDAList(){
		
		//초기화
		mfillMaps.removeAll(mfillMaps);			
		m_adapter.notifyDataSetChanged();
		String query = "exec sp_executesql N'EXEC str_PDA_019Q ''"+posID+"'''";
				
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
				if(results.length() <= 0){					
					Toast.makeText(getApplicationContext(), "조회결과가 없습니다.", Toast.LENGTH_SHORT).show();					
					return;
				}
				
					
				try {
					
					for(int i=0; i < results.length(); i++){
						JSONObject map = results.getJSONObject(i);
						HashMap<String, String> temp = new HashMap<String, String>();
						temp.put("Or_Date", map.get("Or_Date").toString());
						temp.put("Office_Code", map.get("Office_Code").toString());
						temp.put("Office_Name", map.get("Office_Name").toString());
						temp.put("nCount", map.get("nCount").toString());
						temp.put("Price_Sum", map.get("Price_Sum").toString());
						
						mfillMaps.add(temp);			
					}
					
				} catch (JSONException e) {
					Toast.makeText(getApplicationContext(), "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}

				m_adapter.notifyDataSetChanged();
				//총매입가 계산
				makeFillvapsWithStockList();
				
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

	}	
	
	/**************	사용자 검색 함수  끝 **************************************************************************/
	
	
	
	/**************	 초기화 함수   **************************************************************************/
	
	//새로입력
	private void setRenew(){
		System.out.println("새로고침");
		
		//임시상품 데이터 삭제
		m_tempProduct.clear();
		
		//거래처고정
		if(!m_checkBoxOfficeFix.isChecked()){
			m_customerName.setText("");	//거래처명
			m_customerCode.setText("");		//거래처코드
		}
		
		//행사구분 
		setSaleGubun("일반");
		
		m_profitRatio.setText("");				//이익률
		m_amount.setText("");					//수량
		m_purpriTaxyn.setSelected(true);	//별도포함
		m_et_addTax.setText("");				//부가세
		m_et_purchaseCost.setText("");		//매입원가
		m_et_salePrice.setText("");				//행사가
		m_et_purchasePrice.setText(""); 		//매입가
		m_textProductName.setText(""); 		//상품명
		m_textBarcode.setText("");			//바코드
		
		m_textviewProSto.setText("안전재고 : 0");
		m_textviewRealSto.setText("현재고 : 0");
		
		// 전체매입가 초기화
		//m_totalPurpri.setText("");
		//totalPurPri = 0.0;
		
		/*m_textBarcode.setFocusable(true);
		m_textBarcode.setClickable(true);*/
		
		m_textBarcode.requestFocus();
		
		hideSoftKeyboard(true);
	}
	
	/**************	 초기화 함수  끝 **************************************************************************/
	
	
	
	
	
	
	
	
	/**************	사용자 처리 함수   **************************************************************************/

	/**
	 * * 기본 입력 정보를 체크 합니다.
	 * 
	 * 유효성 체크
	 * 
	 */
	public void checkData() {
		
		
		String purchaseDate = m_period.getText().toString();
		String barcode = m_textBarcode.getText().toString();
		String productName = m_textProductName.getText().toString();
		String code = m_customerCode.getText().toString();
		String name = m_customerName.getText().toString();
		String purchasePrice = m_et_purchasePrice.getText().toString();
		String purchaseCost = m_et_purchaseCost.getText().toString();
		String salePrice = m_et_salePrice.getText().toString();
		String amount = m_amount.getText().toString();
		String profitRatio = m_profitRatio.getText().toString();

		// 비어 있는 값 확인
		if (code.equals("")){
			//거래처 코드
			Toast.makeText(mContext, "거래처 코드를 입력해 주세요!", Toast.LENGTH_SHORT).show();			
			m_customerCode.requestFocus();
			return;
		}
				
		if(name.equals("")){
			//거래처명
			Toast.makeText(mContext, "거래처명을 입력해 주세요!", Toast.LENGTH_SHORT).show();			
			m_customerName.requestFocus();
			return;
		}
				
		if(purchaseDate.equals("")){
			//발주일자
			Toast.makeText(mContext, "매입일을 입력해 주세요!", Toast.LENGTH_SHORT).show();			
			m_period.requestFocus();
			return;
		}
		
				
		//바코드
		if(barcode.equals("")){
			//바코드
			Toast.makeText(mContext, "바코드를 입력해 주세요!", Toast.LENGTH_SHORT).show();			
			m_textBarcode.requestFocus();
			return;
		}
		
		//상품명
		if(productName.equals("")){
			//상품명
			Toast.makeText(mContext, "상품명을 입력해 주세요!", Toast.LENGTH_SHORT).show();			
			m_textProductName.requestFocus();
			return;
		}
		
		//매입가
		if(purchasePrice.equals("")){			
			//매입가
			Toast.makeText(mContext, "발주가를 입력해 주세요!", Toast.LENGTH_SHORT).show();			
			m_et_purchasePrice.requestFocus();
			return;
		}
		
		//판매가
		if(salePrice.equals("")){
			//판매가
			Toast.makeText(mContext, "판매가를 입력해 주세요!", Toast.LENGTH_SHORT).show();			
			m_et_salePrice.requestFocus();
			return;
		}
		 
		if(amount.equals("")){
			//수량
			Toast.makeText(mContext, "발주 수량을 입력해 주세요!", Toast.LENGTH_SHORT).show();			
			m_amount.requestFocus();
			return;
		}
		
		if(profitRatio.equals("")) {
			//이익률
			Toast.makeText(mContext, "이익률을 확인해 주세요!", Toast.LENGTH_SHORT).show();			
			m_profitRatio.requestFocus();
			return;
		}

		
		
		// 0원 입력값 확인
		try{
		
			if (Float.parseFloat(purchasePrice) < 0){	//매입가
				Toast.makeText(getApplicationContext(), "발주가를 입력해 주세요.", Toast.LENGTH_SHORT).show();
				m_et_purchasePrice.requestFocus();
				return;
			}
					
			if(Float.parseFloat(salePrice) < 0 ){		//판매가				
				Toast.makeText(getApplicationContext(), "판매가를 입력해 주세요.", Toast.LENGTH_SHORT).show();
				m_et_salePrice.requestFocus();
				return;
			}
			
			if(Float.parseFloat(amount) <= 0){				
				Toast.makeText(getApplicationContext(), "발주 수량을 입력해 주셔야 합니다.", Toast.LENGTH_SHORT).show();
				m_amount.requestFocus();
				return;
			}

		}catch(NumberFormatException e){
			m_et_purchasePrice.requestFocus();
			Toast.makeText(getApplicationContext(), "숫자만 입력 하실 수 있습니다.", Toast.LENGTH_SHORT).show();
			return;
		}
				

		// 이익률 체크시 숫자가 아닌 문자형식 들어갔을때 체크합니다.
		float ratio;
		try {
			ratio = Float.parseFloat(profitRatio);
			// 이이률 이상한 값 계산하기
			/*
			2019-07-16 (-)이익율 처리 가능
			if (ratio < 0 || ratio > 200) {
				Toast.makeText(getApplicationContext(), "이익률을 확인해 주세요", Toast.LENGTH_SHORT).show();
				return;
			}
			*/

		} catch (NumberFormatException e) {
			Toast.makeText(getApplicationContext(), "이익률을 확인해 주세요", Toast.LENGTH_SHORT).show();
			return;
		}

		// 매입가 체크
		if (Double.parseDouble(purchasePrice) > 10000000.00) {
			Toast.makeText(getApplicationContext(), " 발주가를 확인해 주세요.", Toast.LENGTH_SHORT).show();
			m_et_purchasePrice.requestFocus();
			return;
		}

		// 매입원가 체크
		if (Double.parseDouble(purchaseCost) < 10) {
			Toast.makeText(getApplicationContext(), " 발주원가를 확인해 주세요.", Toast.LENGTH_SHORT).show();
			m_et_purchaseCost.requestFocus();
			return;
		}

		// 수량 체크
		if (amount.length() >= 5 || amount.equals("0")) {
			Toast.makeText(getApplicationContext(), " 수량을 확인해 주세요.", Toast.LENGTH_SHORT).show();
			m_amount.requestFocus();
			return;
		}

		// 포커스가 수량에 없을때 판매가 및 매입가 이익률 변경되고 적용 안되었을때!!
		if (!m_amount.hasFocus()) {
			if (m_et_purchasePrice.hasFocus()) {
				calculateProfitRatio();
				calculatePurpri();
			} else if (m_et_salePrice.hasFocus()) {
				calculateProfitRatio();
			} else if (m_et_purchaseCost.hasFocus()) {
				purpricostTopurpri();
				calculateProfitRatio();
				calculatePurpri();
			}
		}
		
		//발주일이 오늘 날자와 같지 않고 매입일 고정에 체크가 풀려있다면
		if(!purchaseDate.equals(upu.getTodayData()) && !m_purdataFix.isChecked() ){
			
			String str = "현재 발주일이 오늘 날자와 일치하지 않습니다.\r\n"					
					+ "- 예를 선택하시면 지정날자로 발주를 잡으실 수 있습니다.\r\n"
					+ "  지정일자로 발주작업을 완료 후 꼭 발주일체크를 해제해 주세요!"
					+ "- 아니오를 선택하시면 발주일이 현재 날자로 변경됩니다.";

			//----------------------------------------//
			// 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
			//----------------------------------------//
			//AlertDialog.Builder alt_bld = new AlertDialog.Builder(mContext);
			AlertDialog.Builder alt_bld = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
			//----------------------------------------//

			alt_bld.setMessage(str)
		    .setCancelable(false)
		    .setPositiveButton("예", new DialogInterface.OnClickListener(){
		        public void onClick(DialogInterface dialog, int id) {		            
		        	m_purdataFix.setChecked(true);
		        	sendOrderData();
		        	//dialog.cancel(); 
		        }
		    })
		    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {        	
		        	m_period.setText(upu.getTodayData());
		        	sendOrderData();
		            //dialog.cancel();           
		        }
		    });
		    
		    AlertDialog alert = alt_bld.create();
		    // Title for AlertDialog
		    alert.setTitle("발주일 지정오류");
		    // Icon for AlertDialog
		    //alert.setIcon(R.drawable.icon);
		    alert.show();
			
		}else{
			sendOrderData();
		}

		//목록 불러오기(포스번호로)
		//getTempInDPDAList();

	}
	
	
	private void setInDate(){
		
		m_inDate.setText("입고예정일 : "+m_period.getText());
		//입고예정일 변경
		Calendar temp = Calendar.getInstance();		
		temp.add(temp.DAY_OF_MONTH, 2);
		m_period1 = m_dateFormatter.format(temp.getTime());
		m_inDate.setText("입고예정일 : "+m_period1);
		Log.d("입고예정일", m_period1);
	}
	
	
	
	/**
	 * 일반 // 행사 구분 추가 
	 * @param gubun 행사, 일반 으로 구분
	 */
	private void setSaleGubun(String gubun){
		
		if("행사".equals(gubun)){			
			String str = "행사상품";
			
			m_goodsGubun.setText(str);
			m_goodsGubun.setTextColor(Color.RED);
		}else{
			String str = "일반상품";
			m_goodsGubun.setText(str);
			m_goodsGubun.setTextColor(Color.WHITE);
		}
		
	}
	
	
	//원상품 검색 정보 임시테이블에 저장하기
	private void setGoodsInfo(JSONObject obj){
		
		//원상품정보 초기화
		m_tempProduct.clear();
		
		Iterator<String> iter = obj.keys();		
		while(iter.hasNext()){
			String s= iter.next();
			try {
				m_tempProduct.put(s, obj.getString(s));
			} catch (JSONException e) {				
				m_tempProduct.put(s, "");				
			}
		}		
		
	}
	
	
	// 판매가 원단위 절사
	private String sellpriceRound(String price) {
		String sell_price = price.toString();
		
		try{
			sell_price = String.valueOf(Math.round(Double.parseDouble(sell_price) / 10) * 10);
		}catch(NumberFormatException e){
			sell_price="0";
		}
		
		return sell_price;
	}

	// 매입가, 판매가로 이익률 계산하기
	private void calculateProfitRatio() {
		Log.i("매입가 판매가 연동 계산", "계산 시작합니다.");
		String ratio = m_profitRatio.getText().toString();
		String purchasePrice = m_et_purchasePrice.getText().toString();
		String salePrice = m_et_salePrice.getText().toString();

		Log.i("이익률/매입가/판매가", ratio + purchasePrice + salePrice);

		if (!ratio.equals("") && !purchasePrice.equals("") && !salePrice.equals("")) {
			float f_ratio = Float.valueOf(ratio).floatValue();
			float f_purchasePrice = Float.valueOf(purchasePrice).floatValue();
			float f_salePrice = Float.valueOf(salePrice).floatValue();
			f_ratio = (f_salePrice - f_purchasePrice) / f_salePrice * 100;
			m_profitRatio.setText(String.format("%.2f", f_ratio));
		}

		Log.i("매입가 판매가 연동 계산", "계산 완료.");
	}

	//매입가로 매입원가 이익률 계산하기
	private void calculatePurpri() {

		String tax_yn = m_tempProduct.get("Tax_YN");
		String pur_pri = m_et_purchasePrice.getText().toString();

		// 매입가 변경시 부가세 및 원가 변경하기
		if (!pur_pri.toString().equals("") && !pur_pri.toString().equals("0") && !pur_pri.toString().equals("0.00")) {
			float f_purchasePrice = 0.00f;
			try{
				f_purchasePrice = Float.parseFloat(pur_pri);
			}catch(NumberFormatException e){
				Toast.makeText(this, "매입가 입력 오류입니다.\r\n"+e.getMessage(), Toast.LENGTH_SHORT).show();
				m_et_purchasePrice.requestFocus();
				return;
			}			
			
			// m_tempProduct.put("Pur_Pri", String.format("%.2f",
			// f_purchasePrice)); // 매입가
			
			if(f_purchasePrice == 0){
				Toast.makeText(this, "매입가 0원은 계산할 수 없습니다.", Toast.LENGTH_SHORT).show();
				m_et_purchasePrice.requestFocus();
				return;
			}			

			if (tax_yn.equals("1")) { // 과세
				m_et_purchaseCost.setText(String.format("%.2f", f_purchasePrice / 1.1)); // 매입원가
				m_et_addTax.setText(String.format("%.2f", f_purchasePrice - (f_purchasePrice / 1.1)));// 부가세
			}
			if (tax_yn.equals("0")) { // 면세
				m_et_purchaseCost.setText(String.format("%.2f", f_purchasePrice)); // 매입원가
				m_et_addTax.setText(String.format("%.2f", 0f));// 부가세
			}
		} else {
			m_et_purchaseCost.setText("0.00"); // 매입원가
			m_et_addTax.setText("0.00");// 부가세
		}
	}

	// 매입원가로 매입가 만들기
	private void purpricostTopurpri() {
		// 매입원가를 불러와서 매입가를 만들어 줍니다.
		String purpricost = m_et_purchaseCost.getText().toString();
		if (!purpricost.equals("") && purpricost.length() > 1) {
			double purchaseCost = Double.parseDouble(purpricost);
			purchaseCost = Math.round(purchaseCost * 1.1);
			m_et_purchasePrice.setText(String.valueOf(purchaseCost));
		}
	}	
	
	
	
	
	// 부가세 별도 포함 선택 함수
	private void setTaxSeparate(String s, String p) {

		Log.i("첫번째 매개변수", s.toString() + p.toString());
		// Tax_YN 이 1이면 과세 상품이므로 활성화 해야합니다.
		// s는 Tax_YN구분 p는 VAT_CHK 구분

		if (s.equals("1")) {
			m_purpriTaxyn.setEnabled(true);
		} else {
			//면세상품 알림 표시
			//scanNewProductSpeak("면세상품 입니다.");
			m_purpriTaxyn.setEnabled(false);
		}

		if (p.equals("1")) {
			m_purpriTaxyn.setChecked(true);
		} else {
			m_purpriTaxyn.setChecked(false);
			if (s.equals("1")) {
				m_et_purchaseCost.setEnabled(true);
			}
		}
	}
	
	//키보드 없애기
	protected void hideSoftKeyboard(boolean onoff) {
	    InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
	    if(onoff){
	    	//imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 1);	    	
	    	imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	    }else{
	    	//imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);	    	
	    	imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);	    
	    }	    
	}
	
	
	// 매입가 계산해서 보여 주기
	public void makeFillvapsWithStockList() {			

		Iterator<HashMap<String, String>> iterator = mfillMaps.iterator();
		totalPurPri = 0.0;
		while (iterator.hasNext()) {				
			HashMap<String, String> element = iterator.next();			
			totalPurPri += Double.valueOf(element.get("Price_Sum").replace(",", ""));
			Log.i("결과물", String.format("%.2f", totalPurPri));				
		}

		// 전체 매입가 결과 출력
		m_totalPurpri.setText(String.format("%,.2f", totalPurPri));
		
	}
	
	
	/*************  사용자 처리 함수 끝  ********************************************************************/
		
	
	
	
	
	/**************	등록/저장/전송/삭제 처리 함수  *******************************************************/
	
	//발주저장
	private void sendOrderData(){
		
		//유효성 체크
		//checkData();
		
		String orDate = m_period.getText().toString();
		String office_Code = m_customerCode.getText().toString();
				
		//order테이블에 전표를 불러 옵니다.
		String query = "exec sp_executesql N'EXEC str_PDA_005Q ''"+orDate+"'', '''', '''', ''"+office_Code+"'', ''"+posID+"'', ''MI''' ";
				
		// 로딩 다이알로그
		dialog = new ProgressDialog(this);
		dialog.setMessage("Loading....");
		dialog.setCancelable(false);
		dialog.show();

		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				
				if (results.length() > 0) {

					// 1번 검색결과 OK
					// 2번 검색번호 000
					// 3번 추가여부 Y - N
					try {
						JSONObject obj = results.getJSONObject(0);
						String res = (String) obj.get("1");
						String num = (String) obj.get("2");
						String gubun = (String) obj.get("3");

						if (!res.equals("OK")) {
							dialog.dismiss();
							dialog.cancel();
							Toast.makeText(getApplicationContext(), "저장실패 \r\n" + res, Toast.LENGTH_SHORT).show();
							return;
						}

						// 전표번호를 생성합니다.
						if (gubun.equals("Y")) {
							try {
								Log.d("전표번호 : ", num);
								int num_b = Integer.parseInt(num);
								num_b++;

								// 숫자를 문자형식으로 변경
								num = String.format("%03d", num_b);

							} catch (NumberFormatException e) {
								dialog.dismiss();
								dialog.cancel();
								Toast.makeText(getApplicationContext(), "숫자변환 오류", Toast.LENGTH_SHORT).show();
								return;
							}

							// 상품 전송							
							doSendSaveData(num);
						} else {							
							doSendSaveData(num);
						}
						

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						dialog.dismiss();
						dialog.cancel();
						
						Toast.makeText(getApplicationContext(), "전송실패(" + e.getMessage() + ")", Toast.LENGTH_SHORT)
								.show();
						return;
					}

				}
				// Toast.makeText(getApplicationContext(), "저장완료.",
				// Toast.LENGTH_SHORT).show();
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
	
	
	// 발주상품 temp_ord 테이블 저장
	public void doSendSaveData(String num) {				
		
		String query = "exec sp_executesql N'EXEC str_PDA_005I ";

		// 입력화면에서 정보 가져오기
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		// 입력창과 상관없이 현재 시간으로 등록일 수정일 불러오기
		String currentDate = sdf.format(new Date());
				
		String sale_yn = m_tempProduct.get("Sale_BarCode");						//행사 시 행사 바코드
		
		// 발주등록창의 발주일 불러오기
		String purchaseDate = m_period.getText().toString();						//발주일
		
		String barcode = m_textBarcode.getText().toString();						//바코드		
		//String productName = m_textProductName.getText().toString();		//상품명
		String code = m_customerCode.getText().toString();						//거래처코드
		String name = m_customerName.getText().toString();						//거래처명
		boolean Vat_Chk = m_purpriTaxyn.isChecked();								//면과세
		String Tax_YN = m_tempProduct.get("Tax_YN"); 								// 과세여부(0:면세,1:과세)
		String VAT_CHK = m_tempProduct.get("VAT_CHK"); // 부가세구분(0:별도,1:포함)
		
		String amount = m_amount.getText().toString();								//발주수량
		String pro_sto = m_tempProduct.get("Pro_Sto");								//안전재고
		String real_sto = m_tempProduct.get("Real_Sto");							//현재고
		
		String purchasePrice = m_et_purchasePrice.getText().toString();			//발주가
		String Org_PurPri = m_tempProduct.get("Pur_Pri");							//원발주가		
		String Pur_Cost = m_et_purchaseCost.getText().toString();					//발주원가
		String Add_Tax = m_et_addTax.getText().toString();							//발주부가세
		
		String salePrice = m_et_salePrice.getText().toString();						//판매가
		String Org_SellPri = m_tempProduct.get("Sell_Pri");							//원판매가
		String profitRatio = m_profitRatio.getText().toString();						//이익율
		
		String Bot_Code = m_tempProduct.get("Bot_Code"); 						// 공병코드
		String Bot_Name = m_tempProduct.get("Bot_Name"); 						// 공병명
		String Bot_Pur = m_tempProduct.get("Bot_Pur");							// 공병매입가
		String Bot_Sell = m_tempProduct.get("Bot_Sell"); 							// 공병매출가
		String Summarize = "정상가";
		
		String Box_Use = m_tempProduct.get("Box_Use"); // 박스상품여부(0:미사용,1:사용)
		String BarCode1;
		if (m_tempProduct.containsKey("BarCode1")) {
			BarCode1 = m_tempProduct.get("BarCode1"); // 박스 바코드
		} else {
			BarCode1 = "";
		}
		
		String Scale_Use = m_tempProduct.get("Scale_Use"); 			// 저울상품
		String Obtain = m_tempProduct.get("Obtain"); 					// 입수수량
		
		//String Pur_DCRate = "0"; 									// 매입DC률 매입가에서 적용 %만큼 DC한다
		//String Pur_PriDC = "0"; 									// 매입DC금액 %DC된 금액
				
		String Tax_Gubun = "1"; 									// 부가세포함, 별도 구분
		if (Vat_Chk) {
			Tax_Gubun = "1";			
		} else {
			//부가세 별도입니다. 오리지널도 부가세로 표기해 주세요
			//매입가-> 매입원가로
			//오리지널매입가->오리지널매입원가로
			purchasePrice = m_et_purchaseCost.getText().toString();
			Org_PurPri = m_tempProduct.get("Pur_Cost");			
			
			Tax_Gubun = "0";
			Summarize = "별도가";	
		}
		
		//String Pack_Use = m_tempProduct.get("Pack_Use"); // 묶음상품여부(0:미사용,1:사용)
						
		if (!purchasePrice.equals(Org_PurPri))
			Summarize += ", 매입가변경";
		if (!salePrice.equals(Org_SellPri))
			Summarize += ", 판매가변경";
		
		if (Bot_Pur == null)
			Bot_Pur = "0";
		if (Bot_Sell == null)
			Bot_Sell = "0";

		// 반품 체크되어있을시에 수량 -값 적용 하기
		if (m_checkBoxRejectedProduct.isChecked()) {
			amount = String.valueOf(Integer.valueOf(amount) * -1);
		}
		
		// 쿼리에 저장하기
		//exec sp_executesql N'EXEC str_PDA_005I ''20170330H001'',''2017-03-30'',''2017-04-02'',
		//''88006611'',''00002'',''거래처2'',''1'',''1'',10,2,12,1900.00,1900.00,1727.27,172.73,4500,4500,57.78,''''
		//,'''',0.00,0,''정상가'',''1'', ''0'',''0'',''0'','''',''0'',0.00'
		Log.d("전표번호", num);

		// 전표번호
		String j_num = purchaseDate.replace("-", "") + posID + num;
		query += "''" + j_num + "'', " // 전표번호				
				+ "''" + purchaseDate + "'', " // 발주일
				+ "''" + m_period1 + "'', " // 입고예정일
				+ "''" + barcode + "'', " // 바코드
				+ "''" + code + "'', " // 거래처코드
				+ "''" + name + "'', " // 거래처명
				+ "''" + Tax_YN + "'', " // 면과세 (1:과세 0:면세)				
				+ "''" + VAT_CHK + "'', " // 1:포함, 0:별도 별도시 Summarize 별도가 넣어야함
				+ amount + ", " // 발주수량
				+ pro_sto + ", " // 안전재고
				+ real_sto + ", " // 현재고				
				+ purchasePrice + ", " // 발주가
				+ Org_PurPri + ", " // 원발주가
				+ Pur_Cost + ", " // 발주원가
				+ Add_Tax + ", " // 발주과세
				+ salePrice + ", " // 판매가
				+ Org_SellPri + ", " // 원판매가
				+ profitRatio + ", " // 이익률
				+ "''" + Bot_Code + "'', " // 공병코드
				+ "''" + Bot_Name + "'', " // 공병명
				+ Bot_Pur + ", " // 공병매입가
				+ Bot_Sell + ", " // 공병판매가
				+ "''" + Summarize +"'', "	//비고
				+ "''" + posID +"'', "	//작성자
				+ "''0'', "	//거래처변경
				+ "''0'', "	//매입가변경
				+ "''" + Box_Use + "'', " // 박스상품 (0:박스아님 1:박스상품)
				+ "''" + BarCode1 + "'', " // 상위상품
				+ "''" + Scale_Use + "'', " // 저울상품 (1:저울상품, 0:공산품)
				+ "" + Obtain + "'"; // 입수
							
				
		//쿼리전송
		sendToQuery(query);

		
	}

	// 상품 저장하기 2017-04 저장프로시저 사용하기
	private void sendToQuery(String query) {

		// 로딩 다이알로그
		/*dialog = new ProgressDialog(this);
		dialog.setMessage("Loading....");
		dialog.setCancelable(false);
		dialog.show();*/

		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				if (results.length() > 0) {

					// 1번 검색결과 OK
					// 2번 검색번호 000
					// 3번 추가여부 Y - N
					try {

						JSONObject obj = results.getJSONObject(0);
						Log.d("저장결과 : ", obj.toString());
						String res = (String) obj.get("1");

						if ("OK".equals(res)) {

							//조회???
							//목록 불러오기(포스번호로)
							getTempInDPDAList();

							//초기화 정상등록 완료
							setRenew();
							
						} else {
							Toast.makeText(getApplicationContext(), "저장실패(" + res + ")", Toast.LENGTH_SHORT).show();
							return;
						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						Toast.makeText(getApplicationContext(), "전송실패(" + e.getMessage() + ")", Toast.LENGTH_SHORT)
								.show();
						return;
					}

				}

				Toast.makeText(getApplicationContext(), "저장완료.", Toast.LENGTH_SHORT).show();
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
	
	
	//발주장 삭제
	private void transferDelete(){
				
		HashMap<String, String> map = mfillMaps.get(m_selectedListIndex);
		
		String office_code = map.get("Office_Code");
		String or_date = map.get("Or_Date");
		
		String query = "exec sp_executesql N'EXEC str_PDA_019D ''"+office_code+"'', ''"+or_date+"'', ''"+posID+"'', ''DS'''";
		
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
				try {
					String res = (String) results.getJSONObject(0).get("1");
					if (!"OK".equals(res)) {
						Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
						return;
					}

					//재조회합니다.
					m_selectedListIndex = -1;
					getTempInDPDAList();
					Toast.makeText(getApplicationContext(), "삭제 성공", Toast.LENGTH_SHORT).show();
													
				} catch (JSONException e) {
					Toast.makeText(getApplicationContext(), "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "삭제 실패", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
		
	}
		
	
	//발주장 한장씩 전송하기
	private void toSendPurList(int num){
		
		final int select_num = num;
				
		HashMap<String, String> map = mfillMaps.get(select_num);
		String table_name = map.get("Or_Date");
		table_name = table_name.replace("-", "").substring(0, 6);
		Log.d("테이블명", table_name);
		
		String query = "exec sp_executesql N'SELECT ISNULL(MAX(RIGHT(Or_Num,3)),''000'') FROM OrT_"+table_name+" WHERE Or_Date=''"+map.get("Or_Date")+"'' AND SUBSTRING(Or_Num,9,1) =''"+posID+"''' ";
		
		// 로딩 다이알로그
		dialog = new ProgressDialog(this);
		dialog.setMessage("Loading....");
		dialog.show();

		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				
				try {					
					String res = (String) results.getJSONObject(0).get("1");
					
					Log.d("전표번호 : ", res);
					int num_b = Integer.parseInt(res);
					num_b++;

					// 숫자를 문자형식으로 변경
					res = String.format("%03d", num_b);
					
					//매입장 전송
					toSendPurList_sub(res, select_num);					
					
				} catch (JSONException  e) {
					Toast.makeText(getApplicationContext(), "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
					dialog.dismiss();
					dialog.cancel();
					return;
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "등록 실패", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
				
	}
	
	/**
	 * 매입장에 전송
	 * @param num 전표번호 뒷자리
	 * @param select_num temp_indpda에서 선택된 전표
	 */
	private void toSendPurList_sub(String num, int select_num){				
		String gubun_num = num;
		int sel_num = select_num;		
		
		HashMap<String,String> map = mfillMaps.get(sel_num);
		
		String office_code = map.get("Office_Code");
		String or_date = map.get("Or_Date");
		String junpyo_num = or_date.replace("-", "")+posID+gubun_num;
		Log.d("전표번호", junpyo_num);
		
		String query = "exec sp_executesql N'EXEC str_PDA_019I ''"+office_code+"'', ''"+or_date+"'', ''"+userID+"'', ''"+posID+"'', ''"+junpyo_num+"'''";
				
		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {
	
			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				try {
					String res = (String) results.getJSONObject(0).get("1");
					if (!"OK".equals(res)) {
						Toast.makeText(getApplicationContext(), res, Toast.LENGTH_SHORT).show();
						return;
					}
	
					
					if(mAllSend){
						
						//나머지 전표를 다시 전송 합니다.
						m_selectedListIndex++;
						
						//전체전송중 전송완료 수량과 전송나머지 수량이 같다면 종료
						if(m_selectedListIndex == mfillMaps.size()){
							//재조회합니다.
							m_selectedListIndex = -1;
							getTempInDPDAList();
							Toast.makeText(mContext, "전송완료", Toast.LENGTH_SHORT).show();
							return;
						}
						
						toSendPurList(m_selectedListIndex);						
						
					}else{
						//재조회합니다.				
						m_selectedListIndex = -1;
						getTempInDPDAList();
						Toast.makeText(mContext, "전송완료", Toast.LENGTH_SHORT).show();
					}
					
					Toast.makeText(getApplicationContext(), "전송완료", Toast.LENGTH_SHORT).show();
				} catch (JSONException e) {
					Toast.makeText(getApplicationContext(), "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}
	
			}
	
			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "등록 실패", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
		
		
	}
	
	//전체전송
	public void sendAlltempData(View view){
		
		int mCount = mfillMaps.size();
		
		if(mCount <= 0 ) return;
		
		//전체전송입니다.
		mAllSend = true;
		m_selectedListIndex = 0;
		toSendPurList(m_selectedListIndex);	
		
		
	}
	
	
	/**************	등록/저장/전송/삭제 처리 끝   *********************************************************/
	
	
	
	
	
	/**************	시스템 함수   **************************************************************************/
	
	
	//2017-04 m3mobile 추가 
	@Override
	protected void onResume() {
	    super.onResume();
	    if(m3Mobile){
		    /*Intent intent = new Intent(SCANNER_ACTION_SETTING_CHANGE);
		    intent.putExtra("setting", "output_mode");
		    intent.putExtra("output_mode_value", 2);		
		    mContext.sendOrderedBroadcast(intent, null);*/
	    	M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textBarcode, m_amount);
		    M3MoblieBarcodeScanBroadcast.setOnResume();
	    }
	}
	
	//2017-04 m3mobile 추가
	@Override
	protected void onDestroy() {		
		super.onDestroy();		
		if(m3Mobile){
			/*Intent intent = new Intent(SCANNER_ACTION_SETTING_CHANGE);
			intent.putExtra("setting", "output_mode");
			intent.putExtra("output_mode_value", 1);	
			
			mContext.sendOrderedBroadcast(intent, null);
					
			intent.putExtra("setting", "read_mode");		
			intent.putExtra("read_mode_value", 0);	
			mContext.sendOrderedBroadcast(intent, null);*/
			M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textBarcode, m_amount);
			M3MoblieBarcodeScanBroadcast.setOnDestory();			
			
		}
	}		
	
	public void onClickSetDate1(View v) {
		DatePickerDialog newDlg = new DatePickerDialog(this, this, m_dateCalender1.get(Calendar.YEAR),
				m_dateCalender1.get(Calendar.MONTH), m_dateCalender1.get(Calendar.DAY_OF_MONTH));
		newDlg.show();					
	};

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		m_dateCalender1.set(year, monthOfYear, dayOfMonth);		
		m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
		
		//입고예정일 변경
		Calendar temp = Calendar.getInstance();
		temp.set(year, monthOfYear, dayOfMonth);
		temp.add(temp.DAY_OF_MONTH, 2);
		m_period1 = m_dateFormatter.format(temp.getTime());
		m_inDate.setText("입고예정일 : "+m_period1);
		Log.d("입고예정일", m_period1);
		
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
				m_textBarcode.setText(barcode);
				m_amount.requestFocus();

				// doQueryWithBarcode(); 검색을 두번씩 해서 변경함

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
			}
			break;
		// 목록 검색을 통한 바코드 검색
		case BARCODE_MANAGER_REQUEST:
			if (resultCode == RESULT_OK && data != null) {
				HashMap<String, String> hashMap = (HashMap<String, String>) data.getSerializableExtra("fillmaps");
				m_textBarcode.setText(hashMap.get("BarCode"));
				doQueryWithBarcode();
			}
			break;
		case CUSTOMER_MANAGER_REQUEST:
			if (resultCode == RESULT_OK && data != null) {
				HashMap<String, String> hashMap = (HashMap<String, String>) data.getSerializableExtra("fillmaps");
				m_customerCode.setText(hashMap.get("Office_Code"));
				m_customerName.setText(hashMap.get("Office_Name"));
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		
		m_selectedListIndex = position;
		
		new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT).setTitle("발주전표").setMessage("작업을 선택해 주세요!")
		.setPositiveButton("전송", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {				
				//매입전표 전송
				mAllSend = false;
				toSendPurList(m_selectedListIndex);
			}
		}).setNegativeButton("삭제", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub	
				
				new AlertDialog.Builder(ManageBaljuRegActivity.this,AlertDialog.THEME_HOLO_LIGHT).setTitle("전표삭제").setMessage("매입전표를 삭제 하시겠습니까?")
				.setPositiveButton("예", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {					
						//매입전표 삭제
						transferDelete();
					}
				})
				.setNegativeButton("아니요", null).show();
				
			}
		}).setNeutralButton("상세보기", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
								
				HashMap<String, String> map = mfillMaps.get(m_selectedListIndex);
				
				String in_num = "";
				String or_date =  map.get("Or_Date");
				String office_code = map.get("Office_Code");
				String office_name = map.get("Office_Name");
				String in_pri = map.get("Price_Sum").replace(",", "");
				String in_repri = "";
				String profit_pri = "";
				String profit_rate = "";
				
				Log.i("전표번호", in_num);
				
				Intent intent = new Intent(getApplicationContext(), ManageBaljuDetailActivity.class);
				intent.putExtra("In_Num", in_num);
				intent.putExtra("Office_Code", office_code);
				intent.putExtra("Or_Date", or_date);
				intent.putExtra("Office_Name", office_name);
				intent.putExtra("In_Pri", in_pri);
				intent.putExtra("In_RePri", in_repri);
				intent.putExtra("Profit_Pri", profit_pri);
				intent.putExtra("Profit_Rate", profit_rate);
				intent.putExtra("Gubun", "OrDPDA");
				startActivity(intent);
								
			}
		}).show();
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
			actionbar.setTitle("발주 등록");

			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.purchase_detail, menu);
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

	/**************	시스템 함수   **************************************************************************/	
	
	//2017-04 m3mobile 추가
	/*public BroadcastReceiver BarcodeIntentBroadcast = new BroadcastReceiver(){

		private String barcode;
		private String type;
		private String module;
		private byte[] rawdata;
		private int length;
		
		private static final String SCANNER_EXTRA_BARCODE_DATA = "m3scannerdata";
		private static final String SCANNER_EXTRA_BARCODE_CODE_TYPE = "m3scanner_code_type";
		private static final String SCANNER_EXTRA_MODULE_TYPE = "m3scanner_module_type";
		private static final String SCANNER_EXTRA_BARCODE_RAW_DATA = "m3scannerdata_raw";	// add 2017-01-17	after ScanEmul 1.3.0
		private static final String SCANNER_EXTRA_BARCODE_DATA_LENGTH = "m3scannerdata_length";	// add 2017-01-31	after ScanEmul 1.3.0
		
		
		@Override
		public void onReceive(Context content, Intent intent) {
			
			if (intent.getAction().equals("com.android.server.scannerservice.broadcast")) {
				
				barcode = intent.getExtras().getString(SCANNER_EXTRA_BARCODE_DATA);
				type = intent.getExtras().getString(SCANNER_EXTRA_BARCODE_CODE_TYPE);
				module = intent.getExtras().getString(SCANNER_EXTRA_MODULE_TYPE); 
				rawdata = intent.getExtras().getByteArray(SCANNER_EXTRA_BARCODE_RAW_DATA);
				length = intent.getExtras().getInt(SCANNER_EXTRA_BARCODE_DATA_LENGTH);
				
				if(barcode != null){
					
					if(rawdata.length > 0){

						String strRawData = "";
						for(int i = 0; i< length; i++){
							strRawData += String.format("0x%02X ", (int)rawdata[i]&0xFF);		
						}
						
						//m_textBarcode.setText("data: " + barcode + " \ntype: " + type + " \nraw: " + strRawData);	
						m_textBarcode.setText(barcode+strRawData);
												
					}else{
						//mTvResult.setText("data: " + barcode + " type: " + type);	
						m_textBarcode.requestFocus();
						m_textBarcode.setText(barcode.trim());
						m_amount.requestFocus();
						
					//}
				}
				else //QR CODE
				{
					int nSymbol = intent.getExtras().getInt("symbology", -1);
					int nValue = intent.getExtras().getInt("value", -1);

					Log.i(TAG,"getSymbology ["+ nSymbol + "][" + nValue + "]");	
					
					if(nSymbol != -1)
					{
						edSymNum.setText(Integer.toString(nSymbol));						
						edValNum.setText(Integer.toString(nValue));						
					}
				}	
			}
		}
		
	};*/
	
}
