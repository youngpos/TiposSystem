package tipsystem.tips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import kr.co.tipos.tips.R;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.MSSQL4;
import tipsystem.utils.StringFormat;
import tipsystem.utils.UserPublicUtils;

public class ManageBaljuDetailActivity extends Activity implements OnItemClickListener{
	
	JSONObject m_shop;
	JSONObject m_userProfile;

	String posID = "P";

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

	String TAG = "발주목록 상세보기";
		
	ListView m_listPurchaseDetail;
	TextView m_textViewDate;
	TextView m_textViewjunpyo;
	EditText customerCode;

	TextView textviewpurpri;
	TextView textviewpurrepri;
	TextView textviewprofit;
	TextView textviewprofitrate;
	
	String mGubun; //매입목록[OrT]-매입등록["OrDPDA"] 구분
	
	//선택목록
	int mListPosition = -1;
	
	Context mContext;
	
	// fill in the grid_item layout
	SimpleAdapter adapter;

	ProgressDialog dialog;
	// prepare the list of all records
	List<HashMap<String, String>> fillMaps = new ArrayList<HashMap<String, String>>();

	UserPublicUtils upu;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_balju_detail);

		m_shop = LocalStorage.getJSONObject(this, "currentShopData");
		m_userProfile = LocalStorage.getJSONObject(this, "userProfile");
		
		mContext = this;
		upu = new UserPublicUtils(mContext);
		
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

			String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
			posID = LocalStorage.getString(this, "currentPosID:" + OFFICE_CODE);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		m_listPurchaseDetail = (ListView) findViewById(R.id.listviewPurchaseDetailList);

		// create the grid item mapping
		String[] from = new String[] { "바코드", "상품명", "발주가", "판매가", "수량" };
		int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5 };

		String Office_Name = getIntent().getStringExtra("Office_Name");
		String Office_Code = getIntent().getStringExtra("Office_Code");
		String purchaseDate = getIntent().getStringExtra("Or_Date");
		String purpri = StringFormat.convertToNumberFormat(getIntent().getStringExtra("In_Pri"));
		String purrepri = StringFormat.convertToNumberFormat(getIntent().getStringExtra("In_RePri"));
		String junpyo = getIntent().getStringExtra("In_Num");
		String profit_pri = getIntent().getStringExtra("Profit_Pri");
		String profit_rate = getIntent().getStringExtra("Profit_Rate");
		mGubun = getIntent().getStringExtra("Gubun");

		customerCode = (EditText) findViewById(R.id.editTextCustomerCode);
		EditText customerName = (EditText) findViewById(R.id.editTextCustomerName);
		textviewpurpri = (TextView) findViewById(R.id.textViewpurpri);
		textviewpurrepri = (TextView) findViewById(R.id.textViewpurrepri);
		textviewprofit = (TextView) findViewById(R.id.textViewprofit);
		textviewprofitrate = (TextView) findViewById(R.id.textViewprofitrate);

		m_textViewDate = (TextView) findViewById(R.id.textViewDate);
		m_textViewjunpyo = (TextView) findViewById(R.id.textViewjunpyo);

		customerCode.setText(Office_Code);
		customerName.setText(Office_Name);
		m_textViewDate.setText(purchaseDate);
		m_textViewjunpyo.setText(junpyo);
		customerCode.setEnabled(false);
		customerName.setEnabled(false);
		textviewpurpri.setText(purpri);
		textviewpurrepri.setText(purrepri);
		textviewprofit.setText(profit_pri);
		textviewprofitrate.setText(profit_rate);

		adapter = new SimpleAdapter(this, fillMaps, R.layout.activity_listview_purchase_detail_list, from, to);
		m_listPurchaseDetail.setAdapter(adapter);
		m_listPurchaseDetail.setOnItemClickListener(this);
		
		Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
		TextView textView = (TextView) findViewById(R.id.textView1);
		textView.setTypeface(typeface);

		textView = (TextView) findViewById(R.id.textView4);
		textView.setTypeface(typeface);

		m_textViewDate.setTypeface(typeface);

		textviewpurpri.requestFocus();
		
		if("OrT".equals(mGubun)){
			//발주장 상세보기
			evtMasterdoQuery();
		}else{
			//Temp_Indpda 상세보기
			evtMasterdoQueryIndPDA();
		}
	}

	// 전표별 상품 검색 하기 SQL QUERY 실행
	public void evtMasterdoQuery() {

		String period = m_textViewDate.getText().toString();
		String query = "";
		String junpyo = m_textViewjunpyo.getText().toString();

		int year1 = Integer.parseInt(period.substring(0, 4));
		int month1 = Integer.parseInt(period.substring(5, 7));

		String tableName = String.format("%04d%02d", year1, month1);
		query += " Select A.In_Seq, A.BarCode, B.G_Name, A.Pur_Pri, A.Sell_Pri, A.In_Count, "
				+ " A.TPur_Pri, A.TPur_Cost, A.TAdd_Tax, A.In_Pri, A.In_SellPri, A.Bot_Pri, "
				+ " A.Bot_SellPri, Profit_Rate=Case When A.In_SellPri=0 Then 0 Else ((A.In_SellPri - A.In_Pri)/A.In_SellPri)*100 End , "
				+ " A.Summarize " + " From OrD_" + tableName + " As A Inner Join Goods B On A.BarCode = B.BarCode "
				+ " Where A.In_Num = '" + junpyo + "' Order By A.In_Seq ";

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
					try {
						for (int i = 0; i < results.length(); i++) {
							JSONObject obj = results.getJSONObject(i);
							HashMap<String, String> map = new HashMap<String, String>();
							map.put("바코드", obj.getString("BarCode"));
							map.put("상품명", obj.getString("G_Name"));
							map.put("매입가", obj.getString("Pur_Pri"));
							map.put("판매가", obj.getString("Sell_Pri"));
							map.put("수량", obj.getString("In_Count"));

							map.put("매입가", StringFormat.convertToNumberFormat(map.get("매입가")));
							map.put("판매가", StringFormat.convertToNumberFormat(map.get("판매가")));
							fillMaps.add(map);
						}
						adapter.notifyDataSetChanged();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(getApplicationContext(), "조회 결과가 없습니다.", Toast.LENGTH_SHORT).show();
				}
				// fillMaps.notifyAll();
			}

			@Override
			public void onRequestFailed(int code, String msg) {

			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
	}

	// exec sp_executesql N'SELECT A.BarCode as BarCode,B.G_Name as
	// G_Name,A.In_Count asIn_Count,
	// REPLACE(CONVERT(VARCHAR,CONVERT(MONEY,ISNULL(A.in_Pri,0),2),1),''.00'','''')
	// as in_Pri FROM (select Office_Code,In_Date,BarCode,sum(In_Count) as
	// In_Count,sum(in_Pri) as in_Pri from Temp_InDPDA where
	// Office_Code=''00002'' and In_Date=''2017-03-28'' AND
	// SubString(In_Num,9,1) = ''H'' group by Office_Code,In_Date,BarCode) A
	// LEFT JOIN Goods B ON A.BarCode = B.BarCode'

	
	// 전표별 상품 검색 하기 SQL QUERY 실행
	public void evtMasterdoQueryIndPDA() {
		
		String query = "";
		
		String period = m_textViewDate.getText().toString();
		String office_code = customerCode.getText().toString();
		
		query = "SELECT "				
				+ " A.*, B.G_Name " 
				+ "From "
				+ "(select * "
				+ "From Temp_OrDPDA "
				+ "Where "
				+ "Office_Code='"+office_code+"' and Or_Date='"+period+"' AND "
				+ "SubString(Or_Num,9,1) = '"+posID+"' ) A "
				+ "LEFT JOIN Goods B ON A.BarCode = B.BarCode ";
		

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
					try {						
						
						float m매입가합 = 0; //매입가(공병포함) 합
						float m반품가합 = 0; //반품가(공병포함) 합
						
						//이익률 계산
						float m전체매입가 = 0;
						float m전체판매가 = 0;
												
						//전체 이익금 계산						
						float m전체이익금 = 0;
						
						
						for (int i = 0; i < results.length(); i++) {
							JSONObject obj = results.getJSONObject(i);
							HashMap<String, String> map = new HashMap<String, String>();
							
							String in_pri = obj.getString("In_Pri");		//매입가(공병포함)합							
							String in_sellpri = obj.getString("In_SellPri");	//판매가(공병포함)합							
												
							String pur_pri = obj.getString("Pur_Pri");	//단품매입가
							String sell_pri = obj.getString("Sell_Pri");	//단품판매가
							
							String profit_pri = obj.getString("Profit_Pri");  //이익금
							
							try{
								
								if(obj.getString("In_Count").contains("-")){
									m반품가합 += Float.parseFloat(in_pri);
								}else{
									m매입가합 += Float.parseFloat(in_pri);
								}
								
								m전체매입가 += Float.parseFloat(in_pri);
								m전체판매가 += Float.parseFloat(in_sellpri);
								m전체이익금 += Float.parseFloat(profit_pri);
								
							}catch(NumberFormatException e){
								e.printStackTrace();
							}
												
							map.put("바코드", obj.getString("BarCode"));
							map.put("상품명", obj.getString("G_Name"));
							map.put("수량", obj.getString("In_Count"));

							map.put("매입가", StringFormat.convertToNumberFormat(pur_pri));
							map.put("판매가", StringFormat.convertToNumberFormat(sell_pri));
							
							map.put("전표번호", obj.getString("Or_Num"));
							map.put("순번", obj.getString("Or_Seq"));
							map.put("매입원가", obj.getString("Pur_Cost"));
							map.put("부가세", obj.getString("Add_Tax"));
							map.put("공병매입가", obj.getString("Bot_Pri"));
							map.put("공병판매가", obj.getString("Bot_SellPri"));
							map.put("구분", obj.getString("In_YN"));
							
							map.put("총매입가", obj.getString("In_Pri"));
							map.put("총판매가", obj.getString("In_SellPri"));
							map.put("이익금", obj.getString("Profit_Pri"));
							map.put("이익률", obj.getString("Profit_Rate"));
							map.put("면과세", obj.getString("Tax_YN"));
							
							fillMaps.add(map);
						}
						adapter.notifyDataSetChanged();
						
						//금액 합치기
						textviewpurpri.setText(StringFormat.convertToNumberFormat(""+m매입가합)); //매입가
						textviewpurrepri.setText(StringFormat.convertToNumberFormat(""+m반품가합)); //반품가
						textviewprofit.setText(StringFormat.convertToNumberFormat(""+Math.abs(m전체이익금))); //이익금
						
						float profit_rate =((m전체판매가-m전체매입가)/m전체판매가)*100;
						Log.d("이익률", "("+m전체판매가+"-"+m전체매입가+") / "+m전체판매가+" * 100 = "+profit_rate);
						textviewprofitrate.setText(StringFormat.convertToRoundFormat(""+profit_rate)); //이익률
						
						
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(getApplicationContext(), "조회 결과가 없습니다.", Toast.LENGTH_SHORT).show();
				}
				//fillMaps.notifyAll();
			}

			@Override
			public void onRequestFailed(int code, String msg) {

			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	
	/**
	 * 매입 및 발주 
	 * 수량 및 삭제 기능
	 */
	public void getEditAmount(){
				
		if(mListPosition < 0 ){
			return;
		}
		
		HashMap<String, String> map = fillMaps.get(mListPosition);
		final String count = map.get("수량");

		//----------------------------------------//
		// 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
		//----------------------------------------//
		//AlertDialog.Builder ad = new AlertDialog.Builder(mContext);
		AlertDialog.Builder ad = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
		//----------------------------------------//

		ad.setTitle("목록수정");       // 제목 설정
		ad.setMessage("수량변경 및 삭제");   // 내용 설정
	
		// EditText 삽입하기
		final TextView ev = new TextView(getApplicationContext());
		ev.setText("수량변경");
		final EditText et = new EditText(getApplicationContext());

		//----------------------------------------//
		// 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
		//----------------------------------------//
		et.setTextColor(Color.BLACK);
		et.setTextSize(30);
		//----------------------------------------//

		et.setText(count);
		et.setInputType(EditorInfo.TYPE_CLASS_NUMBER|EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
		et.selectAll();		
		ad.setView(et);
		
		// 수량변경버튼 설정
		ad.setPositiveButton("수량변경", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        Log.v(TAG, "Yes Btn Click");
	
		        // Text 값 받아서 로그 남기기
		        String value = et.getText().toString();
		        Log.v(TAG, value);
		        
		        try{
		        	//수량 입력 없을 시
		        	int a = Integer.parseInt(value);
		        	
		        	if(a == 0){
		        		Toast.makeText(mContext, "매입 수량을 0개 이상 입력해 주세요!", Toast.LENGTH_SHORT).show();
		        		dialog.dismiss();
		        		upu.hideSoftKeyboard(false);
		        		return;
		        	}
		        	
		        }catch(NumberFormatException e){
		        	dialog.dismiss();		   
		        	upu.hideSoftKeyboard(false);
		        	Log.d(TAG, "수량오류");
		        	return;		        	
		        }
		        
		    	//수량변경 없을 시
		        if( count.equals(value.trim())){
		        	dialog.dismiss();		   
		        	upu.hideSoftKeyboard(false);
		        	Log.d(TAG, "입력변경없슴");
	        		return;
	        	}
		        
		        //수량변경
		        doChangeAmountPurpri(value);
						    
		        Log.d(TAG, "정상변경");
		        dialog.dismiss();     //닫기
		        upu.hideSoftKeyboard(false);
		        // Event
		    }
		});
	
		//삭제버튼 설정
		ad.setNeutralButton("삭제", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        Log.v(TAG,"Neutral Btn Click");
		        
		        //전표 삭제하기
		        doDeleteListPurpri();	        
		        
		        dialog.dismiss();     //닫기
		        upu.hideSoftKeyboard(false);
		        // Event
		    }
		});
	
		// 취소 버튼 설정
		ad.setNegativeButton("닫기", new DialogInterface.OnClickListener() {
		    @Override
		    public void onClick(DialogInterface dialog, int which) {
		        Log.v(TAG,"No Btn Click");
		        dialog.dismiss();     //닫기
		        upu.hideSoftKeyboard(false);
		        // Event
		    }
		});
		
		//창 띄우기
		ad.show();
		
		upu.hideSoftKeyboard(true);
		
	}
	
	//매입 수량변경
	public void doChangeAmountPurpri(String amount){
		
		//수량입력
		int mInCount = 0;
				
		//필요 Value 가져오기
		HashMap<String, String> map = fillMaps.get(mListPosition);
		
		//전표번호
		String m전표번호 = map.get("전표번호");
		//순번
		String m순번 = map.get("순번");
		
		//면과세
		String m면과세 = map.get("면과세");
		
		//구분 
		//String m구분 = map.get("구분");
		
		//기존수량
		int m기존수량 = 0;
		
		//단품 매입가
		float m매입가 = 0; 
		//단품 판매가
		float m판매가 = 0;
		
		//단품 매입원가
		float m매입원가 = 0;
		//단품 부가세
		float m부가세 = 0;
		
		//공병 매입가
		float m공병매입가 = 0;
		//공병 판매가
		float m공병판매가 = 0;
		
		try{
			
			//기존수량 - 공병가 계산 때문에 필요함
			m기존수량 = Integer.parseInt(map.get("수량"));
						
			//수량입력
			mInCount = Integer.parseInt(amount);
			
			//단품 매입가
			m매입가 = Float.parseFloat(map.get("매입가").replace(",", "")); 
			//단품 판매가
			m판매가 = Float.parseFloat(map.get("판매가").replace(",", ""));
			
			//단품 매입원가
			m매입원가 = Float.parseFloat(map.get("매입원가"));
			//단품 부가세
			m부가세 = Float.parseFloat(map.get("부가세"));
			
			//공병 매입가
			m공병매입가 = Float.parseFloat(map.get("공병매입가"));
			//공병 판매가
			m공병판매가 = Float.parseFloat(map.get("공병판매가"));
			
		}catch(NumberFormatException e){
			e.printStackTrace();
			Toast.makeText(mContext, "1번 숫자변환중 오류발생", Toast.LENGTH_SHORT).show();
			return;
		}		
		
		//변경해야하는 값		
		//매입가 합
		float tPur_Pri = 0;
		//매입가 합(공병포함)
		float in_Pri = 0;				
		//토탈 매입원가
		double tPur_Cost = 0;
		//토탈 부가세
		double tAdd_tax = 0;	
		//토탈 판매가
		float tSell_Pri = 0;
		//토탈 판매가(공병포함)
		float in_SellPri = 0;
		//공병매입가
		float bot_Pri = 0;
		//공병판매가
		float bot_SellPri = 0;				
		//이익금 (In_SellPri-In_Pri)   
		float profit_Pri = 0;
		
		
		try{
			
			//공병매입가
			bot_Pri = m공병매입가 / m기존수량;
			bot_Pri = Math.abs(bot_Pri) * mInCount;
			
			//공병판매가
			bot_SellPri = m공병판매가 / m기존수량;
			bot_SellPri = Math.abs(bot_SellPri) * mInCount;
			
			//부가세 별도 때문에 원가+부가세 가격으로 계산 합니다.
			float pur_pri = m매입원가 + m부가세;
			
			//매입가 합			
			tPur_Pri = pur_pri * mInCount;
			
			//매입가 합(공병포함) + 공병합
			in_Pri = bot_Pri + ( pur_pri  * mInCount );
					
			
			//부과세 계산
			if("0".equals(m면과세)){
				tPur_Cost = tPur_Pri;
				tAdd_tax = 0;
			}else{
				//토탈 매입원가
				tPur_Cost = tPur_Pri / 1.1;
				
				//토탈 부가세
				tAdd_tax = (tPur_Pri / 1.1) * 0.1;
			}
						
			//토탈 판매가
			tSell_Pri = m판매가 * mInCount;
			
			//토탈 판매가(공병포함) + 공병합
			in_SellPri = bot_SellPri + ( m판매가 * mInCount );
						
			//이익금 (In_SellPri-In_Pri)
			//profit_Pri = tSell_Pri - tPur_Pri;
			profit_Pri = ( m판매가 - m매입가 ) * mInCount;
			
		}catch(NumberFormatException e){
			e.printStackTrace();
			Toast.makeText(mContext, "2번 숫자 형식오류 입니다.", Toast.LENGTH_SHORT).show();
			return;
		}
		
		/*//구분변경
		int gubun = Integer.parseInt(m구분);		
		if(mInCount < 0 && m기존수량 >= 0){	 //기존은 + 매입  현재는 - 반품
			//In_YN --(1:매입, 0:반품, 2:행사반품, 3:행사매입 (Default 1))    
			switch(gubun){
			case 1:
				m구분 = "0";
				break;
			case 3:
				m구분 = "2";
				break;
			}			
		}*/
		
		/*if( mInCount >= 0 && m기존수량 < 0 ){
			//In_YN --(1:매입, 0:반품, 2:행사반품, 3:행사매입 (Default 1))    
			switch(gubun){
			case 0:
				m구분 = "1";
				break;
			case 2:
				m구분 = "3";
				break;
			}		
		}*/
		
		//변경된 내용 리스트에 적용하기		
		//수량변경
		map.put("수량", ""+ mInCount);
		//공병매입가
		map.put("공병매입가", ""+bot_Pri);
		//공병판매가
		map.put("공병판매가", ""+bot_SellPri);
		//총매입가
		map.put("총매입가", ""+in_Pri);
		//총판매가
		map.put("총판매가", ""+in_SellPri);
		//이익금
		map.put("이익금", ""+profit_Pri);
		//구분
		//map.put("구분", m구분);
		
		fillMaps.set(mListPosition, map);
		    
		String query = "Update Temp_OrDPDA Set "
				//+ "Or_YN='"+m구분+"', "
				+ "In_Count='"+mInCount+"', "
				+ "TPur_Pri='"+tPur_Pri+"', "
				+ "TPur_Cost='"+String.format("%.2f", tPur_Cost)+"', "
				+ "TAdd_Tax='"+String.format("%.2f", tAdd_tax)+"', "
				+ "In_Pri='"+in_Pri+"', "
				+ "TSell_Pri='"+tSell_Pri+"', "
				+ "In_SellPri='"+in_SellPri+"', "
				+ "Profit_Pri='"+profit_Pri+"', "
				+ "Bot_Pri='"+bot_Pri+"', "
				+ "Bot_SellPri='"+bot_SellPri+"' "
				+ "Where Or_Num='"+m전표번호+"' and Or_Seq='"+m순번+"' ";
				
		// 로딩 다이알로그
		dialog = new ProgressDialog(this);
		dialog.setMessage("Loading....");
		dialog.setCancelable(false);
		dialog.show();

		// 콜백함수와 함께 실행
		new MSSQL4(new MSSQL4.MSSQL4CallbackInterface() {

			@Override
			public void onRequestCompleted(Integer results) {
				dialog.dismiss();
				dialog.cancel();
				
				Log.d(TAG, "수량변경 결과 : "+results);				
				adapter.notifyDataSetChanged();
				
				mListPosition = -1;
				
				//판매가 이익률 재계산
				totalCalcurater();
				
				Toast.makeText(getApplicationContext(), "저장완료.", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "전송실패(" + String.valueOf(code) + "):" + msg, Toast.LENGTH_SHORT)
						.show();
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
		
		
	}
	
	
	//매입 삭제
	public void doDeleteListPurpri(){
				
		//마지막 한가지 남은 상품은 삭제 할수 없습니다.
		if(fillMaps.size() == 1){
			Toast.makeText(mContext, "마지막 상품은 전 단계인 \r\n매입목록에서 삭제해 주세요", Toast.LENGTH_SHORT).show();
			return;
		}
		
		//선택된 상품번호 및 전표 번호로 상품을 삭제 합니다.
		HashMap<String, String> map = fillMaps.get(mListPosition);
		String or_num = map.get("전표번호");
		String or_seq = map.get("순번");
		
		String query = "Delete From Temp_OrDPDA Where or_num='"+or_num+"' and or_seq='"+or_seq+"' ";
		
		// 콜백함수와 함께 실행
		new MSSQL4(new MSSQL4.MSSQL4CallbackInterface() {

			@Override
			public void onRequestCompleted(Integer results) {
				//dialog.dismiss();
				//dialog.cancel();

				Log.d(TAG, "삭제 결과 : "+results);
				
				fillMaps.remove(mListPosition);		
				adapter.notifyDataSetChanged();
				
				mListPosition = -1;
				
				//판매가 이익률 재계산
				totalCalcurater();
				
				Toast.makeText(mContext, "삭제완료", Toast.LENGTH_SHORT).show();
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				//dialog.dismiss();
				//dialog.cancel();
				Toast.makeText(mContext, "삭제 실패(" + String.valueOf(code) + "):" + msg, Toast.LENGTH_SHORT).show();
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
		
		
	}
	
	//매입가 이익률 계산하기	
	public void totalCalcurater(){
		
		//매입가 반품가 재계산 및 전체 이익금 이익률 재계산 합니다.
		Iterator<HashMap<String, String>> itr = fillMaps.iterator();
		
		int m매입가합 = 0; //매입가(공병포함) 합
		int m반품가합 = 0; //반품가(공병포함) 합
		
		//이익률 계산
		float m전체매입가 = 0;
		float m전체판매가 = 0;
						
		//전체 이익금 계산						
		float m전체이익금 = 0;
		
		while(itr.hasNext()){
		
			HashMap<String, String> map = itr.next();
			
			String in_pri = map.get("총매입가").replace(",", "");		//매입가(공병포함)합							
			String in_sellpri = map.get("총판매가").replace(",", "");	//판매가(공병포함)합							
			
			String profit_pri = map.get("이익금").replace(",", "");  //이익금
					
			try{
			
				if(map.get("수량").contains("-") ){
					m반품가합 += Float.parseFloat(in_pri);
				}else{
					m매입가합 += Float.parseFloat(in_pri);
				}
				
				m전체매입가 += Float.parseFloat(in_pri);
				m전체판매가 += Float.parseFloat(in_sellpri);
				m전체이익금 += Float.parseFloat(profit_pri);
								
			}catch(NumberFormatException e){
				Toast.makeText(mContext, "숫자 형식 오류", Toast.LENGTH_SHORT).show();
				return;				
			}
			
		}
		
		//금액 합치기
		textviewpurpri.setText(StringFormat.convertToNumberFormat(""+m매입가합)); //매입가
		textviewpurrepri.setText(StringFormat.convertToNumberFormat(""+m반품가합)); //반품가
		textviewprofit.setText(StringFormat.convertToNumberFormat(""+m전체이익금)); //이익금
		
		float profit_rate =((m전체판매가-m전체매입가)/m전체판매가)*100;
		Log.d("이익률", "("+m전체판매가+"-"+m전체매입가+") / "+m전체판매가+" * 100 = "+profit_rate);
		textviewprofitrate.setText(StringFormat.convertToRoundFormat(""+profit_rate)); //이익률
		
	}
	
	
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {

			ActionBar actionbar = getActionBar();
			actionbar.setDisplayShowHomeEnabled(false);
			actionbar.setDisplayShowTitleEnabled(true);
			actionbar.setDisplayShowCustomEnabled(true);
			actionbar.setTitle("발주 상세보기");

			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.purchase_detail, menu);
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

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
	
		mListPosition = position;
		
		//매입등록 시에만 수량변경
		if(!"OrT".equals(mGubun)){
			getEditAmount();
		}
	}
	
	
	
}
