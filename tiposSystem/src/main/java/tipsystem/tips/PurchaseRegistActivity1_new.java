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
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.M3MoblieBarcodeScanBroadcast;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.MSSQL4;
import tipsystem.utils.StringFormat;
import tipsystem.utils.UserPublicUtils;


// 매입등록
public class PurchaseRegistActivity1_new extends Activity implements OnDateSetListener {

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

    /* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
    int m_KeypadType = 0;

    DatePicker m_datePicker;
    SimpleDateFormat m_dateFormatter;
    Calendar m_dateCalender1;

    //ListView m_listReadyToSend; 2018-08-07 : 목록보기에서 정보표시로 변경합니다.
    //SimpleAdapter m_adapter;

    Button m_period; //매입일
    Button m_setchangedate; //매입일 변경 버튼

    Button m_customerChange; //거래처 변경 불가

    EditText m_customerCode; //거래처코드
    EditText m_customerName; //거래처명

    EditText m_textBarcode;    //바코드
    EditText m_textProductName;    //상품명
    Button m_bt_keyboardUp;            //키보드 띄우기 버튼

    EditText m_et_purchasePrice;    //매입가
    EditText m_et_purchaseCost;    //매입원가
    EditText m_et_addTax;            //부가세
    CheckBox m_purpriTaxyn;        //과세상품 부가세 포함 별도 옵션
    EditText m_amount;                //수량
    EditText m_profitRatio;            //이익률
    EditText m_et_salePrice;        //판매가
    CheckBox m_checkBoxRejectedProduct; //반품

    TextView m_textviewStock;        //재고고

    TextView m_goodsGubun; // 행사상품 표시

    //----------------------------------------//
    // 김영목 2020.12.01. 매입시 유효기간 추가
    //----------------------------------------//
    LinearLayout indExpiredLinearLayout; // 유효기간 레이어(안보이기 기본)
    EditText indExpiredNumEditText; // 유효기간 입력박스
    TextView indExpiredGubnTextView; // 유효기간 단위 표시

    //private boolean expired = false;
    private String indExpiredYn = "";
    private String indExpiredNum = "";
    private String indExpiredGubn = "";
    //----------------------------------------//

    HashMap<String, String> m_tempTotalInfo = new HashMap<String, String>();//매입잡고 있는 거래처의 총 매입 정보
    HashMap<String, String> m_tempProduct = new HashMap<String, String>(); // 현재 불러온 상품정보

    Context mContext;
    //2017-04 m3mobile
    SharedPreferences pref;
    boolean m3Mobile;
    //boolean mAllSend;

    boolean m_customerFix = false;
    int m_junpyoInTIdx = 1;

    //신규 미등록 상품 등록여부
    boolean m_newproduct = false;

    // 수정모드시 포지션 저장하기
    //int modify_position = 0; // 신규 등록시 0, 목록에 불러 진것 수정하면 1,  2017-04

    //매입목록 선택 포지션
    //int m_selectedListIndex = -1;

    //이전 거래처 데이터
    String[] mOfficeData = {"", ""};

    String m_textGname = ""; // 상품명 변경 체크 위해서

    Button saveButton;                //저장/수정 버튼

    // 2023.01.11. 거래처코드 고정 임시 추가
    CheckBox m_checkBoxOfficeFix; //거래처코드 고정

    // 옵션 정보
    String m_changeOption;
    String m_newProductOption;

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_regist1_new);

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

            //----------------------------------------//
            // 김영목 2020.12.01. 매입시 유효기간 추가
            //----------------------------------------//
            indExpiredYn = m_userProfile.getString("ind_expired_yn");
            indExpiredGubn = m_userProfile.getString("ind_expired_gubun");
            //----------------------------------------//

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 환경설정 관련 옵션 아이템 불러오기
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        m_newProductOption = pref.getString("prefProductMethod", "");
        m3Mobile = pref.getBoolean("m3mobile", false);

        dialog = new ProgressDialog(this);

        //매입목록 리스트
        //m_listReadyToSend = (ListView) findViewById(R.id.listviewReadyToSendList);

        //목록 리스트
        //String[] from = new String[] { "index", "In_Date", "Office_Code", "Office_Name", "nCount", "Price_Sum" };
        //int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5 };
        //m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_item4_15, from, to);
        //m_listReadyToSend.setAdapter(m_adapter);

        m_period = (Button) findViewById(R.id.buttonSetDate1);
        m_setchangedate = (Button) findViewById(R.id.buttonChangeDate);

        m_customerChange = (Button) findViewById(R.id.buttonCustomerChange);

        m_customerCode = (EditText) findViewById(R.id.editTextCustomerCode);
        m_customerName = (EditText) findViewById(R.id.editTextCustomerName);
        m_textBarcode = (EditText) findViewById(R.id.editTextBarcode);

        // 김영목 2020.09.14. 키패드입력 옵션 설정에 따라 적용 추가
        if (m_KeypadType == 0) { // 숫자키패드
            m_textBarcode.setInputType(2);
        }
        if (m_KeypadType == 1) { // 문자키패드
            m_textBarcode.setInputType(145); //textVisiblePassword ) 0x00000091
        }

        //----------------------------------------//
        // 김영목 2020.12.01. 매입시 유효기간 추가
        //----------------------------------------//
        indExpiredLinearLayout = (LinearLayout) findViewById(R.id.ind_expired_linearlayout);
        indExpiredGubnTextView = (TextView) findViewById(R.id.ind_expired_gubn_textview);
        indExpiredNumEditText = (EditText) findViewById(R.id.ind_expired_num_edittext);

        if (indExpiredYn.equals("0")) {
            indExpiredLinearLayout.setVisibility(View.INVISIBLE);

            indExpiredGubnTextView.setText("");
            indExpiredNumEditText.setText("");
            indExpiredNum = "";
            indExpiredGubn = "";

        } else {
            indExpiredGubnTextView.setText(indExpiredGubn);
            indExpiredLinearLayout.setVisibility(View.VISIBLE);
        }

        indExpiredNumEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                    upu.setChangeBackGroundColor(1, indExpiredNumEditText);
                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                } else {
                    upu.setChangeBackGroundColor(0, indExpiredNumEditText);
                }
            }
        });

        indExpiredNumEditText.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                // TODO Auto-generated method stub
                //완료 버튼을 눌렀습니다.
                if (i == EditorInfo.IME_ACTION_DONE) {
                    //단품 저장
                    checkData();
                }

                return false;
            }
        });
        //----------------------------------------//

        m_bt_keyboardUp = (Button) findViewById(R.id.buttonKeyboardUp);
        m_bt_keyboardUp.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                m_textBarcode.requestFocus();

                //m_textBarcode.setInputType(2);
                // 김영목 2020.09.14. 키패드입력 옵션 설정에 따라 적용 추가
                if (m_KeypadType == 0) { // 숫자키패드
                    m_textBarcode.setInputType(2);
                }
                if (m_KeypadType == 1) { // 문자키패드
                    m_textBarcode.setInputType(145); //textVisiblePassword ) 0x00000091
                }

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                //2019-11-14
                //imm.showSoftInput(m_textBarcode, InputMethodManager.SHOW_IMPLICIT);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

            }
        });

        m_textProductName = (EditText) findViewById(R.id.editTextProductName);

        m_et_purchasePrice = (EditText) findViewById(R.id.editTextPurchasePrice);
        m_et_purchaseCost = (EditText) findViewById(R.id.editTextPurchaseCost);
        m_et_addTax = (EditText) findViewById(R.id.editTextAddTax);
        m_purpriTaxyn = (CheckBox) findViewById(R.id.checkBoxTaxProduct); // 부가세
        // 별도
        // 포함
        // 옵션
        m_purpriTaxyn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b){
                    m_et_purchaseCost.setEnabled(false);
                }else{
                    m_et_purchaseCost.setEnabled(true);
                }
            }
        });

        m_et_salePrice = (EditText) findViewById(R.id.editTextSalePrice);
        m_amount = (EditText) findViewById(R.id.editTextAmount);
        m_profitRatio = (EditText) findViewById(R.id.editTextProfitRatio);

        //2019-02-02 재고표현
        m_textviewStock = (TextView) findViewById(R.id.textView_Stock);

        //행사상품표시
        m_goodsGubun = (TextView) findViewById(R.id.textViewGoodsGubun);


        // 반품 체크
        m_checkBoxRejectedProduct = (CheckBox) findViewById(R.id.checkBoxRejectedProduct);

        //매입거래처 고정
        m_checkBoxOfficeFix = (CheckBox)findViewById(R.id.checkboxOfiiceFix);
        m_checkBoxOfficeFix.setChecked(true);
        m_customerCode.setEnabled(false);
        m_customerName.setEnabled(false);
        //m_customerFix=true;

        m_checkBoxOfficeFix.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if(isChecked){
                    m_customerCode.setEnabled(false);
                    m_customerName.setEnabled(false);
                    //m_customerFix=true;
                }else{
                    m_customerCode.setEnabled(true);
                    m_customerName.setEnabled(true);
                    //m_customerFix=false;
                }
            }
        });

        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        m_dateCalender1 = Calendar.getInstance();
        m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

        m_setchangedate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSetDate1(v);
            }
        });

        // 저장누르기 ( 목록으로 넘어감 )
        saveButton = (Button) findViewById(R.id.buttonSave);
        saveButton.setTag("저장");
        saveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                //오류체크 후 저장
                checkData();

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

        // 바코드 입력 후 포커스 딴 곳을 옮길 경우
        m_textBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    upu.setChangeBackGroundColor(0, m_textBarcode);
                    String barcode = m_textBarcode.getText().toString();

                    if (!barcode.equals("")) {
                        doQueryWithBarcode();
                    }

                } else {
                    //m_textBarcode.setInputType(0);
                    //2019-11-14
                    //m_textBarcode.setTextIsSelectable(true);
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

                    try {
                        Float.parseFloat(m_et_purchasePrice.getText().toString());
                    } catch (NumberFormatException e) {
                        m_et_purchasePrice.setText("0");
                        m_et_addTax.setText("0");
                        m_et_purchaseCost.setText("0");
                    }

                    // 이익률 변경하기
                    calculateProfitRatio();
                    // 매입원가, 부가세 계산하기
                    calculatePurpri();

                } else {
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
                    try {
                        Float.parseFloat(m_et_purchaseCost.getText().toString());
                    } catch (NumberFormatException e) {
                        return;
                    }

                    // 매입원가를 이용해서 매입가를 만들어 줍니다.
                    purpricostTopurpri();
                    // 매입가를 이용해서 판매가와 계산해 이익률 만들어 줍니다.
                    calculateProfitRatio();
                    // 매입가를 다시 가공해서 매입원가와 부가세를 뿌려줍니다.
                    calculatePurpri();

                } else {
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

                    try {
                        Float.parseFloat(m_et_salePrice.getText().toString());
                    } catch (NumberFormatException e) {
                        m_et_salePrice.setText("0");
                    }

                    String sell = m_et_salePrice.getText().toString();

                    // 판매가 원단위 절사 하기
                    if (!sell.equals("") || !sell.equals("0")) {
                        m_et_salePrice.setText(sellpriceRound(sell));
                    }
                    calculateProfitRatio();
                } else {
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

                    try {
                        Float.parseFloat(m_profitRatio.getText().toString());
                    } catch (NumberFormatException e) {
                        m_profitRatio.setText("0");
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

                        if (Float.isInfinite(f_ratio) || Float.isNaN(f_ratio)) {
                            m_profitRatio.setText("0");
                            Log.i("계산 오류", f_ratio + "");
                            return;
                        }

                        m_profitRatio.setText(String.format("%.2f", f_ratio * 100));
                        m_et_salePrice.setText(String.valueOf(rest));
                    }
                } else {
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

                    //2019-11-14 toggleSoftInput 로 변경
                    // 키보드를 띄운다.
                    //imm.showSoftInput(getCurrentFocus(), 0);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                    // 키보드를 없앤다.
                    // imm.hideSoftInputFromWindow(smsid.getWindowToken(),0);

                    // 키보드 강제로 띄우기
                    // getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                } else {
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
                } else {
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
                if (actionId == EditorInfo.IME_ACTION_DONE) {
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

        //----------------------------------------//
        // 김영목 2020.12.01. 매입시 유효기간 추가
        //----------------------------------------//
        //m_amount.setImeOptions(EditorInfo.IME_ACTION_DONE);
        if (indExpiredYn.equals("0")) {
            m_amount.setImeOptions(EditorInfo.IME_ACTION_DONE);

        } else {
            indExpiredNumEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        }
        //----------------------------------------//

        //m_listReadyToSend.setOnItemClickListener(this);

        Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
        TextView textView = (TextView) findViewById(R.id.textView2);
        textView.setTypeface(typeface);

        //releaseInputMode();

        // intent filter 2017-04 m3mobile
        if (m3Mobile) {
			/*IntentFilter filter = new IntentFilter();
			filter.addAction(SCANNER_ACTION_BARCODE);
			registerReceiver(BarcodeIntentBroadcast,filter);*/

            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textBarcode, m_amount);

        }


        //넘어온 자료가 있으면 수정모드로 전환하여야 합니다.
        Intent intent = new Intent(this.getIntent());

        String office_code = intent.getStringExtra("Office_Code");
        String in_date = intent.getStringExtra("In_Date");
        String office_name = intent.getStringExtra("Office_Name");

        if (!"".equals(office_code)) {
            m_customerCode.setText(office_code);
            m_customerName.setText(office_name);
            m_period.setText(in_date);

            //목록 불러오기(포스번호로) khs 최초 호출 시 이전 작업 내용이 호출될 때에만 불러지도록 변경함
            getTempInDPDAList();
        }

        //2019-05-19 이전 작업 내용이 있을때만 호출되도록 변경함 khs
        //목록 불러오기(포스번호로)
        //getTempInDPDAList();

        m_textBarcode.requestFocus();

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

            if (Float.isInfinite(f_ratio) || Float.isNaN(f_ratio)) {
                m_profitRatio.setText("0");
                Log.i("계산 오류", f_ratio + "");
                return;
            }

            m_profitRatio.setText(String.format("%.2f", f_ratio));

            Log.i("매입가 변경 계산", "이익률 계산 완료.");
        } else {
            m_profitRatio.setText("0");
            Log.i("매입가 변경 계산", "이익률 계산 실패.");
        }

    }

    // 매입가로 매입원가 이익률 계산하기
    private void calculatePurpri() {

        //2022.08.03. 상품검색 없이 매입가 처리시 에러 수정
        if (m_tempProduct.isEmpty()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(PurchaseRegistActivity1_new.this, AlertDialog.THEME_HOLO_LIGHT);
            builder.setTitle("상품검색");
            builder.setMessage("상품을 먼저 검색하세요!");
            builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    clearInputBox();
                    return;
                }
            });
            builder.show();
        } else {


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
                m_et_purchasePrice.setText("0"); //매입가
                m_et_purchaseCost.setText("0"); // 매입원가
                m_et_addTax.setText("0");// 부가세
            }

            Log.i("매입가 변경 계산", "원가/부가세 계산 완료.");

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

        //----------------------------------------//
        // 김영목 2020.12.01. 매입시 유효기간 추가
        //----------------------------------------//
        indExpiredNum = indExpiredNumEditText.getText().toString();
        //----------------------------------------//

        // 비어 있는 값 확인
        if (code.equals("")) { //거래처코드
            m_customerCode.requestFocus();
            Toast.makeText(getApplicationContext(), "거래처를 선택해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (name.equals("")) { //거래처명
            m_customerName.requestFocus();
            Toast.makeText(getApplicationContext(), "거래처를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (purchaseDate.equals("")) { //매입일
            m_period.requestFocus();
            Toast.makeText(getApplicationContext(), "매입일을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (barcode.equals("")) {    //바코드
            m_textBarcode.requestFocus();
            Toast.makeText(getApplicationContext(), "상품을 검색해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (productName.equals("")) {    //상품명
            m_textProductName.requestFocus();
            Toast.makeText(getApplicationContext(), "상품명을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (purchasePrice.equals("")) {    //매입가
            m_et_purchasePrice.requestFocus();
            Toast.makeText(getApplicationContext(), "매입가를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (purchaseCost.equals("")) {    //매입원가
            m_et_purchaseCost.requestFocus();
            Toast.makeText(getApplicationContext(), "매입원가를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (salePrice.equals("")) {        //판매가
            m_et_salePrice.requestFocus();
            Toast.makeText(getApplicationContext(), "판매가를 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (amount.equals("")) {    //매입수량
            m_amount.requestFocus();
            Toast.makeText(getApplicationContext(), "매입수량을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        if (profitRatio.equals("")) {    //이익률
            m_profitRatio.requestFocus();
            Toast.makeText(getApplicationContext(), "이익률을 입력해주세요.", Toast.LENGTH_SHORT).show();
            return;
        }


        // 0원 입력값 확인
        try {

            if (Float.parseFloat(purchasePrice) < 0) {    //매입가
                Toast.makeText(getApplicationContext(), "매입가를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                m_et_purchasePrice.requestFocus();
                return;
            }

            if (Float.parseFloat(salePrice) < 0) {        //판매가
                Toast.makeText(getApplicationContext(), "판매가를 입력해 주세요.", Toast.LENGTH_SHORT).show();
                m_et_salePrice.requestFocus();
                return;
            }

            if (Float.parseFloat(amount) <= 0) {
                m_amount.requestFocus();
                Toast.makeText(getApplicationContext(), "매입 수량을 입력해 주셔야 합니다.", Toast.LENGTH_SHORT).show();
                return;
            }

        } catch (NumberFormatException e) {
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
			if (ratio < 0 || ratio > 100) {
				m_profitRatio.requestFocus();
				Toast.makeText(getApplicationContext(), "이익률을 확인해 주세요", Toast.LENGTH_SHORT).show();
				return;
			}
			*/

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
        if (Double.parseDouble(purchaseCost) < 0) {
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


        //상품명이 변경 되었으면 먼저 저장합니다.
        if (!("".equals(m_textGname) || m_textGname == null) && !m_textGname.equals(productName)) {
            setProductNameChange(barcode, productName);
        }

        //발주일이 오늘 날자와 같지 않고 매입일 고정에 체크가 풀려있다면
		/*if(!purchaseDate.equals(upu.getTodayData()) && !m_purDataFix.isChecked() ){

			String str = "현재 매입일이 오늘 날자와 일치하지 않습니다.\r\n"
					+ "[ 예 ] - 지정날자 매입 (완료 후 체크해제 필수)\r\n"
					+ "[ 아니오 ] - 오늘 날자로 변경";

			AlertDialog.Builder alt_bld = new AlertDialog.Builder(mContext);

			alt_bld.setMessage(str)
					.setCancelable(false)
					.setPositiveButton("예", new DialogInterface.OnClickListener(){
						public void onClick(DialogInterface dialog, int id) {
							m_purDataFix.setChecked(true);
							doSend();
		        	*//*String tag = (String)saveButton.getTag();
					if (tag.toString().equals("저장")) {
						doSend();
					} *//*
							//dialog.cancel();
						}
					})
					.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							m_period.setText(upu.getTodayData());
							doSend();
		        	*//*String tag = (String)saveButton.getTag();
					if (tag.toString().equals("저장")) {
						doSend();
					}*//*
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
		}*/

        doSend();

    }

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
     * - 키보드 띄우기 - 삭제
     * - 거래처코드 고정
     */
    public void clearInputBox() {

        //----------------------------------------//
        // 김영목 2020.12.01. 매입시 유효기간 추가
        //----------------------------------------//
        indExpiredNumEditText.setText("");
        indExpiredNum = "";
        //----------------------------------------//

//        //거래처를 클리어 합니다.
//        if (!m_customerFix) {
//            m_customerCode.setText("");
//            m_customerName.setText("");
//        }
        //거래처고정
        if(!m_checkBoxOfficeFix.isChecked()) {
            m_customerCode.setEnabled(true);
            m_customerCode.setText("");
            m_customerName.setEnabled(true);
            m_customerName.setText("");
        }


        //m_selectedListIndex = -1;
        m_textBarcode.setText(""); // 바코드
        m_textProductName.setText(""); // 상품명

        m_et_purchasePrice.setText(""); // 매입가

        m_purpriTaxyn.setChecked(true);
        m_purpriTaxyn.setEnabled(true);

        m_et_salePrice.setText(""); // 판매가
        m_et_purchaseCost.setText(""); // 매입원가
        m_et_addTax.setText(""); // 부가세
        m_amount.setText(""); // 매입수량
        m_profitRatio.setText(""); // 이익률

        m_goodsGubun.setText("일반"); // 행사구분
        m_goodsGubun.setTextColor(Color.WHITE); // 행사구분 색


        //hideSoftKeyboard(true);

        //현상품정보 초기화
        // 원상품명 초기화
        m_textGname = ""; // 원상품명
        m_tempProduct.clear();

        m_textBarcode.setEnabled(true);
        m_textBarcode.requestFocus(); // 바코드로 포커스이동

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        // 키보드를 띄운다.
        //imm.showSoftInput(getCurrentFocus(), 0);
        // 키보드를 없앤다.
        imm.hideSoftInputFromWindow(m_textBarcode.getWindowToken(), 0);

        // 키보드 강제로 띄우기
        // getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);

        //2019-02-02
        m_textviewStock.setText("");

    }


    /**
     * 상품명을 수정합니다.
     * m_textGname 변수와 현재 텍스트 필드의 값이 틀리다면 상품명이 수정된걸로 합니다.
     * 프로시저를 사용 못하고 바로 다이렉트로 변경 합니다.
     */
    private void setProductNameChange(String barcode, String gname) {

        String query = "Update Goods Set G_Name='" + gname + "', Edit_Date=CONVERT(NVARCHAR(10), GETDATE(), 120), Editor='" + posID + "', " +
                "Edit_Check='1', Tran_Chk='1' Where Barcode = '" + barcode + "'; ";

        // 로딩 다이알로그
        //다이얼로그 통일화
        // dialog = new ProgressDialog(this);
        if (!dialog.isShowing()) {
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
                Log.d("상품명 변경 : ", "성공");
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Log.d("상품명 변경 : ", "실패[" + msg + "]");
            }

        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

    }


    /**
     * 상품 스캔 후
     * - 바코드 수정불가
     */
    public void porductScanAfter() {
        m_textBarcode.setEnabled(false);
    }

    /**
     * 상품등록 후 거래처 및 날자 변경 불가
     * - 거래처코드 입력 불가
     * - 거래처명 입력 불가
     */
    public void officeChangeFalse() {
        m_customerCode.setEnabled(false);
        m_customerName.setEnabled(false);

        m_period.setEnabled(false);
        m_setchangedate.setEnabled(false);

        //m_customerFix = true;
    }

    //거래처 변경 버튼을 사용 못하게 처리 합니다.
    private void officeChangeTrue() {

        m_customerCode.setEnabled(true);
        m_customerName.setEnabled(true);

        m_customerCode.setText("");
        m_customerName.setText("");

        m_period.setEnabled(true);
        m_setchangedate.setEnabled(true);

        //m_customerFix = false;

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
        //다이얼로그 통일화
        // dialog = new ProgressDialog(this);
        if (!dialog.isShowing()) {
            dialog.setMessage("Loading....");
            dialog.setCancelable(false);
            dialog.show();
        }


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

            //2019-05-27
            VAT_CHK = "1";
        } else {
            //부가세 별도입니다. 오리지널도 부가세로 표기해 주세요
            //매입가-> 매입원가로
            //오리지널매입가->오리지널매입원가로
            purchasePrice = m_et_purchaseCost.getText().toString();
            Org_PurPri = m_tempProduct.get("Pur_Cost");
            Tax_Gubun = "0";
            Summarize += ", 별도가";

            //2019-05-27
            VAT_CHK = "0";
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

        // 매입여부(0:반품,1:매입,2:행사반품,3:행사매입)
        if (In_YN.toString().equals("3")) { // 행사상품일때
            Change_Use = "0"; // 행사 상품일때 상품 매입가 판매가 거래처 변경 불가능!!
            //행사 상품이라면 매입가 판매가 변경이 안됩니다.
            //그래서 원장에 있는 내용으로 적용합니다.
            if (Vat_Chk) {
                purchasePrice = m_tempProduct.get("Pur_Pri");
            } else {
                purchasePrice = m_tempProduct.get("Pur_Cost");
            }
            Pur_Cost = m_tempProduct.get("Pur_Cost");
            Add_Tax = m_tempProduct.get("Add_Tax");
            salePrice = m_tempProduct.get("Sell_Pri");
            profitRatio = m_tempProduct.get("Profit_Rate");

            Summarize += ", 행사매입";
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
                //----------------------------------------//
                // 김영목 2020.12.01. 매입시 유효기간 추가
                //----------------------------------------//
                //+ "''0'', ''0'', ''0''' " ;
                + "''0'', ''0'', ''0'', ''" + indExpiredNum + "'', ''" + indExpiredGubn + "''' ";
        //----------------------------------------//

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
        //다이얼로그 통일화
        if (!dialog.isShowing()) {
            dialog.setMessage("Loading....");
            dialog.setCancelable(false);
            dialog.show();
        }

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
                            m_customerFix = true;

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


        //목록 불러오기(포스번호로)
        getTempInDPDAList();

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

                    Log.i("ZBAR_SCANNER_REQUEST", "Scan Result:" + data.getStringExtra(ZBarConstants.SCAN_RESULT));
                    Log.i("ZBAR_SCANNER_REQUEST", "Scan Result Type:" + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE));

                    Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();
                    Toast.makeText(this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE), Toast.LENGTH_SHORT).show();

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

                    if (m_newproduct) {
                        m_newproduct = false;

                        doRegister();
                    }

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

//        if (m_customerFix) {
//            Toast.makeText(this, "매입 등록된 상품이 있어 거래처를 변경할수 없습니다.", Toast.LENGTH_SHORT).show();
//            return;
//        }

        String customer = "";
        String customername = "";

//        if (m_customerCode.isEnabled()) {
//            customer = m_customerCode.getText().toString();
//            customername = m_customerName.getText().toString();
//        }

        if (m_checkBoxOfficeFix.isChecked()){
            customer = m_customerCode.getText().toString();
            customername = m_customerName.getText().toString();
        }

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
            final String[] option = new String[]{"목록", "카메라"};
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.user_select_dialog_item, option);
            AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
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
        //다이얼로그 통일화
        //ProgressDialog dialog = new ProgressDialog(this);
        if (!dialog.isShowing()) {
            dialog.setMessage("Loading....");
            dialog.show();
        }

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

                        if (m_tempProduct.get("Goods_Use").toString().equals("0") || m_tempProduct.get("Pur_Use").toString().equals("0")) {

                            String str = "사용중지상품 입니다. 변경사용 하시겠습니까? ";

                            new AlertDialog.Builder(PurchaseRegistActivity1_new.this, AlertDialog.THEME_HOLO_LIGHT).setTitle(str).setNeutralButton("예", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String barcode = m_textBarcode.getText().toString();
                                    String query = "exec sp_executesql N'EXEC str_PDA_004U ''" + barcode + "'''";

                                    // 콜백함수와 함께 실행
                                    new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

                                        @Override
                                        public void onRequestCompleted(JSONArray results) {
                                            if (results.length() > 0) {
                                                try {
                                                    if (!"OK".equals((String) results.getJSONObject(0).get("1"))) {
                                                        Toast.makeText(PurchaseRegistActivity1_new.this, "변경 하지 못했습니다", Toast.LENGTH_SHORT).show();
                                                        return;
                                                    }
                                                } catch (JSONException e) {
                                                    Toast.makeText(PurchaseRegistActivity1_new.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                            }
                                        }

                                        @Override
                                        public void onRequestFailed(int code, String msg) {
                                            Toast.makeText(PurchaseRegistActivity1_new.this, "연결실패\r\n" + msg, Toast.LENGTH_SHORT).show();
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

//                        // 거래처 고정선택 되어 있을시에 거래처 변경 안되게 설정 됩니다.
//                        if (m_customerCode.isEnabled()) {
//                            m_customerCode.setText(results.getJSONObject(0).getString("Bus_Code"));
//                            m_customerName.setText(results.getJSONObject(0).getString("Bus_Name"));
//
//                            //직전 상품 거래처
//                            mOfficeData[0] = results.getJSONObject(0).getString("Bus_Code");
//                            mOfficeData[1] = results.getJSONObject(0).getString("Bus_Name");
//                        } else {
//                            //거래처 고정인데 거래처 코드란이 비어있다면 채워줍니다.
//                            if ("".equals(m_customerCode.getText().toString())) {
//                                m_customerCode.setText(results.getJSONObject(0).getString("Bus_Code"));
//                                m_customerName.setText(results.getJSONObject(0).getString("Bus_Name"));
//
//                                //직전 상품 거래처
//                                mOfficeData[0] = results.getJSONObject(0).getString("Bus_Code");
//                                mOfficeData[1] = results.getJSONObject(0).getString("Bus_Name");
//                            }
//                        }
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

                        //2019-02-02
                        //재고수량 표현
                        m_textviewStock.setText(results.getJSONObject(0).getString("Real_Sto"));


                        //2019-05-27 일반/행사구분 초기화
                        m_goodsGubun.setText("일반");
                        m_goodsGubun.setTextColor(Color.WHITE);

                        // 행사상품 검사하기
                        doQueryWithBarcode2(m_textBarcode.getText().toString());


                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                } else {

                    //첫 상품 미등록 상품 스캔 시
                    if ("".equals(mOfficeData[0])) {
                        alert_show();
                        m_newproduct = true;
                        return;
                    }

                    //m_et_purchasePrice.requestFocus();
                    //후등록 상품 등록 하기
                    doRegister();
                }

                // 스캔완료 비푸흠
                scanResult();
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                Toast.makeText(PurchaseRegistActivity1_new.this, "상품검색 실패\r\n" + msg, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                dialog.cancel();
            }

        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }


    private void alert_show() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle("미등록 상품");
        builder.setMessage("거래처를 먼저 지정 하세요");
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                onCustomerSearch(new View(PurchaseRegistActivity1_new.this));
            }
        });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getApplicationContext(), "첫 상품은 신규 상품을 등록 할수 없습니다.", Toast.LENGTH_LONG).show();
                    }
                });
        builder.show();
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

        // 로딩 다이알로그
        //다이얼로그 통일화
        //ProgressDialog dialog = new ProgressDialog(this);
        if (!dialog.isShowing()) {
            dialog.setMessage("Loading....");
            dialog.show();
        }

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
                dialog.dismiss();
                dialog.cancel();
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
		/*String pur_pri = "800"; // 10.매입가
		String pur_cost = "727.27"; // 11.매입원가
		String add_tax = "72.73"; // 12.부가세
		String salesPrice = "1000"; // 13.판매가
		String ratio = "20"; // 14.이익률*/

        String pur_pri = "0"; // 10.매입가
        String pur_cost = "0"; // 11.매입원가
        String add_tax = "0"; // 12.부가세
        String salesPrice = "0"; // 13.판매가
        String ratio = "0"; // 14.이익률

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
        //ProgressDialog dialog = new ProgressDialog(this);
        if (!dialog.isShowing()) {
            dialog.setMessage("Loading....");
            dialog.show();
        }

        // 콜백함수와 함께 실행
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                try {
                    String res = (String) results.getJSONObject(0).get("1");
                    if (!"OK".equals(res)) {
                        Toast.makeText(PurchaseRegistActivity1_new.this, res, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String str = "신상품등록 완료";

                    doQueryWithBarcode();
                } catch (JSONException e) {
                    Toast.makeText(PurchaseRegistActivity1_new.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    //뷰에서 직접 실행
    public void toSendPurList(View view) {

        //매입전표가 한장 입니다. 바로 전송 합니다.
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setTitle("매입 전송").setMessage("매입전표를 전송 하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //매입전표 전송
                        toSendPurList_local();
                    }
                }).setNegativeButton("아니요", null).show();

    }


    //매입장 한장씩 전송하기
    private void toSendPurList_local() {

        String table_name = m_period.getText().toString();
        String pri_date = m_period.getText().toString();

        table_name = table_name.replace("-", "").substring(0, 6);
        Log.d("테이블명", table_name);

        String query = "exec sp_executesql N'SELECT ISNULL(MAX(RIGHT(In_Num,3)),''000'') FROM InT_" + table_name + " WHERE In_Date=''" + pri_date + "'' AND SUBSTRING(in_Num,9,1) =''" + posID + "''' ";

        // 로딩 다이알로그
        //ProgressDialog dialog = new ProgressDialog(this);
        if (dialog.isShowing()) {
            dialog.setMessage("Loading....");
            dialog.show();
        }

        // 콜백함수와 함께 실행
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();

                try {

                    String res = (String) results.getJSONObject(0).get("1");

                    Log.d("전표번호 : ", res);
                    int num_b = Integer.parseInt(res);
                    num_b++;

                    // 숫자를 문자형식으로 변경
                    res = String.format("%03d", num_b);

                    //매입장 전송
                    toSendPurList_sub(res);

                } catch (JSONException e) {
                    Toast.makeText(PurchaseRegistActivity1_new.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
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
     *
     * @param num 전표번호 뒷자리
     */
    private void toSendPurList_sub(String num) {
        String gubun_num = num;

        String office_code = m_customerCode.getText().toString();
        String in_date = m_period.getText().toString();
        String junpyo_num = in_date.replace("-", "") + posID + gubun_num;
        Log.d("전표번호", junpyo_num);

        String query = "exec sp_executesql N'EXEC str_PDA_013I ''" + office_code + "'', ''" + in_date + "'', ''" + userID + "'', ''" + posID + "'', ''" + junpyo_num + "'''";


        // 로딩 다이알로그
        //ProgressDialog dialog = new ProgressDialog(this);
        if (!dialog.isShowing()) {
            dialog.setMessage("Loading....");
            dialog.show();
        }

        // 콜백함수와 함께 실행
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();

                try {
                    String res = (String) results.getJSONObject(0).get("1");
                    if (!"OK".equals(res)) {
                        Toast.makeText(PurchaseRegistActivity1_new.this, res, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    getTempInDPDAList();
                    Toast.makeText(PurchaseRegistActivity1_new.this, "전송완료", Toast.LENGTH_SHORT).show();
                    m_customerFix = false;

                } catch (JSONException e) {
                    Toast.makeText(PurchaseRegistActivity1_new.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
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


    public void onClickSetDate1(View v) {
        DatePickerDialog newDlg = new DatePickerDialog(this, this, m_dateCalender1.get(Calendar.YEAR),
                m_dateCalender1.get(Calendar.MONTH), m_dateCalender1.get(Calendar.DAY_OF_MONTH));
        newDlg.show();
    }

    ;

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        m_dateCalender1.set(year, monthOfYear, dayOfMonth);
        m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

    }


    /**
     * 부가세 별도 포함 선택 함수
     *
     * @param s s는 Tax_YN구분
     * @param p p는 VAT_CHK 구분
     */
    private void setTaxSeparate(String s, String p) {

        Log.i("첫번째 매개변수", s.toString() + p.toString());

        if (s.equals("1")) {
            m_purpriTaxyn.setEnabled(true);
        } else {
            //면세상품 알림 표시

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
    private void getTempInDPDAList() {

        String query = "SELECT "
                + " A.*, B.G_Name "
                + "From "
                + "(select * "
                + "From Temp_InDPDA "
                + "Where "
                + "Office_Code='" + m_customerCode.getText() + "' and In_Date='" + m_period.getText() + "' AND "
                + "SubString(In_Num,9,1) = '" + posID + "' ) A "
                + "LEFT JOIN Goods B ON A.BarCode = B.BarCode Order By In_Seq";

        // 로딩 다이알로그
        //ProgressDialog dialog = new ProgressDialog(this);
        if (!dialog.isShowing()) {
            dialog.setMessage("Loading....");
            dialog.show();
        }

        // 콜백함수와 함께 실행
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {

                dialog.dismiss();
                dialog.cancel();

                int m매입가합 = 0; //매입가(공병포함) 합
                int m반품가합 = 0; //반품가(공병포함) 합

                //이익률 계산
                float m전체매입가 = 0;
                float m전체판매가 = 0;

                //전체 이익금 계산
                float m전체이익금 = 0;

                int m총수량 = 0;

                if (results.length() > 0) {

                    try {

                        for (int i = 0; i < results.length(); i++) {

                            JSONObject obj = results.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<String, String>();

                            String in_pri = obj.getString("In_Pri");        //매입가(공병포함)합
                            String in_sellpri = obj.getString("In_SellPri");    //판매가(공병포함)합

                            String pur_pri = obj.getString("Pur_Pri");    //단품매입가
                            String sell_pri = obj.getString("Sell_Pri");    //단품판매가

                            String profit_pri = obj.getString("Profit_Pri");  //이익금
                            String pCount = obj.getString("In_Count"); //매입 수량

                            //In_YN --(1:매입, 0:반품, 2:행사반품, 3:행사매입 (Default 1))
                            try {

                                //매입가 반품가 계산
                                if ("0".equals(obj.getString("In_YN")) || "2".equals(obj.getString("In_YN"))) {
                                    m반품가합 += Float.parseFloat(in_pri);
                                } else {
                                    m매입가합 += Float.parseFloat(in_pri);
                                }

                                m총수량 += Integer.parseInt(pCount);
                                m전체매입가 += Float.parseFloat(in_pri);
                                m전체판매가 += Float.parseFloat(in_sellpri);
                                m전체이익금 += Float.parseFloat(profit_pri);

                            } catch (NumberFormatException e) {
                                e.printStackTrace();
                            }

                            map.put("매입가", StringFormat.convertToNumberFormat(pur_pri));
                            map.put("판매가", StringFormat.convertToNumberFormat(sell_pri));

                            //map.put("전표번호", obj.getString("In_Num"));
                            //map.put("순번", obj.getString("In_Seq"));
                            //map.put("매입원가", obj.getString("Pur_Cost"));
                            //map.put("부가세", obj.getString("Add_Tax"));
                            //map.put("공병매입가", obj.getString("Bot_Pri"));
                            //map.put("공병판매가", obj.getString("Bot_SellPri"));
                            //map.put("구분", obj.getString("In_YN"));

                            //map.put("총매입가", obj.getString("In_Pri"));
                            //map.put("총판매가", obj.getString("In_SellPri"));
                            //map.put("이익금", obj.getString("Profit_Pri"));
                            //map.put("이익률", obj.getString("Profit_Rate"));
                            //map.put("면과세", obj.getString("Tax_YN"));
                        }

                        //금액 합치기
                        m_tempTotalInfo.put("In_Pri", (StringFormat.convertToNumberFormat("" + m매입가합))); //매입가
                        m_tempTotalInfo.put("In_RePri", (StringFormat.convertToNumberFormat("" + m반품가합))); //반품가
                        m_tempTotalInfo.put("Profit_Pri", StringFormat.convertToNumberFormat("" + Math.abs(m전체이익금))); //이익금
                        m_tempTotalInfo.put("pCount", m총수량 + "");
                        m_tempTotalInfo.put("tCount", results.length() + "");

                        float profit_rate = ((m전체판매가 - m전체매입가) / m전체판매가) * 100;
                        Log.d("이익률", "(" + m전체판매가 + "-" + m전체매입가 + ") / " + m전체판매가 + " * 100 = " + profit_rate);
                        m_tempTotalInfo.put("Profit_Rate", (StringFormat.convertToRoundFormat("" + profit_rate))); //이익률
                        m_tempTotalInfo.put("In_TPri", (StringFormat.convertToNumberFormat((m매입가합 + m반품가합) + "")));

                        //거래처 및 날자변경을 제한합니다.
                        officeChangeFalse();

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //금액 합치기
                    m_tempTotalInfo.put("In_Pri", "0"); //매입가
                    m_tempTotalInfo.put("In_RePri", "0"); //반품가
                    m_tempTotalInfo.put("Profit_Pri", "0"); //이익금
                    m_tempTotalInfo.put("pCount", "0");    // 총수량
                    m_tempTotalInfo.put("tCount", "0");            //총건수
                    m_tempTotalInfo.put("Profit_Rate", "0"); //이익률
                    m_tempTotalInfo.put("In_TPri", "0");    //총매입액

                    //거래처 및 날자변경을 제한합니다.
                    officeChangeTrue();

                    Toast.makeText(getApplicationContext(), "조회 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }

                //화면에 표기해 주세요
                makeFillvapsWithInfo();
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();

                Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    //하단에 정보 표기하기
    private void makeFillvapsWithInfo() {

        TextView pur_pri = (TextView) findViewById(R.id.total_purpri);    //매입가
        TextView pur_repri = (TextView) findViewById(R.id.total_repri);    //반품가
        TextView total_price = (TextView) findViewById(R.id.total_price);    //총매입가

        TextView pur_totalcount = (TextView) findViewById(R.id.pur_totalcount);    //총수량
        TextView pur_totalnum = (TextView) findViewById(R.id.pur_totalnum);        //총건수
        TextView pur_totalprofitrate = (TextView) findViewById(R.id.pur_totalprofitrate);    //이익률
        TextView pur_totalprofitpri = (TextView) findViewById(R.id.pur_totalprofitpri);    //이익금

        pur_pri.setText(m_tempTotalInfo.get("In_Pri"));
        pur_repri.setText(m_tempTotalInfo.get("In_RePri"));
        total_price.setText(m_tempTotalInfo.get("In_TPri"));

        pur_totalcount.setText(m_tempTotalInfo.get("pCount"));
        pur_totalnum.setText(m_tempTotalInfo.get("tCount"));
        pur_totalprofitrate.setText(m_tempTotalInfo.get("Profit_Rate"));
        pur_totalprofitpri.setText(m_tempTotalInfo.get("Profit_Pri"));

    }


    public void transferDelete(View view) {

        //매입전표가 한장 입니다. 바로 전송 합니다.
        new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT).setTitle("매입 삭제").setMessage("매입전표를 삭제 하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //매입전표 전송
                        transferDelete();
                    }
                }).setNegativeButton("아니요", null).show();

    }


    //매입장 삭제
    private void transferDelete() {

        //선택 매입장
		/*if(mfillMaps.size() <= 0){
			Toast.makeText(this, "매입 목록이 없습니다.", Toast.LENGTH_SHORT).show();
			return;
		}

		if(m_selectedListIndex < 0 ){
			Toast.makeText(this, "삭제할 전표를 선택해 주세요!", Toast.LENGTH_SHORT).show();
			return;
		}*/


        String office_code = m_customerCode.getText().toString();
        String in_date = m_period.getText().toString();

        String query = "exec sp_executesql N'EXEC str_PDA_013D ''" + office_code + "'', ''" + in_date + "'', ''" + posID + "'', ''DS'''";

        // 로딩 다이알로그
        //ProgressDialog dialog = new ProgressDialog(this);
        if (!dialog.isShowing()) {
            dialog.setMessage("Loading....");
            dialog.show();
        }

        // 콜백함수와 함께 실행
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                try {
                    String res = (String) results.getJSONObject(0).get("1");
                    if (!"OK".equals(res)) {
                        Toast.makeText(PurchaseRegistActivity1_new.this, res, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //재조회합니다.
                    getTempInDPDAList();
                    Toast.makeText(PurchaseRegistActivity1_new.this, "삭제 성공", Toast.LENGTH_SHORT).show();


                } catch (JSONException e) {
                    Toast.makeText(PurchaseRegistActivity1_new.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
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

    //상세보기를 합니다.
    public void setDetailView(View view) {
        //2019-07-16 매입건수 확인 추가
        TextView in_totalnum = (TextView) findViewById(R.id.pur_totalnum);        //총건수
        String iCnt = in_totalnum.getText().toString();
        if (iCnt.equals("0")) {
            Toast.makeText(getApplicationContext(), "조회 결과가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        String in_num = "";
        String in_date = m_period.getText().toString();
        String office_code = m_customerCode.getText().toString();
        String office_name = m_customerName.getText().toString();
        String in_pri = m_tempTotalInfo.get("In_Pri").replace(",", "");
        String in_repri = m_tempTotalInfo.get("In_RePri").replace(",", "");
        String profit_pri = m_tempTotalInfo.get("Profit_Pri").replace(",", "");
        String profit_rate = m_tempTotalInfo.get("Profit_Rate");

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

    //뒤로가기 버튼처리
    @Override
    public void onBackPressed() {

        if (m_customerFix) {

            new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("매입등록창 닫기")
                    .setMessage("등록 대기중인 매입자료가 있습니다. 종료할까요? 종료시 매입 미전송 목록으로 수정")
                    .setPositiveButton("매입전송", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            toSendPurList_local();
                        }

                    })
                    .setNeutralButton("종료", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }

                        ;
                    })
                    .setNegativeButton("취소", null)
                    .show();

        } else {
            super.onBackPressed();
        }
    }


    /**
     * Set up the {@link ActionBar}.
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


    //키보드 없애기
    protected void hideSoftKeyboard(boolean onoff) {
        InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        if (onoff) {
            //imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 1);
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        } else {
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

    //2017-04 m3mobile 추가
    @Override
    protected void onResume() {
        super.onResume();
        if (m3Mobile) {
            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textBarcode, m_amount);
            M3MoblieBarcodeScanBroadcast.setOnResume();
        }
    }

    //2017-04 m3mobile 추가
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m3Mobile) {
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
