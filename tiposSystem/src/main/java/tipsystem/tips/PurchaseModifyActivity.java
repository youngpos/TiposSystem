package tipsystem.tips;

import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import kr.co.tipos.tips.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.MSSQL2;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;
import android.support.v4.app.NavUtils;

// 매입등록
public class PurchaseModifyActivity extends Activity implements OnItemClickListener, OnDateSetListener {

	private static final int ZBAR_SCANNER_REQUEST = 0;
	private static final int BARCODE_MANAGER_REQUEST = 2;
	private static final int CUSTOMER_MANAGER_REQUEST = 3;
	private static final int PURCHASE_REGIST_REQUEST = 4;

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

	String m_junpyo = "";
	int m_junpyoInTIdx = 1;

	DatePicker m_datePicker;
	SimpleDateFormat m_dateFormatter;
	Calendar m_dateCalender1;

	ListView m_listReadyToSend;
	SimpleAdapter m_adapter;

	int m_selectedListIndex = -1;

	Button m_period; // 날자선택
	// CheckBox m_immediatePayment; //즉시결재
	EditText m_customerCode; // 거래처코드
	EditText m_customerName; // 거래처명
	EditText m_textBarcode; // 상품바코드
	EditText m_textProductName; // 상품명
	EditText m_et_purchasePrice; // 매입가
	EditText m_et_salePrice; // 판매가
	EditText m_amount; // 수량
	EditText m_profitRatio; // 이익률
	Button m_bt_barcodeSearch; // 상품검색버튼
	CheckBox m_checkBoxRejectedProduct; // 반품고정
	CheckBox m_checkBoxAllcheck; // 전체고정
	CheckBox m_checkBoxPurpri; // 매입가고정
	CheckBox m_checkBoxSellpri; // 판매가고정
	CheckBox m_checkBoxCustomer; // 거래처고정

	List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();
	List<HashMap<String, String>> m_purList = new ArrayList<HashMap<String, String>>();
	HashMap<String, String> m_tempProduct = new HashMap<String, String>();

