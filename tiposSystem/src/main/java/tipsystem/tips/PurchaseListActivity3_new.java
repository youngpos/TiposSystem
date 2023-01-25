package tipsystem.tips;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;

public class PurchaseListActivity3_new extends Activity implements AdapterView.OnItemClickListener {

	//검색 결과 돌려 받기
	private static final int CUSTOMER_MANAGER_REQUEST = 3;

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

	String posID = "P";
	String userID = "1";
	JSONObject m_userProfile;

	//목록
	ListView m_listPurchaseList;

	//매입목록 선택 포지션
	int m_selectedListIndex = -1;

	//전체전송 옵션
	boolean mAllSend;

	//다이얼로그 박스
	ProgressDialog dialog;

	//리스트목록 필요한 것들
	SimpleAdapter adapter;
	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();
	//HashMap<String, String> m_tempInNum = new HashMap<String, String>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_purchase_list3_new);

		// 매장 정보 불러 오기
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

			String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
			posID = LocalStorage.getString(this, "currentPosID:" + OFFICE_CODE);
			userID = m_userProfile.getString("User_ID");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		dialog = new ProgressDialog(this);

        //목록 할당 하기
        m_listPurchaseList= (ListView)findViewById(R.id.listviewPurchaseList);
		m_listPurchaseList.setOnItemClickListener(this);

