package tipsystem.tips.models;

public class SPPL3000 {

    private int _idx;
    private String Print_Size;
    private int Lavel_Hight;
    private int Lavel_Width;
    private int Print_Direction;
    private int Paper_Gubun;
    private int Gap_Width;
    private int Print_StdSize_YN;
    private int Print_Price_YN;
    private int Print_Office_YN;
    private int Print_Danga_YN;
    private int Word_Length;
    private String Goods_Setting;
    private String Stdsize_Setting;
    private String Price_Setting;
    private String Office_Setting;
    private String Danga_Setting;
    private String Barcode_Setting;

    //----------------------------------------//
    // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
    //----------------------------------------//
    private int Print_SellPrice_YN;
    private int Print_SaleSellRate_YN;
    private String SellPrice_Setting;
    private String SaleSellRate_Setting;
    //----------------------------------------//
    //----------------------------------------//
    // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
    //----------------------------------------//
    private int Print_Location_YN;
    private int Print_NickName_YN;
    private int Print_BranchName_YN;
    private int Print_AddItem_YN;
    private String Location_Setting;
    private String NickName_Setting;
    private String BranchName_Setting;
    private String AddItem_Setting;
    //----------------------------------------//

    public String m_idx = "_id";
    public String m_Print_Size = "Print_Size";
    public String m_Lavel_Hight = "Lavel_Hight";
    public String m_Lavel_Width = "Lavel_Width";
    public String m_Print_Direction = "Print_Direction";
    public String m_Paper_Gubun = "Paper_Gubun";
    public String m_Gap_Width = "Gap_Width";
    public String m_Print_StdSize_YN = "Print_StdSize_YN";
    public String m_Print_Price_YN = "Print_Price_YN";
    public String m_Print_Office_YN = "Print_Office_YN";
    public String m_Print_Danga_YN = "Print_Danga_YN";
    public String m_Word_Length = "Word_Length";
    public String m_Goods_Setting = "Goods_Setting";
    public String m_Stdsize_Setting = "Stdsize_Setting";
    public String m_Price_Setting = "Price_Setting";
    public String m_Office_Setting = "Office_Setting";
    public String m_Danga_Setting = "Danga_Setting";
    public String m_Barcode_Setting = "Barcode_Setting";
    //----------------------------------------//
    // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
    //----------------------------------------//
    public String m_Print_SellPrice_YN = "Print_SellPrice_YN";
    public String m_Print_SaleSellRate_YN = "Print_SaleSellRate_YN";
    public String m_SellPrice_Setting = "SellPrice_Setting";
    public String m_SaleSellRate_Setting = "SaleSellRate_Setting";
    //----------------------------------------//

    //----------------------------------------//
    // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
    //----------------------------------------//
    public String m_Print_Location_YN = "Print_Location_YN";
    public String m_Print_NickName_YN = "Print_NickName_YN";
    public String m_Print_BranchName_YN = "Print_BranchName_YN";
    public String m_Print_AddItem_YN = "Print_AddItem_YN";
    public String m_Location_Setting = "Location_Setting";
    public String m_NickName_Setting = "NickName_Setting";
    public String m_BranchName_Setting = "BranchName_Setting";
    public String m_AddItem_Setting = "AddItem_Setting";

    public int getPrint_Location_YN() {
        return Print_Location_YN;
    }

    public void setPrint_Location_YN(int print_Location_YN) {
        Print_Location_YN = print_Location_YN;
    }

    public int getPrint_NickName_YN() {
        return Print_NickName_YN;
    }

    public void setPrint_NickName_YN(int print_NickName_YN) {
        Print_NickName_YN = print_NickName_YN;
    }

    public int getPrint_BranchName_YN() {
        return Print_BranchName_YN;
    }

    public void setPrint_BranchName_YN(int print_BranchName_YN) {
        Print_BranchName_YN = print_BranchName_YN;
    }

    public int getPrint_AddItem_YN() {
        return Print_AddItem_YN;
    }

