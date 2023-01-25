package tipsystem.tips;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.BuildConfig;
import kr.co.tipos.tips.R;
import tipsystem.utils.FTPUpload;
import tipsystem.utils.JsonHelper;
import tipsystem.utils.LocalStorage;
import tipsystem.utils.M3MoblieBarcodeScanBroadcast;
import tipsystem.utils.MSSQL;
import tipsystem.utils.TIPOS;

public class ManageImageFtpActivity_local extends Activity implements OnClickListener {

    String TAG = "이미지 업로드 파일 ";

    // 2017-04 m3mobile
    private static final String SCANNER_ACTION_SETTING_CHANGE = "com.android.server.scannerservice.settingchange";
    public static final String SCANNER_ACTION_PARAMETER = "android.intent.action.SCANNER_PARAMETER";
    public static final String SCANNER_ACTION_ENABLE = "com.android.server.scannerservice.m3onoff";
    public static final String SCANNER_EXTRA_ENABLE = "scanneronoff";

    public static final String SCANNER_ACTION_START = "android.intent.action.M3SCANNER_BUTTON_DOWN";
    public static final String SCANNER_ACTION_CANCEL = "android.intent.action.M3SCANNER_BUTTON_UP";

    public static final String SCANNER_ACTION_BARCODE = "com.android.server.scannerservice.broadcast";


    Context context;

    //WebView webview;
    EditText text_barcode;

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

    // 2022.05.26.본사서버 IP변경
    //String m_imgip = "14.38.161.45";
    //String m_imgport = "18975";
    //String m_dbname = "ShoppingMall";
    //String m_dbpw = "tips";

    String m_OfficeCode = "";
    String img_path = "";

    //String img_path_local = "http://14.38.161.45:7080/";
    String img_path_local = "";

    Uri mImageCaptureUri;
    Uri photoURI;

    /* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
    int m_KeypadType = 0;

    // loading bar
    private ProgressDialog dialog;


    //픽셀을 dp로 변환해 줍니다.
    float mTargetW; // WebView 의 가로 사이즈 구하기
    float mTargetH; // WebView 의 세로 사이즈 구하기

    //다시촬영버튼생성
    Button btn_rePick;
    Button btn_cameraOn;
    Button btn_imageUpload;

    Button btn_setReNew;
    Button btn_serachFolder;

    EditText text_gname;

    String mCurrentPhotoPath = "";

    private static final int ZBAR_SCANNER_REQUEST = 0;
    private static final int PICK_CAMERA_REQUEST = 1;
    private static final int PICK_CROP_REQUEST = 2;

    ImageView mImageView;
    Bitmap mImageBitmap;

    //수정작업중 표시 // 온라인 등록 모드인지 확인
    Boolean editTrue = false;
    Boolean onlineTrue = false;
    String tempBarcode = "";


    SharedPreferences pref;
    boolean m3Mobile;


    //상품정보 수정을 위해서 상품 정보를 담아 두고 있습니다.
    HashMap<String, String> tempGoods = new HashMap<String, String>();
    HashMap<String, String> tempImage = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_ftp_local);

        context = this;

        m_shop = LocalStorage.getJSONObject(this, "currentShopData");
        m_userProfile = LocalStorage.getJSONObject(this, "userProfile");

        try {

            m_OfficeCode = m_shop.getString("OFFICE_CODE");

            m_ip = m_shop.getString("SHOP_IP");
            m_port = m_shop.getString("SHOP_PORT");
            //----------------------------------------//
            // 2021.12.21. 매장DB IP,PW,DB 추가
            //----------------------------------------//
            m_uuid = m_shop.getString("shop_uuid");
            m_uupw = m_shop.getString("shop_uupass");
            m_uudb = m_shop.getString("shop_uudb");
            //----------------------------------------//

            //----------------------------------------//
            // 2022.05.26. 변경 추가
            //----------------------------------------//
            img_path_local = "http://"+TIPOS.HTTP_IP+":"+TIPOS.HTTP_PORT_S+"/";
            //----------------------------------------//

            /* 김영목 2020.09.14. 바코드 입력 키패드 설정 옵션 추가 */
            m_KeypadType = LocalStorage.getInt(this, "KeypadType:" + m_OfficeCode);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // 환경설정 관련 옵션 아이템 불러오기
        pref = PreferenceManager.getDefaultSharedPreferences(this);
        m3Mobile = pref.getBoolean("m3mobile", false);

