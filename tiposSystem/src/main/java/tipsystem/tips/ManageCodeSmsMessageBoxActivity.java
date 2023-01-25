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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

public class ManageCodeSmsMessageBoxActivity extends Activity{

	JSONArray m_results;
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

	ListView m_cusList;
	SimpleAdapter m_adapter; 
	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();
	
	//메세지 조회
	Button m_messagesearch;	
	
	EditText m_messagenumber;
	EditText m_messagecomment;
		
	int selectListMsg = 0;
	String MODE = "modify" ;
	
    // loading bar
	private ProgressDialog dialog;

    // loading more in listview
   /* int currentVisibleItemCount;
    private boolean isEnd = false;
    private OnScrollListener customScrollListener = new OnScrollListener() {
        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            currentVisibleItemCount = visibleItemCount;

            if((firstVisibleItem + visibleItemCount) == totalItemCount && firstVisibleItem != 0) 
            	isEnd = true;            
            else 
            	isEnd = false;
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (isEnd && currentVisibleItemCount > 0 && scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				doSearch();
		    }
        }
    };*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_code_smsmessagebox);
		
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
        
        try{
	        Intent intent = getIntent();
	        if(intent.getStringExtra("MODE") != null )
	        	MODE = intent.getStringExtra("MODE");
        }catch(IllegalStateException e){        	
        	
        }
        
        m_messagenumber = (EditText)findViewById(R.id.editTextMessageCode);
        m_messagecomment = (EditText)findViewById(R.id.editTextMessageComment);
        
		m_cusList= (ListView)findViewById(R.id.listviewMessageList);
		m_cusList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	selectListMsg = position;
            	
            	AlertDialog.Builder dialog = new AlertDialog.Builder(ManageCodeSmsMessageBoxActivity.this,AlertDialog.THEME_HOLO_LIGHT)
        	    .setTitle("메세지 선택")
        	    .setMessage("선택한 메세지를 어떤 작업을 할까요?");
        	    dialog.setPositiveButton("선택", new DialogInterface.OnClickListener() {
        	     public void onClick(DialogInterface dialog, int whichButton) {
        	    	 selectMessageSend();
        	     }
        	   });
        	   dialog.setNeutralButton("수정", new DialogInterface.OnClickListener() {
          	     public void onClick(DialogInterface dialog, int whichButton) {
          	    	returnResultData(selectListMsg, "modify");
           	     }
           	   });
           	   dialog.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            	 public void onClick(DialogInterface dialog, int whichButton) {
            	    	 
                 }
               });
            	dialog.show();
            }
        });
		
		//m_cusList.setOnScrollListener(customScrollListener);

		String[] from = new String[] {"SMS_Num", "SMS_Memo"};
        int[] to = new int[] { R.id.item1, R.id.item2 };
        m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_item1, from, to);
        m_cusList.setAdapter(m_adapter);
        
        m_messagesearch = (Button)findViewById(R.id.buttonMessageSearch);
        m_messagesearch.setOnClickListener( new OnClickListener() {			
			@Override
			public void onClick(View v) {
				doSearch();
			}
		});
		doSearch();
	}

	public void selectMessageSend(){
		HashMap<String, String> hashMap = mfillMaps.get(selectListMsg);		
		Button bt = (Button)findViewById(R.id.button_smssend);
		bt.setText( hashMap.get("SMS_Num").toString() + "번 선택 \n 메세지전송" );
		bt.setTag(String.format("%d", selectListMsg));		
	}
	
	public void sendReciver(View v){
		if(MODE.equals("sendMsg")){
			returnResultData(selectListMsg, "sendMsg");
		}else{
			HashMap<String, String> hashMap = mfillMaps.get(selectListMsg);
			
			Intent intent = new Intent(this, ManageSMSserverActivity.class);			
			intent.putExtra("SMS_Num", hashMap.get("SMS_Num").toString());
			intent.putExtra("SMS_Memo", hashMap.get("SMS_Memo").toString());			
			//setResult(RESULT_OK, intent);
			startActivity(intent);
			finish();
		}
	}
	
	private void returnResultData(int position, String choose) {
		HashMap<String, String> hashMap = mfillMaps.get(position);
		if(choose.equals("sendMsg")){			
			Intent intent = new Intent();
						
			intent.putExtra("SMS_Num", hashMap.get("SMS_Num").toString());
			intent.putExtra("SMS_Memo", hashMap.get("SMS_Memo").toString());			
			setResult(RESULT_OK, intent);
			//startActivity(intent);
			finish();			
		}
		
		if(choose.equals("modify")){
			Intent intent = new Intent(this, SmsMessageBoxReg.class);
			//intent.putExtra("fillmaps", mfillMaps.get(position)); //HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra("fillmaps");	
		
			intent.putExtra("Modify", "n");
			intent.putExtra("SMS_Num", hashMap.get("SMS_Num").toString());
			intent.putExtra("SMS_Memo", hashMap.get("SMS_Memo").toString());
			intent.putExtra("Write_Date", hashMap.get("Write_Date").toString());
			intent.putExtra("Edit_Date", hashMap.get("Edit_Date").toString());
			intent.putExtra("Writer", hashMap.get("Writer").toString());
			intent.putExtra("Editor", hashMap.get("Editor").toString());		
			
			//this.setResult(RESULT_OK, intent);
			//finish();
			startActivity(intent);
		}
	}

	private void updateListView(JSONArray results) {
        
        for (int i = 0; i < results.length(); i++) {
        	
        	try {
            	JSONObject json = results.getJSONObject(i);
            	HashMap<String, String> map = JsonHelper.toStringHashMap(json);            	
	            mfillMaps.add(map);
		 
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
        m_adapter.notifyDataSetChanged();
    }
	
	// 조회 실행 함수 
    public void doSearch() {
    	
    	mfillMaps.removeAll(mfillMaps);
    	
    	//메세지 번호 불러오기
    	String number = m_messagenumber.getText().toString();
    	//메세지 내용 불러오기
    	String comment = m_messagecomment.getText().toString();
    	String number1 = " ";
    	//번호가 있으면 쿼리 추가 
    	if(number.length() > 0 && comment.length() > 0 ){    		//둘다 있을때
    		number1 = "WHERE SMS_Num = '"+number+"' AND Sms_memo like '%"+comment+"%' ";    		
    	}else if (number.length() > 0 && comment.length() <= 0 ) {	//숫자 검색일때
    		number1 = "WHERE SMS_Num = '"+number+"' ";
    	}else if (number.length() <= 0 && comment.length() > 0 ) {	//내용 검색일때
    		number1 = "WHERE Sms_memo like '%"+comment+"%' ";
    	}
    	// 쿼리 작성하기
    	String index = String.valueOf(mfillMaps.size());
		String query = "";
    	/*query = "SELECT TOP 50 * FROM SMS_Msg WHERE "
    			+ number 
    			+ " SMS_Num NOT IN(SELECT TOP " + index + " SMS_Num FROM SMS_Msg "
    			+ number1
    			+ " Order By SMS_Num DESC) Order By SMS_Num DESC;" ;*/

