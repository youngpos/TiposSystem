package tipsystem.tips;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
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

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;

public class SaleMessageManageActivity extends Activity implements AdapterView.OnItemClickListener{

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
    ListView saleMessageManageListView;

    SimpleAdapter mAdapter;
    List<HashMap<String,String>> mFillMaps = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_message_manage);

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
        saleMessageManageListView = (ListView)findViewById(R.id.sale_message_manage_listview);

        //리스트뷰 목록 생성 어댑터
        //                             1        2       3          4        5
        String[] from = new String[] { "No", "Barcode", "Gname", "Mname", "SellPri", "WriteDate", "EditDate", "Writer", "Editor"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8, R.id.item9};

        mAdapter = new SimpleAdapter(this,mFillMaps,R.layout.activity_listview_sale_message_manage,from,to);
        saleMessageManageListView.setAdapter(mAdapter);
        saleMessageManageListView.setOnItemClickListener(this);

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
            andQry = " WHERE B.G_NAME LIKE '%" + gNameEditText.getText().toString() + "%' ";
        }

        query = " Select "
                + " A.S_IDX AS No, "
                + " ISNULL(A.S_BARCODE,'') AS Barcode, "
                + " ISNULL(B.G_NAME,'') AS Gname, "
                + " ISNULL(A.G_NAME,'') AS Mname, "
                + " FLOOR(ISNULL(A.Sell_Pri,0)) AS SellPri, "
                + " ISNULL(A.Write_Date,'') AS WriteDate, "
                + " ISNULL(A.Edit_Date,'') AS EditDate, "
                + " ISNULL(A.Writer,'') AS Writer, "
                + " ISNULL(A.Editor,'') AS Editor "
                + " FROM Sell_Msg A "
                + " LEFT JOIN GOODS B On A.S_Barcode=B.Barcode "
                + andQry
                + " Order by A.S_IDX;";

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
        }).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw,query);
    }

    // 리스트뷰에 출력
    private void displayListView(JSONArray results){
        try {
            if ( results.length() > 0 ) {
                HashMap<String, String> map = new HashMap<String, String>();
                for(int i = 0; i < results.length() ; i++) {
                    JSONObject object = results.getJSONObject(i);
                    map = JsonHelper.toStringHashMap(object);
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

        String no = tempMap.get("No");
        String barcode = tempMap.get("Barcode");
        String gName = tempMap.get("Gname");
        String mName = tempMap.get("Mname");
        String sellPri = tempMap.get("SellPri");
        String writeDate = tempMap.get("WriteDate");
        String editDate = tempMap.get("EditDate");
        String writer = tempMap.get("Writer");
        String editor = tempMap.get("Editor");

        Intent intent;
        intent = new Intent(this, SaleMessageRegistActivity.class);

        intent.putExtra("no",no);
        intent.putExtra("barcode",barcode);
        intent.putExtra("gName",gName);
        intent.putExtra("mName",mName);
        intent.putExtra("sellPri",sellPri);
        intent.putExtra("writeDate",writeDate);
        intent.putExtra("editDate",editDate);
        intent.putExtra("writer",writer);
        intent.putExtra("editor",editor);

        startActivity(intent);

    }
}