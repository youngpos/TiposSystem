package tipsystem.tips;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kr.co.tipos.tips.R;
import tipsystem.tips.models.ShopSelectItem;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.TIPOS;

public class RealSaleMainActivity extends Activity implements DatePickerDialog.OnDateSetListener {

    JSONObject m_shop;
    JSONObject m_userProfile;
    String m_APP_USER_GRADE;    //앱권한
    String m_OFFICE_CODE = null;  //수수료매장거래처코드
    String m_OFFICE_NAME = "";

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

    TextView m_realSales;
    TextView m_viewNumber;
    TextView m_viewKNumber;
    TextView m_viewRealSalesYesterday;
    TextView m_viewPrice;
    TextView m_viewCash;
    TextView m_viewCard;
    TextView m_viewCredit;
    TextView m_viewOther;

    // 수수료 매장에 한해 뷰보기 옵션 cardY : 다중매장보기 / All : 전체보기 / cardN : 매장보기
    String m_viewOption;

    //ListView m_listSale;

    Button m_buttonSetDate; // 일자 세트

    SimpleDateFormat m_dateFormatter;
    Calendar m_dateCalender1;
    Calendar m_dateCalender2;
    NumberFormat m_numberFormat;

    public ListView m_list;
    int mSelectedPosition = 0;
    AlertDialog m_alert;
    // loading bar
    private ProgressDialog dialog;

    TextView toolbarTitle;

    //boolean isRealSaleNew;
    boolean isRealSaleNewOnly;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realsale_main);

        //----------------------------------------//
        // Toolbar
        //----------------------------------------//
        ImageView homeIcon = findViewById(R.id.toolbar_home_icon);
        toolbarTitle = (TextView) findViewById(R.id.toolbar_title_text);
        ImageView powerIcon = findViewById(R.id.toolbar_power_off);

        homeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isRealSaleNewOnly) fetchOffices();
                else {
                    Intent intent = new Intent(RealSaleMainActivity.this, MainMenuActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); // 스택 중간 지우기
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP); // 스택 맨처음 확인
                    startActivity(intent);

//                    Intent intent = new Intent(RealSaleMainActivity.this, MainActivity.class);
//                    intent.putExtra("PreviewActivity","RealSaleMainActivity");
//                    startActivity(intent);


                }
            }
        });

        powerIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                powerOff();
            }
        });

        toolbarTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchOffices();
            }
        });

        //toolbarTitle.setText("실시간매출");
        //----------------------------------------//

        //----------------------------------------//
        // 매장 정보 정의
        //----------------------------------------//
        GetShopInfo();
        //isRealSaleNew = LocalStorage.getBoolean(RealSaleMainActivity.this, "RealSaleNew:" + m_OFFICE_CODE);
        isRealSaleNewOnly = LocalStorage.getBoolean(RealSaleMainActivity.this, "RealSaleNewOnly:" + m_OFFICE_CODE);
        if (isRealSaleNewOnly)
            powerIcon.setVisibility(View.VISIBLE);
        else powerIcon.setVisibility(View.INVISIBLE);

