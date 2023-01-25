package tipsystem.tips;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class SaleMessageRegistActivity extends Activity {
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
    TextView mNameTextView;
    EditText sellPriEditText;
    TextView writeDateTextView;
    TextView writerTextView;
    TextView editDateTextView;
    TextView editorTextView;

    Button saveButton;
    Button closeButton;

    String S_Idx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sale_message_regist);

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
        mNameTextView = (TextView)findViewById(R.id.m_name_textview);
        sellPriEditText = (EditText)findViewById(R.id.sell_pri_edittext);
        writeDateTextView = (TextView)findViewById(R.id.write_date_textview);
        writerTextView = (TextView)findViewById(R.id.writer_textview);
        editDateTextView = (TextView)findViewById(R.id.edit_date_textview);
        editorTextView = (TextView)findViewById(R.id.editor_textview);

        saveButton = (Button)findViewById(R.id.save_button);
        closeButton = (Button)findViewById(R.id.close_button);

        S_Idx = getIntent().getStringExtra("no");
        String barcode = getIntent().getStringExtra("barcode");
        String gName = getIntent().getStringExtra("gName");
        String mName = getIntent().getStringExtra("mName");
        String sellPri = getIntent().getStringExtra("sellPri");
        String writeDate = getIntent().getStringExtra("writeDate");
        String writer = getIntent().getStringExtra("writer");
        String editDate = getIntent().getStringExtra("editDate");
        String editor = getIntent().getStringExtra("editor");

        barcodeTextView.setText(barcode);
        gNameTextView.setText(gName);
        mNameTextView.setText(mName);
        sellPriEditText.setText(sellPri);
        writeDateTextView.setText(writeDate);
        writerTextView.setText(writer);
        editDateTextView.setText(editDate);
        editorTextView.setText(editor);



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

        if(sellPriEditText.getText().toString().equals("")){
            newSellPri = 0;
        }else{
            newSellPri = Float.parseFloat (sellPriEditText.getText().toString().replace(",",""));
        }

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = sdf.format(new Date());

            //
            String query = "Update Sell_Msg Set ";
            query += " S_Barcode = '" + barcodeTextView.getText().toString() + "' ";
            query += ",G_Name = '" + mNameTextView.getText().toString() + "' ";
            query += ",Sell_Pri = " + String.valueOf(newSellPri) + "  ";
            query += ",Edit_Date = '" + currentDate + "' ";
            query += ",Editor = '" + tips.getUSER_ID() + "' ";
            query += " WHERE S_IDX =  " + S_Idx + " ";

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
                    String str = "판매금액메세지 변경 완료";
                    Toast.makeText(SaleMessageRegistActivity.this,str,Toast.LENGTH_SHORT).show();
                    AlertDialog.Builder builder = new AlertDialog.Builder(SaleMessageRegistActivity.this,AlertDialog.THEME_HOLO_LIGHT);
                    builder.setTitle("알림");
                    builder.setMessage("판매금액메세지 변경이 정상적으로 변경되었습니다.");
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
                    String str = "판매금액메세지 변경 실패\r\n"+msg;
                    Toast.makeText(SaleMessageRegistActivity.this,str,Toast.LENGTH_SHORT).show();
                }
            }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

}