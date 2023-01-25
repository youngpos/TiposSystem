package tipsystem.tips;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.sewoo.jpos.command.CPCLConst;
import com.sewoo.jpos.printer.CPCLPrinter;
import com.sewoo.port.android.BluetoothPort;

import java.io.IOException;

import kr.co.tipos.tips.R;
import tipsystem.utils.BarcodePrintLKP30II;
import tipsystem.utils.BluetoothConnectMenu;

public class MobilePinterTestActivity extends Activity {
    Context mContext;

    // 프린터 설정 버튼 정의
    private Button printerSetting;

    // 프린트 전송  버튼 정의
    private Button printerSend;

    // 프린터 연걸 정보
    private TextView printerState;

    //프린터 사용유무
    private int checkPrinter;    //매장프린터 사용유무

    //세우테크 무선 프린터 출력
    private BluetoothConnectMenu blueConnect;
    private BluetoothPort bluetoothPort;

    // 2021.01.05.김영목. 바코드프린터 설정값 새로 추가
    String barcodePrinterName; //매장프린터 명
    String barcodePrinterAddress; //매장프린터 주소
    String barcodePrinterPort;    //매장프린터 포트
    String posID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_pinter_test);

        mContext = this;
        barcodePrinterName = "LK-P21";

        // 블루투스 연결 버튼 레이아웃 정의
        printerSetting = (Button) findViewById(R.id.button_printersetting);

        // 텍스트뷰 레이아웃 연결
        printerState = (TextView) findViewById(R.id.textview_printstate);
        printerState.setText("");

        // 프린트 전송 버튼 레이아웃 정의
        printerSend = (Button) findViewById(R.id.button_printsend);

        // 블루투스 연결 버튼 클릭 리스너 정의
        printerSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSelectedLKP30II();
            }
        });

        printerSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        // Bluetooth 인스턴스 생성
        bluetoothPort = BluetoothPort.getInstance();
        blueConnect = new BluetoothConnectMenu(mContext, bluetoothPort);

    }
    private void showSelectedLKP30II() {

        final CharSequence[] items = {"택설정", "프린터연결", "연결종료"};
        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        //AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
        //----------------------------------------//

        // 제목셋팅
        alertDialogBuilder.setTitle("옵션을 선택해 주세요");
        alertDialogBuilder.setItems(items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // 프로그램을 종료한다
                        switch (items[id].toString()) {
                            case "택설정":
                                Intent intent = new Intent(mContext, BarcodePrintLKP30II.class);
                                startActivity(intent);
                                break;
                            case "프린터연결":
                                if (!BluetoothPort.getInstance().isConnected()) {

                                    //--------------------//
                                    // 김영목 테스트
                                    //--------------------//
                                    //dialog.dismiss();
                                    //--------------------//

                                    startBluetoothPrintSearch();
                                } else {
                                    if (!statusCheck(new CPCLPrinter()).equals("Nomal")) {
                                        try {
                                            bluetoothPort.disconnect();
                                            startBluetoothPrintSearch();
                                        } catch (IOException | InterruptedException e) {
                                            e.printStackTrace();
                                            return;
                                        }
                                    } else {
                                        Toast.makeText(mContext, "프린터가 연결되어 있습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                break;
                            case "연결종료":
                                if (BluetoothPort.getInstance().isConnected()) {
                                    try {
                                        BluetoothPort.getInstance().disconnect();
                                        printerState.setText("발행 목록 : LK-P21(연결안됨)");
                                    } catch (IOException | InterruptedException e) {
                                        e.printStackTrace();
                                        Toast.makeText(mContext, "블루투스 연결종료 실패", Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    Toast.makeText(mContext, "프린터가 연결되어있지 않습니다.", Toast.LENGTH_SHORT).show();
                                    printerState.setText("발행 목록 : LK-P21(연결안됨)");
                                }
                                break;
                        }
                        dialog.dismiss();
                    }
                });

        // 다이얼로그 생성
        AlertDialog alertDialog = alertDialogBuilder.create();

        // 다이얼로그 보여주기
        alertDialog.show();

    }

    //세우테크 프린터
    public String statusCheck(CPCLPrinter cpclPrinter) {
        String result = "";
        if (!(cpclPrinter.printerCheck() < 0)) {
            int sts = cpclPrinter.status();
            if (sts == CPCLConst.LK_STS_CPCL_NORMAL)
                return "Normal";
            if ((sts & CPCLConst.LK_STS_CPCL_BUSY) > 0)
                result = result + "Busy\r\n";
            if ((sts & CPCLConst.LK_STS_CPCL_PAPER_EMPTY) > 0)
                result = result + "Paper empty\r\n";
            if ((sts & CPCLConst.LK_STS_CPCL_COVER_OPEN) > 0)
                result = result + "Cover open\r\n";
            if ((sts & CPCLConst.LK_STS_CPCL_BATTERY_LOW) > 0)
                result = result + "Battery low\r\n";
            //result = result + cpclPrinter.voltage()+ "\r\n";
            //result = result + cpclPrinter.temperature();
        } else {
            result = "Check the printer\r\nNo response";
            printerState.setText("발행 목록 : LK-P21(연결안됨)");
        }

        Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        return result;
    }

    private void startBluetoothPrintSearch() {
        //Intent intent = new Intent(mContext, BluetoothConnectMenu.class);
        //startActivity(intent);
        blueConnect.setStart(printerState);
    }

}