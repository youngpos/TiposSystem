package tipsystem.tips;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.M3MoblieBarcodeScanBroadcast;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;

public class ManagerProductBox extends Activity {

    //2017-04 m3mobile
    private static final String SCANNER_ACTION_SETTING_CHANGE = "com.android.server.scannerservice.settingchange";
    public static final String SCANNER_ACTION_PARAMETER = "android.intent.action.SCANNER_PARAMETER";
    public static final String SCANNER_ACTION_ENABLE = "com.android.server.scannerservice.m3onoff";
    public static final String SCANNER_EXTRA_ENABLE = "scanneronoff";

    public static final String SCANNER_ACTION_START = "android.intent.action.M3SCANNER_BUTTON_DOWN";
    public static final String SCANNER_ACTION_CANCEL = "android.intent.action.M3SCANNER_BUTTON_UP";

    public static final String SCANNER_ACTION_BARCODE = "com.android.server.scannerservice.broadcast";

    JSONArray m_results;
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

    String m_getBarcode;
    String m_getObtain;

    EditText m_boxbarcode;
    EditText m_boxpurpri;
    EditText m_boxsellpri;
    EditText m_boxgname;

    EditText m_obtain;  //입수량
    EditText m_searchBarcode;   //검색 바코드

    // loading bar
    private ProgressDialog dialog;

    ListView m_productList;
    SimpleAdapter m_adapter;
    List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

    Context mContext;
    SharedPreferences pref;
    boolean m3Mobile;
    //boolean mAllSend;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_manage_productbox_list);

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


        // 환경설정 관련 옵션 아이템 불러오기
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        m3Mobile = pref.getBoolean("m3mobile", false);
        mContext = this;

        //넘겨온 바코드를 받습니다.
        m_getBarcode = getIntent().getStringExtra("barcode");
        m_getObtain = getIntent().getStringExtra("obtain");

        if("".equals(m_getBarcode) || m_getBarcode == null){
            Toast.makeText(this, "바코드를 받지 못했습니다.", Toast.LENGTH_SHORT).show();
            returnResultData(RESULT_CANCELED);
        }

        //입수량을 입력받지 못했다면 0으로 기본 셋팅 합니다.
        if("".equals(m_getObtain) || m_getObtain == null){
            m_getObtain ="0";
        }

        //입수량 입력
        m_obtain = (EditText) findViewById(R.id.edittextobtain);
        m_obtain.setText(m_getObtain);


        //화면에서 사용되는 필드
        m_boxbarcode = (EditText) findViewById(R.id.edittextboxbarcode);
        m_boxgname = (EditText) findViewById(R.id.edittextboxgname);
        m_boxpurpri = (EditText) findViewById(R.id.edittextboxpurpri);
        m_boxsellpri = (EditText) findViewById(R.id.edittextboxsellpri);

        m_searchBarcode = (EditText) findViewById(R.id.edittextbarcode);

