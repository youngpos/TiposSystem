package tipsystem.tips;

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
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;

//2019-11-08 import 추가
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;

//2019-10-24 다른화면에 해당 import 없음 확인바람(* 제거 처리 후 다음키 튕김 문제 해결)
//import android.view.View.OnFocusChangeListener;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
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

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
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
import java.util.List;
import java.util.Locale;
import java.util.Set;

import kr.co.tipos.tips.R;
import tipsystem.tips.models.SPPL3000;
import tipsystem.utils.BarcodePrintLKP30II;
import tipsystem.utils.BarcodePrinterSPPL3000;
import tipsystem.utils.BluetoothConnectMenu;
import tipsystem.utils.DBAdapter;
import tipsystem.utils.DialogManager;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.M3MoblieBarcodeScanBroadcast;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.MSSQL3;
import tipsystem.utils.MSSQL4;
import tipsystem.utils.StringFormat;
import tipsystem.utils.TIPS_Config;
import tipsystem.utils.UserPublicUtils;

import static java.lang.Thread.*;


@SuppressLint({"HandlerLeak", "NewApi"})
public class BarcodePrinterActivity extends Activity implements DatePickerDialog.OnDateSetListener {

    private static final int ZBAR_SCANNER_REQUEST = 0;
    //private static final int ZBAR_QR_SCANNER_REQUEST = 1;
    private static final int BARCODE_MANAGER_REQUEST = 2;
    private static final int CUSTOMER_MANAGER_REQUEST = 3;
    private static final int BARCODEPRINT_HISTORY_REQUEST = 4;
    private static final String TAG = "BarcodePrinter";


    //2017-04 m3mobile
    private static final String SCANNER_ACTION_SETTING_CHANGE = "com.android.server.scannerservice.settingchange";
    public static final String SCANNER_ACTION_PARAMETER = "android.intent.action.SCANNER_PARAMETER";
    public static final String SCANNER_ACTION_ENABLE = "com.android.server.scannerservice.m3onoff";
    public static final String SCANNER_EXTRA_ENABLE = "scanneronoff";

    public static final String SCANNER_ACTION_START = "android.intent.action.M3SCANNER_BUTTON_DOWN";
    public static final String SCANNER_ACTION_CANCEL = "android.intent.action.M3SCANNER_BUTTON_UP";

    public static final String SCANNER_ACTION_BARCODE = "com.android.server.scannerservice.broadcast";

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

    BarcodePrinterAsyncTask m_bprinter;

    //바코드프린터 환경설정
    String m_barcodePrinter;    //매장프린터 주소

    // 2021.01.05.김영목. 바코드프린터 설정값 새로 추가
    String barcodePrinterName; //매장프린터 명
    String barcodePrinterAddress; //매장프린터 주소
    String barcodePrinterPort;    //매장프린터 포트
    String posID;

    int m_checkPrinter;    //매장프린터 사용유무
    int m_barocdePrinterGubun;    //프린터기 구분(사용안함:0, 매장:1, spp-l3000:2,... )


