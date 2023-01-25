package tipsystem.tips;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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

public class SalesReceiptActivity extends Activity implements AdapterView.OnItemClickListener,
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
    private Spinner m_spinnerPosNo; // 포스번호 스피너
    private EditText m_SaleNum;     // 전표번호 입력 테스트박스
    private CheckBox m_checkBox1Day;

    private ListView listSlip;      // 데이타 리스트

    private TextView m_Rcnt; //건수
    private TextView m_Ramt; //순매출

    //private String m_OFFICE_CODE = null;  //수수료매장거래처코드
    //private String m_PosNo;

    // 어댑터 정의
    private SimpleAdapter adapter;
    private List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_receipt);

        mContext = this;

        //----------------------------------------//
        // 매장 정보 정의
        //----------------------------------------//
        GetShopInfo();

        // 레이아웃 컨트롤 정의
        m_period1 = (Button) findViewById(R.id.buttonSetDate1);
        m_period2 = (Button) findViewById(R.id.buttonSetDate2);

        // 합계 레이아웃 정의
        m_Rcnt = (TextView) findViewById(R.id.textViewCount);
        m_Ramt = (TextView) findViewById(R.id.textViewRamt);

        // 일자클래스 생성
        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        m_dateCalender1 = Calendar.getInstance();
        m_dateCalender2 = Calendar.getInstance();

        m_period1.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        m_period2.setText(m_dateFormatter.format(m_dateCalender2.getTime()));

        m_spinnerPosNo = (Spinner) findViewById(R.id.spinnerPosNo);
        m_SaleNum = (EditText) findViewById(R.id.editTextSaleNum);
        m_checkBox1Day = (CheckBox) findViewById(R.id.checkBox1Day);
        m_checkBox1Day.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
