package tipsystem.tips;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

import tippsystem.adapter.ImgArrayAdapter;
import tippsystem.adapter.ImgInfoClass;
import kr.co.tipos.tips.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class ManageImageListView extends Activity{
	
	private Context mContext;
	//private final int imgWidth = 320;
	//private final int imgHeight = 372;
	
	//이미지리스트
	private ListView mListView = null;
	private ArrayList<ImgInfoClass> mCareList = null;
	
	private ImgArrayAdapter iaa;
	
	//삭제 관련 버튼
	Button btn_deleteButton;
	Button btn_selectAll;
	Button btn_selectFalse;
	
	//이미지 목록 리스트
	private ArrayList<HashMap<String, String>> imageDataList = new ArrayList<HashMap<String, String>>();    
	
	String filePath;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_manage_image_list_view);
		
		mContext = this;
		
		//이미지리스트
		//리스트뷰를 호출합니다.
		mListView = (ListView)findViewById(R.id.imgListView);
		//넘어온 파일패스 값을 불러 냅니다.
		Intent intent = getIntent();
		filePath = intent.getStringExtra("FILEPATH");
				
        mCareList = new ArrayList<ImgInfoClass>();
       
        btn_deleteButton = (Button)findViewById(R.id.btn_selectDelete);
        btn_deleteButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				 // 체크되 있는 CheckBox의 Position을 얻어 온다.
				
				AlertDialog.Builder builder = new AlertDialog.Builder(ManageImageListView.this,AlertDialog.THEME_HOLO_LIGHT);
				
				builder.setMessage("선택 이미지를 삭제 하시겠습니까?")
				.setCancelable(false)
				.setPositiveButton("확인", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
						//선택 파일을 삭제 합니다.
						selectDelete();
						
		                iaa.notifyDataSetChanged();						
					}
				})
				.setNegativeButton("취소", new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.cancel();
					}
				} );   
				
				//알림창 띄우기 
				AlertDialog dialog = builder.create();
				dialog.show();
				
			}
		});
        
        btn_selectAll = (Button)findViewById(R.id.btn_allSelect);
        btn_selectAll.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				iaa.setAllChecked(true);
				btn_selectAll.setText(iaa.getCount()+"개 선택");
				iaa.notifyDataSetChanged();
			}
		});
        
        btn_selectFalse = (Button)findViewById(R.id.btn_selectFalse);
        btn_selectFalse.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				iaa.setAllChecked(false);
				btn_selectAll.setText("전체 선택");
				iaa.notifyDataSetChanged();
			}
		});
        
        //어뎁터에 이미지 올리기
        getImageList();
       
        //어뎁터 연결
        iaa = new ImgArrayAdapter(this, R.layout.activity_imagelistview_row, mCareList, btn_selectAll);
        
        // ArrayAdapter 연결
        mListView.setAdapter(iaa);         
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {        	       
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				
				//선택 이미지를 확대합니다.
				setImageView(position);			
				//Toast.makeText(getApplicationContext(), "ITEM CLICK = " + position, Toast.LENGTH_SHORT).show();
			}        
        });    
	}
	
	// 1. 이미지 목록 만들기
	private void getImageList(){
		
		File list = new File(filePath);
		
    	//이미지 파일만 불러옵니다.
		File[] filelist = list.listFiles(new FilenameFilter() {			
			@Override
			public boolean accept(File dir, String filename) {
				// TODO Auto-generated method stub
				Boolean bOK = false;
				if(filename.endsWith(".png")) bOK = true;
				if(filename.endsWith(".gif")) bOK = true;
				if(filename.endsWith(".jpg")) bOK = true;
				if(filename.endsWith(".bmp")) bOK = true;		
							
				return bOK;
			}
		});
		
		//전체 경로와 파일명을 분리 합니다.
		String filename="", filepath="", filesize="", filedate="";		
		
		for(File temp : filelist){
			HashMap<String, String> map = new HashMap<String, String>();
					
			filename = temp.getName();
			filepath  = temp.getAbsolutePath();
			
			if(temp.length() > 0){
				filesize = getFileSize(temp.length());
			}else{
				temp.delete();
				return;
			}
			//날자 입력하기
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy. MM. dd.", Locale.KOREA);
			filedate = sdf.format(temp.lastModified());					
					
			//목록의 파일을 bitmap 으로 변환 
			BitmapFactory.Options bo = new BitmapFactory.Options();
	        bo.inSampleSize = 8;
	        Bitmap bmp = BitmapFactory.decodeFile(filepath.toString(), bo);
	        Bitmap resized = Bitmap.createScaledBitmap(bmp, 60, 60, true);
			
			//저장하기
			map.put("imgName", filename);
			map.put("imgPath", filepath);	
			map.put("imgSize", filesize);
			map.put("imgDate", filedate);			
			
			Log.w("파일 검색 결과 --> ", "파일명 : "+filename+" 파일경로 : "+filepath+" 파일크기 : "+ filesize+" 파일생성일 : "+filedate);			
			imageDataList.add(map);
			
			String[] data = {filename, filepath, filesize, filedate};
	        //첫번째 chk박스, 두번째 이미지 , 세번째 text 순 
			mCareList.add(new ImgInfoClass( data, resized));	
		}
		
		///목록올리기
		if(imageDataList.size() <= 0){
			Toast.makeText(this, "저장된 이미지가 없습니다.", Toast.LENGTH_SHORT).show();
		}
	}
		
	//파일 사이즈 구하기
	private String getFileSize(long filesize){
	    String gubun[] = { "Byte", "KB", "MB" } ;
	    String returnSize = new String();       
	    int gubnKey = 0;
	    double changeSize = 0; 
	    double fileSize = filesize;     
	 
	    try{
	        
	        for( int x=0 ; Math.round(fileSize /= (double)1024 ) > 0.0  ; x++){	        		
	               gubnKey = x+1;
	               changeSize = fileSize;
	               Log.w("파일 사이즈 --> ", changeSize+"입니다.");
	             }
	        	//확장자를 붙여서 보낸다. 4.9KB
	            returnSize = (Math.round(changeSize*100d) / 100d) + gubun[gubnKey];
	        }catch ( Exception ex){
	             returnSize = "0.0 Byte";
	        }  
	 
	        return returnSize;   
	}
	
	//선택시 이미지 키우기 기능 
	private void setImageView(int selectedIndex){
		
		HashMap<String, String> map = imageDataList.get(selectedIndex);
		String imgPath = map.get("imgPath");
    	BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inSampleSize = 2;
		ImageView iv = (ImageView)findViewById(R.id.image_popupgeView);
		Bitmap bm = BitmapFactory.decodeFile(imgPath, bfo);
		
		Bitmap resized = Bitmap.createScaledBitmap(bm, iv.getWidth(), iv.getHeight(), true);
		iv.setImageBitmap(resized);	
		
	}
	
	
	//삭제기능
	private void selectDelete(){
		
		// 체크되 있는 CheckBox의 Position을 얻어 온다.
        for(int i=iaa.getChecked().size() ; i > 0 ; i--){
            Log.d("test", "체크되 있는 Position = " + iaa.getChecked().get(i-1));
            
            int position = iaa.getChecked().get(i-1);            
                                    
            ImgInfoClass temp = iaa.getItem(position);
        	Log.w("선택 파일 삭제 -->", temp.filepath);
            File file = new File(temp.filepath);
            if(file.exists()){
            	file.delete();
            	mCareList.remove(position);
            	Log.w("선택 파일 삭제 -->", "파일을 삭제 했습니다.");            	
            }            
        }        
        
        iaa.setAllChecked(false);
		btn_selectAll.setText("전체 선택");
        //목록 새로고침
        iaa.notifyDataSetChanged();				
	}
	
	
	//전화면으로 넘기기기능
		
	
	
	/********************************************** 하단정보 ******************************************************************/
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.manage_image_list_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