        m_searchBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    doListSearch(v);
                }
            }
        });

        m_productList = (ListView) findViewById(R.id.listviewProductList);
        m_productList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //입수 등록 여부
                setBoxProduct(position);
            }
        });

        String[] from = new String[] {"BarCode", "G_Name", "Std_Size", "Pur_Pri", "Sell_Pri"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5 };
        m_adapter = new SimpleAdapter(this, mfillMaps, R.layout. activity_listview_boxproduct_list, from, to);
        m_productList.setAdapter(m_adapter);

        doBoxProductSearch();


        // intent filter 2017-04 m3mobile
        if(m3Mobile){
			/*IntentFilter filter = new IntentFilter();
			filter.addAction(SCANNER_ACTION_BARCODE);
			registerReceiver(BarcodeIntentBroadcast,filter);*/

            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_searchBarcode, m_obtain);

        }

    }


    //박스상품을 등록 합니다.
    public void setBoxProduct(int pos){

        HashMap<String, String> map = mfillMaps.get(pos);
        final String barcode = map.get("BarCode");

        AlertDialog.Builder alt_bld = new AlertDialog.Builder(ManagerProductBox.this,AlertDialog.THEME_HOLO_LIGHT);
        alt_bld.setMessage("하위 상품으로 등록 하시겠습니까?")
                .setCancelable(false)
                .setPositiveButton("네", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Action for 'Yes' Button
                        setInsertBoxProduct(barcode);
                    }
                }).setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Action for 'NO' Button
                dialog.cancel();
            }
        });

        AlertDialog alert = alt_bld.create();
        // Title for AlertDialog
        alert.setTitle("단품 등록");
        alert.show();

    }

    //박스상품 입력하기
    private void setInsertBoxProduct(String barcode){

        //입수를 체크 합니다.
        int obtain = 0;
        try{
            obtain = Integer.parseInt(m_obtain.getText().toString());
        }catch(NumberFormatException e){
            e.printStackTrace();
        }

        //입수가 입력이 안되었다면 입수를 입력 합니다.
        if(obtain <= 0){
            Toast.makeText(this, "입수를 입력해 주세요!!", Toast.LENGTH_SHORT).show();
            m_obtain.requestFocus();
            return;
        }

        //쿼리를 작성합니다.
        String query = "UPDATE Goods Set barcode1='"+barcode+"',obtain="+obtain+", Box_Use='1' WHERE barcode='"+m_getBarcode+"'";

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "등록이 완료 되었습니다.", Toast.LENGTH_SHORT).show();

                //결과를 리턴 합니다.
                returnResultData();
            }

           /* @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "등록 실패", Toast.LENGTH_SHORT).show();

            }*/
        }).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);



    }


    // 조회 실행 함수
    public void doBoxProductSearch() {

        // 쿼리 작성하기
        String query = "SELECT * FROM Goods "
                + " WHERE Barcode = '"+m_getBarcode+"'";

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
                    try {
                        JSONObject json = results.getJSONObject(0);
                        HashMap<String, String> map = JsonHelper.toStringHashMap(json);

                        m_boxbarcode.setText(m_getBarcode);
                        m_boxgname.setText(map.get("G_Name"));
                        m_boxpurpri.setText(map.get("Pur_Pri"));
                        m_boxsellpri.setText(map.get("Sell_Pri"));

                    }catch(JSONException e){
                        e.printStackTrace();
                    }


                }
                else {
                    Toast.makeText(getApplicationContext(), "박스상품을 찾지 못했습니다.", Toast.LENGTH_SHORT).show();
                    returnResultData(RESULT_CANCELED);
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                m_adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();

            }
        }).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
    }


    //목록에서 재검색 시 필요한 함수
    public void doListSearch(View view){

        mfillMaps.clear();
        doSearch();

    }

    //화면에서 검색 버튼 누르면 검색 됩니다.
    private void doSearch(){

        String searchBarcode = m_searchBarcode.getText().toString();

        if("".equals(searchBarcode) || searchBarcode.length() <= 4 ){

            Toast.makeText(this, "바코드를 입력해 주세요!!", Toast.LENGTH_SHORT).show();
            m_searchBarcode.requestFocus();
            return;
        }

        if(searchBarcode.equals(m_getBarcode)){
            Toast.makeText(this, "같은 상품을 하위 상품으로 등록 할수 없습니다.!!", Toast.LENGTH_SHORT).show();
            m_searchBarcode.setText("");
            m_searchBarcode.requestFocus();
            return;
        }

        String query = "Select * From Goods Where Barcode Like '%"+searchBarcode+"%' ";

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
                    updateListView(results);
                    Toast.makeText(getApplicationContext(), "조회 완료", Toast.LENGTH_SHORT).show();
                }
                else {
                    m_adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                m_adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();

            }
        }).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);

    }


    private void updateListView(JSONArray results) {

        for (int i = 0; i < results.length(); i++) {

            try {
                JSONObject json = results.getJSONObject(i);
                HashMap<String, String> map = JsonHelper.toStringHashMap(json);
                mfillMaps.add(map);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        m_adapter.notifyDataSetChanged();
    }

    //등록취소 및 실패시
    private void returnResultData(int result) {

        Intent intent = new Intent();
        this.setResult(result, intent);
        finish();
    }

    //등록 성공 시
    private void returnResultData() {

        String obtain = m_obtain.getText().toString();

        Intent intent = new Intent();
        intent.putExtra("obtain", obtain);
        this.setResult(RESULT_OK, intent);
        finish();
    }


    //2017-04 m3mobile 추가
    @Override
    protected void onResume() {
        super.onResume();
        if(m3Mobile){
            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_searchBarcode, m_obtain);
            M3MoblieBarcodeScanBroadcast.setOnResume();
        }
    }

    //2017-04 m3mobile 추가
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(m3Mobile){
            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(mContext, m_searchBarcode, m_obtain);
            M3MoblieBarcodeScanBroadcast.setOnDestory();
        }
    }

}
