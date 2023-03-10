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
 * ???????????? -> ????????????
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
    // 2022.05.26.???????????? IP??????
    //----------------------------------------//
    String m_ip = "";
    String m_port = "";
    //----------------------------------------//
    // 2021.12.21. ??????DB IP,PW,DB ??????
    //----------------------------------------//
    String m_uuid = "";
    String m_uupw = "";
    String m_uudb = "";
    //----------------------------------------//

    String m_posID = "";

    /* ????????? 2020.09.14. ????????? ?????? ????????? ?????? ?????? ?????? */
    int m_KeypadType = 0;

    String m_officeCode = "";

    String m_barcode;
    String productReg_YN;
    String mGubun;        //????????? ??????????????? ???????????????.
    String mPreviousActivity = "";

    EditText m_textBarcode; // 1.?????????
    EditText m_textProductName; // 2.?????????
    EditText m_textCustomerCode; // 3.???????????????
    EditText m_textCustomerName; // 4.????????????
    Button m_buttonbarcode; // ?????????????????????
    Button m_buttonCustomerClassification1; // 5-1.?????????
    Button m_buttonCustomerClassification2; // 5-2.?????????
    Button m_buttonCustomerClassification3; // 5-3.?????????
    EditText m_textStandard; // 9.??????
    EditText m_textAcquire; // 10.??????
    Button buttonAcquire;    //???????????? ??????
    EditText m_textPurchasePrice; // 11.?????????
    EditText m_textPurchasePriceOriginal; // 12.????????????
    /*
     * 2014-12-18??? ?????? ?????? ????????? ??????, ?????? ?????? ??????
     */
    CheckBox m_checkboxPurPrivatGubun; // ????????? ?????? ?????? ??????
    EditText m_textPurchasePriceOriginalTax;// 13.?????????
    EditText m_textSalesPrice; // 14.?????????

    //----------------------------------------//
    //2022-05-26 ?????????????????? ??????
    //----------------------------------------//
    EditText m_editTextSellPri1;
    //----------------------------------------//

    EditText m_textDifferentRatio; // 15.?????????(%)
    EditText m_textRealSto; // 16.??????
    Spinner m_spinTaxation; // 6.?????????
    // CheckBox m_checkSurtax;
    Spinner m_spinGoods; // 7.????????????
    //Spinner m_spinGroup; // 8.??????
    Spinner m_spinMemberPoint; // 17.????????????
    Spinner m_spinGoodsType; // 18.????????????
    EditText m_textEditDate; // 19.?????????
    EditText m_textEditModified; // 20.?????????

    //2018-02-20 ???????????? ????????????
    EditText m_textASell_Pri; // 21.??????A??????
    EditText m_textADifferentRatio; // 22. ??????A?????????
    EditText m_textBSell_Pri; //23. ??????B??????
    EditText m_textBDifferentRatio; // 24. ??????B?????????
    EditText m_textCSell_Pri; // 25.??????C??????
    EditText m_textCDifferentRatio; // 26. ??????C?????????

    EditText m_textEventPurPri;    //???????????????
    EditText m_textEventSalePri;    //???????????????

    EditText m_textStartEventDate;    //???????????????
    EditText m_textEndEventDate;    //???????????????


    //2020-06-15 ?????? ????????? ????????????
    Spinner m_spinGoodsDanwi;        //??????
    EditText m_TextCon_Rate;    //?????????
    EditText m_TextStd_Rate;    //????????????

    // ListView m_listProduct;
    Button m_registButton;
    Button m_modfiyButton;
    Button m_recoveryButton;

    Button m_barprint;    //?????????
    Button m_barprintTran;    //?????????

    //----------------------------------------//
    //2023-01-04 ???????????? ??????
    //----------------------------------------//
    // 1 row ?????? ??????
    Button buttonPrinterSetting;    // ????????? ??????
    Button buttonPrinterOption;     // ????????? ??????
    Button buttonPrinterPrint;      // ????????? ??????

    // 2 row ????????? ?????? ??????
    TextView textViewPrinterState; // ????????? ?????? ??????
    Spinner spinnerSelectPrinter; // ?????????????????????????????????

    // ????????? ?????? ?????? ??????
    int barcodePrinterCheck; //??????????????? ??????(??????)
    String barcodePrinterName; //??????????????? ???
    String barcodePrinterAddress; //??????????????? ??????
    String barcodePrinterPort;    //??????????????? ??????
    int barcodePrinterGubun;    //??????????????? ????????????

    CheckBox checkboxSaleYn;            // ????????? ????????????
    CheckBox checkboxSellPri1;          //?????????????????? ????????????
    Spinner spinnerSelectBranchName;    //??????(?????????,?????????,????????? ??????)
    Spinner spinnerSelectAddItem;       // ???????????? ??????(?????????,???????????????)
    Button buttonPrintSetDate;          // ???????????? ?????????
    EditText edittextPrintUserText;     // ???????????? ??????????????? ????????? ??????
    CheckBox checkboxPrintDateTitle;    //????????? ?????? ????????????
    Calendar calendarPrintSetDate;      //???????????? ????????? ??????

    String mSaleYn = "0";               // ?????????
    String mSellPri1 = "0";             //??????????????????
    int mSelectBranchName = 1;          //??????(?????????,?????????,????????? ??????)
    int mSelectAddItem = 1;             // ???????????? ??????(?????????,???????????????)
    String mPrintSetDate = "";          // ???????????? ?????????
    String mPrintUserText = "";         // ???????????? ??????????????? ????????? ??????
    String mPrintDateTitle = "1";       //????????? ?????? ????????????

    //int BranchNameType = 1; //1:?????????(??????),2:?????????,3:?????????
    //int AddItemType = 1; // ??????????????????(1:?????????(??????), 2:???????????????)
    //String AddItemUserText="";

    BarcodePrinterAsyncTask2 m_bprinter;
    //----------------------------------------//

    //----------------------------------------//
    // 2023.02.08. ????????? ???????????? ?????? ??????
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

    //2018-10-26 ????????? ?????? ????????? ????????? ??????
    private String mConnectedDeviceName = null;
    static public BixolonLabelPrinter mBixolonLabelPrinter;
    public Handler m_hHandler = null;
    public BluetoothAdapter m_BluetoothAdapter = null;
    public ArrayAdapter<String> adapter = null;
    public ArrayList<BluetoothDevice> m_LeDevices;

    //???????????? ?????? ????????? ??????
    //private ZPLRFIDPrinter zplprinter;
    private BluetoothConnectMenu blueConnect;
    private BluetoothPort bluetoothPort;

    // loading bar
    public ProgressDialog dialog;
    DBAdapter dba;
    TIPS_Config tips;

    AlertDialog m_alert;

    // ????????? ?????? ???????????? ?????? ??????(??????,?????? ?????? ??????)
    int workIndex = 0; //(0:????????????????????????,1:???????????????,2:???????????????)

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
//		toolbarTitle.setText("????????????");
//		//----------------------------------------//

        ccp = new CalculateChangePrice();
        m_shop = LocalStorage.getJSONObject(this, "currentShopData");

        try {
            m_ip = m_shop.getString("SHOP_IP");
            m_port = m_shop.getString("SHOP_PORT");
            //----------------------------------------//
            // 2021.12.21. ??????DB IP,PW,DB ??????
            //----------------------------------------//
            m_uuid = m_shop.getString("shop_uuid");
            m_uupw = m_shop.getString("shop_uupass");
            m_uudb = m_shop.getString("shop_uudb");
            //----------------------------------------//

            String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
            m_officeCode = m_shop.getString("OFFICE_CODE");
            m_posID = LocalStorage.getString(this, "currentPosID:" + OFFICE_CODE);

            /* ????????? 2020.09.14. ????????? ?????? ????????? ?????? ?????? ?????? */
            m_KeypadType = LocalStorage.getInt(this, "KeypadType:" + OFFICE_CODE);

            //----------------------------------------//
            // ???????????? ?????????????????? ?????? ??????????????? ????????????
            //----------------------------------------//
            mSaleYn = LocalStorage.getString(this, "BarcodePrinterSaleYN");        //????????? ?????? ?????????(1:?????????,0:)
            mSellPri1 = LocalStorage.getString(this, "BarcodePrinterSellPri1");    //?????????????????? ?????????(0:??????,1:??????????????????)

            //????????? ?????? ?????????
            mSelectBranchName = 1;
            mSelectAddItem = 1; //??????????????????(1:?????????,2:?????????????????????

            //???????????? ????????? ????????? ??????
            SimpleDateFormat simpleDateFormat;
            simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
            calendarPrintSetDate = Calendar.getInstance();
            mPrintSetDate = simpleDateFormat.format(calendarPrintSetDate.getTime());

            mPrintUserText = LocalStorage.getString(this, "AddItemUserText");// ???????????? ??????????????? ??????("")
            mPrintDateTitle = LocalStorage.getString(this, "BarcodePrinterDateTitle"); // ????????? ????????????(1:????????? ????????????,0:??????)

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

        // ???????????????(???) ?????????
        switch (barcodePrinterCheck) {
            case 0:
                //????????????
                barcodePrinterName = "????????????";
                break;
            case 1:
                //???????????????
                barcodePrinterName = "???????????????";
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

        // ???????????? ?????? ?????? ????????? ????????????
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        m3Mobile = pref.getBoolean("m3mobile", false);


        //????????? context, ????????? ?????????????????????(String)
        Log.i("????????????", m_officeCode + ".tips");
        dba = new DBAdapter(getApplicationContext(), m_officeCode + ".tips");


        m_registButton = (Button) findViewById(R.id.buttonProductRegist); // ????????????
        m_registButton.setText("??????");
        m_registButton.setTag("reg");


        Button renewButton = (Button) findViewById(R.id.buttonProductRenew); // ???????????????
        m_modfiyButton = (Button) findViewById(R.id.buttonProductModify); // ??????????????????
        m_recoveryButton = (Button) findViewById(R.id.buttonProductRecovery); // ????????????

        // ?????? ?????? ??????
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

        // ???????????? ?????? ??????
        m_modfiyButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                // ???????????? ??????
                Intent intent = new Intent(ManageProductActivityModfiy.this, ManageProductPriceDetailActivity.class);
                intent.putExtra("Shop_Area", "??????");
                intent.putExtra("OFFICE_CODE", m_officeCode);
                intent.putExtra("fillMaps", m_tempProduct);
                startActivity(intent);

            }
        });


        //----------------------------------------//
        //2023-01-04 ?????????????????? ?????? ??????
        //----------------------------------------//
        // ?????????????????? ????????????
        buttonPrinterSetting = (Button) findViewById(R.id.button_printer_setting);
        buttonPrinterSetting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                printerSettingPage();
            }
        });

        // ?????????????????? ????????????
        buttonPrinterOption = (Button) findViewById(R.id.button_printer_option);
        buttonPrinterOption.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showPronterOption();
            }
        });

        // ??? ?????? ??????
        buttonPrinterPrint = (Button) findViewById(R.id.button_printer_print);
        buttonPrinterPrint.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // ???????????? ?????? ??????
                barcodePrinterMain();
            }
        });

        //????????? ?????? ??????
        textViewPrinterState = (TextView) findViewById(R.id.textview_printer_state);
        textViewPrinterState.setText(tips.getSELECTPRINT_NAME());

        // ????????? ?????? ?????????
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
                        Toast.makeText(getApplicationContext(), "?????????????????? ????????? ??????????????????.", Toast.LENGTH_LONG).show();
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


        // ?????? ?????? ??????
        m_recoveryButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                updateFormView(m_tempProPurSell);
            }
        });

        // ?????????(????????????) ?????? ??????
        renewButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doClear();
            }
        });


        m_barcode = getIntent().getStringExtra("barcode"); // ??????????????????
        productReg_YN = getIntent().getStringExtra("productRegYN");
        mGubun = getIntent().getStringExtra("Gubun");        //????????????  -??????//??????
        mPreviousActivity = getIntent().getStringExtra("PreviousActivity");
        String officeCode = getIntent().getStringExtra("officeCode"); // ???????????????
        String officeName = getIntent().getStringExtra("officeName"); // ????????????
        Log.d("????????? ??????", officeCode + " " + officeName);

        m_textBarcode = (EditText) findViewById(R.id.editTextBarcode); // ?????????

        // ????????? 2020.09.14. ??????????????? ?????? ????????? ?????? ?????? ??????
        if (m_KeypadType == 0) { // ???????????????
            m_textBarcode.setInputType(2);
        }
        if (m_KeypadType == 1) { // ???????????????
            m_textBarcode.setInputType(145); //textVisiblePassword ) 0x00000091
        }

        m_textProductName = (EditText) findViewById(R.id.editTextProductName); // ?????????
        m_textCustomerCode = (EditText) findViewById(R.id.editTextCustomerCode); // ???????????????
        m_textCustomerName = (EditText) findViewById(R.id.editTextCustomerName); // ????????????

        m_buttonbarcode = (Button) findViewById(R.id.buttonBarcode); // ?????????????????????

        m_textStandard = (EditText) findViewById(R.id.editTextStandard); // ??????
        m_textAcquire = (EditText) findViewById(R.id.editTextAcquire); // ??????

        buttonAcquire = (Button) findViewById(R.id.buttonAcquire); // ????????????

        buttonAcquire.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????? ?????? ??????
                setAcquireData();
            }
        });

        m_textPurchasePrice = (EditText) findViewById(R.id.editTextPurchasePrice); // ?????????
        m_textPurchasePriceOriginal = (EditText) findViewById(R.id.editTextPurchasePriceOriginal); // ????????????
        m_textPurchasePriceOriginal.setEnabled(false);

        /*
         * 2014-12-18 ????????? ?????? ?????? ?????? ??????
         */
        m_checkboxPurPrivatGubun = (CheckBox) findViewById(R.id.checkbox_purpritaxgubun); // ?????????
        // ??????,??????
        // ??????
        m_checkboxPurPrivatGubun.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO VAT_CHK 0:????????? 1:??????
                if (isChecked) {
                    m_tempProduct.put("VAT_CHK", "1");
                } else {
                    m_tempProduct.put("VAT_CHK", "0");
                }
            }
        });

        m_textPurchasePriceOriginalTax = (EditText) findViewById(R.id.editTextPurchasePriceOriginalTax); // ?????????
        m_textPurchasePriceOriginalTax.setEnabled(false);
        m_textSalesPrice = (EditText) findViewById(R.id.editTextSalesPrice); // ?????????
        //2022-05-26 ?????????????????? ??????
        m_editTextSellPri1 = (EditText) findViewById(R.id.editTextSell_Pri1);

        m_textSalesPrice.setNextFocusDownId(R.id.editTextDifferentRatio); //???????????? ????????? ????????????
        m_textDifferentRatio = (EditText) findViewById(R.id.editTextDifferentRatio); // ?????????
        m_textDifferentRatio.setNextFocusDownId(R.id.editTextASell_Pri); //???????????? ????????? ????????????

        //2018-02-20 ???????????? ????????????
        m_textASell_Pri = (EditText) findViewById(R.id.editTextASell_Pri);
        m_textADifferentRatio = (EditText) findViewById(R.id.editTextADifferentRatio);
        m_textBSell_Pri = (EditText) findViewById(R.id.editTextBSell_Pri);
        m_textBDifferentRatio = (EditText) findViewById(R.id.editTextBDifferentRatio);
        m_textCSell_Pri = (EditText) findViewById(R.id.editTextCSell_Pri);
        m_textCDifferentRatio = (EditText) findViewById(R.id.editTextCDifferentRatio);


        //2018-02-20 ????????? ?????? ??????
        // ????????? ????????? -> ????????? + ???????????? ?????????
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


        //2019-02-01 ????????? ????????????
        m_textEventPurPri = (EditText) findViewById(R.id.editTextEventPurchasePrice);
        m_textEventSalePri = (EditText) findViewById(R.id.editTextEventSalesPrice);

        m_textStartEventDate = (EditText) findViewById(R.id.editTextEventStartDate);
        m_textEndEventDate = (EditText) findViewById(R.id.editTextEventEndDate);


        // ????????? ????????? -> ????????? + ???????????? ?????????
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


        // ????????? ????????? -> ????????? + ???????????? ?????????
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


        // ????????? ????????? -> ????????? + ???????????? ?????????
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


        // ????????? ????????? -> ????????? + ???????????? ?????????
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


        // ????????? ????????? -> ????????? + ???????????? ?????????
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

        m_textRealSto = (EditText) findViewById(R.id.editTextRealSto); // ??????
        m_textRealSto.setEnabled(false);

        m_buttonCustomerClassification1 = (Button) findViewById(R.id.buttonClassificationType1); // ??????
        m_buttonCustomerClassification2 = (Button) findViewById(R.id.buttonClassificationType2); // ??????
        m_buttonCustomerClassification3 = (Button) findViewById(R.id.buttonClassificationType3); // ??????

        m_spinTaxation = (Spinner) findViewById(R.id.spinnerTaxationType); // ?????????
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

        m_spinGoods = (Spinner) findViewById(R.id.spinnerGoodsUse); // ????????????
        m_spinGoods.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) { // ??????
                    m_tempProduct.put("Goods_Use", "1");
                }
                if (position == 1) { // ?????????
                    m_tempProduct.put("Goods_Use", "0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

		/*m_spinGroup = (Spinner) findViewById(R.id.spinnerGroupType); // ????????????
		m_spinGroup.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
				m_tempProduct.put("G_grade", position + "");
			}

			@Override
			public void onNothingSelected(AdapterView<?> parentView) {
			}
		});*/

        m_spinMemberPoint = (Spinner) findViewById(R.id.spinnerMemberPoint); // ????????????
        m_spinMemberPoint.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                m_tempProduct.put("Point_Use", position + "");
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });

        m_spinGoodsType = (Spinner) findViewById(R.id.spinnerGoodsTypeReg); // ????????????
        m_spinGoodsType.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                m_tempProduct.put("Scale_Use", position + "");
                if (position == 1) {
                    m_textPurchasePrice.setEnabled(false);
                    m_textSalesPrice.setEnabled(false);
                    //2022-05-26 ?????????????????? ??????
                    m_editTextSellPri1.setEnabled(false);

                    m_textDifferentRatio.setEnabled(false);
                    m_textPurchasePrice.setText("0");
                    m_textPurchasePriceOriginal.setText("0");
                    m_textPurchasePriceOriginalTax.setText("0");
                    m_textSalesPrice.setText("0");
                    //2022-05-26 ?????????????????? ??????
                    m_editTextSellPri1.setText("0");

                    m_textDifferentRatio.setText("0");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });


        //2020-06-15
        m_spinGoodsDanwi = (Spinner) findViewById(R.id.spinnerGoodsDanwi);   // ??????
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
        m_TextCon_Rate = (EditText) findViewById(R.id.editTextCon_Rate); // ?????????
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

        m_TextStd_Rate = (EditText) findViewById(R.id.editTextStd_Rate); // ????????????
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

        // ????????? ?????? ??? ????????? ??? ?????? ?????? ??????
        m_textBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String barcode = m_textBarcode.getText().toString();
                    m_tempProduct.put("BarCode", barcode);

                    if (!barcode.equals("")) // ????????? Barcode??? ?????? ?????? ?????????
                        doQueryWithBarcode();
                    if (barcode.length() == 4) {
                        m_textPurchasePrice.setEnabled(false);
                        m_textSalesPrice.setEnabled(false);
                        //2022-05-26 ?????????????????? ??????
                        m_editTextSellPri1.setEnabled(false);

                        m_textDifferentRatio.setEnabled(false);
                        m_textPurchasePrice.setText("0");
                        m_textPurchasePriceOriginal.setText("0");
                        m_textPurchasePriceOriginalTax.setText("0");
                        m_textSalesPrice.setText("0");
                        //2022-05-26 ?????????????????? ??????
                        m_editTextSellPri1.setText("0");

                        m_textDifferentRatio.setText("0");
                    }

                    setChangeBackGroundColor(0, m_textBarcode);
                } else {

                    // ????????? ????????? ????????????
                    setChangeBackGroundColor(1, m_textBarcode);

                }
            }
        });

        // ????????? ?????? ?????? ??? ????????? ??? ?????? ?????? ??????
        m_textCustomerCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {

                    setChangeBackGroundColor(0, m_textCustomerCode);

                    String customerCode = m_textCustomerCode.getText().toString();
                    m_tempProduct.put("Bus_Code", customerCode);

                    if (!customerCode.equals("")) // ????????? customerCode??? ?????? ?????? ?????????
                        fillBusNameFromBusCode(customerCode);
                } else {
                    setChangeBackGroundColor(1, m_textCustomerCode);
                }
            }
        });

        // ????????? ????????? ????????? ????????????
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

        // ???????????? ????????? ????????? ?????? ??????
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

        // ?????? ????????? ????????? ?????? ??????
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


        // ???????????? ????????? -> ????????? + ???????????? ?????????
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
         * f_purchasePriceOriginal)); // ???????????? m_tempProduct.put("Pur_Pri",
         * String.format("%.2f", f_purchasePrice)); // ????????? }
         *
         * if(!purchasePriceOriginal.equals("") && !Sell_Pri.equals("")) {
         *
         * float f_salesPrice = Float.parseFloat(Sell_Pri); float
         * f_purchasePriceOriginal = Float.parseFloat(purchasePriceOriginal);
         * float f_purchasePrice =
         * f_purchasePriceOriginal+f_purchasePriceOriginal/10; float f_ratio =
         * (f_salesPrice - f_purchasePrice) / f_salesPrice ;
         * m_tempProduct.put("Profit_Rate", String.format("%.2f", f_ratio*100));
         * // ????????? } updateFormView(m_tempProduct); } } });
         */

        // ????????? ????????? -> ????????? + ???????????? ?????????
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

        // ????????? ????????? -> ????????? + ???????????? ?????????
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

        //2022-05-26 ?????????????????? ??????
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


        // ????????? ????????? -> ????????? + ???????????? ?????????
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
                //?????????

                if ("".equals(m_textBarcode.getText().toString())) {
                    Toast.makeText(mContext, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
                    return;
                }

                m_tempProduct.put("Count", "1");
                //----------------------------------------//
                // 2021.01.08.?????????. ???????????? ??????
                //----------------------------------------//
                //m_tempProduct.put("Sell_Pri", m_textSalesPrice.getText().toString().replace(",", ""));
                m_tempProduct.put("Sell_Org", m_tempProduct.get("Sell_Pri1"));
                //----------------------------------------//

                mfillMapsBar.add(m_tempProduct);

                Toast.makeText(mContext, "?????? ??????", Toast.LENGTH_SHORT).show();
            }
        });

        m_barprintTran.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //?????????
                //DB??? ???????????????.

                if (mfillMapsBar.size() <= 0) {
                    Toast.makeText(mContext, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
                }

                dba.insert_BaPrintHistory(mfillMapsBar, "0");
                mfillMapsBar.removeAll(mfillMapsBar);
                Toast.makeText(mContext, "?????? ??????", Toast.LENGTH_SHORT).show();
            }
        });


        //?????? ?????????
        fetchLName();

        //?????? ?????? ?????????
        doClear();

        if (!m_barcode.isEmpty()) { // ???????????? ??????????????????

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


        // ?????????????????? ?????? ????????????
        getBarcodePrinterSet();

    }

    //?????? ?????? ??????
    public void setAcquireData() {

        String type = m_registButton.getTag().toString();

        if (!"modify".equals(type)) {
            //???????????? ????????????.
            Toast.makeText(this, "????????? ????????? ???????????? ????????? ???????????????.", Toast.LENGTH_SHORT).show();
            return;
        }

        //?????? ????????? ????????? ?????? ?????????.
        //???????????? ?????? ?????????.
        String barcode = m_textBarcode.getText().toString();
        String obtain = m_textAcquire.getText().toString();

        if ("".equals(obtain) || obtain == null) {
            Toast.makeText(mContext, "????????? ????????? ?????????!", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, ManagerProductBox.class);
        intent.putExtra("barcode", barcode);
        intent.putExtra("obtain", obtain);
        startActivityForResult(intent, CUSTOMER_BOXPRODUCT_REQUEST);

    }

    /*
     * // ?????????????????? ????????? private void calculateSellPriByProfitRate () { String ratio
     * = m_textDifferentRatio.getText().toString(); String Pur_Pri =
     * m_textPurchasePrice.getText().toString();
     *
     * if( !ratio.equals("") && !Pur_Pri.equals("") ){ float f_ratio =
     * Float.valueOf(ratio).floatValue()/100; float f_purchasePrice =
     * Float.parseFloat(Pur_Pri); float f_salesPrice = f_purchasePrice/(1 -
     * f_ratio);
     *
     * // ????????? ?????? int rest = ((int)(f_salesPrice /10.0f))*10; int one =
     * (int)f_salesPrice - rest; if (one>=5) rest+=10;
     *
     * //????????? ???????????? ????????? ????????? ?????? f_ratio = (rest - f_purchasePrice) / rest;
     *
     * m_tempProduct.put("Profit_Rate", String.format("%.2f", f_ratio*100)); //
     * ????????? m_tempProduct.put("Sell_Pri", String.format("%d", rest)); // ?????????
     *
     * updateFormView(m_tempProduct); } }
     */

    // ?????? ??? ?????????
    public void updateFormView(HashMap<String, String> object) {

        m_textBarcode.setText(object.get("BarCode")); // ?????????
        m_textProductName.setText(object.get("G_Name")); // ?????????
        m_textCustomerCode.setText(object.get("Bus_Code")); // ???????????????
        m_textCustomerName.setText(object.get("Bus_Name")); // ????????????
        m_textStandard.setText(object.get("Std_Size")); // ??????
        try {
            if (object.get("Obtain").equals("") || object.get("Obtain") == null) {
                m_textAcquire.setText("0"); // ??????
            } else {
                m_textAcquire.setText(StringFormat.convertToNumberFormat(object.get("Obtain"))); // ??????
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        m_textPurchasePrice.setText(object.get("Pur_Pri")); // ?????????
        m_textSalesPrice.setText(StringFormat.convertToNumberFormat(object.get("Sell_Pri"))); // ?????????
        //2022-05-26 ?????????????????? ??????
        m_editTextSellPri1.setText(StringFormat.convertToNumberFormat(object.get("Sell_Pri1"))); // ??????????????????

        m_textPurchasePriceOriginal.setText(object.get("Pur_Cost")); // ????????????
        m_textPurchasePriceOriginalTax.setText(object.get("Add_Tax")); // ?????????

        /*
         * 2014-12-18 ????????? ??????, ?????? ?????? ??????
         *
         */

        if (object.get("VAT_CHK").equals("1"))
            m_checkboxPurPrivatGubun.setChecked(true);
        else
            m_checkboxPurPrivatGubun.setChecked(false);

        m_textDifferentRatio.setText(object.get("Profit_Rate")); // ?????????
        m_textRealSto.setText(object.get("Real_Sto")); // ??????
        m_buttonCustomerClassification1.setText(object.get("L_Name")); // ??????
        if (!object.get("L_Code").equals("")) {
            m_tempProduct.put("L_Code", object.get("L_Code"));
        }
        m_buttonCustomerClassification2.setText(object.get("M_Name")); // ??????
        if (!object.get("M_Code").equals("")) {
            m_tempProduct.put("M_Code", object.get("M_Code"));
        }
        m_buttonCustomerClassification3.setText(object.get("S_Name")); // ??????
        if (!object.get("S_Code").equals("")) {
            m_tempProduct.put("S_Code", object.get("S_Code"));
        }

        if (object.get("Tax_YN").equals("0")) // ?????????
            m_spinTaxation.setSelection(0);
        else
            m_spinTaxation.setSelection(1);

        /*
         * if(object.get("VAT_CHK").equals("0"))
         * m_checkSurtax.setChecked(false); else m_checkSurtax.setChecked(true);
         */

        if (object.get("Goods_Use").equals("1")) // ????????????
            m_spinGoods.setSelection(0);
        else
            m_spinGoods.setSelection(1);

		/*if (object.containsKey("G_grade")) { // ????????????
			int a = 1; // A??????
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

        if (object.get("Point_Use").equals("0")) // ?????????????????????
            m_spinMemberPoint.setSelection(0);
        else
            m_spinMemberPoint.setSelection(1);

        if (object.get("Scale_Use").equals("0")) // ????????????
            m_spinGoodsType.setSelection(0); // ?????????
        else
            m_spinGoodsType.setSelection(1); // ????????????

        //2020-06-15 ?????? ????????? ????????????
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
            case "???":
                iUnit = 4;
                break;
            case "M":
                iUnit = 5;
                break;
            case "???":
                iUnit = 6;
                break;
            case "???":
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
            //2022-05-26 ?????????????????? ??????
            //m_editTextSellPri1.setEnabled(false);
            m_textDifferentRatio.setEnabled(false);
        }

        //2018-2-20 ???????????? ??????
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

        //????????? ????????????
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
				this.setTitle("?????????????????? (?????????)");
			}

		} catch (ParseException e) {
			e.printStackTrace();
		}*/


    }

    // ????????? ?????????
    public void doClear() {

        workIndex = 0;

        newTempProduct();
        updateFormView(m_tempProduct);
        productNewRegisterYN("Y");
        this.setTitle("????????????");
        m_textBarcode.requestFocus();
    }

    // ?????? ????????? ?????????
    public void newTempProduct() {
        m_tempProduct = new HashMap<String, String>();
        m_tempProduct.put("BarCode", ""); // 1.?????????
        m_tempProduct.put("G_Name", ""); // 2.?????????
        m_tempProduct.put("Bus_Code", ""); // 3.???????????????
        m_tempProduct.put("Bus_Name", ""); // 4.????????????
        m_tempProduct.put("Std_Size", ""); // 5.??????
        m_tempProduct.put("Obtain", "0"); // 6. ??????
        m_tempProduct.put("Pur_Pri", "0"); // 7. ?????????
        m_tempProduct.put("Sell_Pri", "0"); // 8.?????????

        //2022-05-26 ?????????????????? ??????
        m_tempProduct.put("Sell_Pri1", "0"); // ??????????????????

        m_tempProduct.put("Pur_Cost", "0"); // 9.????????????
        m_tempProduct.put("Add_Tax", "0"); // 10.?????????
        m_tempProduct.put("VAT_CHK", "1"); // ????????? ?????? ??????
        m_tempProduct.put("Profit_Rate", "0"); // 11. ?????????
        m_tempProduct.put("L_Code", "AA"); // 12.?????????
        m_tempProduct.put("L_Name", "???????????????"); // 13.??????
        m_tempProduct.put("M_Code", "AA"); // 14.?????????
        m_tempProduct.put("M_Name", "???????????????"); // 15.??????
        m_tempProduct.put("S_Code", "AAA"); // 16.?????????
        m_tempProduct.put("S_Name", "???????????????"); // 17.??????
        m_tempProduct.put("Tax_YN", "1"); // 18. ?????????
        m_tempProduct.put("VAT_CHK", "1"); // 19. ???????????????
        m_tempProduct.put("Goods_Use", "1"); // 20. ????????????
        m_tempProduct.put("Point_Use", "1"); // 21. ??????????????????
        m_tempProduct.put("Edit_Check", "1"); // 22.??????????????????
        m_tempProduct.put("Tran_Chk", "1"); // 23.????????????????????????
        m_tempProduct.put("Scale_Use", "0"); // 24.????????????
        m_tempProduct.put("G_grade", "0"); // 25.????????????
        m_tempProduct.put("Edit_Date", ""); // 26.????????????
        m_tempProduct.put("Editor", ""); // 27.?????????
        m_tempProduct.put("Write_Date", ""); // 28.?????????
        m_tempProduct.put("Writer", ""); // 29.?????????

        m_tempProduct.put("ASell_Pri", ""); //30. ?????? A??????
        m_tempProduct.put("Profit_Rate_APir", ""); //31. ?????? A?????????
        m_tempProduct.put("BSell_Pri", ""); //32. ?????? A??????
        m_tempProduct.put("Profit_Rate_BPir", ""); //33. ?????? A?????????
        m_tempProduct.put("CSell_Pri", ""); //34. ?????? A??????
        m_tempProduct.put("Profit_Rate_CPir", ""); //35. ?????? A?????????

        m_tempProduct.put("Sale_Pur", "");    //36.???????????????
        m_tempProduct.put("Sale_Sell", "");    //37.???????????????
        m_tempProduct.put("Sale_SDate", "");    //38.???????????????
        m_tempProduct.put("Sale_EDate", "");    //39.???????????????

        //2020-06-15;
        m_tempProduct.put("Unit", "kg");
        m_tempProduct.put("Con_Rate", "10");
        m_tempProduct.put("Std_Rate", "100");
        m_tempProduct.put("Bot_Sell", "0");

        m_tempProPurSell.clear();


    }

    public void historyProductData(HashMap<String, String> object) {
        // ????????? ????????? ?????? ????????? ???????????? ?????????????????? ????????????
        m_tempProPurSell.put("Pur_Pri_old", object.get("Pur_Pri"));
        m_tempProPurSell.put("Sell_Pri_old", object.get("Sell_Pri"));
        m_tempProPurSell.putAll(object);
    }

    // ????????? ?????????
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
        String barcode = m_textBarcode.getText().toString(); // 1.?????????
        String g_name = m_textProductName.getText().toString(); // 2.?????????
        String bus_code = m_textCustomerCode.getText().toString(); // 3.???????????????
        String bus_name = m_textCustomerName.getText().toString(); // 4.????????????

        /*
         * String lname = m_buttonCustomerClassification1.getText().toString();
         * //5.?????? String mname =
         * m_buttonCustomerClassification2.getText().toString(); //6.?????? String
         * sname = m_buttonCustomerClassification3.getText().toString(); //7.??????
         * String lcode = getCodeFromListByName(m_Ltype, lname); //????????? String
         * mcode = getCodeFromListByName(m_Mtype, mname); //????????? String scode =
         * getCodeFromListByName(m_Stype, sname); //?????????
         */

        String lname = m_tempProduct.get("L_Name"); // 5-1.?????????
        String mname = m_tempProduct.get("M_Name"); // 5-2.?????????
        String sname = m_tempProduct.get("S_Name"); // 5-3.?????????
        String lcode = m_tempProduct.get("L_Code");// 5.?????????
        String mcode = m_tempProduct.get("M_Code");// 5.?????????
        String scode = m_tempProduct.get("S_Code");// 5.?????????

        String tax_yn = m_spinTaxation.getSelectedItem().toString(); // 8.?????????
        String Std_Size = m_textStandard.getText().toString(); // 9.??????
        // String Obtain = m_textAcquire.getText().toString(); //??????
        String pur_pri = m_textPurchasePrice.getText().toString(); // 10.?????????
        String pur_cost = m_textPurchasePriceOriginal.getText().toString(); // 11.????????????
        String add_tax = m_textPurchasePriceOriginalTax.getText().toString(); // 12.?????????
        String salesPrice = m_textSalesPrice.getText().toString().replace(",", ""); // 13.?????????
        //2022-05-26 ?????????????????? ??????
        String salesPrice1 = m_editTextSellPri1.getText().toString().replace(",", ""); // ??????????????????

        String ratio = m_textDifferentRatio.getText().toString(); // 14.?????????
        String vat_chk = null;// ????????? ??????, ?????? ??????
        if (m_checkboxPurPrivatGubun.isChecked())
            vat_chk = "1";
        else
            vat_chk = "0";

        //2018-02-20 ???????????? ?????? ??????
        String aSell_Pri = m_textASell_Pri.getText().toString();
        String aProfit_Rate = m_textADifferentRatio.getText().toString();
        String bSell_Pri = m_textBSell_Pri.getText().toString();
        String bProfit_Rate = m_textBDifferentRatio.getText().toString();
        String cSell_Pri = m_textCSell_Pri.getText().toString();
        String cProfit_Rate = m_textCDifferentRatio.getText().toString();


        //String g_grade = String.valueOf(m_spinGroup.getSelectedItem().toString()); // 15.????????????
        // String good_use = String.format("%d", m_spinGoods.getSelectedItem()
        // ); //16.????????????
        String good_type = String.valueOf(m_spinGoodsType.getSelectedItem().toString()); // 17.????????????
        String memberpoint = String.valueOf(m_spinMemberPoint.getSelectedItem().toString()); // 17.????????????

        //2020-06-15 ?????? ????????? ????????????
        String g_Unit = String.valueOf(m_spinGoodsDanwi.getSelectedItem().toString()); // 17.????????????
        String g_Con_Rate = String.valueOf(m_TextCon_Rate.getText().toString());
        String g_Std_Rate = String.valueOf(m_TextStd_Rate.getText().toString());

        if (g_Con_Rate.equals("")) {
            g_Con_Rate = "10";
        }

        if (g_Std_Rate.equals("")) {
            g_Std_Rate = "100";
        }


        if (pur_pri.equals("")) { // ????????? ????????????
            pur_pri = "0";
            pur_cost = "0";
            add_tax = "0";
        }
        // String Add_Tax = String.format("%f",
        // Float.parseFloat(purchasePrice)-Float.parseFloat(purchasePriceOriginal));
        /*
         * if(m_checkSurtax.isChecked()) surtax = "1"; else surtax = "0";
         */

        if (tax_yn.equals("??????"))
            tax_yn = "0";
        else
            tax_yn = "1";

        // if(Obtain.equals("")) Obtain = "0";

        if (good_type.equals("?????????")) {
            good_type = "0";
        } else {
            good_type = "1";
        }

        if (memberpoint.equals("??????")) {
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
            Toast.makeText(getApplicationContext(), "???????????? ????????? ????????????", Toast.LENGTH_SHORT).show();
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

        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();

        // ??????????????? ?????? ??????
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                if (results.length() > 0) {

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ManageProductActivityModfiy.this, AlertDialog.THEME_HOLO_LIGHT);
                    alertDialog.setMessage("??????????????? ?????????????????????..");
                    alertDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            doClear();
                        }
                    });
                    alertDialog.show();
                } else {
                    Toast.makeText(getApplicationContext(), "????????? ????????????", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
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
        // ????????? ?????? ?????????
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
        String barcode = m_textBarcode.getText().toString(); // 1.?????????
        String g_name = m_textProductName.getText().toString(); // 2.?????????
        String bus_code = m_textCustomerCode.getText().toString();// 3.???????????????
        String bus_name = m_textCustomerName.getText().toString();// 4.????????????

        String lname = m_buttonCustomerClassification1.getText().toString(); // 5-1.?????????
        String mname = m_buttonCustomerClassification2.getText().toString(); // 5-2.?????????
        String sname = m_buttonCustomerClassification3.getText().toString(); // 5-3.?????????
        String lcode = m_tempProduct.get("L_Code");// 5.?????????
        String mcode = m_tempProduct.get("M_Code");// 5.?????????
        String scode = m_tempProduct.get("S_Code");// 5.?????????

        String Tax_YN = m_spinTaxation.getSelectedItem().toString(); // 6.?????????

        String Std_Size = m_textStandard.getText().toString();// 9.??????
        //String Obtain = m_textAcquire.getText().toString();// 10.??????

        //----------------------------------------//
        // ????????? 2020.10.26. ???????????? ?????? ????????? ?????? ????????? ????????? ??????(??????,?????? ?????? ??????)
        //----------------------------------------//
        if (workIndex == 1) {
            m_tempProduct.put("Pur_Pri", m_textPurchasePrice.getText().toString());
            updateFormView(ccp.calculateSellPriByPurprice(m_tempProduct));
        }
        //----------------------------------------//

        String pur_pri = m_textPurchasePrice.getText().toString();// 11.?????????
        String pur_cost = m_textPurchasePriceOriginal.getText().toString();// 12.????????????
        String Add_Tax = m_textPurchasePriceOriginalTax.getText().toString();// 13.?????????

        String Vat_Chk = null;
        if (m_checkboxPurPrivatGubun.isChecked())
            Vat_Chk = "1";
        else
            Vat_Chk = "0";

        //----------------------------------------//
        // ????????? 2020.10.26. ???????????? ?????? ????????? ?????? ????????? ????????? ??????(??????,?????? ?????? ??????)
        //----------------------------------------//
        if (workIndex == 2) {
            m_tempProduct.put("Sell_Pri", m_textSalesPrice.getText().toString().replace(",", ""));
            updateFormView(ccp.calculateSellPriBySellPrice(m_tempProduct));
        }
        //----------------------------------------//

        String salesPrice = m_textSalesPrice.getText().toString().replace(",", "");// 14.?????????
        //2022-05-26 ?????????????????? ??????
        String salesPrice1 = m_editTextSellPri1.getText().toString().replace(",", "");// ??????????????????

        String ratio = m_textDifferentRatio.getText().toString();// 15.?????????

        String aSell_Pri = m_textASell_Pri.getText().toString();
        String aProfit_Rate = m_textADifferentRatio.getText().toString();
        String bSell_Pri = m_textBSell_Pri.getText().toString();
        String bProfit_Rate = m_textBDifferentRatio.getText().toString();
        String cSell_Pri = m_textCSell_Pri.getText().toString();
        String cProfit_Rate = m_textCDifferentRatio.getText().toString();

        String goodsType = String.valueOf(m_spinGoodsType.getSelectedItemPosition()); // 18.????????????
        String memberpoint = String.valueOf(m_spinMemberPoint.getSelectedItemPosition()); // ????????????
        //String g_grade = m_spinGroup.getSelectedItem().toString(); // ????????????
        String good_use = String.valueOf(m_spinGoods.getSelectedItemPosition()); // ????????????


        //2020-06-15 ?????? ????????? ????????????
        String g_Unit = String.valueOf(m_spinGoodsDanwi.getSelectedItem().toString()); // 17.????????????
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

        if (Tax_YN.equals("??????"))
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
            Toast.makeText(getApplicationContext(), "???????????? ????????? ????????????", Toast.LENGTH_SHORT).show();
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

        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();

        // ??????????????? ?????? ??????
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                if (results.length() > 0) {
                    // deleteListViewAll();
                    // doSearch();

                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(ManageProductActivityModfiy.this, AlertDialog.THEME_HOLO_LIGHT);
                    alertDialog.setMessage("??????????????? ?????????????????????..");
                    alertDialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            doClear();
                        }
                    });
                    alertDialog.show();

                } else {
                    Toast.makeText(getApplicationContext(), "????????? ????????????", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
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
     * // ?????? ??????????????? dialog = new ProgressDialog(this);
     * dialog.setMessage("Loading...."); dialog.show();
     *
     * // ??????????????? ?????? ?????? new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {
     *
     * @Override public void onRequestCompleted(JSONArray results) {
     * dialog.dismiss(); dialog.cancel(); if (results.length() > 0) {
     * Toast.makeText(getApplicationContext(), "?????? ??????",
     * Toast.LENGTH_SHORT).show(); //updateListView(results); } else {
     * Toast.makeText(getApplicationContext(), "????????? ????????????",
     * Toast.LENGTH_SHORT).show(); } }
     *
     * @Override public void onRequestFailed(int code, String msg) {
     * dialog.dismiss(); dialog.cancel();
     * Toast.makeText(getApplicationContext(), "?????? ??????",
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

    // ???????????? ?????????
    private void fetchLName() {
        String query = "";
        //2019-07-16
        //query = "SELECT L_Name, L_Code FROM Goods GROUP BY L_Name, L_Code;";
        query = "SELECT L_Name, L_Code FROM L_Branch;";

        // query = "Select L_Code, L_Name From L_Branch Where L_Code <> 'AA'
        // Order By L_Code";

        // ?????? ???????????????
        /*
         * dialog = new ProgressDialog(this); dialog.setMessage("Loading....");
         * dialog.setCancelable(false); dialog.show();
         */

        // ??????????????? ?????? ??????
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
                    Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
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
        builder.setTitle("???????????????");
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
            Toast.makeText(getApplicationContext(), "?????? ???????????? ?????? ???????????????", Toast.LENGTH_SHORT).show();
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
        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // ??????????????? ?????? ??????
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
                    builder.setTitle("???????????????");
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
                    Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    public void onClassification3(View view) {
        String lname = m_buttonCustomerClassification1.getText().toString();
        String mname = m_buttonCustomerClassification2.getText().toString();
        if (lname.equals("") || mname.equals("")) {
            Toast.makeText(getApplicationContext(), "?????? ?????????,???????????? ?????? ???????????????", Toast.LENGTH_SHORT).show();
            return;
        }

        String query = "";
        //2019-07-15
        //query = "SELECT S_Name, S_Code FROM Goods WHERE L_Name='" + lname + "' AND M_Name='" + mname + "' GROUP BY S_Name, S_Code;";
        query = "SELECT S_Name, S_Code FROM S_Branch WHERE L_Name='" + lname + "' AND M_Name='" + mname + "';";

        // query = "Select S_Code, S_Name From S_Branch Where L_Name =
        // '"+lname+"' AND M_Name = '"+mname+"' AND S_Code <> 'AAA' Order By
        // S_Code";
        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // ??????????????? ?????? ??????
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();

                if (results.length() > 0) {
                    m_Stype.removeAll(m_Stype);

                    ArrayList<String> lSpList = new ArrayList<String>();
                    for (int index = 0; index < results.length(); index++) {
                        //2019-07-16 ??????
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
                    builder.setTitle("???????????????");
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
                    Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    // ????????? ????????? ???????????? ?????? ??????
    private void fillBusNameFromBusCode(String customerCode) {
        // ?????? ???????????????
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
                    Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
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
            // ????????? ?????? ?????? ????????? ????????? ?????? ??????
            final String[] option = new String[]{"??????", "?????????"};
            //ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.user_select_dialog_item, option);
            AlertDialog.Builder builder = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
            builder.setTitle("Select Option");

            // ?????? ????????? ????????? ??????
            builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {

                    if (which == 0) { // ???????????? ????????? ??????
                        startProductList();
                    } else { // ????????? ??????
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
            // ????????? ????????? ?????? ????????? ??????
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
            // ?????? ????????? ?????? ????????? ??????
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

    // SQL QUERY ??????
    public void doQueryWithBarcode() {

        String query = "";
        String barcode = m_textBarcode.getText().toString();
        query = "SELECT * FROM Goods WHERE Barcode = '" + barcode + "';";

        if (barcode.equals(""))
            return;


        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // ??????????????? ?????? ??????
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
                    //?????? ????????? ????????? ????????? ???????????? ??? ?????? ?????????.
                    if (!"".equals(m_textProductName.getText().toString())) {
                        String barcode = m_textBarcode.getText().toString();
                        doClear();
                        m_textBarcode.setText(barcode);
                        m_textProductName.requestFocus();
                    } else {
                        Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    private void productNewRegisterYN(String proyn) {
        if ((proyn == "Y" || proyn == "y")) { // ???????????????
            m_textBarcode.setEnabled(true);
            m_buttonbarcode.setEnabled(true);
            m_registButton.setText("??????");
            m_registButton.setTag("reg");
            m_recoveryButton.setEnabled(false);
            // ???????????? ????????? ?????? ???????????? ????????? ????????????
            m_textPurchasePrice.setEnabled(true);
            m_textSalesPrice.setEnabled(true);
            //2022-05-26 ?????????????????? ??????
            m_editTextSellPri1.setEnabled(true);

            m_textDifferentRatio.setEnabled(true);

            buttonAcquire.setEnabled(false);

        } else { // ??????????????????
            m_textBarcode.setEnabled(false);
            m_buttonbarcode.setEnabled(false);
            m_registButton.setText("??????");
            m_registButton.setTag("modify");
            m_recoveryButton.setEnabled(true);

            buttonAcquire.setEnabled(true);
        }
    }

    // ????????? ????????? ??????????????? ??? ?????? ??????

    /**
     * ????????? ???????????? ????????? ?????? ?????????.
     *
     * @param gubun 0 ?????? ?????????, 1 ????????? ?????????
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


    //?????? ????????? ??????
    public void setSelfBarcode(View view) {

        //DB?????? ???????????? ???????????? ?????? ?????????.
        String query = "";
        //query = "Select Max(LEFT(BarCode,12)) As selfBarcode From Goods a, Pos_Set b Where Left(BarCode, 2)  = left([106], 2) AND LEN(Barcode)=13";
        // 2023.02.28. ????????? ???????????? ?????? ????????? ?????? ????????????
        if (isSelfBarcodeUse == true && selfBarcodePrefixCode.length() == 2) {
            query = "Select Max(LEFT(BarCode,12)) As selfBarcode From Goods Where Left(BarCode, 2)  = '" + selfBarcodePrefixCode + "' AND LEN(Barcode)=13";
        } else {
            query = "Select Max(LEFT(BarCode,12)) As selfBarcode From Goods a, Pos_Set b Where Left(BarCode, 2)  = left([106], 2) AND LEN(Barcode)=13";
        }

        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // ??????????????? ?????? ??????
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
                            Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        setSelfBarcode(selfBar);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //?????? ????????? ????????? ????????? ???????????? ??? ?????? ?????????.
                    if (!"".equals(m_textProductName.getText().toString())) {
                        String barcode = m_textBarcode.getText().toString();
                        doClear();
                        m_textBarcode.setText(barcode);
                        m_textProductName.requestFocus();
                    } else {
                        Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

    }

    private void setSelfBarcode(String str) {


        //?????? ???????????? 1??? ????????????.
        long bar = Long.parseLong(str) + 1;
        Log.d("????????????", String.valueOf(bar));

        //??????????????? ?????? ?????????.
        String barcode = String.valueOf(bar);

        if (!str.substring(0, 2).equals(barcode.substring(0, 2))) {
            Toast.makeText(mContext, "??????????????? ?????????????????? ?????? ?????????????????????.", Toast.LENGTH_SHORT).show();
            return;
        }
        //???????????? ??????????????? ??????
        String chk_bar = barcode.substring(0, 2);
        int[] digits = new int[barcode.length()];
        int i_even = 0;
        int i_odd = 0;

        //1?????? ????????? ???????????????.
        //2?????? ?????? ????????? ????????????.
        //3?????? ???????????? ????????? 3??? ??????.
        //4?????? ?????? ????????? ??? ????????????.

        for (int i = 0; i < barcode.length(); ++i) {
            digits[i] = Integer.parseInt(barcode.substring(i, i + 1));
            if ((i + 1) % 2 == 0) {
                i_even += digits[i];
            } else {
                i_odd += digits[i];
            }
        }

        //5?????? 3????????? 4????????? ????????????.
        int total = i_even + i_odd;

        //6??????
        String sub_str = String.valueOf(total).substring(String.valueOf(total).length() - 1, String.valueOf(total).length());

        int result = 0;
        if (!sub_str.equals("0")) {
            result = 10 - Integer.parseInt(sub_str);
        }

        barcode = barcode + "" + result;
        m_textBarcode.setText(barcode);

        // ??????????????? ????????? ????????????
        ///???
        m_textProductName.setInputType(145); //
        //m_textProductName.setPrivateImeOptions("defaultInputmode=korean;");
        m_textProductName.requestFocus();

        upu.hideSoftKeyboard(false);

    }

    /**************	????????? ??????   **************************************************************************/

    //2017-04 m3mobile ??????
    @Override
    protected void onResume() {
        super.onResume();
        if (m3Mobile) {
            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textBarcode, m_textProductName);
            M3MoblieBarcodeScanBroadcast.setOnResume();
        }
        tips.setReflash();
    }

    //2017-04 m3mobile ??????
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m3Mobile) {
            M3MoblieBarcodeScanBroadcast.setOnDestory();
        }
        //????????? spp-l3000
        if (barcodePrinterCheck == 2 && mBixolonLabelPrinter.isConnected()) {
            mBixolonLabelPrinter.disconnect();
        }

        String aaa = blueConnect.toString();

        //???????????? lkp30
        if (barcodePrinterCheck == 3 && bluetoothPort.isConnected()) {
            if (!mPreviousActivity.equals("BarcodePrinter")) {
                blueConnect.close();
            }
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
            actionbar.setTitle("????????????");

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

    // 2021.01.05.?????????. ?????????????????? ????????????
    private void getBarcodePrinterSet() {

        String query = "";

        query += " SELECT * FROM BarcodePrinter_Sett";

        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();

        // ??????????????? ?????? ??????
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
                Toast.makeText(getApplicationContext(), "?????? ??????", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    private void setSpinner(JSONArray results) {

        ArrayList<String> spinner_list = new ArrayList<String>();
        String temp = "";
        temp = barcodePrinterName;
        if (barcodePrinterName.equals("???????????????")) {
            temp += "|" + barcodePrinterAddress;
            temp += "|" + barcodePrinterPort;
        }

        // ???????????? ?????? ????????? ??????
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
            Toast.makeText(getApplicationContext(), "????????? ????????????", Toast.LENGTH_SHORT).show();
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinner_list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSelectPrinter.setAdapter(adapter);

        //2023.02.09. ???????????? ????????? ?????????????????? ????????? ??????
        int SelectBarcodePrinter = 0; //???????????????????????? ?????????
        try {
            SelectBarcodePrinter = LocalStorage.getInt(mContext, "SelectBarcodePrinter");
        } catch (RuntimeException e) {
            LocalStorage.setInt(mContext, "SelectBarcodePrinter", 0);
        }
        int spinnerCnt = spinnerSelectPrinter.getAdapter().getCount();
        if (spinnerCnt > SelectBarcodePrinter)
            spinnerSelectPrinter.setSelection(SelectBarcodePrinter);

    }

    //????????? ?????? ??????
    private void printerSettingPage() {

        switch (barcodePrinterCheck) {
            case 0:
                //????????????
                break;
            case 1:
                //???????????????
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

        final CharSequence[] items = {"?????????", "???????????????", "????????????"};
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ManageProductActivityModfiy.this, AlertDialog.THEME_HOLO_LIGHT);

        // ????????????
        alertDialogBuilder.setTitle("????????? ????????? ?????????");
        alertDialogBuilder.setItems(items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // ??????????????? ????????????
                        switch (items[id].toString()) {
                            case "?????????":
                                Intent intent = new Intent(mContext, BarcodePrintLKP30II.class);
                                startActivity(intent);
                                break;
                            case "???????????????":
                                if (!BluetoothPort.getInstance().isConnected()) {

                                    //--------------------//
                                    // ????????? ?????????
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
                                        Toast.makeText(mContext, "???????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                break;
                            case "????????????":
                                if (BluetoothPort.getInstance().isConnected()) {
                                    try {
                                        BluetoothPort.getInstance().disconnect();
                                        textViewPrinterState.setText("LK-P30N(????????????)");
                                    } catch (IOException | InterruptedException e) {
                                        e.printStackTrace();
                                        Toast.makeText(mContext, "???????????? ???????????? ??????", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(mContext, "???????????? ?????????????????? ????????????.", Toast.LENGTH_SHORT).show();
                                    textViewPrinterState.setText("LK-P30N(????????????)");
                                }
                                break;
                        }
                        dialog.dismiss();
                    }
                });

        // ??????????????? ??????
        AlertDialog alertDialog = alertDialogBuilder.create();

        // ??????????????? ????????????
        alertDialog.show();

    }

    private void startBluetoothPrintSearch() {
        //Intent intent = new Intent(mContext, BluetoothConnectMenu.class);
        //startActivity(intent);
        blueConnect.setStart(textViewPrinterState);
    }

    //???????????? ?????????
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
            textViewPrinterState.setText("LK-P30N(????????????)");
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
                    buffer.append("?????? ??????.\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_COVER_OPEN) == BixolonLabelPrinter.STATUS_1ST_BYTE_COVER_OPEN) {
                    buffer.append("?????? ??????.\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_CUTTER_JAMMED) == BixolonLabelPrinter.STATUS_1ST_BYTE_CUTTER_JAMMED) {
                    buffer.append("?????? ??????.\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_TPH_OVERHEAT) == BixolonLabelPrinter.STATUS_1ST_BYTE_TPH_OVERHEAT) {
                    buffer.append("TPH(thermal head) overheat.\n");
                }
                if ((report[0] & BixolonLabelPrinter.STATUS_1ST_BYTE_AUTO_SENSING_FAILURE) == BixolonLabelPrinter.STATUS_1ST_BYTE_AUTO_SENSING_FAILURE) {
                    buffer.append("?????? ?????? ??????. (??????????????????)\n");
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
                    buffer.append("??????");
                    //????????? ????????? ???????????? ???????????? ???????????????.
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
                            textViewPrinterState.setText("?????????(" + mConnectedDeviceName + ")");
                            break;

                        case BixolonLabelPrinter.STATE_CONNECTING:
                            textViewPrinterState.setText("?????????...");
                            break;

                        case BixolonLabelPrinter.STATE_NONE:
                            textViewPrinterState.setText("????????????");
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

    // ?????? ?????? ????????? ?????? ???????????? ????????? ??????
    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

        Toast.makeText(getApplicationContext(), "?????? DateSet ?????????...", Toast.LENGTH_SHORT).show();

        SimpleDateFormat simpleDateFormat;
        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        calendarPrintSetDate.set(year, monthOfYear, dayOfMonth);
        buttonPrintSetDate.setText(simpleDateFormat.format(calendarPrintSetDate.getTime()));
        mPrintSetDate = buttonPrintSetDate.getText().toString();

    }

    // ????????? ?????? ????????????????????? ??????
    private void showPronterOption() {

        LayoutInflater inflater = getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.dialog_printer_option, null);

        /*----------------------------------------*/
        // ???????????? ??????????????? ????????????
        /*----------------------------------------*/

        //?????????
        checkboxSaleYn = (CheckBox) dialogView.findViewById(R.id.checkbox_sale_yn);

        if (mSaleYn.equals("1")) {
            checkboxSaleYn.setChecked(true);
        } else {
            checkboxSaleYn.setChecked(false);
        }

        // ??????????????????
        checkboxSellPri1 = (CheckBox) dialogView.findViewById(R.id.checkbox_sell_pri1);  // ?????????????????? ??????
        if (mSellPri1.equals("1")) {
            checkboxSellPri1.setChecked(true);
        } else {
            checkboxSellPri1.setChecked(false);
        }

        // ??????(?????????,?????????,?????????) ?????????
        spinnerSelectBranchName = (Spinner) dialogView.findViewById(R.id.spinner_select_branch_name);
        //mSelectBranchName = 1;//??????
        spinnerSelectBranchName.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String tempData = ((Spinner) dialogView.findViewById(R.id.spinner_select_branch_name)).getSelectedItem().toString();
                if (tempData.equals("?????????")) {
                    mSelectBranchName = 1;
                } else if (tempData.equals("?????????")) {
                    mSelectBranchName = 2;
                } else if (tempData.equals("?????????")) {
                    mSelectBranchName = 3;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        // ???????????? ?????????
        spinnerSelectAddItem = (Spinner) dialogView.findViewById(R.id.spinner_select_add_item);

        spinnerSelectAddItem.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String tempData = ((Spinner) dialogView.findViewById(R.id.spinner_select_add_item)).getSelectedItem().toString();
                if (tempData.equals("?????????")) {
                    buttonPrintSetDate.setVisibility(View.VISIBLE); //0:visible,1:invisible,2:gone
                    edittextPrintUserText.setVisibility(View.GONE);
                    mPrintUserText = "";
                    mSelectAddItem = 1;
                } else {
                    // ????????? ?????? ??????????????? ?????????
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

        // ???????????? ????????? ?????? ??????
        edittextPrintUserText = (EditText) dialogView.findViewById(R.id.edittext_print_user_text);
        edittextPrintUserText.setText(mPrintUserText);

        //???????????? ????????? ??????
        buttonPrintSetDate = (Button) dialogView.findViewById(R.id.button_print_set_date); // ???????????? ?????????
        buttonPrintSetDate.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(), "?????? ?????? ?????????", Toast.LENGTH_SHORT).show();
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


        // ????????? ?????? ??????
        checkboxPrintDateTitle = (CheckBox) dialogView.findViewById(R.id.checkbox_print_date_title); // ????????? ?????? ??????
        if (mPrintDateTitle.equals("1")) {
            checkboxPrintDateTitle.setChecked(true);
        } else {
            checkboxPrintDateTitle.setChecked(false);
        }

        /*----------------------------------------*/

        AlertDialog.Builder builder = new AlertDialog.Builder(ManageProductActivityModfiy.this, AlertDialog.THEME_HOLO_LIGHT);

        builder.setTitle("????????? ??????");

        builder.setView(dialogView);

        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                /*----------------------------------------*/
                // ???????????? ??????????????? ????????????
                /*----------------------------------------*/
                // ?????????
                if (checkboxSaleYn.isChecked()) {
                    mSaleYn = "1";
                } else {
                    mSaleYn = "0";
                }
                LocalStorage.setString(ManageProductActivityModfiy.this, "BarcodePrinterSaleYN", mSaleYn);

                // ??????????????????
                if (checkboxSellPri1.isChecked()) {
                    mSellPri1 = "1";
                } else {
                    mSellPri1 = "0";
                }
                LocalStorage.setString(ManageProductActivityModfiy.this, "BarcodePrinterSellPri1", mSellPri1);

                //?????? ?????????
                //mSelectBranchName 1,2,3

                //???????????? ?????? ?????????
                //mSelectAddItem 1,2

                // ????????? ?????? ??????
                if (checkboxPrintDateTitle.isChecked()) {
                    mPrintDateTitle = "1";
                } else {
                    mPrintDateTitle = "0";
                }
                LocalStorage.setString(mContext, "BarcodePrinterDateTitle", mPrintDateTitle);

                // ??????????????? ?????? ??????
                mPrintUserText = edittextPrintUserText.getText().toString();
                LocalStorage.setString(mContext, "AddItemUserText", mPrintUserText);
                /*----------------------------------------*/
            }
        });

        AlertDialog dialog = builder.create();

        dialog.setCanceledOnTouchOutside(false);//Dialog??? ???????????? ???????????? ??? Dialog??? ????????? ??????

        dialog.show();

    }

    // ?????????????????? ?????? ??????
    private void barcodePrinterMain() {

        //----------------------------------------//
        // ?????? ??? ???????????? ?????? ??????
        //----------------------------------------//
        if ("".equals(m_textBarcode.getText().toString())) {
            Toast.makeText(mContext, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ?????? ????????? ????????? ?????? ?????? ??? ??? ??? ????????? ????????????
        doQueryWithBarcodePrinter();

    }

    // ?????????????????? ??????
    private void barcodePrinterExec(JSONArray results) {

        try {
            JSONObject json = results.getJSONObject(0);
            HashMap<String, String> map = JsonHelper.toStringHashMap(json);

            m_tempProduct.put("Sell_Pri_2", map.get("Sell_Pri"));
            m_tempProduct.put("Sell_Org_2", map.get("Sell_Org"));

            // 2023.02.20.?????????. ??????????????????????????? ???????????? ???????????? ?????? ??????
            m_tempProduct.put("NickName", map.get("NickName"));
            m_tempProduct.put("Location", map.get("Location"));
            m_tempProduct.put("BranchName", map.get("BranchName"));
            m_tempProduct.put("AddItem", map.get("AddItem"));



        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "?????????????????? ????????? ????????? ????????????.", Toast.LENGTH_LONG).show();
            return;
        }

        //----------------------------------------//
        // 2023.02.08. ?????? ????????? ?????? ??????
        //----------------------------------------//
        m_tempProduct.put("Count", "1");
        m_tempProduct.put("Sell_Pri", m_tempProduct.get("Sell_Pri_2"));
        m_tempProduct.put("Sell_Org", m_tempProduct.get("Sell_Org_2"));

        //----------------------------------------//
        // 2021.01.06.?????????. ????????? ??????
        //----------------------------------------//
        float f_ratio = 0;
        float f_sellPri = Float.valueOf(m_tempProduct.get("Sell_Pri")).floatValue();
        float f_sellOrg = Float.valueOf(m_tempProduct.get("Sell_Org")).floatValue();

        try {
            if (f_sellOrg > f_sellPri) {
                f_ratio = (f_sellOrg - f_sellPri) / f_sellOrg * 100;
                if (Float.isInfinite(f_ratio) || Float.isNaN(f_ratio)) {
                    f_ratio = 0;
                }
            }
        } catch (Exception e) {
        }
        m_tempProduct.put("Sale_Rate", String.format("%.0f", f_ratio)); // ?????????

        //----------------------------------------//

        mfillMapsBar.removeAll(mfillMapsBar);
        mfillMapsBar.add(m_tempProduct);

        //----------------------------------------//
        // 2021.01.08.?????????. ????????? ?????? ?????? ??????
        //----------------------------------------//
        if (barcodePrinterName.equals("") || barcodePrinterName.equals("????????????")) {
            Toast.makeText(getApplicationContext(), "?????????????????? ????????? ????????? ????????????.", Toast.LENGTH_LONG).show();
        } else if (barcodePrinterName.equals("SPP-L3000")) {
            barPrintSPP3000Trnasfer();
        } else if (barcodePrinterName.equals("LK-P30II")) {
            barPrintLKP30IITransfer();
        } else {
            m_bprinter = new BarcodePrinterAsyncTask2();
            m_bprinter.execute("Start");
        }

    }

    //????????? ????????? spp-l3000 ????????????
    private void barPrintSPP3000Trnasfer() {

        if (!mBixolonLabelPrinter.isConnected()) {
            Toast.makeText(this, "????????? ???????????? ????????? ?????????!", Toast.LENGTH_SHORT).show();
            return;
        }

        //????????? ?????? ??????
        boolean report = false;
        mBixolonLabelPrinter.getStatus(report);
    }

    //???????????? ????????? ??????
    private void barPrintLKP30IITransfer() {

        if (!bluetoothPort.isConnected()) {
            Toast.makeText(this, "???????????? ???????????? ????????? ?????????!", Toast.LENGTH_SHORT).show();
            return;
        }

        CPCLPrinter cpcl = new CPCLPrinter();
        if (statusCheck(cpcl).equals("Normal")) {
            printStartSeWoo();
        }
    }

    //???????????? ????????? ??????
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
        // 2021.01.06.?????????. ????????????,????????? ??????
        //--------------------------------------------------------------------------------//
        String[] sellPrice = spp.getSellPrice_Setting().split("\\|");
        String[] saleSellRate = spp.getSaleSellRate_Setting().split("\\|");
        //--------------------------------------------------------------------------------//

        //--------------------------------------------------------------------------------//
        // 2021.07.08. ?????????????????? ??????,??????,????????????,???????????? ??????
        //--------------------------------------------------------------------------------//
        String[] location = spp.getLocation_Setting().split("\\|");
        String[] nickName = spp.getNickName_Setting().split("\\|");
        String[] branchName = spp.getBranchName_Setting().split("\\|");
        String[] addItem = spp.getAddItem_Setting().split("\\|");
        //--------------------------------------------------------------------------------//

        Log.d(TAG, "?????? ????????? : " + price[0] + "|" + price[1] + "|" + price[2] + "|" + price[3] + "|" + price[4] + "|" + price[5] + "|" + price[6]);
        //Log.d(TAG, "???????????? : " + mfillMapsBar.size());
        Log.d(TAG, "???????????? : " + 1);  // ???????????? ???????????? 1??? ??????
        //mfillMapsBarBar
        //for (int i = 0; i < mfillMapsBar.size(); i++) {

        HashMap<String, String> map = mfillMapsBar.get(0);

        int count = 1;
        //Log.d(TAG, "?????? ????????? : "+count);

        String g_name = stringToNullCheck(map, "G_Name", "");
        if (g_name.length() > spp.getWord_Length()) {
            g_name = g_name.substring(0, spp.getWord_Length());
        }

        try {

            CPCLPrinter cpclprinter = new CPCLPrinter("EUC-KR");

            //?????? ?????? ??????				intToinchPrint(spp.getLavel_Hight())
            int height = intToinchPrint(spp.getLavel_Hight());  //???????????? ??????
            int width = intToinchPrint(spp.getLavel_Width());   //???????????? ??????
            int direction = spp.getPrint_Direction();           //????????????(0:?????????,1:?????????)

            //cpclprinter.setForm(0, 200, 200, height, count);
            // 2023.03.2.?????????. ????????? ??????
            cpclprinter.setForm(0, 200, 200, height, width, direction, count);

            //?????? ??????
            cpclprinter.setMedia(intTopapergubun_s(spp.getPaper_Gubun()));
            Log.d(TAG, "???????????? : " + intTopapergubun_s(spp.getPaper_Gubun()));

            //????????? ??????
            //????????????, ????????????, ???????????????, ????????????, ????????????, ?????????, ??????)

            //----------------------------------------//
            // 2022.07.28.????????? ?????? ??????
            // 2022.08.04. ???????????????, ?????????????????? ?????? ?????? ?????? ??????
            // ?????? ??????
            // 1. ?????????????????????
            // 2. ?????? ?????? ??????
            // 3. ?????? ?????? ?????????
            // ?????????
            // 1. ?????? ?????? ?????? 1??? ?????? ??????
            // 2. 2??? ?????? ??????
            // 3. 2??? ????????? ?????? ??????
            //----------------------------------------//
            // ??????????????? ?????? ??????
            // 58:400
            // 58:464 inch
            int w0 = spp.getLavel_Width();
            int x0 = 0; // ?????????????????????(0,1,2,3)
            int y0 = 0; // ?????????????????????(0,1,2,3)
            int xp = 0; // ?????? ????????????

            int xl = 0; // ?????? ?????? ??????
            int tl = 0; // 1??? ?????? ?????????

            int ty = 0; // 2??? ?????? ??????

            try {
                x0 = stringTointPrint(gname[3]);
                y0 = stringTointPrint(gname[4]);
                xp = stringTointPrint(gname[0]);

                xl = (int) Math.round((400 * w0 / 58) - xp);
                // ?????? ????????? ?????? ?????? ?????? ??????
                // 0,1 = 36, 2=18,3=12
                if (x0 <= 1) {
                    tl = (int) Math.round((xl * 36) / 400);
                } else if (x0 == 2) {
                    tl = (int) Math.round((xl * 18) / 400);
                } else if (x0 == 3) {
                    tl = (int) Math.round((xl * 12) / 400);
                }

                // 2??? ?????? ??????
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
            // ????????? ?????? ???????????? 2??? ??????
            //----------------------------------------//
            int len = 0;
            String aaa = "";
            String bbb = "";

            char ch[] = g_name.toCharArray();
            int max = ch.length;
            int cnt = 0;

            for (int ii = 0; ii < max; ii++) {
                // 0x80: ????????? ?????? +2
                if (ch[ii] > 0x80) {
                    cnt++;
                }
                cnt++;

                // ????????? ???????????? ??????
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
                    // ?????? ?????? ??????
                    //----------------------------------------//
                    char ca[] = aaa.toCharArray();
                    boolean bla = false;
                    int maxa = ca.length;

                    for (int ii = 0; ii < maxa; ii++) {
                        // 0x80: ????????? ?????? +2
                        if (ca[ii] > 0x80) {
                            bla = true;
                            break;
                        }
                    }

                    char cb[] = bbb.toCharArray();
                    boolean blb = false;
                    int maxb = cb.length;

                    for (int ii = 0; ii < maxb; ii++) {
                        // 0x80: ????????? ?????? +2
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

            //?????? ??????
            if (spp.getPrint_StdSize_YN() == 1) {
                if (!stringToNullCheck(map, "Std_Size", "").equals("")) {
                    cpclprinter.printAndroidFont(stringTointPrint(stdsize[0]), stringTointPrint(stdsize[1]), stringToNullCheck(map, "Std_Size", " "), intToinchPrint(spp.getLavel_Width()), stringTointPrint(stdsize[3] + stdsize[4]) + 10);
                }
            }

            //?????? ??????
            if (spp.getPrint_Price_YN() == 1) {
                cpclprinter.setMagnify(stringTointPrint(price[3]), stringTointPrint(price[4]));
                cpclprinter.printCPCLText(stringTointPrint(price[6]), 7, 0, stringTointPrint(price[0]), stringTointPrint(price[1]), StringFormat.convertToNumberFormat(stringToNullCheck(map, "Sell_Pri_2", "0")) + "???", 0);
                cpclprinter.resetMagnify();
            }

            //????????? ??????
            if (spp.getPrint_Office_YN() == 1) {
                cpclprinter.printAndroidFont(stringTointPrint(office[0]), stringTointPrint(office[1]), stringToNullCheck(map, "Bus_Name", "?????????"), intToinchPrint(spp.getLavel_Width()), stringTointPrint(office[3] + office[4]) + 10);
            }

            //?????? ??????
            if (spp.getPrint_Danga_YN() == 1) {
                double a = Double.parseDouble(stringToNullCheck(map, "Con_Rate", "0"));
                double b = Double.parseDouble(stringToNullCheck(map, "Std_Rate", "0"));
                double c = Double.parseDouble(stringToNullCheck(map, "Sell_Pri_2", "0"));
                a = a / b;

                String dan = String.valueOf(Math.round(c / a)) + " ???/ " + stringToNullCheck(map, "Std_Rate", "0") + stringToNullCheck(map, "Unit", "0");
                Log.i("????????????", dan);

                cpclprinter.printAndroidFont(stringTointPrint(danga[0]), stringTointPrint(danga[1]), dan, intToinchPrint(spp.getLavel_Width()), stringTointPrint(danga[3] + danga[4]) + 10);
            }

            //--------------------------------------------------------------------------------//
            // 2021.01.06.?????????. ????????????,????????? ??????
            // 2023.02.20.?????????. ????????????=0 ?????? ????????????=????????? ??? ?????? ?????? ??????
            // StringFormat.convertToNumberFormat(stringToNullCheck(map, "Sell_Pri", "0")) ?????????
            // StringFormat.convertToNumberFormat(stringToNullCheck(map, "Sell_Org", "0")) ????????????
            //--------------------------------------------------------------------------------//
            double pri = Double.parseDouble(stringToNullCheck(map, "Sell_Pri_2", "0"));
            double org = Double.parseDouble(stringToNullCheck(map, "Sell_Org_2", "0"));
            double rat = Double.parseDouble(stringToNullCheck(map, "Sale_Rate", "0"));   //?????????
            //if (org > pri) {
            //???????????? ??????
            if (spp.getPrint_SellPrice_YN() == 1) {
                if (org > 0) {
                    if (pri < org) {
                        cpclprinter.setMagnify(stringTointPrint(sellPrice[3]), stringTointPrint(sellPrice[4]));
                        cpclprinter.printCPCLText(stringTointPrint(sellPrice[6]), 7, 0, stringTointPrint(sellPrice[0]), stringTointPrint(sellPrice[1]), StringFormat.convertToNumberFormat(stringToNullCheck(map, "Sell_Org_2", "0")) + "", 0);
                        cpclprinter.resetMagnify();
                    }
                }
            }

            //?????????(%) ??????
            if (spp.getPrint_SaleSellRate_YN() == 1) {
                if (rat != 0) {
                    try {
                        cpclprinter.setMagnify(stringTointPrint(saleSellRate[3]), stringTointPrint(saleSellRate[4]));
                        cpclprinter.printCPCLText(stringTointPrint(saleSellRate[6]), 7, 0, stringTointPrint(saleSellRate[0]), stringTointPrint(saleSellRate[1]), StringFormat.convertToNumberFormat(stringToNullCheck(map, "Sale_Rate", "0")) + "%", 0);
                        cpclprinter.resetMagnify();
                    } catch (Exception e) {
                        //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            //}
            //--------------------------------------------------------------------------------//

            //--------------------------------------------------------------------------------//
            // 2021.07.08. ?????????????????? ??????,??????,????????????,???????????? ??????
            //--------------------------------------------------------------------------------//
            //?????? ??????
            if (spp.getPrint_Location_YN() == 1) {
                if (!stringToNullCheck(map, "Location", "").equals("")) {
                    try {
                        cpclprinter.printAndroidFont(stringTointPrint(location[0]), stringTointPrint(location[1]), stringToNullCheck(map, "Location", " "), intToinchPrint(spp.getLavel_Width()), stringTointPrint(location[3] + location[4]) + 10);
                    } catch (Exception e) {
                        //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            //?????? ??????
            if (spp.getPrint_NickName_YN() == 1) {
                try {
                    if (!stringToNullCheck(map, "NickName", "").equals("")) {
                        cpclprinter.printAndroidFont(stringTointPrint(nickName[0]), stringTointPrint(nickName[1]), stringToNullCheck(map, "NickName", " "), intToinchPrint(spp.getLavel_Width()), stringTointPrint(nickName[3] + nickName[4]) + 10);
                    }
                } catch (Exception e) {
                    //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            //?????? ??????
            if (spp.getPrint_BranchName_YN() == 1) {
                String branchname = "";

                if (mSelectBranchName == 1) {  // ?????????
                    branchname = stringToNullCheck(map, "L_Name", " ");
                } else if (mSelectBranchName == 2) {  // ?????????
                    branchname = stringToNullCheck(map, "M_Name", " ");
                } else if (mSelectBranchName == 3) {  // ?????????
                    branchname = stringToNullCheck(map, "S_Name", " ");
                } else {  // ?????? ?????????
                    branchname = stringToNullCheck(map, "L_Name", " ");
                }
                try {
                    cpclprinter.printAndroidFont(stringTointPrint(branchName[0]), stringTointPrint(branchName[1]), branchname, intToinchPrint(spp.getLavel_Width()), stringTointPrint(branchName[3] + branchName[4]) + 10);
                } catch (Exception e) {
                    //Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }

            }

            //???????????? ??????

            if (spp.getPrint_AddItem_YN() == 1) {

                String additem = "";

                if (mSelectAddItem == 1) {
                    if (mPrintDateTitle.equals("1")) {
                        additem = "*?????????:" + mPrintSetDate;
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
            //Toast.makeText(this, "????????????", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            e.printStackTrace();
        }

        //????????????
        dba.insert_BaPrintHistory(mfillMapsBar, "1");
        mfillMapsBar.removeAll(mfillMapsBar);
        //m_adapter.notifyDataSetChanged();

        dialog.dismiss();
        dialog.cancel();

        Toast.makeText(this, "????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
    }

    //????????? ????????? ?????????

    /**
     * ?????? ????????? ????????????.
     *
     * @param gubun int paper_gubun
     * @return 0: ?????????(71), 1: ????????????(67), 2: ??????????????????(66)
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
     * ???????????? ????????? ??????????????? ?????????
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
     * ????????? ??????????????? ??????
     *
     * @param str ?????????
     * @return ?????????
     */
    private int fontSizeToString(String str) {
        if (str.equals("0")) {
            return BixolonLabelPrinter.FONT_SIZE_KOREAN1;
        } else {
            return BixolonLabelPrinter.FONT_SIZE_KOREAN2;
        }
    }

    /**
     * ???????????? boolean?????? ?????? ??????
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

    //????????? ????????? ????????? - ??? -


    //?????? ????????? ?????????

    /**
     * ?????? ????????? ????????????.
     *
     * @param gubun int paper_gubun
     * @return 0: ?????????(0), 1: ????????????(2), 2: ??????????????????(1)
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
     * ????????? ??????????????? ??????
     *
     * @param str ?????????
     * @return ?????????
     */
    private int fontSizeToString_s(String str) {
        if (str.equals("0")) {
            return 0;
        } else {
            return 5;
        }
    }

    /**
     * ????????? ????????? ????????? ???????????? ??????
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
        if (res == null) res = def;

        return res;
    }
    //?????? ????????? ????????? - ??? -

    // SQL QUERY ??????
    public void doQueryWithBarcodePrinter() {


        String barcode = m_textBarcode.getText().toString();
        if (barcode.equals(""))
            return;


        String query = "";

        //2019-11-13 ?????? ?????? ?????? ?????? ??????
        query += " SELECT ";
        query += "  A.BarCode AS BarCode "; // ?????????
        query += " ,A.G_Name AS G_Name "; // ?????????

        // ?????????(?????????+???????????????)/?????????
        if (mSaleYn.equals("1")) {
            query += " ,Sell_Pri = CASE WHEN A.SALE_USE = '1' THEN (ISNULL(A.SALE_SELL,0) + ISNULL(A.BOT_SELL,0)) ELSE (ISNULL(A.SELL_PRI,0) + ISNULL(A.BOT_SELL,0)) END ";
        } else {
            query += " ,(ISNULL(A.SELL_PRI,0) + ISNULL(A.BOT_SELL,0)) AS Sell_Pri ";
        }

        query += " ,A.Std_Size AS Std_Size "; // ??????
        query += " ,A.Bus_Name AS Bus_Name "; // ????????????
        query += " ,A.Con_Rate AS Con_Rate "; // ?????????
        query += " ,A.Std_Rate AS Std_Rate "; // ????????????
        query += " ,A.Unit AS Unit ";

        //----------------------------------------//
        // 2021.01.06.?????????. ????????????,????????? ??????
        //----------------------------------------//
        //query += " ,(ISNULL(A.SELL_PRI,0) + ISNULL(A.BOT_SELL,0)) AS Sell_Org "; //????????????
        if (mSellPri1.equals("1")) {
            query += " ,ISNULL(A.Sell_Pri1,0) AS Sell_Org "; // ??????????????????
        } else {
            query += " ,(ISNULL(A.SELL_PRI,0) + ISNULL(A.BOT_SELL,0)) AS Sell_Org "; //????????????
        }
        query += " ,0 AS Sale_Rate "; //?????????(?????? ?????? ??????-?????? ???????????? ??????)
        //----------------------------------------/

        //----------------------------------------//
        // 2021.07.08.?????????. ??????,??????,??????,?????????????????? ??????
        //----------------------------------------//
        query += " ,ISNULL(A.Location,'') AS Location  "; // ??????
        query += " ,ISNULL(B.NickName,'') AS NickName  "; // ??????
        //query += " ,ISNULL(A.Sell_Pri1,'') AS Sell_Pri1 "; // ??????????????????
        if (mSelectBranchName == 1) {
            query += " ,ISNULL(A.L_Name,'') AS BranchName  "; // ????????????
        } else if (mSelectBranchName == 2) {
            query += " ,ISNULL(A.M_Name,'') AS BranchName  "; // ????????????
        } else if (mSelectBranchName == 3) {
            query += " ,ISNULL(A.S_Name,'') AS BranchName  "; // ????????????
        } else {
            query += " ,ISNULL(A.L_Name,'') AS BranchName  "; // ????????????
        }

        if (mSelectAddItem == 1) {
            String tmpDate = mPrintSetDate;
            if (mPrintDateTitle.equals("1")) {
                tmpDate = "*?????????:" + mPrintSetDate;
            }
            query += " ,'" + tmpDate + "' AS AddItem  "; // ?????????
        } else if (mSelectAddItem == 2) {
            String tmpUser = mPrintUserText;
            query += " ,'" + tmpUser + "' AS AddItem  "; // ???????????????
        } else {
            query += " ,'' AS AddItem  "; // ???????????????
        }
        //----------------------------------------//

        query += " FROM Goods A ";
        query += " LEFT OUTER JOIN (SELECT Barcode, nickName FROM goods_nick WHERE nickgubun = '1' ) B ";
        query += " ON A.Barcode = B.Barcode ";
        query += " WHERE ";
        query += " A.Barcode = '" + barcode + "'";
        query += " AND A.Scale_use = '0' ";    //???????????? ??????
        query += " AND A.goods_use = '1'; ";    //??????????????? ??????


        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // ??????????????? ?????? ??????
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();

                if (results.length() > 0) {
                    barcodePrinterExec(results);
                } else {
                    Toast.makeText(getApplicationContext(), "????????? ????????????", Toast.LENGTH_SHORT).show();
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

        //????????? ????????? ?????????.
        //?????? ?????? ??????
        mBixolonLabelPrinter.setLength(spp.getLavel_Hight(), spp.getGap_Width(), intTopapergubun(spp.getPaper_Gubun()), spp.getGap_Width());

        //????????????
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
        // 2021.01.06.?????????. ????????????,????????? ??????
        //--------------------------------------------------------------------------------//
        String[] sellPrice = spp.getSellPrice_Setting().split("\\|");
        String[] saleSellRate = spp.getSaleSellRate_Setting().split("\\|");
        //--------------------------------------------------------------------------------//
        //--------------------------------------------------------------------------------//
        // 2021.07.08. ?????????????????? ??????,??????,????????????,???????????? ??????
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


            //?????? ??????

            if (spp.getPrint_StdSize_YN() == 1) {
                mBixolonLabelPrinter.drawText(map.get("Std_Size"), stringTointPrint(stdsize[0]), stringTointPrint(stdsize[1]), fontSizeToString(stdsize[2]),
                        stringTointPrint(stdsize[3]) + 1, stringTointPrint(stdsize[4]) + 1, 0,
                        stringTointPrint(stdsize[6]), false, booleanToString(stdsize[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }

            //?????? ??????
            if (spp.getPrint_Price_YN() == 1) {

                mBixolonLabelPrinter.drawText(StringFormat.convertToNumberFormat(map.get("Sell_Pri_2")), stringTointPrint(price[0]), stringTointPrint(price[1]),
                        fontSizeToString(price[2]), stringTointPrint(price[3]) + 1, stringTointPrint(price[4]) + 1, 0,
                        stringTointPrint(price[6]), false, booleanToString(price[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_RIGHT);

                mBixolonLabelPrinter.drawText("???", stringTointPrint(price[0]) + 4, stringTointPrint(price[1]),
                        fontSizeToString(price[2]), stringTointPrint(price[3]) + 1, stringTointPrint(price[4]) + 1, 0,
                        stringTointPrint(price[6]), false, booleanToString(price[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_LEFT);
            }

            //????????? ??????
            if (spp.getPrint_Office_YN() == 1) {
                mBixolonLabelPrinter.drawText(map.get("Bus_Name"), stringTointPrint(office[0]), stringTointPrint(office[1]), fontSizeToString(office[2]),
                        stringTointPrint(office[3]) + 1, stringTointPrint(office[4]) + 1, 0,
                        stringTointPrint(office[6]), false, booleanToString(office[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }

            //?????? ??????
            if (spp.getPrint_Danga_YN() == 1) {
                double a = Double.parseDouble(map.get("Con_Rate"));
                double b = Double.parseDouble(map.get("Std_Rate"));
                double c = Double.parseDouble(map.get("Sell_Pri_2"));
                a = a / b;

                String dan = String.valueOf(Math.round(c / a)) + " ???/ " + map.get("Std_Rate") + map.get("Unit");
                Log.i("????????????", dan);

                mBixolonLabelPrinter.drawText(dan, stringTointPrint(danga[0]), stringTointPrint(danga[1]), fontSizeToString(danga[2]),
                        stringTointPrint(danga[3]) + 1, stringTointPrint(danga[4]) + 1, 0,
                        stringTointPrint(danga[6]), false, booleanToString(danga[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }

            //--------------------------------------------------------------------------------//
            // 2021.01.06.?????????. ????????????,????????? ??????
            // 2023.02.20.?????????. ????????????=0 ?????? ????????????=????????? ??? ?????? ?????? ??????
            // StringFormat.convertToNumberFormat(stringToNullCheck(map, "Sell_Pri", "0")) ?????????
            // StringFormat.convertToNumberFormat(stringToNullCheck(map, "Sell_Org", "0")) ????????????
            //--------------------------------------------------------------------------------//
            double pri = Double.parseDouble(stringToNullCheck(map, "Sell_Pri_2", "0")); // ?????????
            double org = Double.parseDouble(stringToNullCheck(map, "Sell_Org_2", "0")); // ????????????
            double rat = Double.parseDouble(stringToNullCheck(map, "Sale_Rate", "0"));  //?????????
            if (org > pri) {
                //???????????? ??????
                if (spp.getPrint_SellPrice_YN() == 1) {
                    mBixolonLabelPrinter.drawText(StringFormat.convertToNumberFormat(map.get("Sell_Org_2")), stringTointPrint(sellPrice[0]), stringTointPrint(sellPrice[1]),
                            fontSizeToString(sellPrice[2]), stringTointPrint(sellPrice[3]) + 1, stringTointPrint(sellPrice[4]) + 1, 0,
                            stringTointPrint(sellPrice[6]), false, booleanToString(sellPrice[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_RIGHT);

                    mBixolonLabelPrinter.drawText("", stringTointPrint(sellPrice[0]) + 4, stringTointPrint(sellPrice[1]),
                            fontSizeToString(sellPrice[2]), stringTointPrint(sellPrice[3]) + 1, stringTointPrint(sellPrice[4]) + 1, 0,
                            stringTointPrint(sellPrice[6]), false, booleanToString(sellPrice[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_LEFT);
                }

                //?????????(%) ??????
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
            // 2021.07.08. ?????????????????? ??????,??????,????????????,???????????? ??????
            //--------------------------------------------------------------------------------//
            //?????? ??????
            if (spp.getPrint_Location_YN() == 1) {
                mBixolonLabelPrinter.drawText(map.get("Location"), stringTointPrint(location[0]), stringTointPrint(location[1]), fontSizeToString(location[2]),
                        stringTointPrint(location[3]) + 1, stringTointPrint(location[4]) + 1, 0,
                        stringTointPrint(location[6]), false, booleanToString(location[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }

            //?????? ??????
            if (spp.getPrint_NickName_YN() == 1) {
                mBixolonLabelPrinter.drawText(map.get("NickName"), stringTointPrint(nickName[0]), stringTointPrint(nickName[1]), fontSizeToString(nickName[2]),
                        stringTointPrint(nickName[3]) + 1, stringTointPrint(nickName[4]) + 1, 0,
                        stringTointPrint(nickName[6]), false, booleanToString(nickName[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);
            }

            //?????? ??????
            if (spp.getPrint_BranchName_YN() == 1) {
//                mBixolonLabelPrinter.drawText(map.get("BranchName"), stringTointPrint(branchName[0]), stringTointPrint(branchName[1]), fontSizeToString(branchName[2]),
//                        stringTointPrint(branchName[3]) + 1, stringTointPrint(branchName[4]) + 1, 0,
//                        stringTointPrint(branchName[6]), false, booleanToString(branchName[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);

                String branchname = "";

                if (mSelectBranchName == 1) {  // ?????????
                    branchname = stringToNullCheck(map, "L_Name", " ");
                } else if (mSelectBranchName == 2) {  // ?????????
                    branchname = stringToNullCheck(map, "M_Name", " ");
                } else if (mSelectBranchName == 3) {  // ?????????
                    branchname = stringToNullCheck(map, "S_Name", " ");
                } else {  // ?????? ?????????
                    branchname = stringToNullCheck(map, "L_Name", " ");
                }
                mBixolonLabelPrinter.drawText(branchname, stringTointPrint(branchName[0]), stringTointPrint(branchName[1]), fontSizeToString(branchName[2]),
                        stringTointPrint(branchName[3]) + 1, stringTointPrint(branchName[4]) + 1, 0,
                        stringTointPrint(branchName[6]), false, booleanToString(branchName[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);

            }

            //???????????? ??????
            if (spp.getPrint_AddItem_YN() == 1) {
//                mBixolonLabelPrinter.drawText(map.get("AddItem"), stringTointPrint(addItem[0]), stringTointPrint(addItem[1]), fontSizeToString(addItem[2]),
//                        stringTointPrint(addItem[3]) + 1, stringTointPrint(addItem[4]) + 1, 0,
//                        stringTointPrint(addItem[6]), false, booleanToString(addItem[5]), BixolonLabelPrinter.TEXT_ALIGNMENT_NONE);


                String additem = "";

                if (mSelectAddItem == 1) {
                    if (mPrintDateTitle.equals("1")) {
                        additem = "*?????????:" + mPrintSetDate;
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


        //????????????
        dba.insert_BaPrintHistory(mfillMapsBar, "1");
        mfillMapsBar.removeAll(mfillMapsBar);
        //m_adapter.notifyDataSetChanged();

        dialog.dismiss();
        dialog.cancel();

        Toast.makeText(this, "????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();


    }

    //????????? ????????? ?????????????????? ????????????
    public class BarcodePrinterAsyncTask2 extends AsyncTask<String, Integer, Boolean> {

        PrintWriter out;
        Socket socket = null;
        private int count_num = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "????????????", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub

            // 2021.01.05.?????????. ?????? ???????????? ??????
            // 2021.01.05.?????????. ?????? ???????????? ??????
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
                for (int i = 0; i < mfillMapsBar.size(); i++) {
                    String content = "";
                    map.putAll(mfillMapsBar.get(i));
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
                    Log.i("????????????", content.toString());

                    //--------------------------------------------------------------------------------//
                    // 2021.01.06.?????????. ???????????? ??????
                    //--------------------------------------------------------------------------------//
                    //double pri = Double.parseDouble(stringToNullCheck(map, "Sell_Pri", "0"));
                    //double org = Double.parseDouble(stringToNullCheck(map, "Sell_Org", "0"));
/*                    if (org > pri) {
                        content += "; ";
                        content += StringFormat.convertToNumberFormat(map.get("Sell_Org"));
                    }*/
                    //--------------------------------------------------------------------------------//
                    // 2021.07.20.?????????. ???????????? ?????? ??????
                    //--------------------------------------------------------------------------------//
                    content += "; ";
                    content += StringFormat.convertToNumberFormat(map.get("Sell_Org_2"));
                    //----------------------------------------//

                    //----------------------------------------//
                    // 2021.07.08.?????????. ??????,??????,??????,?????????????????? ??????
                    //----------------------------------------//
                    //??????
                    content += "; ";
                    content += map.get("Location");

                    //??????
                    content += "; ";
                    content += map.get("NickName");

                    //??????
                    content += "; ";
                    //content += map.get("BranchName");

                    String branchname = "";

                    if (mSelectBranchName == 1) {  // ?????????
                        branchname = stringToNullCheck(map, "L_Name", " ");
                    } else if (mSelectBranchName == 2) {  // ?????????
                        branchname = stringToNullCheck(map, "M_Name", " ");
                    } else if (mSelectBranchName == 3) {  // ?????????
                        branchname = stringToNullCheck(map, "S_Name", " ");
                    } else {  // ?????? ?????????
                        branchname = stringToNullCheck(map, "L_Name", " ");
                    }
                    content += branchname;

                    //????????????
                    content += "; ";
                    //content += map.get("AddItem");
                    String additem = "";

                    if (mSelectAddItem == 1) {
                        if (mPrintDateTitle.equals("1")) {
                            additem = "*?????????:" + mPrintSetDate;
                        } else {
                            additem = mPrintSetDate;
                        }
                    } else if (mSelectAddItem == 2) {
                        additem = mPrintUserText;
                    }
                    content += additem;
                    //----------------------------------------//

                    //2018??? 12??? 17??? ??????
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
                dba.insert_BaPrintHistory(mfillMapsBar, "1");
                mfillMapsBar.removeAll(mfillMapsBar);
                //m_adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), String.valueOf(count_num) + " ??? ????????????", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getApplicationContext(), "?????????????????? ??????????????? ????????? ?????????.", Toast.LENGTH_LONG).show();
            }
        }
    }

}
