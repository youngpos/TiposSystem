package tipsystem.tips;

//import java.text.ParseException;
//import java.text.SimpleDateFormat;
import java.util.ArrayList;
//import java.util.Calendar;
//import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL;
import tipsystem.utils.MSSQL2;
import tipsystem.utils.StringFormat;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
//import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
//import android.view.inputmethod.EditorInfo;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
//import android.widget.CheckBox;
//import android.widget.CompoundButton;
//import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
//import android.widget.TextView;
//import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
/*
 * 기본관리 -> 상품관리
 * */
public class ManageProductActivity extends Activity {
	private static final int ZBAR_SCANNER_REQUEST = 0;
	//private static final int ZBAR_QR_SCANNER_REQUEST = 1;
	private static final int BARCODE_MANAGER_REQUEST = 2;
	private static final int CUSTOMER_MANAGER_REQUEST = 3;
	private static final int PRODUCT_REGISTER = 4;
	
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

	String m_officeCode="";
	
	//Date m_Finish_Date; //마감기준일
	
	EditText m_textBarcode; // 바코드
	EditText m_textProductName; // 상품명
	EditText m_textCustomerCode; //거래처코드
	EditText m_textCustomerName; //거래처명
	Button m_buttonCustomerClassification1; //대분류
	Button m_buttonCustomerClassification2; //중분류
	Button m_buttonCustomerClassification3; //소분류
	Button m_buttonChooseShowyn; //추가검색버튼
	EditText m_textPurchasePrice; //매입가	
	EditText m_textSalesPrice; //판매가	
	Spinner m_spinnerGubunType; //상품구분
	Spinner m_spinnerGoodsType; //사용구분
	Spinner m_spinnerGroupType; //그룹구분		
	
	ListView m_listProduct; //상품목록
	SimpleAdapter m_adapter;  //어댑터
	
	// 추가검색 버튼
	LinearLayout m_linearlayoutChooseYn;

	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();
	HashMap<String, String> m_tempProduct =new HashMap<String, String>();
	HashMap<String, String> m_totalCount =new HashMap<String, String>();
	List<HashMap<String, String>> m_Ltype = new ArrayList<HashMap<String, String>>();
	List<HashMap<String, String>> m_Mtype = new ArrayList<HashMap<String, String>>();
	List<HashMap<String, String>> m_Stype = new ArrayList<HashMap<String, String>>();

    // loading bar
    public ProgressDialog dialog; 
	
