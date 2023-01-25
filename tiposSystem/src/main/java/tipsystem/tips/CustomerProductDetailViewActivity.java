package tipsystem.tips;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dm.zbar.android.scanner.ZBarConstants;

import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.StringFormat;

import android.os.Build;
import android.os.Bundle;
import android.R.integer;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v4.app.NavUtils;
//TODO 매출관리/거래처별 매출분석/ 클릭시 상세보기 
public class CustomerProductDetailViewActivity extends Activity {
	
	ListView m_listDetailView;
	TextView m_period1;
	TextView m_period2;	
	TextView m_customerCode;
	TextView m_customerName;
	TextView textViewNetSale;
	TextView textViewMargin;
	TextView	textViewProfits;
	TextView	textViewShare;
	
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

	String Card_Yn;
	
	NumberFormat m_numberFormat;
	ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_customer_product_detail_view);
		
		m_shop = LocalStorage.getJSONObject(this, "currentShopData");
	       
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

		} catch (JSONException e) {
			e.printStackTrace();
		}
        
		m_numberFormat = NumberFormat.getInstance();
		
		m_listDetailView= (ListView)findViewById(R.id.listviewCustomerProductDetailViewList);
		m_period1 = (TextView)findViewById(R.id.textViewPeriod1);
		m_period2 = (TextView)findViewById(R.id.textViewPeriod2);
		m_customerCode = (TextView)findViewById(R.id.textViewCustomerCode);
		m_customerName = (TextView)findViewById(R.id.textViewCustomerName);
		// 상단 고정 표시부 입니다.
		textViewNetSale = (TextView)findViewById(R.id.textViewNetSale);
		textViewMargin = (TextView)findViewById(R.id.textViewMargin);
		textViewProfits = (TextView)findViewById(R.id.textViewProfits);
		textViewShare = (TextView)findViewById(R.id.textViewShare);
		
		/*Intent intent = getIntent();
		
		// 상단 표시 하기 데이터 입력 부
		m_period1.setText(intent.getExtras().getString("PERIOD1"));
		m_period2.setText(intent.getExtras().getString("PERIOD2"));		
		m_customerCode.setText(intent.getExtras().getString("OFFICE_CODE"));
		m_customerName.setText(intent.getExtras().getString("OFFICE_NAME"));		
		textViewNetSale.setText(intent.getExtras().getString("NETSALE"));
		textViewMargin.setText(intent.getExtras().getString("MARGIN")); //이익금
		textViewProfits.setText(intent.getExtras().getString("PROFITS")); //이익률
		
		
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();
		String m_hour = intent.getExtras().getString("HOUR"); 
		String customerCode = m_customerCode.getText().toString();
		String customerName = m_customerName.getText().toString();
		String barcode = intent.getExtras().getString("BARCODE");
		//수수료 매장 다중매장시 전체 상품 보이기
		Card_Yn = intent.getExtras().getString("Card_Yn");
		
		// 점유율 시간으로 변경 해서 보여짐
		if(!m_hour.equals("")){		
		TextView textview = (TextView) findViewById(R.id.textview_shareText);
		textview.setText(" 시  간 : ");
		textViewShare.setText(" " + m_hour + " 시");	//점유율
		}else{
			textViewShare.setText(intent.getExtras().getString("SHARE"));	//점유율
		}
		
		executeQuery(period1, period2, customerCode, customerName, barcode, m_hour);*/
		activityResultStrat();
	}

	
	private void executeQuery(String... urls)
	{	
		String period1 = urls[0];
		String period2 = urls[1];
		String customerCode = urls[2];
		String customerName = urls[3];
		String barcode = urls[4];
		String month = urls[5];		
		
		String query = "";
	    
		int year1 = Integer.parseInt(period1.substring(0, 4));
		int year2 = Integer.parseInt(period2.substring(0, 4));
		
		int month1 = Integer.parseInt(period1.substring(5, 7));
		int month2 = Integer.parseInt(period2.substring(5, 7));
		
		query = " Select G.Office_Code, G.Office_Name, G.Barcode, G.G_Name, G.Std_Size, G.평균원가, G.평균판가, "
			      + " G.판매수량, G.판매, G.반품수량, G.반품, G.할인, G.순판매수량, G.순매출, G.포인트, G.캐쉬백, G.이익금, G.이익률, '점유율'=CASE WHEN 0 <> 0 Then (순매출/0)*100  ELSE 0 End "
			      + " From ( "
			      + " Select G.Office_Code,G.Office_Name, G.Barcode, G.G_Name, G.Std_Size, "
			      + " '평균원가'=Case When (Sum(G.판매수량)-Sum(G.반품수량))=0 Then 0 Else (Sum(G.매출원가)/(Sum(G.판매수량)-Sum(G.반품수량))) End, "
			      + " '평균판가'=Case When (Sum(G.판매수량)-Sum(G.반품수량))=0 Then 0 Else (Sum(G.순매출)/(Sum(G.판매수량)-Sum(G.반품수량))) End, "
			      + " Sum (G.판매) '판매', Sum(G.판매수량) '판매수량', Sum (G.반품) '반품', Sum(G.반품수량) '반품수량', Sum (G.할인) '할인', "
			      + " Sum (G.순매출) '순매출', Sum(G.판매수량 - G.반품수량) '순판매수량', Sum (G.S_Point) '포인트', Sum (G.S_CashBackPoint) '캐쉬백', "
			      + " Sum (G.이익금) '이익금', '이익률'=Case When Sum(G.이익금)=0 Or Sum(G.순매출)=0 Then 0 Else (Sum(G.이익금)/Sum(G.순매출))*100 End	"
			      + " From ( ";
			      
			      for ( int y = year1; y <= year2; y++ ) {
						int m1 = 1, m2 = 12;
						if (y == year1) m1 = month1;
						if (y == year2) m2 = month2;
						for ( int m = m1; m <= m2; m++ ) {
						  
			String tableName = String.format("%04d%02d", y, m);

			query += " Select A.Office_Code, A.Office_Name, A.Barcode, B.G_Name, B.Std_Size, '판매수량'=Sum(Case When A.Sale_Yn='1' Then A.Sale_Count Else 0 End), "
			      + " '판매'=Sum(Case When A.Sale_Yn='1' Then A.TSell_Pri+A.Dc_Pri Else 0 End), "
			      + " '반품수량'=Sum(Case When A.Sale_Yn='0' Then ABS(A.Sale_Count) Else 0 End), "
			      + " '반품'=Sum(Case When A.Sale_Yn='0' Then A.TSell_RePri+A.Dc_Pri Else 0 End), "
			      + " '할인'=Sum(Case When A.Sale_Yn='1' Then A.DC_Pri Else A.DC_Pri *-1 End), Sum (a.TSell_Pri - a.TSell_RePri) '순매출', "
			      + " Sum (a.Profit_Pri) '이익금', Sum(A.S_Point) S_Point, Sum(A.S_CashBackPoint) S_CashBackPoint , "
			      + " '매출원가'=Sum(Case When A.Sale_YN='1' Then A.Pur_Pri*A.Sale_Count Else A.Pur_Pri*A.Sale_Count End) "
			      + " From SaD_"+tableName+" A LEFT JOIN Goods B ON A.Barcode=B.Barcode "
			      + " Where A.Office_Code Like '"+customerCode+"%' AND "+Card_Yn+" A.barcode like '"+barcode+"%' AND "
			      + month
			      + " A.Sale_Date >= '"+period1+"' AND A.Sale_Date <= '"+period2+"' "
			      + " GRoup By A.Office_Code, A.Office_Name, A.Barcode, B.G_Name, B.Std_Size "
			      + " UNION ALL ";
			      }
			   }   
			query = query.substring(0, query.length()-11);
			query += " ) G "
			      + " Group By G.Office_Code,G.Office_Name,G.Barcode, G.G_Name, G.Std_Size "
			      + " ) G ";
		
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
				
				updateList(results);			
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	private void updateList(JSONArray results)
	{
		//2019-10-28 판매수량 열 가져오는 오류 수정
		//String[] from = new String[] {"Barcode", "G_Name", "판매수량", "순매출", "이익금", "이익률"};
		String[] from = new String[] {"Barcode", "G_Name", "순판매수량", "순매출", "이익금", "이익률"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6 };
		
		try {
			
			if ( results.length() > 0 )
			{
				List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();

				for(int i = 0; i < results.length() ; i++) {
					JSONObject son = results.getJSONObject(i);
					HashMap<String, String> map = JsonHelper.toStringHashMap(son);
					map.put("순매출", StringFormat.convertToNumberFormat(map.get("순매출")));
					map.put("이익금", StringFormat.convertToIntNumberFormat(map.get("이익금")));
					map.put("이익률", StringFormat.convertToIntNumberFormat( map.get("이익률")) + " %");					
					fillMaps.add(map);
				}	
						
				SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.activity_listview_item4_5, from, to);
				m_listDetailView.setAdapter(adapter);
			}
			else  {
				List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();
				SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.activity_listview_item4_5, from, to);
				m_listDetailView.setAdapter(adapter);
			}
			
			Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();
			
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
	
	
	//14년 3월 5일 여러가지 방법으로 변경 하기

	private void activityResultStrat()
	{   
		
		Intent intent = getIntent();
		String gubun = intent.getExtras().getString("GUBUN");
		
		if (gubun.equals("1") ) {
			//기간별 검색시 
			m_period1.setText(intent.getExtras().getString("PERIOD1"));
			m_period2.setText(intent.getExtras().getString("PERIOD2"));		
			m_customerCode.setText(intent.getExtras().getString("OFFICE_CODE"));
			m_customerName.setText(intent.getExtras().getString("OFFICE_NAME"));		
			textViewNetSale.setText(intent.getExtras().getString("NETSALE"));// 순매출
			textViewMargin.setText(intent.getExtras().getString("MARGIN")); // 이익금
			textViewProfits.setText(intent.getExtras().getString("PROFITS")); // 이익률
			textViewShare.setText(intent.getExtras().getString("SHARE"));	//점유율
						
			String period1 = m_period1.getText().toString();
			String period2 = m_period2.getText().toString();
			String m_hour = ""; 
			String customerCode = m_customerCode.getText().toString();
			String customerName = m_customerName.getText().toString();
			String barcode = intent.getExtras().getString("BARCODE");
			//수수료 매장 다중매장시 전체 상품 보이기
			Card_Yn = intent.getExtras().getString("Card_Yn");
			executeQuery(period1, period2, customerCode, customerName, barcode, m_hour);
			return;
		}else if (gubun.equals("2") ){
			// 일자별 검색시
		    	// 상단 표시 하기 데이터 입력 부
				m_period1.setText(intent.getExtras().getString("PERIOD1"));
				m_period2.setText(intent.getExtras().getString("PERIOD2"));		
				m_customerCode.setText(intent.getExtras().getString("OFFICE_CODE")); //거래처 코드
				m_customerName.setText(intent.getExtras().getString("OFFICE_NAME"));	//거래처명	
				textViewNetSale.setText(intent.getExtras().getString("NETSALE")); //순매출
				textViewMargin.setText(intent.getExtras().getString("MARGIN")); //순매입
				textViewProfits.setText(intent.getExtras().getString("PROFITS")); //매출원가
				textViewShare.setText(intent.getExtras().getString("SHARE")); // 이익금
				
				TextView tx1 = (TextView)findViewById(R.id.textview_profitsText);
				tx1.setText(" 매출원가 : ");
				TextView tx2 = (TextView)findViewById(R.id.textview_marginText);
				tx2.setText(" 순매입 : ");
				TextView tx3 = (TextView)findViewById(R.id.textview_shareText);
				tx3.setText(" 이익금 : ");
				String period1 = m_period1.getText().toString();
				String period2 = m_period2.getText().toString();
				//String m_hour = intent.getExtras().getString("HOUR"); 
				String customerCode = m_customerCode.getText().toString();
				String customerName = m_customerName.getText().toString();
				String barcode = intent.getExtras().getString("BARCODE");
				//String month = intent.getExtras().getString("MONTH");
				
				//수수료 매장 다중매장시 전체 상품 보이기
				Card_Yn = intent.getExtras().getString("Card_Yn");
				String month = "";
				
				executeQuery(period1, period2, customerCode, customerName, barcode, month);
				return;
		}else if(gubun.equals("4")){
			
			//시간별 검색시 
			m_period1.setText(intent.getExtras().getString("PERIOD1"));
			m_period2.setText(intent.getExtras().getString("PERIOD2"));		
			m_customerCode.setText(intent.getExtras().getString("OFFICE_CODE"));
			m_customerName.setText(intent.getExtras().getString("OFFICE_NAME"));		
			textViewNetSale.setText(intent.getExtras().getString("NETSALE"));// 순매출
			textViewMargin.setText(intent.getExtras().getString("MARGIN")); // 이익금
			textViewProfits.setText(intent.getExtras().getString("PROFITS")); // 이익률
			
			TextView textview = (TextView) findViewById(R.id.textview_shareText);
			textview.setText(" 판매시간 : ");
			
			
			String period1 = m_period1.getText().toString();
			String period2 = m_period2.getText().toString();
			String m_hour = intent.getExtras().getString("HOUR"); 
			String customerCode = m_customerCode.getText().toString();
			String customerName = m_customerName.getText().toString();
			String barcode = intent.getExtras().getString("BARCODE");
			//수수료 매장 다중매장시 전체 상품 보이기
			Card_Yn = intent.getExtras().getString("Card_Yn");								
			
			textViewShare.setText(" " + m_hour + " 시");	// 판매시간
			m_hour = " LEFT(A.sale_time,2) = '"+m_hour+"' AND ";
			executeQuery(period1, period2, customerCode, customerName, barcode, m_hour);
			return;
		}else{
			Toast.makeText(this, "다시 선택해 주세요 ", Toast.LENGTH_SHORT);			
			try {
				Thread.sleep(30000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finish();
		}
	}
	
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
		
			ActionBar actionbar = getActionBar();         
	//		LinearLayout custom_action_bar = (LinearLayout) View.inflate(this, R.layout.activity_custom_actionbar, null);
	//		actionbar.setCustomView(custom_action_bar);
	
			actionbar.setDisplayShowHomeEnabled(false);
			actionbar.setDisplayShowTitleEnabled(true);
			actionbar.setDisplayShowCustomEnabled(true);
			actionbar.setTitle("거래처별 상품 상세보기");
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.customer_product_detail_view, menu);
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
