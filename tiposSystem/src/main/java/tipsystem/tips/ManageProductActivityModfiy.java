package tipsystem.tips;

import static java.lang.Thread.sleep;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.bixolon.labelprinter.BixolonLabelPrinter;
import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.sewoo.jpos.command.CPCLConst;
import com.sewoo.jpos.printer.CPCLPrinter;
import com.sewoo.port.android.BluetoothPort;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import kr.co.tipos.tips.R;
import tipsystem.tips.models.SPPL3000;
import tipsystem.utils.BarcodePrintLKP30II;
import tipsystem.utils.BarcodePrinterSPPL3000;
import tipsystem.utils.BluetoothConnectMenu;
import tipsystem.utils.CalculateChangePrice;
import tipsystem.utils.DBAdapter;
import tipsystem.utils.DialogManager;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.M3MoblieBarcodeScanBroadcast;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;
import tipsystem.utils.TIPS_Config;
import tipsystem.utils.UserPublicUtils;

/*
 * 기본관리 -> 상품관리
 * */
public class ManageProductActivityModfiy extends Activity implements DatePickerDialog.OnDateSetListener {
    private static final int ZBAR_SCANNER_REQUEST = 0;
    // private static final int ZBAR_QR_SCANNER_REQUEST = 1;
    private static final int BARCODE_MANAGER_REQUEST = 2;
    private static final int CUSTOMER_MANAGER_REQUEST = 3;
    private static final int CUSTOMER_BOXPRODUCT_REQUEST = 4;

    // 2017-04 m3mobile
    private static final String SCANNER_ACTION_SETTING_CHANGE = "com.android.server.scannerservice.settingchange";
    public static final String SCANNER_ACTION_PARAMETER = "android.intent.action.SCANNER_PARAMETER";
    public static final String SCANNER_ACTION_ENABLE = "com.android.server.scannerservice.m3onoff";
    public static final String SCANNER_EXTRA_ENABLE = "scanneronoff";

    public static final String SCANNER_ACTION_START = "android.intent.action.M3SCANNER_BUTTON_DOWN";
    public static final String SCANNER_ACTION_CANCEL = "android.intent.action.M3SCANNER_BUTTON_UP";

    public static final String SCANNER_ACTION_BARCODE = "com.android.server.scannerservice.broadcast";


    private static final String TAG = "ManageProductActivityModfiy";

    JSONObject m_shop;

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

    String m_posID = "";

    /* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
    int m_KeypadType = 0;

    String m_officeCode = "";

    String m_barcode;
    String productReg_YN;
    String mGubun;        //어디서 넘어왔는지 저장합니다.

    EditText m_textBarcode; // 1.바코드
    EditText m_textProductName; // 2.상품명
    EditText m_textCustomerCode; // 3.거래처코드
    EditText m_textCustomerName; // 4.거래처명
    Button m_buttonbarcode; // 바코드검색버튼
    Button m_buttonCustomerClassification1; // 5-1.대분류
    Button m_buttonCustomerClassification2; // 5-2.중분류
    Button m_buttonCustomerClassification3; // 5-3.소분류
    EditText m_textStandard; // 9.규격
    EditText m_textAcquire; // 10.입수
    Button buttonAcquire;    //입수등록 버튼
    EditText m_textPurchasePrice; // 11.매입가
    EditText m_textPurchasePriceOriginal; // 12.매입원가
    /*
     * 2014-12-18일 추가 수정 부가세 별도, 포함 옵션 추가
     */
    CheckBox m_checkboxPurPrivatGubun; // 부가세 별도 포함 옵션
    EditText m_textPurchasePriceOriginalTax;// 13.부가세
    EditText m_textSalesPrice; // 14.판매가

    //----------------------------------------//
    //2022-05-26 권장소비자가 추가
    //----------------------------------------//
    EditText m_editTextSellPri1;
    //----------------------------------------//

    EditText m_textDifferentRatio; // 15.이익률(%)
    EditText m_textRealSto; // 16.재고
    Spinner m_spinTaxation; // 6.면과세
    // CheckBox m_checkSurtax;
    Spinner m_spinGoods; // 7.사용구분
    //Spinner m_spinGroup; // 8.그룹
    Spinner m_spinMemberPoint; // 17.마일리지
    Spinner m_spinGoodsType; // 18.상품구분
    EditText m_textEditDate; // 19.수정일
    EditText m_textEditModified; // 20.수정자

    //2018-02-20 도매단가 추가하기
    EditText m_textASell_Pri; // 21.도매A단가
    EditText m_textADifferentRatio; // 22. 도매A이익률
    EditText m_textBSell_Pri; //23. 도매B단가
    EditText m_textBDifferentRatio; // 24. 도매B이익률
    EditText m_textCSell_Pri; // 25.도매C단가
    EditText m_textCDifferentRatio; // 26. 도매C이익률

    EditText m_textEventPurPri;    //행사매입가
    EditText m_textEventSalePri;    //행사판매가

    EditText m_textStartEventDate;    //행사시작일
    EditText m_textEndEventDate;    //행사종료일


    //2020-06-15 단위 내용량 기준용량
    Spinner m_spinGoodsDanwi;        //단위
    EditText m_TextCon_Rate;    //내용량
    EditText m_TextStd_Rate;    //기준용량

    // ListView m_listProduct;
    Button m_registButton;
    Button m_modfiyButton;
    Button m_recoveryButton;

    Button m_barprint;    //택저장
    Button m_barprintTran;    //택전송

    //----------------------------------------//
    //2023-01-04 택프린터 추가
    //----------------------------------------//
    // 1 row 버튼 정의
    Button buttonPrinterSetting;    // 택설정 버튼
    Button buttonPrinterOption;     // 택옵션 버튼
    Button buttonPrinterPrint;      // 택발행 버튼

    // 2 row 프린터 상태 정의
    TextView textViewPrinterState; // 프린터 연결 상태
    Spinner spinnerSelectPrinter; // 바코드프린터선택리스트

    // 프린터 관련 변수 정의
    int barcodePrinterCheck; //매장프린터 구분(번호)
    String barcodePrinterName; //매장프린터 명
    String barcodePrinterAddress; //매장프린터 주소
    String barcodePrinterPort;    //매장프린터 포트
    int barcodePrinterGubun;    //매장프린터 용지구분

    CheckBox checkboxSaleYn;            // 행사가 체크박스
    CheckBox checkboxSellPri1;          //권장소비자가 체크박스
    Spinner spinnerSelectBranchName;    //분류(대분류,중분류,소분류 선택)
    Spinner spinnerSelectAddItem;       // 추가항목 선택(출력일,사용자정의)
    Button buttonPrintSetDate;          // 추가항목 출력일
    EditText edittextPrintUserText;     // 추가항목 사용자정의 텍스트 문자
    CheckBox checkboxPrintDateTitle;    //출력일 문구 체크박스
    Calendar calendarPrintSetDate;      //추가항목 출력일 사용

    String mSaleYn = "0";               // 행사가
    String mSellPri1 = "0";             //권장소비자가
    int mSelectBranchName = 1;          //분류(대분류,중분류,소분류 선택)
    int mSelectAddItem = 1;             // 추가항목 선택(출력일,사용자정의)
    String mPrintSetDate = "";          // 추가항목 출력일
    String mPrintUserText = "";         // 추가항목 사용자정의 텍스트 문자
    String mPrintDateTitle = "1";       //출력일 문구 체크박스

    //int BranchNameType = 1; //1:대분류(기본),2:중분류,3:소분류
    //int AddItemType = 1; // 추가항목구분(1:출력일(기본), 2:사용자정의)
    //String AddItemUserText="";

    BarcodePrinterAsyncTask2 m_bprinter;
    //----------------------------------------//

    //----------------------------------------//
    // 2023.02.08. 바코드 자동생성 옵션 추가
    //----------------------------------------//
    boolean isSelfBarcodeUse = false;
    String selfBarcodePrefixCode = "";
    //----------------------------------------//

    // SimpleAdapter m_adapter;
    CalculateChangePrice ccp;
    List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();
    List<HashMap<String, String>> mfillMapsBar = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> m_tempProduct = new HashMap<String, String>();
    HashMap<String, String> m_tempProPurSell = new HashMap<String, String>();

    List<HashMap<String, String>> m_Ltype = new ArrayList<HashMap<String, String>>();
    List<HashMap<String, String>> m_Mtype = new ArrayList<HashMap<String, String>>();
    List<HashMap<String, String>> m_Stype = new ArrayList<HashMap<String, String>>();

    UserPublicUtils upu;

    Context mContext;
    SharedPreferences pref;
    boolean m3Mobile;

    //2018-10-26 빅슬론 무선 바코드 프린터 출력
    private String mConnectedDeviceName = null;
    static public BixolonLabelPrinter mBixolonLabelPrinter;
    public Handler m_hHandler = null;
    public BluetoothAdapter m_BluetoothAdapter = null;
    public ArrayAdapter<String> adapter = null;
    public ArrayList<BluetoothDevice> m_LeDevices;

    //세우테크 무선 프린터 출력
    //private ZPLRFIDPrinter zplprinter;
    private BluetoothConnectMenu blueConnect;
    private BluetoothPort bluetoothPort;

    // loading bar
    public ProgressDialog dialog;
    DBAdapter dba;
    TIPS_Config tips;

    AlertDialog m_alert;

    // 마지막 작업 체크하기 위한 변수(매입,판매 구분 필요)
    int workIndex = 0; //(0:포커스정상이탈시,1:매입포커스,2:판매포커스)

