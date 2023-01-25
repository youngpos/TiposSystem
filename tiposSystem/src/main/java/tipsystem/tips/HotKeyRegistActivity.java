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
import org.w3c.dom.Text;

import java.text.SimpleDateFormat;
import java.util.Date;

import kr.co.tipos.tips.R;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL4;
import tipsystem.utils.TIPS_Config;
import tipsystem.utils.UserPublicUtils;

public class HotKeyRegistActivity extends Activity {

    // 기본
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
    TextView stdSizeTextView;
    TextView unitTextView;
    TextView purPriTextView;
    TextView sellPriTextView;
    EditText purPriEditText;
    EditText sellPriEditText;
    TextView profitRateTextView;
    TextView scaleTextView;
    Button saveButton;
    Button closeButton;
    String mScaleUse;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hot_key_regist);

        mContext = this;
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
        stdSizeTextView = (TextView)findViewById(R.id.std_size_textview);
        unitTextView = (TextView)findViewById(R.id.unit_textview);
        profitRateTextView = (TextView)findViewById(R.id.profit_rate_textview);
        purPriTextView = (TextView)findViewById(R.id.pur_pri_textview);
        sellPriTextView = (TextView)findViewById(R.id.sell_pri_textview);
        purPriEditText = (EditText)findViewById(R.id.pur_pri_edittext);
        sellPriEditText = (EditText)findViewById(R.id.sell_pri_edittext);
        saveButton = (Button)findViewById(R.id.save_button);
        closeButton = (Button)findViewById(R.id.close_button);
        scaleTextView = (TextView)findViewById(R.id.scale_textview);

        String barcode = getIntent().getStringExtra("barcode");
        String gName = getIntent().getStringExtra("gName");
        String stdSize = getIntent().getStringExtra("stdSize");
        String unit = getIntent().getStringExtra("unit");
        String profitRate = getIntent().getStringExtra("profitRate");
        String purPri = getIntent().getStringExtra("purPri");
        String sellPri = getIntent().getStringExtra("sellPri");
        String scaleUse = getIntent().getStringExtra("scaleUse");
        String taxYn = getIntent().getStringExtra("taxYn");
        String scalePurPri = getIntent().getStringExtra("scalePurPri");
        String scaleSellPri = getIntent().getStringExtra("scaleSellPri");
        mScaleUse = scaleUse;

        barcodeTextView.setText(barcode);
        gNameTextView.setText(gName);
        stdSizeTextView.setText(stdSize);
        unitTextView.setText(unit);
        profitRateTextView.setText(profitRate);
        purPriTextView.setText(purPri);
        sellPriTextView.setText(sellPri);


        if(barcodeTextView.length() == 4){
            scaleTextView.setText("부문상품");
        }else if(scaleUse.equals("1")){
            scaleTextView.setText("저울상품");
        }else{
            scaleTextView.setText("일반상품");
        }

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

        purPriEditText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        sellPriEditText.setImeOptions (EditorInfo.IME_ACTION_DONE);

        purPriEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        sellPriEditText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        sellPriEditText.requestFocus(); // 판매가로 포커스이동

        purPriEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // TODO Auto-generated method stub
                //완료 버튼을 눌렀습니다.
                if(i == EditorInfo.IME_ACTION_DONE){
                    setProfitRate();
                }

                return false;
            }
        });

        sellPriEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                // TODO Auto-generated method stub
                //완료 버튼을 눌렀습니다.
                if(i == EditorInfo.IME_ACTION_DONE){
                    setProfitRate();
                }
                return false;
            }
        });



    }

    private void setProfitRate(){
        float oldPurPri = 0;
        float oldSellPri = 0;
        float newPurPri = 0;
        float newSellPri = 0;
        float profitRate = 0;

        boolean purChk = false;
        boolean sellChk = false;

        String taxYn = ""; // 부가세구분
        float purCost = 0; //매입가 공급가?
        float addTax = 0; // 추가부가세?

        if (purPriTextView.getText().toString().equals("")) {
            oldPurPri = 0;
        } else {
            oldPurPri = Float.parseFloat(purPriTextView.getText().toString().replace(",",""));
        }

        if (sellPriTextView.getText().toString().equals("")) {
            oldSellPri = 0;
        } else {
            oldSellPri = Float.parseFloat (sellPriTextView.getText().toString().replace(",",""));
        }

        if (purPriEditText.getText().toString().equals("")) {
            newPurPri = 0;
        } else {
            newPurPri = Float.parseFloat(purPriEditText.getText().toString().replace(",",""));
        }

        if (sellPriEditText.getText().toString().equals("")) {
            newSellPri = 0;
        } else {
            newSellPri = Float.parseFloat(sellPriEditText.getText().toString().replace(",",""));
        }

        if (newPurPri == 0) {
            newPurPri = oldPurPri;
        }

        if (newSellPri == 0) {
            newSellPri = oldSellPri;
        }

        if (oldPurPri != newPurPri) {
            purChk = true;
        }

        if (oldSellPri != newSellPri) {
            sellChk = true;
        }

        // 매입가나 판매가가 변도이 있을 시
        if ((purChk == true) || (sellChk == true)) {

            // 매입가 부가세 구분
            if (taxYn == "1") {
                purCost = Float.parseFloat( String.valueOf( newPurPri / 1.1));
                addTax = newPurPri - purCost;
            } else {
                purCost = newPurPri;
                addTax = 0;
            }

            // 판매가 원단위 절사
            newSellPri = Math.round(newSellPri / 10) * 10;

            // 이익율 계산
            if ((newPurPri == 0) || (newSellPri == 0)) {
                profitRate = 0;
            } else {
                profitRate = (newSellPri - newPurPri) / newSellPri * 100;
                profitRateTextView.setText(String.format("%.2f",profitRate));
            }
        }
    }

    private void setUpdateMain(){
        float oldPurPri = 0;
        float oldSellPri = 0;
        float newPurPri = 0;
        float newSellPri = 0;
        float profitRate = 0;

        boolean purChk = false;
        boolean sellChk = false;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentDateTime = sdf.format(new Date());
        // 날짜와 시간 구하기
        String currentDate = currentDateTime.substring(0, 10);
        String currentTime = currentDateTime.substring(11);
        String query = "";

        String taxYn = ""; // 부가세구분
        float purCost = 0; //매입가 공급가?
        float addTax = 0; // 추가부가세?

        if(purPriTextView.getText().toString().equals("")){
            oldPurPri = 0;
        }else{
            oldPurPri = Float.parseFloat (purPriTextView.getText().toString().replace(",",""));
        }

        if(sellPriTextView.getText().toString().equals("")){
            oldSellPri = 0;
        }else{
            oldSellPri = Float.parseFloat (sellPriTextView.getText().toString().replace(",",""));
        }

        if(purPriEditText.getText().toString().equals("")){
            newPurPri = 0;
        }else{
            newPurPri = Float.parseFloat (purPriEditText.getText().toString().replace(",",""));
        }

        if(sellPriEditText.getText().toString().equals("")){
            newSellPri = 0;
        }else{
            newSellPri = Float.parseFloat (sellPriEditText.getText().toString().replace(",",""));
        }

        if(newPurPri == 0){
            newPurPri = oldPurPri;
        }

        if(newSellPri == 0){
            newSellPri = oldSellPri;
        }

        if(oldPurPri != newPurPri){
            purChk = true;
        }

        if(oldSellPri != newSellPri){
            sellChk = true;
        }

        // 매입가나 판매가가 변도이 있을 시
        if((purChk == true) || (sellChk == true)){

            //----------------------------------------//
            //  바코드4자리(부문코드) 또는 저울상품인 경우 여기만 처리한다
            //----------------------------------------//
            if((barcodeTextView.getText().length() == 4) || (mScaleUse.equals("1")) ){

                // 부문이나 전자저울 상품은 여기만 업데이트한다
                query = "";
                query = "Update Goods Set ";
                query += " Edit_Check = '1', ";
                query += " Tran_Chk = '1', ";
                query += " Edit_Date = '" + currentDate + "', ";
                query += " Editor = '" + userID + "' ";
                query += " WHERE Barcode = '" + barcodeTextView.getText().toString() + "';";

                query += " UPDATE HOT_KEY SET ";
                query += " H_SellPri = " + String.valueOf(newSellPri) + " ";
                query += " WHERE H_Barcode = '" + barcodeTextView.getText().toString() + "';";

            }else{
                // 매입가 부가세 구분
                if(taxYn == "1"){
                    purCost = Float.parseFloat ( String.valueOf( newPurPri/1.1));
                    addTax = newPurPri - purCost;
                }else{
                    purCost = newPurPri;
                    addTax = 0;
                }

                // 판매가 원단위 절사
                newSellPri =  Math.round(newSellPri / 10) * 10;

                // 이익율 계산
                if((newPurPri == 0) || (newSellPri == 0)){
                    profitRate = 0;
                }else{
                    profitRate = (newSellPri - newPurPri) / newSellPri * 100;
                }

                if(Double.isInfinite(profitRate) || Double.isNaN(profitRate)){
                    profitRateTextView.setText("0");
                    Log.i("계산 오류", profitRate + "");
                    return;
                }

                profitRateTextView.setText(String.format("%.2f",profitRate));

                // 상품마스타, 히스토리 마스타 업데이트
                query = "";
                query = "Update Goods Set ";
                if(purChk == true){
                    query += " Pur_Pri = " + String.valueOf(newPurPri) + ", ";
                    query += " Pur_Cost = " + String.valueOf(purCost) + ", ";
                    query += " Add_Tax = " + String.valueOf(addTax) + ", ";
                    query += " Profit_Rate_APri = CASE WHEN Sell_APri = 0 THEN 0 ELSE ROUND(((Sell_APri - " + String.valueOf(newPurPri) + ") * 100) / Sell_APri, 2) END, ";
                    query += " Profit_Rate_BPri = CASE WHEN Sell_BPri = 0 THEN 0 ELSE ROUND(((Sell_BPri - " + String.valueOf(newPurPri) + ") * 100) / Sell_BPri, 2) END, ";
                    query += " Profit_Rate_CPri = CASE WHEN Sell_CPri = 0 THEN 0 ELSE ROUND(((Sell_CPri - " + String.valueOf(newPurPri) + ") * 100) / Sell_CPri, 2) END,";

                }

                if(sellChk == true){
                    query += " Sell_Pri = " + String.valueOf(newSellPri) + ", ";
                    query += " ReSell_Pri = " + String.valueOf(oldSellPri) + ", ";
                }

                query += " Profit_Rate = " + profitRate + ", ";
                query += " Edit_Check = '1', ";
                query += " Tran_Chk = '1', ";
                query += " Edit_Date = '" + currentDate + "', ";
                query += " Editor = '" + userID + "' ";
                query += " WHERE Barcode = '" + barcodeTextView.getText().toString() + "';";

                // 가격변경이력 저장 P_Gubun = 5
                query += " INSERT ProductPrice_History (";
                query += " P_Day,";
                query += " P_Time,";
                query += " P_Barcode,";
                query += " P_OldDanga,";
                query += " P_NewDanga,";
                query += " P_OldPrice,";
                query += " P_NewPrice,";
                query += " P_Gubun,";
                query += " P_PosNO,";
                query += " P_UserID ";
                query += " ) VALUES ( ";
                query += " '" + currentDate + "', ";
                query += " '" + currentTime + "', ";
                query += " '" + barcodeTextView.getText().toString() + "', ";
                query += " " + String.valueOf(oldPurPri) + ", ";
                query += " " + String.valueOf(newPurPri) + ", ";
                query += " " + String.valueOf(oldSellPri) + ", ";
                query += " " + String.valueOf(newSellPri) + ",";
                query += " '5', ";
                query += " '00', ";
                query += " '" + userID + "');";
            }




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
                    String str = "가격변경 완료";
                    Toast.makeText(HotKeyRegistActivity.this,str,Toast.LENGTH_SHORT).show();
                    //----------------------------------------//
                    // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
                    //----------------------------------------//
                    AlertDialog.Builder builder = new AlertDialog.Builder(HotKeyRegistActivity.this,AlertDialog.THEME_HOLO_LIGHT);
                    builder.setTitle("알림");
                    builder.setMessage("상품가격이 정상적으로 변경되었습니다.");
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
                    String str = "가격변경 실패\r\n"+msg;
                    Toast.makeText(HotKeyRegistActivity.this,str,Toast.LENGTH_SHORT).show();
                }
            }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);




        }


    }

}