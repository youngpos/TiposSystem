package tipsystem.tips;

import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import kr.co.tipos.tips.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.m3.sdk.scannerlib.Barcode;
import com.m3.sdk.scannerlib.BarcodeListener;
import com.m3.sdk.scannerlib.BarcodeManager;
import com.m3.sdk.scannerlib.Barcode.Symbology;

import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.M3MoblieBarcodeScanBroadcast;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.MSSQL4;
import tipsystem.utils.StringFormat;
import tipsystem.utils.UserPublicUtils;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView.OnEditorActionListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.NavUtils;

// 매입등록
public class PurchaseRegistActivity extends Activity implements OnItemClickListener, OnDateSetListener {

	private static final int ZBAR_SCANNER_REQUEST = 0;
	private static final int BARCODE_MANAGER_REQUEST = 2;
	private static final int CUSTOMER_MANAGER_REQUEST = 3;
	private static final int PURCHASE_REGIST_REQUEST = 4;
	private static final int PRODUCT_REGIST_REQUEST = 5;
	
	//2017-04 m3mobile
	private static final String SCANNER_ACTION_SETTING_CHANGE = "com.android.server.scannerservice.settingchange";
	public static final String SCANNER_ACTION_PARAMETER = "android.intent.action.SCANNER_PARAMETER";
	public static final String SCANNER_ACTION_ENABLE = "com.android.server.scannerservice.m3onoff";
	public static final String SCANNER_EXTRA_ENABLE = "scanneronoff";

	public static final String SCANNER_ACTION_START = "android.intent.action.M3SCANNER_BUTTON_DOWN";
	public static final String SCANNER_ACTION_CANCEL = "android.intent.action.M3SCANNER_BUTTON_UP";
	
	public static final String SCANNER_ACTION_BARCODE = "com.android.server.scannerservice.broadcast";
	
	UserPublicUtils upu;
	
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

	// 전표번호 생성
	String posID = "P";
	String userID = "1";

	String m_junpyo = "";
	int m_junpyoInTIdx = 1;
	int m_tranSEQ = 1;
	// 수정모드시 포지션 저장하기
	//int modify_position = 0; // 신규 등록시 0, 목록에서 불러 진것 수정하면 1,  2017-04

	DatePicker m_datePicker;
	SimpleDateFormat m_dateFormatter;
	Calendar m_dateCalender1;

	ListView m_listReadyToSend;
	SimpleAdapter m_adapter;
		
	//매입목록 선택 포지션
	int m_selectedListIndex = -1;

	CheckBox m_purDataFix;
	
	Button m_period;
	// CheckBox m_immediatePayment; 2017-04 저장프로시저변경
	EditText m_customerCode;
	EditText m_customerName;
	
	//이전 거래처 데이터
	String[] mOfficeData = {"", ""}; 
	
	EditText m_textBarcode;
	EditText m_textProductName;
	String m_textGname = ""; // 상품명 변경 체크 위해서
	EditText m_et_purchasePrice;
	EditText m_et_salePrice;
	EditText m_et_purchaseCost; // 매입원가
	EditText m_et_addTax;
	CheckBox m_purpriTaxyn; // 과세상품 부가세 포함 별도 옵션
	EditText m_amount;
	EditText m_profitRatio;
	TextView m_totalPurpri; // 전체 매입가 
	TextView m_goodsGubun; // 행사상품 표시
	Button m_bt_barcodeSearch;
	Button saveButton; // 저장/수정버튼

	CheckBox m_checkBoxOfficeFix; //거래처코드 고정
	
	CheckBox m_checkBoxRejectedProduct;
	//CheckBox m_checkBoxProductName; // 상품명 변경
	CheckBox m_checkBoxPurpri; // 매입가고정
	CheckBox m_checkBoxSellpri; // 판매가고정
	CheckBox m_checkBoxCustomer; // 거래처고정

	// 옵션 정보
	String m_changeOption;
	String m_newProductOption;
	
	// CheckBox m_checkAllDelete;// 전체삭제 체크버튼 2017-04 전체삭제버튼
	// 매입 합계 금액을 계산해 주세요~
	Double totalPurPri = 0.0;

	// 음성 출력을 위한 멤버변수선
	TextToSpeech tts;

	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>(); // 매입목록 리스트
	//List<JSONObject> mfillMaps = new ArrayList<JSONObject>(); // 매입목록 리스트
	//List<HashMap<String, String>> m_purList = new ArrayList<HashMap<String, String>>();
	HashMap<String, String> m_tempProduct = new HashMap<String, String>(); // 현재 불러온 상품정보
																			
	Context mContext;
	//2017-04 m3mobile
	SharedPreferences pref;
	boolean m3Mobile;
	
	boolean mAllSend;
	
