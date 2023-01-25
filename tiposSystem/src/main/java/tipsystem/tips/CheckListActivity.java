package tipsystem.tips;

import android.annotation.TargetApi;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;

public class CheckListActivity extends Activity implements OnItemClickListener,
        DatePickerDialog.OnDateSetListener {

    //검색 결과 돌려 받기
    private static final int CUSTOMER_MANAGER_REQUEST = 3;

    JSONObject m_shop;

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

    //날자 설정관련
    DatePicker m_datePicker;
    Button m_buttonSetDate;

    SimpleDateFormat m_dateFormatter;
    Calendar m_dateCalender1;
    Calendar m_dateCalender2;
    NumberFormat m_numberFormat;

    //목록
    ListView m_listPurchaseList;
    TextView m_ListViewRow;

    //거래처 검색 버튼
    Button bottnSearch;
    EditText m_customerCode;
    EditText m_customerName;

    //신규 매입 등록 버튼
    Button buttonNewRegister;

    //리스트목록 필요한 것들
    SimpleAdapter adapter;
    List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();
    HashMap<String, String> m_tempInNum = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_list);

        // 매장 정보 불러 오기
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

        //목록 할당 하기
        m_listPurchaseList= (ListView)findViewById(R.id.listviewCheckList);

        //날자 설정 관련
        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        m_numberFormat = NumberFormat.getInstance();
        m_dateCalender1 = Calendar.getInstance();
        m_dateCalender2 = Calendar.getInstance();

        m_buttonSetDate = (Button)findViewById(R.id.buttonSetDate);
        m_ListViewRow = (TextView) findViewById(R.id.textViewListViewRow);

        m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        //날자 설정 관련 끝

        //리스트뷰 목록 생성 어댑터
        String[] from = new String[] {"In_Date", "In_Num", "Office_Code", "Office_Name", "I_Pri", "I_RePri", "In_Pri", "State", "Chk_State", "Chk_Num" };
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8, R.id.item9, R.id.item10};

        adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_check_list, from, to);
        m_listPurchaseList.setAdapter(adapter);

        m_listPurchaseList.setOnItemClickListener(this);

    }
    //목록 검색 하기 함수

    private void serachCheckList(){
        Toast.makeText(getApplicationContext(), "조회 시작.", Toast.LENGTH_SHORT);

        mfillMaps.removeAll(mfillMaps);

        String period = m_buttonSetDate.getText().toString();

        String query = "";

        int year1 = Integer.parseInt(period.substring(0, 4));
        int month1 = Integer.parseInt(period.substring(5, 7));

        String tableName = String.format("%04d%02d", year1, month1);

        //query = "Select * From int_"+tableName+" Where in_date='"+period+"' "+customerCode+" order by In_Seq ASC ";
        query  = " SELECT ";
        query += " A.In_Num AS In_Num, ";
        query += " A.Chk_Num AS Chk_Num, ";
        query += " A.Office_Code AS Office_Code, ";
        query += " A.Office_Name AS Office_Name, ";
        query += " A.In_Date As In_Date, ";
        query += " ISNULL((SELECT SUM(TPur_Pri) I_Pri From InD_" + tableName + " C WHERE C.IN_Num = A.In_Num AND In_Count > 0),0) AS I_Pri, ";
        query += " ISNULL((SELECT ABS(SUM(TPur_Pri)) I_RePri From InD_" + tableName + " C WHERE C.IN_Num = A.In_Num AND In_Count < 0),0) AS I_RePri, ";
        query += " A.In_Pri AS In_Pri, ";
        query += " IsNull(C.State,'0') AS State, ";
        query += " CASE WHEN ISNULL(C.State,'')='0' THEN '미검수' WHEN ISNULL(C.State,'')='1' THEN '작업중..' WHEN ISNULL(C.State,'')='2' THEN '검수완료' WHEN ISNULL(C.State,'')='3' THEN '작업진행중..' WHEN ISNULL(C.State,'') = '' THEN '미검수' ELSE ISNULL(C.State,'') END AS Chk_State ";
        query += " FROM inT_" + tableName + " A LEFT JOIN Purchase_Chk_T AS C ON A.Chk_num=C.Chk_Num ";
        query += " WHERE A.In_Date = '" + period + "' ";
        query += " ORDER BY A.In_Num ";


        new MSSQL(new MSSQL.MSSQLCallbackInterface() {
            @Override
            public void onRequestCompleted(JSONArray results) {
                updateCustomerList(results);
            }
        }).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
    }

    //리스트에 뿌리기
    private void updateCustomerList(JSONArray results){
        try {
            if ( results.length() > 0 ) {
                HashMap<String, String> map = new HashMap<String, String>();
                for(int i = 0; i < results.length() ; i++) {
                    JSONObject son = results.getJSONObject(i);
                    map =JsonHelper.toStringHashMap(son);
                    //"In_Date", "In_Num", "Office_Code", "Office_Name", "In_Pri", "In_RePri", "Profit_Pri", "Profit_Rate"
                    //map.put("In_Pri", StringFormat.convertToIntNumberFormat(map.get("In_Pri")));
                    mfillMaps.add(map);
                }
            }else{
                Toast.makeText(getApplicationContext(), "조회 결과가 없습니다.", Toast.LENGTH_SHORT);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //다음 엑티브로 가기
    private void goCheckRegist(){

        String row = m_ListViewRow.getText().toString();
        Integer position = Integer.parseInt(row);

        m_tempInNum.putAll(mfillMaps.get(position));
        //m_ListViewRow.setText(position);

        String in_num = m_tempInNum.get("In_Num");
        String in_date =  m_tempInNum.get("In_Date");
        String office_code = m_tempInNum.get("Office_Code");
        String office_name = m_tempInNum.get("Office_Name");
        String i_pri = m_tempInNum.get("I_Pri");
        String i_repri = m_tempInNum.get("I_RePri");
        String in_pri = m_tempInNum.get("In_Pri");
        String state = m_tempInNum.get("State");
        String chk_state = m_tempInNum.get("Chk_State");
        String chk_num = m_tempInNum.get("Chk_Num");

        Log.i("전표번호", in_num);

        Intent intent = new Intent(this, CheckRegistActivity.class);
        intent.putExtra("In_Num", in_num);
        intent.putExtra("Office_Code", office_code);
        intent.putExtra("In_Date", in_date);
        intent.putExtra("Office_Name", office_name);
        intent.putExtra("I_Pri", i_pri);
        intent.putExtra("I_RePri", i_repri);
        intent.putExtra("In_Pri", in_pri);
        intent.putExtra("State", state);
        intent.putExtra("Chk_State", chk_state);
        intent.putExtra("Chk_Num", chk_num);
        //intent.putExtra("Gubun", "InT");
        startActivity(intent);

    }

    //날자 변경설정
    public void onClickSetDate(View view) {
        DatePickerDialog newDlg = new DatePickerDialog(this, this,
                m_dateCalender1.get(Calendar.YEAR),
                m_dateCalender1.get(Calendar.MONTH),
                m_dateCalender1.get(Calendar.DAY_OF_MONTH));
        newDlg.show();
    }

    //다음 날자 선택
    public void onClickSetDatePrevious(View view) {
        m_dateCalender1.add(Calendar.DAY_OF_MONTH, -1);
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);
        m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

        serachCheckList();
    }

    //이전날자 선택
    public void onClickSetDateNext(View view) {
        m_dateCalender1.add(Calendar.DAY_OF_MONTH, 1);
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, 1);
        m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

        serachCheckList();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        m_dateCalender1.set(year, monthOfYear, dayOfMonth);
        m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        m_dateCalender2.set(year, monthOfYear, dayOfMonth);
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);

        serachCheckList();
    }

    public void onButtonClose(View v) {
        //Toast.makeText(getApplicationContext(),"돌아가기 버큰을 눌렀습니다.",Toast.LENGTH_LONG);
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parg0, View arg1, int arg2, long arg3) {

        //"In_Date", "In_Num", "Office_Code", "Office_Name", "In_Pri", "In_RePri", "Profit_Pri", "Profit_Rate"

        Integer position = arg2;

        m_tempInNum.putAll(mfillMaps.get(position));
        m_ListViewRow.setText(position.toString());

        String state = m_tempInNum.get("State");

        //Chk_State
        // '1' THEN '작업중..' WHEN ISNULL(C.State,'')=
        // '2' THEN '검수완료' WHEN ISNULL(C.State,'')=
        // '3' THEN '작업진행중..' WHEN ISNULL(C.State,'') =
        // '' THEN '미검수' ELSE ISNULL(C.State,'') END AS Chk_State ";

        //if ((chk_state.equals("검수완료")) || (chk_state.equals("작업진행중"))){
        if (state.equals("2")){
            Toast.makeText(getApplicationContext(), "검수 완료되었습니다.", Toast.LENGTH_SHORT);
        }else if (state.equals("3")){
            // 전체 매입검수
            //----------------------------------------//
            // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
            //----------------------------------------//
            //AlertDialog.Builder dialog = new AlertDialog.Builder(CheckListActivity.this)
            AlertDialog.Builder dialog = new AlertDialog.Builder(CheckListActivity.this,AlertDialog.THEME_HOLO_LIGHT)
                    //----------------------------------------//
                    .setTitle("검수확인")
                    .setMessage("작업진행중인 자료를 검수 할까요?");
            dialog.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                    String row = m_ListViewRow.getText().toString();
                    goCheckRegist();
                }
            });
            dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            dialog.show();

        }else{
            goCheckRegist();

        }

    }

    public void showDialog(String msg) {

        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        //AlertDialog.Builder builder = new AlertDialog.Builder(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
        //----------------------------------------//
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    @Override
    public void onResume(){

        serachCheckList();
        super.onResume();
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            ActionBar actionbar = getActionBar();
            actionbar.setDisplayShowHomeEnabled(false);
            actionbar.setDisplayShowTitleEnabled(true);
            actionbar.setDisplayShowCustomEnabled(true);
            actionbar.setTitle("검수목록");

            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.purchase_list_detail_view, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, TIPSPreferences.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void nextCheckRegist(){
    }


}