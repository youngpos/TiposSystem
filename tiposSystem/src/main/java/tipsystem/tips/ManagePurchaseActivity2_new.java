package tipsystem.tips;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import kr.co.tipos.tips.R;

public class ManagePurchaseActivity2_new extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_purchase2_new);

		// Show the Up button in the action bar.
		//setupActionBar();

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
				Intent intent = new Intent(ManagePurchaseActivity2_new.this, MainMenuActivity.class);
				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 스택 중간 지우기
				intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 스택 맨처음 확인
				startActivity(intent);
			}
		});
//		toobarSetting.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				startActivity(new Intent(ManagePurchaseActivity2_new.this, TIPSPreferences.class));
//
//			}
//		});
		configIcon.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(ManagePurchaseActivity2_new.this, TIPSPreferences.class));
			}
		});

		toolbarTitle.setText("매입등록");
		//----------------------------------------//

	}

	/**
	 * Set up the {@link ActionBar}.
	 */
//	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
//	public void setupActionBar(){
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
//
//			ActionBar actionbar = getActionBar();
//
//
//			actionbar.setDisplayShowHomeEnabled(false);
//			actionbar.setDisplayShowTitleEnabled(true);
//			actionbar.setDisplayShowCustomEnabled(true);
//
//			getActionBar().setDisplayHomeAsUpEnabled(false);
//		}
//	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manage_purchase, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item){
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
		
	//매입전표 등록
	public void onClickPurchaseRegist(View view){
		Intent intent = new Intent(this, PurchaseRegistActivity1_new.class);
    	//Intent intent = new Intent(this, SelectShopActivity.class);    	
    	//EditText editText = (EditText) findViewById(R.id.editTextShopCode);
    	//String message = editText.getText().toString();
    	//intent.putExtra(EXTRA_MESSAGE, message);
		intent.putExtra("Office_Code", "");
		intent.putExtra("In_Date", "");
		intent.putExtra("Office_Name", "");
    	startActivity(intent);
		overridePendingTransition(0, 0);

	}

	//매입 미전송목록
	public void onClickPurchaseWatingList(View view){
		Intent intent = new Intent(this, PurchaseListActivity3_new.class);
		//EditText editText = (EditText) findViewById(R.id.editTextShopCode);
		//String message = editText.getText().toString();
		//intent.putExtra(EXTRA_MESSAGE, message);
		startActivity(intent);
		overridePendingTransition(0, 0);

	}
	
	//매입 전송목록
	public void onClickPurchasePaymentStatus(View view){
		Intent intent = new Intent(this, PurchaseListActivity2_new.class);
    	//EditText editText = (EditText) findViewById(R.id.editTextShopCode);
    	//String message = editText.getText().toString();
    	//intent.putExtra(EXTRA_MESSAGE, message);
    	startActivity(intent);
		overridePendingTransition(0, 0);

	}
}
