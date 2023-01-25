package tipsystem.tips;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import tipsystem.utils.DBAdapter;
import kr.co.tipos.tips.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class BarcodePrintHistoryListActivity extends Activity implements DatePickerDialog.OnDateSetListener{
		
	private static final String TAG="BarcodePrintHistoryList";
	
	//매장코드
	String m_OfficeCode;
	
	//날자 설정관련
	DatePicker m_datePicker;
	Button m_buttonSetDate;
		 
	SimpleDateFormat m_dateFormatter;
	Calendar m_dateCalender1;
	Calendar m_dateCalender2;
	NumberFormat m_numberFormat;
	
	//재발행 버튼
	Button m_buttonSend;
	Button closeButton;
	
	//목록
	//ListView m_listPurchaseList;
	ListView barcodePrintHistoryListView;

	//리스트목록 필요한 것들
	SimpleAdapter adapter;
	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();
	HashMap<String, String> m_tempInNum = new HashMap<String, String>();
	
	Builder listDialog;
	
	//SQLite3 어뎁터
	DBAdapter dba;
	
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_barcodeprint_history);
				
		try{
			m_OfficeCode = getIntent().getStringExtra("OfficeCode");
		}catch(NullPointerException e){
			e.printStackTrace();						
		}
		
		//첫번째 context, 두번째 데이터베이스명(String) 
        Log.i(TAG, m_OfficeCode+".tips");
        dba = new DBAdapter(getApplicationContext(), m_OfficeCode+".tips");
		
		//목록 할당 하기
		barcodePrintHistoryListView = (ListView)findViewById(R.id.barcode_print_history_listview);
		// 2021.01.08.김영목. 리스트에서 출력 추가
		barcodePrintHistoryListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

				final String baprintNum = mfillMaps.get(i).get("BaPrint_Num");

				// 재발행 확인 박스
				//----------------------------------------//
				// 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
				//----------------------------------------//
				//AlertDialog.Builder builder = new AlertDialog.Builder(BarcodePrintHistoryListActivity.this);
				AlertDialog.Builder builder = new AlertDialog.Builder(BarcodePrintHistoryListActivity.this,AlertDialog.THEME_HOLO_LIGHT);
				//----------------------------------------//
				builder.setTitle("재발행 목록");
				builder.setMessage("재발행 리스트로 불러오시겠습니까?");
				builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						setResults(baprintNum);
					}
				});
				builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				});
				builder.show();

			}
		});
		
		//날자 설정 관련
		m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
		m_numberFormat = NumberFormat.getInstance();		
		m_dateCalender1 = Calendar.getInstance();
		m_dateCalender2 = Calendar.getInstance();
		
		m_buttonSetDate = (Button) findViewById(R.id.buttonSetDate);
		m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));
		//날자 설정 관련 끝
				
		//리스트뷰 목록 생성 어댑터
		String[] from = new String[] {"BaPrint_Num", "Count", "Print_Date", "BarCode", "G_Name", "Sell_Pri" };		
		int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6 };
		
		adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_item3_1, from, to);
		barcodePrintHistoryListView.setAdapter(adapter);
		
		m_buttonSend = (Button)findViewById(R.id.buttonSend);
		m_buttonSend.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				sendReprint();
			}			
		});
		closeButton = (Button)findViewById(R.id.close_button);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				finish();
			}
		});
		
		//시작시 검색
		sendRequestList();
	}

	//자료 호출하기
	private void sendRequestList(){
		String date = m_buttonSetDate.getText().toString();
				
		/*String	query = "Select Sum(cast(Count as integer)) Counts, BaPrint_Num, Print_Date From BaPrint_History "
				+ "Where Print_Date='"+date.toString()+"' "
				+ "Group By BaPrint_Num, Print_Date "; */
		
		String query = "Select * from BaPrint_History where Print_Date='"+date.toString()+"' and Office_Code='"+m_OfficeCode+"' order by baprint_num desc ; ";
		mfillMaps.removeAll(mfillMaps);
		try{
			mfillMaps.addAll(dba.getBarPrintHistory(query));
			adapter.notifyDataSetChanged();	
			return;
		}catch(NullPointerException e){
			e.printStackTrace();
		}		
		
		adapter.notifyDataSetChanged();
	}
		
	private void sendReprint() {
		// TODO Auto-generated method stub
		//----------------------------------------//
		// 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
		//----------------------------------------//
		//listDialog = new AlertDialog.Builder(this);
		listDialog = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
		//----------------------------------------//
		final String[] items;
		
		String date = m_buttonSetDate.getText().toString();
		List <HashMap<String, String>> mfillMaps_list = new ArrayList<HashMap<String, String>>();
		String	query = "Select Sum(cast(Count as integer)) Counts, BaPrint_Num, Print_Date From BaPrint_History "
				+ "Where Print_Date='"+date.toString()+"' and Office_Code='"+m_OfficeCode+"' "
				+ "Group By BaPrint_Num, Print_Date ";
		
		int num = 0;
		try{
			mfillMaps_list.addAll(dba.getBarPrintHistory(query));
			num = mfillMaps_list.size();
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		
		items = new String[num];
		if(num > 0){		
			for(int i=0; i<num; i++){			
				HashMap<String, String> map = new HashMap<String, String>();
				map = mfillMaps_list.get(i);
				items[i] = map.get("BaPrint_Num");
			}		
		}
		
		listDialog.setTitle("바코드프린터 재발행")
		.setItems(items, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				String baprint_num = items[which].toString();
				Log.w(TAG, baprint_num.toString());
				setResults(baprint_num);
				
			}
		}).setNegativeButton("취소",	null).show();
		
	}
	
	
	//선택 완료후 전송하기
	public void setResults(String baprint_num){		
		Intent intent = new Intent();
		intent.putExtra("BaPrint_Num", baprint_num); //HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra("fillmaps");
		this.setResult(RESULT_OK, intent);
		finish();		
	}
	
	
	//날자 변경설정
	public void onClickSetDate(View view) {
		DatePickerDialog newDlg = new DatePickerDialog(this, this,
				m_dateCalender1.get(Calendar.YEAR),
				m_dateCalender1.get(Calendar.MONTH),
				m_dateCalender1.get(Calendar.DAY_OF_MONTH));
		 newDlg.show();
	}
	
	//다음 날자 선택
	public void onClickSetDatePrevious(View view) {
		m_dateCalender1.add(Calendar.DAY_OF_MONTH, -1);	
		m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);
		m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));	
				
		sendRequestList();
	}
	
	//이전날자 선택
	public void onClickSetDateNext(View view) {
		m_dateCalender1.add(Calendar.DAY_OF_MONTH, 1);
		m_dateCalender2.add(Calendar.DAY_OF_MONTH, 1);	
		m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));			
 		
		sendRequestList();
	}

	//날자 선택 험수
	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		
		m_dateCalender1.set(year, monthOfYear, dayOfMonth);
		m_buttonSetDate.setText(m_dateFormatter.format(m_dateCalender1.getTime()));		
		m_dateCalender2.set(year, monthOfYear, dayOfMonth);
		m_dateCalender2.add(Calendar.DAY_OF_MONTH, -1);
		
		sendRequestList();
	}	
	
}
