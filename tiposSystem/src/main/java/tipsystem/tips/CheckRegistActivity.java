package tipsystem.tips;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TextView.OnEditorActionListener;
import android.view.inputmethod.EditorInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;
import tipsystem.utils.UserPublicUtils;

public class CheckRegistActivity extends Activity implements OnItemSelectedListener, OnDateSetListener, AdapterView.OnItemClickListener {

    //검색 결과 돌려 받기
    private static final int CUSTOMER_MANAGER_REQUEST = 3;

    UserPublicUtils upu;

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

    /* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
    int m_KeypadType = 0;

    Integer m_selectedListIndex = -1;
    public Boolean m_BarSearchLock = false;

    //목록
    ListView m_listCheckDetail;
    SimpleAdapter m_adapter;
    TextView m_ListViewRow;

    //거래처 검색 버튼
    Button bottnSearch;
    TextView m_tvOffice_Code;
    TextView m_tvOffice_Name;
    TextView m_tvIn_Date;
    TextView m_tvIn_Num;
    TextView m_tvChk_Num;
    TextView m_tvI_Pri;
    TextView m_tvI_RePri;
    TextView m_tvIn_Pri;
    TextView m_tvChk_Count;

    EditText m_etBarcode;
    String m_textBarcode = "";
    TextView m_tvG_Name;
    TextView m_tvPur_Pri;
    TextView m_tvSell_Pri;

    TextView m_tvGood_Pur_Pri;
    TextView m_tvGood_Sell_Pri;

    TextView m_tvIn_Count;
    EditText m_etChk_Count;

    CheckBox m_checkboxAddTotal;

    //신규 매입 등록 버튼
    Button buttonNewRegister;

    //리스트목록 필요한 것들
    SimpleAdapter adapter;

    //List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();
    List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

    ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_regist);

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

            String OFFICE_CODE = m_shop.getString("OFFICE_CODE");

            /* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
            m_KeypadType = LocalStorage.getInt(this, "KeypadType:" + OFFICE_CODE);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //목록 할당 하기
        m_listCheckDetail= (ListView)findViewById(R.id.listviewCheckRegistDetail);
        m_listCheckDetail.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Integer row = position;
                m_ListViewRow.setText(row.toString());
                m_selectedListIndex = position;

                fillCustomerFormFromList(position);

                m_etChk_Count.setEnabled(true);
                m_etChk_Count.requestFocus(); // 바코드로 포커스이동

                //----------------------------------------//
                //2022.06.23. 숫자키패드 마이너스 부호 추가
                //----------------------------------------//
                //m_etChk_Count.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                m_etChk_Count.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
                //----------------------------------------//

                m_etChk_Count.selectAll();

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                // 키보드를 띄운다.
                imm.showSoftInput(getCurrentFocus(), 0);

                try{
                    for (int ctr=0;ctr<=m_listCheckDetail.getChildCount();ctr++){
                        if(position==ctr){
                            m_listCheckDetail.getChildAt(ctr).setBackgroundColor(Color.GRAY);
                        }else{
                            //m_listCheckDetail.getChildAt(ctr).setBackgroundColor(Color.BLACK);
                            m_listCheckDetail.getChildAt(ctr).setBackgroundColor(Color.WHITE);
                        }
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        //리스트뷰 목록 생성 어댑터
        String[] from = new String[] { "순번", "바코드", "상품명", "매입가", "판매가", "상품매입가", "상품판매가", "매입", "검수", "차액", "Gubun" , "구분", "Writer" };
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8, R.id.item9, R.id.item10, R.id.item11 , R.id.item12, R.id.item13 };

        // fill in the grid_item layout
        m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_check_regist, from, to);
        m_listCheckDetail.setAdapter(m_adapter);

        m_ListViewRow = (TextView) findViewById(R.id.textViewListViewRow);

        String Office_Code = getIntent().getStringExtra("Office_Code");
        String Office_Name = getIntent().getStringExtra("Office_Name");
        String In_Date = getIntent().getStringExtra("In_Date");
        String I_Pri = StringFormat.convertToNumberFormat(getIntent().getStringExtra("I_Pri"));
        String I_RePri = StringFormat.convertToNumberFormat(getIntent().getStringExtra("I_RePri"));
        String In_Num = getIntent().getStringExtra("In_Num");
        String Chk_Num = getIntent().getStringExtra("Chk_Num");
        String In_Pri = StringFormat.convertToNumberFormat(getIntent().getStringExtra("In_Pri"));

        m_tvOffice_Code = (TextView) findViewById(R.id.textViewOfficeCode);
        m_tvOffice_Name = (TextView) findViewById(R.id.textViewOfficeName);
        m_tvIn_Date = (TextView) findViewById(R.id.textViewInDate);
        m_tvIn_Num = (TextView) findViewById(R.id.textViewInNum);
        m_tvChk_Num = (TextView) findViewById(R.id.textViewChkNum);
        m_tvI_Pri = (TextView) findViewById(R.id.textViewIPri);
        m_tvI_RePri = (TextView) findViewById(R.id.textViewIRepri);
        m_tvIn_Pri = (TextView) findViewById(R.id.textViewInPri);
        m_tvChk_Count = (TextView) findViewById(R.id.textViewChkQty);

        m_etBarcode = (EditText) findViewById(R.id.editTextBarcode);

        // 김영목 2020.09.14. 키패드입력 옵션 설정에 따라 적용 추가
        if (m_KeypadType == 0) { // 숫자키패드
            m_etBarcode.setInputType(2);
        }
        if (m_KeypadType == 1) { // 문자키패드
            m_etBarcode.setInputType(145); //textVisiblePassword ) 0x00000091
        }

        m_tvG_Name = (TextView) findViewById(R.id.textViewGName);
        m_tvPur_Pri = (TextView) findViewById(R.id.textViewPurPri);
        m_tvSell_Pri = (TextView) findViewById(R.id.textViewSellPri);
        m_tvGood_Pur_Pri = (TextView) findViewById(R.id.textViewGoodPurPri);
        m_tvGood_Sell_Pri = (TextView) findViewById(R.id.textViewGoodSellPri);
        m_tvIn_Count = (TextView) findViewById(R.id.textViewInCount);
        m_etChk_Count = (EditText) findViewById(R.id.editTextChkCount);

        m_checkboxAddTotal = (CheckBox)findViewById(R.id.checkbox_add_total);

        m_tvOffice_Code.setText(Office_Code);
        m_tvOffice_Name.setText(Office_Name);
        m_tvIn_Date.setText(In_Date);
        m_tvIn_Num.setText(In_Num);
        m_tvChk_Num.setText(Chk_Num);
        m_tvI_Pri.setText(I_Pri);
        m_tvI_RePri.setText(I_RePri);
        m_tvIn_Pri.setText(In_Pri);

        // 새로입력 누르기
        Button btn_Renew = (Button) findViewById(R.id.buttonRenew);
        Button btn_Delete = (Button) findViewById(R.id.buttonDelete);
        Button btn_DeleteAll = (Button)findViewById(R.id.buttonDeleteAll);
        Button btn_SendAll = (Button)findViewById(R.id.buttonSendAll);
        Button btn_Search = (Button)findViewById(R.id.buttonBarcode);
        Button btn_Regist = (Button)findViewById(R.id.buttonRegist);
        Button btn_CheckAll = (Button)findViewById(R.id.buttonInCheckAll);

        btn_CheckAll.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                //현재 단일 상품등록중이면 단일만. 아니면 전체
                if (m_selectedListIndex > 0) {

                    checkUnCheckedRow();

                    //--------------------------------//
                    // 2. DB에 저장하자
                    //--------------------------------//
                    sendSelectedData();
                    clearInputBox();

                }else{
                    // 전체 매입검수
                    //----------------------------------------//
                    // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
                    //----------------------------------------//
                    //AlertDialog.Builder dialog = new AlertDialog.Builder(CheckRegistActivity.this)
                    AlertDialog.Builder dialog = new AlertDialog.Builder(CheckRegistActivity.this,AlertDialog.THEME_HOLO_LIGHT)
                            .setTitle("전체매입검수")
                            .setMessage("전부 매입수량으로 검수 할까요?");
                    dialog.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            checkUnCheckedAll();
                            clearInputBox();
                        }
                    });
                    dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                        }
                    });
                    dialog.show();
                }
            }
        });

        btn_Search.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                String barcode = m_etBarcode.getText().toString();
                if (!barcode.equals("")) {
                    search(barcode);
                }
            }
        });

        btn_Regist.setOnClickListener(new OnClickListener() {
            //단일품목 저장
            public void onClick(View v) {
                ChkCount_Reg();
            }
        });

        btn_Renew.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                clearInputBox();
            }
        });

        //단품 초기화
        btn_Delete.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                deleteList();
                //--------------------------------//
                // 2. DB에 저장하자
                //--------------------------------//
                sendSelectedData();
                //clearInputBox();
            }
        });

        //전체 초기화
        btn_DeleteAll.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                // 전체 삭제 기능으로 대체
                //----------------------------------------//
                // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
                //----------------------------------------//
                //AlertDialog.Builder dialog = new AlertDialog.Builder(CheckRegistActivity.this)
                AlertDialog.Builder dialog = new AlertDialog.Builder(CheckRegistActivity.this,AlertDialog.THEME_HOLO_LIGHT)
                        .setTitle("검수자료삭제")
                        .setMessage("검수자료를 전부 삭제 할까요?");
                dialog.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        deleteListAll();
                        clearInputBox();
                    }
                });
                dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                dialog.show();
            }

        });

        btn_SendAll.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                // 전체 전송
                //----------------------------------------//
                // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
                //----------------------------------------//
                //AlertDialog.Builder dialog = new AlertDialog.Builder(CheckRegistActivity.this)
                AlertDialog.Builder dialog = new AlertDialog.Builder(CheckRegistActivity.this,AlertDialog.THEME_HOLO_LIGHT)
                        .setTitle("검수자료전송")
                        .setMessage("검수자료를 전부 전송합니다?");
                dialog.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        sendSelectedAllData();
                    }
                });
                dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                });
                dialog.show();
            }
        });

        // 바코드 입력 후 포커스 딴 곳을 옮길 경우
        m_etBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            // @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String barcode = m_etBarcode.getText().toString();
                    if (!barcode.equals("")) {
                        if (m_BarSearchLock==false){
                            search(barcode);
                        }
                    }

                }else{
                    //m_etBarcode.setInputType(0);
                }
            }
        });

        m_etChk_Count.setImeOptions(EditorInfo.IME_ACTION_DONE);

        //검수입력수량 키보드 완료버튼 입력 시 자동저장 기능 리스너
        m_etChk_Count.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // TODO Auto-generated method stub
                //완료 버튼을 눌렀습니다.
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    ChkCount_Reg();
                }
                return false;
            }
        });
        m_etChk_Count.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.showSoftInput(getCurrentFocus(), 0);

                }else{
                    //upu.setChangeBackGroundColor(0, m_etChk_Count);
                }
            }
        });
    }

    public void ChkCount_Reg() {

        setDataIntoList();
        clearInputBox();

    }

    //검수수량 입력시 기존 수량에 누적으로 한다.
    public void setDataIntoList() {

        if (m_selectedListIndex < 0){
            return;
        }

        String numNewChkCount = m_etChk_Count.getText().toString();

        if (numNewChkCount.equals("")) {
            Toast.makeText(CheckRegistActivity.this, "검수 수량을 입력해 주세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        //--------------------------------//
        // 1. ListView 에 뿌려줌
        //--------------------------------//
        HashMap<String, String> map = new HashMap<String, String>();
        map.putAll(mfillMaps.get(m_selectedListIndex));

        String numInCount = map.get("매입").toString();
        String numPreviousChkCount = map.get("검수").toString();  //이전 검수 수량


        Integer numTotalChkCount = Integer.parseInt(numPreviousChkCount) + Integer.parseInt(numNewChkCount);

        //----------------------------------------//
        // 2022.06.23.누게 옵션 처리 추가
        //----------------------------------------//
        if (m_checkboxAddTotal.isChecked())
            numTotalChkCount = Integer.parseInt(numPreviousChkCount) + Integer.parseInt(numNewChkCount);
        else
            numTotalChkCount = Integer.parseInt(numNewChkCount);
        //----------------------------------------//

        Integer numGapCount = Integer.parseInt(numPreviousChkCount) + Integer.parseInt(numNewChkCount) - Integer.parseInt(numInCount);
        //----------------------------------------//
        // 2022.06.23.누게 옵션 처리 추가
        //----------------------------------------//
        if (m_checkboxAddTotal.isChecked())
            numGapCount = Integer.parseInt(numPreviousChkCount) + Integer.parseInt(numNewChkCount) - Integer.parseInt(numInCount);
        else
            numGapCount = Integer.parseInt(numNewChkCount) - Integer.parseInt(numInCount);
        //----------------------------------------//

        //최종 검수 처리
        map.put("검수", numTotalChkCount.toString());

        //차액 처리
        map.put("차액", numGapCount.toString());

        //구분 처리
        map.put("Gubun", "1");
        map.put("Writer", "PDA");
        map.put("구분", "검수");

        mfillMaps.set(m_selectedListIndex,map);
        //adapter.notifyDataSetChanged();

        //--------------------------------//
        // 2. DB에 저장하자
        //--------------------------------//

        //--------------------------------//
        //2022.06.23. 저장 전에 검수TOTAL 값을 임시로 검수수량에 넣는다
        //--------------------------------//
        m_etChk_Count.setText(numTotalChkCount.toString());
        //--------------------------------//

        sendSelectedData();
        //sendSelectedData(numTotalChkCount);

    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onItemClick(AdapterView<?> parg0, View arg1, int arg2, long arg3) {

    }
    @Override
    public void onResume(){

        doQueryGetInList();
        updateStartPurchaseChkT();
        super.onResume();

    }

    //매입수량 불러오기
    public void doQueryGetInCount() {

        Intent intent = getIntent();
        String query ="";

        String in_num = intent.getExtras().getString("In_Num");
        String tablename = in_num.substring(0, 6);

        query  = " Select  ";
        query += " IsNull(SUM(In_Count),0) AS IN_COUNT FROM inD_" + tablename + " ";
        query += " WHERE In_Num = '" + in_num + "' ";

        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                try {
                    HashMap<String, String> a = JsonHelper.toStringHashMap(results.getJSONObject(0));
                    String in_count = a.get("IN_COUNT");
                    m_tvChk_Count.setText(in_count + " / 0");

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);

    }

    //매입자료 불러오기
    public void doQueryGetInList() {

        Intent intent = getIntent();

        String chk_state = intent.getExtras().getString("Chk_State");
        String chk_num = intent.getExtras().getString("Chk_Num");
        m_tvChk_Num.setText(chk_num);

        //신규시 검수자료 지우고 새로 만든다
        if(chk_state.equals("미검수")) {
            makeFirstData();
        }else{
            //m_Chk_Num = chk_num;
            viewListData();
        }
    }

    private void updateListView(JSONArray results){

        //초기화
        mfillMaps.removeAll(mfillMaps);
        //adapter.notifyDataSetChanged();

        for (int i = 0; i < results.length(); i++) {

            try {
                JSONObject obj = results.getJSONObject(i);
                HashMap<String, String> map = new HashMap<String, String>();

                map.put("순번", obj.getString("Seq"));
                map.put("바코드", obj.getString("Barcode"));
                map.put("상품명", obj.getString("G_Name"));

                map.put("매입가", obj.getString("IN_PUR_PRI"));
                map.put("판매가", obj.getString("IN_SELL_PRI"));
                map.put("상품매입가", obj.getString("GOOD_PUR_PRI"));
                map.put("상품판매가", obj.getString("GOOD_SELL_PRI"));

                map.put("매입", obj.getString("In_Count"));
                //map.put("검수", "0");
                map.put("검수", obj.getString("Chk_Count"));

                map.put("Gubun", obj.getString("Gubun"));
                map.put("Writer", obj.getString("Writer"));
                map.put("구분",  obj.getString("Chk_Gubun"));

                //map.put("차액", "0");
                String  in_count = obj.getString("In_Count");
                String  chk_count = obj.getString("Chk_Count");
                Integer num_in_count = Integer.parseInt(in_count);
                Integer num_chk_count = Integer.parseInt(chk_count);
                Integer num_check_count = num_chk_count -  num_in_count;
                String chk_count_tot = "";
                chk_count_tot = num_check_count.toString();

                map.put("차액", chk_count_tot);

                map.put("매입가", StringFormat.convertToNumberFormat(map.get("매입가")));
                map.put("판매가", StringFormat.convertToNumberFormat(map.get("판매가")));
                map.put("상품매입가", StringFormat.convertToNumberFormat(map.get("상품매입가")));
                map.put("상품판매가", StringFormat.convertToNumberFormat(map.get("상품판매가")));

                mfillMaps.add(map);

                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                // 키보드를 띄운다.
                //imm.showSoftInput(getCurrentFocus(), 0);
                // 키보드를 없앤다.
                imm.hideSoftInputFromWindow(m_etBarcode.getWindowToken(),0);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        m_adapter.notifyDataSetChanged();

    }

    // 상품 검색 하기 SQL QUERY 실행
    public void doQueryWithBarcode() {

        String query = "";

        // 상품명 원레 이름 등록
        m_textBarcode = "";
        String barcode = m_etBarcode.getText().toString();
        query = "SELECT * FROM Goods WHERE Barcode = '" + barcode + "';";

        if (barcode.equals(""))
            return;

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {
            @Override
            public void onRequestCompleted(JSONArray results) {
                updateViewGoodList(results);
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    private void updateViewGoodList(JSONArray results){
        try {
            if (results.length() > 0) { // 검색결과 확인
                m_tvG_Name.setText(results.getJSONObject(0).getString("G_Name"));
                m_tvPur_Pri.setText(results.getJSONObject(0).getString("Pur_Pri"));
                m_tvSell_Pri.setText(results.getJSONObject(0).getString("Sell_Pri"));
                //m_tvIn_Count.setText(results.getJSONObject(0).getString("In_Count"));
            } else {
                //Toast.makeText(getApplicationContext(), "조회 결과가 없습니다.", LENGTH_SHORT);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
    // private methods
    private void fillCustomerFormFromList(int position) {

        String code = mfillMaps.get(position).get("바코드");
        String name = mfillMaps.get(position).get("상품명");
        String pur_pri = mfillMaps.get(position).get("매입가");
        String sell_pri = mfillMaps.get(position).get("판매가");
        String good_pur_pri = mfillMaps.get(position).get("상품매입가");
        String good_sell_pri = mfillMaps.get(position).get("상품판매가");

        String in_count = mfillMaps.get(position).get("매입");
        String chk_count = mfillMaps.get(position).get("검수");

        m_etBarcode.setText(code);
        m_tvG_Name.setText(name);
        m_tvPur_Pri.setText(pur_pri);
        m_tvSell_Pri.setText(sell_pri);
        m_tvGood_Pur_Pri.setText(good_pur_pri);
        m_tvGood_Sell_Pri.setText(good_sell_pri);

        m_tvIn_Count.setText(in_count);
        m_etChk_Count.setText(chk_count);

        int pos = position;

    }

    // 검색을 수행하는 메소드
    public void search(String charText) {

        Boolean findItem = false;

        m_selectedListIndex = -1;
        m_ListViewRow.setText("-1");

        // 리스트의 모든 데이터를 검색한다.
        for(int i = 0;i < mfillMaps.size(); i++)
        {

            // arraylist의 모든 데이터에 입력받은 단어(charText)가 포함되어 있으면 true를 반환한다.
            if (mfillMaps.get(i).get("바코드").contains(charText))
            {
                fillCustomerFormFromList(i);
                m_selectedListIndex = i;
                findItem = true;
                m_listCheckDetail.setSelection(i);

                m_etChk_Count.setEnabled(true);
                m_etChk_Count.requestFocus(); // 바코드로 포커스이동

                m_etChk_Count.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
                m_etChk_Count.selectAll();
            }
        }

        //매입장에 없으므로 상품장에서 찾는다
        if (findItem == false){

            // 상품장에서 찾는다
            //----------------------------------------//
            // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
            //----------------------------------------//
            //AlertDialog.Builder dialog = new AlertDialog.Builder(CheckRegistActivity.this)
            AlertDialog.Builder dialog = new AlertDialog.Builder(CheckRegistActivity.this,AlertDialog.THEME_HOLO_LIGHT)
                    .setTitle("상품장검색")
                    .setMessage("상품장에서 검색 할까요?");
            dialog.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    searchGoodData();
                }
            });
            dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            });
            dialog.show();
        }
    }

    public void clearInputBox() {

        m_selectedListIndex = -1;
        m_ListViewRow.setText("-1");

        m_etBarcode.setText(""); // 바코드
        m_tvG_Name.setText(""); // 상품명

        m_tvPur_Pri.setText(""); // 매입가
        m_tvSell_Pri.setText(""); // 판매가
        m_tvGood_Pur_Pri.setText(""); // 상품매입가
        m_tvGood_Sell_Pri.setText(""); // 상품판매가
        m_tvIn_Count.setText(""); // 매입수량
        m_etChk_Count.setText(""); // 검수수량

        m_etBarcode.setEnabled(true);
        m_etBarcode.setSelected(true);
        m_etBarcode.setFocusable(true);
        m_etBarcode.requestFocus(); // 바코드로 포커스이동

        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        // 키보드를 띄운다.
        //imm.showSoftInput(getCurrentFocus(), 0);
        // 키보드를 없앤다.
        imm.hideSoftInputFromWindow(m_etBarcode.getWindowToken(),0);

    }

    // MSSQL
    // StT, StD 한품목 저장
    public void sendSelectedData(){
//int _total_count
        HashMap<String, String> check = new HashMap<String, String>();
        check.putAll(mfillMaps.get(m_selectedListIndex));
        String gubun = check.get("Gubun").toString();

        Integer int_seq = m_selectedListIndex + 1;
        String in_num = m_tvIn_Num.getText().toString();
        String chk_num = m_tvChk_Num.getText().toString();
        String barcode = m_etBarcode.getText().toString();
        String in_count = m_tvIn_Count.getText().toString();
        //String total_count = String.format("%d",_total_count);
        String chk_count = m_etChk_Count.getText().toString();

        // 쿼리 작성하기
        String query =  "";
        query +=  " IF EXISTS( ";
        query +=  "  SELECT * FROM Purchase_Chk_D ";
        query +=  "  WHERE Chk_Num = '" + chk_num + "' ";
        query +=  "    AND Barcode = '" + barcode + "' ";
        query +=  " ) ";
        query +=  " BEGIN ";
        query +=  "  UPDATE Purchase_Chk_D SET ";
        query +=  "  Gubun = '" + gubun + "', ";
        query += "  Writer = '" + "PDA" + "',  "; //Writer

        query +=  "  Chk_Count = " + chk_count + " ";
        //query +=  "  Chk_Count = " + total_count + " ";

        query +=  "  WHERE Chk_Num = '" + chk_num + "' ";
        query +=  "    AND Barcode = '" + barcode + "' ";
        query +=  " END ELSE ";
        query +=  " BEGIN ";

        query +=  " INSERT INTO Purchase_Chk_D (Chk_Num, Barcode, Chk_Count, Gubun, In_Count, IN_Num, Page, Seq, Writer ) ";
        query += " VALUES (";
        query += "'" + chk_num + "', ";
        query += "'" + barcode + "', ";

        query += "'" + chk_count + "', ";
        //query += "'" + total_count + "', ";

        if (gubun == "0"){
            query +=  " '" + "1" + "', ";
        }else{
            query +=  " '" + gubun + "', ";
        }
        query += "'" + in_count + "', ";
        query += "'" + in_num + "', "; //매입전표번호
        query += "'" + "1" + "', "; // Page
        query += " " + int_seq.toString() + " , "; // Seq
        query += "'" + "PDA" + "');"; //Writer
        query +=  " END;";

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

                //Toast.makeText(CheckRegistActivity.this, "단일품목 전송완료.", Toast.LENGTH_SHORT).show();
                //finish();
            }

            //@Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(),
                        "단일품목 등록 실패",
                        Toast.LENGTH_SHORT).show();
            }

        }).execute(m_ip+":"+m_port,  m_uudb, m_uuid, m_uupw, query);
    }

    public void sendSelectedAllData(){

        checkUnCheckedData();

        JSONObject userProfile = LocalStorage.getJSONObject(this, "userProfile");
        String userid ="";
        try {
            userid = userProfile.getString("User_ID");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currentDate = sdf.format(new Date());

        String in_num = m_tvIn_Num.getText().toString();
        String chk_num = m_tvChk_Num.getText().toString();
        String tablename = in_num.substring(0, 6);

        int year = Integer.parseInt(currentDate.substring(0, 4));
        int month = Integer.parseInt(currentDate.substring(5, 7));

        // 쿼리 작성하기
        String query =  "";
        for ( int i = 0, seq=1; i < mfillMaps.size(); i++, seq++ ) {

            HashMap<String, String> check = mfillMaps.get(i);

            query +=  " UPDATE Purchase_Chk_D SET ";
            query +=  " Chk_Count = " + check.get("검수").toString() + " , ";
            query +=  " Gubun = '" + check.get("Gubun").toString() + "', ";

            //----------------------------------------//
            //무조선 하면 안된다
            //----------------------------------------//
            //query +=  " Writer = '" + "PDA" + "' ";
            query +=  " Writer = '" + check.get("Writer").toString() + "'  ";
            //----------------------------------------//

            query +=  " WHERE Chk_Num = '" + chk_num + "' ";
            query +=  "   AND Seq = " + check.get("순번").toString() + "; ";

        }
        query += " UPDATE Purchase_Chk_T SET ";
        query += " Edit_Date = '" + currentDate + "', ";
        query += " Editer = '" + userid + "', ";
        query += " Bigo = '" + "수정" + "', ";
        query += " State = '" + "2" + "' "; //0:미검수, 1:작업중, 2:검수완료
        query += " WHERE Chk_Num = '" + chk_num + "' ; ";

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

                //Toast.makeText(CheckRegistActivity.this, "전송완료.", Toast.LENGTH_SHORT).show();
                finish();
            }

            //@Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(),
                        "등록 실패",
                        Toast.LENGTH_SHORT).show();
            }

        }).execute(m_ip+":"+m_port,  m_uudb, m_uuid, m_uupw, query);
    }

    // MSSQL
    // StT,
    public void deleteList(){

        if (!(m_selectedListIndex < 0)){

            HashMap<String, String> map = mfillMaps.get(m_selectedListIndex);

            map.put("검수", "0");
            map.put("차액", "0");
            map.put("Gubun", "0");
            map.put("Writer", "");
            map.put("구분", "미검");

            mfillMaps.set(m_selectedListIndex, map);
            m_adapter.notifyDataSetChanged();
        }
    }

    // StT, StD 테이블 삭제
    public void deleteListAll(){

        makeFirstData();
        viewListData();

    }

    public void makeFirstData(){

        //새로운 전표생성(한번만 한다)
        deleteChkJunPyo();

    }

    public void makeFirstData2(){

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currentDate = sdf.format(new Date());

        JSONObject userProfile = LocalStorage.getJSONObject(this, "userProfile");
        String userid ="";
        try {
            userid = userProfile.getString("User_ID");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        String in_num = intent.getExtras().getString("In_Num");

        //String in_num = m_tvIn_Num.getText().toString();
        String chk_num = m_tvChk_Num.getText().toString();
        String tablename = in_num.substring(0, 6);

        int t_seq = Integer.parseInt(chk_num.substring(9, 12));

        // 쿼리 작성하기
        String query =  "";

        //검수상세자료 일괄등록
        query +=  " INSERT INTO Purchase_Chk_D ";
        query +=  " SELECT ";
        query +=  " '" + chk_num + "', ";
        query +=  " Barcode, ";
        query +=  " 0, ";
        query +=  " '0', ";
        query +=  " In_Count, ";
        query +=  " In_num, ";
        query +=  " '', ";
        query +=  " In_Seq, ";
        query +=  " '' ";   //최초시는 공란
        query +=  " FROM inD_" + tablename + " ";
        query +=  " WHERE IN_Num = '" + in_num + "' ";
        query +=  " ORDER BY In_Seq;";

        //검수집계자료 등록
        query +=  " INSERT INTO Purchase_Chk_T (Chk_Num, Write_Date, Edit_Date, Writer, Editer, Bigo, In_Num, State, Seq, Chk_YN ) "
                + " VALUES ("
                + "'" + chk_num + "', "
                + "'" + currentDate + "', "
                + "'" + currentDate + "', "
                + "'" + userid + "', "
                + "'" + userid + "', "
                + "'" + "PDA에서 검수완료" + "', " //비고
                + "'" + in_num + "', " //매입전표번호
                + "'" + "3" + "', " //0:미검수, 1:작업중, 2:검수완료 3:작업진행중
                + "'" + t_seq + "', " // 전표번호순번(chk_num의 끝자리로 정의)
                + "'" + "0" + "');"; //매입원장확인여부

        //매입자료 집계에 검수전표번호 업데이트
        query +=  " UPDATE InT_" + tablename + " SET ";
        query +=  " Chk_Num = '" + chk_num + "' ";
        query +=  " WHERE IN_Num = '" + in_num + "' ";

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
                viewListData();
            }

            //@Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
            }

        }).execute(m_ip+":"+m_port,  m_uudb, m_uuid, m_uupw, query);
    }

    public void viewListData(){

        Intent intent = getIntent();

        String in_num = intent.getExtras().getString("In_Num");
        String tablename = in_num.substring(0, 6);

        String query ="";
        query += " select ";
        query += " A.Seq, "; /*순번*/
        query += " IsNull(C.Pur_Pri,0) AS IN_PUR_PRI, "; /*매입가*/
        query += " IsNull(C.Sell_Pri, 0) AS IN_SELL_PRI, "; /*판매가*/
        query += " IsNull(C.In_Count,0) AS In_Count, "; /*매입수량*/
        query += " IsNull(A.Writer,'') AS Writer, "; /*Wrier*/
        query += " A.Chk_Count, "; /*검수수량*/
        query += " A.Gubun AS Gubun, "; /*검수구분*/
        query += " CASE WHEN ISNULL(A.Gubun,'')='0' THEN '미검' WHEN ISNULL(A.Gubun,'')='1' THEN '검수' WHEN ISNULL(A.Gubun,'')='2' THEN '미입' WHEN ISNULL(A.Gubun,'') = '' THEN '미검' ELSE ISNULL(A.Gubun,'') END AS Chk_Gubun, ";
        query += " A.Barcode, "; /*바코드*/
        query += " B.G_Name, "; /*상품명*/
        query += " IsNull(B.Pur_Pri,0) AS GOOD_PUR_PRI, "; /*상품장매입가*/
        query += " IsNull(B.Sell_Pri,0) AS GOOD_SELL_PRI "; /*상품장판매가*/
        query += " FROM Purchase_Chk_D AS A  LEFT OUTER JOIN Goods AS B ON A.Barcode=B.Barcode ";
        query += " FULL OUTER JOIN inD_" + tablename + " AS C ON A.IN_NUM = C.IN_NUM AND A.SEQ = C.IN_SEQ AND A.BARCODE = C.BARCODE ";
        query += " WHERE A.IN_Num = '" + in_num + "' ";
        query += " ORDER BY A.Seq ";

        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                if (results.length() > 0) {
                    //Toast.makeText(getApplicationContext(), "조회 완료", Toast.LENGTH_SHORT).show();
                    updateListView(results);
                }
                else {
                    //Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
    }

