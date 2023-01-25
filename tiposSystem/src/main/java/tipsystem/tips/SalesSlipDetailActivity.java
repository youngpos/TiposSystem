package tipsystem.tips;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;

public class SalesSlipDetailActivity extends Activity {

    private Context mContext;

    private JSONObject m_shop;
    //----------------------------------------//
    // 2022.05.26.본사서버 IP변경
    //----------------------------------------//
    private String m_ip = "";
    private String m_port = "";
    //----------------------------------------//
    // 2021.12.21. 매장DB IP,PW,DB 추가
    //----------------------------------------//
    private String m_uuid = "";
    private String m_uupw = "";
    private String m_uudb = "";
    //----------------------------------------//

    private ProgressDialog dialog;

    // head
    private TextView m_SaleDate;
    private TextView m_PosNo;
    private TextView m_SaleNum;
    private TextView m_Writer;

    // 합계
    private TextView m_seq;
    private TextView m_cnt;
    private TextView m_pri;

    // 결제
    private LinearLayout m_linearCash;
    private LinearLayout m_linearCard;
    private LinearLayout m_linearDec;

    private TextView m_CashPri;
    private TextView m_CardPri;
    private TextView m_DecPri;

    // 카드
    private LinearLayout m_CardInfo;
    private TextView m_CardNo;

    // 회원
    private LinearLayout m_CusInfo;
    private TextView m_CusCode;
    private TextView m_CusName;
    private TextView m_CusPoint;


    private ListView m_listviewDetail;  // 리스트 상세
    SimpleAdapter adapter1;
    SimpleAdapter adapter2;


    List<HashMap<String, String>> mfillMaps1 = new ArrayList<HashMap<String, String>>();
    List<HashMap<String, String>> mfillMaps2 = new ArrayList<HashMap<String, String>>();

