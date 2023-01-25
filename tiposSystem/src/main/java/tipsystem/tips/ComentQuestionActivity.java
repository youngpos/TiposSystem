package tipsystem.tips;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.TIPOS;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class ComentQuestionActivity extends Activity {

    // loading bar
    private ProgressDialog dialog;
    //Spinner m_spin;
    Button m_cancelButton;
    Button m_comentSaveButton;

    EditText m_comentText;

    JSONObject m_shop;
    JSONObject m_userProfile;
    String m_office_code;

    String m_Mode;

    //글번호 저장
    String m_idx;
    ListView listviewComentList;
    List<HashMap<String, String>> fillMaps;
    JSONArray m_results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coment_question);

        //샵정보 불러오기
        m_shop = LocalStorage.getJSONObject(this, "currentShopData");
        m_userProfile = LocalStorage.getJSONObject(this, "userProfile");

        Log.i("currentShopData", m_shop.toString());
        Log.i("userProfile", m_userProfile.toString());

        try {
            //오피스 코드 불러오기
            m_office_code = m_shop.getString("OFFICE_CODE");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //폰트 설정
        Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");

        TextView textViewShopTitle = (TextView) findViewById(R.id.textViewShopTitle);
        textViewShopTitle.setTypeface(typeface);

        TextView textViewContentTitle = (TextView) findViewById(R.id.content_title);
        textViewContentTitle.setTypeface(typeface);

        m_comentText = (EditText) findViewById(R.id.textviewComents);
        m_comentText.setTypeface(typeface);

        //리스트 선언
        listviewComentList = (ListView) findViewById(R.id.listviewComentsList);

        //댓글 버튼 선언하기
        m_comentSaveButton = (Button) findViewById(R.id.buttonComentSave);

        m_comentSaveButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                saveComent();
            }
        });

        //닫기 버튼 선언하기
        m_cancelButton = (Button) findViewById(R.id.buttonComentCancel);
        m_cancelButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });


        String data = getIntent().getStringExtra("DATA");

        try {
            JSONObject json = new JSONObject(data);
            Log.w("data", json.toString());

            //댓글버튼 댓글수 등록하기
            m_idx = json.getString("B_idx");
            textViewContentTitle.setText(json.getString("B_Title"));

        } catch (JSONException e) {
            e.printStackTrace();
        }


        // 매장명 입력
        JSONObject currentShopData = LocalStorage.getJSONObject(this, "currentShopData");
        String Office_Name = null;
        try {
            Office_Name = currentShopData.getString("Office_Name");
            textViewShopTitle.setText(Office_Name);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //댓글 목록 불러오기
        listViewStart();

    }

    //댓글목록 불러오기 함수
    public void listViewStart() {

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();

        String query = "";

        // Tran서버에서 BBS_Coment 테이블의 B_idx를 기준으로 office_user 테이블
        query = " select * From " +
                " BBS_Coment A left join v_office_user B on A.c_writerId=B.sto_cd " +
                " where A.b_idx='" + m_idx.toString() + "' order by A.C_idx DESC ";


        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                fetchComents(results);
            }

            // 2022.05.26.본사서버 IP변경
        }).execute(TIPOS.HOST_SERVER_IP+":"+TIPOS.HOST_SERVER_PORT, TIPOS.HOST_SERVER_DB, TIPOS.HOST_SERVER_ID, TIPOS.HOST_SERVER_PW, query);

    }

    //댓글 리스트 보기
    public void fetchComents(JSONArray results) {

        // create the grid item mapping
        String[] from = new String[]{"Office_Name", "C_RegDate", "C_Memo"};
        int[] to = new int[]{R.id.itemName, R.id.itemDateTime, R.id.itemComent};

        // prepare the list of all records
        //List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
        fillMaps = new ArrayList<HashMap<String, String>>();

        /**
         * 댓글 리스트 표시 하기
         *
         * */
        for (int i = 0; i < results.length(); i++) {
            try {
                JSONObject listcoments = results.getJSONObject(i);
                HashMap<String, String> map = JsonHelper.toStringHashMap(listcoments);

                String datetime = map.get("C_RegDate").toString();
                datetime.substring(1, 18);
                map.put("C_RegDate", datetime.toString());

                //목록에 등록
                fillMaps.add(map);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //댓글쓰기창 초기화
        m_comentText.setText("");
        // fill in the grid_item layout
        SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.activity_listview_coment, from, to);

        listviewComentList.setAdapter(adapter);
    }


    // private methos
    private void saveComent() {

        //EditText editTextManagerName = (EditText) findViewById(R.id.editTextManagerName);
        EditText editTextContect = (EditText) findViewById(R.id.textviewComents);
        //String ip = getLocalIpAddress();
        String coment = editTextContect.getText().toString();

        //코멘트내용이 비어 있으면 다시 작성 요청
        if (coment.equals("")) {
            String msg = "댓글 내용을 입력해 주세요~!";
            didAddOrUpdateNewNotice(msg);
            return;
        }
        //Log.w("현재 IP 주소 : ", ip.toString());
        String contentNumber = LocalStorage.getString(this, "phoneNumber");
        // ..
        addNewNotice(contentNumber, m_office_code, coment);

    }


    // 새로운 공지사항 추가 함수
    public void addNewNotice(String b_ip, String code, String content) {

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();

        // 쿼리 작성하기
        String query = "";
        m_Mode = "NEW";
        if (m_Mode.equals("NEW")) {
            query = " INSERT INTO BBS_Coment( B_idx, C_Memo, C_RegDate, C_WriterID, C_IP, B_Gubun ) "
                    + " VALUES( '" + m_idx.toString() + "', '" + content.toString() + "', CONVERT(VARCHAR(20), getdate(), 120), '" + code.toString() + "', '" + b_ip.toString() + "', '3' ); ";
        } else {
	    	/*query = "UPDATE Mult_BBS SET(b_gubun,b_pass,b_html,b_title,b_content,b_ip,b_writerID,OFFICE_CODE,APP_USERNAME,APP_HP,APP_GUBUN)" 
		    		+ " VALUES('3','','1',CONVERT(VARCHAR(8000),'" + content+"'),'"+content+"','"
		    		+ b_ip +"','" + code+"','" + code+"','" + name+"','" + phone+"','" + gubun+"');"; */
            query = "UPDATE MULT_BBS SET B_REGROUP= B_IDX";
            query += " WHERE B_REGROUP = 0 ;";
        }

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                String msg = "저장이 완료 되었습니다.";
                didAddOrUpdateNewNotice(msg);
            }
            // 2022.05.26.본사서버 IP변경
        }).execute(TIPOS.HOST_SERVER_IP+":"+TIPOS.HOST_SERVER_PORT, TIPOS.HOST_SERVER_DB, TIPOS.HOST_SERVER_ID, TIPOS.HOST_SERVER_PW, query);
    }

    // DB에 접속후 호출되는 함수
    public void didAddOrUpdateNewNotice(String msg) {

        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        //AlertDialog.Builder builder = new AlertDialog.Builder(ComentQuestionActivity.this);
        AlertDialog.Builder builder = new AlertDialog.Builder(ComentQuestionActivity.this,AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle("알림");
        builder.setMessage(msg.toString());
        builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listViewStart();
            }
        });
        builder.show();
    }

    /**
     * 휴대폰 ip정보 불러오기
     */
    public String getLocalIpAddress() {
        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            while (en.hasMoreElements()) {
                NetworkInterface interf = en.nextElement();
                Enumeration<InetAddress> ips = interf.getInetAddresses();
                while (ips.hasMoreElements()) {
                    InetAddress inetAddress = ips.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("Testing", ex.toString());
        }
        return null;
    }

    /**
     * Set up the {@link android.app.ActionBar}.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void setupActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

            getActionBar().setDisplayHomeAsUpEnabled(true);
            ActionBar actionbar = getActionBar();
            //LinearLayout custom_action_bar = (LinearLayout) View.inflate(this, R.layout.activity_custom_actionbar, null);
            //actionbar.setCustomView(custom_action_bar);
            actionbar.setDisplayShowHomeEnabled(false);
            actionbar.setDisplayShowTitleEnabled(true);
            actionbar.setTitle("댓글달기");
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.input_question, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }
}
