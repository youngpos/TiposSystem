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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;

public class HotKeyManageActivity extends Activity  implements AdapterView.OnItemClickListener{

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
    ListView hotKeyManageListView;

    SimpleAdapter mAdapter;
    List<HashMap<String,String>> mFillMaps = new ArrayList<HashMap<String, String>>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_key_manage);

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
        hotKeyManageListView = (ListView)findViewById(R.id.hot_key_manage_listview);

        //리스트뷰 목록 생성 어댑터
        //                              1         2        3        4          5         6       7         8          9             10          11        12      13,       14
        String[] from = new String[] { "Hindex", "Hname", "Kindex", "Barcode", "Gname", "StdSize", "Unit", "PurPri", "SellPri", "ProfitRate", "ScaleUse", "TaxYn","ScalePurPri", "ScaleSellPri"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8, R.id.item9, R.id.item10, R.id.item11, R.id.item12, R.id.item13, R.id.item14};

        mAdapter = new SimpleAdapter(this,mFillMaps,R.layout.activity_listview_hot_key_manage,from,to);
        hotKeyManageListView.setAdapter(mAdapter);
        hotKeyManageListView.setOnItemClickListener(this);

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

        // 대표키 조회
        boolean chk = false;
        if(gNameEditText.length() == 1) {
            chk = Pattern.matches("[a-zA-Z]",gNameEditText.getText());
        }

        String query = "";
        String andQry = "";

        if(gNameEditText.length()>0){
            if(chk == true){
                andQry = " AND H_INDEX = '" + gNameEditText.getText().toString() + "' ";
            }else{
                andQry = " AND A.G_NAME LIKE '%" + gNameEditText.getText().toString() + "%' ";
            }
        }

        query = " Select "
                + " ISNULL(h_Index,'') AS Hindex, "
                + " ISNULL(C.H_Name,'') AS Hname, "
                + " '' AS Kindex, "
                + " ISNULL(a.BARCODE,'') AS Barcode, "
                + " ISNULL(A.G_NAME,'') AS Gname, "
                + " ISNULL(A.Std_Size,'') AS StdSize, "
                + " ISNULL(A.unit,'') AS Unit, "
                + " ISNULL(Pur_Pri,0) AS PurPri, "
                //+ " ISNULL(Sell_Pri,0) AS SellPri, "
                + " FLOOR(ISNULL(Sell_Pri,0)) AS SellPri, "
                //+ " CAST(CAST(ISNULL(Sell_Pri,0) AS INTEGER(18)) AS FLOAT) AS SellPri, "
                + " ISNULL(CASE WHEN LEN(a.Barcode)=4 or a.Scale_Use='1' THEN 0 ELSE a.Pur_Pri END,0) AS ScalePurPri, "
                + " FLOOR(ISNULL(CASE WHEN LEN(a.Barcode)=4 or a.Scale_Use='1' THEN ISNULL(b.H_SellPri,0) ELSE a.Sell_Pri END,0)) AS ScaleSellPri, "
                + " ISNULL(Profit_Rate,0) AS ProfitRate, "
                + " ISNULL(H_Number,0) AS Hnumber, "
                + " ISNULL(A.Scale_Use,'') AS ScaleUse, "
                + " ISNULL(A.Sale_Use,'') AS SaelUse, "
                + " ISNULL(A.Tax_YN,'') AS TaxYn "
                + " FROM HOT_KEY B "
                + " INNER JOIN GOODS A  On A.Barcode=B.H_Barcode "
                + " LEFT JOIN HOT_KEY_DEFNAME C  On C.H_Code=B.H_Code "
                + " WHERE barcode <> '' "
                + "   AND C.app_view_yn = '1' "
                + andQry
                + " Order by B.H_code, B.H_Order, B.H_Number;";

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

                    /*
                    String a = map.get("SellPri");
                    double n = Double.valueOf(map.get("SellPri"));
                    String pattern = "###,###,###";
                    DecimalFormat df = new DecimalFormat(pattern);
                    String b = df.format(n);
                    */
                    map.put("PurPri", StringFormat.convertT00NumberFormat(map.get("PurPri")));
                    map.put("SellPri", StringFormat.convertTNumberFormat(map.get("SellPri")));
                    map.put("ScalePurPri", StringFormat.convertT00NumberFormat(map.get("ScalePurPri")));
                    map.put("ScaleSellPri", StringFormat.convertTNumberFormat(map.get("ScaleSellPri")));

                    //----------------------------------------//
                    // 부문 또는 전자저울 상품 구분
                    //----------------------------------------//
                    if((map.get("Barcode").length() == 4) || (map.get("ScaleUse").equals("1"))){
                        map.put("PurPri", map.get("ScalePurPri"));
                        map.put("SellPri", map.get("ScaleSellPri"));
                    }
                    //----------------------------------------//

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
        String stdSize = tempMap.get("StdSize");
        String unit = tempMap.get("Unit");
        String profitRate = tempMap.get("ProfitRate");
        String purPri = tempMap.get("PurPri");
        String sellPri = tempMap.get("SellPri");
        String scaleUse = tempMap.get("ScaleUse");
        String taxYn = tempMap.get("TaxYn");
        String scalePurPri = tempMap.get("ScalePurPri");
        String scaleSellPri = tempMap.get("ScaleSellPri");

        /*
        // 상품체크
        if(barcode.length() == 4){
            // 상품코드 4자리는 가격변경 못한다
            Toast.makeText(HotKeyManageActivity.this, "상품코드 4자리는 가격변경 못합니다.", Toast.LENGTH_SHORT);

            AlertDialog.Builder builder = new AlertDialog.Builder(HotKeyManageActivity.this);
            builder.setTitle("알림");
            builder.setMessage("변경할 수 없은 상품입니다.");
            builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
            return;
        }
        */

        Intent intent;
        intent = new Intent(this, HotKeyRegistActivity.class);

        intent.putExtra("barcode",barcode);
        intent.putExtra("gName",gName);
        intent.putExtra("stdSize",stdSize);
        intent.putExtra("unit",unit);
        intent.putExtra("profitRate",profitRate);
        intent.putExtra("purPri",purPri);
        intent.putExtra("sellPri",sellPri);
        intent.putExtra("scaleUse",scaleUse);
        intent.putExtra("taxYn",taxYn);
        intent.putExtra("scalePurPri",scalePurPri);
        intent.putExtra("scaleSellPri",scaleSellPri);
        startActivity(intent);

    }
}