    //검수전표번호 삭제
    public void deleteChkJunPyo () {

        Intent intent = getIntent();
        String in_num = intent.getExtras().getString("In_Num");

        String query =  "";
        if (!in_num.equals("")) {
            query +=  " DELETE Purchase_Chk_T WHERE In_Num = '" + in_num + "';";
            query +=  " DELETE Purchase_Chk_D WHERE In_Num = '" + in_num + "';";

            new MSSQL(new MSSQL.MSSQLCallbackInterface() {

                @Override
                public void onRequestCompleted(JSONArray results) {
                    makeChkJunPyo();
                }
            }).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
        }
    }

    //검수전표번호 새로 만든다(그 달의 일련번호)
    public void makeChkJunPyo () {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());

        String query ="";
        query  = " Select  ";
        query += " IsNull(MAX(Seq),0)+1 AS MAX_SEQ FROM Purchase_Chk_T ";
        query += " WHERE Write_Date Like '%" + currentDate + "%' ";

        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                try {
                    HashMap<String, String> a = JsonHelper.toStringHashMap(results.getJSONObject(0));
                    //max_seq[0] = a.get("MAX_SEQ");
                    Integer chk_seq = Integer.parseInt(a.get("MAX_SEQ"));

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    String currentDate = sdf.format(new Date());

                    int year = Integer.parseInt(currentDate.substring(0, 4));
                    int month = Integer.parseInt(currentDate.substring(5, 7));
                    int day = Integer.parseInt(currentDate.substring(8, 10));

                    String chk_num = String.format("%04d%02d%02d", year, month, day) + "#" + String.format("%03d", chk_seq);
                    m_tvChk_Num.setText(chk_num);
                    makeFirstData2();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
    }

