package tipsystem.tips;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

import kr.co.tipos.tips.BuildConfig;
import kr.co.tipos.tips.R;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.Reachability;

//버젼체크로 인해서 추가함

public class TIPSSplashActivity extends Activity {
	String rtn, verSion;	
	//AlertDialog.Builder alt_bld;
	
	Context m_ctx;
	public final int MY_PERMISSIONS_REQUEST_RESULT = 0;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        checkNetwork();
        
        m_ctx = this;
        
		//new Version().execute();		
		
		//개인 전용 프로그램 
		//2015.11.12 위에 새 버전 체크 후  폰넘버 저장함수로 이동합니다. 그래서 필요없습니다.
        //savePhoneNumber(m_ctx);
        //startMainActivity();
        //버전저장하기
        //2016년1월 버전 업데이트 처리 변경
		//Log.i("현재버전 정보", "1.8.4");
		//LocalStorage.setString(getApplicationContext(), "nowVersion", "1.8.4");
        versionCheck();

        //추가 권한 설정을 합니다.
        getPermissionSetting();


        
		//testQuery();
    }

    //권한 설정을 합니다.
	private void getPermissionSetting() {

    	//<uses-permission android:name="android.permission.WRITE_SETTINGS" /> 와 관련된 내용
    	//버전에 따라서 권한부여를 확인합니다.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			/*
			if (Settings.System.canWrite(this)) {
				// Toast.makeText(this, "onCreate: Already Granted", Toast.LENGTH_SHORT).show();
				if (savePhoneNumber(m_ctx)) startMainActivity();
			} else {
				//권한이 없다면 권한을 호출합니다.
				Toast.makeText(this, "onCreate: Not Granted. Permission Requested", Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS);
				intent.setData(Uri.parse("package:" + this.getPackageName()));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			}
			*/

			if(checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
					checkSelfPermission(Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED ||
					checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
					checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
					//checkSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED ||
					//checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED
			){
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE,
						Manifest.permission.READ_PHONE_NUMBERS,
						Manifest.permission.CAMERA,
						Manifest.permission.WRITE_EXTERNAL_STORAGE
						//Manifest.permission.SEND_SMS,
						//Manifest.permission.CALL_PHONE
						}, MY_PERMISSIONS_REQUEST_RESULT);
			}else{
				if (savePhoneNumber(m_ctx)) startMainActivity();
			}
		}else{
			if (savePhoneNumber(m_ctx)) startMainActivity();
		}
	}


	private void checkNetwork() {
    	if (Reachability.isOnline(this) == false) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_HOLO_LIGHT);
            builder.setTitle("알림");
            builder.setMessage("네트워크에 연결되어 있지 않습니다! 네트워크 연결을 확인후 다시 실행시켜 주세요.");
            builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                	finish();
                }
            });
            builder.show();
            return;
		}
    }
    
    private void checkUpdate() {
    	
    	AlertDialog.Builder alt_bld = new AlertDialog.Builder(m_ctx,AlertDialog.THEME_HOLO_LIGHT);
		alt_bld.setMessage(" 신규 버전이 있습니다. \r\n 업데이트 후 사용해주세요.")
				.setCancelable(false)
				.setNegativeButton("업데이트 하기",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								Intent marketLaunch = new Intent(
										Intent.ACTION_VIEW);
								marketLaunch.setData(Uri
										.parse("https://play.google.com/store/apps/details?id=kr.co.tipos.tips"));
								startActivity(marketLaunch);
								finish();
							}
						})
				.setPositiveButton("나중에 하기", new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int id) {								
								LocalStorage.setString(getApplicationContext(), "nowVersion", verSion);
								if (savePhoneNumber(m_ctx)) startMainActivity();		
							}
						});				
		
		AlertDialog alert = alt_bld.create();
		alert.setTitle("안 내");
		alert.show();    	
    }


	private boolean savePhoneNumber(Context ctx){

		// TODO : 정식 버젼 오픈시 주석 삭제후 발행
		TelephonyManager phoneManager = (TelephonyManager)getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);

		String phoneNumber = phoneManager.getLine1Number();

		String aaa = BuildConfig.BUILD_TYPE;

