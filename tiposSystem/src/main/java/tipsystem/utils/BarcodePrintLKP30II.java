package tipsystem.utils;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import kr.co.tipos.tips.R;
import tipsystem.tips.models.SPPL3000;

public class BarcodePrintLKP30II extends Activity {

    String TAG = "LK-P30II";

    SPPL3000 spp;
    DBAdapter dba;
    Context context;

    Spinner spinner_papersize;

    EditText m_paperheight;
    EditText m_paperwidth;
    Spinner m_direction;
    Spinner m_paper_gubun;
    EditText m_paperinterval;
    CheckBox m_stdsize_yn;
    CheckBox m_price_yn;
    CheckBox m_office_yn;
    CheckBox m_danga_yn;
    EditText m_str_length;

    EditText m_gname_garow;
    EditText m_gname_serow;
    Spinner m_gname_fontsize;
    Spinner m_gname_garoz;
    Spinner m_gname_seroz;
    Spinner m_gname_bold;
    Spinner m_gname_rota;

    EditText m_stdsize_garow;
    EditText m_stdsize_serow;
    Spinner m_stdsize_fontsize;
    Spinner m_stdsize_garoz;
    Spinner m_stdsize_seroz;
    Spinner m_stdsize_bold;
    Spinner m_stdsize_rota;

    EditText m_pirce_garow;
    EditText m_price_serow;
    Spinner m_price_fontsize;
    Spinner m_price_garoz;
    Spinner m_price_seroz;
    Spinner m_pirce_bold;
    Spinner m_price_rota;

    EditText m_office_garow;
    EditText m_office_serow;
    Spinner m_office_fontsize;
    Spinner m_office_garoz;
    Spinner m_offiec_seroz;
    Spinner m_office_bold;
    Spinner m_office_rota;

    EditText m_danga_garow;
    EditText m_danga_serow;
    Spinner m_danga_fontsize;
    Spinner m_danga_garoz;
    Spinner m_danga_seroz;
    Spinner m_danga_bold;
    Spinner m_danga_rota;

    EditText m_barcode_garow;
    EditText m_barcode_serow;
    Spinner m_barcode_codetype;
    EditText m_barcode_narr;
    EditText m_barcode_wide;
    EditText m_barcode_high;
    Spinner m_barcode_rota;
    Spinner m_barcode_numprint;

    //----------------------------------------//
    // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
    //----------------------------------------//
    CheckBox sellPriceYnCheckBox;
    CheckBox saleSellRateYnCheckBox;

    EditText sellPriceWidthPointEditText;
    EditText sellPriceHeightPointEditText;
    Spinner sellPriceFontSizeSpinner;
    Spinner sellPriceWidthExpandSpinner;
    Spinner sellPriceHeightExpandSpinner;
    Spinner sellPriceFontBoldSpinner;
    Spinner sellPriceFontRotateSpinner;

    EditText saleSellRateWidthPointEditText;
    EditText saleSellRateHeightPointEditText;
    Spinner saleSellRateFontSizeSpinner;
    Spinner saleSellRateWidthExpandSpinner;
    Spinner saleSellRateHeightExpandSpinner;
    Spinner saleSellRateFontBoldSpinner;
    Spinner saleSellRateFontRotateSpinner;
    //----------------------------------------//

    //----------------------------------------//
    // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
    //----------------------------------------//
    //위치
    CheckBox locationYnCheckBox;
    EditText locationWidthPointEditText;
    EditText locationHeightPointEditText;
    Spinner locationFontSizeSpinner;
    Spinner locationWidthExpandSpinner;
    Spinner locationHeightExpandSpinner;
    Spinner locationFontBoldSpinner;
    Spinner locationFontRotateSpinner;

    //품번
    CheckBox nickNameYnCheckBox;
    EditText nickNameWidthPointEditText;
    EditText nickNameHeightPointEditText;
    Spinner nickNameFontSizeSpinner;
    Spinner nickNameWidthExpandSpinner;
    Spinner nickNameHeightExpandSpinner;
    Spinner nickNameFontBoldSpinner;
    Spinner nickNameFontRotateSpinner;

    //분류
    CheckBox branchNameYnCheckBox;
    EditText branchNameWidthPointEditText;
    EditText branchNameHeightPointEditText;
    Spinner branchNameFontSizeSpinner;
    Spinner branchNameWidthExpandSpinner;
    Spinner branchNameHeightExpandSpinner;
    Spinner branchNameFontBoldSpinner;
    Spinner branchNameFontRotateSpinner;

    //추가항목
    CheckBox addItemYnCheckBox;
    EditText addItemWidthPointEditText;
    EditText addItemHeightPointEditText;
    Spinner addItemFontSizeSpinner;
    Spinner addItemWidthExpandSpinner;
    Spinner addItemHeightExpandSpinner;
    Spinner addItemFontBoldSpinner;
    Spinner addItemFontRotateSpinner;
    //----------------------------------------//

    Button m_reset;

