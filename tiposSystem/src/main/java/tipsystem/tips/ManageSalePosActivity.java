package tipsystem.tips;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;

public class ManageSalePosActivity extends Activity implements AdapterView.OnItemClickListener,
        TabHost.OnTabChangeListener,
        DatePickerDialog.OnDateSetListener {

    Context mContext;

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

    String m_APP_USER_GRADE;
    String m_OFFICE_CODE;    // 수수료매장일때 고정될 오피스코드
    String m_OFFICE_NAME;    // 수수료매장일때 고정될 오피스네임
    private String Card_Yn = "A.Card_Yn = '0' AND"; // 다중 매장시 사용 여부 결정

    TabHost m_tabHost;
    ListView m_listSalesTab1;
    ListView m_listSalesTab2;
    ListView m_listSalesTab3;

    SimpleAdapter adapter1;
    SimpleAdapter adapter2;
    SimpleAdapter adapter3;

    List<HashMap<String, String>> mfillMaps1 = new ArrayList<HashMap<String, String>>();
    List<HashMap<String, String>> mfillMaps2 = new ArrayList<HashMap<String, String>>();
    List<HashMap<String, String>> mfillMaps3 = new ArrayList<HashMap<String, String>>();

    Button m_period1;
    Button m_period2;
    CheckBox m_checkBox1Day;
    Spinner m_spinnerPosNo; // 포스번호 스피너
    Spinner m_spinnerWriter; // 판매담당자 스피너

    /*TextView m_barCode;
    TextView m_productName;*/
    //TextView m_customerCode;
    //TextView m_customerName;


    String m_CalendarDay;

    //매출보기 옵션
    String m_viewOption;

    DatePicker m_datePicker;

    SimpleDateFormat m_dateFormatter;
    Calendar m_dateCalender1;
    Calendar m_dateCalender2;

    int m_dateMode = 0;

    NumberFormat m_numberFormat;

    ProgressDialog dialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managesale_pos);

        mContext = this;

        //----------------------------------------//
        // 매장 정보 정의
        //----------------------------------------//
        GetShopInfo();

        // 일자 버튼 정의
        m_period1 = (Button) findViewById(R.id.buttonSetDate1);
        m_period2 = (Button) findViewById(R.id.buttonSetDate2);

        m_spinnerPosNo = (Spinner) findViewById(R.id.spinnerPosNo);
        m_spinnerWriter = (Spinner) findViewById(R.id.spinnerWriter);

        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        m_numberFormat = NumberFormat.getInstance();

        m_dateCalender1 = Calendar.getInstance();
        m_dateCalender2 = Calendar.getInstance();

        m_CalendarDay = m_dateFormatter.format(m_dateCalender1.getTime());

        m_period1.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        m_period2.setText(m_dateFormatter.format(m_dateCalender2.getTime()));

        // 탭별 리스트뷰 정의
        m_listSalesTab1 = (ListView) findViewById(R.id.listviewPosListTab1);
        m_listSalesTab2 = (ListView) findViewById(R.id.listviewPosListTab2);
        m_listSalesTab3 = (ListView) findViewById(R.id.listviewPosListTab3);

        // 포스별
        String[] from1 = new String[]{"포스", "Temp", "판매", "현금", "반품", "카드", "순매출", "기타"};
        int[] to1 = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8};

        adapter1 = new SimpleAdapter(this, mfillMaps1, R.layout.activity_listview_managsale_pos_list_tab1, from1, to1);
        m_listSalesTab1.setAdapter(adapter1);


        // 포스별/담당자별
        String[] from2 = new String[]{"포스", "담당자", "판매", "현금", "반품", "카드", "순매출", "기타"};
        int[] to2 = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8};

        adapter2 = new SimpleAdapter(this, mfillMaps2, R.layout.activity_listview_managsale_pos_list_tab2, from2, to2);
        m_listSalesTab2.setAdapter(adapter2);

        // 포스/일자별
        String[] from3 = new String[]{"포스", "일자", "판매", "현금", "반품", "카드", "순매출", "기타"};
        int[] to3 = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8};

        adapter3 = new SimpleAdapter(this, mfillMaps3, R.layout.activity_listview_managsale_pos_list_tab3, from3, to3);
        m_listSalesTab3.setAdapter(adapter3);

        m_tabHost = (TabHost) findViewById(R.id.tabhostManageSales);
        m_tabHost.setup();

        TabHost.TabSpec spec = m_tabHost.newTabSpec("tag1");

        spec.setContent(R.id.tab1);
        spec.setIndicator("포스");
        m_tabHost.addTab(spec);

        spec = m_tabHost.newTabSpec("tag2");
        spec.setContent(R.id.tab2);
        spec.setIndicator("포스\n담당자별");
        m_tabHost.addTab(spec);

        spec = m_tabHost.newTabSpec("tag3");
        spec.setContent(R.id.tab3);
        spec.setIndicator("포스\n일자별");
        m_tabHost.addTab(spec);

        //----------------------------------------//
        // 탭 속성 정의
        //----------------------------------------//
        //https://www.tabnine.com/code/java/methods/android.widget.TabHost/getTabWidget
        // 2022.04.04. 탭 텍스트 색상 변경
        for (int i = 0; i < m_tabHost.getTabWidget().getChildCount(); i++) {
            TextView tv = (TextView) m_tabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#ffffff"));
        }
        //----------------------------------------//

        // 탭 클릭리스너 설정
        m_listSalesTab1.setOnItemClickListener(this);
        m_listSalesTab2.setOnItemClickListener(this);
        m_listSalesTab3.setOnItemClickListener(this);

        // 하루 체크박스 정의
        m_checkBox1Day = (CheckBox) findViewById(R.id.checkBox1Day);
        m_checkBox1Day.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    m_period2.setEnabled(false);
                    m_period2.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
                } else {
                    m_period2.setEnabled(true);
                }
            }
        });

        setPosNo();
        //setWriter();
        //doQuery();
    }

    // 매장정보
    private void GetShopInfo() {
        //----------------------------------------//
        // 매장 정보 정의
        //----------------------------------------//
        m_shop = LocalStorage.getJSONObject(this, "currentShopData");
        m_userProfile = LocalStorage.getJSONObject(this, "userProfile");  // 2/27 수수료 매장일때 추가 내용

        //매출보기 옵션 설정
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        //m_OFFICE_CODE = "";

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

            m_APP_USER_GRADE = m_userProfile.getString("APP_USER_GRADE");
            m_OFFICE_CODE = m_userProfile.getString("OFFICE_CODE");
            m_OFFICE_NAME = m_userProfile.getString("Office_Name");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //----------------------------------------//
    }

    private void setPosNo() {

        String query = "SELECT IsNull([224],1) AS PosCount FROM POS_SET";

        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                setPosNoComboBox(results);
                // 담당자 조회
                setWriter();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

    }

    private void setPosNoComboBox(JSONArray results) {

        ArrayList<String> lSpList = new ArrayList<String>();
        try {

            if (results.length() > 0) {

                String posCnt = null;

                lSpList.add("전체");

                // 자료는 1개 이므로
                JSONObject son = results.getJSONObject(0);
                posCnt = son.getString("PosCount");
                int posCount = Integer.parseInt(posCnt);
                for (int index = 0; index < posCount; index++) {

                    String PosNo = String.format("%02d", index + 1);
                    lSpList.add(PosNo);
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ManageSalePosActivity.this, android.R.layout.simple_spinner_item, lSpList);

                m_spinnerPosNo.setAdapter(adapter);
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(ManageSalePosActivity.this, android.R.layout.simple_spinner_item, lSpList);

                m_spinnerPosNo.setAdapter(adapter);
            }

            //Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setWriter() {

        String query = "Select Distinct(User_Name) UserName From Admin_User Order by User_Name ASC";

        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                setWriterComboBox(results);
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

    }

    private void setWriterComboBox(JSONArray results) {

        ArrayList<String> lSpList = new ArrayList<String>();
        lSpList.add("전체");

        if (results.length() > 0) {

            for (int index = 0; index < results.length(); index++) {

                try {
                    JSONObject son = results.getJSONObject(index);
                    HashMap<String, String> map = JsonHelper.toStringHashMap(son);

                    String writer = map.get("UserName");
                    lSpList.add(writer);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ManageSalePosActivity.this, android.R.layout.simple_spinner_item, lSpList);
        m_spinnerWriter.setAdapter(adapter);

    }

    // 새로입력 버튼 클릭 이벤트
    public void OnClickRenew(View v) {

        mfillMaps1.removeAll(mfillMaps1);
        mfillMaps2.removeAll(mfillMaps2);
        mfillMaps3.removeAll(mfillMaps3);

        m_spinnerPosNo.setSelection(0);
        m_spinnerWriter.setSelection(0);

        adapter1.notifyDataSetChanged();
        adapter2.notifyDataSetChanged();
        adapter3.notifyDataSetChanged();

    }

    // 조회 버튼 클릭 이벤트
    public void OnClickSearch(View v) {
        doQuery();
    }

    // 조회
    public void doQuery() {

        switch (m_tabHost.getCurrentTab()) {
            case 0:
                queryListForTab(0);
                break;
            case 1:
                queryListForTab(1);
                break;
            case 2:
                queryListForTab(2);
                break;
        }
    }

    // From 일자 버튼 클릭 이벤트
    public void onClickSetDate1(View v) {

        DatePickerDialog newDlg = new DatePickerDialog(this, this,
                m_dateCalender1.get(Calendar.YEAR),
                m_dateCalender1.get(Calendar.MONTH),
                m_dateCalender1.get(Calendar.DAY_OF_MONTH));
        newDlg.show();

        m_dateMode = 1;
    }

    // To 일자 버튼 클릭 이벤트
    public void onClickSetDate2(View v) {

        DatePickerDialog newDlg = new DatePickerDialog(this, this,
                m_dateCalender2.get(Calendar.YEAR),
                m_dateCalender2.get(Calendar.MONTH),
                m_dateCalender2.get(Calendar.DAY_OF_MONTH));

        newDlg.show();

        m_dateMode = 2;
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        if (m_dateMode == 1) {
            m_dateCalender1.set(i, i1, i2);
            m_period1.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        } else if (m_dateMode == 2) {
            m_dateCalender2.set(i, i1, i2);
            m_period2.setText(m_dateFormatter.format(m_dateCalender2.getTime()));
        }

        // 체크되어 있으면 날짜 2개를 똑같이 바꾸어줌
        if (m_checkBox1Day.isChecked()) {
            m_period2.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        }

        m_dateMode = 0;

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
    }

    @Override
    public void onTabChanged(String s) {
        // 반응 없음 ???
        //doQuery();
    }

    private void queryListForTab(int tabIndex) {

        //mfillMaps1.removeAll(mfillMaps1);
        if (tabIndex == 0) mfillMaps1.removeAll(mfillMaps1);
        else if (tabIndex == 1) mfillMaps2.removeAll(mfillMaps2);
        else if (tabIndex == 2) mfillMaps3.removeAll(mfillMaps3);

        String period1 = m_period1.getText().toString();
        String period2 = m_period2.getText().toString();
        String posNo = m_spinnerPosNo.getSelectedItem().toString();
        String writer = m_spinnerWriter.getSelectedItem().toString();
        //String customerCode = m_customerCode.getText().toString();


        String query = "";

        int year1 = Integer.parseInt(period1.substring(0, 4));
        int month1 = Integer.parseInt(period1.substring(5, 7));

        int year2 = Integer.parseInt(period2.substring(0, 4));
        int month2 = Integer.parseInt(period2.substring(5, 7));

        query = " Select G.Pos_No '포스',";

        if (tabIndex == 0) query += "	'' 'Temp',";
        else if (tabIndex == 1) query += " G.Writer '담당자',";
        else if (tabIndex == 2) query += " G.sale_date '일자',";

        query += "	G.판매 '판매',"
                + "	G.반품 '반품',"
                + "	G.순매출 '순매출',"
                + "	G.현금 '현금',"
                + "	G.카드 '카드',"
                + "	G.외상금액 +"
                + "	G.CMS할인 +"
                + "	G.포인트사용 +"
                + "	G.에누리할인 +"
                + "	G.상품권 +"
                + "	G.앱쿠폰 +"
                + "	G.예비쿠폰1 +"
                + "	G.예비쿠폰2 +"
                + "	G.예비쿠폰3 +"
                + "	G.캐쉬백사용 AS '기타'"
                + "	From (";

        query += " Select G.Pos_No,";


        if (tabIndex == 1) query += " G.Writer,";
        else if (tabIndex == 2) query += " G.sale_date,";

        query += " Sum (G.판매) '판매',"
                + " Sum(G.반품) '반품',"
                + " Sum(G.할인) '할인',"
                + " Sum (G.순매출) '순매출',"
                + " Sum (G.현금) '현금',"
                + " Sum (G.카드) '카드',"
                + " Sum (G.외상금액) '외상금액',"
                + " Sum (G.CMS할인) 'CMS할인',"
                + " Sum (G.포인트사용) '포인트사용',"
                + " Sum (G.에누리할인) '에누리할인',"
                + " Sum (G.상품권) '상품권',"
                + " Sum (G.앱쿠폰) '앱쿠폰',"
                + " Sum (G.예비쿠폰1) '예비쿠폰1',"
                + " Sum (G.예비쿠폰2) '예비쿠폰2',"
                + " Sum (G.예비쿠폰3) '예비쿠폰3',"
                + " Sum (G.캐쉬백사용) '캐쉬백사용',"
                + " Sum (G.절사금액) '절사금액',"
                + " Sum (G.쿠폰할인액) '쿠폰할인액',"
                + " Sum (G.카드현장할인액) '카드현장할인액'"
                + " From (";

        for (int y = year1; y <= year2; y++) {
            int m1 = 1, m2 = 12;
            if (y == year1) m1 = month1;
            if (y == year2) m2 = month2;
            for (int m = m1; m <= m2; m++) {

                String tableName = String.format("%04d%02d", y, m);

                query += " Select A.Pos_No, ";

                if (tabIndex == 1) query += "	A.Writer,";
                else if (tabIndex == 2) query += " A.sale_date,";

                query += " '판매'=Sum(Case When A.Sale_Yn='1' Then A.TSell_Pri+A.Dc_Pri Else 0 End),"
                        + " '반품'=Sum(Case When A.Sale_Yn='0' Then A.TSell_RePri+A.Dc_Pri Else 0 End),"
                        + " '할인'=Sum(Case When A.Sale_Yn='1' Then A.DC_Pri Else A.DC_Pri *-1 End),"
                        + " Sum (a.TSell_Pri - a.TSell_RePri) '순매출',"
                        + " Sum (Round(b.Cash_Pri * a.Money_Per, 4)) '현금',"
                        + " Sum (a.Card_Pri) '카드',"
                        + " Sum (Round(b.Dec_Pri * a.Money_Per, 4)) '외상금액',"
                        + " Sum (Round(b.CMS_Pri * a.Money_Per, 4)) 'CMS할인',"
                        + " Sum (Round(b.Cus_PointUse * a.Money_Per, 4)) '포인트사용',"
                        + " Sum (Round(b.Sub_Pri * a.Money_Per, 4)) '에누리할인',"
                        + " Sum (Round(b.Gift_Pri * a.Money_Per, 4)) '상품권',"
                        + " Sum (Round(isnull(b.APP_DCPri,0)* a.Money_Per,4)) '앱쿠폰',"
                        + " Sum (Round(isnull(b.ETC_DCPRI1,0)*a.Money_per,4)) '예비쿠폰1',"
                        + " Sum (Round(isnull(b.ETC_DCPRI2,0)*a.Money_per,4)) '예비쿠폰2',"
                        + " Sum (Round(isnull(b.ETC_DCPRI3,0)*a.Money_per,4)) '예비쿠폰3',"
                        + " Sum (Round(b.CashBack_PointUse * a.Money_Per, 4)) '캐쉬백사용',"
                        + " Sum (Round(b.Cut_Pri * a.Money_Per, 4)) '절사금액',"
                        + " Sum (Round(b.BC_Coupon_DC * a.Money_Per, 4)) '쿠폰할인액',"
                        + " Sum (Round(b.BC_Card_DC * a.Money_Per, 4)) '카드현장할인액'"
                        + " From SaD_" + tableName + " A LEFT JOIN SaT_" + tableName + " b"
                        + " ON A.Sale_Num=b.Sale_Num"
                        + " Where 1=1"
                        + " AND A.Sale_Date >= '" + period1 + "'"
                        + " AND A.Sale_Date <= '" + period2 + "'"
                        + " AND A.Office_Code  LIKE '" + m_OFFICE_CODE + "%' ";// 수수료 조건 추가

                // 포스번호 조건 추가
                if (posNo.equals("") || posNo.equals("전체")) {
                } else query += "   and A.Pos_No ='" + posNo + "' ";

                // 담당자 조건 추가
                if (writer.equals("") || writer.equals("전체")) {
                } else query += "   and A.Writer ='" + writer + "' ";

                query += " Group By A.Pos_No";

                if (tabIndex == 1) query += ",A.Writer ";
                else if (tabIndex == 2) query += ",A.sale_date ";

                query += " UNION ALL ";
            }
        }
        query = query.substring(0, query.length() - 11);
        if (tabIndex == 0)
            query += "	) G "
                    + " Group By G.Pos_No "
                    + " ) G "
                    + " ORDER BY G.Pos_No ";
        else if (tabIndex == 1)
            query += "	) G "
                    + " Group By G.Pos_No, G.Writer "
                    + " ) G "
                    + " ORDER BY G.Pos_No, G.Writer ";
        else if (tabIndex == 2)
            query += "	) G "
                    + " Group By G.Pos_No, G.sale_date "
                    + " ) G "
                    + " ORDER BY G.Pos_No, G.sale_date ";

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {

                dialog.dismiss();
                dialog.cancel();
                if (results.length() > 0)
                    updateListForTab(results, tabIndex);

                if (tabIndex == 0)
                    adapter1.notifyDataSetChanged();
                else if (tabIndex == 1)
                    adapter2.notifyDataSetChanged();
                else if (tabIndex == 2)
                    adapter3.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();

                if (tabIndex == 0)
                    adapter1.notifyDataSetChanged();
                else if (tabIndex == 1)
                    adapter2.notifyDataSetChanged();
                else if (tabIndex == 2)
                    adapter3.notifyDataSetChanged();

                Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    private void updateListForTab(JSONArray results, int tabIndex) {

        for (int index = 0; index < results.length(); index++) {

            try {
                JSONObject son = results.getJSONObject(index);
                HashMap<String, String> map = JsonHelper.toStringHashMap(son);

                map.put("판매", StringFormat.convertToNumberFormat(map.get("판매")));
                map.put("반품", StringFormat.convertToNumberFormat(map.get("반품")));
                map.put("순매출", StringFormat.convertToNumberFormat(map.get("순매출")));
                map.put("현금", StringFormat.convertToNumberFormat(map.get("현금")));
                map.put("카드", StringFormat.convertToNumberFormat(map.get("카드")));
                map.put("기타", StringFormat.convertToNumberFormat(map.get("기타")));

                if (tabIndex == 0)
                    mfillMaps1.add(map);
                else if (tabIndex == 1)
                    mfillMaps2.add(map);
                else if (tabIndex == 2)
                    mfillMaps3.add(map);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
