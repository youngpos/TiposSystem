package tipsystem.tips;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.M3MoblieBarcodeScanBroadcast;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.MSSQL4;
import tipsystem.utils.StringFormat;
import tipsystem.utils.UserPublicUtils;

public class ManageDomeSaleRegActivity extends Activity implements OnClickListener, OnItemClickListener{
	
String TAG = "출고 등록관리";
	
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
		
	// 전표번호 생성
	String posID = "P";
	String userID = "1";

	/* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
	int m_KeypadType = 0;

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
	HashMap<String,String> temp_goods = new HashMap<String, String>(); // 상품정보 저장
	
	
	//거래처 정보
	TextView edittext_OfficeCode;
	TextView edittext_OfficeName;
	
	TextView textview_salegradetype1;
	TextView textview_salegradetype2;
	TextView textview_salegradetype3;
			
	//상품정보
	TextView textview_goodsInfo;
	EditText edittext_Barcode;
	EditText edittext_Gname;
	EditText edittext_SellPri;
	EditText edittext_Amount;
	
	//수량고정
	CheckBox checkbox_CFix;
	
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
	
	String mOffice_Code;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_domesaleregmanager);
		
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

			/* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
			m_KeypadType = LocalStorage.getInt(this, "KeypadType:" + OFFICE_CODE);

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
	}
	
	private void start(){
		
		Intent intent = getIntent();
		 			
		String office_code = intent.getStringExtra("거래처코드");
		String office_name = intent.getStringExtra("거래처명");
		
		if("".equals(office_code)){
			Toast.makeText(mContext, "거래처를 선택해 주세요!!", Toast.LENGTH_SHORT).show();
			finish();
			return;
		}
		
		edittext_OfficeCode.setText(office_code);
		edittext_OfficeName.setText(office_name);
		
		//거래처코드를 입력합니다.
		mOffice_Code = office_code;
		
		//거래처정보를 가져 옵니다.
		getOfficeInfo(office_code);
		
		//견적서목록에서 거래처코드 기준으로 등록되있던 상품정보목록을 호출합니다.
		getHomeDileveryList(0);
		
	}
		
	private void init(){
		
		//거래처정보
		edittext_OfficeCode = (TextView)findViewById(R.id.textViewOfficeCode);	//거래처코드
		edittext_OfficeName = (TextView)findViewById(R.id.textViewOfficeName);	//거래처명
		
		textview_salegradetype1 = (TextView)findViewById(R.id.salegradetype1);
		textview_salegradetype2 = (TextView)findViewById(R.id.salegradetype2);
		textview_salegradetype3 = (TextView)findViewById(R.id.salegradetype3);
				
		//상품정보
		textview_goodsInfo = (TextView)findViewById(R.id.textview_goodsinfo);
		edittext_Barcode = (EditText)findViewById(R.id.editTextBarcode);

		// 김영목 2020.09.14. 키패드입력 옵션 설정에 따라 적용 추가
		if (m_KeypadType == 0) { // 숫자키패드
			edittext_Barcode.setInputType(2);
		}
		if (m_KeypadType == 1) { // 문자키패드
			edittext_Barcode.setInputType(145); //textVisiblePassword ) 0x00000091
		}

		edittext_Gname = (EditText)findViewById(R.id.editTextProductName);
		edittext_SellPri = (EditText)findViewById(R.id.editTextSalePrice);
		edittext_Amount = (EditText)findViewById(R.id.editTextAmount);
				
		checkbox_CFix = (CheckBox)findViewById(R.id.checkBox_amountfix);	//수량고정
		
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
		Button button_Save = (Button)findViewById(R.id.buttonSave);
		Button button_Scan = (Button)findViewById(R.id.buttonScan);
				
		button_Search.setOnClickListener(this);
		button_Renew.setOnClickListener(this);
		button_Save.setOnClickListener(this);
		button_Scan.setOnClickListener(this);
		
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
					edittext_Amount.setText("");
				}
			}
		});
		
		edittext_Barcode.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(!hasFocus){
					upu.setChangeBackGroundColor(0, edittext_Barcode);
					
					String barcode = edittext_Barcode.getText().toString();
					if("".equals(barcode)){
						return;
					}
					doQueryWithBarcode();
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
					if(checkbox_CFix.isChecked()){	//체크여부 확인
						//수량 1개로 강제 등록 합니다.
						
						if(!onCheckData()){
			    			return;
			    		}  
						
						doSend();		
					}
					
					upu.setChangeBackGroundColor(1, edittext_Amount);
				}else{
					upu.setChangeBackGroundColor(0, edittext_Amount);
				}
			}
		});
		
		edittext_Amount.setImeOptions(EditorInfo.IME_ACTION_DONE);
		edittext_Amount.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if(actionId == EditorInfo.IME_ACTION_DONE)
                {
                	if(!onCheckData()){
		    			return false;
		    		}

					doSend();
                    return true;

                }
                return false;
            }
        });

		/*edittext_Amount.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				
				if(event.getAction() == KeyEvent.ACTION_DOWN){				
					if(keyCode == event.KEYCODE_ENTER || keyCode == EditorInfo.IME_ACTION_DONE){
						
						if(!onCheckData()){
			    			return false;
			    		}  
						
						doSend();
					}
				}
				return false;
			}
		});*/
				