//        m_shop = LocalStorage.getJSONObject(this, "currentShopData");
//        m_userProfile = LocalStorage.getJSONObject(this, "userProfile");  // 2/27 수수료 매장일때 추가 내용
//
//        //매출보기 옵션 설정
//        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//        m_viewOption = pref.getString("prefSaleViewMethod", "All");
//
//        m_OFFICE_CODE = "";
//        m_OFFICE_NAME = "";
//
//        try {
//            m_ip = m_shop.getString("SHOP_IP");
//            m_port = m_shop.getString("SHOP_PORT");
//            //----------------------------------------//
//            // 2021.12.21. 매장DB IP,PW,DB 추가
//            //----------------------------------------//
//            m_uuid = m_shop.getString("shop_uuid");
//            m_uupw = m_shop.getString("shop_uupass");
//            m_uudb = m_shop.getString("shop_uudb");
//            //----------------------------------------//
//
//
//            //  2/27 수수료 매장일때 추가 내용 추가
//            m_APP_USER_GRADE = m_userProfile.getString("APP_USER_GRADE");
//            m_OFFICE_CODE = m_userProfile.getString("OFFICE_CODE");
//            m_OFFICE_NAME = m_userProfile.getString("Office_Name");
//
//            toolbarTitle.setText("m_OFFICE_NAME");
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        //----------------------------------------//

        m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        m_numberFormat = NumberFormat.getInstance();
        m_dateCalender1 = Calendar.getInstance();
        m_dateCalender2 = Calendar.getInstance();

        m_buttonSetDate = (Button) findViewById(R.id.buttonSetDate);
        m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);

        // 상단 텍스트 뷰
        m_realSales = (TextView) findViewById(R.id.textViewRealSales);
        m_viewKNumber = (TextView) findViewById(R.id.textViewKNumber);
        m_viewRealSalesYesterday = (TextView) findViewById(R.id.textViewRealSalesYesterday);
        m_viewPrice = (TextView) findViewById(R.id.textViewKPrice);
        m_viewCash = (TextView) findViewById(R.id.textViewCash);
        m_viewCard = (TextView) findViewById(R.id.textViewCard);
        m_viewCredit = (TextView) findViewById(R.id.textViewCredit);
        m_viewOther = (TextView) findViewById(R.id.textViewOther);

        queryCommonInformation();
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
        m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

        queryCommonInformation();
    }

    public void onClickSetDateNext(View view) {
        m_dateCalender1.add(Calendar.DAY_OF_MONTH, 1);
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, 1);
        m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

        queryCommonInformation();
    }

    private void queryCommonInformation() {
        String period1 = m_buttonSetDate.getText().toString();
        String period2 = m_dateFormatter.format(m_dateCalender2.getTime());
        String query = "";

        int year1 = Integer.parseInt(period1.substring(0, 4));
        int month1 = Integer.parseInt(period1.substring(5, 7));
        int year2 = Integer.parseInt(period2.substring(0, 4));
        int month2 = Integer.parseInt(period2.substring(5, 7));

        String tableName = String.format("%04d%02d", year1, month1);
        String tableName2 = String.format("%04d%02d", year2, month2);

        //2016년6월10일 앱쿠폰사용금액/수수료매장 현금영수증 사용금액별 구분 추가합니다.
        query = "Select "
                + "  ISNULL(판매액,0) '판매액', "
                + "  IsNull(반품액, 0) '반품액', "
                + "  IsNull(할인액, 0) '할인액', "
                + "  ISNULL(과세,0) '과세', "
                + "  ISNULL(면세,0) '면세', "
                + "  ISNULL(순매출,0) '순매출', "
                + "  ISNULL(현금,0) '현금', "
                + "  ISNULL(카드,0) '카드', "
                + "  ISNULL(외상,0) '외상', "
                + "  ISNULL(포인트사용,0) + "
                + "  ISNULL(상품권,0) + "
                + "  ISNULL(앱쿠폰,0) + "
                + "  ISNULL(캐쉬백사용,0) + "
                + "  ISNULL(절사금액,0) + "
                + "  ISNULL(CMS할인,0) + ISNULL(예비쿠폰1,0) + ISNULL(예비쿠폰2,0) + ISNULL(예비쿠폰3,0) + ISNULL(쿠폰할인액,0) + ISNULL(카드할인액,0) + ISNULL(에누리할인,0) '기타',  "
                + "  ISNULL(객수,0) '객수', "
                + "  ISNULL(객단가,0) '객단가',  "
                + "  ISNULL(이익금,0) '이익금',  "
                + "  ISNULL(순매출_B,0) '전일_순매출',  "
                + "  ISNULL(객수_B,0) '전일_객수',  "
                + "  ISNULL(객단가_B,0) '전일_객단가' "
                + " From (      "
                + "  Select      "
                + "  'a' a,판매액,반품액,할인액,과세,면세,순매출,      "
                + "  현금,카드,외상,CMS할인,포인트사용,에누리할인, "
                + "  상품권,앱쿠폰,예비쿠폰1,예비쿠폰2,예비쿠폰3,쿠폰할인액,카드할인액,캐쉬백사용,절사금액,  "
                + "  객수, '객단가' = CASE WHEN 순매출=0 Then 0 ELSE 순매출/객수 END,   "
                + "  이익금   "
                + "  From (  "
                + "    Select "
                + "    Sum(Case When A.Sale_Yn='1' Then A.TSell_Pri + A.Dc_Pri Else 0 End ) '판매액',          "
                + "    Sum(Case When A.Sale_Yn='0' Then A.TSell_RePri + A.Dc_Pri Else 0 End ) '반품액',          "
                + "    Sum(Case When A.Sale_Yn='1' then A.DC_Pri Else  A.DC_Pri *-1 End) '할인액',          "
                + "    Sum(Case When A.Tax_YN='1' then  A.TSell_Pri - A.TSell_RePri Else 0 End) '과세',       "
                + "    Sum(Case When A.Tax_YN='0' then  A.TSell_Pri - A.TSell_RePri Else 0 End)  '면세',        "
                + "    Sum (A.TSell_Pri - A.TSell_RePri) '순매출',          "
                + "    Sum (Round(B.Cash_Pri * A.Money_Per, 4)) '현금',       "
                + "    Sum (A.Card_Pri) '카드',          "
                + "    Sum (Round(B.Dec_Pri * A.Money_Per, 4)) '외상',          "
                + "    Sum (Round(B.CMS_Pri * A.Money_Per, 4)) 'CMS할인',         "
                + "    Sum (Round(B.Cus_PointUse * A.Money_Per, 4)) '포인트사용',   "
                + "    Sum (Round(B.Sub_Pri * A.Money_Per, 4)) '에누리할인',          "
                + "    Sum (Round(B.Gift_Pri * A.Money_Per, 4)) '상품권',          "
                + "    Sum (Round(isnull(B.APP_DCPri,0) * A.Money_Per, 4)) '앱쿠폰', "
                + "    Sum (Round(isnull(B.ETC_DCPRI1,0) * A.Money_Per, 4)) '예비쿠폰1',          "
                + "    Sum (Round(isnull(B.ETC_DCPRI2,0) * A.Money_Per, 4)) '예비쿠폰2',          "
                + "    Sum (Round(isnull(B.ETC_DCPRI3,0) * A.Money_Per, 4)) '예비쿠폰3',          "
                + "    Sum (Round(B.BC_Coupon_DC * A.Money_Per, 4)) '쿠폰할인액',          "
                + "    Sum (Round(B.BC_Card_DC * A.Money_Per, 4)) '카드할인액',          "
                + "    Sum (Round(B.CashBack_PointUse * A.Money_Per, 4)) '캐쉬백사용',     "
                + "    Sum (Round(B.Cut_Pri * A.Money_Per, 4)) '절사금액',          "
                + "    Count (Distinct(B.Sale_Num)) '객수',          "
                + "    Sum (A.ProFit_Pri) '이익금'          "
                + "    From SaD_" + tableName + " A, SaT_" + tableName + " B        "
                + "    Where A.Sale_Num=B.Sale_Num And A.Sale_Date='" + period1 + "' ";
        if (m_APP_USER_GRADE.equals("2") && !m_OFFICE_CODE.equals("") && null != m_OFFICE_CODE) {
            query += " ANd A.office_code='" + m_OFFICE_CODE + "' ";
        }
        //매출보기 옵션 설정
        if (m_viewOption.equals("cardY")) {
            query += "AND A.Card_Yn='1' ";
        } else if (m_viewOption.equals("cardN")) {
            query += "AND A.Card_Yn='0' ";
        }

        query += "    Group By A.Sale_Date      "
                + "    ) G  "
                + "  ) A FULL JOIN (      "
                + "    Select     'a' a,     순매출 '순매출_B',      "
                + "    객수 '객수_B' ,'객단가_B' = CASE WHEN 순매출=0 Then 0 ELSE 순매출/객수 END      "
                + "    From (          "
                + "      Select          "
                + "      Sum (A.TSell_Pri - A.TSell_RePri) '순매출',          "
                + "      Count (Distinct(B.Sale_Num)) '객수'  "
                + "      From SaD_" + tableName2 + " A, SaT_" + tableName2 + " B "
                + "      Where A.Sale_Num=B.Sale_Num And A.Sale_Date='" + period2 + "'          ";
        if (m_APP_USER_GRADE.equals("2") && !m_OFFICE_CODE.equals("") && null != m_OFFICE_CODE) {
            query += " ANd A.office_code='" + m_OFFICE_CODE + "' ";
        }
        //매출보기 옵션 설정
        if (m_viewOption.equals("cardY")) {
            query += "AND Card_Yn='1' ";
        } else if (m_viewOption.equals("cardN")) {
            query += "AND Card_Yn='0' ";
        }
        query += "      Group By A.Sale_Date      "
                + "    ) G  "
                + "  ) B ON A.a=B.a  ";

        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {

                updateCommonInformation(results);
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
    }

    private void updateCommonInformation(JSONArray results) {
        try {
            if (results.length() > 0) {
                for (int i = 0; i < results.length(); i++) {
                    JSONObject son = results.getJSONObject(i);
                    m_realSales.setText(m_numberFormat.format(son.getInt("순매출")));
                    m_viewKNumber.setText(m_numberFormat.format(son.getInt("객수")));
                    m_viewPrice.setText(m_numberFormat.format(son.getInt("객단가")));
                    m_viewCash.setText(m_numberFormat.format(son.getInt("현금")));
                    m_viewCard.setText(m_numberFormat.format(son.getInt("카드")));
                    m_viewCredit.setText(m_numberFormat.format(son.getInt("외상")));
                    m_viewOther.setText(m_numberFormat.format(son.getInt("기타")));
                    m_viewRealSalesYesterday.setText(m_numberFormat.format(son.getInt("전일_순매출")));
                }
            } else {
                m_realSales.setText(m_numberFormat.format(0));
                m_viewKNumber.setText(m_numberFormat.format(0));
                m_viewPrice.setText(m_numberFormat.format(0));
                m_viewCash.setText(m_numberFormat.format(0));
                m_viewCard.setText(m_numberFormat.format(0));
                m_viewCredit.setText(m_numberFormat.format(0));
                m_viewOther.setText(m_numberFormat.format(0));
                m_viewRealSalesYesterday.setText(m_numberFormat.format(0));
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.manage_code, menu);
        return true;
    }

    //시간대별매출분석
    public void onClickRealSaleTime(View view) {

        Intent intent = new Intent(this, SalesNewsActivity.class);
        intent.putExtra("tabId", "0");
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    //거래처별매출분석
    public void onClickManageCustomers(View view) {
        Intent intent = new Intent(this, SalesNewsActivity.class);
        intent.putExtra("tabId", "1");
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    //분류별 매출분석
    public void onClickManageClassification(View view) {
        Intent intent = new Intent(this, SalesNewsActivity.class);
        intent.putExtra("tabId", "2");
        intent.putExtra("barcode", "");
        startActivity(intent);
        overridePendingTransition(0, 0);

    }

    //상품별 매출분석
    public void onClickManageGoods(View view) {
        Intent intent = new Intent(this, ManageSaleProductActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    // 매장별
    public void onClickSalesShop(View view) {

        Intent intent = new Intent(this, SalesShopActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    // 전표출력
    public void onClickRealSaleReceipt(View view) {

        Intent intent = new Intent(this, SalesReceiptActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    // 달력출력
    public void onClickRealSaleCalendar(View view) {

//        Intent intent = new Intent(this, ManageSalesCalendarActivity.class);
//        intent.putExtra("OFFICE_CODE", "");
//        intent.putExtra("OFFICE_NAME", "");
//        startActivity(intent);
//        overridePendingTransition(0, 0);
        Intent intent = new Intent(this, SalesDateActivity.class);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    // 재부팅
    public void onClickReboot(View view){
        Intent intent = new Intent(this,ShopRebootActivity.class);
        startActivity(intent);
        overridePendingTransition(0,0);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int monthOfYear, int dayOfMonth) {

        m_dateCalender1.set(year, monthOfYear, dayOfMonth);
        m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
        m_dateCalender2.set(year, monthOfYear, dayOfMonth);
        m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);

        queryCommonInformation();

    }

    private void powerOff() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RealSaleMainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle("종료");
        builder.setMessage("종료하시겠습니까!");

        // 확인 버튼
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                //ActivityCompat.finishAffinity(RealSaleMainActivity.this);
                //System.exit(0);
                exit();
            }
        });

        // 취소 버튼
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

            }
        });

        // 박스 실행
        builder.show();

    }

    // 매장선택 뷰
// 매장선택 화면 레이아웃
    private View createCustomView() {
        LinearLayout linearLayoutView = new LinearLayout(this);
        m_list = new ListView(this);

        ArrayList<ShopSelectItem> shopList;
        ShopListAdapter2 listAdapter;

        shopList = new ArrayList<ShopSelectItem>();
        JSONArray shopsData = LocalStorage.getJSONArray(RealSaleMainActivity.this, "shopsData");

        for (int i = 0; i < shopsData.length(); i++) {
            try {
                JSONObject shop = shopsData.getJSONObject(i);
                String Office_Name = shop.getString("Office_Name");
                String SHOP_IP = shop.getString("SHOP_IP");
                String APP_SDATE = shop.getString("APP_SDATE");
                String APP_EDATE = shop.getString("APP_EDATE");

                shopList.add(new ShopSelectItem(Office_Name, SHOP_IP, APP_SDATE, APP_EDATE, false));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        listAdapter = new ShopListAdapter2(this, R.layout.activity_select_shop_list, shopList);
        m_list.setAdapter(listAdapter);

        linearLayoutView.setOrientation(LinearLayout.VERTICAL);
        linearLayoutView.addView(m_list);
        return linearLayoutView;
    }

    // 커스틈 리스트 클래스
    private class ShopListAdapter2 extends BaseAdapter {
        Context ctx;
        int itemLayout;

        private ArrayList<ShopSelectItem> object;

        public ShopListAdapter2(Context ctx, int itemLayout, ArrayList<ShopSelectItem> object) {
            super();
            this.object = object;
            this.ctx = ctx;
            this.itemLayout = itemLayout;
        }

        @Override
        public int getCount() {
            return object.size();
        }

        @Override
        public Object getItem(int position) {
            return m_list.getItemAtPosition(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RealSaleMainActivity.ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(RealSaleMainActivity.this);
                convertView = inflater.inflate(R.layout.activity_select_shop_list, parent, false);
                holder = new RealSaleMainActivity.ViewHolder(ctx);
                holder.radioShop = (RadioButton) convertView.findViewById(R.id.radioButtonShop);

                //holder.radioShop.setBackgroundResource(R.drawable.check_icon);
                holder.txtIP = (TextView) convertView.findViewById(R.id.textViewShopIP);
                holder.buttonConfig = (Button) convertView.findViewById(R.id.buttonShopConfig);
                holder.txtShopName = (TextView) convertView.findViewById(R.id.textViewShopName);
                holder.txtDate = (TextView) convertView.findViewById(R.id.textViewDate);

                convertView.setTag(holder);
            } else {
                holder = (RealSaleMainActivity.ViewHolder) convertView.getTag();
            }

            String name = object.get(position).getName();
            String strIP = object.get(position).getIP();
            String edate = object.get(position).getEdate();
            String sdate = object.get(position).getSdate();

            holder.object = object.get(position);
            holder.txtShopName.setText(name);
            holder.txtDate.setText("[" + sdate + "~" + edate + "]");
            holder.radioShop.setChecked(position == mSelectedPosition);
            holder.txtIP.setText(strIP);
            holder.m_position = position;
            holder.radioShop.setTag(holder);
            holder.radioShop.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    RealSaleMainActivity.ViewHolder mH = (RealSaleMainActivity.ViewHolder) v.getTag();

                    Log.i("test", "Test=" + mH.m_position);
                    mSelectedPosition = mH.m_position;
                    notifyDataSetChanged();
                }
            });
            holder.buttonConfig.setTag(position);
            //비밀번호 없이 바로 진입합니다.
            holder.buttonConfig.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    int idx = (Integer) v.getTag();

                    JSONArray shopsData = LocalStorage.getJSONArray(RealSaleMainActivity.this, "shopsData");

                    try {
                        JSONObject shop = shopsData.getJSONObject(idx);
                        LocalStorage.setJSONObject(RealSaleMainActivity.this, "currentShopData", shop);

                        Intent intent = new Intent(RealSaleMainActivity.this, ConfigActivity.class);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return convertView;
        }
    }

    static class ViewHolder extends Activity {
        public Context ctx;
        public RadioButton radioShop;
        public TextView txtIP;
        public Button buttonConfig;
        public int m_position;
        public TextView txtShopName;
        public TextView txtDate;
        public ShopSelectItem object;

        public ViewHolder(Context ctx) {
            this.ctx = ctx;
        }
    }

    // 매장 정보 가져오기
    public void fetchOffices() {

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        Calendar c = Calendar.getInstance();
        String today = f.format(c.getTime());

        String phoneNumber = LocalStorage.getString(RealSaleMainActivity.this, "phoneNumber");

        // 쿼리 작성하기
        String query = "";
        query = "select * "
                + "  from APP_USER as A inner join V_OFFICE_USER as B "
                + " on A.OFFICE_CODE = B.Sto_CD "
                + " JOIN APP_SETTLEMENT as C on A.OFFICE_CODE = C.OFFICE_CODE "
                + " where A.APP_HP ='" + phoneNumber + "' AND C.DEL_YN = '0' "
                + " AND C.APP_SDATE<='" + today + "' AND C.APP_EDATE>='" + today + "'"
                + "order by office_name";

        // 로딩 다이알로그
        dialog = new ProgressDialog(this);
        dialog.setMessage("Loading....");
        dialog.show();

        // 콜백함수와 함께 실행
        new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                dialog.dismiss();
                dialog.cancel();
                if (results.length() > 0) {
                    didFetchOffices(results);
                } else {
                    showDialog("등록된 매장정보가 없습니다");
                }
            }

            @Override
            public void onRequestFailed(int code, String msg) {
                dialog.dismiss();
                dialog.cancel();
                Toast.makeText(getApplicationContext(), "매장목록을 가져오는데 실패하였습니다", Toast.LENGTH_SHORT).show();
            }
            // 2022.05.26.본사서버 IP변경
            //}).execute("122.49.118.102:18971", "trans", "app_tips", "app_tips", query);
        }).execute(TIPOS.HOST_SERVER_IP + ":" + TIPOS.HOST_SERVER_PORT, TIPOS.HOST_SERVER_DB, TIPOS.HOST_SERVER_ID, TIPOS.HOST_SERVER_PW, query);
    }

    // DB에 접속후 호출되는 함수
    public void didFetchOffices(JSONArray results) {

        Toast.makeText(getApplicationContext(), "검색 완료", Toast.LENGTH_SHORT).show();

        LocalStorage.setJSONArray(RealSaleMainActivity.this, "shopsData", results);
        showSelectShop();
    }

    public void showDialog(String msg) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle("알림");
        builder.setMessage(msg);
        builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        builder.show();
    }

    public void showSelectShop() {

//        //20190910 매장선택화면에 앱 사용자버젼 추가
//        PackageInfo pi = null;
//        try {
//            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
//            //String sMyVersion = getResources().getString(R.string.check_version.);
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        String verSion = pi.versionName;

//        TextView textView = new TextView(this);
//        textView.setText("Select an option");
//        textView.setPadding(20, 30, 20, 30);
//        textView.setTextSize(20F);
//        textView.setBackgroundColor(Color.CYAN);
//        textView.setTextColor(Color.WHITE);

        m_alert = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT)
                //.setCustomTitle(textView)
                .setTitle("관리매장선택")
                .setView(createCustomView())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        JSONArray shopsData = LocalStorage.getJSONArray(RealSaleMainActivity.this, "shopsData");
                        try {
                            JSONObject shop = shopsData.getJSONObject(mSelectedPosition);

                            String APP_EDATE = shop.getString("APP_EDATE");
                            String APP_SDATE = shop.getString("APP_SDATE");
                            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
                            Date edate = formatter.parse(APP_EDATE);
                            Date sdate = formatter.parse(APP_SDATE);

                            //2021-12-21 DB,ID,PW 체크 추가
                            String shop_uudb = shop.getString("shop_uudb");
                            String shop_uuid = shop.getString("shop_uuid");
                            String shop_uupass = shop.getString("shop_uupass");


                            //2015-01-05 날자비교 버그 수정오류 변경
                            //저장 데이타에는 시간이 없서서 비교시 종료날자보다 하루 늦게 안보여지는 현상 변경
                            Date today = new Date();
                            String c = formatter.format(today);
                            today = formatter.parse(c);

						/*Log.i("sdate", sdate.toString());
						Log.i("edate", edate.toString());
						Log.i("today", today.toString());
						Log.i("sdate", String.format("%d", sdate.getTime()) );
						Log.i("edate", String.format("%d", edate.getTime()) );
						Log.i("today", String.format("%d", today.getTime()) );*/

                            if (today.getTime() < sdate.getTime()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RealSaleMainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
                                builder.setTitle("알림");
                                builder.setMessage("아직 이용할수 없습니다.\r\n관리자에게 문의하세요\r\n1600-1883");
                                builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.show();
                                return;
                            }

                            if (today.getTime() > edate.getTime()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RealSaleMainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
                                builder.setTitle("알림");
                                builder.setMessage("기간이 지났습니다.\r\n관리자에게 문의하세요\r\n1600-1883");
                                builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.show();
                                return;
                            }

                            //----------------------------------------//
                            // 2021.12.21. 매장별 DB명,로그인ID,PW 체크 추가
                            //----------------------------------------//
                            //DB
                            if (shop_uudb.isEmpty()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RealSaleMainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
                                builder.setTitle("알림");
                                builder.setMessage("매장DB 이름이 없습니다.\r\n관리자에게 문의하세요\r\n1600-1883");
                                builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.show();
                                return;
                            }

                            //ID
                            if (shop_uuid.isEmpty()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RealSaleMainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
                                builder.setTitle("알림");
                                builder.setMessage("매장DB 아이디가 없습니다.\r\n관리자에게 문의하세요\r\n1600-1883");
                                builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.show();
                                return;
                            }

                            //PW
                            if (shop_uupass.isEmpty()) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(RealSaleMainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
                                builder.setTitle("알림");
                                builder.setMessage("매장DB 비밀번호가 없습니다.\r\n관리자에게 문의하세요\r\n1600-1883");
                                builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                                builder.show();
                                return;
                            }
                            //----------------------------------------//
                            LocalStorage.setJSONObject(RealSaleMainActivity.this, "currentShopData", shop);
                            next();
                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                })

                // 뒤로가기 버튼 클릭시
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {

                        //  종료 창
                        //exit();
                    }
                })
                .create();

        //m_alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_alert.show();

        //----------------------------------------//
        // 2022.04.07. 다이얼박스 테스트 색상 정의
        //----------------------------------------//
        int textViewId = m_alert.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
        TextView tv = m_alert.findViewById(textViewId);
        if (tv != null) {
            tv.setTextColor(Color.parseColor("#ffffff"));
            tv.setBackgroundColor(Color.parseColor("#282828"));
        }
        //----------------------------------------//

