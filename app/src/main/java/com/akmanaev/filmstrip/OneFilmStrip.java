package com.akmanaev.filmstrip;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.Vector;

public class OneFilmStrip 
{//информация о диафильме
	protected Vector<String>m_strArCat=new Vector<String>(); 
	protected String m_strID;	
	protected String m_strURL;
	protected String m_strYear;
	protected String m_strTitle;	
	protected String m_strImageURL;
	protected String m_strStudio;
	protected Bitmap m_cBmpIcon;
	protected long m_lNewsDate=0;//дата добавления
	protected String m_strAdding;//дата добавления в виде строки
	
	public void LoadIcon()
	{
		try
		{
			if(m_cBmpIcon==null)
			{//попробуем через шорткат
				String strURL=m_strImageURL;
				int nPos=strURL.lastIndexOf(".");
				if(nPos!=-1)
				{
					strURL=strURL.substring(0,nPos)+"-thumb-samsung-tv-medium.jpg";
					strURL=strURL.toLowerCase().replace("/uploads/","/thumbs/");
					Bitmap cBmp=LoadIconInternal(strURL,true);
					if (cBmp != null)
					{
						m_cBmpIcon=cBmp;
						return;
					}
				}
				m_cBmpIcon=LoadIconInternal(m_strImageURL,false);//иначе по-старому
			}
		}
		catch(Exception ex){}
	}

	private Bitmap LoadIconInternal(String strURL, boolean bThumb)
	{
		try
		{
			if(m_cBmpIcon==null)
			{//попробуем через шорткат
				byte[] byArData=CacheManager.ReadFile(strURL, false);
				BitmapFactory.Options cOpt=new BitmapFactory.Options();
				cOpt.inJustDecodeBounds=true;
				BitmapFactory.decodeByteArray(byArData, 0, byArData.length, cOpt);
				if(!bThumb)
					cOpt.inSampleSize=Math.max(Math.max(cOpt.outWidth/48, cOpt.outHeight/48),1);
				cOpt.inJustDecodeBounds=false;
				return BitmapFactory.decodeByteArray(byArData, 0, byArData.length,cOpt);
			}
		}
		catch (OutOfMemoryError oom){}
		catch(Exception ex){}
		return  null;
	}
}