    /* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
    int m_KeypadType = 0;

    Button m_period;    //상품 변경된 일자

    int m_dateMode = 0; // 날자 구분 번호
    DatePicker m_datePicker;
    SimpleDateFormat m_dateFormatter;
    Calendar m_dateCalender1;
    Calendar m_dateCalender2; //추가항목 출력일 사용

    //거래처코드, 거래처명 검색
    EditText m_officecode;
    EditText m_officename;

    //바코드, 상품명
    EditText m_barcode;
    EditText m_gname;

    //전송(발행)
    Button b_sendAll;
    Button b_countdown;
    Button b_countup;
    Button b_listsave;

    // 전송추가
    Button sendButton;
    Spinner selectPrinterSpinner;

    //상품등록
    Button b_newproductreg;

    //발행수량
    EditText m_count;

    //수량고정 체크
    CheckBox c_countfix;

    //2019-11-13 행사가 체크박스
    CheckBox c_Sale_YN;

    //발행목록
    ListView l_printList;
    SimpleAdapter m_adapter;

    //목록저장맵
    List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

    //프린터 상태
    TextView m_printerState;

    //프린터 설정
    Button m_printSetting;

    // 2021.07.08. 권장소비자가 선택,분류,추가항목,문구출력 추가
    CheckBox sellPri1CheckBox;  // 권장소비자가 선택
    Spinner selectBranchNameSpinner; //분류(대분류,중분류,소분류 선택)
    Spinner selectAddItemSpinner;  // 추가항목 선택(출력일,사용자정의)
    Button printSetDateButton; // 추가항목 출력일
    EditText printUserTextEditText; // 추가항목 사용자정의 텍스트 문자
    CheckBox printDateTitleCheckBox; // 출력일 문구 출력

    private int BranchNameType = 1; //1:대분류(기본),2:중분류,3:소분류
    private int AddItemType = 1; // 추가항목구분(1:출력일(기본), 2:사용자정의)
    private String AddItemUserText;

    DBAdapter dba;
    TIPS_Config tips;

    UserPublicUtils upu;

    Context mContext;
    //2017-04 m3mobile
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
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcodeprinter);

        mContext = this;

        //매장 정보 불러오기
        m_shop = LocalStorage.getJSONObject(this, "currentShopData");

        try {
            //매장 DDNS주소 불러오기 매장 정보 포함
            m_ip = m_shop.getString("SHOP_IP");
            m_port = m_shop.getString("SHOP_PORT");
            m_OfficeCode = m_shop.getString("OFFICE_CODE");

            //----------------------------------------//
            // 2021.12.21. 매장DB IP,PW,DB 추가
            //----------------------------------------//
            m_uuid = m_shop.getString("shop_uuid");
            m_uupw = m_shop.getString("shop_uupass");
            m_uudb = m_shop.getString("shop_uudb");
            //----------------------------------------//

            /* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
            m_KeypadType = LocalStorage.getInt(this, "KeypadType:" + m_OfficeCode);
            posID = LocalStorage.getString(this, "currentPosID:" + m_OfficeCode);

            AddItemUserText = LocalStorage.getString(this, "AddItemUserText");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        upu = new UserPublicUtils(mContext);
        tips = new TIPS_Config(mContext);

        m_checkPrinter = tips.getSELECTPRINT();
        m_barcodePrinter = tips.getBARPRINTER_ADDRESS();
        barcodePrinterAddress = tips.getBARPRINTER_ADDRESS();
        barcodePrinterPort = tips.getBARPRINTER_PORT();

        // 프린터구분(명) 구하기
        switch (m_checkPrinter) {
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
                showSelectedLKP30II();
                barcodePrinterName = "LK-P30II";
                break;
        }

        if (barcodePrinterPort.equals("")) {
            barcodePrinterPort = "8671";
            tips.setBARPRINTER_PORT(barcodePrinterPort);
        }

        m_barocdePrinterGubun = tips.SELECTPRINT_GUBUN;

        //환경설정 관련 옵션 아이템 불러오기
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        m3Mobile = pref.getBoolean("m3mobile", false);

        //첫번째 context, 두번째 데이터베이스명(String) 
        Log.i(TAG, m_OfficeCode + ".tips");
        dba = new DBAdapter(getApplicationContext(), m_OfficeCode + ".tips");

        Button m_barhistory = (Button) findViewById(R.id.buttonPrintSend_History);
        m_barhistory.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                barPrintHistory();
            }
        });

        m_period = (Button) findViewById(R.id.buttonDateSet1);
        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        m_dateCalender1 = Calendar.getInstance();
        m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

        m_officecode = (EditText) findViewById(R.id.editTextCustomer);
        m_officename = (EditText) findViewById(R.id.editTextCustomer2);

        m_barcode = (EditText) findViewById(R.id.editTextBarcord);

        // 김영목 2020.09.14. 키패드입력 옵션 설정에 따라 적용 추가
        if (m_KeypadType == 0) { // 숫자키패드
            m_barcode.setInputType(2);
        }
        if (m_KeypadType == 1) { // 문자키패드
            m_barcode.setInputType(145); //textVisiblePassword ) 0x00000091
        }

        m_gname = (EditText) findViewById(R.id.editTextGoodsName);

        b_newproductreg = findViewById(R.id.button_newproductreg);
        b_newproductreg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                regNewProduct("");
            }
        });

        m_count = (EditText) findViewById(R.id.editTextCount);

        m_barcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (!hasFocus) {

                    upu.setChangeBackGroundColor(0, m_barcode);

//                    if (c_countfix.isChecked()) {
//                        //목록에 저장하기
//                        sendList();
//                    } else {
//                        m_count.selectAll();
//                        m_count.requestFocus();
//                    }
                    // 무저건 목록에 저장장
                    //목록에 저장하기
                    sendList();

                } else {
                    upu.setChangeBackGroundColor(1, m_barcode);
                }
            }
        });

        m_gname.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {

                if (i == EditorInfo.IME_ACTION_DONE || i == EditorInfo.IME_ACTION_NEXT || i == EditorInfo.IME_ACTION_GO || (keyEvent != null && keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    if (c_countfix.isChecked()) {
                        //목록에 저장하기
                        //sendList();
                    } else {
                        m_count.selectAll();
                        m_count.requestFocus();
                    }
                    return true;
                } else {
                    return false;
                }
            }
        });


        m_printerState = (TextView) findViewById(R.id.textview_printstate);
        m_printerState.setText("발행 목록 : " + tips.getSELECTPRINT_NAME());

        m_printSetting = (Button) findViewById(R.id.buttonPrintSetting);
        m_printSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                printerSettingPage();
            }
        });

        //버튼 불러오기
        b_sendAll = (Button) findViewById(R.id.buttonPrintSend);
        sendButton = (Button) findViewById(R.id.send_button);
        selectPrinterSpinner = (Spinner) findViewById(R.id.select_printer_spinner);

        selectPrinterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                LocalStorage.setInt(mContext, "SelectBarcodePrinter", i);
                String tempData = ((Spinner) findViewById(R.id.select_printer_spinner)).getSelectedItem().toString();
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

        // 2021.01.05.김영목. 전송 추가
        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mfillMaps.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "전송 상품이 없습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 전송 추가
                sendTempAppGoods();
            }
        });

        //String[] spinner_items = new String[] {"BarCode", "G_Name", "Sell_Pri", "Count"};
        //ArrayAdapter arrayAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_item,spinner_items);
        //selectPrinterSpinner.setAdapter(arrayAdapter);

        //수량변경 버튼
        b_countdown = (Button) findViewById(R.id.buttonCountDown);
        b_countup = (Button) findViewById(R.id.buttonCountUp);
        b_listsave = (Button) findViewById(R.id.buttonSend);
        b_listsave.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                sendList();
            }
        });

        //수량 고정 버튼
        c_countfix = (CheckBox) findViewById(R.id.checkboxCountFix);

        // 최초 테스트
        //LocalStorage.setString(mContext,"BarcodePrinterCountFix","");
        //LocalStorage.setString(mContext,"BarcodePrinterCountNum","");

        //2022.10.24. 김영목. 옵션설정에 따라 기본값
        //c_countfix.setChecked(true);
        String countFix = LocalStorage.getString(this, "BarcodePrinterCountFix");
        // 최초시에는 기본 체크
        if (countFix.equals("1") || countFix.equals("")) {

            c_countfix.setChecked(true);

            String countNum = LocalStorage.getString(this, "BarcodePrinterCountNum");
            //최초시 1을 세팅한다
            if (countNum.equals("")) {
                countNum = "1";
                LocalStorage.setString(mContext, "BarcodePrinterCountNum", countNum);
            }
            m_count.setText(countNum);
        } else {
            c_countfix.setChecked(false);
            m_count.setText("1");
        }

        c_countfix.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (c_countfix.isChecked()) {
                    //수량 고정에 체크 되면 모두 없애기
                    b_countdown.setEnabled(false);
                    b_countup.setEnabled(false);
                    m_count.setEnabled(false);
                    b_listsave.setEnabled(false);
                    //2022.10.24. 김영목. 로컬 스토리지에 저장
                    LocalStorage.setString(mContext, "BarcodePrinterCountFix", "1");
                    String countNum = m_count.getText().toString();
                    LocalStorage.setString(mContext, "BarcodePrinterCountNum", countNum);
                } else {
                    //수량 고정에 체크 해제되면 모두 보이기
                    b_countdown.setEnabled(true);
                    b_countup.setEnabled(true);
                    m_count.setEnabled(true);
                    b_listsave.setEnabled(true);
                    //2022.10.24. 김영목. 로컬 스토리지에 저장
                    LocalStorage.setString(mContext, "BarcodePrinterCountFix", "0");
                    LocalStorage.setString(mContext, "BarcodePrinterCountNum", "1");
                }
            }
        });

        //2019-11-13 행사가 체크박스
        c_Sale_YN = (CheckBox) findViewById(R.id.checkboxSale_YN);

        //2022.10.27. 김영목. 행사 옵션 로컬스토리지에서 불러 옴
        String saleYN = LocalStorage.getString(this, "BarcodePrinterSaleYN");
        if (saleYN.equals("1")) {
            c_Sale_YN.setChecked(true);
        } else {
            c_Sale_YN.setChecked(false);
        }

        // 행사 체크시 로컬스토리지 저장
        c_Sale_YN.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (c_Sale_YN.isChecked()) {
                    //2022.10.27. 김영목. 로컬 스토리지에 저장
                    LocalStorage.setString(mContext, "BarcodePrinterSaleYN", "1");
                } else {
                    //2022.10.27. 김영목. 로컬 스토리지에 저장
                    LocalStorage.setString(mContext, "BarcodePrinterSaleYN", "0");
                }
            }
        });

        //----------------------------------------//
        // 2021.07.08. 권장소비자가 선택,분류,추가항목,문구출력 추가
        //----------------------------------------//
        sellPri1CheckBox = (CheckBox) findViewById(R.id.sell_pri1_checkbox);  // 권장소비자가 선택

        //----------------------------------------//
        // 2021.07.08. 이벤트 정의
        //----------------------------------------//
        //2022.10.24. 김영목. 옵션설정에 따라 기본값
        //sellPri1CheckBox.setChecked(false); // 권장소비자가 기본은 출력안함
        String sellPri1 = LocalStorage.getString(this, "BarcodePrinterSellPri1");
        if (sellPri1.equals("1")) {
            sellPri1CheckBox.setChecked(true);
        } else {
            sellPri1CheckBox.setChecked(false);
        }

        sellPri1CheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (sellPri1CheckBox.isChecked()) {
                    //2022.10.24. 김영목. 로컬 스토리지에 저장
                    LocalStorage.setString(mContext, "BarcodePrinterSellPri1", "1");
                } else {
                    //2022.10.24. 김영목. 로컬 스토리지에 저장
                    LocalStorage.setString(mContext, "BarcodePrinterSellPri1", "0");
                }
            }
        });


        selectBranchNameSpinner = (Spinner) findViewById(R.id.select_branch_name_spinner); //분류(대분류,중분류,소분류 선택)
        selectAddItemSpinner = (Spinner) findViewById(R.id.select_add_item_spinner);  // 추가항목 선택(출력일,사용자정의)
        printSetDateButton = (Button) findViewById(R.id.print_set_date_button); // 추가항목 출력일
        printUserTextEditText = (EditText) findViewById(R.id.print_user_text_edittext); // 추가항목 사용자정의 텍스트 문자

        printDateTitleCheckBox = (CheckBox) findViewById(R.id.print_date_title_checkbox); // 출력일 문구 출력

        //2022.10.24. 김영목. 옵션설정에 따라 기본값
        //printDateTitleCheckBox.setChecked(true); // 출력일 문구 출력 기본
        String dateTitle = LocalStorage.getString(this, "BarcodePrinterDateTitle");
        if (dateTitle.equals("1")) {
            printDateTitleCheckBox.setChecked(true);
        } else {
            printDateTitleCheckBox.setChecked(false);
        }

        printDateTitleCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (printDateTitleCheckBox.isChecked()) {
                    //2022.10.24. 김영목. 로컬 스토리지에 저장
                    LocalStorage.setString(mContext, "BarcodePrinterDateTitle", "1");
                } else {
                    //2022.10.24. 김영목. 로컬 스토리지에 저장
                    LocalStorage.setString(mContext, "BarcodePrinterDateTitle", "0");
                }
            }
        });

        // 분류명 선택 이벤트
        selectBranchNameSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String tempData = ((Spinner) findViewById(R.id.select_branch_name_spinner)).getSelectedItem().toString();
                if (tempData.equals("대분류")) {
                    BranchNameType = 1;
                } else if (tempData.equals("중분류")) {
                    BranchNameType = 2;
                } else if (tempData.equals("소분류")) {
                    BranchNameType = 3;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        // 추가항목 선택 이벤트
        selectAddItemSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String tempData = ((Spinner) findViewById(R.id.select_add_item_spinner)).getSelectedItem().toString();
                if (tempData.equals("출력일")) {
                    printSetDateButton.setVisibility(View.VISIBLE); //0:visible,1:invisible,2:gone
                    printUserTextEditText.setVisibility(View.GONE);
                    AddItemType = 1;

                } else {
                    // 사용자 정의 텍스트박스 보이기
                    printUserTextEditText.setVisibility(View.VISIBLE);
                    printSetDateButton.setVisibility(View.GONE); //0:visible,1:invisible,2:gone
                    printUserTextEditText.setText(AddItemUserText);
                    AddItemType = 2;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        m_dateCalender2 = Calendar.getInstance();
        printSetDateButton.setText(m_dateFormatter.format(m_dateCalender2.getTime()));

        // 사용자 정의 문자 변경시 저장
        printUserTextEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if (b) {
                } else {
                    String tmp = printUserTextEditText.getText().toString();
                    LocalStorage.setString(mContext, "AddItemUserText", tmp);
                    //LocalStorage.setString(this, "AddItemUserText", tmp); //로컬에 저장하여 재사용

                }
            }
        });


        //----------------------------------------//

        l_printList = (ListView) findViewById(R.id.listviewPrintSearchList);
        l_printList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //hideSoftKeyboard(false);

                // 바코드 검색 버튼 클릭시 나오는 목록 셋팅
                final String[] option = new String[]{"삭제하기", "수량변경"};
                final int pos = position;
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(BarcodePrinterActivity.this, R.layout.user_select_dialog_item, option);
                //----------------------------------------//
                // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
                //----------------------------------------//
                //AlertDialog.Builder builder = new AlertDialog.Builder(BarcodePrinterActivity.this);
                AlertDialog.Builder builder = new AlertDialog.Builder(BarcodePrinterActivity.this, AlertDialog.THEME_HOLO_LIGHT);
                //----------------------------------------//
                builder.setTitle("Select Option");

                // 목록 선택시 이벤트 처리
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0) { // 삭제옵션 클릭 시
                            listItemDelete(pos);
                        } else { // 수량변경 클릭 시
                            listItemCountUpDown(pos);
                        }
                    }
                });
                builder.show();
            }
        });

        String[] from = new String[]{"BarCode", "G_Name", "Sell_Pri", "Count", "Sell_Org", "Sale_Rate"};
        int[] to = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6};
        m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_barcodeprinter, from, to);

        l_printList.setAdapter(m_adapter);

        if (m_checkPrinter == 0) {
            Toast.makeText(getApplicationContext(), "바코드프린터 설정이 안되어 있습니다. \n\r 환경설정에서 확인해 주세요", Toast.LENGTH_LONG).show();
        }

        // intent filter 2017-04 m3mobile
        if (m3Mobile) {
            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_barcode, m_count);
        }


        final int ANDROID_NOUGAT = 24;
        if (Build.VERSION.SDK_INT >= ANDROID_NOUGAT) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }


        switch (tips.getSELECTPRINT()) {
            case 2:
                mBixolonLabelPrinter = new BixolonLabelPrinter(this, mHandler, null);
                break;
            case 3:
                //zplprinter = new ZPLRFIDPrinter();
                bluetoothPort = BluetoothPort.getInstance();
                blueConnect = new BluetoothConnectMenu(mContext, bluetoothPort);
                break;
        }

        // 바코드프린터 목록 불러오기
        // 2022.12.05. 왜 막았었는지 기억이 없어서 주석 처리하고 풀어줌.
        getBarcodePrinterSet();

        // 2022.11.09. 임시저장자료 있으면 불러오기
        if (dba.chkBarPrintTemp()) {
            // 확인 창
            new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle("이전작업 등록")
                    .setMessage("작업중이던 내역이 존재합니다.")
                    .setPositiveButton("이어하기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            TempBaPrintList();
                        }

                    })
                    .setNeutralButton("초기화", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //그래도 여기서 테이블 자료 지우자
                            dba.delete_TempBaPrint();
                        }
                    })
                    //.setNegativeButton("취소", null)
                    .show();
        }
    }

    @Override
    public void onBackPressed() {
        if (mfillMaps.size() > 0) {
            //자료가 있으면 임시 테이블에 저장하기
            dba.insert_TempBaPrint(mfillMaps, "1");
            finish();
        } else {
            super.onBackPressed();
        }

    }

    //프린터 셋팅 목록
    private void printerSettingPage() {

        switch (m_checkPrinter) {
            case 0:
                //사용안함
                break;
            case 1:
                //매장프린터
                break;
            case 2:
                //spp-l3000
                showSelectedSppl3000();
                break;
            case 3:
                //LK-P30II
                showSelectedLKP30II();
                break;
        }
    }

    //신상품 등록
    public void regNewProduct(String barcode) {
        String m_barcode = "";
        if (!"".equals(barcode) || barcode != null) {
            m_barcode = barcode;
        }

        Intent intent = new Intent(this, ManageProductActivityModfiy.class);
        intent.putExtra("barcode", m_barcode);
        startActivity(intent);
    }

    //거래처 검색
    public void onCustomerSearch(View view) {
        String customer = m_officecode.getText().toString();
        String customername = m_officename.getText().toString();
        Intent intent = new Intent(this, ManageCustomerListActivity.class);
        intent.putExtra("customer", customer);
        intent.putExtra("customername", customername);
        startActivityForResult(intent, CUSTOMER_MANAGER_REQUEST);
    }

    //바코드검색
    public void onBarcodeSearch(View view) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String list_value = pref.getString("prefSearchMethod", "");
        if (list_value.equals("camera")) {
            startCameraSearch();
        } else if (list_value.equals("list")) {
            startProductList();
        } else {

        	/* 김영목 2020.08.04 목록/카메라 선택화면 생략
        	// 바코드 검색 버튼 클릭시 나오는 목록 셋팅
    		final String[] option = new String[] { "목록", "카메라"};
    		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option);
    		AlertDialog.Builder builder = new AlertDialog.Builder(this);
    		builder.setTitle("Select Option");
    		
    		// 목록 선택시 이벤트 처리
    		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {

    				if(which == 0){ // 목록으로 조회할 경우
    					startProductList();
    				} else { // 스캔할 경우	
    					startCameraSearch();
    				}
    			}
    		}); 
    		builder.show();
    		*/ //김영목 2020.08.04 목록/카메라 선택화면 생략