    // loading more in listview
    int currentVisibleItemCount;
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
    };
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_product);

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

			m_officeCode = m_shop.getString("OFFICE_CODE");
			// 해당 OFFICE_CODE 에 포스ID 가 없을경우 'P' 로 셋팅
	       // m_posID = LocalStorage.getString(this, "currentPosID:"+OFFICE_CODE);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		m_textBarcode = (EditText)findViewById(R.id.editTextBarcode);
		m_textProductName = (EditText)findViewById(R.id.editTextProductName);
		m_textCustomerCode = (EditText)findViewById(R.id.editTextCustomerCode);
		m_textCustomerName = (EditText)findViewById(R.id.editTextCustomerName);
		m_buttonCustomerClassification1 = (Button)findViewById(R.id.buttonClassificationType1);
		m_buttonCustomerClassification2 = (Button)findViewById(R.id.buttonClassificationType2);
		m_buttonCustomerClassification3 = (Button)findViewById(R.id.buttonClassificationType3);
		m_buttonChooseShowyn = (Button)findViewById(R.id.buttonChooseShowyn);
		m_linearlayoutChooseYn = (LinearLayout)findViewById(R.id.linearlayoutChooseYn);		
		m_linearlayoutChooseYn.setVisibility(View.GONE);
		m_spinnerGubunType = (Spinner)findViewById(R.id.spinnerGubunType);
		m_spinnerGoodsType = (Spinner)findViewById(R.id.spinnerGoodsType);		
		m_spinnerGroupType = (Spinner)findViewById(R.id.spinnerGroupType);
		
		m_buttonChooseShowyn.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {											
				if ( m_linearlayoutChooseYn.getVisibility() == View.GONE ){
				m_linearlayoutChooseYn.setVisibility(View.VISIBLE);
				m_buttonChooseShowyn.setText("추가검색(분류/구분/그룹/판매가/매입가)  ▲");
				}else{
					m_linearlayoutChooseYn.setVisibility(View.GONE);
					m_buttonChooseShowyn.setText("추가검색(분류/구분/그룹/판매가/매입가)  ▼");
				}
			}
		});		
		
		//그룹선택
		m_spinnerGroupType = (Spinner)findViewById(R.id.spinnerGroupType);
		m_spinnerGroupType.setOnItemSelectedListener(new OnItemSelectedListener() {
		    @Override
		    public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
		    	
		    }

		    @Override
		    public void onNothingSelected(AdapterView<?> parentView) {
		    }
		});
				
		m_textPurchasePrice = (EditText)findViewById(R.id.editTextPurchasePrice);		
		m_textSalesPrice = (EditText)findViewById(R.id.editTextSalesPrice);
				
		//리스트 클릭시 처리 루트
		m_listProduct = (ListView)findViewById(R.id.listviewProductList);
		m_listProduct.setOnItemClickListener( new OnItemClickListener() {						
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            
            	m_tempProduct = mfillMaps.get(position);
                Intent intent = new Intent(ManageProductActivity.this, ManageProductActivityModfiy.class);            	
                String barcode = m_tempProduct.get("BarCode");
                intent.putExtra("barcode", barcode);
                intent.putExtra("productRegYN", "n");
                startActivityForResult(intent, PRODUCT_REGISTER);
          //	updateFormView(m_tempProduct);
          }
		});
		m_listProduct.setOnScrollListener(customScrollListener);

		/*String[] from = new String[] {"BarCode", "G_Name", "Pur_Pri", "Sell_Pri"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4 };*/
        
        String[] from = new String[] {"BarCode", "G_Name", "Bus_Name", "Pur_Pri", "Sell_Pri", "Sale_Sell", "Profit_Rate", "면과세", "Real_Sto"};
        int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4, R.id.item5, R.id.item6, R.id.item7, R.id.item8, R.id.item9 };
        
        // fill in the grid_item layout
        m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_product_list, from, to);
        m_listProduct.setAdapter(m_adapter);
        
		Button searchButton = (Button) findViewById(R.id.buttonProductSearch);
		Button registButton = (Button) findViewById(R.id.buttonProductRegist);
		Button renewButton = (Button) findViewById(R.id.buttonProductRenew);
		//Button modifyButton = (Button) findViewById(R.id.buttonProductModify);
		
		// 조회 버튼 클릭
		searchButton.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) { 
	        	deleteListViewAll();
	        	doSearch();
	        	m_linearlayoutChooseYn.setVisibility(View.GONE);
	        	m_buttonChooseShowyn.setText("추가검색(분류/구분/그룹/판매가/매입가)  ▼");
	        }
		});
		
		// 등록 버튼 클릭		
		registButton.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	deleteListViewAll();
	        	doRegister();
	        }
		});
		
		// 수정 버튼 클릭
		/*modifyButton.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {
	        	deleteListViewAll();
	        	//doModify();
	        }
		});*/

		// 초기화 버튼 클릭		
		renewButton.setOnClickListener(new OnClickListener() {
	        public void onClick(View v) {	        	
	        	deleteListViewAll();
	        	doClear();	        	
	        }
		});
		
		// 바코드 입력 후 포커스 딴 곳을 옮길 경우
		m_textBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
			    if(!hasFocus){
			    	String barcode = m_textBarcode.getText().toString();
		    		m_tempProduct.put("BarCode", barcode); 
			    	
			    	if(!barcode.equals("")) // 입력한 Barcode가 값이 있을 경우만
			    		doQueryWithBarcode();	    	
			    }
			}
		});
		// 거래처 코드 입력 후 포커스 딴 곳을 옮길 경우
		m_textCustomerCode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
			    if(!hasFocus){
			    	String customerCode = m_textCustomerCode.getText().toString();
		    		m_tempProduct.put("Bus_Code", customerCode); 
		    		
			    	if(!customerCode.equals("")) // 입력한 customerCode가 값이 있을 경우만
			    		fillBusNameFromBusCode(customerCode);	    	
			    }
			}
		});		
		m_textProductName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
			    if(!hasFocus){
		    		m_tempProduct.put("G_Name", m_textProductName.getText().toString()); 
			    }
			}
		});
		m_textCustomerName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
			    if(!hasFocus){
		    		m_tempProduct.put("Bus_Name", m_textCustomerName.getText().toString()); 
			    }
			}
		});
		
		//getFinishDate();
		fetchLName();
		doClear() ; 
	}
	
	
    // 입력폼 초기화 
    public void doClear() {
    	m_textBarcode.setText("") ; // 바코드
    	m_textProductName.setText(""); // 상품명
    	m_textCustomerCode.setText(""); //거래처코드
    	m_textCustomerName.setText(""); //거래처명
    	m_buttonCustomerClassification1.setText(""); //대분류
    	m_buttonCustomerClassification2.setText(""); //중분류
    	m_buttonCustomerClassification3.setText(""); //소분류    	
    	m_textPurchasePrice.setText(""); //매입가	
    	m_textSalesPrice.setText(""); //판매가
    	m_spinnerGubunType.setSelection(0);//상품구분
    	m_spinnerGoodsType.setSelection(1);//사용구분
    	m_spinnerGroupType.setSelection(0);//그룹구분
    	
    	//새로고침 시 바코드로 복귀
    	m_textBarcode.requestFocus();
	}
    
    // 상품 데이터 초기화
    public void newTempProduct () {
    	m_tempProduct = new HashMap<String, String>(); 
    	m_tempProduct.put("BarCode", ""); 
    	m_tempProduct.put("G_Name", ""); 
    	m_tempProduct.put("Bus_Code", ""); 
    	m_tempProduct.put("Bus_Name", ""); 
    	m_tempProduct.put("Std_Size", ""); 
    	m_tempProduct.put("Obtain", "0"); 
    	m_tempProduct.put("Pur_Pri", ""); 
    	m_tempProduct.put("Sell_Pri", ""); 
    	m_tempProduct.put("Pur_Cost", ""); 
    	m_tempProduct.put("Profit_Rate", ""); 
    	m_tempProduct.put("L_Name", ""); 
    	m_tempProduct.put("M_Name", ""); 
    	m_tempProduct.put("S_Name", ""); 
    	m_tempProduct.put("Tax_YN", "1"); 
    	m_tempProduct.put("VAT_CHK", "1"); 
    	m_tempProduct.put("Goods_Use", "1"); 
    }

    // 리스트 초기화
	public void deleteListViewAll() {
		if (mfillMaps.isEmpty()) return;
        
		mfillMaps.removeAll(mfillMaps);
		m_adapter.notifyDataSetChanged();
	}	
	
	public String getCodeFromListByName (List<HashMap<String, String>> l, String name) {
		
		Iterator<HashMap<String, String>> it = l.iterator();
		while(it.hasNext()) {
			HashMap<String, String> obj = it.next();
			if (obj.get("name").equals(name) ) {
				return obj.get("code");
			}
		}		
		return "";
	}
	
	public void doSearch() {

    	String index = String.valueOf(mfillMaps.size());  
		String query = "";
		String barcode = m_textBarcode.getText().toString();
	    String productName = m_textProductName.getText().toString();
	    String customerCode = m_textCustomerCode.getText().toString();
	    String customerName = m_textCustomerName.getText().toString();
	    
	    String l_name = m_buttonCustomerClassification1.getText().toString(); //대명
    	String m_name = m_buttonCustomerClassification2.getText().toString(); //중명
    	String s_name = m_buttonCustomerClassification3.getText().toString(); //소명    
    	String pure_pri = m_textPurchasePrice.getText().toString(); //매입가	
	    String sale_pri = m_textSalesPrice.getText().toString(); //판매가
	    int gubun_type = m_spinnerGubunType.getSelectedItemPosition();//상품구분
	    int goods_type = m_spinnerGoodsType.getSelectedItemPosition();//사용구분
	    int group_type = m_spinnerGroupType.getSelectedItemPosition();//그룹구분	
		
	    query += "SELECT TOP 50 * FROM Goods ";
		query += " WHERE ";
		query += " BarCode like '%"+barcode +"%' ";
		query += " AND G_Name like '%"+productName +"%' ";
		query += " AND Bus_Name like '%"+customerName +"%' ";
		query += " AND Bus_Code like '%"+customerCode +"%' ";
		
		if( 0 < l_name.length()) query += " AND L_Name = '"+l_name+"' ";
		if( 0 < m_name.length()) { query += " AND M_Name = '"+m_name+"' ";	}
		if( 0 < s_name.length()) { query += " AND S_Name = '"+s_name+"' ";		}
		if( 0 < pure_pri.length() ) { query += " AND pur_pri = '"+pure_pri+"' ";	 }
		if( 0 < sale_pri.length() ) { query += " AND sell_pri = '"+sale_pri+"' ";		}
		if( gubun_type != 0 ) { // 전체상품
			if( gubun_type == 1 ) { //공산품
				query += " AND Scale_use = '0' ";
			}else if( gubun_type == 2){ //저울상품
				query += " AND Scale_use = '1' ";
			}else if( gubun_type == 3 ){ //부분상품
				query += " AND LEN(BarCode)='4' ";
			}
		}
		if( 0 != goods_type ){ //전체
			if( goods_type == 1 ){ //사용
				query += " AND goods_use = '1' ";
			}else if( goods_type == 2 ){ //미사용
				query += " AND goods_use = '0' ";
			}
		}
		if( 0 != group_type ){ // 그룹
			int a = 65; // A그룹
			for(int i = 1; i <= 12 ; i++){				
				if ( i == group_type){
				query += " AND G_grade = '"+(char)a+"' ";
				}
				a++;
			}
		}		
		query += " AND BarCode NOT IN(SELECT TOP " + index + " BarCode FROM Goods ";
		query += " where BarCode like '%"+barcode +"%' ";	
		query += " AND G_Name like '%"+productName +"%' ";
		query += " AND Bus_Name like '%"+customerName +"%' ";
		query += " AND Bus_Code like '%"+customerCode +"%' ";
		if( 0 < l_name.length()) query += " AND L_Name = '"+l_name+"' ";
		if( 0 < m_name.length()) { query += " AND M_Name = '"+m_name+"' ";	}
		if( 0 < s_name.length()) { query += " AND S_Name = '"+s_name+"' ";		}
		if( 0 < pure_pri.length() ) { query += " AND pur_pri = '"+pure_pri+"' ";	 }
		if( 0 < sale_pri.length() ) { query += " AND sell_pri = '"+sale_pri+"' ";		}
		if( gubun_type != 0 ) { // 전체상품
			if( gubun_type == 1 ) { //공산품
				query += " AND Scale_use = '0' ";
			}else if( gubun_type == 2){ //저울상품
				query += " AND Scale_use = '1' ";
			}else if( gubun_type == 3 ){ //부분상품
				query += " AND LEN(BarCode)='4' ";
			}
		}
		if( 0 != goods_type ){ //전체
			if( goods_type == 1 ){ //사용
				query += " AND goods_use = '1' ";
			}else if( goods_type == 2 ){ //미사용
				query += " AND goods_use = '0' ";
			}
		}
		if( 0 != group_type ){ // 그룹
			int a = 65; // A그룹
			for(int i = 1; i <= 12 ; i++){				
				if ( i == group_type){
				query += " AND G_grade = '"+(char)a+"' ";
				}
				a++;
			}
		}		
		query += " Order By BarCode ASC) Order By BarCode ASC;";
		
		doTotalCount();

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
					int re = results.length();
					int ra = mfillMaps.size() + re;
					String totalcount = m_totalCount.get("total");
					String result_text = "총 "+totalcount+"건중 "+ra+"건 조회 완료";
					Toast.makeText(getApplicationContext(), result_text, Toast.LENGTH_SHORT).show();
					updateListView(results);
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
	
	public void updateListView(JSONArray results) {
				
        for (int i = 0; i < results.length(); i++) {
        	
        	try {
            	JSONObject json = results.getJSONObject(i);
            	HashMap<String, String> map = JsonHelper.toStringHashMap(json);
            	
				map.put("Sell_Pri", StringFormat.convertToIntNumberFormat((map.get("Sell_Pri"))));
				/*Date sd = makeTimeWithStringDateFormat(map.get("Sale_SDate"));
			    Date ed = makeTimeWithStringDateFormat(map.get("Sale_EDate"));
			    if((m_Finish_Date.after(sd) || m_Finish_Date.equals(sd)) && (m_Finish_Date.before(ed) || m_Finish_Date.equals(ed))) {
			    	map.put("Sale_Sell", StringFormat.convertToIntNumberFormat(map.get("Sale_Sell")));	
			    }else{
			    	map.put("Sale_Sell", "0");	
			    }*/
				map.put("Sale_Sell", StringFormat.convertToIntNumberFormat(map.get("Sale_Sell")));
			    if(map.get("Tax_YN").equals("0")){
            		map.put("면과세", "면세");
            	}else{
            		map.put("면과세", "과세");
            	}				
	            mfillMaps.add(map);
		 
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
        m_adapter.notifyDataSetChanged();
    }
	 
	// 분류관련 메소드
    private void fetchLName() {
    	String query = "";
		query = "SELECT L_Name, L_Code FROM Goods GROUP BY L_Name, L_Code order by L_Code ASC;";
	
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();

	    // 콜백함수와 함께 실행
	    new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				
				if (results.length() > 0) {   

					for(int index = 0; index < results.length() ; index++)
					{
						HashMap<String, String> data =new HashMap<String, String>();
						JSONObject son;
						try {
							son = results.getJSONObject(index);
							String name = son.getString("L_Name");
							String code = son.getString("L_Code");
							
							data.put("name", name);
							data.put("code", code);
							m_Ltype.add(data);
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}					
				}
				else {
					Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
				}
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
    
	public void onClassification1(View view)
	{
		m_buttonCustomerClassification2.setText("");
		m_buttonCustomerClassification3.setText("");
		
		ArrayList<String> lSpList = new ArrayList<String>();       
		for(int index = 0; index < m_Ltype.size() ; index++)
		{
			HashMap<String, String> data = m_Ltype.get(index);
			String name = data.get("name");
			lSpList.add(name);
		}

		final CharSequence[] charSequenceItems = lSpList.toArray(new CharSequence[lSpList.size()]);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
	    builder.setTitle("선택하세요");
	    builder.setItems(charSequenceItems, new DialogInterface.OnClickListener() {
	    	public void onClick(DialogInterface dialog, int item) {
	    		String name = charSequenceItems[item].toString();
	    		m_buttonCustomerClassification1.setText(name);
		    	m_tempProduct.put("L_Name", name); 
	        }
	    });
	    AlertDialog alert = builder.create();
	    alert.show();
	}
	
	public void onClassification2(View view)
	{
		String lname = m_buttonCustomerClassification1.getText().toString();
		if (lname.equals("")) {
			Toast.makeText(getApplicationContext(), "먼저 대분류를 먼저 선택하세요", Toast.LENGTH_SHORT).show();
			return;
		}
		
		m_buttonCustomerClassification3.setText("");
		String query = "";
		query = "SELECT M_Name, M_Code FROM Goods WHERE L_Name='"+lname+"' GROUP BY M_Name, M_Code order by M_code ASC;";
	
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();

	    // 콜백함수와 함께 실행
	    new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				
				if (results.length() > 0) {
					m_Mtype.removeAll(m_Mtype);
					
					ArrayList<String> lSpList = new ArrayList<String>();       
					for(int index = 0; index < results.length() ; index++)
					{
						HashMap<String, String> data =new HashMap<String, String>();
						JSONObject son;
						try {
							son = results.getJSONObject(index);
							String name = son.getString("M_Name");
							String code = son.getString("M_Code");
							lSpList.add(name);

							data.put("name", name);
							data.put("code", code);
							m_Mtype.add(data);

						} catch (JSONException e) {
							e.printStackTrace();
						}
					}

					final CharSequence[] charSequenceItems = lSpList.toArray(new CharSequence[lSpList.size()]);
					
					AlertDialog.Builder builder = new AlertDialog.Builder(ManageProductActivity.this,AlertDialog.THEME_HOLO_LIGHT);
				    builder.setTitle("선택하세요");
				    builder.setItems(charSequenceItems, new DialogInterface.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int item) {
				    		String name = charSequenceItems[item].toString();
				    		m_buttonCustomerClassification2.setText(name);
					    	m_tempProduct.put("M_Name", name); 
				        }
				    });
				    AlertDialog alert = builder.create();
				    alert.show();
				}
				else {
					Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
				}
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	public void onClassification3(View view)
	{
		String lname = m_buttonCustomerClassification1.getText().toString();
		String mname = m_buttonCustomerClassification2.getText().toString();
		if (lname.equals("")||mname.equals("")) {
			Toast.makeText(getApplicationContext(), "먼저 대분류,중분류를 먼저 선택하세요", Toast.LENGTH_SHORT).show();
			return;
		}
		
		String query = "";
		query = "SELECT S_Name, S_Code FROM Goods WHERE L_Name='"+lname+"' AND M_Name='"+mname+"' GROUP BY S_Name, S_Code order by S_Code ASC;";

		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();

	    // 콜백함수와 함께 실행
	    new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				
				if (results.length() > 0) {
					m_Stype.removeAll(m_Stype);

					ArrayList<String> lSpList = new ArrayList<String>();       
					for(int index = 0; index < results.length() ; index++)
					{
						JSONObject son;
						try {
							HashMap<String, String> data =new HashMap<String, String>();
							son = results.getJSONObject(index);
							String name = son.getString("S_Name");
							String code = son.getString("S_Code");
							lSpList.add(name);
							
							data.put("name", name);
							data.put("code", code);
							m_Stype.add(data);
							
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					
					final CharSequence[] charSequenceItems = lSpList.toArray(new CharSequence[lSpList.size()]);
					
					AlertDialog.Builder builder = new AlertDialog.Builder(ManageProductActivity.this,AlertDialog.THEME_HOLO_LIGHT);
				    builder.setTitle("선택하세요");
				    builder.setItems(charSequenceItems, new DialogInterface.OnClickListener() {
				    	public void onClick(DialogInterface dialog, int item) {
				    		String name = charSequenceItems[item].toString();
				    		m_buttonCustomerClassification3.setText(name);
					    	m_tempProduct.put("S_Name", name); 
				        }
				    });
				    AlertDialog alert = builder.create();
				    alert.show();
				}
				else {
					Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
				}
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}

	// 거래처 코드로 거래처명 자동 완성
	private void fillBusNameFromBusCode(String customerCode) {
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();
 		
		String query = "";		
		query = "SELECT Office_Name From Office_Manage WHERE Office_Code = '" + customerCode + "';";
	    new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				if (results.length() > 0) {
					try {
						JSONObject json = results.getJSONObject(0);
						String bus_name = json.getString("Office_Name");
						m_textCustomerName.setText(bus_name);
					} catch (JSONException e) {
						e.printStackTrace();
					}
		            Toast.makeText(getApplicationContext(), "조회 완료", Toast.LENGTH_SHORT).show();
				}
				else {
		            Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();					
				}
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	public void onCustomerSearch(View view)
	{
		String customer = m_textCustomerCode.getText().toString();
		String customername = m_textCustomerName.getText().toString();
		Intent intent = new Intent(this, ManageCustomerListActivity.class);
		intent.putExtra("customer", customer);
		intent.putExtra("customername", customername);
    	startActivityForResult(intent, CUSTOMER_MANAGER_REQUEST);
	}

	public void onBarcodeSearch(View view)
	{
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        String list_value = pref.getString("prefSearchMethod", "");
        if (list_value.equals("camera")) {
			startCameraSearch();
        }
        else if (list_value.equals("list")) {
        	startProductList();
        }
        else {
        	// 바코드 검색 버튼 클릭시 나오는 목록 셋팅
    		final String[] option = new String[] { "목록", "카메라"};
    		//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.user_select_dialog_item, option);
    		AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
    		builder.setTitle("Select Option");
    		
    		// 목록 선택시 이벤트 처리
    		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int which) {

    				if(which == 0){ // 목록으로 조회할 경우
    					startProductList();
    				} else { // 스캔할 경우	
    					startCameraSearch();
    				}
    			}
    		}); 
    		builder.show();
        }
	}
	
	private void startProductList() {
		String barcode = m_textBarcode.getText().toString();
		String gname = m_textProductName.getText().toString();
		String officecode = m_textCustomerCode.getText().toString();
		Intent intent = new Intent(this, ManageProductListActivity.class);
		intent.putExtra("barcode", barcode);
		intent.putExtra("gname", gname);
		intent.putExtra("Office_Code", officecode);	
    	startActivityForResult(intent, BARCODE_MANAGER_REQUEST);
	}
	
	private void startCameraSearch() {
		Intent intent = new Intent(this, ZBarScannerActivity.class);		
    	startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
	} 
	
	private void doRegister(){		
		Intent intent = new Intent(this, ManageProductActivityModfiy.class);
		intent.putExtra("barcode", "");
		startActivityForResult(intent, PRODUCT_REGISTER);		
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{    
		super.onActivityResult(requestCode, resultCode, data);
		
		switch(requestCode){
		// 카메라 스캔을 통한 바코드 검색
		case ZBAR_SCANNER_REQUEST :
			if (resultCode == RESULT_OK) {
		        // Scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT)
		        // Type of the scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
		        Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();
		        //Toast.makeText(this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE), Toast.LENGTH_SHORT).show();
		        // The value of type indicates one of the symbols listed in Advanced Options below.

		        String barcode = data.getStringExtra(ZBarConstants.SCAN_RESULT);
		        m_textBarcode.setText(barcode);
		        doQueryWithBarcode();
				
		    } else if(resultCode == RESULT_CANCELED) {
		        Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
		    }
			break;
		// 목록 검색을 통한 바코드 검색				
		case BARCODE_MANAGER_REQUEST :
			if(resultCode == RESULT_OK && data != null) {
				HashMap<String, String> hashMap = (HashMap<String, String>)data.getSerializableExtra("fillmaps");        	
				m_textBarcode.setText(hashMap.get("BarCode"));
				m_tempProduct.put("BarCode", hashMap.get("BarCode")); //  
	        	doQueryWithBarcode();
	        }
			break;
		case CUSTOMER_MANAGER_REQUEST :
			if(resultCode == RESULT_OK && data != null) {
				HashMap<String, String> hashMap = (HashMap<String, String>)data.getSerializableExtra("fillmaps");     	
				m_textCustomerCode.setText(hashMap.get("Office_Code"));
				m_textCustomerName.setText(hashMap.get("Office_Name"));
				
				m_tempProduct.put("Bus_Code", hashMap.get("Office_Code")); //  
				m_tempProduct.put("Bus_Name", hashMap.get("Office_Name")); //  
	        }
			break;
		case PRODUCT_REGISTER :
			if(resultCode == RESULT_OK && data != null) {
				 String barcode = data.getStringExtra("Barcode");
			     m_textBarcode.setText(barcode);
			     doQueryWithBarcode();
	        }
			break;
		}
	}
	
	// SQL QUERY 실행
	public void doQueryWithBarcode () {
		
		String query = "";
		String barcode = m_textBarcode.getText().toString();
		query = "SELECT * FROM Goods WHERE Barcode = '" + barcode + "';";
	
		if (barcode.equals("")) return;
		
		// 로딩 다이알로그 
    	dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();

	    // 콜백함수와 함께 실행
	    new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();
				
				if (results.length() > 0) {
					/*try {
						
						//m_tempProduct = JsonHelper.toStringHashMap(results.getJSONObject(0));
						//updateFormView(m_tempProduct);
					} catch (JSONException e) {
						e.printStackTrace();
					}*/
					doSearch();
				}
				else {
					
					Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
				}
			}
		}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
	}
	
	// SQL QUERY 합계 구하는 
		public void doTotalCount () {
			
			String query = "";			
			String barcode = m_textBarcode.getText().toString();
		    String productName = m_textProductName.getText().toString();
		    String customerCode = m_textCustomerCode.getText().toString();
		    String customerName = m_textCustomerName.getText().toString();
		    
		    String l_name = m_buttonCustomerClassification1.getText().toString(); //대명
	    	String m_name = m_buttonCustomerClassification2.getText().toString(); //중명
	    	String s_name = m_buttonCustomerClassification3.getText().toString(); //소명    
	    	String pure_pri = m_textPurchasePrice.getText().toString(); //매입가	
		    String sale_pri = m_textSalesPrice.getText().toString(); //판매가
		    int gubun_type = m_spinnerGubunType.getSelectedItemPosition();//상품구분
		    int goods_type = m_spinnerGoodsType.getSelectedItemPosition();//사용구분
		    int group_type = m_spinnerGroupType.getSelectedItemPosition();//그룹구분	
			
		    query += "SELECT Count(*) total FROM Goods ";
			query += " WHERE 1=1 ";
			query += " AND BarCode like '%"+barcode +"%' ";
			query += " AND G_Name like '%"+productName +"%' ";
			query += " AND Bus_Name like '%"+customerName +"%' ";
			query += " AND Bus_Code like '%"+customerCode +"%' ";
			
			if( 0 < l_name.length()) query += " AND L_Name = '"+l_name+"' ";
			if( 0 < m_name.length()) { query += " AND M_Name = '"+m_name+"' ";	}
			if( 0 < s_name.length()) { query += " AND S_Name = '"+s_name+"' ";		}
			if( 0 < pure_pri.length() ) { query += " AND pur_pri = '"+pure_pri+"' ";	 }
			if( 0 < sale_pri.length() ) { query += " AND sell_pri = '"+sale_pri+"' ";		}
			if( gubun_type != 0 ) { // 전체상품
				if( gubun_type == 1 ) { //공산품
					query += " AND Scale_use = '0' ";
				}else if( gubun_type == 2){ //저울상품
					query += " AND Scale_use = '1' ";
				}else if( gubun_type == 3 ){ //부분상품
					query += " AND LEN(BarCode)='4' ";
				}
			}
			if( 0 != goods_type ){ //전체
				if( goods_type == 1 ){ //사용
					query += " AND goods_use = '1' ";
				}else if( goods_type == 2 ){ //미사용
					query += " AND goods_use = '0' ";
				}
			}
			if( 0 != group_type ){ // 그룹
				int a = 65; // A그룹
				for(int i = 1; i <= 12 ; i++){				
					if ( i == group_type){
					query += " AND G_grade = '"+(char)a+"' ";
					}
					a++;
				}
			}	

		    // 콜백함수와 함께 실행
		    new MSSQL(new MSSQL.MSSQLCallbackInterface() {

				@Override
				public void onRequestCompleted(JSONArray results) {
					dialog.dismiss();
					dialog.cancel();
					
					if (results.length() > 0) {
						try {
							m_totalCount = JsonHelper.toStringHashMap(results.getJSONObject(0));							
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					else {
						Toast.makeText(getApplicationContext(), "합계조회 실패", Toast.LENGTH_SHORT).show();
					}
				}
			}).execute(m_ip+":"+m_port, m_uudb, m_uuid, m_uupw, query);
		}
	//마감기준일 불러오기
	/*public void getFinishDate() {

		// 쿼리 작성하기
		String query =  "";
	    query += " Select Finish_Date from finish;";
	    		
	    // 콜백함수와 함께 실행
	    new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				if (results.length()>0) {
					try {
						String Finish_Date = results.getJSONObject(0).getString("Finish_Date");
						Calendar cal = Calendar.getInstance();
						cal.setTime(makeTimeWithStringDateFormat(Finish_Date));
						cal.add(Calendar.DATE, 1);  // number of days to add
						m_Finish_Date = cal.getTime();
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}else{
					Toast.makeText(getApplicationContext(), "조회 실패", Toast.LENGTH_SHORT).show();
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "실패("+String.valueOf(code)+"):" + msg, Toast.LENGTH_SHORT).show();
			}			
	    }).execute(m_ip+":"+m_port, "TIPS", "sa", "tips", query);
	}
	
	//날짜 만들기
	public Date makeTimeWithStringDateFormat(String date) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar c = Calendar.getInstance();
		try {
			c.setTime(sdf.parse(date));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return c.getTime();
	}*/		
		
			
		
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
			actionbar.setTitle("상품목록");
			
			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manage_product, menu);
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
