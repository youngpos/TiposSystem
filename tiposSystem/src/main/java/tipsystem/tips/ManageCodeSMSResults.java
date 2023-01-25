package tipsystem.tips;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;

import kr.co.tipos.tips.R;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Toast;

public class ManageCodeSMSResults extends Activity implements DatePickerDialog.OnDateSetListener {

    JSONObject m_shop;
    JSONObject m_userProfile;

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

    String m_OfficeCode = "";
    String posID = "";

    //날자 선택 모음
    DatePicker m_datePicker;

    SimpleDateFormat m_dateFormatter;
    Calendar m_dateCalender1;
    Calendar m_dateCalender2;
    NumberFormat m_numberFormat;
    Button m_period;

    //입력 항목
    EditText m_edittextInput;
    Spinner m_searchGubun;
    Spinner m_smsResults;
    Button m_reSend;

    //전역변수 선언
    String m_inputGubun;
    String m_resultsGubun;

    //검색 조회 버튼
    Button m_buttoSearch;

    //라디오버튼 그룹
    int mSelectedPosition = 0;
    public RadioGroup m_rgShop;

    // loading bar
    private ProgressDialog dialog;

    //전송결과 리스트 목록
    SmsListAdapter adapter;
    ListView m_listSMSresults;
    List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_code_smsresults);

        m_shop = LocalStorage.getJSONObject(this, "currentShopData");
        m_userProfile = LocalStorage.getJSONObject(this, "userProfile");

        try {
            m_ip = m_shop.getString("SHOP_IP");
            m_port = m_shop.getString("SHOP_PORT");
            //----------------------------------------//
            // 2021.12.21. 매장DB IP,PW,DB 추가
            //----------------------------------------//
            m_uuid = m_shop.getString("shop_uuid");
            m_uupw = m_shop.getString("shop_uupass");
            m_uudb = m_shop.getString("shop_uudb");
            //----------------------------------------//

            m_OfficeCode = m_shop.getString("OFFICE_CODE");
            posID = LocalStorage.getString(this, "currentPosID:" + m_OfficeCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        //달력 날자 선언
        m_period = (Button) findViewById(R.id.buttonSetDate);
        onClickDateReSet();

        m_edittextInput = (EditText) findViewById(R.id.editText_input);
        m_searchGubun = (Spinner) findViewById(R.id.spinner_sms_gubun);
        m_smsResults = (Spinner) findViewById(R.id.spinner_sms_results);
        m_reSend = (Button) findViewById(R.id.button_resend);
        m_listSMSresults = (ListView) findViewById(R.id.listView_smsresultList);

        m_buttoSearch = (Button) findViewById(R.id.button_search);
        m_buttoSearch.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                smsSendSearch();
            }
        });

        //첫번째 조회구분 스피너설정
        m_searchGubun.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                switch (arg2) {
                    case 0: //전체
                        m_inputGubun = "";
                        break;
                    case 1:
                        m_inputGubun = " AND SMS_Num Like ";
                        break;
                    case 2:
                        m_inputGubun = " AND Pos_No Like ";
                        break;
                    case 3:
                        m_inputGubun = " AND CallBack Like ";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        //두번째 조회구분 스피너설정
        m_smsResults.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {
                // TODO Auto-generated method stub
                switch (arg2) {
                    case 0: //전체
                        m_resultsGubun = "";
                        break;
                    case 1:
                        m_resultsGubun = " AND Results='0' ";
                        break;
                    case 2:
                        m_resultsGubun = " AND Results='1' ";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        //라디오버튼 그룹
        m_rgShop = new RadioGroup(this);

        adapter = new SmsListAdapter(this, mfillMaps);
        m_listSMSresults.setAdapter(adapter);

        m_listSMSresults.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // TODO Auto-generated method stub
                Intent intent = new Intent(ManageCodeSMSResults.this, ManageCodeSMSResultsListActivity.class);

                HashMap<String, String> map = mfillMaps.get(arg2);

                TextView tv1 = (TextView) arg1.findViewById(R.id.item1);
                TextView tv2 = (TextView) arg1.findViewById(R.id.item3);
                intent.putExtra("SMS_Count", map.get("SMS_Count"));
                intent.putExtra("Pos_No", map.get("Pos_No"));
                intent.putExtra("Msg", map.get("Msg"));
                intent.putExtra("SMS_Num", tv1.getText().toString());
                intent.putExtra("CallBack", tv2.getText().toString());

                startActivity(intent);
            }
        });


        smsSendSearch();
        //onCreate 마지막 부분
    }


    //날자 리셋 하기 함수
    private void onClickDateReSet() {
        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        m_numberFormat = NumberFormat.getInstance();
        m_dateCalender1 = Calendar.getInstance();
        m_dateCalender2 = Calendar.getInstance();

        m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

    }

    public void onClickSetDate(View view) {
        DatePickerDialog newDlg = new DatePickerDialog(this, this,
                m_dateCalender1.get(Calendar.YEAR),
                m_dateCalender1.get(Calendar.MONTH),
                m_dateCalender1.get(Calendar.DAY_OF_MONTH));
        newDlg.show();
    }

    public void onClickSetDatePrevious(View view) {
        m_dateCalender1.add(Calendar.DAY_OF_MONTH, -1);
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);
        m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

        smsSendSearch();
    }

    public void onClickSetDateNext(View view) {
        m_dateCalender1.add(Calendar.DAY_OF_MONTH, 1);
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, 1);
        m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

        smsSendSearch();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear,
                          int dayOfMonth) {
        // TODO Auto-generated method stub
        m_dateCalender1.set(year, monthOfYear, dayOfMonth);
        m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        m_dateCalender2.set(year, monthOfYear, dayOfMonth);
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);
    }

    //새로입력
    public void onClickReNew(View v) {
        mfillMaps.removeAll(mfillMaps);
        adapter.notifyDataSetChanged();
        onClickDateReSet();
        m_edittextInput.setText("");
        m_searchGubun.setSelection(0);
        m_smsResults.setSelection(0);
        m_inputGubun = "";
        m_resultsGubun = "";
    }

    //검색 함수
    private void smsSendSearch() {
        mfillMaps.removeAll(mfillMaps);
        String query = "";
        String inpugubun = "";
        String smsresults = "";

        String period = m_period.getText().toString();
        if (0 == m_searchGubun.getSelectedItemPosition()) {
            inpugubun = " ";
        } else {
            inpugubun = m_inputGubun + "'%" + m_edittextInput.getText() + "%' ";
        }

        if (0 == m_smsResults.getSelectedItemPosition()) {
            smsresults = " ";
        } else {
            smsresults = m_resultsGubun;
        }

        query = "Select * From hphone_sms_send Where CONVERT(char(10), date, 23)='" + period + "' "
                + inpugubun + smsresults;

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.setCancelable(false);
        dialog.show();

        // 콜백함수와 함께 실행
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                if (results.length() > 0) {
                    updateSMSsend(results);
                } else {
                    Toast.makeText(getApplicationContext(), "조회 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "회원정보를 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    private void updateSMSsend(JSONArray results) {

        try {

            for (int i = 0; i < results.length(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                JSONObject son = results.getJSONObject(i);
                map = JsonHelper.toStringHashMap(son);
                map.put("Date", map.get("Date").substring(0, 16));
                map.put("Results", resetResultsString(map.get("Results")));
                mfillMaps.add(map);
            }
            adapter.notifyDataSetChanged();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private String resetResultsString(String result) {
        String results;
        if (result.equals("0")) {
            results = "전송중";
        } else {
            results = "전송완료";
        }
        return results;
    }


    class SmsListAdapter extends BaseAdapter {
        Context ctx;

        private List<HashMap<String, String>> object;

        public SmsListAdapter(Context ctx, List<HashMap<String, String>> object) {
            super();
            this.object = object;
            this.ctx = ctx;
        }

        @Override
        public int getCount() {
            return object.size();
        }

        @Override
        public Object getItem(int position) {
            return m_listSMSresults.getItemAtPosition(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewSmsHolder holder;
            if (convertView == null) {

                LayoutInflater inflater = LayoutInflater.from(ManageCodeSMSResults.this);
                convertView = inflater.inflate(R.layout.activity_listview_item_smsresultlist, parent, false);
                holder = new ViewSmsHolder(ctx);
                holder.radioShop = (RadioButton) convertView.findViewById(R.id.radioButtonSms);

                //holder.radioShop.setBackgroundResource(R.drawable.check_icon);
                holder.txtSMSnum = (TextView) convertView.findViewById(R.id.item1);
                holder.txtphone = (TextView) convertView.findViewById(R.id.item2);
                holder.txtCallback = (TextView) convertView.findViewById(R.id.item3);
                holder.txtDatetime = (TextView) convertView.findViewById(R.id.item4);
                holder.txtSmsCount = (TextView) convertView.findViewById(R.id.item5);
                holder.txtResults = (TextView) convertView.findViewById(R.id.item6);

                convertView.setTag(holder);
            } else {
                holder = (ViewSmsHolder) convertView.getTag();
            }

            HashMap<String, String> map = new HashMap<String, String>();
            map = object.get(position);
            //{"SMS_Num", "Pos_No", "CallBack", "Date", "SMS_Count", "Results"};
            String SMS_Num = map.get("SMS_Num");
            String Pos_No = map.get("Pos_No");
            String CallBack = map.get("CallBack");
            String Date = map.get("Date");
            String SMS_Count = map.get("SMS_Count") + " | " + map.get("Success") + " / " + map.get("Fail");
            String Results = map.get("Results");

            holder.txtSMSnum.setText(SMS_Num);
            holder.txtphone.setText(Pos_No);
            holder.txtCallback.setText(CallBack);
            holder.txtDatetime.setText(Date);
            holder.txtSmsCount.setText(SMS_Count);
            holder.txtResults.setText(Results);

            holder.radioShop.setChecked(position == mSelectedPosition);
            holder.m_position = position;
            holder.radioShop.setTag(holder);
            holder.radioShop.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    ViewSmsHolder mH = (ViewSmsHolder) v.getTag();

                    Log.i("test", "Test=" + mH.m_position);
                    mSelectedPosition = mH.m_position;
                    notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }

    static class ViewSmsHolder extends Activity {
        public Context ctx;
        public RadioButton radioShop;
        public TextView txtSMSnum;
        public TextView txtphone;
        public TextView txtCallback;
        public TextView txtDatetime;
        public TextView txtSmsCount;
        public TextView txtResults;
        public int m_position;

        public ViewSmsHolder(Context ctx) {
            this.ctx = ctx;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_customer, menu);
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


}