            // 김영목 2020.08.04 바로 목록으로 조회
            startProductList();
        }
    }

    //상품검색 화면목록
    private void startProductList() {
        String barcode = m_barcode.getText().toString();
        String gname = m_gname.getText().toString();
        Intent intent = new Intent(this, ManageProductListActivity.class);
        intent.putExtra("barcode", barcode);
        intent.putExtra("gname", gname);
        intent.putExtra("Office_Code", "");
        startActivityForResult(intent, BARCODE_MANAGER_REQUEST);

        //2020-06-15
        m_barcode.setText("");
        m_gname.setText("");
    }

    //카메라검색화면
    private void startCameraSearch() {
        Intent intent = new Intent(this, ZBarScannerActivity.class);
        startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
    }

    //리스트삭제하기
    private void listItemDelete(int position) {
        int pos = position;
        mfillMaps.remove(pos);
        m_adapter.notifyDataSetChanged();
        m_barcode.setText("");
        m_barcode.requestFocus();
    }

    //리스트 수량변경하기
    private void listItemCountUpDown(int position) {

        HashMap<String, String> map = mfillMaps.get(position);
        final String count = map.get("Count");

        // 바코드 검색 버튼 클릭시 나오는 목록 셋팅
        final EditText input = new EditText(BarcodePrinterActivity.this);
        final int pos = position;
        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        input.setTextColor(Color.BLACK);
        input.setTextSize(30);
        //----------------------------------------//
        input.setText(count);


        input.setInputType(EditorInfo.TYPE_CLASS_NUMBER | EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
        input.selectAll();
        input.requestFocus();

        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        //new AlertDialog.Builder(BarcodePrinterActivity.this)
        new AlertDialog.Builder(BarcodePrinterActivity.this, AlertDialog.THEME_HOLO_LIGHT)
                //----------------------------------------//
                .setTitle("수량변경")
                .setMessage("변경할 수량을 입력해 주세요!")
                .setView(input)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = input.getText().toString();
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.putAll(mfillMaps.get(pos));
                        try {
                            //2019-11-13수량 0 변경 불가 처리 추가
                            if (Integer.parseInt(value) == 0) {
                                Toast.makeText(getApplicationContext(), "수량 0 입력 불가! ", Toast.LENGTH_LONG).show();
                                return;
                            }

                            if (0 > Integer.parseInt(value) || Integer.parseInt(value) > 100) {
                                Toast.makeText(getApplicationContext(), "수량이 0이하 또는 100개 이상입니다. \n\r 다시 확인해 주세요! ", Toast.LENGTH_LONG).show();
                                return;
                            }
                        } catch (NumberFormatException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "수량은 숫자만 입력 하실 수 있습니다. \n\r 다시 확인해 주세요 ", Toast.LENGTH_LONG).show();
                            return;
                        }
                        map.put("Count", value);

                        mfillMaps.set(pos, map);

                        m_adapter.notifyDataSetChanged();
                        m_barcode.setText("");
                        m_barcode.requestFocus();

                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).show();

        //2019-11-08
        hideSoftKeyboard(true);
    }

    //2019-11-08 추가 키보드 보여주기
    protected void hideSoftKeyboard(boolean onoff) {
        InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        if (onoff) {
            //imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 1);
            //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            //imm.showSoftInput( getCurrentFocus().getWindowToken(),InputMethodManager.SHOW_IMPLICIT);
            imm.showSoftInput(getCurrentFocus(), InputMethodManager.SHOW_FORCED);
        } else {
            //imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            //imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);//
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    //숫자빼기
    public void onCountDown(View view) {
        String count = m_count.getText().toString();
        Log.w("count results : ", count.toString());
        if (count.equals("0")) {
            Toast.makeText(this, "더이상 수량을 뺄수 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            count = String.valueOf(Integer.parseInt(count) - 1);
        }
        m_count.setText(count);
    }

    //숫자더하기
    public void onCountUp(View view) {
        String count = m_count.getText().toString();
        count = String.valueOf(Integer.parseInt(count) + 1);
        m_count.setText(count);
    }

    //발행하기
    public void sendAll(View view) {

        if (mfillMaps.isEmpty()) {
            Toast.makeText(getApplicationContext(), "발행 상품이 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

		/*
		//프린터별 출력하기
		switch(m_checkPrinter){
			case 2:
				//spp-l3000
    			barPrintSPP3000Trnasfer();
				break;
			case 3:
				barPrintLKP30IITransfer();
				break;
			default:
				if(barcodePrinterName.equals("") || barcodePrinterName.equals("사용안함")){
					Toast.makeText(getApplicationContext(), "바코드프린터 설정이 안되어 있습니다.", Toast.LENGTH_LONG).show();
				}else{
					m_bprinter = new BarcodePrinterAsyncTask();
					m_bprinter.execute("Start");
				}
				break;
		}
        */

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
            m_bprinter = new BarcodePrinterAsyncTask();
            m_bprinter.execute("Start");
        }

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
    //LK-P30II
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
        Log.d(TAG, "발행수량 : " + mfillMaps.size());

        for (int i = 0; i < mfillMaps.size(); i++) {

            HashMap<String, String> map = mfillMaps.get(i);

            int count = stringTointPrint(map.get("Count"));
            //Log.d(TAG, "발행 카운트 : "+count);

            String g_name = stringToNullCheck(map, "G_Name", "");
            if (g_name.length() > spp.getWord_Length()) {
                g_name = g_name.substring(0, spp.getWord_Length());
            }

            try {

                CPCLPrinter cpclprinter = new CPCLPrinter("EUC-KR");

                //인쇄 용지 선택				intToinchPrint(spp.getLavel_Hight())
                int height = intToinchPrint(spp.getLavel_Hight()); //인트를 넣으면 도트로 환산해서 출력
                int width = intToinchPrint(spp.getLavel_Width()); //인트를 넣으면 도트로 환산해서 출력
                cpclprinter.setForm(0, 200, 200, height, count);
                //cpclprinter.setForm(0, 100, 100, 100, count);

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
                //int w0 = spp.getLavel_Hight();
                int w0 = spp.getLavel_Width();
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

//
//                        cpclprinter.setMagnify(stringTointPrint(gname[3]), stringTointPrint(gname[4]));
//                        cpclprinter.printCPCLText(stringTointPrint(gname[6]), 0, 0, stringTointPrint(gname[0]), stringTointPrint(gname[1]), aaa, 0);
//                        //cpclprinter.resetMagnify();
//
//                        cpclprinter.setMagnify(stringTointPrint(gname[3]), stringTointPrint(gname[4]));
//                        cpclprinter.printCPCLText(stringTointPrint(gname[6]), 0, 0, stringTointPrint(gname[0]), stringTointPrint(gname[1] + ty), bbb, 0);
//                        //cpclprinter.resetMagnify();

                    } else {
                        cpclprinter.setMagnify(stringTointPrint(gname[3]), stringTointPrint(gname[4]));
                        cpclprinter.printCPCLText(stringTointPrint(gname[6]), 0, 0, stringTointPrint(gname[0]), stringTointPrint(gname[1]), g_name, 0);

                    }

                    //cpclprinter.printAndroidFont(stringTointPrint(gname[0]), stringTointPrint(gname[1]), g_name, intToinchPrint(spp.getLavel_Width()), stringTointPrint(gname[3]+gname[4])+10);}
                }

                //규격 출력
                if (spp.getPrint_StdSize_YN() == 1) {
                    //cpclprinter.setMagnify(stringTointPrint(stdsize[3]), stringTointPrint(stdsize[4]));
                    //cpclprinter.printCPCLText(stringTointPrint(stdsize[6]), 0, 2, stringTointPrint(stdsize[0]), stringTointPrint(stdsize[1]), map.get("Std_Size"), 0);
                    //cpclprinter.printAndroidFont(stringTointPrint(stdsize[0]), stringTointPrint(stdsize[1]), stringToNullCheck(map, "Std_Size", ""), intToinchPrint(spp.getLavel_Width()), stringTointPrint(stdsize[3]+stdsize[4])+10);
                    if (!stringToNullCheck(map, "Std_Size", "").equals("")) {
                        cpclprinter.printAndroidFont(stringTointPrint(stdsize[0]), stringTointPrint(stdsize[1]), stringToNullCheck(map, "Std_Size", " "), intToinchPrint(spp.getLavel_Width()), stringTointPrint(stdsize[3] + stdsize[4]) + 10);
                    }
                }

                //가격 출력
                if (spp.getPrint_Price_YN() == 1) {
                    cpclprinter.setMagnify(stringTointPrint(price[3]), stringTointPrint(price[4]));
                    cpclprinter.printCPCLText(stringTointPrint(price[6]), 7, 0, stringTointPrint(price[0]), stringTointPrint(price[1]), StringFormat.convertToNumberFormat(stringToNullCheck(map, "Sell_Pri", "0")) + " 원", 0);
                    cpclprinter.resetMagnify();
                    //cpclprinter.printAndroidFont(stringTointPrint(price[0]), stringTointPrint(price[1]), map.get("Std_Size"), intToinchPrint(spp.getLavel_Width()), stringTointPrint(price[3]+price[4])+10);
                }

                //거래처 출력
                if (spp.getPrint_Office_YN() == 1) {
                    //cpclprinter.setMagnify(stringTointPrint(office[3]), stringTointPrint(office[4]));
                    //cpclprinter.printCPCLText(stringTointPrint(office[6]), 0, 0, stringTointPrint(office[0]), stringTointPrint(office[1]), map.get("Bus_Name"), 0);
                    cpclprinter.printAndroidFont(stringTointPrint(office[0]), stringTointPrint(office[1]), stringToNullCheck(map, "Bus_Name", "거래처"), intToinchPrint(spp.getLavel_Width()), stringTointPrint(office[3] + office[4]) + 10);
                }

                //단가 출력
                if (spp.getPrint_Danga_YN() == 1) {
                    double a = Double.parseDouble(stringToNullCheck(map, "Con_Rate", "0"));
                    double b = Double.parseDouble(stringToNullCheck(map, "Std_Rate", "0"));
                    double c = Double.parseDouble(stringToNullCheck(map, "Sell_Pri", "0"));
                    a = a / b;

                    String dan = String.valueOf(Math.round(c / a)) + " 원/ " + stringToNullCheck(map, "Std_Rate", "0") + stringToNullCheck(map, "Unit", "0");
                    Log.i("기준단가", dan);

                    //cpclprinter.setMagnify(stringTointPrint(danga[3]), stringTointPrint(danga[4]));
                    //cpclprinter.printCPCLText(stringTointPrint(danga[6]), 0, 0, stringTointPrint(danga[0]), stringTointPrint(danga[1]), dan, 0);
                    cpclprinter.printAndroidFont(stringTointPrint(danga[0]), stringTointPrint(danga[1]), dan, intToinchPrint(spp.getLavel_Width()), stringTointPrint(danga[3] + danga[4]) + 10);
                }

                //--------------------------------------------------------------------------------//
                // 2021.01.06.김영목. 원판매가,할인율 추가
                //--------------------------------------------------------------------------------//
                double pri = Double.parseDouble(stringToNullCheck(map, "Sell_Pri", "0"));
                double org = Double.parseDouble(stringToNullCheck(map, "Sell_Org", "0"));
                //if (org > pri) {
                //원판매가 출력
                if (spp.getPrint_SellPrice_YN() == 1) {
                    cpclprinter.setMagnify(stringTointPrint(sellPrice[3]), stringTointPrint(sellPrice[4]));
                    cpclprinter.printCPCLText(stringTointPrint(sellPrice[6]), 7, 0, stringTointPrint(sellPrice[0]), stringTointPrint(sellPrice[1]), StringFormat.convertToNumberFormat(stringToNullCheck(map, "Sell_Org", "0")) + "", 0);
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
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
                    if (!stringToNullCheck(map, "BranchName", "").equals("")) {
                        try {
                            cpclprinter.printAndroidFont(stringTointPrint(branchName[0]), stringTointPrint(branchName[1]), stringToNullCheck(map, "BranchName", " "), intToinchPrint(spp.getLavel_Width()), stringTointPrint(branchName[3] + branchName[4]) + 10);
                        } catch (Exception e) {
                            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                //추가항목 출력
                if (spp.getPrint_AddItem_YN() == 1) {
                    try {
                        if (!stringToNullCheck(map, "AddItem", "").equals("")) {
                            cpclprinter.printAndroidFont(stringTointPrint(addItem[0]), stringTointPrint(addItem[1]), stringToNullCheck(map, "AddItem", " "), intToinchPrint(spp.getLavel_Width()), stringTointPrint(addItem[3] + addItem[4]) + 10);
                        }
                    } catch (Exception e) {
                        //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
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
        }

        //저장하기
        dba.insert_barPrint(mfillMaps, "1");
        mfillMaps.removeAll(mfillMaps);
        m_adapter.notifyDataSetChanged();

        dialog.dismiss();
        dialog.cancel();

        Toast.makeText(this, "발행이 완료 되었습니다.", Toast.LENGTH_SHORT).show();


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
            m_printerState.setText("발행 목록 : LK-P30N(연결안됨)");
        }

        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        return result;
    }

    //바코드 발행하기
    //바코드 프린터 spp-l3000 전송하기
    //SPP3000
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
        //mBixolonLabelPrinter.setLength(spp.getLavel_Hight(), spp.getGap_Width(), intTopapergubun(spp.getPaper_Gubun()), spp.getGap_Width());
        mBixolonLabelPrinter.setLength(spp.getGap_Width(), spp.getLavel_Hight(), intTopapergubun(spp.getPaper_Gubun()), spp.getGap_Width());

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

        for (int i = 0; i < mfillMaps.size(); i++) {

            HashMap<String, String> map = mfillMaps.get(i);

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

                mBixolonLabelPrinter.drawText(StringFormat.convertToNumberFormat(map.get("Sell_Pri")), stringTointPrint(price[0]), stringTointPrint(price[1]),
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
                double c = Double.parseDouble(map.get("Sell_Pri"));
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
            double pri = Double.parseDouble(stringToNullCheck(map, "Sell_Pri", "0"));
            double org = Double.parseDouble(stringToNullCheck(map, "Sell_Org", "0"));
            if (org > pri) {
                //원판매가 출력
                if (spp.getPrint_SellPrice_YN() == 1) {
                    mBixolonLabelPrinter.drawText(StringFormat.convertToNumberFormat(map.get("Sell_Org")), stringTointPrint(sellPrice[0]), stringTointPrint(sellPrice[1]),
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
                mBixolonLabelPrinter.drawText(map.get("BranchName"), stringTointPrint(branchName[0]), stringTointPrint(branchName[1]), fontSizeToString(branchName[2]),
                        stringTointPrint(branchName[3]) + 1, stringTointPrint(branchName[4]) + 1, 0,
                        stringTointPrint(branchName[6]), false, booleanToString(branchName[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }

            //추가항목 출력
            if (spp.getPrint_AddItem_YN() == 1) {
                mBixolonLabelPrinter.drawText(map.get("AddItem"), stringTointPrint(addItem[0]), stringTointPrint(addItem[1]), fontSizeToString(addItem[2]),
                        stringTointPrint(addItem[3]) + 1, stringTointPrint(addItem[4]) + 1, 0,
                        stringTointPrint(addItem[6]), false, booleanToString(addItem[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }
            //--------------------------------------------------------------------------------//

            mBixolonLabelPrinter.draw1dBarcode(map.get("BarCode"), stringTointPrint(barcode[0]), stringTointPrint(barcode[1]), stringTointPrint(barcode[2]),
                    stringTointPrint(barcode[3]), stringTointPrint(barcode[4]), stringTointPrint(barcode[5]), stringTointPrint(barcode[6]),
                    stringTointPrint(barcode[7]), 0);

            mBixolonLabelPrinter.print(1, stringTointPrint(map.get("Count")));

        }


        //저장하기
        dba.insert_barPrint(mfillMaps, "1");
        mfillMaps.removeAll(mfillMaps);
        m_adapter.notifyDataSetChanged();

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
        if (res == null) res = "";

        return res;
    }


    //세우 프린터 변환값 - 끝 -

    public void onClickSetDate1(View v) {
        DatePickerDialog newDlg = new DatePickerDialog(this, this, m_dateCalender1.get(Calendar.YEAR),
                m_dateCalender1.get(Calendar.MONTH), m_dateCalender1.get(Calendar.DAY_OF_MONTH));
        newDlg.show();

        m_dateMode = 1;

    }

    public void onClickSetPrintDate(View v) {
        DatePickerDialog newDlg = new DatePickerDialog(this, this,
                m_dateCalender2.get(Calendar.YEAR),
                m_dateCalender2.get(Calendar.MONTH),
                m_dateCalender2.get(Calendar.DAY_OF_MONTH));
        newDlg.show();

        m_dateMode = 2;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        if (m_dateMode == 1) {
            m_dateCalender1.set(year, monthOfYear, dayOfMonth);
            m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        } else if (m_dateMode == 2) {
            m_dateCalender2.set(year, monthOfYear, dayOfMonth);
            printSetDateButton.setText(m_dateFormatter.format(m_dateCalender2.getTime()));
        }

        m_dateMode = 0;

    }

    //리스트저장하기
    private void sendList() {
        String barcode = m_barcode.getText().toString();
        if (!barcode.equals("")) // 입력한 Barcode가 값이 있을 경우만
            doQueryWithBarcode(barcode);
    }

    //목록삭제하기
    public void deleteListViewAll() {
        if (mfillMaps.isEmpty()) return;

        mfillMaps.removeAll(mfillMaps);
        m_adapter.notifyDataSetChanged();
    }

    //초기화
    public void listReNew(View view) {
        deleteListViewAll();
    }

    //변경된 날짜로 검색하기
    public void onDateChangeSearch(View view) {

        String query = "";
        String searchDate = m_period.getText().toString();

        //2019-11-13 공병 누락 금액 처리 추가
        query += " SELECT ";
        query += "  A.BarCode AS Barcode "; // 바코드
        query += " ,A.G_Name AS G_Name "; // 상품명

        // 정상가(판매가+공병매출가)/행사가
        if (c_Sale_YN.isChecked()) {
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
        if (sellPri1CheckBox.isChecked()) {
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
        if (BranchNameType == 1) {
            query += " ,ISNULL(A.L_Name,'') AS BranchName  "; // 대분류명
        } else if (BranchNameType == 2) {
            query += " ,ISNULL(A.M_Name,'') AS BranchName  "; // 중분류명
        } else if (BranchNameType == 3) {
            query += " ,ISNULL(A.S_Name,'') AS BranchName  "; // 중분류명
        } else {
            query += " ,ISNULL(A.L_Name,'') AS BranchName  "; // 대분류명
        }

        if (AddItemType == 1) {
            String tmpDate = printSetDateButton.getText().toString();
            if (printDateTitleCheckBox.isChecked()) {
                tmpDate = "*출력일:" + tmpDate;
            }
            query += " ,'" + tmpDate + "' AS AddItem  "; // 출력일
        } else if (AddItemType == 2) {
            String tmpUser = printUserTextEditText.getText().toString();
            query += " ,'" + tmpUser + "' AS AddItem  "; // 사용자정의
        } else {
            query += " ,'' AS AddItem  "; // 사용자정의
        }
        //----------------------------------------//

        query += " FROM Goods A ";
        query += " LEFT OUTER JOIN (SELECT Barcode, nickName FROM goods_nick WHERE nickgubun = '1' ) B ";
        query += " ON A.Barcode = B.Barcode ";
        query += " WHERE ";
        query += " A.Edit_Date='" + searchDate + "' ";
        query += " AND A.Scale_use = '0' ";    //공산품만 검색
        query += " AND A.goods_use = '1' ";    //상품사용만 검색
        query += " Order By A.BarCode ASC;";

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

                    printerListViewOffice(results);
                } else {
                    Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();
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

    //거래처 상품 조회하기
    public void doOfficeProductSearch() {

        String index = String.valueOf(mfillMaps.size());
        String query = "";
        String customerCode = m_officecode.getText().toString();
        String customerName = m_officename.getText().toString();

        if (customerCode.equals("") || customerName.equals("")) {
            Toast.makeText(this, "거래처 검색이 잘못 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        //2019-11-13 공병 누락 금액 처리 추가
        query += " SELECT ";
        query += "  A.BarCode AS BarCode "; // 바코드
        query += " ,A.G_Name AS G_Name "; // 상품명

        // 정상가(판매가+공병매출가)/행사가
        if (c_Sale_YN.isChecked()) {
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
        if (sellPri1CheckBox.isChecked()) {
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
        if (BranchNameType == 1) {
            query += " ,ISNULL(A.L_Name,'') AS BranchName  "; // 대분류명
        } else if (BranchNameType == 2) {
            query += " ,ISNULL(A.M_Name,'') AS BranchName  "; // 중분류명
        } else if (BranchNameType == 3) {
            query += " ,ISNULL(A.S_Name,'') AS BranchName  "; // 중분류명
        } else {
            query += " ,ISNULL(A.L_Name,'') AS BranchName  "; // 대분류명
        }

        if (AddItemType == 1) {
            String tmpDate = printSetDateButton.getText().toString();
            if (printDateTitleCheckBox.isChecked()) {
                tmpDate = "*출력일:" + tmpDate;
            }
            query += " ,'" + tmpDate + "' AS AddItem  "; // 출력일
        } else if (AddItemType == 2) {
            String tmpUser = printUserTextEditText.getText().toString();
            query += " ,'" + tmpUser + "' AS AddItem  "; // 사용자정의
        } else {
            query += " ,'' AS AddItem  "; // 사용자정의
        }
        //----------------------------------------//

        query += " FROM Goods A ";
        query += " LEFT OUTER JOIN (SELECT Barcode, nickName FROM goods_nick WHERE nickgubun = '1' ) B ";
        query += " ON A.Barcode = B.Barcode ";
        query += " WHERE ";
        query += " A.Bus_Name = '" + customerName + "' ";
        query += " AND A.Bus_Code = '" + customerCode + "' ";
        query += " AND A.Scale_use = '0' ";    //공산품만 검색
        query += " AND A.goods_use = '1' ";    //상품사용만 검색
        query += " Order By A.BarCode ASC;";

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

                    printerListViewOffice(results);
                } else {
                    Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();
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

    //목록에 뿌려줍니다.
    private void printerListViewOffice(JSONArray results) {

        for (int i = 0; i < results.length(); i++) {
            try {
                JSONObject json2 = results.getJSONObject(i);
                HashMap<String, String> map2 = JsonHelper.toStringHashMap(json2);
                map2.put("Count", "1"); //강제로 1개로 셋팅 합니다.

                //----------------------------------------//
                // 2021.01.06.김영목. 할인율 추가
                //----------------------------------------//
                float f_ratio = 0;
                float f_sellPri = Float.valueOf(map2.get("Sell_Pri")).floatValue();
                float f_sellOrg = Float.valueOf(map2.get("Sell_Org")).floatValue();

                try {
                    if (f_sellOrg > f_sellPri) {
                        f_ratio = (f_sellOrg - f_sellPri) / f_sellOrg * 100;
                        if (Float.isInfinite(f_ratio) || Float.isNaN(f_ratio)) {
                            f_ratio = 0;
                        }
                    }
                } catch (Exception e) {
                }
                map2.put("Sale_Rate", String.format("%.0f", f_ratio)); // 할인율
                //----------------------------------------//

                mfillMaps.add(map2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        m_adapter.notifyDataSetChanged();
        m_barcode.requestFocus();
    }

    //상품조회하기
    public void doQueryWithBarcode(String barcode) {

        String query = "";

        //2019-11-13 공병 누락 금액 처리 추가
        query += " SELECT ";
        query += "  A.BarCode AS BarCode "; // 바코드
        query += " ,A.G_Name AS G_Name "; // 상품명

        // 정상가(판매가+공병매출가)/행사가
        if (c_Sale_YN.isChecked()) {
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
        if (sellPri1CheckBox.isChecked()) {
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
        if (BranchNameType == 1) {
            query += " ,ISNULL(A.L_Name,'') AS BranchName  "; // 대분류명
        } else if (BranchNameType == 2) {
            query += " ,ISNULL(A.M_Name,'') AS BranchName  "; // 중분류명
        } else if (BranchNameType == 3) {
            query += " ,ISNULL(A.S_Name,'') AS BranchName  "; // 중분류명
        } else {
            query += " ,ISNULL(A.L_Name,'') AS BranchName  "; // 대분류명
        }

        if (AddItemType == 1) {
            String tmpDate = printSetDateButton.getText().toString();
            if (printDateTitleCheckBox.isChecked()) {
                tmpDate = "*출력일:" + tmpDate;
            }
            query += " ,'" + tmpDate + "' AS AddItem  "; // 출력일
        } else if (AddItemType == 2) {
            String tmpUser = printUserTextEditText.getText().toString();
            LocalStorage.setString(this, "AddItemUserText", tmpUser); //로컬에 저장하여 재사용
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

        if (barcode.equals("")) return;

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
                    printerListView(results);
                } else {
                    Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();

                    //2020-06-15
                    m_barcode.setText("");
                    m_gname.setText("");
                    m_barcode.requestFocus();
                    //getCurrentFocus().requestFocus();
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    //목록에 뿌려줍니다.
    private void printerListView(JSONArray results) {

        for (int i = 0; i < results.length(); i++) {
            try {
                JSONObject json2 = results.getJSONObject(i);
                HashMap<String, String> map2 = JsonHelper.toStringHashMap(json2);
                map2.put("Count", m_count.getText().toString());

                //----------------------------------------//
                // 2021.01.06.김영목. 할인율 추가
                //----------------------------------------//
                float f_ratio = 0;
                float f_sellPri = Float.valueOf(map2.get("Sell_Pri")).floatValue();
                float f_sellOrg = Float.valueOf(map2.get("Sell_Org")).floatValue();

                try {
                    if (f_sellOrg > f_sellPri) {
                        f_ratio = (f_sellOrg - f_sellPri) / f_sellOrg * 100;
                        if (Float.isInfinite(f_ratio) || Float.isNaN(f_ratio)) {
                            f_ratio = 0;
                        }
                    }
                } catch (Exception e) {
                }
                map2.put("Sale_Rate", String.format("%.0f", f_ratio)); // 할인율
                //----------------------------------------//

                mfillMaps.add(map2);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        m_adapter.notifyDataSetChanged();

        //2020-06-15
        m_barcode.setText("");
        m_gname.setText("");
        m_barcode.requestFocus();
        //getCurrentFocus().requestFocus();

        SoundPool mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        int beep = mSoundPool.load(getApplicationContext(), R.raw.windowsding, 1);
        //.play(사운드파일, 왼쪽볼륨, 오른쪽볼륨,
        mSoundPool.play(beep, 1.0f, 1.0f, 0, 0, 0.5f);
    }

    //발행 목록 보여주기
    public void barPrintHistory() {
        Intent intent = new Intent(this, BarcodePrintHistoryListActivity.class);
        intent.putExtra("OfficeCode", m_OfficeCode);
        startActivityForResult(intent, BARCODEPRINT_HISTORY_REQUEST);
    }

    //재발행 다시 불러오기
    private void barPrintHistoryList(String baprint_num) {

        try {

            mfillMaps.removeAll(mfillMaps);
            String query = "Select * From BaPrint_History Where BaPrint_Num='" + baprint_num.toString() + "' ; ";
            mfillMaps.addAll(dba.getBarPrintHistory(query));


            //----------------------------------------//
            // 2023.02.15.김영목. 재발행시 누락된 목록 추가
            // Sell_Org, Sale_Rate, Location,NickName,BranchName,AddItem
            //----------------------------------------//
            HashMap<String, String> map = new HashMap<String, String>();
                String content = "";
                map.putAll(mfillMaps.get(0));

            float f_ratio = 0;
            float f_sellPri = Float.valueOf(map.get("Sell_Pri")).floatValue();
            float f_sellOrg = Float.valueOf(map.get("Sell_Org")).floatValue();

            try {
                if (f_sellOrg > f_sellPri) {
                    f_ratio = (f_sellOrg - f_sellPri) / f_sellOrg * 100;
                    if (Float.isInfinite(f_ratio) || Float.isNaN(f_ratio)) {
                        f_ratio = 0;
                    }
                }
            } catch (Exception e) {
            }
            map.put("Sale_Rate", String.format("%.0f", f_ratio)); // 할인율

            //----------------------------------------//



            Log.d(TAG, this.mfillMaps.toString());

            m_adapter.notifyDataSetChanged();
            m_barcode.setText("");
            m_barcode.requestFocus();

            SoundPool mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            int beep = mSoundPool.load(getApplicationContext(), R.raw.windowsding, 1);
            //.play(사운드파일, 왼쪽볼륨, 오른쪽볼륨,
            mSoundPool.play(beep, 1.0f, 1.0f, 0, 0, 0.5f);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void getBaPrintTemp() {


    }

    //임시저장자료 불러오기
    private void TempBaPrintList() {

        try {

            mfillMaps.removeAll(mfillMaps);
            String query = "Select * From Temp_BaPrint;";
            mfillMaps.addAll(dba.getTempBaPrint(query));
            // 임시저장자료 다 불러온 후 지운다
            dba.delete_TempBaPrint();
            Log.d(TAG, this.mfillMaps.toString());

            m_adapter.notifyDataSetChanged();
            m_barcode.setText("");
            m_barcode.requestFocus();

            SoundPool mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
            int beep = mSoundPool.load(getApplicationContext(), R.raw.windowsding, 1);
            //.play(사운드파일, 왼쪽볼륨, 오른쪽볼륨,
            mSoundPool.play(beep, 1.0f, 1.0f, 0, 0, 0.5f);

        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void showSelectedLKP30II() {

        final CharSequence[] items = {"택설정", "프린터연결", "연결종료"};
        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        //----------------------------------------//

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
                                        m_printerState.setText("발행 목록 : LK-P30N(연결안됨)");
                                    } catch (IOException | InterruptedException e) {
                                        e.printStackTrace();
                                        Toast.makeText(mContext, "블루투스 연결종료 실패", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(mContext, "프린터가 연결되어있지 않습니다.", Toast.LENGTH_SHORT).show();
                                    m_printerState.setText("발행 목록 : LK-P30N(연결안됨)");
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

    // 2021.01.05.김영목. 바코드프린터 조회하기
    public void getBarcodePrinterSet() {

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
        selectPrinterSpinner.setAdapter(adapter);

        //2023.02.09. 여기에서 선택된 바코드프린터 인덱스 체크
        int SelectBarcodePrinter = 0; //선택바코드프린터 인덱스
        try {
            SelectBarcodePrinter = LocalStorage.getInt(mContext, "SelectBarcodePrinter");
        } catch (RuntimeException e) {
            LocalStorage.setInt(mContext, "SelectBarcodePrinter", 0);
        }
        int spinnerCnt = selectPrinterSpinner.getAdapter().getCount();
        if (spinnerCnt > SelectBarcodePrinter)
            selectPrinterSpinner.setSelection(SelectBarcodePrinter);
    }

    // 2021.01.05.김영목. 바코드프린터 조회하기
    public void sendTempAppGoods() {

        String query = "";

        // 현재 시간을 msec으로 구한다.
        long now = System.currentTimeMillis();
        // 현재 시간을 저장 한다.
        Date date = new Date(now);

        // 시간 포맷으로 만든다.
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMddHHmmss");
        String taCode = posID + sdfNow.format(date);

        HashMap<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < mfillMaps.size(); i++) {
            String content = "";
            map.putAll(mfillMaps.get(i));

            query += "INSERT INTO temp_app_goods (ta_code,ta_seq,barcode,qty,sell_pri) VALUES( ";
            query += " '" + taCode + "'";   // 2. 코드
            query += ", " + String.format("%d", i + 1) + " ";   // 4. 순번
            query += ",'" + map.get("BarCode") + "'";   // 5. 바코드
            query += ", " + map.get("Count") + " ";   // 6. 수량
            query += ", " + map.get("Sell_Pri") + " );"; // 7. 판매가
        }

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();
        new MSSQL4(new MSSQL4.MSSQL4CallbackInterface() {
            @Override
            public void onRequestCompleted(Integer results) {
                dialog.dismiss();
                dialog.cancel();

                dba.insert_barPrint(mfillMaps, "1");
                mfillMaps.removeAll(mfillMaps);
                m_adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "전송 성공", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "전송 실패", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

    }

    private void startBluetoothPrintSearch() {
        //Intent intent = new Intent(mContext, BluetoothConnectMenu.class);
        //startActivity(intent);
        blueConnect.setStart(m_printerState);
    }

    private void showSelectedSppl3000() {

        final CharSequence[] items = {"택설정", "프린터연결", "연결종료"};

        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        //----------------------------------------//

        // 제목셋팅
        alertDialogBuilder.setTitle("옵션을 선택해 주세요");
        alertDialogBuilder.setItems(items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 프로그램을 종료한다
                        switch (items[id].toString()) {
                            case "택설정":
                                Intent intent = new Intent(mContext, BarcodePrinterSPPL3000.class);
                                startActivity(intent);
                                break;
                            case "프린터연결":
                                if (!mBixolonLabelPrinter.isConnected()) {
                                    mBixolonLabelPrinter.findBluetoothPrinters();
                                } else {
                                    Toast.makeText(mContext, "프린터가 연결되어 있습니다.", Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case "연결종료":
                                if (mBixolonLabelPrinter.isConnected()) {
                                    mBixolonLabelPrinter.disconnect();
                                } else {
                                    Toast.makeText(mContext, "프린터가 연결되어있지 않습니다.", Toast.LENGTH_SHORT).show();
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

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BixolonLabelPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BixolonLabelPrinter.STATE_CONNECTED:
                            m_printerState.setText("발행 목록 : 연결됨(" + mConnectedDeviceName + ")");
                            break;

                        case BixolonLabelPrinter.STATE_CONNECTING:
                            m_printerState.setText("발행 목록 : 연결중...");
                            break;

                        case BixolonLabelPrinter.STATE_NONE:
                            m_printerState.setText("발행 목록 : 연결안됨");
                            break;
                    }
                    break;

                case BixolonLabelPrinter.MESSAGE_READ:
                    BarcodePrinterActivity.this.dispatchMessage(msg);
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
                        DialogManager.showBluetoothDialog(BarcodePrinterActivity.this, (Set<BluetoothDevice>) msg.obj);
                    }
                    break;
				/*
				case BixolonLabelPrinter.MESSAGE_USB_DEVICE_SET:
					if(msg.obj == null)
					{
						Toast.makeText(getApplicationContext(), "No connected device", Toast.LENGTH_SHORT).show();
					}
					else
					{
						DialogManager.showUsbDialog(BarcodePrinterActivity.this, (Set<UsbDevice>) msg.obj, mUsbReceiver);
					}
					break;

				case BixolonLabelPrinter.MESSAGE_NETWORK_DEVICE_SET:
					if(msg.obj == null)
					{
						Toast.makeText(getApplicationContext(), "No connectable device", Toast.LENGTH_SHORT).show();
					}
					DialogManager.showNetworkDialog(BarcodePrinterActivity.this, (Set<String>) msg.obj);
					break;
				*/
            }
        }
    };

    //2017-04 m3mobile 추가
    @Override
    protected void onResume() {
        super.onResume();
        if (m3Mobile) {
            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_barcode, m_count);
            M3MoblieBarcodeScanBroadcast.setOnResume();
        }

        tips.setReflash();
    }

    //2017-04 m3mobile 추가
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m3Mobile) {
            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_barcode, m_count);
            M3MoblieBarcodeScanBroadcast.setOnDestory();
        }

        //빅슬론 spp-l3000
        if (m_checkPrinter == 2 && mBixolonLabelPrinter.isConnected()) {
            mBixolonLabelPrinter.disconnect();
        }

        //세우테크 lkp30
        if (m_checkPrinter == 3 && bluetoothPort.isConnected()) {
            blueConnect.close();
        }
    }


    //스캐너및 목록에서 검색시 돌려받아 처리합니다.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            // 카메라 스캔을 통한 바코드 검색
            case ZBAR_SCANNER_REQUEST:
                if (resultCode == RESULT_OK) {
                    // Scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT)
                    // Type of the scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
                    Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE), Toast.LENGTH_SHORT).show();
                    // The value of type indicates one of the symbols listed in Advanced Options below.

                    String barcode = data.getStringExtra(ZBarConstants.SCAN_RESULT);
                    m_barcode.setText(barcode);
                    doQueryWithBarcode(barcode);

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
                }
                break;
            // 목록 검색을 통한 바코드 검색
            case BARCODE_MANAGER_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) data.getSerializableExtra("fillmaps");
                    //m_barcode.setText(hashMap.get("BarCode"));
                    doQueryWithBarcode(hashMap.get("BarCode"));
                }
                break;
            case CUSTOMER_MANAGER_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    HashMap<String, String> hashMap = (HashMap<String, String>) data.getSerializableExtra("fillmaps");
                    m_officecode.setText(hashMap.get("Office_Code"));
                    m_officename.setText(hashMap.get("Office_Name"));

                    doOfficeProductSearch();
                }
                break;
            //바코드 프린터 재발행 목록 불러오기
            case BARCODEPRINT_HISTORY_REQUEST:
                if (resultCode == RESULT_OK && data != null) {
                    //List <HashMap<String, String>> hashMap = (List<HashMap<String, String>>)data.getSerializableExtra("fillmaps");
                    String baprint_num = data.getStringExtra("BaPrint_Num");
                    barPrintHistoryList(baprint_num);
                }
                break;
        }
    }

    //옵션화면

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
            actionbar.setTitle("바코드발행관리");

            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_code, menu);
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

    //바코드 프린터 소켓통신으로 전송하기
    public class BarcodePrinterAsyncTask extends AsyncTask<String, Integer, Boolean> {

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
                    content += map.get("Count");
                    count_num = count_num + Integer.parseInt(map.get("Count"));
                    //content += "; \n\r";
                    content += "; ";
                    content += StringFormat.convertToNumberFormat(map.get("Sell_Pri"));
                    //content += "; \n\r";
                    content += "; ";
                    content += map.get("Bus_Name");
                    //content += "; \n\r";
                    content += "; ";

                    double a = Double.parseDouble(map.get("Con_Rate"));
                    double b = Double.parseDouble(map.get("Std_Rate"));
                    double c = Double.parseDouble(map.get("Sell_Pri"));
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
                    content += StringFormat.convertToNumberFormat(map.get("Sell_Org"));
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
                    content += map.get("BranchName");

                    //추가항목
                    content += "; ";
                    content += map.get("AddItem");
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
                dba.insert_barPrint(mfillMaps, "1");
                mfillMaps.removeAll(mfillMaps);
                m_adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), String.valueOf(count_num) + " 개 발행완료", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "바코드프린터 소켓연결을 확인해 주세요.", Toast.LENGTH_LONG).show();
            }
        }
    }

}
