package tipsystem.tips;

import static tipsystem.utils.DBAdapter.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;
import tipsystem.utils.LocalStorage;

public class MainMenuActivity extends Activity {

    private JSONObject m_shop;
    private JSONObject m_userProfile;
    private String version;
    private String m_APP_USER_GRADE;

    private String OFFICE_CODE;
    private ProgressDialog dialog;
    private List<HashMap<String, String>> fillMaps;

    private ListView m_listBoard;
    private JSONArray m_results;

    private Context mContext;
    private SharedPreferences pref;
    private boolean m3Mobile;
    private boolean purchase_new;

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        LinearLayout layoutLayout = (LinearLayout) findViewById(R.id.linearLayout11);    //최고관리자

        Button button = (Button) findViewById(R.id.button_menu01);
        float height = button.getWidth();
        float weight = button.getHeight();

        float a = layoutLayout.getWidth();
        float b = layoutLayout.getHeight();

        Log.d("Button size", "dpHeight : : " + button.getHeight() + "  dpWidth : " + button.getWidth());

        //dinsity 구하기
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int px = displayMetrics.densityDpi;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        // 해상도 구하기
//        Display display = getWindowManager().getDefaultDisplay();
//        Point size = new Point();
//        display.getSize(size);
//        Log.d(TAG, ">>> size.x : " + size.x + ", size.y : " + size.y);
        //출처: https://devuryu.tistory.com/194 [대부류 - [Dev u Ryu]]


// 안드로이드 실제 디바이스 사이즈 구하기 (xml에 dp값)
//https://lcw126.tistory.com/280
//		Display display = getWindowManager().getDefaultDisplay();
//		DisplayMetrics outMetrics = new DisplayMetrics ();
//		display.getMetrics(outMetrics);
//
//		float density = getResources().getDisplayMetrics().density;
//		float dpHeight = outMetrics.heightPixels / density;
//		float dpWidth = outMetrics.widthPixels / density;
//
//		//dp별 layout 별도 적용
//		Log.d("Device dp","dpHeight : : "+dpHeight+"  dpWidth : "+dpWidth+"  density : "+density);


//		InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
//		imm.showSoftInput(input, 0);

        ImageView homeIcon = findViewById(R.id.toolbar_home_icon);
        TextView toolbarTitle = findViewById(R.id.toolbar_title_text);
        ImageView configIcon = findViewById(R.id.toolbar_config_icon);

        //----------------------------------------//
        // Tool Bar
        //----------------------------------------//
        //homeIcon.setVisibility(View.VISIBLE);
        //leftIcon.setVisibility(View.GONE);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //startActivity(new Intent(MainMenuActivity.this,MainActivity.class));
                Intent intent = new Intent(MainMenuActivity.this, MainActivity.class);

                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 스택 중간 지우기
                //intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 스택 맨처음 확인

                //intent.putExtra("blnPreviewActivity",true);
                intent.putExtra("PreviewActivity","MainMenuActivity");

                startActivity(intent);
            }
        });

