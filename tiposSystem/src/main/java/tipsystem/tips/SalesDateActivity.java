package tipsystem.tips;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
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
import java.util.Timer;
import java.util.TimerTask;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;

public class SalesDateActivity extends Activity implements AdapterView.OnItemClickListener,
        DatePickerDialog.OnDateSetListener {

    private Context mContext;

    private JSONObject m_shop;
    private JSONObject m_userProfile;
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

    private SimpleDateFormat m_dateFormatter;
    private Calendar m_dateCalender1;
    private Calendar m_dateCalender2;
    private int m_dateMode = 0;
    private NumberFormat m_numberFormat;
    private ProgressDialog dialog;

    // 레이아웃 컨트롤 정의
    private Button m_period1;
    private Button m_period2;

    private ListView listSlip;              // 데이타 리스트

    private TextView m_Ramt; //순매출
    private TextView m_Card; // 카드
    private TextView m_Cash;//현금
    private TextView m_Other;//기타

    String m_APP_USER_GRADE;    //앱권한
    String m_OFFICE_CODE = null;  //수수료매장거래처코드
    String m_viewOption;

    // 어댑터 정의
    private SimpleAdapter adapter;
    private List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_date);

        mContext = this;

        //----------------------------------------//
        // 매장 정보 정의
        //----------------------------------------//
        GetShopInfo();

        // 레이아웃 컨트롤 정의
        m_period1 = (Button) findViewById(R.id.buttonSetDate1);
        m_period2 = (Button) findViewById(R.id.buttonSetDate2);

        // 합계 레이아웃 정의
        m_Ramt = (TextView) findViewById(R.id.textViewRamt);
        m_Card = (TextView) findViewById(R.id.textViewCard);
        m_Cash = (TextView) findViewById(R.id.textViewCash);
        m_Other = (TextView) findViewById(R.id.textViewOther);

        // 일자클래스 생성
        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);

        m_dateCalender1 = Calendar.getInstance();
        m_dateCalender2 = Calendar.getInstance();

