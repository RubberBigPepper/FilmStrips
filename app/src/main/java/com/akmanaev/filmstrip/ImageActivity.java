package com.akmanaev.filmstrip;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.view.animation.TranslateAnimation;
import android.widget.ImageSwitcher;
import android.widget.ImageView;

import com.akmanaev.common.ZoomView;
//import com.vungle.publisher.VunglePub;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Vector;

public class ImageActivity extends Activity implements OnTouchListener, ImageSwitcher.ViewFactory
{
//	final VunglePub vunglePub = VunglePub.getInstance();

	private static final int MENU_START = 1;
	private static final int MENU_PREV = 2;
	private static final int MENU_NEXT = 3;
	private static final int MENU_CLOSE = 4;
	private static final int MENU_FAVORITES = 5;
	private static final int MENU_REMFAVORITES = 6;
	
	private ImageSwitcher m_cSwitcher;
	private Vector<byte[]> m_cArBmp=new Vector<byte[]>(); 
	private Vector<String> m_strArMp3=new Vector<String>();
	private int m_nCurIndex=-1;
	private DownLoadThread m_cDownloadThread=null;
	private PointF m_ptPres=null;
	private ZoomView m_cZoomView;
	private String m_strID="";//ID диафильма
	private MediaPlayer m_cPlayer=null;
	int m_nStartPage=0;
	int m_nBGColor=0;
	private Handler m_cHandler=new Handler();
	private boolean m_bMP3Exist=false;

	private Runnable m_cShowNextRunnable=new Runnable()
	{//это для перелистывания с задержкой после воспроизведения файла
		public void run()
		{
			m_cHandler.removeCallbacks(m_cShowNextRunnable);
			ShowNext();
		}
	};

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
       // setTheme(android.R.style.Theme_NoTitleBar_Fullscreen);
        		//Theme_Black_NoTitleBar_Fullscreen);
        
        setContentView(R.layout.image);
        
        m_cZoomView=(ZoomView)findViewById(R.id.progress1);
        m_cSwitcher=(ImageSwitcher)findViewById(R.id.imageSwitcher1);
//        m_cSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,android.R.anim.fade_in));
  //      m_cSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,android.R.anim.fade_out));
       
        m_nCurIndex=-1;
        
        m_cSwitcher.addView(makeView(), -1, -1);
        m_cSwitcher.addView(makeView(), -1, -1);
        
        /*m_cSwitcher.addView(new ImageView(this), -1, -1);
        m_cSwitcher.addView(new ImageView(this), -1, -1);*/
        m_cSwitcher.getNextView().setOnTouchListener(this);
        m_cSwitcher.showNext();
        m_cSwitcher.getNextView().setOnTouchListener(this);
        m_strID=this.getIntent().getStringExtra("ID");
        ReadImagesFromHTTP(m_strID);
        try
        {
        	if(CacheManager.m_nLastSeenId==Integer.valueOf(m_strID))
        		m_nCurIndex=CacheManager.m_nLastSeenPage;
        }
        catch (Exception e) 
        {
		}
        