		if(m3Mobile){			
			M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, edittext_Barcode, edittext_Amount);				
		}
		
		//저울상품 정보셋팅
		posSet();
		
	}
	
	//거래처 정보를 불러와서 정보창에 표시합니다.
	private void getOfficeInfo(String officecode){
		
		//거래처 코드
		String office_code = officecode;
		
		String query = "Select Office_Code, Office_Name, Office_Sec, Office_Use, Lately_Use, Dir_Office, "
						+ "case When Cost_Grade <> '' Then Cost_Grade Else '1' End Cost_Grade, (Select isnull(Max(Ds_Date), '최초등록') From SdT_Total Where Office_Code='"+office_code+"')  as Last_VisDate "
						+ "From Office_Manage Where Office_Code='"+office_code+"' ";
		
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
						
						HashMap<String, String > temp_office = JsonHelper.toStringHashMap(results.getJSONObject(0));												
						
						//직영 체크
						if("1".equals(temp_office.get("Dir_Office"))){
							edittext_OfficeName.setText("[직영] "+temp_office.get("Office_Name"));
						}
						
						//등급
						int grade = Integer.parseInt(temp_office.get("Cost_Grade"));
						switch(grade){
							case 1:	//판매가
								textview_salegradetype1.setText("판매가");
								break;
							case 3:	//A등급가
								textview_salegradetype1.setText("A판가");
								break;
							case 4:	//B등급가
								textview_salegradetype1.setText("B판가");
								break;
							case 5:	//C등급가
								textview_salegradetype1.setText("C판가");
								break;
						}
						
						//최근판가
						String lately_use = temp_office.get("Lately_Use");
						if("1".equals(lately_use)){
							textview_salegradetype2.setText("최근판가");
							textview_salegradetype2.setTextColor(Color.GREEN);
						}
						
						//최근 방문일
						textview_salegradetype3.setText(temp_office.get("Last_VisDate"));
						
												
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
	
	//저장
	public void doSend(){
				
		//상품정보를 추출합니다.
		String office_code = edittext_OfficeCode.getText().toString();
		String barcode = temp_goods.get("Barcode");
		String g_name = temp_goods.get("G_Name");
		String std_size = temp_goods.get("Std_Size");
		String pur_pri = temp_goods.get("Pur_Pri");
		String sell_pri = edittext_SellPri.getText().toString();
		String amount = edittext_Amount.getText().toString();
		String tax_yn = temp_goods.get("Tax_YN");
		String vat_chk = temp_goods.get("Vat_Chk");

		/* 김영목 2020.08.05. eSEQ 추가
		String query = "Insert Into EmD_Total(Order_Num, Barcode, G_Name, Std_Size, Pur_Pri, Sell_Pri, Tax_YN, Or_Count, Bigo, Vat_Chk) " 
						+ "Select '"+office_code+"', '"+barcode+"', '"+g_name+"', "
						+ "'"+std_size+"', "+pur_pri+", "+sell_pri.replace(",", "")+", '"+tax_yn+"', "+amount+", "
						+ "isnull(Max(bigo)+1, 1), '"+vat_chk+"' From EmD_Total Where Order_Num='"+office_code+"' ";
		 */

		/* 김영목 2021.05.31. pos_no 컬럼 추가(posID) 추가 */
/*
		String query = "Insert Into EmD_Total(Order_Num, Barcode, G_Name, Std_Size, Pur_Pri, Sell_Pri, Tax_YN, Or_Count, Bigo, Vat_Chk, eSEQ) "
				+ "Select '"+office_code+"', '"+barcode+"', '"+g_name+"', "
				+ "'"+std_size+"', "+pur_pri+", "+sell_pri.replace(",", "")+", '"+tax_yn+"', "+amount+", "
				+ "isnull(Max(bigo)+1, 1), '"+vat_chk+"', isnull(Max(eSEQ)+1, 1) From EmD_Total Where Order_Num='"+office_code+"' ";
*/

		String query = "Insert Into EmD_Total(Order_Num, Barcode, G_Name, Std_Size, Pur_Pri, Sell_Pri, Tax_YN, Or_Count, Bigo, Vat_Chk, eSEQ, pos_no) "
				+ "Select '"+office_code+"', '"+barcode+"', '"+g_name+"', "
				+ "'"+std_size+"', "+pur_pri+", "+sell_pri.replace(",", "")+", '"+tax_yn+"', "+amount+", "
				+ "isnull(Max(bigo)+1, 1), '"+vat_chk+"', isnull(Max(eSEQ)+1, 1), '" + posID + "' "
		        + "From EmD_Total Where Order_Num='"+office_code+"' ";

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

				//----------------------------------------//
				//2021.01.11.김영목. 조회 기능 추가
				//----------------------------------------//
				//getHomeDileveryList(1);
				getHomeDileveryList_Add(1);
				//----------------------------------------//

				//----------------------------------------//
				//2021.01.11.김영목. 0.5초 정도 딜레이
				//----------------------------------------//
				//Log.d(TAG, "견적서상품 등록 결과 : "+results);
				//onReNew();
				//new Handler().postDelayed(new Runnable() {
				//	@Override
				//	public void run() {
				//		onReNew();
				//	}
				//},500); // 0.5초 정도 딜레이를 준 후 시작
				//----------------------------------------//
				//Toast.makeText(getApplicationContext(), "저장완료.", Toast.LENGTH_SHORT).show();
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
		query = "Select  Top 1 a.Barcode, a.G_Name, a.Std_Size,  a.Pur_Pri, " 
				+ "Case When Dir_Office = 1 Then "
				+ "		Case  When a.Sale_Use='1' And isnull(a.Sale_Pur, 0) > 0 Then a.Sale_Pur " 
				+ "		When isnull(a.Pur_Pri, 0) > 0 Then a.Pur_Pri Else 0 End "
				+ "	When isnull(b.Fix_Pri, 0) > 0 Then b.Fix_Pri "  
				+ "	When ( c.Lately_Use = '1' And isnull(d.Sell_Pri, 0) > 0 )Then d.Sell_Pri " 
				+ "	When ( c.Cost_Grade = '3' and isnull(a.Sell_APri, 0) > 0 ) Then a.Sell_APri "  
				+ "When ( c.Cost_Grade = '4' and isnull(a.Sell_BPri, 0) > 0 ) Then a.Sell_BPri "  
				+ "When ( c.Cost_Grade = '5' and isnull(a.Sell_CPri, 0) > 0 ) Then a.Sell_CPri "  
				+ "When isnull(a.Sell_Pri, 0) > 0 Then a.Sell_Pri "  
				+ "Else 0 "  
				+ "End Sell_Pri, " 
				+ "Case When Dir_Office = 1 Then "
				+ "		Case  When a.Sale_Use='1' And isnull(a.Sale_Sell, 0) > 0 Then '직영세일가' "
				+ "		When isnull(a.Pur_Pri, 0) > 0 Then '직영판매가' Else '직영임시판가' End "
				+ "When isnull(b.Fix_Pri, 0) > 0 Then '고정가' "  
				+ "When ( c.Lately_Use = '1' And isnull(d.Sell_Pri, 0) > 0 )Then '최근판가' "
				+ "When ( c.Cost_Grade = '3' and isnull(a.Sell_APri, 0) > 0 ) Then 'A판매가' "  
				+ "When ( c.Cost_Grade = '4' and isnull(a.Sell_BPri, 0) > 0 ) Then 'B판매가' "
				+ "When ( c.Cost_Grade = '5' and isnull(a.Sell_CPri, 0) > 0 ) Then 'C판매가' "
				+ "When isnull(a.Sell_Pri, 0) > 0 Then '판매가' "
				+ "Else '임시판가' "  
				+ "End Sell_Name, a.Tax_YN, a.Vat_Chk, a.Sale_Use "
				+ "From Goods a Left Join Evt_FixPri b "
				+ "On a.Barcode=b.Barcode Left Join Office_Manage c "
				+ "On c.Office_Code='"+mOffice_Code+"' Left Join SdD_Total d "
				+ "On d.Barcode='"+barcode+"' And d.Office_Code='"+mOffice_Code+"' "
				+ "Where a.Barcode='"+barcode+"' "
				+ "Order By InDate DESC; ";
		
		
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
						
						temp_goods = JsonHelper.toStringHashMap(results.getJSONObject(0));												
						edittext_Gname.setText(temp_goods.get("G_Name"));
						
						if("1".equals(temp_goods.get("Scale_Use"))){
							textview_goodsInfo.setText("상품정보 [저울상품]");
							edittext_Amount.requestFocus();
							return;
						}
						
						String sale_use = temp_goods.get("Sale_Use");
						String sale_name = "상품정보 "; //상품가격 정보
						
						sale_name += "["+temp_goods.get("Sell_Name")+"] ";
						
						//행사 상품체크		
						if( "1".equals(sale_use)){							
							sale_name += "[행사]";
						}else{
							sale_name += "[일반]";						
						}
						
						//상품정보
						textview_goodsInfo.setText(sale_name);						
						edittext_SellPri.setText(StringFormat.convertToNumberFormat(temp_goods.get("Sell_Pri")));						
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
	
	//유효성 체크
	public boolean onCheckData(){
		
		String barcode = edittext_Barcode.getText().toString();
		String g_name = edittext_Gname.getText().toString();
		String sell_pri = edittext_SellPri.getText().toString();
		String amount = edittext_Amount.getText().toString();
		
				
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
			//----------------------------------------//
			// 2022.12.26. 출고가 소수점 입력 에러 수정
			//----------------------------------------//
			/*
			int a = Integer.parseInt(sell_pri);
			if(a <= 0){
				Toast.makeText(mContext, "판매가를 입력해 주세요~!", Toast.LENGTH_SHORT).show();
				edittext_SellPri.requestFocus();
				return false;
			}
			*/
			//----------------------------------------//
			if (sell_pri.equals("")){
				Toast.makeText(mContext, "판매가를 입력해 주세요~!", Toast.LENGTH_SHORT).show();
				edittext_SellPri.requestFocus();
				return false;
			}

			Float a = Float.parseFloat(sell_pri);
			if(a <= 0){
				Toast.makeText(mContext, "판매가를 입력해 주세요~!", Toast.LENGTH_SHORT).show();
				edittext_SellPri.requestFocus();
				return false;
			}
			//----------------------------------------//

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
				
		//상품정보 초기화
		temp_goods.clear();
		
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
	
	//목록 초기화	
	private void setReNewList(){		
		mfillMaps.clear();		
		textview_TCount.setText("0");		
	}
	
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
	
	//견적상품 목록 조회
	private void getHomeDileveryList(final int onNew ){
		
		//목록 초기화
		setReNewList();
		
		//거래처코드로 조회
		String office_code = edittext_OfficeCode.getText().toString();		
		if("".equals(office_code)) return;

		/* 김영목 2021.05.31. pos_no 컬럼 추가(posID) 추가 */
//		String query = "Select * From EmD_Total Where Order_Num='"+office_code+"' Order By Bigo";
//		String query = "Select * From EmD_Total Where Order_Num='"+office_code+"' AND pos_no = '" + posID + "' Order By Bigo";
		String query = "Select * From EmD_Total Where Order_Num='"+office_code+"' AND pos_no = '" + posID + "' Order By eSeq";

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
				// 여기서 좀 쉬고...

				//처리결과
				if(results.length() > 0){

					try{

						for(int i=0; i < results.length(); i++){

							JSONObject map = results.getJSONObject(i);

							HashMap<String, String> temp = new HashMap<String, String>();
							temp.put("고유값", map.get("Bigo").toString());
							temp.put("바코드", map.get("Barcode").toString());
							temp.put("상품명", map.get("G_Name").toString());
							temp.put("매입가", map.get("Pur_Pri").toString());
							temp.put("판매가", map.get("Sell_Pri").toString());
							temp.put("수량", map.get("Or_Count").toString());

							mfillMaps.add(temp);
						}

						//총상품수량
						textview_TCount.setText(mfillMaps.size()+"");
						Toast.makeText(getApplicationContext(), "등록 상품 조회 성공", Toast.LENGTH_SHORT).show();

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}else{
					Toast.makeText(getApplicationContext(), "등록된 상품이 없습니다.", Toast.LENGTH_SHORT).show();
				}

				m_adapter.notifyDataSetChanged();

				//2021.01.12.김영목. 재등록시 새입력으로 이동 추가
				// 스레드 때문에 여기서 안하면 안되는 문제
				if(onNew == 1){
					onReNew();
				}
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

	// 2022.11.02. 목록조회 시간걸림이 에러 원인인 듯 하여 리스트에 추가로 변경 처리
	private void getHomeDileveryList_Add(final int onNew ){

		//String idx =  String.format("%d", mfillMaps.size()+1); //에러 위험해서 삭제한다
		// 마지막 행의 고유값을 얻어서 1 증가후 새 고유값을 얻는다
		String idx="";

		int idxNew = 1;	//최초시
		/*
		mfillMaps.size() = 4
		listview1	position = 0
		listview2   position = 1
		listview3   position = 2
		listview4   position = 3
		 */
		//HashMap<String, String> map = mfillMaps.get(idxNew);
		int lastRow = mfillMaps.size();

		if (lastRow > 0){
			int pos = lastRow-1;
			HashMap<String, String> map = mfillMaps.get(pos);
			idx = map.get("고유값");
			idxNew = Integer.parseInt(idx)+1;
		}

		idx =  String.format("%d", idxNew);

		String barcode = temp_goods.get("Barcode");
		String g_name = temp_goods.get("G_Name");
		String pur_pri = temp_goods.get("Pur_Pri");
		String sell_pri = edittext_SellPri.getText().toString();
		String amount = edittext_Amount.getText().toString();
		HashMap<String, String> temp = new HashMap<String, String>();

		temp.put("고유값", idx);	//???
		temp.put("바코드",  barcode);
		temp.put("상품명", g_name);
		temp.put("매입가", pur_pri);
		temp.put("판매가", sell_pri );
		temp.put("수량", amount);

		mfillMaps.add(temp);

		m_adapter.notifyDataSetChanged();

		//총상품수량
		textview_TCount.setText(mfillMaps.size()+"");

		//2021.01.12.김영목. 재등록시 새입력으로 이동 추가
		// 스레드 때문에 여기서 안하면 안되는 문제
		if(onNew == 1){
			onReNew();
		}
	}

	//견적서 생성 
	public void onAllDataTrans(View v){
		
		if(mfillMaps.size() <= 0){
			Toast.makeText(this, "등록된 상품이 없거나 조회를 해주세요", Toast.LENGTH_LONG).show();
			return;
		}

		// 한번은 물어보자
		AlertDialog.Builder alert_builder = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
		alert_builder.setMessage("전체 자료를 전송하시겠습니까?")
				.setCancelable(false)
				.setPositiveButton("네", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						AllDataTrans();					}
				})
				.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialog.cancel();
					}
				});
		AlertDialog alert = alert_builder.create();
		alert.setTitle("자료전송");
		alert.show();


		
//		String query = "Select isnull(Max(right(order_num, 3)), '000') as 최종전표 "
//						+ "From EmT_Total "
//						+ "Where Write_Date = Convert(Char(23), getdate(), 23) ";
//
//		// 로딩 다이알로그
//		dialog = new ProgressDialog(this);
//		dialog.setMessage("Loading....");
//		dialog.show();
//
//		// 콜백함수와 함께 실행
//		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {
//
//			@Override
//			public void onRequestCompleted(JSONArray results) {
//				dialog.dismiss();
//				dialog.cancel();
//
//				//처리결과
//				if(results.length() > 0){
//
//					try {
//						JSONObject obj = results.getJSONObject(0);
//						String mlastjp = (String) obj.get("최종전표");
//
//						//신규번호 생성
//						String num = upu.getTodayData().replace("-", "")+posID+upu.getJunPyoNumStyle(mlastjp);
//						Log.d(TAG, "생성 전표 번호"+num);
//
//						//전표생성 및 임시데이터 삭제합니다.
//						setDataEMTTotal(num);
//
//					} catch (JSONException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//				}
//			}
//
//			@Override
//			public void onRequestFailed(int code, String msg) {
//				dialog.dismiss();
//				dialog.cancel();
//				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
//			}
//
//		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

	}
	private void AllDataTrans(){
		String query = "Select isnull(Max(right(order_num, 3)), '000') as 최종전표 "
				+ "From EmT_Total "
				+ "Where Write_Date = Convert(Char(23), getdate(), 23) ";

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
						Log.d(TAG, "생성 전표 번호"+num);

						//전표생성 및 임시데이터 삭제합니다.
						setDataEMTTotal(num);

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
	
	//견적서 이동
	private void setDataEMTTotal(String num){
		
		//전표번호
		String order_num = num;
		
		//목록에 있는 모든 상품을 견적서로 생성합니다.
		//총 상품판매가, 총 상품매입가

        /* 김영목 2021.05.31. pos_no 컬럼 추가(posID) 추가
/*
		String query = "Insert Into EmT_Total(Order_Num, Office_Code, TSell_Pri, TPur_Pri, Write_Date, Writer) "
						+ "Select  '"+order_num+"', "
						+ "'"+mOffice_Code+"', "
						+ "Sum(Sell_Pri*Or_Count), "
						+ "Sum(Pur_Pri*Or_Count), "
						+ "Convert(Char(23), getdate(), 23), "
						+ "'"+posID+"' "						
						+ "From EmD_Total Where Order_Num='"+mOffice_Code+"'; ";
*/
		String query = "Insert Into EmT_Total(Order_Num, Office_Code, TSell_Pri, TPur_Pri, Write_Date, Writer) "
				+ "Select  '"+order_num+"', "
				+ "'"+mOffice_Code+"', "
				+ "Sum(Sell_Pri*Or_Count), "
				+ "Sum(Pur_Pri*Or_Count), "
				+ "Convert(Char(23), getdate(), 23), "
				+ "'"+posID+"' "
				+ "From EmD_Total Where Order_Num='" + mOffice_Code + "' AND pos_no = '" + posID + "'; ";

/*
		query += "Update EmD_Total Set Order_Num='"+num+"', Bigo='' Where Order_Num='"+mOffice_Code+"'; ";
*/
		query += "Update EmD_Total Set Order_Num='"+num+"', Bigo='' Where Order_Num='"+ mOffice_Code + "' AND pos_no = '" + posID + "'; ";

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
				
				Log.d(TAG, "출고 등록 결과 : "+results);
				
				//목록 초기화 및 데이터 삭제
				setReNewList();	
				m_adapter.notifyDataSetChanged();
				
							
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
	
	//목록 삭제
	private void onDataDelete(int position){
		
		final int pos = position;
		
		HashMap<String, String> map = mfillMaps.get(pos);
		
		String idx = map.get("고유값");
        /* 김영목 2021.05.31. pos_no 컬럼 추가(posID) 추가 */
//		String query = "Delete From EmD_Total Where Order_Num='"+mOffice_Code+"' And Bigo='"+idx+"' ";
		String query = "Delete From EmD_Total Where Order_Num='"+mOffice_Code+"' And Bigo='"+idx+"' AND pos_no = '" + posID + "' ";

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
		if(mfillMaps.size() <= 0){
			Toast.makeText(this, "등록된 상품이 없습니다.", Toast.LENGTH_LONG).show();
			return;
		}

		// 한번은 물어보자
		AlertDialog.Builder alert_builder = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
		alert_builder.setMessage("전체 자료를 삭제하시겠습니까?")
				.setCancelable(false)
				.setPositiveButton("네", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						AllDataDelete();					}
				})
				.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						dialog.cancel();
					}
				});
		AlertDialog alert = alert_builder.create();
		alert.setTitle("전체삭제");
		alert.show();

