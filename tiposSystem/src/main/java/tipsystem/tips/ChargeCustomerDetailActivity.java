package tipsystem.tips;

import java.text.NumberFormat;
import java.util.HashMap;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.StringFormat;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.support.v4.app.NavUtils;

public class ChargeCustomerDetailActivity extends Activity {

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

	TextView m_period1;
	TextView m_period2;
	TextView m_customerCode;
	TextView m_customerName;
	
	TextView m_contents[];
	
	Boolean m_isCashDeduction;
	TextView m_ratioDeduction;

	String m_cashDeduction;
	Boolean m_checkCard;
	Boolean m_checkCash;
	Boolean m_checkPoint;
	//Button m_buttonPriceSearch;
		
	int m_qIndex = 0;
	int m_isTax = 0;
	int m_isCashR = 0;
	
	ProgressDialog dialog;
	NumberFormat m_numberFormat;
	
	HashMap<String, String> m_data = new HashMap<String, String>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_charge_customer_detail);
		
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
		
		m_period1 = (TextView)findViewById(R.id.textViewPeriod1);
		m_period2 = (TextView)findViewById(R.id.textViewPeriod2);
		m_customerCode = (TextView)findViewById(R.id.textViewCustomerCode);
		m_customerName = (TextView)findViewById(R.id.textViewCustomerName);
		
		m_numberFormat = NumberFormat.getInstance();

		m_contents = new TextView[27];
		
		m_contents[0] = (TextView)findViewById(R.id.content1);
		m_contents[1] = (TextView)findViewById(R.id.content2);
		m_contents[2] = (TextView)findViewById(R.id.content3);
		m_contents[3] = (TextView)findViewById(R.id.content4);
		m_contents[4] = (TextView)findViewById(R.id.content5);
		
		m_contents[5] = (TextView)findViewById(R.id.content6);
		m_contents[6] = (TextView)findViewById(R.id.content7);
		m_contents[7] = (TextView)findViewById(R.id.content8);
		m_contents[8] = (TextView)findViewById(R.id.content9);
		m_contents[9] = (TextView)findViewById(R.id.content10);
		
		m_contents[10] = (TextView)findViewById(R.id.content11);
		m_contents[11] = (TextView)findViewById(R.id.content12);
		m_contents[12] = (TextView)findViewById(R.id.content13);
		m_contents[13] = (TextView)findViewById(R.id.content14);
		m_contents[14] = (TextView)findViewById(R.id.content15);
		
		m_contents[15] = (TextView)findViewById(R.id.content16);
		m_contents[16] = (TextView)findViewById(R.id.content17);
		m_contents[17] = (TextView)findViewById(R.id.content18);
		m_contents[18] = (TextView)findViewById(R.id.content19);
		m_contents[19] = (TextView)findViewById(R.id.content20);
		
		m_contents[20] = (TextView)findViewById(R.id.content21);
		m_contents[21] = (TextView)findViewById(R.id.content22);
		m_contents[22] = (TextView)findViewById(R.id.content23);
		m_contents[23] = (TextView)findViewById(R.id.content24);
		
		//24 - 자카드 / 25 - 자_현영 / 26 - 수_현영
		m_contents[24] = (TextView)findViewById(R.id.content25);
		m_contents[25] = (TextView)findViewById(R.id.content26);
		m_contents[26] = (TextView)findViewById(R.id.content27);
		
		
		 //매출보기 옵션 설정
        /*SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
      	m_checkBoxCard = pref.getBoolean("checkCrad", true);		
        m_checkBoxCash = pref.getBoolean("checkCashBack", true);
      	m_checkBoxPoint = pref.getBoolean("checkPoint", true);
        m_isCashDeduction = pref.getBoolean("checkCash", true);
        m_editTextCashDeduction = pref.getString("checkCashPercent", "");*/
      	
		/*m_editTextCashDeduction = (EditText)findViewById(R.id.editTextCashDeduction);
		m_isCashDeduction = (CheckBox)findViewById(R.id.checkBoxIsCashDeduction);
		m_checkBoxCard =(CheckBox)findViewById(R.id.checkBoxCard);
		m_checkBoxCash =(CheckBox)findViewById(R.id.checkBoxCash);
		m_checkBoxPoint =(CheckBox)findViewById(R.id.checkBoxPoint);
		m_isCashDeduction.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				doCalculateGongjae();
			}
		});
		m_checkBoxCard.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				doCalculateGongjae();
			}
		});
		m_checkBoxCash.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				doCalculateGongjae();
			}
		});
		m_checkBoxPoint.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				doCalculateGongjae();
			}
		});
		m_buttonPriceSearch =(Button)findViewById(R.id.buttonPriceSearch);
		m_buttonPriceSearch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				doCalculateGongjae();
			}
		});*/
		
		Intent intent = getIntent();
		
		m_period1.setText(intent.getExtras().getString("PERIOD1"));
		m_period2.setText(intent.getExtras().getString("PERIOD2"));		
		m_customerCode.setText(intent.getExtras().getString("OFFICE_CODE"));
		m_customerName.setText(intent.getExtras().getString("OFFICE_NAME"));
				
		
	}

	private void doCalculateGongjae() {
		/*boolean isCard = m_checkBoxCard.isChecked();
		boolean isCashback = m_checkBoxCash.isChecked();
		boolean isPoint = m_checkBoxPoint.isChecked();
		boolean isCash = m_isCashDeduction.isChecked();*/
		
		boolean isCard = m_checkCard;
		boolean isCashback = m_checkCash;
		boolean isPoint = m_checkPoint;
		boolean isCash = m_isCashDeduction;
		
		double rsale = Double.valueOf(m_data.get("순매출"));
		double cashback = Double.valueOf(m_data.get("캐쉬백"));
		double cash = Double.valueOf(m_data.get("자_현영매출"));
		double card = Double.valueOf(m_data.get("카드수수료"));
		double point = Double.valueOf(m_data.get("포인트"));
		
		double su = Double.valueOf(m_data.get("수_카드금액"));
		double maejang = Double.valueOf(m_data.get("매장수수료"));
		
		String r = m_cashDeduction;
		
		double ratio = (r.equals(""))? 0: Double.valueOf(r);
		if (isCash) cash = cash * ratio / 100.0f;
		else cash =0;

		double m =su +maejang;
		if (isCard)   m += card;
		if (isCashback)   m += cashback;
		if (isPoint)   m += point;
		if (isCash)   m += cash;
		
		rsale -= m;
		
		//공제금액= 수카드금액 + 매장수수료 + (카드수수료 + 포인트  + 캐쉬백 + 현금영수증)
		//공제후지급액= 순매출 - 공제금액
		m_contents[20].setText(StringFormat.convertToNumberFormat(String.format("%.2f", cash)));	//현금영수공제액
		m_contents[21].setText(StringFormat.convertToNumberFormat(String.format("%.2f", m)));	//공제금액
		m_contents[22].setText(StringFormat.convertToNumberFormat(roundOff(rsale,0)));	//공제후지급액
	}
	
	private void doQueryToGetTotalSale() {

		String query ="";
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();
		int year1 = Integer.parseInt(period1.substring(0, 4));
		int month1 = Integer.parseInt(period1.substring(5, 7));

		int year2 = Integer.parseInt(period2.substring(0, 4));
		int month2 = Integer.parseInt(period2.substring(5, 7));

		query = "Select ISNULL(Sum(X.순매출합계),0) '순매출합계'"
				+ " From ("; 
				
		for ( int y = year1; y <= year2; y++ ) {
			for ( int m = month1; m <= month2; m++ ) {

				String tableName = String.format("SaD_%04d%02d", y, m);
				
				query += " Select Sum(A.TSell_Pri - A.Tsell_RePri) '순매출합계' " 
						+ " From "+tableName+" A Inner JOIN Office_Manage B" 
						+ " ON A.Office_Code=B.Office_Code" 
						+ " Where B.Office_Sec = '2' AND A.Sale_Date >= '" + period1 + "' AND A.Sale_Date <= '" + period2 + "'" ;

				query += " union all ";
			}
		}
		query = query.substring(0, query.length()-11);
		query += " ) X";

		new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				try {
					HashMap<String, String> a = JsonHelper.toStringHashMap(results.getJSONObject(0));

					String totalSale = a.get("순매출합계");
					doCalculate(totalSale);
					
				} catch (JSONException e) {
					e.printStackTrace();
				}		
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	private void doCalculate(String totalSale) {

		Intent intent = getIntent();
		String query ="";
		String customerCode = intent.getExtras().getString("OFFICE_CODE");
		String customerName = intent.getExtras().getString("OFFICE_NAME");
		String period1 = m_period1.getText().toString();
		String period2 = m_period2.getText().toString();
		int year1 = Integer.parseInt(period1.substring(0, 4));
		int month1 = Integer.parseInt(period1.substring(5, 7));

		int year2 = Integer.parseInt(period2.substring(0, 4));
		int month2 = Integer.parseInt(period2.substring(5, 7));
		
		query = "Select G.Office_Code,G.Office_Name, "
				+ " G.판매,G.반품,G.할인," 
				+ " G.순매출,G.과세매출,G.면세매출, "
				+ " G.현금매출, G.현금과세, G.현금면세, "
				+ " G.카드매출, G.카드과세, G.카드면세, G.자_카드매출, "
				+ " G.현영매출, G.현영과세, G.현영면세, G.자_현영매출, G.수_현영매출, "
				+ " G.수_카드금액, G.매장수수료, G.카드수수료, G.포인트, G.캐쉬백, "
				+ " (G.현영매출 * 0) / 100 '현영공제',  G.기타매출,"
				+ " 0 '공제금액', "
				+ " 0 '공제후지급액', "
				+ " G.이익금, G.이익률, G.기타매출,"
				+ " '점유율'=CASE WHEN "+totalSale+" <> 0 Then (순매출/"+totalSale+")*100  ELSE 0 End "
				+ " From (";
		
		
				
				query += " Select G.Office_Code,G.Office_Name,"
						+ "  Sum (G.판매) '판매',"
						+ "  Sum (G.반품) '반품',"
						+ "  Sum (G.할인) '할인',"
						+ "  Sum (G.순매출) '순매출', Sum(G.과세매출) '과세매출', Sum(G.면세매출) '면세매출',"
						+ "  Sum (G.현금매출) '현금매출', Sum(G.현금과세) '현금과세', Sum(G.현금면세) '현금면세',"
						+ "  Sum (G.카드매출) '카드매출', Sum(G.카드과세) '카드과세', Sum(G.카드면세) '카드면세', Sum(G.자_카드) '자_카드매출',"
						+ "  Sum (G.현영매출) '현영매출', Sum(G.현영과세) '현영과세', Sum(G.현영면세) '현영면세',  "
						+ "  Sum (G.자_현영) '자_현영매출', Sum (G.수_현영) '수_현영매출', "
						+ "  Sum (G.수_카드금액) '수_카드금액',"
						+ "  Sum (G.매장수수료) '매장수수료',"
						+ "  Sum (G.카드수수료) '카드수수료',"
						+ "  Sum (G.S_Point) '포인트',"
						+ "  Sum (G.S_CashBackPoint) '캐쉬백',"
						+ "  Sum (G.이익금) '이익금',"
						+ "  Sum(G.기타매출) '기타매출',"
						+ "  '이익률'=Case When Sum(G.이익금)=0 Or Sum(G.순매출)=0 Then 0 Else (Sum(G.이익금)/Sum(G.순매출))*100 End"
						+ "  From (";
						
						for ( int y = year1; y <= year2; y++ ) {
							for ( int m = month1; m <= month2; m++ ) {

								String tableName = String.format("%04d%02d", y, m);
						
						query += "   Select A.Office_Code, A.Office_Name,"
						+ "   '판매'=Sum(Case When A.Sale_Yn='1' Then A.TSell_Pri+A.Dc_Pri Else 0 End),"
						+ "        '반품'=Sum(Case When A.Sale_Yn='0' Then A.TSell_RePri+A.Dc_Pri Else 0 End),"
						+ "        '할인'=Sum(Case When A.Sale_Yn='1' Then A.DC_Pri Else A.DC_Pri *-1 End),"
						+ "        '순매출'=Sum (a.TSell_Pri - a.TSell_RePri),"
						+ "        '과세매출'=Sum(Case When A.Tax_YN='1' Then A.TSell_Pri-A.TSell_RePri Else 0 End),"
						+ "        '면세매출'=Sum(Case When A.Tax_YN='0' Then A.TSell_Pri-A.TSell_RePri Else 0 End),"
						+ "        Sum ((a.TSell_Pri - a.TSell_RePri) - a.Card_Pri) '현금매출',"						
						+ "        '현금과세'=Sum(Case When A.Tax_YN='1' Then (A.TSell_Pri-A.TSell_RePri)-A.Card_Pri Else 0 End),"
						+ "        '현금면세'=Sum(Case When A.Tax_YN='0' Then (A.TSell_Pri-A.TSell_RePri)-A.Card_Pri Else 0 End),"
						+ "        '카드매출'=Sum (a.Card_Pri),"
						+ "        '카드과세'=Sum(Case When A.Tax_YN='1' Then A.Card_Pri Else 0 End),"
						+ "        '카드면세'=Sum(Case When A.Tax_YN='0' Then A.Card_Pri Else 0 End),"
						+ "			'자_카드'=Sum(Case When A.Card_YN='0' Then A.Card_Pri Else 0 End),"
						+ "			'현영매출'=Sum(Case When C.Cash_No<>'' Then dCash_Pri Else 0 End) ,"         
				        + "			'현영과세'=Sum(Case When C.Cash_No<>'' AND A.TAx_YN='1' Then dCash_Pri Else 0 End) ,"         
				        + "			'현영면세'=Sum(Case When C.Cash_No<>'' AND A.TAx_YN='0' Then dCash_Pri Else 0 End) ,"						
						+ " 		'자_현영'=Sum(Case When C.Cash_No<>'' AND A.Cash_YN='0' Then A.dCash_Pri Else 0 End),"
						+ "			'수_현영'=Sum(Case When C.Cash_No<>'' AND A.Cash_YN='1' Then A.dCash_Pri Else 0 End), "         
						+ "			Sum (Round((c.Dec_Pri + c.CMS_Pri + c.Cus_PointUse + c.Sub_Pri + c.Gift_Pri + c.CashBack_PointUse + c.Cut_Pri + c.BC_Coupon_DC + c.BC_Card_DC ) * a.Money_Per, 4)) '기타매출',"
						+ "        '수_카드금액'=Sum(Case When A.Card_YN='1' Then A.Card_Pri Else 0 End),"
						+ "        '매장수수료'=Sum(Case When A.Sale_YN='1' Then (A.TSell_Pri+A.Dc_Pri)*(A.Fee/100) Else ((A.TSell_RePri+A.Dc_Pri)*(A.Fee/100))*-1 End),"
						+ "        '카드수수료'=Sum(Case When A.Card_YN='0' Then a.Card_Pri * (a.Card_Fee / 100) Else 0 End),"
						+ "        Sum(CASE WHEN A.S_Point=0 or C.CUS_POINT + C.CUT_POINT = 0 THEN 0 ELSE A.S_Point - ROUND(C.CUT_POINT * (A.S_POINT / (C.CUS_POINT + C.CUT_POINT)),4) END ) S_Point,"
						+ "        Sum(A.S_CashBackPoint) S_CashBackPoint ,"
						+ "        Sum (a.Profit_Pri) '이익금'"
						+ "        From SaD_"+tableName+" A LEFT JOIN  SaT_"+tableName+" C"
						+ "        ON A.Sale_Num=C.Sale_Num"
						+ "        LEFT JOIN Office_Manage B"
						+ "        ON A.Office_Code=B.Office_Code"        
						+ "        Where B.Office_Sec = '2'" 
						+ "   And A.Office_Code ='"+customerCode+"' "
						+ "   AND A.Sale_Date >= '"+period1+"' AND A.Sale_Date <= '"+period2+"'" 
						+ "   Group By A.Office_Code, A.Office_Name";
		
						query += " union all ";
			}
		}
		query = query.substring(0, query.length()-11);
		query += " ) G "
				+ " Group By G.Office_Code,G.Office_Name" 
				+ " ) G" 
				+ " ORDER BY G.Office_Code ;" ;
	
		new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				updateList(results);				
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	private void updateList(JSONArray results)
	{
		try {
			m_data = JsonHelper.toStringHashMap(results.getJSONObject(0));

			m_contents[0].setText(StringFormat.convertToNumberFormat(m_data.get("판매")));
			m_contents[1].setText(StringFormat.convertToNumberFormat(m_data.get("반품")));
			m_contents[2].setText(StringFormat.convertToNumberFormat(m_data.get("할인")));
			m_contents[3].setText(StringFormat.convertToNumberFormat(m_data.get("순매출")));
			m_contents[4].setText(StringFormat.convertToNumberFormat(m_data.get("과세매출")));
			m_contents[5].setText(StringFormat.convertToNumberFormat(m_data.get("면세매출")));
			m_contents[6].setText(StringFormat.convertToNumberFormat(m_data.get("현금매출")));
			m_contents[7].setText(StringFormat.convertToNumberFormat(m_data.get("현금과세")));
			m_contents[8].setText(StringFormat.convertToNumberFormat(m_data.get("현금면세")));
			m_contents[9].setText(StringFormat.convertToNumberFormat(m_data.get("카드매출")));
			m_contents[10].setText(StringFormat.convertToNumberFormat(m_data.get("카드과세")));
			m_contents[11].setText(StringFormat.convertToNumberFormat(m_data.get("카드면세")));
			m_contents[12].setText(StringFormat.convertToNumberFormat(m_data.get("현영매출")));
			m_contents[13].setText(StringFormat.convertToNumberFormat(m_data.get("현영과세")));
			m_contents[14].setText(StringFormat.convertToNumberFormat(m_data.get("현영면세")));
			m_contents[15].setText(StringFormat.convertToNumberFormat(m_data.get("수_카드금액")));
			m_contents[16].setText(StringFormat.convertToNumberFormat(String.format("%.2f", Float.valueOf(m_data.get("매장수수료")))));
			m_contents[17].setText(StringFormat.convertToNumberFormat(String.format("%.2f", Float.valueOf(m_data.get("카드수수료")))));
			m_contents[18].setText(StringFormat.convertToNumberFormat(m_data.get("포인트")));
			m_contents[19].setText(StringFormat.convertToNumberFormat(m_data.get("캐쉬백")));
			m_contents[20].setText(StringFormat.convertToNumberFormat(m_data.get("현영공제")));
			m_contents[21].setText(StringFormat.convertToNumberFormat(m_data.get("공제금액")));
			m_contents[22].setText(StringFormat.convertToNumberFormat(roundOff(Float.valueOf(m_data.get("공제후지급액")),0)));
			m_contents[23].setText(String.format("%.2f", Float.valueOf(m_data.get("점유율"))));
			m_contents[24].setText(StringFormat.convertToNumberFormat(m_data.get("자_카드매출")));
			m_contents[25].setText(StringFormat.convertToNumberFormat(m_data.get("자_현영매출")));
			m_contents[26].setText(StringFormat.convertToNumberFormat(m_data.get("수_현영매출")));
			
			
			
		} catch (JSONException e) {
			e.printStackTrace();
		}		
		doCalculateGongjae();
	}
	
	public String roundOff(double num, int point){

		return String.valueOf(Math.floor(num * Math.pow(10, point) + 0.5) / Math.pow(10, point));
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		 //매출보기 옵션 설정
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        m_checkCard = pref.getBoolean("checkCrad", true);		
        m_checkCash = pref.getBoolean("checkCashBack", true);
      	m_checkPoint = pref.getBoolean("checkPoint", true);
        m_isCashDeduction = pref.getBoolean("checkCash", true);
        m_cashDeduction = pref.getString("checkCashPercent", "");
        
        doQueryToGetTotalSale();
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.charge_customer_detail, menu);
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
		case R.id.action_sususettings: 
			startActivity(new Intent(this, TIPSSuSuPreferences.class));
			return true;	
		}
		return super.onOptionsItemSelected(item);
	}
}