//        // 해당 월의 첫 일자 가져오기
//        m_dateCalender1.set(Calendar.DAY_OF_MONTH, 1);
//        // 해당 월의 마지막 일자 가져오기
//        m_dateCalender2.set(Calendar.DAY_OF_MONTH, m_dateCalender1.getActualMaximum(Calendar.DAY_OF_MONTH));

        m_period1.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        m_period2.setText(m_dateFormatter.format(m_dateCalender2.getTime()));

        //m_checkBoxMonth = (CheckBox) findViewById(R.id.checkBoxMonth);
        listSlip = (ListView) findViewById(R.id.listviewSlip);

        String[] from2 = new String[]{"일자", "순매출", "카드", "현금", "기타"};
        int[] to2 = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5};

        adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_date_list, from2, to2);
        listSlip.setAdapter(adapter);
        listSlip.setOnItemClickListener(this);

        doQuery();
    }


    public void onClickSearch(View view) {
        doQuery();
    }

    private void doQuery() {
        mfillMaps.removeAll(mfillMaps);
        adapter.notifyDataSetChanged();

        m_Ramt.setText("0");
        m_Card.setText("0");
        m_Cash.setText("0");
        m_Other.setText("0");

        String period1 = m_period1.getText().toString();
        String period2 = m_period2.getText().toString();

        int year1 = Integer.parseInt(period1.substring(0, 4));
        int month1 = Integer.parseInt(period1.substring(5, 7));
        int day1 = Integer.parseInt(period1.substring(8, 10));

        int year2 = Integer.parseInt(period2.substring(0, 4));
        int month2 = Integer.parseInt(period2.substring(5, 7));
        int day2 = Integer.parseInt(period2.substring(8, 10));

//        // 범위 체크
//        if (year1 != year2) {
//            Toast.makeText(getApplicationContext(), "연도는 동일한 범위에서만 가능합니다!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (month1 != month2) {
//            Toast.makeText(getApplicationContext(), "해당월은 동일한 범위에서만 가능합니다!", Toast.LENGTH_SHORT).show();
//            return;
//        }
//        if (day1 > day2) {
//            Toast.makeText(getApplicationContext(), "일자 범위가 잘못되었습니다!", Toast.LENGTH_SHORT).show();
//            return;
//        }

        //String tableName = String.format("%04d%02d", year1, month1);

        // 쿼리 작성하기
        String query = "";
        query += " Select";
        query += " CASE WHEN B.DDAY IS NULL THEN '' ELSE B.DDAY End '일자',";
        query += " ISNULL(b.순매출,0) '순매출',";
        //query += " ISNULL(b.과세,0) '과세',";
        //query += " ISNULL(b.면세,0) '면세',";
        query += " ISNULL(b.현금,0) '현금',";
        query += " ISNULL(b.카드,0) '카드',";
        //query += " ISNULL(b.외상금액,0) '외상금액',";
        query += " ISNULL(b.CMS할인+b.포인트사용+b.에누리할인+b.상품권+b.앱쿠폰+b.예비쿠폰1+b.예비쿠폰2+b.예비쿠폰3+b.캐쉬백사용+b.절사금액+b.쿠폰할인액+b.카드현장할인액+b.외상금액,0) '기타' ";
        //query += " ISNULL(b.객수,0) '객수',";
        //query += " ISNULL(Case When b.순매출=0 then 0 Else b.순매출/b.객수 end,0) '객단가',";
        //query += " ISNULL(b.이익금,0) '이익금'";
        query += " From (";

        //----------------------------------------//
        // 월단위로 테이블 변경 쿼리후 UNION ALL
        //----------------------------------------//
        for (int y = year1; y <= year2; y++) {
            int m1 = 1, m2 = 12;
            if (y == year1) m1 = month1;
            if (y == year2) m2 = month2;

            for (int m = m1; m <= m2; m++) {
                String tableName = String.format("%04d%02d", y, m);

                query += " Select";
                query += " B.Sale_Date DDAY,";
                query += " Sum (a.TSell_Pri - a.TSell_RePri) '순매출',";
                query += " Sum (CASE WHEN a.TAX_YN='1' THEN a.TSell_Pri - a.TSell_RePri  END) '과세',";
                query += " Sum (CASE WHEN a.TAX_YN='0' THEN a.TSell_Pri - a.TSell_RePri  END) '면세',";
                query += " Sum (Round(b.Cash_Pri * a.Money_Per, 4)) '현금',";
                query += " Sum (a.Card_Pri) '카드',";
                query += " Sum (Round(b.Dec_Pri * a.Money_Per, 4)) '외상금액',";
                query += " Sum (Round(b.CMS_Pri * a.Money_Per, 4)) 'CMS할인',";
                query += " Sum (Round(b.Cus_PointUse * a.Money_Per, 4)) '포인트사용',";
                query += " Sum (Round(b.Sub_Pri * a.Money_Per, 4)) '에누리할인',";
                query += " Sum (Round(b.Gift_Pri * a.Money_Per, 4)) '상품권',";
                query += " Sum (Round(isnull(b.APP_DCPri,0) * a.Money_Per, 4)) '앱쿠폰',";
                query += " Sum (Round(isnull(b.ETC_DCPRI1,0) * a.Money_Per, 4)) '예비쿠폰1',";
                query += " Sum (Round(isnull(b.ETC_DCPRI2,0) * a.Money_Per, 4)) '예비쿠폰2',";
                query += " Sum (Round(isnull(b.ETC_DCPRI3,0) * a.Money_Per, 4)) '예비쿠폰3',";
                query += " Sum (Round(b.CashBack_PointUse * a.Money_Per, 4)) '캐쉬백사용',";
                query += " Sum (Round(b.Cut_Pri * a.Money_Per, 4)) '절사금액',";
                query += " Sum (Round(b.BC_Coupon_DC * a.Money_Per, 4)) '쿠폰할인액',";
                query += " Sum (Round(b.BC_Card_DC * a.Money_Per, 4)) '카드현장할인액',";
                query += " Count (Distinct(a.Sale_Num)) '객수',";
                query += " Sum (a.ProFit_Pri) '이익금'";
                query += " From SaD_" + tableName + " A, SaT_" + tableName + " B";
                query += " Where A.Sale_Num=B.Sale_Num And A.Sale_date>='" + period1 + "' And A.Sale_Date<='" + period2 + "'";
                query += " Group By B.Sale_Date";
                query += " UNION ALL ";
            }
        }
        //----------------------------------------//
        // 마자막은 UNION ALL 생략한다
        query = query.substring(0, query.length() - 11);

        query += " )  B";
        query += " Order by DDAY ";

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
                    DataListView(results);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(), "자료가 없습니다!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "자료를 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

    }

    public void onClickSetDate1(View v) {

        DatePickerDialog newDlg = new DatePickerDialog(this, this,
                m_dateCalender1.get(Calendar.YEAR),
                m_dateCalender1.get(Calendar.MONTH),
                m_dateCalender1.get(Calendar.DAY_OF_MONTH));
        newDlg.show();

        m_dateMode = 1;
    }

    ;

    public void onClickSetDate2(View v) {

        DatePickerDialog newDlg = new DatePickerDialog(this, this,
                m_dateCalender2.get(Calendar.YEAR),
                m_dateCalender2.get(Calendar.MONTH),
                m_dateCalender2.get(Calendar.DAY_OF_MONTH));

        newDlg.show();

        m_dateMode = 2;
    }

    ;

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        if (m_dateMode == 1) {
            m_dateCalender1.set(i, i1, i2);
            ;
            m_period1.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        } else if (m_dateMode == 2) {
            m_dateCalender2.set(i, i1, i2);
            ;
            m_period2.setText(m_dateFormatter.format(m_dateCalender2.getTime()));
        }
        m_dateMode = 0;

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        //String period = m_period1.getText().toString();
        //int year1 = Integer.parseInt(period.substring(0, 4));
        //int month1 = Integer.parseInt(period.substring(5, 7));
        //String tableName = String.format("%04d%02d", year1, month1);

        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(mfillMaps.get(i));

        String saleDay = map.get("일자");
        //int day1 = Integer.parseInt(saleDay);
        //String saleDate = String.format("%04d-%02d-%02d", year1, month1, day1);

        doDetailQuery(saleDay);

    }


    private void DataListView(JSONArray results) {

        // 합계 계산
        float mRamt = 0;
        float mCard = 0;
        float mCash = 0;
        float mOther = 0;

        for (int index = 0; index < results.length(); index++) {

            try {
                JSONObject son = results.getJSONObject(index);
                HashMap<String, String> map = JsonHelper.toStringHashMap(son);

                String ramt = son.getString("순매출");
                String card = son.getString("카드");
                String cash = son.getString("현금");
                String other = son.getString("기타");

                map.put("순매출", StringFormat.convertTNumberFormat(map.get("순매출")));
                map.put("카드", StringFormat.convertTNumberFormat(map.get("카드")));
                map.put("현금", StringFormat.convertTNumberFormat(map.get("현금")));
                map.put("기타", StringFormat.convertTNumberFormat(map.get("기타")));

                mRamt += Float.parseFloat(ramt);
                mCard += Float.parseFloat(card);
                mCash += Float.parseFloat(cash);
                mOther += Float.parseFloat(other);

                mfillMaps.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        //m_contents[20].setText(StringFormat.convertToNumberFormat(String.format("%.2f", cash)));	//현
        // 합계 출력
        m_Ramt.setText(StringFormat.convertToNumberFormat(String.format("%.0f", mRamt)));
        m_Card.setText(StringFormat.convertToNumberFormat(String.format("%.0f", mCard)));
        m_Cash.setText(StringFormat.convertToNumberFormat(String.format("%.0f", mCash)));
        m_Other.setText(StringFormat.convertToNumberFormat(String.format("%.0f", mOther)));

    }

    private void doDetailQuery(String saleDate) {

        Calendar dateCalender1;
        Calendar dateCalender2;

        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        dateCalender1 = Calendar.getInstance();
        dateCalender2 = Calendar.getInstance();

        dateCalender1.set(Calendar.YEAR, Integer.parseInt(saleDate.substring(0, 4)));
        dateCalender1.set(Calendar.MONTH, Integer.parseInt(saleDate.substring(5, 7)));
        dateCalender1.set(Calendar.DATE, Integer.parseInt(saleDate.substring(8, 10)));

        dateCalender2.set(Calendar.YEAR, Integer.parseInt(saleDate.substring(0, 4)));
        dateCalender2.set(Calendar.MONTH, Integer.parseInt(saleDate.substring(5, 7)));
        dateCalender2.set(Calendar.DATE, Integer.parseInt(saleDate.substring(8, 10)));

        String period1 = saleDate;
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);

        String period2 = m_dateFormatter.format(m_dateCalender2.getTime());
        String query = "";

        int year1 = Integer.parseInt(period1.substring(0, 4));
        int month1 = Integer.parseInt(period1.substring(5, 7));
        int year2 = Integer.parseInt(period2.substring(0, 4));
        int month2 = Integer.parseInt(period2.substring(5, 7));

        String tableName = String.format("%04d%02d", year1, month1);
        String tableName2 = String.format("%04d%02d", year2, month2);

        //2016년6월10일 앱쿠폰사용금액/수수료매장 현금영수증 사용금액별 구분 추가합니다.
        query = "Select "
                + "  ISNULL(판매액,0) '판매액', "
                + "  IsNull(반품액, 0) '반품액', "
                + "  IsNull(할인액, 0) '할인액', "
                + "  ISNULL(과세,0) '과세', "
                + "  ISNULL(면세,0) '면세', "
                + "  ISNULL(순매출,0) '순매출', "
                + "  ISNULL(현금,0) '현금', "
                + "  ISNULL(카드,0) '카드', "
                + "  ISNULL(외상,0) '외상', "
                + "  ISNULL(포인트사용,0) + "
                + "  ISNULL(상품권,0) + "
                + "  ISNULL(앱쿠폰,0) + "
                + "  ISNULL(캐쉬백사용,0) + "
                + "  ISNULL(절사금액,0) + "
                + "  ISNULL(CMS할인,0) + ISNULL(예비쿠폰1,0) + ISNULL(예비쿠폰2,0) + ISNULL(예비쿠폰3,0) + ISNULL(쿠폰할인액,0) + ISNULL(카드할인액,0) + ISNULL(에누리할인,0) '기타',  "
                + "  ISNULL(객수,0) '객수', "
                + "  ISNULL(객단가,0) '객단가',  "
                + "  ISNULL(이익금,0) '이익금',  "
                + "  ISNULL(순매출_B,0) '전일_순매출',  "
                + "  ISNULL(객수_B,0) '전일_객수',  "
                + "  ISNULL(객단가_B,0) '전일_객단가' "
                + " From (      "
                + "  Select      "
                + "  'a' a,판매액,반품액,할인액,과세,면세,순매출,      "
                + "  현금,카드,외상,CMS할인,포인트사용,에누리할인, "
                + "  상품권,앱쿠폰,예비쿠폰1,예비쿠폰2,예비쿠폰3,쿠폰할인액,카드할인액,캐쉬백사용,절사금액,  "
                + "  객수, '객단가' = CASE WHEN 순매출=0 Then 0 ELSE 순매출/객수 END,   "
                + "  이익금   "
                + "  From (  "
                + "    Select "
                + "    Sum(Case When A.Sale_Yn='1' Then A.TSell_Pri + A.Dc_Pri Else 0 End ) '판매액',          "
                + "    Sum(Case When A.Sale_Yn='0' Then A.TSell_RePri + A.Dc_Pri Else 0 End ) '반품액',          "
                + "    Sum(Case When A.Sale_Yn='1' then A.DC_Pri Else  A.DC_Pri *-1 End) '할인액',          "
                + "    Sum(Case When A.Tax_YN='1' then  A.TSell_Pri - A.TSell_RePri Else 0 End) '과세',       "
                + "    Sum(Case When A.Tax_YN='0' then  A.TSell_Pri - A.TSell_RePri Else 0 End)  '면세',        "
                + "    Sum (A.TSell_Pri - A.TSell_RePri) '순매출',          "
                + "    Sum (Round(B.Cash_Pri * A.Money_Per, 4)) '현금',       "
                + "    Sum (A.Card_Pri) '카드',          "
                + "    Sum (Round(B.Dec_Pri * A.Money_Per, 4)) '외상',          "
                + "    Sum (Round(B.CMS_Pri * A.Money_Per, 4)) 'CMS할인',         "
                + "    Sum (Round(B.Cus_PointUse * A.Money_Per, 4)) '포인트사용',   "
                + "    Sum (Round(B.Sub_Pri * A.Money_Per, 4)) '에누리할인',          "
                + "    Sum (Round(B.Gift_Pri * A.Money_Per, 4)) '상품권',          "
                + "    Sum (Round(isnull(B.APP_DCPri,0) * A.Money_Per, 4)) '앱쿠폰', "
                + "    Sum (Round(isnull(B.ETC_DCPRI1,0) * A.Money_Per, 4)) '예비쿠폰1',          "
                + "    Sum (Round(isnull(B.ETC_DCPRI2,0) * A.Money_Per, 4)) '예비쿠폰2',          "
                + "    Sum (Round(isnull(B.ETC_DCPRI3,0) * A.Money_Per, 4)) '예비쿠폰3',          "
                + "    Sum (Round(B.BC_Coupon_DC * A.Money_Per, 4)) '쿠폰할인액',          "
                + "    Sum (Round(B.BC_Card_DC * A.Money_Per, 4)) '카드할인액',          "
                + "    Sum (Round(B.CashBack_PointUse * A.Money_Per, 4)) '캐쉬백사용',     "
                + "    Sum (Round(B.Cut_Pri * A.Money_Per, 4)) '절사금액',          "
                + "    Count (Distinct(B.Sale_Num)) '객수',          "
                + "    Sum (A.ProFit_Pri) '이익금'          "
                + "    From SaD_" + tableName + " A, SaT_" + tableName + " B        "
                + "    Where A.Sale_Num=B.Sale_Num And A.Sale_Date='" + period1 + "' ";
        if (m_APP_USER_GRADE.equals("2") && !m_OFFICE_CODE.equals("") && null != m_OFFICE_CODE) {
            query += " ANd A.office_code='" + m_OFFICE_CODE + "' ";
        }
        //매출보기 옵션 설정
        if (m_viewOption.equals("cardY")) {
            query += "AND A.Card_Yn='1' ";
        } else if (m_viewOption.equals("cardN")) {
            query += "AND A.Card_Yn='0' ";
        }

        query += "    Group By A.Sale_Date      "
                + "    ) G  "
                + "  ) A FULL JOIN (      "
                + "    Select     'a' a,     순매출 '순매출_B',      "
                + "    객수 '객수_B' ,'객단가_B' = CASE WHEN 순매출=0 Then 0 ELSE 순매출/객수 END      "
                + "    From (          "
                + "      Select          "
                + "      Sum (A.TSell_Pri - A.TSell_RePri) '순매출',          "
                + "      Count (Distinct(B.Sale_Num)) '객수'  "
                + "      From SaD_" + tableName2 + " A, SaT_" + tableName2 + " B "
                + "      Where A.Sale_Num=B.Sale_Num And A.Sale_Date='" + period2 + "'          ";
        if (m_APP_USER_GRADE.equals("2") && !m_OFFICE_CODE.equals("") && null != m_OFFICE_CODE) {
            query += " ANd A.office_code='" + m_OFFICE_CODE + "' ";
        }
        //매출보기 옵션 설정
        if (m_viewOption.equals("cardY")) {
            query += "AND Card_Yn='1' ";
        } else if (m_viewOption.equals("cardN")) {
            query += "AND Card_Yn='0' ";
        }
        query += "      Group By A.Sale_Date      "
                + "    ) G  "
                + "  ) B ON A.a=B.a  ";

        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {

                DetailDataDisp(results);
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

    }

    private void DetailDataDisp(JSONArray results) {

        m_numberFormat = NumberFormat.getInstance();

        Context mContext = SalesDateActivity.this;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.activity_sale_date_detail, (ViewGroup) findViewById(R.id.layout_root));

        final TextView m_RealSales = (TextView) layout.findViewById(R.id.textViewRealSales);
        final TextView m_KNumber = (TextView) layout.findViewById(R.id.textViewKNumber);
        final TextView m_RealSalesYesterday = (TextView) layout.findViewById(R.id.textViewRealSalesYesterday);
        final TextView m_KPrice = (TextView) layout.findViewById(R.id.textViewKPrice);
        final TextView m_Cash = (TextView) layout.findViewById(R.id.textViewCash);
        final TextView m_Card = (TextView) layout.findViewById(R.id.textViewCard);
        final TextView m_Credit = (TextView) layout.findViewById(R.id.textViewCredit);
        final TextView m_Other = (TextView) layout.findViewById(R.id.textViewOther);

        // m_RealSales.setText();
        try {
            if (results.length() > 0) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject son = results.getJSONObject(i);
                    m_RealSales.setText(m_numberFormat.format(son.getInt("순매출")));
                    m_KNumber.setText(m_numberFormat.format(son.getInt("객수")));
                    m_KPrice.setText(m_numberFormat.format(son.getInt("객단가")));
                    m_Cash.setText(m_numberFormat.format(son.getInt("현금")));
                    m_Card.setText(m_numberFormat.format(son.getInt("카드")));
                    m_Credit.setText(m_numberFormat.format(son.getInt("외상")));
                    m_Other.setText(m_numberFormat.format(son.getInt("기타")));
                    m_RealSalesYesterday.setText(m_numberFormat.format(son.getInt("전일_순매출")));
                }
            } else {
                m_RealSales.setText(m_numberFormat.format(0));
                m_KNumber.setText(m_numberFormat.format(0));
                m_KPrice.setText(m_numberFormat.format(0));
                m_Cash.setText(m_numberFormat.format(0));
                m_Card.setText(m_numberFormat.format(0));
                m_Credit.setText(m_numberFormat.format(0));
                m_Other.setText(m_numberFormat.format(0));
                m_RealSalesYesterday.setText(m_numberFormat.format(0));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Alert
        new AlertDialog.Builder(SalesDateActivity.this,AlertDialog.THEME_HOLO_LIGHT)
                .setTitle("상세정보")
                //.setMessage("")
                .setView(layout)
                .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();

    }

    private void GetShopInfo() {
        //----------------------------------------//
        // 매장 정보 정의
        //----------------------------------------//
        m_shop = LocalStorage.getJSONObject(this, "currentShopData");
        m_userProfile = LocalStorage.getJSONObject(this, "userProfile");  // 2/27 수수료 매장일때 추가 내용

        //매출보기 옵션 설정
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        m_viewOption = pref.getString("prefSaleViewMethod", "All");

        m_OFFICE_CODE = "";
        //m_OFFICE_NAME = "";

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


            //  2/27 수수료 매장일때 추가 내용 추가
            m_APP_USER_GRADE = m_userProfile.getString("APP_USER_GRADE");
            //m_OFFICE_CODE = m_userProfile.getString("OFFICE_CODE");
            //m_OFFICE_NAME = m_userProfile.getString("Office_Name");

            m_OFFICE_CODE = m_shop.getString("OFFICE_CODE");
            //m_OFFICE_NAME = m_shop.getString("Office_Name");
            //toolbarTitle.setText(m_OFFICE_NAME);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //----------------------------------------//

    }

}