//		HashMap<String, String> map = mfillMaps.get(0);
////		String query = "Delete From EmD_Total Where Order_Num='"+mOffice_Code+"' ";
//		String query = "Delete From EmD_Total Where Order_Num='"+mOffice_Code+"' AND pos_no = '" + posID + "' ";
//
//		// 로딩 다이알로그
//		dialog = new ProgressDialog(this);
//		dialog.setMessage("Loading....");
//		dialog.setCancelable(false);
//		dialog.show();
//
//		// 콜백함수와 함께 실행
//		new MSSQL4(new MSSQL4.MSSQL4CallbackInterface() {
//
//			@Override
//			public void onRequestCompleted(Integer results) {
//				dialog.dismiss();
//				dialog.cancel();
//
//				Log.d(TAG, "삭제 결과 : "+results);
//
//				//원 상품을 목록에서 삭제해 줍니다.
//				mfillMaps.clear();
//
//				//총 수량재입력
//				textview_TCount.setText("0");
//				m_adapter.notifyDataSetChanged();
//
//				Toast.makeText(getApplicationContext(), "삭제완료.", Toast.LENGTH_SHORT).show();
//			}
//
//			@Override
//			public void onRequestFailed(int code, String msg) {
//				dialog.dismiss();
//				dialog.cancel();
//				Toast.makeText(getApplicationContext(), "삭제실패(" + String.valueOf(code) + "):" + msg, Toast.LENGTH_SHORT)
//						.show();
//			}
//
//		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
		
		
	}

	private void AllDataDelete(){
		HashMap<String, String> map = mfillMaps.get(0);
//		String query = "Delete From EmD_Total Where Order_Num='"+mOffice_Code+"' ";
		String query = "Delete From EmD_Total Where Order_Num='"+mOffice_Code+"' AND pos_no = '" + posID + "' ";

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

		/* 김영목 2021.05.31. pos_no 컬럼 추가(posID) 추가 */
//		String query = "Update EmD_Total Set Or_Count='"+count+"' Where Order_Num='"+mOffice_Code+"' And Bigo='"+idx+"' ";
		String query = "Update EmD_Total Set Or_Count='"+count+"' Where Order_Num='"+mOffice_Code+"' And Bigo='"+idx+"' AND pos_no = '" + posID + "' ";

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
            //견적상품 목록 검색
        	Log.d(TAG, "출고상품 목록조회");
        	getHomeDileveryList(0);
        	
        	upu.hideSoftKeyboard(false);
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		
		final int pos = position;
		
		HashMap<String, String> map = mfillMaps.get(pos);
		final String count = map.get("수량");

		//----------------------------------------//
		// 2022.01.27. 팝업창 수량 잘 안보임 테마 수정
		//----------------------------------------//
		//AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
		AlertDialog.Builder ad = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
		//----------------------------------------//
	
		ad.setTitle("출고 상품 목록");       // 제목 설정
		ad.setMessage("수량변경 및 삭제");   // 내용 설정
	
		// EditText 삽입하기
		final TextView ev = new TextView(getApplicationContext());
		ev.setText("수량변경");
		final EditText et = new EditText(getApplicationContext());

		//----------------------------------------//
		// 2022.01.27. 팝업창 수량 잘 안보임 테마 수정
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
		        		Toast.makeText(mContext, "매입 수량을 0개 이상 입력해 주세요!", Toast.LENGTH_SHORT).show();
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