        m_nBGColor=getResources().getColor(CacheManager.m_bBlackBackground?R.color.titlebackgroundcolorblack:R.color.titlebackgroundcolor);
        m_cSwitcher.setBackgroundColor(m_nBGColor);
        getWindow().getDecorView().setBackgroundColor(m_nBGColor);
    }

	@Override
	protected void onPause() {
		super.onPause();
	//	vunglePub.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	//	vunglePub.onResume();
	}
    
    public void onDestroy()
    {
    	super.onDestroy();
    	if(isFinishing())
    	{//останавливаем поток
			m_cHandler.removeCallbacks(m_cShowNextRunnable);
    		StopPlay();
    		try
    		{
    			if(m_cDownloadThread!=null)
    			{
    				m_cDownloadThread.setStop();
    				m_cDownloadThread.join();
    			}
    		}
    		catch(Exception ex){}
    		try
    		{
    			CacheManager.m_nLastSeenId=Integer.parseInt(m_strID);
    			CacheManager.m_nLastSeenPage=m_nCurIndex;
    			if(CacheManager.m_nLastSeenPage>=m_cArBmp.size()-2)
    				CacheManager.m_nLastSeenPage=0;//считаем, что просмотрели весь диафильм
    		}
    		catch(Exception ex){}
    	}
    }
    
    private void ShowNext()
    {
    	switch(CacheManager.m_nSwype)
		{
		case 0:
			SetAnimation(0);
			SetImage(m_nCurIndex+1,true);
			break;
		case 3:
			SetAnimation(3);
			SetImage(m_nCurIndex+1,true);
			break;
		case 1:
			SetAnimation(1);
			SetImage(m_nCurIndex-1,false);
			break;
		default:
			SetAnimation(2);
			SetImage(m_nCurIndex-1,false);
			break;
		}
		/*if(CacheManager.AUTO_LIST&&m_cArBmp.get(m_nCurIndex)!=null&&m_bMP3Exist)//включено автоперелистывание и битмапа уже загружена, тогда включаем отсчет
		{
			m_cHandler.removeCallbacks(m_cShowNextRunnable);
			m_cHandler.postDelayed(m_cShowNextRunnable,3000l);
		}*/
    }
    
    private void ShowPrev()
    {
		switch(CacheManager.m_nSwype)
		{
		case 1:
			SetAnimation(0);
			SetImage(m_nCurIndex+1,true);
			break;
		case 2:
			SetAnimation(3);
			SetImage(m_nCurIndex+1,true);
			break;
		case 3:
			SetAnimation(2);
			SetImage(m_nCurIndex-1,false);
			break;
		default:
			SetAnimation(1);
			SetImage(m_nCurIndex-1,false);
			break;
		}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) 
    {
    	switch(keyCode)
    	{
    	case KeyEvent.KEYCODE_VOLUME_UP:
    		if(CacheManager.USE_VOLUME)
    		{
    			ShowPrev();
        		return true;
    		}
    		break;
    	case KeyEvent.KEYCODE_PAGE_UP:
    		if(CacheManager.USE_PAGE)
    		{
    			ShowPrev();
        		return true;
    		}
    		break;
    	case KeyEvent.KEYCODE_DPAD_UP:
    	case KeyEvent.KEYCODE_DPAD_LEFT:
    		if(CacheManager.USE_CURSOR)
    		{
    			ShowPrev();
	    		return true;
    		}
    		break;
    	case KeyEvent.KEYCODE_VOLUME_DOWN:
    		if(CacheManager.USE_VOLUME)
    		{
    			ShowNext();
        		return true;
    		}
    		break;
    	case KeyEvent.KEYCODE_PAGE_DOWN:
    		if(CacheManager.USE_PAGE)
    		{
    			ShowNext();
        		return true;
    		}
    		break;
    	case KeyEvent.KEYCODE_DPAD_DOWN:
    	case KeyEvent.KEYCODE_DPAD_RIGHT:
    		if(CacheManager.USE_CURSOR)
    		{
    			ShowNext();
	    		return true;
    		}
    		break;
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) 
    {
    	switch(keyCode)
    	{
    	case KeyEvent.KEYCODE_VOLUME_UP:
    	case KeyEvent.KEYCODE_VOLUME_DOWN:
    		if(CacheManager.USE_VOLUME)
        		return true;
    		break;
    	}
    	return super.onKeyUp(keyCode, event);
    }
    
    private void SetImage(int nIndex, boolean bNext) {
        m_cHandler.removeCallbacks(m_cShowNextRunnable);
        if (nIndex >= m_cArBmp.size() || nIndex < 0) {
            Log.e("Filmscope", "index out of band");
            return;//индекс вне диапазона
        }
        byte[] byArData = m_cArBmp.get(nIndex);
        if (byArData == null) {
            Log.e("Filmscope", "frame did not load yet");
            m_cSwitcher.setImageResource(R.drawable.loading);
            m_nCurIndex = nIndex;
            m_cZoomView.setCurrent(m_nCurIndex + 1);
            return;
        }
        BitmapFactory.Options cOpt = new BitmapFactory.Options();
        cOpt.inJustDecodeBounds = true;
        try {
            BitmapFactory.decodeByteArray(byArData, 0, byArData.length, cOpt);
        } catch (OutOfMemoryError er) {
        }
        cOpt.inSampleSize = 1;
        try {
            cOpt.inSampleSize = Math.max(1, Math.max(cOpt.outWidth / m_cSwitcher.getWidth(), cOpt.outHeight / m_cSwitcher.getHeight()));
        }
        catch (Exception ex){}

    	cOpt.inJustDecodeBounds=false;
    	Bitmap cBmp=null;
    	while(cOpt.inSampleSize<=16)
    	{
    		try
    		{
    			cBmp=BitmapFactory.decodeByteArray(byArData, 0, byArData.length,cOpt);
    			if(cBmp!=null)
    				break;
    		}
    		catch(OutOfMemoryError er)
    		{//если ошибка декодирования по нехватке памяти-уменьшим размер битмапы и попробуем снова
    			cOpt.inSampleSize*=2;
    		}
    	}
    	if(cBmp==null)
    	{
    		Log.e("Filmscope","frame decode error");
    		m_nCurIndex=nIndex;
    		return;
    	}
    	
    	Drawable cDrawable=new BitmapDrawable(cBmp);
    	m_cSwitcher.setImageDrawable(cDrawable);
    	m_nCurIndex=nIndex;
    	m_cZoomView.setCurrent(m_nCurIndex+1);
    	if(nIndex<m_strArMp3.size()&&nIndex>=0&&m_strArMp3.get(nIndex).length()>0)
    		PlayMP3(m_strArMp3.get(nIndex));
    	else
        {
            StopPlay();
			if(bNext)
				AutoScrollFrame();
        }
    }
    
	public void ReadImagesFromHTTP(String strID)
	{
		m_cDownloadThread=new DownLoadThread(strID);
		m_cDownloadThread.start();
	}
	
	public boolean onTouch(View v, MotionEvent event) 
	{
		if(event.getAction()==MotionEvent.ACTION_DOWN)
		{
			m_ptPres=new PointF(event.getX(),event.getY());
			Log.e("Filmscope","action down");
		}
		if(event.getAction()==MotionEvent.ACTION_MOVE&&m_ptPres!=null)
		{
			float fYShift=event.getY()-m_ptPres.y;
			float fXShift=event.getX()-m_ptPres.x;
			if(CacheManager.m_nSwype>1)
			{//по горизонтали
				if(Math.abs(fXShift)>m_cSwitcher.getWidth()/8)
				{
					if(fXShift>0)
					{//слева-направо
						SetAnimation(3);
						if(CacheManager.m_nSwype==3)
							SetImage(m_nCurIndex+1,true);
						else
							SetImage(m_nCurIndex-1,false);
					}
					if(fXShift<0)
					{//справа-налево
						SetAnimation(2);
						if(CacheManager.m_nSwype==2)
							SetImage(m_nCurIndex+1,true);
						else
							SetImage(m_nCurIndex-1,false);
					}
					m_ptPres=null;
				}
				else if(Math.abs(fYShift)>m_cSwitcher.getHeight()/4)
					finish();//иначе свайп на выход
			}
			else
			{//по вертикали
				if(Math.abs(fYShift)>m_cSwitcher.getHeight()/4)
				{//только если более трети прошли переключаем
					if(fYShift>0)
					{//сверху-вниз
						SetAnimation(1);
						if(CacheManager.m_nSwype==1)
							SetImage(m_nCurIndex+1,true);
						else
							SetImage(m_nCurIndex-1,false);
					}
					if(fYShift<0)
					{//снизу вверх
						SetAnimation(0);						
						if(CacheManager.m_nSwype==0)
							SetImage(m_nCurIndex+1,true);
						else
							SetImage(m_nCurIndex-1,false);
					}
					m_ptPres=null;
				}
                else if(Math.abs(fXShift)>m_cSwitcher.getWidth()/8)
                    finish();//иначе свайп на выход
			}
		}
		return true;
	}
	
	private void SetAnimation(int nType)
	{
		TranslateAnimation slide = null;
		switch(nType)
		{
		case 1://снизу-вверх
			slide = new TranslateAnimation(0, 0,0,m_cSwitcher.getHeight() );   
		    slide.setDuration(500);   
		    m_cSwitcher.setOutAnimation(slide);
		    slide = new TranslateAnimation(0, 0, -m_cSwitcher.getHeight(),0);   
		    slide.setDuration(500);   
		    m_cSwitcher.setInAnimation(slide);		    
		    break;
		case 2://справа-налево
			slide = new TranslateAnimation(0,-m_cSwitcher.getWidth(),0,0);   
		    slide.setDuration(500);   
		    m_cSwitcher.setOutAnimation(slide);
		    slide = new TranslateAnimation(m_cSwitcher.getWidth(), 0, 0,0);   
		    slide.setDuration(500);   
		    m_cSwitcher.setInAnimation(slide);
		    break;
		case 3://Слева-направо
			slide = new TranslateAnimation(0,m_cSwitcher.getWidth(),0,0);   
		    slide.setDuration(500);   
		    m_cSwitcher.setOutAnimation(slide);
		    slide = new TranslateAnimation(-m_cSwitcher.getWidth(), 0, 0,0);   
		    slide.setDuration(500);   
		    m_cSwitcher.setInAnimation(slide);
		    break;
		default://сверху-вниз
			slide = new TranslateAnimation(0, 0,0,-m_cSwitcher.getHeight());   
		    slide.setDuration(500);   
		    m_cSwitcher.setOutAnimation(slide);
		    slide = new TranslateAnimation(0, 0, m_cSwitcher.getHeight(),0);   
		    slide.setDuration(500);   
		    m_cSwitcher.setInAnimation(slide);
			break;
		}
	}
	
	@Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {//пїЅпїЅпїЅпїЅпїЅпїЅпїЅ пїЅпїЅпїЅпїЅ
		menu.clear();
       menu.add(0, MENU_START, 0, R.string.MenuStart);
       menu.add(0, MENU_PREV, 0, R.string.MenuPrev);
       menu.add(0, MENU_NEXT, 0, R.string.MenuNext);
       SharedPreferences cPrefs=getSharedPreferences("common",0/*Context.MODE_PRIVATE*/);
       String[]strArSet=null;
       try
       {
       	strArSet=cPrefs.getString("Favorites", "").split(",");
       }
       catch(Exception ex){}
       HashSet<String>strArFav=new HashSet<String>();
       if(strArSet!=null)
       {
    	   for(int n=0;n<strArSet.length;n++)
    		   strArFav.add(strArSet[n]);
       }
       if(!strArFav.contains(m_strID))//нет в избранных
    	   menu.add(0, MENU_FAVORITES, 0, R.string.AddToFavorites);
       else
    	   menu.add(0, MENU_REMFAVORITES, 0, R.string.DelFromFavorites);
       menu.add(0, MENU_CLOSE, 0, R.string.MenuExit);
 	   return true;
    }
	
	public boolean onOptionsItemSelected(MenuItem item) 
    {
		switch(item.getItemId())
    	{
    	case MENU_START:
            m_cSwitcher.setInAnimation(AnimationUtils.loadAnimation(this,android.R.anim.fade_in));
    		m_cSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this,android.R.anim.fade_out));
    		SetImage(0,false);
    		return true;
    	case MENU_PREV:
    		switch(CacheManager.m_nSwype)
    		{
    		case 1://сверху-вниз
    			SetAnimation(0);//снизу-вверх
    			break;
    		case 2://справа-налево
    			SetAnimation(3);//слева-направо
    			break;
    		case 3://слева-направо
    			SetAnimation(2);//справа-налево
    			break;
    		default:
    			SetAnimation(1);//сверху-вниз
    			break;
    		}
    		SetImage(m_nCurIndex-1,false);
    		return true;
    	case MENU_NEXT:
    		SetAnimation(CacheManager.m_nSwype);
    		SetImage(m_nCurIndex+1,true);
    		return true;
    	case MENU_CLOSE:
    		finish();
    		return true;
    	case MENU_FAVORITES:
    		{
    			SharedPreferences cPrefs=getSharedPreferences("common",Context.MODE_PRIVATE);
    			String[]strArSet=null;
    	        try
    	        {
    	        	strArSet=cPrefs.getString("Favorites", "").split(",");
    	        }
    	        catch(Exception ex){}
		       HashSet<String>strArFav=new HashSet<String>();
		       if(strArSet!=null)
		       {
		    	   for(int n=0;n<strArSet.length;n++)
		    		   strArFav.add(strArSet[n]);
		       }
	            Log.e("FilmStrips",strArFav.toString());
	            if(!strArFav.contains(m_strID))
	            {//нет такого в избранном
	            	strArFav.add(m_strID);
	            	SharedPreferences.Editor cEditor=cPrefs.edit();
	            	StringBuilder cBuilder=new StringBuilder();    		
	        		Iterator<String> cIter=strArFav.iterator();
	        		while(cIter.hasNext())
	        		{
	        			if(cBuilder.length()==0)
	        				cBuilder.append(cIter.next());
	        			else
	        			{
	        				cBuilder.append(",");
	        				cBuilder.append(cIter.next());
	        			}
	        		}
	        		cEditor.putString("Favorites", cBuilder.toString());
	            	cEditor.commit();
	            	Log.e("FilmStrips",strArFav.toString());
	            	
	            }
	    		return true;
    		}
    	case MENU_REMFAVORITES:
	    	{
	    		SharedPreferences cPrefs=getSharedPreferences("common",Context.MODE_PRIVATE);
	    		String[]strArSet=null;
	            try
	            {
	            	strArSet=cPrefs.getString("Favorites", "").split(",");
	            }
	            catch(Exception ex){}
    	       HashSet<String>strArFav=new HashSet<String>();
    	       if(strArSet!=null)
    	       {
    	    	   for(int n=0;n<strArSet.length;n++)
    	    		   strArFav.add(strArSet[n]);
    	       }
	            Log.e("FilmStrips",strArFav.toString());
	            if(strArFav.contains(m_strID))
	            {//нет такого в избранном
	            	strArFav.remove(m_strID);
	            	SharedPreferences.Editor cEditor=cPrefs.edit();
	            	StringBuilder cBuilder=new StringBuilder();    		
	        		Iterator<String> cIter=strArFav.iterator();
	        		while(cIter.hasNext())
	        		{
	        			if(cBuilder.length()==0)
	        				cBuilder.append(cIter.next());
	        			else
	        			{
	        				cBuilder.append(",");
	        				cBuilder.append(cIter.next());
	        			}
	        		}
	        		cEditor.putString("Favorites", cBuilder.toString());
	            	cEditor.commit();
	            	Log.e("FilmStrips",strArFav.toString());
	            	
	            }
	    		return true;
	    	}
    	}
    	return false;
    }
	
	private class DownLoadThread extends Thread
	{
		private volatile boolean m_bStop=false;
		private String m_strID;
		private Vector<OneFileDownLoad> m_cArDLThreads=new Vector<OneFileDownLoad>(); 
		
		public DownLoadThread(String strID)
		{
			m_strID=strID;
		}
		
		public void setStop()
		{
			m_bStop=true;
		}
		
		public void run()
		{
			//String strURL="http://www.diafilmy.su/dia.php?id="+m_strID;
			String strURL="https://www.diafilmy.su/dia-android.php?id="+m_strID;
			Log.e("Filmscope","Reading "+strURL);
			//MyXMLElement cRoot=MyXMLElement.ReadFromHTTP(strURL);
			String strFile=CacheManager.CopyToCache(strURL,false);
			Log.e("Filmscope","loading file "+strFile);
			MyXMLElement cRoot=null;
			if(strFile!=null&&strFile.length()>0)
				cRoot=MyXMLElement.ReadFromFile(strFile);
			else
				cRoot=MyXMLElement.ReadFromHTTP(strURL);
			CacheManager.AdjustCacheSize();
			m_cArBmp.clear();
			if(cRoot==null)
				return;
			cRoot=cRoot.getChildAt(0);
			if(cRoot==null)
				return;
			for(int n=0;n<cRoot.getChildCount();n++)
			{//ищем imgs
				if(m_bStop)
					break;
				MyXMLElement cChild=cRoot.getChildAt(n);
				if(!cChild.getName().equalsIgnoreCase("imgs"))
					continue;
				int nChildCount=cChild.getChildCount();
				for(int m=0;m<nChildCount;m++)
				{
					MyXMLElement cImg=cChild.getChildAt(m);//берем img
					if(m_bStop)
						break;
					if(cImg.getName().equalsIgnoreCase("img"))
					{
						m_cArBmp.add(null);
						m_strArMp3.add("");
					}
				}
				Log.e("Filmscope",String.format("imgs found, count=%d",m_cArBmp.size()));
				m_cZoomView.setMax(m_cArBmp.size());
				int nIndex=0;
				int nMp3Index=0;
				for(int m=0;m<nChildCount;m++)
				{
					if(m_bStop)
						break;
					MyXMLElement cImg=cChild.getChildAt(m);//берем img
					if(cImg.getName().equalsIgnoreCase("mp3"))
					{
						Log.e("Fimpscope","Adding mp3 "+cImg.getContent());
						//m_strArMp3.set(nIndex,cImg.getContent());
						m_strArMp3.set(nMp3Index,cImg.getContent());
						nMp3Index++;
					}
					if(cImg.getName().equalsIgnoreCase("img"))
					{
						while(m_cArDLThreads.size()>4&&!m_bStop)
						{//придется ждать
							try
							{
								Thread.sleep(500);//ждем полсекунды
							}
							catch(Exception ex){}	
						}
						//if(m_cArDLThreads.size()<4)//можно добавить еще поток для загрузки
						{
							m_cArDLThreads.add(new OneFileDownLoad(cImg.getContent(),nIndex));
							while(nMp3Index<nIndex)
								nMp3Index++;
							nIndex++;
						}
						
					}
				}
				break;
			}
			while(m_cArDLThreads.size()>0)
			{
				try
				{
					Thread.sleep(500);//ждем полсекунды
				}
				catch(Exception ex){}
			}
			Log.e("Filmscope","Readed "+strURL);
		}	
		
		private class OneFileDownLoad extends Thread
		{
			private String m_strFileName="";//адрес с которого грузим
			private int m_nIndex=-1;//индекс в массиве, в который грузим

			public OneFileDownLoad(String strFileName, int nIndex)
			{
				m_strFileName=strFileName;
				m_nIndex=nIndex;
				start();
			}

        	public void run()
			{
				int nAttempt=0;
				while(nAttempt<5&&!m_bStop)//10 попыток даем
				{
					byte[]byArData=CacheManager.ReadFile(m_strFileName, false);
					if(byArData!=null)
					{//кадр загружен
						m_cArBmp.set(m_nIndex,byArData);
						//if(m_nCurIndex==-1)
						{
							runOnUiThread(new Runnable()
							{
								public void run() 
								{
									if(m_nIndex==m_nCurIndex||m_nCurIndex==-1&&m_nIndex==0)
									{
                                        SetImage(m_nIndex,false);
                                      //  AutoScrollFrame();
                                    }
								}							
							});
						}
						m_cZoomView.setProgress(m_nIndex);
						break;
					}
					else
						nAttempt++;					
				}
				m_cArDLThreads.remove(this);
			}
		}
		
	}
	private void StopPlay()
	{
		if(m_cPlayer!=null)
		{
			try
			{
					m_cPlayer.stop();
			}
			catch(Exception ex){}
			try
			{
					m_cPlayer.reset();
			}
			catch(Exception ex){}
			try
			{
					m_cPlayer.release();
			}
			catch(Exception ex){}
			m_cPlayer=null;
		}
	}

	private boolean IsPlayingMP3() {
        try {
            if (m_cPlayer != null)
                return m_cPlayer.isPlaying();
        }
        catch (Exception ex){}
        return  false;
    }

	private void PlayMP3(String strURL)
	{
		Log.e("Filmoscope","Playing "+strURL);
		//Uri uri=getUriFromUrl(strURL);
		//if(uri==null)
			//return;
        StopPlay();
		if(strURL.length()>0)
		{
			StopPlay();
			try
			{
				m_cPlayer=new MediaPlayer();
				m_cPlayer.setOnPreparedListener(new OnPreparedListener()
				{
					public void onPrepared(MediaPlayer mp) 
					{
						try
						{
							m_cPlayer.start();
                            m_bMP3Exist=true;
						}
						catch(Exception ex){}
					}
				});
				m_cPlayer.setDataSource(strURL);				
				m_cPlayer.prepareAsync();
				m_cPlayer.setOnCompletionListener(new OnCompletionListener() 
				{				
					public void onCompletion(MediaPlayer mp) 
					{//закончили читать, перелистываем
						StopPlay();
						if(CacheManager.AUTO_LIST)//включено автоперелистывание, включаем задержку 1 секунду для этого
						{
							m_cHandler.removeCallbacks(m_cShowNextRunnable);
							m_cHandler.postDelayed(m_cShowNextRunnable,500L);
						}
	//					ShowNext();
					}
				});
				m_cHandler.removeCallbacks(m_cShowNextRunnable);
			}
			catch(Exception ex)
			{
				ex.printStackTrace();
                AutoScrollFrame();
			}
		}
	}

	private void AutoScrollFrame()
    {
        if(CacheManager.AUTO_LIST&&m_cArBmp.get(m_nCurIndex)!=null&&m_bMP3Exist)//включено автоперелистывание и битмапа уже загружена, тогда включаем отсчет
        {
            m_cHandler.removeCallbacks(m_cShowNextRunnable);
            m_cHandler.postDelayed(m_cShowNextRunnable,3000l);
        }
    }

	public View makeView() 
	{
		ImageView i = new ImageView(this);
	    i.setBackgroundColor(m_nBGColor);
	    try {
			i.setImageResource(R.drawable.loading);
		}
		catch (OutOfMemoryError oom){}
	    if(CacheManager.STRETCH_TO_FULLSCREEN)
	    	i.setScaleType(ImageView.ScaleType.FIT_XY);
	    else
	    	i.setScaleType(ImageView.ScaleType.FIT_CENTER);
	    i.setLayoutParams(new ImageSwitcher.LayoutParams(ImageSwitcher.LayoutParams.FILL_PARENT, ImageSwitcher.LayoutParams.FILL_PARENT));
	    return i;
	}
}
