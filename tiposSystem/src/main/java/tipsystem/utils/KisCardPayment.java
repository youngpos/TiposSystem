package tipsystem.utils;

import android.app.Activity;
import android.os.Bundle;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;

import kr.co.tipos.tips.R;

public class KisCardPayment extends Activity {

    private String kisMobileAction = "com.kismobile.pay";
    private String TAG = "KIS_AppToApp_Sample";

    private EditText inputBizNo, inputSerialNo, inputDownPasswordNo, inputAmount, inputAuthNo, inputLocalNo;

    private String bizNo;
    private String serialNo;
    private String downPasswordNo;
    private String localNo;
    private int amount;

    private String callType = "";
    private String callMode = "";

    //System
    TIPS_Config tips;
    Context mContext;
    UserPublicUtils upu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kis_card_payment);

        mContext = this;
        tips = new TIPS_Config(mContext);
        upu = new UserPublicUtils(mContext);

        init();
    }


    @Override
    protected void onDestroy() {
        //NOTE : 결제 결과를 받을 브로드 캐스트 등록 해제
        unregisterReceiver(broadcastReceiver);
        super.onDestroy();
    }

    private void init() {
        //NOTE : 결제 결과를 받을 브로드 캐스트 등록
        registerReceiver(broadcastReceiver, new IntentFilter(Intent.ACTION_MAIN));

        inputBizNo = (EditText) findViewById(R.id.editText);
        inputDownPasswordNo = (EditText) findViewById(R.id.editText2);
        inputSerialNo = (EditText) findViewById(R.id.editText3);
        inputLocalNo = (EditText)findViewById(R.id.editText_localnum);
        inputAmount = (EditText) findViewById(R.id.editText5);
        inputAuthNo = (EditText) findViewById(R.id.editText4);

        HashMap<String, String> map = tips.getCardInfo();

        inputBizNo.setText(map.get("bizNo"));
        inputDownPasswordNo.setText(map.get("downPasswordNo"));
        inputSerialNo.setText(map.get("serialNo"));
        inputLocalNo.setText(map.get("localNo"));

    }

    private boolean setParams() {
        if(inputBizNo.getText().length() > 0 && inputDownPasswordNo.getText().length() > 0 && inputSerialNo.getText().length() > 0 && inputAmount.getText().length() > 0 && inputLocalNo.getText().length() > 0) {
            bizNo = inputBizNo.getText().toString();
            serialNo = inputSerialNo.getText().toString();
            downPasswordNo = inputDownPasswordNo.getText().toString();
            amount = Integer.parseInt(inputAmount.getText().toString());
            localNo = inputLocalNo.getText().toString();

            return true;
        } else {
            Toast.makeText(this, "사업자번호, 다운로드 비밀번호, 단말기번호 중 공백이 있습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private boolean getParams() {
        if(inputBizNo.getText().length() > 0 && inputDownPasswordNo.getText().length() > 0 && inputSerialNo.getText().length() > 0 && inputLocalNo.getText().length() > 0) {
            bizNo = inputBizNo.getText().toString();
            serialNo = inputSerialNo.getText().toString();
            downPasswordNo = inputDownPasswordNo.getText().toString();
            localNo = inputLocalNo.getText().toString();

            return true;
        } else {
            Toast.makeText(this, "사업자번호, 다운로드 비밀번호, 단말기번호, 지역번호 중 공백이 있습니다.", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void alert(final String str,final DialogInterface.OnClickListener okListener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
        alert.setPositiveButton("확인", okListener);
        alert.setCancelable(false);
        alert.setMessage(str);
        alert.show();
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

        @SuppressWarnings("unchecked")
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.e(TAG,"Broadcast Receiving....................."+intent.hashCode()+"\n"+intent.toUri(0));

            try{
                HashMap<String, String> response = (HashMap<String, String>)intent.getSerializableExtra("result");
                Iterator<String> iterator = response.keySet().iterator();
                while (iterator.hasNext()) {
                    String key = (String) iterator.next();
                    String val = response.get(key);
                    if (val != null) val = val.trim();
                    Log.e(TAG, "RECEIVER key = " + key + ",   value = "+val);
                }

                Log.e(TAG,response.toString());

                String code = response.get("outReplyCode");
                if (code != null && code.equals("0000") && callMode.equalsIgnoreCase("pay")) {

                    // 취소 정보가 있을 시 취소
                    final String approvalNo = response.get("outAuthNo");
                    final String approvalDate = response.get("outAuthDate");
                    alert("승인번호 ["+approvalNo+"]의 결제를 취소 합니다",new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                            callMode = "cancel";
                            Intent intent = new Intent(kisMobileAction);
                            intent.putExtra("uuid",String.valueOf(System.currentTimeMillis()));
                            intent.putExtra("bizNo",bizNo);
                            intent.putExtra("serialNo",serialNo);
                            intent.putExtra("downPasswordNo",downPasswordNo);
                            intent.putExtra("type",callType);
                            intent.putExtra("mode",callMode);
                            intent.putExtra("approvalNo",approvalNo);
                            intent.putExtra("approvalDate",approvalDate);

                            startActivity(intent);
                        }
                    });
                }
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    //카드 결제
    public void clickCard(View v) {
        callType = "card";
        callMode = "pay";
        if(setParams()) {

            Intent intent = new Intent(kisMobileAction);
            intent.putExtra("uuid",String.valueOf(System.currentTimeMillis()));
            intent.putExtra("bizNo",bizNo);
            intent.putExtra("serialNo",serialNo);
            intent.putExtra("downPasswordNo",downPasswordNo);
            intent.putExtra("type",callType);
            intent.putExtra("mode",callMode);
            intent.putExtra("amount",amount);
            intent.putExtra("taxFree",0);
            intent.putExtra("fee",0);
            intent.putExtra("installment",0);

            startActivity(intent);
        }

    }

    // 포인트 결제
    public void clickPoint(View v) {
        callType = "point";
        callMode = "pay";
        if(setParams()) {

            CharSequence colors[] = new CharSequence[] {"OK캐시백","SKT멤버십","KT멤버십","LGU+멤버십"};

            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, android.R.style.Theme_Dialog));
            builder.setTitle("포인트/멤버십 선택");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    String pointType = "ok";

                    switch(which) {
                        case 0 : pointType="ok";break;
                        case 1 : pointType="skt";break;
                        case 2 : pointType="kt";break;
                        case 3 : pointType="lgt";break;
                    }

                    Intent intent = new Intent(kisMobileAction);
                    intent.putExtra("uuid",String.valueOf(System.currentTimeMillis()));
                    intent.putExtra("bizNo",bizNo);
                    intent.putExtra("serialNo",serialNo);
                    intent.putExtra("downPasswordNo",downPasswordNo);
                    intent.putExtra("type",callType);
                    intent.putExtra("mode",callMode);
                    intent.putExtra("point",amount);
                    intent.putExtra("pointType",pointType);

                    startActivity(intent);

                }
            });
            builder.show();
        }

    }

    // 현금 영수증
    public void clickCash(View v) {
        callType = "cash";
        callMode = "pay";
        if(setParams()) {
            Intent intent = new Intent(kisMobileAction);
            intent.putExtra("uuid",String.valueOf(System.currentTimeMillis()));
            intent.putExtra("bizNo",bizNo);
            intent.putExtra("serialNo",serialNo);
            intent.putExtra("downPasswordNo",downPasswordNo);
            intent.putExtra("type",callType);
            intent.putExtra("mode",callMode);
            intent.putExtra("amount",1004);
            intent.putExtra("taxFree",0);
            intent.putExtra("fee",0);
            intent.putExtra("cashReceiptType","personal");
            intent.putExtra("cashReceiptNo","01011111111");

            startActivity(intent);
        }

    }

    // 취소 (승인번호 직접 입력)
    public void clickCancel(View v) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd", Locale.KOREA);
        callType = "card";
        callMode = "cancel";
        if(setParams()) {
            String approvalNo;
            String approvalDate = sdf.format(new Date());

            if(inputAuthNo.getText().length() > 0) {
                approvalNo = inputAuthNo.getText().toString();
            } else {
                Toast.makeText(this, "승인번호 입력", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(kisMobileAction);

            intent.putExtra("uuid",""+System.currentTimeMillis());
            intent.putExtra("bizNo",bizNo);
            intent.putExtra("serialNo",serialNo);
            intent.putExtra("downPasswordNo",downPasswordNo);
            intent.putExtra("type",callType);
            intent.putExtra("mode",callMode);
            intent.putExtra("approvalNo",approvalNo);
            intent.putExtra("approvalDate",approvalDate);
            intent.putExtra("amount", amount);
            intent.putExtra("taxFree",0);
            intent.putExtra("fee",0);
            intent.putExtra("installment",0);

            startActivity(intent);
        }

    }

    public void info_save(View view){
        if(getParams()) {
            if(tips.setCardInfo(bizNo, serialNo, downPasswordNo, localNo)){
                upu.showDialog("정상 등록 완료", 0);
                return;
            }else{
                upu.showDialog("등록 실패", 0);
            }
        }
    }

}

