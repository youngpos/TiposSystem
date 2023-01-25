package tipsystem.utils;

import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class TipsSQLitHelper extends SQLiteOpenHelper {

	private static final String TAG="TipSQLitHelper";

	private Context context;
	
	//context, db_name, factory, version
	public TipsSQLitHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
		this.context = context;
	}


	/**
	 * Database가 존재하지 않을 때, 딱 한번 실행된다.
	 * DB를 만드는 역할을 한다.
	 * @param db
	 *
	 */
	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String exQuery = "";
		Log.i("DB Table 생성시작", "생성시작");
		db.beginTransaction();
		try {
			//DB를 생성합니다.
			exQuery = "CREATE TABLE Goods ("
					+"BarCode TEXT PRIMARY KEY, G_Name TEXT, Tax_YN TEXT, Std_Size TEXT, Pur_Pri TEXT, Pur_Cost TEXT, Add_Tax TEXT,"
					+"RePur_Pri TEXT, Sell_Pri TEXT, ReSell_Pri TEXT, Sell_Pri1 TEXT, Profit_Rate TEXT, Bus_Code TEXT, Bus_Name TEXT, "
					+"Pur_Use TEXT, Ord_Use TEXT, Sell_Use TEXT, Sto_Use TEXT, Box_Use TEXT, Pack_Use TEXT, Bulk_Use TEXT, Sale_Use TEXT,"
					+"CusSale_Use TEXT, Scale_Use TEXT, Goods_Use TEXT, Real_Sto TEXT, Sale_Pur TEXT, Sale_Sell TEXT, Re_Pri TEXT, Sale_SDate TEXT,"
					+"Sale_EDate TEXT, VAT_CHK TEXT, G_grade TEXT ); ";
			Log.d(TAG, "Create Table Goods : " + exQuery);
			db.execSQL(exQuery);

			exQuery = "CREATE TABLE L_Branch("
					+"_id INTEGER PRIMARY KEY AUTOINCREMENT, L_Code TEXT, L_Name TEXT );";
			Log.d(TAG, "Create Table L_Branch : " + exQuery);
			db.execSQL(exQuery);

			exQuery = "CREATE TABLE M_Branch ("
					+"_id INTEGER PRIMARY KEY AUTOINCREMENT, L_Code TEXT, L_Name TEXT, M_Code TEXT, M_Name TEXT );";
			Log.d(TAG, "Create Table M_Branch : " + exQuery);
			db.execSQL(exQuery);

			exQuery = "CREATE TABLE S_Branch ("
					+"_id INTEGER PRIMARY KEY AUTOINCREMENT, L_Code TEXT, L_Name TEXT, M_Code TEXT, M_Name TEXT, S_Code TEXT, S_Name TEXT );";
			Log.d(TAG, "Create Table S_Branch : " + exQuery);
			db.execSQL(exQuery);

			exQuery = "CREATE TABLE Office_Manage ("
					+"Office_Code TEXT PRIMARY KEY, Office_Name TEXT, Office_Sec TEXT, Office_Use TEXT, Office_Num TEXT, Owner_Name TEXT, Vat_Use TEXT,"
					+"Office_Tel1 TEXT, Office_Tel2 TEXT, Office_Mobile1 TEXT, Office_Mobile2 TEXT );";
			Log.d(TAG, "Create Table Office_Manage" + exQuery);
			db.execSQL(exQuery);

			exQuery = "CREATE TABLE Admin_User("
					+"_id INTEGER PRIMARY KEY AUTOINCREMENT, User_ID TEXT, User_PWD TEXT, User_Name TEXT, Gubun TEXT, Admin_Gubun TEXT, PDA_Gubun TEXT,"
					+"Jumin TEXT, Job_Position TEXT, PayMent_Gubun TEXT, Salary TEXT, Grant_Pay TEXT, Dec_Grant TEXT, Tel1 TEXT, Tel2 TEXT, Zip_Code TEXT, Address1 TEXT,"
					+"Address2 TEXT, Bigo TEXT, Enter_Date TEXT, Retire_Date TEXT, S_Date TEXT, SPay_Date TEXT, Grant_User TEXT, Grant_User1 TEXT, Write_Date TEXT, Edit_Date TEXT,"
					+"Writer TEXT, Editor TEXT, APP_USER_GRADE TEXT, OFFICE_CODE TEXT, SALEMENU_PWD TEXT, SALEMENU_PWD_USE TEXT );";
			Log.d(TAG, "Create Table Admin_User" + exQuery);
			db.execSQL(exQuery);

			exQuery = "CREATE TABLE Temp_InDPDA ("
					+"_id INTEGER PRIMARY KEY AUTOINCREMENT, In_Num TEXT, In_BarCode TEXT, In_YN TEXT, In_Gubun TEXT,"
					+"In_Date TEXT, BarCode TEXT, In_Seq TEXT, Office_Code TEXT, Office_Name TEXT, Tax_YN TEXT, Tax_Gubun TEXT, In_Count TEXT, Pur_Pri TEXT,"
					+"Org_PurPri TEXT, Pur_DCRate TEXT, Pur_PriDC TEXT, Pur_Cost TEXT, Add_Tax TEXT, TPur_Pri TEXT, TPur_Cost TEXT, TAdd_Tax TEXT, In_Pri TEXT,"
					+"Sell_Pri TEXT, Org_SellPri TEXT, TSell_Pri TEXT, In_SellPri TEXT, Profit_Pri TEXT, Profit_Rate TEXT, Bot_Code TEXT, Bot_Name TEXT, Bot_Pri TEXT,"
					+"Bot_SellPri TEXT, P_BarCode TEXT, Box_Use TEXT, Pack_Use TEXT, Summarize TEXT, Write_Date TEXT, Edit_Date TEXT, Writer TEXT, Editor TEXT,"
					+"Scale_Use TEXT, BarCode1 TEXT, Obtain TEXT, Chk_Cust TEXT, Chk_Pur TEXT, Chk_Sell TEXT );";
			Log.d(TAG, "Create Table Temp_InDPDA" + exQuery);
			db.execSQL(exQuery);

			exQuery = "CREATE TABLE Temp_OrDPDA ("
					+"_id INTEGER PRIMARY KEY AUTOINCREMENT, Or_Num TEXT, Or_BarCode TEXT, In_YN TEXT, Or_Gubun TEXT, Or_Date TEXT, In_Date TEXT,"
					+"BarCode TEXT, Or_Seq TEXT, Office_Code TEXT, Office_Name TEXT, Tax_YN TEXT, Tax_Gubun TEXT, In_Count TEXT, Pro_Sto TEXT, Real_Sto TEXT,"
					+"Pur_Pri TEXT, Org_PurPri TEXT, Pur_DCRate TEXT, Pur_PriDC TEXT, Pur_Cost TEXT, Add_Tax TEXT, TPur_Pri TEXT, TPur_Cost TEXT, TAdd_Tax TEXT,"
					+"In_Pri TEXT, Sell_Pri TEXT, Org_SellPri TEXT, TSell_Pri TEXT, In_SellPri TEXT, Profit_Pri TEXT, Profit_Rate TEXT, Bot_Code TEXT,  Bot_Name TEXT,"
					+"Bot_Pri TEXT, Bot_SellPri TEXT, Summarize TEXT, Write_Date TEXT, Edit_Date TEXT, Writer TEXT, Editor TEXT, Chk_Cust TEXT, Chk_Pur TEXT,"
					+"Box_Use TEXT, BarCode1 TEXT, Scale_Use TEXT, Obtain TEXT );";
			Log.d(TAG, "Create Table Temp_OrDPDA" + exQuery);
			db.execSQL(exQuery);

			exQuery = "CREATE TABLE Temp_StDPDA ("
					+"_id INTEGER PRIMARY KEY AUTOINCREMENT, St_Num TEXT, St_BarCode TEXT, St_Gubun TEXT, St_Date TEXT, Office_Code TEXT, Office_Name TEXT,"
					+"BarCode TEXT, St_Seq TEXT, St_Count TEXT, Tax_YN TEXT, Tax_Gubun TEXT, Bot_Code TEXT, Bot_Pur TEXT, Bot_Sell TEXT, Pur_Pri TEXT, Pur_Cost TEXT,"
					+"Add_Tax TEXT, TPur_Pri TEXT, TPur_Cost TEXT, TAdd_Tax TEXT, In_Pri TEXT, Sell_Pri TEXT, TSell_Pri TEXT, In_SellPri TEXT, Bot_Pri TEXT, Bot_SellPri TEXT,"
					+"Write_Date TEXT, Edit_Date TEXT, Writer TEXT, Editor TEXT, oReal_STO TEXT );";
			Log.d(TAG, "Create Table Temp_StDPDA" + exQuery);
			db.execSQL(exQuery);

			exQuery = "CREATE TABLE Temp_EvtPDA ("
					+"_id INTEGER PRIMARY KEY AUTOINCREMENT, Evt_CD TEXT, Evt_Name TEXT, Evt_Gubun TEXT, BarCode TEXT, Evt_SDate TEXT, Evt_EDate TEXT, Evt_STime TEXT,"
					+"Evt_ETime TEXT, DC_Pri TEXT, DC_Rate TEXT, Profit_Rate TEXT, Profit_Pri TEXT, UP_Limit TEXT, DN_Limit TEXT, Bigo TEXT, Write_Date TEXT, Edit_Date TEXT, Writer TEXT,"
					+"Editor TEXT,Sale_Pur TEXT, Sale_Sell TEXT, INEvt_SDate TEXT, INEvt_EDate TEXT, Sell_YN TEXT, G_Name TEXT );";
			Log.d(TAG, "Create Table Temp_EvtPDA" + exQuery);
			db.execSQL(exQuery);

			exQuery = "CREATE TABLE BaPrint_History ("
					+"_id INTEGER PRIMARY KEY AUTOINCREMENT, BaPrint_Num TEXT, BarCode TEXT, G_Name TEXT, Std_Size TEXT, Count TEXT, Sell_Pri TEXT, Bus_Name TEXT,"
					+"Con_Rate TEXT, Unit TEXT, Std_Rate TEXT, OrgSell_Pri TEXT, Print_Date TEXT, Print_DateTime TEXT, Office_Code TEXT );";
			Log.d(TAG, "Create Table BaPrint_History" + exQuery);
			db.execSQL(exQuery);

			//----------------------------------------//
			// 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
			//----------------------------------------//
			/*
			exQuery = "CREATE TABLE BaPrint_SPPL3000 ("
					+"_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Print_Size TEXT, Lavel_Hight INTEGER, Lavel_Width INTEGER, Print_Direction INTEGER, Paper_Gubun INTEGER,"
					+"Gap_Width INTEGER, Print_StdSize_YN INTEGER, Print_Price_YN INTEGER, Print_Office_YN INTEGER, Print_Danga_YN INTEGER, Word_Length INTEGER,"
					+"Goods_Setting TEXT, Stdsize_Setting TEXT, Price_Setting TEXT,"
					+"Office_Setting TEXT , Danga_Setting TEXT, Barcode_Setting TEXT);";
			db.execSQL(exQuery);

			exQuery = "insert into BaPrint_SPPL3000(Print_Size, Lavel_Hight, Lavel_Width, Print_Direction, Paper_Gubun, Gap_Width, Print_StdSize_YN,"
					+"Print_Price_YN, Print_Office_YN, Print_Danga_YN, Word_Length, Goods_Setting, Stdsize_Setting,"
					+"Price_Setting, Office_Setting, Danga_Setting, Barcode_Setting)"
					+"values('30*58', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1'),"
					+"('23*40', '23', '40', '0', '0', '3', '1', '1', '1', '1', '30', '150|10|0|0|1|0|0', '350|60|0|0|0|1|0', '350|130|0|1|1|0|0', '320|110|0|0|0|0|0', '350|80|0|0|0|1|0', '155|50|7|2|6|40|0|1'),"
					+"('사용자정의1', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1'),"
					+"('사용자정의2', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1'),"
					+"('사용자정의3', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1');";
			db.execSQL(exQuery);
		    */
			//----------------------------------------//
			exQuery = "CREATE TABLE BaPrint_SPPL3000 ("
					+"_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Print_Size TEXT, Lavel_Hight INTEGER, Lavel_Width INTEGER, Print_Direction INTEGER, Paper_Gubun INTEGER,"
					+"Gap_Width INTEGER, Print_StdSize_YN INTEGER, Print_Price_YN INTEGER, Print_Office_YN INTEGER, Print_Danga_YN INTEGER, Word_Length INTEGER,"
					+"Goods_Setting TEXT, Stdsize_Setting TEXT, Price_Setting TEXT,"
					+"Office_Setting TEXT , Danga_Setting TEXT, Barcode_Setting TEXT,"
					+"Print_SellPrice_YN TEXT, Print_SaleSellRate_YN TEXT, SellPrice_Setting TEXT, SaleSellRate_Setting TEXT);"; // 원판매가,할인율,인쇄구분 추가
			db.execSQL(exQuery);

			exQuery = "insert into BaPrint_SPPL3000(Print_Size, Lavel_Hight, Lavel_Width, Print_Direction, Paper_Gubun, Gap_Width, Print_StdSize_YN,"
					+"Print_Price_YN, Print_Office_YN, Print_Danga_YN, Word_Length, Goods_Setting, Stdsize_Setting,"
					+"Price_Setting, Office_Setting, Danga_Setting, Barcode_Setting,"
					+"Print_SellPrice_YN, Print_SaleSellRate_YN, SellPrice_Setting, SaleSellRate_Setting)"
					+"values('30*58', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0'),"
					+"('23*40', '23', '40', '0', '0', '3', '1', '1', '1', '1', '30', '150|10|0|0|1|0|0', '350|60|0|0|0|1|0', '350|130|0|1|1|0|0', '320|110|0|0|0|0|0', '350|80|0|0|0|1|0', '155|50|7|2|6|40|0|1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0'),"
					+"('사용자정의1', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0'),"
					+"('사용자정의2', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0'),"
					+"('사용자정의3', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1','1','1','400|160|1|2|2|0|0','400|160|1|2|2|0|0');";
			db.execSQL(exQuery);
			//----------------------------------------//

			db.setTransactionSuccessful();
			Log.i("DB Table 생성시작", "생성완료");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();			
			Log.i("DB Table 생성시작", "생성실패");
		}finally {
			db.endTransaction();
		}
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method smtub
		switch (oldVersion) {
			case 1:
				String exQuery = "";
				Log.i("DB update ", "Update start");
				db.beginTransaction();
				try {

					//DB를 업데이트 합니다.
                    exQuery = "ALTER TABLE BaPrint_History ADD Office_Code TEXT;";
                    db.execSQL(exQuery);

					exQuery = "CREATE TABLE BaPrint_SPPL3000 ("
							+"_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, Print_Size TEXT, Lavel_Hight INTEGER, Lavel_Width INTEGER, Print_Direction INTEGER, Paper_Gubun INTEGER,"
							+"Gap_Width INTEGER, Print_StdSize_YN INTEGER, Print_Price_YN INTEGER, Print_Office_YN INTEGER, Print_Danga_YN INTEGER, Word_Length INTEGER,"
							+"Goods_Setting TEXT, Stdsize_Setting TEXT, Price_Setting TEXT,"
							+"Office_Setting TEXT , Danga_Setting TEXT, Barcode_Setting TEXT);";
					db.execSQL(exQuery);

					exQuery = "insert into BaPrint_SPPL3000(Print_Size, Lavel_Hight, Lavel_Width, Print_Direction, Paper_Gubun, Gap_Width, Print_StdSize_YN,"
							+"Print_Price_YN, Print_Office_YN, Print_Danga_YN, Word_Length, Goods_Setting, Stdsize_Setting,"
							+"Price_Setting, Office_Setting, Danga_Setting, Barcode_Setting)"
							+"values('30*58', '30', '58', '0', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1'),"
							+"('23*40', '23', '40', '0', '0', '3', '1', '1', '1', '1', '30', '150|10|0|0|1|0|0', '350|60|0|0|0|1|0', '350|130|0|1|1|0|0', '320|110|0|0|0|0|0', '350|80|0|0|0|1|0', '155|50|7|2|6|40|0|1'),"
							+"('사용자정의1', '30', '58', '2', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1'),"
							+"('사용자정의2', '30', '58', '3', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1'),"
							+"('사용자정의3', '30', '58', '3', '0', '3', '1', '1', '1', '1', '30', '100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1');";
					db.execSQL(exQuery);

					db.setTransactionSuccessful();
					Log.i("DB Update 완료", "UPdate 완료");

				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.i("DB Update 완료", "UPdate 실패");
				}finally {
					db.endTransaction();
				}

				break;
		}
	}

	// Assets 파일 불러오기	
	private String readText(String file) throws IOException {		
		InputStream is = context.getAssets().open(file);
		int size = is.available();
		byte[] buffer = new byte[size];
		is.read(buffer);
		is.close();
		String text = new String(buffer,"UTF-8");
		return text;
	}
	
}
