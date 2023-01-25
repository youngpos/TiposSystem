package tipsystem.tips;

import java.util.HashMap;
import java.util.List;

import kr.co.tipos.tips.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


class CompareListAdapter extends BaseAdapter 
{
	Context ctx;
	int itemLayout;
	
	private List<HashMap<String, String>> object ;
	public CompareListAdapter(Context ctx, int itemLayout, List<HashMap<String, String>> object)
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
		TextView barcode = (TextView) convertView.findViewById(R.id.item1);
		TextView gname  = (TextView) convertView.findViewById(R.id.item2);
		TextView pur  = (TextView) convertView.findViewById(R.id.item3);
		TextView sell  = (TextView) convertView.findViewById(R.id.item4);
		
		barcode.setText(obj.get("BarCode"));
		gname.setText(obj.get("G_Name"));
		pur.setText(obj.get("Pur_Pri_dif"));
		sell.setText(obj.get("Sell_Pri_dif"));
		
		
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