//		if(aaa=="debug") {
//			Toast.makeText(TIPSSplashActivity.this, "DEBUG 모드입니다.", Toast.LENGTH_SHORT).show();
//		}else{
//			Toast.makeText(TIPSSplashActivity.this, "RELEASE 모드입니다.", Toast.LENGTH_SHORT).show();
//		}

    	if (phoneNumber == null || phoneNumber.equals("")) {
			if(aaa=="debug"){
				Toast.makeText(TIPSSplashActivity.this, "DEBUG 모드입니다.", Toast.LENGTH_SHORT).show();
				phoneNumber="01090424515";
			}else{
				AlertDialog.Builder builder = new AlertDialog.Builder(ctx,AlertDialog.THEME_HOLO_LIGHT);
				builder.setTitle("알림");
				builder.setMessage("기기에 등록된 전화번호가 없습니다. 어플이용이 불가능합니다!");
				builder.setNeutralButton("확인", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						finish();
					}
				});
				builder.show();
				return false;
			}
        }
    	
    	String prefix = phoneNumber.substring(0, 3);
    	if (prefix.equals("+82")) phoneNumber = "0" + phoneNumber.substring(3, phoneNumber.length());
    	
    	LocalStorage.setString(ctx, "phoneNumber", phoneNumber);
    			
		//테스트 연결하기 배포버젼에선 이부분 주석처리 하고 윗부분 주석 해제 해야함
    	//LocalStorage.setString(ctx, "phoneNumber", "01090077611");    	
    	return true;
    }
	
	private void startMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

	private void versionCheck(){
				
		// Version check the execution application.
		PackageInfo pi = null;
		try {
			pi = getPackageManager().getPackageInfo(getPackageName(), 0);
			//String sMyVersion = getResources().getString(R.string.check_version.);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		verSion = pi.versionName;
		//Log.i("verSion-1", pi.versionName);
		
		//버전저장하기
		Log.i("현재버전 정보", verSion.toString());
		LocalStorage.setString(getApplicationContext(), "nowVersion", verSion);
		
	}


	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_RESULT:
				// If request is cancelled, the result arrays are empty.
			{
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay! Do the
					// contacts-related task you need to do.
					if (savePhoneNumber(m_ctx)) startMainActivity();
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(this, "권한을 수락하지 않아 앱을 종료합니다.\r\n설정>일반>앱정보 에서 권한을 수락해 주세요!", Toast.LENGTH_SHORT).show();
					finish();
				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
	}

	
	//업데이트후 처리 하기
	private class Version extends AsyncTask<Void, Void, String> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(Void... params) {
			Log.w("Version", "시작합니다. "); //버젼 표시 하기
			// Confirmation of market information in the Google Play Store
			try {
				Document doc = Jsoup
						.connect(
								//"https://play.google.com/store/apps/details?id=kr.co.tipos.tips").timeout(5000)
								"https://play.google.com/store/apps/details?id=kr.co.tipos.tips").userAgent(
								"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/535.21 (KHTML, like Gecko) Chrome/19.0.1042.0 Safari/535.21").timeout(5000)
						.get();
				Elements Version = doc.select(".content");

				for (Element v : Version) {
					if (v.attr("itemprop").equals("softwareVersion")) {
						rtn = v.text();
						Log.w("Version", rtn); //버젼 표시 하기
					}					
				}
				return rtn;
				//return "";
			
			}catch(HttpStatusException e){
				e.printStackTrace();
				
			}catch(IOException e){
				e.printStackTrace();
				
			}
			Log.w("Version", "버전 찾기 종료. "); //버젼 표시 하기
			return "";
		}

		@Override
		protected void onPostExecute(String result) {				
						
			Log.i("onPosteExecute 버전정보 표시", result);
			// Version check the execution application.
			PackageInfo pi = null;
			try {
				pi = getPackageManager().getPackageInfo(getPackageName(), 0);
				//String sMyVersion = getResources().getString(R.string.check_version.);
			} catch (NameNotFoundException e) {
				e.printStackTrace();
			}
			verSion = pi.versionName;
			Log.i("verSion-1", pi.versionName);	
			
			if(!result.equals("")){
			rtn = result;

			if (!verSion.equals(rtn)) {
				//버전체크 후 
				checkUpdate();
			}else{
				//버전저장하기
				Log.i("현재버전 정보", rtn.toString());
				LocalStorage.setString(getApplicationContext(), "nowVersion", rtn);
				if (savePhoneNumber(m_ctx)) startMainActivity();
			}
			super.onPostExecute(result);
			}else{	
				
			//버전저장하기
			Log.i("현재버전 정보", verSion.toString());
			LocalStorage.setString(getApplicationContext(), "nowVersion", verSion);
			if (savePhoneNumber(m_ctx)) startMainActivity();					
			Toast.makeText(getApplicationContext(), " 버전 정보를 찾지 못했습니다. ", Toast.LENGTH_LONG).show();
			}
		}
	}
}