    String m_tableName = "";
    String m_saleNum = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_slip_detail);

        mContext = this;

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

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 합계
        m_seq = (TextView)findViewById(R.id.textviewSeq);
        m_cnt = (TextView)findViewById(R.id.textviewCnt);
        m_pri = (TextView)findViewById(R.id.textviewPri);


        m_SaleDate = (TextView) findViewById(R.id.textviewSaleDate);
        m_PosNo = (TextView) findViewById(R.id.textviewPosNo);
        m_SaleNum = (TextView) findViewById(R.id.textviewSaleNum);
        m_Writer = (TextView) findViewById(R.id.textviewWriter);

        // 결제
        m_linearCash=(LinearLayout)findViewById(R.id.linearLayoutCash);
        m_linearCard=(LinearLayout)findViewById(R.id.linearLayoutCard);
        m_linearDec=(LinearLayout)findViewById(R.id.linearLayoutDec);
        m_CashPri = (TextView) findViewById(R.id.textviewCashPri);
        m_CardPri = (TextView) findViewById(R.id.textviewCardPri);
        m_DecPri = (TextView) findViewById(R.id.textviewDecPri);

        // 카드
        m_CardInfo = (LinearLayout) findViewById(R.id.linearLayoutCardInfo);
        m_CardNo = (TextView) findViewById(R.id.textviewCardNo);

        // 회원
        m_CusInfo = (LinearLayout) findViewById(R.id.linearLayoutCusInfo);
        m_CusCode = (TextView) findViewById(R.id.textviewCusCode);
        m_CusName = (TextView) findViewById(R.id.textviewCusName);
        m_CusPoint = (TextView) findViewById(R.id.textviewCusPoint);


        m_listviewDetail = (ListView) findViewById(R.id.listviewDetail);

        //String[] from1 = new String[]{"Sale_Date", "Sale_Time", "Pos_No", "Writer", "", "TSell_Pri"};
        //int[] to1 = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6};


        String[] from2 = new String[]{"Sale_Seq", "BarCode", "G_Name", "Sell_Pri", "Sale_Count", "TSell_Pri"};
        int[] to2 = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6};
        adapter2 = new SimpleAdapter(this, mfillMaps2, R.layout.activity_listview_slip_detail_list, from2, to2);
        m_listviewDetail.setAdapter(adapter2);

        m_tableName = getIntent().getStringExtra("tableName");
        m_saleNum = getIntent().getStringExtra("saleNum");

        //m_SaleNum.setText(m_saleNum);

        // 헤드 조회
        doQuery();
        // 상품조회
        //doQueryItem();

    }

    private void doQuery() {

        // 쿼리 작성하기
        String query = "";
        query = "select "
                + " IsNull(A.Sale_Date,'') AS Sale_Date, "  // 1 판매일자
                + " IsNull(A.Sale_Time,'') AS Sale_Time, "  // 2 판매시간
                + " IsNull(A.Pos_No,'') AS Pos_No, "     // 3 포스번호
                + " IsNull(A.Writer,'') AS Writer, "     // 4 판매자(계산원)

                + " IsNull(A.TSell_Pri,0) AS Tsell_Pri, "   // 5 판매금액
                + " IsNull(A.TSell_RePri,0) AS Tsell_RePri, " // 6 반품금액

                + " IsNull(A.Cash_Pri,0) AS Cash_Pri, "   // 7 현금
                + " IsNull(A.Card_Pri,0) AS Card_Pri, "   // 8 카드
                + " IsNull(A.Dec_Pri,0) AS Dec_Pri, "    // 9 외상

                + " IsNull(A.Card_No,'') AS Card_No, "   // 10 카드번호

                + " IsNull(A.Cus_Code,'') AS Cus_Code, "   // 11 회원포인트
                + " IsNull(B.Cus_Name,'') AS Cus_Name, "   // 12 회원명
                + " IsNull(A.cus_point,0) AS Cus_Point  "  // 13 발생포인트

                + " From SaT_" + m_tableName + " A Left Join Customer_Info B  "
                + " On A.Cus_code=b.Cus_code "
                + " Where A.Sale_num ='" + m_saleNum + "' ";

        //+ "order by office_name";

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
                    SlipHeadView(results);
                    doQueryItem();
                } else {
                    Toast.makeText(getApplicationContext(), "자료가 없습니다!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "자료를 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    private void doQueryItem() {

        mfillMaps2.removeAll(mfillMaps2);

        // 쿼리 작성하기
        String query = "";
        query = "select "
                + " a.Sale_Seq, "
                + " a.BarCode, "
                + " a.G_Name, "
                + " a.Sell_Pri, "
                + " a.Sale_Count, "
                + " A.TSell_Pri "
                + " From SaD_" + m_tableName + " a left join Goods b "
                + " On a.Barcode=b.Barcode "
                + " where Sale_num ='" + m_saleNum + "' "
                + " Order by Sale_Seq asc ";

        //+ "order by office_name";

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
                    SlipDetailListView(results);
                    adapter2.notifyDataSetChanged();

                    Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(), "자료가 없습니다!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                adapter2.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "자료를 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    private void SlipHeadView(JSONArray results) {
        int index = 0;
        try {
            JSONObject son = results.getJSONObject(index);
            HashMap<String, String> map = JsonHelper.toStringHashMap(son);

            // 헤드
            String temp = map.get("Sale_Date"); //Sale_Date

            m_SaleDate.setText(map.get("Sale_Date") + " " + map.get("Sale_Time"));  //Sale_Date, Sale_Time
            m_PosNo.setText(map.get("Pos_No")); // Pos_No
            m_SaleNum.setText(m_saleNum);
            m_Writer.setText(map.get("Writer")); // Writer

            // 결제
            //temp = map.get("Cash_Pri");
            //String temp = StringFormat.convertToNumberFormat(map.get("Cash_Pri"));


            m_CashPri.setText(StringFormat.convertToNumberFormat(map.get("Cash_Pri"))); //Cash_Pri
            m_CardPri.setText(StringFormat.convertToNumberFormat(map.get("Card_Pri"))); //Card_Pri
            m_DecPri.setText(StringFormat.convertToNumberFormat(map.get("Dec_Pri"))); //Dec_Pri

            if ( Float.parseFloat(map.get("Cash_Pri").replace(",", ""))>0){
                m_linearCash.setVisibility(View.VISIBLE);
            }
            if ( Float.parseFloat(map.get("Card_Pri").replace(",", ""))>0){
                m_linearCard.setVisibility(View.VISIBLE);
            }
            if ( Float.parseFloat(map.get("Dec_Pri").replace(",", ""))>0){
                m_linearDec.setVisibility(View.VISIBLE);
            }

//            //카드
//            if (map.get("Card_No").equals("")) {
//                m_CardInfo.setVisibility(View.GONE);
//            } else {
//                m_CardInfo.setVisibility(View.VISIBLE);
//                m_CardNo.setText(map.get("Card_No"));
//            }

            // 회원 Cus_Code
            if (map.get("Cus_Code").equals("")) {
                //회원창 안보이게(기본)
                m_CusInfo.setVisibility(View.GONE);
            } else {
                m_CusInfo.setVisibility(View.VISIBLE);
                m_CusCode.setText(map.get("Cus_Code"));//Cus_Code
                m_CusName.setText(map.get("Cus_Name"));//Cus_Name
                m_CusPoint.setText(StringFormat.convertToNumberFormat(map.get("Cus_Point")));//Cus_Point
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void SlipDetailListView(JSONArray results) {

        int seq = 0; //순번
        int cnt = 0; //수량
        float pri = 0; //금액

        for (int index = 0; index < results.length(); index++) {

            try {
                JSONObject son = results.getJSONObject(index);
                HashMap<String, String> map = JsonHelper.toStringHashMap(son);

                map.put("Sell_Pri", StringFormat.convertToNumberFormat(map.get("Sell_Pri")));
                map.put("Sale_Count", StringFormat.convertToNumberFormat(map.get("Sale_Count")));
                map.put("TSell_Pri", StringFormat.convertToNumberFormat(map.get("TSell_Pri")));

                cnt += Integer.parseInt(map.get("Sale_Count").replace(",", ""));
                pri += Float.parseFloat(map.get("TSell_Pri").replace(",", ""));

                mfillMaps2.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // 하단에 합계 출력
        seq = results.length();

        m_seq.setText(StringFormat.convertToNumberFormat(""+seq)); //총건수
        m_cnt.setText(StringFormat.convertToNumberFormat(""+cnt)); //총수량
        m_pri.setText(StringFormat.convertToNumberFormat(""+pri)); //총금액

    }
    public void onButtonClose(View v) {
        //Toast.makeText(getApplicationContext(),"돌아가기 버큰을 눌렀습니다.",Toast.LENGTH_LONG);
        finish();
    }

}