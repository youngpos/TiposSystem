package tipsystem.tips;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.tipos.tips.R;
import tipsystem.utils.LocalStorage;

public class ManageRealSaleMainActivity extends Activity {

    private JSONObject m_userProfile;
    private String m_APP_USER_GRADE;    //앱권한
    private String m_OFFICE_CODE = null;  //수수료매장거래처코드

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_managerealsale_main);

        //----------------------------------------//
        // Toolbar
        //----------------------------------------//
        ImageView homeIcon = findViewById(R.id.toolbar_home_icon);
        TextView toolbarTitle = findViewById(R.id.toolbar_title_text);
        //Button toobarSetting = findViewById(R.id.toolbar_setting_button);
        ImageView configIcon = findViewById(R.id.toolbar_config_icon);

//		leftIcon.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				finish();
//			}
//		});
        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageRealSaleMainActivity.this, MainMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 스택 중간 지우기
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 스택 맨처음 확인
                startActivity(intent);
            }
        });
//        toobarSetting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(ManageSaleMainActivity.this, TIPSPreferences.class));
//
//            }
//        });

        configIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ManageRealSaleMainActivity.this, TIPSPreferences.class));
            }
        });
        toolbarTitle.setText("매출분석");
        //----------------------------------------//

        m_userProfile = LocalStorage.getJSONObject(this, "userProfile");

        try {
            m_APP_USER_GRADE = m_userProfile.getString("APP_USER_GRADE");
            m_OFFICE_CODE = m_userProfile.getString("OFFICE_CODE"); //수수료매장코드

        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
        //TextView textView = (TextView) findViewById(R.id.textView1);
        //textView.setTypeface(typeface);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_code, menu);
        return true;
    }

    // 매장별매출현황
    public void onClickManageShop(View view) {

        Intent intent = new Intent(this, SalesShopActivity.class);
        intent.putExtra("OFFICE_CODE", "");
        intent.putExtra("OFFICE_NAME", "");
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    // 전표출력
    public void onClickManageSlip(View view) {

        Intent intent = new Intent(this, SalesSlipActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

}