//                if (isChecked) {
//                    m_period2.setEnabled(false);
//                    m_period2.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
//                } else {
//                    m_period2.setEnabled(true);
//                }
            }
        });

        listSlip = (ListView) findViewById(R.id.listviewSlip);

        String[] from2 = new String[]{"일자", "시간", "포스번호", "전표번호", "순매출"};
        int[] to2 = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5};

        adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_receipt_list, from2, to2);
        listSlip.setAdapter(adapter);
        listSlip.setOnItemClickListener(this);

        //doQuery();

        // 스피너에 포스번호 세트
        //m_PosNo = "전체";
        setPosNo();

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

    public void onClickSearch(View view) {
        doQuery();
    }

    private void doQuery() {
        mfillMaps.removeAll(mfillMaps);
        adapter.notifyDataSetChanged();

        m_Rcnt.setText("0");
        m_Ramt.setText("0");

        String period1 = m_period1.getText().toString();
        String period2 = m_period2.getText().toString();

        int year1 = Integer.parseInt(period1.substring(0, 4));
        int month1 = Integer.parseInt(period1.substring(5, 7));
        int day1 = Integer.parseInt(period1.substring(8, 10));

        int year2 = Integer.parseInt(period2.substring(0, 4));
        int month2 = Integer.parseInt(period2.substring(5, 7));
        int day2 = Integer.parseInt(period2.substring(8, 10));

        // 범위 체크
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

        String posNo = m_spinnerPosNo.getSelectedItem().toString();
        String saleNum = m_SaleNum.getText().toString();

        // 쿼리 작성하기
        String query = "";
        query = " select "
                + " G.일자, "
                + " G.시간, "
                + " G.포스번호, "
                + " G.전표번호, "
                + " G.순매출 "
                + " From ( ";

        //----------------------------------------//
        // 월단위로 테이블 변경 쿼리후 UNION ALL
        //----------------------------------------//
        for (int y = year1; y <= year2; y++) {
            int m1 = 1, m2 = 12;
            if (y == year1) m1 = month1;
            if (y == year2) m2 = month2;

            for (int m = m1; m <= m2; m++) {
                String tableName = String.format("%04d%02d", y, m);

                query += " select "
                        + " A.Sale_Date AS '일자', "
                        + " A.Sale_Time AS '시간', "
                        + " A.Pos_No AS '포스번호', "
                        + " SUBSTRING(A.Sale_Num,13,4) AS '전표번호', "
                        + " SUM(A.TSell_Pri -A.TSell_RePri) AS 순매출 "
                        + " From SaD_" + tableName + " A Right Join SaT_" + tableName + " B "
                        + " On A.Sale_Num=B.Sale_Num "
                        + " where A.Sale_date>='" + period1 + "' And A.Sale_Date<='" + period2 + "'";

                if (posNo.equals("") || posNo.equals("전체")) {
                } else query += "   and A.Pos_No ='" + posNo + "' ";

                if (saleNum.equals("")) {
                } else query += "   and A.Sale_Num LIKE '%" + saleNum + "%' ";

                query += " Group By A.Sale_Date, A.Sale_Time,A.Pos_No,A.Sale_Num  ";
                query += " UNION ALL ";
            }
        }
        //----------------------------------------//
        // 마자막은 UNION ALL 생략한다
        query = query.substring(0, query.length() - 11);
        query += " ) G "
                + " Order By G.일자,G.시간, G.포스번호, G.전표번호 ";

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

//        // 체크되어 있으면 날짜 2개를 똑같이 바꾸어줌
//        if (m_checkBox1Day.isChecked()) {
//            m_period2.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
//        }

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

        String saleDay = map.get("일자").toString();
        String tableName = saleDay.replace("-", "").substring(0, 6);
        String posNo = map.get("포스번호");
        //int day1 = Integer.parseInt(saleDay);
        //String saleDate = String.format("%04d-%02d-%02d", year1, month1, day1);
        String saleNum = map.get("전표번호");

        Intent intent;
        intent = new Intent(this, SalesSlipDetailActivity.class);

        String allSaleNum = saleDay + posNo + saleNum;

        intent.putExtra("tableName", tableName);
        intent.putExtra("saleNum", allSaleNum);

        startActivity(intent);
    }

    private void DataListView(JSONArray results) {
        // 합계 계산
        float mRamt = 0;
        int mCnt = results.length();

        for (int index = 0; index < results.length(); index++) {

            try {
                JSONObject son = results.getJSONObject(index);
                HashMap<String, String> map = JsonHelper.toStringHashMap(son);

                String ramt = son.getString("순매출");

                map.put("순매출", StringFormat.convertTNumberFormat(map.get("순매출")));

                // 여기서 트리밍 하면 시간이 오래 걸린다 주의!!!
                //String saleNum = map.get("전표번호");
                //map.put("전표번호", saleNum.substring(12,16));

                mRamt += Float.parseFloat(ramt);

                mfillMaps.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        // 합계 출력
        m_Rcnt.setText(StringFormat.convertToNumberFormat(String.format("%d", mCnt)));
        m_Ramt.setText(StringFormat.convertToNumberFormat(String.format("%.0f", mRamt)));
    }

    private void setPosNo() {

        String query = "SELECT IsNull([224],1) AS PosCount FROM POS_SET";

        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {

                setPosNoComboBox(results);
                doQuery();
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

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(SalesReceiptActivity.this, android.R.layout.simple_spinner_item, lSpList);

                m_spinnerPosNo.setAdapter(adapter);
            } else {
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(SalesReceiptActivity.this, android.R.layout.simple_spinner_item, lSpList);

                m_spinnerPosNo.setAdapter(adapter);
            }

            //Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
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

            //m_OFFICE_CODE = m_shop.getString("OFFICE_CODE");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //----------------------------------------//
    }

    public void OnClickRenew(View v) {

        mfillMaps.removeAll(mfillMaps);

        // 스피너 값 리셋
        //spinnerReset();
        //queryComboBoxIn(0, "", "", "");
        m_spinnerPosNo.setSelection(0);

        adapter.notifyDataSetChanged();
        m_SaleNum.setText("");

    }
}