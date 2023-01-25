package tipsystem.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class TIPS_Config {

    private String TAG = "TIPS_Config";

    //----------------------------------------//
    // 2022.05.26.본사서버 IP변경
    //----------------------------------------//
    private String SHOP_IP = "";
    private String SHOP_PORT = "";
    private String DB_ID = "";
    private String DB_NAME = "";
    private String DB_PW = "";
    //----------------------------------------//

    private String SHOP_OFFICECODE;
    private String POS_ID = "P";
    private String SHOP_NAME = "할인마트";

    private String APP_USER_GRADE = "";

    public String getDB_ID() {
        return DB_ID;
    }

    public void setDB_ID(String DB_ID) {
        this.DB_ID = DB_ID;
    }

    public String getDB_NAME() {
        return DB_NAME;
    }

    public void setDB_NAME(String DB_NAME) {
        this.DB_NAME = DB_NAME;
    }

    public String getDB_PW() {
        return DB_PW;
    }

    public void setDB_PW(String DB_PW) {
        this.DB_PW = DB_PW;
    }

    //로그인 관련 정보
    private String USER_ID = "1";
    private String USER_PW = "1";
    private boolean LOCAL_FOOD = false;

    //암호화관련
    private String EN_USE = "0";

    //배달 문자내용
    private String TRAN_MSG = "";


    //바코드프린터 환경설정
    private String BARPRINTER_ADDRESS = "";
    private String BARPRINTER_PORT = "";
    private int SELECTPRINT = 0; //매장프린터 //프린터 종류
    public int SELECTPRINT_GUBUN = 0; //용지 선택 구분

    private Context context;

    private SharedPreferences pref;

    public String getAPP_USER_GRADE() {
        return APP_USER_GRADE;
    }

    private JSONObject m_shop;
    private JSONObject m_userProfile;

    private boolean M3MOBLIE;

    //암호화키
    private String KEY = "!@#$tipos16001883)(*&";
    private String IV = "1234567890abcdef";


    public boolean isM3MOBLIE() {
        // 환경설정 관련 옵션 아이템 불러오기
        pref = PreferenceManager.getDefaultSharedPreferences(this.context);
        M3MOBLIE = pref.getBoolean("m3mobile", false);

        return M3MOBLIE;
    }

    public String getEN_USE() {
        return EN_USE;
    }

    public String getTRAN_MSG() {
        return TRAN_MSG;
    }

    public TIPS_Config(Context context){

        this.context = context;

        //매장 정보 불러오기
        m_shop = LocalStorage.getJSONObject(this.context, "currentShopData");
        m_userProfile = LocalStorage.getJSONObject(this.context, "userProfile");

        try {

            //매장 DDNS주소 불러오기 매장 정보 포함
            SHOP_IP = m_shop.getString("SHOP_IP");
            SHOP_PORT = m_shop.getString("SHOP_PORT");
            //----------------------------------------//
            // 2021.12.21. 매장DB 아이다,비밀번호,데이타베이스명 추가
            //----------------------------------------//
            DB_ID = m_shop.getString("shop_uuid");
            DB_PW = m_shop.getString("shop_uupass");
            DB_NAME = m_shop.getString("shop_uudb");
            //----------------------------------------//

            SHOP_OFFICECODE = m_shop.getString("OFFICE_CODE");
            SHOP_NAME = m_shop.getString("Office_Name");

            APP_USER_GRADE = m_userProfile.getString("APP_USER_GRADE");

            //----------------------------------------//
            // 2022.04.25. 로그인 아이다,비밀번호 오류 수정
            //----------------------------------------//
            USER_ID =  m_userProfile.getString("User_ID");
            USER_PW =  m_userProfile.getString("User_PWD");
            // 로컬 등록
            LocalStorage.setString(this.context, "LoginID:"+SHOP_OFFICECODE, USER_ID);
            LocalStorage.setString(this.context, "LoginPW:"+SHOP_OFFICECODE, USER_PW);
            //----------------------------------------//


        } catch (JSONException e) {
            e.printStackTrace();
        }


        try {
            EN_USE = m_userProfile.getString("en_use");
            TRAN_MSG = m_userProfile.getString("Mobile_Tran_Msg");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //바코드 프린터 관련
        SELECTPRINT = LocalStorage.getInt(this.context, "BarcodePrinter:"+SHOP_OFFICECODE);
        BARPRINTER_ADDRESS = LocalStorage.getString(this.context, "PrintAddress:"+SHOP_OFFICECODE);
        BARPRINTER_PORT = LocalStorage.getString(this.context, "PrintPort:"+SHOP_OFFICECODE);

        SELECTPRINT_GUBUN = LocalStorage.getInt(this.context, "BarcodePrinterPaperGubun:"+SHOP_OFFICECODE);

        POS_ID = LocalStorage.getString(this.context, "currentPosID:" + SHOP_OFFICECODE);

        //USER_ID = LocalStorage.getString(this.context, "LoginID:"+SHOP_OFFICECODE);
        //USER_PW = LocalStorage.getString(this.context, "LoginPW:"+SHOP_OFFICECODE);

        LOCAL_FOOD = LocalStorage.getBoolean(this.context, "LocalFood:"+SHOP_OFFICECODE);

    }

    public String getBARPRINTER_ADDRESS() {
        return BARPRINTER_ADDRESS;
    }

    public String getBARPRINTER_PORT() {
        return BARPRINTER_PORT;
    }

    public void setBARPRINTER_PORT(String BARPRINTER_PORT) {
        this.BARPRINTER_PORT = BARPRINTER_PORT;
    }

    public void setSELECTPRINT_GUBUN(String SELECTPRINT_GUBUN) {
        int gubun = 0;
        switch(SELECTPRINT_GUBUN){
            case "30*58":
                gubun = 0;
                break;
            case "23*40":
                gubun = 1;
                break;
            case "사용자정의1":
                gubun = 2;
                break;
            case "사용자정의2":
                gubun = 3;
                break;
            case "사용자정의3":
                gubun = 4;
                break;
        }

        LocalStorage.setInt(this.context, "BarcodePrinterPaperGubun:"+SHOP_OFFICECODE, gubun);
        Log.d(TAG, gubun+"");
        this.SELECTPRINT_GUBUN = gubun;
    }

    //갱싱합니다.
    public void setReflash(){
        //바코드 프린터 관련
        SELECTPRINT = LocalStorage.getInt(this.context, "BarcodePrinter:"+SHOP_OFFICECODE);
        BARPRINTER_ADDRESS = LocalStorage.getString(this.context, "PrintAddress:"+SHOP_OFFICECODE);
        BARPRINTER_PORT = LocalStorage.getString(this.context, "PrintPort:"+SHOP_OFFICECODE);

        SELECTPRINT_GUBUN = LocalStorage.getInt(this.context, "BarcodePrinterPaperGubun:"+SHOP_OFFICECODE);
    }

    public String getSELECTPRINT_GUBUN() {
        String result = TIPOS.BARCODE_PRINTER_PAPERGUBUN_0;
        switch(SELECTPRINT_GUBUN){
            case 0:
                result = TIPOS.BARCODE_PRINTER_PAPERGUBUN_0;
                break;
            case 1:
                result = TIPOS.BARCODE_PRINTER_PAPERGUBUN_1;
                break;
            case 2:
                result = TIPOS.BARCODE_PRINTER_PAPERGUBUN_2;
                break;
            case 3:
                result = TIPOS.BARCODE_PRINTER_PAPERGUBUN_3;
                break;
            case 4:
                result = TIPOS.BARCODE_PRINTER_PAPERGUBUN_4;
                break;

        }
        return result;
    }

    public int getSELECTPRINT() {
        return SELECTPRINT;
    }

    public String getSELECTPRINT_NAME(){
        String result = "사용안함";
        switch(SELECTPRINT){
            case 0:
                result = "사용안함";
                break;
            case 1:
                result = "매장프린터";
                break;
            case 2:
                result = "SPP-L3000";
                break;
            case 3:
                result = "LK-P30II";
                break;
        }
        return result;
    }

    public boolean setCardInfo(String office_num, String catID, String password, String localnum){
        try {
            LocalStorage.setString(this.context, "KisVan_OfficeNum:" + SHOP_OFFICECODE, office_num);
            LocalStorage.setString(this.context, "KisVan_CatID:" + SHOP_OFFICECODE, catID);
            LocalStorage.setString(this.context, "KisVan_Password:" + SHOP_OFFICECODE, password);
            LocalStorage.setString(this.context, "KisVan_LocalNo:" + SHOP_OFFICECODE, localnum);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public HashMap<String, String> getCardInfo(){

        HashMap<String, String> map = new HashMap<>();

        map.put("agencyApp", "com.kismobile.pay");
        map.put("bizNo", LocalStorage.getString(this.context, "KisVan_OfficeNum:"+SHOP_OFFICECODE));
        map.put("serialNo", LocalStorage.getString(this.context, "KisVan_CatID:"+SHOP_OFFICECODE));
        map.put("downPasswordNo", LocalStorage.getString(this.context, "KisVan_Password:"+SHOP_OFFICECODE));
        map.put("localNo", LocalStorage.getString(this.context, "KisVan_LocalNo:"+SHOP_OFFICECODE));

        return map;
    }

    public String getSHOP_IP() {
        return SHOP_IP;
    }

    public String getSHOP_PORT() {
        return SHOP_PORT;
    }

    public String getSHOP_OFFICECODE() {
        return SHOP_OFFICECODE;
    }

    public String getPOS_ID() { return POS_ID; }

    public String getSHOP_NAME(){ return SHOP_NAME; }

    public String getUSER_ID(){ return USER_ID; }

    public String getUSER_PW(){ return USER_PW; }

    public boolean getULOCAL_FOOD(){ return LOCAL_FOOD; }

    public String getMobile_MSG(){
        String sms_msg = "";
        try {
            sms_msg = m_userProfile.getString("Mobile_Tran_Msg");
        }catch(JSONException | NullPointerException e){
            e.printStackTrace();
        }
        return sms_msg;
    }

    public String getKEY() {
        return KEY;
    }

    public String getIV() {
        return IV;
    }
}
