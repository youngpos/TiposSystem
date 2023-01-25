package tipsystem.tips;

import static android.support.v4.os.LocaleListCompat.create;

import static tipsystem.utils.DBAdapter.TAG;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import kr.co.tipos.tips.R;
import tipsystem.tips.models.ShopSelectItem;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.TIPOS;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class MainActivity extends Activity {

    public final static String EXTRA_MESSAGE = "unikys.todo.MESSAGE";

    public ListView m_list;
    int mSelectedPosition = 0;
    AlertDialog m_alert;
    public RadioGroup m_rgShop;
    //private boolean m_blnPreviewActivity;//이전 액티비티
    private String m_PreviewActivity=""; // 이전 액티비티 명


    // loading bar
    private ProgressDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //m_blnPreviewActivity = getIntent().getBooleanExtra("blnPreviewActivity", false);
        m_PreviewActivity = getIntent().getStringExtra("PreviewActivity");

        m_rgShop = new RadioGroup(this);
        fetchOffices();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        m_rgShop = new RadioGroup(this);
        fetchOffices();
    }

    public void showSelectShop() {

        //20190910 매장선택화면에 앱 사용자버젼 추가
        PackageInfo pi = null;
        try {
            pi = getPackageManager().getPackageInfo(getPackageName(), 0);
            //String sMyVersion = getResources().getString(R.string.check_version.);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }

        String verSion = pi.versionName;

        //----------------------------------------//
        // 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
        //----------------------------------------//
        //m_alert = new AlertDialog.Builder(this)
        m_alert = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT)
                //----------------------------------------//
                .setTitle("관리매장(버전. " + verSion + ")")
                .setView(createCustomView())
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        JSONArray shopsData = LocalStorage.getJSONArray(MainActivity.this, "shopsData");
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
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
                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,AlertDialog.THEME_HOLO_LIGHT);
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

                            LocalStorage.setJSONObject(MainActivity.this, "currentShopData", shop);
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
                        try {
                            if (m_PreviewActivity.equals("MainMenuActivity")) {
                                m_PreviewActivity = "";
                                Intent intent = new Intent(MainActivity.this, MainMenuActivity.class);
                                startActivity(intent);
                            }else exit();

                        } catch (Exception e) {
                            //String msg = e.getMessage();
                            //Toast.makeText(MainActivity.this,e.getMessage(),Toast.LENGTH_SHORT).show();
                            exit();
                        }


//                        if (m_PreviewActivity.equals("MainMenuActivity")) {
//                            m_PreviewActivity = "";
//                            Intent intent = new Intent(MainActivity.this, MainMenuActivity.class);
//                            startActivity(intent);
//
////                        } else if (m_PreviewActivity.equals("RealSaleMainActivity")) {
////                            m_PreviewActivity = "";
////                            Intent intent = new Intent(MainActivity.this, RealSaleMainActivity.class);
////                            startActivity(intent);
//
//                        } else exit();

                    }
                })
                .create();

        //m_alert.requestWindowFeature(Window.FEATURE_NO_TITLE);
        m_alert.show();

//        //----------------------------------------//
//        // 2022.04.07. 다이얼박스 테스트 색상 정의
//        //----------------------------------------//
//        int textViewId = m_alert.getContext().getResources().getIdentifier("android:id/alertTitle", null, null);
//        TextView tv = m_alert.findViewById(textViewId);
//        if (tv != null) {
//            tv.setTextColor(Color.parseColor("#ffffff"));
//            tv.setBackgroundColor(Color.parseColor("#282828"));
//        }
//        //----------------------------------------//

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

