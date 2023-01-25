package tipsystem.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;

import android.os.AsyncTask;
import android.util.Log;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;
import it.sauronsoftware.ftp4j.FTPListParseException;

public class FTPUpload extends AsyncTask<String, Integer, Integer> {

    private String TAG = "FTP 파일 업로드";
    /*
     * 결과 목록
     * 0: 정상 등록
     * 1: 전송할 파일이 없습니다.
     *
     *
     */

    @Override
    protected Integer doInBackground(String... params) {
        // TODO Auto-generated method stub

        //오피스코드
        String officecode = params[0];
        //파일패스
        String filepath = params[1];
        //파일구분 upload // delete
        String gubun = params[2];

        try {
            FTPClient mFTP = new FTPClient();

            // 2022.05.26. 변경
            //mFTP.connect("14.38.161.45", 7000);  // ftp로 접속
            mFTP.connect(TIPOS.FTP_IP, TIPOS.FTP_PORT);  // ftp로 접속

            mFTP.login("xodlfvhtm", "tips");

            //mFTP.setFileType(FTP.BINARY_FILE_TYPE); // 바이너리 파일
            //mFTP.enterLocalPassiveMode(); // 패시브 모드로 접속

            // 업로드 경로 수정 (선택 사항 )
            Log.w(TAG, filepath);
            Log.w(TAG, officecode);
            mFTP.changeDirectory(officecode);

            File path = new File(filepath); // 업로드 할 파일

            if (!path.exists()) {
                //업로드할 파일이 없다면 팅깁니다.
                return 1;
            }
		     
		    /*if (path.listFiles().length > 0) { // 폴더를 가지고와 폴더 내부 파일 리스트를 만든다
		          for (File file : path.listFiles()) {
		               if (file.isFile()) {
		                    FileInputStream ifile = new FileInputStream(file);
		                    mFTP.rest(file.getName());  // ftp에 해당 파일이있다면 이어쓰기
		                    mFTP.appendFile(file.getName(), ifile); // ftp 해당 파일이 없다면 새로쓰기
		               }
		          }
		     }*/
		     
		    /* FileInputStream ifile = new FileInputStream(path);
		     	
		     //mFTP.rest(path.getName());  // ftp에 해당 파일이 있다면 이어쓰기
		     mFTP.appendFile(path.getName(), ifile); // ftp 해당 파일이 없다면 새로쓰기
*/
            if (gubun.equals("upload")) {
                try {
                    if (0 < mFTP.fileSize(path.getName()))
                        mFTP.deleteFile(path.getName());
                } catch (FTPException e) {
                    Log.w(TAG, "파일이 존재 하지 않습니다.");
                }
                mFTP.upload(path);
            } else {
                try {
                    String[] file = mFTP.listNames();
                    for (String name : file) {

                        if (path.getName().equals(name)) {
                            mFTP.deleteFile(name);
                        }
                    }

                } catch (FTPListParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }

            mFTP.disconnect(true); // ftp disconnect

        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FTPIllegalReplyException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FTPException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FTPDataTransferException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FTPAbortedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return 0;
    }

    @Override
    protected void onPostExecute(Integer result) {
        // 다 받아진 후 받은 파일 총용량 표시 작업
        // showDialog("Downloaded " + result + " bytes");
    }

}