		//리스트뷰 목록 생성 어댑터
		String[] from = new String[] {"index", "In_Num", "Office_Name", "In_Date", "Price_Sum"};
		int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5 };

		adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_item4_17, from, to);
		m_listPurchaseList.setAdapter(adapter);

		//신규 등록 버튼
		Button buttonClose = (Button)findViewById(R.id.buttonClose);
		buttonClose.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				//화면 닫기
				finish();
			}
		});

	}


	//저장된 매입목록을 불러옵니다.
	private void getTempInDPDAList(){

		//초기화
		mfillMaps.removeAll(mfillMaps);
		adapter.notifyDataSetChanged();
		//전표 조회 시 반품 버튼 초기화
		//m_checkBoxRejectedProduct.setChecked(false);

		//String query = "exec sp_executesql N'EXEC str_PDA_013Q ''"+posID+"'''";

		String query = "SELECT ISNULL(Office_Code,'') AS Office_Code "
            		 + ", ISNULL(Office_Name,'') AS Office_Name "
					 + ", ISNULL(COUNT(In_Seq),0) AS nCount "
            		 + ", REPLACE(CONVERT(VARCHAR,CONVERT(MONEY,ISNULL(SUM(In_Pri),0),2),1),'.00','') AS Price_Sum "
            		 + ", ISNULL(In_Date,'') AS In_Date, In_Num "
					 + "From Temp_InDPDA "
					 + "WHERE SubString(In_Num,9,1) = '" + posID + "' "
					 + "GROUP BY Office_Code "
					 + ", Office_Name "
					 + ", In_Date ,In_Num ";

		// 로딩 다이알로그
		//ProgressDialog dialog = new ProgressDialog(this);
		if(!dialog.isShowing()) {
			dialog.setMessage("Loading....");
			dialog.show();
		}

		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {

				dialog.dismiss();
				dialog.cancel();

				//처리결과
				if(results.length() <= 0){
					Toast.makeText(getApplicationContext(), "조회결과가 없습니다.", Toast.LENGTH_SHORT).show();

					return;
				}


				try {

					for(int i=0; i < results.length(); i++){
						JSONObject map = results.getJSONObject(i);
						HashMap<String, String> temp = new HashMap<String, String>();
						temp.put("index", i+1+"");
						temp.put("In_Num", map.get("In_Num").toString());
						temp.put("In_Date", map.get("In_Date").toString());
						temp.put("Office_Code", map.get("Office_Code").toString());
						temp.put("Office_Name", map.get("Office_Name").toString());
						temp.put("nCount", map.get("nCount").toString());
						temp.put("Price_Sum", map.get("Price_Sum").toString());
						//temp.put("Price_Sum", StringFormat.convertToIntNumberFormat(map.get("Price_Sum").toString()));
						mfillMaps.add(temp);
					}

				} catch (JSONException e) {
					Toast.makeText(PurchaseListActivity3_new.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}

				adapter.notifyDataSetChanged();

			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();

				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);


	}


	//매입장 전체전송 하기
	public void sendAlltempData(View view){

		int mCount = mfillMaps.size();

		if(mCount <= 0 ) return;

		//전체전송입니다.
		mAllSend = true;
		m_selectedListIndex = 0;
		toSendPurList(m_selectedListIndex);

	}


	//매입장 한장씩 전송하기
	private void toSendPurList(int num){

		final int select_num = num;

		HashMap<String, String> map = mfillMaps.get(select_num);
		String table_name = map.get("In_Date");
		table_name = table_name.replace("-", "").substring(0, 6);
		Log.d("테이블명", table_name);

		String query = "exec sp_executesql N'SELECT ISNULL(MAX(RIGHT(In_Num,3)),''000'') FROM InT_"+table_name+" WHERE In_Date=''"+map.get("In_Date")+"'' AND SUBSTRING(in_Num,9,1) =''"+posID+"''' ";

		// 로딩 다이알로그
		//ProgressDialog dialog = new ProgressDialog(this);
		if(dialog.isShowing()) {
			dialog.setMessage("Loading....");
			dialog.show();
		}

		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();

				try {

					String res = (String) results.getJSONObject(0).get("1");

					Log.d("전표번호 : ", res);
					int num_b = Integer.parseInt(res);
					num_b++;

					// 숫자를 문자형식으로 변경
					res = String.format("%03d", num_b);

					//매입장 전송
					toSendPurList_sub(res, select_num);

				} catch (JSONException  e) {
					Toast.makeText(PurchaseListActivity3_new.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();

				Toast.makeText(getApplicationContext(), "등록 실패", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

	}

	/**
	 * 매입장에 전송
	 * @param num 전표번호 뒷자리
	 * @param select_num temp_indpda에서 선택된 전표
	 */
	private void toSendPurList_sub(String num, int select_num){
		String gubun_num = num;
		int sel_num = select_num;

		HashMap<String,String> map = mfillMaps.get(sel_num);

		String office_code = map.get("Office_Code");
		String in_date = map.get("In_Date");
		String junpyo_num = in_date.replace("-", "")+posID+gubun_num;
		Log.d("전표번호", junpyo_num);

		String query = "exec sp_executesql N'EXEC str_PDA_013I ''"+office_code+"'', ''"+in_date+"'', ''"+userID+"'', ''"+posID+"'', ''"+junpyo_num+"'''";


		// 로딩 다이알로그
		//ProgressDialog dialog = new ProgressDialog(this);
		if(!dialog.isShowing()) {
			dialog.setMessage("Loading....");
			dialog.show();
		}

		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();

				try {
					String res = (String) results.getJSONObject(0).get("1");
					if (!"OK".equals(res)) {
						Toast.makeText(PurchaseListActivity3_new.this, res, Toast.LENGTH_SHORT).show();
						return;
					}


					if(mAllSend){

						//나머지 전표를 다시 전송 합니다.
						m_selectedListIndex++;

						//전체전송중 전송완료 수량과 전송나머지 수량이 같다면 종료
						if(m_selectedListIndex == mfillMaps.size()){
							//재조회합니다.
							m_selectedListIndex = -1;
							getTempInDPDAList();
							Toast.makeText(PurchaseListActivity3_new.this, "전송완료", Toast.LENGTH_SHORT).show();
							return;
						}

						toSendPurList(m_selectedListIndex);

					}else{
						//재조회합니다.
						m_selectedListIndex = -1;
						getTempInDPDAList();
						Toast.makeText(PurchaseListActivity3_new.this, "전송완료", Toast.LENGTH_SHORT).show();
					}

				} catch (JSONException e) {

					Toast.makeText(PurchaseListActivity3_new.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}

			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "등록 실패", Toast.LENGTH_SHORT).show();

			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);


	}


	//매입장 삭제
	private void transferDelete(){

		HashMap<String, String> map = mfillMaps.get(m_selectedListIndex);

		String office_code = map.get("Office_Code");
		String in_date = map.get("In_Date");

		String query = "exec sp_executesql N'EXEC str_PDA_013D ''"+office_code+"'', ''"+in_date+"'', ''"+posID+"'', ''DS'''";

		// 로딩 다이알로그
		//ProgressDialog dialog = new ProgressDialog(this);
		if(!dialog.isShowing()) {
			dialog.setMessage("Loading....");
			dialog.show();
		}

		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				try {
					String res = (String) results.getJSONObject(0).get("1");
					if (!"OK".equals(res)) {
						Toast.makeText(PurchaseListActivity3_new.this, res, Toast.LENGTH_SHORT).show();
						return;
					}

					//재조회합니다.
					m_selectedListIndex = -1;
					getTempInDPDAList();
					Toast.makeText(PurchaseListActivity3_new.this, "삭제 성공", Toast.LENGTH_SHORT).show();


				} catch (JSONException e) {
					Toast.makeText(PurchaseListActivity3_new.this, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "삭제 실패", Toast.LENGTH_SHORT).show();
			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);

	}





	// 2017-04 저장프로시서 사용하기로 인해 삭제됨
	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		/*
		 * CheckBox checkbox = ((CheckBox)arg1.findViewById(R.id.item5));
		 * if(checkbox != null) { checkbox.setChecked(!checkbox.isChecked()); //
		 * 베리 킥 포인트 요거 꼭해줘야 checkbox 에서 바로 바로 적용됩니다.
		 * m_adapter.notifyDataSetChanged(); }
		 */

		//modify_position = 1;
		m_selectedListIndex = position;


		new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT).setTitle("매입전표").setMessage("작업을 선택해 주세요!")
				.setPositiveButton("전송", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						//매입전표 전송
						//진정 전송입니다.
						mAllSend = false;
						toSendPurList(m_selectedListIndex);

					}
				}).setNegativeButton("삭제", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				new AlertDialog.Builder(PurchaseListActivity3_new.this,AlertDialog.THEME_HOLO_LIGHT).setTitle("전표삭제").setMessage("매입전표를 삭제 하시겠습니까?")
						.setPositiveButton("예", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int whichButton) {
								//매입전표 삭제
								transferDelete();
							}
						})
						.setNegativeButton("아니요", null).show();

			}
		}).setNeutralButton("전표수정", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub

				HashMap<String, String> map = mfillMaps.get(m_selectedListIndex);

				String in_num = "";
				String in_date =  map.get("In_Date");
				String office_code = map.get("Office_Code");
				String office_name = map.get("Office_Name");

				Log.i("전표번호", in_num);

				Intent intent = new Intent(getApplicationContext(), PurchaseRegistActivity1_new.class);
				intent.putExtra("Office_Code", office_code);
				intent.putExtra("In_Date", in_date);
				intent.putExtra("Office_Name", office_name);
				startActivity(intent);

				finish();
			}
		}).show();

	}







	@Override
	public void onResume(){

		getTempInDPDAList();
		super.onResume();
	}

	/**
	 * Set up the {@link ActionBar}.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	
			ActionBar actionbar = getActionBar();
			actionbar.setDisplayShowHomeEnabled(false);
			actionbar.setDisplayShowTitleEnabled(true);
			actionbar.setDisplayShowCustomEnabled(true);
			actionbar.setTitle("매입목록");
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.purchase_list_detail_view, menu);
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