		query = "SELECT * FROM SMS_Msg  "
    			+ number1 
    			+ " Order By SMS_Num DESC; ";
		
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
					updateListView(results);
		            Toast.makeText(getApplicationContext(), "조회 완료", Toast.LENGTH_SHORT).show();
				}
				else {
					Toast.makeText(getApplicationContext(), "결과가 없습니다", Toast.LENGTH_SHORT).show();							
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();	
				
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
    }

    public void onClickMessageRenew(View view){    	
    	Intent intent = new Intent(this, SmsMessageBoxReg.class);
		//HashMap<String, String> hashMap = (HashMap<String, String>)intent.getSerializableExtra("fillmaps");
    	startActivity(intent);
		//this.setResult(RESULT_OK, intent);
		//finish();		
    }
    
    
	/**
	 * Set up the {@link android.app.ActionBar}.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
	
			ActionBar actionbar = getActionBar();         
			LinearLayout custom_action_bar = (LinearLayout) View.inflate(this, R.layout.activity_custom_actionbar, null);
			actionbar.setCustomView(custom_action_bar);
	
			actionbar.setDisplayShowHomeEnabled(false);
			actionbar.setDisplayShowTitleEnabled(false);
			actionbar.setDisplayShowCustomEnabled(true);
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manage_customer, menu);
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
