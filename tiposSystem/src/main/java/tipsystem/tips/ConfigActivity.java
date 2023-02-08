package tipsystem.tips;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import tipsystem.utils.BarcodePrintLKP30II;
import tipsystem.utils.BarcodePrinterSPPL3000;
import tipsystem.utils.DBAdapter;
import tipsystem.utils.KisCardPayment;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.TIPOS;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.text.Editable;
import android.text.Html;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.support.v4.app.NavUtils;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Typeface;
import android.os.Build;

public class ConfigActivity extends Activity {

    private String TAG = "ConfigActivity";

    JSONObject m_shop;

    String rtn, verSion;

    LinearLayout m_setting_infomaition;
    LinearLayout m_setting_autologin;
    LinearLayout m_setting_database;

    Button btn_barcode;
    Spinner spinner_barcode_type;
    Spinner spinnerKeypadType;

    String printAddress;    //바코드프린터 주소
    String printPort; // 바코드프린터 포트 추가(2021.01.04.김영목)

    ToggleButton checkBoxAutoLogin;
    String id;
    String pw;
    Button m_databaseRenewal;
    //String[] datalist;

    CheckBox checkboxRealSaleNew;
    CheckBox checkboxRealSaleNewOnly;

    CheckBox checkBoxSelfBarcodePrefixUse;
    EditText editTextSelfBarcodePrefixCode;
    TextView textViewSelfBarcodePrefixCode;

    DBAdapter dba;

    Context mContext;

