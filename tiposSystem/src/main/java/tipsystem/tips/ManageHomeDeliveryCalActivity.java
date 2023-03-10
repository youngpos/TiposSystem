package tipsystem.tips;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.M3MoblieBarcodeScanBroadcast;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL3;
import tipsystem.utils.StringEncrypter;
import tipsystem.utils.StringFormat;
import tipsystem.utils.TIPS_Config;
import tipsystem.utils.UserPublicUtils;

public class ManageHomeDeliveryCalActivity extends Activity implements View.OnClickListener {

    private static final int ZBAR_SCANNER_REQUEST = 0;
    //private static final int ZBAR_QR_SCANNER_REQUEST = 1;
    private static final int BARCODE_MANAGER_REQUEST = 2;
    private static final int CUSTOMER_MANAGER_REQUEST = 3;
    private static final int BARCODEPRINT_HISTORY_REQUEST = 4;

    private static final String TAG="HomeDeliveryCal";

    private TIPS_Config tips;
    private UserPublicUtils upu;
    private Context mContext;

    //2017-04 m3mobile
    boolean m3Mobile;

    // loading bar
    private ProgressDialog dialog;


    // search ui
    private CheckBox m_date_check;  //????????? ??????
    private EditText m_textSalenum;     //????????????
    private Spinner m_delverly_gubun;   //????????????
    private Spinner m_delverly_deposit; //????????????

    private Button m_btnSearch;     //?????? ??????
    private Button m_btnSearch_trans; //??????????????????

    private Button m_deliverly_sender_reg; //??????????????? ????????????
    private Button m_delverly_reg;    //????????????
    private Button m_delverly_reg_cancle; //??????????????????

    private ListView m_delverly_list;   //???????????? ?????????
    private CustomAdapter m_adapter;
    private List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>(); // ???????????? ?????????

    private int select_position = 0;
    private String hp_num = "";

    @Override
    public void onCreate(Bundle savedInstanceState){
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_managehomedeliverycal);

         mContext = this;
         tips = new TIPS_Config(mContext);
         upu = new UserPublicUtils(mContext);

         m3Mobile = tips.isM3MOBLIE();

        // search ui
        m_date_check = (CheckBox)findViewById(R.id.checkbox_date);  //?????????
        m_textSalenum = (EditText)findViewById(R.id.editText_saleNum);              //????????????
        m_delverly_gubun = (Spinner)findViewById(R.id.spinner_delivery_gubun);      //????????????
        m_delverly_deposit = (Spinner)findViewById(R.id.spinner_delivery_deposit);  //????????????

        m_btnSearch = (Button)findViewById(R.id.button_search);                 //?????? ??????
        m_btnSearch_trans =  (Button)findViewById(R.id.Button_delivery_search);  //????????????

        m_deliverly_sender_reg = (Button)findViewById(R.id.button_delivery_sender_reg); //??????????????? ??????
        m_delverly_reg = (Button)findViewById(R.id.button_delivery_reg);   //????????????
        m_delverly_reg_cancle = (Button)findViewById(R.id.button_delivery_reg_cancle); //???????????? ??????

        if("0".equals(tips.getAPP_USER_GRADE()) || "1".equals(tips.getAPP_USER_GRADE())){
            m_deliverly_sender_reg.setVisibility(View.VISIBLE);
        }else{
            m_deliverly_sender_reg.setVisibility(View.GONE);
        }

