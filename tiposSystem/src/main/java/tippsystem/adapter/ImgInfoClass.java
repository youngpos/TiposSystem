package tippsystem.adapter;

import android.graphics.Bitmap;

public class ImgInfoClass {
	public String title;
	public String filesize;
	public String filedate;
	public String filepath;
	//public Drawable image;	
	public Bitmap image;
	
	public ImgInfoClass() { }
	
	public ImgInfoClass(String[] title, Bitmap image){
		
	//public ImgInfoClass(String title, Drawable image, Bitmap bit_image){		
		this.title = title[0];
		this.filepath = title[1];
		this.filesize = title[2];
		this.filedate = title[3];
		//this.image = image;	
		this.image = image;
	}
}