    String Office_Name = null, OFFICE_CODE = null, SHOP_IP = null, SHOP_PORT = null, APP_HP = null;
    //목록저장맵
    List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_detail);

        mContext = this;

        m_shop = LocalStorage.getJSONObject(ConfigActivity.this, "currentShopData");

        try {
            Office_Name = m_shop.getString("Office_Name");
            OFFICE_CODE = m_shop.getString("OFFICE_CODE");
            SHOP_IP = m_shop.getString("SHOP_IP");
            SHOP_PORT = m_shop.getString("SHOP_PORT");
            APP_HP = m_shop.getString("APP_HP");
            Log.i("ConfigDetailActivity", m_shop.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean isAutoLogin = LocalStorage.getBoolean(ConfigActivity.this, "AutoLogin:" + OFFICE_CODE);
        String posID = LocalStorage.getString(ConfigActivity.this, "currentPosID:" + OFFICE_CODE);

        //바코드 프린터 관련
        int isBarcodePrinter = 0;
        try {
            isBarcodePrinter = LocalStorage.getInt(ConfigActivity.this, "BarcodePrinter:" + OFFICE_CODE);
        } catch (RuntimeException e) {
            LocalStorage.setInt(ConfigActivity.this, "BarcodePrinter:" + OFFICE_CODE, 0);
        }

        /* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
        int intKeypadType = 0;
        try {
            intKeypadType = LocalStorage.getInt(ConfigActivity.this, "KeypadType:" + OFFICE_CODE);
        } catch (RuntimeException e) {
            LocalStorage.setInt(ConfigActivity.this, "KeypadType:" + OFFICE_CODE, 0);
        }

        printAddress = LocalStorage.getString(ConfigActivity.this, "PrintAddress:" + OFFICE_CODE);
        printPort = LocalStorage.getString(ConfigActivity.this, "PrintPort:" + OFFICE_CODE); // 바코드프린터 포트 추가(2021.01.04.김영목)

        // 해당 OFFICE_CODE 에 포스ID 가 없을경우 'P' 로 셋팅
        if (posID.equals("")) {
            posID = "P";
            LocalStorage.setString(ConfigActivity.this, "currentPosID:" + OFFICE_CODE, "P");
        }


        Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
        TextView textView = (TextView) findViewById(R.id.textViewShopTitle);
        textView.setTypeface(typeface);
        textView.setText(Office_Name + " 환경설정");

        textView = (TextView) findViewById(R.id.textViewPhoneNumber);
        textView.setTypeface(typeface);

        String ph1, ph2, ph3;
        if (APP_HP.length() == 11) {
            ph1 = APP_HP.substring(0, 3);
            ph2 = APP_HP.substring(3, 7);
            ph3 = APP_HP.substring(7, 11);
        } else {
            ph1 = APP_HP.substring(0, 3);
            ph2 = APP_HP.substring(3, 6);
            ph3 = APP_HP.substring(6, 10);
        }
        ph1 = ph1 + "-" + ph2 + "-" + ph3;
        textView.setText("현재기기의 전화번호는 : " + ph1 + " 입니다.");


        /*
         * 매장정보 설정
         * 2105/1/23 김현수
         * 환경설정 화면 변경
         * */
        m_setting_infomaition = (LinearLayout) findViewById(R.id.setting_infomation);
        m_setting_infomaition.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //Version version = new Version();
                //version.execute();
                //2018-04-23 변경됩니다.
                visonCheckStart();
            }
        });

        /*
         * 포스ID 설정
         * 2105/1/23 김현수
         * 환경설정 화면 변경
         * */
        Spinner spinnerPosID = (Spinner) findViewById(R.id.spinnerPosID);

        if (!posID.equals("")) {
            char c = posID.charAt(0);
            spinnerPosID.setSelection(c - 'A');
        }

        spinnerPosID.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                String posID = ((Spinner) findViewById(R.id.spinnerPosID)).getSelectedItem().toString();

                try {
                    String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
                    LocalStorage.setString(ConfigActivity.this, "currentPosID:" + OFFICE_CODE, posID);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        /* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
        spinnerKeypadType = (Spinner) findViewById(R.id.spinnerKeypadType);
        spinnerKeypadType.setSelection(intKeypadType, false);
        spinnerKeypadType.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub
                String keypadType = ((Spinner) findViewById(R.id.spinnerKeypadType)).getSelectedItem().toString();
                switch (keypadType) {
                    case "숫자키패드":
                        // TODO Auto-generated method stub
                        LocalStorage.setInt(ConfigActivity.this, "KeypadType:" + OFFICE_CODE, 0);
                        break;
                    case "문자키패드":
                        LocalStorage.setInt(ConfigActivity.this, "KeypadType:" + OFFICE_CODE, 1);
                        break;
                    default:
                        LocalStorage.setInt(ConfigActivity.this, "KeypadType:" + OFFICE_CODE, 0);
                        break;
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
        /* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */

        /*
         * 바코드프린터 설정
         * 2105/1/23 김현수
         * 환경설정 화면 변경
         * */
        btn_barcode = (Button) findViewById(R.id.setting_barcodeprint_Button);

        spinner_barcode_type = (Spinner) findViewById(R.id.spinnerBarcodePrint);
        spinner_barcode_type.setSelection(isBarcodePrinter, false);
        spinner_barcode_type.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                btn_barcode.setEnabled(true);
                barcodeChooseType();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //바코드 용지 설정
        btn_barcode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                String barcode_type = (String) spinner_barcode_type.getSelectedItem();
                switch (barcode_type) {
                    case "매장프린터":
                        // TODO Auto-generated method stub
                        barcodeSave();
                        break;
                    case "SPP-L3000":
                        barcode_sppl3000_setting();
                        break;
                    case "LK-P30II":
                        barcode_lkp30ii_setting();
                        break;
                }
            }
        });
        
        /*m_setting_barcodeprint = (LinearLayout)findViewById(R.id.setting_barcodeprinter);
        m_setting_barcodeprint.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if(tgbtn_barcode.isChecked()){				
					barcodeSave();
				}else{
					Toast.makeText(ConfigActivity.this, "사용체크를 해주세요", Toast.LENGTH_LONG).show();
				}
			}
		});*/

        /*
         * 자동로그인 설정
         * 2105/1/23 김현수
         * 환경설정 화면 변경
         * */
        checkBoxAutoLogin = (ToggleButton) findViewById(R.id.setting_autologin_Button);
        checkBoxAutoLogin.setChecked(isAutoLogin);
        checkBoxAutoLogin.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked) {
                    autoLoginSave();
                } else {
                    try {
                        String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
                        LocalStorage.setBoolean(ConfigActivity.this, "AutoLogin:" + OFFICE_CODE, false);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        m_setting_autologin = (LinearLayout) findViewById(R.id.setting_autologin);
        m_setting_autologin.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (checkBoxAutoLogin.isChecked()) {
                    autoLoginSave();
                } else {
                    Toast.makeText(ConfigActivity.this, "사용체크를 해주세요", Toast.LENGTH_LONG).show();
                }
            }
        });


        /*
         * 2015-01-25
         * 김현수 데이타 베이스 갱신 관련 업데이트
         *
         * */
		/*dba = new DBAdapter(ConfigActivity.this, OFFICE_CODE+".tips");
		
		m_setting_database = (LinearLayout)findViewById(R.id.setting_databaserenewal);
		datalist = new String[] {"true", "true", "true"};
		m_setting_database.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				databaseRenewal();				
			}
		});
		
		m_databaseRenewal = (Button)findViewById(R.id.setting_database_button);
		m_databaseRenewal.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				databaseRenewal();	
			}
		});*/
        dba = new DBAdapter(ConfigActivity.this, OFFICE_CODE + ".tips");

        m_setting_database = (LinearLayout) findViewById(R.id.setting_databaserenewal);
        //datalist = new String[] {"true", "true", "true"};
        m_setting_database.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                Toast.makeText(ConfigActivity.this, "데이타베이스 테이블을 삭제 합니다.", Toast.LENGTH_LONG).show();
            }
        });

        m_databaseRenewal = (Button) findViewById(R.id.setting_database_button);
        m_databaseRenewal.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                //dba.delete_all();
                dba.dropTable_all();
            }
        });

        Button kisvan_setting = (Button) findViewById(R.id.setting_kisvan_btn);
        kisvan_setting.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(mContext, KisCardPayment.class));
            }
        });

        /* 김영목 2022.06.30. 신매출속보보기 옵션 추가 */
        boolean isRealSaleNew = LocalStorage.getBoolean(ConfigActivity.this, "RealSaleNew:" + OFFICE_CODE);
        boolean isRealSaleNewOnly = LocalStorage.getBoolean(ConfigActivity.this, "RealSaleNewOnly:" + OFFICE_CODE);

        checkboxRealSaleNew = (CheckBox)findViewById(R.id.checkboxRealSaleNew);
        checkboxRealSaleNew.setChecked(isRealSaleNew);
        checkboxRealSaleNew.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
                    LocalStorage.setBoolean(ConfigActivity.this, "RealSaleNew:" + OFFICE_CODE, b);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (checkboxRealSaleNew.isChecked()){
                    checkboxRealSaleNewOnly.setVisibility(View.VISIBLE);
                }else{
                    // 안보이면 기본값으로 false 로 정의
                    LocalStorage.setBoolean(ConfigActivity.this, "RealSaleNewOnly:" + OFFICE_CODE, false);
                    checkboxRealSaleNewOnly.setVisibility(View.INVISIBLE);
                }

            }
        });

        checkboxRealSaleNewOnly = (CheckBox)findViewById(R.id.checkboxRealSaleNewOnly);
        checkboxRealSaleNewOnly.setChecked(isRealSaleNewOnly);
        checkboxRealSaleNewOnly.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
                    LocalStorage.setBoolean(ConfigActivity.this, "RealSaleNewOnly:" + OFFICE_CODE, b);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        if (isRealSaleNew){
            checkboxRealSaleNewOnly.setVisibility(View.VISIBLE);
        }else{
            checkboxRealSaleNewOnly.setVisibility(View.INVISIBLE);
        }

        //--------------------------------------------------------------------------------//
        /* 김영목 2023.02.08. 바코드자동생성 옵션 추가 */
        //--------------------------------------------------------------------------------//
        boolean isSelfBarcodeUse = LocalStorage.getBoolean(ConfigActivity.this, "SelfBarcodePrefixUse:" + OFFICE_CODE);
        String selfBarcodePrefixCode = LocalStorage.getString(ConfigActivity.this, "SelfBarcodePrefixCode:" + OFFICE_CODE);

        checkBoxSelfBarcodePrefixUse = (CheckBox) findViewById(R.id.checkbox_self_barcode_prefix_use);
        checkBoxSelfBarcodePrefixUse.setChecked(isSelfBarcodeUse);
        checkBoxSelfBarcodePrefixUse.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                try {
                    String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
                    LocalStorage.setBoolean(ConfigActivity.this, "SelfBarcodePrefixUse:" + OFFICE_CODE, b);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                if (checkBoxSelfBarcodePrefixUse.isChecked()){
                    textViewSelfBarcodePrefixCode.setVisibility(View.VISIBLE);
                    editTextSelfBarcodePrefixCode.setVisibility(View.VISIBLE);
                }else{
                    textViewSelfBarcodePrefixCode.setVisibility(View.INVISIBLE);
                    editTextSelfBarcodePrefixCode.setVisibility(View.INVISIBLE);
                }
            }
        });

        editTextSelfBarcodePrefixCode = (EditText) findViewById(R.id.edittext_self_barcode_prefix_code);
        editTextSelfBarcodePrefixCode.setText(selfBarcodePrefixCode);
        editTextSelfBarcodePrefixCode.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 입력란에 변화가 있을 시
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                // 입력이 끝났을 때
                String selfBarcodePrefixCode = editTextSelfBarcodePrefixCode.getText().toString();
                LocalStorage.setString(ConfigActivity.this, "SelfBarcodePrefixCode:" + OFFICE_CODE, selfBarcodePrefixCode);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // 입력하기 전에
            }
        });

        textViewSelfBarcodePrefixCode = (TextView) findViewById(R.id.textview_self_barcode_prefix_code);

        if (isSelfBarcodeUse){
            textViewSelfBarcodePrefixCode.setVisibility(View.VISIBLE);
            editTextSelfBarcodePrefixCode.setVisibility(View.VISIBLE);
        }else{
            textViewSelfBarcodePrefixCode.setVisibility(View.INVISIBLE);
            editTextSelfBarcodePrefixCode.setVisibility(View.INVISIBLE);
        }
        //--------------------------------------------------------------------------------//

    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            ActionBar actionbar = getActionBar();
            LinearLayout custom_action_bar = (LinearLayout) View.inflate(this, R.layout.activity_custom_actionbar, null);
            actionbar.setCustomView(custom_action_bar);

            actionbar.setDisplayShowHomeEnabled(true);
            actionbar.setDisplayShowTitleEnabled(false);
            actionbar.setDisplayShowCustomEnabled(true);

            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    //버전체크 방식 변경됩니다. 구글에서 긁어오기를 못해서 변경합니다.
    //2018-04-23
    private void visonCheckStart() {


        //서버에 등록된 버전 정보를 확인합니다.
        String query = "";
        query = "Select AppVersion From Version;";

        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                if (results.length() > 0) {
                    try {
                        JSONObject json = results.getJSONObject(0);
                        String m_version = json.getString("AppVersion");
                        Log.i("onPosteExecute 버전정보표시", m_version);
                        // Version check the execution application.
                        PackageInfo pi = null;
                        try {
                            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                            //String sMyVersion = getResources().getString(R.string.check_version.);
                        } catch (NameNotFoundException e) {
                            e.printStackTrace();
                        }
                        verSion = pi.versionName;
                        Log.i("verSion-1", pi.versionName);

                        if (!m_version.equals("")) {
                            rtn = m_version;

                            //매장 정보 띄우기
                            officeConfig();
                        } else {
                            //버전저장하기
                            Log.i("현재버전 정보", verSion.toString());
                            Toast.makeText(getApplicationContext(), " 버전 정보를 찾지 못했습니다. ", Toast.LENGTH_LONG).show();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "종료기간을 알수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {

                Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
                finish();
            }
            // 2022.05.26.본사서버 IP변경
            //}).execute("14.38.161.45:18975", "ShoppingMall", "sa", "tips", query);
        }).execute(TIPOS.SHOPPING_MALL_IP + ":" + TIPOS.SHOPPING_MALL_PORT, TIPOS.SHOPPING_MALL_DB, TIPOS.SHOPPING_MALL_ID, TIPOS.SHOPPING_MALL_PW, query);

    }

    /* 바코드 프린터 설정
     * 2105/1/23 김현수  * */
    private void barcodeChooseType() {

        String barcode_type = (String) spinner_barcode_type.getSelectedItem();
        switch (barcode_type) {
            case "매장프린터":
                // TODO Auto-generated method stub
                barcodeSave();
                break;
            case "SPP-L3000":
                LocalStorage.setInt(ConfigActivity.this, "BarcodePrinter:" + OFFICE_CODE, 2);
                LocalStorage.setInt(ConfigActivity.this, "BarcodePrinterPaperGubun:" + OFFICE_CODE, 0);
                barcode_sppl3000_setting();
                break;
            case "LK-P30II":
                LocalStorage.setInt(ConfigActivity.this, "BarcodePrinter:" + OFFICE_CODE, 3);
                LocalStorage.setInt(ConfigActivity.this, "BarcodePrinterPaperGubun:" + OFFICE_CODE, 0);
                barcode_lkp30ii_setting();
                break;
            default:
                LocalStorage.setInt(ConfigActivity.this, "BarcodePrinter:" + OFFICE_CODE, 0);
                btn_barcode.setEnabled(false);
                break;
        }
    }


    private void barcodeSave() {
        Context mContext = ConfigActivity.this;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_barcode_printer_input, (ViewGroup) findViewById(R.id.layout_root));


        // 바코드 검색 버튼 클릭시 나오는 목록 셋팅
        //final EditText input = new EditText(ConfigActivity.this);
        final EditText addressEditText = (EditText) layout.findViewById(R.id.address_edittext);
        printAddress = LocalStorage.getString(ConfigActivity.this, "PrintAddress:" + OFFICE_CODE);

        final EditText portEditText = (EditText) layout.findViewById(R.id.port_edittext); // 바코드프린터 포트 추가(2021.01.04.김영목)
        printPort = LocalStorage.getString(ConfigActivity.this, "PrintPort:" + OFFICE_CODE); // 바코드프린터 포트 추가(2021.01.04.김영목)

        Log.w(TAG, printAddress.toString());
        if (printAddress.isEmpty()) {
            addressEditText.setText(SHOP_IP);
        } else {
            addressEditText.setText(printAddress);
        }

        //portEditText.setText(printPort);

        if (printPort.isEmpty()) {
            portEditText.setText("8671");
        } else {
            portEditText.setText(printPort);
        }

        // 2021.07.06.김영목 IP 주소 입력시 숫자만 입력 에러 수정
        //addressEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        addressEditText.setInputType(InputType.TYPE_CLASS_TEXT);

        portEditText.setInputType(InputType.TYPE_CLASS_NUMBER);

        addressEditText.requestFocus();

        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        //new AlertDialog.Builder(ConfigActivity.this)
        new AlertDialog.Builder(ConfigActivity.this,AlertDialog.THEME_HOLO_LIGHT)
                .setTitle("프린터 연결 설정")
                //.setMessage("바코드 프린터 연결 IP주소 입력 \n\r방화벽 포트설정값 8671 ")
                .setMessage("바코드 프린터 연결 IP주소 및 PORT 입력")
                .setView(layout)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String address = addressEditText.getText().toString();
                        String port = portEditText.getText().toString();

                        if (address.isEmpty()) {
                            Toast.makeText(ConfigActivity.this, "주소를 입력해 주세요!", Toast.LENGTH_SHORT).show();
                            barcodeSave();
                            return;
                        }

                        if (port.isEmpty()) {
                            Toast.makeText(ConfigActivity.this, "포트를 입력해 주세요!", Toast.LENGTH_SHORT).show();
                            barcodeSave();
                            return;
                        }
                        LocalStorage.setInt(ConfigActivity.this, "BarcodePrinter:" + OFFICE_CODE, 1);
                        LocalStorage.setString(ConfigActivity.this, "PrintAddress:" + OFFICE_CODE, address);
                        LocalStorage.setString(ConfigActivity.this, "PrintPort:" + OFFICE_CODE, port);


                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String address = addressEditText.getText().toString();
                        String port = portEditText.getText().toString();

                        if (address.isEmpty() || port.isEmpty()) {
                            spinner_barcode_type.setSelection(0);
                            LocalStorage.setInt(ConfigActivity.this, "BarcodePrinter:" + OFFICE_CODE, 0);
                            LocalStorage.setString(ConfigActivity.this, "PrintAddress:" + OFFICE_CODE, address);
                            LocalStorage.setString(ConfigActivity.this, "PrintPort:" + OFFICE_CODE, port);
                        }
                    }
                }).show();
    }

    private void barcode_sppl3000_setting() {
        Intent intent = new Intent(this, BarcodePrinterSPPL3000.class);
        startActivity(intent);
    }

    private void barcode_lkp30ii_setting() {
        Intent intent = new Intent(this, BarcodePrintLKP30II.class);
        startActivity(intent);
    }


    /* 자동로그인 설정
     * 2105/1/23 김현수  */
    private void autoLoginSave() {

        // 오토로그인 셋팅
        Context mContext = ConfigActivity.this;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_autologin_input, (ViewGroup) findViewById(R.id.layout_root));

        final CheckBox local_food = (CheckBox) layout.findViewById(R.id.checkBox_local);
        final EditText input_id = (EditText) layout.findViewById(R.id.editText_id);
        final EditText input_pw = (EditText) layout.findViewById(R.id.editText_pw);
        boolean isLocal_Food = LocalStorage.getBoolean(ConfigActivity.this, "LocalFood:" + OFFICE_CODE);
        Log.d("로그인", "로컬푸드 : " + isLocal_Food);
        id = LocalStorage.getString(ConfigActivity.this, "LoginID:" + OFFICE_CODE);
        pw = LocalStorage.getString(ConfigActivity.this, "LoginPW:" + OFFICE_CODE);
        local_food.setChecked(isLocal_Food);

        local_food.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (((CheckBox) v).isChecked()) {
                    input_pw.setEnabled(false);
                    input_pw.setText("");
                } else {
                    input_pw.setEnabled(true);
                }
            }
        });

        input_id.setText(id);
        if (isLocal_Food) {
            input_pw.setEnabled(false);
        } else {
            input_pw.setText(pw);
        }
        input_id.requestFocus();

        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        new AlertDialog.Builder(ConfigActivity.this,AlertDialog.THEME_HOLO_LIGHT)
                .setTitle("자동 로그인")
                .setMessage("매장 로그인 정보를 입력해 주세요")
                .setView(layout)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value_id = input_id.getText().toString();
                        String value_pw = input_pw.getText().toString();
                        if (value_id.isEmpty()) {
                            Toast.makeText(ConfigActivity.this, "아이디를 입력해 주세요!", Toast.LENGTH_SHORT).show();
                            return;
                        }
                
               /* if(value_id.isEmpty()) {
                	Toast.makeText(ConfigActivity.this, "패스워드를 입력해 주세요!", Toast.LENGTH_SHORT).show();
                	return;
                }*/

                        boolean isCheckedAutoLogin = checkBoxAutoLogin.isChecked();
                        boolean isCheckedLocalFood = local_food.isChecked();

                        try {
                            String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
                            LocalStorage.setBoolean(ConfigActivity.this, "LocalFood:" + OFFICE_CODE, isCheckedLocalFood);
                            LocalStorage.setBoolean(ConfigActivity.this, "AutoLogin:" + OFFICE_CODE, isCheckedAutoLogin);
                            LocalStorage.setString(ConfigActivity.this, "LoginID:" + OFFICE_CODE, value_id);
                            LocalStorage.setString(ConfigActivity.this, "LoginPW:" + OFFICE_CODE, value_pw);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value_id = input_id.getText().toString();
                        String value_pw = input_pw.getText().toString();

                        if (value_id.isEmpty() || value_pw.isEmpty()) {
                            checkBoxAutoLogin.setChecked(false);
                        }
                    }
                }).show();
    }


    public void officeConfig() {

        // 바코드 검색 버튼 클릭시 나오는 목록 셋팅
        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        AlertDialog.Builder ab = new AlertDialog.Builder(ConfigActivity.this,AlertDialog.THEME_HOLO_LIGHT);
        ab.setMessage(
                Html.fromHtml("<strong><font color=\"#6799FF\"> <h1> 매장 관련 정보 </h1>"
                        + "</font></strong><br> 매장 IP 주소 : " + SHOP_IP.toString() + ""
                        + "<br> 매장 Port : " + SHOP_PORT.toString() + "<br><br>"
                        + " 현재 버전 : " + verSion.toString() + "<br>"
                        + " 최신 버전 : " + rtn.toString() + " <br>"));
        ab.setPositiveButton("닫기", null);
        ab.setNegativeButton("업데이트 하기", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                Intent marketLaunch = new Intent(
                        Intent.ACTION_VIEW);
                marketLaunch.setData(Uri
                        .parse("https://play.google.com/store/apps/details?id=kr.co.tipos.tips"));
                startActivity(marketLaunch);
                finish();
            }
        });
        ab.show();

    }

    /* 데이타베이스 갱신
     * 2105/1/23 김현수  * */
	/*private void databaseRenewal(){
		// 바코드 검색 버튼 클릭시 나오는 목록 셋팅
		final DataBaseRenewal dbr = new DataBaseRenewal();		
		
    	new AlertDialog.Builder(ConfigActivity.this)
        .setTitle("데아타베이스 갱신")       
        .setMultiChoiceItems(R.array.database_renewal_item, 
        		new boolean[]{true, true, true},
        		new DialogInterface.OnMultiChoiceClickListener() {					
					@Override
					public void onClick(DialogInterface dialog, int which, boolean isChecked) {
						// TODO Auto-generated method stub						
						datalist[which] = String.valueOf(isChecked);
						
					}
				})        
        .setPositiveButton("시작", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            	
            	dba.delete_all(datalist);
            	dba.dropTable_all(datalist);
            	dbr.execute(datalist);
            	//startDatabase(datalist);
            }
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {			    
            	            	
            }
        }).show();	
	}
	
	private void startDatabase(String... param){
		Boolean goods = Boolean.parseBoolean(param[0]);
		Boolean branch = Boolean.parseBoolean(param[1]);
		Boolean office = Boolean.parseBoolean(param[2]);
				
		if(goods){
			String query = "";
			query = "SELECT count(*) total FROM Goods;";
			final HashMap<String, String> map = new HashMap<String, String>();
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
						for(int i=0; i < results.length(); i++){
							try{			
								JSONObject json = results.getJSONObject(i);				
								HashMap<String, String> map = JsonHelper.toStringHashMap(json);							
								mfillMaps.add(map);								
							}catch(JSONException e ){
								e.printStackTrace();
							}
						}		
						
						map.putAll(mfillMaps.get(0));
						String total = map.get("total");
						if(Integer.parseInt(total) > 1000 ){
							int page = Integer.parseInt(total)/1000;				
							for( int j=0; j <= page; j++){
								String query = " SELECT TOP 1000 * FROM Goods "
										+ " WHERE  BarCode NOT IN( "
										+ " SELECT TOP "+String.valueOf(j*1000)+" BarCode FROM Goods " 
										+ " Order By BarCode ASC) Order By BarCode ASC ";								
								startDbcheck(query);
							}				
						}else{
							String query = "select * from goods";
							startDbcheck(query);				
						}
						
						
					} else {
						Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
					}
				}
		    }).execute(SHOP_IP+":"+SHOP_PORT, "TIPS", "sa", "tips", query);
			
		}
		
		if(goods){
			String query = "";
			query = "Select TOP 1000 * from goods;";
			
			//startDbcheck(query);
		}
		
		if(branch){
			String query = "";
			query = "SELECT * FROM L_Branch;";
						
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
						
					} else {
						Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
					}
				}
		    }).execute(SHOP_IP+":"+SHOP_PORT, "TIPS", "sa", "tips", query);		
		    
		    query = "SELECT * FROM M_Branch;";
		    
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
						
					} else {
						Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
					}
				}
		    }).execute(SHOP_IP+":"+SHOP_PORT, "TIPS", "sa", "tips", query);		
		    
		    query = "SELECT * FROM S_Branch;";
		    
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
						
					} else {
						Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
					}
				}
		    }).execute(SHOP_IP+":"++, "TIPS", "sa", "tips", query);
		}
		
		if(office){
			String query = "";
			query = "SELECT * FROM Office_Manage;";
		
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
						
					} else {
						Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
					}
				}
		    }).execute(SHOP_IP+":"+SHOP_PORT, "TIPS", "sa", "tips", query);		
		}		
	}
	*/
    /*
     * 2015-02-05
     * 김현수
     * 자체 db생성 파일 구상중 시간 딜레이 문제로 인해 보류함
     *
     */
	/*public void dbsearchStart(View view){
		SQLiteDatabase db = dba.getSQLiteDatabase("read");
		Cursor cursor = db.rawQuery("select * from goods", null);
	        
	        while(cursor.moveToNext()){
	        	Log.i("테이블명", cursor.getString(0));
	        	Log.i("테이블명", cursor.getString(1));
	        }
		
		JSONArray m_list = LocalStorage.getJSONArray( ConfigActivity.this, "Goods" );
		
		Log.i("리스트 사이즈", String.valueOf(m_list.toString()));
		
		for(int i=0; i < m_list.length() ; i++){
			JSONObject content = new JSONObject();
			try {
				content = m_list.getJSONObject(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			Log.i("상품 "+i, content.toString());
		}
		File file = new File("Goods.dat");
		try {
			FileInputStream fis = openFileInput("Goods.dat");
			byte[] buffer = new byte[fis.available()];
			fis.read(buffer);
			String readedStr = new String(buffer);
			
			Log.d("ConfigActivity", ""+readedStr.toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//readFile(file);
		
	}*/
	
	

    /*private void readFile(File file){
        int readcount=0;
        if(file!=null&&file.exists()){
            try {
                FileInputStream fis = new FileInputStream(file);
                readcount = (int)file.length();
                byte[] buffer = new byte[readcount];
                fis.read(buffer);
                for(int i=0 ; i<file.length();i++){
                    Log.d("ConfigActivity", ""+buffer[i]);
                }
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.config_detail, menu);
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


    //업데이트후 처리 하기
    private class Version extends AsyncTask<Void, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Void... params) {
            Log.w("Version", "시작합니다. "); //버젼 표시 하기
            // Confirmation of market information in the Google Play Store
            try {
                Document doc = Jsoup
                        .connect(
                                //"https://play.google.com/store/apps/details?id=kr.co.tipos.tips").timeout(5000)
                                "https://play.google.com/store/apps/details?id=kr.co.tipos.tips").userAgent(
                                "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21").timeout(5000)
                        .get();
                Elements Version = doc.select(".content");

                for (Element v : Version) {
                    if (v.attr("itemprop").equals("softwareVersion")) {
                        rtn = v.text();
                        Log.w("Version", rtn); //버젼 표시 하기
                    }
                }
                return rtn;
                //return "";

            } catch (HttpStatusException e) {
                e.printStackTrace();

            } catch (IOException e) {
                e.printStackTrace();

            }
            Log.w("Version", "버전 찾기 종료. "); //버젼 표시 하기
            return "";
        }

        @Override
        protected void onPostExecute(String result) {

            Log.i("onPosteExecute 버전정보 표시", result);
            // Version check the execution application.
            PackageInfo pi = null;
            try {
                pi = getPackageManager().getPackageInfo(getPackageName(), 0);
                //String sMyVersion = getResources().getString(R.string.check_version.);
            } catch (NameNotFoundException e) {
                e.printStackTrace();
            }
            verSion = pi.versionName;
            Log.i("verSion-1", pi.versionName);

            if (!result.equals("")) {
                rtn = result;
                super.onPostExecute(result);
                //매장 정보 띄우기
                officeConfig();
            } else {
                //버전저장하기
                Log.i("현재버전 정보", verSion.toString());
                Toast.makeText(getApplicationContext(), " 버전 정보를 찾지 못했습니다. ", Toast.LENGTH_LONG).show();
            }
        }
    }

    /*
     * 일   자 : 2015-01-25
     * 작성자 : 김현수
     * 내   용 : 데이터 베이스 생성 클래스
     *
     * */
	/*public class DataBaseRenewal extends AsyncTask<String, Integer, Boolean>{

		ProgressBar pro_goods;
		ProgressBar pro_branch;
		ProgressBar pro_office;
		Button btn_ok;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			Toast.makeText(getApplicationContext(), "갱신 시작", Toast.LENGTH_SHORT).show();
			Context mContext = ConfigActivity.this;
			LayoutInflater inflater = (LayoutInflater)mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.activity_databaserenewal, (ViewGroup)findViewById(R.id.layout_database_renewal));
			
			pro_goods = (ProgressBar)layout.findViewById(R.id.progressBar_goods);			
			pro_branch = (ProgressBar)layout.findViewById(R.id.progressBar_branch);
			pro_office = (ProgressBar)layout.findViewById(R.id.progressBar_office);
			btn_ok = (Button)layout.findViewById(R.id.button_ok);
			btn_ok.setEnabled(false);
			
			new AlertDialog.Builder(ConfigActivity.this)
	        .setView(layout)       
	        .show();
		}
				
		@Override
		protected Boolean doInBackground(String... params) {
			// TODO Auto-generated method stub
			final Boolean goods = Boolean.parseBoolean(params[0]);
			final Boolean branch = Boolean.parseBoolean(params[1]);
			final Boolean office = Boolean.parseBoolean(params[2]);
			
			Log.i("MSSQL4"," MSSQL4 Connect Example.");
	    	Connection conn = null;
	    	//JSONArray json = new JSONArray();
	    	ResultSet rs = null;
	    	String query = null;	    	
	    	
	    	if(goods){
	    		query = "Select TOP 1000 * from goods;";
		        Log.e("MSSQL4","query: " + query );
		    	
		    	try {
		    	    Class.forName("net.sourceforge.jtds.jdbc.Driver").newInstance();
		    	    Log.i("MSSQL4","MSSQL4 driver load");
	
		    	    conn = DriverManager.getConnection("jdbc:jtds:sqlserver://" +SHOP_IP+":"+SHOP_PORT+"/"+ "tips", "sa", "tips");
		    	    Log.i("MSSQL4","MSSQL4 open");
		    	    Statement stmt = conn.createStatement();
		    	    
		        	//ResultSet rs =null;
		            rs = stmt.executeQuery(query);	            
		        	//json = ResultSetConverter.convert(rs);		    	  
		            //dba.insert_goods(rs);
		    	    conn.close();
		    	    
		    	    return true;
		   	 	 } catch (SQLException e) {
		    	    Log.w("Error connection","" + e.getMessage());	
		    	 } catch (Exception e) {
		    	    Log.w("Error connection","" + e.getMessage());		   
		    	 }
	    	}
	    	
			return false;
		}
		
		
		@Override
		protected void onProgressUpdate(Integer... progress){
			
		}
		
		@Override
		protected void onPostExecute(Boolean result){
			super.onPostExecute(result);
			if(result){			
				Log.i("DB insert", "완료");
			}else{
				Log.i("DB insert", "실패");
			}			
		}		
		
	}*/

}
