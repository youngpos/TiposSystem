package tipsystem.tips;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.StringFormat;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CalendarView.OnDateChangeListener;
import android.widget.TextView;

import android.widget.Toast;

public class ManageSalesCalendarActivity extends Activity {

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

	String m_APP_USER_GRADE;
	String m_OFFICE_CODE ="";	// 수수료매장일때 고정될 오피스코드
	String m_OFFICE_NAME ="";
	
	Button m_buttonSetDate;
	android.widget.CalendarView m_calendar;	
	String m_viewOption;
	String m_CalendarDay;
	NumberFormat m_numberFormat;	
	ProgressDialog dialog;	
	JSONArray m_results;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_calendar_sale);
		
		 //매출보기 옵션 설정
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
      	m_viewOption = pref.getString("prefSaleViewMethod", "");
      	
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

			m_APP_USER_GRADE =m_userProfile.getString("APP_USER_GRADE");
		} catch (JSONException e) {
			e.printStackTrace();
		}      

        //20190910
        //일반 최고관리자일경우
        //m_OFFICE_CODE = getIntent().getStringExtra("OFFICE_CODE");
        //m_OFFICE_NAME = getIntent().getStringExtra("OFFICE_NAME");
        
        //수수료매장의 경우 
        //if (m_APP_USER_GRADE.equals("2") && !m_OFFICE_CODE.equals("") && null != m_OFFICE_CODE) {
        if (m_APP_USER_GRADE.equals("2")){
			try {
				m_OFFICE_CODE = m_userProfile.getString("OFFICE_CODE");
				m_OFFICE_NAME = m_userProfile.getString("OFFICE_NAME");
			} catch (JSONException e) {
				e.printStackTrace();
			}   	      	
        } 
      
        m_buttonSetDate = (Button)findViewById(R.id.buttonSetDate);
        //m_buttonSetDate.setText
        m_numberFormat = NumberFormat.getInstance();
       
        //m_calendar = (CalendarView)findViewById(R.id.calendarView1);
        m_calendar = (android.widget.CalendarView)findViewById(R.id.calendarView_main);


        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        m_CalendarDay = sdf.format(cal.getTime());

        TextView totalofday = (TextView)findViewById(R.id.textView_totalofday);
        TextView totalofmonth = (TextView)findViewById(R.id.textView_totalofmonth);
        totalofday.setText((cal.get(Calendar.MONTH) + 1 )+"월 "+cal.get(Calendar.DATE) + "일 매출 합계");
        totalofmonth.setText( (cal.get(Calendar.MONTH) + 1 ) + " 월 매출 합계");        
        
        Log.w("현재시간 : ", cal.getTime().toString() );
        //cal.getTime();
        
        //현재날자 에서 한달 더하고 그달 마지막 날까지를 검색 조건으로 넣는다
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DATE, (cal.getActualMaximum(Calendar.DATE) - cal.get(Calendar.DATE)));
        m_calendar.setMaxDate(cal.getTimeInMillis());
        
        //검색을 1년 단위까지만 검색 되어지게 넣는다
        cal.add(Calendar.YEAR, -1);
        cal.add(Calendar.MONTH, -1);
        cal.set(Calendar.DATE, 1);
        m_calendar.setMinDate(cal.getTimeInMillis());
                              
        m_calendar.setOnDateChangeListener(new OnDateChangeListener() { 
            @Override 
            public void onSelectedDayChange(CalendarView view, int year, int month, int day) {
            	
            	String[] monthDay = m_CalendarDay.split("-");
            	
            	int m_year = Integer.parseInt(monthDay[0].toString());
            	int m_month = Integer.parseInt(monthDay[1].toString());
            	int m_day = Integer.parseInt(monthDay[2].toString());
            	
            	if( m_year != year ){
            		m_CalendarDay = String.format("%04d-%02d-%02d", year, month+1, day);
            		query();
            	}else if( m_month != month+1 ){            		
            		m_CalendarDay = String.format("%04d-%02d-%02d", year, month+1, day);
            		query();            		            		
            	}else if( m_day != day ){
            		m_CalendarDay = String.format("%04d-%02d-%02d", year, month+1, day);
            		didUpdate();
            	}
            	
            	//m_CalendarDay = String.format("%04d-%02d-%02d", year, month, day);
            	TextView totalofday = (TextView)findViewById(R.id.textView_totalofday);
            	TextView totalofmonth = (TextView)findViewById(R.id.textView_totalofmonth);
            	totalofday.setText((month+1)+"월 "+ day + "일 매출 합계");
            	//totalofday.setText(day + " 일 매출 합계");
                totalofmonth.setText((month+1) + " 월 매출 합계");
        		Log.w("선택 날자", m_CalendarDay.toString());
            } 
        }); 

        
        /*m_calendar.setOnCellTouchListener(new OnCellTouchListener() {
        
        	@Override
        	public void onTouch(Cell cell) {
        		
    			int year  = m_calendar.getYear();
    			int month = m_calendar.getMonth()+1;
    			int day   = cell.getDayOfMonth();
        		m_CalendarDay = String.format("%04d-%02d-%02d", year, month, day);

        		updateDate();
        		didUpdate(day);
        	}
        });
        */
        
		//updateDate();
        query();
		
	}
	/*
	public void onClickSetDatePrevious(View v) {
		//m_calendar.previousMonth();
		updateDate();
		query();
	}
	
	public void onClickSetDateNext(View v) {
		//m_calendar.nextMonth();
		updateDate();
		query();
	}
	
	*/
	/*private void updateDate() {

		int year  = m_calendar.getYear();
		int month = m_calendar.getMonth()+1;
		m_buttonSetDate.setText(String.format("%04d년 %02d월", year, month));
	}*/
	
	private void query()
	{
		String query = "";
		
		//int year  = m_calendar.getYear();
		//int month = m_calendar.getMonth()+1;
		String[] monthDay = m_CalendarDay.split("-");
    	
    	String m_year = monthDay[0].toString();
    	String m_month = monthDay[1].toString();		
    	String m_day = monthDay[2].toString();
    	
		//String tableName = String.format("%04d%02d", year, month);
    	String tableName = m_year+m_month;
    	Log.w("테이블명", tableName.toString());

    	//20190910 수수료거래처 조회 쿼리 변경
		query = "SELECT "
				+ " '일자'=CASE WHEN G.SALE_DATE IS NULL THEN IN_DATE ELSE G.SALE_DATE END, "
				+ " ISNULL(순매출,0) '순매출', "
				+ " ISNULL(객수,0) '객수', "
				+ " '객단가' = CASE WHEN ISNULL(순매출,0)=0 Then 0 ELSE ISNULL(순매출,0)/ISNULL(객수,0) END, "
				+ " ISNULL(IN_Pri,0) '매입금액' "
				+ " FROM ( "
				+ "  Select Sale_DATE,  "
				+ "  IsNull(Sum(TSell_Pri-TSell_RePri), 0) '순매출', "
				+ "  Count (Distinct(Sale_Num)) '객수' "
				+ "  From SaD_"+tableName+" "
				+ "  WHERE  1 = 1 ";
				//매출보기 옵션 설정 
		        if(m_viewOption.equals("cardY")){
		        	query += " AND Card_Yn='1' ";
		        }else if(m_viewOption.equals("cardN")){
		        	query += " AND Card_Yn='0' ";
		        }

		        if (m_APP_USER_GRADE.equals("2")) {
					query += "AND Office_Code = '" + m_OFFICE_CODE + "' ";
				}

		query += "  Group By Sale_DATE "
				+ " ) G FULL JOIN  ( "
				+ "      Select In_Date,Sum(In_Pri) IN_Pri "
				+ "      From InT_"+tableName+" "
				+ "  WHERE 1 = 1 ";

		        if (m_APP_USER_GRADE.equals("2")) {
					query += " AND Office_Code = '" + m_OFFICE_CODE + "' ";
				}

		query += "      GRoup By In_Date  "
				+ " ) B ON G.SALE_DATE=B.IN_DATE ";
		        //----------------------------------------//
		        // 김영목 2020.10.26. 달력조회 에러 수정(MSSQL2012 Group By 다음 Order By 사용시 에러 )
				//+ " Order by G.일자 ";


		Log.w("달력_쿼리", query.toString());
		
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
		dialog.setCancelable(false);
 		dialog.show();
 		
		new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {

				dialog.dismiss();
				dialog.cancel();
				m_results  = results;

				/*Cell c = m_calendar.getSelectedCell();
    			
    			int day   = c.getDayOfMonth();
    			*/
    			String[] monthDay = m_CalendarDay.split("-");
    	
		    	//String m_year = monthDay[0].toString();
		    	//String m_month = monthDay[1].toString();		
		    	//int m_day = Integer.parseInt(monthDay[2].toString());
    			
    			//if (m_day>0)				
    			didUpdate();
    			
    			didUpdateTotal();
				//Log.i("date", String.format("%02d", m_day));
			}

		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	private void didUpdateTotal ()	{
		
		//총순매출 표시
		/*int year  = m_calendar.getYear();
		int month = m_calendar.getMonth()+1;
		String s = String.format("%04d-%02d-%02d", year, month, 1);*/
		
		String[] monthDay = m_CalendarDay.split("-");
    	
    	String m_year = monthDay[0].toString();
    	String m_month = monthDay[1].toString();
    	String s = String.format("%4s-%2s-%02d", m_year, m_month, 1);
		
		Date date = null;
		try {
			date = new SimpleDateFormat("yyyy-MM-dd").parse(s);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		int days = cal.getActualMaximum(Calendar.DAY_OF_MONTH); // 해당월의 총 일수
		
		try {
			double a=0, b=0, c=0, d=0; 
		
			for(int i = 0; i < m_results.length() ; i++) {				
				JSONObject son = m_results.getJSONObject(i);
				a += son.getInt("순매출");
				b += son.getInt("객수");
				c += son.getInt("객단가");
				d += son.getInt("매입금액");
			}		
			DecimalFormat df = new DecimalFormat(",###");
			TextView tv1 = (TextView)findViewById(R.id.textViewTop1);
			tv1.setText( StringFormat.convertToNumberFormat(a));

			TextView tv2 = (TextView)findViewById(R.id.textViewTop2);
			tv2.setText( df.format(a/days));			

			TextView tv3 = (TextView)findViewById(R.id.textViewTop3);
			tv3.setText( df.format(c/days));
			
			TextView tv4 = (TextView)findViewById(R.id.textViewTop4);
			tv4.setText( df.format(b/days));

			TextView tv5 = (TextView)findViewById(R.id.textViewTop5);
			tv5.setText( StringFormat.convertToNumberFormat(d));
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
		
	private void didUpdate () {
		//일자별 순매출
		TextView tv1 = (TextView)findViewById(R.id.textView1);
		TextView tv2 = (TextView)findViewById(R.id.textView2);
		TextView tv3 = (TextView)findViewById(R.id.textView3);
		TextView tv4 = (TextView)findViewById(R.id.textView4);
		tv1.setText("0 원");
		tv2.setText("0 명");
		tv3.setText("0 원");
		tv4.setText("0 원");
		
		/*int year  = m_calendar.getYear();
		int month = m_calendar.getMonth()+1;
		String currentDay = String.format("%04d-%02d-%02d", year, month, day);*/
		
		try {
			//검색 결과 값이 없으면 0원 표시
			if (m_results.length()==0) {
				return;
			}
			
			JSONObject son = null;
			boolean a = false;		
			for(int i = 0; i < m_results.length() ; i++) {				
				son = m_results.getJSONObject(i);
				String date = son.getString("일자");
				Log.w("일자 비교 : " , date.toString() + " " + m_CalendarDay.toString());
				if (date.equals(m_CalendarDay.toString())) {
					a = true;
					break;
				} else {
					a = false;
				}				
			}		
			if(a){
			/*String rSale = String.format("순매출 : %s 원", m_numberFormat.format(son.getInt("순매출")));
			String saleNum = String.format("객 수 : %s 명", m_numberFormat.format(son.getInt("객수")));
			String salePri = String.format("객단가 : %s 원", m_numberFormat.format(son.getInt("객단가")));
			String tPurPri = String.format("매입금액 : %s 원", m_numberFormat.format(son.getInt("매입금액")));*/		
			
			String rSale = String.format("%s 원", m_numberFormat.format(son.getInt("순매출")));
			String saleNum = String.format("%s 명", m_numberFormat.format(son.getInt("객수")));
			String salePri = String.format("%s 원", m_numberFormat.format(son.getInt("객단가")));
			String tPurPri = String.format("%s 원", m_numberFormat.format(son.getInt("매입금액")));
								
			tv1.setText(rSale);
			tv2.setText(saleNum);
			tv3.setText(salePri);			
			tv4.setText(tPurPri);
			
			}else{
				tv1.setText("0 원");
				tv2.setText("0 명");
				tv3.setText("0 원");
				tv4.setText("0 원");
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