    public void setPrint_AddItem_YN(int print_AddItem_YN) {
        Print_AddItem_YN = print_AddItem_YN;
    }

    public String getLocation_Setting() {
        return Location_Setting;
    }

    public void setLocation_Setting(String location_Setting) {
        Location_Setting = location_Setting;
    }

    public String getNickName_Setting() {
        return NickName_Setting;
    }

    public void setNickName_Setting(String nickName_Setting) {
        NickName_Setting = nickName_Setting;
    }

    public String getBranchName_Setting() {
        return BranchName_Setting;
    }

    public void setBranchName_Setting(String branchName_Setting) {
        BranchName_Setting = branchName_Setting;
    }

    public String getAddItem_Setting() {
        return AddItem_Setting;
    }

    public void setAddItem_Setting(String addItem_Setting) {
        AddItem_Setting = addItem_Setting;
    }

    //----------------------------------------//
    // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
    //----------------------------------------//
    public int getPrint_SellPrice_YN() {
        return Print_SellPrice_YN;
    }

    public void setPrint_SellPrice_YN(int print_SellPrice_YN) {
        Print_SellPrice_YN = print_SellPrice_YN;
    }

    public int getPrint_SaleSellRate_YN() {
        return Print_SaleSellRate_YN;
    }

    public void setPrint_SaleSellRate_YN(int print_SaleSellRate_YN) {
        Print_SaleSellRate_YN = print_SaleSellRate_YN;
    }

    public String getSellPrice_Setting() {
        return SellPrice_Setting;
    }

    public void setSellPrice_Setting(String sellPrice_Setting) {
        SellPrice_Setting = sellPrice_Setting;
    }

    public String getSaleSellRate_Setting() {
        return SaleSellRate_Setting;
    }

    public void setSaleSellRate_Setting(String saleSellRate_Setting) {
        SaleSellRate_Setting = saleSellRate_Setting;
    }
    //----------------------------------------//

    public int get_idx() {
        return _idx;
    }

    public void set_idx(int _idx) {
        this._idx = _idx;
    }

    public String getPrint_Size() {
        return Print_Size;
    }

    public void setPrint_Size(String print_Size) {
        Print_Size = print_Size;
    }

    public int getLavel_Hight() {
        return Lavel_Hight;
    }

    public void setLavel_Hight(int lavel_Hight) {
        Lavel_Hight = lavel_Hight;
    }

    public int getLavel_Width() {
        return Lavel_Width;
    }

    public void setLavel_Width(int lavel_Width) {
        Lavel_Width = lavel_Width;
    }

    public int getPrint_Direction() {
        return Print_Direction;
    }

    public void setPrint_Direction(int print_Direction) {
        Print_Direction = print_Direction;
    }

    public int getPaper_Gubun() {
        return Paper_Gubun;
    }

    public void setPaper_Gubun(int paper_Gubun) {
        Paper_Gubun = paper_Gubun;
    }

    public int getGap_Width() {
        return Gap_Width;
    }

    public void setGap_Width(int gap_Width) {
        Gap_Width = gap_Width;
    }

    public int getPrint_StdSize_YN() {
        return Print_StdSize_YN;
    }

    public void setPrint_StdSize_YN(int print_StdSize_YN) {
        Print_StdSize_YN = print_StdSize_YN;
    }

    public int getPrint_Price_YN() {
        return Print_Price_YN;
    }

    public void setPrint_Price_YN(int print_Price_YN) {
        Print_Price_YN = print_Price_YN;
    }

    public int getPrint_Office_YN() {
        return Print_Office_YN;
    }

    public void setPrint_Office_YN(int print_Office_YN) {
        Print_Office_YN = print_Office_YN;
    }

    public int getPrint_Danga_YN() {
        return Print_Danga_YN;
    }

    public void setPrint_Danga_YN(int print_Danga_YN) {
        Print_Danga_YN = print_Danga_YN;
    }

    public int getWord_Length() {
        return Word_Length;
    }

    public void setWord_Length(int word_Length) {
        Word_Length = word_Length;
    }