    public void checkUnCheckedData(){

        for ( int i = 0, seq=1; i < mfillMaps.size(); i++, seq++ ) {

            HashMap<String, String> check = mfillMaps.get(i);
            String in_count = check.get("매입").toString();
            String gubun = check.get("Gubun").toString();

            if (gubun.equals("0") || gubun.equals("2")){

                //검수
                check.put("검수", in_count.toString());

                //차액 처리
                check.put("차액", "0");

                //구분 처리
                check.put("Gubun", "1");
                check.put("Writer", "PDA");
                check.put("구분", "검수");

                mfillMaps.set(i, check);
/*(


                m_selectedListIndex = i;

                // 미검수 확인
                AlertDialog.Builder dialog = new AlertDialog.Builder(CheckRegistActivity.this)
                        .setTitle("미검수확인")
                        .setMessage("미검수자료를 매입으로 검수 할까요?");
                dialog.setPositiveButton("선택", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                        HashMap<String, String> check = mfillMaps.get(m_selectedListIndex);

                        String in_count = check.get("매입").toString();
                        String gubun = check.get("Gubun").toString();

                        //검수
                        check.put("검수", in_count.toString());

                        //차액 처리
                        check.put("차액", "0");

                        //구분 처리
                        check.put("Gubun", "1");
                        check.put("구분", "검수");

                        mfillMaps.set(m_selectedListIndex, check);
                        m_adapter.notifyDataSetChanged();


                    }
                }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //HashMap<String, String> check = mfillMaps.get(m_selectedListIndex);
                    }
                });
                dialog.show();

 */
            }
        }
        m_adapter.notifyDataSetChanged();

    }

