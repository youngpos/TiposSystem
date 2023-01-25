package tipsystem.tips;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
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
import java.util.regex.Pattern;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;

public class HotKeyScaleActivity extends Activity implements AdapterView.OnItemClickListener{
    private Context mContext;
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
    EditText gNameEditText;
    Button searchButton;
    Button closeButton;
    ListView hotKeyScaleListView;

    SimpleAdapter mAdapter;
    List<HashMap<String,String>> mFillMaps = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_key_scale);

        mContext = this;

        //매장정보
        m_shop = LocalStorage.getJSONObject(this,"currentShopData");
        try{
            m_ip = m_shop.getString("SHOP_IP");
            m_port = m_shop.getString("SHOP_PORT");
            //----------------------------------------//
            // 2021.12.21. 매장DB IP,PW,DB 추가
            //----------------------------------------//
            m_uuid = m_shop.getString("shop_uuid");
            m_uupw = m_shop.getString("shop_uupass");
            m_uudb = m_shop.getString("shop_uudb");
            //----------------------------------------//

       }catch (Exception e){
            e.printStackTrace();
        }

        gNameEditText = (EditText)findViewById(R.id.g_name_edittext);

        gNameEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        gNameEditText.setPrivateImeOptions("defaultInputmode=korean;");
        gNameEditText.requestFocus(); // 판매가로 포커스이동

        searchButton = (Button)findViewById(R.id.search_button);
        closeButton = (Button)findViewById(R.id.close_button);
        hotKeyScaleListView = (ListView)findViewById(R.id.hot_key_scale_listview);

        //리스트뷰 목록 생성 어댑터
        //                             1        2       3          4        5
        String[] from = new String[] { "No", "Name", "Barcode", "Gname", "SellPri"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5};

        mAdapter = new SimpleAdapter(this,mFillMaps,R.layout.activity_listview_hot_key_scale,from,to);
        hotKeyScaleListView.setAdapter(mAdapter);
        hotKeyScaleListView.setOnItemClickListener(this);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchCheckList();
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        searchCheckList();
    }


    // 조회
    private void searchCheckList(){
        Toast.makeText(getApplicationContext(),"조회시작",Toast.LENGTH_SHORT);
        mFillMaps.removeAll(mFillMaps);

        String query = "";
        String andQry = "";

        if(gNameEditText.length()>0){
            andQry = " AND B.G_NAME LIKE '%" + gNameEditText.getText().toString() + "%' ";
        }

        query = " Select "
                + " '' AS No, "
                + " ISNULL(C.Name,'') AS Name, "
                + " ISNULL(A.BARCODE,'') AS Barcode, "
                + " ISNULL(B.G_NAME,'') AS Gname, "
                + " FLOOR(ISNULL(B.Sell_Pri,0)) AS SellPri "
                + " FROM Group_ScaleManage A "
                + " LEFT JOIN GOODS B On A.Barcode=B.Barcode "
                + " LEFT JOIN Group_Scale C On A.GroupID=C.ID "
                + " WHERE B.Scale_Use = '1' "
                + "   AND C.app_view_yn = '1' "
                + andQry
                + " Order by A.Index_Num;";

        // 쿼리문 작성후 문자는 지운다
        gNameEditText.setText("");

        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {
            @Override
            public void onRequestCompleted(JSONArray results) {
                displayListView(results);
            }

            @Override
            public void onRequestFailed(int code, String msg) {

            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    // 리스트뷰에 출력
    private void displayListView(JSONArray results){
        try {
            if ( results.length() > 0 ) {
                HashMap<String, String> map = new HashMap<String, String>();
                for(int i = 0; i < results.length() ; i++) {
                    JSONObject object = results.getJSONObject(i);
                    map = JsonHelper.toStringHashMap(object);
                    String tmp = String.format("%d",i+1);
                    map.put("No",tmp);
                    map.put("SellPri", StringFormat.convertTNumberFormat(map.get("SellPri")));

                    mFillMaps.add(map);
                }
            }else{
                Toast.makeText(getApplicationContext(), "조회 결과가 없습니다.", Toast.LENGTH_SHORT);
            }
            mAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        Integer row = i;

        HashMap<String, String> tempMap = new HashMap<String, String>();

        tempMap.putAll(mFillMaps.get(row));

        String barcode = tempMap.get("Barcode");
        String gName = tempMap.get("Gname");
        String unit = tempMap.get("Unit");
        String sellPri = tempMap.get("SellPri");

        Intent intent;
        intent = new Intent(this, HotKeyScaleRegistActivity.class);

        intent.putExtra("barcode",barcode);
        intent.putExtra("gName",gName);
        intent.putExtra("sellPri",sellPri);
        startActivity(intent);

    }
}