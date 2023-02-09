package tipsystem.utils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import android.widget.Toast;

import tipsystem.tips.models.SPPL3000;

public class DBAdapter {

    public static final String TAG = "DBAdapter";

    //2018-10-30
    private static final int VERSION = 2;

    private Context context;
    private SQLiteDatabase db;
    private TipsSQLitHelper tipssql;
    private String m_officeCode;

    /**
     * 디비정보를 호출합니다.
     *
     * @param context this
     * @param name    officecode.tips
     */
    public DBAdapter(Context context, String name) {
        this.context = context;
        tipssql = new TipsSQLitHelper(context, name, null, VERSION);
        this.m_officeCode = name.substring(0, 7);


        //this.open();
        Log.i("DB Start", "start");

        // 2022.11.10.김영목. 임시저장 테이블 체크
        addTables("Temp_BaPrint");
        //--------------------//
        // 2021.01.05. 김영목. 바코드프린터 추가필드
        //--------------------//
        alterTable_BaPrint();
        //--------------------//

    }

    //DB open
    public void open() throws SQLException {
        try {
            db = tipssql.getWritableDatabase();
        } catch (SQLException e) {
            db = tipssql.getReadableDatabase();
        }
        Log.i("DB Open", "open");
    }

    /*
     * 바코드 프린터 전송결과 저장하기
     * 김현수 2015-02-06
     *
     */
    public void insert_barPrint(List<HashMap<String, String>> mfillMaps, String active_type) {
        Log.d("DB BarPrint 시작", "BarPrint 저장중... ");

        db = tipssql.getWritableDatabase();

        //생성할 전표번호
        String posID = LocalStorage.getString(context, "currentPosID:" + m_officeCode);
        String query = "";

        //저장일자
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentDateTime = sdf1.format(new Date());

        String BaPrint_Num = currentDate.replace("-", "");


        String s_sell_pri = "0";
        String s_bot_sell = "0";
        int i_t_sellpri = 0;

        //전송 전표번호 생성
        BaPrint_Num += posID + getBarPrintNum(currentDate);
        try {
            for (int i = 0; i < mfillMaps.size(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                map = mfillMaps.get(i);

                //2020-06-15 판매가에 공병가 포함
                s_sell_pri = StringFormat.convertToNumberFormat(map.get("Sell_Pri")).replace(",", "");
                Log.e("" + s_sell_pri, "Sell_Pri");

                if (active_type == "0") {  //상품관리
                    s_bot_sell = StringFormat.convertToNumberFormat(map.get("Bot_Sell")).replace(",", "");
                    Log.e("" + s_bot_sell, "Bot_Sell");

                    i_t_sellpri = Integer.parseInt(s_sell_pri) + Integer.parseInt(s_bot_sell);
                    Log.e("" + i_t_sellpri, "i_t_sellpri");
                } else {  //바코드발행관리
                    i_t_sellpri = Integer.parseInt(s_sell_pri);
                }

                query = "insert into BaPrint_History(BaPrint_Num, BarCode, G_Name, Std_Size, Count, Sell_Pri, Bus_Name, "
                        + "Con_Rate, Unit, Std_Rate, OrgSell_Pri, Print_Date, Print_DateTime, Office_Code) values("
                        + "'" + BaPrint_Num + "', "
                        + "'" + map.get("BarCode") + "', "
                        + "'" + map.get("G_Name") + "', "
                        + "'" + map.get("Std_Size") + "', "
                        + "'" + map.get("Count") + "', "
                        + "'" + String.valueOf(i_t_sellpri) + "', "
                        + "'" + map.get("Bus_Name") + "', "
                        + "'" + map.get("Con_Rate") + "', "
                        + "'" + map.get("Unit") + "', "
                        + "'" + map.get("Std_Rate") + "', "

                        // 2021.01.06.김영목. 원판매가 추가
                        //+"'', "//+ "'" +map.get("OrgSell_Pri")+"', "
                        + "'" + map.get("Sell_Org") + "', "

                        + "'" + currentDate.toString() + "', "
                        + "'" + currentDateTime.toString() + "', "
                        + "'" + m_officeCode + "' "
                        + ");";

                Log.d(query, "db in");

                db.execSQL(query);
            }

        } catch (SQLiteException e) {
            e.printStackTrace();
        }
        Log.d("DB BarPrint 완료", "BarPrint 저장완료 ");
    }

    private String getBarPrintNum(String currentDate) {

        db = tipssql.getReadableDatabase();
        String query = "select BaPrint_Num from BaPrint_History where print_date='" + currentDate + "' group by BaPrint_Num;";

        Cursor cursor = db.rawQuery(query, null);
        String num = "";
        if (cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                num = cursor.getString(0);
                //Log.i("테이블명", cursor.getString(0));
            }

            num = String.format("%03d", Integer.parseInt(num.substring(9)) + 1);
            //Log.i("전표번호 : ", num.toString());
        } else {
            //Log.i("전표번호 : ", "001");
            return "001";
        }
        return num;
    }

