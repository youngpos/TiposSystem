package tipsystem.tips;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.tipos.tips.R;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL4;
import tipsystem.utils.TIPS_Config;
import tipsystem.utils.UserPublicUtils;

public class HotKeyScaleRegistActivity extends Activity {

    // 기본
    private TIPS_Config tips;
    private UserPublicUtils upu;
    private ProgressDialog dialog;
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
    String userID;

    // xml 레이아웃 정의
    TextView barcodeTextView;
    TextView gNameTextView;
    TextView unitTextView;
    TextView sellPriTextView;
    EditText sellPriEditText;
    Button saveButton;
    Button closeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_key_scale_regist);

        mContext = this;
        tips = new TIPS_Config(mContext);

        // 매장정보
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

        JSONObject userProfile = LocalStorage.getJSONObject(this, "userProfile");
        try {
            userID = userProfile.getString("User_ID");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        dialog = new ProgressDialog(this);

        barcodeTextView = (TextView)findViewById(R.id.barcode_textview);
        gNameTextView = (TextView)findViewById(R.id.g_name_textview);
        unitTextView = (TextView)findViewById(R.id.unit_textview);
        sellPriTextView = (TextView)findViewById(R.id.sell_pri_textview);
        sellPriEditText = (EditText)findViewById(R.id.sell_pri_edittext);
        saveButton = (Button)findViewById(R.id.save_button);
        closeButton = (Button)findViewById(R.id.close_button);

        String barcode = getIntent().getStringExtra("barcode");
        String gName = getIntent().getStringExtra("gName");
        String sellPri = getIntent().getStringExtra("sellPri");

        barcodeTextView.setText(barcode);
        gNameTextView.setText(gName);
        sellPriTextView.setText(sellPri);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUpdateMain();
            }
        });

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        sellPriEditText.setImeOptions (EditorInfo.IME_ACTION_DONE);

        sellPriEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        sellPriEditText.requestFocus(); // 판매가로 포커스이동

        sellPriEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // TODO Auto-generated method stub
                //완료 버튼을 눌렀습니다.
                if(i == EditorInfo.IME_ACTION_DONE){
                    //setProfitRate();
                }
                return false;
            }
        });
    }

    private void setUpdateMain(){
        float oldSellPri = 0;
        float newSellPri = 0;

        boolean sellChk = false;


        if(sellPriTextView.getText().toString().equals("")){
            oldSellPri = 0;
        }else{
            oldSellPri = Float.parseFloat (sellPriTextView.getText().toString().replace(",",""));
        }

        if(sellPriEditText.getText().toString().equals("")){
            newSellPri = 0;
        }else{
            newSellPri = Float.parseFloat (sellPriEditText.getText().toString().replace(",",""));
        }

        if(newSellPri == 0){
            newSellPri = oldSellPri;
        }

        if(oldSellPri != newSellPri){
            sellChk = true;
        }

        // 매입가나 판매가가 변동이 있을 시
        if(sellChk == true){

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = sdf.format(new Date());

            // 상품마스타, 히스토리 마스타 업데이트
            String query = "Update Goods Set ";
            query += " Sell_Pri = " + String.valueOf(newSellPri) + "  ";
            query += ",Edit_Date = '" + currentDate + "' ";
            query += ",Editor = '" + tips.getUSER_ID() + "' ";
            query += " WHERE Barcode = '" + barcodeTextView.getText().toString() + "' ";
            query += " AND Scale_Use = '1';";

            // 로딩 다이알로그
            //다이얼로그 통일화
            // dialog = new ProgressDialog(this);
            if(!dialog.isShowing()) {
                dialog.setMessage("Loading....");
                dialog.setCancelable(false);
                dialog.show();
            }

            new MSSQL4(new MSSQL4.MSSQL4CallbackInterface() {
                @Override
                public void onRequestCompleted(Integer results) {
                    dialog.dismiss();
                    dialog.cancel();
                    String str = "저울가격변경 완료";
                    Toast.makeText(HotKeyScaleRegistActivity.this,str,Toast.LENGTH_SHORT).show();
                    //----------------------------------------//
                    // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
                    //----------------------------------------//
                    AlertDialog.Builder builder = new AlertDialog.Builder(HotKeyScaleRegistActivity.this,AlertDialog.THEME_HOLO_LIGHT);
                    builder.setTitle("알림");
                    builder.setMessage("저울상품가격이 정상적으로 변경되었습니다.");
                    builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    });
                    builder.show();

                }

                @Override
                public void onRequestFailed(int code, String msg) {
                    dialog.dismiss();
                    dialog.cancel();
                    String str = "저울가격변경 실패\r\n"+msg;
                    Toast.makeText(HotKeyScaleRegistActivity.this,str,Toast.LENGTH_SHORT).show();
                }
            }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
        }
    }

}