        m_deliverly_sender_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDelverly_Sender();
            }
        });

        /*if("4".equals(tips.getAPP_USER_GRADE())){
            m_delverly_reg.setVisibility(View.GONE);
        }*/

        m_delverly_list = (ListView)findViewById(R.id.listview_delverlylist);   //????????????

        //?????? ?????????
        String[] from = new String[] { "??????", "????????????", "??????", "????????????", "????????????", "?????????", "????????????", "????????????", "??????", "????????????", "????????????", "????????????"};
        int[] to = new int[] { R.id.item0, R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8, R.id.item9, R.id.item10, R.id.item11 };
        m_adapter = new CustomAdapter(this, mfillMaps, R.layout.activity_delivery_list1, from, to);
        m_delverly_list.setAdapter(m_adapter);
        m_delverly_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = mfillMaps.get(position);
                Intent intent = new Intent(mContext, ManageHonmeDeliveryDetailActivity.class);
                intent.putExtra("sale_num", map.get("????????????"));
                intent.putExtra("status", map.get("????????????"));
                //????????? ???????????? ?????? ????????? ?????? ?????? ????????? ????????? ?????? ?????????.
                if(map.get("????????????").equals("?????????") && map.get("????????????").equals("??????")){
                    intent.putExtra("payment", map.get("????????????"));
                }else {
                    intent.putExtra("payment", map.get("????????????"));
                }
                intent.putExtra("delivery_gubun", map.get("????????????"));
                intent.putExtra("cus_code", map.get("????????????"));
                intent.putExtra("cus_name", map.get("?????????"));
                intent.putExtra("phone_num", map.get("????????????"));
                intent.putExtra("hp_num", map.get("???????????????"));
                intent.putExtra("address", map.get("??????"));
                intent.putExtra("address1", map.get("????????????"));
                intent.putExtra("total_price", map.get("????????????"));
                intent.putExtra("user_id", map.get("?????????"));

                startActivity(intent);
            }
        });

        /*m_delverly_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                select_position = position;
                AlertDialog.Builder alert_bulder = new AlertDialog.Builder(mContext);
                alert_bulder.setMessage("?????? ????????? ??????????????? ?????? ???????????????????")
                        .setCancelable(false)
                        .setPositiveButton("???", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                regDeliveryList_one();
                            }
                        })
                        .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alert_bulder.create();
                alert.setTitle("???????????? ??????");
                alert.show();
                return false;
            }
        });*/

        //Action
        m_textSalenum.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    try {
                        Thread.sleep(1000);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    doQueryWithSaleNum("??????");
                }
                return false;
            }
        });

        /*m_textSalenum.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if ((keyCode == EditorInfo.IME_ACTION_DONE) || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                        doQueryWithSaleNum("??????");
                    }
                }

                return false;
            }
        });*/

        m_textSalenum.setImeOptions(EditorInfo.IME_ACTION_DONE);
        m_btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doQueryWithSaleNum("");
            }
        });

        m_btnSearch_trans.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doQueryWithSaleNum("????????????");
            }
        });

        m_delverly_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //?????? ?????? ?????? ?????? ??????
                if (getRegCount(0)) {
                    upu.showDialog("?????? ????????? ????????? ????????????.", 0);
                    return;
                }

                //----------------------------------------//
                // 2022.07.28. ????????? ?????? ??? ????????? ?????? ??????
                //----------------------------------------//
                //AlertDialog.Builder alert_bulder = new AlertDialog.Builder(mContext);
                AlertDialog.Builder alert_bulder = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
                //----------------------------------------//
                alert_bulder.setMessage("??????????????? ?????? ???????????????????")
                        .setCancelable(false)
                        .setPositiveButton("???", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                regDeliveryList();
                            }
                        })
                        .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alert_bulder.create();
                alert.setTitle("???????????? ??????");
                alert.show();
            }
        });

        //???????????? ??????
        m_delverly_reg_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //?????? ?????? ?????? ?????? ??????
                if (getRegCount(1)) {
                    upu.showDialog("?????? ????????? ????????? ????????????.", 0);
                    return;
                }

                //----------------------------------------//
                // 2022.07.28. ????????? ?????? ??? ????????? ?????? ??????
                //----------------------------------------//
                //AlertDialog.Builder alert_bulder = new AlertDialog.Builder(mContext);
                AlertDialog.Builder alert_bulder = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
                //----------------------------------------//
                alert_bulder.setMessage("????????? ????????? ?????? ???????????????????\r\n????????? ?????? ?????? ?????? ?????????.")
                        .setCancelable(false)
                        .setPositiveButton("???", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setDelverly_cancle();
                            }
                        })
                        .setNegativeButton("?????????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alert_bulder.create();
                alert.setTitle("???????????? ??????");
                alert.show();
            }
        });



    // intent filter 2017-04 m3mobile
        if (m3Mobile) {
			/*IntentFilter filter = new IntentFilter();
			filter.addAction(SCANNER_ACTION_BARCODE);
			registerReceiver(BarcodeIntentBroadcast, filter);*/
        M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textSalenum, new M3MoblieBarcodeScanBroadcast.M3MoblieCallbackInterface(){
                    @Override
                    public void onRequestCompleted(boolean results) {
                        if (results) {
                            doQueryWithSaleNum("??????");
                        }
                    }
                }
        );
    }
}

    //??????????????? ??????
    private void setDelverly_Sender(){


        if(getRegCount(0)){
            upu.showDialog("????????? ?????? ????????? ???????????? ????????????.", 0);
            return;
        }

        String query = "Select User_ID, User_Name, Tel1 From Admin_User Where APP_USER_GRADE='4'";

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

                    final ArrayList<HashMap<String, String>> sender_list = new ArrayList<HashMap<String,String>>();
                    String[] sender = new String[results.length()];
                    for(int i =0; i < results.length(); i++) {
                        HashMap<String, String> m_tempProduct; // ???????????? ?????????
                        try {
                            m_tempProduct = JsonHelper.toStringHashMap(results.getJSONObject(i));
                            sender[i] = m_tempProduct.get("User_Name");
                            sender_list.add(m_tempProduct);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Dialog));
                    builder.setTitle("??????????????? ??????");
                    builder.setItems(sender, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                           HashMap<String, String> map = sender_list.get(which);
                           regDeliveryList_one(map.get("User_ID"));
                           dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                        /*builder.setPositiveButton("????????????", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                regDeliveryList_back();
                            }
                        });*/

                    builder.show();



                } else {
                    //?????? ????????? ????????? ????????? ???????????? ??? ?????? ?????????.
                    Toast.makeText(getApplicationContext(), "????????? ???????????? ????????????.", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(tips.getSHOP_IP() + ":" + tips.getSHOP_PORT(), tips.getDB_NAME(), tips.getDB_ID(), tips.getDB_PW(), query);


    }


    public void viewDetail(int position){

        HashMap<String, String> map = mfillMaps.get(position);
        Intent intent = new Intent(mContext, ManageHonmeDeliveryDetailActivity.class);
        intent.putExtra("sale_num", map.get("????????????"));
        intent.putExtra("status", map.get("????????????"));
        //????????? ???????????? ?????? ????????? ?????? ?????? ????????? ????????? ?????? ?????????.
        if(map.get("????????????").equals("?????????") && map.get("????????????").equals("??????")){
            intent.putExtra("payment", map.get("????????????"));
        }else {
            intent.putExtra("payment", map.get("????????????"));
        }
        intent.putExtra("delivery_gubun", map.get("????????????"));
        intent.putExtra("cus_code", map.get("????????????"));
        intent.putExtra("cus_name", map.get("?????????"));
        intent.putExtra("phone_num", map.get("????????????"));
        intent.putExtra("hp_num", map.get("???????????????"));
        intent.putExtra("address", map.get("??????"));
        intent.putExtra("address1", map.get("????????????"));
        intent.putExtra("total_price", map.get("????????????"));
        intent.putExtra("user_id", map.get("?????????"));

        startActivity(intent);

    }

    public void setSelectOne(int position){

        HashMap<String, String> map = mfillMaps.get(position);

        String sel = map.get("??????");
        if(sel.equals("true")){
            map.put("??????", "false");
        }else{
            map.put("??????", "true");
        }

        mfillMaps.set(position, map);
        m_adapter.notifyDataSetChanged();

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
                    m_textSalenum.setText(barcode);
                    doQueryWithSaleNum("??????");

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //Method

    /**
     * 0 : ????????? ?????? , 1: ?????? ??????????????? ????????? ??????
     * @return true : ??????????????? ????????? ??????
     */
    private boolean getRegCount(int num){

        if(mfillMaps.size() <= 0){
            return true;
        }

        int count = 0;
        switch(num){
            case 0:
                for(int i=0; i < mfillMaps.size(); i++){
                    HashMap<String, String> map = mfillMaps.get(i);

                    if("true".equals(map.get("??????")) && map.get("????????????").equals("?????????")){
                        count++;
                    }
                }
                break;
            case 1:
                for(int i=0; i < mfillMaps.size(); i++){
                    HashMap<String, String> map = mfillMaps.get(i);
                    if("true".equals(map.get("??????")) && map.get("????????????").equals("?????????") && map.get("?????????").equals(tips.getUSER_ID())){
                        count++;
                    }
                }
                break;
        }

        if(count <= 0){
            return true;
        }

        return false;
    }

    private void doQueryWithSaleNum(String search){

        String query = "";
        String salenum = m_textSalenum.getText().toString();

        if(search.equals("??????")){
            if(isCompareSaleNum(salenum)){
                upu.showDialog("?????? ????????? ????????????.", 0);
                return;
            }
        }else{
            mfillMaps.clear();
        }

        String todayMonth = upu.getTodayDataMonth();
        String today = upu.getTodayData();
        String onlyToday = "";

        //????????? ????????? ???????????????.
        if(m_date_check.isChecked()){
            onlyToday = "AND c.Sale_Date = '"+today+"' ";
        }

        if(!salenum.equals("")){
            todayMonth = getSaleNumPerfix(salenum);
            salenum = " AND replace(c.Sale_Num, '-', '')='"+salenum+"' ";
        }

        //0:?????????, 1:?????????, 2:????????????
        int delivery_state = m_delverly_gubun.getSelectedItemPosition()-1;
        String state = "";
        if(delivery_state >= 0){
            state = " AND IsNull(c.Delivery_State, '0')='"+delivery_state+"' ";
        }

        int delivery_deposit = m_delverly_deposit.getSelectedItemPosition();
        String deposit = "";
        switch(delivery_deposit){
            case 1: //????????? = ????????????, ?????????
                deposit = "AND (c.Delivery_YN ='2' AND ISNULL(inF_YN,'0')='0') ";
                break;
            case 2: //?????? = ????????????, ??????
                deposit = "AND (c.Delivery_YN = '1' or ISNULL(inF_YN,'0')='1') ";
                break;
        }

        /*if(delivery_deposit != 0){
            //deposit = "AND Isnull(c.Delivery_Deposit, '0')='"+(delivery_deposit-1)+"' ";
            deposit = "AND ISNULL(inF_YN,'0')='"+(delivery_deposit-1)+"' ";
        }*/

        /*String search = m_btnSearch.getText().toString();*/
        String myDelivery = "";
        if(search.equals("????????????")){
            //deposit = " AND Isnull(c.Delivery_Deposit, '0')='0' ";
            //deposit = "";
            myDelivery = " AND IsNull(c.Delivery_Sender, '')='"+tips.getUSER_ID()+"' ";
        }else if(search.equals("??????")){
            onlyToday = "";
        }

        query = "Select c.Sale_Num as '????????????', (c.Sale_Date+' '+C.Sale_Time) as '??????', c.Cus_Code as '????????????', ISNULL(B.Cus_Name, '') as '?????????', "
                + "isnull(b.Cus_Tel, '') as ????????????, isnull(b.Cus_Mobile, '') as ???????????????, ";

        if (tips.getEN_USE().equals("1")) {
            query += "isnull(b.en_uKey1, '') as ???????????????, isnull(b.en_uKey2, '') as ??????????????????, ";
        }
        query += "isnull(b.Address1, '') as ??????, isnull(b.Address2, '') as '????????????', c.Close_YN as '??????', c.Gift_Pri as Gift_Pri, "
                + "'????????????' = CASE WHEN c.Delivery_YN ='2' THEN a.B_TotalMoney ELSE c.Cash_pri + c.Card_Pri + c.Dec_Pri END, "
                + "c.CMS_Pri+c.Sub_Pri+c.Cut_Pri+c.Gift_Pri+c.Cus_PointUse+c.Cashback_PointUse + c.bc_coupon_dc + c.bc_card_dc + isnull(c.APP_DCPri,0) as '???????????????', "
                + "c.Rec_Pri as '????????????', '??????????????????' = CASE WHEN c.Delivery_YN ='2' THEN a.tranpoint_chk ELSE 0 END, "
                + "c.cus_point as '???????????????', c.bir_point as '???????????????', c.cut_point as '???????????????', c.writer as '?????????', "
                + "'????????????' = CASE WHEN c.Delivery_YN ='2' THEN a.inDate ELSE c.Sale_Date + ' ' + C.Sale_Time END, "
                + "'????????????' =  CASE WHEN c.Delivery_YN ='2' THEN a.inPOS ELSE c.Pos_No END, "
                + "'????????????' = CASE WHEN c.Delivery_YN ='2' THEN ISNULL(a.inCash_Pri,0) ELSE c.Cash_Pri END, "
                + "'????????????' = CASE WHEN c.Delivery_YN ='2' THEN ISNULL(a.inCard_Pri,0) ELSE c.Card_Pri END, "
                + "'????????????' = CASE WHEN c.Delivery_YN ='2' THEN ISNULL(a.inDec_Pri,0) ELSE c.Dec_Pri END, "
                + "'?????????' = CASE WHEN c.Delivery_YN ='2' THEN ISNULL((Select b.USER_NAME From ADMIN_USER B WHERE b.USER_ID=a.inUser_ID),'') ELSE c.writer END, "
                + "'????????????' = CASE WHEN c.Delivery_YN ='2' THEN CASE WHEN ISNULL(inF_YN,'0') =0 THEN '?????????' ELSE '??????' END ELSE '??????' END, "
                + "'????????????' = CASE WHEN c.Delivery_YN ='2' THEN '??????' ELSE '??????' END, "
                + "'????????????' = CASE WHEN A.inBanpum_YN ='1' THEN '????????? ??????' ELSE '' END, "
                + "isnull(c.Delivery_State, '0') as '????????????', isnull(c.Delivery_Deposit, '0') as '????????????', "
                + "isnull(c.Delivery_Sender, '') as '?????????', "
                + "'????????????' = CASE WHEN isnull(c.Delivery_Sender, '') = '' THEN isnull(c.Delivery_Sender, '') ELSE (select User_Name from admin_user where user_id=c.Delivery_Sender) END "
                + "From SAT_"+todayMonth+" c left join Customer_Info b ON c.cus_code = b.cus_code LEFT JOIN BaeDal_List A ON c.Sale_Num = a. B_JeonPyo "
                + "WHERE Return_Chk='0' AND Befor_Jeonpyo IS NULL AND c.Delivery_YN <> '0' "+salenum+state+deposit+myDelivery+onlyToday
                + "ORDER BY c.SALE_NUM ";

        Log.d(TAG, tips.getSHOP_IP() + ":" + tips.getSHOP_PORT()+ "TIPS"+ "sa"+ "tips"+ query);

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

                    for(int i =0; i < results.length(); i++) {
                        HashMap<String, String> m_tempProduct; // ???????????? ?????????
                        try {
                            m_tempProduct = JsonHelper.toStringHashMap(results.getJSONObject(i));

                            m_tempProduct.put("??????", "false");
                            m_tempProduct.put("????????????", StringFormat.convertToIntNumberFormat(m_tempProduct.get("????????????")));
                            m_tempProduct.put("????????????", StringFormat.convertToIntNumberFormat(m_tempProduct.get("????????????")));
                            m_tempProduct.put("????????????", getDeliveryStateToString(m_tempProduct.get("????????????")));

                            if(m_tempProduct.get("????????????").equals("??????")) {
                                m_tempProduct.put("????????????", getDeliveryDepositToString(m_tempProduct.get("????????????")));
                            }else{
                                m_tempProduct.put("????????????", "????????????");
                            }

                            try {

                                if (tips.getEN_USE().equals("1")) {
                                    try {
                                        String phone = new StringEncrypter(tips.getKEY(), tips.getIV()).decrypt(m_tempProduct.get("???????????????"));
                                        m_tempProduct.put("????????????", phone);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        String hp = new StringEncrypter(tips.getKEY(), tips.getIV()).decrypt(m_tempProduct.get("??????????????????"));
                                        m_tempProduct.put("???????????????", hp);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }

                            mfillMaps.add(m_tempProduct);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    //?????? ????????? ????????? ????????? ???????????? ??? ?????? ?????????.
                    Toast.makeText(getApplicationContext(), "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
                }

                m_textSalenum.setText("");
                m_textSalenum.requestFocus();
                m_adapter.notifyDataSetChanged();
            }
            //}).execute(tips.getSHOP_IP() + ":" + tips.getSHOP_PORT(), "TIPS", "sa", "tips", query);
        }).execute(tips.getSHOP_IP() + ":" + tips.getSHOP_PORT(), tips.getDB_NAME(), tips.getDB_ID(), tips.getDB_PW(), query);


    }


    //??????????????? ?????? ?????? true : ????????? ??????, false : ????????? ??????
    private boolean isCompareSaleNum(String salenum){

        if(salenum.equals("")){
            return false;
        }

        if(mfillMaps.size() <= 0){
            return false;
        }

        Iterator<HashMap<String, String>> iterator = mfillMaps.iterator();
        while(iterator.hasNext()){
            HashMap<String, String> map = iterator.next();
            if(map.get("????????????").replace("-", "").equals(salenum)){
                return true;
            }
        }

        return false;
    }


    //????????????
    private void regDeliveryList(){

        ArrayList<String> query_list = new ArrayList<String>();
        final ArrayList<String> phone_num = new ArrayList<>();
        for(int i = 0; i < mfillMaps.size(); i++){
            HashMap<String, String> map = mfillMaps.get(i);
            String query = "";
            if("true".equals(map.get("??????")) && map.get("????????????").equals("?????????")){
                String salenum =  map.get("????????????");
                String todayMonth = getSaleNumPerfix(salenum);
                query = "Update Sat_"+todayMonth+" set Delivery_State='1', Delivery_Deposit='0', Delivery_Sender='"+tips.getUSER_ID()+"' " +
                        "Where Sale_Num='"+salenum+"'; ";
                query_list.add(query);

                //sms?????????
                if(map.get("???????????????").length() > 9){
                    phone_num.add(map.get("???????????????"));
                }
            }
        }

        if(query_list.size() <= 0){
            Toast.makeText(this, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // ??????????????? ?????? ??????
        ArrayList<String> arraylist = new ArrayList<String>();
        arraylist.add(tips.getSHOP_IP() + ":" + tips.getSHOP_PORT());
        //arraylist.add("TIPS");
        //arraylist.add("sa");
        //arraylist.add("tips");
        arraylist.add(tips.getDB_NAME());
        arraylist.add(tips.getDB_ID());
        arraylist.add(tips.getDB_PW());

        new MSSQL3(new MSSQL3.MSSQL3CallbackInterface() {
            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(mContext, "????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
                //doQueryWithSaleNum("????????????");
                getSelectListManager();

                //?????? ?????? ????????? ?????? ?????????.
                /*if(phone_num.size() > 0 && tips.getTRAN_MSG().length() > 0){
                    Log.d(TAG, "??????????????? ?????????.");
                    for (String phone:phone_num) {
                        //String contents = "???????????????~! "+tips.getSHOP_NAME()+" ?????????. ???????????? ????????? ????????? ?????????????????????. ????????? ???????????? ?????????????????????.";

                        String contents = tips.getTRAN_MSG();
                        Log.d(TAG, contents);
                        upu.sendSMS(contents, phone);
                    }
                }*/
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(mContext, "?????? ?????? ???????????????.\r\n"+msg, Toast.LENGTH_SHORT).show();
            }
        }).execute(arraylist, query_list);
    }


    //??????????????? ????????????
    private void regDeliveryList_one(String sender){

        ArrayList<String> query_list = new ArrayList<String>();
        final ArrayList<String> phone_num = new ArrayList<>();
        for(int i = 0; i < mfillMaps.size(); i++){
            HashMap<String, String> map = mfillMaps.get(i);
            String query = "";
            if("true".equals(map.get("??????")) && map.get("????????????").equals("?????????")){
                String salenum =  map.get("????????????");
                String todayMonth = getSaleNumPerfix(salenum);
                query = "Update Sat_"+todayMonth+" set Delivery_State='1', Delivery_Deposit='0', Delivery_Sender='"+sender+"' " +
                        "Where Sale_Num='"+salenum+"'; ";
                query_list.add(query);

                //sms?????????
                if(map.get("???????????????").length() > 9){
                    phone_num.add(map.get("???????????????"));
                }
            }
        }

        if(query_list.size() <= 0){
            Toast.makeText(this, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // ??????????????? ?????? ??????
        ArrayList<String> arraylist = new ArrayList<String>();
        arraylist.add(tips.getSHOP_IP() + ":" + tips.getSHOP_PORT());
        //arraylist.add("TIPS");
        //arraylist.add("sa");
        //arraylist.add("tips");
        arraylist.add(tips.getDB_NAME());
        arraylist.add(tips.getDB_ID());
        arraylist.add(tips.getDB_PW());

        new MSSQL3(new MSSQL3.MSSQL3CallbackInterface() {
            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(mContext, "????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
                //doQueryWithSaleNum("????????????");
                getSelectListManager();

                /*if(phone_num.size() > 0 && tips.getTRAN_MSG().length() > 0){
                    Log.d(TAG, "??????????????? ?????????.");
                    for (String phone:phone_num) {
                        //String contents = "???????????????~! "+tips.getSHOP_NAME()+" ?????????. ???????????? ????????? ????????? ?????????????????????. ????????? ???????????? ?????????????????????.";

                        String contents = tips.getTRAN_MSG();
                        Log.d(TAG, contents);
                        upu.sendSMS(contents, phone);
                    }
                }*/
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(mContext, "?????? ?????? ???????????????.\r\n"+msg, Toast.LENGTH_SHORT).show();

            }
        }).execute(arraylist, query_list);


    }

    //????????? ??????
    private void setDelverly_cancle(){

        ArrayList<String> query_list = new ArrayList<String>();
        final ArrayList<String> phone_num = new ArrayList<>();
        if("0".equals(tips.getAPP_USER_GRADE()) || "1".equals(tips.getAPP_USER_GRADE())){
            //???????????? ?????? ?????? ??????????????? ?????? ?????? ????????????.
            for (int i = 0; i < mfillMaps.size(); i++) {
                HashMap<String, String> map = mfillMaps.get(i);
                String query = "";
                if ("true".equals(map.get("??????")) && map.get("????????????").equals("?????????")) {
                    String salenum = map.get("????????????");
                    String todayMonth = getSaleNumPerfix(salenum);
                    query = "Update Sat_" + todayMonth + " set Delivery_State=null, Delivery_Deposit=null, Delivery_Sender=null " +
                            "Where Sale_Num='" + salenum + "'; ";
                    query_list.add(query);
                }
            }
        }else {
            //???????????? ?????? ????????? ????????? ?????? ?????? ????????????.
            for (int i = 0; i < mfillMaps.size(); i++) {
                HashMap<String, String> map = mfillMaps.get(i);
                String query = "";
                if ("true".equals(map.get("??????")) && map.get("????????????").equals("?????????") && map.get("?????????").equals(tips.getUSER_ID())) {
                    String salenum = map.get("????????????");
                    String todayMonth = getSaleNumPerfix(salenum);
                    query = "Update Sat_" + todayMonth + " set Delivery_State='', Delivery_Deposit='', Delivery_Sender='' " +
                            "Where Sale_Num='" + salenum + "'; ";
                    query_list.add(query);
                }
            }
        }

        if(query_list.size() <= 0){
            Toast.makeText(this, "????????? ????????? ????????????.", Toast.LENGTH_SHORT).show();
            return;
        }

        // ?????? ???????????????
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // ??????????????? ?????? ??????
        ArrayList<String> arraylist = new ArrayList<String>();
        arraylist.add(tips.getSHOP_IP() + ":" + tips.getSHOP_PORT());
        //arraylist.add("TIPS");
        //arraylist.add("sa");
        //arraylist.add("tips");
        arraylist.add(tips.getDB_NAME());
        arraylist.add(tips.getDB_ID());
        arraylist.add(tips.getDB_PW());

        new MSSQL3(new MSSQL3.MSSQL3CallbackInterface() {
            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(mContext, "?????? ????????? ?????? ???????????????.", Toast.LENGTH_SHORT).show();
                //doQueryWithSaleNum("????????????");
                getSelectListManager();
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(mContext, "?????? ?????? ???????????????.\r\n"+msg, Toast.LENGTH_SHORT).show();
            }
        }).execute(arraylist, query_list);

    }



    //?????? ????????? ????????????
    private String getSaleNumPerfix(String junpyo){
        String sale_num = "";
        sale_num = junpyo.replace("-", "");
        sale_num = sale_num.substring(0, 6);
        Log.d(TAG, sale_num);

        return sale_num;
    }


    private String getDeliveryStateToString(String state_now){
        String status = "?????????";
        String[] deposit = getResources().getStringArray(R.array.delivery_gubun_summry);

        int num = 0;
        try {
            num = Integer.parseInt(state_now);
        }catch(NumberFormatException e){
            e.printStackTrace();
        }

        if(num != 0) {
            status = deposit[num+1];
        }
        return status;
    }

    private String getDeliveryDepositToString(String state){
        String status = "?????????";
        String[] deposit = getResources().getStringArray(R.array.deposit_gubun_summry);
        int num = 0;
        try {
            num = Integer.parseInt(state);
        }catch(NumberFormatException e){
            e.printStackTrace();
        }
        if(num != 0) {
            status = deposit[num];
        }
        return status;
    }


    public void setAllSelect(View view){

        if( mfillMaps.size() <= 0 ){
            return;
        }

        Button button = (Button)view.findViewById(R.id.button_allselect);

        String selectItem  = "true";
        if(button.getText().equals("????????????")){
            button.setText("????????????");
        }else{
            button.setText("????????????");
            selectItem = "false";
        }

        for(int i =0; i<mfillMaps.size(); i++){
            HashMap<String, String> map = mfillMaps.get(i);

            map.put("??????", selectItem);
            mfillMaps.set(i, map);
        }

        m_adapter.notifyDataSetChanged();
    }

    private void getSelectListManager(){

        //????????????????????? ?????? ????????? ???????????? ???????????????.
        if("4".equals(tips.getAPP_USER_GRADE())){
            //?????????
            doQueryWithSaleNum("????????????");
        }else{
            //????????????????????? ?????? ???????????? ??????
            doQueryWithSaleNum("");
        }

        //?????? ?????? ?????????
        Button button = (Button)findViewById(R.id.button_allselect);
        button.setText("????????????");

    }

    //?????????
    public void setRenew(View view){

        mfillMaps.clear();
        m_textSalenum.setText("");

        m_delverly_gubun.setSelection(0);
        m_delverly_deposit.setSelection(0);

        m_adapter.notifyDataSetChanged();
    }

    //2017-04 m3mobile ??????
    @Override
    protected void onResume() {
        super.onResume();
        if(m3Mobile){
            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textSalenum,new M3MoblieBarcodeScanBroadcast.M3MoblieCallbackInterface(){
                @Override
                public void onRequestCompleted(boolean results) {
                    if (results) {
                        doQueryWithSaleNum("??????");
                    }
                }
            });
            M3MoblieBarcodeScanBroadcast.setOnResume();
        }

        //?????????
        //doQueryWithSaleNum("????????????");
        getSelectListManager();
    }

    //2017-04 m3mobile ??????
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(m3Mobile){
            M3MoblieBarcodeScanBroadcast.setOnDestory();
        }
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
            actionbar.setTitle("??????????????????");

            getActionBar().setDisplayHomeAsUpEnabled(false);
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

    @Override
    public void onClick(View v) {
        Log.d(TAG, "????????? ????????????>?");
    }


    private class CustomAdapter extends SimpleAdapter {

        private List<? extends Map<String, ?>> item;
        private HashMap<String, String> temp;

        private int[] mTo;
        private String[] mFrom;
        private ViewBinder mViewBinder;

        private List<? extends Map<String, ?>> mData;

        private int mResource;

        private LayoutInflater mInflater;


        public CustomAdapter (Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.item = data;

            mData = data;
            mFrom = from;
            mTo = to;

            mResource = resource;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.activity_delivery_list1, null);
            }

            temp = (HashMap<String, String>)item.get(position);

            /*if (temp != null) {
                //List Item control
                String str = temp.get("????????????");
                if(str.equals("?????????")){
                    TextView delivery_gubun = (TextView)v.findViewById(R.id.item6);
                    delivery_gubun.setTextColor(Color.BLUE);
                }
            }*/

            return createViewFromResource(mInflater, position, convertView, parent, mResource);
        }


        private View createViewFromResource(LayoutInflater inflater, int position, View convertView,
                                            ViewGroup parent, int resource) {
            View v;
            if (convertView == null) {
                v = inflater.inflate(resource, parent, false);
            } else {
                v = convertView;
            }

            bindView(position, v);

            return v;
        }

        private void bindView(int position, View view) {
            final Map dataSet = mData.get(position);
            final int pos = position;
            if (dataSet == null) {
                return;
            }

            final ViewBinder binder = mViewBinder;
            final String[] from = mFrom;
            final int[] to = mTo;
            final int count = to.length;

            Button button_detail = (Button)view.findViewById(R.id.button_detail);
            button_detail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "?????????????????????.");
                    viewDetail(pos);
                }
            });

            CheckBox chk = (CheckBox)view.findViewById(R.id.item0);
            chk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "?????????????????????.");
                    setSelectOne(pos);
                }
            });



            for (int i = 0; i < count; i++) {
                final View v = view.findViewById(to[i]);
                if (v != null) {
                    final Object data = dataSet.get(from[i]);
                    String text = data == null ? "" : data.toString();
                    if (text == null) {
                        text = "";
                    }

                    boolean bound = false;
                    if (binder != null) {
                        bound = binder.setViewValue(v, data, text);
                    }

                    if (!bound) {
                        if (v instanceof Checkable) {

                            if ( Boolean.valueOf(text) instanceof Boolean) {
                                ((Checkable) v).setChecked(Boolean.valueOf(text));

                            } else if (v instanceof TextView) {
                                // Note: keep the instanceof TextView check at the bottom of these
                                // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                                setViewText((TextView) v, text);

                            } else {
                                throw new IllegalStateException(v.getClass().getName() +
                                        " should be bound to a Boolean, not a " +
                                        (data == null ? "<unknown type>" : data.getClass()));
                            }
                        } else if (v instanceof TextView) {
                            // Note: keep the instanceof TextView check at the bottom of these
                            // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                            setViewText((TextView) v, text);
                            if(i == 6){
                                if(text.equals("?????????")) {
                                    setTextColor((TextView) v, text, Color.RED);
                                }else if(text.equals("?????????")){
                                    setTextColor((TextView) v, text, Color.GREEN);
                                }else{
                                    setTextColor((TextView) v, text, Color.WHITE);
                                }
                            }

                        } else if (v instanceof ImageView) {
                            if (data instanceof Integer) {
                                setViewImage((ImageView) v, (Integer) data);
                            } else {
                                setViewImage((ImageView) v, text);
                            }
                        } else {
                            throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                    " view that can be bounds by this SimpleAdapter");
                        }
                    }
                }
            }
        }

        public void setTextColor(TextView v, String text, int color) {
            v.setTextColor(color);
        }

    }


}


