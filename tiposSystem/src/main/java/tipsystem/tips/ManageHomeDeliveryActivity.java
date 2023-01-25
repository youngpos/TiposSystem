package tipsystem.tips;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.TIPS_Config;
import tipsystem.utils.UserPublicUtils;

public class ManageHomeDeliveryActivity extends Activity implements OnItemClickListener, OnClickListener{
		
	String TAG = "배달관리";
	UserPublicUtils upu;

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

	//암호화여부
	String en_use = "0";

	TIPS_Config tips;
	
	EditText edittext_MemberCode;
	EditText edittext_MemberGubun;
	
	Spinner spinner_MemberGubun;
	
	Button button_MemberSearch;
	Button button_MemberNone;

	ListView listview_MemberList;
	SimpleAdapter m_adapter;
	
	Context mContext;
	
	private ProgressDialog dialog;
	
	//검색조건 고정 저장하기
	SharedPreferences pref;
	String gubun;
	
	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>(); // 회원목록 리스트
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_managehomedelivery);
		
		mContext = this;
		tips = new TIPS_Config(mContext);

		//사용자 함수
		upu = new UserPublicUtils(mContext);


		m_ip = tips.getSHOP_IP();
		m_port = tips.getSHOP_PORT();
		//----------------------------------------//
		// 2021.12.21. 매장DB IP,PW,DB 추가
		//----------------------------------------//
		m_uuid = tips.getDB_ID();
		m_uupw = tips.getDB_PW();
		m_uudb = tips.getDB_NAME();
		//----------------------------------------//

		posID = tips.getPOS_ID();
		userID = tips.getUSER_ID();

		en_use = tips.getEN_USE();


		//환경설정 관련 옵션 아이템 불러오기
		pref = PreferenceManager.getDefaultSharedPreferences(this);		
		gubun = pref.getString("prefHDSearchGubun", "0");
		Log.d(TAG, "변경값 : "+gubun);
		
		edittext_MemberCode = (EditText)findViewById(R.id.edittextmembercode);
		edittext_MemberGubun = (EditText)findViewById(R.id.edittextmembername);
		
		spinner_MemberGubun = (Spinner)findViewById(R.id.spinner_membergubun);
		spinner_MemberGubun.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				//검색조건 변경식 계속 저장되게 변경				
				SharedPreferences.Editor editor = pref.edit();
				editor.putString("prefHDSearchGubun", position+"");				
				editor.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// TODO Auto-generated method stub
				
			}
		});
		
		int a = Integer.parseInt(gubun);
		spinner_MemberGubun.setSelection(a);
					
		button_MemberSearch = (Button)findViewById(R.id.buttonSearch);
		button_MemberNone = (Button)findViewById(R.id.buttonManageHDMemberNone);
		
		listview_MemberList = (ListView)findViewById(R.id.listviewMemberList);
		
		//목록 리스트
		String[] from = new String[] { "회원번호", "회원명", "전화번호", "휴대폰번호" };
		int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4 };
		m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_item4_3, from, to);
		listview_MemberList.setAdapter(m_adapter);
		
		listview_MemberList.setOnItemClickListener(this);
		
		button_MemberSearch.setOnClickListener(this);		
		button_MemberNone.setOnClickListener(this);
		
	}
	
	//회원검색
	private void searchMember(){
		
		String member_code = edittext_MemberCode.getText().toString();
		int pos = spinner_MemberGubun.getSelectedItemPosition();
		String member_gubun = edittext_MemberGubun.getText().toString();
		
		if( "".equals(member_code) && "".equals(member_gubun)){
			Toast.makeText(mContext, "검색값을 입력해 주세요!", Toast.LENGTH_SHORT).show();
			edittext_MemberCode.requestFocus();
			return;
		}
		
		String query = "Select Cus_Code, Cus_Name, Cus_Tel, Cus_Mobile From Customer_Info Where 1=1 ";
		
		if( !"".equals(member_code)){
			query += " And Cus_Code='"+member_code+"' ";			
		}
		
		if( !"".equals(member_gubun)){
		
			switch(pos){
			case 0: //전화번호 검색
				query += " And ( Cus_Tel Like '%"+member_gubun+"%' or Cus_Mobile Like '%"+member_gubun+"%' ) "; 			
				break;
			case 1: //회원명 검색
				query += " And Cus_Name Like '%"+member_gubun+"%' ";
				break;
			case 2: //주소검색
				query += " And ( Address1 Like '%"+member_gubun+"%' or Address2 Like '%"+member_gubun+"%' ) ";
				break;
			}
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
						temp.put("회원명", map.get("Cus_Name").toString());
						temp.put("회원번호", map.get("Cus_Code").toString());
						temp.put("전화번호", map.get("Cus_Tel").toString());
						temp.put("휴대폰번호", map.get("Cus_Mobile").toString());						
						
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
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub\
		
		final int pos = position;
		
		new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT).setTitle("배달등록").setMessage("배달등록을 할까요?")
		.setPositiveButton("예", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				//매입전표 전송			
				//진정 전송입니다.
				HashMap<String, String> map = mfillMaps.get(pos);
				String mem_code = map.get("회원번호");
				String mem_name = map.get("회원명");
				
				Intent intent = new Intent(mContext, ManageHomeDeliveryRegActivity.class);
		    	intent.putExtra("회원번호", mem_code);
		    	intent.putExtra("회원명", mem_name);
		    	intent.putExtra("구분", true);
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


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub		
		
		
		switch(v.getId()){
        case R.id.buttonSearch:
            //회원검색
        	Log.d(TAG, "회원검색");
        	searchMember();

			keypad_hide();
        	
            break;
        case R.id.buttonManageHDMemberNone:
        	//비회원등록
        	Log.d(TAG, "비회원등록");
        	Intent intent = new Intent(this, ManageHomeDeliveryRegActivity.class);
        	intent.putExtra("회원번호", "");
        	intent.putExtra("회원명", "");
        	intent.putExtra("구분", false);
        	startActivity(intent);

			keypad_hide();

        	break;

        }
	}


	private void keypad_hide() {
		//2020-06-15
		InputMethodManager imm = (InputMethodManager)getSystemService(getApplicationContext().INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),	InputMethodManager.HIDE_NOT_ALWAYS);
	}
}