    public String getGoods_Setting() {
        return Goods_Setting;
    }

    public void setGoods_Setting(String goods_Setting) {
        Goods_Setting = goods_Setting;
    }

    public String getStdsize_Setting() {
        return Stdsize_Setting;
    }

    public void setStdsize_Setting(String stdsize_Setting) {
        Stdsize_Setting = stdsize_Setting;
    }

    public String getPrice_Setting() {
        return Price_Setting;
    }

    public void setPrice_Setting(String price_Setting) {
        Price_Setting = price_Setting;
    }

    public String getOffice_Setting() {
        return Office_Setting;
    }

    public void setOffice_Setting(String office_Setting) {
        Office_Setting = office_Setting;
    }

    public String getDanga_Setting() {
        return Danga_Setting;
    }

    public void setDanga_Setting(String danga_Setting) {
        Danga_Setting = danga_Setting;
    }

    public String getBarcode_Setting() {
        return Barcode_Setting;
    }

    public void setBarcode_Setting(String barcode_Setting) {
        Barcode_Setting = barcode_Setting;
    }

    public void setSPP3000_3058() {

        Print_Direction = 0;
        Paper_Gubun = 2;
        Gap_Width = 3;
        Print_StdSize_YN = 1;
        Print_Price_YN = 1;
        Print_Office_YN = 1;
        Print_Danga_YN = 1;
        Word_Length = 30;
        //100|20|1|0|1|0|0', '350|80|1|0|0|1|0', '400|160|1|2|2|0|0', '320|130|1|0|0|0|0', '350|105|1|0|0|1|0', '120|80|7|2|6|40|0|1'
        Goods_Setting = "80|10|1|0|1|0|0";
        Stdsize_Setting = "350|80|1|0|0|1|0";
        Price_Setting = "400|160|1|2|2|0|0";
        Office_Setting = "320|130|1|0|0|0|0";
        Danga_Setting = "320|105|1|0|0|1|0";
        Barcode_Setting = "120|80|7|2|6|40|0|1";
        //----------------------------------------//
        // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
        //----------------------------------------//
        Print_SellPrice_YN = 1;
        Print_SaleSellRate_YN = 1;
        SellPrice_Setting = "200|160|1|1|1|0|0";
        SaleSellRate_Setting = "100|200|1|0|0|0|0";
        //----------------------------------------//
        //----------------------------------------//
        // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
        //----------------------------------------//
        // 위치
        Print_Location_YN = 1;
        Location_Setting = "400|160|1|0|0|0|0";
        // 품번
        Print_NickName_YN = 1;
        NickName_Setting = "400|160|1|0|0|0|0";
        // 분류
        Print_BranchName_YN = 1;
        BranchName_Setting = "320|80|1|0|0|0|0";
        // 추가항목
        Print_AddItem_YN = 1;
        AddItem_Setting = "300|0|0|0|0|0|0";
        //----------------------------------------//

    }

    public void setSPP3000_2340() {

        //'150|10|0|0|1|0|0', '350|60|0|0|0|1|0', '350|130|0|1|1|0|0', '320|110|0|0|0|0|0', '350|80|0|0|0|1|0', '155|50|7|2|6|40|0|1'),"

        Print_Direction = 0;
        Paper_Gubun = 2;
        Gap_Width = 3;
        Print_StdSize_YN = 1;
        Print_Price_YN = 1;
        Print_Office_YN = 1;
        Print_Danga_YN = 1;
        Word_Length = 30;
        Goods_Setting = "80|10|1|0|1|0|0";
        Stdsize_Setting = "350|80|1|0|0|1|0";
        Price_Setting = "400|160|1|2|2|0|0";
        Office_Setting = "320|130|1|0|0|0|0";
        Danga_Setting = "320|105|1|0|0|1|0";
        Barcode_Setting = "120|80|7|2|6|40|0|1";
        //----------------------------------------//
        // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
        //----------------------------------------//
        Print_SellPrice_YN = 1;
        Print_SaleSellRate_YN = 1;
        SellPrice_Setting = "200|160|1|1|1|0|0";
        SaleSellRate_Setting = "100|200|1|0|0|0|0";
        //----------------------------------------//
        //----------------------------------------//
        // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
        //----------------------------------------//
        // 위치
        Print_Location_YN = 1;
        Location_Setting = "400|160|1|0|0|0|0";
        // 품번
        Print_NickName_YN = 1;
        NickName_Setting = "400|160|1|0|0|0|0";
        // 분류
        Print_BranchName_YN = 1;
        BranchName_Setting = "320|80|1|0|0|0|0";
        // 추가항목
        Print_AddItem_YN = 1;
        AddItem_Setting = "300|0|0|0|0|0|0";
        //----------------------------------------//

    }

