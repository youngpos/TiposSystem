package tipsystem.tips;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import kr.co.tipos.tips.R;
import tipsystem.utils.LocalStorage;

public class DeliveryCallHomeManager extends Activity {

    JSONObject m_shop;
    JSONObject m_userProfile;
    String m_OfficeCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_call_home_manager);

        //----------------------------------------//
        // Toolbar
        //----------------------------------------//
        ImageView homeIcon = findViewById(R.id.toolbar_home_icon);
        TextView toolbarTitle = findViewById(R.id.toolbar_title_text);
        //Button toobarSetting = findViewById(R.id.toolbar_setting_button);
        ImageView configIcon = findViewById(R.id.toolbar_config_icon);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DeliveryCallHomeManager.this, MainMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 스택 중간 지우기
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 스택 맨처음 확인
                startActivity(intent);
            }
        });

//        toobarSetting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(DeliveryCallHomeManager.this, TIPSPreferences.class));
//            }
//        });
        configIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DeliveryCallHomeManager.this, TIPSPreferences.class));
            }
        });

        toolbarTitle.setText("배송 관리");
        //----------------------------------------//

        m_shop = LocalStorage.getJSONObject(this, "currentShopData");
        m_userProfile = LocalStorage.getJSONObject(this, "userProfile");

        try {
            m_OfficeCode = m_shop.getString("OFFICE_CODE");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    //도매등록 견적서 기능
	/*public void onClickDomeSaleCall(View view)
	{
		Intent intent = new Intent(this, ManageDomeSaleCallActivity.class);
    	startActivity(intent);
	}*/

    //판매호출 기능
    public void onClickHomeDeliveryActivity(View view) {
        Intent intent = new Intent(this, ManageHomeDeliveryActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    //배송관리기능
    public void onClickHomeDeliveryCalActivity(View view) {
        Intent intent = new Intent(this, ManageHomeDeliveryCalActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }


}
