package tippsystem.adapter;

import java.util.ArrayList;

import kr.co.tipos.tips.R;
import android.R.integer;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ImgArrayAdapter extends ArrayAdapter<ImgInfoClass> {

	private ViewHolder viewHolder = null;
	private LayoutInflater inflater = null;
	//private ArrayList<ImgInfoClass> infoList = null;
	private Context mContext = null;	
	private Button selectAll;
	
	private boolean[] isCheckedConfrim;
	
	public ImgArrayAdapter(Context c, int textViewResourceId, ArrayList<ImgInfoClass> arrays, Button selectAll) {
		super(c, textViewResourceId, arrays);
		this.inflater = LayoutInflater.from(c);
		this.mContext = c;
		//체크함수
		this.isCheckedConfrim = new boolean[arrays.size()];
		this.selectAll = selectAll;
	}
	
	// CheckBox를 모두 선택하는 메서드
    public void setAllChecked(boolean ischeked) {
        int tempSize = isCheckedConfrim.length;
        for(int a=0 ; a<tempSize ; a++){
            isCheckedConfrim[a] = ischeked;
        }
    }

    public void setChecked(int position) {
        isCheckedConfrim[position] = !isCheckedConfrim[position];
    }

    public ArrayList<Integer> getChecked(){
        int tempSize = isCheckedConfrim.length;
        ArrayList<Integer> mArrayList = new ArrayList<Integer>();
        for(int b=0 ; b<tempSize ; b++){
            if(isCheckedConfrim[b]){
                mArrayList.add(b);
            }
        }
        return mArrayList;
    }	
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return super.getCount();		
	}
	
	@Override
	public ImgInfoClass getItem(int position) {
		// TODO Auto-generated method stub
		return super.getItem(position);
	}
	
	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return super.getItemId(position);
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		//return super.getView(position, convertView, parent);
		View v = convertView;
		
		if(v == null){
			viewHolder = new ViewHolder();
			v = inflater.inflate(R.layout.activity_imagelistview_row, parent, false);
			viewHolder.tv_title = (TextView)v.findViewById(R.id.tv_title);
			viewHolder.iv_image = (ImageView)v.findViewById(R.id.iv_image);	
			viewHolder.cb_box = (CheckBox)v.findViewById(R.id.cb_box);
			viewHolder.tv_filesize = (TextView)v.findViewById(R.id.tv_filesize);
			viewHolder.tv_filedate = (TextView)v.findViewById(R.id.tv_filedate);

			v.setTag(viewHolder);
			
		}else {
			viewHolder = (ViewHolder)v.getTag();
		}
		
		viewHolder.cb_box.setTag(position);
		viewHolder.cb_box.setChecked(isCheckedConfrim[position]);
		viewHolder.cb_box.setOnClickListener(buttonClickListener);
		
		viewHolder.tv_title.setText(getItem(position).title);
		
		viewHolder.iv_image.setOnClickListener(buttonClickListener);
		viewHolder.iv_image.setImageBitmap(getItem(position).image);
		viewHolder.iv_image.setTag(position);
		
		viewHolder.tv_filesize.setText(getItem(position).filesize);
		viewHolder.tv_filedate.setText(getItem(position).filedate);
		
		return v;
	}
	
	//아이템 추가하기  혹시나 필요 할까봐 설정해줍니다. 파일명 file[0]; 파일사이즈 file[1];	파일패스 file[2];
	public void addItem(String[] file, Bitmap bitmap){
		ImgInfoClass addInfo = null;
		addInfo = new ImgInfoClass(file, bitmap);
			
		this.add(addInfo);
		
	}
	
	public void remove(int position){
		//infoList.remove(position);
		//Log.w("목록", infoList.toString());
		this.remove(position);
	}
	
	public void setArrayList(ArrayList<ImgInfoClass> arrays){
		this.setArrayList(arrays);
	}
	
	public ArrayList<ImgInfoClass> getArrayList(){
		return this.getArrayList();
	}
	
	private View.OnClickListener buttonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			
			// 이미지 클릭
			/*case R.id.iv_image:
				Toast.makeText(
						mContext, 
						"이미지 Tag = " + v.getTag(),
						Toast.LENGTH_SHORT
						).show();
				break;*/
			
			// CheckBox
			case R.id.cb_box:
				
				 isCheckedConfrim[Integer.parseInt(v.getTag().toString())] = !isCheckedConfrim[Integer.parseInt(v.getTag().toString())];
				 
				 if(getChecked().size() > 0){
					 selectAll.setText(getChecked().size()+"개 선택");
				 }else{
					 selectAll.setText("전체 선택");
				 }
				 
				/*Toast.makeText(
						mContext, 
						"체크박스 Tag = " + v.getTag(),
						Toast.LENGTH_SHORT
						).show();*/
				break;

			default:
				break;
			}
		}
	};
	
	
	/*
	 * ViewHolder
	 */
	class ViewHolder{
		public TextView tv_title = null;
		public ImageView iv_image = null;	
		public CheckBox cb_box = null;
		public TextView tv_filesize = null;
		public TextView tv_filedate = null;
	}
	
	@Override
	protected void finalize() throws Throwable {
		free();
		super.finalize();
	}
	
	private void free(){
		inflater = null;
		//infoList = null;
		viewHolder = null;
		mContext = null;
	}
}
