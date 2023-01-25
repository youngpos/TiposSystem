package tipsystem.tips;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL3;
import tipsystem.utils.StringFormat;
import tipsystem.utils.TIPS_Config;
import tipsystem.utils.UserPublicUtils;

public class ManageHonmeDeliveryDetailActivity extends Activity {

    //TAG
    private String TAG = "DeliveryDetail";

    //System
    private TIPS_Config tips;
    private Context mContext;
    private UserPublicUtils upu;

    //UI
    private TextView m_salenum;
    private String m_deliverygubun;
    private String m_paygubun;

    private TextView m_status;

    private TextView m_total_count;
    private String total_price;
    private String phone_num = "";
    private String hp_num = "";
    private String user_id= "";

    private Button m_payment;
    private Button m_deliverycomplete;
    private String del_gubun;

    //Param
    private String getSaleNum;
    private double tax_0 = 0;
    private double tax_1 = 0;

    //ListView
    private ListView m_detaillist;
    private SimpleAdapter m_adapter;
    private ArrayList<HashMap<String, String>> mfillMaps;

    // loading bar
    private ProgressDialog dialog;

    //카드결제
    private String callType = "";   //card, cash, point
    private String callMode = "";   //pay, cancel

    private HashMap<String, String> card_info;

    //카드결제 가능여부
    private boolean card_setting_yn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_honme_delivery_detail);

        //NOTE : 결제 결과를 받을 브로드 캐스트 등록
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_MAIN));

        //System
        mContext = this;
        tips = new TIPS_Config(mContext);
        upu = new UserPublicUtils(mContext);

        card_info = tips.getCardInfo();

        String state = "";
        String cus_code = "";
        String cus_name = "";

        String adress = "";
        String adress1 = "";


        //Parameter
        try {
            getSaleNum = getIntent().getStringExtra("sale_num");
            m_deliverygubun = getIntent().getStringExtra("status");
            del_gubun = getIntent().getStringExtra("delivery_gubun");
            m_paygubun = getIntent().getStringExtra("payment");
            cus_code = getIntent().getStringExtra("cus_code");
            cus_name = getIntent().getStringExtra("cus_name");
            phone_num = getIntent().getStringExtra("phone_num");
            hp_num = getIntent().getStringExtra("hp_num");
            adress = getIntent().getStringExtra("address");
            adress1 = getIntent().getStringExtra("address1");
            total_price = getIntent().getStringExtra("total_price");
            user_id = getIntent().getStringExtra("user_id");
        }catch(Exception e){
            upu.showDialog("전달값을 확인해 주세요!", 0);
            finish();
        }

        //UI
        m_salenum = (TextView)findViewById(R.id.textview_salenum);
        m_salenum.setText(getSaleNum);
        m_status = (TextView)findViewById(R.id.textview_state);
        m_status.setText(m_deliverygubun);

        TextView m_paygubun2 = (TextView)findViewById(R.id.textview_payment);
        m_paygubun2.setText(m_paygubun);
        TextView m_membercode = (TextView)findViewById(R.id.textview_cuscode);
        m_membercode.setText(cus_code);
        TextView m_membername = (TextView)findViewById(R.id.textview_cusname);
        m_membername.setText(cus_name);
        TextView m_phonenumber = (TextView)findViewById(R.id.textview_phonenumber);
        m_phonenumber.setText(phone_num);
        TextView m_hpnumber = (TextView)findViewById(R.id.textview_hpumber);
        m_hpnumber.setText(hp_num);
        TextView m_address = (TextView)findViewById(R.id.textview_address);
        m_address.setText(adress);
        TextView m_address1 = (TextView)findViewById(R.id.textview_address1);
        m_address1.setText(adress1);
        m_total_count = (TextView)findViewById(R.id.textview_totalcount);
        TextView m_total_price = (TextView) findViewById(R.id.textview_totalpayment);
        m_total_price.setText(total_price);

        //전화걸기
        final TextView phonecall = (TextView)findViewById(R.id.textview_phonecall);
        final TextView hpcall = (TextView)findViewById(R.id.textview_hpcall);

        phonecall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(phone_num.length() > 0){
                    startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:"+phone_num)));
                }
            }
        });

        hpcall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hp_num.length() > 0){
                    startActivity(new Intent("android.intent.action.CALL", Uri.parse("tel:"+hp_num)));
                }
            }
        });

        m_payment = (Button)findViewById(R.id.buttonPayment);
        m_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regDeliveryList_back();
            }
        });


        m_deliverycomplete = (Button)findViewById(R.id.buttonComplete);

        //listview
        m_detaillist = (ListView)findViewById(R.id.listview_delverlydetaillist);
        mfillMaps = new ArrayList<>();

        //목록 리스트
        String[] from = new String[] { "순번", "바코드", "단가", "상품명", "수량", "판매금액"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6 };
        m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_delivery_list2, from, to);
        m_detaillist.setAdapter(m_adapter);

        //Listioner
        m_deliverycomplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String str_buttonName = m_deliverycomplete.getText().toString();
                if(str_buttonName.equals("배송등록")){
                    regDeliveryList_one();
                }else if(str_buttonName.equals("입금처리")){

                    if(!m_deliverygubun.equals("배송중")){
                        upu.showDialog("배송중이 아닌 전표입니다.", 0);
                        return;
                    }

                    if(m_paygubun.equals("입금")){

                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Dialog));
                        builder.setTitle("배송 완료");
                        builder.setNegativeButton("입금완료", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                if(!tips.getUSER_ID().equals(user_id)){
                                    upu.showDialog("등록한 배송자가 아닙니다.", 0);
                                    return;
                                }
                                setDeliveryComplete(0);
                            }
                        });

                        builder.setPositiveButton("배송해제", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                regDeliveryList_back();
                            }
                        });

                        builder.show();
                    }else{

                        String[] deposit = getResources().getStringArray(R.array.deposit_gubun_summry);
                        deposit[0] = "미입금";

                        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Dialog));
                        builder.setTitle("배송 완료(입금선택)");
                        builder.setItems(deposit, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(!tips.getUSER_ID().equals(user_id)){
                                    upu.showDialog("등록한 배송자가 아닙니다.", 0);
                                    return;
                                }

                                switch(which){
                                    case 0:
                                    case 1:
                                    case 2:
                                    case 3:
                                    case 4:
                                        setDeliveryComplete(which);
                                    break;
                                    case 5: //카드
                                        //true 셋팅 잘됨, false 셋팅 안됨
                                        if(!getCardInfo()){
                                            upu.showDialog("환경설정에서 카드사 정보를 입력해 주세요", 0);
                                            return;
                                        }
                                        clickCard();
                                        break;
                                    case 6: //현영
                                        //true 셋팅 잘됨, false 셋팅 안됨
                                        if(!getCardInfo()){
                                            upu.showDialog("환경설정에서 카드사 정보를 입력해 주세요", 0);
                                            return;
                                        }
                                        clickCash();
                                        break;
                                }

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
                    }

                }else if(str_buttonName.equals("배송완료")){

                    AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(mContext, android.R.style.Theme_Dialog));
                    builder.setTitle("내방배달 배송완료");
                    builder.setNegativeButton("배송완료", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            if(!tips.getUSER_ID().equals(user_id)){
                                upu.showDialog("등록한 배송자가 아닙니다.", 0);
                                return;
                            }
                            setDeliveryComplete(0);
                        }
                    });

                    /*builder.setPositiveButton("배송해제", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    regDeliveryList_back();
                                }

                    });
*/
                    builder.show();

                    //upu.showDialog("배송이 완료된 전표입니다.", 0);
                }

            }
        });

        //배송상태 변경하기
        setDelivergubunColor(m_deliverygubun);

        //리스트 뷰
        getSaleNumList();

    }

    //배송등록
    private void regDeliveryList_one(){

        String query = "";
        ArrayList<String> query_list = new ArrayList<>();
        String todayMonth = getSaleNumPerfix(getSaleNum);
        query = "Update Sat_"+todayMonth+" set Delivery_State='1', Delivery_Deposit='0', Delivery_Sender='"+tips.getUSER_ID()+"' " +
                "Where Sale_Num='"+getSaleNum+"'; ";
        query_list.add(query);

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

                if(hp_num.length() > 0 && tips.getTRAN_MSG().length() > 0){
                    //String contents = "안녕하세요~! "+tips.getSHOP_NAME()+" 입니다. 주문하신 상품의 배송이 시작되었습니다. 빠르고 안전하게 배송하겠습니다.";
                    String contents = tips.getTRAN_MSG();
                    upu.sendSMS(contents, hp_num);
                    Log.d(TAG, "전송완료");
                }else{
                    Log.d(TAG, "전송실패");
                }

                finish();
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(mContext, "등록 실패 하였습니다.\r\n"+msg, Toast.LENGTH_SHORT).show();

            }
        }).execute(arraylist, query_list);


    }

    //배송등록
    private void regDeliveryList_back(){

        String query = "";
        ArrayList<String> query_list = new ArrayList<>();

        if(!tips.getUSER_ID().equals(user_id)){
            upu.showDialog("등록한 배송자가 아닙니다.", 0);
            return;
        }

        String todayMonth = getSaleNumPerfix(getSaleNum);
        query = "Update Sat_"+todayMonth+" set Delivery_State='', Delivery_Deposit='', Delivery_Sender='' " +
                "Where Sale_Num='"+getSaleNum+"'; ";
        query_list.add(query);

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
                Toast.makeText(mContext, "해제를 완료 하였습니다.", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(mContext, "등록 실패 하였습니다.\r\n"+msg, Toast.LENGTH_SHORT).show();

            }
        }).execute(arraylist, query_list);


    }


    //카드정보 셋팅이 잘되어 있는지 확인 합니다.
    private boolean getCardInfo(){
        if(card_info.get("agencyApp").equals("") && card_info.get("bizNo").equals("") && card_info.get("serialNo").equals("") && card_info.get("downPasswordNo").equals("") && card_info.get("localNo").equals("") ){
            return false;
        }
        return true;
    }


    //전표 앞부분 가져오기
    private String getSaleNumPerfix(String junpyo){
        String sale_num = "";
        sale_num = junpyo.replace("-", "");
        sale_num = sale_num.substring(0, 6);
        Log.d(TAG, sale_num);

        return sale_num;
    }

    private void setDeliveryComplete(int deposit){


        if(deposit == 5 && deposit == 6){

            final int depo = deposit;

            String todayMonth = getSaleNumPerfix(getSaleNum);
            String query = "";

            ArrayList<String> query_list = new ArrayList<>();
            if(m_deliverygubun.equals("배송중")){
                query = "Update Sat_"+todayMonth+" set Delivery_State='2', Delivery_Deposit='"+depo+"', Delivery_Sender='"+tips.getUSER_ID()+"' " +
                        "Where Sale_Num='"+getSaleNum+"'; ";
            }

            query_list.add(query);

                        /*// 로딩 다이알로그
                        dialog = new ProgressDialog(this);
                        dialog.setMessage("Loading....");
                        dialog.setCancelable(false);
                        dialog.show();*/

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
                                /*dialog.dismiss();
                                dialog.cancel();*/
                    Toast.makeText(mContext, "등록을 완료 하였습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                }

                @Override
                public void onRequestFailed(int code, String msg) {
                                /*dialog.dismiss();
                                dialog.cancel();*/
                    Toast.makeText(mContext, "등록 실패 하였습니다.\r\n"+msg, Toast.LENGTH_SHORT).show();
                }
            }).execute(arraylist, query_list);

        }else {

            final int depo = deposit;

            AlertDialog.Builder alt_bld = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
            alt_bld.setMessage("입금 처리 하시겠습니까?").setCancelable(
                    false).setPositiveButton("네",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Action for 'Yes' Button

                            String todayMonth = getSaleNumPerfix(getSaleNum);
                            String query = "";

                            ArrayList<String> query_list = new ArrayList<>();
                            if (m_deliverygubun.equals("배송중")) {
                                query = "Update Sat_" + todayMonth + " set Delivery_State='2', Delivery_Deposit='" + depo + "', Delivery_Sender='" + tips.getUSER_ID() + "' " +
                                        "Where Sale_Num='" + getSaleNum + "'; ";
                            }

                            query_list.add(query);

                            /*// 로딩 다이알로그
                            dialog = new ProgressDialog(this);
                            dialog.setMessage("Loading....");
                            dialog.setCancelable(false);
                            dialog.show();*/

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
                                    /*dialog.dismiss();
                                    dialog.cancel();*/
                                    Toast.makeText(mContext, "등록을 완료 하였습니다.", Toast.LENGTH_SHORT).show();
                                    finish();
                                }

                                @Override
                                public void onRequestFailed(int code, String msg) {
                                    /*dialog.dismiss();
                                    dialog.cancel();*/
                                    Toast.makeText(mContext, "등록 실패 하였습니다.\r\n" + msg, Toast.LENGTH_SHORT).show();
                                }
                            }).execute(arraylist, query_list);

                        }
                    }).setNegativeButton("아니요",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Action for 'NO' Button
                            dialog.cancel();
                        }
                    });

            AlertDialog alert = alt_bld.create();
            // Title for AlertDialog
            alert.setTitle("입금처리 확인");
            // Icon for AlertDialog
            //alert.setIcon(R.drawable.icon);
            alert.show();

        }
    }


    private void setDelivergubunColor(String str){
        switch(str){
            case "배송전":
                m_status.setTextColor(Color.GREEN);
                m_deliverycomplete.setText("배송등록");
                break;
            case "배송중":
                //배송등록된 아이디와 동일한 아이디일때 버튼 표시
                if(user_id.equals(tips.getUSER_ID())){
                    if(del_gubun.equals("전화")){
                        m_status.setTextColor(Color.RED);
                        //m_payment.setVisibility(View.VISIBLE);
                        m_deliverycomplete.setText("입금처리");
                    }else{ //내방배달처리
                        m_status.setTextColor(Color.WHITE);
                        m_deliverycomplete.setText("배송완료");
                    }
                }else{
                    m_deliverycomplete.setVisibility(View.GONE);
                }
                break;
            case "배송완료":
                m_status.setTextColor(Color.WHITE);
                //m_deliverycomplete.setText("배송완료");
                m_deliverycomplete.setVisibility(View.GONE);
                break;
            default:
                m_status.setTextColor(Color.WHITE);
                break;
        }
    }

    //전표 검색하기
    private void getSaleNumList(){

        if(getSaleNum.equals("")){
            upu.showDialog("전표 번호를 받지 못했습니다.", 0);
            finish();
        }

        String table_name = getSaleNumPerfix(getSaleNum);
        String query = "Select a.Sale_Seq as '순번', a.Barcode as '바코드', a.G_Name as '상품명', a.Sell_Pri as '단가', a.Tax_YN as '면과세', "
                     + "a.SellDC_Pri as '받을금액', a.Sale_Count as '수량', a.DC_Pri as '받은금액', a.TSell_Pri as '판매금액', "
                     + "a.TSell_RePri as '반품금액', a.Profit_Pri as '이익금', a.SaleDC_YN, '0', b.CMS_Pri,b.Sub_Pri,b.Cut_Pri,b.Gift_Pri, "
                     + "b.Cus_PointUse,b.Cashback_PointUse, a.S_Point, a.e_seq, "
                     + "a.e_gubun, a.e_barcode, isnull(b.APP_DCPri,0) APP_DCPri, "
                     + "CASE WHEN a.TAX_YN='1' THEN a.TSell_Pri-a.TSell_RePri ELSE 0 END '과세', "
                     + "CASE WHEN a.TAX_YN='0' THEN  a.TSell_Pri-a.TSell_RePri ELSE 0 END '면세' "
                     + "From Sad_"+table_name+" a , Sat_"+table_name+" b "
                     + "Where a.sale_num=b.sale_num and a.Sale_Num='"+getSaleNum+"' "
                     + "Order by a.Sale_Seq ASC ";

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

                int total_count = 0;

                if (results.length() > 0) {

                    for(int i =0; i < results.length(); i++) {
                        HashMap<String, String> m_tempProduct; // 배송목록 리스트
                        try {
                            m_tempProduct = JsonHelper.toStringHashMap(results.getJSONObject(i));

                            //수량
                            total_count += Integer.parseInt(m_tempProduct.get("수량"));
                            tax_0 += Double.parseDouble(m_tempProduct.get("면세"));
                            tax_1 += Double.parseDouble(m_tempProduct.get("과세"));

                            double total_price = Double.parseDouble(m_tempProduct.get("판매금액")) - Double.parseDouble(m_tempProduct.get("반품금액"));
                            m_tempProduct.put("판매금액", StringFormat.convertToIntNumberFormat(String.valueOf(total_price+"")));
                            m_tempProduct.put("단가", StringFormat.convertToIntNumberFormat(m_tempProduct.get("단가")));
                            mfillMaps.add(m_tempProduct);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }

                //면과세합
                Log.d(TAG, "면세 : " + tax_0 + ", 과세 : " + tax_1);

                m_total_count.setText(String.valueOf(total_count));
                m_adapter.notifyDataSetChanged();

            }
            //}).execute(tips.getSHOP_IP() + ":" + tips.getSHOP_PORT(), "TIPS", "sa", "tips", query);
        }).execute(tips.getSHOP_IP() + ":" + tips.getSHOP_PORT(), tips.getDB_NAME(), tips.getDB_ID(), tips.getDB_PW(), query);


    }

    public void kismobile(View view){

        //true 셋팅 잘됨, false 셋팅 안됨
        if(!getCardInfo()){
            upu.showDialog("환경설정에서 카드정보를 입력해 주세요", 0);
        }

        if(!m_deliverygubun.equals("배송중")){
            upu.showDialog("배송중이 아닌 전표입니다. ", 0);
            return;
        }

        if(!m_paygubun.equals("미입금")){
            upu.showDialog("입금된 전표입니다. ", 0);
            return;
        }

        String[] deposit = getResources().getStringArray(R.array.deposit_gubun_summry2);

        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        //AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
        //----------------------------------------//
        builder.setTitle("배송 완료(입금선택)");
        builder.setItems(deposit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                gotoCardPayMent(which);
                dialog.dismiss();
            }
        });
        builder.show();
        //startActivity(new Intent(this, KisCardPayment.class));

    }


    public void close(View view){
        this.finish();
    }


    private void gotoCardPayMent(int which){

        switch(which){
            case 0: //현영
                clickCash();
                break;
            case 1: //카드
                clickCard();
                break;
        }
    }



    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e(TAG,"Broadcast Receiving....................."+intent.hashCode()+"\n"+intent.toUri(0));

            try{

                HashMap<String, String> response = (HashMap<String, String>)intent.getSerializableExtra("result");
                Iterator<String> iterator = response.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String val = response.get(key);
                    if (val != null) val = val.trim();
                    Log.e(TAG, "RECEIVER key = " + key + ",   value = "+val);
                }

                Log.e(TAG,response.toString());

                String code = response.get("outReplyCode");

                //결제결과 저장하기
                //정상결제인지 체크
                if (code != null && code.equals("0000") && callMode.equalsIgnoreCase("pay")){
                    //결제완료
                    //2번 현영, 3번 카드
                    switch (callType){
                        case "card":
                            setDeliveryComplete(5);
                            break;
                        case "cash":
                            setDeliveryComplete(6);
                            break;
                    }
                }else if(code != null && !code.equals("0000")){
                    //결과 오류
                    upu.showDialog("결제 오류 \r\n 결과코드 : "+code+"\r\n메세지 : "+response.get("outDisplayMsg"), 0);
                }



                /*
                if (code != null && code.equals("0000") && callMode.equalsIgnoreCase("pay")) {

                    // 취소 정보가 있을 시 취소
                    final String approvalNo = response.get("outAuthNo");
                    final String approvalDate = response.get("outAuthDate");
                    upu.alert("승인번호 ["+approvalNo+"]의 결제를 취소 합니다",new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            callMode = "cancel";

                            Intent intent = new Intent(card_info.get("agencyApp"));
                            intent.putExtra("uuid",String.valueOf(System.currentTimeMillis()));
                            intent.putExtra("bizNo",card_info.get("bizNo"));
                            intent.putExtra("serialNo",card_info.get("serialNo"));
                            intent.putExtra("downPasswordNo",card_info.get("downPasswordNo"));
                            intent.putExtra("type",callType);
                            intent.putExtra("mode",callMode);
                            intent.putExtra("approvalNo",approvalNo);
                            intent.putExtra("approvalDate",approvalDate);

                            startActivity(intent);
                        }
                    });
                }
                */
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    };


    //카드 결제
    public void clickCard() {

        callType = "card";
        callMode = "pay";

        int total = 0;
        try {
            if(total_price.contains(",")){
                total_price = total_price.replace(",", "");
            }
            total = Integer.parseInt(total_price);
        }catch(NumberFormatException e){
            upu.showDialog("결제 금액이 잘못됐습니다.", 0);
            return;
        }

        // List Adapter 생성
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext, android.R.layout.select_dialog_item);
        adapter.add("일시불");
        adapter.add("1");
        adapter.add("2");
        adapter.add("3");
        adapter.add("4");
        adapter.add("5");
        adapter.add("6");
        adapter.add("7");
        adapter.add("8");
        adapter.add("9");
        adapter.add("10");
        adapter.add("11");
        adapter.add("12");


        //5만원 이상 서명
        if(total >= 50000) {
            //----------------------------------------//
            // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
            //----------------------------------------//
            //AlertDialog.Builder aDialog = new AlertDialog.Builder(mContext);
            AlertDialog.Builder aDialog = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
            //----------------------------------------//
            aDialog.setTitle("할부개월수");
            aDialog.setAdapter(adapter, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String str = adapter.getItem(which);

                    int installment = 0;
                    if(!str.equals("일시불")){
                        installment = Integer.parseInt(str);
                    }

                    int total = Integer.parseInt(total_price);
                    transCard(total, installment);
                }
            });

            aDialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });

            aDialog.show();
        }else{
            //5만원 이하일때 일시불로 결제 됩니다.
            transCard(total, 0);
        }
    }


    private void transCard(int total, int installment){

        Log.d(TAG,  "면세 " + tax_0);

        Intent intent = new Intent(card_info.get("agencyApp"));
        intent.putExtra("uuid",String.valueOf(System.currentTimeMillis()));
        intent.putExtra("bizNo",card_info.get("bizNo"));
        intent.putExtra("serialNo", card_info.get("serialNo"));
        intent.putExtra("downPasswordNo", card_info.get("downPasswordNo"));
        intent.putExtra("type",callType);
        intent.putExtra("mode",callMode);
        intent.putExtra("amount", total);
        intent.putExtra("taxFree", (int)tax_0);
        intent.putExtra("fee",0);
        intent.putExtra("installment",installment);

        startActivity(intent);

    }


    // 현금 영수증
    public void clickCash() {

        callType = "cash";
        callMode = "pay";

        int total = 0;
        try {
            if(total_price.contains(",")){
                total_price = total_price.replace(",", "");
            }
            total = Integer.parseInt(total_price);
        }catch(NumberFormatException e){
            upu.showDialog("결제 금액이 잘못됐습니다.", 0);
            return;
        }

        Intent intent = new Intent(card_info.get("agencyApp"));
        intent.putExtra("uuid",String.valueOf(System.currentTimeMillis()));
        intent.putExtra("bizNo",card_info.get("bizNo"));
        intent.putExtra("serialNo", card_info.get("serialNo"));
        intent.putExtra("downPasswordNo", card_info.get("downPasswordNo"));
        intent.putExtra("type",callType);
        intent.putExtra("mode",callMode);
        intent.putExtra("amount", total);
        intent.putExtra("taxFree", (int)tax_0);
        intent.putExtra("fee",0);
        intent.putExtra("cashReceiptType","personal");
        intent.putExtra("cashReceiptNo","");

        startActivity(intent);

    }

    @Override
    protected void onDestroy() {
        //NOTE : 결제 결과를 받을 브로드 캐스트 등록 해제
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

}