//https://www.tabnine.com/code/java/methods/android.app.AlertDialog/getContext

        //----------------------------------------//
        // 2022.04.07. 다이얼박스 테스트 분리선 색상 정의
        //----------------------------------------//
        int dividerId = m_alert.getContext().getResources().getIdentifier("android:id/titleDivider", null, null);
        if (dividerId != 0) {
            View divider = m_alert.findViewById(dividerId);
            divider.setBackgroundColor(Color.parseColor("#403c3d"));
        }
        //----------------------------------------//


    }

    private void next() {
        //조회로 가자
        // 매장정보 새로 정의
        GetShopInfo();
        queryCommonInformation();
    }

    private void exit() {

        finishAffinity();
        System.runFinalization();
        System.exit(0);
    }

    private void GetShopInfo() {
        //----------------------------------------//
        // 매장 정보 정의
        //----------------------------------------//
        m_shop = LocalStorage.getJSONObject(this, "currentShopData");
        m_userProfile = LocalStorage.getJSONObject(this, "userProfile");  // 2/27 수수료 매장일때 추가 내용

        //매출보기 옵션 설정
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        m_viewOption = pref.getString("prefSaleViewMethod", "All");

        m_OFFICE_CODE = "";
        m_OFFICE_NAME = "";

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


            //  2/27 수수료 매장일때 추가 내용 추가
            m_APP_USER_GRADE = m_userProfile.getString("APP_USER_GRADE");
            //m_OFFICE_CODE = m_userProfile.getString("OFFICE_CODE");
            //m_OFFICE_NAME = m_userProfile.getString("Office_Name");

            m_OFFICE_CODE = m_shop.getString("OFFICE_CODE");
            m_OFFICE_NAME = m_shop.getString("Office_Name");

            toolbarTitle.setText(m_OFFICE_NAME);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        //----------------------------------------//

    }

}