        //mImageView = new ImageView(getApplicationContext());
        mImageView = (ImageView) findViewById(R.id.viewImage);

        //dp 를 px로 변환 합니다. 
        mTargetW = dpToPixel(250f);
        mTargetH = dpToPixel(250f);

        //진입부
        init();

        // intent filter 2017-04 m3mobile
        if (m3Mobile) {
			/*IntentFilter filter = new IntentFilter();
			filter.addAction(SCANNER_ACTION_BARCODE);
			registerReceiver(BarcodeIntentBroadcast, filter);*/

            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(context, text_barcode, text_gname);
        }


    }

    //진입부
    public void init() {

        //바코드 입력
        text_barcode = (EditText) findViewById(R.id.editBarcode);

        // 김영목 2020.09.14. 키패드입력 옵션 설정에 따라 적용 추가
        if (m_KeypadType == 0) { // 숫자키패드
            text_barcode.setInputType(2);
        }
        if (m_KeypadType == 1) { // 문자키패드
            text_barcode.setInputType(145); //textVisiblePassword ) 0x00000091
        }

		/*text_barcode.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				
				if(event.getAction() == KeyEvent.ACTION_UP){					
					if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
						if(text_barcode.getText().length() >= 4){
							getImageUrl(new View(ManageImageFtpActivity_local.this));
						}
					}
				}
				return false;
			}
		});*/


        text_barcode.setOnFocusChangeListener(new OnFocusChangeListener() {

            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                // TODO Auto-generated method stub
                if (hasFocus) {
                    text_barcode.selectAll();
                } else {
                    if (text_barcode.getText().length() >= 4) {
                        getImageUrl(new View(ManageImageFtpActivity_local.this));
                    }
                }
            }
        });

        //임시 출력
		/*webview = (WebView)findViewById(R.id.webviewImage);
		webview.setVerticalScrollBarEnabled(false);
		webview.setVerticalScrollbarOverlay(false);
		webview.setHorizontalScrollBarEnabled(false);
		webview.setHorizontalScrollbarOverlay(false);
		webview.setInitialScale(100);*/
		
		
		/*webview.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				//웹뷰에 테그 설정 imageView --- 온라인에서 이미지를 불러왔으면 웹뷰 터치를 막아주세요!!
				if(v.getTag().equals("imageView") && event.getAction() == MotionEvent.ACTION_DOWN && !onlineTrue){
					
					//선택된 이미지를 조정합니다.
					setImageCrop();
			        return true;
				}
				
				return false;
			}
		});
		webview.setTag("imageView");*/

        text_gname = (EditText) findViewById(R.id.text_gname);

        //다시 촬영 버튼
        btn_rePick = (Button) findViewById(R.id.btnRePick);
        btn_rePick.setOnClickListener(this);
        btn_cameraOn = (Button) findViewById(R.id.btnCameraOn);
        btn_cameraOn.setOnClickListener(this);
        btn_imageUpload = (Button) findViewById(R.id.btnImageUpload);
        btn_imageUpload.setOnClickListener(this);

        btn_setReNew = (Button) findViewById(R.id.btnRenew);
        btn_setReNew.setOnClickListener(this);
        btn_serachFolder = (Button) findViewById(R.id.btnFolderSearch);
        btn_serachFolder.setOnClickListener(this);

    }

    //바코드를 검색해서 이미지를 불러옵니다.
    public void getImageUrl(View view) {

        //검색 조건을 받습니다.
        String barcode = text_barcode.getText().toString();

        //임시 바코드를 지정 합니다.
        tempBarcode = barcode.toString();

        if ("".equals(barcode)) {
            startCameraSearch();
            return;
        }

        //이미지 기록 초기화 합니다.
        img_path = "";

        // 로딩 다이알로그
    	/*dialog = new ProgressDialog(this);
 		dialog.setMessage("Loading....");
 		dialog.setCancelable(false);
 		dialog.show();*/

        // 쿼리 작성하기
        String query = "Select * From Goods Where Barcode='" + barcode + "' ";

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {

				/*dialog.dismiss();
				dialog.cancel();*/

                if (results.length() > 0) {

                    // 바코드를 검색 해서 값을 불러 옵니다.
                    //for(int i = 0; i < results.length(); i++){
                    try {

                        JSONObject temp = results.getJSONObject(0);
                        tempGoods = JsonHelper.toStringHashMap(temp);

                        //상품명을 자동으로 입력합니다.
                        try {
                            System.out.println(tempGoods.get("G_Name").toString());
                            text_gname.setText(tempGoods.get("G_Name").toString());
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            text_gname.setText("");
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                        text_gname.setText("");
                    }

                    //이미지정보를 검색하고 이미지를 표현합니다.
                    getImageView();

                } else {
                    text_gname.setText("");
                    Toast.makeText(ManageImageFtpActivity_local.this, "등록된 상품이 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }).execute(m_ip + ":" + m_port, m_uudb, m_uuid, m_uupw, query);


    }


    //바코드를 검색해서 이미지를 불러옵니다.
    public void getImageView() {

        //검색 조건을 받습니다.
        String barcode = text_barcode.getText().toString();

        //이미지 기록 초기화 합니다.
        img_path = "";

        // 로딩 다이알로그
		/*dialog = new ProgressDialog(this);
		dialog.setMessage("Loading....");
		dialog.setCancelable(false);
		dialog.show();*/

        // 쿼리 작성하기
        String query = "Select * From Ftp_Image Where Barcode='" + barcode + "' and path_gubun='0' and path='" + m_OfficeCode + "' ";

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {

				/*dialog.dismiss();
				dialog.cancel();*/

                if (results.length() > 0) {

                    // 바코드를 검색 해서 값을 불러 옵니다.
                    //for(int i = 0; i < results.length(); i++){
                    try {

                        JSONObject temp = results.getJSONObject(0);
                        tempImage = JsonHelper.toStringHashMap(temp);

                        // 결과값을 데이터베이스에 추가할수있도록 쿼리를 구성한다.
                        try {

                            String officecode = tempImage.get("Path").toString();
                            String file_per = tempImage.get("Barcode").toString();
                            String file_ext = tempImage.get("Ext").toString();

                            img_path = img_path_local + officecode + "/" + file_per + "." + file_ext;

                            onlineTrue = true;

                        } catch (NullPointerException e) {
                            img_path = "";
                            //상품을 검색시 상품 경로가 있는지 없는지를 확인 합니다.
                            onlineTrue = false;
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {

                    img_path = "";
                    //상품을 검색시 상품 경로가 있는지 없는지를 확인 합니다.
                    onlineTrue = false;
                }

                //이미지를 보여줍니다.
                setImageView();

            }
            // 2022.05.26.본사서버 IP변경
            //}).execute(m_imgip + ":" + m_imgport, m_dbname, "sa", m_dbpw, query);
        }).execute(TIPOS.SHOPPING_MALL_IP + ":" + TIPOS.SHOPPING_MALL_PORT, TIPOS.SHOPPING_MALL_DB, TIPOS.SHOPPING_MALL_ID, TIPOS.SHOPPING_MALL_PW, query);

    }


    //이미지 불러오기
    public void setImageView() {

        webViewClear();

        //입력된 이미지를 서버에서 검색 합니다.
        //바코드 불러오기
        //검색된 이미지의 주소를 불러옵니다.
        //이미지 주소를 생성 합니다.
        Log.d("검사시작후 이미지 패스 ->", img_path);
        //웹뷰에 뿌려 줍니다.
        if (img_path == null || "".equals(img_path) || img_path.endsWith("noimage.jpg")) {//이미지 파일이 없으면 앱내에서 지정된 이미지를 뿌려주고
			
		/*	webview.setBackgroundColor(0);
			webview.setBackgroundResource(R.drawable.noimage);
			*/
            //사진 불러왔는데 이미지가 없다면 사진촬영버튼을 활성화 합니다.
            btn_cameraOn.setEnabled(true);
            //이미지가 없다면 촬영을 합니다.
            dispatchTakePictureIntent();

        } else {// 이미지 파일이 있으면

            //webViewClear();
			/*StringBuffer sb = new StringBuffer("<HTML>");
			sb.append("<HEAD>");
			sb.append("</HEAD>");
			sb.append("<BODY  style='margin: 0; padding: 0'>");
			sb.append("<img width=\""+mTargetW+"\" height=\""+mTargetH+"\" src=\"" + img_path+"\">");
			sb.append("</BODY>");
			sb.append("</HTML>");*/

            //이미지는 있고 단독 모드라면 재촬영후 업로드 할수있습니다.
            //pathGubun - 0 : 단독 폴더(체크) / 1 : 공용폴더 (체크안됨) 재촬영
            btn_rePick.setEnabled(true);

            //webview.loadDataWithBaseURL(null,   sb.toString(), "text/html", "utf-8", null);
			/*try{
				Log.d("이미지 패스 ->", img_path);
				URL url = new URL(img_path);
				URLConnection conn = url.openConnection();
				conn.connect();
				BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
				Bitmap bm = BitmapFactory.decodeStream(bis);
				bis.close();
				
				mImageView.setImageBitmap(bm);;
			}catch(Exception e){
				Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}*/

            new LoadImagefromUrl().execute(mImageView, img_path);

        }
    }

    //새로입력
    public void setReNew() {

        text_barcode.setText("");

        //하단버튼 초기화
        btn_cameraOn.setEnabled(false);
        btn_imageUpload.setEnabled(false);
        btn_rePick.setEnabled(false);

        text_gname.setText("");
        text_barcode.requestFocus();

        webViewClear();

        mCurrentPhotoPath = "";
        mImageCaptureUri = null;

        editTrue = false;
    }


    private void webViewClear() {
				
		/*webview.clearView();
		webview.clearCache(true);
		webview.clearHistory();
		
		webview.setBackgroundResource(R.drawable.noimage);
		webview.removeAllViews();*/
        mImageView.setImageResource(0);
        mImageView.setImageResource(android.R.color.transparent);
    }

    //사진 찍기 시작 입니다.
    //Intent 에 사진이 저장될 File 객체를 넘겨주면 카메라 앱으로 촬영된 사진은 해당 File 에 저장된다.
    public void dispatchTakePictureIntent() {

        //입력 상품이 있는지 확인 한다.
        if (text_barcode.getText().length() < 4) {
            Toast.makeText(this, "3자리 이하 바코드상품은 사진을 등록할수 없습니다! ", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra("PreviewDisplay", false);
        if (isIntentAvailable(getApplicationContext(), MediaStore.ACTION_IMAGE_CAPTURE)) {
            try {

                File f = newCreateImageFile(text_barcode.getText().toString());
                mCurrentPhotoPath = f.getAbsolutePath();

                mImageCaptureUri = FileProvider.getUriForFile(ManageImageFtpActivity_local.this,
                        BuildConfig.APPLICATION_ID + ".provider", f);
                //mImageCaptureUri = Uri.fromFile(f);
                // 이미지가 저장될 파일은 카메라 앱이 구동되기 전에 세팅해서 넘겨준다.

                photoURI = FileProvider.getUriForFile(ManageImageFtpActivity_local.this,
                        BuildConfig.APPLICATION_ID + ".provider", f);

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                try {
                    startActivityForResult(takePictureIntent, PICK_CAMERA_REQUEST);
                } catch (ActivityNotFoundException e) {
                    // Do nothing for now
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    //카메라 앱으로 활용할 수 있는 앱이 있는지 체크하는 방법
    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }


    private File newCreateImageFile(String filename) throws IOException {

        //경로를 지정합니다. 없다면 새로 생성합니다.(외장경로에 파일을 저장한다.)
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dirPath = new File(path.toString() + "/" + m_OfficeCode.toString());
        Log.d("매장 코드 -->> ", dirPath.getAbsolutePath());
        if (!dirPath.exists()) {
            dirPath.mkdirs();
            Log.w("폴더생성", "폴더생성완료");
        }
        File image = new File(dirPath, filename + ".jpg");
        if (!image.exists()) {
            image.createNewFile();
            Log.w("파일생성", "파일생성완료");
        }

        return image;
    }

    //사진 이미지 사이즈 조절하기
    private void setPic() {

        webViewClear();

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath);//), bmOptions);
        storeCropImage(bitmap, mCurrentPhotoPath);

        mImageView.setImageBitmap(bitmap);

    }

    //화면 사이즈 구하기
    public float dpToPixel(float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
        return px;
    }

    //이미지 저장하기
    public void storeCropImage(Bitmap bitmap, String filePath) {

        File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try {

            int height = bitmap.getHeight();
            int width = bitmap.getWidth();
            Log.w(TAG, "원본 높이" + height);
            Log.w(TAG, "원본 너이" + width);

            double scaleFactor = Math.max(Math.round((width / 500.0d) * 100d) / 100d, Math.round((height / 500.0d) * 100d) / 100d);
            Log.w(TAG, "축소 배율" + scaleFactor);

            //너비가 길이보다 길면 너비를 축소 합니다.
            int r_height = (int) Math.round(height / scaleFactor);
            int r_width = (int) Math.round(width / scaleFactor);

            Log.w(TAG, "축소할 높이" + r_height);
            Log.w(TAG, "축소할 너비" + r_width);

            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));
            //bitmap = Bitmap.createScaledBitmap(bitmap, 500, height/(width/500), true);
            bitmap = Bitmap.createScaledBitmap(bitmap, r_width, r_height, true);

            Bitmap bmOverlay = Bitmap.createBitmap(500, 500, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmOverlay);
            canvas.drawColor(0xffffffff);

            //너비 높이를 계산해서 겹치는 이미지를 중앙에 배치한다
            int center_width, center_height;

            try {
                center_width = (500 - bitmap.getWidth()) / 2;
            } catch (ArithmeticException e) {
                center_width = 0;
            }

            try {
                center_height = (500 - bitmap.getHeight()) / 2;
            } catch (ArithmeticException e) {
                center_height = 0;
            }

            canvas.drawBitmap(bitmap, center_width, center_height, null);
            //canvas.drawBitmap(bitmap, new Matrix(), null);
            //canvas.drawBitmap(bmp2, 0, 0, null);

            //bitmap.compress(CompressFormat.JPEG, 100, out);
            bmOverlay.compress(CompressFormat.JPEG, 100, out);

            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //카메라 앱을 통해 찍힌 사진 보여주기
    private void handleSmallCameraPhoto() {

        setPic();
        //webview.addView(mImageView);

        //사진을 재촬영할수 있게 합니다.
        btn_rePick.setEnabled(true);
        btn_cameraOn.setEnabled(false);
        //btn_imageUpload.setEnabled(true);

    }

    //사진 다시찍기
    //파일 삭제하고 카메라 재시작합니다.
    public void tempFileDeleteReStart() {

        //다시 찍기 일때 시작이 온라인 모드라면 임시파일을 생성해 주고
        //다시 찍을수 있게 합니다.
        if (!onlineTrue) {
            File delFile = new File(mCurrentPhotoPath);

            if (!delFile.delete()) {
                Toast.makeText(this, "파일을 삭제 하지 못했습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            ;
        }
				
		/*btn_rePick.setEnabled(false);
		//btn_cameraOn.setEnabled(true);
		webview.removeAllViews();*/
        dispatchTakePictureIntent();

    }

    //이미지 목록 보여주기
    public void imageListView() {

        //경로를 지정합니다. 없다면 새로 생성합니다.(외장경로에 파일을 저장한다.)
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dirPath = new File(path.toString() + "/" + m_OfficeCode.toString());
        Log.d("매장 코드 -->> ", dirPath.getAbsolutePath());
        if (!dirPath.exists()) {
            dirPath.mkdirs();
            Log.w("폴더생성", "폴더생성완료");
        }

        Intent intent = new Intent(this, ManageImageListView.class);
        intent.putExtra("FILEPATH", dirPath.getPath());
        startActivity(intent);
    }

    //이미지 잘라내기
    public void setImageCrop1() {

        if (onlineTrue) {
            return;
        }
        //현재 선택된 이미지를 불러옵니다.
        File temp = new File(mCurrentPhotoPath);

        //경로를 지정합니다. 없다면 새로 생성합니다.(외장경로에 파일을 저장한다.)
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dirPath = new File(path.toString() + "/" + m_OfficeCode.toString(), text_barcode.getText() + "_temp.jpg");

        //현재 이미지의 이름을 변경합니다.
        temp.renameTo(dirPath);
        Log.w("임시 파일명 --> ", temp.getName());

        this.grantUriPermission("com.android.camera", photoURI,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent intent = new Intent("com.android.camera.action.CROP");
        photoURI = FileProvider.getUriForFile(ManageImageFtpActivity_local.this,
                BuildConfig.APPLICATION_ID + ".provider",
                dirPath);

        //intent.setDataAndType(Uri.fromFile(dirPath), "image/*");
        intent.setDataAndType(photoURI, "image/*");

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        //intent.putExtra("outputX", 500);
        //intent.putExtra("outputY", 500);
        //intent.putExtra("aspectX", 1);
        //intent.putExtra("aspectY", 0.5);        
        intent.putExtra("scale", false);

        this.grantUriPermission("com.android.camera.action.CROP", mImageCaptureUri,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        //intent.putExtra("return-data", true);
        //intent.putExtra("output", Uri.fromFile(temp));

        intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        //intent.putExtra("output", mImageCaptureUri);
        startActivityForResult(intent, 2);

    }

    public void setImageCrop() {
        if (onlineTrue) {
            return;
        }
        //현재 선택된 이미지를 불러옵니다.
        File temp = new File(mCurrentPhotoPath);

        //경로를 지정합니다. 없다면 새로 생성합니다.(외장경로에 파일을 저장한다.)
        String path = Environment.getExternalStorageDirectory().getAbsolutePath();
        File dirPath = new File(path.toString() + "/" + m_OfficeCode.toString(), text_barcode.getText() + "_temp.jpg");

        //현재 이미지의 이름을 변경합니다.
        temp.renameTo(dirPath);
        Log.w("임시 파일명 --> ", temp.getName());

        this.grantUriPermission("com.android.camera", photoURI,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent intent = new Intent("com.android.camera.action.CROP");
        photoURI = FileProvider.getUriForFile(ManageImageFtpActivity_local.this,
                BuildConfig.APPLICATION_ID + ".provider",
                dirPath);

        //intent.setDataAndType(Uri.fromFile(dirPath), "image/*");
        intent.setDataAndType(photoURI, "image/*");


        List<ResolveInfo> list = getPackageManager().queryIntentActivities(intent, 0);
        grantUriPermission(list.get(0).activityInfo.packageName, photoURI,
                Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        int size = list.size();
        if (size == 0) {
            Toast.makeText(this, "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            return;
        } else {
            Toast.makeText(this, "용량이 큰 사진의 경우 시간이 오래 걸릴 수 있습니다.", Toast.LENGTH_SHORT).show();
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            intent.putExtra("crop", "true");
            //intent.putExtra("aspectX", 4);
            //intent.putExtra("aspectY", 3);
            intent.putExtra("scale", false);


            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);


            intent.putExtra("return-data", false);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString()); //Bitmap 형태로 받기 위해 해당 작업 진행

            Intent i = new Intent(intent);
            ResolveInfo res = list.get(0);
            i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            grantUriPermission(res.activityInfo.packageName, mImageCaptureUri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);

            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            startActivityForResult(i, 2);
        }
    }


    //찍은 이미지를 적당한 사이즈로 업로드를 합니다. FTp서버및 sql 쿼리로 업로드 실시
    public void pickImageUpload() {

        //찍은 사진이 있는지 먼저 확인 합니다.
        //사진이 없다면 리턴
        //찍은 사진의 값을 가져 옵니다.
        //먼저 Ftp서버로 업로드 합니다.
        //FTPUpload ftpUpload = new FTPUpload();
        new FTPUpload().execute(m_OfficeCode, mCurrentPhotoPath, "upload");

        //ftp서버에 인서트 합니다.
        //바코드, 패스, 구분, 상품명, 확장자
        String ftp_query = "Insert into FTP_Image values('" + tempBarcode + "', '" + m_OfficeCode + "', '0', '" + text_gname.getText().toString() + "', 'jpg', '0')";

        // 콜백함수와 함께 실행
        new MSSQL(new MSSQL.MSSQLCallbackInterface() {

            @Override
            public void onRequestCompleted(JSONArray results) {
                //중복 검사후 최종 이미지가 저장됩니다.
                String queryOver = "Delete From Ftp_Image Where idx in( select a.idx as idx "
                        + "From Ftp_Image A INNER JOIN ( Select Max(idx) as idx, barcode, count(*) as overlapCount "
                        + "From Ftp_Image Where path_gubun='0' and path='" + m_OfficeCode + "' Group by barcode Having count(*) > 1 ) B "
                        + "on a.barcode = b.barcode and A.idx <> B.idx and a.path_gubun='0' and path='" + m_OfficeCode + "')";

                // 콜백함수와 함께 실행
                new MSSQL(new MSSQL.MSSQLCallbackInterface() {

                    @Override
                    public void onRequestCompleted(JSONArray results) {
                        Log.w("이미지 업로드", "FTP서버에 저장완료");
                    }
                    // 2022.05.26.본사서버 IP변경
                    //}).execute("14.38.161.45" + ":" + "18975", "ShoppingMall", "sa", "tips", queryOver);
                }).execute(TIPOS.SHOPPING_MALL_IP + ":" + TIPOS.SHOPPING_MALL_PORT, TIPOS.SHOPPING_MALL_DB, TIPOS.SHOPPING_MALL_ID, TIPOS.SHOPPING_MALL_PW, queryOver);
            }
            // 2022.05.26.본사서버 IP변경
            //}).execute("14.38.161.45" + ":" + "18975", "ShoppingMall", "sa", "tips", ftp_query);
        }).execute(TIPOS.SHOPPING_MALL_IP + ":" + TIPOS.SHOPPING_MALL_PORT, TIPOS.SHOPPING_MALL_DB, TIPOS.SHOPPING_MALL_ID, TIPOS.SHOPPING_MALL_PW, ftp_query);

        //모든 저장이 완료 되면 기존 파일을 삭제 합니다.
        //nowPickDelete();
        editTrue = false;
        setReNew();
        Toast.makeText(this, "저장이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
    }


    //선택한 정보를 수정합니다.
	/*private void selectGoodsInfoChange(String query){
		
		String shop_query = "Update Goods_Info set "+query+", edit_tran='1' where Barcode='"+tempBarcode+"' ";
		
		// 콜백함수와 함께 실행
	    new MSSQL(new MSSQL.MSSQLCallbackInterface() {

			@Override
			public void onRequestCompleted(JSONArray results) {
				Log.w("선택정보 수정", "매장 서버에 정보 수정완료");
			}
	    }).execute(m_ip + ":" + m_port, "tips", "sa", "tips", shop_query);
	    
	}*/

    //상품 카메라 검색
    private void startCameraSearch() {
        Intent intent = new Intent(this, ZBarScannerActivity.class);
        startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case ZBAR_SCANNER_REQUEST:

                if (resultCode == RESULT_OK) {
                    // Scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT)
                    // Type of the scan result is available by making a call to data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE)
                    Toast.makeText(this, "Scan Result = " + data.getStringExtra(ZBarConstants.SCAN_RESULT), Toast.LENGTH_SHORT).show();
                    //Toast.makeText(this, "Scan Result Type = " + data.getStringExtra(ZBarConstants.SCAN_RESULT_TYPE), Toast.LENGTH_SHORT).show();
                    // The value of type indicates one of the symbols listed in Advanced Options below.

                    String barcode = data.getStringExtra(ZBarConstants.SCAN_RESULT);
                    text_barcode.setText(barcode);

                    if (text_barcode.getText().length() >= 4) {
                        getImageUrl(new View(ManageImageFtpActivity_local.this));
                    }

                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Camera unavailable", Toast.LENGTH_SHORT).show();
                }

                break;
            case PICK_CAMERA_REQUEST:

                if (resultCode == RESULT_OK) {

                    //온라인 모드를 해제합니다.
                    onlineTrue = false;
                    Log.d("사진찌기 --> ", "사진찍기 성공");
                    //결과를 갤러리에 저장합니다.
                    //sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
                    setImageCrop();
                    //handleSmallCameraPhoto();
                }

                break;
            case PICK_CROP_REQUEST:

                if (resultCode == RESULT_OK) {
                    // 크롭이 된 이후의 이미지를 넘겨 받습니다.
                    //임시 파일을 삭제 합니다.
                    //경로를 지정합니다. 없다면 새로 생성합니다.(외장경로에 파일을 저장한다.)
                    String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                    File dirPath = new File(path.toString() + "/" + m_OfficeCode.toString(), text_barcode.getText() + "_temp.jpg");
                    dirPath.delete();

                    //크롭된 이미지를 다시 보여줍니다.
                    //setPic();

                    handleSmallCameraPhoto();

                    //이미지를 수정 완료 했습니다. 업로드가 가능합니다.
                    editTrue = true;
                    onlineTrue = false;
                    btn_imageUpload.setEnabled(true);
                } else if (resultCode == RESULT_CANCELED) {

                    //사진 사이즈 조정을 완료하지 못했습니다. 선택된 사진의 템프파일을 원상복구 합니다.
                    //임시파일을 불러옵니다.
				/*String path = Environment.getExternalStorageDirectory().getAbsolutePath();
				String dirPath = path.toString()+"/"+m_OfficeCode.toString()+"/"+text_barcode.getText()+"_temp.jpg";
				//현재 선택된 이미지를 불러옵니다.
			    File temp = new File(dirPath);
				
				//변경할 파일의 이름을 재 생성합니다.
				File reTemp = new File(mCurrentPhotoPath);
				   
			    //현재 이미지의 이름을 변경합니다.
			    temp.renameTo(reTemp);
			    Log.w("임시 파일명 --> ", temp.getName());*/
                }

                break;
        }

    }

    //이미지 URL로 불러오기
    private class LoadImagefromUrl extends AsyncTask<Object, Void, Bitmap> {
        ImageView ivPreview = null;

        @Override
        protected Bitmap doInBackground(Object... params) {
            this.ivPreview = (ImageView) params[0];
            String url = (String) params[1];
            return loadBitmap(url);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            ivPreview.setImageBitmap(result);
        }
    }

    public Bitmap loadBitmap(String url) {
        URL newurl = null;
        Bitmap bitmap = null;
        try {
            newurl = new URL(url);
            bitmap = BitmapFactory.decodeStream(newurl.openConnection().getInputStream());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (editTrue) {
            Toast.makeText(this, "업로드 하지 않은 작업이 남아 있습니다. \r\n업로드를 완료해 주세요!!", Toast.LENGTH_SHORT).show();
            return;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub

        switch (v.getId()) {
            //폴더열기
            case R.id.btnFolderSearch:
                imageListView();
                break;
            //이미지 업로드
            case R.id.btnImageUpload:
                pickImageUpload();
                break;
            //사진찍기
            case R.id.btnCameraOn:
                dispatchTakePictureIntent();
                break;

            //다시찍기
            case R.id.btnRePick:
                tempFileDeleteReStart();
                break;
            case R.id.btnRenew:
                setReNew();
                break;


        }
    }

    //2017-04 m3mobile 추가
    @Override
    protected void onResume() {
        super.onResume();
        if (m3Mobile) {
            M3MoblieBarcodeScanBroadcast.setBarcodeEditText(context, text_barcode, text_gname);
            M3MoblieBarcodeScanBroadcast.setOnResume();
        }
    }

    //2017-04 m3mobile 추가
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (m3Mobile) {
            M3MoblieBarcodeScanBroadcast.setOnDestory();
        }
    }

    //끝
}