//		toobarSetting.setOnClickListener(new View.OnClickListener() {
//			@Override
//			public void onClick(View view) {
//				startActivity(new Intent(MainMenuActivity.this,TIPSPreferences.class));
//			}
//		});

        configIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainMenuActivity.this, TIPSPreferences.class));
            }
        });

        //toolbarTitle.setText("POSON 관리");
        //----------------------------------------//

        mContext = this;

        m_shop = LocalStorage.getJSONObject(this, "currentShopData");
        m_userProfile = LocalStorage.getJSONObject(this, "userProfile");
        version = LocalStorage.getString(this, "nowVersion");
        Log.i("현재버전", version.toString());
        Log.i("currentShopData", m_shop.toString());
        Log.i("userProfile", m_userProfile.toString());

        String Office_Name = null, SHOP_IP = null, SHOP_PORT = null, APP_HP = null, OFFICE_CODE2 = null;

        try {
            Office_Name = m_shop.getString("Office_Name");
            OFFICE_CODE = m_shop.getString("OFFICE_CODE");

            m_APP_USER_GRADE = m_userProfile.getString("APP_USER_GRADE");
            OFFICE_CODE2 = m_userProfile.getString("OFFICE_CODE"); //수수료매장코드
        } catch (JSONException e) {
            e.printStackTrace();
        }

        toolbarTitle.setText(Office_Name);

        // 환경설정 관련 옵션 아이템 불러오기
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        m3Mobile = pref.getBoolean("m3mobile", false);
        purchase_new = pref.getBoolean("purchase_new", false);

        //권한에 따른 버튼배열 정리
        buttonVisibility();



    }


    /**
     * 권한별 버튼 Show 함수
     */
    private void buttonVisibility() {
        //m_APP_USER_GRADE 0:최고관리자, 1:부관리자, 2:수수료매장 3:판매분매입거래처(2017-06-07 아직 추가안됨)
        LinearLayout layoutLayout1 = (LinearLayout) findViewById(R.id.linearLayout1);    //최고관리자
        LinearLayout layoutLayout2 = (LinearLayout) findViewById(R.id.linearLayout2);    //부관리자
        LinearLayout layoutLayout3 = (LinearLayout) findViewById(R.id.linearLayout3);    //수수료매장
        LinearLayout layoutLayout4 = (LinearLayout) findViewById(R.id.linearLayout4);    //발주관리자
        LinearLayout layoutLayout5 = (LinearLayout) findViewById(R.id.linearLayout5);    //배달관리자

        switch (m_APP_USER_GRADE) {
            case "0":
                layoutLayout1.setVisibility(View.VISIBLE);
                break;
            case "1":
                layoutLayout2.setVisibility(View.VISIBLE);
                break;
            case "2":
                layoutLayout3.setVisibility(View.VISIBLE);
                break;
            case "3":
                layoutLayout4.setVisibility(View.VISIBLE);
                break;
            case "4":
                layoutLayout5.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }


    //게시
    public void onClickQuestion(View view) {
        Intent intent = new Intent(this, InputQuestionActivity.class);
        intent.putExtra("MODE", "NEW");
        startActivity(intent);
    }

    //1. 기본관리
    public void onClickDefaultManage(View view) {

        Intent intent = new Intent(this, ManageCodeActivity.class);
        startActivity(intent);

        //전환할때 나타나는 애니메이션을 제거하는 방법
        overridePendingTransition(0, 0);
        //출처: https://mine-it-record.tistory.com/235 [나만의 기록들]
    }

//	// 2022.03.10.매출전표 새로 추가
//	//2. 매출전표
//	public void onClickSaleList(View view)
//	{
//		AlertDialog.Builder builder = new AlertDialog.Builder(MainMenuActivity.this);
//		builder.setTitle("알림");
//		builder.setMessage("아직 이용할수 없습니다.\r\n작업중 입니다!");
//		builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
//			@Override
//			public void onClick(DialogInterface dialog, int which) {
//			}
//		});
//		builder.show();
//	}

    //2. 매출속보
    public void onClickSalesNews(View view) {

//        Intent intent = new Intent(this, SalesNewsActivity.class);
//        startActivity(intent);

        // 2022.06.30. 신매출속보 바로가기 체크
        boolean isRealSaleNew = LocalStorage.getBoolean(MainMenuActivity.this, "RealSaleNew:" + OFFICE_CODE);

        if (isRealSaleNew){
            //----------------------------------------//
            // 2022.07.25. 수수료는 제외
            //----------------------------------------//
            if (m_APP_USER_GRADE.equals("2")){
                Intent intent = new Intent(this, SalesNewsActivity.class);
                startActivity(intent);
            }else{
                Intent intent = new Intent(this, RealSaleMainActivity.class);
                startActivity(intent);
            }
        }else{
            Intent intent = new Intent(this, SalesNewsActivity.class);
            startActivity(intent);
        }

        overridePendingTransition(0, 0);

    }

    //3. 매출관리
    public void onClickSalesManage(View view) {

        Intent intent = new Intent(this, ManageSaleMainActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    //4. 매입관리
    public void onClickPurchaseManage(View view) {
        //신규매입장
        Intent intent = new Intent(this, ManagePurchaseActivity1_new.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    //5. 발주관리
    public void onClickBalJuManage(View view) {
        Intent intent = new Intent(this, ManageBaljuRegActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }


    //6. 수주관리
    public void onClickSuJuManage(View view) {
        Intent intent = new Intent(this, ManagePurchaseActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    //7. 행사관리
    public void onClickEventManage(View view) {
        Intent intent = new Intent(this, ManageEventListActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

//		Intent intent = new Intent(this, ManageEventMenuActivity.class);
//    	startActivity(intent);
//		overridePendingTransition(0, 0);

    }

    //8. 재고관리
    public void onClickManageStock(View view) {
        Intent intent = new Intent(this, ManageStockActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }


    //9. 도매관리
    public void onClickBaeDalManage(View view) {
        Intent intent = new Intent(this, SaleCallHomeManager.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

        //바로가기로 수정
        //Intent intent = new Intent(this, ManageDomeSaleCallActivity.class);
        //startActivity(intent);


    }

    //배송관리
    public void toManageHomeDeliveryActivity(View view) {

        Intent intent = new Intent(mContext, DeliveryCallHomeManager.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    // 단축키관리
    public void onClickHotKeyMain(View view) {
        Intent intent = new Intent(this, HotKeyMainActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    //11 매출전표 추가
    public void onClickRealSaleMain(View view) {
        //Intent intent = new Intent(this, ManageRealSaleMainActivity.class);
        //Intent intent = new Intent(this, SalesSlipActivity.class);
        Intent intent = new Intent(this, SalesReceiptActivity.class);

        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
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

    @Override
    public void onResume() {
        super.onResume();

        //fetchNotices(m_ip,m_port,OFFICE_CODE);
    }
}
