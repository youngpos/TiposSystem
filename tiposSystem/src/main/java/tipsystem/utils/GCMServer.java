package tipsystem.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.google.android.gcm.server.Message;
import com.google.android.gcm.server.MulticastResult;
import com.google.android.gcm.server.Result;
import com.google.android.gcm.server.Sender;

import android.os.AsyncTask;
import android.util.Log;

public class GCMServer extends AsyncTask<List<String>, Integer, String>{

	@Override
	protected String doInBackground(List<String>... params) {
		
		try {
			sendMessage(params[0], params[1]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}

	public void sendMessage(List<String> list, List<String> list_junpyo) throws IOException {	 

		Log.d("시작합니다.", "발송 했습니다.");
		//GCM 등록 아이디
		Sender sender = new Sender("AIzaSyBA9BUJnmDP8JU-doB5NLT2VQTpnRPtIAA");

		boolean SHOW_ON_IDLE = false;    //기기가 활성화 상태일때 보여줄것인지
		int LIVE_TIME = 1800;  //이건 시간이 아니고 초를 의미..
		//int RETRY = 5;  //메시지 전송실패시 재시도 횟수

		//테스트용 핸드폰별 등록 아이디
		//String regId = "APA91bFQ7zXF6_LIJ44u4PXQQOQUSVpoFhO6f2FTueLNI9SVegbYEkxLzrnrwRt12DIL3KEPqSonWXL3Hus1kEdQCamJ7IhaPTSC8p-H2z8hA5xd9z_cCkZfUbV8GPStjMaAl-JDICUtTjGLuQsx8xdx4n5IwLHMV17Eyb0fxw7fK-QePVxt56Y";	 
		
		Message message = new Message.Builder()
				.addData("Title", "태일포스 문자 서비스")
				.addData("Message", list_junpyo.get(0).toString())
				.delayWhileIdle(SHOW_ON_IDLE)
		        .timeToLive(LIVE_TIME)
		        .build();		 

		MulticastResult multiResult = sender.send(message, list, 5);

		if (multiResult != null) {
			List<Result> resultList = multiResult.getResults();	 

			for (Result result : resultList) {
					//System.out.println(result.getMessageId());
				Log.e("PUSH errer", result.getMessageId());
			}
		}
		Log.d("종료합니다.", "발송 완료 했습니다.");
		}	
}