	private ProgressDialog dialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_purchase_modify);

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

		} catch (JSONException e) {
			e.printStackTrace();
		}

		// 단품 삭제 버튼
		Button btn_SendDelete = (Button) findViewById(R.id.buttonSendDelete);

		// 전체수정완료 전송
		Button btn_SendAll = (Button) findViewById(R.id.buttonSendAll);

		// 상품 상세 목록
		m_listReadyToSend = (ListView) findViewById(R.id.listviewReadyToSendList);

		// 날자 선택 버튼
		m_period = (Button) findViewById(R.id.buttonSetDate1);
		// 즉시결제 선택 체크박스
		// m_immediatePayment =
		// (CheckBox)findViewById(R.id.checkBoxImmediatePayment);
		// 거래처 코드
		m_customerCode = (EditText) findViewById(R.id.editTextCustomerCode);
		// 거래처명
		m_customerName = (EditText) findViewById(R.id.editTextCustomerName);
		// 바코드
		m_textBarcode = (EditText) findViewById(R.id.editTextBarcode);
		// 상품명
		m_textProductName = (EditText) findViewById(R.id.editTextProductName);
		// 매입가
		m_et_purchasePrice = (EditText) findViewById(R.id.editTextPurchasePrice);
		// 판매가
		m_et_salePrice = (EditText) findViewById(R.id.editTextSalePrice);
		// 수량
		m_amount = (EditText) findViewById(R.id.editTextAmount);
		// 이익률
		m_profitRatio = (EditText) findViewById(R.id.editTextProfitRatio);
		// 상품 검색버튼
		m_bt_barcodeSearch = (Button) findViewById(R.id.buttonBarcode);
		// 반품 고정 버튼
		m_checkBoxRejectedProduct = (CheckBox) findViewById(R.id.checkBoxRejectedProduct);
		// 전체고정버튼
		// m_checkBoxAllcheck = (CheckBox)findViewById(R.id.checkboxAllCheck);
		// 매입가 고정
		m_checkBoxPurpri = (CheckBox) findViewById(R.id.checkboxPurPri);
		// 판매가 고정
		m_checkBoxSellpri = (CheckBox) findViewById(R.id.checkboxSellPri);
		// 거래처 고정
		m_checkBoxCustomer = (CheckBox) findViewById(R.id.checkboxCustomer);

		m_dateFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA);
		m_dateCalender1 = Calendar.getInstance();
		m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

		// 전송대기목록 뷰에 값 전달
		Button saveButton = (Button) findViewById(R.id.buttonSave);
		saveButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if (!checkData())
					return;
				doSendListView();
				blockInputMode();
				clearInputBox();
			}
		});

		// 새로 입력 버튼
		Button btn_Renew = (Button) findViewById(R.id.buttonRenew);
		btn_Renew.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				releaseInputMode();
				clearInputBox();
				m_customerCode.setText("");
				m_customerName.setText("");
			}
		});

		// 단품 삭제 버튼
		btn_SendDelete.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				clearInputBox();
				deleteListAll();
			}
		});

		// 매입장 수정 완료 전송 버튼
		btn_SendAll.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				getInTSeq();
				m_selectedListIndex = -1;
			}
		});

		// 바코드 입력 후 포커스 딴 곳을 옮길 경우
		m_textBarcode.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String barcode = m_textBarcode.getText().toString();
					if (!barcode.equals("")) // 입력한 Barcode가 값이 있을 경우만
						doQueryWithBarcode();
				}
			}
		});

		// 매입원가 + 이익률로 판매가
		m_et_purchasePrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					calculateProfitRatio();
				}
			}
		});

		// 매입원가 + 이익률로 판매가
		m_et_salePrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					calculateProfitRatio();
				}
			}
		});
		// 매입원가 + 이익률로 판매가
		m_profitRatio.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			// @Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					String ratio = m_profitRatio.getText().toString();
					String purchasePrice = m_et_purchasePrice.getText().toString();
					String salePrice = m_et_salePrice.getText().toString();

					if (!ratio.equals("") && !purchasePrice.equals("") && !salePrice.equals("")) {

						float f_ratio = Float.valueOf(ratio).floatValue();
						float f_purchasePrice = Float.valueOf(purchasePrice).floatValue();
						float f_salePrice = Float.valueOf(salePrice).floatValue();
						f_purchasePrice = f_salePrice - (f_salePrice * f_ratio / 100);
						m_et_purchasePrice.setText(String.valueOf(f_purchasePrice));
					}
				}
			}
		});

		m_listReadyToSend.setOnItemClickListener(this);

		Typeface typeface = Typeface.createFromAsset(getAssets(), "Fonts/NanumGothic.ttf");
		TextView textView = (TextView) findViewById(R.id.textView2);
		textView.setTypeface(typeface);

		// 입력 대기 모드로 초기화
		releaseInputMode();
	}

	// 매입가 이익률 계산
	private void calculateProfitRatio() {
		String ratio = m_profitRatio.getText().toString();
		String purchasePrice = m_et_purchasePrice.getText().toString();
		String salePrice = m_et_salePrice.getText().toString();

		if (!ratio.equals("") && !purchasePrice.equals("") && !salePrice.equals("")) {

			float f_ratio = Float.valueOf(ratio).floatValue();
			float f_purchasePrice = Float.valueOf(purchasePrice).floatValue();
			float f_salePrice = Float.valueOf(salePrice).floatValue();
			f_ratio = (f_salePrice - f_purchasePrice) / f_salePrice * 100;
			m_profitRatio.setText(String.format("%.2f", f_ratio));
		}
	}

	public String makeJunPyo() {

		String period = m_period.getText().toString();
		int year = Integer.parseInt(period.substring(0, 4));
		int month = Integer.parseInt(period.substring(5, 7));
		int day = Integer.parseInt(period.substring(8, 10));

		// 전표번호 생성
		String posID = "P";
		try {
			String OFFICE_CODE = m_shop.getString("OFFICE_CODE");
			posID = LocalStorage.getString(this, "currentPosID:" + OFFICE_CODE);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		String junpyo = String.format("%04d%02d%02d", year, month, day) + posID + String.format("%03d", m_junpyoInTIdx);
		return junpyo;
	}

	public boolean checkData() {
		String purchaseDate = m_period.getText().toString();
		String barcode = m_textBarcode.getText().toString();
		String productName = m_textProductName.getText().toString();
		String code = m_customerCode.getText().toString();
		String name = m_customerName.getText().toString();
		String purchasePrice = m_et_purchasePrice.getText().toString();
		String salePrice = m_et_salePrice.getText().toString();
		String amount = m_amount.getText().toString();
		String profitRatio = m_profitRatio.getText().toString();

		// 비어 있는 값 확인
		if (code.equals("") || name.equals("") || purchaseDate.equals("") || barcode.equals("")
				|| productName.equals("") || purchasePrice.equals("") || salePrice.equals("") || amount.equals("")
				|| profitRatio.equals("")) {
			Toast.makeText(getApplicationContext(), "값을 모두 입력해주세요.", Toast.LENGTH_SHORT).show();
			return false;
		}

		return true;
	}

	public void deleteData() {

		if (m_selectedListIndex == -1) {
			Toast.makeText(this, "선택된 항목이 없습니다.", Toast.LENGTH_SHORT).show();
			return;
		}

		mfillMaps.remove(m_selectedListIndex);
		m_adapter.notifyDataSetChanged();
		m_purList.remove(m_purList);
		m_selectedListIndex = -1;
	}

	public void deleteListAll() {
		if (mfillMaps.isEmpty())
			return;

		mfillMaps.removeAll(mfillMaps);
		m_adapter.notifyDataSetChanged();
		m_purList.removeAll(m_purList);
		m_selectedListIndex = -1;
	}

	public void clearInputBox() {
		m_selectedListIndex = -1;
		m_textBarcode.setText("");
		m_textProductName.setText("");
		m_bt_barcodeSearch.setText("");
		m_et_purchasePrice.setText("");
		m_et_salePrice.setText("");
		m_amount.setText("");
		m_profitRatio.setText("");
		m_textBarcode.requestFocus();
	}

	public void blockInputMode() {
		m_textBarcode.setEnabled(true);
		m_textProductName.setEnabled(true);
		m_period.setEnabled(false);
		m_checkBoxRejectedProduct.setEnabled(false);
		m_bt_barcodeSearch.setEnabled(true);
		m_customerCode.setEnabled(true);
		m_customerName.setEnabled(true);
		m_et_purchasePrice.setEnabled(true);
		m_et_salePrice.setEnabled(true);
		m_amount.setEnabled(true);
		m_profitRatio.setEnabled(true);
	}

	// 새로입력모드 초기화
	public void releaseInputMode() {
		m_textBarcode.setEnabled(true);
		m_textProductName.setEnabled(true);
		m_period.setEnabled(true);
		m_checkBoxRejectedProduct.setEnabled(true);
		m_bt_barcodeSearch.setEnabled(true);
		m_customerCode.setEnabled(true);
		m_customerName.setEnabled(true);
		m_et_purchasePrice.setEnabled(true);
		m_et_salePrice.setEnabled(true);
		m_amount.setEnabled(true);
		m_profitRatio.setEnabled(true);
	}

	// 저장 버튼
	public void doSendListView() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String currentDate = sdf.format(new Date());

		// boolean immediatePayment = m_immediatePayment.isChecked();
		String purchaseDate = m_period.getText().toString();
		String barcode = m_textBarcode.getText().toString();
		String productName = m_textProductName.getText().toString();
		String code = m_customerCode.getText().toString();
		String name = m_customerName.getText().toString();
		String purchasePrice = m_et_purchasePrice.getText().toString();
		String salePrice = m_et_salePrice.getText().toString();
		String amount = m_amount.getText().toString();
		String profitRatio = m_profitRatio.getText().toString();

		JSONObject userProfile = LocalStorage.getJSONObject(this, "userProfile");
		String userid = "";
		try {
			userid = userProfile.getString("User_ID");
		} catch (JSONException e) {
			e.printStackTrace();
		}

		// 선택된 상품장의 데이터를 꺼내옴
		String Org_PurPri = m_tempProduct.get("Pur_Pri"); // 원매입가
		String Org_SellPri = m_tempProduct.get("Sell_Pri"); // 원매출가
		String Pur_Cost = m_tempProduct.get("Pur_Cost"); // 매입원가
		String Add_Tax = m_tempProduct.get("Add_Tax"); // 부가세
		String Bot_Pur = m_tempProduct.get("Bot_Pur"); // 공병매입가
		String Bot_Sell = m_tempProduct.get("Bot_Sell"); // 공병매출가
		String Tax_YN = m_tempProduct.get("Tax_YN"); // 과세여부(0:면세,1:과세)
		String Tax_Gubun = m_tempProduct.get("VAT_CHK"); // 부가세구분(0:별도,1:포함)
		String In_YN = m_tempProduct.get("In_YN"); // 매입여부(0:반품,1:매입,2:행사반품,3:행사매입)
		String Box_Use = m_tempProduct.get("Box_Use"); //
		String Pack_Use = m_tempProduct.get("Pack_Use"); //
		String Bot_Code = m_tempProduct.get("Bot_Code"); //
		String Bot_Name = m_tempProduct.get("Bot_Name"); //

		// 매입여부(0:반품,1:매입,2:행사반품,3:행사매입)
		if (In_YN.equals("3")) { // 행사상품일때
			if (m_checkBoxRejectedProduct.isChecked()) // 반품
				In_YN = "2";
			else
				In_YN = "3";
		} else {
			if (m_checkBoxRejectedProduct.isChecked()) // 반품
				In_YN = "0";
			else
				In_YN = "1";
		}

		if (Bot_Pur == null)
			Bot_Pur = "0";
		if (Bot_Sell == null)
			Bot_Sell = "0";

		if (m_checkBoxRejectedProduct.isChecked()) {
			amount = String.valueOf(Integer.valueOf(amount) * -1);
		}

		double In_Pri = Double.valueOf(purchasePrice) * Double.valueOf(amount); // 총
																				// 매입가(공병포함)=매입가x수량
		double In_SellPri = Double.valueOf(salePrice) * Double.valueOf(amount); // 판매가x수량

		String period = m_period.getText().toString();

		HashMap<String, String> rmap = new HashMap<String, String>();
		rmap.put("purchaseDate", purchaseDate);
		// rmap.put("immediatePayment", (immediatePayment)?"1":"0");
		rmap.put("In_YN", In_YN);
		rmap.put("In_Gubun", "3");
		rmap.put("In_Date", period);
		rmap.put("BarCode", barcode);
		rmap.put("G_Name", productName);
		rmap.put("Office_Code", code);
		rmap.put("Office_Name", name);
		rmap.put("Tax_YN", Tax_YN);
		rmap.put("Tax_Gubun", Tax_Gubun);
		rmap.put("In_Count", amount);// 수량
		rmap.put("Pur_Pri", purchasePrice); // 매입가
		rmap.put("Org_PurPri", Org_PurPri);
		rmap.put("Pur_Cost", Pur_Cost);
		rmap.put("Add_Tax", Add_Tax);
		rmap.put("TPur_Pri",
				String.valueOf((Double.valueOf(purchasePrice) - Double.valueOf(Bot_Pur)) * Double.valueOf(amount))); // 총
																														// 매입가(공병제외)=매입가x수량
		rmap.put("TPur_Cost", String.valueOf(Double.valueOf(Pur_Cost) * Double.valueOf(amount))); // 매입원가x수량
		rmap.put("TAdd_Tax", String.valueOf(Double.valueOf(Add_Tax) * Double.valueOf(amount))); // 부가세x수량
		rmap.put("In_Pri", String.valueOf(In_Pri)); // 총 매입가(공병포함)=매입가x수량
		rmap.put("Sell_Pri", salePrice);
		rmap.put("Org_SellPri", Org_SellPri);
		rmap.put("TSell_Pri",
				String.valueOf((Double.valueOf(salePrice) - Double.valueOf(Bot_Sell)) * Double.valueOf(amount))); // 총
																													// 판매가(공병제외)=판매가x수량
		rmap.put("In_SellPri", String.valueOf(In_SellPri)); // 판매가x수량
		rmap.put("Profit_Pri", String.valueOf(In_SellPri - In_Pri)); // 이익금
		rmap.put("Profit_Rate", profitRatio); // 이익률
		rmap.put("Box_Use", Box_Use); //
		rmap.put("Pack_Use", Pack_Use); //
		rmap.put("Bot_Code", Bot_Code); //
		rmap.put("Bot_Name", Bot_Name); //
		rmap.put("Write_Date", currentDate);
		rmap.put("Edit_Date", currentDate);
		rmap.put("Writer", userid);
		rmap.put("Editor", userid);
		rmap.put("Bot_Pur", Bot_Pur);
		rmap.put("Bot_Sell", Bot_Sell);

		m_purList.add(rmap);

		// ListView 에 뿌려줌
		mfillMaps = makeFillvapsWithStockList();

		String[] from = new String[] { "Office_Code", "Office_Name", "TPur_Pri", "In_Count" };
		int[] to = new int[] { R.id.item1, R.id.item2, R.id.item3, R.id.item4 };
		m_adapter = new SimpleAdapter(this, mfillMaps, R.layout.activity_listview_item4_1, from, to);
		m_listReadyToSend.setAdapter(m_adapter);

		m_adapter.notifyDataSetChanged();
	}

	public List<HashMap<String, String>> makeFillvapsWithStockList() {
		List<HashMap<String, String>> fm = new ArrayList<HashMap<String, String>>();

		Iterator<HashMap<String, String>> iterator = m_purList.iterator();
		while (iterator.hasNext()) {
			boolean isNew = true;
			HashMap<String, String> element = iterator.next();
			String Office_Code = element.get("Office_Code");

			Iterator<HashMap<String, String>> fm_iterator = fm.iterator();
			while (fm_iterator.hasNext()) {

				HashMap<String, String> fm_element = fm_iterator.next();
				String fm_Office_Code = fm_element.get("Office_Code");

				if (fm_Office_Code.equals(Office_Code)) {
					isNew = false;
					// 같은게 있으면 fm_element에 추가
					String fm_Pur_Pri = fm_element.get("TPur_Pri");
					String fm_In_Count = fm_element.get("In_Count");
					String Pur_Pri = element.get("TPur_Pri");
					String In_Count = element.get("In_Count");

					fm_element.put("TPur_Pri", String.valueOf(Double.valueOf(Pur_Pri) + Double.valueOf(fm_Pur_Pri)));
					fm_element.put("In_Count",
							String.valueOf(Integer.valueOf(In_Count) + Integer.valueOf(fm_In_Count)));
				}
			}

			if (isNew) {
				String BarCode = element.get("BarCode");
				String Office_Name = element.get("Office_Name");
				String Pur_Pri = element.get("TPur_Pri");
				String In_Count = element.get("In_Count");
				String purchaseDate = element.get("purchaseDate");
				HashMap<String, String> map = new HashMap<String, String>();

				map.put("Office_Code", Office_Code);
				map.put("BarCode", BarCode);
				map.put("Office_Name", Office_Name);
				map.put("TPur_Pri", Pur_Pri);
				map.put("In_Count", In_Count);
				map.put("purchaseDate", purchaseDate);

				fm.add(map);
			}
		}

		return fm;
	}

	// Comparator 를 만든다.
	private final static Comparator<HashMap<String, String>> myComparator = new Comparator<HashMap<String, String>>() {

		private final Collator collator = Collator.getInstance();

		@Override
		public int compare(HashMap<String, String> lhs, HashMap<String, String> rhs) {
			String In_Num1 = lhs.get("Office_Code");
			String In_Num2 = rhs.get("Office_Code");

			return collator.compare(In_Num1, In_Num2);
		}
	};

	// MSSQL
	public void sendAllData() {

		String tableName = null, tableName2 = null;
		String period = m_period.getText().toString();

		int year = Integer.parseInt(period.substring(0, 4));
		int month = Integer.parseInt(period.substring(5, 7));

		tableName = String.format("InD_%04d%02d", year, month);
		tableName2 = String.format("InT_%04d%02d", year, month);

		// 거래처기준으로 정렬
		Collections.sort(m_purList, myComparator);

		// 쿼리 작성하기 (Ind 테이블)
		String query = "", prevOffice_Code = "", curOffice_Code = "";
		String junpyo = makeJunPyo();

		double TTPur_Pri = 0, TTAdd_Tax = 0, TFPur_Pri = 0, In_TPri = 0, In_FPri = 0, In_Pri = 0, In_RePri = 0;
		double TSell_Pri = 0, In_SellPri = 0, Bot_Pri = 0, Bot_SellPri = 0, Profit_Pri = 0, Profit_Rate = 0;
		boolean isIncludedYN = false; // 전표내의 물건중 반품이 포함된경우,

		for (int i = 0, seq = 1, tseq = 1; i < m_purList.size(); i++, seq++) {

			HashMap<String, String> pur = m_purList.get(i);
			curOffice_Code = pur.get("Office_Code");
			// 새로운 거래처이면 새로운 전표 발행
			if (!prevOffice_Code.equals(curOffice_Code)) {
				junpyo = makeJunPyo();
				seq = 1;
				isIncludedYN = false;
			}
			prevOffice_Code = pur.get("Office_Code");

			// 매입상세 (InD)
			query += "insert into " + tableName
					+ "(In_Num, In_BarCode, In_YN, In_Gubun, In_Date, BarCode, In_Seq, Office_Code, Office_Name, Tax_YN, Tax_Gubun, "
					+ "In_Count, Pur_Pri, Org_PurPri, Pur_Cost, Add_Tax, TPur_Pri, TPur_Cost, TAdd_Tax, In_Pri, "
					+ "Sell_Pri, Org_SellPri, TSell_Pri, In_SellPri, Profit_Pri, Profit_Rate, Box_Use, Pack_Use, "
					+ "Bot_Code, Bot_Name, Write_Date, Edit_Date, Writer, Editor) " + " values (" + "'" + junpyo + "', "
					+ "'" + junpyo + "', " + "'" + pur.get("In_YN").toString() + "', " + "'"
					+ pur.get("In_Gubun").toString() + "', " + "'" + pur.get("In_Date").toString() + "', " + "'"
					+ pur.get("BarCode").toString() + "', " + "'" + seq + "', " + "'"
					+ pur.get("Office_Code").toString() + "', " + "'" + pur.get("Office_Name").toString() + "', " + "'"
					+ pur.get("Tax_YN").toString() + "', " + "'" + pur.get("Tax_Gubun").toString() + "', " + "'"
					+ pur.get("In_Count").toString() + "', " + "'" + pur.get("Pur_Pri").toString() + "', " + "'"
					+ pur.get("Org_PurPri").toString() + "', " + "'" + pur.get("Pur_Cost").toString() + "', " + "'"
					+ pur.get("Add_Tax").toString() + "', " + "'" + pur.get("TPur_Pri").toString() + "', " + "'"
					+ pur.get("TPur_Cost").toString() + "', " + "'" + pur.get("TAdd_Tax").toString() + "', " + "'"
					+ pur.get("In_Pri").toString() + "', " + "'" + pur.get("Sell_Pri").toString() + "', " + "'"
					+ pur.get("Org_SellPri").toString() + "', " + "'" + pur.get("TSell_Pri").toString() + "', " + "'"
					+ pur.get("In_SellPri").toString() + "', " + "'" + pur.get("Profit_Pri").toString() + "', " + "'"
					+ pur.get("Profit_Rate").toString() + "', " + "'" + pur.get("Box_Use").toString() + "', " + "'"
					+ pur.get("Pack_Use").toString() + "', " + "'" + pur.get("Bot_Code").toString() + "', " + "'"
					+ pur.get("Bot_Name").toString() + "', " + "'" + pur.get("Write_Date").toString() + "', " + "'"
					+ pur.get("Edit_Date").toString() + "', " + "'" + pur.get("Writer").toString() + "', " + "'"
					+ pur.get("Editor").toString() + "');";

			// InT용 데이터 누적시킴
			String Tax_YN = pur.get("Tax_YN"); // 과세여부(0:면세,1:과세)
			String In_YN = pur.get("In_YN"); // 매입여부(0:반품,1:매입,2:행사반품,3:행사매입)

			if (In_YN.equals("0"))
				isIncludedYN = true; // 전표내의 물건중 반품이 포함된경우,

			TTAdd_Tax += Double.valueOf(pur.get("Add_Tax")); // 총 과세 부가세
			if (Tax_YN.equals("1"))
				TTPur_Pri += Double.valueOf(pur.get("TPur_Pri")); // 총
																	// 과세매입가(공병제외)
			if (Tax_YN.equals("0"))
				TFPur_Pri += Double.valueOf(pur.get("TPur_Pri")); // 총
																	// 면세매입가(공병제외)
			if (Tax_YN.equals("1"))
				In_TPri += Double.valueOf(pur.get("In_Pri")); // 총 과세매입가(공병포함)
			if (Tax_YN.equals("0"))
				In_FPri += Double.valueOf(pur.get("In_Pri")); // 총 면세매입가(공병포함)
			if (In_YN.equals("0"))
				In_RePri += Double.valueOf(pur.get("In_Pri")) * -1; // 총
																	// 반품가(공병포함)
			In_Pri += Double.valueOf(pur.get("In_Pri")); // 총 매입가(공병포함)
			TSell_Pri += Double.valueOf(pur.get("TSell_Pri")); // 총 판매가(공병제외)
			In_SellPri += Double.valueOf(pur.get("In_SellPri")); // 총 판매가(공병포함)
			Bot_Pri += Double.valueOf(pur.get("Bot_Pur")); // 공병 총 매입가
			Bot_SellPri += Double.valueOf(pur.get("Bot_Sell")); // 공병 총 판매가
			Profit_Pri = In_SellPri - In_Pri;
			Profit_Rate = Profit_Pri / In_SellPri * 100;

			// 마지막이거나 다음것이 새로운 거래처이면,
			if (m_purList.size() >= i + 1) {
				if (m_purList.size() > i + 1) {
					HashMap<String, String> nextPur = m_purList.get(i + 1);
					if (curOffice_Code.equals(nextPur.get("Office_Code")))
						continue;
				}

				// 매입총괄(InT) 쿼리생성
				query += "insert into " + tableName2 + "(In_Num, In_Date, In_Seq, Office_Code, Office_Name, "
						+ "TTPur_Pri, TTAdd_Tax, TFPur_Pri, In_TPri, In_FPri, In_Pri, In_RePri, "
						+ "TSell_Pri, In_SellPri, Bot_Pri, Bot_SellPri, Profit_Pri, Profit_Rate, "
						+ "Write_Date, Edit_Date, Writer, Editor) " + " values (" + "'" + junpyo + "', " + "'"
						+ pur.get("In_Date").toString() + "', " + "'" + String.valueOf(m_junpyoInTIdx) + "', " + "'"
						+ pur.get("Office_Code").toString() + "', " + "'" + pur.get("Office_Name").toString() + "', "
						+ "'" + String.valueOf(TTPur_Pri) + "', " + "'" + String.valueOf(TTAdd_Tax) + "', " + "'"
						+ String.valueOf(TFPur_Pri) + "', " + "'" + String.valueOf(In_TPri) + "', " + "'"
						+ String.valueOf(In_FPri) + "', " + "'" + String.valueOf(In_Pri) + "', " + "'"
						+ String.valueOf(In_RePri) + "', " + "'" + String.valueOf(TSell_Pri) + "', " + "'"
						+ String.valueOf(In_SellPri) + "', " + "'" + String.valueOf(Bot_Pri) + "', " + "'"
						+ String.valueOf(Bot_SellPri) + "', " + "'" + String.valueOf(Profit_Pri) + "', " + "'"
						+ String.valueOf(Profit_Rate) + "', " + "'" + pur.get("Write_Date").toString() + "', " + "'"
						+ pur.get("Edit_Date").toString() + "', " + "'" + pur.get("Writer").toString() + "', " + "'"
						+ pur.get("Editor").toString() + "');";

				// 거래처 대금 결제 (Office_Settlement)
				/*
				 * String immediatePayment =
				 * pur.get("immediatePayment").toString() ; if
				 * (immediatePayment.equals("1")) { // 즉시결제 query +=
				 * " insert into Office_Settlement ( Sale_Code, Pro_Date, In_Seq, Office_Code, Office_Name, Pay_Pri, "
				 * +
				 * " Sale_Pri, Sale_Rate, Gubun, Bigo, Write_Date, Edit_Date, Writer, Editor) Values ("
				 * + "'" + junpyo + "', " + "'" + pur.get("In_Date").toString()
				 * + "', " + "'" + tseq + "', " + "'" +
				 * pur.get("Office_Code").toString() + "', " + "'" +
				 * pur.get("Office_Name").toString() + "', " + "'" +
				 * String.valueOf(In_Pri) + "', " + "'0', " + "'0', " + "'3', "
				 * + "'매입시 즉시결제', " + "'" + pur.get("Write_Date").toString() +
				 * "', " + "'" + pur.get("Edit_Date").toString() + "', " + "'" +
				 * pur.get("Writer").toString() + "', " + "'" +
				 * pur.get("Editor").toString() + "');"; }
				 */

				if (isIncludedYN) { // 반품
					query += " insert into Office_Settlement ( Sale_Code, Pro_Date, In_Seq, Office_Code, Office_Name, Buy_RePri, "
							+ " Sale_Pri, Sale_Rate, Gubun, Bigo, Write_Date, Edit_Date, Writer, Editor) Values (" + "'"
							+ junpyo + "', " + "'" + pur.get("In_Date").toString() + "', " + "'" + tseq + "', " + "'"
							+ pur.get("Office_Code").toString() + "', " + "'" + pur.get("Office_Name").toString()
							+ "', " + "'" + String.valueOf(In_RePri) + "', " + "'0', " + "'0', " + "'4', " + "'', "
							+ "'" + pur.get("Write_Date").toString() + "', " + "'" + pur.get("Edit_Date").toString()
							+ "', " + "'" + pur.get("Writer").toString() + "', " + "'" + pur.get("Editor").toString()
							+ "');";
				}
				{ // 매입
					query += " insert into Office_Settlement ( Sale_Code, Pro_Date, In_Seq, Office_Code, Office_Name, Buy_Pri, "
							+ " Sale_Pri, Sale_Rate, Gubun, Bigo, Write_Date, Edit_Date, Writer, Editor) Values (" + "'"
							+ junpyo + "', " + "'" + pur.get("In_Date").toString() + "', " + "'" + tseq + "', " + "'"
							+ pur.get("Office_Code").toString() + "', " + "'" + pur.get("Office_Name").toString()
							+ "', " + "'" + String.valueOf(In_Pri + In_RePri) + "', " + "'0', " + "'0', " + "'2', "
							+ "'', " + "'" + pur.get("Write_Date").toString() + "', " + "'"
							+ pur.get("Edit_Date").toString() + "', " + "'" + pur.get("Writer").toString() + "', " + "'"
							+ pur.get("Editor").toString() + "');";
				}

				// 미수금액 업데이트
				String Dec_Pri = String.valueOf(In_TPri);
				query += " update Office_Manage Set Dec_Pri = IsNull(Dec_Pri, 0) + '" + Dec_Pri
						+ "' where Office_Code='" + junpyo + "';";

				m_junpyoInTIdx++;
			}

			// 재고등록
			query += "update Goods Set Real_Sto=Real_Sto + IsNull(B.In_Count,0) "
					+ "From Goods A Inner Join ( Select Barcode, Sum (In_Count) In_Count "
					+ " From (Select A.BarCode, IsNull(Sum(A.In_Count),0) In_Count " + " From " + tableName
					+ " A, Goods B " + " Where A.Barcode=B.Barcode AND A.Box_Use='0' AND A.Pack_Use='0' AND A.In_Num ='"
					+ junpyo + "' Group by A.Barcode "
					+ " Union All Select B.BarCode1, IsNull(Sum(A.In_Count * IsNull(B.Obtain,0)), 0) In_Count "
					+ " From " + tableName + " A, Goods B "
					+ "  Where A.Barcode=B.Barcode AND A.Box_Use='1' AND A.In_Num = '" + junpyo
					+ "' Group By B.BarCode1 "
					+ " Union All Select C.C_BARCODE BARCODE, IsNull(Sum(In_Count*C.G_COUNT), 0) InD_Count " + " From "
					+ tableName + " A, Goods B, Bundle C "
					+ "  Where A.BARCODE=B.BARCODE AND A.BARCODE=C.P_BARCODE AND A.PACK_USE='1' AND A.In_NUm='" + junpyo
					+ "' Group By C.C_BARCODE) " + " X Group By Barcode )" + " B On A.Barcode=B.Barcode;";

		}

		query += " select * from " + tableName + " where In_Num='" + junpyo + "';";

		Log.i("r", query);
		// if (true )return;

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

				deleteListAll();
				clearInputBox();
				Toast.makeText(getApplicationContext(), "전송완료.", Toast.LENGTH_SHORT).show();
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

	// 전표갯수를 구함
	public void getInTSeq() {

		String tableName = null;
		String period = m_period.getText().toString();

		int year = Integer.parseInt(period.substring(0, 4));
		int month = Integer.parseInt(period.substring(5, 7));

		tableName = String.format("InT_%04d%02d", year, month);

		// 쿼리 작성하기
		String query = "";
		query += "SELECT TOP 1 In_Seq, In_Num FROM " + tableName + " WHERE In_Date='" + period
				+ "' AND (In_Seq NOT IN(SELECT TOP 0 In_Seq FROM " + tableName + ")) order by In_Num DESC;";

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
				if (results.length() > 0) {
					try {
						m_junpyoInTIdx = results.getJSONObject(0).getInt("In_Seq") + 1;

					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					m_junpyoInTIdx = 1;
				}
				sendAllData();
			}

			@Override
			public void onRequestFailed(int code, String msg) {
				dialog.dismiss();
				dialog.cancel();
				Toast.makeText(getApplicationContext(), "실패(" + String.valueOf(code) + "):" + msg, Toast.LENGTH_SHORT)
						.show();
			}

		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		// 카메라 스캔을 통한 바코드 검색
		case ZBAR_SCANNER_REQUEST:
			if (resultCode == RESULT_OK) {
				// Scan result is available by making a call to
				// data.getStringExtra(ZBarConstants.SCAN_RESULT)
				// Type of the scan result is available by making a call to
				// data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
				Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT),
						Toast.LENGTH_SHORT).show();
				Toast.makeText(this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE),
						Toast.LENGTH_SHORT).show();
				// The value of type indicates one of the symbols listed in
				// Advanced Options below.

				String barcode = data.getStringExtra(ZBarConstants.SCAN_RESULT);
				m_textBarcode.setText(barcode);
				doQueryWithBarcode();

			} else if (resultCode == RESULT_CANCELED) {
				Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
			}
			break;
		// 목록 검색을 통한 바코드 검색
		case BARCODE_MANAGER_REQUEST:
			if (resultCode == RESULT_OK && data != null) {
				HashMap<String, String> hashMap = (HashMap<String, String>) data.getSerializableExtra("fillmaps");
				m_textBarcode.setText(hashMap.get("BarCode"));
				doQueryWithBarcode();
			}
			break;
		case CUSTOMER_MANAGER_REQUEST:
			if (resultCode == RESULT_OK && data != null) {
				HashMap<String, String> hashMap = (HashMap<String, String>) data.getSerializableExtra("fillmaps");
				m_customerCode.setText(hashMap.get("Office_Code"));
				m_customerName.setText(hashMap.get("Office_Name"));
			}
			break;
		}
	}

	public void onCustomerSearch(View view) {
		String customer = m_customerCode.getText().toString();
		String customername = m_customerName.getText().toString();
		Intent intent = new Intent(this, ManageCustomerListActivity.class);
		intent.putExtra("customer", customer);
		intent.putExtra("customername", customername);
		startActivityForResult(intent, CUSTOMER_MANAGER_REQUEST);
	}

	public void onBarcodeSearch(View view) {
		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		String list_value = pref.getString("prefSearchMethod", "");
		if (list_value.equals("camera")) {
			startCameraSearch();
		} else if (list_value.equals("list")) {
			startProductList();
		} else {
			// 바코드 검색 버튼 클릭시 나오는 목록 셋팅
			final String[] option = new String[] { "목록", "카메라" };
			//ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.select_dialog_item, option);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.user_select_dialog_item, option);
			AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
			builder.setTitle("Select Option");

			// 목록 선택시 이벤트 처리
			builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {

					if (which == 0) { // 목록으로 조회할 경우
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
		String officecode = m_customerCode.getText().toString();
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

	// SQL QUERY 실행
	public void doQueryWithBarcode() {

		String query = "";
		String barcode = m_textBarcode.getText().toString();
		query = "SELECT * FROM Goods WHERE Barcode = '" + barcode + "';";

		if (barcode.equals(""))
			return;

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
						m_tempProduct = JsonHelper.toStringHashMap(results.getJSONObject(0));
						m_tempProduct.put("In_YN", "0");
						m_textProductName.setText(results.getJSONObject(0).getString("G_Name"));
						String s = results.getJSONObject(0).getString("Pur_Pri");
						m_et_purchasePrice.setText(s);
						m_et_salePrice.setText(results.getJSONObject(0).getString("Sell_Pri"));
						m_profitRatio.setText(results.getJSONObject(0).getString("Profit_Rate"));
						m_amount.requestFocus();

						m_customerCode.setText(results.getJSONObject(0).getString("Bus_Code"));
						m_customerName.setText(results.getJSONObject(0).getString("Bus_Name"));

						doQueryWithBarcode2(m_textBarcode.getText().toString());
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					// 새로등록
					DialogInterface.OnClickListener newBarcodeListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							String barcode = m_textBarcode.getText().toString();

							Intent intent = new Intent(PurchaseModifyActivity.this, ManageProductActivity.class);
							intent.putExtra("barcode", barcode);
							startActivity(intent);
						}
					};

					DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					};

					new AlertDialog.Builder(PurchaseModifyActivity.this,AlertDialog.THEME_HOLO_LIGHT).setTitle("등록되지않은 바코드입니다")
							.setNeutralButton("새로등록", newBarcodeListener).setNegativeButton("취소", cancelListener)
							.show();
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {

			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
	}

	// 행사상품인지 검사
	public void doQueryWithBarcode2(String barcode) {

		String date = m_period.getText().toString();
		String query = "";
		query += "Select A.BarCode, B.G_Name, B.Std_Size, A.Sale_Pur, A.Sale_Sell, A.Profit_Rate, A.Profit_Pri, B.Tax_YN, B.Bot_Code, B.Bot_Name, B.Bot_Pur, B.Bot_Sell, B.Box_Use, B.Pack_Use, B.VAT_CHK";
		query += " From Evt_Mst As A Inner Join Goods As B ";
		query += " On A.BarCode = B.BarCode ";
		query += " Where A.BarCode = '" + barcode + "' AND (INEvt_SDate <= '" + date + "' And INEvt_EDate >= '" + date
				+ "') ";
		query += " ORDER BY A.SALE_PUR ";

		// 콜백함수와 함께 실행
		new MSSQL2(new MSSQL2.MSSQL2CallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				dialog.dismiss();
				dialog.cancel();

				if (results.length() > 0) {
					try {
						m_et_purchasePrice.setText(results.getJSONObject(0).getString("Sale_Pur"));
						m_et_salePrice.setText(results.getJSONObject(0).getString("Sale_Sell"));

						m_tempProduct.put("In_YN", "3"); // 행사상품인경우, 마크
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onRequestFailed(int code, String msg) {

			}
		}).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
		CheckBox checkbox = ((CheckBox) arg1.findViewById(R.id.item5));
		if (checkbox != null) {
			checkbox.setChecked(!checkbox.isChecked());
			// 베리 킥 포인트 요거 꼭해줘야 checkbox 에서 바로 바로 적용됩니다.
			m_adapter.notifyDataSetChanged();
		}

		String Office_Code = mfillMaps.get(position).get("Office_Code");
		String Office_Name = mfillMaps.get(position).get("Office_Name");
		String purchaseDate = mfillMaps.get(position).get("purchaseDate");

		Iterator<HashMap<String, String>> iterator = m_purList.iterator();

		JSONArray array = new JSONArray();

		while (iterator.hasNext()) {
			HashMap<String, String> element = iterator.next();
			String eOffice_Code = element.get("Office_Code");

			if (eOffice_Code.equals(Office_Code)) {
				JSONObject object = new JSONObject(element);
				array.put(object);
			}
		}

		Intent intent = new Intent(this, PurchaseDetailActivity.class);
		intent.putExtra("data", array.toString());
		intent.putExtra("Office_Code", Office_Code);
		intent.putExtra("Office_Name", Office_Name);
		intent.putExtra("purchaseDate", purchaseDate);
		startActivityForResult(intent, PURCHASE_REGIST_REQUEST);
	}

	public void onClickSetDate1(View v) {
		DatePickerDialog newDlg = new DatePickerDialog(this, this, m_dateCalender1.get(Calendar.YEAR),
				m_dateCalender1.get(Calendar.MONTH), m_dateCalender1.get(Calendar.DAY_OF_MONTH));
		newDlg.show();
	};

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		m_dateCalender1.set(year, monthOfYear, dayOfMonth);
		m_period.setText(m_dateFormatter.format(m_dateCalender1.getTime()));

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
			actionbar.setTitle("매입신규등록");

			getActionBar().setDisplayHomeAsUpEnabled(false);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.purchase_regist, menu);
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
