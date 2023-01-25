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
    private CheckBox m_date_check;  //하루만 검색
    private EditText m_textSalenum;     //전표번호
    private Spinner m_delverly_gubun;   //배달구분
    private Spinner m_delverly_deposit; //입금구분

    private Button m_btnSearch;     //전표 조회
    private Button m_btnSearch_trans; //배송목록조회

    private Button m_deliverly_sender_reg; //배달지정자 등록버튼
    private Button m_delverly_reg;    //배송등록
    private Button m_delverly_reg_cancle; //배송등록취소

    private ListView m_delverly_list;   //배송목록 리스트
    private CustomAdapter m_adapter;
    private List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>(); // 배송목록 리스트

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
        m_date_check = (CheckBox)findViewById(R.id.checkbox_date);  //오늘만
        m_textSalenum = (EditText)findViewById(R.id.editText_saleNum);              //전표번호
        m_delverly_gubun = (Spinner)findViewById(R.id.spinner_delivery_gubun);      //배달구분
        m_delverly_deposit = (Spinner)findViewById(R.id.spinner_delivery_deposit);  //입금구분

        m_btnSearch = (Button)findViewById(R.id.button_search);                 //전표 조회
        m_btnSearch_trans =  (Button)findViewById(R.id.Button_delivery_search);  //배송목록

        m_deliverly_sender_reg = (Button)findViewById(R.id.button_delivery_sender_reg); //배달지정자 등록
        m_delverly_reg = (Button)findViewById(R.id.button_delivery_reg);   //배송등록
        m_delverly_reg_cancle = (Button)findViewById(R.id.button_delivery_reg_cancle); //배송등록 취소

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

        m_delverly_list = (ListView)findViewById(R.id.listview_delverlylist);   //배송목록

        //목록 리스트
        String[] from = new String[] { "선택", "전표번호", "일자", "판매금액", "회원번호", "회원명", "배송구분", "입금완료", "주소", "입금구분", "상세주소", "배송자명"};
        int[] to = new int[] { R.id.item0, R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8, R.id.item9, R.id.item10, R.id.item11 };
        m_adapter = new CustomAdapter(this, mfillMaps, R.layout.activity_delivery_list1, from, to);
        m_delverly_list.setAdapter(m_adapter);
        m_delverly_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                HashMap<String, String> map = mfillMaps.get(position);
                Intent intent = new Intent(mContext, ManageHonmeDeliveryDetailActivity.class);
                intent.putExtra("sale_num", map.get("전표번호"));
                intent.putExtra("status", map.get("배송구분"));
                //입금이 완료되지 않은 전표는 배달 입금 구분을 표시해 줘야 합니다.
                if(map.get("입금완료").equals("미입금") && map.get("배달구분").equals("전화")){
                    intent.putExtra("payment", map.get("입금구분"));
                }else {
                    intent.putExtra("payment", map.get("입금완료"));
                }
                intent.putExtra("delivery_gubun", map.get("배달구분"));
                intent.putExtra("cus_code", map.get("회원번호"));
                intent.putExtra("cus_name", map.get("회원명"));
                intent.putExtra("phone_num", map.get("전화번호"));
                intent.putExtra("hp_num", map.get("휴대폰번호"));
                intent.putExtra("address", map.get("주소"));
                intent.putExtra("address1", map.get("상세주소"));
                intent.putExtra("total_price", map.get("받을금액"));
                intent.putExtra("user_id", map.get("배송자"));

                startActivity(intent);
            }
        });

        /*m_delverly_list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                select_position = position;
                AlertDialog.Builder alert_bulder = new AlertDialog.Builder(mContext);
                alert_bulder.setMessage("선택 전표를 배송중으로 변경 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                regDeliveryList_one();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alert_bulder.create();
                alert.setTitle("배송구분 변경");
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
                    doQueryWithSaleNum("추가");
                }
                return false;
            }
        });

        /*m_textSalenum.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if(event.getAction() == KeyEvent.ACTION_DOWN){
                    if ((keyCode == EditorInfo.IME_ACTION_DONE) || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                        doQueryWithSaleNum("추가");
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
                doQueryWithSaleNum("배송목록");
            }
        });

        m_delverly_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //배송 조회 가능 수량 체크
                if (getRegCount(0)) {
                    upu.showDialog("등록 가능한 전표가 없습니다.", 0);
                    return;
                }

                //----------------------------------------//
                // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
                //----------------------------------------//
                //AlertDialog.Builder alert_bulder = new AlertDialog.Builder(mContext);
                AlertDialog.Builder alert_bulder = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
                //----------------------------------------//
                alert_bulder.setMessage("배송중으로 변경 하시겠습니까?")
                        .setCancelable(false)
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                regDeliveryList();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alert_bulder.create();
                alert.setTitle("배송구분 변경");
                alert.show();
            }
        });

        //배송등록 취소
        m_delverly_reg_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //배송 조회 가능 수량 체크
                if (getRegCount(1)) {
                    upu.showDialog("취소 가능한 전표가 없습니다.", 0);
                    return;
                }

                //----------------------------------------//
                // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
                //----------------------------------------//
                //AlertDialog.Builder alert_bulder = new AlertDialog.Builder(mContext);
                AlertDialog.Builder alert_bulder = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
                //----------------------------------------//
                alert_bulder.setMessage("배송중 전표를 취소 하시겠습니까?\r\n선택된 전표 모두 취소 됩니다.")
                        .setCancelable(false)
                        .setPositiveButton("네", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                setDelverly_cancle();
                            }
                        })
                        .setNegativeButton("아니요", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = alert_bulder.create();
                alert.setTitle("배송구분 변경");
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
                            doQueryWithSaleNum("추가");
                        }
                    }
                }
        );
    }
}

    //배달지정자 등록
    private void setDelverly_Sender(){


        if(getRegCount(0)){
            upu.showDialog("등록할 배송 전표가 올바르지 않습니다.", 0);
            return;
        }

        String query = "Select User_ID, User_Name, Tel1 From Admin_User Where APP_USER_GRADE='4'";

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

                    final ArrayList<HashMap<String, String>> sender_list = new ArrayList<HashMap<String,String>>();
                    String[] sender = new String[results.length()];
                    for(int i =0; i < results.length(); i++) {
                        HashMap<String, String> m_tempProduct; // 배송목록 리스트
                        try {
                            m_tempProduct = JsonHelper.toStringHashMap(results.getJSONObject(i));
                            sender[i] = m_tempProduct.get("User_Name");
                            sender_list.add(m_tempProduct);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Dialog));
                    builder.setTitle("배달지정자 등록");
                    builder.setItems(sender, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                           HashMap<String, String> map = sender_list.get(which);
                           regDeliveryList_one(map.get("User_ID"));
                           dialog.dismiss();
                        }
                    });
                    builder.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });

                        /*builder.setPositiveButton("배송해제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                regDeliveryList_back();
                            }
                        });*/

                    builder.show();



                } else {
                    //이전 검색된 내용이 있다면 삭제하고 재 검색 합니다.
                    Toast.makeText(getApplicationContext(), "검색된 배달자가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(tips.getSHOP_IP() + ":" + tips.getSHOP_PORT(), tips.getDB_NAME(), tips.getDB_ID(), tips.getDB_PW(), query);


    }


    public void viewDetail(int position){

        HashMap<String, String> map = mfillMaps.get(position);
        Intent intent = new Intent(mContext, ManageHonmeDeliveryDetailActivity.class);
        intent.putExtra("sale_num", map.get("전표번호"));
        intent.putExtra("status", map.get("배송구분"));
        //입금이 완료되지 않은 전표는 배달 입금 구분을 표시해 줘야 합니다.
        if(map.get("입금완료").equals("미입금") && map.get("배달구분").equals("전화")){
            intent.putExtra("payment", map.get("입금구분"));
        }else {
            intent.putExtra("payment", map.get("입금완료"));
        }
        intent.putExtra("delivery_gubun", map.get("배달구분"));
        intent.putExtra("cus_code", map.get("회원번호"));
        intent.putExtra("cus_name", map.get("회원명"));
        intent.putExtra("phone_num", map.get("전화번호"));
        intent.putExtra("hp_num", map.get("휴대폰번호"));
        intent.putExtra("address", map.get("주소"));
        intent.putExtra("address1", map.get("상세주소"));
        intent.putExtra("total_price", map.get("받을금액"));
        intent.putExtra("user_id", map.get("배송자"));

        startActivity(intent);

    }

    public void setSelectOne(int position){

        HashMap<String, String> map = mfillMaps.get(position);

        String sel = map.get("선택");
        if(sel.equals("true")){
            map.put("선택", "false");
        }else{
            map.put("선택", "true");
        }

        mfillMaps.set(position, map);
        m_adapter.notifyDataSetChanged();

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
                    m_textSalenum.setText(barcode);
                    doQueryWithSaleNum("추가");

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    //Method

    /**
     * 0 : 배송전 검색 , 1: 같은 포스번호의 배송중 검색
     * @return true : 변경가능한 전표가 없슴
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

                    if("true".equals(map.get("선택")) && map.get("배송구분").equals("배송전")){
                        count++;
                    }
                }
                break;
            case 1:
                for(int i=0; i < mfillMaps.size(); i++){
                    HashMap<String, String> map = mfillMaps.get(i);
                    if("true".equals(map.get("선택")) && map.get("배송구분").equals("배송중") && map.get("배송자").equals(tips.getUSER_ID())){
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

        if(search.equals("추가")){
            if(isCompareSaleNum(salenum)){
                upu.showDialog("이미 목록에 있습니다.", 0);
                return;
            }
        }else{
            mfillMaps.clear();
        }

        String todayMonth = upu.getTodayDataMonth();
        String today = upu.getTodayData();
        String onlyToday = "";

        //날자를 오늘로 한정합니다.
        if(m_date_check.isChecked()){
            onlyToday = "AND c.Sale_Date = '"+today+"' ";
        }

        if(!salenum.equals("")){
            todayMonth = getSaleNumPerfix(salenum);
            salenum = " AND replace(c.Sale_Num, '-', '')='"+salenum+"' ";
        }

        //0:배송전, 1:배송중, 2:배송완료
        int delivery_state = m_delverly_gubun.getSelectedItemPosition()-1;
        String state = "";
        if(delivery_state >= 0){
            state = " AND IsNull(c.Delivery_State, '0')='"+delivery_state+"' ";
        }

        int delivery_deposit = m_delverly_deposit.getSelectedItemPosition();
        String deposit = "";
        switch(delivery_deposit){
            case 1: //미입금 = 전화배달, 미입금
                deposit = "AND (c.Delivery_YN ='2' AND ISNULL(inF_YN,'0')='0') ";
                break;
            case 2: //입금 = 내방배달, 입금
                deposit = "AND (c.Delivery_YN = '1' or ISNULL(inF_YN,'0')='1') ";
                break;
        }

        /*if(delivery_deposit != 0){
            //deposit = "AND Isnull(c.Delivery_Deposit, '0')='"+(delivery_deposit-1)+"' ";
            deposit = "AND ISNULL(inF_YN,'0')='"+(delivery_deposit-1)+"' ";
        }*/

        /*String search = m_btnSearch.getText().toString();*/
        String myDelivery = "";
        if(search.equals("배송목록")){
            //deposit = " AND Isnull(c.Delivery_Deposit, '0')='0' ";
            //deposit = "";
            myDelivery = " AND IsNull(c.Delivery_Sender, '')='"+tips.getUSER_ID()+"' ";
        }else if(search.equals("추가")){
            onlyToday = "";
        }

        query = "Select c.Sale_Num as '전표번호', (c.Sale_Date+' '+C.Sale_Time) as '일자', c.Cus_Code as '회원번호', ISNULL(B.Cus_Name, '') as '회원명', "
                + "isnull(b.Cus_Tel, '') as 전화번호, isnull(b.Cus_Mobile, '') as 휴대폰번호, ";

        if (tips.getEN_USE().equals("1")) {
            query += "isnull(b.en_uKey1, '') as 전화암호화, isnull(b.en_uKey2, '') as 휴대폰암호화, ";
        }
        query += "isnull(b.Address1, '') as 주소, isnull(b.Address2, '') as '상세주소', c.Close_YN as '마감', c.Gift_Pri as Gift_Pri, "
                + "'판매금액' = CASE WHEN c.Delivery_YN ='2' THEN a.B_TotalMoney ELSE c.Cash_pri + c.Card_Pri + c.Dec_Pri END, "
                + "c.CMS_Pri+c.Sub_Pri+c.Cut_Pri+c.Gift_Pri+c.Cus_PointUse+c.Cashback_PointUse + c.bc_coupon_dc + c.bc_card_dc + isnull(c.APP_DCPri,0) as '총할인금액', "
                + "c.Rec_Pri as '받을금액', '배달적립구분' = CASE WHEN c.Delivery_YN ='2' THEN a.tranpoint_chk ELSE 0 END, "
                + "c.cus_point as '적립포인트', c.bir_point as '생일포인트', c.cut_point as '절사포인트', c.writer as '등록자', "
                + "'수금일자' = CASE WHEN c.Delivery_YN ='2' THEN a.inDate ELSE c.Sale_Date + ' ' + C.Sale_Time END, "
                + "'수금포스' =  CASE WHEN c.Delivery_YN ='2' THEN a.inPOS ELSE c.Pos_No END, "
                + "'현금입금' = CASE WHEN c.Delivery_YN ='2' THEN ISNULL(a.inCash_Pri,0) ELSE c.Cash_Pri END, "
                + "'카드입금' = CASE WHEN c.Delivery_YN ='2' THEN ISNULL(a.inCard_Pri,0) ELSE c.Card_Pri END, "
                + "'외상입금' = CASE WHEN c.Delivery_YN ='2' THEN ISNULL(a.inDec_Pri,0) ELSE c.Dec_Pri END, "
                + "'입금자' = CASE WHEN c.Delivery_YN ='2' THEN ISNULL((Select b.USER_NAME From ADMIN_USER B WHERE b.USER_ID=a.inUser_ID),'') ELSE c.writer END, "
                + "'입금완료' = CASE WHEN c.Delivery_YN ='2' THEN CASE WHEN ISNULL(inF_YN,'0') =0 THEN '미입금' ELSE '입금' END ELSE '입금' END, "
                + "'배달구분' = CASE WHEN c.Delivery_YN ='2' THEN '전화' ELSE '내방' END, "
                + "'반품구분' = CASE WHEN A.inBanpum_YN ='1' THEN '미입금 반품' ELSE '' END, "
                + "isnull(c.Delivery_State, '0') as '배송구분', isnull(c.Delivery_Deposit, '0') as '입금구분', "
                + "isnull(c.Delivery_Sender, '') as '배송자', "
                + "'배송자명' = CASE WHEN isnull(c.Delivery_Sender, '') = '' THEN isnull(c.Delivery_Sender, '') ELSE (select User_Name from admin_user where user_id=c.Delivery_Sender) END "
                + "From SAT_"+todayMonth+" c left join Customer_Info b ON c.cus_code = b.cus_code LEFT JOIN BaeDal_List A ON c.Sale_Num = a. B_JeonPyo "
                + "WHERE Return_Chk='0' AND Befor_Jeonpyo IS NULL AND c.Delivery_YN <> '0' "+salenum+state+deposit+myDelivery+onlyToday
                + "ORDER BY c.SALE_NUM ";

        Log.d(TAG, tips.getSHOP_IP() + ":" + tips.getSHOP_PORT()+ "TIPS"+ "sa"+ "tips"+ query);

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

                    for(int i =0; i < results.length(); i++) {
                        HashMap<String, String> m_tempProduct; // 배송목록 리스트
                        try {
                            m_tempProduct = JsonHelper.toStringHashMap(results.getJSONObject(i));

                            m_tempProduct.put("선택", "false");
                            m_tempProduct.put("판매금액", StringFormat.convertToIntNumberFormat(m_tempProduct.get("판매금액")));
                            m_tempProduct.put("받을금액", StringFormat.convertToIntNumberFormat(m_tempProduct.get("받을금액")));
                            m_tempProduct.put("배송구분", getDeliveryStateToString(m_tempProduct.get("배송구분")));

                            if(m_tempProduct.get("배달구분").equals("전화")) {
                                m_tempProduct.put("입금구분", getDeliveryDepositToString(m_tempProduct.get("입금구분")));
                            }else{
                                m_tempProduct.put("입금구분", "내방배달");
                            }

                            try {

                                if (tips.getEN_USE().equals("1")) {
                                    try {
                                        String phone = new StringEncrypter(tips.getKEY(), tips.getIV()).decrypt(m_tempProduct.get("전화암호화"));
                                        m_tempProduct.put("전화번호", phone);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    try {
                                        String hp = new StringEncrypter(tips.getKEY(), tips.getIV()).decrypt(m_tempProduct.get("휴대폰암호화"));
                                        m_tempProduct.put("휴대폰번호", hp);
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
                    //이전 검색된 내용이 있다면 삭제하고 재 검색 합니다.
                    Toast.makeText(getApplicationContext(), "검색된 전표가 없습니다.", Toast.LENGTH_SHORT).show();
                }

                m_textSalenum.setText("");
                m_textSalenum.requestFocus();
                m_adapter.notifyDataSetChanged();
            }
            //}).execute(tips.getSHOP_IP() + ":" + tips.getSHOP_PORT(), "TIPS", "sa", "tips", query);
        }).execute(tips.getSHOP_IP() + ":" + tips.getSHOP_PORT(), tips.getDB_NAME(), tips.getDB_ID(), tips.getDB_PW(), query);


    }


    //이미등록된 전표 검색 true : 목록에 있슴, false : 목록에 없슴
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
            if(map.get("전표번호").replace("-", "").equals(salenum)){
                return true;
            }
        }

        return false;
    }


    //배송등록
    private void regDeliveryList(){

        ArrayList<String> query_list = new ArrayList<String>();
        final ArrayList<String> phone_num = new ArrayList<>();
        for(int i = 0; i < mfillMaps.size(); i++){
            HashMap<String, String> map = mfillMaps.get(i);
            String query = "";
            if("true".equals(map.get("선택")) && map.get("배송구분").equals("배송전")){
                String salenum =  map.get("전표번호");
                String todayMonth = getSaleNumPerfix(salenum);
                query = "Update Sat_"+todayMonth+" set Delivery_State='1', Delivery_Deposit='0', Delivery_Sender='"+tips.getUSER_ID()+"' " +
                        "Where Sale_Num='"+salenum+"'; ";
                query_list.add(query);

                //sms보내기
                if(map.get("휴대폰번호").length() > 9){
                    phone_num.add(map.get("휴대폰번호"));
                }
            }
        }

        if(query_list.size() <= 0){
            Toast.makeText(this, "등록할 전표가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // 콜백함수와 함께 실행
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
                Toast.makeText(mContext, "등록을 완료 하였습니다.", Toast.LENGTH_SHORT).show();
                //doQueryWithSaleNum("배송목록");
                getSelectListManager();

                //문자 전송 부분을 삭제 합니다.
                /*if(phone_num.size() > 0 && tips.getTRAN_MSG().length() > 0){
                    Log.d(TAG, "문자전송을 합니다.");
                    for (String phone:phone_num) {
                        //String contents = "안녕하세요~! "+tips.getSHOP_NAME()+" 입니다. 주문하신 상품의 배송이 시작되었습니다. 빠르고 안전하게 배송하겠습니다.";

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
                Toast.makeText(mContext, "등록 실패 하였습니다.\r\n"+msg, Toast.LENGTH_SHORT).show();
            }
        }).execute(arraylist, query_list);
    }


    //배달지정자 등록하기
    private void regDeliveryList_one(String sender){

        ArrayList<String> query_list = new ArrayList<String>();
        final ArrayList<String> phone_num = new ArrayList<>();
        for(int i = 0; i < mfillMaps.size(); i++){
            HashMap<String, String> map = mfillMaps.get(i);
            String query = "";
            if("true".equals(map.get("선택")) && map.get("배송구분").equals("배송전")){
                String salenum =  map.get("전표번호");
                String todayMonth = getSaleNumPerfix(salenum);
                query = "Update Sat_"+todayMonth+" set Delivery_State='1', Delivery_Deposit='0', Delivery_Sender='"+sender+"' " +
                        "Where Sale_Num='"+salenum+"'; ";
                query_list.add(query);

                //sms보내기
                if(map.get("휴대폰번호").length() > 9){
                    phone_num.add(map.get("휴대폰번호"));
                }
            }
        }

        if(query_list.size() <= 0){
            Toast.makeText(this, "등록할 전표가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // 콜백함수와 함께 실행
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
                Toast.makeText(mContext, "등록을 완료 하였습니다.", Toast.LENGTH_SHORT).show();
                //doQueryWithSaleNum("배송목록");
                getSelectListManager();

                /*if(phone_num.size() > 0 && tips.getTRAN_MSG().length() > 0){
                    Log.d(TAG, "문자전송을 합니다.");
                    for (String phone:phone_num) {
                        //String contents = "안녕하세요~! "+tips.getSHOP_NAME()+" 입니다. 주문하신 상품의 배송이 시작되었습니다. 빠르고 안전하게 배송하겠습니다.";

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
                Toast.makeText(mContext, "등록 실패 하였습니다.\r\n"+msg, Toast.LENGTH_SHORT).show();

            }
        }).execute(arraylist, query_list);


    }

    //배송중 취소
    private void setDelverly_cancle(){

        ArrayList<String> query_list = new ArrayList<String>();
        final ArrayList<String> phone_num = new ArrayList<>();
        if("0".equals(tips.getAPP_USER_GRADE()) || "1".equals(tips.getAPP_USER_GRADE())){
            //관리자의 경우 모든 배송내역을 취소 할수 있습니다.
            for (int i = 0; i < mfillMaps.size(); i++) {
                HashMap<String, String> map = mfillMaps.get(i);
                String query = "";
                if ("true".equals(map.get("선택")) && map.get("배송구분").equals("배송중")) {
                    String salenum = map.get("전표번호");
                    String todayMonth = getSaleNumPerfix(salenum);
                    query = "Update Sat_" + todayMonth + " set Delivery_State=null, Delivery_Deposit=null, Delivery_Sender=null " +
                            "Where Sale_Num='" + salenum + "'; ";
                    query_list.add(query);
                }
            }
        }else {
            //배송자의 경우 자기의 배송만 취소 할수 있습니다.
            for (int i = 0; i < mfillMaps.size(); i++) {
                HashMap<String, String> map = mfillMaps.get(i);
                String query = "";
                if ("true".equals(map.get("선택")) && map.get("배송구분").equals("배송중") && map.get("배송자").equals(tips.getUSER_ID())) {
                    String salenum = map.get("전표번호");
                    String todayMonth = getSaleNumPerfix(salenum);
                    query = "Update Sat_" + todayMonth + " set Delivery_State='', Delivery_Deposit='', Delivery_Sender='' " +
                            "Where Sale_Num='" + salenum + "'; ";
                    query_list.add(query);
                }
            }
        }

        if(query_list.size() <= 0){
            Toast.makeText(this, "등록할 전표가 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // 콜백함수와 함께 실행
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
                Toast.makeText(mContext, "배송 취소를 완료 하였습니다.", Toast.LENGTH_SHORT).show();
                //doQueryWithSaleNum("배송목록");
                getSelectListManager();
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(mContext, "취소 실패 하였습니다.\r\n"+msg, Toast.LENGTH_SHORT).show();
            }
        }).execute(arraylist, query_list);

    }



    //전표 앞부분 가져오기
    private String getSaleNumPerfix(String junpyo){
        String sale_num = "";
        sale_num = junpyo.replace("-", "");
        sale_num = sale_num.substring(0, 6);
        Log.d(TAG, sale_num);

        return sale_num;
    }


    private String getDeliveryStateToString(String state_now){
        String status = "배송전";
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
        String status = "미입금";
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
        if(button.getText().equals("전체선택")){
            button.setText("선택해제");
        }else{
            button.setText("전체선택");
            selectItem = "false";
        }

        for(int i =0; i<mfillMaps.size(); i++){
            HashMap<String, String> map = mfillMaps.get(i);

            map.put("선택", selectItem);
            mfillMaps.set(i, map);
        }

        m_adapter.notifyDataSetChanged();
    }

    private void getSelectListManager(){

        //배달관리자라면 자기 배송을 중심으로 검색합니다.
        if("4".equals(tips.getAPP_USER_GRADE())){
            //재검색
            doQueryWithSaleNum("배송목록");
        }else{
            //최고관리자라면 전체 검색되게 변경
            doQueryWithSaleNum("");
        }

        //선택 버튼 초기화
        Button button = (Button)findViewById(R.id.button_allselect);
        button.setText("전체선택");

    }

    //초기화
    public void setRenew(View view){

        mfillMaps.clear();
        m_textSalenum.setText("");

        m_delverly_gubun.setSelection(0);
        m_delverly_deposit.setSelection(0);

        m_adapter.notifyDataSetChanged();
    }

    //2017-04 m3mobile 추가
    @Override
    protected void onResume() {
        super.onResume();
        if(m3Mobile){
            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_textSalenum,new M3MoblieBarcodeScanBroadcast.M3MoblieCallbackInterface(){
                @Override
                public void onRequestCompleted(boolean results) {
                    if (results) {
                        doQueryWithSaleNum("추가");
                    }
                }
            });
            M3MoblieBarcodeScanBroadcast.setOnResume();
        }

        //재검색
        //doQueryWithSaleNum("배송목록");
        getSelectListManager();
    }

    //2017-04 m3mobile 추가
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
            actionbar.setTitle("배달배송관리");

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
        Log.d(TAG, "버튼이 눌렸나요>?");
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
                String str = temp.get("배송구분");
                if(str.equals("배송중")){
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
                    Log.d(TAG, "선택되었씁니다.");
                    viewDetail(pos);
                }
            });

            CheckBox chk = (CheckBox)view.findViewById(R.id.item0);
            chk.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "변경되었습니다.");
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
                                if(text.equals("배송중")) {
                                    setTextColor((TextView) v, text, Color.RED);
                                }else if(text.equals("배송전")){
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


