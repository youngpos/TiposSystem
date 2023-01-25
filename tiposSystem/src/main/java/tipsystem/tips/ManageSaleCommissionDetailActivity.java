package tipsystem.tips;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class ManageSaleCommissionDetailActivity extends Activity {

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
	//String m_OFFICE_NAME ="";
		
	ListView m_listSalesTab1;		
	SimpleAdapter adapter1;	
	
	List<HashMap<String, String>> mfillMaps1 = new ArrayList<HashMap<String, String>>();	
		
	ProgressDialog dialog;	
	JSONArray m_results;
	
	//회원구매시 보여줄 리스트화면
	LinearLayout m_member_list;	
	
	//넘어온 저장변수
	String m_sale_date, m_sale_time, m_pos_number, m_slae_num;	
	String m_casher;
	
	//회원정보		
	String m_cuscode;
	
	// Header, Footer 생성 및 등록
    View header;
    View footer;
	    
    // 넘겨온값 구분
    String m_gubun;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_managesale_commission_detail);
		
		m_shop = LocalStorage.getJSONObject(this, "currentShopData");
		m_userProfile = LocalStorage.getJSONObject(this, "userProfile"); 
		
		// Header, Footer 생성 및 등록
        header = getLayoutInflater().inflate(R.layout.activity_listview_item6_header, null, false);
        footer = getLayoutInflater().inflate(R.layout.activity_listview_item6_footer, null, false);
		
		//상단 출력 변수 선언
		String customernumber;
		String cutomeraddress, customerceoname, customerphone;
		
		m_gubun = getIntent().getStringExtra("GUBUN");
		
		//매장 정보 상단 출력
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


			//사업자 번호
	        customernumber = m_shop.getString("Office_Num").toString();	       
	        TextView m_customernumber = (TextView) header.findViewById(R.id.text_customernumber);
	        m_customernumber.setText(customernumber.toString());
	        
	        //주소
	        cutomeraddress = m_shop.getString("Address1").toString();	        
	        TextView m_cutomeraddress = (TextView)header.findViewById(R.id.text_customeraddress);
	        m_cutomeraddress.setText(cutomeraddress.toString());
	        
	        //대표자명
	        customerceoname = m_shop.getString("Owner_Name").toString();	        
	        TextView m_customerceoname = (TextView)header.findViewById(R.id.text_customerceoname);
	        m_customerceoname.setText(customerceoname.toString());
	        
	        //전화번호
	        customerphone = m_shop.getString("Office_Tel1").toString();	 	        
	        TextView m_customerphone = (TextView)header.findViewById(R.id.text_customerphone);
	        m_customerphone.setText(customerphone);
	        
			m_APP_USER_GRADE =m_userProfile.getString("APP_USER_GRADE");
		} catch (JSONException e) {
			e.printStackTrace();
		}      
        
        //넘겨온값 받기
        m_sale_date = getIntent().getStringExtra("SALE_DATE");        
        m_sale_time = getIntent().getStringExtra("SALE_TIME");
        m_pos_number = getIntent().getStringExtra("POS_NUMBER");
        m_slae_num = m_sale_date+m_pos_number+getIntent().getStringExtra("SALE_NUM");
        //Card_Yn = getIntent().getStringExtra("CARD_YN");
        
        //포스번호      
        TextView m_textPosnum = (TextView) header.findViewById(R.id.text_posnumber);
        m_textPosnum.setText(m_pos_number.toString());
        
        //판매일자        
        TextView m_textSaledate = (TextView) header.findViewById(R.id.text_saledate);
        m_textSaledate.setText(m_sale_date.toString());
                
        //판매시간        
        TextView m_textSaletime = (TextView) header.findViewById(R.id.text_saletime);
        m_textSaletime.setText(m_sale_time.toString());  
        
        //전표번호        
        TextView m_textSalenum = (TextView) header.findViewById(R.id.text_sale_num);
        m_textSalenum.setText(m_slae_num.toString());  
        
        m_listSalesTab1= (ListView)findViewById(R.id.commission_list);
		  
        m_listSalesTab1.addHeaderView(header);
        m_listSalesTab1.addFooterView(footer);

        
        
		String[] from1 = new String[] {"G_Name", "Barcode", "Sell_Pri", "Sale_Count" };
	    int[] to1 = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4 };
        
		adapter1 = new SimpleAdapter(this, mfillMaps1, R.layout.activity_listview_item6_1, from1, to1);		
		m_listSalesTab1.setAdapter(adapter1);	
        
                
        //일반 최고관리자일경우
        m_OFFICE_CODE = getIntent().getStringExtra("OFFICE_CODE");
        //m_OFFICE_NAME = getIntent().getStringExtra("OFFICE_NAME");
        
        //수수료매장의 경우 
        if (m_APP_USER_GRADE.equals("2")) {
			try {
				m_OFFICE_CODE = m_userProfile.getString("OFFICE_CODE");
				//m_OFFICE_NAME = m_userProfile.getString("OFFICE_NAME");
			} catch (JSONException e) {
				e.printStackTrace();
			}   	      	
        }
        
        queryList1();
	}
	
	private void queryList1()
	{
		//mfillMaps1.removeAll(mfillMaps1);
		String year = m_sale_date.substring(0, 4);
		String month = m_sale_date.substring(5, 7);
		String sale_date = year+month;
		
		String query = "";
	    		
		query = " select Barcode, G_Name, Tax_Yn, Sale_Count, Sell_Pri from sad_"+sale_date+" where sale_num = '"+m_slae_num+"' ";
		
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();
 		
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				
				dialog.dismiss();
				dialog.cancel();
				if ( results.length() > 0 ) 
				updateList1(results);
				queryList2();
				adapter1.notifyDataSetChanged();
				//Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();	
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				adapter1.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();				
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	private void updateList1(JSONArray results)
	{				
		
		for(int index = 0; index < results.length() ; index++) {
			
			try {
				JSONObject son = results.getJSONObject(index);
				HashMap<String, String> map = JsonHelper.toStringHashMap(son);
				//Barcode, G_Name, sale_count, sell_pri, writer,
				map.put("Sell_Pri", StringFormat.convertToIntNumberFormat(map.get("Sell_Pri")));
				String tax = map.get("Tax_Yn").toString();							
				if (tax.equals("0")){
				map.put("G_Name", "* "+map.get("G_Name") );
				}
				mfillMaps1.add(map);								
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
		
	private void queryList2()
	{
		
		String year = m_sale_date.substring(0, 4);
		String month = m_sale_date.substring(5, 7);
		String sale_date = year+month;
		
		String query = "";
	    		
		query = " select   A.Cus_Code, A.TSell_Pri, ISNULL(A.DC_Pri,0) '할인', ISNULL(A.Dec_Pri,0) '외상' , ISNULL(A.Cash_Pri,0) 'Cash_Pri', A.Card_No, " +
				" ISNULL(A.Card_Pri,0) 'Card_Pri', A.Cus_Point, A.Cus_PointUse, A.Writer, A.AF_TPOINT, A.BE_POINT,(A.AF_TPOINT + A.BE_POINT )  BE_TPOINT, A.BE_TDEC, " +
				" case When B.과세합 <> 0 Then (B.과세합/1.1) else 0 end '과세합', case When B.과세합 <> 0 Then (B.과세합-( B.과세합/1.1)) else 0 end '부가세', ISNULL(B.면세합,0) '면세합', C.Cus_Name,  case When B.과세합 <> 0 Then (B.과세합-( B.과세합/1.1)) else 0 end '부가세', A.SU_CardPri, ( A.Card_Pri-A.Su_CardPri ) '매장카드', " +
				" '회원등급'= case When C.Cus_Class = '1' Then '일반' When C.Cus_Class = '2' Then '실버' When C.Cus_Class = '3' Then '골드' When C.Cus_Class = '4' Then 'VIP' else '기타' end " +
				" from sat_"+sale_date+" A LEFT JOIN ( " +
				" select sale_num, SUM(case When Tax_Yn = '1' Then (TSell_Pri-TSell_RePri) else 0 end) '과세합'," +
				"  SUM(case When Tax_Yn = 0 Then (TSell_Pri-TSell_RePri) else 0 end ) '면세합' " +
				" from sad_"+sale_date+" where sale_num='"+m_slae_num+"' group by sale_num " +				
				" ) B ON A.sale_num=B.sale_Num " +
				" LEFT JOIN Customer_Info C ON A.Cus_Code=C.Cus_Code " +
				" where A.sale_num='"+m_slae_num+"' ";
		
		// 로딩 다이알로그 
    	/*dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();*/
 		
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
			/*	dialog.dismiss();
				dialog.cancel();*/
				if (results.length()>0)
					updateList2(results);
				
				Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();	
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				/*dialog.dismiss();
				dialog.cancel();				*/
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
				
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}		
	
	private void updateList2(JSONArray results)
	{				
		
		for(int index = 0; index < results.length() ; index++) {
			
			try {
				JSONObject son = results.getJSONObject(index);
				HashMap<String, String> map = JsonHelper.toStringHashMap(son);
				//Barcode, G_Name, sale_count, sell_pri, writer,
				
				TextView m_commissiontaxN = (TextView)footer.findViewById(R.id.text_commissiontaxN);
				m_commissiontaxN.setText(StringFormat.convertToNumberFormat(map.get("면세합").toString()));
				TextView m_commissiontaxY = (TextView)footer.findViewById(R.id.text_commissiontaxtY);
				m_commissiontaxY.setText(StringFormat.convertT00NumberFormat(map.get("과세합").toString()));
				TextView m_commissiontax = (TextView)footer.findViewById(R.id.text_commissiontax);
				m_commissiontax.setText(StringFormat.convertT00NumberFormat(map.get("부가세").toString()));
				
				//사원
				TextView m_casher = (TextView)header.findViewById(R.id.text_casher);
				m_casher.setText(map.get("Writer").toString());											
				
				//합계
				TextView m_commissionselltotal = (TextView)footer.findViewById(R.id.text_commissionselltotal);
				m_commissionselltotal.setText(StringFormat.convertToIntNumberFormat(map.get("TSell_Pri").toString()));
				
				//할인
				String dc_pri = StringFormat.convertToIntNumberFormat(map.get("할인"));
				if(!dc_pri.equals("0")){
				TextView m_commissionDC = (TextView)footer.findViewById(R.id.text_commissionDC);
				m_commissionDC.setText(StringFormat.convertToIntNumberFormat(map.get("할인")));
				}else{
					LinearLayout li = (LinearLayout)findViewById(R.id.linear_commissionDC);
					li.setVisibility(View.GONE);
				}
				
				//외상
				String dec_pri = StringFormat.convertToIntNumberFormat(map.get("외상"));
				if( !dec_pri.equals("0") ){
					TextView m_commissionDec = (TextView)footer.findViewById(R.id.text_commissionDec);
					m_commissionDec.setText(StringFormat.convertToIntNumberFormat(map.get("외상")));
				}else{
					LinearLayout li = (LinearLayout)findViewById(R.id.linear_commissionRec);
					li.setVisibility( View.GONE );
				}
				
				//현금
				TextView m_commissioncash = (TextView)footer.findViewById(R.id.text_commissioncash);
				m_commissioncash.setText(StringFormat.convertToIntNumberFormat(map.get("Cash_Pri").toString()));
							
				//카드							
				String cardpri = StringFormat.convertToNumberFormat(map.get("Card_Pri"));
				if( !cardpri.equals("0") ){
					TextView m_commissioncard = (TextView)footer.findViewById(R.id.text_commissioncard);
					m_commissioncard.setText(StringFormat.convertToIntNumberFormat(map.get("Card_Pri").toString()));					
					queryList3();					
				}else{
					LinearLayout li = (LinearLayout)findViewById(R.id.list_commissionsucard);
					li.setVisibility( View.GONE );					
				}
				
				//수카드
				String su_cardpri = StringFormat.convertToNumberFormat(map.get("SU_CardPri"));
				if( !su_cardpri.equals("0") ){
					TextView m_commissionsucard = (TextView)footer.findViewById(R.id.text_commissionsucard);
					m_commissionsucard.setText(StringFormat.convertToNumberFormat(map.get("SU_CardPri")));
					
					TextView m_commissionjacard = (TextView)footer.findViewById(R.id.text_commissionjacard);
					m_commissionjacard.setText(StringFormat.convertToNumberFormat(map.get("매장카드")));					
				}else{
					LinearLayout li = (LinearLayout)findViewById(R.id.list_commissionsucard);
					li.setVisibility( View.GONE );
					LinearLayout li1 = (LinearLayout)findViewById(R.id.list_commissionjacard);
					li1.setVisibility( View.GONE );
				}
				
				
				//회원정보
				String member = map.get("Cus_Code");
				if(member.equals("")){
					LinearLayout li = (LinearLayout)findViewById(R.id.textView_member_list);
					li.setVisibility( View.GONE );
				}else{
				
					TextView m_commissionmenbercode = (TextView)footer.findViewById(R.id.text_commissionmembercode);
					m_commissionmenbercode.setText(map.get("Cus_Code").toString());			
					
					TextView m_commissionmembername = (TextView)footer.findViewById(R.id.text_commissionmembername);
					m_commissionmembername.setText(map.get("Cus_Name").toString());	
					
					TextView m_commissionmenbergrade = (TextView)footer.findViewById(R.id.text_commissionmembergrade);
					m_commissionmenbergrade.setText(map.get("회원등급").toString());	
					
					TextView m_commissionmenberbeforpoint = (TextView)footer.findViewById(R.id.text_memberbeforpoint);
					m_commissionmenberbeforpoint.setText(map.get("AF_TPOINT").toString());
					
					TextView m_commissionmenberpointplus = (TextView)footer.findViewById(R.id.text_memberpointplus);
					m_commissionmenberpointplus.setText(map.get("BE_POINT").toString());
					
					TextView m_commissionmenberpointuse = (TextView)footer.findViewById(R.id.text_memberpointuse);
					m_commissionmenberpointuse.setText(map.get("BE_TDEC").toString());
					
					TextView m_commissionmenberafterpoint = (TextView)footer.findViewById(R.id.text_memberafterpoint);
					m_commissionmenberafterpoint.setText(map.get("BE_TPOINT").toString());
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void queryList3()
	{		
		String query = "";
		
		query = " select C_SaleDate '거래일자', C_AppDate '승인일자', C_Month '할부', Office_Code '매장정보', "
				+ " C_CardNumber '카드번호', C_Price '승인금액', C_AppNumber '승인번호', C_CardName '카드사명', C_OfficeNo '가맹번호' "
				+ " from Card_log " 
				+ " where Office_Code Like '%%' and C_JeonPyo Like '%"+m_slae_num+"%' ";
		
		/*// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();*/
 		
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
			/*	dialog.dismiss();
				dialog.cancel();*/
				if (results.length()>0)
					updateList3(results);				
				//Toast.makeText(getApplicationContext(), "조회 완료: " + results.length(), Toast.LENGTH_SHORT).show();	
			}

			@Override
			public void onRequestFailed(int code, String msg) {
			/*	dialog.dismiss();
				dialog.cancel();				*/
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();			
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}		
	
	private void updateList3(JSONArray results)
	{						
		for(int index = 0; index < results.length() ; index++) {			
			try {
				JSONObject son = results.getJSONObject(index);
				HashMap<String, String> map = JsonHelper.toStringHashMap(son);				
				
				//footer = getLayoutInflater().inflate(R.layout.activity_listview_item6_footer, null, false);
				View view_cardlist = getLayoutInflater().inflate(R.layout.activity_listview_item6_footer_cardlist, null);
				TextView text_cardList = (TextView) view_cardlist.findViewById(R.id.text_price);
				text_cardList.setText(StringFormat.convertToIntNumberFormat(map.get("승인금액").toString()));
				
				TextView text_month = (TextView) view_cardlist.findViewById(R.id.text_month);
				text_month.setText(map.get("할부").toString());
				
				TextView text_cardnumber = (TextView) view_cardlist.findViewById(R.id.text_cardnumber);				
				String cardnum = map.get("카드번호").toString();
				/*if ( cardnum.length() > 1 && cardnum.length() < 16 ){
					text_cardnumber.setText( cardnum.substring(0, 11)+"****" );
				}else if ( cardnum.length() == 16 ) {
					text_cardnumber.setText( cardnum.substring(0, 12)+"****" );
				}else{
					text_cardnumber.setText( cardnum);					
				}*/	
				
				text_cardnumber.setText(cardnum);
				
				TextView text_cardname = (TextView) view_cardlist.findViewById(R.id.text_cardname);
				text_cardname.setText(map.get("카드사명").toString());
				
				TextView text_appnumber = (TextView) view_cardlist.findViewById(R.id.text_appnumber);
				text_appnumber.setText(map.get("승인번호").toString());
				
				TextView text_appdate = (TextView) view_cardlist.findViewById(R.id.text_appdate);
				text_appdate.setText(map.get("승인일자").toString());
				
				TextView text_officenum = (TextView) view_cardlist.findViewById(R.id.text_officenum);
				text_officenum.setText(map.get("가맹번호").toString());
				
				TextView text_officecode = (TextView) view_cardlist.findViewById(R.id.text_officecode);
				if(map.get("매장정보").equals("xxxxx")){
					text_officecode.setText("마트매출");
				}else{
					text_officecode.setText(map.get("매장정보").toString());
				}
				/*// activity_main.xml에서 정의한 LinearLayout 객체 할당       
		        LinearLayout inflatedLayout = (LinearLayout)footer.findViewById(R.id.layout_card_list);		        
		        // LayoutInflater 객체 생성
		        LayoutInflater inflater =  (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);		        
		        // Inflated_Layout.xml로 구성한 레이아웃을 inflatedLayout 영역으로 확장
		        //inflater.inflate(view_cardlist, inflatedLayout);
		        */
				m_listSalesTab1.addFooterView(view_cardlist);													
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
		}
	}
	
}
