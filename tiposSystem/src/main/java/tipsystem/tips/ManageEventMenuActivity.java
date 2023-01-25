package tipsystem.tips;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import kr.co.tipos.tips.R;

public class ManageEventMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event_menu);

        //----------------------------------------//
        // Toolbar
        //----------------------------------------//
        ImageView homeIcon = findViewById(R.id.toolbar_home_icon);
        TextView toolbarTitle = findViewById(R.id.toolbar_title_text);
        ImageView configIcon = findViewById(R.id.toolbar_config_icon);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ManageEventMenuActivity.this, MainMenuActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 스택 중간 지우기
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 스택 맨처음 확인
                startActivity(intent);
            }
        });
        configIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ManageEventMenuActivity.this, TIPSPreferences.class));
            }
        });

        toolbarTitle.setText("도매 관리");
        //----------------------------------------//
    }

    // 행사상품등록
    public void onClickManageEvent(View view){
        Intent intent = new Intent(this, ManageEventActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    // 행사전표조회
    public void onClickManageEventList(View view){
        Intent intent = new Intent(this, ManageEventListActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }


}