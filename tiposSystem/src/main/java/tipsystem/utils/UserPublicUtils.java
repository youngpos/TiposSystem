package tipsystem.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;

import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import kr.co.tipos.tips.R;

public class UserPublicUtils {
	
	Context mContext;

	public UserPublicUtils(Context ctx)	{
		this.mContext = ctx;
	}
		
	//키보드 없애기
	public void hideSoftKeyboard(boolean onoff) {
	    InputMethodManager imm = (InputMethodManager)mContext.getSystemService(mContext.getApplicationContext().INPUT_METHOD_SERVICE);
	    if(onoff){
	    	//imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 1);	    	
	    	imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
	    }else{
	    	//imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);	    	
	    	imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);	    
	    }	    
	}  
	
	
	//포커스 이동시 백그라운드 색 변경 하기
	/**
	 * 텍스트 에디터의 색상을 변경 합니다.
	 * @param gubun 0 흰색 배경색, 1 노란색 배경색
	 * @param et EditText
	 */
	public void setChangeBackGroundColor(int gubun, EditText et){
				
		switch(gubun){
		case 0:
			//et.setBackgroundColor(Color.WHITE);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				et.setBackground(ContextCompat.getDrawable(mContext, R.drawable.edit_border));
			}
			break;
		case 1:
			//et.setBackgroundColor(Color.YELLOW);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				et.setBackground(ContextCompat.getDrawable(mContext,R.drawable.edit_border_focus));
			}
			break;
		}				
	}
	
	/**
	 * 오늘 날자를 출력합니다.
	 * @return yyyy-MM-dd 으로 반환합니다.
	 */
	public String getTodayData(){
		String today = "";
		
		SimpleDateFormat formatter = new SimpleDateFormat ( "yyyy-MM-dd", Locale.KOREA );
		Date currentTime = new Date();
		today = formatter.format ( currentTime );
		System.out.println ( today );
		
		return today;
	}

	/**
	 * 요번달을 출력합니다.
	 * @return yyyyMM 으로 반환합니다.
	 */
	public String getTodayDataMonth(){
		String today = "";

		SimpleDateFormat formatter = new SimpleDateFormat ( "yyyyMM", Locale.KOREA );
		Date currentTime = new Date();
		today = formatter.format ( currentTime );
		System.out.println ( today );

		return today;
	}

	
	/**
	 * 전표번호 끝자리 변환 표기
	 * @param num 변환할 숫자값 001
	 * @return 001 002 변환
	 */
	public String getJunPyoNumStyle(String num){
				
		int a = Integer.parseInt(num);		
		String jp_num =  String.format("%03d", a+1);
		
		return jp_num; 
	}
	
	/**
	 * 행사 시작일과 행사 종료일을 넣으면 오늘날자로 비교합니다.
	 * @param sDate 행사 시작일 'yyyy-MM-dd'
	 * @param eDate 행사 종료일 'yyyy-MM-dd'
	 * @return 행사중 true
	 */
	public boolean DateCompare(String sDate, String eDate){
		
		boolean sale = false;
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		
		//행사일
		Date day1 = null;
		Date day2 = null;
		Date day3 = null;
				
		try{			
			day1 = format.parse(sDate);
			day2 = format.parse(eDate);
			day3 = format.parse(getTodayData());
		}catch(ParseException e){
			e.printStackTrace();
		}
				
		//날자를 비교합니다.
		int compare1 = day1.compareTo(day3);
		int compare2 = day2.compareTo(day3);
		//행사시작일 보다 크고 종료일보다 작을 때
		if( compare1 <= 0 && compare2 >= 0){
			sale = true;
		}
	
		return sale;
		
	}

	//알림창을 띄웁니다.
	public void showDialog(String str, int pos){
		Toast.makeText(mContext, str, pos).show();
	}

	//확인 창을 띄웁니다.
	public void alert(final String str,final DialogInterface.OnClickListener okListener) {

		//----------------------------------------//
		// 2022.07.28. 팝업창 수량 잘 안보임 테마 수정
		//----------------------------------------//
		//AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
		AlertDialog.Builder alert = new AlertDialog.Builder(mContext,AlertDialog.THEME_HOLO_LIGHT);
		//----------------------------------------//
		alert.setPositiveButton("확인", okListener);
		alert.setCancelable(false);
		alert.setMessage(str);
		alert.show();
	}

	public void sendSMS(String content, String phone){
		try {
			SmsManager mSmsManager = SmsManager.getDefault();
			mSmsManager.sendTextMessage(phone, null, content, null, null);
		}catch(SecurityException e){
			new UserPublicUtils(mContext).showDialog("앱관리에서 SMS권한설정을 수락해주세요~!!", 0);
		}
	}
}
