package tipsystem.tips;

import static java.lang.Thread.sleep;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import kr.co.tipos.tips.R;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;
import tipsystem.utils.TIPOS;

public class ShopRebootActivity extends Activity {

    // Context
    private Context mContext;
    private JSONObject m_shop;
    private String m_ip = "";
    private String m_port = "";
    private String m_Wsock_port = "9801";
    private String Office_Code = null;
    private String phoneNumber="";
    // loading bar
    private ProgressDialog dialog;

    //리스트 컨트롤 정의
    private ListView listShop;
    // 어댑터 정의
    private CustomAdapter adapter;
    private List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

    private int mListPosition = -1;
    private String mCommand = "";
    private String mWork = "connect"; // reboot

//    private Socket socket;
//    private DataOutputStream dataOutputStream;
//    private PrintWriter printWriter;

    private PosWsockAsyncTask posWsock;
    private PrintWriter output;
    private BufferedReader input;

    private TextView m_log;
    private Button buttonRefresh;

    private ProgressDialog progressDialog;

    private PrintWriter out;
    private Socket socket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_reboot);

        mContext=this;
        m_shop = LocalStorage.getJSONObject(this, "currentShopData");
        try {
            m_ip = m_shop.getString("SHOP_IP");
            m_port = m_shop.getString("SHOP_PORT");
            Office_Code = m_shop.getString("OFFICE_CODE");
            phoneNumber = LocalStorage.getString(ShopRebootActivity.this, "phoneNumber");

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 리스트 컨트롤 레이아웃 정의
        listShop = (ListView) findViewById(R.id.listviewshop);

        String[] from = new String[]{"Sto_Cd", "Office_Name", "Pos_No", "Wsock_port", "Wsock_regdate","Pos_State","Pos_Reboot"};
        int[] to = new int[]{R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5,R.id.item6,R.id.buttonPosReboot} ;

        adapter = new CustomAdapter(this, mfillMaps, R.layout.activity_listview_shop_reboot_list, from, to);
        listShop.setAdapter(adapter);

        m_log=(TextView)findViewById(R.id.textViewLog);
        buttonRefresh = (Button)findViewById(R.id.buttonRefresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doQuery();
            }
        });

        doQuery();

    }

    private void doQuery() {

        mWork = "connect";

        // 대기중 시작
        progressDialog = new ProgressDialog(mContext);

        progressDialog.setMessage("매장별 조회 중 입니다...");
        progressDialog.setCancelable(false);
        progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
        progressDialog.show();

        mfillMaps.removeAll(mfillMaps);


        // 쿼리 작성하기
        String query = "";

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        Calendar c = Calendar.getInstance();
        String today = f.format(c.getTime());

        // test
        //Office_Code="0001163";
        query = "";
        query += " select ";
        query += " A.Sto_Cd AS Sto_Cd";
        query += ",B.Office_Name AS Office_Name";
        query += ",A.Pos_No AS Pos_No";
        query += ",A.Wsock_Port AS Wsock_Port";
        query += ",A.Wsock_Regdate AS Wsock_Regdate";
        query += " from Poson_Wsock_Reg as A inner join ";
        query += "(select a.OFFICE_CODE as Sto_Cd, b.Office_Name as office_name ";
        query += " from APP_USER as A inner join V_OFFICE_USER as B ";
        query += " on A.OFFICE_CODE = B.Sto_CD ";
        query += " JOIN APP_SETTLEMENT as C on A.OFFICE_CODE = C.OFFICE_CODE ";
        query += " where A.APP_HP ='" + phoneNumber + "' AND C.DEL_YN = '0' ";
        query += " AND C.APP_SDATE<='" + today + "' AND C.APP_EDATE>='" + today + "')";
        query += " as B on A.Sto_Cd = B.Sto_CD ";
        query += " order by Pos_No";

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();

        // 콜백함수와 함께 실행
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                progressDialog.dismiss();

                dialog.dismiss();
                dialog.cancel();
                if (results.length() > 0) {

                    ShopPosListView(results);
                    adapter.notifyDataSetChanged();

                    Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();

                    // 정상조회시 매장별 전체 조회 시작
                    //  일단 1번부터 1개만 하자

                    mListPosition = 0;
                    //SqlListRowCheck(mListPosition);
                    SocketCommand(mListPosition,"connect");

                } else {
                    Toast.makeText(getApplicationContext(), "매장목록을 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                progressDialog.dismiss();

                dialog.dismiss();
                dialog.cancel();
                adapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), "매장목록을 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
        }).execute(TIPOS.HOST_SERVER_IP + ":" + TIPOS.HOST_SERVER_PORT, TIPOS.HOST_SERVER_DB, TIPOS.HOST_SERVER_ID, TIPOS.HOST_SERVER_PW, query);
    }

    private void ShopPosListView(JSONArray results) {
        for (int index = 0; index < results.length(); index++) {

            try {
                JSONObject son = results.getJSONObject(index);
                HashMap<String, String> map = JsonHelper.toStringHashMap(son);

//                map.put("Sto_Cd", map.get("Sto_Cd");
//                map.put("Office_Name", map.get("Office_Name");
//                map.put("Pos_No", map.get("Pos_No");
//                map.put("Wsock_Port", map.get("Wsock_Port");
//                map.put("Wsock_Regdate", map.get("Wsock_Regdate");

                map.put("Pos_State", "OFF");
                map.put("Pos_Reboot", "");

                mfillMaps.add(map);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

//    private void SqlListRowCheck(int pos){
//
//        if (mfillMaps.size() > 0) {
//
//            HashMap<String, String> map = new HashMap<String, String>();
//
//            map.putAll(mfillMaps.get(pos));
//
//            m_Wsock_port=map.get("Wsock_Port");
//            String posno = map.get("Pos_No");
//            posWsock = new PosWsockAsyncTask();
//
//            // 대기중 시작
//            progressDialog = new ProgressDialog(mContext);
//            progressDialog.setMessage("[" + posno + "] 포스 소켓 연결중 입니다...");
//            progressDialog.setCancelable(false);
//            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
//            progressDialog.show();
//
//
//            // 접속정보등록
//            mCommand = "";
//            posWsock.execute(mCommand);
//        }
//    }

    //Socket Command 공통으로(connect, reboot)
    private void SocketCommand(int pos,String work){

        if (mfillMaps.size() > 0) {

            HashMap<String, String> map = new HashMap<String, String>();

            map.putAll(mfillMaps.get(pos));

            m_Wsock_port=map.get("Wsock_Port");
            String posno = map.get("Pos_No");
            posWsock = new PosWsockAsyncTask();

            // 대기중 시작
            String msg = "";
            if (work=="reboot"){
                msg="재부팅 전송중입니다...";
            }else{
                msg="소켓 연결중입니다...";
            }
            progressDialog = new ProgressDialog(mContext);
            progressDialog.setMessage("[" + posno + "] 포스" + msg);
            progressDialog.setCancelable(false);
            progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
            progressDialog.show();

            // 접속정보등록
            mCommand = "";

            if (work == "reboot"){
                // 재부팅
                mCommand += "\u0002";
                mCommand += "ADD";
                mCommand += "|";
                mCommand += phoneNumber;
                mCommand += "\u0003";
            }
            posWsock.execute(mCommand);
        }


//        String errorCheck= "";
//
//        HashMap<String, String> map = new HashMap<String, String>();
//        map.putAll(mfillMaps.get(mListPosition));
//
//        errorCheck = map.get("Pos_State");
//
//        while (errorCheck == "OFF"){
//
//            mListPosition++;
//            if (mListPosition == mfillMaps.size()){
//                break;
//            }
//            map.putAll(mfillMaps.get(mListPosition));
//            errorCheck = map.get("Pos_State");
//        }
//
//        if (mListPosition < mfillMaps.size()) {
//            SqlListRow();
//        }

    }

    private class CustomAdapter extends SimpleAdapter {

        private List<? extends Map<String, ?>> item;
        private HashMap<String, String> temp;

        private int[] mTo;
        private String[] mFrom;
        private ViewBinder mViewBinder;

        private List<? extends Map<String, ?>> mData;

        private int mResource;

        private LayoutInflater mInflater;


        public CustomAdapter (Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
            super(context, data, resource, from, to);
            this.item = data;

            mData = data;
            mFrom = from;
            mTo = to;

            mResource = resource;
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {

            View v = convertView;
            if (v == null) {
                LayoutInflater vi = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = vi.inflate(R.layout.activity_listview_shop_reboot_list, null);
            }

            temp = (HashMap<String, String>)item.get(position);

            return createViewFromResource(mInflater, position, convertView, parent, mResource);
        }


        private View createViewFromResource(LayoutInflater inflater, int position, View convertView,
                                            ViewGroup parent, int resource) {
            View v;
            if (convertView == null) {
                v = inflater.inflate(resource, parent, false);
            } else {
                v = convertView;
            }

            bindView(position, v);

            return v;
        }

        private void bindView(int position, View view) {
            final Map dataSet = mData.get(position);
            final int pos = position;
            if (dataSet == null) {
                return;
            }

            final ViewBinder binder = mViewBinder;
            final String[] from = mFrom;
            final int[] to = mTo;
            final int count = to.length;

            Button buttonPosReboot = (Button)view.findViewById(R.id.buttonPosReboot);

            // 여기서 버튼 글자 바꿔보자
//            HashMap<String, String> map = new HashMap<String, String>();
//            map.putAll(mfillMaps.get(pos));
//            String posState=map.get("Pos_State");
//
//            if (posState=="OFF"){
//                buttonPosReboot.setText("B");
//            }

            // listview 에서 버튼 클릭시 이벤트
            buttonPosReboot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //viewDetail(pos);
                    //여기서 소켓으로 재부팅 시도
                    mWork="reboot";

                    HashMap<String, String> map = new HashMap<String, String>();

                    map.putAll(mfillMaps.get(pos));
                    String posState=map.get("Pos_State");

                    if (posState=="OFF"){
                        //Toast.makeText(getApplicationContext(), "OFF인 경우는 재부팅 할 수 없습니다!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    mListPosition=pos;
                    String posno=map.get("Pos_No");

                    //확인 창 추가하자
                    // 한번은 물어보자
                    AlertDialog.Builder alert_builder = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
                    alert_builder.setMessage("[" + posno + "] 포스를 재부팅하시겠습니까?")
                            .setCancelable(false)
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    SocketCommand(mListPosition,"reboot");
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialog.cancel();
                                }
                            });
                    AlertDialog alert = alert_builder.create();
                    alert.setTitle("POS 재부팅");
                    alert.show();

//                    progressDialog = new ProgressDialog(mContext);
//
//                    progressDialog.setMessage("[" + posno + "] 포스 재부팅 전문 전송중 입니다...");
//                    progressDialog.setCancelable(false);
//                    progressDialog.setProgressStyle(android.R.style.Widget_ProgressBar_Horizontal);
//                    progressDialog.show();
//
//                    m_Wsock_port=map.get("Wsock_Port");
//                    posWsock = new PosWsockAsyncTask();
//
//                        // 재부팅
//                    mCommand = "";
//                    mCommand += "\u0002";
//                    mCommand += "ADD";
//                    mCommand += "|";
//                    mCommand += phoneNumber;
//                    mCommand += "\u0003";
//                    posWsock.execute(mCommand);

                }
            });

            for (int i = 0; i < count; i++) {
                final View v = view.findViewById(to[i]);
                if (v != null) {
                    final Object data = dataSet.get(from[i]);
                    String text = data == null ? "" : data.toString();
                    if (text == null) {
                        text = "";
                    }

                    boolean bound = false;
                    if (binder != null) {
                        bound = binder.setViewValue(v, data, text);
                    }

                    if (!bound) {
                        if (v instanceof Checkable) {

                            if ( Boolean.valueOf(text) instanceof Boolean) {
                                ((Checkable) v).setChecked(Boolean.valueOf(text));

                            } else if (v instanceof TextView) {
                                // Note: keep the instanceof TextView check at the bottom of these
                                // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                                setViewText((TextView) v, text);

                            } else {
                                throw new IllegalStateException(v.getClass().getName() +
                                        " should be bound to a Boolean, not a " +
                                        (data == null ? "<unknown type>" : data.getClass()));
                            }
                        } else if (v instanceof TextView) {
                            // Note: keep the instanceof TextView check at the bottom of these
                            // ifs since a lot of views are TextViews (e.g. CheckBoxes).
                            setViewText((TextView) v, text);

                        } else if (v instanceof ImageView) {
                            if (data instanceof Integer) {
                                setViewImage((ImageView) v, (Integer) data);
                            } else {
                                setViewImage((ImageView) v, text);
                            }
                        } else {
                            throw new IllegalStateException(v.getClass().getName() + " is not a " +
                                    " view that can be bounds by this SimpleAdapter");
                        }
                    }
                }
            }
        }

        public void setTextColor(TextView v, String text, int color) {
            v.setTextColor(color);
        }

    }

    //소켓통신으로 전송하기
    private class PosWsockAsyncTask extends AsyncTask<String, Integer, Boolean> {

        private int count_num = 0;
        private int connect = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Toast.makeText(getApplicationContext(), "시작", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            // TODO Auto-generated method stub

            // 2021.01.05.김영목. 포트 동적으로 수정
            // 2021.01.05.김영목. 포트 동적으로 수정
            //SocketAddress socketAddress = new InetSocketAddress(m_barcodePrinter, 8671);
            //SocketAddress socketAddress = new InetSocketAddress(m_barcodePrinter, port);

            int port = Integer.parseInt(m_Wsock_port);
            SocketAddress socketAddress = new InetSocketAddress(m_ip, port);

            socket = new Socket();
            try {
                //socket = new Socket("192.168.10.156", 8671);
                //socket = new Socket(m_barcodePrinter, 8671);

                socket.setSoTimeout(5000);
                try {
                    socket.connect(socketAddress, 5000);
                    connect=1;

                } catch (SocketTimeoutException e) {
                    return false;
                }

                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "MS949")), true);
                HashMap<String, String> map = new HashMap<String, String>();

                try {
                    sleep(300);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                out.println(mCommand.toString());
                //out.flush();

                if(mCommand.contains("ADD")) {
                    // 전화번호 전송시 끊으면 안된다.???
                    mCommand="";
                    mCommand += "\u0002";
                    mCommand += "BOT";
                    mCommand += "|";
                    mCommand += "0";
                    mCommand += "\u0003";

                    out.println(mCommand.toString());
                    out.flush();
                }

            } catch (SocketException e) {
                e.printStackTrace();
                return false;
            } catch (SocketTimeoutException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                progressDialog.dismiss();

            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            // 성공시
            if (result) {

                String posno="";

                HashMap<String, String> map = mfillMaps.get(mListPosition);
                posno=map.get("Pos_No");

                if (mWork=="connect"){
                    map.put("Pos_State", "ON");
                    map.put("Pos_Reboot", "재부팅");
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "[" + posno + "]" + " POS 연결에 성공하였습니다!", Toast.LENGTH_LONG).show();

                }else if (mWork=="reboot"){
                    map.put("Pos_State", "OFF");
                    map.put("Pos_Reboot", "");
                    adapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), "[" + posno + "]" + " POS 재부팅 전문을 전송하였습니다!", Toast.LENGTH_LONG).show();
                }

                // 실패시
            } else {
                Toast.makeText(getApplicationContext(), "POS 소켓연결을 확인해 주세요.", Toast.LENGTH_LONG).show();
            }

            if (mWork=="connect"){

                // 이전 포스가 실패하더라도 다음 포스 연결 처리
                //----------------------------------------//
                //마지막을 여기서 체크하여 처리하자
                //----------------------------------------//
                mListPosition ++; //1 증가

                // 마지막
                if (mListPosition < mfillMaps.size()) {
                    SocketCommand(mListPosition,"connect");
                }else{
                    //
                }
                //----------------------------------------//
            }
        }
    }



}