    public void setLKP30_3058() {

        Print_Direction = 0;
        Paper_Gubun = 2;
        Gap_Width = 3;
        Print_StdSize_YN = 1;
        Print_Price_YN = 1;
        Print_Office_YN = 1;
        Print_Danga_YN = 1;
        Word_Length = 30;
        Goods_Setting = "0|0|0|1|2|0|0";
        Stdsize_Setting = "230|50|0|1|1|1|0";
        Price_Setting = "180|60|0|2|2|0|0";
        Office_Setting = "200|30|0|1|1|0|0";
        Danga_Setting = "200|110|0|1|1|1|0";
        Barcode_Setting = "0|170|7|2|6|30|0|1";
        //----------------------------------------//
        // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
        //----------------------------------------//
        Print_SellPrice_YN = 1;
        Print_SaleSellRate_YN = 1;
        SellPrice_Setting = "0|60|1|2|2|0|0";
        SaleSellRate_Setting = "0|110|1|1|1|0|0";
        //----------------------------------------//
        //----------------------------------------//
        // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
        //----------------------------------------//
        // 위치
        Print_Location_YN = 1;
        Location_Setting = "200|0|1|1|1|0|0";
        // 품번
        Print_NickName_YN = 1;
        NickName_Setting = "200|30|1|1|1|0|0";
        // 분류
        Print_BranchName_YN = 1;
        BranchName_Setting = "0|140|1|1|1|0|0";
        // 추가항목
        Print_AddItem_YN = 1;
        AddItem_Setting = "200|140|1|1|1|0|0";
        //----------------------------------------//

    }

    public void setLKP30_2340() {

        Print_Direction = 0;
        Paper_Gubun = 0;
        Gap_Width = 3;
        Print_StdSize_YN = 1;
        Print_Price_YN = 1;
        Print_Office_YN = 1;
        Print_Danga_YN = 1;
        Word_Length = 15;
        Goods_Setting = "0|0|0|1|2|0|0";
        Stdsize_Setting = "230|50|0|1|1|1|0";
        Price_Setting = "180|60|0|2|2|0|0";
        Office_Setting = "200|30|0|1|1|0|0";
        Danga_Setting = "200|110|0|1|1|1|0";
        Barcode_Setting = "0|170|7|2|6|30|0|1";
        //----------------------------------------//
        // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
        //----------------------------------------//
        Print_SellPrice_YN = 1;
        Print_SaleSellRate_YN = 1;
        SellPrice_Setting = "0|60|1|2|2|0|0";
        SaleSellRate_Setting = "0|110|1|1|1|0|0";
        //----------------------------------------//
        //----------------------------------------//
        // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
        //----------------------------------------//
        // 위치
        Print_Location_YN = 1;
        Location_Setting = "200|0|1|1|1|0|0";
        // 품번
        Print_NickName_YN = 1;
        NickName_Setting = "200|30|1|1|1|0|0";
        // 분류
        Print_BranchName_YN = 1;
        BranchName_Setting = "0|140|1|1|1|0|0";
        // 추가항목
        Print_AddItem_YN = 1;
        AddItem_Setting = "200|140|1|1|1|0|0";
        //----------------------------------------//

    }
}