    // 단일 품목을 매입자료를 검수자료로 등록
    public void checkUnCheckedRow(){
        Integer row = m_selectedListIndex;
        HashMap<String, String> check = mfillMaps.get(row);

        String in_count = check.get("매입").toString();

        //검수
        check.put("검수", in_count.toString());
        //차액 처리
        check.put("차액", "0");
        //구분 처리
        check.put("Gubun", "1");
        check.put("Writer", "PDA");
        check.put("구분", "검수");

        mfillMaps.set(row, check);
        m_adapter.notifyDataSetChanged();

    }
    // 전체 미검수 자료 전체를 매입자료를 검수자료로 등록
    public void checkUnCheckedAll(){

        for ( int i = 0, seq=1; i < mfillMaps.size(); i++, seq++ ) {

            HashMap<String, String> check = mfillMaps.get(i);

            String in_count = check.get("매입").toString();
            String gubun = check.get("Gubun").toString();

            if (gubun.equals("0")){

                //검수
                check.put("검수", in_count.toString());
                //차액 처리
                check.put("차액", "0");
                //구분 처리
                check.put("Gubun", "1");
                check.put("Writer", "PDA");
                check.put("구분", "검수");

                mfillMaps.set(i, check);
            }
        }
        m_adapter.notifyDataSetChanged();
    }

