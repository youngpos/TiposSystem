package tipsystem.tips;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kr.co.tipos.tips.R;
import tipsystem.tips.models.ShopSelectItem;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;
import tipsystem.utils.TIPOS;

public class SalesShopActivity extends Activity implements AdapterView.OnItemClickListener,
                                                           DatePickerDialog.OnDateSetListener{
    private Context mContext;

    SimpleDateFormat m_dateFormatter;
    Calendar m_dateCalender1;
    Calendar m_dateCalender2;

    int mListPosition = -1;
    //String[] saleAmount;

    // loading bar
    private ProgressDialog dialog;
    Button buttonSetDate;

    //리스트 컨트롤 정의
    ListView listShop;
    // 어댑터 정의
    SimpleAdapter adapter;
    List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

    CheckBox checkBoxShop;
    TextView textViewShopName;
    TextView textViewShopDate;
    TextView textViewShopIP;
    Button buttonShop;

    TextView m_realSales;
    TextView m_viewNumber;
    TextView m_viewKNumber;
    TextView m_viewRealSalesYesterday;
    TextView m_viewPrice;
    TextView m_viewCash;
    TextView m_viewCard;
    TextView m_viewCredit;
    TextView m_viewOther;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_shop);

        mContext = this;

        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        //m_numberFormat = NumberFormat.getInstance();
        m_dateCalender1 = Calendar.getInstance();
        m_dateCalender2 = Calendar.getInstance();

        buttonSetDate = (Button) findViewById(R.id.buttonSetDate);
        buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);

        buttonShop = (Button) findViewById(R.id.buttonShop);
        buttonShop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mfillMaps.size() > 0)
                {
                    ControlInit();
                    mListPosition = 0;
                    SqlListRowCheck();
                }
            }
        });

        // 상단 텍스트 뷰
        m_realSales = (TextView)findViewById(R.id.textViewRealSales);
        m_viewKNumber = (TextView)findViewById(R.id.textViewKNumber);
        m_viewRealSalesYesterday = (TextView)findViewById(R.id.textViewRealSalesYesterday);
        m_viewPrice = (TextView)findViewById(R.id.textViewKPrice);
        m_viewCash = (TextView)findViewById(R.id.textViewCash);
        m_viewCard = (TextView)findViewById(R.id.textViewCard);
        m_viewCredit = (TextView)findViewById(R.id.textViewCredit);
        m_viewOther = (TextView)findViewById(R.id.textViewOther);

        // 리스트 컨트롤 레이아웃 정의
        listShop = (ListView) findViewById(R.id.listviewshop);

        String[] from2 = new String[]{"Sto_Cd", "Office_Name", "순매출", "전일_순매출", "객수", "객단가", "현금", "카드", "외상", "기타", "Shop_Ip", "Shop_Port", "Shop_Db", "Shop_Id", "Shop_Pw"};
        int[] to2 = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8, R.id.item9, R.id.item10, R.id.item11, R.id.item12, R.id.item13, R.id.item14, R.id.item15} ;

        adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_shop_list, from2, to2);
        listShop.setAdapter(adapter);
        listShop.setOnItemClickListener(this);

        doQuery();
    }


    private void doQuery() {

        mfillMaps.removeAll(mfillMaps);

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        Calendar c = Calendar.getInstance();
        String today = f.format(c.getTime());

        String phoneNumber = LocalStorage.getString(SalesShopActivity.this, "phoneNumber");

        // 쿼리 작성하기
        String query = "";
        query = "select "
                + " B.Sto_Cd AS Sto_Cd, "
                + " B.Office_Name AS Office_Name, "
                + " 0 AS 순매출, "
                + " 0 AS 전일_순매출, "
                + " 0 AS 객수, "
                + " 0 AS 객단가, "
                + " 0 AS 현금, "
                + " 0 AS 카드, "
                + " 0 AS 외상, "
                + " 0 AS 기타, "
                + " IsNull(b.SHOP_IP,'') as Shop_Ip, "
                + " IsNull(b.SHOP_PORT,'') as Shop_Port, "
                + " IsNull(b.shop_uudb,'') as Shop_Db, "
                + " IsNull(b.shop_uuid,'') as Shop_Id, "
                + " IsNull(b.shop_uupass,'') as Shop_Pw "
                + "  from APP_USER as A inner join V_OFFICE_USER as B "
                + " on A.OFFICE_CODE = B.Sto_CD "
                + " JOIN APP_SETTLEMENT as C on A.OFFICE_CODE = C.OFFICE_CODE "

                + " where A.APP_HP ='" + phoneNumber + "' AND C.DEL_YN = '0' "
                // 임시 테스트
                //+ " AND B.Sto_Cd <>'0002678' "

                // 사장님 휴대폰버호로 테스트
                //+ " where A.APP_HP ='" + "01087877873" + "' AND C.DEL_YN = '0' "

                + " AND C.APP_SDATE<='" + today + "' AND C.APP_EDATE>='" + today + "'"
                + "order by office_name";

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
                    ShopListView(results);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();

                    // 정상조회시 매장별 전체 조회 시작
                    //  일단 1번부터 1개만 하자

                    ControlInit();
                    mListPosition = 0;
                    SqlListRowCheck();

                } else {
                    Toast.makeText(getApplicationContext(), "매장목록을 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "매장목록을 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
        }).execute(TIPOS.HOST_SERVER_IP + ":" + TIPOS.HOST_SERVER_PORT, TIPOS.HOST_SERVER_DB, TIPOS.HOST_SERVER_ID, TIPOS.HOST_SERVER_PW, query);
    }

    private void ShopListView(JSONArray results) {
        for (int index = 0; index < results.length(); index++) {

            try {
                JSONObject son = results.getJSONObject(index);
                HashMap<String, String> map = JsonHelper.toStringHashMap(son);

                map.put("순매출", StringFormat.convertToNumberFormat(map.get("순매출")));
                map.put("전일_순매출", StringFormat.convertToNumberFormat(map.get("전일_순매출")));
                map.put("객수", StringFormat.convertToNumberFormat(map.get("객수")));
                map.put("객단가", StringFormat.convertToNumberFormat(map.get("객단가")));
                map.put("현금", StringFormat.convertToNumberFormat(map.get("현금")));
                map.put("카드", StringFormat.convertToNumberFormat(map.get("카드")));
                map.put("외상", StringFormat.convertToNumberFormat(map.get("외상")));
                map.put("기타", StringFormat.convertToNumberFormat(map.get("기타")));

                mfillMaps.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    private void GetShopData(String stoCd,String stoName, String shopIp, String shopPort, String shopDb, String shopId, String shopPw) {

        String period1 = buttonSetDate.getText().toString();
        String period2 = m_dateFormatter.format(m_dateCalender2.getTime());

        int year1 = Integer.parseInt(period1.substring(0, 4));
        int month1 = Integer.parseInt(period1.substring(5, 7));

        int year2 = Integer.parseInt(period2.substring(0, 4));
        int month2 = Integer.parseInt(period2.substring(5, 7));

        String tableName = String.format("%04d%02d", year1, month1);
        String tableName2 = String.format("%04d%02d", year2, month2);

        String query = "";
        query ="Select "
                +"  ISNULL(판매액,0) '판매액', "
                +"  IsNull(반품액, 0) '반품액', "
                +"  IsNull(할인액, 0) '할인액', "
                +"  ISNULL(과세,0) '과세', "
                +"  ISNULL(면세,0) '면세', "
                +"  ISNULL(순매출,0) '순매출', "
                +"  ISNULL(현금,0) '현금', "
                +"  ISNULL(카드,0) '카드', "
                +"  ISNULL(외상,0) '외상', "
                +"  ISNULL(포인트사용,0) + "
                +"  ISNULL(상품권,0) + "
                +"  ISNULL(앱쿠폰,0) + "
                +"  ISNULL(캐쉬백사용,0) + "
                +"  ISNULL(절사금액,0) + "
                +"  ISNULL(CMS할인,0) + ISNULL(예비쿠폰1,0) + ISNULL(예비쿠폰2,0) + ISNULL(예비쿠폰3,0) + ISNULL(쿠폰할인액,0) + ISNULL(카드할인액,0) + ISNULL(에누리할인,0) '기타',  "
                +"  ISNULL(객수,0) '객수', "
                +"  ISNULL(객단가,0) '객단가',  "
                +"  ISNULL(이익금,0) '이익금',  "
                +"  ISNULL(순매출_B,0) '전일_순매출',  "
                +"  ISNULL(객수_B,0) '전일_객수',  "
                +"  ISNULL(객단가_B,0) '전일_객단가' "
                +" From (      "
                +"  Select      "
                +"  'a' a,판매액,반품액,할인액,과세,면세,순매출,      "
                +"  현금,카드,외상,CMS할인,포인트사용,에누리할인, "
                +"  상품권,앱쿠폰,예비쿠폰1,예비쿠폰2,예비쿠폰3,쿠폰할인액,카드할인액,캐쉬백사용,절사금액,  "
                +"  객수, '객단가' = CASE WHEN 순매출=0 Then 0 ELSE 순매출/객수 END,   "
                +"  이익금   "
                +"  From (  "
                +"    Select "
                +"    Sum(Case When A.Sale_Yn='1' Then A.TSell_Pri + A.Dc_Pri Else 0 End ) '판매액',          "
                +"    Sum(Case When A.Sale_Yn='0' Then A.TSell_RePri + A.Dc_Pri Else 0 End ) '반품액',          "
                +"    Sum(Case When A.Sale_Yn='1' then A.DC_Pri Else  A.DC_Pri *-1 End) '할인액',          "
                +"    Sum(Case When A.Tax_YN='1' then  A.TSell_Pri - A.TSell_RePri Else 0 End) '과세',       "
                +"    Sum(Case When A.Tax_YN='0' then  A.TSell_Pri - A.TSell_RePri Else 0 End)  '면세',        "
                +"    Sum (A.TSell_Pri - A.TSell_RePri) '순매출',          "
                +"    Sum (Round(B.Cash_Pri * A.Money_Per, 4)) '현금',       "
                +"    Sum (A.Card_Pri) '카드',          "
                +"    Sum (Round(B.Dec_Pri * A.Money_Per, 4)) '외상',          "
                +"    Sum (Round(B.CMS_Pri * A.Money_Per, 4)) 'CMS할인',         "
                +"    Sum (Round(B.Cus_PointUse * A.Money_Per, 4)) '포인트사용',   "
                +"    Sum (Round(B.Sub_Pri * A.Money_Per, 4)) '에누리할인',          "
                +"    Sum (Round(B.Gift_Pri * A.Money_Per, 4)) '상품권',          "
                +"    Sum (Round(isnull(B.APP_DCPri,0) * A.Money_Per, 4)) '앱쿠폰', "
                +"    Sum (Round(isnull(B.ETC_DCPRI1,0) * A.Money_Per, 4)) '예비쿠폰1',          "
                +"    Sum (Round(isnull(B.ETC_DCPRI2,0) * A.Money_Per, 4)) '예비쿠폰2',          "
                +"    Sum (Round(isnull(B.ETC_DCPRI3,0) * A.Money_Per, 4)) '예비쿠폰3',          "
                +"    Sum (Round(B.BC_Coupon_DC * A.Money_Per, 4)) '쿠폰할인액',          "
                +"    Sum (Round(B.BC_Card_DC * A.Money_Per, 4)) '카드할인액',          "
                +"    Sum (Round(B.CashBack_PointUse * A.Money_Per, 4)) '캐쉬백사용',     "
                +"    Sum (Round(B.Cut_Pri * A.Money_Per, 4)) '절사금액',          "
                +"    Count (Distinct(B.Sale_Num)) '객수',          "
                +"    Sum (A.ProFit_Pri) '이익금'          "
                +"    From SaD_"+tableName+" A, SaT_"+tableName+" B        "
                +"    Where A.Sale_Num=B.Sale_Num And A.Sale_Date='"+period1+"' ";

        query +="    Group By A.Sale_Date      "
                +"    ) G  "
                +"  ) A FULL JOIN (      "
                +"    Select     'a' a,     순매출 '순매출_B',      "
                +"    객수 '객수_B' ,'객단가_B' = CASE WHEN 순매출=0 Then 0 ELSE 순매출/객수 END      "
                +"    From (          "
                +"      Select          "
                +"      Sum (A.TSell_Pri - A.TSell_RePri) '순매출',          "
                +"      Count (Distinct(B.Sale_Num)) '객수'  "
                +"      From SaD_"+tableName2+" A, SaT_"+tableName2+" B "
                +"      Where A.Sale_Num=B.Sale_Num And A.Sale_Date='" + period2 + "'          ";
        query += "      Group By A.Sale_Date      "
                +"    ) G  "
                +"  ) B ON A.a=B.a  ";

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage(stoCd + " " + stoName +  " 조회 중....");
        dialog.show();

        // 콜백함수와 함께 실행
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();

                try {
                    if (results.length() > 0) { // 검색결과 확인

                        //int row = Integer.parseInt(results.getJSONObject(0).getString("ShopRow"));
                        //String shopAmount = results.getJSONObject(0).getString("순매출");

                        // 기존 자료 불러오기
                        HashMap<String, String> map = mfillMaps.get(mListPosition);
                        //map.put("순매출", shopAmount);
                        map.put("순매출", StringFormat.convertToNumberFormat(results.getJSONObject(0).getString("순매출")));
                        map.put("전일_순매출", StringFormat.convertToNumberFormat(results.getJSONObject(0).getString("전일_순매출")));
                        map.put("객수", StringFormat.convertToNumberFormat(results.getJSONObject(0).getString("객수")));
                        map.put("객단가", StringFormat.convertToNumberFormat(results.getJSONObject(0).getString("객단가")));
                        map.put("현금", StringFormat.convertToNumberFormat(results.getJSONObject(0).getString("현금")));
                        map.put("칻드", StringFormat.convertToNumberFormat(results.getJSONObject(0).getString("카드")));
                        map.put("외상", StringFormat.convertToNumberFormat(results.getJSONObject(0).getString("외상")));
                        map.put("기타", StringFormat.convertToNumberFormat(results.getJSONObject(0).getString("기타")));

                        // 최종 순매출 수정
                        mfillMaps.set(mListPosition, map);
                        adapter.notifyDataSetChanged();

                    } else {
                        //Toast.makeText(getApplicationContext(), "조회 결과가 없습니다.", LENGTH_SHORT);
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                // 성공시
                //----------------------------------------//
                //마지막을 여기서 체크하여 처리하자
                //----------------------------------------//
                mListPosition ++;
                // 마지막
                if (mListPosition < mfillMaps.size()) {
                    SqlListRowCheck();
                }else if(mListPosition == mfillMaps.size()){
                    // 상단에 합계 표시
                    GetTotal();
                }
                //----------------------------------------//

            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();

                // 기존 자료 불러오기
                HashMap<String, String> map = mfillMaps.get(mListPosition);
                //map.put("순매출", shopAmount);
                map.put("순매출", "에러");

                // 최종 순매출 수정
                mfillMaps.set(mListPosition, map);
                adapter.notifyDataSetChanged();

                // 실패시에도 다음건으로 처리
                //----------------------------------------//
                //마지막을 여기서 체크하여 처리하자
                //----------------------------------------//
                mListPosition ++;
                // 마지막
                if (mListPosition < mfillMaps.size()) {
                    SqlListRowCheck();
                }else if(mListPosition == mfillMaps.size()){
                    // 상단에 합계 표시
                    GetTotal();
                }
                //----------------------------------------//

                adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "매장별 자료를 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
        }).execute(shopIp + ":" + shopPort, shopDb, shopId, shopPw, query);
    }

    private void GetTotal() {

        ControlInit();

        int kNumber = 0; //객수
        float realSales = 0; //순매출
        float realSalesYesterday = 0; //전순매출
        float price = 0; //객단가
        float cash = 0; //현금;
        float card = 0; //카드
        float credit = 0; //외상
        float other = 0; //기타

        if (mfillMaps.size() > 0) {

            for (int index = 0; index < mfillMaps.size(); index++) {

                HashMap<String, String> map = new HashMap<String, String>();

                map.putAll(mfillMaps.get(index));

                if (map.get("순매출") != "에러"){
                    realSales += Float.parseFloat(map.get("순매출").replace(",", ""));// 순매출
                    kNumber += Integer.parseInt(map.get("객수").replace(",", ""));
                    price += Float.parseFloat(map.get("객단가").replace(",", ""));
                    cash += Float.parseFloat(map.get("현금").replace(",", ""));
                    card += Float.parseFloat(map.get("카드").replace(",", ""));
                    credit += Float.parseFloat(map.get("외상").replace(",", ""));
                    other += Float.parseFloat(map.get("기타").replace(",", ""));
                    realSalesYesterday += Float.parseFloat(map.get("전일_순매출").replace(",", ""));
                }

            }

            m_realSales.setText(StringFormat.convertToNumberFormat(""+realSales)); //순매출
            m_viewKNumber.setText(StringFormat.convertToNumberFormat(""+kNumber)); //객수
            m_viewRealSalesYesterday.setText(StringFormat.convertToNumberFormat(""+realSalesYesterday)); //전일순매출
            m_viewPrice.setText(StringFormat.convertToNumberFormat(""+price)); //객단가
            m_viewCash.setText(StringFormat.convertToNumberFormat(""+cash)); //현금
            m_viewCard.setText(StringFormat.convertToNumberFormat(""+card)); //카드
            m_viewCredit.setText(StringFormat.convertToNumberFormat(""+credit));//외상
            m_viewOther.setText(StringFormat.convertToNumberFormat(""+other));//기타

        }

        Toast.makeText(getApplicationContext(), "매장 전체 자료입니다", Toast.LENGTH_SHORT).show();
    }

    private void SqlListRowCheck(){

        String errorCheck= "";

        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(mfillMaps.get(mListPosition));
        errorCheck = map.get("순매출");

        while (errorCheck == "에러"){

            mListPosition++;
            if (mListPosition == mfillMaps.size()){
                break;
            }
            map.putAll(mfillMaps.get(mListPosition));
            errorCheck = map.get("순매출");
        }

        if (mListPosition < mfillMaps.size()) {
            SqlListRow();
        }else if(mListPosition == mfillMaps.size()){
            // 상단에 합계 표시
            GetTotal();
        }
    }
    private void SqlListRow(){

        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(mfillMaps.get(mListPosition));
        //
        String errorCheck = map.get("순매출");
        if (errorCheck == "에러"){
            mListPosition++;
        }


        String stoCd = map.get("Sto_Cd");

        String stoName = map.get("Office_Name");
        String shopIp = map.get("Shop_Ip");
        String shopPort = map.get("Shop_Port");
        String shopDb = map.get("Shop_Db");
        String shopId = map.get("Shop_Id");
        String shopPw = map.get("Shop_Pw");

        //map.put("순매출","");
        GetShopData(stoCd,stoName, shopIp, shopPort, shopDb, shopId, shopPw);

    }

    private void ControlInit(){

        m_realSales.setText("0");
        m_viewKNumber.setText("0");
        m_viewRealSalesYesterday.setText("0");
        m_viewPrice.setText("0");
        m_viewCash.setText("0");
        m_viewCard.setText("0");
        m_viewCredit.setText("0");
        m_viewOther.setText("0");

    }

    public void onClickSetDate(View view) {
        DatePickerDialog newDlg = new DatePickerDialog(this, this,
                m_dateCalender1.get(Calendar.YEAR),
                m_dateCalender1.get(Calendar.MONTH),
                m_dateCalender1.get(Calendar.DAY_OF_MONTH));
        newDlg.show();
    }

    public void onClickSetDatePrevious(View view) {
        m_dateCalender1.add(Calendar.DAY_OF_MONTH, -1);
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);
        buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        //doQuery();
        ControlInit();
        mListPosition = 0;
        SqlListRowCheck();

    }

    public void onClickSetDateNext(View view) {
        m_dateCalender1.add(Calendar.DAY_OF_MONTH, 1);
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, 1);
        buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        //doQuery();
        ControlInit();
        mListPosition = 0;
        SqlListRowCheck();

    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

        m_dateCalender1.set(i, i1, i2);
        buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        m_dateCalender2.set(i, i1, i2);
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);

        //doQuery();
        ControlInit();
        mListPosition = 0;
        SqlListRowCheck();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(mfillMaps.get(i));
        //
        String stoCd = map.get("Sto_Cd");
        String stoName = map.get("Office_Name");
        String shopIp = map.get("Shop_Ip");
        String shopPort = map.get("Shop_Port");
        String shopDb = map.get("Shop_Db");
        String shopId = map.get("Shop_Id");
        String shopPw = map.get("Shop_Pw");

        if (map.get("순매출") == "에러"){
            String msg = stoCd + " " + stoName + " 매장은 연결이 안됩니다!";
            Toast.makeText(getApplicationContext(),  msg, Toast.LENGTH_SHORT).show();
            return;
        }

        //String msg = stoCd + " " + stoName + " 매장을 선택하셨습니다!";
        //Toast.makeText(getApplicationContext(),  msg, Toast.LENGTH_SHORT).show();

        Intent intent;
        intent = new Intent(this, SalesNewsActivity.class);

        intent.putExtra("stoCd",stoCd);
        intent.putExtra("stoName",stoName);
        intent.putExtra("shopIp",shopIp);
        intent.putExtra("shopPort",shopPort);
        intent.putExtra("shopDb",shopDb);
        intent.putExtra("shopId",shopId);
        intent.putExtra("shopPw",shopPw);

        startActivity(intent);

    }
}