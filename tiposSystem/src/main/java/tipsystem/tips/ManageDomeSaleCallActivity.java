package tipsystem.tips;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import kr.co.tipos.tips.R;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.UserPublicUtils;

public class ManageDomeSaleCallActivity extends Activity implements OnItemClickListener, OnClickListener {

	String TAG = "도매관리";
	
	UserPublicUtils upu;
	
	JSONObject m_shop;
	JSONObject m_userProfile;
		
	// 전표번호 생성
	String posID = "P";
	String userID = "1";
	
	//임시부여 아이피
	String m_ip = "127.0.0.1";
	String m_port = "1433";
	//----------------------------------------//
	// 2021.12.21. 매장DB IP,PW,DB 추가
	//----------------------------------------//
	String m_uuid = "sa";
	String m_uupw = "tips";
	String m_uudb = "tips";
	//----------------------------------------//

	EditText edittext_OfficeCode;
	EditText edittext_OfficeName;
		
	EditText edittext_OfficeGubun;
	
	Button button_OfficeSearch;
	
	ListView listview_OfficeList;
	SimpleAdapter m_adapter;
	
	Context mContext;
	
	private ProgressDialog dialog;
	
	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>(); // 거래처목록 리스트
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_domesalecallmanager);
		
		mContext = this;
		m_shop = LocalStorage.getJSONObject(this, "currentShopData");
		m_userProfile = LocalStorage.getJSONObject(this, "userProfile");

		//사용자 함수
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
			userID = m_userProfile.getString("User_ID");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		edittext_OfficeCode = (EditText)findViewById(R.id.edittextofficecode);
		edittext_OfficeName = (EditText)findViewById(R.id.edittextofficename);
				
		button_OfficeSearch = (Button)findViewById(R.id.buttonSearch);				
		listview_OfficeList = (ListView)findViewById(R.id.listviewOfficeList);
		
		//목록 리스트
		String[] from = new String[] { "거래처코드", "거래처명", "전화번호", "거래처구분" };
		int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4 };
		m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_item4_3, from, to);
		listview_OfficeList.setAdapter(m_adapter);
		
		listview_OfficeList.setOnItemClickListener(this);		
		button_OfficeSearch.setOnClickListener(this);		
		
		//거래처 검색
		searchOfficeList();
		
	}

	//거래처 검색입니다.
	private void searchOfficeList(){
		
		String office_code = edittext_OfficeCode.getText().toString();		
		String office_name = edittext_OfficeName.getText().toString();
		
		String query = "Select Office_Code, Office_Name, Office_Tel1, "
				+ "Office_Sec=CASE WHEN Office_Sec=1 THEN CASE WHEN Dir_Office=1 THEN '[직영]매입' ELSE '매입' END "      
				+ "WHEN Office_Sec=2 then '수수료' "      
				+ "WHEN Office_Sec=3 THEN CASE WHEN Dir_Office=1 THEN '[직영]매출' ELSE '매출' END ELSE '' END "
				+ "From Office_Manage Where Office_Use = '1' AND Office_Sec='3' ";
		
		if( !"".equals(office_code)){
			query += " And Office_Code='"+office_code+"' ";			
		}
		
		if( !"".equals(office_name)){
			query += " And Office_Name like '%"+office_name+"%' ";
		}
		
		//초기화
		mfillMaps.removeAll(mfillMaps);			
		
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
				
				//처리결과
				if(results.length() <= 0){
					m_adapter.notifyDataSetChanged();
					Toast.makeText(getApplicationContext(), "조회결과가 없습니다.", Toast.LENGTH_SHORT).show();					
					return;
				}

				try {
					
					for(int i=0; i < results.length(); i++){
						JSONObject map = results.getJSONObject(i);
						HashMap<String, String> temp = new HashMap<String, String>();
						temp.put("거래처명", map.get("Office_Name").toString());
						temp.put("거래처코드", map.get("Office_Code").toString());
						temp.put("전화번호", map.get("Office_Tel1").toString());
						temp.put("거래처구분", map.get("Office_Sec").toString());						
						
						mfillMaps.add(temp);			
					}
					
				} catch (JSONException e) {
					Toast.makeText(mContext, "결과값 이상 오류\r\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
					return;
				}

				m_adapter.notifyDataSetChanged();
				
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				m_adapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), "조회실패(" + String.valueOf(code) + "):" + msg, Toast.LENGTH_SHORT)
						.show();
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
		
	}
	
	
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId()){
	        case R.id.buttonSearch:
	            //회원검색
	        	Log.d(TAG, "거래처검색");
	        	searchOfficeList();
	        	
	        break;
		}
	}

	
	
	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		final int pos = position;

		// 선택한 거래처명 표기 위하여
		HashMap<String, String> maptmp = mfillMaps.get(pos);
		String mem_code_tmp = maptmp.get("거래처코드");
		String mem_name_tmp = maptmp.get("거래처명");
		
		new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT).setTitle("출고등록").setMessage("["+ mem_name_tmp +"] 거래처를 출고 등록을 할까요?")
				
		.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				
				HashMap<String, String> map = mfillMaps.get(pos);
				String mem_code = map.get("거래처코드");
				String mem_name = map.get("거래처명");
				
				Intent intent = new Intent(mContext, ManageDomeSaleRegActivity.class);
		    	intent.putExtra("거래처코드", mem_code);
		    	intent.putExtra("거래처명", mem_name);		    	
		    	startActivity(intent);
				
			}
		}).setNegativeButton("아니요", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub	
				return;
			}
		}).show();
	}
	
}
