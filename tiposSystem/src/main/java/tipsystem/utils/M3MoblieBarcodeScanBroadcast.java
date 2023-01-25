package tipsystem.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.EditText;

import org.json.JSONArray;

public class M3MoblieBarcodeScanBroadcast extends BroadcastReceiver{

	private String barcode;
	private String type;
	private String module;
	private byte[] rawdata;
	private int length;	
	
	//개별 페이지에 심어도 됩니다.
	private static final String SCANNER_ACTION_SETTING_CHANGE = "com.android.server.scannerservice.settingchange";
	public static final String SCANNER_ACTION_PARAMETER = "android.intent.action.SCANNER_PARAMETER";
	public static final String SCANNER_ACTION_ENABLE = "com.android.server.scannerservice.m3onoff";
	public static final String SCANNER_EXTRA_ENABLE = "scanneronoff";

	public static final String SCANNER_ACTION_START = "android.intent.action.M3SCANNER_BUTTON_DOWN";
	public static final String SCANNER_ACTION_CANCEL = "android.intent.action.M3SCANNER_BUTTON_UP";	
	public static final String SCANNER_ACTION_BARCODE = "com.android.server.scannerservice.broadcast";
	//여기까지
	
	public static EditText mBarcode_text;
	public static EditText mFocusMove;
	public static Context mM3moblicContext;
	
	private static final String SCANNER_EXTRA_BARCODE_DATA = "m3scannerdata";
	private static final String SCANNER_EXTRA_BARCODE_CODE_TYPE = "m3scanner_code_type";
	private static final String SCANNER_EXTRA_MODULE_TYPE = "m3scanner_module_type";
	private static final String SCANNER_EXTRA_BARCODE_RAW_DATA = "m3scannerdata_raw";	// add 2017-01-17	after ScanEmul 1.3.0
	private static final String SCANNER_EXTRA_BARCODE_DATA_LENGTH = "m3scannerdata_length";	// add 2017-01-31	after ScanEmul 1.3.0




	//바코드를 반환합니다.
	/**
	 * 바코드 반환받을 객체를 넘깁니다.
	 * @param et 바코드를 입력 받을 EditText객체를 넘깁니다.
	 * @param fm 포커스 이동할 객체를 넘깁니다.
	 */
	public static void setBarcodeEditText(Context m3cxt, EditText et, EditText fm){
				
		mM3moblicContext = m3cxt;
		
		//바코드 반환합니다.
		if(et != null){
			mBarcode_text = et;
		}
		
		//포커스를 이동합니다.
		if(fm != null){
			mFocusMove = fm;
		}
		
	}


	public interface M3MoblieCallbackInterface {
		public void onRequestCompleted(boolean result);
	}

	public static M3MoblieBarcodeScanBroadcast.M3MoblieCallbackInterface mCallback;

	//바코드를 반환합니다.
	/**
	 * 바코드 반환받을 객체를 넘깁니다.
	 * @param et 바코드를 입력 받을 EditText객체를 넘깁니다.
	 *
	 */
	public static void setBarcodeEditText(Context m3cxt, EditText et, M3MoblieBarcodeScanBroadcast.M3MoblieCallbackInterface callback){

		mM3moblicContext = m3cxt;

		//바코드 반환합니다.
		if(et != null){
			mBarcode_text = et;
		}

		mCallback = callback;
	}

	
	/**
	 * 객체를 null 상태로 만듭니다.
	 */
	public static void setLostEditText(){
		
		mM3moblicContext = null;
		mBarcode_text = null;
		mFocusMove = null;
		mCallback = null;
	}	
	
	
	public static void setOnResume(){
		
		Intent intent = new Intent(SCANNER_ACTION_SETTING_CHANGE);
	    intent.putExtra("setting", "output_mode");
	    intent.putExtra("output_mode_value", 2);		
	    mM3moblicContext.sendOrderedBroadcast(intent, null);
		
	}
	
	public static void setOnDestory(){
		
		Intent intent = new Intent(SCANNER_ACTION_SETTING_CHANGE);
		intent.putExtra("setting", "output_mode");
		intent.putExtra("output_mode_value", 1);
		
		mM3moblicContext.sendOrderedBroadcast(intent, null);
				
		intent.putExtra("setting", "read_mode");		
		intent.putExtra("read_mode_value", 0);	
		mM3moblicContext.sendOrderedBroadcast(intent, null);
		
		setLostEditText();
		
	}
	
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		
		if (intent.getAction().equals("com.android.server.scannerservice.broadcast")) {
			
			barcode = intent.getExtras().getString(SCANNER_EXTRA_BARCODE_DATA);
			type = intent.getExtras().getString(SCANNER_EXTRA_BARCODE_CODE_TYPE);
			module = intent.getExtras().getString(SCANNER_EXTRA_MODULE_TYPE); 
			rawdata = intent.getExtras().getByteArray(SCANNER_EXTRA_BARCODE_RAW_DATA);
			length = intent.getExtras().getInt(SCANNER_EXTRA_BARCODE_DATA_LENGTH);
			
			if(barcode != null){
				
				/*if(rawdata.length > 0){

					String strRawData = "";
					for(int i = 0; i< length; i++){
						strRawData += String.format("0x%02X ", (int)rawdata[i]&0xFF);		
					}
					
					//m_textBarcode.setText("data: " + barcode + " \ntype: " + type + " \nraw: " + strRawData);	
					m_textBarcode.setText(barcode+strRawData);
											
				}else{*/
					//mTvResult.setText("data: " + barcode + " type: " + type);	
				//바코드를 반환 합니다.
				if(mBarcode_text != null){
					mBarcode_text.requestFocus();
					mBarcode_text.setText(barcode.trim());
				}

				//if(mM3moblicContext.getClass().getName().equals("tipsystem.tips.ManageHomeDeliveryCalActivity")){
				if(mCallback != null){
					mCallback.onRequestCompleted(true);
					Log.d("M3Moblie", mM3moblicContext.getClass().getName());
				}
				
				//포커스를 이동합니다.
				if(mFocusMove != null){
					mFocusMove.requestFocus();
				}

				//}
			}
			/*else //QR CODE
			{
				int nSymbol = intent.getExtras().getInt("symbology", -1);
				int nValue = intent.getExtras().getInt("value", -1);

				Log.i(TAG,"getSymbology ["+ nSymbol + "][" + nValue + "]");	
				
				if(nSymbol != -1)
				{
					edSymNum.setText(Integer.toString(nSymbol));						
					edValNum.setText(Integer.toString(nValue));						
				}
			}	*/
		}
		
		
		
	}

}