//
//        int iconId = m_alert.getContext().getResources().getIdentifier("android:id/icon", null, null);
//        if (iconId != 0) {
//            ImageView icon = (ImageView) m_alert.findViewById(iconId);
//            icon.setColorFilter(Color.parseColor("#282828"));
//        }

    }

    private void next() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
    }

    // 매장선택 화면 레이아웃
    private View createCustomView() {
        LinearLayout linearLayoutView = new LinearLayout(this);
        m_list = new ListView(this);

        ArrayList<ShopSelectItem> shopList;
        ShopListAdapter listAdapter;

        shopList = new ArrayList<ShopSelectItem>();
        JSONArray shopsData = LocalStorage.getJSONArray(MainActivity.this, "shopsData");

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

        listAdapter = new ShopListAdapter(this, R.layout.activity_select_shop_list, shopList);
        m_list.setAdapter(listAdapter);

        linearLayoutView.setOrientation(LinearLayout.VERTICAL);
        linearLayoutView.addView(m_list);
        return linearLayoutView;
    }

    // 매장 정보 가져오기
    public void fetchOffices() {

        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
        Calendar c = Calendar.getInstance();
        String today = f.format(c.getTime());

        String phoneNumber = LocalStorage.getString(MainActivity.this, "phoneNumber");

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

        LocalStorage.setJSONArray(MainActivity.this, "shopsData", results);
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

    public void onAuthentication(View view) {
        fetchOffices();
    }

    class ShopListAdapter extends BaseAdapter {
        Context ctx;
        int itemLayout;

        private ArrayList<ShopSelectItem> object;

        public ShopListAdapter(Context ctx, int itemLayout, ArrayList<ShopSelectItem> object) {
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
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
                convertView = inflater.inflate(R.layout.activity_select_shop_list, parent, false);
                holder = new ViewHolder(ctx);
                holder.radioShop = (RadioButton) convertView.findViewById(R.id.radioButtonShop);

                //holder.radioShop.setBackgroundResource(R.drawable.check_icon);
                holder.txtIP = (TextView) convertView.findViewById(R.id.textViewShopIP);
                holder.buttonConfig = (Button) convertView.findViewById(R.id.buttonShopConfig);
                holder.txtShopName = (TextView) convertView.findViewById(R.id.textViewShopName);
                holder.txtDate = (TextView) convertView.findViewById(R.id.textViewDate);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
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
            holder.radioShop.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    ViewHolder mH = (ViewHolder) v.getTag();

                    Log.i("test", "Test=" + mH.m_position);
                    mSelectedPosition = mH.m_position;
                    notifyDataSetChanged();
                }
            });
            holder.buttonConfig.setTag(position);
            //비밀번호 없이 바로 진입합니다.
            holder.buttonConfig.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    int idx = (Integer) v.getTag();

                    JSONArray shopsData = LocalStorage.getJSONArray(MainActivity.this, "shopsData");

                    try {
                        JSONObject shop = shopsData.getJSONObject(idx);
                        LocalStorage.setJSONObject(MainActivity.this, "currentShopData", shop);

                        Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
                        startActivity(intent);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            //비밀번호 있어야 진입할수 있습니다.
			/*holder.buttonConfig.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					int idx = (Integer) v.getTag();

					JSONArray shopsData = LocalStorage.getJSONArray(MainActivity.this, "shopsData");

					try {
						JSONObject shop = shopsData.getJSONObject(idx);
			    		LocalStorage.setJSONObject(MainActivity.this, "currentShopData", shop);

			    		final EditText input = new EditText(MainActivity.this);
			    		input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

				    	new AlertDialog.Builder(MainActivity.this)
				        .setTitle("암호를 입력하세요")
				        .setMessage("문의:1600-1883")
				        .setView(input)
				        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int whichButton) {
				                String value = input.getText().toString();
				                Log.i("passwd", value);
				                if (value.equals("1883")) {
					                Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
							    	startActivity(intent);
				                }
				                else {
						            Toast.makeText(MainActivity.this, "비밀번호가 틀렸습니다", Toast.LENGTH_SHORT).show();
				                }
				            }
				        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
				            public void onClick(DialogInterface dialog, int whichButton) {
				                // Do nothing.
				            }
				        }).show();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			});*/

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

    ;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void exit() {

        finishAffinity();
        System.runFinalization();
        System.exit(0);

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("종료");
//        builder.setMessage("종료하시겠습니까?");
//        builder.setCancelable(true);
//
//        // 예
//        builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
//            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                finishAffinity();
//                System.runFinalization();
//                System.exit(0);
//
//            }
//        });
//
//        // 뒤로가기 버튼 클릭시
//        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialogInterface) {
//                fetchOffices();
//            }
//        });
//        builder.show();

    }

}

