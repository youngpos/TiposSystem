package tipsystem.tips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

public class ManageCustomerActivity extends Activity {

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

    Spinner m_spin;
    ListView m_cusList;
    SimpleAdapter m_adapter;

    TextView m_customerCode;
    TextView m_customerName;
    Spinner m_customerSection;

    List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

    // loading bar
    public ProgressDialog dialog;

    // loading more in listview
    int currentVisibleItemCount;
    private boolean isEnd = false;
    private OnScrollListener customScrollListener = new OnScrollListener() {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            currentVisibleItemCount = visibleItemCount;

            if ((firstVisibleItem + visibleItemCount) == totalItemCount && firstVisibleItem != 0)
                isEnd = true;
            else
                isEnd = false;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (isEnd && currentVisibleItemCount > 0 && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
                doSearch();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_customer);

//		//----------------------------------------//
//		// Toolbar
//		//----------------------------------------//
//        ImageView leftIcon = findViewById(R.id.toolbar_left_icon);
//        TextView toolbarTitle = findViewById(R.id.toolbar_title_text);
//        Button toobarSetting = findViewById(R.id.toolbar_setting_button);
//
//        leftIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                finish();
//            }
//        });
//        toobarSetting.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(ManageCustomerActivity.this, TIPSPreferences.class));
//            }
//        });
//
//        toolbarTitle.setText("거래처관리");
//		//----------------------------------------//

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
        //거래처코드
        m_customerCode = (TextView) findViewById(R.id.editTextCustomerCode);
        //거래처명
        m_customerName = (TextView) findViewById(R.id.editTextCustomerName);
        m_customerSection = (Spinner) findViewById(R.id.spinnerCustomerCodeType);

        Button searchButton = (Button) findViewById(R.id.buttonCustomerSearch);

        Button renewButton = (Button) findViewById(R.id.buttonCustomerRenew);

        //조회버튼
        searchButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                deleteListViewAll();
                doSearch();
                //closeKeyboard();
            }
        });
        //등록버튼
        /*registButton.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) { 
	        	doRegister();
	        	closeKeyboard();
	        }
		});*/
        //새로고침버튼
        renewButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                doClearInputBox();
            }
        });

        m_cusList = (ListView) findViewById(R.id.listviewCustomerList);
        m_cusList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fillCustomerFormFromList(position);
            }
        });

        m_cusList.setOnScrollListener(customScrollListener);

        String[] from = new String[]{"Office_Code", "Office_Sec", "Charge_User", "Office_Name", "Office_Tel1", "Charge_Tel"};
        int[] to = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6};

        // fill in the grid_item layout
        m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_customer_detail_list, from, to);
        m_cusList.setAdapter(m_adapter);

        //Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
        TextView textView = (TextView) findViewById(R.id.textView2);
        //textView.setTypeface(typeface);

        textView = (TextView) findViewById(R.id.textView5);
        //textView.setTypeface(typeface);

        textView = (TextView) findViewById(R.id.textView4);
        // textView.setTypeface(typeface);

        textView = (TextView) findViewById(R.id.textView3);
        // textView.setTypeface(typeface);

        textView = (TextView) findViewById(R.id.textView2);
        // textView.setTypeface(typeface);

        textView = (TextView) findViewById(R.id.item1);
        //textView.setTypeface(typeface);

        textView = (TextView) findViewById(R.id.item2);
        //  textView.setTypeface(typeface);

        textView = (TextView) findViewById(R.id.item3);
        // textView.setTypeface(typeface);

        // 거래처 코드 입력 후 포커스 딴 곳을 옮길 경우
        m_customerCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String customerCode = m_customerCode.getText().toString();

                    if (!customerCode.equals("")) // 입력한 customerCode가 값이 있을 경우만
                        doSearch();
                }
            }
        });
    }

    // private methods
    private void fillCustomerFormFromList(int position) {
        String code = mfillMaps.get(position).get("Office_Code");
        String name = mfillMaps.get(position).get("Office_Name");
        String StringSection = mfillMaps.get(position).get("Office_Sec");
        int section;

        m_customerCode.setText(code);
        m_customerName.setText(name);
        if (StringSection.equals("매입거래처")) section = 1;
        else if (StringSection.equals("수수료거래처")) section = 2;
        else if (StringSection.equals("매출거래처")) section = 3;
        else section = 0;
        m_customerSection.setSelection(section);
    }

    private void closeKeyboard() {
        getWindow().getCurrentFocus().clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getWindow().getCurrentFocus().getWindowToken(), 0);
    }

    public void deleteListViewAll() {
        if (mfillMaps.isEmpty()) return;

        mfillMaps.removeAll(mfillMaps);
        m_adapter.notifyDataSetChanged();
    }

    public void doClearInputBox() {
        m_customerCode.setText("");
        m_customerName.setText("");
        m_customerSection.setSelection(0);
    }

    public void updateListView(JSONArray results) {

        for (int i = 0; i < results.length(); i++) {

            try {
                JSONObject json = results.getJSONObject(i);
                HashMap<String, String> map = JsonHelper.toStringHashMap(json);

                String section = map.get("Office_Sec");
                if (section.equals("1")) section = "매입거래처";
                else if (section.equals("2")) section = "수수료거래처";
                else if (section.equals("3")) section = "매출거래처";
                map.put("Office_Sec", section);

                mfillMaps.add(map);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        m_adapter.notifyDataSetChanged();
    }

    // 조회
    public void doSearch() {
        //입력된 코드 가져오기
        String customerCode = m_customerCode.getText().toString();
        String customerName = m_customerName.getText().toString();
        String customerSection = String.valueOf(m_customerSection.getSelectedItemPosition());

        if (customerSection.equals("0")) customerSection = "";

        String query = "";
        String index = String.valueOf(mfillMaps.size());
        query = "select TOP 50 * from Office_Manage "
                + " WHERE "
                + " Office_Use = '1' AND "
                + " Office_Code like '%" + customerCode + "%' AND"
                + " Office_Name like '%" + customerName + "%' AND"
                + " Office_Sec like '%" + customerSection + "%' AND "
                + " Office_Code NOT IN (SELECT TOP " + index + " Office_Code FROM Office_Manage "
                + " WHERE Office_Use = '1' AND Office_Code like '%" + customerCode + "%'"
                + " AND Office_Name like '%" + customerName + "%'"
                + " AND Office_Sec like '%" + customerSection + "%'"
                + " Order By Office_Code ASC) Order By Office_Code ASC;";

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();

        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                if (results.length() > 0) {
                    Toast.makeText(getApplicationContext(), "조회 완료", Toast.LENGTH_SHORT).show();
                    updateListView(results);
                } else {
                    Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    public void doRegister() {
        //입력된 코드 가져오기
        String customerCode = m_customerCode.getText().toString();
        String customerName = m_customerName.getText().toString();
        String customerSection = String.valueOf(m_customerSection.getSelectedItemPosition());

        String query = "";

        if (customerCode.equals("") || customerName.equals("") || customerSection.equals("0")) {
            Toast.makeText(getApplicationContext(), "비어있는 필드가 있습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        query += "insert into Office_Manage(Office_Code, Office_Name, Office_Sec)" +
                "values('" + customerCode + "', '" + customerName + "', '" + customerSection + "');";
        query += "select * from Office_Manage WHERE Office_Code = '" + customerCode + "';";

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();
        deleteListViewAll();

        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();

                if (results.length() > 0) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getApplicationContext(),AlertDialog.THEME_HOLO_LIGHT);
                    alertDialog.setMessage("정상적으로 등록되었습니다.");
                    alertDialog.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    alertDialog.show();
                    updateListView(results);
                } else {
                    Toast.makeText(getApplicationContext(), "알수없는 이유로 등록하지 못하였습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "등록 실패", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            ActionBar actionbar = getActionBar();
            actionbar.setDisplayShowHomeEnabled(false);
            actionbar.setDisplayShowTitleEnabled(true);
            actionbar.setDisplayShowCustomEnabled(true);
            actionbar.setTitle("거래처관리");

            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_customer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // This ID represents the Home or Up button. In the case of this
                // activity, the Up button is shown. Use NavUtils to allow users
                // to navigate up one level in the application structure. For
                // more details, see the Navigation pattern on Android Design:
                //
                // http://developer.android.com/design/patterns/navigation.html#up-vs-back
                //
                NavUtils.navigateUpFromSameTask(this);
                return true;

            case R.id.action_settings:
                startActivity(new Intent(this, TIPSPreferences.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