	private ProgressDialog dialog;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_purchase_regist);
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
		m_newProductOption = pref.getString("prefProductMethod", "");
		m3Mobile = pref.getBoolean("m3mobile", false);
		
		// 삭제버튼 2017-04
		// Button btn_Send = (Button) findViewById(R.id.buttonSend);
		// 전체전송 2017-04
		// Button btn_SendAll = (Button) findViewById(R.id.buttonSendAll);
		
		m_listReadyToSend = (ListView) findViewById(R.id.listviewReadyToSendList);

		//목록 리스트
		String[] from = new String[] { "In_Date", "Office_Code", "Office_Name", "nCount", "Price_Sum" };
		int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5 };
		m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_item4_15, from, to);
		m_listReadyToSend.setAdapter(m_adapter);
		
		m_purDataFix=(CheckBox)findViewById(R.id.checkboxPurDataFix);
		m_period = (Button) findViewById(R.id.buttonSetDate1);
		// m_immediatePayment = (CheckBox)
		// findViewById(R.id.checkBoxImmediatePayment); 2017-04 저장프로시저 변경
				
		m_customerCode = (EditText) findViewById(R.id.editTextCustomerCode);
		m_customerName = (EditText) findViewById(R.id.editTextCustomerName);
		m_textBarcode = (EditText) findViewById(R.id.editTextBarcode);
			
		m_textProductName = (EditText) findViewById(R.id.editTextProductName);
		
		//2017-04 상품명고정 기능 삭제
		/*TextWatcher textwatcher = new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				if (s.length() > 0 && !m_textGname.equals("")) {
					if (!m_textGname.equals(m_textProductName.getText().toString()))
						m_checkBoxProductName.setChecked(true);
					else
						m_checkBoxProductName.setChecked(false);
				}
			}
		};
		m_textProductName.addTextChangedListener(textwatcher);*/

		m_et_purchasePrice = (EditText) findViewById(R.id.editTextPurchasePrice);
		m_et_purchaseCost = (EditText) findViewById(R.id.editTextPurchaseCost);
		m_et_addTax = (EditText) findViewById(R.id.editTextAddTax);
		m_purpriTaxyn = (CheckBox) findViewById(R.id.checkBoxTaxProduct); // 부가세
																			// 별도
																			// 포함
																			// 옵션
		
		m_et_salePrice = (EditText) findViewById(R.id.editTextSalePrice);
		m_amount = (EditText) findViewById(R.id.editTextAmount);
		m_profitRatio = (EditText) findViewById(R.id.editTextProfitRatio);
		m_bt_barcodeSearch = (Button) findViewById(R.id.buttonBarcode);
		m_goodsGubun = (TextView) findViewById(R.id.textViewGoodsGubun);

		// 반품 체크
		m_checkBoxRejectedProduct = (CheckBox) findViewById(R.id.checkBoxRejectedProduct);
		// 상품명 변경
		//m_checkBoxProductName = (CheckBox) findViewById(R.id.checkboxProductName);
		// 매입가 고정
		m_checkBoxPurpri = (CheckBox) findViewById(R.id.checkboxPurPri);
		// 판매가 고정
		m_checkBoxSellpri = (CheckBox) findViewById(R.id.checkboxSellPri);
		// 거래처 고정
		m_checkBoxCustomer = (CheckBox) findViewById(R.id.checkboxCustomer);

		//매입거래처 고정
		m_checkBoxOfficeFix = (CheckBox)findViewById(R.id.checkboxOfiiceFix);
		m_checkBoxOfficeFix.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				// TODO Auto-generated method stub				
				if(isChecked){
					m_customerCode.setEnabled(false);
					m_customerName.setEnabled(false);
				}else{
					m_customerCode.setEnabled(true);
					m_customerName.setEnabled(true);
				}
			}
		});
		
		
		// 전체삭제 체크버튼 2017-04 저장프로시저 변경 전체삭제
		// m_checkAllDelete = (CheckBox) findViewById(R.id.checkAllDelete);

		/*
		 * //전체 고정 체크시 올 고정 되어지고 변경 못하게 막아드림
		 * m_checkBoxProductName.setOnCheckedChangeListener(new
		 * OnCheckedChangeListener() {
		 * 
		 * @Override public void onCheckedChanged(CompoundButton buttonView,
		 * boolean isChecked) { //전체고정 체크 했다면 if(isChecked){ //체크고정하고
		 * m_checkBoxPurpri.setChecked(true); //수정 못하게 막습니다.
		 * m_checkBoxPurpri.setEnabled(false);
		 * m_checkBoxSellpri.setChecked(true);
		 * m_checkBoxSellpri.setEnabled(false);
		 * m_checkBoxCustomer.setChecked(true);
		 * m_checkBoxCustomer.setEnabled(false); }else{
		 * m_checkBoxPurpri.setChecked(false);
		 * m_checkBoxPurpri.setEnabled(true);
		 * m_checkBoxSellpri.setChecked(false);
		 * m_checkBoxSellpri.setEnabled(true);
		 * m_checkBoxCustomer.setChecked(false);
		 * m_checkBoxCustomer.setEnabled(true); } } });
		 */

		m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
		m_dateCalender1 = Calendar.getInstance();
		m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

		// 전체매입가 선언
		m_totalPurpri = (TextView) findViewById(R.id.textviewTPurPri);

		// 저장누르기 ( 목록으로 넘어감 )
		saveButton = (Button) findViewById(R.id.buttonSave);
		saveButton.setTag("저장");
		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				
				//오류체크 후 저장
				checkData();
				
				/*if (!checkData())
					return;
				*/
				/*String tag = v.getTag().toString();
				if (tag.toString().equals("저장")) {

					doSend();

					
					 * doSendListView(); blockInputMode(); clearInputBox();
					 * checkboxReset();
					 
				} *//*
					 * else { //목록에서 클릭시 저장 했을때 입력 하기 doReSendListView();
					 * blockInputMode(); clearInputBox();
					 * saveButton.setBackgroundResource(R.drawable.
					 * purchase_save_btn); saveButton.setTag("저장");
					 * checkboxReset(); }
					 */
			}
		});

		// 새로입력 누르기
		Button btn_Renew = (Button) findViewById(R.id.buttonRenew);
		btn_Renew.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				//releaseInputMode();
				clearInputBox();				
				//checkboxReset();
			}
		});

		
		//조회버튼
		Button btn_Search = (Button)findViewById(R.id.buttonSearch);
		btn_Search.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//전체 초기화
				clearInputBox();
				
				//키보드 내리기
				hideSoftKeyboard(false);
								
				//목록 불러오기(포스번호로)
				getTempInDPDAList();
				
				//매입합계금액표시	
			}
		});
				
		//스캔버튼 2017-04 m3mobile
		Button btn_Scanner = (Button)findViewById(R.id.buttonScan);
		btn_Scanner.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//TODO Auto-generated method stub
				
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
		
		
		// 2017-04 저장프로시저 변경 삭제버튼
		// 삭제버튼
		/*
		 * btn_Send.setOnClickListener(new OnClickListener() {
		 * 
		 * public void onClick(View v) { if (m_checkAllDelete.isChecked()) { new
		 * AlertDialog.Builder(PurchaseRegistActivity.this).setTitle("종료")
		 * .setMessage("목록의 전체 매입자료가 삭제 됩니다. 삭제 하시겠습니까?")
		 * .setPositiveButton("예", new DialogInterface.OnClickListener() {
		 * public void onClick(DialogInterface dialog, int whichButton) {
		 * clearInputBox(); deleteListAll(); setRenew(); }
		 * }).setNegativeButton("아니요", new DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * return; } }).show(); } else { new
		 * AlertDialog.Builder(PurchaseRegistActivity.this).setTitle("종료")
		 * .setMessage("선택된 매입자료를 삭제 하시겠습니까?") .setPositiveButton("예", new
		 * DialogInterface.OnClickListener() { public void
		 * onClick(DialogInterface dialog, int whichButton) { deleteData();
		 * clearInputBox(); // 목록이 마지막 상품이라서 초기화를 해야 할때 if (mfillMaps.isEmpty())
		 * { setRenew(); }
		 * 
		 * } }).setNegativeButton("아니요", new DialogInterface.OnClickListener() {
		 * 
		 * @Override public void onClick(DialogInterface dialog, int which) {
		 * return; } }).show(); } } });
		 */

		// 2017-04 저장프로시저 변경
		// 전체전송
		/*
		 * btn_SendAll.setOnClickListener(new OnClickListener() { public void
		 * onClick(View v) { if (mfillMaps.isEmpty()) {
		 * Toast.makeText(getApplicationContext(), "매입 상품이 없습니다.",
		 * Toast.LENGTH_SHORT).show(); return; } getInTSeq();
		 * m_selectedListIndex = -1; } });
		 */

		// 바코드 입력 후 포커스 딴 곳을 옮길 경우
		m_textBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					upu.setChangeBackGroundColor(0, m_textBarcode);
					String barcode = m_textBarcode.getText().toString();
					//if (!barcode.equals("") && modify_position == 0) // 입력한
																		// Barcode가
																		// 값이 있을
																		// 경우만
					if (!barcode.equals("")) 
						doQueryWithBarcode();
				}else{
					upu.setChangeBackGroundColor(1, m_textBarcode);
				}
			}
		});

		// 매입가 + 이익률로 판매가
		m_et_purchasePrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					upu.setChangeBackGroundColor(0, m_et_purchasePrice);
										
					try{						
						Float.parseFloat(m_et_purchasePrice.getText().toString());						
					}catch(NumberFormatException e){
						return;
					}
					
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
										
					try{						
						Float.parseFloat(m_et_purchaseCost.getText().toString());						
					}catch(NumberFormatException e){
						return;
					}
					
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
										
					try{						
						Float.parseFloat(m_et_salePrice.getText().toString());						
					}catch(NumberFormatException e){
						return;
					}
					
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
					
					try{						
						Float.parseFloat(m_profitRatio.getText().toString());						
					}catch(NumberFormatException e){
						return;
					}					
					
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

		// 수량칸 이동시 키보드 띄우기
		m_amount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					
					upu.setChangeBackGroundColor(1, m_amount);
					
					InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
					// 키보드를 띄운다.
					imm.showSoftInput(getCurrentFocus(), 0);
					// 키보드를 없앤다.
					// imm.hideSoftInputFromWindow(smsid.getWindowToken(),0);

					// 키보드 강제로 띄우기
					// getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
				}else{
					upu.setChangeBackGroundColor(0, m_amount);
				}
			}
		});
		
		//상품명
		m_textProductName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {					
					upu.setChangeBackGroundColor(1, m_textProductName);					
				}else{
					upu.setChangeBackGroundColor(0, m_textProductName);
				}
			}
		});
		
		
		//입력수량 완료버튼 입력 시 자동저장 기능 리스너
		m_amount.setOnEditorActionListener(new OnEditorActionListener() {
			
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				// TODO Auto-generated method stub
				//완료 버튼을 눌렀습니다.
				if(actionId == EditorInfo.IME_ACTION_DONE){				
					//단품 저장
					checkData();
					/*if (!checkData()) return false;
					
					String tag = (String)saveButton.getTag();
					if (tag.toString().equals("저장")) {
						doSend();
					}*/
				}
				
				return false;
			}
		});
		
		m_amount.setImeOptions(EditorInfo.IME_ACTION_DONE);
		
		m_listReadyToSend.setOnItemClickListener(this);

		Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
		TextView textView = (TextView) findViewById(R.id.textView2);
		textView.setTypeface(typeface);
	
		//releaseInputMode();
		
		// intent filter 2017-04 m3mobile
		if(m3Mobile){
			/*IntentFilter filter = new IntentFilter();
			filter.addAction(SCANNER_ACTION_BARCODE);
			registerReceiver(BarcodeIntentBroadcast,filter);*/
			
			M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textBarcode, m_amount);
			
		}				
	}

	// 판매가 원단위 절사
	private String sellpriceRound(String price) {
		String sell_price = price.toString();
		sell_price = String.valueOf(Math.round(Double.parseDouble(sell_price) / 10) * 10);
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

		Log.i("매입가 변경 계산", "이익률 계산 완료.");
	}

	// 매입가로 매입원가 이익률 계산하기
	private void calculatePurpri() {

		String tax_yn = m_tempProduct.get("Tax_YN");
		String pur_pri = m_et_purchasePrice.getText().toString();

		// 매입가 변경시 부가세 및 원가 변경하기
		if (!pur_pri.toString().equals("") && !pur_pri.toString().equals("0") && !pur_pri.toString().equals("0.00")) {
			float f_purchasePrice = Float.parseFloat(pur_pri);
			// m_tempProduct.put("Pur_Pri", String.format("%.2f",
			// f_purchasePrice)); // 매입가

			if (tax_yn.equals("1")) { // 과세
				m_et_purchaseCost.setText(String.format("%.2f", f_purchasePrice / 1.1)); // 매입원가
				m_et_addTax.setText(String.format("%.2f", f_purchasePrice - (f_purchasePrice / 1.1)));// 부가세
			}
			if (tax_yn.equals("0")) { // 면세
				m_et_purchaseCost.setText(String.format("%.2f", f_purchasePrice)); // 매입원가
				m_et_addTax.setText(String.format("%.2f", 0f));// 부가세
			}
		} else {
			m_et_purchaseCost.setText("0"); // 매입원가
			m_et_addTax.setText("0");// 부가세
		}
		
		Log.i("매입가 변경 계산", "원가/부가세 계산 완료.");
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

	// 전표 번호 생성 합니다.
	public String makeJunPyo() {

		String period = m_period.getText().toString();
		int year = Integer.parseInt(period.substring(0, 4));
		int month = Integer.parseInt(period.substring(5, 7));
		int day = Integer.parseInt(period.substring(8, 10));

		String junpyo = String.format("%04d%02d%02d", year, month, day) + posID + String.format("%03d", m_junpyoInTIdx);
		return junpyo;
	}

	/**
	 * 기본 입력 정보를 체크 합니다.
	 * 유효성 체크
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
		if (code.equals("")){ //거래처코드
			m_customerCode.requestFocus();
			Toast.makeText(getApplicationContext(), "거래처를 선택해주세요.", Toast.LENGTH_SHORT).show();
			return;
		}				
		if(name.equals("")){ //거래처명
			m_customerName.requestFocus();
			Toast.makeText(getApplicationContext(), "거래처를 입력해주세요.", Toast.LENGTH_SHORT).show();
			return;
		}		
		if(purchaseDate.equals("")){ //매입일
			m_period.requestFocus();
			Toast.makeText(getApplicationContext(), "매입일을 입력해주세요.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		
				
		
		if(barcode.equals("")){	//바코드
			m_textBarcode.requestFocus();
			Toast.makeText(getApplicationContext(), "상품을 검색해주세요.", Toast.LENGTH_SHORT).show();
			return;
		}
		if(productName.equals("")){	//상품명
			m_textProductName.requestFocus();
			Toast.makeText(getApplicationContext(), "상품명을 입력해주세요.", Toast.LENGTH_SHORT).show();
			return;
		}
			
		if(purchasePrice.equals("")){	//매입가
			m_et_purchasePrice.requestFocus();
			Toast.makeText(getApplicationContext(), "매입가를 입력해주세요.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(purchaseCost.equals("")){	//매입원가
			m_et_purchaseCost.requestFocus();
			Toast.makeText(getApplicationContext(), "매입원가를 입력해주세요.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(salePrice.equals("")){		//판매가
			m_et_salePrice.requestFocus();
			Toast.makeText(getApplicationContext(), "판매가를 입력해주세요.", Toast.LENGTH_SHORT).show();
			return;
		}
		if(amount.equals("")){	//매입수량
			m_amount.requestFocus();
			Toast.makeText(getApplicationContext(), "매입수량을 입력해주세요.", Toast.LENGTH_SHORT).show();
			return;			
		}
		if(profitRatio.equals("")){	//이익률
			m_profitRatio.requestFocus();
			Toast.makeText(getApplicationContext(), "이익률을 입력해주세요.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		
		// 0원 입력값 확인
		try{
		
			if (Float.parseFloat(purchasePrice) < 0){	//매입가
				Toast.makeText(getApplicationContext(), "매입가를 입력해 주세요.", Toast.LENGTH_SHORT).show();
				m_et_purchasePrice.requestFocus();
				return;
			}
					
			if(Float.parseFloat(salePrice) < 0 ){		//판매가
				Toast.makeText(getApplicationContext(), "판매가를 입력해 주세요.", Toast.LENGTH_SHORT).show();
				m_et_salePrice.requestFocus();
				return;
			}
			
			if(Float.parseFloat(amount) <= 0){	
				m_amount.requestFocus();
				Toast.makeText(getApplicationContext(), "매입 수량을 입력해 주셔야 합니다.", Toast.LENGTH_SHORT).show();
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
			if (ratio < 0 || ratio > 100) {
				m_profitRatio.requestFocus();
				Toast.makeText(getApplicationContext(), "이익률을 확인해 주세요", Toast.LENGTH_SHORT).show();
				return;
			}
		} catch (NumberFormatException e) {
			m_profitRatio.requestFocus();
			Toast.makeText(getApplicationContext(), "이익률을 확인해 주세요", Toast.LENGTH_SHORT).show();
			return;
		}

		// 매입가 체크
		if (Double.parseDouble(purchasePrice) > 10000000.00) {
			Toast.makeText(getApplicationContext(), " 매입가를 확인해 주세요.", Toast.LENGTH_SHORT).show();
			m_et_purchasePrice.requestFocus();
			return;
		}

		// 매입원가 체크
		if (Double.parseDouble(purchaseCost) < 10) {
			Toast.makeText(getApplicationContext(), " 매입원가를 확인해 주세요.", Toast.LENGTH_SHORT).show();
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
				//이익률 변경, 원가/부가세 변경
				calculateProfitRatio();
				calculatePurpri();
			} else if (m_et_salePrice.hasFocus()) {
				//이익률 변경
				calculateProfitRatio();
			} else if (m_et_purchaseCost.hasFocus()) {
				//매입가,이익률,원가/부가세 변경
				purpricostTopurpri();
				calculateProfitRatio();
				calculatePurpri();
			}
		}		
		
		//발주일이 오늘 날자와 같지 않고 매입일 고정에 체크가 풀려있다면
		if(!purchaseDate.equals(upu.getTodayData()) && !m_purDataFix.isChecked() ){
			
			String str = "현재 매입일이 오늘 날자와 일치하지 않습니다.\r\n"					
					+ "[ 예 ] - 지정날자 매입 (완료 후 체크해제 필수)\r\n"					
					+ "[ 아니오 ] - 오늘 날자로 변경";

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
		        	m_purDataFix.setChecked(true);
		        	doSend();
		        	/*String tag = (String)saveButton.getTag();
					if (tag.toString().equals("저장")) {
						doSend();
					} */
		        	//dialog.cancel();
		        }
		    })
		    .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
		        public void onClick(DialogInterface dialog, int id) {		        	
		        	m_period.setText(upu.getTodayData());
		        	doSend();
		        	/*String tag = (String)saveButton.getTag();
					if (tag.toString().equals("저장")) {
						doSend();
					}*/ 
		        }
		    });
		    
		    AlertDialog alert = alt_bld.create();
		    // Title for AlertDialog
		    alert.setTitle("매입일 지정오류");
		    // Icon for AlertDialog
		    //alert.setIcon(R.drawable.icon);
		    alert.show();
		    
		}else{			
			doSend();
		}
		
	}

	// 2017-04 저장프로시저 변경 삭제버튼
	// 목록에서 삭제합니다
	/*
	 * public void deleteData() { // 삭제하기
	 * 
	 * if (m_selectedListIndex == -1) { Toast.makeText(this, "선택된 항목이 없습니다.",
	 * Toast.LENGTH_SHORT).show(); return; }
	 * 
	 * if (m_checkAllDelete.isChecked()) { mfillMaps.remove(mfillMaps);
	 * m_purList.remove(m_purList); m_adapter.notifyDataSetChanged();
	 * m_selectedListIndex = -1; } else { mfillMaps.remove(m_selectedListIndex);
	 * m_purList.remove(m_selectedListIndex); m_adapter.notifyDataSetChanged();
	 * makeFillvapsWithStockList(); m_selectedListIndex = -1; } }
	 */

	// 저장소 초기화
	/*public void deleteListAll() {
		if (mfillMaps.isEmpty())
			return;
		totalPurPri = 0.0;
		mfillMaps.removeAll(mfillMaps);
		// m_adapter.notifyDataSetChanged(); 2017-04
		m_purList.removeAll(m_purList);
		m_selectedListIndex = -1;
	}*/

	
	/**
	 * 입력란 초기화(저장버튼/새로입력
	 * - 목록선택 초기화
	 * - 바코드 초기화
	 * - 상품명 초기화
	 * - 바코드 검색버튼 초기화
	 * - 매입가 초기화
	 * - 판매가 초기화
	 * - 매입원가 초기화
	 * - 부가세 초기화
	 * - 매입수량 초기화
	 * - 이익률 초기화
	 * - 행사구분 초기화 (색상,명칭)
	 * - 원상품명 초기화(m_textGname)
	 * - 바코드칸으로 이동
	 * - 키보드 띄우기
	 * - 거래처코드 고정
	 */
	public void clearInputBox() {

		m_selectedListIndex = -1;
		m_textBarcode.setText(""); // 바코드
		m_textProductName.setText(""); // 상품명
		m_bt_barcodeSearch.setText(""); // 바코드검색
		m_et_purchasePrice.setText(""); // 매입가
		m_et_salePrice.setText(""); // 판매가
		m_et_purchaseCost.setText(""); // 매입원가
		m_et_addTax.setText(""); // 부가세
		m_amount.setText(""); // 매입수량
		m_profitRatio.setText(""); // 이익률
		 
		m_goodsGubun.setText("일반"); // 행사구분
		m_goodsGubun.setTextColor(Color.WHITE); // 행사구분 색
				
		hideSoftKeyboard(true);
		
		//거래처고정
		if(!m_checkBoxOfficeFix.isChecked()) {
			m_customerCode.setEnabled(true);
			m_customerCode.setText("");
			m_customerName.setEnabled(true);
			m_customerName.setText("");
		}
		
		//현상품정보 초기화
		// 원상품명 초기화
		m_textGname = ""; // 원상품명
		m_tempProduct.clear();
		
		m_textBarcode.setEnabled(true);
		m_textBarcode.requestFocus(); // 바코드로 포커스이동
		
		m_totalPurpri.setText(""); //총매입가 삭제
		
		// 포지션 변경 옵션
		//modify_position = 0;
		//saveButton.setBackgroundResource(R.drawable.purchase_save_btn);
		//saveButton.setTag("저장");
	}

	/**
	 * 상품 스캔 후 
	 * - 바코드 수정불가 
	 */
	public void porductScanAfter(){
		m_textBarcode.setEnabled(false);		
	}
	
	/**
	 * 거래처 검색 후
	 * - 거래처코드 입력 불가
	 * - 거래처명 입력 불가
	 */
	public void officeScanAfter(){
		m_customerCode.setEnabled(false);
		m_customerName.setEnabled(false);
	}
	
	
	/**
	 * 체크박스및 다시 표시하기(저장버튼)
	 * - 바코드입력가능
	 * - 상품명입력가능
	 * - 매입일 수정불가
	 * - 바코드검색버튼 활성화
	 * - 거래처코드 입력가능
	 * - 거래처명 입력가능
	 * - 매입가 입력가능
	 * - 판매가 입력가능
	 * - 매입수량 입력가능
	 * - 이익률 입력가능
	 * - 부과세 입력가능
	 */
	/*public void blockInputMode() {
		m_textBarcode.setEnabled(true); // 바코드
		m_textProductName.setEnabled(true); // 상품명
		m_period.setEnabled(false); // 매입일
		// m_checkBoxRejectedProduct.setEnabled(false);
		m_bt_barcodeSearch.setEnabled(true); // 바코드검색
		m_customerCode.setEnabled(false); // 거래처코드
		m_customerName.setEnabled(false); // 거래처이름
		m_et_purchasePrice.setEnabled(true); // 매입가
		m_et_purchaseCost.setEnabled(false); // 매입원가
		m_et_salePrice.setEnabled(true); // 판매가
		m_amount.setEnabled(true); // 매입수량
		m_profitRatio.setEnabled(true); // 이익률
		m_purpriTaxyn.setEnabled(true); // 부과세
	}*/

	/**
	 * 체크박스 초기화 ( 매입가, 판매가, 거래처, 상품명 )
	 */
	public void checkboxReset() {
		m_checkBoxCustomer.setChecked(false);
		//m_checkBoxProductName.setChecked(false);
		m_checkBoxPurpri.setChecked(false);
		m_checkBoxSellpri.setChecked(false);
	}

	/**
	 * 새로입력
	 * - 바코드입력 가능
	 * - 상품명 입력가능
	 * - 하단목록있을경우 setRenew실행
	 * - 바코드검색버튼 활성화
	 * - 매입가입력가능
	 * - 판매가 입력가능
	 * - 매입수량 입력가능
	 * - 이익률 입력가능
	 * - 면과세 표시 변경
	 */
	/*public void releaseInputMode() {
		m_textBarcode.setEnabled(true);
		m_textProductName.setEnabled(true);
		if (mfillMaps.isEmpty())
			setRenew();
		m_bt_barcodeSearch.setEnabled(true);
		m_et_purchasePrice.setEnabled(true);
		m_et_salePrice.setEnabled(true);
		m_amount.setEnabled(true);
		m_profitRatio.setEnabled(true);
		setTaxSeparate("1", "0");
	}*/

	
	/**
	 * 새로입력 - 하단목록이 있을경우 
	 * setRenew
	 * 	- 날자 수정가능
	 * - 부가세포함별도 체크해제
	 * - 반품 수정가능
	 * - 반품 체크해제
	 * - 거래처코드/거래처명 수정가능
	 * - 거래처명/거래처코드 지움
	 * - 총 매입가 지움
	 * - 행사상품 구분 > 일반으로 변경
	 * - 행사상품 구분명 - 흰색으로 변경
	 */
	/*public void setRenew() {
		m_period.setEnabled(true);
		if (m_purpriTaxyn.isChecked())
			m_purpriTaxyn.setChecked(false);
		m_checkBoxRejectedProduct.setEnabled(true);
		m_checkBoxRejectedProduct.setChecked(false);
		// m_checkAllDelete.setChecked(false); 2017-04 전체삭제 체크버튼
		// 거래처 고정
		m_customerCode.setEnabled(true);
		m_customerName.setEnabled(true);
		m_customerCode.setText("");
		m_customerName.setText("");
		m_totalPurpri.setText("");
		// 행사 상품 표시
		m_goodsGubun.setText("일반");
		m_goodsGubun.setTextColor(Color.WHITE);
	}*/

	// 체크버튼
	public void allCheckOption() {
		// m_checkBoxAllcheck.setChecked(true);
		m_checkBoxPurpri.setChecked(true);
		m_checkBoxSellpri.setChecked(true);
		m_checkBoxCustomer.setChecked(true);
	}

	// 저장하기 2017-04 저장프로시저 사용하기
	private void doSend() {

		String query = "exec sp_executesql N'EXEC str_PDA_004Q "; // '2017-03-30',
																	// '',
																	// '00002',
																	// 'H',
																	// 'MI'";
		// exec sp_executesql N'EXEC str_PDA_004Q ''2017-03-30'', '''',
		// ''00002'', ''H'', ''MI'''
		// 매입등록창의 매입일 불러오기
		String purchaseDate = m_period.getText().toString();
		String code = m_customerCode.getText().toString();

		query += "''" + purchaseDate + "'', '''', "; // 매입일
		query += "''" + code + "'', "; // 거래처코드
		query += "''" + posID + "'', ''MI'''"; // 포스아이디

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
							doSendListView(num);
						} else {							
							doSendListView(num);
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

	// 저장 버튼 2017-04 저장프로시저 사용하기
	public void doSendListView(String num) {
		//mfillMaps.removeAll(mfillMaps);

		String query = "exec sp_executesql N'EXEC str_PDA_004I ";

		// 입력화면에서 정보 가져오기
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		// 입력창과 상관없이 현재 시간으로 등록일 수정일 불러오기
		String currentDate = sdf.format(new Date());
		// 매입등록창의 매입일 불러오기
		String purchaseDate = m_period.getText().toString();
		String barcode = m_textBarcode.getText().toString();
		Log.d("바코드", barcode.toString());
		String productName = m_textProductName.getText().toString();
		String code = m_customerCode.getText().toString();
		String name = m_customerName.getText().toString();
		String purchasePrice = m_et_purchasePrice.getText().toString();
		String Pur_Cost = m_et_purchaseCost.getText().toString();
		String Add_Tax = m_et_addTax.getText().toString();
		// Vat_chk 값 저장하기 입니다.
		boolean Vat_Chk = m_purpriTaxyn.isChecked();
		String salePrice = m_et_salePrice.getText().toString();
		String amount = m_amount.getText().toString();
		String profitRatio = m_profitRatio.getText().toString();

		// 선택된 상품장의 데이터를 꺼내옴
		String Org_PurPri = m_tempProduct.get("Pur_Pri"); // 원매입가
		String Org_SellPri = m_tempProduct.get("Sell_Pri"); // 원매출가
		String Pur_DCRate = "0"; // 매입DC률 매입가에서 적용 %만큼 DC한다
		String Pur_PriDC = "0"; // 매입DC금액 %DC된 금액
		// String Pur_Cost = m_tempProduct.get("Pur_Cost"); // 매입원가
		// String Add_Tax = m_tempProduct.get("Add_Tax"); // 부가세
		String Tax_YN = m_tempProduct.get("Tax_YN"); // 과세여부(0:면세,1:과세)
		String Summarize = "정상가";

		String VAT_CHK = m_tempProduct.get("VAT_CHK"); // 부가세구분(0:별도,1:포함)
		String Tax_Gubun = "1"; // 부가세포함, 별도 구분
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
		String In_YN = m_tempProduct.get("In_YN"); // 매입여부(0:반품,1:매입,2:행사반품,3:행사매입)
		String Box_Use = m_tempProduct.get("Box_Use"); // 박스상품여부(0:미사용,1:사용)
		String Pack_Use = m_tempProduct.get("Pack_Use"); // 묶음상품여부(0:미사용,1:사용)
		String Bot_Code = m_tempProduct.get("Bot_Code"); // 공병코드
		String Bot_Name = m_tempProduct.get("Bot_Name"); // 공병명
		String Bot_Pur = m_tempProduct.get("Bot_Pur"); // 공병매입가
		String Bot_Sell = m_tempProduct.get("Bot_Sell"); // 공병매출가
		String Scale_Use = m_tempProduct.get("Scale_Use"); // 저울상품
		String BarCode1;
		if (m_tempProduct.containsKey("BarCode1")) {
			BarCode1 = m_tempProduct.get("BarCode1"); // 박스 바코드
		} else {
			BarCode1 = "";
		}
		String Obtain = m_tempProduct.get("Obtain"); // 입수수량

		// 저울상품 또는 부문상품일때 가격변경 불가
		String Change_Use = "1";
		if (m_tempProduct.get("Scale_Use").toString().equals("1") || (barcode.length() == 4))
			Change_Use = "0";

		// 전체 체크 시에 모두 변경 해야 합니다.
		// String checkAll;
		String checkPurpri;
		String checkSellpri;
		String checkProductName;
		String checkCustomer;

		// 매입가변경 체크
		if (m_checkBoxPurpri.isChecked()) {
			checkPurpri = "1";
		} else {
			checkPurpri = "0";
		}
		// 판매가변경 체크
		if (m_checkBoxSellpri.isChecked()) {
			checkSellpri = "1";
		} else {
			checkSellpri = "0";
		}

		// 상품명 변경 체크 2017-04
		/*if (m_checkBoxProductName.isChecked()) {
			checkProductName = "1";
		} else {
			checkProductName = "0";
		}*/

		// 거래처변경 체크
		if (m_checkBoxCustomer.isChecked()) {
			checkCustomer = "1";
		} else {
			checkCustomer = "0";
		}

		// 매입여부(0:반품,1:매입,2:행사반품,3:행사매입)
		if (In_YN.toString().equals("3")) { // 행사상품일때
			Change_Use = "0"; // 행사 상품일때 상품 매입가 판매가 거래처 변경 불가능!!
			//행사 상품이라면 매입가 판매가 변경이 안됩니다.
			//그래서 원장에 있는 내용으로 적용합니다.
			purchasePrice = m_tempProduct.get("Pur_Pri");
			Pur_Cost = m_tempProduct.get("Pur_Cost");
			Add_Tax = m_tempProduct.get("Add_Tax");
			salePrice = m_tempProduct.get("Sell_Pri");
			profitRatio = m_tempProduct.get("Profit_Rate");
			
			Summarize += "행사가";			
			if (m_checkBoxRejectedProduct.isChecked()) // 반품
				In_YN = "2";
			else
				In_YN = "3";
		} else {
			if (!purchasePrice.equals(Org_PurPri))
				Summarize += ", 매입가변경";
			if (!salePrice.equals(Org_SellPri))
				Summarize += ", 판매가변경";
			if (m_checkBoxRejectedProduct.isChecked()) // 반품
				In_YN = "0";
			else
				In_YN = "1";
		}

		if (Bot_Pur == null)
			Bot_Pur = "0";
		if (Bot_Sell == null)
			Bot_Sell = "0";

		// 반품 체크되어있을시에 수량 -값 적용 하기
		if (m_checkBoxRejectedProduct.isChecked()) {
			amount = String.valueOf(Integer.valueOf(amount) * -1);
		}

		// double In_Pri = Double.valueOf(purchasePrice) *
		// Double.valueOf(amount); // 총
		// 매입가(공병포함)=매입가x수량
		// double In_SellPri = Double.valueOf(salePrice) *
		// Double.valueOf(amount); // 판매가x수량

		// String period = m_period.getText().toString();

		// 쿼리에 저장하기
		// exec sp_executesql N'EXEC str_PDA_004I
		// ''20170330H001'',''1'',''2017-03-30'',''88006611'',
		// ''00002'',''거래처2'',''1'',''1'',10,1900.00,1900.00,1727.27,172.73,4500,4500,57.78,'''',
		// '''',0.00,0,''0'',''0'',''정상가'',''1'',''0'','''',0.00,''0'',''0'',''0'''
		Log.d("전표번호", num);

		// 전표번호
		String j_num = purchaseDate.replace("-", "") + posID + num;
		query += "''" + j_num + "'', " // 전표번호
				+ "''" + In_YN + "'', " // 매입구분(1:매입,2:반품,3:행사매입,4:행사반품)
				+ "''" + purchaseDate + "'', " // 매입일
				+ "''" + barcode + "'', " // 바코드
				+ "''" + code + "'', " // 거래처코드
				+ "''" + name + "'', " // 거래처명
				+ "''" + Tax_YN + "'', " // 면과세 (1:과세 0:면세)
				+ "''" + VAT_CHK + "'', " // 1:포함, 0:별도 별도시 Summarize 별도가 넣어야함
				+ amount + ", " // 매입수량
				+ purchasePrice + ", " // 매입가
				+ Org_PurPri + ", " // 원매입가
				+ Pur_Cost + ", " // 매입원가
				+ Add_Tax + ", " // 매입과세
				+ salePrice + ", " // 판매가
				+ Org_SellPri + ", " // 원판매가
				+ profitRatio + ", " // 이익률
				+ "''" + Bot_Code + "'', " // 공병코드
				+ "''" + Bot_Name + "'', " // 공병명
				+ Bot_Pur + ", " // 공병매입가
				+ Bot_Sell + ", " // 공병판매가
				+ "''" + Box_Use + "'', " // 박스상품 (0:박스아님 1:박스상품)
				+ "''" + Pack_Use + "'', " // 묶음상품 (0:묶음아님 1:묶음상품)
				+ "''" + Summarize + "'', " // 비고
				+ "''" + userID + "'', " // 작성자
				+ "''" + Scale_Use + "'', " // 저울상품 (1:저울상품, 0:공산품)
				+ "''" + BarCode1 + "'', " // 상위상품
				+ "" + Obtain + ", " // 입수
				+ "''" + checkCustomer + "'', " // 고정거래처 변경
				+ "''" + checkPurpri + "'', " // 매입가 변경
				+ "''" + checkSellpri + "''' "; // 판매가 변경

		sendToQuery(query);

		/*
		 * // 목록에 저장하기 HashMap<String, String> rmap = new HashMap<String,
		 * String>(); rmap.put("purchaseDate", purchaseDate); // 매입일 //
		 * rmap.put("immediatePayment", (immediatePayment)?"1":"0"); // 즉시결재
		 * rmap.put("In_YN", In_YN); // 매입여부 rmap.put("In_Gubun", "3"); // 매입구분
		 * rmap.put("In_Date", period); // 매입일 rmap.put("BarCode", barcode); //
		 * 바코드 rmap.put("G_Name", productName); // 상품명 rmap.put("Office_Code",
		 * code); // 거래처코드 rmap.put("Office_Name", name); // 거래처명
		 * rmap.put("Tax_YN", Tax_YN); // 면과세 rmap.put("VAT_CHK", VAT_CHK); //
		 * 부가세 별도,포함 원레값 rmap.put("Tax_Gubun", Tax_Gubun); // 부가세 별도 포함 구분
		 * rmap.put("In_Count", amount); // 매입수량 rmap.put("Pur_Pri",
		 * purchasePrice); // 매입가 rmap.put("Org_PurPri", Org_PurPri); // 원매입가
		 * rmap.put("Pur_DCRate", Pur_DCRate); // 매입DC율 rmap.put("Pur_PriDC",
		 * Pur_PriDC); // 매입DC금액 rmap.put("Pur_Cost", Pur_Cost); // 매입원가
		 * rmap.put("Add_Tax", Add_Tax); // 매입과세 rmap.put("TPur_Pri",
		 * String.valueOf((Double.valueOf(purchasePrice) -
		 * Double.valueOf(Bot_Pur)) * Double.valueOf(amount))); // 총 //
		 * 매입가(공병제외)=매입가x수량 rmap.put("TPur_Cost",
		 * String.valueOf(Double.valueOf(Pur_Cost) * Double.valueOf(amount)));
		 * // 매입원가x수량 rmap.put("TAdd_Tax",
		 * String.valueOf(Double.valueOf(Add_Tax) * Double.valueOf(amount))); //
		 * 부가세x수량 rmap.put("In_Pri", String.valueOf(In_Pri)); // 총
		 * 매입가(공병포함)=매입가x수량 rmap.put("Sell_Pri", salePrice); // 판매가
		 * rmap.put("Org_SellPri", Org_SellPri); // 원판매가 rmap.put("TSell_Pri",
		 * String.valueOf((Double.valueOf(salePrice) - Double.valueOf(Bot_Sell))
		 * * Double.valueOf(amount))); // 총 // 판매가(공병제외)=판매가x수량
		 * rmap.put("In_SellPri", String.valueOf(In_SellPri)); // 판매가x수량
		 * rmap.put("Profit_Pri", String.valueOf(In_SellPri - In_Pri)); // 이익금
		 * rmap.put("Profit_Rate", profitRatio); // 이익률 rmap.put("Box_Use",
		 * Box_Use); // 박스상품 rmap.put("Pack_Use", Pack_Use); // 묶음상품
		 * rmap.put("Bot_Code", Bot_Code); // 공병코드 rmap.put("Bot_Name",
		 * Bot_Name); // 공병명 rmap.put("Bot_Pri", Bot_Pur); // 공병매입가
		 * rmap.put("Bot_SellPri", Bot_Sell); // 공병판매가 rmap.put("Summarize",
		 * Summarize); // 적요 rmap.put("Write_Date", currentDate); // 등록일
		 * rmap.put("Edit_Date", currentDate); // 수정일 rmap.put("Writer",
		 * userid); // 등록자 rmap.put("Editor", userid); // 수정자
		 * rmap.put("checkProductName", checkProductName); // 상품명 변경 체크
		 * rmap.put("checkPurpri", checkPurpri); // 매입가 변경
		 * rmap.put("checkSellpri", checkSellpri); // 판매가 변경
		 * rmap.put("checkCustomer", checkCustomer); // 거래처변경
		 * rmap.put("Change_Use", Change_Use); // 가격변경상품 구분
		 * rmap.put("Scale_Use", Scale_Use); rmap.put("BarCode1", BarCode1);
		 * rmap.put("Obtain", Obtain);
		 * 
		 * m_purList.add(rmap);
		 * 
		 * // ListView 에 뿌려줌 // mfillMaps = makeFillvapsWithStockList();
		 * makeFillvapsWithStockList(); // 저장한 결과를 넣어 주세요 원레 있던 소스에 덭붙이기 해서 이렇게
		 * 됨 mfillMaps.addAll(m_purList);
		 * 
		 * String[] from = new String[] { "BarCode", "G_Name", "Pur_Pri",
		 * "Sell_Pri", "In_Count", "Profit_Rate", "In_Pri", "In_SellPri" };
		 * int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3,
		 * R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8 };
		 * m_adapter = new SimpleAdapter(this, mfillMaps,
		 * R.layout.activity_listview_item4_15, from, to);
		 * m_listReadyToSend.setAdapter(m_adapter);
		 * 
		 * m_adapter.notifyDataSetChanged();
		 */
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
							//blockInputMode();
							clearInputBox();
							//checkboxReset();
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

	// 2017-04 저장프로시저 사용
	// 수정 버튼
	/*
	 * public void doReSendListView() {
	 * 
	 * mfillMaps.removeAll(mfillMaps); // 입력화면에서 정보 가져오기 SimpleDateFormat sdf =
	 * new SimpleDateFormat("yyyy-MM-dd"); // 입력창과 상관없이 현재 시간으로 등록일 수정일 불러오기
	 * String currentDate = sdf.format(new Date()); // 매입등록창의 매입일 불러오기 String
	 * purchaseDate = m_period.getText().toString(); String barcode =
	 * m_textBarcode.getText().toString(); String productName =
	 * m_textProductName.getText().toString(); String code =
	 * m_customerCode.getText().toString(); String name =
	 * m_customerName.getText().toString(); String purchasePrice =
	 * m_et_purchasePrice.getText().toString(); String Pur_Cost =
	 * m_et_purchaseCost.getText().toString(); String Add_Tax =
	 * m_et_addTax.getText().toString(); // Vat_chk 값 저장하기 입니다. boolean Vat_Chk
	 * = m_purpriTaxyn.isChecked(); String salePrice =
	 * m_et_salePrice.getText().toString(); String amount =
	 * m_amount.getText().toString(); String profitRatio =
	 * m_profitRatio.getText().toString();
	 * 
	 * JSONObject userProfile = LocalStorage.getJSONObject(this, "userProfile");
	 * // 등록수정자 불러오기 String userid = ""; try { userid =
	 * userProfile.getString("User_ID"); } catch (JSONException e) {
	 * e.printStackTrace(); }
	 * 
	 * // 선택된 상품장의 데이터를 꺼내옴 String Org_PurPri = m_tempProduct.get("Pur_Pri"); //
	 * 원매입가 String Org_SellPri = m_tempProduct.get("Sell_Pri"); // 원매출가 String
	 * Pur_DCRate = "0"; // 매입DC률 매입가에서 적용 %만큼 DC한다 String Pur_PriDC = "0"; //
	 * 매입DC금액 %DC된 금액 // String Pur_Cost = m_tempProduct.get("Pur_Cost"); //
	 * 매입원가 // String Add_Tax = m_tempProduct.get("Add_Tax"); // 부가세 String
	 * Tax_YN = m_tempProduct.get("Tax_YN"); // 과세여부(0:면세,1:과세) String VAT_CHK =
	 * m_tempProduct.get("VAT_CHK"); // 부가세구분(0:별도,1:포함) String Tax_Gubun = "1";
	 * // 부가세포함, 별도 구분 if (Vat_Chk) { Tax_Gubun = "1"; } else { Tax_Gubun = "0";
	 * } String In_YN = m_tempProduct.get("In_YN"); //
	 * 매입여부(0:반품,1:매입,2:행사반품,3:행사매입) String Box_Use =
	 * m_tempProduct.get("Box_Use"); // 박스상품여부(0:미사용,1:사용) String Pack_Use =
	 * m_tempProduct.get("Pack_Use"); // 묶음상품여부(0:미사용,1:사용) String Bot_Code =
	 * m_tempProduct.get("Bot_Code"); // 공병코드 String Bot_Name =
	 * m_tempProduct.get("Bot_Name"); // 공병명 String Bot_Pur =
	 * m_tempProduct.get("Bot_Pur"); // 공병매입가 String Bot_Sell =
	 * m_tempProduct.get("Bot_Sell"); // 공병매출가 String Scale_Use =
	 * m_tempProduct.get("Scale_Use"); // 저울상품 String BarCode1 =
	 * m_tempProduct.get("BarCode1"); // 박스 바코드 String Obtain =
	 * m_tempProduct.get("Obtain"); // 입수수량
	 * 
	 * // 저울상품 또는 부문상품일때 가격변경 불가 String Change_Use = "1"; if
	 * (m_tempProduct.get("Scale_Use").toString().equals("1") ||
	 * (barcode.length() == 4)) Change_Use = "0";
	 * 
	 * // 전체 체크 시에 모두 변경 해야 합니다. // String checkAll; String checkPurpri; String
	 * checkSellpri; String checkProductName; String checkCustomer;
	 * 
	 * // 매입가변경 체크 if (m_checkBoxPurpri.isChecked()) { checkPurpri = "1"; } else
	 * { checkPurpri = "0"; } // 판매가변경 체크 if (m_checkBoxSellpri.isChecked()) {
	 * checkSellpri = "1"; } else { checkSellpri = "0"; }
	 * 
	 * // 상품명 변경 체크 if (m_checkBoxProductName.isChecked()) { checkProductName =
	 * "1"; } else { checkProductName = "0"; } // 거래처체크 if
	 * (m_checkBoxCustomer.isChecked()) { checkCustomer = "1"; } else {
	 * checkCustomer = "0"; }
	 * 
	 * String Summarize = "정상가"; // 매입여부(0:반품,1:매입,2:행사반품,3:행사매입) if
	 * (In_YN.toString().equals("2") || In_YN.toString().equals("3")) { //
	 * 행사상품일때 Change_Use = "0"; // 행사 상품일때 상품 매입가 판매가 거래처 변경 불가능!! if
	 * (m_checkBoxRejectedProduct.isChecked()) // 반품 In_YN = "2"; else In_YN =
	 * "3"; } else { if (!purchasePrice.toString().equals(Org_PurPri)) Summarize
	 * += ", 매입가변경"; if (!salePrice.toString().equals(Org_SellPri)) Summarize +=
	 * ", 판매가변경"; if (m_checkBoxRejectedProduct.isChecked()) // 반품 In_YN = "0";
	 * else In_YN = "1"; }
	 * 
	 * if (Bot_Pur == null) Bot_Pur = "0"; if (Bot_Sell == null) Bot_Sell = "0";
	 * 
	 * // 반품 체크되어있을시에 수량 -값 적용 하기 if (m_checkBoxRejectedProduct.isChecked()) {
	 * amount = String.valueOf(Integer.valueOf(amount) * -1); }
	 * 
	 * double In_Pri = Double.valueOf(purchasePrice) * Double.valueOf(amount);
	 * // 총 // 매입가(공병포함)=매입가x수량 double In_SellPri = Double.valueOf(salePrice) *
	 * Double.valueOf(amount); // 판매가x수량
	 * 
	 * String period = m_period.getText().toString();
	 * 
	 * // 목록에 저장하기 HashMap<String, String> rmap = new HashMap<String, String>();
	 * rmap.put("purchaseDate", purchaseDate); // 매입일 //
	 * rmap.put("immediatePayment", (immediatePayment)?"1":"0"); // 즉시결재
	 * rmap.put("In_YN", In_YN); // 매입여부 rmap.put("In_Gubun", "3"); // 매입구분
	 * rmap.put("In_Date", period); // 매입일 rmap.put("BarCode", barcode); // 바코드
	 * rmap.put("G_Name", productName); // 상품명 rmap.put("Office_Code", code); //
	 * 거래처코드 rmap.put("Office_Name", name); // 거래처명 rmap.put("Tax_YN", Tax_YN);
	 * // 면과세 rmap.put("VAT_CHK", VAT_CHK); // 부가세 별도,포함 rmap.put("Tax_Gubun",
	 * Tax_Gubun); // 면과세 구분 rmap.put("In_Count", amount); // 매입수량
	 * rmap.put("Pur_Pri", purchasePrice); // 매입가 rmap.put("Org_PurPri",
	 * Org_PurPri); // 원매입가 rmap.put("Pur_DCRate", Pur_DCRate); // 매입DC율
	 * rmap.put("Pur_PriDC", Pur_PriDC); // 매입DC금액 rmap.put("Pur_Cost",
	 * Pur_Cost); // 매입원가 rmap.put("Add_Tax", Add_Tax); // 매입과세
	 * rmap.put("TPur_Pri", String.valueOf((Double.valueOf(purchasePrice) -
	 * Double.valueOf(Bot_Pur)) * Double.valueOf(amount))); // 총 //
	 * 매입가(공병제외)=매입가x수량 rmap.put("TPur_Cost",
	 * String.valueOf(Double.valueOf(Pur_Cost) * Double.valueOf(amount))); //
	 * 매입원가x수량 rmap.put("TAdd_Tax", String.valueOf(Double.valueOf(Add_Tax) *
	 * Double.valueOf(amount))); // 부가세x수량 rmap.put("In_Pri",
	 * String.valueOf(In_Pri)); // 총 매입가(공병포함)=매입가x수량 rmap.put("Sell_Pri",
	 * salePrice); // 판매가 rmap.put("Org_SellPri", Org_SellPri); // 원판매가
	 * rmap.put("TSell_Pri", String.valueOf((Double.valueOf(salePrice) -
	 * Double.valueOf(Bot_Sell)) * Double.valueOf(amount))); // 총 //
	 * 판매가(공병제외)=판매가x수량 rmap.put("In_SellPri", String.valueOf(In_SellPri)); //
	 * 판매가x수량 rmap.put("Profit_Pri", String.valueOf(In_SellPri - In_Pri)); //
	 * 이익금 rmap.put("Profit_Rate", profitRatio); // 이익률 rmap.put("Box_Use",
	 * Box_Use); // 박스상품 rmap.put("Pack_Use", Pack_Use); // 묶음상품
	 * rmap.put("Bot_Code", Bot_Code); // 공병코드 rmap.put("Bot_Name", Bot_Name);
	 * // 공병명 rmap.put("Bot_Pri", Bot_Pur); // 공병매입가 rmap.put("Bot_SellPri",
	 * Bot_Sell); // 공병판매가 rmap.put("Summarize", Summarize); // 적요
	 * rmap.put("Write_Date", currentDate); // 등록일 rmap.put("Edit_Date",
	 * currentDate); // 수정일 rmap.put("Writer", userid); // 등록자
	 * rmap.put("Editor", userid); // 수정자 rmap.put("checkProductName",
	 * checkProductName); // 상품명 변경 체크 rmap.put("checkPurpri", checkPurpri); //
	 * 매입가 변경 rmap.put("checkSellpri", checkSellpri); // 판매가 변경
	 * rmap.put("checkCustomer", checkCustomer); // 거래처변경 rmap.put("Change_Use",
	 * Change_Use); // 가격변경상품 구분 rmap.put("Scale_Use", Scale_Use);
	 * rmap.put("BarCode1", BarCode1); rmap.put("Obtain", Obtain);
	 * 
	 * Log.i("상품정보 저장전", rmap.toString()); m_purList.set(m_selectedListIndex,
	 * rmap);
	 * 
	 * // ListView 에 뿌려줌 // mfillMaps = makeFillvapsWithStockList();
	 * makeFillvapsWithStockList(); // 저장한 결과를 넣어 주세요 원레 있던 소스에 덭붙이기 해서 이렇게 됨
	 * mfillMaps.addAll(m_purList);
	 * 
	 * m_adapter.notifyDataSetChanged(); // 초기화 해주세요 m_selectedListIndex = -1; }
	 */

	// 매입가 계산해서 보여 주기
	public void makeFillvapsWithStockList() {
		List<HashMap<String, String>> fm = new ArrayList<HashMap<String, String>>();

		Iterator<HashMap<String, String>> iterator = mfillMaps.iterator();
		totalPurPri = 0.0;
		while (iterator.hasNext()) {
			boolean isNew = true;
			HashMap<String, String> element = iterator.next();			
			totalPurPri += Double.valueOf(element.get("Price_Sum").replace(",", ""));
			Log.i("결과물", String.format("%.2f", totalPurPri));
			/*
			 * Iterator<HashMap<String, String>> fm_iterator = fm.iterator();
			 * while (fm_iterator.hasNext()) {
			 * 
			 * HashMap<String, String> fm_element = fm_iterator.next(); String
			 * fm_Office_Code = fm_element.get("Office_Code");
			 * 
			 * if (fm_Office_Code.equals(Office_Code)) { isNew = false; // 같은게
			 * 있으면 fm_element에 추가 String fm_Pur_Pri =
			 * fm_element.get("TPur_Pri"); String fm_In_Count =
			 * fm_element.get("In_Count"); String Pur_Pri =
			 * element.get("TPur_Pri"); String In_Count =
			 * element.get("In_Count");
			 * 
			 * fm_element.put("TPur_Pri", String.valueOf(Double.valueOf(Pur_Pri)
			 * + Double.valueOf(fm_Pur_Pri)));
			 * fm_element.put("In_Count",String.valueOf(Integer.valueOf(
			 * In_Count) + Integer.valueOf(fm_In_Count))); } }
			 * 
			 * if (isNew) { String BarCode = element.get("BarCode"); String
			 * Office_Name = element.get("Office_Name"); String Pur_Pri =
			 * element.get("TPur_Pri"); String In_Count =
			 * element.get("In_Count"); String purchaseDate =
			 * element.get("purchaseDate"); HashMap<String, String> map = new
			 * HashMap<String, String>();
			 * 
			 * map.put("Office_Code", Office_Code); map.put("BarCode", BarCode);
			 * map.put("Office_Name", Office_Name); map.put("TPur_Pri", Pur_Pri
			 * ); map.put("In_Count", In_Count ); map.put("purchaseDate",
			 * purchaseDate );
			 * 
			 * fm.add(map); }
			 */
		}

		// 전체 매입가 결과 출력
		m_totalPurpri.setText(String.format("%,.2f", totalPurPri));
		
		// 전체매입가
		// return fm;
	}

	// Comparator 를 만든다.
	private final static Comparator<HashMap<String, String>> myComparator = new Comparator<HashMap<String, String>>() {

		private final Collator collator = Collator.getInstance();

		@Override
		public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
			String In_Num1 = lhs.get("Office_Code");
			String In_Num2 = rhs.get("Office_Code");

			return collator.compare(In_Num1, In_Num2);
		}
	};

	// 2017-04 저장프로시저 사용하기
	// MSSQL 전체 전송하기 (데이타 베이스에 저장하기)
	/*
	 * public void sendAllData() {
	 * 
	 * String tableName = null, tableName2 = null; String period =
	 * m_period.getText().toString(); String gname =
	 * m_textProductName.getText().toString(); String code =
	 * m_customerCode.getText().toString(); String name =
	 * m_customerName.getText().toString();
	 * 
	 * int year = Integer.parseInt(period.substring(0, 4)); int month =
	 * Integer.parseInt(period.substring(5, 7));
	 * 
	 * tableName = String.format("InD_%04d%02d", year, month); tableName2 =
	 * String.format("InT_%04d%02d", year, month);
	 * 
	 * // 거래처기준으로 정렬 // Collections.sort(m_purList, myComparator);
	 * 
	 * // 쿼리 작성하기 (Ind 테이블) String query = ""; String query1 = ""; String
	 * m_querySQLite = ""; String junpyo = makeJunPyo();
	 * 
	 * double TTPur_Pri = 0, TTAdd_Tax = 0, TFPur_Pri = 0, In_TPri = 0, In_FPri
	 * = 0, In_Pri = 0, In_RePri = 0; double TSell_Pri = 0, In_SellPri = 0,
	 * Bot_Pri = 0, Bot_SellPri = 0, Profit_Pri = 0, Profit_Rate = 0; boolean
	 * isIncludedYN = false; // 전표내의 물건중 반품이 포함된경우,
	 * 
	 * // 여러개 중복해서 거래처있을때 했던 내용 지금은 사용안함 for (int i = 0, seq = 1, tseq = 1; i <
	 * m_purList.size(); i++, seq++) {
	 * 
	 * // 목록에 있는것을 불러오기 HashMap<String, String> pur = m_purList.get(i); //
	 * curOffice_Code = pur.get("Office_Code"); // 새로운 거래처이면 새로운 전표 발행
	 * 
	 * if (!prevOffice_Code.equals(curOffice_Code)) { junpyo = makeJunPyo(); seq
	 * =1; isIncludedYN = false; } prevOffice_Code = pur.get("Office_Code");
	 * 
	 * 
	 * // 매입상세 (InD_* table) 저장하기 쿼리 query += "insert into " + tableName +
	 * "(In_Num, In_BarCode, In_YN, In_Gubun, In_Date, BarCode, In_Seq, Office_Code, Office_Name, Tax_YN, Tax_Gubun, "
	 * +
	 * "In_Count, Pur_Pri, Org_PurPri, Pur_DCRate, Pur_PriDC, Pur_Cost, Add_Tax, TPur_Pri, TPur_Cost, TAdd_Tax, In_Pri, "
	 * +
	 * "Sell_Pri, Org_SellPri, TSell_Pri, In_SellPri, Profit_Pri, Profit_Rate, Bot_Code, Bot_Name,  Bot_Pri, Bot_SellPri, Box_Use, Pack_Use, "
	 * + " Summarize, Write_Date, Edit_Date, Writer, Editor) " + " values ("; //
	 * 기록하기 위해 남깁니다. m_querySQLite = "insert into Temp_InDPDA_HISTORY" +
	 * "(In_Num, In_BarCode, In_YN, In_Gubun, In_Date, BarCode, In_Seq, Office_Code, Office_Name, Tax_YN, Tax_Gubun, "
	 * +
	 * "In_Count, Pur_Pri, Org_PurPri, Pur_DCRate, Pur_PriDC, Pur_Cost, Add_Tax, TPur_Pri, TPur_Cost, TAdd_Tax, In_Pri, "
	 * +
	 * "Sell_Pri, Org_SellPri, TSell_Pri, In_SellPri, Profit_Pri, Profit_Rate, Bot_Code, Bot_Name,  Bot_Pri, Bot_SellPri, Box_Use, Pack_Use, "
	 * +
	 * " Summarize, Write_Date, Edit_Date, Writer, Editor, Scale_Use, BarCode1, Obtain, Chk_Cust, Chk_Pur,"
	 * + " Chk_Sell, tIn_Num, PDA_TRAN_DATE, TRAN_SEQ ) " + " values ("; query1
	 * = "'" + junpyo + "', " // 전표번호 : '20140514B002' + "'" + junpyo + "', " //
	 * 전표번호 : '20140514B002' + "'" + pur.get("In_YN").toString() + "', " //
	 * 매입여부(0:반품, // 1:매입,2:행사반품,3:행사매입) // : '0' + "'" +
	 * pur.get("In_Gubun").toString() + "', " // 등록구분(0:수기,1:핸디,2:PDA,3:App) //
	 * : '1' + "'" + pur.get("In_Date").toString() + "', " // 매입일 : //
	 * '2014-05-14' + "'" + pur.get("BarCode").toString() + "', " // 바코드 : //
	 * '8801136361049' + "'" + seq + "', " // 순번 : 1 + "'" +
	 * pur.get("Office_Code").toString() + "', " // 거래처코드 : // '10004' + "'" +
	 * pur.get("Office_Name").toString() + "', " // 거래처명 : // '세기유통(주' + "'" +
	 * pur.get("Tax_YN").toString() + "', " // 과세여부(0:면세,1:과세) // : '1' + "'" +
	 * pur.get("Tax_Gubun").toString() + "', " // 부가세구분(0:별도,1:포함) // : '1' +
	 * "'" + pur.get("In_Count").toString() + "', "; // 매입수량 : 10 if
	 * (pur.get("Tax_YN").equals("1") && pur.get("Tax_Gubun").equals("0")) {
	 * query1 += "'" + pur.get("Pur_Cost").toString() + "', "; // 매입가 : // 3400
	 * } else { query1 += "'" + pur.get("Pur_Pri").toString() + "', "; // 매입가 :
	 * // 3400 } query1 += "'" + pur.get("Org_PurPri").toString() + "', " //
	 * 원매입가 : // 3400 // 2014-05-14 추가내용 빠져있어서 추가했슴 // , Pur_DCRate, Pur_PriDC,
	 * + "'" + pur.get("Pur_DCRate").toString() + "', " // 매입가 DC%율 // : 0 + "'"
	 * + pur.get("Pur_PriDC").toString() + "', " // 매입가 DC 금액 // : 0 + "'" +
	 * pur.get("Pur_Cost").toString() + "', " // 매입원가 : // 3090.91 + "'" +
	 * pur.get("Add_Tax").toString() + "', " // 부가세 : // 309.09 + "'" +
	 * pur.get("TPur_Pri").toString() + "', " // 총 // 매입가(공병제외) // : 34000 + "'"
	 * + pur.get("TPur_Cost").toString() + "', " // 총 매입원가 : // 30909.09 + "'" +
	 * pur.get("TAdd_Tax").toString() + "', " // 총 부가세 : // 3090.91 + "'" +
	 * pur.get("In_Pri").toString() + "', " // 총 매입가(공병포함) // : 34000 + "'" +
	 * pur.get("Sell_Pri").toString() + "', " // 판매가 : 4250 + "'" +
	 * pur.get("Org_SellPri").toString() + "', " // 원 판매가 : // 4250 + "'" +
	 * pur.get("TSell_Pri").toString() + "', " // 총 // 판매가(공병제외) // : 42500 +
	 * "'" + pur.get("In_SellPri").toString() + "', " // 총 // 판매가(공병포함) // : //
	 * 42500 + "'" + pur.get("Profit_Pri").toString() + "', " // 이익금 : // 8500 +
	 * "'" + pur.get("Profit_Rate").toString() + "', " // 이익율 : // 20 //
	 * 2014-05-14 추가내용 빠져있어서 추가했슴 // Bot_Code, Bot_Name, Bot_Pri, Bot_SellPri +
	 * "'" + pur.get("Bot_Code").toString() + "', " // 콩병코드 : '' + "'" +
	 * pur.get("Bot_Name").toString() + "', " // 공병명 : '' + "'" +
	 * pur.get("Bot_Pri").toString() + "', " // 공병총매입가 : 0 + "'" +
	 * pur.get("Bot_SellPri").toString() + "', " // 공병총판매가 // : 0 + "'" +
	 * pur.get("Box_Use").toString() + "', " // 박스 상품 유무 : // 0 + "'" +
	 * pur.get("Pack_Use").toString() + "', " // 묶음 상품 유무 : // 0 // 2014-05-14
	 * 추가내용 빠져있어서 추가했슴 // Summarize + "'" + pur.get("Summarize").toString() +
	 * "', " // 적요 : // '정상가' + "'" + pur.get("Write_Date").toString() + "', "
	 * // 등록일 : // '2014-05-14' + "'" + pur.get("Edit_Date").toString() + "', "
	 * // 수정일 : // '2014-05-14' + "'" + pur.get("Writer").toString() + "', "; //
	 * 등록자 : // 'tips'
	 * 
	 * m_querySQLite += query1.toString(); query += query1.toString(); query +=
	 * "'" + pur.get("Editor").toString() + "'); "; // 수정자 : // 'tips'
	 * 
	 * // 체크 확인 하고 상품장 변경하기 SimpleDateFormat sdf = new
	 * SimpleDateFormat("hh:mm:ss"); // 입력창과 상관없이 현재 시간으로 등록일 수정일 불러오기 String
	 * currentTime = sdf.format(new Date()); SimpleDateFormat sdf1 = new
	 * SimpleDateFormat("yyyy-MM-dd hh:mm:ss"); String currentDateTime =
	 * sdf1.format(new Date()); // Log.i("체크데이타", pur.get("checkAll").toString()
	 * + // pur.get("Change_Use") + pur.get("Change_Use").toString() );
	 * 
	 * m_querySQLite += "'" + pur.get("Editor").toString() + "', " // 수정자 : //
	 * 'tips' + "'" + pur.get("Scale_Use").toString() + "', " // 저울상품 + "'" +
	 * pur.get("BarCode1").toString() + "', " // 박스상품 + "'" +
	 * pur.get("Obtain").toString() + "', " // 입수량 + "'" +
	 * pur.get("checkCustomer").toString() + "', " // 거래처변경 + "'" +
	 * pur.get("checkPurpri").toString() + "', " // 매입가 변경 + "'" +
	 * pur.get("checkSellpri").toString() + "', " // 판매가 변경 + "'" + junpyo +
	 * "', " // 토탈전표번호 + "'" + currentDateTime.toString() + "', " // 일자 + "'" +
	 * String.valueOf(m_tranSEQ) + "' ); "; // 전송횟수
	 * 
	 * query += m_querySQLite.toString();
	 * 
	 * // 저울 상품 또는 행사상품 부문상품 매입가 판매가 거래처 변경 불가적용 if(
	 * pur.get("checkAll").equals("1") && pur.get("Change_Use").equals("1") ){
	 * 
	 * //변경해야할것들 query += " Update Goods Set " + " Pur_Pri = "
	 * +pur.get("Pur_Pri").toString()+"," + " Pur_Cost = "
	 * +pur.get("Pur_Cost").toString()+"," + " Add_Tax = "
	 * +pur.get("Add_Tax").toString()+"," + " Sell_Pri = "+
	 * pur.get("Sell_Pri").toString() +"," + " Profit_Rate = "
	 * +pur.get("Profit_Rate").toString()+"," + " Bus_Code = '" +code+"'," +
	 * " Bus_Name = '"+ name +"'," + " Edit_Check = '1'," + " Tran_Chk = '1'," +
	 * " Edit_Date = '" +pur.get("Edit_Date").toString()+ "'," + " Editor = '"
	 * +pur.get("Writer").toString()+ "'" + " Where BarCode  = '"+
	 * pur.get("BarCode").toString() +"' ;";
	 * 
	 * //상품장 변경후 히스토리에 추가해 주기 query +=
	 * " Insert Into ProductPrice_History(P_Day, P_Time, P_Barcode, P_OldDanga, "
	 * + " P_NewDanga, P_OldPrice, P_NewPrice, P_Gubun, P_PosNo, P_UserID) " +
	 * " Values( " + "'" + pur.get("Edit_Date").toString() + "', " + "'" +
	 * currentTime + "', " + "'" + pur.get("BarCode").toString() + "', " + "'" +
	 * pur.get("Org_PurPri").toString() + "', " + "'" +
	 * pur.get("Pur_Pri").toString() + "', " + "'" +
	 * pur.get("Org_SellPri").toString() + "', " + "'" +
	 * pur.get("Sell_Pri").toString() + "', " + "'4', " + "'" + posID + "', " +
	 * "'" + pur.get("Writer").toString() + "' ); ";
	 * 
	 * }else
	 * 
	 * if (pur.get("Change_Use").equals("1") &&
	 * (pur.get("checkPurpri").equals("1") ||
	 * pur.get("checkSellpri").equals("1") ||
	 * pur.get("checkCustomer").equals("1")) ||
	 * pur.get("checkProductName").equals("1")) { // 전체 체크 안되있고 저울상품 부분상품이 아닌
	 * 것들은 가격변경 가능하게 해주세요 String query_1 = " ";
	 * 
	 * // 따로 체크일때 이익률 계산을 위해서 float f_ratio; float org_purpri; float
	 * org_sellpri; String purpri = pur.get("Pur_Pri").toString(); String
	 * sellpri = pur.get("Sell_Pri").toString();
	 * 
	 * if (pur.get("checkPurpri").equals("1") ||
	 * pur.get("checkSellpri").equals("1")) {
	 * 
	 * // 매입가에 체크 되어 있을때 if (pur.get("checkPurpri").equals("1")) { query_1 +=
	 * " Pur_Pri = " + pur.get("Pur_Pri").toString() + ", " + " Pur_Cost = " +
	 * pur.get("Pur_Cost").toString() + ", " + " Add_Tax = " +
	 * pur.get("Add_Tax").toString() + ", "; } // 판매가에 체크 되어 있을때 if
	 * (pur.get("checkSellpri").equals("1")) { query_1 += " Sell_Pri = " +
	 * pur.get("Sell_Pri").toString() + ", "; } // 기본 이익률은 넣어야 합니다. // 매입가 변경체크
	 * 안되있고 판매가 변경 체크 되있을때 if (pur.get("checkPurpri").equals("0")) { // 매입가 변경
	 * 체크는 안되있는데 매입가는 변경 되었나? if
	 * (!pur.get("Org_PurPri").equals(pur.get("Pur_Pri"))) { // 체크를 안했기 때문에
	 * 원매입가와 현변경 판매가로 새로 이익률 계산 purpri = pur.get("Org_PurPri").toString();
	 * org_purpri = Float.parseFloat(purpri); // 원매입가 org_sellpri =
	 * Float.parseFloat(pur.get("Sell_Pri")); // 변경판매가 // 이익률 재계산 f_ratio =
	 * (org_sellpri - org_purpri) / org_sellpri * 100; query_1 +=
	 * " Profit_Rate = " + String.format("%.2f", f_ratio) + ","; } // 판매가 변경 체크
	 * 안되있고 매입가 변경체크 되있을때 } else if (pur.get("checkSellpri").equals("0")) { //
	 * 판매가 변경 체크는 안되있는데 판매가는 변경 되었나? if
	 * (!pur.get("Org_SellPri").equals(pur.get("Sell_Pri"))) { // 체크를 안했기 때문에
	 * 원판매가와 현변경 매입가로 새로 이익률 계산 sellpri = pur.get("Org_SellPri").toString();
	 * org_purpri = Float.parseFloat(pur.get("Pur_Pri")); org_sellpri =
	 * Float.parseFloat(sellpri); // 이익률 재계산 f_ratio = (org_sellpri -
	 * org_purpri) / org_sellpri * 100; query_1 += " Profit_Rate = " +
	 * String.format("%.2f", f_ratio) + ","; }
	 * 
	 * } else { query_1 += " Profit_Rate = " + pur.get("Profit_Rate").toString()
	 * + ","; }
	 * 
	 * // 상품장 변경후 히스토리에 추가해 주기 query +=
	 * " Insert Into ProductPrice_History(P_Day, P_Time, P_Barcode, P_OldDanga, "
	 * + " P_NewDanga, P_OldPrice, P_NewPrice, P_Gubun, P_PosNo, P_UserID) " +
	 * " Values( " + "'" + pur.get("Edit_Date").toString() + "', " + "'" +
	 * currentTime + "', " + "'" + pur.get("BarCode").toString() + "', " + "'" +
	 * pur.get("Org_PurPri").toString() + "', " + "'" + purpri.toString() +
	 * "', " + "'" + pur.get("Org_SellPri").toString() + "', " + "'" +
	 * sellpri.toString() + "', " + "'4', " + "'" + posID + "', " + "'" +
	 * pur.get("Writer").toString() + "' ); "; }
	 * 
	 * // 상품명 변경 체크 if (pur.get("checkProductName").equals("1")) { query_1 +=
	 * " G_Name = '" + gname + "' , "; }
	 * 
	 * // 거래처 변경 체크 if (pur.get("checkCustomer").toString().equals("1")) {
	 * query_1 += " Bus_Code = '" + code + "'," + " Bus_Name = '" + name + "', "
	 * ; }
	 * 
	 * // 변경해야할것들 query += " Update Goods Set " + query_1 + " Edit_Check = '1',"
	 * + " Tran_Chk = '1'," + " Edit_Date = '" + pur.get("Edit_Date").toString()
	 * + "'," + " Editor = '" + pur.get("Writer").toString() + "'" +
	 * " Where BarCode  = '" + pur.get("BarCode").toString() + "' ; "; }
	 * 
	 * // InT용 데이터 누적시킴 String Tax_YN = pur.get("Tax_YN"); // 과세여부(0:면세,1:과세)
	 * String Vat_Chk = pur.get("VAT_CHK"); // 부가세 별도 포함 String Tax_Gubun =
	 * pur.get("Tax_Gubun"); // 변경 되었는지 체크 해야합니다. String In_YN =
	 * pur.get("In_YN"); // 매입여부(0:반품,1:매입,2:행사반품,3:행사매입)
	 * 
	 * if (!Vat_Chk.equals(Tax_Gubun.toString())) { query +=
	 * " update goods set VAT_CHK='" + Tax_Gubun + "' where barcode='" +
	 * pur.get("BarCode").toString() + "' "; }
	 * 
	 * if (In_YN.equals("0")) isIncludedYN = true; // 전표내의 물건중 반품이 포함된경우,
	 * 
	 * TTAdd_Tax += Double.valueOf(pur.get("Add_Tax")); // 총 과세 부가세 if
	 * (Tax_YN.equals("1")) TTPur_Pri += Double.valueOf(pur.get("TPur_Pri")); //
	 * 총 // 과세매입가(공병제외) if (Tax_YN.equals("0")) TFPur_Pri +=
	 * Double.valueOf(pur.get("TPur_Pri")); // 총 // 면세매입가(공병제외) if
	 * (Tax_YN.equals("1")) In_TPri += Double.valueOf(pur.get("In_Pri")); // 총
	 * 과세매입가(공병포함) if (Tax_YN.equals("0")) In_FPri +=
	 * Double.valueOf(pur.get("In_Pri")); // 총 면세매입가(공병포함) if
	 * (In_YN.equals("0")) In_RePri += Double.valueOf(pur.get("In_Pri")) * -1;
	 * // 총 // 반품가(공병포함) In_Pri += Double.valueOf(pur.get("In_Pri")); // 총
	 * 매입가(공병포함) TSell_Pri += Double.valueOf(pur.get("TSell_Pri")); // 총
	 * 판매가(공병제외) In_SellPri += Double.valueOf(pur.get("In_SellPri")); // 총
	 * 판매가(공병포함) Bot_Pri += Double.valueOf(pur.get("Bot_Pri")); // 공병 총 매입가
	 * Bot_SellPri += Double.valueOf(pur.get("Bot_SellPri")); // 공병 총 판매가
	 * Profit_Pri = In_SellPri - In_Pri; Profit_Rate = Profit_Pri / In_SellPri *
	 * 100;
	 * 
	 * // 마지막이거나 다음것이 새로운 거래처이면, if (m_purList.size() == i + 1) {
	 * 
	 * if (m_purList.size() > i+1) { HashMap<String, String> nextPur =
	 * m_purList.get(i+1); if
	 * (curOffice_Code.equals(nextPur.get("Office_Code")))continue; }
	 * 
	 * 
	 * // 매입총괄(InT) 쿼리생성 query += "insert into " + tableName2 +
	 * "(In_Num, In_Date, In_Seq, Office_Code, Office_Name, " +
	 * "TTPur_Pri, TTAdd_Tax, TFPur_Pri, In_TPri, In_FPri, In_Pri, In_RePri, " +
	 * "TSell_Pri, In_SellPri, Bot_Pri, Bot_SellPri, Profit_Pri, Profit_Rate, Bigo,"
	 * + "Write_Date, Edit_Date, Writer, Editor, EDIT_YN) " + " values (" + "'"
	 * + junpyo + "', " + "'" + pur.get("In_Date").toString() + "', " + "'" +
	 * String.valueOf(m_junpyoInTIdx) + "', " // + "'" +
	 * pur.get("Office_Code").toString() + "', " // + "'" +
	 * pur.get("Office_Name").toString() + "', " + "'" + code + "', " + "'" +
	 * name + "', " + "'" + String.valueOf(TTPur_Pri) + "', " + "'" +
	 * String.valueOf(TTAdd_Tax) + "', " + "'" + String.valueOf(TFPur_Pri) +
	 * "', " + "'" + String.valueOf(In_TPri) + "', " + "'" +
	 * String.valueOf(In_FPri) + "', " + "'" + String.valueOf(In_Pri) + "', " +
	 * "'" + String.valueOf(In_RePri) + "', " + "'" + String.valueOf(TSell_Pri)
	 * + "', " + "'" + String.valueOf(In_SellPri) + "', " + "'" +
	 * String.valueOf(Bot_Pri) + "', " + "'" + String.valueOf(Bot_SellPri) +
	 * "', " + "'" + String.valueOf(Profit_Pri) + "', " + "'" +
	 * String.valueOf(Profit_Rate) + "', " + "'', " // 비고 + "'" +
	 * pur.get("Write_Date").toString() + "', " + "'" +
	 * pur.get("Edit_Date").toString() + "', " + "'" +
	 * pur.get("Writer").toString() + "', " + "'" + pur.get("Editor").toString()
	 * + "', '0' );";
	 * 
	 * // 거래처 대금 결제 (Office_Settlement) // String immediatePayment = //
	 * pur.get("immediatePayment").toString() ; if
	 * (m_immediatePayment.isChecked()) { // 즉시결제 query +=
	 * " insert into Office_Settlement ( Sale_Code, Pro_Date, In_Seq, Office_Code, Office_Name, Pay_Pri, "
	 * +
	 * " Sale_Pri, Sale_Rate, Gubun, Bigo, Write_Date, Edit_Date, Writer, Editor) Values ("
	 * + "'" + junpyo + "', " + "'" + pur.get("In_Date").toString() + "', " +
	 * "'" + tseq + "', " + "'" + code + "', " + "'" + name + "', " + "'" +
	 * String.valueOf(In_Pri) + "', " + "'0', " + "'0', " + "'3', " +
	 * "'매입시 즉시결제', " + "'" + pur.get("Write_Date").toString() + "', " + "'" +
	 * pur.get("Edit_Date").toString() + "', " + "'" +
	 * pur.get("Writer").toString() + "', " + "'" + pur.get("Editor").toString()
	 * + "');"; } if (isIncludedYN) { // 반품 query +=
	 * " insert into Office_Settlement ( Sale_Code, Pro_Date, In_Seq, Office_Code, Office_Name, Buy_RePri, "
	 * +
	 * " Sale_Pri, Sale_Rate, Gubun, Bigo, Write_Date, Edit_Date, Writer, Editor) Values ("
	 * + "'" + junpyo + "', " + "'" + pur.get("In_Date").toString() + "', " +
	 * "'" + tseq + "', " + "'" + code + "', " + "'" + name + "', " + "'" +
	 * String.valueOf(In_RePri) + "', " + "'0', " + "'0', " + "'4', " + "'', " +
	 * "'" + pur.get("Write_Date").toString() + "', " + "'" +
	 * pur.get("Edit_Date").toString() + "', " + "'" +
	 * pur.get("Writer").toString() + "', " + "'" + pur.get("Editor").toString()
	 * + "');"; } else { // 매입 query +=
	 * " insert into Office_Settlement ( Sale_Code, Pro_Date, In_Seq, Office_Code, Office_Name, Buy_Pri, "
	 * +
	 * " Sale_Pri, Sale_Rate, Gubun, Bigo, Write_Date, Edit_Date, Writer, Editor) Values ("
	 * + "'" + junpyo + "', " + "'" + pur.get("In_Date").toString() + "', " +
	 * "'" + tseq + "', " + "'" + code + "', " + "'" + name + "', " + "'" +
	 * String.valueOf(In_Pri + In_RePri) + "', " + "'0', " + "'0', " + "'2', " +
	 * "'', " + "'" + pur.get("Write_Date").toString() + "', " + "'" +
	 * pur.get("Edit_Date").toString() + "', " + "'" +
	 * pur.get("Writer").toString() + "', " + "'" + pur.get("Editor").toString()
	 * + "');"; }
	 * 
	 * // 미수금액 업데이트 String Dec_Pri = String.valueOf(In_TPri); query +=
	 * " update Office_Manage Set Dec_Pri = IsNull(Dec_Pri, 0) + '" + Dec_Pri +
	 * "' where Office_Code='" + junpyo + "';";
	 * 
	 * m_junpyoInTIdx++; }
	 * 
	 * // 재고등록 query +=
	 * "update Goods Set Real_Sto=Real_Sto + IsNull(B.In_Count,0) " +
	 * "From Goods A Inner Join ( Select Barcode, Sum (In_Count) In_Count " +
	 * " From (Select A.BarCode, IsNull(Sum(A.In_Count),0) In_Count " + " From "
	 * + tableName + " A, Goods B " +
	 * " Where A.Barcode=B.Barcode AND A.Box_Use='0' AND A.Pack_Use='0' AND A.In_Num ='"
	 * + junpyo + "' Group by A.Barcode " +
	 * " Union All Select B.BarCode1, IsNull(Sum(A.In_Count * IsNull(B.Obtain,0)), 0) In_Count "
	 * + " From " + tableName + " A, Goods B " +
	 * "  Where A.Barcode=B.Barcode AND A.Box_Use='1' AND A.In_Num = '" + junpyo
	 * + "' Group By B.BarCode1 " +
	 * " Union All Select C.C_BARCODE BARCODE, IsNull(Sum(In_Count*C.G_COUNT), 0) InD_Count "
	 * + " From " + tableName + " A, Goods B, Bundle C " +
	 * "  Where A.BARCODE=B.BARCODE AND A.BARCODE=C.P_BARCODE AND A.PACK_USE='1' AND A.In_NUm='"
	 * + junpyo + "' Group By C.C_BARCODE) " + " X Group By Barcode )" +
	 * " B On A.Barcode=B.Barcode;"; } query += " select * from " + tableName +
	 * " where In_Num='" + junpyo + "';";
	 * 
	 * Log.i("r", query); // if (true )return;
	 * 
	 * // 로딩 다이알로그 dialog = new ProgressDialog(this);
	 * dialog.setMessage("Loading...."); dialog.setCancelable(false);
	 * dialog.show();
	 * 
	 * // 콜백함수와 함께 실행 new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {
	 * 
	 * @Override public void onRequestCompleted(JSONArray results) {
	 * dialog.dismiss(); dialog.cancel();
	 * 
	 * deleteListAll(); clearInputBox(); setRenew();
	 * Toast.makeText(getApplicationContext(), "전송완료.",
	 * Toast.LENGTH_SHORT).show(); }
	 * 
	 * @Override public void onRequestFailed(int code, String msg) {
	 * dialog.dismiss(); dialog.cancel();
	 * Toast.makeText(getApplicationContext(), "전송실패(" + String.valueOf(code) +
	 * "):" + msg, Toast.LENGTH_SHORT) .show(); }
	 * 
	 * }).execute(m_ip + ":" + m_port, "TIPS", "sa", "tips", query);
	 * 
	 * }
	 */

	// 2017-04 저장 프로시저 변경
	// 전표갯수를 구함
	/*
	 * public void getInTSeq() {
	 * 
	 * String tableName = null; String period = m_period.getText().toString();
	 * 
	 * int year = Integer.parseInt(period.substring(0, 4)); int month =
	 * Integer.parseInt(period.substring(5, 7));
	 * 
	 * tableName = String.format("InT_%04d%02d", year, month);
	 * 
	 * // 쿼리 작성하기 String query = ""; query +=
	 * "SELECT TOP 1 In_Seq, In_Num FROM " + tableName + " WHERE In_Date='" +
	 * period + "' AND (In_Seq NOT IN(SELECT TOP 0 In_Seq FROM " + tableName +
	 * ")) order by In_Num DESC;";
	 * 
	 * // 로딩 다이알로그 dialog = new ProgressDialog(this);
	 * dialog.setMessage("Loading...."); dialog.setCancelable(false);
	 * dialog.show();
	 * 
	 * // 콜백함수와 함께 실행 new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {
	 * 
	 * @Override public void onRequestCompleted(JSONArray results) {
	 * dialog.dismiss(); dialog.cancel(); if (results.length() > 0) { try {
	 * m_junpyoInTIdx = results.getJSONObject(0).getInt("In_Seq") + 1;
	 * Log.i("전표시작번호", String.format("%d", m_junpyoInTIdx));
	 * 
	 * } catch (JSONException e) { e.printStackTrace(); } } else {
	 * m_junpyoInTIdx = 1; } getHistorySeq(); }
	 * 
	 * @Override public void onRequestFailed(int code, String msg) {
	 * dialog.dismiss(); dialog.cancel();
	 * Toast.makeText(getApplicationContext(), "실패(" + String.valueOf(code) +
	 * "):" + msg, Toast.LENGTH_SHORT) .show(); } }).execute(m_ip + ":" +
	 * m_port, "TIPS", "sa", "tips", query); }
	 */

	// 2017-04 저장프로시저 변경
	// 히스토리 전표 갯수를 구함
	/*
	 * public void getHistorySeq() {
	 * 
	 * // 쿼리 작성하기 String query = ""; query =
	 * "Select TOP 1 TRAN_SEQ from Temp_InDPDA_HISTORY Order By TRAN_SEQ DESC;";
	 * 
	 * // 로딩 다이알로그 dialog = new ProgressDialog(this);
	 * dialog.setMessage("Loading...."); dialog.setCancelable(false);
	 * dialog.show();
	 * 
	 * // 콜백함수와 함께 실행 new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {
	 * 
	 * @Override public void onRequestCompleted(JSONArray results) {
	 * dialog.dismiss(); dialog.cancel(); if (results.length() > 0) { try {
	 * m_tranSEQ = results.getJSONObject(0).getInt("TRAN_SEQ") + 1;
	 * Log.i("TRAN시작번호", String.format("%d", m_tranSEQ)); } catch (JSONException
	 * e) { e.printStackTrace(); } } else { m_junpyoInTIdx = 1; } sendAllData();
	 * }
	 * 
	 * @Override public void onRequestFailed(int code, String msg) {
	 * dialog.dismiss(); dialog.cancel();
	 * Toast.makeText(getApplicationContext(), "실패(" + String.valueOf(code) +
	 * "):" + msg, Toast.LENGTH_SHORT) .show(); } }).execute(m_ip + ":" +
	 * m_port, "TIPS", "sa", "tips", query); }
	 */

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

				//yWithBarcode(); 검색을 두번씩 해서 변경함

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
				
				//이전 거래처 데이터
				mOfficeData[0] = hashMap.get("Office_Code"); 
				mOfficeData[1] = hashMap.get("Office_Name");
				
				//거래처수정 불가
				officeScanAfter();
				
			}
			break;
		case PRODUCT_REGIST_REQUEST:
			if (resultCode == RESULT_OK) {
				doQueryWithBarcode();
			}
			break;
		}
	}

	// 거래처 검색
	public void onCustomerSearch(View view) {
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
		String barcode = m_textBarcode.getText().toString();
		String gname = m_textProductName.getText().toString();
		String officecode = m_customerCode.getText().toString();
		Intent intent = new Intent(this, ManageProductListActivity.class);
		intent.putExtra("barcode", "");
		intent.putExtra("gname", gname);
		intent.putExtra("Office_Code", "");
		startActivityForResult(intent, BARCODE_MANAGER_REQUEST);
	}

	// 상품 카메라 검색
	private void startCameraSearch() {
		Intent intent = new Intent(this, ZBarScannerActivity.class);
		startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
	}

	// 상품 검색 하기 SQL QUERY 실행
	public void doQueryWithBarcode() {

		String query = "";
		// 상품명 원레 이름 등록
		m_textGname = "";
		String barcode = m_textBarcode.getText().toString();
		query = "SELECT * FROM Goods WHERE Barcode = '" + barcode + "';";

		if (barcode.equals(""))
			return;

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

				Log.i("사용 옵션체크", m_newProductOption.toString()); // 신상품 즉시등록 여부
																	// 확인
				if (results.length() > 0) { // 검색결과 확인
					try {
						m_tempProduct = JsonHelper.toStringHashMap(results.getJSONObject(0));
						if (m_tempProduct.get("Goods_Use").toString().equals("0")
								|| m_tempProduct.get("Pur_Use").toString().equals("0")) {
							String str = "사용중지상품 입니다. 변경사용 하시겠습니까? ";
							scanNewProductSpeak(str);
							new AlertDialog.Builder(PurchaseRegistActivity.this,AlertDialog.THEME_HOLO_LIGHT).setTitle(str)
									.setNeutralButton("예", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											String barcode = m_textBarcode.getText().toString();
											String query = "exec sp_executesql N'EXEC str_PDA_004U ''" + barcode
													+ "'''";

											// 콜백함수와 함께 실행
											new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

												@Override
												public void onRequestCompleted(JSONArray results) {
													if (results.length() > 0) {
														try {
															if (!"OK".equals(
																	(String) results.getJSONObject(0).get("1"))) {
																Toast.makeText(PurchaseRegistActivity.this,
																		"변경 하지 못했습니다", Toast.LENGTH_SHORT).show();
																return;
															}
														} catch (JSONException e) {
															Toast.makeText(PurchaseRegistActivity.this,
																	"결과값 이상 오류\r\n" + e.getMessage(),Toast.LENGTH_SHORT).show();
															return;
														}
													}
												}

												@Override
												public void onRequestFailed(int code, String msg) {
													Toast.makeText(PurchaseRegistActivity.this, "연결실패\r\n" + msg, Toast.LENGTH_SHORT)
															.show();
													return;
												}
											}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
										}
									}).setNegativeButton("아니요", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {											
											//releaseInputMode();
											clearInputBox();								
											return;
										}
									}).show();

							scanNewProductSpeak(str);

						}

						m_tempProduct.put("In_YN", "0");
						m_textProductName.setText(results.getJSONObject(0).getString("G_Name"));
						// 상품명 원레 이름 등록
						m_textGname = results.getJSONObject(0).getString("G_Name");
						String s = results.getJSONObject(0).getString("Pur_Pri");
						m_et_purchasePrice.setText(s);
						m_et_purchaseCost.setText(results.getJSONObject(0).getString("Pur_Cost"));
						m_et_addTax.setText(results.getJSONObject(0).getString("Add_Tax"));
						m_et_salePrice.setText(results.getJSONObject(0).getString("Sell_Pri"));
						m_profitRatio.setText(results.getJSONObject(0).getString("Profit_Rate"));

						// 상품장 부가세 별도 포함 상품
						String m_vatchk = results.getJSONObject(0).getString("VAT_CHK").toString();
						setTaxSeparate(results.getJSONObject(0).getString("Tax_YN").toString(), m_vatchk.toString());
						m_amount.requestFocus();
						
						// 거래처 고정선택 되어 있을시에 거래처 변경 안되게 설정 됩니다.
						if (!m_checkBoxOfficeFix.isChecked()) {
							m_customerCode.setText(results.getJSONObject(0).getString("Bus_Code"));
							m_customerName.setText(results.getJSONObject(0).getString("Bus_Name"));
							
							//직전 상품 거래처
							mOfficeData[0] = results.getJSONObject(0).getString("Bus_Code");
							mOfficeData[1] = results.getJSONObject(0).getString("Bus_Name");		
						}else{
							//거래처 고정인데 거래처 코드란이 비어있다면 채워줍니다.
							if("".equals(m_customerCode.getText().toString())){
								m_customerCode.setText(results.getJSONObject(0).getString("Bus_Code"));
								m_customerName.setText(results.getJSONObject(0).getString("Bus_Name"));
								
								//직전 상품 거래처
								mOfficeData[0] = results.getJSONObject(0).getString("Bus_Code");
								mOfficeData[1] = results.getJSONObject(0).getString("Bus_Name");		
							}
						}
						
						// 행사상품 검사하기
						doQueryWithBarcode2(m_textBarcode.getText().toString());
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					
					//신상품 등록 유무
					
					//취소 시
					DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {							
							clearInputBox();			
							dialog.dismiss();
						}
					};
					
					// 새로등록
					DialogInterface.OnClickListener newBarcodeListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							String barcode = m_textBarcode.getText().toString();
							Intent intent = new Intent(PurchaseRegistActivity.this, ManageProductActivityModfiy.class);
							intent.putExtra("barcode", barcode);
							intent.putExtra("productRegYN", "Y");
							intent.putExtra("officeCode", mOfficeData[0]);
							intent.putExtra("officeName", mOfficeData[1]);
							startActivity(intent);
							
							//상품바코드로 다시 이동합니다.
							m_textBarcode.requestFocus();
						}
					};
					
					//후등록 임시 등록해서 사용하기
					DialogInterface.OnClickListener newProductSaveListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// 신상품 후 등록 하고 매입 부터 잡기
							// 거래처 코드 미입력시 상품 첫 스캔 시
							
							String str = "첫 상품은 미등록 상품으로 등록 하실 수 없습니다.";
							
							if("".equals(mOfficeData[0])){
								Toast.makeText(PurchaseRegistActivity.this, str, Toast.LENGTH_SHORT).show();
								m_textBarcode.setText("");
								m_textBarcode.requestFocus();
								dialog.dismiss();
								scanNewProductSpeak(str);
								return;
							}
							
							// m_et_purchasePrice.requestFocus();
							dialog.dismiss();
							
							//후등록 상품 등록 하기
							doRegister();
						}
					};
					
					//미지정 일때 또는 값이 없을때				
					if( "noselection".equals(m_newProductOption) || "".equals(m_newProductOption)){
						
						new AlertDialog.Builder(PurchaseRegistActivity.this,AlertDialog.THEME_HOLO_LIGHT).setTitle("등록되지않은 바코드입니다")
								.setNeutralButton("지금등록", newBarcodeListener)
								.setPositiveButton("후 등록", newProductSaveListener)
								.setNegativeButton("취소", cancelListener)
								.show();											
					}
					
					//즉시등록 시
					if( "newproductyes".equals(m_newProductOption)){					
						//등록하려는 거래처가 필요합니다.
						String barcode = m_textBarcode.getText().toString();
						Intent intent = new Intent(PurchaseRegistActivity.this, ManageProductActivityModfiy.class);
						intent.putExtra("barcode", barcode);
						intent.putExtra("productRegYN", "Y");
						intent.putExtra("officeCode", mOfficeData[0]);
						intent.putExtra("officeName", mOfficeData[1]);
						startActivity(intent);
						
						//상품바코드로 다시 이동합니다.
						m_textBarcode.requestFocus();					
						
					}
					
					//후 등록 시
					if( "newproductno".equals(m_newProductOption)){
						//등록하려는 거래처가 필요합니다.
						// 거래처 코드 미입력시 상품 첫 스캔 시
						
						String str = "첫 상품은 미등록 상품으로 등록 하실 수 없습니다.";
						
						if("".equals(mOfficeData[0])){
							Toast.makeText(PurchaseRegistActivity.this, str, Toast.LENGTH_SHORT).show();
							m_textBarcode.setText("");
							m_textBarcode.requestFocus();
							dialog.dismiss();
							scanNewProductSpeak(str);
							return;
						}
						
						// m_et_purchasePrice.requestFocus();
						dialog.dismiss();
						
						//후등록 상품 등록 하기
						doRegister();
					}				
				}

				// 스캔완료 비푸흠
				scanResult();
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				Toast.makeText(PurchaseRegistActivity.this, "상품검색 실패\r\n" + msg, Toast.LENGTH_SHORT).show();
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
	}

	// 행사상품인지 검사
	public void doQueryWithBarcode2(String barcode) {

		String date = m_period.getText().toString();
		String query = "";
		query += "Select A.BarCode, B.G_Name, B.Std_Size, A.Sale_Pur, A.Sale_Sell, A.Profit_Rate, A.Profit_Pri, B.Tax_YN, B.Bot_Code, B.Bot_Name, B.Bot_Pur, B.Bot_Sell, B.Box_Use, B.Pack_Use, B.VAT_CHK";
		query += " From Evt_Mst As A Inner Join Goods As B ";
		query += " On A.BarCode = B.BarCode ";
		query += " Where A.BarCode = '" + barcode + "' AND (INEvt_SDate <= '" + date + "' And INEvt_EDate >= '" + date
				+ "') ";
		query += " ORDER BY A.SALE_PUR ";

		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();

				if (results.length() > 0) {
					try {
						m_goodsGubun.setText("행사");
						m_goodsGubun.setTextColor(Color.RED);
						m_et_purchasePrice.setText(results.getJSONObject(0).getString("Sale_Pur"));
						m_et_salePrice.setText(results.getJSONObject(0).getString("Sale_Sell"));
						
						//원가 부가세 계산하기						
						calculatePurpri();						
						
						// 이익률 변경하기
						calculateProfitRatio();
												
						m_tempProduct.put("Pur_Pri", m_et_purchasePrice.getText().toString());
						m_tempProduct.put("Pur_Cost", m_et_purchaseCost.getText().toString());
						m_tempProduct.put("Add_Tax", m_et_addTax.getText().toString());
						m_tempProduct.put("Sell_Pri", m_et_salePrice.getText().toString());
						m_tempProduct.put("Profit_Rate", m_profitRatio.getText().toString());
						
						m_tempProduct.put("In_YN", "3"); // 행사상품인경우, 마크
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {

			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
	}

	
	
	
	
	// 후등록 상품 등록 하기 2017-04 저장프로시저 사용하기
	private void doRegister() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = sdf.format(new Date());

		JSONObject userProfile = LocalStorage.getJSONObject(this, "userProfile");
		String userID = "";
		try {
			userID = userProfile.getString("User_ID");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String query = "";
		String barcode = m_textBarcode.getText().toString(); // 1.바코드
		String g_name = "신상품"; // 2.상품명
		String bus_code = mOfficeData[0]; // 3.거래처코드
		String bus_name = mOfficeData[1]; // 4.거래처명

		String lname = "매입대분류"; // 5.대명
		String mname = "매입중분류"; // 6.중명
		String sname = "매입소분류"; // 7.소명
		String lcode = "AA"; // 대코드
		String mcode = "AA"; // 중코드
		String scode = "AAA"; // 소코드

		String tax_yn = "1"; // 8.면과세
		String Std_Size = ""; // 9.규격
		// String Obtain = m_textAcquire.getText().toString(); //입수
		String pur_pri = "800"; // 10.매입가
		String pur_cost = "727.27"; // 11.매입원가
		String add_tax = "72.73"; // 12.부가세
		String salesPrice = "1000"; // 13.판매가
		String ratio = "20"; // 14.이익률
		String vat_chk = "1";// 부가세 포함, 별도 선택

		String g_grade = "None"; // 15.그룹구분
		// String good_use = String.format("%d", m_spinGoods.getSelectedItem()
		// ); //16.사용구분
		String good_type = "0"; // 17.상품구분
		String memberpoint = "1"; // 17.상품구분

		if (g_grade.equals("None")) {
			g_grade = " G_Grade = '' ";
		} else {
			g_grade = " G_Grade = '" + g_grade + "' ";
		}

		// exec sp_executesql N'EXEC str_PDA_007I
		// ''8801136361049'',''삼육두유봉지6입'',''1'',''195ml*5'',3200.00,2909.09,290.91,3900,17.95
		// ''00002'',''거래처2'',10,''g'',10,''0'',''1'',''1'',''1'',''1'',''1'',0,0,''1'',''IN'''

		query = "exec sp_executesql N'EXEC str_PDA_007I " + "''" + barcode + "'', " + "''" + g_name + "'', " + "''"
				+ tax_yn + "'', " + "''" + Std_Size + "'', " + pur_pri + ", " + pur_cost + ", " + add_tax + ", "
				+ salesPrice + ", " + ratio + ", " + "''" + bus_code + "'', " + "''" + bus_name + "'', " + "10, "
				+ "''g'', " + "10, " + "''" + good_type + "'', " + "''1'', " + "''1'', " + "''1'', " + "''1'', "
				+ "''1'', " + "0, " + "0, " + "''" + posID + "'', " + "''IN''' ";

		/*
		 * query +=
		 * "Insert Into Goods ( BarCode, G_Name, Write_Date, Writer, Real_Sto)"
		 * + "Values ('" + barcode + "', '" + g_name + "', '" + currentDate +
		 * "', '" + userID + "',0) ";
		 * 
		 * query += " Update Goods Set G_Name = '" + g_name + "', Tax_YN = '" +
		 * tax_yn + "', Obtain = 0, Std_Size = '" + Std_Size +
		 * "', Con_Rate = 10, Unit = 'g', Std_Rate = 10, " + "Pur_Pri = " +
		 * pur_pri + ", Pur_Cost = " + pur_cost + ", Add_Tax = " + add_tax +
		 * ", Sell_Pri = " + salesPrice + ", Profit_Rate = " + ratio +
		 * ", Branch_Rate = 0, " + "SP_Check = '0', Com_Rate = 0, L_Code = '" +
		 * lcode + "', L_Name = '" + lname + "', M_Code = '" + mcode +
		 * "', M_Name = '" + mname + "', " + "S_Code = '" + scode +
		 * "', S_Name = '" + sname + "', " + "Bus_Code = '" + bus_code +
		 * "', Bus_Name = '" + bus_name +
		 * "', Sell_Pri1 = 0, Location = '', Goods_Use = '1', Pur_Use = '1', Ord_Use = '1', Sell_Use = '1', Sto_Use = '1', "
		 * + "Scale_Use = '" + good_type +
		 * "', Link_Use = '0', Link_BarCode = '' , Pro_Sto = 0, Sale_Pur = 0, Sale_Sell = 0, SaSP_Check = '0', SaCom_Rate = 0, Point_Use = '"
		 * + memberpoint + "', " +
		 * "Point_Mark = 0, CashBack_Use = '0', Sale_SDate = '', Sale_EDate = '', Sale_STime = '', Sale_ETime = '', Sale_PointUse = '1', AmountSale_Use = '0', "
		 * +
		 * "Sale_Amount = 0, Edit_Check = '1', Tran_Chk = '1', Sale_Use = '0', Edit_Date = '"
		 * + currentDate + "', Editor = '" + userID +
		 * "', ALL_PerDC_USE = '1', VAT_CHK = '" + vat_chk + "', " + g_grade +
		 * ", Bot_Code = '', Bot_Name = '', Bot_Pur = 0, Bot_Sell = 0, Col_Pri = 0, Re_Pri = 0 Where BarCode = '"
		 * + barcode + "' ";
		 * 
		 * query += "Update TempPurchase Set Goods_Use = '1' Where BarCode = '"
		 * + barcode + "' ";
		 * 
		 * query += "Select * From Goods where Barcode='" + barcode + "' ";
		 */

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
						Toast.makeText(PurchaseRegistActivity.this, res, Toast.LENGTH_SHORT).show();
						return;
					}

					String str = "신상품등록 완료";
					scanNewProductSpeak(str);
					doQueryWithBarcode();
				} catch (JSONException e) {
					Toast.makeText(PurchaseRegistActivity.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

	
	//매입전송
	/*private void transferPurPriList(){
		
		//목록에서 선택된 전표 전송
		//총 매입가 재 계산
		
		//목록에 거래처가 두개 이상 있는데 선택을 안했을 시에는
		//선택해달라는 메세지를 띄웁니다.
		//또는 전체 전송 할지의 여부를 묻습니다.		
		if(mfillMaps.size() <= 0){
			Toast.makeText(getApplicationContext(), "매입 목록이 없습니다.", Toast.LENGTH_SHORT).show();
			return;			
		}
		
		if(mfillMaps.size() == 1){
			//매입전표가 한장 입니다. 바로 전송 합니다.
			new AlertDialog.Builder(this).setTitle("매입 전송").setMessage("매입전표를 전송 하시겠습니까?")
			.setPositiveButton("예", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {					
					//매입전표 전송
					toSendPurList(0);
				}
			}).setNegativeButton("아니요", null).show();
			
			return;
		}
		
		if(m_selectedListIndex < 0 ){			
			//전송 할 전표를 선택해 주세요~!!
			Toast.makeText(getApplicationContext(), "전송할 전표를 목록에서 선택해 주세요~!", Toast.LENGTH_SHORT).show();
			return;		
		}
				
		//매입전표 전송
		toSendPurList(m_selectedListIndex);
		
	}
	*/
	
	//매입장 한장씩 전송하기
	private void toSendPurList(int num){
		
		final int select_num = num;
				
		HashMap<String, String> map = mfillMaps.get(select_num);
		String table_name = map.get("In_Date");
		table_name = table_name.replace("-", "").substring(0, 6);
		Log.d("테이블명", table_name);
		
		String query = "exec sp_executesql N'SELECT ISNULL(MAX(RIGHT(In_Num,3)),''000'') FROM InT_"+table_name+" WHERE In_Date=''"+map.get("In_Date")+"'' AND SUBSTRING(in_Num,9,1) =''"+posID+"''' ";
		
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
					Toast.makeText(PurchaseRegistActivity.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
		String in_date = map.get("In_Date");
		String junpyo_num = in_date.replace("-", "")+posID+gubun_num;
		Log.d("전표번호", junpyo_num);
		
		String query = "exec sp_executesql N'EXEC str_PDA_013I ''"+office_code+"'', ''"+in_date+"'', ''"+userID+"'', ''"+posID+"'', ''"+junpyo_num+"'''";
				
		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {
	
			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				
				try {
					String res = (String) results.getJSONObject(0).get("1");
					if (!"OK".equals(res)) {
						Toast.makeText(PurchaseRegistActivity.this, res, Toast.LENGTH_SHORT).show();					
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
							Toast.makeText(PurchaseRegistActivity.this, "전송완료", Toast.LENGTH_SHORT).show();
							return;
						}
						
						toSendPurList(m_selectedListIndex);						
						
					}else{
						//재조회합니다.
						m_selectedListIndex = -1;						
						getTempInDPDAList();
						Toast.makeText(PurchaseRegistActivity.this, "전송완료", Toast.LENGTH_SHORT).show();
					}
					
				} catch (JSONException e) {
				
					Toast.makeText(PurchaseRegistActivity.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
	
	
	
	
	// 2017-04 저장프로시서 사용하기로 인해 삭제됨
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		/*
		 * CheckBox checkbox = ((CheckBox)arg1.findViewById(R.id.item5));
		 * if(checkbox != null) { checkbox.setChecked(!checkbox.isChecked()); //
		 * 베리 킥 포인트 요거 꼭해줘야 checkbox 에서 바로 바로 적용됩니다.
		 * m_adapter.notifyDataSetChanged(); }
		 */

		//modify_position = 1;
		m_selectedListIndex = position;
				
		
		new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT).setTitle("매입전표").setMessage("작업을 선택해 주세요!")
		.setPositiveButton("전송", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {				
				//매입전표 전송			
				//진정 전송입니다.
				mAllSend = false;
				toSendPurList(m_selectedListIndex);
				
			}
		}).setNegativeButton("삭제", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub	
				
				new AlertDialog.Builder(PurchaseRegistActivity.this,AlertDialog.THEME_HOLO_LIGHT).setTitle("전표삭제").setMessage("매입전표를 삭제 하시겠습니까?")
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
				String in_date =  map.get("In_Date");
				String office_code = map.get("Office_Code");
				String office_name = map.get("Office_Name");
				String in_pri = map.get("Price_Sum").replace(",", "");
				String in_repri = "";
				String profit_pri = "";
				String profit_rate = "";

				Log.i("전표번호", in_num);

				Intent intent = new Intent(getApplicationContext(), PurchaseDetailActivity.class);
				intent.putExtra("In_Num", in_num);
				intent.putExtra("Office_Code", office_code);
				intent.putExtra("In_Date", in_date);
				intent.putExtra("Office_Name", office_name);
				intent.putExtra("In_Pri", in_pri);
				intent.putExtra("In_RePri", in_repri);
				intent.putExtra("Profit_Pri", profit_pri);
				intent.putExtra("Profit_Rate", profit_rate);
				startActivity(intent);
				
			}
		}).show();
		
		
		
		//넘겨서 자세히 보기 입니다.
				
		// 선택된 것 다시 불러 내서 또는 찾아서
		//m_tempProduct.putAll(mfillMaps.get(position));
		/*Log.i("상품정보", m_tempProduct.toString());

		m_textBarcode.setText(m_tempProduct.get("BarCode"));
		m_textProductName.setText(m_tempProduct.get("G_Name"));
		m_et_purchasePrice.setText(m_tempProduct.get("Pur_Pri"));
		m_et_purchaseCost.setText(m_tempProduct.get("Pur_Cost"));
		m_et_addTax.setText(m_tempProduct.get("Add_Tax"));
		m_et_salePrice.setText(m_tempProduct.get("Sell_Pri"));
		m_profitRatio.setText(m_tempProduct.get("Profit_Rate"));

		// 상품장 부가세 별도 포함 상품
		String m_vatchk = m_tempProduct.get("Tax_Gubun").toString();
		setTaxSeparate(m_tempProduct.get("Tax_YN").toString(), m_vatchk.toString());

		// 반품체크
		if (m_tempProduct.get("In_YN").toString().equals("0") || m_tempProduct.get("In_YN").toString().equals("2")) {
			m_checkBoxRejectedProduct.setChecked(true);
			m_amount.setText(String.valueOf(Math.abs(Integer.parseInt(m_tempProduct.get("In_Count")))));
		} else {
			m_checkBoxRejectedProduct.setChecked(false);
			m_amount.setText(m_tempProduct.get("In_Count"));
		}

		// 행사 상품 체크
		if (m_tempProduct.get("In_YN").toString().equals("2") || m_tempProduct.get("In_YN").toString().equals("3")) {
			m_goodsGubun.setText("행사");
			m_goodsGubun.setTextColor(Color.RED);
		}

		// 매입가 변경
		if (m_tempProduct.get("checkPurpri").toString().equals("1")) {
			m_checkBoxPurpri.setChecked(true);
		} else {
			m_checkBoxPurpri.setChecked(false);
		}
		// 판매가 변경
		if (m_tempProduct.get("checkSellpri").toString().equals("1")) {
			m_checkBoxSellpri.setChecked(true);
		} else {
			m_checkBoxSellpri.setChecked(false);
		}

		// 상품명 변경
		if (m_tempProduct.get("checkProductName").toString().equals("1")) {
			m_checkBoxProductName.setChecked(true);
		} else {
			m_checkBoxProductName.setChecked(false);
		}

		// 거래처 변경
		if (m_tempProduct.get("checkCustomer").toString().equals("1")) {
			m_checkBoxCustomer.setChecked(true);
		} else {
			m_checkBoxCustomer.setChecked(false);
		}

		m_amount.requestFocus();
		saveButton.setBackgroundResource(R.drawable.change_btn);
		saveButton.setTag("수정");*/

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

	}

	
	/**
	 * 부가세 별도 포함 선택 함수
	 * @param s s는 Tax_YN구분 
	 * @param p p는 VAT_CHK 구분
	 */
	private void setTaxSeparate(String s, String p) {

		Log.i("첫번째 매개변수", s.toString() + p.toString());
	
		if (s.equals("1")) {
			m_purpriTaxyn.setEnabled(true);
		} else {
			//면세상품 알림 표시
			scanNewProductSpeak("면세상품 입니다.");
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
	
	//저장된 매입목록을 불러옵니다.
	private void getTempInDPDAList(){
		
		//초기화
		mfillMaps.removeAll(mfillMaps);			
		m_adapter.notifyDataSetChanged();		
		//전표 조회 시 반품 버튼 초기화
		m_checkBoxRejectedProduct.setChecked(false);
		
		String query = "exec sp_executesql N'EXEC str_PDA_013Q ''"+posID+"'''";
				
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
						temp.put("In_Date", map.get("In_Date").toString());
						temp.put("Office_Code", map.get("Office_Code").toString());
						temp.put("Office_Name", map.get("Office_Name").toString());
						temp.put("nCount", map.get("nCount").toString());
						temp.put("Price_Sum", map.get("Price_Sum").toString());
						
						mfillMaps.add(temp);			
					}
					
				} catch (JSONException e) {
					Toast.makeText(PurchaseRegistActivity.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
	
	
	//매입장 전체전송 하기
	//초기화
	public void sendAlltempData(View view){
		
		int mCount = mfillMaps.size();
		
		if(mCount <= 0 ) return;
		
		//전체전송입니다.
		mAllSend = true;
		m_selectedListIndex = 0;
		toSendPurList(m_selectedListIndex);	
		
	}	
	
	
	//매입장 삭제
	private void transferDelete(){
		
		//선택 매입장
		/*if(mfillMaps.size() <= 0){
			Toast.makeText(this, "매입 목록이 없습니다.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		if(m_selectedListIndex < 0 ){
			Toast.makeText(this, "삭제할 전표를 선택해 주세요!", Toast.LENGTH_SHORT).show();
			return;
		}*/
		
		
		HashMap<String, String> map = mfillMaps.get(m_selectedListIndex);
		
		String office_code = map.get("Office_Code");
		String in_date = map.get("In_Date");
		
		String query = "exec sp_executesql N'EXEC str_PDA_013D ''"+office_code+"'', ''"+in_date+"'', ''"+posID+"'', ''DS'''";
		
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
						Toast.makeText(PurchaseRegistActivity.this, res, Toast.LENGTH_SHORT).show();
						return;
					}

					//재조회합니다.
					m_selectedListIndex = -1;
					getTempInDPDAList();
					Toast.makeText(PurchaseRegistActivity.this, "삭제 성공", Toast.LENGTH_SHORT).show();
										
					
				} catch (JSONException e) {
					Toast.makeText(PurchaseRegistActivity.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
			actionbar.setTitle("매입등록");

			getActionBar().setDisplayHomeAsUpEnabled(false);

		}
	}
	
	
	/*
	 * 유틸리티 시작
	 * 
	 * 
	 * 
	 */
	
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
	
	
	// 상품 스캔시 사운드
	private void scanResult() {
		SoundPool mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
		int beep = mSoundPool.load(getApplicationContext(), R.raw.windowsding, 1);
		// .play(사운드파일, 왼쪽볼륨, 오른쪽볼륨,
		mSoundPool.play(beep, 1.0f, 1.0f, 0, 0, 0.5f);
	}

	// 상품 스캔시 사운드
	/*
	 * @SuppressWarnings("deprecation")
	 * 
	 * @TargetApi(Build.VERSION_CODES.LOLLIPOP) public void scanResult(){
	 * 
	 * SoundPool.Builder builder = new SoundPool.Builder();
	 * builder.setMaxStreams(5); //SoundPool mSoundPool = new SoundPool(5,
	 * AudioManager.STREAM_MUSIC, 0); SoundPool mSoundPool = builder.build();
	 * 
	 * int beep = mSoundPool.load( getApplicationContext(), R.raw.windowsding,
	 * 1); //.play(사운드파일, 왼쪽볼륨, 오른쪽볼륨, mSoundPool.play(beep, 1.0f, 1.0f, 0, 0,
	 * 0.5f);
	 * 
	 * SoundPool mSoundPool; if (Build.VERSION.SDK_INT >=
	 * Build.VERSION_CODES.LOLLIPOP) { AudioAttributes audioAttributes = new
	 * AudioAttributes.Builder() .setUsage(AudioAttributes.USAGE_ALARM)
	 * .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION) .build();
	 * 
	 * mSoundPool = new SoundPool.Builder() .setMaxStreams(5)
	 * .setAudioAttributes(audioAttributes) .build(); } else { mSoundPool = new
	 * SoundPool(5, AudioManager.STREAM_MUSIC, 0); }
	 * 
	 * int beep = mSoundPool.load( getApplicationContext(), R.raw.windowsding,
	 * 1); //.play(사운드파일, 왼쪽볼륨, 오른쪽볼륨, mSoundPool.play(beep, 1.0f, 1.0f, 0, 0,
	 * 0.5f); }
	 */

	// 신상품 등록시 신상품 등록 메세지 표시하기
	private void scanNewProductSpeak(String str) {
		final String sperk = str;
		tts = new TextToSpeech(this, new OnInitListener() {

			@Override
			public void onInit(int status) {
				// TODO Auto-generated method stub
				// TextToSpeech 엔진의 초기화가 완료되어 사용할 수 있도록 준비된 상태인 경우

				if (status == TextToSpeech.SUCCESS) {

					// 음성 합성하여 출력하기위한 언어를 Locale.US 로 설정한다.
					// 안드로이드 시스템의 환경 설정에서도 동일한 언어가 선택되어 있어야만
					// 해당 언어의 문장이 음성변환 될 수 있다.
					int result = tts.setLanguage(Locale.KOREA);
					tts.speak(sperk, TextToSpeech.QUEUE_FLUSH, null);
					/*
					 * // 해당 언어에 대한 데이터가 없거나 지원하지 않는 경우 if (result ==
					 * TextToSpeech.LANG_MISSING_DATA || result ==
					 * TextToSpeech.LANG_NOT_SUPPORTED) { // 해당 언어는 사용할 수 없음을
					 * 알린다. Toast.makeText(PurchaseRegistActivity.this,
					 * "해당 언어를 사용할수가 없습니다.", Toast.LENGTH_SHORT).show(); }
					 */
					// TextToSpeech 엔진 초기화에 실패하여 엔진이 TextToSpeech.ERROR 상태인 경우
				}

			}
		});

	}

	//2017-04 저장프로시저 변경
	// 종료시 전표수정중 삭제 하면 안됩니다.~
	/*@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		String msg = " 예를 누르시면 저장 되어 있던 모든 매입 자료가 삭제 됩니다. 종료 하시겠습니까? ";
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (!mfillMaps.isEmpty()) {
				new AlertDialog.Builder(this).setTitle("종료").setMessage(msg.toString())
						.setPositiveButton("예", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								finish();
							}
						}).setNegativeButton("아니요", null).show();
				return false;
			} else {
				finish();
			}
		default:
			return false;
		}
	}*/
	
	//2017-04 m3mobile 추가 
	@Override
	protected void onResume() {
	    super.onResume();
	    if(m3Mobile){
	    	M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textBarcode, m_amount);
		    M3MoblieBarcodeScanBroadcast.setOnResume();  
	    }	    
	}
	
	//2017-04 m3mobile 추가
	@Override
	protected void onDestroy() {		
		super.onDestroy();		
		if(m3Mobile){			
			M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textBarcode, m_amount);
			M3MoblieBarcodeScanBroadcast.setOnDestory();
		}		
	}

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.purchase_regist, menu);
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
