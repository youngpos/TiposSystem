package tipsystem.tips;

import java.util.HashMap;
import java.util.List;

import tipsystem.utils.StringFormat;
import kr.co.tipos.tips.R;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


class CompareListAdapterReNew extends BaseAdapter 
{
	Context ctx;
	int itemLayout;
	
	private List<HashMap<String, String>> object ;
	public CompareListAdapterReNew(Context ctx, int itemLayout, List<HashMap<String, String>> object)
	{
		super();
		this.ctx = ctx;
		this.itemLayout = itemLayout;
		this.object = object;
	}
	@Override
	public int getCount() {
		return object.size();
	}
	@Override
	public Object getItem(int position) {
		return object.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		if ( convertView == null ) {
			LayoutInflater inflater = LayoutInflater.from(this.ctx);
			convertView = inflater.inflate(this.itemLayout, parent, false);
		}
		HashMap<String, String> obj = object.get(position);
		
		
		//레이어에 있는 컨테이너 불러오기
		TextView barcode = (TextView) convertView.findViewById(R.id.item_barcode); //바코드
		TextView gname  = (TextView) convertView.findViewById(R.id.item_gname);  //상품명
		
		TextView pur  = (TextView) convertView.findViewById(R.id.item_purpri);			//매입가
		TextView minpur = (TextView) convertView.findViewById(R.id.item_minpurpri);	//최소매입가
		TextView maxpur = (TextView) convertView.findViewById(R.id.item_maxpurpri); //최대매입가
		TextView avgpur = (TextView) convertView.findViewById(R.id.item_avgpurpri);	//평균매입가
		TextView marpur = (TextView) convertView.findViewById(R.id.item_marginpurpri);	//매입가 - 평균가 차액
		
		TextView sell  = (TextView) convertView.findViewById(R.id.item_sellpri);			//판매가
		TextView minsell = (TextView) convertView.findViewById(R.id.item_minsellpri);	//최소판매가
		TextView maxsell = (TextView) convertView.findViewById(R.id.item_maxsellpri);	//최대판매가
		TextView avgsell = (TextView) convertView.findViewById(R.id.item_avgsellpri);	//평균판매가
		TextView marsell = (TextView) convertView.findViewById(R.id.item_marginsellpri);	//판매가 - 평균판매가 = 차액
		
		barcode.setText(obj.get("BarCode"));
		gname.setText(obj.get("G_Name"));
		
		//매입가
		pur.setText(StringFormat.convertT00NumberFormat(obj.get("Pur_Pri")).trim());
		minpur.setText(StringFormat.convertT00NumberFormat(obj.get("최소매입가")).trim());
		maxpur.setText(StringFormat.convertT00NumberFormat(obj.get("최대매입가")).trim());
		marpur.setText(StringFormat.convertT00NumberFormat(obj.get("Pur_Pri_dif")).trim());
		avgpur.setText(StringFormat.convertT00NumberFormat(obj.get("평균매입가")).trim());
				
		//판매가Sell_Pri_inc
		sell.setText(StringFormat.convertToNumberFormat(obj.get("Sell_Pri")).trim());
		minsell.setText(StringFormat.convertToNumberFormat(obj.get("최소판매가")).trim());
		maxsell.setText(StringFormat.convertToNumberFormat(obj.get("최대판매가")).trim());
		marsell.setText(StringFormat.convertT00NumberFormat(obj.get("Sell_Pri_dif")).trim());
		avgsell.setText(StringFormat.convertT00NumberFormat(obj.get("평균판매가")).trim());
		
		
		ImageView pur_arrow  = (ImageView) convertView.findViewById(R.id.arrow1);
		ImageView sell_arrow  = (ImageView) convertView.findViewById(R.id.arrow2);
		
		
		String Pur_Pri_inc = obj.get("Pur_Pri_inc");
		pur_arrow.setVisibility(View.VISIBLE);
		if (Pur_Pri_inc.equals("+")) pur_arrow.setImageResource(R.drawable.icon_uparrow);
		else if (Pur_Pri_inc.equals("-")) pur_arrow.setImageResource(R.drawable.icon_parte_arrow);
		else pur_arrow.setVisibility(View.INVISIBLE);
		
		
		String Sell_Pri_inc = obj.get("Sell_Pri_inc");
		sell_arrow.setVisibility(View.VISIBLE);
		if (Sell_Pri_inc.equals("+")) sell_arrow.setImageResource(R.drawable.icon_uparrow);
		else if (Sell_Pri_inc.equals("-")) sell_arrow.setImageResource(R.drawable.icon_parte_arrow);
		else sell_arrow.setVisibility(View.INVISIBLE);
		
		return convertView;
	}
}