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
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;
import android.view.inputmethod.EditorInfo;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

//import java.io.File;
//import java.io.FileInputStream;
//import java.io.FileNotFoundException;
//import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.MSSQL_TR;
import tipsystem.utils.UserPublicUtils;

public class ManageStockActivity extends Activity implements OnItemSelectedListener, OnDateSetListener {

    private static final int ZBAR_SCANNER_REQUEST = 0;
    private static final int BARCODE_MANAGER_REQUEST = 2;
    private static final String TAG = "????????????";

    UserPublicUtils upu;

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

    /* ????????? 2020.09.14. ????????? ?????? ????????? ?????? ?????? ?????? */
    int m_KeypadType = 0;

    Context mContext;

    int m_junpyoInTIdx = 0;
    int m_Tran_Seq = 0;

    DatePicker m_datePicker;
    SimpleDateFormat m_dateFormatter;
    Calendar m_dateCalender1;
    SimpleAdapter adapter;

    int m_selectedListIndex = -1;

    List<HashMap<String, String>> m_stockList = null;

    String[] from = new String[]{"BarCode", "G_Name", "St_Count", "Real_Sto"};
    int[] to = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4};
    List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> m_tempProduct = new HashMap<String, String>();

    Button m_period;
    ListView m_listStock;
    EditText m_textBarcode;
    EditText m_textProductName;
    EditText m_et_purchasePrice;
    EditText m_et_salePrice;
    EditText m_et_numOfReal;  //????????????
    EditText m_et_curStock;   //?????????
    Button m_bt_barcodeSearch;
    private ProgressDialog dialog;

    //?????? ??????
    //File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_stock);

        mContext = this;
        //????????? ??????
        upu = new UserPublicUtils(mContext);

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

            /* ????????? 2020.09.14. ????????? ?????? ????????? ?????? ?????? ?????? */
            m_KeypadType = LocalStorage.getInt(this, "KeypadType:" + OFFICE_CODE);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        m_listStock = (ListView) findViewById(R.id.listviewReadyToSendEventList);

        Button btn_Register = (Button) findViewById(R.id.buttonRegist);
        Button btn_Renew = (Button) findViewById(R.id.buttonRenew);
        Button btn_Send = (Button) findViewById(R.id.buttonSend);
        Button btn_SendAll = (Button) findViewById(R.id.buttonSendAll);

        m_period = (Button) findViewById(R.id.buttonSetDate1);

        m_listStock.setOnItemClickListener(mItemClickListener);
        m_listStock.setFocusableInTouchMode(true);
        m_listStock.setFocusable(true);

        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        m_dateCalender1 = Calendar.getInstance();
        m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

        m_textBarcode = (EditText) findViewById(R.id.editTextBarcode);

        // ????????? 2020.09.14. ??????????????? ?????? ????????? ?????? ?????? ??????
        if (m_KeypadType == 0) { // ???????????????
            m_textBarcode.setInputType(2);
        }
        if (m_KeypadType == 1) { // ???????????????
            m_textBarcode.setInputType(145); //textVisiblePassword ) 0x00000091
        }

        // ????????? ?????? ??? ????????? ??? ?????? ?????? ??????
        m_textBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            //@Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String barcode = null;
                    barcode = m_textBarcode.getText().toString();
                    if (!barcode.equals("")) // ????????? Barcode??? ?????? ?????? ?????????
                        doQueryWithBarcode();
                }
            }
        });

        m_textBarcode.setOnKeyListener(new OnKeyListener() {

            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub

                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    m_et_curStock.requestFocus();
                }
                return false;
            }
        });

        m_textProductName = (EditText) findViewById(R.id.editTextProductName);
        m_et_purchasePrice = (EditText) findViewById(R.id.editTextPurchasePrice);
        m_et_salePrice = (EditText) findViewById(R.id.editTextSalePrice);


        //2020-06-15
        m_et_numOfReal = (EditText) findViewById(R.id.editTextNumberOfReal);
        m_et_numOfReal.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (hasFocus) {

                    upu.setChangeBackGroundColor(1,  m_et_numOfReal);

                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                    //2019-11-14 toggleSoftInput ??? ??????
                    // ???????????? ?????????.
                    //imm.showSoftInput(getCurrentFocus(), 0);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

                    /*
                    InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                    if (imm.isAcceptingText()) {
                        imm.showSoftInput(getCurrentFocus(), InputMethodManager.SHOW_FORCED);
                    }
                     */

                }else{
                    upu.setChangeBackGroundColor(0, m_et_numOfReal);
                }
            }
        });

        m_et_numOfReal.setImeOptions(EditorInfo.IME_ACTION_DONE);
        //?????????????????? ???????????? ?????? ??? ???????????? ?????? ?????????
        m_et_numOfReal.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                //?????? ????????? ???????????????.
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    Stock_Reg();
                }

                return false;
            }
        });

        m_et_curStock = (EditText) findViewById(R.id.editTextCurStock);
        m_bt_barcodeSearch = (Button) findViewById(R.id.buttonBarcode);

        m_stockList = new ArrayList<HashMap<String, String>>();

        adapter = new SimpleAdapter(this, fillMaps, R.layout.activity_listview_item4, from, to);
        m_listStock.setAdapter(adapter);

        btn_Register.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                /**
                 //2019-04-14 ?????? ??????????????? ??????
                 //if (checkRedundancyData()==false) return;
                 setDataIntoList();
                 changeInputMode(1);
                 clearInputBox();
                 m_textBarcode.requestFocus();
                 **/

                Stock_Reg();

            }
        });

        btn_Renew.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                changeInputMode(0);
                clearInputBox();
            }
        });

        btn_Send.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // ?????? ?????? ???????????? ??????

                //?????? ?????? ?????? ?????? ??????
                if (m_stockList.size() <= 0) {
                    upu.showDialog("????????? ????????? ????????????.", 0);
                    return;
                }

                //--------------------//
                // 2022.09.28.????????? ????????? ??????
                //--------------------//
                //deleteListAll();
                //clearInputBox();

                AlertDialog.Builder dialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT)
                        .setTitle("????????????")
                        .setMessage("?????? ????????? ?????? ???????????????????");
                dialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteListAll();
                        clearInputBox();

                    }
                });
                dialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                dialog.show();
                //--------------------//

            }
        });

        btn_SendAll.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                //?????? ?????? ?????? ?????? ??????
                if (m_stockList.size() <= 0) {
                    upu.showDialog("?????? ????????? ????????? ????????????.", 0);
                    return;
                }

                // ?????? ???????????? ?????????????????? ????????????
                getMaxTranSeq();

                //sendSelectedAllData();
                //clearInputBox();

            }
        });


        changeInputMode(0);
        getInTSeq();

        //?????? ?????? ??????
        //????????????
        //String savePath = Environment.getExternalStorageDirectory() + "/Handy/stockData";
        String savePath = Environment.getExternalStorageDirectory() + "/Handy/";

        // ?????? ????????? msec?????? ?????????.
        long now = System.currentTimeMillis();
        // ?????? ????????? ?????? ??????.
        Date date = new Date(now);
        // ?????? ???????????? ?????????.
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMddHH");
        String strNow = sdfNow.format(date);

        //????????????
        String saveFile = "stockData" + strNow + "_Jego.txt";

        //?????? ??????
        //File dir = makeDirectory(savePath);

        //?????? ??????
        //file = makeFile(dir, (savePath + saveFile));

        //?????? ??????
        //Log.i(TAG, "" + getAbsolutePath(dir));
        //Log.i(TAG, "" + getAbsolutePath(file));

        Button b_newproductreg = findViewById(R.id.button_newproductreg);
        b_newproductreg.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                regNewProduct("");
            }
        });

    }

    //????????? ??????
    public void regNewProduct(String barcode) {
        String m_barcode = "";
        if (!"".equals(barcode) || barcode != null) {
            m_barcode = barcode;
        }

        Intent intent = new Intent(this, ManageProductActivityModfiy.class);
        intent.putExtra("barcode", m_barcode);
        startActivity(intent);
    }

    public String makeJunPyo() {

        String period = m_period.getText().toString();
        int year = Integer.parseInt(period.substring(0, 4));
        int month = Integer.parseInt(period.substring(5, 7));
        int day = Integer.parseInt(period.substring(8, 10));

        // ???????????? ?????? 
        String posID = "P";
        try {
            String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
            posID = LocalStorage.getString(this, "currentPosID:" + OFFICE_CODE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String junpyo = String.format("%04d%02d%02d", year, month, day) + posID + String.format("%03d", m_junpyoInTIdx);
        return junpyo;
    }

    public void deleteData() {

        if (m_selectedListIndex == -1) {
            Toast.makeText(ManageStockActivity.this, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
            return;
        }

        fillMaps.remove(m_selectedListIndex);
        adapter.notifyDataSetChanged();
        m_stockList.remove(m_selectedListIndex);
        m_selectedListIndex = -1;
    }

    public void deleteListAll() {
        if (fillMaps.isEmpty()) return;

        fillMaps.removeAll(fillMaps);
        adapter.notifyDataSetChanged();
        m_stockList.removeAll(m_stockList);
        m_selectedListIndex = -1;
    }

    public void clearInputBox() {

        m_selectedListIndex = -1;
        m_textBarcode.setText("");
        m_textProductName.setText("");
        m_et_purchasePrice.setText("");
        m_et_salePrice.setText("");
        m_et_numOfReal.setText("");
        m_et_curStock.setText("");

        m_textBarcode.requestFocus();
    }

    public void changeInputMode(int mode) {
        if (mode == 0) {
            m_textBarcode.setEnabled(true);
            m_period.setEnabled(true);
            m_bt_barcodeSearch.setEnabled(true);
        } else {
            m_period.setEnabled(false);
            m_textBarcode.setEnabled(true);
            m_bt_barcodeSearch.setEnabled(true);
        }
    }

    public boolean checkRedundancyData() {
        String barcode = m_textBarcode.getText().toString();
        Iterator<HashMap<String, String>> iterator = m_stockList.iterator();
        while (iterator.hasNext()) {
            HashMap<String, String> element = iterator.next();
            String barcodeinList = element.get("BarCode");
            if (barcodeinList.equals(barcode)) {
                Toast.makeText(ManageStockActivity.this, "?????? ???????????? ?????? ??????????????????.", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    //?????????
    public List<HashMap<String, String>> makeFillvapsWithStockList() {
        List<HashMap<String, String>> fm = new ArrayList<HashMap<String, String>>();

        Iterator<HashMap<String, String>> iterator = m_stockList.iterator();
        while (iterator.hasNext()) {
            boolean isNew = true;
            HashMap<String, String> element = iterator.next();
            String barcode = element.get("BarCode");

            Iterator<HashMap<String, String>> fm_iterator = fm.iterator();
            while (fm_iterator.hasNext()) {

                HashMap<String, String> fm_element = fm_iterator.next();
                String fm_barcode = fm_element.get("BarCode");

                if (fm_barcode.equals(barcode)) {
                    isNew = false;
                    // ????????? ????????? fm_element??? ??????
                    String fm_numOfReal = fm_element.get("Real_Sto");
                    String fm_curStock = fm_element.get("St_Count");
                    String numOfReal = element.get("Real_Sto");
                    String curStock = element.get("St_Count");

                    fm_element.put("Real_Sto", String.valueOf(Integer.valueOf(fm_numOfReal) + Integer.valueOf(numOfReal)));
                    fm_element.put("St_Count", String.valueOf(Integer.valueOf(fm_curStock) + Integer.valueOf(curStock)));
                }
            }

            if (isNew) {
                String productName = element.get("G_Name");
                String numOfReal = element.get("Real_Sto");
                String curStock = element.get("St_Count");
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("BarCode", barcode);
                map.put("G_Name", productName);
                map.put("Real_Sto", numOfReal);
                map.put("St_Count", curStock);

                fm.add(map);
            }
        }

        return fm;
    }

    //2020-06-15 ?????? ?????? ??????
    public void Stock_Reg() {
        //if (checkRedundancyData()==false) return;

        setDataIntoList();
        changeInputMode(1);
        clearInputBox();

        m_textBarcode.requestFocus();
    }


    public void setDataIntoList() {
        NumberFormat f = NumberFormat.getInstance();
        f.setGroupingUsed(false);

        String period = m_period.getText().toString();
        String barcode = m_textBarcode.getText().toString();
        String productName = m_textProductName.getText().toString();
        String purchasePrice = m_et_purchasePrice.getText().toString();
        String salePrice = m_et_salePrice.getText().toString();
        String numOfReal = m_et_numOfReal.getText().toString();
        String curStock = m_et_curStock.getText().toString();

        if (barcode.equals("") || productName.equals("") || purchasePrice.equals("") || salePrice.equals("") || numOfReal.equals("") || numOfReal.equals("")) {
            Toast.makeText(ManageStockActivity.this, "???????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
            return;
        }

        //????????? ???????????? ???????????? ?????????
        String Bus_Code = m_tempProduct.get("Bus_Code");        // ????????????
        String Pur_Pri = m_tempProduct.get("Pur_Pri");        // ????????????
        String Sell_Pri = m_tempProduct.get("Sell_Pri");    // ????????????
        String Pur_Cost = m_tempProduct.get("Pur_Cost");    // ????????????
        String Add_Tax = m_tempProduct.get("Add_Tax");    // ?????????
        String Bot_Pur = m_tempProduct.get("Bot_Pur");    // ???????????????
        String Bot_Sell = m_tempProduct.get("Bot_Sell");    // ???????????????
        String Tax_YN = m_tempProduct.get("Tax_YN");        // ????????????(0:??????,1:??????)
        String Tax_Gubun = m_tempProduct.get("VAT_CHK");    // ???????????????(0:??????,1:??????)
        String In_YN = m_tempProduct.get("In_YN");    // ???????????????(0:??????,1:??????)
        String Box_Use = m_tempProduct.get("Box_Use");    //
        String Pack_Use = m_tempProduct.get("Pack_Use");    //
        String Bot_Code = m_tempProduct.get("Bot_Code");    //
        String Bot_Name = m_tempProduct.get("Bot_Name");    //
        String Real_Sto = m_tempProduct.get("Real_Sto");    //

        if (Bot_Pur == null) Bot_Pur = "0";
        if (Bot_Sell == null) Bot_Sell = "0";

        double In_Pri = (Double.valueOf(purchasePrice) + Double.valueOf(Bot_Pur)) * Double.valueOf(numOfReal);    //??? ?????????(????????????)=?????????x??????
        double In_SellPri = (Double.valueOf(salePrice) + Double.valueOf(Bot_Sell)) * Double.valueOf(numOfReal);    //?????????x??????

        HashMap<String, String> rmap = new HashMap<String, String>();
        //rmap.put("St_Num", period);
        //rmap.put("St_BarCode", barcode);

        rmap.put("G_Name", productName);
        rmap.put("St_Gubun", "2");        //????????????(0:??????,1:??????,2:PDA,3:??????)
        rmap.put("Set_Gubun", "0");
        rmap.put("St_Date", period);
        rmap.put("Office_Code", Bus_Code);
        rmap.put("BarCode", barcode);
        //rmap.put("St_Seq", "");
        rmap.put("St_Count", numOfReal);
        rmap.put("Tax_YN", Tax_YN);
        rmap.put("Tax_Gubun", Tax_Gubun);
        rmap.put("Bot_Code", Bot_Code);
        rmap.put("Bot_Pur", Bot_Pur);
        rmap.put("Bot_Sell", Bot_Sell);
        rmap.put("Pur_Pri", Pur_Pri);

        double f_tax = Double.valueOf(Pur_Pri) * 0.1;
        if (Tax_YN.equals("1") && Tax_Gubun.equals("0")) {
            rmap.put("Pur_Cost", Pur_Pri);
            rmap.put("Add_Tax", String.valueOf(f_tax));

            //rmap.put("TPur_Pri", String.valueOf((Double.valueOf(Pur_Pri) + f_tax) * Double.valueOf(numOfReal)));        //??? ?????????(????????????)=?????????x??????
            rmap.put("TPur_Pri", String.valueOf( f.format( (Double.valueOf(Pur_Pri) + f_tax) * Double.valueOf(numOfReal) ) ));        //??? ?????????(????????????)=?????????x??????
            rmap.put("TPur_Cost", String.valueOf( f.format(Double.valueOf(Pur_Pri) * Double.valueOf(numOfReal) ) ));            //????????????x??????
            rmap.put("TAdd_Tax", String.valueOf( f.format(f_tax * Double.valueOf(numOfReal) ) ));            //?????????x??????
            rmap.put("In_Pri", String.valueOf( f.format((Double.valueOf(Pur_Pri) + Double.valueOf(Bot_Pur) + f_tax) * Double.valueOf(numOfReal) ) ));        //??? ?????????(????????????)=?????????x??????
        } else {
            rmap.put("Pur_Cost", Pur_Cost);
            rmap.put("Add_Tax", Add_Tax);
            //rmap.put("TPur_Pri", String.valueOf((Double.valueOf(purchasePrice)-Double.valueOf(Bot_Pur))*Double.valueOf(numOfReal)));		//??? ?????????(????????????)=?????????x??????
            //rmap.put("TPur_Pri", String.valueOf((Double.valueOf(purchasePrice)) * Double.valueOf(numOfReal)));        //??? ?????????(????????????)=?????????x??????
            rmap.put("TPur_Pri", String.valueOf(f.format( (Double.valueOf(purchasePrice)) * Double.valueOf(numOfReal) ) ));        //??? ?????????(????????????)=?????????x??????

            rmap.put("TPur_Cost", String.valueOf( f.format(Double.valueOf(Pur_Cost) * Double.valueOf(numOfReal) ) ));            //????????????x??????
            rmap.put("TAdd_Tax", String.valueOf( f.format(Double.valueOf(Add_Tax) * Double.valueOf(numOfReal) ) ));            //?????????x??????
            rmap.put("In_Pri", String.valueOf(f.format(In_Pri)));        //??? ?????????(????????????)=?????????x??????
        }

        rmap.put("Sell_Pri", Sell_Pri);
        //rmap.put("TSell_Pri", String.valueOf((Double.valueOf(salePrice)-Double.valueOf(Bot_Sell))*Double.valueOf(numOfReal)));	//??? ?????????(????????????)=?????????x??????
        rmap.put("TSell_Pri", String.valueOf( f.format((Double.valueOf(salePrice)) * Double.valueOf(numOfReal) ) ));    //??? ?????????(????????????)=?????????x??????
        rmap.put("In_SellPri", String.valueOf( f.format(In_SellPri) ));    //?????????x??????

        rmap.put("Bot_Pri", String.valueOf(f.format(Double.valueOf(Bot_Pur) * Double.valueOf(numOfReal)) ));
        rmap.put("Bot_SellPri", String.valueOf(f.format(Double.valueOf(Bot_Sell) * Double.valueOf(numOfReal) ) ));

        rmap.put("Bot_Code", Bot_Code);    //
        rmap.put("Bot_Name", Bot_Name);    //
        //rmap.put("Write_Date", currentDate);
        //rmap.put("Edit_Date", currentDate);
        //rmap.put("Writer", userid);
        //rmap.put("Editor", userid);
        rmap.put("oReal_Sto", Real_Sto);
        rmap.put("Real_Sto", numOfReal);

        m_stockList.add(rmap);

        //ListView ??? ?????????
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("BarCode", barcode);
        map.put("G_Name", productName);

        //2020-06-15
        //map.put("Real_Sto", numOfReal);
        //map.put("St_Count", curStock);
        map.put("St_Count", numOfReal);
        map.put("Real_Sto", curStock);

        fillMaps.add(map);


        // ?????? ????????? msec?????? ?????????.
        long now = System.currentTimeMillis();
        // ?????? ????????? ?????? ??????.
        Date date = new Date(now);
        // ?????? ???????????? ?????????.
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String strNow = sdfNow.format(date);

        //?????? ??????
        //String content = map.toString() + "   " + strNow + " \r\n";
        //writeFile(file, content.getBytes());

        adapter.notifyDataSetChanged();
    }

    public void getMaxTranSeq() {
        //--------------------//
        // 2022.09.28. ?????? ??? ??????
        //--------------------//

        //--------------------//
        String query = "";
        query = " Select  ";
        query += " IsNull(MAX(TRAN_SEQ),0)+1 AS MAX_TRAN_SEQ FROM Temp_StDPDA_HISTORY";

        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                try {
                    HashMap<String, String> a = JsonHelper.toStringHashMap(results.getJSONObject(0));
                    //max_seq[0] = a.get("MAX_SEQ");
                    m_Tran_Seq = Integer.parseInt(a.get("MAX_TRAN_SEQ"));

                    sendSelectedAllData();
                    clearInputBox();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    // MSSQL
    // StT, StD ???????????? ??????
    public void sendSelectedAllData() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());

        JSONObject userProfile = LocalStorage.getJSONObject(this, "userProfile");
        String userid = "";
        try {
            userid = userProfile.getString("User_ID");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String tableName = null, sttTableName = null;
        String period = m_period.getText().toString();

        int year = Integer.parseInt(period.substring(0, 4));
        int month = Integer.parseInt(period.substring(5, 7));

        tableName = String.format("StD_%04d%02d", year, month);
        sttTableName = String.format("StT_%04d%02d", year, month);

        //TODO: ????????? ????????????
        String junpyo = makeJunPyo();

        double TTPur_Pri = 0, TTAdd_Tax = 0, TFPur_Pri = 0, In_TPri = 0, In_FPri = 0, In_Pri = 0, In_RePri = 0;
        double TSell_Pri = 0, In_SellPri = 0, Bot_Pri = 0, Bot_SellPri = 0, Profit_Pri = 0, Profit_Rate = 0;

        //----------------------------------------//
        // 2021.07.09.?????????. ????????? Temp_StDPDA_HISTORY Insert ??????
        // ?????? MAX(TRAN_SEQ) ????????????

        // ?????? ????????????
        String query = "";
        for (int i = 0, seq = 1; i < m_stockList.size(); i++, seq++) {

            HashMap<String, String> stock = m_stockList.get(i);

            query += "insert into Temp_StDPDA_HISTORY (";
            query += " St_Num" +
                    ", St_BarCode" +
                    ", St_Gubun" +
                    ", St_Date" +
                    ", Office_Code" +
                    ", Office_Name" +
                    ", BarCode" +
                    ", St_Seq" +
                    ", St_Count" +
                    ", Tax_YN" +
                    ", Tax_Gubun" +
                    ", Bot_Code" +
                    ", Bot_Pur" +
                    ", Bot_Sell" +
                    ", Pur_Pri" +
                    ", Pur_Cost" +
                    ", Add_Tax" +
                    ", TPur_Pri" +
                    ", TPur_Cost" +
                    ", TAdd_Tax" +
                    ", In_Pri" +
                    ", Sell_Pri" +
                    ", TSell_Pri" +
                    ", In_SellPri" +
                    ", Bot_Pri" +
                    ", Bot_SellPri" +
                    ", Write_Date" +
                    ", Edit_Date" +
                    ", Writer" +
                    ", Editor" +

                    //--------------------//
                    // 2022.09.28. ???????????? ??????
                    //--------------------//
                    // oReal_STO ??????
                    ", oReal_STO" +
                    //--------------------//

                    ", tSt_Num " +
                    ", PDA_TRAN_DATE " +
                    ", TRAN_SEQ ) " +
                    " values ( "
                    + "'" + junpyo + "', "
                    + "'" + junpyo + "', "
                    + "'" + stock.get("St_Gubun").toString() + "', "
                    + "'" + stock.get("St_Date").toString() + "', "
                    + "'" + stock.get("Office_Code").toString() + "', "
                    + "'" + "" + "', "
                    + "'" + stock.get("BarCode").toString() + "', "
                    + "'" + seq + "', "
                    + "'" + stock.get("St_Count").toString() + "', "
                    + "'" + stock.get("Tax_YN").toString() + "', "
                    + "'" + stock.get("Tax_Gubun").toString() + "', "
                    + "'" + stock.get("Bot_Code").toString() + "', "
                    + "'" + stock.get("Bot_Pur").toString() + "', "
                    + "'" + stock.get("Bot_Sell").toString() + "', "
                    + "'" + stock.get("Pur_Pri").toString() + "', "
                    + "'" + stock.get("Pur_Cost").toString() + "', "
                    + "'" + stock.get("Add_Tax").toString() + "', "

                    + "'" + stock.get("TPur_Pri").toString() + "', "
                    //+ "'" + f.format(stock.get("TPur_Pri")) + "', "

                    + "'" + stock.get("TPur_Cost").toString() + "', "
                    + "'" + stock.get("TAdd_Tax").toString() + "', "

                    + "'" + stock.get("In_Pri").toString() + "', "
                    //+ "'" + f.format(stock.get("In_Pri")) + "', "

                    + "'" + stock.get("Sell_Pri").toString() + "', "

                    + "'" + stock.get("TSell_Pri").toString() + "', "
                    + "'" + stock.get("In_SellPri").toString() + "', "
                    //+ "'" + f.format(stock.get("TSell_Pri")) + "', "
                    //+ "'" + f.format(stock.get("In_SellPri")) + "', "

                    + "'" + stock.get("Bot_Pri").toString() + "', "
                    + "'" + stock.get("Bot_SellPri").toString() + "', "
                    + "'" + currentDate + "', "
                    + "'" + currentDate + "', "
                    + "'" + userid + "', "
                    + "'" + userid + "', "
                    + "'" + stock.get("oReal_Sto").toString() + "',"
                    + "'" + junpyo + "', "
                    + "'" + currentDate + "', "
                    + "'" + m_Tran_Seq + "');";

            query += "insert into " + tableName + "(St_Num, St_BarCode, St_Gubun, Set_Gubun, St_Date, Office_Code, BarCode, St_Seq, St_Count,"
                    + "Tax_YN, Tax_Gubun, Bot_Code, Bot_Pur, Bot_Sell, Pur_Pri, Pur_Cost, Add_Tax, TPur_Pri, TPur_Cost, TAdd_Tax, In_Pri,"
                    + "Sell_Pri, TSell_Pri, In_SellPri, Bot_Pri, Bot_SellPri, Write_Date, Edit_Date, Writer, Editor, oReal_Sto) "
                    + " values ("
                    + "'" + junpyo + "', "
                    + "'" + junpyo + "', "
                    + "'" + stock.get("St_Gubun").toString() + "', "
                    + "'" + stock.get("Set_Gubun").toString() + "', "
                    + "'" + stock.get("St_Date").toString() + "', "
                    + "'" + stock.get("Office_Code").toString() + "', "
                    + "'" + stock.get("BarCode").toString() + "', "
                    + "'" + seq + "', "
                    + "'" + stock.get("St_Count").toString() + "', "
                    + "'" + stock.get("Tax_YN").toString() + "', "
                    + "'" + stock.get("Tax_Gubun").toString() + "', "
                    + "'" + stock.get("Bot_Code").toString() + "', "
                    + "'" + stock.get("Bot_Pur").toString() + "', "
                    + "'" + stock.get("Bot_Sell").toString() + "', "
                    + "'" + stock.get("Pur_Pri").toString() + "', "
                    + "'" + stock.get("Pur_Cost").toString() + "', "
                    + "'" + stock.get("Add_Tax").toString() + "', "
                    + "'" + stock.get("TPur_Pri").toString() + "', "
                    + "'" + stock.get("TPur_Cost").toString() + "', "
                    + "'" + stock.get("TAdd_Tax").toString() + "', "
                    + "'" + stock.get("In_Pri").toString() + "', "
                    + "'" + stock.get("Sell_Pri").toString() + "', "
                    + "'" + stock.get("TSell_Pri").toString() + "', "
                    + "'" + stock.get("In_SellPri").toString() + "', "
                    + "'" + stock.get("Bot_Pri").toString() + "', "
                    + "'" + stock.get("Bot_SellPri").toString() + "', "
                    + "'" + currentDate + "', "
                    + "'" + currentDate + "', "
                    + "'" + userid + "', "
                    + "'" + userid + "', "
                    + "'" + stock.get("oReal_Sto").toString() + "');";

            // InT??? ????????? ????????????
            String Tax_YN = stock.get("Tax_YN"); //????????????(0:??????,1:??????)
            //String In_YN = stock.get("In_YN");	//????????????(0:??????,1:??????,2:????????????,3:????????????)

            TTAdd_Tax += Double.valueOf(stock.get("Add_Tax"));    //??? ?????? ?????????
            if (Tax_YN.equals("1"))
                TTPur_Pri += Double.valueOf(stock.get("TPur_Pri"));    //??? ???????????????(????????????)
            if (Tax_YN.equals("0"))
                TFPur_Pri += Double.valueOf(stock.get("TPur_Pri"));    //??? ???????????????(????????????)
            if (Tax_YN.equals("1"))
                In_TPri += Double.valueOf(stock.get("In_Pri"));        //??? ???????????????(????????????)
            if (Tax_YN.equals("0"))
                In_FPri += Double.valueOf(stock.get("In_Pri"));        //??? ???????????????(????????????)

            //----------------------------------------//
            // 2022.09.28. ???????????? ?????? ???????????? ??????
            //----------------------------------------//
            //else In_Pri += Double.valueOf(stock.get("In_Pri"));        //??? ?????????(????????????)
            In_Pri += Double.valueOf(stock.get("In_Pri"));        //??? ?????????(????????????)
            //----------------------------------------//

            TSell_Pri += Double.valueOf(stock.get("TSell_Pri"));    //??? ?????????(????????????)
            In_SellPri += Double.valueOf(stock.get("In_SellPri"));    //??? ?????????(????????????)
            Bot_Pri += Double.valueOf(stock.get("Bot_Pur"));        //?????? ??? ?????????
            Bot_SellPri += Double.valueOf(stock.get("Bot_Sell"));    //?????? ??? ?????????
        }
        //----------------------------------------//
        NumberFormat f = NumberFormat.getInstance();
        f.setGroupingUsed(false);

        query += "insert into " + sttTableName + " (St_Num, St_Date, St_Seq, StSet_Num, TTPur_Pri, TTAdd_Tax, TFPur_Pri, In_TPri, In_FPri, In_Pri, "
                + "TSell_Pri, In_SellPri, Bot_Pri, Bot_SellPri, St_Pri, Bigo, Write_Date, Edit_Date, Writer, Editor ) "
                + " values ("
                + "'" + junpyo + "', "

                //----------------------------------------//
                // 2021.07.09.?????????. ???????????? ????????? ???????????? ?????? ?????? ??????
                //----------------------------------------//
                //+ "'" + currentDate + "', "
                + "'" + period + "', "
                //----------------------------------------//

                + "'" + m_junpyoInTIdx + "', "
                + "'', "
                + "'" + f.format( TTPur_Pri )+ "', "
                + "'" + f.format(TTAdd_Tax) + "', "
                + "'" + f.format(TFPur_Pri) + "', "
                + "'" + f.format(In_TPri) + "', "
                + "'" + f.format(In_FPri) + "', "
                + "'" + f.format(In_Pri) + "', "
                + "'" + f.format(TSell_Pri) + "', "
                + "'" + f.format(In_SellPri) + "', "
                + "'" + f.format(Bot_Pri) + "', "
                + "'" + f.format(Bot_SellPri) + "', "
                + "'" + f.format(In_Pri) + "', "
                + "'', "
                + "'" + currentDate + "', "
                + "'" + currentDate + "', "
                + "'" + userid + "', "
                + "'" + userid + "');";

        query += " select * from " + tableName + " where St_Num='" + junpyo + "';";

        m_junpyoInTIdx++;

        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // ?????? ?????? ?????????
        new MSSQL_TR(new MSSQL_TR.MSSQLCallbackInterface() {
            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                deleteListAll();
                clearInputBox();
                Toast.makeText(ManageStockActivity.this, "????????????.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(ManageStockActivity.this, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

        /*
        // ??????????????? ?????? ??????
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();

                // ????????????(???????????? ??? ????????????)
                if (results.length() > 0) {
                    deleteListAll();
                    clearInputBox();
                    Toast.makeText(ManageStockActivity.this, "????????????.", Toast.LENGTH_SHORT).show();
                }else{
                    //?????? ??????
                    Toast.makeText(ManageStockActivity.this, "????????? ?????????????????????.", Toast.LENGTH_SHORT).show();
                    //tableName = "";
                    //sttTableName = "";
                    //junpyo = "";

                }

            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
         */

    }

    public void sendSelectedAllDataRollBack() {

    }

    //??????????????? ??????
    public void getInTSeq() {

        String tableName = null;
        String period = m_period.getText().toString();

        int year = Integer.parseInt(period.substring(0, 4));
        int month = Integer.parseInt(period.substring(5, 7));

        tableName = String.format("StT_%04d%02d", year, month);

        // ?????? ????????????
        String query = "";

        //query += "SELECT TOP 1 St_Seq, St_Num FROM " + tableName + " WHERE St_Date='" + period
        //        + "' AND (St_Seq NOT IN(SELECT TOP 0 St_Seq FROM " + tableName + ")) order by St_Num DESC;";

        String posID = "P";
        try {
            String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
            posID = LocalStorage.getString(this, "currentPosID:" + OFFICE_CODE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        query += "SELECT TOP 1 St_Seq, St_Num FROM " + tableName + " WHERE St_Date='" + period
                + "' AND Substring(St_num,9,1) = '" + posID + "' "
                + " order by St_Num DESC;";

        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // ??????????????? ?????? ??????
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                if (results.length() > 0) {
                    try {
                        m_junpyoInTIdx = results.getJSONObject(0).getInt("St_Seq") + 1;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    m_junpyoInTIdx = 1;
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "??????(" + String.valueOf(code) + "):" + msg, Toast.LENGTH_SHORT).show();
            }

        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    // SQL QUERY ??????
    public void doQueryWithBarcode() {

        String query = "";
        String barcode = m_textBarcode.getText().toString();

        query = "SELECT * FROM Goods WHERE Barcode = '" + barcode + "';";

        if (barcode.equals("")) return;

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
                        JSONObject object = results.getJSONObject(0);
                        m_tempProduct = JsonHelper.toStringHashMap(object);
                        String Pack_Use = m_tempProduct.get("Pack_Use");
                        String Box_Use = m_tempProduct.get("Box_Use");
                        String BarCode1 = m_tempProduct.get("BarCode1");
                        if (Pack_Use.equals("1")) {
                            m_textBarcode.setText("");
                            Toast.makeText(ManageStockActivity.this, "??????????????? ????????? ??????????????????", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (Box_Use.equals("1")) {
                            m_textBarcode.setText(BarCode1);
                            Toast.makeText(ManageStockActivity.this, "?????????????????????. ?????????????????? ???????????????.", Toast.LENGTH_SHORT).show();
                            doQueryWithBarcode();
                            return;
                        }

                        m_textProductName.setText(results.getJSONObject(0).getString("G_Name"));
                        m_et_purchasePrice.setText(results.getJSONObject(0).getString("Pur_Pri"));
                        m_et_salePrice.setText(results.getJSONObject(0).getString("Sell_Pri"));
                        //m_et_numOfReal.setText(results.getJSONObject(0).getString("Real_Sto"));
                        m_et_curStock.setText(results.getJSONObject(0).getString("Real_Sto"));

                        //--------------------//
                        // ???????????? ???????????? ?????? ??????
                        //--------------------//
                        m_et_numOfReal.requestFocus();
                        //--------------------//


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    //????????????
                    DialogInterface.OnClickListener newBarcodeListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            String barcode = m_textBarcode.getText().toString();

                            Intent intent = new Intent(ManageStockActivity.this, ManageProductActivity.class);
                            intent.putExtra("barcode", barcode);
                            startActivity(intent);
                        }
                    };

                    DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    };

                    new AlertDialog.Builder(ManageStockActivity.this, AlertDialog.THEME_HOLO_LIGHT)
                            .setTitle("?????????????????? ??????????????????")
                            .setNeutralButton("????????????", newBarcodeListener)
                            .setNegativeButton("??????", cancelListener)
                            .show();
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            // ????????? ????????? ?????? ????????? ??????
            case ZBAR_SCANNER_REQUEST:
                if (resultCode == RESULT_OK) {
                    // Scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT)
                    // Type of the scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
                    Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE), Toast.LENGTH_SHORT).show();
                    // The value of type indicates one of the symbols listed in Advanced Options below.

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
                    doQueryWithBarcode();
                }
                break;
        }
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
        Intent intent = new Intent(this, ManageProductListActivity.class);
        intent.putExtra("barcode", barcode);
        intent.putExtra("gname", gname);
        startActivityForResult(intent, BARCODE_MANAGER_REQUEST);
    }

    private void startCameraSearch() {
        Intent intent = new Intent(this, ZBarScannerActivity.class);
        startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
    }

    //????????????
    public void OnClickModify(View v) {
        if (m_selectedListIndex == -1) {
            Toast.makeText(ManageStockActivity.this, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
            return;
        }

        TextView barcode = (TextView) findViewById(R.id.editTextBarcode);
        TextView productName = (TextView) findViewById(R.id.editTextProductName);
        TextView purchasePrice = (TextView) findViewById(R.id.editTextPurchasePrice);
        TextView salePrice = (TextView) findViewById(R.id.editTextSalePrice);
        TextView numOfReal = (TextView) findViewById(R.id.editTextNumberOfReal);
        TextView curStock = (TextView) findViewById(R.id.editTextCurStock);

        String period = m_period.getText().toString();

        HashMap<String, String> map = fillMaps.get(m_selectedListIndex);

        map.put("BarCode", barcode.getText().toString());
        map.put("G_Name", productName.getText().toString());
        map.put("Real_Sto", numOfReal.getText().toString());
        map.put("St_Count", curStock.getText().toString());

        adapter.notifyDataSetChanged();

        HashMap<String, String> rmap = m_stockList.get(m_selectedListIndex);

        rmap.put("St_Date", period);
        rmap.put("BarCode", barcode.getText().toString());
        rmap.put("G_Name", productName.getText().toString());
        rmap.put("Pur_Pri", purchasePrice.getText().toString());
        rmap.put("Sell_Pri", salePrice.getText().toString());
        rmap.put("St_Count", curStock.getText().toString());
        rmap.put("Real_Sto", numOfReal.getText().toString());

        m_selectedListIndex = -1;
    }

    public void onClickSetDate1(View v) {
        DatePickerDialog newDlg = new DatePickerDialog(this, this,
                m_dateCalender1.get(Calendar.YEAR),
                m_dateCalender1.get(Calendar.MONTH),
                m_dateCalender1.get(Calendar.DAY_OF_MONTH));
        newDlg.show();
    }

    ;


//    //******************************* ?????? ????????? ?????? *****************************
//
//
//    /**
//     * ???????????? ??????
//     *
//     * @return dir
//     */
//    private File makeDirectory(String dir_path) {
//        File dir = new File(dir_path);
//        if (!dir.exists()) {
//            dir.mkdirs();
//            Log.i(TAG, "!dir.exists");
//        } else {
//            Log.i(TAG, "dir.exists");
//        }
//
//        return dir;
//    }
//
//    /**
//     * ?????? ??????
//     *
//     * @param dir
//     * @return file
//     */
//    private File makeFile(File dir, String file_path) {
//        File file = null;
//        boolean isSuccess = false;
//        try{
//
//            //if (dir.exists()){
//
//            //if (dir.isDirectory()) {
//                file = new File(file_path);
//                if (file != null && !file.exists()) {
//                    Log.i(TAG, "!file.exists");
//                    try {
//                        isSuccess = file.createNewFile();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    } finally {
//                        Log.i(TAG, "???????????? ?????? = " + isSuccess);
//                    }
//                } else {
//                    Log.i(TAG, "file.exists");
//                }
//            //}
//
//        }catch (Exception e){
//            e.printStackTrace();
//
//        }
//        return file;
//    }
//
//    /**
//     * (dir/file) ?????? ?????? ????????????
//     *
//     * @param file
//     * @return String
//     */
//    private String getAbsolutePath(File file) {
//        return "" + file.getAbsolutePath();
//    }
//
//    /**
//     * (dir/file) ?????? ??????
//     *
//     * @param file
//     */
//    private boolean deleteFile(File file) {
//        boolean result;
//        if (file != null && file.exists()) {
//            file.delete();
//            result = true;
//        } else {
//            result = false;
//        }
//        return result;
//    }
//
//    /**
//     * ???????????? ?????? ??????
//     *
//     * @param file
//     * @return
//     */
//    private boolean isFile(File file) {
//        boolean result;
//        if (file != null && file.exists() && file.isFile()) {
//            result = true;
//        } else {
//            result = false;
//        }
//        return result;
//    }
//
//    /**
//     * ???????????? ?????? ?????? ??????
//     *
//     * @param dir
//     * @return
//     */
//    private boolean isDirectory(File dir) {
//        boolean result;
//        if (dir != null && dir.isDirectory()) {
//            result = true;
//        } else {
//            result = false;
//        }
//        return result;
//    }
//
//    /**
//     * ?????? ?????? ?????? ?????? ??????
//     *
//     * @param file
//     * @return
//     */
//    private boolean isFileExist(File file) {
//        boolean result;
//        if (file != null && file.exists()) {
//            result = true;
//        } else {
//            result = false;
//        }
//        return result;
//    }
//
//    /**
//     * ?????? ?????? ?????????
//     *
//     * @param file
//     */
//    private boolean reNameFile(File file, File new_name) {
//        boolean result;
//        if (file != null && file.exists() && file.renameTo(new_name)) {
//            result = true;
//        } else {
//            result = false;
//        }
//        return result;
//    }
//
//    /**
//     * ??????????????? ?????? ????????? ?????? ??????.
//     *
//     * @param
//     * @return
//     */
//    private String[] getList(File dir) {
//        if (dir != null && dir.exists())
//            return dir.list();
//        return null;
//    }
//
//    /**
//     * ????????? ?????? ??????
//     *
//     * @param file
//     * @param file_content
//     * @return
//     */
//    private boolean writeFile(File file, byte[] file_content) {
//        boolean result;
//        FileOutputStream fos;
//        if (file != null && file.exists() && file_content != null) {
//            try {
//                fos = new FileOutputStream(file, true);
//                try {
//                    fos.write(file_content);
//                    fos.flush();
//                    fos.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            } catch (FileNotFoundException e) {
//                e.printStackTrace();
//            }
//            result = true;
//        } else {
//            result = false;
//        }
//        return result;
//    }
//
//    /**
//     * ?????? ?????? ??????
//     *
//     * @param file
//     */
//    private void readFile(File file) {
//        int readcount = 0;
//        if (file != null && file.exists()) {
//            try {
//                FileInputStream fis = new FileInputStream(file);
//                readcount = (int) file.length();
//                byte[] buffer = new byte[readcount];
//                fis.read(buffer);
//                for (int i = 0; i < file.length(); i++) {
//                    Log.d(TAG, "" + buffer[i]);
//                }
//                fis.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    /**
//     * ?????? ??????
//     *
//     * @param file
//     * @param save_file
//     * @return
//     */
//    private boolean copyFile(File file, String save_file) {
//        boolean result;
//        if (file != null && file.exists()) {
//            try {
//                FileInputStream fis = new FileInputStream(file);
//                FileOutputStream newfos = new FileOutputStream(save_file);
//                int readcount = 0;
//                byte[] buffer = new byte[1024];
//                while ((readcount = fis.read(buffer, 0, 1024)) != -1) {
//                    newfos.write(buffer, 0, readcount);
//                }
//                newfos.close();
//                fis.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//            result = true;
//        } else {
//            result = false;
//        }
//        return result;
//    }
//
//
//    //******************************* ?????? ????????? ?????? ???*****************************


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear,
                          int dayOfMonth) {
        m_dateCalender1.set(year, monthOfYear, dayOfMonth);
        m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

        getInTSeq();
    }

    @Override
    public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2,
                               long arg3) {
    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {
    }


    private AdapterView.OnItemClickListener mItemClickListener = new AdapterView.OnItemClickListener() {
        @Override


        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {

            m_selectedListIndex = arg2;

            /**
             Toast.makeText(ManageStockActivity.this, ""+arg2, Toast.LENGTH_SHORT).show();

             TextView barcode = (TextView)findViewById(R.id.editTextBarcode);
             TextView productName = (TextView)findViewById(R.id.editTextProductName);
             TextView purchasePrice = (TextView)findViewById(R.id.editTextPurchasePrice);
             TextView salePrice = (TextView)findViewById(R.id.editTextSalePrice);
             TextView numOfReal = (TextView)findViewById(R.id.editTextNumberOfReal);
             TextView curStock = (TextView)findViewById(R.id.editTextCurStock);

             barcode.setText(m_stockList.get(arg2).get("BarCode").toString());
             productName.setText(m_stockList.get(arg2).get("G_Name").toString());
             purchasePrice.setText(m_stockList.get(arg2).get("Pur_Pri").toString());
             salePrice.setText(m_stockList.get(arg2).get("Sell_Pri").toString());

             //2020-06-15
             //numOfReal.setText(m_stockList.get(arg2).get("oReal_Sto").toString());
             //curStock.setText(m_stockList.get(arg2).get("St_Count").toString());

             numOfReal.setText(m_stockList.get(arg2).get("St_Count").toString());
             curStock.setText(m_stockList.get(arg2).get("oReal_Sto").toString());

             m_period.setText(m_stockList.get(arg2).get("St_Date").toString());

             numOfReal.requestFocus();
             **/

            m_selectedListIndex = arg2;

            //HashMap<String, String> map = mfillMaps.get(pos);
            //final String count = map.get("??????");

            final String count = m_stockList.get(arg2).get("St_Count").toString();

            //----------------------------------------//
            // 2022.07.28. ????????? ?????? ??? ????????? ?????? ??????
            //----------------------------------------//
            //AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
            AlertDialog.Builder ad = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
            //----------------------------------------//

            ad.setTitle("?????? ?????? ??????");       // ?????? ??????
            ad.setMessage("???????????? ??? ??????");   // ?????? ??????

            // EditText ????????????
            final TextView ev = new TextView(getApplicationContext());
            ev.setText("????????????");
            final EditText et = new EditText(getApplicationContext());

            //----------------------------------------//
            // 2022.07.28. ????????? ?????? ??? ????????? ?????? ??????
            //----------------------------------------//
            et.setTextColor(Color.BLACK);
            et.setTextSize(30);
            //----------------------------------------//

            et.setGravity(Gravity.CENTER);
            et.setTextColor(Color.RED);
            et.setText(count);
            et.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
            et.selectAll();
            ad.setView(et);

            // ?????????????????? ??????
            ad.setPositiveButton("????????????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.v(TAG, "Yes Btn Click");

                    // Text ??? ????????? ?????? ?????????
                    String value = et.getText().toString();
                    Log.v(TAG, value);

                    try {
                        //?????? ?????? ?????? ???
                        int a = Integer.parseInt(value);

                    } catch (NumberFormatException e) {
                        dialog.dismiss();
                        upu.hideSoftKeyboard(false);
                        Log.d(TAG, "????????????");
                        return;
                    }

                    //???????????? ?????? ???
                    if (count.equals(value.trim())) {
                        dialog.dismiss();
                        upu.hideSoftKeyboard(false);
                        Log.d(TAG, "??????????????????");
                        return;
                    }

                    //????????????
                    //setChangeAmount(pos, value);

                    HashMap<String, String> map = fillMaps.get(m_selectedListIndex);
                    map.put("St_Count", value.toString());

                    adapter.notifyDataSetChanged();

                    HashMap<String, String> rmap = m_stockList.get(m_selectedListIndex);

                    String purchasePrice = m_stockList.get(m_selectedListIndex).get("Pur_Pri");    // ?????????
                    String salePrice = m_stockList.get(m_selectedListIndex).get("Sell_Pri");        // ?????????
                    String Pur_Cost = m_stockList.get(m_selectedListIndex).get("Pur_Cost");        // ????????????
                    String Add_Tax = m_stockList.get(m_selectedListIndex).get("Add_Tax");            // ?????????
                    String Bot_Pur = m_stockList.get(m_selectedListIndex).get("Bot_Pur");            // ???????????????
                    String Bot_Sell = m_stockList.get(m_selectedListIndex).get("Bot_Sell");        // ???????????????

                    rmap.put("St_Count", value.toString());
                    rmap.put("TPur_Pri", String.valueOf((Double.valueOf(Pur_Cost) + Double.valueOf(Add_Tax)) * Double.valueOf(value)));        //??? ?????????(????????????)=?????????x??????
                    rmap.put("TPur_Cost", String.valueOf(Double.valueOf(Pur_Cost) * Double.valueOf(value)));            //????????????x??????
                    rmap.put("TAdd_Tax", String.valueOf(Double.valueOf(Add_Tax) * Double.valueOf(value)));            //?????????x??????
                    rmap.put("In_Pri", String.valueOf((Double.valueOf(Pur_Cost) + Double.valueOf(Add_Tax) + Double.valueOf(Bot_Pur)) * Double.valueOf(value)));
                    rmap.put("TSell_Pri", String.valueOf((Double.valueOf(salePrice)) * Double.valueOf(value)));    //??? ?????????(????????????)=?????????x??????
                    rmap.put("In_SellPri", String.valueOf((Double.valueOf(salePrice) + Double.valueOf(Bot_Sell)) * Double.valueOf(value)));
                    rmap.put("Bot_Pri", String.valueOf(Double.valueOf(Bot_Pur) * Double.valueOf(value)));
                    rmap.put("Bot_SellPri", String.valueOf(Double.valueOf(Bot_Sell) * Double.valueOf(value)));

                    m_stockList.get(m_selectedListIndex).put("St_Count", rmap.get("St_Count"));
                    m_stockList.get(m_selectedListIndex).put("TPur_Pri", rmap.get("TPur_Pri"));
                    m_stockList.get(m_selectedListIndex).put("TPur_Cost", rmap.get("TPur_Cost"));
                    m_stockList.get(m_selectedListIndex).put("TAdd_Tax", rmap.get("TAdd_Tax"));
                    m_stockList.get(m_selectedListIndex).put("In_Pri", rmap.get("In_Pri"));
                    m_stockList.get(m_selectedListIndex).put("TSell_Pri", rmap.get("TSell_Pri"));
                    m_stockList.get(m_selectedListIndex).put("In_SellPri", rmap.get("In_SellPri"));
                    m_stockList.get(m_selectedListIndex).put("Bot_Pri", rmap.get("Bot_Pri"));
                    m_stockList.get(m_selectedListIndex).put("Bot_SellPri", rmap.get("Bot_SellPri"));

                    clearInputBox();

                    Log.d(TAG, "????????????");
                    dialog.dismiss();     //??????

                    // Event
                }
            });

            //???????????? ??????
            ad.setNeutralButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.v(TAG, "Neutral Btn Click");

                    //?????? ????????????
                    //onDataDelete(pos);

                    deleteData();
                    clearInputBox();

                    dialog.dismiss();     //??????
                }
            });

            // ?????? ?????? ??????
            ad.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.v(TAG, "No Btn Click");
                    dialog.dismiss();     //??????
                    upu.hideSoftKeyboard(false);
                    // Event
                }
            });

            //??? ?????????
            ad.show();

            upu.hideSoftKeyboard(true);

        }

    };


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
            actionbar.setTitle("????????????");

            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    //????????? ??????????????? ?????? ?????? ????????????.~
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String msg = " ?????? ???????????? ?????? ?????? ?????? ?????? ?????? ????????? ?????? ?????????. ?????? ??????????????????? ";
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (!m_stockList.isEmpty()) {
                    new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
                            .setTitle("??????")
                            .setMessage(msg.toString())
                            .setPositiveButton("???", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    finish();
                                }
                            })
                            .setNegativeButton("?????????", null).show();
                    return false;
                } else {
                    finish();
                }
            default:
                return false;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_stock, menu);
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