    TIPS_Config tips;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_print_lkp30_ii);

        //환경설정 ip,port,officecode
        tips = new TIPS_Config(this);
        context = this;

        init_view();

        //바코드 프린터 설정
        dba = new DBAdapter(context, tips.getSHOP_OFFICECODE() + ".tips");
        Log.d(TAG, tips.getSHOP_OFFICECODE());

        String paper_gubun = tips.getSELECTPRINT_GUBUN();

        spinner_papersize = (Spinner) findViewById(R.id.spinner_papersize);
        spinner_papersize.setSelection(tips.SELECTPRINT_GUBUN, false);
        spinner_papersize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                String gubun = parent.getItemAtPosition(position).toString();
                Log.d(TAG, gubun);
                tips.setSELECTPRINT_GUBUN(gubun);

                Log.d(TAG, tips.getSELECTPRINT_GUBUN());

                String query = "Select * From BaPrint_SPPL3000 Where Print_Size='" + gubun + "'; ";
                Log.d(TAG, query);
                spp = dba.getBarPrint_SPPL3000(query);

                setting_view();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        String query = "Select * From BaPrint_SPPL3000 Where Print_Size='" + paper_gubun + "'; ";
        Log.d(TAG, query);
        spp = dba.getBarPrint_SPPL3000(query);

        if (spp == null) {
            Log.e(TAG, "검색된 결과가 없습니다.");
            return;
        }

        m_reset = (Button) findViewById(R.id.button_reset);

        //바코드 프린터
        setting_view();

    }


    public void init_view() {

        m_paperheight = (EditText) findViewById(R.id.edit_paperheight);
        m_paperwidth = (EditText) findViewById(R.id.edit_paperwidth);
        m_direction = (Spinner) findViewById(R.id.spinner_direction);
        m_paper_gubun = (Spinner) findViewById(R.id.spinner_paper_gubun);
        m_paperinterval = (EditText) findViewById(R.id.edit_paperinterval);
        m_stdsize_yn = (CheckBox) findViewById(R.id.chk_stdsize_yn);
        m_price_yn = (CheckBox) findViewById(R.id.chk_price_yn);
        m_office_yn = (CheckBox) findViewById(R.id.chk_office_yn);
        m_danga_yn = (CheckBox) findViewById(R.id.chk_danga_yn);
        m_str_length = (EditText) findViewById(R.id.edit_str_length);

        m_gname_garow = (EditText) findViewById(R.id.edit_gname_garow);
        m_gname_serow = (EditText) findViewById(R.id.edit_gname_serow);
        m_gname_fontsize = (Spinner) findViewById(R.id.spinner_gname_fontsize);
        m_gname_garoz = (Spinner) findViewById(R.id.spinner_gname_garoz);
        m_gname_seroz = (Spinner) findViewById(R.id.spinner_gname_seroz);
        m_gname_bold = (Spinner) findViewById(R.id.spinner_gname_bold);
        m_gname_rota = (Spinner) findViewById(R.id.spinner_gname_rota);

        m_stdsize_garow = (EditText) findViewById(R.id.edit_stdsize_garow);
        m_stdsize_serow = (EditText) findViewById(R.id.edit_stdsize_serow);
        m_stdsize_fontsize = (Spinner) findViewById(R.id.spinner_stdsize_fontsize);
        m_stdsize_garoz = (Spinner) findViewById(R.id.spinner_stdsize_garoz);
        m_stdsize_seroz = (Spinner) findViewById(R.id.spinner_stdsize_seroz);
        m_stdsize_bold = (Spinner) findViewById(R.id.spinner_stdsize_bold);
        m_stdsize_rota = (Spinner) findViewById(R.id.spinner_stdsize_rota);

        m_pirce_garow = (EditText) findViewById(R.id.edit_price_garow);
        m_price_serow = (EditText) findViewById(R.id.edit_price_serow);
        m_price_fontsize = (Spinner) findViewById(R.id.spinner_price_fontsize);
        m_price_garoz = (Spinner) findViewById(R.id.spinner_price_garoz);
        m_price_seroz = (Spinner) findViewById(R.id.spinner_price_seroz);
        m_pirce_bold = (Spinner) findViewById(R.id.spinner_price_bold);
        m_price_rota = (Spinner) findViewById(R.id.spinner_price_rota);

        m_office_garow = (EditText) findViewById(R.id.edit_office_garow);
        m_office_serow = (EditText) findViewById(R.id.edit_office_serow);
        m_office_fontsize = (Spinner) findViewById(R.id.spinner_office_fontsize);
        m_office_garoz = (Spinner) findViewById(R.id.spinner_office_garoz);
        m_offiec_seroz = (Spinner) findViewById(R.id.spinner_office_seroz);
        m_office_bold = (Spinner) findViewById(R.id.spinner_office_bold);
        m_office_rota = (Spinner) findViewById(R.id.spinner_office_rota);

        m_danga_garow = (EditText) findViewById(R.id.edit_danga_garow);
        m_danga_serow = (EditText) findViewById(R.id.edit_danga_serow);
        m_danga_fontsize = (Spinner) findViewById(R.id.spinner_danga_fontsize);
        m_danga_garoz = (Spinner) findViewById(R.id.spinner_danga_garoz);
        m_danga_seroz = (Spinner) findViewById(R.id.spinner_danga_seroz);
        m_danga_bold = (Spinner) findViewById(R.id.spinner_danga_bold);
        m_danga_rota = (Spinner) findViewById(R.id.spinner_danga_rota);

        m_barcode_garow = (EditText) findViewById(R.id.edit_barcode_garow);
        m_barcode_serow = (EditText) findViewById(R.id.edit_barcode_serow);
        m_barcode_codetype = (Spinner) findViewById(R.id.spinner_barcode_codetype);
        m_barcode_narr = (EditText) findViewById(R.id.edit_barcode_narr);
        m_barcode_wide = (EditText) findViewById(R.id.edit_barcode_wide);
        m_barcode_high = (EditText) findViewById(R.id.edit_barcode_high);
        m_barcode_rota = (Spinner) findViewById(R.id.spinner_barcode_rota);
        m_barcode_numprint = (Spinner) findViewById(R.id.spinner_barcode_numprint);

        //----------------------------------------//
        // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
        //----------------------------------------//
        sellPriceYnCheckBox = (CheckBox) findViewById(R.id.sell_price_yn_checkbox);
        saleSellRateYnCheckBox = (CheckBox) findViewById(R.id.sale_sell_rate_yn_checkbox);

        sellPriceWidthPointEditText = (EditText) findViewById(R.id.sell_price_width_point_edittext);
        sellPriceHeightPointEditText = (EditText) findViewById(R.id.sell_price_height_point_edittext);
        sellPriceFontSizeSpinner = (Spinner) findViewById(R.id.sell_price_font_size_spinner);
        sellPriceWidthExpandSpinner = (Spinner) findViewById(R.id.sell_price_width_expand_spinner);
        sellPriceHeightExpandSpinner = (Spinner) findViewById(R.id.sell_price_height_expand_spinner);
        sellPriceFontBoldSpinner = (Spinner) findViewById(R.id.sell_price_font_bold_spinner);
        sellPriceFontRotateSpinner = (Spinner) findViewById(R.id.sell_price_font_rotate_spinner);

        saleSellRateWidthPointEditText = (EditText) findViewById(R.id.sale_sell_rate_width_point_edittext);
        saleSellRateHeightPointEditText = (EditText) findViewById(R.id.sale_sell_rate_height_point_edittext);
        saleSellRateFontSizeSpinner = (Spinner) findViewById(R.id.sale_sell_rate_font_size_spinner);
        saleSellRateWidthExpandSpinner = (Spinner) findViewById(R.id.sale_sell_rate_width_expand_spinner);
        saleSellRateHeightExpandSpinner = (Spinner) findViewById(R.id.sale_sell_rate_height_expand_spinner);
        saleSellRateFontBoldSpinner = (Spinner) findViewById(R.id.sale_sell_rate_font_bold_spinner);
        saleSellRateFontRotateSpinner = (Spinner) findViewById(R.id.sale_sell_rate_font_rotate_spinner);
        //----------------------------------------//
        //----------------------------------------//
        // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
        //1.위치
        locationYnCheckBox = (CheckBox) findViewById(R.id.location_yn_checkbox);
        locationWidthPointEditText = (EditText) findViewById(R.id.location_width_point_edittext);
        locationHeightPointEditText = (EditText) findViewById(R.id.location_height_point_edittext);
        locationFontSizeSpinner = (Spinner) findViewById(R.id.location_font_size_spinner);
        locationWidthExpandSpinner = (Spinner) findViewById(R.id.location_width_expand_spinner);
        locationHeightExpandSpinner = (Spinner) findViewById(R.id.location_height_expand_spinner);
        locationFontBoldSpinner = (Spinner) findViewById(R.id.location_font_bold_spinner);
        locationFontRotateSpinner = (Spinner) findViewById(R.id.location_font_rotate_spinner);
        //2.품번
        nickNameYnCheckBox = (CheckBox) findViewById(R.id.nick_name_yn_checkbox);
        nickNameWidthPointEditText = (EditText) findViewById(R.id.nick_name_width_point_edittext);
        nickNameHeightPointEditText = (EditText) findViewById(R.id.nick_name_height_point_edittext);
        nickNameFontSizeSpinner = (Spinner) findViewById(R.id.nick_name_font_size_spinner);
        nickNameWidthExpandSpinner = (Spinner) findViewById(R.id.nick_name_width_expand_spinner);
        nickNameHeightExpandSpinner = (Spinner) findViewById(R.id.nick_name_height_expand_spinner);
        nickNameFontBoldSpinner = (Spinner) findViewById(R.id.nick_name_font_bold_spinner);
        nickNameFontRotateSpinner = (Spinner) findViewById(R.id.nick_name_font_rotate_spinner);
        //3.분류
        branchNameYnCheckBox = (CheckBox) findViewById(R.id.branch_name_yn_checkbox);
        branchNameWidthPointEditText = (EditText) findViewById(R.id.branch_name_width_point_edittext);
        branchNameHeightPointEditText = (EditText) findViewById(R.id.branch_name_height_point_edittext);
        branchNameFontSizeSpinner = (Spinner) findViewById(R.id.branch_name_font_size_spinner);
        branchNameWidthExpandSpinner = (Spinner) findViewById(R.id.branch_name_width_expand_spinner);
        branchNameHeightExpandSpinner = (Spinner) findViewById(R.id.branch_name_height_expand_spinner);
        branchNameFontBoldSpinner = (Spinner) findViewById(R.id.branch_name_font_bold_spinner);
        branchNameFontRotateSpinner = (Spinner) findViewById(R.id.branch_name_font_rotate_spinner);
        //4.추가항목
        addItemYnCheckBox = (CheckBox) findViewById(R.id.add_item_yn_checkbox);
        addItemWidthPointEditText = (EditText) findViewById(R.id.add_item_width_point_edittext);
        addItemHeightPointEditText = (EditText) findViewById(R.id.add_item_height_point_edittext);
        addItemFontSizeSpinner = (Spinner) findViewById(R.id.add_item_font_size_spinner);
        addItemWidthExpandSpinner = (Spinner) findViewById(R.id.add_item_width_expand_spinner);
        addItemHeightExpandSpinner = (Spinner) findViewById(R.id.add_item_height_expand_spinner);
        addItemFontBoldSpinner = (Spinner) findViewById(R.id.add_item_font_bold_spinner);
        addItemFontRotateSpinner = (Spinner) findViewById(R.id.add_item_font_rotate_spinner);
        //----------------------------------------//

    }

    public void setting_view() {

        if (spp.getPrint_Size().equals(TIPOS.BARCODE_PRINTER_PAPERGUBUN_0) || spp.getPrint_Size().equals(TIPOS.BARCODE_PRINTER_PAPERGUBUN_1)) {
            setPaperEnale(false);
        } else {
            setPaperEnale(true);
        }

        m_paperheight.setText(String.valueOf(spp.getLavel_Hight()));
        m_paperwidth.setText(String.valueOf(spp.getLavel_Width()));
        m_direction.setSelection(spp.getPrint_Direction(), false);
        m_paper_gubun.setSelection(spp.getPaper_Gubun(), false);
        m_paperinterval.setText(String.valueOf(spp.getGap_Width()));
        if (spp.getPrint_StdSize_YN() == 0) {
            m_stdsize_yn.setChecked(false);
        } else {
            m_stdsize_yn.setChecked(true);
        }
        if (spp.getPrint_Price_YN() == 0) {
            m_price_yn.setChecked(false);
        } else {
            m_price_yn.setChecked(true);
        }
        if (spp.getPrint_Office_YN() == 0) {
            m_office_yn.setChecked(false);
        } else {
            m_office_yn.setChecked(true);
        }
        if (spp.getPrint_Danga_YN() == 0) {
            m_danga_yn.setChecked(false);
        } else {
            m_danga_yn.setChecked(true);
        }
        m_str_length.setText(String.valueOf(spp.getWord_Length()));

        String[] gname = spp.getGoods_Setting().split("\\|");
        Log.d(TAG, gname[0] + "|" + gname[1] + "|" + gname[2] + "|" + gname[3] + "|" + gname[4] + "|" + gname[5] + "|" + gname[6]);
        m_gname_garow.setText(gname[0]);
        m_gname_serow.setText(gname[1]);
        m_gname_fontsize.setSelection(Integer.parseInt(gname[2]), false);
        m_gname_garoz.setSelection(Integer.parseInt(gname[3]), false);
        m_gname_seroz.setSelection(Integer.parseInt(gname[4]), false);
        m_gname_bold.setSelection(Integer.parseInt(gname[5]), false);
        m_gname_rota.setSelection(Integer.parseInt(gname[6]), false);

        String[] stdsize = spp.getStdsize_Setting().split("\\|");
        m_stdsize_garow.setText(stdsize[0]);
        m_stdsize_serow.setText(stdsize[1]);
        m_stdsize_fontsize.setSelection(Integer.parseInt(stdsize[2]), false);
        m_stdsize_garoz.setSelection(Integer.parseInt(stdsize[3]), false);
        m_stdsize_seroz.setSelection(Integer.parseInt(stdsize[4]), false);
        m_stdsize_bold.setSelection(Integer.parseInt(stdsize[5]), false);
        m_stdsize_rota.setSelection(Integer.parseInt(stdsize[6]), false);

        String[] price = spp.getPrice_Setting().split("\\|");
        m_pirce_garow.setText(price[0]);
        m_price_serow.setText(price[1]);
        m_price_fontsize.setSelection(Integer.parseInt(price[2]), false);
        m_price_garoz.setSelection(Integer.parseInt(price[3]), false);
        m_price_seroz.setSelection(Integer.parseInt(price[4]), false);
        m_pirce_bold.setSelection(Integer.parseInt(price[5]), false);
        m_price_rota.setSelection(Integer.parseInt(price[6]), false);

        String[] office = spp.getOffice_Setting().split("\\|");
        m_office_garow.setText(office[0]);
        m_office_serow.setText(office[1]);
        m_office_fontsize.setSelection(Integer.parseInt(office[2]), false);
        m_office_garoz.setSelection(Integer.parseInt(office[3]), false);
        m_offiec_seroz.setSelection(Integer.parseInt(office[4]), false);
        m_office_bold.setSelection(Integer.parseInt(office[5]), false);
        m_office_rota.setSelection(Integer.parseInt(office[6]), false);

        String[] danga = spp.getDanga_Setting().split("\\|");
        m_danga_garow.setText(danga[0]);
        m_danga_serow.setText(danga[1]);
        m_danga_fontsize.setSelection(Integer.parseInt(danga[2]), false);
        m_danga_garoz.setSelection(Integer.parseInt(danga[3]), false);
        m_danga_seroz.setSelection(Integer.parseInt(danga[4]), false);
        m_danga_bold.setSelection(Integer.parseInt(danga[5]), false);
        m_danga_rota.setSelection(Integer.parseInt(danga[6]), false);

        String[] barcode = spp.getBarcode_Setting().split("\\|");
        m_barcode_garow.setText(barcode[0]);
        m_barcode_serow.setText(barcode[1]);
        m_barcode_codetype.setSelection(Integer.parseInt(barcode[2]), false);
        m_barcode_narr.setText(barcode[3]);
        m_barcode_wide.setText(barcode[4]);
        m_barcode_high.setText(barcode[5]);
        m_barcode_rota.setSelection(Integer.parseInt(barcode[6]), false);
        m_barcode_numprint.setSelection(Integer.parseInt(barcode[7]), false);

        //----------------------------------------//
        // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
        //----------------------------------------//
        if (spp.getPrint_SellPrice_YN() == 0) {
            sellPriceYnCheckBox.setChecked(false);
        } else {
            sellPriceYnCheckBox.setChecked(true);
        }
        if (spp.getPrint_SaleSellRate_YN() == 0) {
            saleSellRateYnCheckBox.setChecked(false);
        } else {
            saleSellRateYnCheckBox.setChecked(true);
        }

        String[] sellPrice = spp.getSellPrice_Setting().split("\\|");
        sellPriceWidthPointEditText.setText(sellPrice[0]);
        sellPriceHeightPointEditText.setText(sellPrice[1]);
        sellPriceFontSizeSpinner.setSelection(Integer.parseInt(sellPrice[2]), false);
        sellPriceWidthExpandSpinner.setSelection(Integer.parseInt(sellPrice[3]), false);
        sellPriceHeightExpandSpinner.setSelection(Integer.parseInt(sellPrice[4]), false);
        sellPriceFontBoldSpinner.setSelection(Integer.parseInt(sellPrice[5]), false);
        sellPriceFontRotateSpinner.setSelection(Integer.parseInt(sellPrice[6]), false);

        String[] saleSellRate = spp.getSaleSellRate_Setting().split("\\|");
        saleSellRateWidthPointEditText.setText(saleSellRate[0]);
        saleSellRateHeightPointEditText.setText(saleSellRate[1]);
        saleSellRateFontSizeSpinner.setSelection(Integer.parseInt(saleSellRate[2]), false);
        saleSellRateWidthExpandSpinner.setSelection(Integer.parseInt(saleSellRate[3]), false);
        saleSellRateHeightExpandSpinner.setSelection(Integer.parseInt(saleSellRate[4]), false);
        saleSellRateFontBoldSpinner.setSelection(Integer.parseInt(saleSellRate[5]), false);
        saleSellRateFontRotateSpinner.setSelection(Integer.parseInt(saleSellRate[6]), false);
        //----------------------------------------//
        //----------------------------------------//
        // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
        //----------------------------------------//
        // 위치(location)
        if (spp.getPrint_Location_YN() == 0) {
            locationYnCheckBox.setChecked(false);
        } else {
            locationYnCheckBox.setChecked(true);
        }
        String[] location = spp.getLocation_Setting().split("\\|");
        locationWidthPointEditText.setText(location[0]);
        locationHeightPointEditText.setText(location[1]);
        locationFontSizeSpinner.setSelection(Integer.parseInt(location[2]), false);
        locationWidthExpandSpinner.setSelection(Integer.parseInt(location[3]), false);
        locationHeightExpandSpinner.setSelection(Integer.parseInt(location[4]), false);
        locationFontBoldSpinner.setSelection(Integer.parseInt(location[5]), false);
        locationFontRotateSpinner.setSelection(Integer.parseInt(location[6]), false);

        // 품번(nickName)
        if (spp.getPrint_NickName_YN() == 0) {
            nickNameYnCheckBox.setChecked(false);
        } else {
            nickNameYnCheckBox.setChecked(true);
        }
        String[] nickName = spp.getNickName_Setting().split("\\|");
        nickNameWidthPointEditText.setText(nickName[0]);
        nickNameHeightPointEditText.setText(nickName[1]);
        nickNameFontSizeSpinner.setSelection(Integer.parseInt(nickName[2]), false);
        nickNameWidthExpandSpinner.setSelection(Integer.parseInt(nickName[3]), false);
        nickNameHeightExpandSpinner.setSelection(Integer.parseInt(nickName[4]), false);
        nickNameFontBoldSpinner.setSelection(Integer.parseInt(nickName[5]), false);
        nickNameFontRotateSpinner.setSelection(Integer.parseInt(nickName[6]), false);

        // 분류(BranchName)
        if (spp.getPrint_BranchName_YN() == 0) {
            branchNameYnCheckBox.setChecked(false);
        } else {
            branchNameYnCheckBox.setChecked(true);
        }
        String[] branchName = spp.getBranchName_Setting().split("\\|");
        branchNameWidthPointEditText.setText(branchName[0]);
        branchNameHeightPointEditText.setText(branchName[1]);
        branchNameFontSizeSpinner.setSelection(Integer.parseInt(branchName[2]), false);
        branchNameWidthExpandSpinner.setSelection(Integer.parseInt(branchName[3]), false);
        branchNameHeightExpandSpinner.setSelection(Integer.parseInt(branchName[4]), false);
        branchNameFontBoldSpinner.setSelection(Integer.parseInt(branchName[5]), false);
        branchNameFontRotateSpinner.setSelection(Integer.parseInt(branchName[6]), false);

        // 추가항목(addItem)
        if (spp.getPrint_AddItem_YN() == 0) {
            addItemYnCheckBox.setChecked(false);
        } else {
            addItemYnCheckBox.setChecked(true);
        }
        String[] addItem = spp.getAddItem_Setting().split("\\|");
        addItemWidthPointEditText.setText(addItem[0]);
        addItemHeightPointEditText.setText(addItem[1]);
        addItemFontSizeSpinner.setSelection(Integer.parseInt(addItem[2]), false);
        addItemWidthExpandSpinner.setSelection(Integer.parseInt(addItem[3]), false);
        addItemHeightExpandSpinner.setSelection(Integer.parseInt(addItem[4]), false);
        addItemFontBoldSpinner.setSelection(Integer.parseInt(addItem[5]), false);
        addItemFontRotateSpinner.setSelection(Integer.parseInt(addItem[6]), false);
        //----------------------------------------//

    }

    public void activityClose(View view) {
        this.finish();
    }


    public void allSave(View view) {

        try {

            String height = m_paperheight.getText().toString();
            if (nullCheck(height)) {
                Toast.makeText(this, "높이를 입력해 주세요", Toast.LENGTH_SHORT).show();
                m_paperheight.requestFocus();
                return;
            }
            int paper_height = Integer.parseInt(height);
            spp.setLavel_Hight(paper_height);

            String width = m_paperwidth.getText().toString();
            if (nullCheck(width)) {
                Toast.makeText(this, "너비를 입력해 주세요", Toast.LENGTH_SHORT).show();
                m_paperwidth.requestFocus();
                return;
            }
            int paper_width = Integer.parseInt(width);
            spp.setLavel_Width(paper_width);

            int direction = m_direction.getSelectedItemPosition();
            spp.setPrint_Direction(direction);

            int print_size = m_paper_gubun.getSelectedItemPosition();
            spp.setPaper_Gubun(print_size);

            int paperinterval = Integer.parseInt(m_paperinterval.getText().toString());
            spp.setGap_Width(paperinterval);
            if (m_stdsize_yn.isChecked()) {
                spp.setPrint_StdSize_YN(1);
            } else {
                spp.setPrint_StdSize_YN(0);
            }
            if (m_price_yn.isChecked()) {
                spp.setPrint_Price_YN(1);
            } else {
                spp.setPrint_Price_YN(0);
            }
            if (m_office_yn.isChecked()) {
                spp.setPrint_Office_YN(1);
            } else {
                spp.setPrint_Office_YN(0);
            }
            if (m_danga_yn.isChecked()) {
                spp.setPrint_Danga_YN(1);
            } else {
                spp.setPrint_Danga_YN(0);
            }
            int str_length = Integer.parseInt(m_str_length.getText().toString());
            spp.setWord_Length(str_length);

            String garow = m_gname_garow.getText().toString();
            String serow = m_gname_serow.getText().toString();
            int fontsize = m_gname_fontsize.getSelectedItemPosition();
            int garoz = m_gname_garoz.getSelectedItemPosition();
            int seroz = m_gname_seroz.getSelectedItemPosition();
            int bold = m_gname_bold.getSelectedItemPosition();
            int rota = m_gname_rota.getSelectedItemPosition();

            if (nullCheck(garow) || nullCheck(serow)) {
                Toast.makeText(this, "가로,세로를 확인해 주세요", Toast.LENGTH_SHORT).show();
                m_gname_garow.requestFocus();
                return;
            }

            String gname = garow + "|" + serow + "|" + fontsize + "|" + garoz + "|" + seroz + "|" + bold + "|" + rota;
            spp.setGoods_Setting(gname);

            garow = m_stdsize_garow.getText().toString();
            serow = m_stdsize_serow.getText().toString();
            fontsize = m_stdsize_fontsize.getSelectedItemPosition();
            garoz = m_stdsize_garoz.getSelectedItemPosition();
            seroz = m_stdsize_seroz.getSelectedItemPosition();
            bold = m_stdsize_bold.getSelectedItemPosition();
            rota = m_stdsize_rota.getSelectedItemPosition();

            if (nullCheck(garow) || nullCheck(serow)) {
                Toast.makeText(this, "가로,세로를 확인해 주세요", Toast.LENGTH_SHORT).show();
                m_stdsize_garow.requestFocus();
                return;
            }

            String stdsize = garow + "|" + serow + "|" + fontsize + "|" + garoz + "|" + seroz + "|" + bold + "|" + rota;
            spp.setStdsize_Setting(stdsize);

            garow = m_pirce_garow.getText().toString();
            serow = m_price_serow.getText().toString();
            fontsize = m_price_fontsize.getSelectedItemPosition();
            garoz = m_price_garoz.getSelectedItemPosition();
            seroz = m_price_seroz.getSelectedItemPosition();
            bold = m_pirce_bold.getSelectedItemPosition();
            rota = m_price_rota.getSelectedItemPosition();

            if (nullCheck(garow) || nullCheck(serow)) {
                Toast.makeText(this, "가로,세로를 확인해 주세요", Toast.LENGTH_SHORT).show();
                m_pirce_garow.requestFocus();
                return;
            }

            String price = garow + "|" + serow + "|" + fontsize + "|" + garoz + "|" + seroz + "|" + bold + "|" + rota;
            spp.setPrice_Setting(price);

            garow = m_office_garow.getText().toString();
            serow = m_office_serow.getText().toString();
            fontsize = m_office_fontsize.getSelectedItemPosition();
            garoz = m_office_garoz.getSelectedItemPosition();
            seroz = m_offiec_seroz.getSelectedItemPosition();
            bold = m_office_bold.getSelectedItemPosition();
            rota = m_office_rota.getSelectedItemPosition();

            if (nullCheck(garow) || nullCheck(serow)) {
                Toast.makeText(this, "가로,세로를 확인해 주세요", Toast.LENGTH_SHORT).show();
                m_office_garow.requestFocus();
                return;
            }

            String office = garow + "|" + serow + "|" + fontsize + "|" + garoz + "|" + seroz + "|" + bold + "|" + rota;
            spp.setOffice_Setting(office);

            garow = m_danga_garow.getText().toString();
            serow = m_danga_serow.getText().toString();
            fontsize = m_danga_fontsize.getSelectedItemPosition();
            garoz = m_danga_garoz.getSelectedItemPosition();
            seroz = m_danga_seroz.getSelectedItemPosition();
            bold = m_danga_bold.getSelectedItemPosition();
            rota = m_danga_rota.getSelectedItemPosition();

            if (nullCheck(garow) || nullCheck(serow)) {
                Toast.makeText(this, "가로,세로를 확인해 주세요", Toast.LENGTH_SHORT).show();
                m_danga_garow.requestFocus();
                return;
            }

            String danga = garow + "|" + serow + "|" + fontsize + "|" + garoz + "|" + seroz + "|" + bold + "|" + rota;
            spp.setDanga_Setting(danga);

            garow = m_barcode_garow.getText().toString();
            serow = m_barcode_serow.getText().toString();
            fontsize = m_barcode_codetype.getSelectedItemPosition();
            String narr = m_barcode_narr.getText().toString();
            String wide = m_barcode_wide.getText().toString();
            String high = m_barcode_high.getText().toString();
            rota = m_barcode_rota.getSelectedItemPosition();
            int numprint = m_barcode_numprint.getSelectedItemPosition();

            if (nullCheck(garow) || nullCheck(serow) || nullCheck(narr) || nullCheck(wide) || nullCheck(high)) {
                Toast.makeText(this, "셋팅설정이 잘못 입력되었습니다.", Toast.LENGTH_SHORT).show();
                m_barcode_garow.requestFocus();
                return;
            }

            String barcode = garow + "|" + serow + "|" + fontsize + "|" + narr + "|" + wide + "|" + high + "|" + rota + "|" + numprint;
            spp.setBarcode_Setting(barcode);

            //----------------------------------------//
            // 2021.01.05. 김영목. 원판매가,할인율,인쇄구분 추가
            //----------------------------------------//
            if (sellPriceYnCheckBox.isChecked()) {
                spp.setPrint_SellPrice_YN(1);
            } else {
                spp.setPrint_SellPrice_YN(0);
            }
            if (saleSellRateYnCheckBox.isChecked()) {
                spp.setPrint_SaleSellRate_YN(1);
            } else {
                spp.setPrint_SaleSellRate_YN(0);
            }

            //----------------------------------------//
            // 원판매가
            //----------------------------------------//
            garow = sellPriceWidthPointEditText.getText().toString();
            serow = sellPriceHeightPointEditText.getText().toString();
            fontsize = sellPriceFontSizeSpinner.getSelectedItemPosition();
            garoz = sellPriceWidthExpandSpinner.getSelectedItemPosition();
            seroz = sellPriceHeightExpandSpinner.getSelectedItemPosition();
            bold = sellPriceFontBoldSpinner.getSelectedItemPosition();
            rota = sellPriceFontRotateSpinner.getSelectedItemPosition();

            if (nullCheck(garow) || nullCheck(serow)) {
                Toast.makeText(this, "가로,세로를 확인해 주세요", Toast.LENGTH_SHORT).show();
                sellPriceWidthPointEditText.requestFocus();
                return;
            }

            String sellPrice = garow + "|" + serow + "|" + fontsize + "|" + garoz + "|" + seroz + "|" + bold + "|" + rota;
            spp.setSellPrice_Setting(sellPrice);
            //----------------------------------------//

            //----------------------------------------//
            // 할인율(%)
            //----------------------------------------//
            garow = saleSellRateWidthPointEditText.getText().toString();
            serow = saleSellRateHeightPointEditText.getText().toString();
            fontsize = saleSellRateFontSizeSpinner.getSelectedItemPosition();
            garoz = saleSellRateWidthExpandSpinner.getSelectedItemPosition();
            seroz = saleSellRateHeightExpandSpinner.getSelectedItemPosition();
            bold = saleSellRateFontBoldSpinner.getSelectedItemPosition();
            rota = saleSellRateFontRotateSpinner.getSelectedItemPosition();

            if (nullCheck(garow) || nullCheck(serow)) {
                Toast.makeText(this, "가로,세로를 확인해 주세요", Toast.LENGTH_SHORT).show();
                saleSellRateWidthPointEditText.requestFocus();
                return;
            }

            String saleSellRate = garow + "|" + serow + "|" + fontsize + "|" + garoz + "|" + seroz + "|" + bold + "|" + rota;
            spp.setSaleSellRate_Setting(saleSellRate);
            //----------------------------------------//
            //----------------------------------------//
            // 2021.07.07. 김영목. 1위치, 2품번, 3분류, 4추가항목
            //----------------------------------------//

            //----------------------------------------//
            // 위치
            //----------------------------------------//
            if (locationYnCheckBox.isChecked()) {
                spp.setPrint_Location_YN(1);
            } else {
                spp.setPrint_Location_YN(0);
            }
            garow = locationWidthPointEditText.getText().toString();
            serow = locationHeightPointEditText.getText().toString();
            fontsize = locationFontSizeSpinner.getSelectedItemPosition();
            garoz = locationWidthExpandSpinner.getSelectedItemPosition();
            seroz = locationHeightExpandSpinner.getSelectedItemPosition();
            bold = locationFontBoldSpinner.getSelectedItemPosition();
            rota = locationFontRotateSpinner.getSelectedItemPosition();

            if (nullCheck(garow) || nullCheck(serow)) {
                Toast.makeText(this, "가로,세로를 확인해 주세요", Toast.LENGTH_SHORT).show();
                locationWidthPointEditText.requestFocus();
                return;
            }

            String location = garow + "|" + serow + "|" + fontsize + "|" + garoz + "|" + seroz + "|" + bold + "|" + rota;
            spp.setLocation_Setting(location);
            //----------------------------------------//

            //----------------------------------------//
            // 품번
            //----------------------------------------//
            if (nickNameYnCheckBox.isChecked()) {
                spp.setPrint_NickName_YN(1);
            } else {
                spp.setPrint_NickName_YN(0);
            }
            garow = nickNameWidthPointEditText.getText().toString();
            serow = nickNameHeightPointEditText.getText().toString();
            fontsize = nickNameFontSizeSpinner.getSelectedItemPosition();
            garoz = nickNameWidthExpandSpinner.getSelectedItemPosition();
            seroz = nickNameHeightExpandSpinner.getSelectedItemPosition();
            bold = nickNameFontBoldSpinner.getSelectedItemPosition();
            rota = nickNameFontRotateSpinner.getSelectedItemPosition();

            if (nullCheck(garow) || nullCheck(serow)) {
                Toast.makeText(this, "가로,세로를 확인해 주세요", Toast.LENGTH_SHORT).show();
                nickNameWidthPointEditText.requestFocus();
                return;
            }

            String nickName = garow + "|" + serow + "|" + fontsize + "|" + garoz + "|" + seroz + "|" + bold + "|" + rota;
            spp.setNickName_Setting(nickName);
            //----------------------------------------//

            //----------------------------------------//
            // 분류
            //----------------------------------------//
            if (branchNameYnCheckBox.isChecked()) {
                spp.setPrint_BranchName_YN(1);
            } else {
                spp.setPrint_BranchName_YN(0);
            }
            garow = branchNameWidthPointEditText.getText().toString();
            serow = branchNameHeightPointEditText.getText().toString();
            fontsize = branchNameFontSizeSpinner.getSelectedItemPosition();
            garoz = branchNameWidthExpandSpinner.getSelectedItemPosition();
            seroz = branchNameHeightExpandSpinner.getSelectedItemPosition();
            bold = branchNameFontBoldSpinner.getSelectedItemPosition();
            rota = branchNameFontRotateSpinner.getSelectedItemPosition();

            if (nullCheck(garow) || nullCheck(serow)) {
                Toast.makeText(this, "가로,세로를 확인해 주세요", Toast.LENGTH_SHORT).show();
                branchNameWidthPointEditText.requestFocus();
                return;
            }

            String branchName = garow + "|" + serow + "|" + fontsize + "|" + garoz + "|" + seroz + "|" + bold + "|" + rota;
            spp.setBranchName_Setting(branchName);
            //----------------------------------------//

            //----------------------------------------//
            // 추가항목
            //----------------------------------------//
            if (addItemYnCheckBox.isChecked()) {
                spp.setPrint_AddItem_YN(1);
            } else {
                spp.setPrint_AddItem_YN(0);
            }
            garow = addItemWidthPointEditText.getText().toString();
            serow = addItemHeightPointEditText.getText().toString();
            fontsize = addItemFontSizeSpinner.getSelectedItemPosition();
            garoz = addItemWidthExpandSpinner.getSelectedItemPosition();
            seroz = addItemHeightExpandSpinner.getSelectedItemPosition();
            bold = addItemFontBoldSpinner.getSelectedItemPosition();
            rota = addItemFontRotateSpinner.getSelectedItemPosition();

            if (nullCheck(garow) || nullCheck(serow)) {
                Toast.makeText(this, "가로,세로를 확인해 주세요", Toast.LENGTH_SHORT).show();
                addItemWidthPointEditText.requestFocus();
                return;
            }

            String addItem = garow + "|" + serow + "|" + fontsize + "|" + garoz + "|" + seroz + "|" + bold + "|" + rota;
            spp.setAddItem_Setting(addItem);
            //----------------------------------------//


        } catch (NumberFormatException e) {
            e.printStackTrace();
            Toast.makeText(this, "입력이 잘못 되었습니다.", Toast.LENGTH_LONG).show();
            return;
        }

        if (dba.setBarPrint_SPPL3000(spp)) {
            Toast.makeText(this, "정상등록 되었습니다.", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "저장하지 못했습니다.", Toast.LENGTH_LONG).show();
        }
    }


    public void setReset(View view) {
        Toast.makeText(this, "설정을 초기화 합니다." + tips.getSELECTPRINT_GUBUN(), Toast.LENGTH_SHORT).show();
        switch (tips.getSELECTPRINT_GUBUN()) {
            case "30*58":
                spp.setLKP30_3058();
                break;
            case "23*40":
                spp.setLKP30_2340();
                break;
        }

        Log.d(TAG, tips.getSELECTPRINT_GUBUN() + "");
        setting_view();
    }


    //null 체크

    /**
     * null 및 공백 체크
     *
     * @param str String 입력
     * @return 입력된값이 "" : true, 있다면 false
     */
    private boolean nullCheck(String str) {
        if (str.equals("")) {
            return true;
        } else {
            return false;
        }
    }


    /**
     * 사용자 지정 true, 고정용지는  false
     */
    private void setPaperEnale(boolean enable) {

        m_paperheight.setEnabled(enable);
        m_paperwidth.setEnabled(enable);

        m_reset.setEnabled(!enable);


    }
}