    /*
     * // loading more in listview int currentVisibleItemCount; private boolean
     * isEnd = false; private OnScrollListener customScrollListener = new
     * OnScrollListener() {
     *
     * @Override public void onScroll(AbsListView view, int firstVisibleItem,
     * int visibleItemCount, int totalItemCount) {
     *
     * currentVisibleItemCount = visibleItemCount;
     *
     * if((firstVisibleItem + visibleItemCount) == totalItemCount &&
     * firstVisibleItem != 0) isEnd = true; else isEnd = false; }
     *
     * @Override public void onScrollStateChanged(AbsListView view, int
     * scrollState) { if (isEnd && currentVisibleItemCount > 0 && scrollState ==
     * OnScrollListener.SCROLL_STATE_IDLE) { doSearch(); } } };
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_product_modfiy);

        mContext = this;

//		//----------------------------------------//
//		// Toolbar
//		//----------------------------------------//
//		ImageView leftIcon = findViewById(R.id.toolbar_left_icon);
//		TextView toolbarTitle = findViewById(R.id.toolbar_title_text);
//		Button toobarSetting = findViewById(R.id.toolbar_setting_button);
//
//		leftIcon.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				finish();
//			}
//		});
//		toobarSetting.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				startActivity(new Intent(ManageProductActivityModfiy.this,TIPSPreferences.class));
//
//			}
//		});
//
//		toolbarTitle.setText("상품관리");
//		//----------------------------------------//

        ccp = new CalculateChangePrice();
        m_shop = LocalStorage.getJSONObject(this, "currentShopData");

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
            m_officeCode = m_shop.getString("OFFICE_CODE");
            m_posID = LocalStorage.getString(this, "currentPosID:" + OFFICE_CODE);

            /* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
            m_KeypadType = LocalStorage.getInt(this, "KeypadType:" + OFFICE_CODE);

            //----------------------------------------//
            // 여기에서 바코그프린터 관련 로컬설정값 가져오자
            //----------------------------------------//
            mSaleYn = LocalStorage.getString(this, "BarcodePrinterSaleYN");        //행사가 옵션 설정값(1:행사가,0:)
            mSellPri1 = LocalStorage.getString(this, "BarcodePrinterSellPri1");    //권장소비자가 설정값(0:기본,1:권장소비자가)

            //분류는 기본 대분류
            mSelectBranchName = 1;
            mSelectAddItem = 1; //추가항목구분(1:출력일,2:사용자정의문자

            //추가항목 출력일 금일로 정의
            SimpleDateFormat simpleDateFormat;
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            calendarPrintSetDate = Calendar.getInstance();
            mPrintSetDate = simpleDateFormat.format(calendarPrintSetDate.getTime());

            mPrintUserText = LocalStorage.getString(this, "AddItemUserText");// 추가항목 사용자정의 문자("")
            mPrintDateTitle = LocalStorage.getString(this, "BarcodePrinterDateTitle"); // 출력일 문구출력(1:출력일 문구출력,0:없음)

            isSelfBarcodeUse = LocalStorage.getBoolean(this, "SelfBarcodePrefixUse:" + OFFICE_CODE);
            selfBarcodePrefixCode = LocalStorage.getString(this, "SelfBarcodePrefixCode:" + OFFICE_CODE);
            //----------------------------------------//

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //mContext = getApplicationContext();
        upu = new UserPublicUtils(mContext);

        tips = new TIPS_Config(mContext);

        barcodePrinterCheck = tips.getSELECTPRINT();
        barcodePrinterAddress = tips.getBARPRINTER_ADDRESS();
        barcodePrinterPort = tips.getBARPRINTER_PORT();

        // 프린터구분(명) 구하기
        switch (barcodePrinterCheck) {
            case 0:
                //사용안함
                barcodePrinterName = "사용안함";
                break;
            case 1:
                //매장프린터
                barcodePrinterName = "매장프린터";
                break;
            case 2:
                //spp-l3000
                barcodePrinterName = "SPP-L3000";
                break;
            case 3:
                //LK-P30II
                //showSelectedLKP30II();
                barcodePrinterName = "LK-P30II";
                break;
        }

        if (barcodePrinterPort.equals("")) {
            barcodePrinterPort = "8671";
            tips.setBARPRINTER_PORT(barcodePrinterPort);
        }

        barcodePrinterGubun = tips.SELECTPRINT_GUBUN;

        // 환경설정 관련 옵션 아이템 불러오기
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        m3Mobile = pref.getBoolean("m3mobile", false);


        //첫번째 context, 두번째 데이터베이스명(String)
        Log.i("상품등록", m_officeCode + ".tips");
        dba = new DBAdapter(getApplicationContext(), m_officeCode + ".tips");


        m_registButton = (Button) findViewById(R.id.buttonProductRegist); // 등록버튼
        m_registButton.setText("등록");
        m_registButton.setTag("reg");


        Button renewButton = (Button) findViewById(R.id.buttonProductRenew); // 초기화버튼
        m_modfiyButton = (Button) findViewById(R.id.buttonProductModify); // 가격비교버튼
        m_recoveryButton = (Button) findViewById(R.id.buttonProductRecovery); // 복구버튼

        // 등록 버튼 클릭
        m_registButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // deleteListViewAll();
                String tag = v.getTag().toString();
                if (tag.equals("reg")) {
                    doRegister();
                }
                if (tag.equals("modify")) {
                    doModify();
                }
            }
        });

        // 가격비교 버튼 클릭
        m_modfiyButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                // 가격비교 검색
                Intent intent = new Intent(ManageProductActivityModfiy.this, ManageProductPriceDetailActivity.class);
                intent.putExtra("Shop_Area", "전체");
                intent.putExtra("OFFICE_CODE", m_officeCode);
                intent.putExtra("fillMaps", m_tempProduct);
                startActivity(intent);

            }
        });


        //----------------------------------------//
        //2023-01-04 바코드프린터 관련 추가
        //----------------------------------------//
        // 바코드프린터 설정버튼
        buttonPrinterSetting = (Button) findViewById(R.id.button_printer_setting);
        buttonPrinterSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                printerSettingPage();
            }
        });

        // 바코드프린터 옵션버튼
        buttonPrinterOption = (Button) findViewById(R.id.button_printer_option);
        buttonPrinterOption.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showPronterOption();
            }
        });

        // 택 발행 버튼
        buttonPrinterPrint = (Button) findViewById(R.id.button_printer_print);
        buttonPrinterPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 출력기능 추가 예정
                barcodePrinterMain();
            }
        });

        //프린터 연결 상태
        textViewPrinterState = (TextView) findViewById(R.id.textview_printer_state);
        textViewPrinterState.setText(tips.getSELECTPRINT_NAME());

        // 프린터 선택 리스트
        spinnerSelectPrinter = (Spinner) findViewById(R.id.spinner_select_printer);
        spinnerSelectPrinter.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LocalStorage.setInt(mContext, "SelectBarcodePrinter", i);
                String tempData = ((Spinner) findViewById(R.id.spinner_select_printer)).getSelectedItem().toString();
                if (tempData.contains("|") == true) {
                    try {
                        String[] printerInfo = tempData.split("\\|");
                        barcodePrinterName = printerInfo[0];
                        barcodePrinterAddress = printerInfo[1];
                        barcodePrinterPort = printerInfo[2];
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "바코드프린터 선택이 안되었습니다.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    barcodePrinterName = tempData;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //----------------------------------------//


        // 복구 버튼 클릭
        m_recoveryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFormView(m_tempProPurSell);
            }
        });

        // 초기화(새로입력) 버튼 클릭
        renewButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doClear();
            }
        });


        m_barcode = getIntent().getStringExtra("barcode"); // 넘어온바코드
        productReg_YN = getIntent().getStringExtra("productRegYN");
        mGubun = getIntent().getStringExtra("Gubun");        //출처구분  -상품//매입
        String officeCode = getIntent().getStringExtra("officeCode"); // 오피스코드
        String officeName = getIntent().getStringExtra("officeName"); // 오피스명
        Log.d("오피스 정보", officeCode + " " + officeName);

        m_textBarcode = (EditText) findViewById(R.id.editTextBarcode); // 바코드

        // 김영목 2020.09.14. 키패드입력 옵션 설정에 따라 적용 추가
        if (m_KeypadType == 0) { // 숫자키패드
            m_textBarcode.setInputType(2);
        }
        if (m_KeypadType == 1) { // 문자키패드
            m_textBarcode.setInputType(145); //textVisiblePassword ) 0x00000091
        }

        m_textProductName = (EditText) findViewById(R.id.editTextProductName); // 상품명
        m_textCustomerCode = (EditText) findViewById(R.id.editTextCustomerCode); // 거래처코드
        m_textCustomerName = (EditText) findViewById(R.id.editTextCustomerName); // 거래처명

        m_buttonbarcode = (Button) findViewById(R.id.buttonBarcode); // 바코드검색버튼

        m_textStandard = (EditText) findViewById(R.id.editTextStandard); // 규격
        m_textAcquire = (EditText) findViewById(R.id.editTextAcquire); // 입수

        buttonAcquire = (Button) findViewById(R.id.buttonAcquire); // 입수등록

        buttonAcquire.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //입수 등록 버튼
                setAcquireData();
            }
        });

        m_textPurchasePrice = (EditText) findViewById(R.id.editTextPurchasePrice); // 매입가
        m_textPurchasePriceOriginal = (EditText) findViewById(R.id.editTextPurchasePriceOriginal); // 매입원가
        m_textPurchasePriceOriginal.setEnabled(false);

        /*
         * 2014-12-18 부가세 별도 포함 옵션 추가
         */
        m_checkboxPurPrivatGubun = (CheckBox) findViewById(R.id.checkbox_purpritaxgubun); // 부가세
        // 포함,별도
        // 선택
        m_checkboxPurPrivatGubun.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO VAT_CHK 0:미포함 1:포함
                if (isChecked) {
                    m_tempProduct.put("VAT_CHK", "1");
                } else {
                    m_tempProduct.put("VAT_CHK", "0");
                }
            }
        });

        m_textPurchasePriceOriginalTax = (EditText) findViewById(R.id.editTextPurchasePriceOriginalTax); // 부가세
        m_textPurchasePriceOriginalTax.setEnabled(false);
        m_textSalesPrice = (EditText) findViewById(R.id.editTextSalesPrice); // 판매가
        //2022-05-26 권장소비자가 추가
        m_editTextSellPri1 = (EditText) findViewById(R.id.editTextSell_Pri1);

        m_textSalesPrice.setNextFocusDownId(R.id.editTextDifferentRatio); //이익률로 포커스 이동한다
        m_textDifferentRatio = (EditText) findViewById(R.id.editTextDifferentRatio); // 이익률
        m_textDifferentRatio.setNextFocusDownId(R.id.editTextASell_Pri); //이익률로 포커스 이동한다

        //2018-02-20 도매단가 추가하기
        m_textASell_Pri = (EditText) findViewById(R.id.editTextASell_Pri);
        m_textADifferentRatio = (EditText) findViewById(R.id.editTextADifferentRatio);
        m_textBSell_Pri = (EditText) findViewById(R.id.editTextBSell_Pri);
        m_textBDifferentRatio = (EditText) findViewById(R.id.editTextBDifferentRatio);
        m_textCSell_Pri = (EditText) findViewById(R.id.editTextCSell_Pri);
        m_textCDifferentRatio = (EditText) findViewById(R.id.editTextCDifferentRatio);


        //2018-02-20 도매가 기능 추가
        // 판매가 변경시 -> 매입가 + 판매가로 이익률
        m_textASell_Pri.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setChangeBackGroundColor(0, m_textASell_Pri);
                    m_tempProduct.put("Sell_APri", m_textASell_Pri.getText().toString().replace(",", ""));
                    updateFormView(ccp.calculateDomeSellPriBySellPrice(m_tempProduct, 0));

                    //upu.hideSoftKeyboard(false);
                } else {
                    setChangeBackGroundColor(1, m_textASell_Pri);
                }
            }
        });


        //2019-02-01 행사가 추가하기
        m_textEventPurPri = (EditText) findViewById(R.id.editTextEventPurchasePrice);
        m_textEventSalePri = (EditText) findViewById(R.id.editTextEventSalesPrice);

        m_textStartEventDate = (EditText) findViewById(R.id.editTextEventStartDate);
        m_textEndEventDate = (EditText) findViewById(R.id.editTextEventEndDate);


        // 이익률 변경시 -> 매입가 + 이익률로 판매가
        m_textADifferentRatio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // calculateSellPriByProfitRate();
                    setChangeBackGroundColor(0, m_textADifferentRatio);
                    String ratio = m_textADifferentRatio.getText().toString();
                    m_tempProduct.put("Profit_Rate_APri", ratio);
                    updateFormView(ccp.calculateDomeSellPriByProfitRate(m_tempProduct, 0));

                } else {
                    setChangeBackGroundColor(1, m_textADifferentRatio);
                }
            }
        });


        // 판매가 변경시 -> 매입가 + 판매가로 이익률
        m_textBSell_Pri.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setChangeBackGroundColor(0, m_textBSell_Pri);
                    m_tempProduct.put("Sell_BPri", m_textBSell_Pri.getText().toString().replace(",", ""));
                    updateFormView(ccp.calculateDomeSellPriBySellPrice(m_tempProduct, 1));

                    //upu.hideSoftKeyboard(false);
                } else {
                    setChangeBackGroundColor(1, m_textBSell_Pri);
                }
            }
        });


        // 이익률 변경시 -> 매입가 + 이익률로 판매가
        m_textBDifferentRatio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // calculateSellPriByProfitRate();
                    setChangeBackGroundColor(0, m_textBDifferentRatio);
                    String ratio = m_textBDifferentRatio.getText().toString();
                    m_tempProduct.put("Profit_Rate_BPri", ratio);
                    updateFormView(ccp.calculateDomeSellPriByProfitRate(m_tempProduct, 1));

                } else {
                    setChangeBackGroundColor(1, m_textBDifferentRatio);
                }
            }
        });


        // 판매가 변경시 -> 매입가 + 판매가로 이익률
        m_textCSell_Pri.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setChangeBackGroundColor(0, m_textCSell_Pri);
                    m_tempProduct.put("Sell_CPri", m_textCSell_Pri.getText().toString().replace(",", ""));
                    updateFormView(ccp.calculateDomeSellPriBySellPrice(m_tempProduct, 2));

                    //upu.hideSoftKeyboard(false);
                } else {
                    setChangeBackGroundColor(1, m_textCSell_Pri);
                }
            }
        });


        // 이익률 변경시 -> 매입가 + 이익률로 판매가
        m_textCDifferentRatio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // calculateSellPriByProfitRate();
                    setChangeBackGroundColor(0, m_textCDifferentRatio);
                    String ratio = m_textCDifferentRatio.getText().toString();
                    m_tempProduct.put("Profit_Rate_CPri", ratio);
                    updateFormView(ccp.calculateDomeSellPriByProfitRate(m_tempProduct, 2));

                } else {
                    setChangeBackGroundColor(1, m_textCDifferentRatio);
                }
            }
        });

        m_textRealSto = (EditText) findViewById(R.id.editTextRealSto); // 재고
        m_textRealSto.setEnabled(false);

        m_buttonCustomerClassification1 = (Button) findViewById(R.id.buttonClassificationType1); // 대명
        m_buttonCustomerClassification2 = (Button) findViewById(R.id.buttonClassificationType2); // 중명
        m_buttonCustomerClassification3 = (Button) findViewById(R.id.buttonClassificationType3); // 소명

        m_spinTaxation = (Spinner) findViewById(R.id.spinnerTaxationType); // 면과세
        m_spinTaxation.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                m_tempProduct.put("Tax_YN", position + "");
                updateFormView(ccp.tax_YnChooseSelected(m_tempProduct));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        m_spinGoods = (Spinner) findViewById(R.id.spinnerGoodsUse); // 사용구분
        m_spinGoods.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) { // 사용
                    m_tempProduct.put("Goods_Use", "1");
                }
                if (position == 1) { // 미사용
                    m_tempProduct.put("Goods_Use", "0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

		/*m_spinGroup = (Spinner) findViewById(R.id.spinnerGroupType); // 그룹구분
		m_spinGroup.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				m_tempProduct.put("G_grade", position + "");
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}
		});*/

        m_spinMemberPoint = (Spinner) findViewById(R.id.spinnerMemberPoint); // 마일리지
        m_spinMemberPoint.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                m_tempProduct.put("Point_Use", position + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        m_spinGoodsType = (Spinner) findViewById(R.id.spinnerGoodsTypeReg); // 상품구분
        m_spinGoodsType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                m_tempProduct.put("Scale_Use", position + "");
                if (position == 1) {
                    m_textPurchasePrice.setEnabled(false);
                    m_textSalesPrice.setEnabled(false);
                    //2022-05-26 권장소비자가 추가
                    m_editTextSellPri1.setEnabled(false);

                    m_textDifferentRatio.setEnabled(false);
                    m_textPurchasePrice.setText("0");
                    m_textPurchasePriceOriginal.setText("0");
                    m_textPurchasePriceOriginalTax.setText("0");
                    m_textSalesPrice.setText("0");
                    //2022-05-26 권장소비자가 추가
                    m_editTextSellPri1.setText("0");

                    m_textDifferentRatio.setText("0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });


        //2020-06-15
        m_spinGoodsDanwi = (Spinner) findViewById(R.id.spinnerGoodsDanwi);   // 단위
        m_spinGoodsDanwi.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                //m_tempProduct.put("Unit", position + "");
                m_tempProduct.put("Unit", m_spinGoodsDanwi.getSelectedItem().toString());

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
        m_TextCon_Rate = (EditText) findViewById(R.id.editTextCon_Rate); // 내용량
        m_TextCon_Rate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setChangeBackGroundColor(0, m_TextCon_Rate);

                    if (!"".equals(m_TextCon_Rate.getText().toString())) {
                        String s_val = String.format("%.2f", Float.parseFloat(m_TextCon_Rate.getText().toString()));
                        s_val = s_val.replace(".00", "");
                        m_TextCon_Rate.setText(s_val);
                    }

                    m_tempProduct.put("Con_Rate", m_TextCon_Rate.getText().toString());
                } else {
                    setChangeBackGroundColor(1, m_TextCon_Rate);
                }
            }
        });

        m_TextStd_Rate = (EditText) findViewById(R.id.editTextStd_Rate); // 기준용량
        m_TextStd_Rate.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setChangeBackGroundColor(0, m_TextStd_Rate);
                    m_tempProduct.put("Std_Rate", m_TextStd_Rate.getText().toString());

                    upu.hideSoftKeyboard(false);
                } else {
                    setChangeBackGroundColor(1, m_TextStd_Rate);
                }
            }
        });


        m_textEditDate = (EditText) findViewById(R.id.editTextEditDate);
        m_textEditModified = (EditText) findViewById(R.id.editTextEditModified);

        // 바코드 입력 후 포커스 딴 곳을 옮길 경우
        m_textBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String barcode = m_textBarcode.getText().toString();
                    m_tempProduct.put("BarCode", barcode);

                    if (!barcode.equals("")) // 입력한 Barcode가 값이 있을 경우만
                        doQueryWithBarcode();
                    if (barcode.length() == 4) {
                        m_textPurchasePrice.setEnabled(false);
                        m_textSalesPrice.setEnabled(false);
                        //2022-05-26 권장소비자가 추가
                        m_editTextSellPri1.setEnabled(false);

                        m_textDifferentRatio.setEnabled(false);
                        m_textPurchasePrice.setText("0");
                        m_textPurchasePriceOriginal.setText("0");
                        m_textPurchasePriceOriginalTax.setText("0");
                        m_textSalesPrice.setText("0");
                        //2022-05-26 권장소비자가 추가
                        m_editTextSellPri1.setText("0");

                        m_textDifferentRatio.setText("0");
                    }

                    setChangeBackGroundColor(0, m_textBarcode);
                } else {

                    // 바코드 배경색 색상변경
                    setChangeBackGroundColor(1, m_textBarcode);

                }
            }
        });

        // 거래처 코드 입력 후 포커스 딴 곳을 옮길 경우
        m_textCustomerCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    setChangeBackGroundColor(0, m_textCustomerCode);

                    String customerCode = m_textCustomerCode.getText().toString();
                    m_tempProduct.put("Bus_Code", customerCode);

                    if (!customerCode.equals("")) // 입력한 customerCode가 값이 있을 경우만
                        fillBusNameFromBusCode(customerCode);
                } else {
                    setChangeBackGroundColor(1, m_textCustomerCode);
                }
            }
        });

        // 상품명 입력후 포커스 이동일때
        m_textProductName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    m_tempProduct.put("G_Name", m_textProductName.getText().toString());
                    setChangeBackGroundColor(0, m_textProductName);
                } else {
                    setChangeBackGroundColor(1, m_textProductName);
                }
            }
        });

        // 거래처명 입력후 포커스 옮길 경우
        m_textCustomerName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setChangeBackGroundColor(0, m_textCustomerName);
                    m_tempProduct.put("Bus_Name", m_textCustomerName.getText().toString());
                } else {
                    setChangeBackGroundColor(1, m_textCustomerName);
                }
            }
        });

        // 규격 입력후 포커스 옮길 경우
        m_textStandard.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setChangeBackGroundColor(0, m_textStandard);
                    m_tempProduct.put("Std_Size", m_textStandard.getText().toString());
                } else {
                    setChangeBackGroundColor(1, m_textStandard);
                }
            }
        });


        // 매입원가 변경시 -> 매입가 + 이익률로 판매가
        /*
         * m_textPurchasePriceOriginal.setOnFocusChangeListener(new
         * View.OnFocusChangeListener() {
         *
         * @Override public void onFocusChange(View v, boolean hasFocus) {
         * if(!hasFocus){ String purchasePriceOriginal =
         * m_textPurchasePriceOriginal.getText().toString(); String
         * purchaseAddTax = m_textPurchasePriceOriginalTax.getText().toString();
         * String Sell_Pri = m_tempProduct.get("Sell_Pri");
         *
         * if(!purchasePriceOriginal.equals("") &&
         * !purchasePriceOriginal.equals("0") &&
         * !purchasePriceOriginal.equals("0.00")) { float
         * f_purchasePriceOriginal = Float.parseFloat(purchasePriceOriginal);
         * float f_purchasePrice =
         * f_purchasePriceOriginal+f_purchasePriceOriginal/10;
         * m_tempProduct.put("Pur_Cost", String.format("%.2f",
         * f_purchasePriceOriginal)); // 매입원가 m_tempProduct.put("Pur_Pri",
         * String.format("%.2f", f_purchasePrice)); // 매입가 }
         *
         * if(!purchasePriceOriginal.equals("") && !Sell_Pri.equals("")) {
         *
         * float f_salesPrice = Float.parseFloat(Sell_Pri); float
         * f_purchasePriceOriginal = Float.parseFloat(purchasePriceOriginal);
         * float f_purchasePrice =
         * f_purchasePriceOriginal+f_purchasePriceOriginal/10; float f_ratio =
         * (f_salesPrice - f_purchasePrice) / f_salesPrice ;
         * m_tempProduct.put("Profit_Rate", String.format("%.2f", f_ratio*100));
         * // 이익률 } updateFormView(m_tempProduct); } } });
         */

        // 매입가 변경시 -> 매입가 + 판매가로 이익률
        m_textPurchasePrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setChangeBackGroundColor(0, m_textPurchasePrice);
                    m_tempProduct.put("Pur_Pri", m_textPurchasePrice.getText().toString());
                    updateFormView(ccp.calculateSellPriByPurprice(m_tempProduct));
                    workIndex = 0;
                } else {
                    setChangeBackGroundColor(1, m_textPurchasePrice);
                    workIndex = 1;
                }
            }
        });

        // 판매가 변경시 -> 매입가 + 판매가로 이익률
        m_textSalesPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    setChangeBackGroundColor(0, m_textSalesPrice);
                    m_tempProduct.put("Sell_Pri", m_textSalesPrice.getText().toString().replace(",", ""));
                    updateFormView(ccp.calculateSellPriBySellPrice(m_tempProduct));
                    workIndex = 0;
                    upu.hideSoftKeyboard(false);
                } else {
                    setChangeBackGroundColor(1, m_textSalesPrice);
                    workIndex = 2;
                }
            }
        });

        //2022-05-26 권장소비자가 추가
        m_editTextSellPri1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (!b) {
                    setChangeBackGroundColor(0, m_editTextSellPri1);
                    m_tempProduct.put("Sell_Pri", m_editTextSellPri1.getText().toString().replace(",", ""));
                    //updateFormView(ccp.calculateSellPriBySellPrice(m_tempProduct));
                    upu.hideSoftKeyboard(false);
                } else {
                    setChangeBackGroundColor(1, m_editTextSellPri1);
                }
            }
        });


        // 이익률 변경시 -> 매입가 + 이익률로 판매가
        m_textDifferentRatio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // calculateSellPriByProfitRate();
                    setChangeBackGroundColor(0, m_textDifferentRatio);
                    String ratio = m_textDifferentRatio.getText().toString();
                    m_tempProduct.put("Profit_Rate", ratio);
                    updateFormView(ccp.calculateSellPriByProfitRate(m_tempProduct));

                } else {
                    setChangeBackGroundColor(1, m_textDifferentRatio);
                }
            }
        });

        m_textDifferentRatio.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) { // IME_ACTION_SEARCH
                    // ,
                    // IME_ACTION_GO
                    /* calculateSellPriByProfitRate(); */
                    String ratio = m_textDifferentRatio.getText().toString();
                    m_tempProduct.put("Profit_Rate", ratio);
                    updateFormView(ccp.calculateSellPriByProfitRate(m_tempProduct));

                    InputMethodManager imm = (InputMethodManager) getSystemService(
                            getApplicationContext().INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(m_textDifferentRatio.getWindowToken(), 0);
                }
                return false;
            }
        });


        m_barprint = (Button) findViewById(R.id.buttonProductBarprint);
        m_barprintTran = (Button) findViewById(R.id.buttonProductBarprintTran);

        m_barprint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //택저장

                if ("".equals(m_textBarcode.getText().toString())) {
                    Toast.makeText(mContext, "검색된 상품이 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                m_tempProduct.put("Count", "1");
                //----------------------------------------//
                // 2021.01.08.김영목. 원판매가 추가
                //----------------------------------------//
                //m_tempProduct.put("Sell_Pri", m_textSalesPrice.getText().toString().replace(",", ""));
                m_tempProduct.put("Sell_Org", m_tempProduct.get("Sell_Pri"));
                //----------------------------------------//

                mfillMapsBar.add(m_tempProduct);

                Toast.makeText(mContext, "저장 완료", Toast.LENGTH_SHORT).show();
            }
        });

        m_barprintTran.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //택전송
                //DB에 저장합니다.

                if (mfillMapsBar.size() <= 0) {
                    Toast.makeText(mContext, "저장된 상품이 없습니다.", Toast.LENGTH_SHORT).show();
                }

                dba.insert_barPrint(mfillMapsBar, "0");
                mfillMapsBar.removeAll(mfillMapsBar);
                Toast.makeText(mContext, "전송 완료", Toast.LENGTH_SHORT).show();
            }
        });


        //분류 초기화
        fetchLName();

        //전체 필드 초기화
        doClear();

        if (!m_barcode.isEmpty()) { // 검색에서 넘어왔을경우

            m_textProductName.requestFocus();

            m_textBarcode.setText(m_barcode);
            m_textBarcode.setEnabled(false);
            m_buttonbarcode.setEnabled(false);

            m_textCustomerCode.setText(officeCode);
            m_textCustomerName.setText(officeName);

            if (productReg_YN.equals("Y")) {
                m_tempProduct.put("BarCode", m_barcode);
            } else {
                doQueryWithBarcode();
            }
            m_barcode = "";
        }


        // intent filter 2017-04 m3mobile
        if (m3Mobile) {
			/*IntentFilter filter = new IntentFilter();
			filter.addAction(SCANNER_ACTION_BARCODE);
			registerReceiver(BarcodeIntentBroadcast, filter);*/

            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textBarcode, m_textProductName);
        }

        switch (tips.getSELECTPRINT()) {

            case 2:
                mBixolonLabelPrinter = new BixolonLabelPrinter(this, mHandler, null);
                break;
            case 3:
                //zplprinter = new ZPLRFIDPrinter();
                bluetoothPort = BluetoothPort.getInstance();
                blueConnect = new BluetoothConnectMenu(ManageProductActivityModfiy.this, bluetoothPort);
                break;
        }


        // 바코드프린터 목록 불러오기
        getBarcodePrinterSet();

    }

    //입수 등록 버튼
    public void setAcquireData() {

        String type = m_registButton.getTag().toString();

        if (!"modify".equals(type)) {
            //안내창을 띄웁니다.
            Toast.makeText(this, "등록된 상품만 박스상품 등록이 가능합니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        //박스 상품을 신규로 등록 합니다.
        //바코드를 넘겨 줍니다.
        String barcode = m_textBarcode.getText().toString();
        String obtain = m_textAcquire.getText().toString();

        if ("".equals(obtain) || obtain == null) {
            Toast.makeText(mContext, "입수를 입력해 주세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ManagerProductBox.class);
        intent.putExtra("barcode", barcode);
        intent.putExtra("obtain", obtain);
        startActivityForResult(intent, CUSTOMER_BOXPRODUCT_REQUEST);

    }

    /*
     * // 이익률변경시 계산기 private void calculateSellPriByProfitRate () { String ratio
     * = m_textDifferentRatio.getText().toString(); String Pur_Pri =
     * m_textPurchasePrice.getText().toString();
     *
     * if( !ratio.equals("") && !Pur_Pri.equals("") ){ float f_ratio =
     * Float.valueOf(ratio).floatValue()/100; float f_purchasePrice =
     * Float.parseFloat(Pur_Pri); float f_salesPrice = f_purchasePrice/(1 -
     * f_ratio);
     *
     * // 반올림 수행 int rest = ((int)(f_salesPrice /10.0f))*10; int one =
     * (int)f_salesPrice - rest; if (one>=5) rest+=10;
     *
     * //반올림 수행해서 역으로 이익률 계산 f_ratio = (rest - f_purchasePrice) / rest;
     *
     * m_tempProduct.put("Profit_Rate", String.format("%.2f", f_ratio*100)); //
     * 이익률 m_tempProduct.put("Sell_Pri", String.format("%d", rest)); // 판매가
     *
     * updateFormView(m_tempProduct); } }
     */

    // 입력 폼 채우기
    public void updateFormView(HashMap<String, String> object) {

        m_textBarcode.setText(object.get("BarCode")); // 바코드
        m_textProductName.setText(object.get("G_Name")); // 상품명
        m_textCustomerCode.setText(object.get("Bus_Code")); // 거래처코드
        m_textCustomerName.setText(object.get("Bus_Name")); // 거래처명
        m_textStandard.setText(object.get("Std_Size")); // 규격
        try {
            if (object.get("Obtain").equals("") || object.get("Obtain") == null) {
                m_textAcquire.setText("0"); // 입수
            } else {
                m_textAcquire.setText(StringFormat.convertToNumberFormat(object.get("Obtain"))); // 입수
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        m_textPurchasePrice.setText(object.get("Pur_Pri")); // 매입가
        m_textSalesPrice.setText(StringFormat.convertToNumberFormat(object.get("Sell_Pri"))); // 판매가
        //2022-05-26 권장소비자가 추가
        m_editTextSellPri1.setText(StringFormat.convertToNumberFormat(object.get("Sell_Pri1"))); // 권장소비자가

        m_textPurchasePriceOriginal.setText(object.get("Pur_Cost")); // 매입원가
        m_textPurchasePriceOriginalTax.setText(object.get("Add_Tax")); // 부가세

        /*
         * 2014-12-18 부가세 포함, 별도 옵션 체크
         *
         */

        if (object.get("VAT_CHK").equals("1"))
            m_checkboxPurPrivatGubun.setChecked(true);
        else
            m_checkboxPurPrivatGubun.setChecked(false);

        m_textDifferentRatio.setText(object.get("Profit_Rate")); // 이익률
        m_textRealSto.setText(object.get("Real_Sto")); // 재고
        m_buttonCustomerClassification1.setText(object.get("L_Name")); // 대명
        if (!object.get("L_Code").equals("")) {
            m_tempProduct.put("L_Code", object.get("L_Code"));
        }
        m_buttonCustomerClassification2.setText(object.get("M_Name")); // 중명
        if (!object.get("M_Code").equals("")) {
            m_tempProduct.put("M_Code", object.get("M_Code"));
        }
        m_buttonCustomerClassification3.setText(object.get("S_Name")); // 소명
        if (!object.get("S_Code").equals("")) {
            m_tempProduct.put("S_Code", object.get("S_Code"));
        }

        if (object.get("Tax_YN").equals("0")) // 면과세
            m_spinTaxation.setSelection(0);
        else
            m_spinTaxation.setSelection(1);

        /*
         * if(object.get("VAT_CHK").equals("0"))
         * m_checkSurtax.setChecked(false); else m_checkSurtax.setChecked(true);
         */

        if (object.get("Goods_Use").equals("1")) // 사용구분
            m_spinGoods.setSelection(0);
        else
            m_spinGoods.setSelection(1);

		/*if (object.containsKey("G_grade")) { // 그룹구분
			int a = 1; // A그룹
			String b = object.get("G_grade");
			if (b != "0") {
				for (int i = 65; i >= 90; i++) {
					if (b == String.valueOf(i)) {
						m_spinGroup.setSelection(a);
					}
					a++;
				}
			} else {
				m_spinGroup.setSelection(0);
			}
		} else {
			m_tempProduct.put("G_grade", "0");
			m_spinGroup.setSelection(0);
		}*/

        if (object.get("Point_Use").equals("0")) // 포인트사용여부
            m_spinMemberPoint.setSelection(0);
        else
            m_spinMemberPoint.setSelection(1);

        if (object.get("Scale_Use").equals("0")) // 상품구분
            m_spinGoodsType.setSelection(0); // 공산품
        else
            m_spinGoodsType.setSelection(1); // 저울상품

        //2020-06-15 단위 내용량 기준단위
        String sDanwi = object.get("Unit").replace(" ", "");
        int iUnit = 0;
        switch (sDanwi) {
            case "kg":
                iUnit = 0;
                break;
            case "g":
                iUnit = 1;
                break;
            case "L":
                iUnit = 2;
                break;
            case "ml":
                iUnit = 3;
                break;
            case "개":
                iUnit = 4;
                break;
            case "M":
                iUnit = 5;
                break;
            case "매":
                iUnit = 6;
                break;
            case "장":
                iUnit = 7;
                break;
        }
        m_spinGoodsDanwi.setSelection(iUnit);

        String s_val = String.format("%.2f", Float.parseFloat(object.get("Con_Rate")));
        s_val = s_val.replace(".00", "");
        m_TextCon_Rate.setText(s_val);
        m_TextStd_Rate.setText(object.get("Std_Rate"));

        m_textEditDate.setText(object.get("Edit_Date"));
        m_textEditModified.setText(object.get("Editor"));

        if (object.get("BarCode").length() == 4 || object.get("Scale_Use").equals("1")) {
            m_textPurchasePrice.setEnabled(false);
            m_textSalesPrice.setEnabled(false);
            //2022-05-26 권장소비자가 추가
            //m_editTextSellPri1.setEnabled(false);
            m_textDifferentRatio.setEnabled(false);
        }

        //2018-2-20 도매단가 입력
        m_textASell_Pri.setText(object.get("Sell_APri"));
        m_textADifferentRatio.setText(object.get("Profit_Rate_APri"));
        m_textBSell_Pri.setText(object.get("Sell_BPri"));
        m_textBDifferentRatio.setText(object.get("Profit_Rate_BPri"));
        m_textCSell_Pri.setText(object.get("Sell_CPri"));
        m_textCDifferentRatio.setText(object.get("Profit_Rate_CPri"));

        //2019-02-01
        m_textEventPurPri.setText(object.get("Sale_Pur"));
        m_textEventSalePri.setText(object.get("Sale_Sell"));

        m_textStartEventDate.setText(object.get("Sale_SDate"));
        m_textEndEventDate.setText(object.get("Sale_EDate"));

        //행사중 표시여부
		/*String sdate = m_tempProduct.get("Sale_SDate");
		String edate = m_tempProduct.get("Sale_EDate");

		SimpleDateFormat transFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date toStart;
		Date toEnd;

		long now = System.currentTimeMillis();
		Date today = new Date(now);

		try {
			toStart = transFormat.parse(sdate);
			toEnd = transFormat.parse(edate);

			if(toStart.after(today) && toEnd.before(today) ){
				this.setTitle("상품등록수정 (행사중)");
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}*/


    }

    // 입력폼 초기화
    public void doClear() {

        workIndex = 0;

        newTempProduct();
        updateFormView(m_tempProduct);
        productNewRegisterYN("Y");
        this.setTitle("상품관리");
        m_textBarcode.requestFocus();
    }

    // 상품 데이터 초기화
    public void newTempProduct() {
        m_tempProduct = new HashMap<String, String>();
        m_tempProduct.put("BarCode", ""); // 1.바코드
        m_tempProduct.put("G_Name", ""); // 2.상품명
        m_tempProduct.put("Bus_Code", ""); // 3.거래처코드
        m_tempProduct.put("Bus_Name", ""); // 4.거래처명
        m_tempProduct.put("Std_Size", ""); // 5.규격
        m_tempProduct.put("Obtain", "0"); // 6. 입수
        m_tempProduct.put("Pur_Pri", "0"); // 7. 매입가
        m_tempProduct.put("Sell_Pri", "0"); // 8.판매가

        //2022-05-26 권장소비자가 추가
        m_tempProduct.put("Sell_Pri1", "0"); // 권장소비자가

        m_tempProduct.put("Pur_Cost", "0"); // 9.매입원가
        m_tempProduct.put("Add_Tax", "0"); // 10.부가세
        m_tempProduct.put("VAT_CHK", "1"); // 부가세 별도 포함
        m_tempProduct.put("Profit_Rate", "0"); // 11. 이익률
        m_tempProduct.put("L_Code", "AA"); // 12.대코드
        m_tempProduct.put("L_Name", "매입대분류"); // 13.대명
        m_tempProduct.put("M_Code", "AA"); // 14.중코드
        m_tempProduct.put("M_Name", "매입대분류"); // 15.중명
        m_tempProduct.put("S_Code", "AAA"); // 16.소코드
        m_tempProduct.put("S_Name", "매입대분류"); // 17.소명
        m_tempProduct.put("Tax_YN", "1"); // 18. 면과세
        m_tempProduct.put("VAT_CHK", "1"); // 19. 부가세구분
        m_tempProduct.put("Goods_Use", "1"); // 20. 사용여부
        m_tempProduct.put("Point_Use", "1"); // 21. 고객마일리지
        m_tempProduct.put("Edit_Check", "1"); // 22.수정업데이트
        m_tempProduct.put("Tran_Chk", "1"); // 23.가격비교업데이트
        m_tempProduct.put("Scale_Use", "0"); // 24.상품구분
        m_tempProduct.put("G_grade", "0"); // 25.그룹구분
        m_tempProduct.put("Edit_Date", ""); // 26.수정일자
        m_tempProduct.put("Editor", ""); // 27.수정자
        m_tempProduct.put("Write_Date", ""); // 28.등록일
        m_tempProduct.put("Writer", ""); // 29.등록자

        m_tempProduct.put("ASell_Pri", ""); //30. 도매 A단가
        m_tempProduct.put("Profit_Rate_APir", ""); //31. 도매 A이익률
        m_tempProduct.put("BSell_Pri", ""); //32. 도매 A단가
        m_tempProduct.put("Profit_Rate_BPir", ""); //33. 도매 A이익률
        m_tempProduct.put("CSell_Pri", ""); //34. 도매 A단가
        m_tempProduct.put("Profit_Rate_CPir", ""); //35. 도매 A이익률

        m_tempProduct.put("Sale_Pur", "");    //36.행사매입가
        m_tempProduct.put("Sale_Sell", "");    //37.행사판매가
        m_tempProduct.put("Sale_SDate", "");    //38.행사시작일
        m_tempProduct.put("Sale_EDate", "");    //39.행사종료일

        //2020-06-15;
        m_tempProduct.put("Unit", "kg");
        m_tempProduct.put("Con_Rate", "10");
        m_tempProduct.put("Std_Rate", "100");
        m_tempProduct.put("Bot_Sell", "0");

        m_tempProPurSell.clear();


    }

    public void historyProductData(HashMap<String, String> object) {
        // 매입가 판매가 수정 변경시 비교해서 변경테이블에 저장하기
        m_tempProPurSell.put("Pur_Pri_old", object.get("Pur_Pri"));
        m_tempProPurSell.put("Sell_Pri_old", object.get("Sell_Pri"));
        m_tempProPurSell.putAll(object);
    }

    // 리스트 초기화
    /*
     * public void deleteListViewAll() { if (mfillMaps.isEmpty()) return;
     *
     * mfillMaps.removeAll(mfillMaps); m_adapter.notifyDataSetChanged(); }
     */

    public void doRegister() {

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
        String g_name = m_textProductName.getText().toString(); // 2.상품명
        String bus_code = m_textCustomerCode.getText().toString(); // 3.거래처코드
        String bus_name = m_textCustomerName.getText().toString(); // 4.거래처명

        /*
         * String lname = m_buttonCustomerClassification1.getText().toString();
         * //5.대명 String mname =
         * m_buttonCustomerClassification2.getText().toString(); //6.중명 String
         * sname = m_buttonCustomerClassification3.getText().toString(); //7.소명
         * String lcode = getCodeFromListByName(m_Ltype, lname); //대코드 String
         * mcode = getCodeFromListByName(m_Mtype, mname); //중코드 String scode =
         * getCodeFromListByName(m_Stype, sname); //소코드
         */

        String lname = m_tempProduct.get("L_Name"); // 5-1.대분류
        String mname = m_tempProduct.get("M_Name"); // 5-2.중분류
        String sname = m_tempProduct.get("S_Name"); // 5-3.소분류
        String lcode = m_tempProduct.get("L_Code");// 5.대코드
        String mcode = m_tempProduct.get("M_Code");// 5.중코드
        String scode = m_tempProduct.get("S_Code");// 5.소코드

        String tax_yn = m_spinTaxation.getSelectedItem().toString(); // 8.면과세
        String Std_Size = m_textStandard.getText().toString(); // 9.규격
        // String Obtain = m_textAcquire.getText().toString(); //입수
        String pur_pri = m_textPurchasePrice.getText().toString(); // 10.매입가
        String pur_cost = m_textPurchasePriceOriginal.getText().toString(); // 11.매입원가
        String add_tax = m_textPurchasePriceOriginalTax.getText().toString(); // 12.부가세
        String salesPrice = m_textSalesPrice.getText().toString().replace(",", ""); // 13.판매가
        //2022-05-26 권장소비자가 추가
        String salesPrice1 = m_editTextSellPri1.getText().toString().replace(",", ""); // 권장소비자가

        String ratio = m_textDifferentRatio.getText().toString(); // 14.이익률
        String vat_chk = null;// 부가세 포함, 별도 선택
        if (m_checkboxPurPrivatGubun.isChecked())
            vat_chk = "1";
        else
            vat_chk = "0";

        //2018-02-20 도매단가 기능 추가
        String aSell_Pri = m_textASell_Pri.getText().toString();
        String aProfit_Rate = m_textADifferentRatio.getText().toString();
        String bSell_Pri = m_textBSell_Pri.getText().toString();
        String bProfit_Rate = m_textBDifferentRatio.getText().toString();
        String cSell_Pri = m_textCSell_Pri.getText().toString();
        String cProfit_Rate = m_textCDifferentRatio.getText().toString();


        //String g_grade = String.valueOf(m_spinGroup.getSelectedItem().toString()); // 15.그룹구분
        // String good_use = String.format("%d", m_spinGoods.getSelectedItem()
        // ); //16.사용구분
        String good_type = String.valueOf(m_spinGoodsType.getSelectedItem().toString()); // 17.상품구분
        String memberpoint = String.valueOf(m_spinMemberPoint.getSelectedItem().toString()); // 17.상품구분

        //2020-06-15 단위 내용량 기준용량
        String g_Unit = String.valueOf(m_spinGoodsDanwi.getSelectedItem().toString()); // 17.상품구분
        String g_Con_Rate = String.valueOf(m_TextCon_Rate.getText().toString());
        String g_Std_Rate = String.valueOf(m_TextStd_Rate.getText().toString());

        if (g_Con_Rate.equals("")) {
            g_Con_Rate = "10";
        }

        if (g_Std_Rate.equals("")) {
            g_Std_Rate = "100";
        }


        if (pur_pri.equals("")) { // 매입가 미입력시
            pur_pri = "0";
            pur_cost = "0";
            add_tax = "0";
        }
        // String Add_Tax = String.format("%f",
        // Float.parseFloat(purchasePrice)-Float.parseFloat(purchasePriceOriginal));
        /*
         * if(m_checkSurtax.isChecked()) surtax = "1"; else surtax = "0";
         */

        if (tax_yn.equals("면세"))
            tax_yn = "0";
        else
            tax_yn = "1";

        // if(Obtain.equals("")) Obtain = "0";

        if (good_type.equals("공산품")) {
            good_type = "0";
        } else {
            good_type = "1";
        }

        if (memberpoint.equals("적용")) {
            memberpoint = "1";
        } else {
            memberpoint = "0";
        }

		/*if (g_grade.equals("None")) {
			g_grade = " G_Grade = '' ";
		} else {
			g_grade = " G_Grade = '" + g_grade + "' ";
		}*/


        if (aSell_Pri.equals("")) aSell_Pri = "0";
        if (aProfit_Rate.equals("")) aProfit_Rate = "0";

        if (bSell_Pri.equals("")) bSell_Pri = "0";
        if (bProfit_Rate.equals("")) bProfit_Rate = "0";

        if (cSell_Pri.equals("")) cSell_Pri = "0";
        if (cProfit_Rate.equals("")) cProfit_Rate = "0";


        if (barcode.equals("") || g_name.equals("") || bus_code.equals("") || bus_name.equals("") || lname.equals("")
                || mname.equals("") || sname.equals("") || salesPrice.equals("") || ratio.equals("")) {
            Toast.makeText(getApplicationContext(), "비어있는 필드가 있습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        query += "Insert Into Goods ( BarCode, G_Name, Write_Date, Writer, Real_Sto)" + "Values ('" + barcode + "', '"
                + g_name + "', '" + currentDate + "', '" + userID + "',0) ";

        query += " Update Goods Set "
                + "G_Name = '" + g_name + "', "
                + "Tax_YN = '" + tax_yn + "', "
                + "Obtain = 0, "
                + "Std_Size = '" + Std_Size + "', "
                + "Pur_Pri = " + pur_pri + ", "
                + "Pur_Cost = " + pur_cost + ", "
                + "Add_Tax = " + add_tax + ", "
                + "Sell_Pri = " + salesPrice + ", "
                + "Profit_Rate = " + ratio + ", "
                + "Sell_APri = '" + aSell_Pri + "', "
                + "Profit_Rate_APri = '" + aProfit_Rate + "', "
                + "Sell_BPri = '" + bSell_Pri + "', "
                + "Profit_Rate_BPri = '" + bProfit_Rate + "', "
                + "Sell_CPri = '" + cSell_Pri + "', "
                + "Profit_Rate_CPri = '" + cProfit_Rate + "', "
                + "Branch_Rate = 0, "
                + "SP_Check = '0', "
                + "Com_Rate = 0, "
                + "L_Code = '" + lcode + "', "
                + "L_Name = '" + lname + "', "
                + "M_Code = '" + mcode + "', "
                + "M_Name = '" + mname + "', "
                + "S_Code = '" + scode + "', "
                + "S_Name = '" + sname + "', "
                + "Bus_Code = '" + bus_code + "', "
                + "Bus_Name = '" + bus_name + "', "
                + "Sell_Pri1 = " + salesPrice1 + ", "
                + "Location = '', "
                + "Goods_Use = '1', "
                + "Pur_Use = '1', "
                + "Ord_Use = '1', "
                + "Sell_Use = '1', "
                + "Sto_Use = '1', "
                + "Scale_Use = '" + good_type + "', "
                + "Link_Use = '0', "
                + "Link_BarCode = '', "
                + "Pro_Sto = 0, "
                + "Sale_Pur = 0, "
                + "Sale_Sell = 0, "
                + "SaSP_Check = '0', "
                + "SaCom_Rate = 0, "
                + "Point_Use = '" + memberpoint + "', "
                + "Point_Mark = 0, "
                + "CashBack_Use = '0', "
                + "Sale_SDate = '', "
                + "Sale_EDate = '', "
                + "Sale_STime = '', "
                + "Sale_ETime = '', "
                + "Sale_PointUse = '1', "
                + "AmountSale_Use = '0', "
                + "Sale_Amount = 0, "
                + "Edit_Check = '1', "
                + "Tran_Chk = '1', "
                + "Sale_Use = '0', "
                + "Edit_Date = '" + currentDate + "', "
                + "Editor = '" + userID + "', "
                + "ALL_PerDC_USE = '1', "
                + "VAT_CHK = '" + vat_chk + "', "
                + "Bot_Code = '', "
                + "Bot_Name = '', "
                + "Bot_Pur = 0, "
                + "Bot_Sell = 0, "
                + "Col_Pri = 0, "
                + "Re_Pri = 0 "

                //2020-06-15
                + ", Unit = '" + g_Unit + "' "
                + ", Con_Rate = '" + g_Con_Rate + "' "
                + ", Std_Rate = '" + g_Std_Rate + "' "

                + "Where BarCode = '" + barcode + "' ";

        query += "Update TempPurchase Set Goods_Use = '1' Where BarCode = '" + barcode + "' ";

        query += "Select * From goods where barcode='" + barcode + "' ";

        //Log.d(query,"db goods");

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

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ManageProductActivityModfiy.this, AlertDialog.THEME_HOLO_LIGHT);
                    alertDialog.setMessage("정상적으로 등록되었습니다..");
                    alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            doClear();
                        }
                    });
                    alertDialog.show();
                } else {
                    Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();
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

    public String getCodeFromListByName(List<HashMap<String, String>> l, String name) {

        Iterator<HashMap<String, String>> it = l.iterator();
        while (it.hasNext()) {
            HashMap<String, String> obj = it.next();
            if (obj.get("name").equals(name)) {
                return obj.get("code");
            }
        }
        return "";
    }

    public void doModify() {

        HashMap<String, String> obj = m_tempProduct;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentDateTime = sdf.format(new Date());
        // 날짜와 시간 구하기
        String currentDate = currentDateTime.substring(0, 10);
        String currentTime = currentDateTime.substring(11);
        String writeDate = obj.get("Write_Date");
        if (writeDate == null)
            writeDate = currentDate;

        JSONObject userProfile = LocalStorage.getJSONObject(this, "userProfile");
        String userID = "", writeID = "";
        try {
            userID = userProfile.getString("User_ID");
            writeID = obj.get("Writer");
            if (writeID == null)
                writeID = userID;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String pur_pri_old = m_tempProPurSell.get("Pur_Pri");
        String sell_pri_old = m_tempProPurSell.get("Sell_Pri");

        String query = "";
        String barcode = m_textBarcode.getText().toString(); // 1.바코드
        String g_name = m_textProductName.getText().toString(); // 2.상품명
        String bus_code = m_textCustomerCode.getText().toString();// 3.거래처코드
        String bus_name = m_textCustomerName.getText().toString();// 4.거래처명

        String lname = m_buttonCustomerClassification1.getText().toString(); // 5-1.대분류
        String mname = m_buttonCustomerClassification2.getText().toString(); // 5-2.중분류
        String sname = m_buttonCustomerClassification3.getText().toString(); // 5-3.소분류
        String lcode = m_tempProduct.get("L_Code");// 5.대코드
        String mcode = m_tempProduct.get("M_Code");// 5.중코드
        String scode = m_tempProduct.get("S_Code");// 5.소코드

        String Tax_YN = m_spinTaxation.getSelectedItem().toString(); // 6.면과세

        String Std_Size = m_textStandard.getText().toString();// 9.규격
        //String Obtain = m_textAcquire.getText().toString();// 10.입수

        //----------------------------------------//
        // 김영목 2020.10.26. 수정버튼 바로 입력시 에러 때문에 재계산 추가(매입,판매 구분 필요)
        //----------------------------------------//
        if (workIndex == 1) {
            m_tempProduct.put("Pur_Pri", m_textPurchasePrice.getText().toString());
            updateFormView(ccp.calculateSellPriByPurprice(m_tempProduct));
        }
        //----------------------------------------//

        String pur_pri = m_textPurchasePrice.getText().toString();// 11.매입가
        String pur_cost = m_textPurchasePriceOriginal.getText().toString();// 12.매입원가
        String Add_Tax = m_textPurchasePriceOriginalTax.getText().toString();// 13.부가세

        String Vat_Chk = null;
        if (m_checkboxPurPrivatGubun.isChecked())
            Vat_Chk = "1";
        else
            Vat_Chk = "0";

        //----------------------------------------//
        // 김영목 2020.10.26. 수정버튼 바로 입력시 에러 때문에 재계산 추가(매입,판매 구분 필요)
        //----------------------------------------//
        if (workIndex == 2) {
            m_tempProduct.put("Sell_Pri", m_textSalesPrice.getText().toString().replace(",", ""));
            updateFormView(ccp.calculateSellPriBySellPrice(m_tempProduct));
        }
        //----------------------------------------//

        String salesPrice = m_textSalesPrice.getText().toString().replace(",", "");// 14.판매가
        //2022-05-26 권장소비자가 추가
        String salesPrice1 = m_editTextSellPri1.getText().toString().replace(",", "");// 권장소비자가

        String ratio = m_textDifferentRatio.getText().toString();// 15.이익률

        String aSell_Pri = m_textASell_Pri.getText().toString();
        String aProfit_Rate = m_textADifferentRatio.getText().toString();
        String bSell_Pri = m_textBSell_Pri.getText().toString();
        String bProfit_Rate = m_textBDifferentRatio.getText().toString();
        String cSell_Pri = m_textCSell_Pri.getText().toString();
        String cProfit_Rate = m_textCDifferentRatio.getText().toString();

        String goodsType = String.valueOf(m_spinGoodsType.getSelectedItemPosition()); // 18.상품구분
        String memberpoint = String.valueOf(m_spinMemberPoint.getSelectedItemPosition()); // 마일리지
        //String g_grade = m_spinGroup.getSelectedItem().toString(); // 그룹구분
        String good_use = String.valueOf(m_spinGoods.getSelectedItemPosition()); // 사용구분


        //2020-06-15 단위 내용량 기준용량
        String g_Unit = String.valueOf(m_spinGoodsDanwi.getSelectedItem().toString()); // 17.상품구분
        String g_Con_Rate = String.valueOf(m_TextCon_Rate.getText().toString());
        String g_Std_Rate = String.valueOf(m_TextStd_Rate.getText().toString());

        if (g_Con_Rate.equals("")) {
            g_Con_Rate = "10";
        }

        if (g_Std_Rate.equals("")) {
            g_Std_Rate = "100";
        }


        if (pur_pri.equals(""))
            pur_pri = "0";
        if (pur_cost.equals(""))
            pur_cost = "0";
        // String Add_Tax = String.format("%f",
        // Float.parseFloat(purchasePrice)-Float.parseFloat(purchasePriceOriginal));

		/*if (g_grade.equals("None")) {
			g_grade = " G_Grade = '' ";
		} else {
			g_grade = " G_Grade = '" + g_grade + "' ";
		}*/

        if (Tax_YN.equals("면세"))
            Tax_YN = "0";
        else
            Tax_YN = "1";

        if (good_use.equals("0"))
            good_use = "1";
        else
            good_use = "0";

        // if (Obtain.equals("")) Obtain = "0";

        if (aSell_Pri.equals("")) aSell_Pri = "0";
        if (aProfit_Rate.equals("")) aProfit_Rate = "0";

        if (aSell_Pri.equals("")) bSell_Pri = "0";
        if (aProfit_Rate.equals("")) bProfit_Rate = "0";

        if (aSell_Pri.equals("")) cSell_Pri = "0";
        if (aProfit_Rate.equals("")) cProfit_Rate = "0";

        if (barcode.equals("") || g_name.equals("") || bus_code.equals("") || bus_name.equals("") || lname.equals("")
                || mname.equals("") || sname.equals("") || pur_pri.equals("") || pur_cost.equals("")
                || salesPrice.equals("") || ratio.equals("")) {
            Toast.makeText(getApplicationContext(), "비어있는 필드가 있습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        query += "Update Goods Set "
                + "G_Name = '" + g_name + "', "
                + "Tax_YN = '" + Tax_YN + "', "
                //+ "Obtain = '" + Obtain + "', "
                + "Std_Size = '" + Std_Size + "', "
                + "Pur_Pri = '" + pur_pri + "', "
                + "Pur_Cost = '" + pur_cost + "', "
                + "Add_Tax = '" + Add_Tax + "', "
                + "Sell_Pri = '" + salesPrice + "', "
                + "Sell_Pri1 = '" + salesPrice1 + "', "
                + "Profit_Rate = '" + ratio + "', "
                + "Sell_APri = '" + aSell_Pri + "', "
                + "Profit_Rate_APri = '" + aProfit_Rate + "', "
                + "Sell_BPri = '" + bSell_Pri + "', "
                + "Profit_Rate_BPri = '" + bProfit_Rate + "', "
                + "Sell_CPri = '" + cSell_Pri + "', "
                + "Profit_Rate_CPri = '" + cProfit_Rate + "', "
                + "L_Code = '" + lcode + "', "
                + "L_Name = '" + lname + "', "
                + "M_Code = '" + mcode + "', "
                + "M_Name = '" + mname + "', "
                + "S_Code = '" + scode + "', "
                + "S_Name = '" + sname + "', "
                + "Bus_Code = '" + bus_code + "', "
                + "Bus_Name = '" + bus_name + "', "
                + "Goods_Use = '" + good_use + "', "
                + "Pur_Use = '" + good_use + "', "
                + "Ord_Use = '" + good_use + "', "
                + "Sell_Use = '" + good_use + "', "
                + "Sto_Use = '" + good_use + "', "
                + "Scale_Use = '" + goodsType + "', "
                + "Point_Use = '" + memberpoint + "', "
                + "CashBack_Use = '" + memberpoint + "', "
                + "Edit_Check = '1', Tran_Chk = '1', VAT_CHK = '" + Vat_Chk + "', "
                + "Edit_Date = '" + currentDate + "', "
                + "Editor = '" + userID + "' "

                //2020-06-15
                + ", Unit = '" + g_Unit + "' "
                + ", Con_Rate = '" + g_Con_Rate + "' "
                + ", Std_Rate = '" + g_Std_Rate + "' "

                + " Where BarCode = '" + barcode + "' ";
        if (pur_pri_old != pur_pri || sell_pri_old != salesPrice) {
            query += "Insert Into ProductPrice_History (P_Day, P_Time, P_Barcode, P_OldDanga, P_NewDanga, P_OldPrice, P_NewPrice, P_Gubun, P_PosNo, P_UserID) "
                    + "Values ('" + currentDate + "', '" + currentTime + "', '" + barcode + "', '" + pur_pri_old + "', "
                    + pur_pri + ", '" + sell_pri_old + "', " + salesPrice + ", '4', '" + m_posID + "', '" + userID
                    + "') ";
        }
        query += "Update TempPurchase Set Goods_Use = '1' Where BarCode = '" + barcode + "' ";

        query += "Select * From goods where barcode='" + barcode + "'";

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
                    // deleteListViewAll();
                    // doSearch();

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ManageProductActivityModfiy.this, AlertDialog.THEME_HOLO_LIGHT);
                    alertDialog.setMessage("정상적으로 수정되었습니다..");
                    alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            doClear();
                        }
                    });
                    alertDialog.show();

                } else {
                    Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "수정 실패", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    /*
     * public void doSearch() {
     *
     * String index = String.valueOf(mfillMaps.size()); String query = "";
     * String barcode = m_textBarcode.getText().toString(); String productName =
     * m_textProductName.getText().toString(); String customerCode =
     * m_textCustomerCode.getText().toString(); String customerName =
     * m_textCustomerName.getText().toString();
     *
     * query += "SELECT TOP 50 * FROM Goods "; query +=
     * " WHERE Goods_Use='1' AND Pur_Use='1' "; query += " AND BarCode like '%"
     * +barcode +"%' "; query += " AND G_Name like '%"+productName +"%' "; query
     * += " AND Bus_Name like '%"+customerName +"%' "; query +=
     * " AND Bus_Code like '%"+customerCode +"%' "; query +=
     * " AND BarCode NOT IN(SELECT TOP " + index + " BarCode FROM Goods "; query
     * += " where BarCode like '%"+barcode +"%' "; query +=
     * " AND G_Name like '%"+productName +"%' "; query +=
     * " AND Bus_Name like '%"+customerName +"%' "; query +=
     * " AND Bus_Code like '%"+customerCode +"%' "; query +=
     * " Order By BarCode ASC) Order By BarCode ASC;";
     *
     * // 로딩 다이알로그 dialog = new ProgressDialog(this);
     * dialog.setMessage("Loading...."); dialog.show();
     *
     * // 콜백함수와 함께 실행 new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {
     *
     * @Override public void onRequestCompleted(JSONArray results) {
     * dialog.dismiss(); dialog.cancel(); if (results.length() > 0) {
     * Toast.makeText(getApplicationContext(), "조회 완료",
     * Toast.LENGTH_SHORT).show(); //updateListView(results); } else {
     * Toast.makeText(getApplicationContext(), "결과가 없습니다",
     * Toast.LENGTH_SHORT).show(); } }
     *
     * @Override public void onRequestFailed(int code, String msg) {
     * dialog.dismiss(); dialog.cancel();
     * Toast.makeText(getApplicationContext(), "조회 실패",
     * Toast.LENGTH_SHORT).show(); } }).execute(m_ip+":"+m_port, "TIPS", "sa",
     * "tips", query); }
     */
    /*
     * public void updateListView(JSONArray results) {
     *
     * for (int i = 0; i < results.length(); i++) {
     *
     * try { JSONObject json = results.getJSONObject(i); HashMap<String, String>
     * map = JsonHelper.toStringHashMap(json);
     *
     * map.put("Sell_Pri", String.format("%.0f",
     * Double.parseDouble(map.get("Sell_Pri")))); mfillMaps.add(map);
     *
     * } catch (JSONException e) { e.printStackTrace(); } } //
     * m_adapter.notifyDataSetChanged(); }
     */

    // 분류관련 메소드
    private void fetchLName() {
        String query = "";
        //2019-07-16
        //query = "SELECT L_Name, L_Code FROM Goods GROUP BY L_Name, L_Code;";
        query = "SELECT L_Name, L_Code FROM L_Branch;";

        // query = "Select L_Code, L_Name From L_Branch Where L_Code <> 'AA'
        // Order By L_Code";

        // 로딩 다이알로그
        /*
         * dialog = new ProgressDialog(this); dialog.setMessage("Loading....");
         * dialog.setCancelable(false); dialog.show();
         */

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                /*
                 * dialog.dismiss(); dialog.cancel();
                 */

                if (results.length() > 0) {

                    for (int index = 0; index < results.length(); index++) {
                        HashMap<String, String> data = new HashMap<String, String>();
                        JSONObject son;
                        try {
                            son = results.getJSONObject(index);
                            String name = son.getString("L_Name");
                            String code = son.getString("L_Code");

                            data.put("name", name);
                            data.put("code", code);
                            m_Ltype.add(data);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    public void onClassification1(View view) {
        m_buttonCustomerClassification2.setText("");
        m_tempProduct.put("M_Name", "");
        m_tempProduct.put("M_Code", "");
        m_buttonCustomerClassification3.setText("");
        m_tempProduct.put("S_Name", "");
        m_tempProduct.put("S_Code", "");
        ArrayList<String> lSpList = new ArrayList<String>();
        for (int index = 0; index < m_Ltype.size(); index++) {
            HashMap<String, String> data = m_Ltype.get(index);
            String name = data.get("name");
            lSpList.add(name);
        }

        final CharSequence[] charSequenceItems = lSpList.toArray(new CharSequence[lSpList.size()]);

        AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle("선택하세요");
        builder.setItems(charSequenceItems, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                String name = charSequenceItems[item].toString();
                String code = getCodeFromListByName(m_Ltype, name);
                m_buttonCustomerClassification1.setText(name);
                m_tempProduct.put("L_Name", name);
                m_tempProduct.put("L_Code", code);
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onClassification2(View view) {
        String lname = m_buttonCustomerClassification1.getText().toString();
        if (lname.equals("")) {
            Toast.makeText(getApplicationContext(), "먼저 대분류를 먼저 선택하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        m_tempProduct.put("S_Name", "");
        m_tempProduct.put("S_Code", "");
        m_buttonCustomerClassification3.setText("");
        String query = "";
        //2019-07-15
        //query = "SELECT M_Name, M_Code FROM Goods WHERE L_Name='" + lname + "' GROUP BY M_Name, M_Code ;";
        query = "SELECT M_Name, M_Code FROM M_Branch WHERE L_Name='" + lname + "';";

        // query = "Select M_Code, M_Name From M_Branch Where L_Name =
        // '"+lname+"' And M_Code <> 'AA' Order By M_Code";
        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();

                if (results.length() > 0) {
                    m_Mtype.removeAll(m_Mtype);

                    ArrayList<String> lSpList = new ArrayList<String>();
                    for (int index = 0; index < results.length(); index++) {
                        HashMap<String, String> data = new HashMap<String, String>();
                        JSONObject son;
                        try {
                            son = results.getJSONObject(index);
                            String name = son.getString("M_Name");
                            String code = son.getString("M_Code");
                            lSpList.add(name);

                            data.put("name", name);
                            data.put("code", code);
                            m_Mtype.add(data);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    final CharSequence[] charSequenceItems = lSpList.toArray(new CharSequence[lSpList.size()]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ManageProductActivityModfiy.this, AlertDialog.THEME_HOLO_LIGHT);
                    builder.setTitle("선택하세요");
                    builder.setItems(charSequenceItems, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            String name = charSequenceItems[item].toString();
                            String code = getCodeFromListByName(m_Mtype, name);
                            m_buttonCustomerClassification2.setText(name);
                            m_tempProduct.put("M_Name", name);
                            m_tempProduct.put("M_Code", code);
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    public void onClassification3(View view) {
        String lname = m_buttonCustomerClassification1.getText().toString();
        String mname = m_buttonCustomerClassification2.getText().toString();
        if (lname.equals("") || mname.equals("")) {
            Toast.makeText(getApplicationContext(), "먼저 대분류,중분류를 먼저 선택하세요", Toast.LENGTH_SHORT).show();
            return;
        }

        String query = "";
        //2019-07-15
        //query = "SELECT S_Name, S_Code FROM Goods WHERE L_Name='" + lname + "' AND M_Name='" + mname + "' GROUP BY S_Name, S_Code;";
        query = "SELECT S_Name, S_Code FROM S_Branch WHERE L_Name='" + lname + "' AND M_Name='" + mname + "';";

        // query = "Select S_Code, S_Name From S_Branch Where L_Name =
        // '"+lname+"' AND M_Name = '"+mname+"' AND S_Code <> 'AAA' Order By
        // S_Code";
        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();

                if (results.length() > 0) {
                    m_Stype.removeAll(m_Stype);

                    ArrayList<String> lSpList = new ArrayList<String>();
                    for (int index = 0; index < results.length(); index++) {
                        //2019-07-16 누락
                        HashMap<String, String> data = new HashMap<String, String>();
                        JSONObject son;
                        try {
                            //HashMap<String, String> data = new HashMap<String, String>();
                            son = results.getJSONObject(index);
                            String name = son.getString("S_Name");
                            String code = son.getString("S_Code");
                            lSpList.add(name);

                            data.put("name", name);
                            data.put("code", code);
                            m_Stype.add(data);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    final CharSequence[] charSequenceItems = lSpList.toArray(new CharSequence[lSpList.size()]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(ManageProductActivityModfiy.this, AlertDialog.THEME_HOLO_LIGHT);
                    builder.setTitle("선택하세요");
                    builder.setItems(charSequenceItems, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            String name = charSequenceItems[item].toString();
                            //2019-07-16
                            //String code = getCodeFromListByName(m_Mtype, name);
                            String code = getCodeFromListByName(m_Stype, name);
                            m_buttonCustomerClassification3.setText(name);
                            m_tempProduct.put("S_Name", name);
                            m_tempProduct.put("S_Code", code);
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    // 거래처 코드로 거래처명 자동 완성
    private void fillBusNameFromBusCode(String customerCode) {
        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        String query = "";
        query = "SELECT Office_Name From Office_Manage WHERE Office_Code = '" + customerCode + "';";
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                if (results.length() > 0) {
                    try {
                        JSONObject json = results.getJSONObject(0);
                        String bus_name = json.getString("Office_Name");
                        m_textCustomerName.setText(bus_name);
                        m_tempProduct.put("Bus_Name", bus_name);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(getApplicationContext(), "조회 완료", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    public void onCustomerSearch(View view) {
        String customer = m_textCustomerCode.getText().toString();
        String customername = m_textCustomerName.getText().toString();
        Intent intent = new Intent(this, ManageCustomerListActivity.class);
        intent.putExtra("customer", customer);
        intent.putExtra("customername", customername);
        startActivityForResult(intent, CUSTOMER_MANAGER_REQUEST);
    }

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
            //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option);
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

    private void startProductList() {
        String barcode = m_textBarcode.getText().toString();
        String gname = m_textProductName.getText().toString();
        String officecode = m_textCustomerCode.getText().toString();
        Intent intent = new Intent(this, ManageProductListActivity.class);
        intent.putExtra("barcode", barcode);
        intent.putExtra("gname", gname);
        intent.putExtra("Office_Code", officecode);
        startActivityForResult(intent, BARCODE_MANAGER_REQUEST);
    }

    private void startCameraSearch() {
        Intent intent = new Intent(this, ZBarScannerActivity.class);
        startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
    }

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
                    doQueryWithBarcode();

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
                }
                break;
            // 목록 검색을 통한 바코드 검색
            case BARCODE_MANAGER_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) data.getSerializableExtra("fillmaps");
                    m_textBarcode.setText(hashMap.get("BarCode"));
                    m_tempProduct.put("BarCode", hashMap.get("BarCode")); //
                    doQueryWithBarcode();
                }
                break;
            case CUSTOMER_MANAGER_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) data.getSerializableExtra("fillmaps");
                    m_textCustomerCode.setText(hashMap.get("Office_Code"));
                    m_textCustomerName.setText(hashMap.get("Office_Name"));

                    m_tempProduct.put("Bus_Code", hashMap.get("Office_Code")); //
                    m_tempProduct.put("Bus_Name", hashMap.get("Office_Name")); //
                }
                break;
            case CUSTOMER_BOXPRODUCT_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    String obtain = (String) data.getSerializableExtra("obtain");
                    m_textAcquire.setText(obtain);
                }
                break;
        }

    }

    // SQL QUERY 실행
    public void doQueryWithBarcode() {

        String query = "";
        String barcode = m_textBarcode.getText().toString();
        query = "SELECT * FROM Goods WHERE Barcode = '" + barcode + "';";

        if (barcode.equals(""))
            return;


        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();

                if (results.length() > 0) {
                    try {
                        m_tempProduct = JsonHelper.toStringHashMap(results.getJSONObject(0));
                        updateFormView(m_tempProduct);
                        historyProductData(m_tempProduct);
                        productNewRegisterYN("n");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //이전 검색된 내용이 있다면 삭제하고 재 검색 합니다.
                    if (!"".equals(m_textProductName.getText().toString())) {
                        String barcode = m_textBarcode.getText().toString();
                        doClear();
                        m_textBarcode.setText(barcode);
                        m_textProductName.requestFocus();
                    } else {
                        Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    private void productNewRegisterYN(String proyn) {
        if ((proyn == "Y" || proyn == "y")) { // 신상품등록
            m_textBarcode.setEnabled(true);
            m_buttonbarcode.setEnabled(true);
            m_registButton.setText("등록");
            m_registButton.setTag("reg");
            m_recoveryButton.setEnabled(false);
            // 저울상품 변경시 또는 부분상품 변경시 원상복구
            m_textPurchasePrice.setEnabled(true);
            m_textSalesPrice.setEnabled(true);
            //2022-05-26 권장소비자가 추가
            m_editTextSellPri1.setEnabled(true);

            m_textDifferentRatio.setEnabled(true);

            buttonAcquire.setEnabled(false);

        } else { // 수정모드등록
            m_textBarcode.setEnabled(false);
            m_buttonbarcode.setEnabled(false);
            m_registButton.setText("수정");
            m_registButton.setTag("modify");
            m_recoveryButton.setEnabled(true);

            buttonAcquire.setEnabled(true);
        }
    }

    // 포커스 이동시 백그라운드 색 변경 하기

    /**
     * 텍스트 에디터의 색상을 변경 합니다.
     *
     * @param gubun 0 흰색 배경색, 1 노란색 배경색
     * @param et    EditText
     */
    private void setChangeBackGroundColor(int gubun, EditText et) {

        switch (gubun) {
            case 0:
                //et.setBackgroundColor(Color.WHITE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    et.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_border));
                }
                break;
            case 1:
                //et.setBackgroundColor(Color.YELLOW);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    et.setBackground(ContextCompat.getDrawable(this, R.drawable.edit_border_focus));
                }
                break;
        }
    }


    //자체 바코드 생성
    public void setSelfBarcode(View view) {

        //DB에서 바코드의 최고값을 찾아 봅니다.
        String query = "";
        //query = "Select Max(LEFT(BarCode,12)) As selfBarcode From Goods a, Pos_Set b Where Left(BarCode, 2)  = left([106], 2) AND LEN(Barcode)=13";
        // 2023.02.28. 바코드 자동생성 옵션 추가에 따라 분기한다
        if(isSelfBarcodeUse==true && selfBarcodePrefixCode.length()==2){
            query = "Select Max(LEFT(BarCode,12)) As selfBarcode From Goods Where Left(BarCode, 2)  = '" + selfBarcodePrefixCode + "' AND LEN(Barcode)=13";
        }else{
            query = "Select Max(LEFT(BarCode,12)) As selfBarcode From Goods a, Pos_Set b Where Left(BarCode, 2)  = left([106], 2) AND LEN(Barcode)=13";
        }

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();

                if (results.length() > 0) {
                    try {
                        HashMap<String, String> map = JsonHelper.toStringHashMap(results.getJSONObject(0));
                        String selfBar = map.get("selfBarcode").toString();

                        if ("".equals(selfBar) || selfBar == null) {
                            Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        setSelfBarcode(selfBar);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //이전 검색된 내용이 있다면 삭제하고 재 검색 합니다.
                    if (!"".equals(m_textProductName.getText().toString())) {
                        String barcode = m_textBarcode.getText().toString();
                        doClear();
                        m_textBarcode.setText(barcode);
                        m_textProductName.requestFocus();
                    } else {
                        Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

    }

    private void setSelfBarcode(String str) {


        //구한 최대값에 1을 더합니다.
        long bar = Long.parseLong(str) + 1;
        Log.d("상품등록", String.valueOf(bar));

        //스트링으로 변환 합니다.
        String barcode = String.valueOf(bar);

        if (!str.substring(0, 2).equals(barcode.substring(0, 2))) {
            Toast.makeText(mContext, "사용가능한 자체바코드를 전부 사용하였습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        //자릿수를 초과했는지 여부
        String chk_bar = barcode.substring(0, 2);
        int[] digits = new int[barcode.length()];
        int i_even = 0;
        int i_odd = 0;

        //1단계 숫자를 분리합니다.
        //2단계 짝수 자리를 더합니다.
        //3단계 더합값에 곱하기 3을 한다.
        //4단계 홀수 자리를 다 더합니다.

        for (int i = 0; i < barcode.length(); ++i) {
            digits[i] = Integer.parseInt(barcode.substring(i, i + 1));
            if ((i + 1) % 2 == 0) {
                i_even += digits[i];
            } else {
                i_odd += digits[i];
            }
        }

        //5단계 3단계와 4단계를 더합니다.
        int total = i_even + i_odd;

        //6단계
        String sub_str = String.valueOf(total).substring(String.valueOf(total).length() - 1, String.valueOf(total).length());

        int result = 0;
        if (!sub_str.equals("0")) {
            result = 10 - Integer.parseInt(sub_str);
        }

        barcode = barcode + "" + result;
        m_textBarcode.setText(barcode);

        // 상품명으로 포커스 이동하자
        ///???
        m_textProductName.setInputType(145); //
        //m_textProductName.setPrivateImeOptions("defaultInputmode=korean;");
        m_textProductName.requestFocus();

        upu.hideSoftKeyboard(false);

    }

    /**************	시스템 함수   **************************************************************************/

    //2017-04 m3mobile 추가
    @Override
    protected void onResume() {
        super.onResume();
        if (m3Mobile) {
            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textBarcode, m_textProductName);
            M3MoblieBarcodeScanBroadcast.setOnResume();
        }
    }

    //2017-04 m3mobile 추가
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m3Mobile) {
            M3MoblieBarcodeScanBroadcast.setOnDestory();
        }
        //빅슬론 spp-l3000
        if (barcodePrinterCheck == 2 && mBixolonLabelPrinter.isConnected()) {
            mBixolonLabelPrinter.disconnect();
        }

        //세우테크 lkp30
        if (barcodePrinterCheck == 3 && bluetoothPort.isConnected()) {
            blueConnect.close();
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            ActionBar actionbar = getActionBar();
            actionbar.setDisplayShowHomeEnabled(true);
            actionbar.setDisplayShowTitleEnabled(true);
            actionbar.setDisplayShowCustomEnabled(true);
            actionbar.setTitle("상품관리");

            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_product, menu);
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

    // 2021.01.05.김영목. 바코드프린터 조회하기
    private void getBarcodePrinterSet() {

        String query = "";

        query += " SELECT * FROM BarcodePrinter_Sett";

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
                setSpinner(results);
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    private void setSpinner(JSONArray results) {

        ArrayList<String> spinner_list = new ArrayList<String>();
        String temp = "";
        temp = barcodePrinterName;
        if (barcodePrinterName.equals("매장프린터")) {
            temp += "|" + barcodePrinterAddress;
            temp += "|" + barcodePrinterPort;
        }

        // 맨처음은 기본 프린터 설정
        spinner_list.add(temp);

        if (results.length() > 0) {
            for (int i = 0; i < results.length(); i++) {
                try {
                    JSONObject json2 = results.getJSONObject(i);
                    HashMap<String, String> map2 = JsonHelper.toStringHashMap(json2);

                    String aaa = map2.get("bp_name");
                    aaa += "|" + map2.get("bp_ip");
                    aaa += "|" + map2.get("bp_port");
                    spinner_list.add(aaa);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinner_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelectPrinter.setAdapter(adapter);

        //2023.02.09. 여기에서 선택된 바코드프린터 인덱스 체크
        int SelectBarcodePrinter = 0; //선택바코드프린터 인덱스
        try {
            SelectBarcodePrinter = LocalStorage.getInt(mContext, "SelectBarcodePrinter");
        } catch (RuntimeException e) {
            LocalStorage.setInt(mContext, "SelectBarcodePrinter", 0);
        }
        int spinnerCnt = spinnerSelectPrinter.getAdapter().getCount();
        if (spinnerCnt > SelectBarcodePrinter)
            spinnerSelectPrinter.setSelection(SelectBarcodePrinter);

    }

    //프린터 셋팅 목록
    private void printerSettingPage() {

        switch (barcodePrinterCheck) {
            case 0:
                //사용안함
                break;
            case 1:
                //매장프린터
                break;
            case 2:
                //spp-l3000
                //showSelectedSppl3000();
                break;
            case 3:
                //LK-P30II
                showSelectedLKP30II();
                break;
        }
    }

    private void showSelectedLKP30II() {

        final CharSequence[] items = {"택설정", "프린터연결", "연결종료"};
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ManageProductActivityModfiy.this, AlertDialog.THEME_HOLO_LIGHT);

        // 제목셋팅
        alertDialogBuilder.setTitle("옵션을 선택해 주세요");
        alertDialogBuilder.setItems(items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 프로그램을 종료한다
                        switch (items[id].toString()) {
                            case "택설정":
                                Intent intent = new Intent(mContext, BarcodePrintLKP30II.class);
                                startActivity(intent);
                                break;
                            case "프린터연결":
                                if (!BluetoothPort.getInstance().isConnected()) {

                                    //--------------------//
                                    // 김영목 테스트
                                    //--------------------//
                                    //dialog.dismiss();
                                    //--------------------//

                                    startBluetoothPrintSearch();
                                } else {
                                    if (!statusCheck(new CPCLPrinter()).equals("Nomal")) {
                                        try {
                                            bluetoothPort.disconnect();
                                            startBluetoothPrintSearch();
                                        } catch (IOException | InterruptedException e) {
                                            e.printStackTrace();
                                            return;
                                        }
                                    } else {
                                        Toast.makeText(mContext, "프린터가 연결되어 있습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                break;
                            case "연결종료":
                                if (BluetoothPort.getInstance().isConnected()) {
                                    try {
                                        BluetoothPort.getInstance().disconnect();
                                        textViewPrinterState.setText("LK-P30N(연결안됨)");
                                    } catch (IOException | InterruptedException e) {
                                        e.printStackTrace();
                                        Toast.makeText(mContext, "블루투스 연결종료 실패", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(mContext, "프린터가 연결되어있지 않습니다.", Toast.LENGTH_SHORT).show();
                                    textViewPrinterState.setText("LK-P30N(연결안됨)");
                                }
                                break;
                        }
                        dialog.dismiss();
                    }
                });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();

    }

    private void startBluetoothPrintSearch() {
        //Intent intent = new Intent(mContext, BluetoothConnectMenu.class);
        //startActivity(intent);
        blueConnect.setStart(textViewPrinterState);
    }

    //세우테크 프린터
    public String statusCheck(CPCLPrinter cpclPrinter) {
        String result = "";
        if (!(cpclPrinter.printerCheck() < 0)) {
            int sts = cpclPrinter.status();
            if (sts == CPCLConst.LK_STS_CPCL_NORMAL)
                return "Normal";
            if ((sts & CPCLConst.LK_STS_CPCL_BUSY) > 0)
                result = result + "Busy\r\n";
            if ((sts & CPCLConst.LK_STS_CPCL_PAPER_EMPTY) > 0)
                result = result + "Paper empty\r\n";
            if ((sts & CPCLConst.LK_STS_CPCL_COVER_OPEN) > 0)
                result = result + "Cover open\r\n";
            if ((sts & CPCLConst.LK_STS_CPCL_BATTERY_LOW) > 0)
                result = result + "Battery low\r\n";
            //result = result + cpclPrinter.voltage()+ "\r\n";
            //result = result + cpclPrinter.temperature();
        } else {
            result = "Check the printer\r\nNo response";
            textViewPrinterState.setText("LK-P30N(연결안됨)");
        }

        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        return result;
    }

    @SuppressLint("HandlerLeak")
    private void dispatchMessage(Message msg) {
        switch (msg.arg1) {
            case BixolonLabelPrinter.PROCESS_GET_STATUS:
                byte[] report = (byte[]) msg.obj;
                StringBuffer buffer = new StringBuffer();
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_PAPER_EMPTY) == BixolonLabelPrinter.STATUS_1ST_BYTE_PAPER_EMPTY) {
                    buffer.append("용지 없음.\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_COVER_OPEN) == BixolonLabelPrinter.STATUS_1ST_BYTE_COVER_OPEN) {
                    buffer.append("커버 오픈.\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_CUTTER_JAMMED) == BixolonLabelPrinter.STATUS_1ST_BYTE_CUTTER_JAMMED) {
                    buffer.append("커터 걸림.\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_TPH_OVERHEAT) == BixolonLabelPrinter.STATUS_1ST_BYTE_TPH_OVERHEAT) {
                    buffer.append("TPH(thermal head) overheat.\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_AUTO_SENSING_FAILURE) == BixolonLabelPrinter.STATUS_1ST_BYTE_AUTO_SENSING_FAILURE) {
                    buffer.append("용지 구분 오류. (자동조정실패)\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_RIBBON_END_ERROR) == BixolonLabelPrinter.STATUS_1ST_BYTE_RIBBON_END_ERROR) {
                    buffer.append("Ribbon end error.\n");
                }

                if (report.length == 2) {
                    if ((report[1] & BixolonLabelPrinter.STATUS_2ND_BYTE_BUILDING_IN_IMAGE_BUFFER) == BixolonLabelPrinter.STATUS_2ND_BYTE_BUILDING_IN_IMAGE_BUFFER) {
                        buffer.append("On building label to be printed in image buffer.\n");
                    }
                    if ((report[1] & BixolonLabelPrinter.STATUS_2ND_BYTE_PRINTING_IN_IMAGE_BUFFER) == BixolonLabelPrinter.STATUS_2ND_BYTE_PRINTING_IN_IMAGE_BUFFER) {
                        buffer.append("On printing label in image buffer.\n");
                    }
                    if ((report[1] & BixolonLabelPrinter.STATUS_2ND_BYTE_PAUSED_IN_PEELER_UNIT) == BixolonLabelPrinter.STATUS_2ND_BYTE_PAUSED_IN_PEELER_UNIT) {
                        buffer.append("Issued label is paused in peeler unit.\n");
                    }
                }
                if (buffer.length() == 0) {
                    buffer.append("정상");
                    //상태가 이상이 없을때만 프린트를 시작합니다.
                    printStart();
                }
                Toast.makeText(getApplicationContext(), buffer.toString(), Toast.LENGTH_SHORT).show();
                break;

            case BixolonLabelPrinter.PROCESS_GET_INFORMATION_MODEL_NAME:
            case BixolonLabelPrinter.PROCESS_GET_INFORMATION_FIRMWARE_VERSION:
            case BixolonLabelPrinter.PROCESS_EXECUTE_DIRECT_IO:
                Toast.makeText(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case BixolonLabelPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BixolonLabelPrinter.STATE_CONNECTED:
                            textViewPrinterState.setText("연결됨(" + mConnectedDeviceName + ")");
                            break;

                        case BixolonLabelPrinter.STATE_CONNECTING:
                            textViewPrinterState.setText("연결중...");
                            break;

                        case BixolonLabelPrinter.STATE_NONE:
                            textViewPrinterState.setText("연결안됨");
                            break;
                    }
                    break;

                case BixolonLabelPrinter.MESSAGE_READ:
                    ManageProductActivityModfiy.this.dispatchMessage(msg);
                    break;

                case BixolonLabelPrinter.MESSAGE_DEVICE_NAME:
                    mConnectedDeviceName = msg.getData().getString(BixolonLabelPrinter.DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), mConnectedDeviceName, Toast.LENGTH_LONG).show();
                    break;

                case BixolonLabelPrinter.MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BixolonLabelPrinter.TOAST), Toast.LENGTH_SHORT).show();
                    break;

                case BixolonLabelPrinter.MESSAGE_LOG:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(BixolonLabelPrinter.LOG), Toast.LENGTH_SHORT).show();
                    break;

                case BixolonLabelPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    if (msg.obj == null) {
                        Toast.makeText(getApplicationContext(), "No paired device", Toast.LENGTH_SHORT).show();
                    } else {
                        DialogManager.showBluetoothDialog(ManageProductActivityModfiy.this, (Set<BluetoothDevice>) msg.obj);
                    }
                    break;
            }
        }
    };

    // 달력 버튼 클릭시 결과 받아오는 이벤트 처리
    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

        Toast.makeText(getApplicationContext(), "날자 DateSet 받아옴...", Toast.LENGTH_SHORT).show();

        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        calendarPrintSetDate.set(year, monthOfYear, dayOfMonth);
        buttonPrintSetDate.setText(simpleDateFormat.format(calendarPrintSetDate.getTime()));
        mPrintSetDate = buttonPrintSetDate.getText().toString();

    }

    // 프린터 옵션 다이얼로그박스 출력
    private void showPronterOption() {

        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.dialog_printer_option, null);

        /*----------------------------------------*/
        // 여기서는 로컬자료를 불러온다
        /*----------------------------------------*/

        //행사가
        checkboxSaleYn = (CheckBox) dialogView.findViewById(R.id.checkbox_sale_yn);

        if (mSaleYn.equals("1")) {
            checkboxSaleYn.setChecked(true);
        } else {
            checkboxSaleYn.setChecked(false);
        }

        // 권장소비자가
        checkboxSellPri1 = (CheckBox) dialogView.findViewById(R.id.checkbox_sell_pri1);  // 권장소비자가 선택
        if (mSellPri1.equals("1")) {
            checkboxSellPri1.setChecked(true);
        } else {
            checkboxSellPri1.setChecked(false);
        }

        // 분류(대분류,중분류,소분류) 스피너
        spinnerSelectBranchName = (Spinner) dialogView.findViewById(R.id.spinner_select_branch_name);
        //mSelectBranchName = 1;//기본
        spinnerSelectBranchName.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String tempData = ((Spinner) dialogView.findViewById(R.id.spinner_select_branch_name)).getSelectedItem().toString();
                if (tempData.equals("대분류")) {
                    mSelectBranchName = 1;
                } else if (tempData.equals("중분류")) {
                    mSelectBranchName = 2;
                } else if (tempData.equals("소분류")) {
                    mSelectBranchName = 3;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        // 추가항목 스피너
        spinnerSelectAddItem = (Spinner) dialogView.findViewById(R.id.spinner_select_add_item);

        spinnerSelectAddItem.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String tempData = ((Spinner) dialogView.findViewById(R.id.spinner_select_add_item)).getSelectedItem().toString();
                if (tempData.equals("출력일")) {
                    buttonPrintSetDate.setVisibility(View.VISIBLE); //0:visible,1:invisible,2:gone
                    edittextPrintUserText.setVisibility(View.GONE);
                    mPrintUserText = "";
                    mSelectAddItem = 1;
                } else {
                    // 사용자 정의 텍스트박스 보이기
                    edittextPrintUserText.setVisibility(View.VISIBLE);
                    buttonPrintSetDate.setVisibility(View.GONE); //0:visible,1:invisible,2:gone
                    mPrintUserText = edittextPrintUserText.getText().toString();
                    mSelectAddItem = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 추가항목 사용자 정의 문자
        edittextPrintUserText = (EditText) dialogView.findViewById(R.id.edittext_print_user_text);
        edittextPrintUserText.setText(mPrintUserText);

        //추가항목 출력일 버튼
        buttonPrintSetDate = (Button) dialogView.findViewById(R.id.button_print_set_date); // 추가항목 출력일
        buttonPrintSetDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(), "날자 클릭 이벤트", Toast.LENGTH_SHORT).show();
                DatePickerDialog newDlg = new DatePickerDialog(ManageProductActivityModfiy.this, ManageProductActivityModfiy.this,
                        calendarPrintSetDate.get(Calendar.YEAR),
                        calendarPrintSetDate.get(Calendar.MONTH),
                        calendarPrintSetDate.get(Calendar.DAY_OF_MONTH));
                newDlg.show();
            }
        });

        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        calendarPrintSetDate = Calendar.getInstance();
        buttonPrintSetDate.setText(simpleDateFormat.format(calendarPrintSetDate.getTime()));
        mPrintSetDate = buttonPrintSetDate.getText().toString();


        // 출력일 문구 출력
        checkboxPrintDateTitle = (CheckBox) dialogView.findViewById(R.id.checkbox_print_date_title); // 출력일 문구 출력
        if (mPrintDateTitle.equals("1")) {
            checkboxPrintDateTitle.setChecked(true);
        } else {
            checkboxPrintDateTitle.setChecked(false);
        }

        /*----------------------------------------*/

        AlertDialog.Builder builder = new AlertDialog.Builder(ManageProductActivityModfiy.this, AlertDialog.THEME_HOLO_LIGHT);

        builder.setTitle("프린터 옵션");

        builder.setView(dialogView);

        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                /*----------------------------------------*/
                // 여기서는 로컬자료를 저장한다
                /*----------------------------------------*/
                // 행사가
                if (checkboxSaleYn.isChecked()) {
                    mSaleYn = "1";
                } else {
                    mSaleYn = "0";
                }
                LocalStorage.setString(ManageProductActivityModfiy.this, "BarcodePrinterSaleYN", mSaleYn);

                // 권장소비자가
                if (checkboxSellPri1.isChecked()) {
                    mSellPri1 = "1";
                } else {
                    mSellPri1 = "0";
                }
                LocalStorage.setString(ManageProductActivityModfiy.this, "BarcodePrinterSellPri1", mSellPri1);

                //분류 리스트
                //mSelectBranchName 1,2,3

                //추가항목 선택 리트스
                //mSelectAddItem 1,2

                // 출력일 문구 출력
                if (checkboxPrintDateTitle.isChecked()) {
                    mPrintDateTitle = "1";
                } else {
                    mPrintDateTitle = "0";
                }
                LocalStorage.setString(mContext, "BarcodePrinterDateTitle", mPrintDateTitle);

                // 사용자정의 문자 저장
                mPrintUserText = edittextPrintUserText.getText().toString();
                LocalStorage.setString(mContext, "AddItemUserText", mPrintUserText);
                /*----------------------------------------*/
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setCanceledOnTouchOutside(false);//Dialog의 바깥쪽을 터치했을 때 Dialog를 없앨지 설정

        dialog.show();

    }

    // 바코드프린터 출력 메인
    private void barcodePrinterMain() {

        //----------------------------------------//
        // 기존 택 저장버튼 기능 재현
        //----------------------------------------//
        if ("".equals(m_textBarcode.getText().toString())) {
            Toast.makeText(mContext, "검색된 상품이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 기타 출력을 위하여 쿼리 한번 더 한 후 결과에 출력하자
        doQueryWithBarcodePrinter();

    }

    // 바코드프린터 출력
    private void barcodePrinterExec(JSONArray results) {

        try {
            JSONObject json = results.getJSONObject(0);
            HashMap<String, String> map = JsonHelper.toStringHashMap(json);

            m_tempProduct.put("Sell_Pri_2", map.get("Sell_Pri"));
            m_tempProduct.put("Sell_Org_2", map.get("Sell_Org"));

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "바코드프린터 조회가 안되어 있습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        //----------------------------------------//
        // 2023.02.08. 에러 때문에 필수 추가
        //----------------------------------------//
        m_tempProduct.put("Count", "1");
        m_tempProduct.put("Sell_Org", m_tempProduct.get("Sell_Pri"));
        //----------------------------------------//

        mfillMapsBar.removeAll(mfillMapsBar);
        mfillMapsBar.add(m_tempProduct);

        //----------------------------------------//
        // 2021.01.08.김영목. 프린터 분기 변경 추가
        //----------------------------------------//
        if (barcodePrinterName.equals("") || barcodePrinterName.equals("사용안함")) {
            Toast.makeText(getApplicationContext(), "바코드프린터 설정이 안되어 있습니다.", Toast.LENGTH_LONG).show();
        } else if (barcodePrinterName.equals("SPP-L3000")) {
            barPrintSPP3000Trnasfer();
        } else if (barcodePrinterName.equals("LK-P30II")) {
            barPrintLKP30IITransfer();
        } else {
            m_bprinter = new BarcodePrinterAsyncTask2();
            m_bprinter.execute("Start");
        }

    }

    //바코드 프린터 spp-l3000 전송하기
    private void barPrintSPP3000Trnasfer() {

        if (!mBixolonLabelPrinter.isConnected()) {
            Toast.makeText(this, "빅슬론 프린터를 연결해 주세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        //프린터 상태 체크
        boolean report = false;
        mBixolonLabelPrinter.getStatus(report);
    }

    //세우테크 프린터 출력
    private void barPrintLKP30IITransfer() {

        if (!bluetoothPort.isConnected()) {
            Toast.makeText(this, "세우테크 프린터를 연결해 주세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        CPCLPrinter cpcl = new CPCLPrinter();
        if (statusCheck(cpcl).equals("Normal")) {
            printStartSeWoo();
        }
    }

    //세우테크 프린터 출력
    //LK-P30II
    @SuppressLint("LongLogTag")
    private void printStartSeWoo() {

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        SPPL3000 spp = null;
        String query = "Select * From BaPrint_SPPL3000 Where Print_Size='" + tips.getSELECTPRINT_GUBUN() + "'; ";
        Log.d(TAG, query);
        spp = dba.getBarPrint_SPPL3000(query);


        //mBixolonLabelPrinter.setLength(spp.getLavel_Hight(), ,BixolonLabelPrinter.MEDIA_TYPE_GAP, 3);
        String[] gname = spp.getGoods_Setting().split("\\|");
        String[] stdsize = spp.getStdsize_Setting().split("\\|");
        String[] price = spp.getPrice_Setting().split("\\|");
        String[] office = spp.getOffice_Setting().split("\\|");
        String[] danga = spp.getDanga_Setting().split("\\|");
        String[] barcode = spp.getBarcode_Setting().split("\\|");

        //--------------------------------------------------------------------------------//
        // 2021.01.06.김영목. 원판매가,할인율 추가
        //--------------------------------------------------------------------------------//
        String[] sellPrice = spp.getSellPrice_Setting().split("\\|");
        String[] saleSellRate = spp.getSaleSellRate_Setting().split("\\|");
        //--------------------------------------------------------------------------------//

        //--------------------------------------------------------------------------------//
        // 2021.07.08. 권장소비자가 선택,분류,추가항목,문구출력 추가
        //--------------------------------------------------------------------------------//
        String[] location = spp.getLocation_Setting().split("\\|");
        String[] nickName = spp.getNickName_Setting().split("\\|");
        String[] branchName = spp.getBranchName_Setting().split("\\|");
        String[] addItem = spp.getAddItem_Setting().split("\\|");
        //--------------------------------------------------------------------------------//

        Log.d(TAG, "발행 사이즈 : " + price[0] + "|" + price[1] + "|" + price[2] + "|" + price[3] + "|" + price[4] + "|" + price[5] + "|" + price[6]);
        //Log.d(TAG, "발행수량 : " + mfillMapsBar.size());
        Log.d(TAG, "발행수량 : " + 1);  // 여기서는 발행수는 1로 고정
        //mfillMapsBarBar
        //for (int i = 0; i < mfillMapsBar.size(); i++) {

        HashMap<String, String> map = mfillMapsBar.get(0);

        int count = 1;
        //Log.d(TAG, "발행 카운트 : "+count);

        String g_name = stringToNullCheck(map, "G_Name", "");
        if (g_name.length() > spp.getWord_Length()) {
            g_name = g_name.substring(0, spp.getWord_Length());
        }

        try {

            CPCLPrinter cpclprinter = new CPCLPrinter("EUC-KR");

            //인쇄 용지 선택				intToinchPrint(spp.getLavel_Hight())
            int height = intToinchPrint(spp.getLavel_Hight());
            cpclprinter.setForm(0, 200, 200, height, count);

            //용지 구분
            cpclprinter.setMedia(intTopapergubun_s(spp.getPaper_Gubun()));
            Log.d(TAG, "용지구분 : " + intTopapergubun_s(spp.getPaper_Gubun()));

            //상품명 출력
            //로테이션, 폰트타입, 폰트사이즈, 가로위치, 세로위치, 데이타, 수량)

            //----------------------------------------//
            // 2022.07.28.상품명 길이 체크
            // 2022.08.04. 라벨사이즈, 폰트사이즈에 따라 자동 처리 추가
            // 조건 인자
            // 1. 용지가로사이즈
            // 2. 용지 시작 위치
            // 3. 폰트 세로 사이즈
            // 결과값
            // 1. 문자 가로 인쇄 1단 유효 길이
            // 2. 2단 인쇄 유무
            // 3. 2단 인쇄시 세로 위치
            //----------------------------------------//
            // 용지사이즈 너비 수치
            // 58:400
            // 58:464 inch
            int w0 = spp.getLavel_Hight();
            int x0 = 0; // 폰트가로설정값(0,1,2,3)
            int y0 = 0; // 폰트세로설정값(0,1,2,3)
            int xp = 0; // 가로 시작위치

            int xl = 0; // 가로 유효 길이
            int tl = 0; // 1단 최대 글자수

            int ty = 0; // 2단 세로 위치

            try {
                x0 = stringTointPrint(gname[3]);
                y0 = stringTointPrint(gname[4]);
                xp = stringTointPrint(gname[0]);

                xl = (int) Math.round((400 * w0 / 58) - xp);
                // 폰트 크기에 따라 최대 글자 구함
                // 0,1 = 36, 2=18,3=12
                if (x0 <= 1) {
                    tl = (int) Math.round((xl * 36) / 400);
                } else if (x0 == 2) {
                    tl = (int) Math.round((xl * 18) / 400);
                } else if (x0 == 3) {
                    tl = (int) Math.round((xl * 12) / 400);
                }

                // 2단 세로 위치
                if (y0 <= 1) {
                    ty = 28;
                } else if (y0 == 2) {
                    ty = 52;
                } else if (y0 == 3) {
                    ty = 80;
                }
                //----------------------------------------//

            } catch (Exception e) {
                e.printStackTrace();
            }


            //----------------------------------------//
            // 상품명 길이 체크하여 2단 분리
            //----------------------------------------//
            int len = 0;
            String aaa = "";
            String bbb = "";

            char ch[] = g_name.toCharArray();
            int max = ch.length;
            int cnt = 0;

            for (int ii = 0; ii < max; ii++) {
                // 0x80: 문자일 경우 +2
                if (ch[ii] > 0x80) {
                    cnt++;
                }
                cnt++;

                // 여기서 일정길이 체크
                if (cnt > tl) {
                    len = ii;
                    break;
                }
            }
            //----------------------------------------//

            if (!g_name.equals("")) {

                if (len > 0) {

                    aaa = g_name.substring(0, len);
                    bbb = g_name.substring(len, max);

                    //----------------------------------------//
                    // 한글 유무 체크
                    //----------------------------------------//
                    char ca[] = aaa.toCharArray();
                    boolean bla = false;
                    int maxa = ca.length;

                    for (int ii = 0; ii < maxa; ii++) {
                        // 0x80: 문자일 경우 +2
                        if (ca[ii] > 0x80) {
                            bla = true;
                            break;
                        }
                    }

                    char cb[] = bbb.toCharArray();
                    boolean blb = false;
                    int maxb = cb.length;

                    for (int ii = 0; ii < maxb; ii++) {
                        // 0x80: 문자일 경우 +2
                        if (cb[ii] > 0x80) {
                            blb = true;
                            break;
                        }
                    }
                    //----------------------------------------//

                    if (bla == true) {
                        cpclprinter.setMagnify(stringTointPrint(gname[3]), stringTointPrint(gname[4]));
                    } else {
                        cpclprinter.setMagnify(stringTointPrint(gname[3] + 1), stringTointPrint(gname[4]) + 1);
                    }
                    cpclprinter.printCPCLText(stringTointPrint(gname[6]), 0, 0, stringTointPrint(gname[0]), stringTointPrint(gname[1]), aaa, 0);

                    if (blb == true) {
                        cpclprinter.setMagnify(stringTointPrint(gname[3]), stringTointPrint(gname[4]));
                    } else {
                        cpclprinter.setMagnify(stringTointPrint(gname[3]) + 1, stringTointPrint(gname[4]) + 1);
                    }
                    cpclprinter.printCPCLText(stringTointPrint(gname[6]), 0, 0, stringTointPrint(gname[0]), stringTointPrint(gname[1] + ty), bbb, 0);

                } else {
                    cpclprinter.setMagnify(stringTointPrint(gname[3]), stringTointPrint(gname[4]));
                    cpclprinter.printCPCLText(stringTointPrint(gname[6]), 0, 0, stringTointPrint(gname[0]), stringTointPrint(gname[1]), g_name, 0);

                }
            }

            //규격 출력
            if (spp.getPrint_StdSize_YN() == 1) {
                if (!stringToNullCheck(map, "Std_Size", "").equals("")) {
                    cpclprinter.printAndroidFont(stringTointPrint(stdsize[0]), stringTointPrint(stdsize[1]), stringToNullCheck(map, "Std_Size", " "), intToinchPrint(spp.getLavel_Width()), stringTointPrint(stdsize[3] + stdsize[4]) + 10);
                }
            }

            //가격 출력
            if (spp.getPrint_Price_YN() == 1) {
                cpclprinter.setMagnify(stringTointPrint(price[3]), stringTointPrint(price[4]));
                cpclprinter.printCPCLText(stringTointPrint(price[6]), 7, 0, stringTointPrint(price[0]), stringTointPrint(price[1]), StringFormat.convertToNumberFormat(stringToNullCheck(map, "Sell_Pri_2", "0")) + " 원", 0);
                cpclprinter.resetMagnify();
            }

            //거래처 출력
            if (spp.getPrint_Office_YN() == 1) {
                cpclprinter.printAndroidFont(stringTointPrint(office[0]), stringTointPrint(office[1]), stringToNullCheck(map, "Bus_Name", "거래처"), intToinchPrint(spp.getLavel_Width()), stringTointPrint(office[3] + office[4]) + 10);
            }

            //단가 출력
            if (spp.getPrint_Danga_YN() == 1) {
                double a = Double.parseDouble(stringToNullCheck(map, "Con_Rate", "0"));
                double b = Double.parseDouble(stringToNullCheck(map, "Std_Rate", "0"));
                double c = Double.parseDouble(stringToNullCheck(map, "Sell_Pri_2", "0"));
                a = a / b;

                String dan = String.valueOf(Math.round(c / a)) + " 원/ " + stringToNullCheck(map, "Std_Rate", "0") + stringToNullCheck(map, "Unit", "0");
                Log.i("기준단가", dan);

                cpclprinter.printAndroidFont(stringTointPrint(danga[0]), stringTointPrint(danga[1]), dan, intToinchPrint(spp.getLavel_Width()), stringTointPrint(danga[3] + danga[4]) + 10);
            }

            //--------------------------------------------------------------------------------//
            // 2021.01.06.김영목. 원판매가,할인율 추가
            //--------------------------------------------------------------------------------//
            double pri = Double.parseDouble(stringToNullCheck(map, "Sell_Pri_2", "0"));
            double org = Double.parseDouble(stringToNullCheck(map, "Sell_Org_2", "0"));
            //if (org > pri) {
            //원판매가 출력
            if (spp.getPrint_SellPrice_YN() == 1) {
                cpclprinter.setMagnify(stringTointPrint(sellPrice[3]), stringTointPrint(sellPrice[4]));
                cpclprinter.printCPCLText(stringTointPrint(sellPrice[6]), 7, 0, stringTointPrint(sellPrice[0]), stringTointPrint(sellPrice[1]), StringFormat.convertToNumberFormat(stringToNullCheck(map, "Sell_Org_2", "0")) + "", 0);
                cpclprinter.resetMagnify();
            }

            //할인율(%) 출력
            if (spp.getPrint_SaleSellRate_YN() == 1) {
                try {
                    cpclprinter.setMagnify(stringTointPrint(saleSellRate[3]), stringTointPrint(saleSellRate[4]));
                    cpclprinter.printCPCLText(stringTointPrint(saleSellRate[6]), 7, 0, stringTointPrint(saleSellRate[0]), stringTointPrint(saleSellRate[1]), StringFormat.convertToNumberFormat(stringToNullCheck(map, "Sale_Rate", "0")) + " %", 0);
                    cpclprinter.resetMagnify();
                } catch (Exception e) {
                    //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
            //}
            //--------------------------------------------------------------------------------//

            //--------------------------------------------------------------------------------//
            // 2021.07.08. 권장소비자가 선택,분류,추가항목,문구출력 추가
            //--------------------------------------------------------------------------------//
            //위치 출력
            if (spp.getPrint_Location_YN() == 1) {
                if (!stringToNullCheck(map, "Location", "").equals("")) {
                    try {
                        cpclprinter.printAndroidFont(stringTointPrint(location[0]), stringTointPrint(location[1]), stringToNullCheck(map, "Location", " "), intToinchPrint(spp.getLavel_Width()), stringTointPrint(location[3] + location[4]) + 10);
                    } catch (Exception e) {
                        //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            //품번 출력
            if (spp.getPrint_NickName_YN() == 1) {
                try {
                    if (!stringToNullCheck(map, "NickName", "").equals("")) {
                        cpclprinter.printAndroidFont(stringTointPrint(nickName[0]), stringTointPrint(nickName[1]), stringToNullCheck(map, "NickName", " "), intToinchPrint(spp.getLavel_Width()), stringTointPrint(nickName[3] + nickName[4]) + 10);
                    }
                } catch (Exception e) {
                    //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            //분류 출력
            if (spp.getPrint_BranchName_YN() == 1) {
                String branchname = "";

                if (mSelectBranchName == 1) {  // 대분류
                    branchname = stringToNullCheck(map, "L_Name", " ");
                } else if (mSelectBranchName == 2) {  // 중분류
                    branchname = stringToNullCheck(map, "M_Name", " ");
                } else if (mSelectBranchName == 3) {  // 소분류
                    branchname = stringToNullCheck(map, "S_Name", " ");
                } else {  // 기본 대분류
                    branchname = stringToNullCheck(map, "L_Name", " ");
                }
                try {
                    cpclprinter.printAndroidFont(stringTointPrint(branchName[0]), stringTointPrint(branchName[1]), branchname, intToinchPrint(spp.getLavel_Width()), stringTointPrint(branchName[3] + branchName[4]) + 10);
                } catch (Exception e) {
                    //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            //추가항목 출력

            if (spp.getPrint_AddItem_YN() == 1) {

                String additem = "";

                if (mSelectAddItem == 1) {
                    if (mPrintDateTitle.equals("1")) {
                        additem = "*출력일:" + mPrintSetDate;
                    } else {
                        additem = mPrintSetDate;
                    }
                } else if (mSelectAddItem == 2) {
                    additem = mPrintUserText;
                }

                if (!additem.equals("")) {
                    try {
                        cpclprinter.printAndroidFont(stringTointPrint(addItem[0]), stringTointPrint(addItem[1]), additem, intToinchPrint(spp.getLavel_Width()), stringTointPrint(addItem[3] + addItem[4]) + 10);
                    } catch (Exception e) {
                        //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            //--------------------------------------------------------------------------------//


            cpclprinter.setCPCLBarcode(0, 1, 0);
            cpclprinter.printCPCLBarcode(CPCLConst.LK_CPCL_0_ROTATION, CPCLConst.LK_CPCL_BCS_128, 1, CPCLConst.LK_CPCL_BCS_0RATIO, stringTointPrint(barcode[5]), stringTointPrint(barcode[0]), stringTointPrint(barcode[1]), stringToNullCheck(map, "BarCode", "0"), 0);

            cpclprinter.printForm();
            //Toast.makeText(this, "출력완료", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //저장하기
        dba.insert_barPrint(mfillMapsBar, "1");
        mfillMapsBar.removeAll(mfillMapsBar);
        //m_adapter.notifyDataSetChanged();

        dialog.dismiss();
        dialog.cancel();

        Toast.makeText(this, "발행이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
    }

    //빅슬론 프린터 변환값

    /**
     * 용지 구분을 백합니다.
     *
     * @param gubun int paper_gubun
     * @return 0: 갭용지(71), 1: 연속용지(67), 2: 블랙마크용지(66)
     */
    private int intTopapergubun(int gubun) {
        int res = 71;
        switch (gubun) {
            case 0:
                res = BixolonLabelPrinter.MEDIA_TYPE_GAP;
                break;
            case 1:
                res = BixolonLabelPrinter.MEDIA_TYPE_CONTINUOUS;
                break;
            case 2:
                res = BixolonLabelPrinter.MEDIA_TYPE_BLACK_MARK;
                break;
        }
        return res;
    }

    /**
     * 스트링을 넣으면 인트값으로 변환함
     *
     * @param str String
     * @return int
     */
    private int stringTointPrint(String str) {

        int res = 0;
        try {
            res = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            res = 0;
        }

        return res;
    }

    /**
     * 프린터 폰트사이즈 반환
     *
     * @param str 스트링
     * @return 폰트값
     */
    private int fontSizeToString(String str) {
        if (str.equals("0")) {
            return BixolonLabelPrinter.FONT_SIZE_KOREAN1;
        } else {
            return BixolonLabelPrinter.FONT_SIZE_KOREAN2;
        }
    }

    /**
     * 스트링을 boolean으로 변경 반환
     *
     * @param str
     * @return 1:true, 0:false
     */
    private boolean booleanToString(String str) {
        if (str.equals("0")) {
            return false;
        } else {
            return true;
        }
    }

    //빅슬론 프린터 변환값 - 끝 -


    //세우 프린터 변환값

    /**
     * 용지 구분을 백합니다.
     *
     * @param gubun int paper_gubun
     * @return 0: 갭용지(0), 1: 연속용지(2), 2: 블랙마크용지(1)
     */
    private int intTopapergubun_s(int gubun) {
        int res = 0;
        switch (gubun) {
            case 0:
                res = CPCLConst.LK_CPCL_LABEL;
                break;
            case 1:
                res = CPCLConst.LK_CPCL_CONTINUOUS;
                break;
            case 2:
                res = CPCLConst.LK_CPCL_BLACKMARK;
                break;
        }
        return res;
    }

    /**
     * 프린터 폰트사이즈 반환
     *
     * @param str 스트링
     * @return 폰트값
     */
    private int fontSizeToString_s(String str) {
        if (str.equals("0")) {
            return 0;
        } else {
            return 5;
        }
    }

    /**
     * 인트를 넣으면 도트로 환산해서 출력
     *
     * @param milli String
     * @return int
     */
    @SuppressLint("LongLogTag")
    private int intToinchPrint(int milli) {
        int res = 0;
        //res = (int)(milli / 25.4f * 203);
        res = (int) (milli * 8);
        Log.d(TAG, res + "");
        return res;
    }

    private String stringToNullCheck(HashMap<String, String> map, String str, String def) {
        String res = def;
        try {
            res = map.get(str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return res;
    }
    //세우 프린터 변환값 - 끝 -

    //바코드 프린터 소켓통신으로 전송하기
    public class BarcodePrinterAsyncTask2 extends AsyncTask<String, Integer, Boolean> {

        PrintWriter out;
        Socket socket = null;
        private int count_num = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "발행시작", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub

            // 2021.01.05.김영목. 포트 동적으로 수정
            // 2021.01.05.김영목. 포트 동적으로 수정
            //SocketAddress socketAddress = new InetSocketAddress(m_barcodePrinter, 8671);
            //SocketAddress socketAddress = new InetSocketAddress(m_barcodePrinter, port);

            int port = Integer.parseInt(barcodePrinterPort);
            SocketAddress socketAddress = new InetSocketAddress(barcodePrinterAddress, port);

            socket = new Socket();
            try {
                //socket = new Socket("192.168.10.156", 8671);
                //socket = new Socket(m_barcodePrinter, 8671);

                socket.setSoTimeout(5000);
                try {
                    socket.connect(socketAddress, 5000);
                } catch (SocketTimeoutException e) {
                    return false;
                }
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "MS949")), true);
                HashMap<String, String> map = new HashMap<String, String>();
                for (int i = 0; i < mfillMaps.size(); i++) {
                    String content = "";
                    map.putAll(mfillMaps.get(i));
                    content = map.get("BarCode");
                    //content += "; \n\r";
                    content += "; ";
                    content += map.get("G_Name");
                    //content += "; \n\r";
                    content += "; ";
                    content += map.get("Std_Size");
                    //content += "; \n\r";
                    content += "; ";
                    content += 1;
                    count_num = count_num + 1;
                    //content += "; \n\r";
                    content += "; ";
                    content += StringFormat.convertToNumberFormat(map.get("Sell_Pri_2"));
                    //content += "; \n\r";
                    content += "; ";
                    content += map.get("Bus_Name");
                    //content += "; \n\r";
                    content += "; ";

                    double a = Double.parseDouble(map.get("Con_Rate"));
                    double b = Double.parseDouble(map.get("Std_Rate"));
                    double c = Double.parseDouble(map.get("Sell_Pri_2"));
                    a = a / b;

                    content += String.valueOf(Math.round(c / a)) + " / " + map.get("Std_Rate") + map.get("Unit");
                    Log.i("기준단가", content.toString());

                    //--------------------------------------------------------------------------------//
                    // 2021.01.06.김영목. 원판매가 추가
                    //--------------------------------------------------------------------------------//
                    //double pri = Double.parseDouble(stringToNullCheck(map, "Sell_Pri", "0"));
                    //double org = Double.parseDouble(stringToNullCheck(map, "Sell_Org", "0"));
/*                    if (org > pri) {
                        content += "; ";
                        content += StringFormat.convertToNumberFormat(map.get("Sell_Org"));
                    }*/
                    //--------------------------------------------------------------------------------//
                    // 2021.07.20.김영목. 원판매가 필수 추가
                    //--------------------------------------------------------------------------------//
                    content += "; ";
                    content += StringFormat.convertToNumberFormat(map.get("Sell_Org_2"));
                    //----------------------------------------//

                    //----------------------------------------//
                    // 2021.07.08.김영목. 위치,품번,분류,권장소비자가 추가
                    //----------------------------------------//
                    //위치
                    content += "; ";
                    content += map.get("Location");

                    //품번
                    content += "; ";
                    content += map.get("NickName");

                    //분류
                    content += "; ";
                    //content += map.get("BranchName");

                    String branchname = "";

                    if (mSelectBranchName == 1) {  // 대분류
                        branchname = stringToNullCheck(map, "L_Name", " ");
                    } else if (mSelectBranchName == 2) {  // 중분류
                        branchname = stringToNullCheck(map, "M_Name", " ");
                    } else if (mSelectBranchName == 3) {  // 소분류
                        branchname = stringToNullCheck(map, "S_Name", " ");
                    } else {  // 기본 대분류
                        branchname = stringToNullCheck(map, "L_Name", " ");
                    }
                    content += branchname;

                    //추가항목
                    content += "; ";
                    //content += map.get("AddItem");
                    String additem = "";

                    if (mSelectAddItem == 1) {
                        if (mPrintDateTitle.equals("1")) {
                            additem = "*출력일:" + mPrintSetDate;
                        } else {
                            additem = mPrintSetDate;
                        }
                    } else if (mSelectAddItem == 2) {
                        additem = mPrintUserText;
                    }
                    content += additem;
                    //----------------------------------------//

                    //2018년 12월 17일 추가
                    content += "\u0003";

                    try {
                        sleep(300);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    out.println(content.toString());
                    out.flush();
                }
                out.close();
            } catch (SocketException e) {
                e.printStackTrace();
                return false;
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result) {
                dba.insert_barPrint(mfillMapsBar, "1");
                mfillMapsBar.removeAll(mfillMapsBar);
                //m_adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), String.valueOf(count_num) + " 개 발행완료", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "바코드프린터 소켓연결을 확인해 주세요.", Toast.LENGTH_LONG).show();
            }
        }
    }

    // SQL QUERY 실행
    public void doQueryWithBarcodePrinter() {


        String barcode = m_textBarcode.getText().toString();
        if (barcode.equals(""))
            return;


        String query = "";

        //2019-11-13 공병 누락 금액 처리 추가
        query += " SELECT ";
        query += "  A.BarCode AS BarCode "; // 바코드
        query += " ,A.G_Name AS G_Name "; // 상품명

        // 정상가(판매가+공병매출가)/행사가
        if (mSaleYn.equals("1")) {
            query += " ,Sell_Pri = CASE WHEN A.SALE_USE = '1' THEN (ISNULL(A.SALE_SELL,0) + ISNULL(A.BOT_SELL,0)) ELSE (ISNULL(A.SELL_PRI,0) + ISNULL(A.BOT_SELL,0)) END ";
        } else {
            query += " ,(ISNULL(A.SELL_PRI,0) + ISNULL(A.BOT_SELL,0)) AS Sell_Pri ";
        }

        query += " ,A.Std_Size AS Std_Size "; // 규격
        query += " ,A.Bus_Name AS Bus_Name "; // 거래처명
        query += " ,A.Con_Rate AS Con_Rate "; // 내용량
        query += " ,A.Std_Rate AS Std_Rate "; // 표시단위
        query += " ,A.Unit AS Unit ";

        //----------------------------------------//
        // 2021.01.06.김영목. 원판매가,할인율 추가
        //----------------------------------------//
        //query += " ,(ISNULL(A.SELL_PRI,0) + ISNULL(A.BOT_SELL,0)) AS Sell_Org "; //원판매가
        if (mSellPri1.equals("1")) {
            query += " ,ISNULL(A.Sell_Pri1,0) AS Sell_Org "; // 권장소비자가
        } else {
            query += " ,(ISNULL(A.SELL_PRI,0) + ISNULL(A.BOT_SELL,0)) AS Sell_Org "; //원판매가
        }
        query += " ,0 AS Sale_Rate "; //할인율(일단 필드 추가-추후 계산해서 등록)
        //----------------------------------------/

        //----------------------------------------//
        // 2021.07.08.김영목. 위치,품번,분류,권장소비자가 추가
        //----------------------------------------//
        query += " ,ISNULL(A.Location,'') AS Location  "; // 위치
        query += " ,ISNULL(B.NickName,'') AS NickName  "; // 품번
        //query += " ,ISNULL(A.Sell_Pri1,'') AS Sell_Pri1 "; // 권장소비자가
        if (mSelectBranchName == 1) {
            query += " ,ISNULL(A.L_Name,'') AS BranchName  "; // 대분류명
        } else if (mSelectBranchName == 2) {
            query += " ,ISNULL(A.M_Name,'') AS BranchName  "; // 중분류명
        } else if (mSelectBranchName == 3) {
            query += " ,ISNULL(A.S_Name,'') AS BranchName  "; // 중분류명
        } else {
            query += " ,ISNULL(A.L_Name,'') AS BranchName  "; // 대분류명
        }

        if (mSelectAddItem == 1) {
            String tmpDate = mPrintSetDate;
            if (mPrintDateTitle.equals("1")) {
                tmpDate = "*출력일:" + mPrintSetDate;
            }
            query += " ,'" + tmpDate + "' AS AddItem  "; // 출력일
        } else if (mSelectAddItem == 2) {
            String tmpUser = mPrintUserText;
            query += " ,'" + tmpUser + "' AS AddItem  "; // 사용자정의
        } else {
            query += " ,'' AS AddItem  "; // 사용자정의
        }
        //----------------------------------------//

        query += " FROM Goods A ";
        query += " LEFT OUTER JOIN (SELECT Barcode, nickName FROM goods_nick WHERE nickgubun = '1' ) B ";
        query += " ON A.Barcode = B.Barcode ";
        query += " WHERE ";
        query += " A.Barcode = '" + barcode + "'";
        query += " AND A.Scale_use = '0' ";    //공산품만 검색
        query += " AND A.goods_use = '1'; ";    //상품사용만 검색


        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();

                if (results.length() > 0) {
                    barcodePrinterExec(results);
                } else {
                    Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    @SuppressLint("LongLogTag")
    private void printStart() {

        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        SPPL3000 spp = null;
        String query = "Select * From BaPrint_SPPL3000 Where Print_Size='" + tips.getSELECTPRINT_GUBUN() + "'; ";
        Log.d(TAG, query);
        spp = dba.getBarPrint_SPPL3000(query);

        //프린터 셋팅을 합니다.
        //인쇄 용지 선택
        mBixolonLabelPrinter.setLength(spp.getLavel_Hight(), spp.getGap_Width(), intTopapergubun(spp.getPaper_Gubun()), spp.getGap_Width());

        //인쇄방향
        if (spp.getPrint_Direction() == 0) {
            mBixolonLabelPrinter.setOrientation(BixolonLabelPrinter.ORIENTATION_TOP_TO_BOTTOM);
        } else {
            mBixolonLabelPrinter.setOrientation(BixolonLabelPrinter.ORIENTATION_BOTTOM_TO_TOP);
        }

        //mBixolonLabelPrinter.setLength(spp.getLavel_Hight(), ,BixolonLabelPrinter.MEDIA_TYPE_GAP, 3);
        String[] gname = spp.getGoods_Setting().split("\\|");
        String[] stdsize = spp.getStdsize_Setting().split("\\|");
        String[] price = spp.getPrice_Setting().split("\\|");
        String[] office = spp.getOffice_Setting().split("\\|");
        String[] danga = spp.getDanga_Setting().split("\\|");
        String[] barcode = spp.getBarcode_Setting().split("\\|");

        //--------------------------------------------------------------------------------//
        // 2021.01.06.김영목. 원판매가,할인율 추가
        //--------------------------------------------------------------------------------//
        String[] sellPrice = spp.getSellPrice_Setting().split("\\|");
        String[] saleSellRate = spp.getSaleSellRate_Setting().split("\\|");
        //--------------------------------------------------------------------------------//
        //--------------------------------------------------------------------------------//
        // 2021.07.08. 권장소비자가 선택,분류,추가항목,문구출력 추가
        //--------------------------------------------------------------------------------//
        String[] location = spp.getLocation_Setting().split("\\|");
        String[] nickName = spp.getNickName_Setting().split("\\|");
        String[] branchName = spp.getBranchName_Setting().split("\\|");
        String[] addItem = spp.getAddItem_Setting().split("\\|");
        //--------------------------------------------------------------------------------//

        Log.d(TAG, price[0] + "|" + price[1] + "|" + price[2] + "|" + price[3] + "|" + price[4] + "|" + price[5] + "|" + price[6]);

        for (int i = 0; i < mfillMapsBar.size(); i++) {

            HashMap<String, String> map = mfillMapsBar.get(i);

            String g_name = map.get("G_Name");
            if (g_name.length() > spp.getWord_Length()) {
                g_name = g_name.substring(spp.getWord_Length());
            }

            mBixolonLabelPrinter.drawText(g_name, stringTointPrint(gname[0]), stringTointPrint(gname[1]), fontSizeToString(gname[2]),
                    stringTointPrint(gname[3]) + 1, stringTointPrint(gname[4]) + 1, 0,
                    stringTointPrint(gname[6]), false, booleanToString(gname[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);


            //규격 출력

            if (spp.getPrint_StdSize_YN() == 1) {
                mBixolonLabelPrinter.drawText(map.get("Std_Size"), stringTointPrint(stdsize[0]), stringTointPrint(stdsize[1]), fontSizeToString(stdsize[2]),
                        stringTointPrint(stdsize[3]) + 1, stringTointPrint(stdsize[4]) + 1, 0,
                        stringTointPrint(stdsize[6]), false, booleanToString(stdsize[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }

            //가격 출력
            if (spp.getPrint_Price_YN() == 1) {

                mBixolonLabelPrinter.drawText(StringFormat.convertToNumberFormat(map.get("Sell_Pri_2")), stringTointPrint(price[0]), stringTointPrint(price[1]),
                        fontSizeToString(price[2]), stringTointPrint(price[3]) + 1, stringTointPrint(price[4]) + 1, 0,
                        stringTointPrint(price[6]), false, booleanToString(price[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_RIGHT);

                mBixolonLabelPrinter.drawText("원", stringTointPrint(price[0]) + 4, stringTointPrint(price[1]),
                        fontSizeToString(price[2]), stringTointPrint(price[3]) + 1, stringTointPrint(price[4]) + 1, 0,
                        stringTointPrint(price[6]), false, booleanToString(price[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_LEFT);
            }

            //거래처 출력
            if (spp.getPrint_Office_YN() == 1) {
                mBixolonLabelPrinter.drawText(map.get("Bus_Name"), stringTointPrint(office[0]), stringTointPrint(office[1]), fontSizeToString(office[2]),
                        stringTointPrint(office[3]) + 1, stringTointPrint(office[4]) + 1, 0,
                        stringTointPrint(office[6]), false, booleanToString(office[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }

            //단가 출력
            if (spp.getPrint_Danga_YN() == 1) {
                double a = Double.parseDouble(map.get("Con_Rate"));
                double b = Double.parseDouble(map.get("Std_Rate"));
                double c = Double.parseDouble(map.get("Sell_Pri_2"));
                a = a / b;

                String dan = String.valueOf(Math.round(c / a)) + " 원/ " + map.get("Std_Rate") + map.get("Unit");
                Log.i("기준단가", dan);

                mBixolonLabelPrinter.drawText(dan, stringTointPrint(danga[0]), stringTointPrint(danga[1]), fontSizeToString(danga[2]),
                        stringTointPrint(danga[3]) + 1, stringTointPrint(danga[4]) + 1, 0,
                        stringTointPrint(danga[6]), false, booleanToString(danga[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }

            //--------------------------------------------------------------------------------//
            // 2021.01.06.김영목. 원판매가,할인율 추가
            //--------------------------------------------------------------------------------//
            double pri = Double.parseDouble(stringToNullCheck(map, "Sell_Pri_2", "0"));
            double org = Double.parseDouble(stringToNullCheck(map, "Sell_Org_2", "0"));
            if (org > pri) {
                //원판매가 출력
                if (spp.getPrint_SellPrice_YN() == 1) {
                    mBixolonLabelPrinter.drawText(StringFormat.convertToNumberFormat(map.get("Sell_Org_2")), stringTointPrint(sellPrice[0]), stringTointPrint(sellPrice[1]),
                            fontSizeToString(sellPrice[2]), stringTointPrint(sellPrice[3]) + 1, stringTointPrint(sellPrice[4]) + 1, 0,
                            stringTointPrint(sellPrice[6]), false, booleanToString(sellPrice[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_RIGHT);

                    mBixolonLabelPrinter.drawText("", stringTointPrint(sellPrice[0]) + 4, stringTointPrint(sellPrice[1]),
                            fontSizeToString(sellPrice[2]), stringTointPrint(sellPrice[3]) + 1, stringTointPrint(sellPrice[4]) + 1, 0,
                            stringTointPrint(sellPrice[6]), false, booleanToString(sellPrice[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_LEFT);
                }

                //할인율(%) 출력
                if (spp.getPrint_SaleSellRate_YN() == 1) {
                    mBixolonLabelPrinter.drawText(StringFormat.convertToNumberFormat(map.get("Sale_Rate")), stringTointPrint(saleSellRate[0]), stringTointPrint(saleSellRate[1]),
                            fontSizeToString(saleSellRate[2]), stringTointPrint(saleSellRate[3]) + 1, stringTointPrint(saleSellRate[4]) + 1, 0,
                            stringTointPrint(saleSellRate[6]), false, booleanToString(saleSellRate[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_RIGHT);

                    mBixolonLabelPrinter.drawText("%", stringTointPrint(saleSellRate[0]) + 4, stringTointPrint(saleSellRate[1]),
                            fontSizeToString(saleSellRate[2]), stringTointPrint(saleSellRate[3]) + 1, stringTointPrint(saleSellRate[4]) + 1, 0,
                            stringTointPrint(saleSellRate[6]), false, booleanToString(saleSellRate[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_LEFT);
                }

            }
            //--------------------------------------------------------------------------------//
            //--------------------------------------------------------------------------------//
            // 2021.07.08. 권장소비자가 선택,분류,추가항목,문구출력 추가
            //--------------------------------------------------------------------------------//
            //위치 출력
            if (spp.getPrint_Location_YN() == 1) {
                mBixolonLabelPrinter.drawText(map.get("Location"), stringTointPrint(location[0]), stringTointPrint(location[1]), fontSizeToString(location[2]),
                        stringTointPrint(location[3]) + 1, stringTointPrint(location[4]) + 1, 0,
                        stringTointPrint(location[6]), false, booleanToString(location[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }

            //품번 출력
            if (spp.getPrint_NickName_YN() == 1) {
                mBixolonLabelPrinter.drawText(map.get("NickName"), stringTointPrint(nickName[0]), stringTointPrint(nickName[1]), fontSizeToString(nickName[2]),
                        stringTointPrint(nickName[3]) + 1, stringTointPrint(nickName[4]) + 1, 0,
                        stringTointPrint(nickName[6]), false, booleanToString(nickName[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }

            //분류 출력
            if (spp.getPrint_BranchName_YN() == 1) {
//                mBixolonLabelPrinter.drawText(map.get("BranchName"), stringTointPrint(branchName[0]), stringTointPrint(branchName[1]), fontSizeToString(branchName[2]),
//                        stringTointPrint(branchName[3]) + 1, stringTointPrint(branchName[4]) + 1, 0,
//                        stringTointPrint(branchName[6]), false, booleanToString(branchName[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);

                String branchname = "";

                if (mSelectBranchName == 1) {  // 대분류
                    branchname = stringToNullCheck(map, "L_Name", " ");
                } else if (mSelectBranchName == 2) {  // 중분류
                    branchname = stringToNullCheck(map, "M_Name", " ");
                } else if (mSelectBranchName == 3) {  // 소분류
                    branchname = stringToNullCheck(map, "S_Name", " ");
                } else {  // 기본 대분류
                    branchname = stringToNullCheck(map, "L_Name", " ");
                }
                mBixolonLabelPrinter.drawText(branchname, stringTointPrint(branchName[0]), stringTointPrint(branchName[1]), fontSizeToString(branchName[2]),
                        stringTointPrint(branchName[3]) + 1, stringTointPrint(branchName[4]) + 1, 0,
                        stringTointPrint(branchName[6]), false, booleanToString(branchName[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);

            }

            //추가항목 출력
            if (spp.getPrint_AddItem_YN() == 1) {
//                mBixolonLabelPrinter.drawText(map.get("AddItem"), stringTointPrint(addItem[0]), stringTointPrint(addItem[1]), fontSizeToString(addItem[2]),
//                        stringTointPrint(addItem[3]) + 1, stringTointPrint(addItem[4]) + 1, 0,
//                        stringTointPrint(addItem[6]), false, booleanToString(addItem[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);


                String additem = "";

                if (mSelectAddItem == 1) {
                    if (mPrintDateTitle.equals("1")) {
                        additem = "*출력일:" + mPrintSetDate;
                    } else {
                        additem = mPrintSetDate;
                    }
                } else if (mSelectAddItem == 2) {
                    additem = mPrintUserText;
                }
                mBixolonLabelPrinter.drawText(additem, stringTointPrint(addItem[0]), stringTointPrint(addItem[1]), fontSizeToString(addItem[2]),
                        stringTointPrint(addItem[3]) + 1, stringTointPrint(addItem[4]) + 1, 0,
                        stringTointPrint(addItem[6]), false, booleanToString(addItem[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }
            //--------------------------------------------------------------------------------//

            mBixolonLabelPrinter.draw1dBarcode(map.get("BarCode"), stringTointPrint(barcode[0]), stringTointPrint(barcode[1]), stringTointPrint(barcode[2]),
                    stringTointPrint(barcode[3]), stringTointPrint(barcode[4]), stringTointPrint(barcode[5]), stringTointPrint(barcode[6]),
                    stringTointPrint(barcode[7]), 0);

            mBixolonLabelPrinter.print(1, 1);

        }


        //저장하기
        dba.insert_barPrint(mfillMapsBar, "1");
        mfillMapsBar.removeAll(mfillMapsBar);
        //m_adapter.notifyDataSetChanged();

        dialog.dismiss();
        dialog.cancel();

        Toast.makeText(this, "발행이 완료 되었습니다.", Toast.LENGTH_SHORT).show();


    }

}