    //뒤로가기 버튼처리
    @Override
    public void onBackPressed() {

        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        //new AlertDialog.Builder(this)
        new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("검수등록창 닫기")
                .setMessage("작업중인 검수자료가 있습니다. 종료할까요? 종료시 작업중으로 처리됩니다.")
                .setPositiveButton("검수완료", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sendSelectedAllData();
                    }

                })
                .setNeutralButton("종료", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        //검수작업중으로 완료
                        updatePurchaseChkT();
                        //finish();
                    };
                })
                .setNegativeButton("취소", null)
                .show();

    }
    public void updatePurchaseChkT(){

        String chk_num = m_tvChk_Num.getText().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currentDate = sdf.format(new Date());

        JSONObject userProfile = LocalStorage.getJSONObject(this, "userProfile");
        String userid ="";
        try {
            userid = userProfile.getString("User_ID");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 쿼리 작성하기
        String query =  "";

        query += " UPDATE Purchase_Chk_T SET ";

        query += " Bigo = '" + "작업중수정" + "', ";
        query += " State = '" + "1" + "', "; //0:미검수, 1:작업중, 2:검수완료, 3:작업진행중
        query += " Edit_Date = '" + currentDate + "', ";
        query += " Editer = '" + userid + "' "; //Editer

        query += " WHERE Chk_Num = '" + chk_num + "' ; ";

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

                //Toast.makeText(CheckRegistActivity.this, "전송완료.", Toast.LENGTH_SHORT).show();
                finish();
            }

            //@Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(),
                        "등록 실패",
                        Toast.LENGTH_SHORT).show();
            }

        }).execute(m_ip+":"+m_port,  m_uudb, m_uuid, m_uupw, query);
    }

    //매입장에 없는 상품이므로 검수자료에 추가 등록한다
    public void searchGoodData(){

        String barcode = m_etBarcode.getText().toString();

        String query ="";
        query += " select * ";
        query += " FROM Goods ";
        query += " WHERE Barcode = '" + barcode + "' ";

        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                if (results.length() > 0) {
                    //Toast.makeText(getApplicationContext(), "조회 완료", Toast.LENGTH_SHORT).show();

                    Integer iSeq = mfillMaps.size() + 1;

                    for (int i = 0; i < results.length(); i++) {

                        try {
                            JSONObject obj = results.getJSONObject(i);
                            HashMap<String, String> map = new HashMap<String, String>();

                            Integer max_seq = iSeq + i;

                            //차액 처리
                            map.put("순번", max_seq.toString());
                            map.put("바코드", obj.getString("BarCode"));
                            map.put("상품명", obj.getString("G_Name"));

                            map.put("매입가", obj.getString("Pur_Pri"));
                            map.put("판매가", obj.getString("Sell_Pri"));
                            map.put("상품매입가", obj.getString("Pur_Pri"));
                            map.put("상품판매가", obj.getString("Sell_Pri"));

                            map.put("매입", "0");
                            map.put("검수", "0");
                            map.put("Gubun", "2");  //미매입구분
                            map.put("Writer", "PDA");  //Writer
                            map.put("구분", "미입");  //미매입구분

                            map.put("차액", "0");

                            map.put("매입가", StringFormat.convertToNumberFormat(map.get("매입가")));
                            map.put("판매가", StringFormat.convertToNumberFormat(map.get("판매가")));
                            map.put("상품매입가", StringFormat.convertToNumberFormat(map.get("상품매입가")));
                            map.put("상품판매가", StringFormat.convertToNumberFormat(map.get("상품판매가")));

                            mfillMaps.add(map);

                            m_selectedListIndex = max_seq - 1;
                            //sendSelectedData();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    m_adapter.notifyDataSetChanged();
                    //updateListView(results);
                }
                else {
                    //Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();
                    //상품장에 신규로 등록하고 처리?
                    newGoodData();
                }
            }
        }).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
    }

    //상품장에 없는 상품이추가 등록한다
    public void newGoodData(){

        String barcode = m_etBarcode.getText().toString();

        // 전표번호 생성
        String posID = "P";
        String userID = "1";

        String query ="";

        String g_name = "신상품"; // 2.상품명
        String bus_code = m_tvOffice_Code.getText().toString(); // 3.거래처코드
        String bus_name = m_tvOffice_Name.getText().toString(); // 4.거래처명

        String lname = "매입대분류"; // 5.대명
        String mname = "매입중분류"; // 6.중명
        String sname = "매입소분류"; // 7.소명
        String lcode = "AA"; // 대코드
        String mcode = "AA"; // 중코드
        String scode = "AAA"; // 소코드

        String tax_yn = "1"; // 8.면과세
        String Std_Size = ""; // 9.규격

        String pur_pri = "0"; // 10.매입가
        String pur_cost = "0"; // 11.매입원가
        String add_tax = "0"; // 12.부가세
        String salesPrice = "0"; // 13.판매가
        String ratio = "0"; // 14.이익률

        String vat_chk = "1";// 부가세 포함, 별도 선택

        String g_grade = "None"; // 15.그룹구분
        // String good_use = String.format("%d", m_spinGoods.getSelectedItem()
        // ); //16.사용구분
        String good_type = "0"; // 17.상품구분
        String memberpoint = "1"; // 17.상품구분

        query = "exec sp_executesql N'EXEC str_PDA_007I " + "''" + barcode + "'', " + "''" + g_name + "'', " + "''"
                + tax_yn + "'', " + "''" + Std_Size + "'', " + pur_pri + ", " + pur_cost + ", " + add_tax + ", "
                + salesPrice + ", " + ratio + ", " + "''" + bus_code + "'', " + "''" + bus_name + "'', " + "10, "
                + "''g'', " + "10, " + "''" + good_type + "'', " + "''1'', " + "''1'', " + "''1'', " + "''1'', "
                + "''1'', " + "0, " + "0, " + "''" + posID + "'', " + "''IN''' ";

        // 콜백함수와 함께 실행
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                try {
                    String res = (String) results.getJSONObject(0).get("1");
                    if (!"OK".equals(res)) {
                        //Toast.makeText(CheckRegistActivity.this, res, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String str = "신상품등록 완료";
                    //Toast.makeText(CheckRegistActivity.this, res, Toast.LENGTH_SHORT).show();

                    String barcode = m_etBarcode.getText().toString();

                    Integer iSeq = mfillMaps.size();
                    HashMap<String, String> map = new HashMap<String, String>();

                    Integer max_seq = iSeq + 1;

                    //차액 처리
                    map.put("순번", max_seq.toString());
                    map.put("바코드", barcode);
                    map.put("상품명", "신상품");

                    map.put("매입가", "0");
                    map.put("판매가", "0");
                    map.put("상품매입가", "0");
                    map.put("상품판매가", "0");

                    map.put("매입", "0");
                    map.put("검수", "0");
                    map.put("Gubun", "2");  //미매입구분
                    map.put("Writer", "PDA");  //Writer
                    map.put("구분", "미입");  //미매입구분

                    map.put("차액", "0");

                    map.put("매입가", StringFormat.convertToNumberFormat(map.get("매입가")));
                    map.put("판매가", StringFormat.convertToNumberFormat(map.get("판매가")));
                    map.put("상품매입가", StringFormat.convertToNumberFormat(map.get("상품매입가")));
                    map.put("상품판매가", StringFormat.convertToNumberFormat(map.get("상품판매가")));

                    mfillMaps.add(map);

                    m_selectedListIndex = iSeq;
                    m_adapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Toast.makeText(CheckRegistActivity.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    return;
                }

            }

            @Override
            public void onRequestFailed(int code, String msg) {
                Toast.makeText(getApplicationContext(), "등록 실패", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    public void updateStartPurchaseChkT(){

        String chk_num = m_tvChk_Num.getText().toString();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String currentDate = sdf.format(new Date());

        JSONObject userProfile = LocalStorage.getJSONObject(this, "userProfile");
        String userid ="";
        try {
            userid = userProfile.getString("User_ID");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 쿼리 작성하기
        String query =  "";

        query += " UPDATE Purchase_Chk_T SET ";

        query += " Bigo = '" + "작업진행수정" + "', ";
        query += " State = '" + "3" + "', "; //0:미검수, 1:작업중, 2:검수완료, 3:작업진행중
        query += " Edit_Date = '" + currentDate + "', ";
        query += " Editer = '" + userid + "' "; //Editer

        query += " WHERE Chk_Num = '" + chk_num + "' ; ";

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                //Toast.makeText(CheckRegistActivity.this, "전송완료.", Toast.LENGTH_SHORT).show();
            }

            //@Override
            public void onRequestFailed(int code, String msg) {
                Toast.makeText(getApplicationContext(),
                        "등록 실패",
                        Toast.LENGTH_SHORT).show();
            }

        }).execute(m_ip+":"+m_port,  m_uudb, m_uuid, m_uupw, query);
    }

}