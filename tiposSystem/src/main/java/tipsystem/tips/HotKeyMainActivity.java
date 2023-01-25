package tipsystem.tips;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import kr.co.tipos.tips.R;

public class HotKeyMainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_key_main);

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
                Intent intent = new Intent(HotKeyMainActivity.this, MainMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 스택 중간 지우기
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 스택 맨처음 확인
                startActivity(intent);
            }
        });
//        toobarSetting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                startActivity(new Intent(HotKeyMainActivity.this,TIPSPreferences.class));
//            }
//        });

        configIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(HotKeyMainActivity.this,TIPSPreferences.class));
            }
        });
        toolbarTitle.setText("단축키 설정");
        //----------------------------------------//

    }

    public void onClickHotKeyButton(View view){
        Intent intent = new Intent(this,HotKeyManageActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    public void onClickScaleButton(View view){
        Intent intent = new Intent(this,HotKeyScaleActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    public void onClickSaleMessageButton(View view){
        Intent intent = new Intent(this,SaleMessageManageActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }
}