package tipsystem.tips;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import kr.co.tipos.tips.R;
import tipsystem.utils.DBAdapter;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;

import android.util.Log;

public class LoginActivity extends Activity {

    JSONObject m_shop;

    //----------------------------------------//
    // 2022.05.26.본사서버 IP변경
    //----------------------------------------//
    String m_ip = "";
    String m_port = "";
    String OFFICE_CODE;
    //----------------------------------------//
    // 2021.12.21. 매장DB IP,PW,DB 추가
    //----------------------------------------//
    String m_uuid = "";
    String m_uupw = "";
    String m_uudb = "";
    //----------------------------------------//

    DBAdapter dbadapter;

    // loading bar
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);

        m_shop = LocalStorage.getJSONObject(this, "currentShopData");

        try {
            OFFICE_CODE = m_shop.getString("OFFICE_CODE");
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

        /*
         * 2015-1-15일
         * 김현수 DB기초 테이블 생성 하기  */
        dbadapter = new DBAdapter(LoginActivity.this, OFFICE_CODE + ".tips");
        //차후 DB 베이스 가져오기         

        EditText editTextLoginPW = (EditText) findViewById(R.id.editTextLoginPW);
        editTextLoginPW.setOnEditorActionListener(new OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (actionId == EditorInfo.IME_ACTION_DONE) { // IME_ACTION_SEARCH , IME_ACTION_GO
                    onClickLogin(null);
                }
                return false;
            }
        });

        Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
        TextView textView = (TextView) findViewById(R.id.textViewID);
        textView.setTypeface(typeface);

        textView = (TextView) findViewById(R.id.textViewPW);
        textView.setTypeface(typeface);


        //2019-11-14
        EditText editTextLoginID = (EditText) findViewById(R.id.editTextLoginID);
        editTextLoginID.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

    }

    protected void onResume() {
        super.onResume();

        try {
            String OFFICE_CODE = m_shop.getString("OFFICE_CODE");

            // 해당 OFFICE_CODE 에 포스ID 가 없을경우 'P' 로 셋팅
            String posID = LocalStorage.getString(LoginActivity.this, "currentPosID:" + OFFICE_CODE);
            if (posID.equals(""))
                LocalStorage.setString(LoginActivity.this, "currentPosID:" + OFFICE_CODE, "P");

            boolean isAutoLogin = LocalStorage.getBoolean(LoginActivity.this, "AutoLogin:" + OFFICE_CODE);

            if (isAutoLogin) {
                Toast.makeText(this, "자동로그인 처리 중......", Toast.LENGTH_SHORT).show();

                String id = LocalStorage.getString(LoginActivity.this, "LoginID:" + OFFICE_CODE);
                String pw = LocalStorage.getString(LoginActivity.this, "LoginPW:" + OFFICE_CODE);

                //2020-06-15 자동로그인 키패드 숨기기
                EditText editTextLoginID = (EditText) findViewById(R.id.editTextLoginID);
                editTextLoginID.requestFocus();

                InputMethodManager imm = (InputMethodManager) getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

                doLogin(m_ip, m_port, id, pw);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.config, menu);
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

    public void onClickLogin(View view) {
        EditText editTextLoginID = (EditText) findViewById(R.id.editTextLoginID);
        EditText editTextLoginPW = (EditText) findViewById(R.id.editTextLoginPW);

        String id = editTextLoginID.getText().toString();
        String pw = editTextLoginPW.getText().toString();

        editTextLoginID.setText("");
        editTextLoginPW.setText("");

        if (id.equals("") || pw.equals("")) {
            Toast.makeText(this, "비어있는 칸이 있습니다", Toast.LENGTH_SHORT).show();
            return;
        }

        //2016년 6월 16일 김현수
        //자동로그인 옵션을 여기서 체크 할수 있습니다.
        //CheckBox autoLogin_chkbox = (CheckBox)findViewById(R.id.checkBox_autologin);
        CheckBox autoLogin_chkbox = (CheckBox) findViewById(R.id.checkBoxautologin);
        boolean isCheckedAutoLogin = autoLogin_chkbox.isChecked();

        //자동로그인에 체크를 했다면 내용을 저장합니다.
        if (isCheckedAutoLogin) {
            try {
                String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
                LocalStorage.setBoolean(LoginActivity.this, "AutoLogin:" + OFFICE_CODE, isCheckedAutoLogin);
                LocalStorage.setString(LoginActivity.this, "LoginID:" + OFFICE_CODE, id);
                LocalStorage.setString(LoginActivity.this, "LoginPW:" + OFFICE_CODE, pw);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        doLogin(m_ip, m_port, id, pw);
    }

    // 로그인관련 실행 함수
    public void doLogin(String ip, String port, String id, String pw) {

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();

        boolean isLocalFood = LocalStorage.getBoolean(LoginActivity.this, "LocalFood:" + OFFICE_CODE);

        // 쿼리 작성하기
        String query = "";

        if (isLocalFood) {
            query = "select *, 2 as APP_USER_GRADE, " + id + " as OFFICE_CODE from office_manage, Office_User where office_code='" + id + "' ";
        } else {
            query = "select * "
                    + "from Admin_User, Office_User where User_ID='" + id + "' and User_PWD ='" + pw + "'"
                    + ";";
            //query = "select * from Admin_User where APP_USER_GRADE=2;";
        }

        // 콜백함수와 함께 실행
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                didLogin(results);
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Log.i("Error", msg);
                Toast.makeText(getApplicationContext(), "에러발생", Toast.LENGTH_SHORT).show();
            }
        }).execute(ip + ":" + port, m_uudb, m_uuid, m_uupw, query);
    }

    // DB에 접속후 호출되는 함수
    public void didLogin(JSONArray results) {
        if (results.length() > 0) {

            JSONObject user;

            try {
                // Admin_User 테이블에서 가져온 사용자 정보를 로컬에 저장
                user = results.getJSONObject(0);
                LocalStorage.setJSONObject(this, "userProfile", user);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Toast.makeText(getApplicationContext(), "로그인 완료", Toast.LENGTH_SHORT).show();

            // 2022.06.30. 신매출속보 바로가기 체크
            boolean isRealSaleNew = LocalStorage.getBoolean(LoginActivity.this, "RealSaleNew:" + OFFICE_CODE);
            boolean isRealSaleNewOnly = LocalStorage.getBoolean(LoginActivity.this, "RealSaleNewOnly:" + OFFICE_CODE);

            //----------------------------------------//
            // 2022.07.25. 수수료는 제외
            //----------------------------------------//
            JSONObject m_userProfile;
            String APP_USER_GRADE;

            try {
                m_userProfile = LocalStorage.getJSONObject(LoginActivity.this, "userProfile");
                APP_USER_GRADE = m_userProfile.getString("APP_USER_GRADE");

            } catch (JSONException ee) {
                ee.printStackTrace();
                APP_USER_GRADE = "";
            }
            //----------------------------------------//

            if (isRealSaleNew && isRealSaleNewOnly) {

                if (APP_USER_GRADE.equals("2")) {
                    Intent intent = new Intent(this, MainMenuActivity.class);
                    startActivity(intent);

                } else {
                    Intent intent = new Intent(this, RealSaleMainActivity.class);
                    startActivity(intent);
                }

            } else {
                Intent intent = new Intent(this, MainMenuActivity.class);
                startActivity(intent);
            }

            finish();
        } else {
            Toast.makeText(getApplicationContext(), "로그인 실패", Toast.LENGTH_SHORT).show();
        }
    }
}
