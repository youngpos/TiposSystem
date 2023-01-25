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
import java.util.Timer;
import java.util.TimerTask;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;
import tipsystem.utils.TIPOS;

public class SalesSlipActivity extends Activity implements AdapterView.OnItemClickListener,
        DatePickerDialog.OnDateSetListener {

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

    private SimpleDateFormat m_dateFormatter;
    private Calendar m_dateCalender;
    private ProgressDialog dialog;

    // 레이아웃 컨트롤 정의
    //private Button m_buttonSetDatePrevious; // 이전일자 버튼
    //private Button m_buttonSetDateNext;     // 이훙일자 버튼
    private Button m_buttonSetDate;         // 현재일자 버튼
    //private Button m_buttonSearch;          // 조회 버튼
    private CheckBox m_checkBoxAutoTimer;   // 자동타이머 체크박스
    private Spinner m_spinnerAutoTimer;     // 자동타이머 인터벌 스피너
    private Spinner m_spinnerPosNo;         // 포스번호 스피너
    private CheckBox m_checkBoxOrder;       // 최근순서 체크박스
    private ListView listSlip;              // 데이타 리스트

    // 어댑터 정의
    private SimpleAdapter adapter;
    private List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

    //타이머 정의
    private Timer timerSearch;
    private int timerCnt;
    private TimerTask timerTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_slip);

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

        // 일자클래스 생성
        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        m_dateCalender = Calendar.getInstance();

        // 레이아웃 컨트롤 정의
        //m_buttonSetDatePrevious = (Button) findViewById(R.id.buttonSetDatePrevious);
        //m_buttonSetDateNext = (Button) findViewById(R.id.buttonSetDateNext);
        m_buttonSetDate = (Button) findViewById(R.id.buttonSetDate);
        //m_buttonSearch=(Button)findViewById(R.id.buttonSearch);
        //m_checkBoxAutoTimer = (CheckBox) findViewById(R.id.checkBoxAutoTimer);
        //m_spinnerAutoTimer = (Spinner) findViewById(R.id.spinnerAutoTimer);
        m_spinnerPosNo = (Spinner) findViewById(R.id.spinnerPosNo);
        m_checkBoxOrder = (CheckBox) findViewById(R.id.checkBoxOrder);
        listSlip = (ListView) findViewById(R.id.listviewSlip);

        // 현재일자 버튼에 현재일 텍스트 정의
        m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender.getTime()));

        String[] from2 = new String[]{"전표번호", "시간", "판매금액", "반품금액", "순매출"};
        int[] to2 = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5};

        adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_slip_list, from2, to2);
        listSlip.setAdapter(adapter);
        listSlip.setOnItemClickListener(this);

//        m_spinnerAutoTimer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//
//                Toast.makeText(getApplicationContext(), "["+ String.format("%d",i)+"]가 선택되었습니다!", Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> adapterView) {
//
//            }
//        });

        doQuery();

        // 타이머 설정
        //timerSearch = new Timer();
        //TimerStart();

    }

    public void onClickSetDate(View view) {
        DatePickerDialog newDlg = new DatePickerDialog(this, this,
                m_dateCalender.get(Calendar.YEAR),
                m_dateCalender.get(Calendar.MONTH),
                m_dateCalender.get(Calendar.DAY_OF_MONTH));
        newDlg.show();
    }

    public void onClickSetDatePrevious(View view) {
        m_dateCalender.add(Calendar.DAY_OF_MONTH, -1);
        m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender.getTime()));

        doQuery();
    }

    public void onClickSetDateNext(View view) {
        m_dateCalender.add(Calendar.DAY_OF_MONTH, 1);
        m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender.getTime()));

        doQuery();
    }

    public void onClickSearch(View view) {
        doQuery();
    }

    private void doQuery() {
        mfillMaps.removeAll(mfillMaps);
        adapter.notifyDataSetChanged();

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        Calendar c = Calendar.getInstance();

        String period = m_buttonSetDate.getText().toString();

        int year1 = Integer.parseInt(period.substring(0, 4));
        int month1 = Integer.parseInt(period.substring(5, 7));

        String tableName = String.format("%04d%02d", year1, month1);

        String posNo = m_spinnerPosNo.getSelectedItem().toString();

        if (posNo.equals("")) {
            Toast.makeText(getApplicationContext(), "포스 번호가 없습니다!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 쿼리 작성하기
        String query = "";
        query = "select "
                + " RIGHT(ISNULL(A.Sale_Num,'000000'),4) AS 전표번호, "
                + " A.Sale_Time AS '시간', "
                + " SUM(Case When A.Sale_Yn='1' Then A.TSell_Pri+A.Dc_Pri Else 0 End) AS 판매금액, "
                + " SUM(Case When A.Sale_Yn='0' Then A.TSell_RePri+A.Dc_Pri Else 0 End) AS 반품금액, "
                + " SUM(A.TSell_Pri -A.TSell_RePri) AS 순매출 "
                + " From SaD_" + tableName + " A Right Join SaT_" + tableName + " B "
                + " On A.Sale_Num=B.Sale_Num "
                + " where A.Sale_Date ='" + period + "' "
                + "   and A.Pos_No ='" + posNo + "' "

                + " Group By A.Sale_Num, A.Sale_Time ";
//                + " Order By A.Sale_Num, A.Sale_Time"

        if (m_checkBoxOrder.isChecked()){
            query += "Order By A.Sale_Num Desc, A.Sale_Time ";
        }else{
            query += "Order By A.Sale_Num, A.Sale_Time ";
        }

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
                    SlipListView(results);
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
        m_dateCalender.set(i, i1, i2);
        m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender.getTime()));
        doQuery();

    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

        String period = m_buttonSetDate.getText().toString();
        int year1 = Integer.parseInt(period.substring(0, 4));
        int month1 = Integer.parseInt(period.substring(5, 7));
        String tableName = String.format("%04d%02d", year1, month1);

        String posNo = m_spinnerPosNo.getSelectedItem().toString();

        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(mfillMaps.get(i));

        String saleNum = map.get("전표번호");

        Intent intent;
        intent = new Intent(this, SalesSlipDetailActivity.class);


        String allSaleNum = period + posNo+saleNum;

        intent.putExtra("tableName", tableName);
        //intent.putExtra("saleNum", saleNum);
        intent.putExtra("saleNum", allSaleNum);

        startActivity(intent);
    }

    private void SlipListView(JSONArray results) {
        for (int index = 0; index < results.length(); index++) {

            try {
                JSONObject son = results.getJSONObject(index);
                HashMap<String, String> map = JsonHelper.toStringHashMap(son);

                map.put("판매금액", StringFormat.convertToNumberFormat(map.get("판매금액")));
                map.put("반품금액", StringFormat.convertToNumberFormat(map.get("반품금액")));
                map.put("순매출", StringFormat.convertToNumberFormat(map.get("순매출")));

                mfillMaps.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

//    private void TimerStart() {
//
//        String timerInt = m_spinnerAutoTimer.getSelectedItem().toString();
//        int timerSec = Integer.parseInt(timerInt);
//        long timer;
//
//        if (timerSec > 0) {
//            timer = timerSec * 1000;
//
//            if (m_checkBoxAutoTimer.isChecked()) {
//                if (timerTask != null) {
//                    timerTask.cancel();
//                }
//                timerTask = new TimerTask() {
//                    @Override
//                    public void run() {
//                        doQuery();
//                    }
//                };
//                timerSearch.schedule(timerTask, 0, timer);
//            }
//        }
//
//    }
//
//    private void TimerStop() {
//        if (timerTask != null) {
//            timerSearch.cancel();
//        }
//    }
}