    //지정날자 전표 목록만 가져오기
    public List<HashMap<String, String>> getBarPrintHistory(String query) {

        db = tipssql.getReadableDatabase();

        //query = "Select * from BaPrint_History where print_date='"+date.toString()+"'; ";
        try {
            Cursor cursor = db.rawQuery(query, null);
            List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    //----------------------------------------//
                    // 2021.01.08.김영목. 불러올 때 할인율 계산 추가
                    //----------------------------------------//
                    float f_ratio = 0;
                    float f_sellPri = 0;
                    float f_sellOrg = 0;
                    //----------------------------------------//

                    HashMap<String, String> map = new HashMap<String, String>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        String columnName = cursor.getColumnName(i);
                        map.put(columnName, cursor.getString(i));

                        //----------------------------------------//
                        if (columnName.equals("Sell_Pri")) {
                            f_sellPri = Float.valueOf(cursor.getString(i)).floatValue();
                        }
                        if (columnName.equals("OrgSell_Pri")) {
                            map.put("Sell_Org", cursor.getString(i));
                            f_sellOrg = Float.valueOf(cursor.getString(i)).floatValue();
                        }
                        //----------------------------------------//
                    }

                    //----------------------------------------//
                    try {
                        if (f_sellOrg > f_sellPri) {
                            f_ratio = (f_sellOrg - f_sellPri) / f_sellOrg * 100;
                            if (Float.isInfinite(f_ratio) || Float.isNaN(f_ratio)) {
                                f_ratio = 0;
                            }
                        }
                    } catch (Exception e) {
                    }
                    //----------------------------------------//

                    map.put("Sale_Rate", String.format("%.0f", f_ratio)); // 할인율
                    mfillMaps.add(map);
                }
                //Log.w(TAG, mfillMaps.toString());
                return mfillMaps;
            } else {
                Toast.makeText(context, "조회 결과가 없습니다.", Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(context, "테이블이 존재하지 않습니다. ", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    public Boolean chkBarPrintTemp() {

        boolean result;
        db = tipssql.getReadableDatabase();
        String query = "select * from Temp_BaPrint;";

        Cursor cursor = db.rawQuery(query, null);

        String num = "";
        if (cursor.getCount() > 0) {
            result = true;
        } else {
            result=false;
        }

        return result;
    }
    // 2022.11.09. 바코드프린터 임시테이블 저장
    public void insert_TempBaPrint(List<HashMap<String, String>> mfillMaps, String active_type) {
        Log.d("DB Temp_BaPrint 임시저장", "Temp_BaPrint 저장중... ");

        db = tipssql.getWritableDatabase();

        String query = "";

        //저장일자
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String currentDate = sdf.format(new Date());

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String currentDateTime = sdf1.format(new Date());

        String BaPrint_Num = currentDate.replace("-", "");


        String s_sell_pri = "0";
        String s_bot_sell = "0";
        int i_t_sellpri = 0;


        BaPrint_Num = "";

        db.beginTransaction();

        try {
            // 먼저 혹시 있을 자료 다 지운다
            query = "Delete From Temp_BaPrint;";
            db.execSQL(query);

            for (int i = 0; i < mfillMaps.size(); i++) {
                HashMap<String, String> map = new HashMap<String, String>();
                map = mfillMaps.get(i);

                //2020-06-15 판매가에 공병가 포함
                s_sell_pri = StringFormat.convertToNumberFormat(map.get("Sell_Pri")).replace(",", "");
                Log.e("" + s_sell_pri, "Sell_Pri");

                if (active_type == "0") {  //상품관리
                    s_bot_sell = StringFormat.convertToNumberFormat(map.get("Bot_Sell")).replace(",", "");
                    Log.e("" + s_bot_sell, "Bot_Sell");

                    i_t_sellpri = Integer.parseInt(s_sell_pri) + Integer.parseInt(s_bot_sell);
                    Log.e("" + i_t_sellpri, "i_t_sellpri");
                } else {  //바코드발행관리
                    i_t_sellpri = Integer.parseInt(s_sell_pri);
                }

                query = "insert into Temp_BaPrint(BarCode, G_Name, Std_Size, Count, Sell_Pri, Bus_Name, "
                        + "Con_Rate, Unit, Std_Rate, OrgSell_Pri, Print_Date, Print_DateTime, Office_Code) values("
                        + "'" + map.get("BarCode") + "', "
                        + "'" + map.get("G_Name") + "', "
                        + "'" + map.get("Std_Size") + "', "
                        + "'" + map.get("Count") + "', "
                        + "'" + String.valueOf(i_t_sellpri) + "', "
                        + "'" + map.get("Bus_Name") + "', "
                        + "'" + map.get("Con_Rate") + "', "
                        + "'" + map.get("Unit") + "', "
                        + "'" + map.get("Std_Rate") + "', "

                        // 2021.01.06.김영목. 원판매가 추가
                        //+"'', "//+ "'" +map.get("OrgSell_Pri")+"', "
                        + "'" + map.get("Sell_Org") + "', "

                        + "'" + currentDate.toString() + "', "
                        + "'" + currentDateTime.toString() + "', "
                        + "'" + m_officeCode + "' "
                        + ");";

                Log.d(query, "db in");

                db.execSQL(query);
            }

            db.setTransactionSuccessful();
            Log.d("DB Temp_BaPrint 완료", "Temp_BaPrint 저장완료 ");

        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.d("DB Temp_BaPrint 저장실패", "Temp_BaPrint 저장실패 ");
        } finally {
            db.endTransaction();
        }
    }
    public void delete_TempBaPrint() {

        Log.d("DB Temp_BaPrint 삭제", "Temp_BaPrint 삭제중... ");
        String query = "";

        db = tipssql.getWritableDatabase();
        db.beginTransaction();

        try {
            // 먼저 혹시 있을 자료 다 지운다
            query = "Delete From Temp_BaPrint;";
            db.execSQL(query);

            db.setTransactionSuccessful();
            Log.d("DB Temp_BaPrint 삭제완료", "Temp_BaPrint 삭제완료 ");

        } catch (SQLiteException e) {
            e.printStackTrace();
            Log.d("DB Temp_BaPrint 삭제실패", "Temp_BaPrint 삭제실패 ");
        } finally {
            db.endTransaction();
        }
    }

    // 2022.11.09. 바코드프린터 임시테이블 저장 목록 가져오기
    public List<HashMap<String, String>> getTempBaPrint(String query) {

        db = tipssql.getReadableDatabase();

        try {
            Cursor cursor = db.rawQuery(query, null);
            List<HashMap<String, String>> mfillMaps = new ArrayList<HashMap<String, String>>();

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    //----------------------------------------//
                    // 2021.01.08.김영목. 불러올 때 할인율 계산 추가
                    //----------------------------------------//
                    float f_ratio = 0;
                    float f_sellPri = 0;
                    float f_sellOrg = 0;
                    //----------------------------------------//

                    HashMap<String, String> map = new HashMap<String, String>();
                    for (int i = 0; i < cursor.getColumnCount(); i++) {
                        String columnName = cursor.getColumnName(i);
                        map.put(columnName, cursor.getString(i));

                        //----------------------------------------//
                        if (columnName.equals("Sell_Pri")) {
                            f_sellPri = Float.valueOf(cursor.getString(i)).floatValue();
                        }
                        if (columnName.equals("OrgSell_Pri")) {
                            map.put("Sell_Org", cursor.getString(i));
                            f_sellOrg = Float.valueOf(cursor.getString(i)).floatValue();
                        }
                        //----------------------------------------//
                    }

                    //----------------------------------------//
                    try {
                        if (f_sellOrg > f_sellPri) {
                            f_ratio = (f_sellOrg - f_sellPri) / f_sellOrg * 100;
                            if (Float.isInfinite(f_ratio) || Float.isNaN(f_ratio)) {
                                f_ratio = 0;
                            }
                        }
                    } catch (Exception e) {
                    }
                    //----------------------------------------//

                    map.put("Sale_Rate", String.format("%.0f", f_ratio)); // 할인율
                    mfillMaps.add(map);
                }
                //Log.w(TAG, mfillMaps.toString());
                return mfillMaps;
            } else {
                Toast.makeText(context, "조회 결과가 없습니다.", Toast.LENGTH_LONG).show();
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(context, "테이블이 존재하지 않습니다. ", Toast.LENGTH_LONG).show();
        }
        return null;
    }

    //바코드 프린터 설정 저장하기
    public boolean setBarPrint_SPPL3000(SPPL3000 sppl3000) {

        db = tipssql.getWritableDatabase();
        SPPL3000 spp = sppl3000;
        String key_string = "_id=" + spp.get_idx();

        ContentValues values = new ContentValues();
        //values.put(spp.m_idx, spp.get_idx());
        values.put(spp.m_Print_Size, spp.getPrint_Size());
        values.put(spp.m_Lavel_Hight, spp.getLavel_Hight());
        values.put(spp.m_Lavel_Width, spp.getLavel_Width());
        values.put(spp.m_Print_Direction, spp.getPrint_Direction());
        values.put(spp.m_Paper_Gubun, spp.getPaper_Gubun());
        values.put(spp.m_Gap_Width, spp.getGap_Width());
        values.put(spp.m_Print_StdSize_YN, spp.getPrint_StdSize_YN());
        values.put(spp.m_Print_Price_YN, spp.getPrint_Price_YN());
        values.put(spp.m_Print_Office_YN, spp.getPrint_Office_YN());
        values.put(spp.m_Print_Danga_YN, spp.getPrint_Danga_YN());
        values.put(spp.m_Word_Length, spp.getWord_Length());
        values.put(spp.m_Goods_Setting, spp.getGoods_Setting());
        values.put(spp.m_Stdsize_Setting, spp.getStdsize_Setting());
        values.put(spp.m_Price_Setting, spp.getPrice_Setting());
        values.put(spp.m_Office_Setting, spp.getOffice_Setting());
        values.put(spp.m_Danga_Setting, spp.getDanga_Setting());
        values.put(spp.m_Barcode_Setting, spp.getBarcode_Setting());

        //----------------------------------------//
        // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
        //----------------------------------------//
        values.put(spp.m_Print_SellPrice_YN, spp.getPrint_SellPrice_YN());
        values.put(spp.m_Print_SaleSellRate_YN, spp.getPrint_SaleSellRate_YN());
        values.put(spp.m_SellPrice_Setting, spp.getSellPrice_Setting());
        values.put(spp.m_SaleSellRate_Setting, spp.getSaleSellRate_Setting());
        //----------------------------------------//

        //----------------------------------------//
        // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
        //----------------------------------------//
        values.put(spp.m_Print_Location_YN, spp.getPrint_Location_YN());
        values.put(spp.m_Print_NickName_YN, spp.getPrint_NickName_YN());
        values.put(spp.m_Print_BranchName_YN, spp.getPrint_BranchName_YN());
        values.put(spp.m_Print_AddItem_YN, spp.getPrint_AddItem_YN());

        values.put(spp.m_Location_Setting, spp.getLocation_Setting());
        values.put(spp.m_NickName_Setting, spp.getNickName_Setting());
        values.put(spp.m_BranchName_Setting, spp.getBranchName_Setting());
        values.put(spp.m_AddItem_Setting, spp.getAddItem_Setting());
        //----------------------------------------//

        db.beginTransaction();
        try {
            db.update(TIPOS.BARCODE_PRINTER_TABLE, values, key_string, null);
            db.setTransactionSuccessful();
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        } finally {
            db.endTransaction();
            return true;
        }
    }


    //프린터 설정 가져오기
    public SPPL3000 getBarPrint_SPPL3000(String query) {

        db = tipssql.getReadableDatabase();
        SPPL3000 spp = new SPPL3000();

        try {

            Cursor cursor = db.rawQuery(query, null);
            HashMap<String, String> map = new HashMap<String, String>();

            if (cursor.moveToFirst()) {
                //while(cursor.moveToNext()){
                spp.set_idx(cursor.getInt(0));
                spp.setPrint_Size(cursor.getString(1));
                spp.setLavel_Hight(cursor.getInt(2));
                spp.setLavel_Width(cursor.getInt(3));
                spp.setPrint_Direction(cursor.getInt(4));
                spp.setPaper_Gubun(cursor.getInt(5));
                spp.setGap_Width(cursor.getInt(6));
                spp.setPrint_StdSize_YN(cursor.getInt(7));
                spp.setPrint_Price_YN(cursor.getInt(8));
                spp.setPrint_Office_YN(cursor.getInt(9));
                spp.setPrint_Danga_YN(cursor.getInt(10));
                spp.setWord_Length(cursor.getInt(11));
                spp.setGoods_Setting(cursor.getString(12));
                spp.setStdsize_Setting(cursor.getString(13));
                spp.setPrice_Setting(cursor.getString(14));
                spp.setOffice_Setting(cursor.getString(15));
                spp.setDanga_Setting(cursor.getString(16));
                spp.setBarcode_Setting(cursor.getString(17));

                //----------------------------------------//
                // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
                //----------------------------------------//
                spp.setPrint_SellPrice_YN(cursor.getInt(18));
                spp.setPrint_SaleSellRate_YN(cursor.getInt(19));
                spp.setSellPrice_Setting(cursor.getString(20));
                spp.setSaleSellRate_Setting(cursor.getString(21));
                //----------------------------------------//
                //----------------------------------------//
                // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
                //----------------------------------------//
                // 인쇄유무
                spp.setPrint_Location_YN(cursor.getInt(22));
                spp.setPrint_NickName_YN(cursor.getInt(23));
                spp.setPrint_BranchName_YN(cursor.getInt(24));
                spp.setPrint_AddItem_YN(cursor.getInt(25));
                // 설정
                spp.setLocation_Setting(cursor.getString(26));
				spp.setNickName_Setting(cursor.getString(27));
				spp.setBranchName_Setting(cursor.getString(28));
				spp.setAddItem_Setting(cursor.getString(29));
                //----------------------------------------//

                //}
                Log.w(TAG, map.toString());
            } else {
                spp = null;
                Toast.makeText(context, "조회 결과가 없습니다.", Toast.LENGTH_LONG).show();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(context, "테이블이 존재하지 않습니다. ", Toast.LENGTH_LONG).show();
        }

        return spp;
    }


    //삽입
	/*public void insert_goods(ResultSet rs) {		
		Log.d( "DB Goods 시작", "Goods 갱신중... " );
		
		try {
			
			int count = 1;
			while(rs.next()){				
				String query	 = "insert into Goods(BarCode, G_Name, Tax_YN, Std_Size, Pur_Pri, Pur_Cost, Add_Tax, RePur_Pri, Sell_Pri, "
						+ "ReSell_Pri, Sell_Pri1, Profit_Rate,  Bus_Code , Bus_Name , Pur_Use , Ord_Use , Sell_Use , Sto_Use , Box_Use,"
						+ "Pack_Use , Bulk_Use , Sale_Use , CusSale_Use , Scale_Use , Goods_Use , Real_Sto , Sale_Pur , Sale_Sell , Re_Pri ,"
						+ "Sale_SDate , Sale_EDate , VAT_CHK , G_grade) Values";
				query += "( '"+rs.getString("BarCode")+"',";
				query += "'"+rs.getString("G_Name")+"',";
				query += "'"+rs.getString("Tax_YN")+"',";
				query += "'"+rs.getString("Std_Size")+"',";
				query += "'"+rs.getString("Pur_Pri")+"',";
				query += "'"+rs.getString("Pur_Cost")+"',";
				query += "'"+rs.getString("Add_Tax")+"',";
				query += "'"+rs.getString("RePur_Pri")+"',";
				query += "'"+rs.getString("Sell_Pri")+"',";
				query += "'"+rs.getString("ReSell_Pri")+"',";
				query += "'"+rs.getString("Sell_Pri1")+"',";
				query += "'"+rs.getString("Profit_Rate")+"',";
				query += "'"+rs.getString("Bus_Code")+"',";
				query += "'"+rs.getString("Bus_Name")+"',";
				query += "'"+rs.getString("Pur_Use")+"',";
				query += "'"+rs.getString("Ord_Use")+"',";
				query += "'"+rs.getString("Sell_Use")+"',";
				query += "'"+rs.getString("Sto_Use")+"',";
				query += "'"+rs.getString("Box_Use")+"',";
				query += "'"+rs.getString("Pack_Use")+"',";
				query += "'"+rs.getString("Bulk_Use")+"',";
				query += "'"+rs.getString("Sale_Use")+"',";
				query += "'"+rs.getString("CusSale_Use")+"',";
				query += "'"+rs.getString("Scale_Use")+"',";
				query += "'"+rs.getString("Goods_Use")+"',";
				query += "'"+rs.getString("Real_Sto")+"',";
				query += "'"+rs.getString("Sale_Pur")+"',";
				query += "'"+rs.getString("Sale_Sell")+"',";
				query += "'"+rs.getString("Re_Pri")+"',";
				query += "'"+rs.getString("Sale_SDate")+"',";
				query += "'"+rs.getString("Sale_EDate")+"',";
				query += "'"+rs.getString("VAT_CHK")+"',";
				query += "'"+rs.getString("G_grade")+"'); ";

				db.execSQL(query);
				
				count++;
				Log.w("Count", String.valueOf(count));
			}
				
			Log.d("DB Goods 완료", "Goods 갱신 완료");
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d("DB Goods 갱신", "Goods 갱신 실패");
		}
				
		//JSONArray arraylist = new JSONArray();
	
		
		String filePath = context.getFilesDir().getPath().toString() + "/Goods.dat";
		File file = new File(filePath);
		deleteFile(file);
	    //ResultSetMetaData rsmd;
		
		
		
		try {
			int count = 1;
			
			file.createNewFile();
			FileOutputStream fos = context.openFileOutput("Goods.dat", Context.MODE_APPEND);
			
			while(rs.next()) {
				String obj="";
			  //int numColumns = rsmd.getColumnCount();			  
			  
			  //JSONObject obj = new JSONObject();
			  //Log.w("컬럼수량", String.valueOf(numColumns));
			  
			  for( int i=1; i<numColumns+1; i++) {			  			  
				String column_name = rsmd.getColumnName(i);
				obj += rs.getString(column_name)+", ";
			  }			  
			  obj += rs.getString("BarCode")+",";
			  obj += rs.getString("G_Name")+",";
			  obj += rs.getString("Tax_YN")+",";
			  obj += rs.getString("Std_Size")+",";
			  obj += rs.getString("Pur_Pri")+",";
			  obj += rs.getString("Pur_Cost")+",";
			  obj += rs.getString("Add_Tax")+",";
			  obj += rs.getString("RePur_Pri")+",";
			  obj += rs.getString("Sell_Pri")+",";
				obj += rs.getString("ReSell_Pri")+",";
				obj += rs.getString("Sell_Pri1")+",";
				obj += rs.getString("Profit_Rate")+",";
				obj += rs.getString("Bus_Code")+",";
				obj += rs.getString("Bus_Name")+",";
				obj += rs.getString("Pur_Use")+",";
				obj += rs.getString("Ord_Use")+",";
				obj += rs.getString("Sell_Use")+",";
				obj += rs.getString("Sto_Use")+",";
				obj += rs.getString("Box_Use")+",";
				obj += rs.getString("Pack_Use")+",";
				obj += rs.getString("Bulk_Use")+",";
				obj += rs.getString("Sale_Use")+",";
				obj += rs.getString("CusSale_Use")+",";
				obj += rs.getString("Scale_Use")+",";
				obj += rs.getString("Goods_Use")+",";
				obj += rs.getString("Real_Sto")+",";
				obj += rs.getString("Sale_Pur")+",";
				obj += rs.getString("Sale_Sell")+",";
				obj += rs.getString("Re_Pri")+",";
				obj += rs.getString("Sale_SDate")+",";
				obj += rs.getString("Sale_EDate")+",";
				obj += rs.getString("VAT_CHK")+",";
				obj += rs.getString("G_grade")+"\n\r";
			  count++;
			  Log.w("컬럼 내용 순번 : "+count, obj.toString());			
			  
			 byte[] strByte = obj.getBytes();
			  fos.write(strByte);
			}
			
			fos.close();
			//writeFile(file, obj.getBytes());
			//LocalStorage.setJSONArray(context, "Goods", arraylist);
			
		} catch (java.sql.SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		
		Log.d( "DB Goods 완료", "Goods 저장완료 " );	    
	}*/

    //갱신위해 전체 삭제
    public void delete_all() {
		/*
		//datalist  goods, branch, office
		String sql="";
		try{
			//DB 전체 삭제 하기
			sql = "delete from goods;";
			db.execSQL(sql);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DB delete goods", "데이타를 삭제하지 못했습니다.");
		}
		try{
			sql = "delete from L_Branch;";
			db.execSQL(sql);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DB delete L_Branch", "데이타를 삭제하지 못했습니다.");
		}			
		try{
			sql = "delete from M_Branch;";
			db.execSQL(sql);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DB delete M_Branch", "데이타를 삭제하지 못했습니다.");
		}
		try{
			sql = "delete from S_Branch;";
			db.execSQL(sql);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DB delete", " S_Branch 데이타를 삭제하지 못했습니다.");
		}
		try{
			sql = "delete from Office_Manage;";
			db.execSQL(sql);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DB delete", " Office_Manage 데이타를 삭제하지 못했습니다.");
		}
		try{
			sql = "delete from Temp_InDPDA;";
			db.execSQL(sql);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DB delete", " Temp_InDPDA 데이타를 삭제하지 못했습니다.");
		}
		try{
			sql = "delete from Temp_OrDPDA;";
			db.execSQL(sql);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DB delete", " Temp_OrDPDA 데이타를 삭제하지 못했습니다.");
		}
		try{
			sql = "delete from Temp_StDPDA;";
			db.execSQL(sql);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DB delete", " Temp_StDPDA 데이타를 삭제하지 못했습니다.");
		}
		try{
			sql = "delete from Admin_User;";
			db.execSQL(sql);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DB delete", " Admin_User 데이타를 삭제하지 못했습니다.");
		}
		try{
			sql = "delete from Temp_EvtPDA;";
			db.execSQL(sql);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DB delete", " Temp_EvtPDA 삭제하지 못했습니다.");
		}
		try{
			sql = "delete from BaPrint_History;";
			db.execSQL(sql);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DB delete", " BaPrint_History 삭제하지 못했습니다.");
		}

		try{
			//바코드프린터 셋팅
			sql = "delete from BarPrint_Setting;";
			db.execSQL(sql);
		}catch(SQLException e){
			e.printStackTrace();
			Log.d("DB delete", "BarPrint_Setting 삭제하지 못했습니다.");
		}

		Log.d("DB delete All", "데이타 삭제완료 ");
		*/
    }

    //갱신위해 전체 삭제
    public void dropTable_all() {
        String sql = "";

        db = getSQLiteDatabase("write");

        String exQuery = "";
        Log.i("DB update ", "Update start");
        db.beginTransaction();
        try {

            //exQuery = readText("tips_DBTableDrop.txt");
            //db.execSQL(exQuery);

            sql = "DROP TABLE IF EXISTS Goods;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS L_Branch;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS M_Branch;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS S_Branch;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS Office_Manage;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS Temp_InDPDA;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS Temp_OrDPDA;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS Temp_StDPDA;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS Admin_User;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS Temp_EvtPDA;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS BaPrint_History;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            // 2022.11.09. 바코드프린터 임시테이블 추가
            sql = "DROP TABLE IF EXISTS BaPrint_Temp;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            sql = "DROP TABLE IF EXISTS BaPrint_SPPL3000;";
            Log.d(TAG + "드랍테이블", sql);
            db.execSQL(sql);

            sql = "CREATE TABLE Goods ("
                    + "BarCode TEXT PRIMARY KEY, G_Name TEXT, Tax_YN TEXT, Std_Size TEXT, Pur_Pri TEXT, Pur_Cost TEXT, Add_Tax TEXT,"
                    + "RePur_Pri TEXT, Sell_Pri TEXT, ReSell_Pri TEXT, Sell_Pri1 TEXT, Profit_Rate TEXT, Bus_Code TEXT, Bus_Name TEXT, "
                    + "Pur_Use TEXT, Ord_Use TEXT, Sell_Use TEXT, Sto_Use TEXT, Box_Use TEXT, Pack_Use TEXT, Bulk_Use TEXT, Sale_Use TEXT,"
                    + "CusSale_Use TEXT, Scale_Use TEXT, Goods_Use TEXT, Real_Sto TEXT, Sale_Pur TEXT, Sale_Sell TEXT, Re_Pri TEXT, Sale_SDate TEXT,"
                    + "Sale_EDate TEXT, VAT_CHK TEXT, G_grade TEXT ); ";
            Log.d(TAG, "Create Table Goods : " + sql);
            db.execSQL(sql);

            sql = "CREATE TABLE L_Branch("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, L_Code TEXT, L_Name TEXT );";
            Log.d(TAG, "Create Table L_Branch : " + sql);
            db.execSQL(sql);

            sql = "CREATE TABLE M_Branch ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, L_Code TEXT, L_Name TEXT, M_Code TEXT, M_Name TEXT );";
            Log.d(TAG, "Create Table M_Branch : " + sql);
            db.execSQL(sql);

            sql = "CREATE TABLE S_Branch ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, L_Code TEXT, L_Name TEXT, M_Code TEXT, M_Name TEXT, S_Code TEXT, S_Name TEXT );";
            Log.d(TAG, "Create Table S_Branch : " + sql);
            db.execSQL(sql);

            sql = "CREATE TABLE Office_Manage ("
                    + "Office_Code TEXT PRIMARY KEY, Office_Name TEXT, Office_Sec TEXT, Office_Use TEXT, Office_Num TEXT, Owner_Name TEXT, Vat_Use TEXT,"
                    + "Office_Tel1 TEXT, Office_Tel2 TEXT, Office_Mobile1 TEXT, Office_Mobile2 TEXT );";
            Log.d(TAG, "Create Table Office_Manage" + sql);
            db.execSQL(sql);

            sql = "CREATE TABLE Admin_User("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, User_ID TEXT, User_PWD TEXT, User_Name TEXT, Gubun TEXT, Admin_Gubun TEXT, PDA_Gubun TEXT,"
                    + "Jumin TEXT, Job_Position TEXT, PayMent_Gubun TEXT, Salary TEXT, Grant_Pay TEXT, Dec_Grant TEXT, Tel1 TEXT, Tel2 TEXT, Zip_Code TEXT, Address1 TEXT,"
                    + "Address2 TEXT, Bigo TEXT, Enter_Date TEXT, Retire_Date TEXT, S_Date TEXT, SPay_Date TEXT, Grant_User TEXT, Grant_User1 TEXT, Write_Date TEXT, Edit_Date TEXT,"
                    + "Writer TEXT, Editor TEXT, APP_USER_GRADE TEXT, OFFICE_CODE TEXT, SALEMENU_PWD TEXT, SALEMENU_PWD_USE TEXT );";
            Log.d(TAG, "Create Table Admin_User" + sql);
            db.execSQL(sql);

            sql = "CREATE TABLE Temp_InDPDA ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, In_Num TEXT, In_BarCode TEXT, In_YN TEXT, In_Gubun TEXT,"
                    + "In_Date TEXT, BarCode TEXT, In_Seq TEXT, Office_Code TEXT, Office_Name TEXT, Tax_YN TEXT, Tax_Gubun TEXT, In_Count TEXT, Pur_Pri TEXT,"
                    + "Org_PurPri TEXT, Pur_DCRate TEXT, Pur_PriDC TEXT, Pur_Cost TEXT, Add_Tax TEXT, TPur_Pri TEXT, TPur_Cost TEXT, TAdd_Tax TEXT, In_Pri TEXT,"
                    + "Sell_Pri TEXT, Org_SellPri TEXT, TSell_Pri TEXT, In_SellPri TEXT, Profit_Pri TEXT, Profit_Rate TEXT, Bot_Code TEXT, Bot_Name TEXT, Bot_Pri TEXT,"
                    + "Bot_SellPri TEXT, P_BarCode TEXT, Box_Use TEXT, Pack_Use TEXT, Summarize TEXT, Write_Date TEXT, Edit_Date TEXT, Writer TEXT, Editor TEXT,"
                    + "Scale_Use TEXT, BarCode1 TEXT, Obtain TEXT, Chk_Cust TEXT, Chk_Pur TEXT, Chk_Sell TEXT );";
            Log.d(TAG, "Create Table Temp_InDPDA" + sql);
            db.execSQL(sql);

            sql = "CREATE TABLE Temp_OrDPDA ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, Or_Num TEXT, Or_BarCode TEXT, In_YN TEXT, Or_Gubun TEXT, Or_Date TEXT, In_Date TEXT,"
                    + "BarCode TEXT, Or_Seq TEXT, Office_Code TEXT, Office_Name TEXT, Tax_YN TEXT, Tax_Gubun TEXT, In_Count TEXT, Pro_Sto TEXT, Real_Sto TEXT,"
                    + "Pur_Pri TEXT, Org_PurPri TEXT, Pur_DCRate TEXT, Pur_PriDC TEXT, Pur_Cost TEXT, Add_Tax TEXT, TPur_Pri TEXT, TPur_Cost TEXT, TAdd_Tax TEXT,"
                    + "In_Pri TEXT, Sell_Pri TEXT, Org_SellPri TEXT, TSell_Pri TEXT, In_SellPri TEXT, Profit_Pri TEXT, Profit_Rate TEXT, Bot_Code TEXT,  Bot_Name TEXT,"
                    + "Bot_Pri TEXT, Bot_SellPri TEXT, Summarize TEXT, Write_Date TEXT, Edit_Date TEXT, Writer TEXT, Editor TEXT, Chk_Cust TEXT, Chk_Pur TEXT,"
                    + "Box_Use TEXT, BarCode1 TEXT, Scale_Use TEXT, Obtain TEXT );";
            Log.d(TAG, "Create Table Temp_OrDPDA" + sql);
            db.execSQL(sql);

            sql = "CREATE TABLE Temp_StDPDA ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, St_Num TEXT, St_BarCode TEXT, St_Gubun TEXT, St_Date TEXT, Office_Code TEXT, Office_Name TEXT,"
                    + "BarCode TEXT, St_Seq TEXT, St_Count TEXT, Tax_YN TEXT, Tax_Gubun TEXT, Bot_Code TEXT, Bot_Pur TEXT, Bot_Sell TEXT, Pur_Pri TEXT, Pur_Cost TEXT,"
                    + "Add_Tax TEXT, TPur_Pri TEXT, TPur_Cost TEXT, TAdd_Tax TEXT, In_Pri TEXT, Sell_Pri TEXT, TSell_Pri TEXT, In_SellPri TEXT, Bot_Pri TEXT, Bot_SellPri TEXT,"
                    + "Write_Date TEXT, Edit_Date TEXT, Writer TEXT, Editor TEXT, oReal_STO TEXT );";
            Log.d(TAG, "Create Table Temp_StDPDA" + sql);
            db.execSQL(sql);

            sql = "CREATE TABLE Temp_EvtPDA ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, Evt_CD TEXT, Evt_Name TEXT, Evt_Gubun TEXT, BarCode TEXT, Evt_SDate TEXT, Evt_EDate TEXT, Evt_STime TEXT,"
                    + "Evt_ETime TEXT, DC_Pri TEXT, DC_Rate TEXT, Profit_Rate TEXT, Profit_Pri TEXT, UP_Limit TEXT, DN_Limit TEXT, Bigo TEXT, Write_Date TEXT, Edit_Date TEXT, Writer TEXT,"
                    + "Editor TEXT,Sale_Pur TEXT, Sale_Sell TEXT, INEvt_SDate TEXT, INEvt_EDate TEXT, Sell_YN TEXT, G_Name TEXT );";
            Log.d(TAG, "Create Table Temp_EvtPDA" + sql);
            db.execSQL(sql);

            sql = "CREATE TABLE BaPrint_History ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, BaPrint_Num TEXT, BarCode TEXT, G_Name TEXT, Std_Size TEXT, Count TEXT, Sell_Pri TEXT, Bus_Name TEXT,"
                    + "Con_Rate TEXT, Unit TEXT, Std_Rate TEXT, OrgSell_Pri TEXT, Print_Date TEXT, Print_DateTime TEXT, Office_Code TEXT );";
            Log.d(TAG, "Create Table BaPrint_History" + sql);
            db.execSQL(sql);

            // 2022.11.09. 바코드프린터 임시테이블 추가
            sql = "CREATE TABLE Temp_BaPrint ("
                    + "_id INTEGER PRIMARY KEY AUTOINCREMENT, BarCode TEXT, G_Name TEXT, Std_Size TEXT, Count TEXT, Sell_Pri TEXT, Bus_Name TEXT,"
                    + "Con_Rate TEXT, Unit TEXT, Std_Rate TEXT, OrgSell_Pri TEXT, Print_Date TEXT, Print_DateTime TEXT, Office_Code TEXT );";
            Log.d(TAG, "Create Table Temp_BaPrint" + sql);
            db.execSQL(sql);

            //----------------------------------------//
            // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
            //----------------------------------------//
			/*
			sql = "CREATE TABLE BaPrint_SPPL3000 ("
					+"_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Print_Size TEXT, Lavel_Hight INTEGER, Lavel_Width INTEGER, Print_Direction INTEGER, Paper_Gubun INTEGER,"
					+"Gap_Width INTEGER, Print_StdSize_YN INTEGER, Print_Price_YN INTEGER, Print_Office_YN INTEGER, Print_Danga_YN INTEGER, Word_Length INTEGER,"
					+"Goods_Setting TEXT, Stdsize_Setting TEXT, Price_Setting TEXT,"
					+"Office_Setting TEXT , Danga_Setting TEXT, Barcode_Setting TEXT);";
			db.execSQL(sql);

			sql = "insert into BaPrint_SPPL3000(Print_Size, Lavel_Hight, Lavel_Width, Print_Direction, Paper_Gubun, Gap_Width, Print_StdSize_YN,"
					+"Print_Price_YN, Print_Office_YN, Print_Danga_YN, Word_Length, Goods_Setting, Stdsize_Setting,"
					+"Price_Setting, Office_Setting, Danga_Setting, Barcode_Setting)"
					+"values('30*58', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1'),"
					+"('23*40', '23', '40', '0', '0', '3', '1', '1', '1', '1', '30', '150|10|0|0|1|0|0', '350|60|0|0|0|1|0', '350|130|0|1|1|0|0', '320|110|0|0|0|0|0', '350|80|0|0|0|1|0', '155|50|7|2|6|40|0|1'),"
					+"('사용자정의1', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1'),"
					+"('사용자정의2', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1'),"
					+"('사용자정의3', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1');";
			db.execSQL(sql);
		    */
            //----------------------------------------//
            sql = "CREATE TABLE BaPrint_SPPL3000 ("
                    + "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Print_Size TEXT, Lavel_Hight INTEGER, Lavel_Width INTEGER, Print_Direction INTEGER, Paper_Gubun INTEGER,"
                    + "Gap_Width INTEGER, Print_StdSize_YN INTEGER, Print_Price_YN INTEGER, Print_Office_YN INTEGER, Print_Danga_YN INTEGER, Word_Length INTEGER,"
                    + "Goods_Setting TEXT, Stdsize_Setting TEXT, Price_Setting TEXT,"
                    + "Office_Setting TEXT , Danga_Setting TEXT, Barcode_Setting TEXT,"
                    + "Print_SellPrice_YN TEXT, Print_SaleSellRate_YN TEXT, SellPrice_Setting TEXT, SaleSellRate_Setting TEXT," // 원판매가,할인율,인쇄구분 추가
                    + "Print_Location_YN TEXT, Print_NickName_YN TEXT,Print_BranchName_YN TEXT, Print_AddItem_YN TEXT," // 위치,품번,분류,추가항목 추가
                    + "Location_Setting TEXT, NickName_Setting TEXT,BranchName_Setting TEXT, AddItem_Setting TEXT);"; // 위치,품번,분류,추가항목 추가
            db.execSQL(sql);

            sql = "insert into BaPrint_SPPL3000(Print_Size, Lavel_Hight, Lavel_Width, Print_Direction, Paper_Gubun, Gap_Width, Print_StdSize_YN,"
                    + "Print_Price_YN, Print_Office_YN, Print_Danga_YN, Word_Length, Goods_Setting, Stdsize_Setting,"
                    + "Price_Setting, Office_Setting, Danga_Setting, Barcode_Setting,"
                    + "Print_SellPrice_YN, Print_SaleSellRate_YN, SellPrice_Setting, SaleSellRate_Setting,"
                    + "Print_Location_YN, Print_NickName_YN,Print_BranchName_YN, Print_AddItem_YN,"
                    + "Location_Setting, NickName_Setting, BranchName_Setting, AddItem_Setting)"
                    + "values('30*58', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0','1','1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0','400|160|1|2|2|0|0','400|160|1|2|2|0|0'),"
                    + "('23*40', '23', '40', '0', '0', '3', '1', '1', '1', '1', '30', '150|10|0|0|1|0|0', '350|60|0|0|0|1|0', '350|130|0|1|1|0|0', '320|110|0|0|0|0|0', '350|80|0|0|0|1|0', '155|50|7|2|6|40|0|1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0','1','1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0','400|160|1|2|2|0|0','400|160|1|2|2|0|0'),"
                    + "('사용자정의1', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0','1','1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0','400|160|1|2|2|0|0','400|160|1|2|2|0|0'),"
                    + "('사용자정의2', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0','1','1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0','400|160|1|2|2|0|0','400|160|1|2|2|0|0'),"
                    + "('사용자정의3', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0','1','1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0','400|160|1|2|2|0|0','400|160|1|2|2|0|0');";
            db.execSQL(sql);
            //----------------------------------------//

            db.setTransactionSuccessful();
            Log.i(TAG, "DB Drop And Creat Successful");

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.i(TAG, "DB Drop And Creat Faild");

        } finally {
            db.endTransaction();
        }
    }

    // 2022.11.10. 김영목. 테이블 유무 체크
//    public void addTableExists(String tableName){
//
//        String sql = "";
//        boolean isExists = false;
//
//        db = getSQLiteDatabase("write");
//
//        String exQuery = "";
//        String query = "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'";
//        try (Cursor cursor = db.rawQuery(query, null)) {
//            if(cursor!=null) {
//                if(cursor.getCount()>0) {
//                    return true;
//                }
//            }
//            return false;
//        }
//
//    }

    private void addTables(String tableName){

        String sql = "";

        db = getSQLiteDatabase("write");
        db.beginTransaction();

        try{
            sql = makeTableCreateSql(tableName);
            db.execSQL(sql);
            db.setTransactionSuccessful();
            Log.i(TAG, tableName + " Table Creat Successful");
        }catch (SQLException e){
            e.printStackTrace();
            Log.i(TAG, tableName + " Table Creat Faild");
        } finally {
            db.endTransaction();
        }
    }

    private String makeTableCreateSql(String tableName ){

        String sql="";

        switch (tableName){
            case "Temp_BaPrint":
                // 2022.11.09. 바코드프린터 임시테이블 추가
                sql += "CREATE TABLE IF NOT EXISTS Temp_BaPrint (";
                sql += "_id INTEGER PRIMARY KEY AUTOINCREMENT";
                sql += ",BarCode TEXT";
                sql += ",G_Name TEXT";
                sql += ",Std_Size TEXT";
                sql += ",Count TEXT";
                sql += ",Sell_Pri TEXT";
                sql += ",Bus_Name TEXT";
                sql += ",Con_Rate TEXT";
                sql += ",Unit TEXT";
                sql += ",Std_Rate TEXT";
                sql += ",OrgSell_Pri TEXT";
                sql += ",Print_Date TEXT";
                sql += ",Print_DateTime TEXT";
                sql += ",Office_Code TEXT );";

                break;

            default:
                break;
        }
        return sql;
    }

    // 2021.01.05. 김영목. 테이블 필드 추가 업데이트
    private void alterTable_BaPrint() {
        String sql = "";
        boolean isExists = false;

        db = getSQLiteDatabase("write");

        String exQuery = "";
        Log.i("DB update ", "Update start");

        db.beginTransaction();
        try {

            //----------------------------------------//
            // 원판매가 인쇄유무
            //----------------------------------------//
            doColumnAlter("BaPrint_SPPL3000", "Print_SellPrice_YN", "TEXT", "1");

//			sql = "ALTER TABLE BaPrint_SPPL3000 ADD Print_SellPrice_YN TEXT";
//			db.execSQL(sql);
//
//			sql = "UPDATE BaPrint_SPPL3000 SET Print_SellPrice_YN = '1'";
//			db.execSQL(sql);
            //----------------------------------------//

            //----------------------------------------//
            // 할인율(%) 인쇄유무
            //----------------------------------------//
//			sql = "ALTER TABLE BaPrint_SPPL3000 ADD Print_SaleSellRate_YN TEXT";
//			db.execSQL(sql);
//
//			sql = "UPDATE BaPrint_SPPL3000 SET Print_SaleSellRate_YN = '1'";
//			db.execSQL(sql);
            //----------------------------------------//
            doColumnAlter("BaPrint_SPPL3000", "Print_SaleSellRate_YN", "TEXT", "1");

            //----------------------------------------//
            // 원판매가 정보
            //----------------------------------------//
//			sql = "ALTER TABLE BaPrint_SPPL3000 ADD SellPrice_Setting TEXT";
//			db.execSQL(sql);
//
//			sql = "UPDATE BaPrint_SPPL3000 SET SellPrice_Setting = '400|160|1|2|2|0|0'";
//			db.execSQL(sql);
            //----------------------------------------//
            doColumnAlter("BaPrint_SPPL3000", "SellPrice_Setting", "TEXT", "400|160|1|2|2|0|0");

            //----------------------------------------//
            // 할인율(%) 정보
            //----------------------------------------//
            doColumnAlter("BaPrint_SPPL3000", "SaleSellRate_Setting", "TEXT", "400|160|1|2|2|0|0");
//			sql = "ALTER TABLE BaPrint_SPPL3000 ADD SaleSellRate_Setting TEXT";
//			db.execSQL(sql);
//
//			sql = "UPDATE BaPrint_SPPL3000 SET SaleSellRate_Setting = '400|160|1|2|2|0|0'";
//			db.execSQL(sql);
            //----------------------------------------//


            //----------------------------------------//
            // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
            //----------------------------------------//
            // 위치,품번,분류,추가항목
            // 인쇄유무
            doColumnAlter("BaPrint_SPPL3000", "Print_Location_YN", "TEXT", "1");
            doColumnAlter("BaPrint_SPPL3000", "Print_NickName_YN", "TEXT", "1");
            doColumnAlter("BaPrint_SPPL3000", "Print_BranchName_YN", "TEXT", "1");
            doColumnAlter("BaPrint_SPPL3000", "Print_AddItem_YN", "TEXT", "1");
            // 정보
            doColumnAlter("BaPrint_SPPL3000", "Location_Setting", "TEXT", "400|160|1|2|2|0|0");
            doColumnAlter("BaPrint_SPPL3000", "NickName_Setting", "TEXT", "400|160|1|2|2|0|0");
            doColumnAlter("BaPrint_SPPL3000", "BranchName_Setting", "TEXT", "400|160|1|2|2|0|0");
            doColumnAlter("BaPrint_SPPL3000", "AddItem_Setting", "TEXT", "400|160|1|2|2|0|0");
            //----------------------------------------//

            db.setTransactionSuccessful();
            Log.i(TAG, "DB Alter Table Successful");

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Log.i(TAG, "DB Alter Table Faild");

        } finally {
            db.endTransaction();
        }

    }

    // 테이블 컬럼 존재 여부 처리(2021.05.04.김영목)
    public boolean isColumnExists(String table, String column) {
        boolean isExists = false;
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    if (column.equalsIgnoreCase(name)) {
                        isExists = true;
                        break;
                    }
                }
            }
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }
        return isExists;
    }

    public void doColumnAlter(String table, String column, String type, String defaultValue) {

        boolean isExists = false;
        Cursor cursor = null;
        String sql;
        try {
            cursor = db.rawQuery("PRAGMA table_info(" + table + ")", null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    if (column.equalsIgnoreCase(name)) {
                        isExists = true;
                        break;
                    }
                }
            }
        } finally {
            if (cursor != null && !cursor.isClosed())
                cursor.close();
        }

        // 없으면 추가한다
        if (isExists == false) {
            sql = "ALTER TABLE " + table + " ADD " + column + " " + type;
            if (defaultValue != "") {
                sql += " DEFAULT '" + defaultValue + "' ";
            }
            try {
                db.execSQL(sql);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    //sqlitedatabase 내보내기
    public SQLiteDatabase getSQLiteDatabase(String gubun) {
        if (gubun.equals("read")) {
            db = tipssql.getReadableDatabase();
        } else {
            db = tipssql.getWritableDatabase();
        }
        return db;
    }

    // Assets 파일 불러오기
    private String readText(String file) throws IOException {
        InputStream is = context.getAssets().open(file);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        String text = new String(buffer, "UTF-8");

        return text;
		/*

		InputStream is = context.getAssets().open(file);
		ByteArrayOutputStream byteArrayBuffe = new ByteArrayOutputStream();

		int readVal= 0;
		while(readVal != -1){
			readVal = is.read();
			if(readVal != -1){
				byteArrayBuffe.write(readVal);
			}
		}

		is.close();

		String text = new String(byteArrayBuffe.toByteArray());
		return text;

		InputStream is = context.getAssets().open(file);
		StringBuffer str = null;

		int size = is.available();
		byte[] buffer = new byte[size];
		str = new StringBuffer();
		while(is.read(buffer) != -1){
			str.append(buffer);
		}

		Log.d(TAG, str.toString());
		is.close();

		return str.toString();*/
    }

    /**
     * 파일 생성
     * @param dir
     * @return file
     */
    /*private File makeFile(String file_path){
        File file = null;
        boolean isSuccess = false;
            file = new File(file_path);
            if(file!=null&&!file.exists()){
                Log.i( TAG , "!file.exists" );
                try {
                    isSuccess = file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally{
                    Log.i(TAG, "파일생성 여부 = " + isSuccess);
                }
            }else{
                Log.i( TAG , "file.exists" );
            }
        return file;
    }

    *//**
     * (dir/file) 절대 경로 얻어오기
     * @param file
     * @return String
     *//*
    private String getAbsolutePath(File file){
        return ""+file.getAbsolutePath();
    }
 
    *//**
     * (dir/file) 삭제 하기
     * @param file
     *//*
    private boolean deleteFile(File file){
        boolean result;
        if(file!=null&&file.exists()){
            file.delete();
            result = true;
        }else{
            result = false;
        }
        return result;
    }
 
    *//**
     * 파일여부 체크 하기
     * @param file
     * @return
     *//*
    private boolean isFile(File file){
        boolean result;
        if(file!=null&&file.exists()&&file.isFile()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    
    *//**
     * 파일 존재 여부 확인 하기
     * @param file
     * @return
     *//*
    private boolean isFileExist(File file){
        boolean result;
        if(file!=null&&file.exists()){
            result=true;
        }else{
            result=false;
        }
        return result;
    }

    *//**
     * 파일에 내용 쓰기
     * @param file
     * @param file_content
     * @return
     *//*
    private boolean writeFile(File file , byte[] file_content){
        boolean result;
        FileOutputStream fos;
        if(file!=null&&file.exists()&&file_content!=null){
            try {
                fos = new FileOutputStream(file);
                try {
                    fos.write(file_content);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            result = true;
        }else{
            result = false;
        }
        return result;
    }
 
    *//**
     * 파일 읽어 오기 
     * @param file
     *//*
    private void readFile(File file){
        int readcount=0;
        if(file!=null&&file.exists()){
            try {
                FileInputStream fis = new FileInputStream(file);
                readcount = (int)file.length();
                byte[] buffer = new byte[readcount];
                fis.read(buffer);
                for(int i=0 ; i<file.length();i++){
                    Log.d(TAG, ""+buffer[i]);
                }
                fis.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }*/

}
