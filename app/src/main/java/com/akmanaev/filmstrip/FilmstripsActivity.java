package com.akmanaev.filmstrip;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.format.Time;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

/*import com.vungle.publisher.VungleAdEventListener;
import com.vungle.publisher.VungleInitListener;
import com.vungle.publisher.VunglePub;*/
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.yandex.mobile.ads.AdRequest;
import com.yandex.mobile.ads.AdRequestError;
import com.yandex.mobile.ads.InterstitialAd;
import com.yandex.mobile.ads.InterstitialEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class FilmstripsActivity extends Activity implements OnChildClickListener,TextWatcher, OnScrollListener  
{
	public static final String TAG="com.Filmscope";

	private static final int MENU_EXIT 				= 1;
	private static final int MENU_SETTINGS 			= 2;
	private static final int MENU_SORTTITLE 		= 3;
	private static final int MENU_SORTYEAR 			= 4;
	private static final int MENU_SORTSTUDIO 		= 5;
	private static final int MENU_SORTADDINGDATE	= 6;
	
	private static final int CAT_FAVORITES= -1;
	private static final int CAT_ALL= -2;
	private static final int CAT_NEW= -3;

	private  static final int PERMISSION_REQUEST_CODE=23;
	
	
	private ExpandableListView m_cListView;
	private Vector<OneFilmStrip> m_cArFilms=new Vector<OneFilmStrip>(); 
	private Vector<String> m_strArCat=new Vector<String>();//категории 
	private List<Map<String, ?>>m_cArGroups;
	private List<List<Map<String, ?>>> m_cArChilds=new ArrayList<List<Map<String, ?>>>();//данные
	private List<List<Map<String, ?>>> m_cArChildsOrig=new ArrayList<List<Map<String, ?>>>();//данные
	private DownLoadThread m_cDownloadThread=null;	
	private Set<String> m_strArIDFavorites=new HashSet<String>();
	
	private EditText m_cEditTextSearch;
	private SearchTask m_cSearchTask=null;
	private String m_strSearch="";
	
	private int m_nLastID=-1;

	/*final VunglePub vunglePub = VunglePub.getInstance();
	final String app_id = "59a3aed07f2141151d00156d";
	static final String DEFAULT_PLACEMENT_ID = "DEFAULT38282";
    private final String[] placement_list = { DEFAULT_PLACEMENT_ID, placementIdForLevel};
	static final String placementIdForLevel = "all";
    static boolean VungleInit=false;*/

	private AdRequest mAdRequest;
	static InterstitialAd mInterstitialAd;

    private InterstitialEventListener mInterstitialAdEventListener = new InterstitialEventListener.SimpleInterstitialEventListener() {

        @Override
        public void onInterstitialLoaded()
        {
            //mInterstitialAd.show();
            Log.e(FilmstripsActivity.TAG,"AD loaded");
//            m_cBtnShowAd.setVisibility(View.VISIBLE);
        }

        @Override
        public void onInterstitialFailedToLoad(AdRequestError error) {
            Log.e(FilmstripsActivity.TAG,"AD failed to load "+error.getDescription());
  //          m_cBtnShowAd.setVisibility(View.GONE);
        }
    };

    /** Called when the activity is first created. */
    @SuppressLint("NewApi")
	@Override
    public void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.main);
        setProgressBarIndeterminateVisibility(true);       
        m_cListView=(ExpandableListView)findViewById(R.id.expandableListView1);        
        m_cEditTextSearch=(EditText)findViewById(R.id.editTextSearch);
        m_cListView.setOnScrollListener(this);
        m_cEditTextSearch.addTextChangedListener(this);
        m_cListView.setOnChildClickListener(this);
        registerForContextMenu(m_cListView);
        ReadSettings();
		m_cDownloadThread=new DownLoadThread();

        initInterstitialAd();
/*
		vunglePub.init(this, app_id, placement_list, new VungleInitListener()
		{
			public void onSuccess()
			{
				Log.e(TAG, "vungle success");
				vunglePub.clearAndSetEventListeners(vungleDefaultListener);
				vunglePub.loadAd(DEFAULT_PLACEMENT_ID);
                VungleInit=true;
			}
			public void onFailure(Throwable e)
			{
				Log.e(TAG, "vungle fail "+e.getMessage());
			}
		});*/
		RequestPermissionOrLoadData();
		//m_cEditTextSearch.setInputType(0);
		//InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		//imm.hideSoftInputFromWindow(m_cEditTextSearch.getWindowToken(),  InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void initInterstitialAd() {
        mInterstitialAd = new InterstitialAd(this);

        /*
         * Replace demo R-M-DEMO-320x480 with actual Block ID
         * Following demo Block IDs may be used for testing:
         * R-M-DEMO-320x480
         * R-M-DEMO-480x320
         * R-M-DEMO-400x240-context
         * R-M-DEMO-240x400-context
         * R-M-DEMO-video-interstitial
         */
        mInterstitialAd.setBlockId("R-M-284257-1");

        mAdRequest = AdRequest.builder().build();
        mInterstitialAd.setInterstitialEventListener(mInterstitialAdEventListener);
        Log.e(FilmstripsActivity.TAG,"Load AD");
        mInterstitialAd.loadAd(mAdRequest);
    }

    private  void ReadSettings()
    {
        SharedPreferences cPrefs=getSharedPreferences("common",Context.MODE_PRIVATE);
        String[]strArSet=null;
        try
        {
            strArSet=cPrefs.getString("Favorites", "").split(",");
        }
        catch(Exception ex){}
        m_strArIDFavorites.clear();
        if(strArSet!=null)
		{
			for(String val : strArSet)
                m_strArIDFavorites.add(val);
        }
        Log.e(TAG,m_strArIDFavorites.toString());
        CacheManager.setFolder(cPrefs.getString("Folder", CacheManager.getFolder()));
        CacheManager.setMaxCacheSize(cPrefs.getLong("Folder maxsize", CacheManager.getMaxCacheSize()));
        CacheManager.m_nSwype=cPrefs.getInt("Swype type", CacheManager.m_nSwype);
        CacheManager.USE_VOLUME=cPrefs.getBoolean("Use volume", CacheManager.USE_VOLUME);
        CacheManager.USE_CURSOR=cPrefs.getBoolean("Use cursor", CacheManager.USE_CURSOR);
        CacheManager.USE_PAGE=cPrefs.getBoolean("Use page", CacheManager.USE_PAGE);
        CacheManager.STRETCH_TO_FULLSCREEN=cPrefs.getBoolean("Stretch fullscreen", CacheManager.STRETCH_TO_FULLSCREEN);
        CacheManager.LAST_DOWNLOADED_FILMSTRIPID=cPrefs.getInt("Last download filmstrip ID", CacheManager.LAST_DOWNLOADED_FILMSTRIPID);
        CacheManager.m_nSortType=cPrefs.getInt("Sort type", CacheManager.m_nSortType);
        CacheManager.m_bBlackBackground=cPrefs.getBoolean("Black background", CacheManager.m_bBlackBackground);
        CacheManager.AUTO_LIST=cPrefs.getBoolean("Auto listing", CacheManager.AUTO_LIST);
    }

    private void RequestPermissionOrLoadData()
	{
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
			m_cDownloadThread.start();
		else
			requestMultiplePermissions();
	}

	public void requestMultiplePermissions()
	{
		//ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},PERMISSION_REQUEST_CODE);
		if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
			Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
		} else {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
			{
				ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		if (requestCode == PERMISSION_REQUEST_CODE && grantResults.length == 1)
		{
			if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
			{//дали разрешение на запись папки
				m_cDownloadThread.start();
			}
			else
			{

				//requestMultiplePermissions();
			}
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

    @Override
    public void onPause()
	{
		super.onPause();
		//vunglePub.onPause();
	}

	/*private final VungleAdEventListener vungleDefaultListener = new VungleAdEventListener()
	{

		public void onAdEnd(String placementReferenceId, boolean wasSuccessFulView, boolean wasCallToActionClicked) {
			// Called when user exits the ad and control is returned to your application
			// if wasSuccessfulView is true, the user watched the ad and could be rewarded
			// if wasCallToActionClicked is true, the user clicked the call to action button in the ad.
			Log.d(TAG, "onAdEnd: " + placementReferenceId + " ,wasSuccessfulView: " + wasSuccessFulView + " ,wasCallToActionClicked: " + wasCallToActionClicked);

		}

		public void onAdStart(String placementReferenceId) {
			// Called before playing an ad
			Log.d(TAG, "onAdStart: " + placementReferenceId);
		}

		public void onUnableToPlayAd(String placementReferenceId, String reason) {
			// Called after playAd(placementId, adConfig) is unable to play the ad
			Log.d(TAG, "onUnableToPlayAd: " + placementReferenceId + " ,reason: " + reason);
		}

		public void onAdAvailabilityUpdate(String placementReferenceId, boolean isAdAvailable)
		{
			// Notifies ad availability for the indicated placement
			// There can be duplicate notifications
			Log.d(TAG, "onAdAvailabilityUpdate: " + placementReferenceId + " isAdAvailable: " + isAdAvailable);
		}
	};*/


	public void onDestroy()
    {
        mInterstitialAd.destroy();
        super.onDestroy();
    	if(isFinishing())
    	{//останавливаем поток
    		StopSearch();
    		try
    		{
    			if(m_cDownloadThread!=null)
    			{
    				m_cDownloadThread.setStop();
    				m_cDownloadThread.join();
    			}
    		}
    		catch(Exception ex){}
			WriteSettings();
    	}
    }

    private void WriteSettings()
	{
		SharedPreferences.Editor cPrefs=getSharedPreferences("common",Context.MODE_PRIVATE).edit();
		cPrefs.clear();
		cPrefs.putString("Folder", CacheManager.getFolder());
		cPrefs.putLong("Folder maxsize", CacheManager.getMaxCacheSize());
		cPrefs.putInt("Swype type", CacheManager.m_nSwype);
		cPrefs.putBoolean("Use volume", CacheManager.USE_VOLUME);
		cPrefs.putBoolean("Use cursor", CacheManager.USE_CURSOR);
		cPrefs.putBoolean("Use page", CacheManager.USE_PAGE);
		cPrefs.putBoolean("Stretch fullscreen", CacheManager.STRETCH_TO_FULLSCREEN);
		CacheManager.LAST_DOWNLOADED_FILMSTRIPID=m_nLastID;
		cPrefs.putInt("Last download filmstrip ID", CacheManager.LAST_DOWNLOADED_FILMSTRIPID);
		cPrefs.putInt("Sort type", CacheManager.m_nSortType);
		cPrefs.putBoolean("Black background", CacheManager.m_bBlackBackground);
        cPrefs.putBoolean("Auto listing", CacheManager.AUTO_LIST);
		StringBuilder cBuilder=new StringBuilder();
		Iterator<String> cIter=m_strArIDFavorites.iterator();
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
		cPrefs.putString("Favorites", cBuilder.toString());
		cPrefs.commit();
	}
    
    @SuppressLint("NewApi")
	public void onResume()
    {
    	super.onResume();
		//vunglePub.onResume();
    	int nBGColor=getResources().getColor(CacheManager.m_bBlackBackground?R.color.titlebackgroundcolorblack:R.color.titlebackgroundcolor);
		ColorDrawable cColorDrawable=new ColorDrawable(nBGColor);
		if(Build.VERSION.SDK_INT>=11)
        	getActionBar().setBackgroundDrawable(cColorDrawable);
        else
        {
	        View titleView = getWindow().findViewById(android.R.id.title);
	        if (titleView != null) 
	        {
	          ViewParent parent = titleView.getParent();
	          if (parent != null && (parent instanceof View)) 
	          {
	            View parentView = (View)parent;
	            parentView.setBackgroundColor(nBGColor);
	          }
	        }
        }
		getWindow().getDecorView().setBackgroundColor(nBGColor);
    	SharedPreferences cPrefs=getSharedPreferences("common",Context.MODE_PRIVATE);
    	String[]strArSet=null;
        try
        {
        	strArSet=cPrefs.getString("Favorites", "").split(",");
        }
        catch(Exception ex){}
        m_strArIDFavorites.clear();
        if(strArSet!=null)
        {
     	   for(int n=0;n<strArSet.length;n++)
     		  m_strArIDFavorites.add(strArSet[n]);
        }
    	UpdateFavoritesList();
    	
    }
    
    private void UpdateFavoritesList()
    {
    	try
    	{
	    	if(m_cArChilds!=null&&m_cArGroups!=null)
	    	{
	    		if(m_cArChilds.size()<0)
	    		{
	    			m_cArChilds.add(getFilmStrips(CAT_FAVORITES));
	    			m_cArChildsOrig.clear();
	    			m_cArChildsOrig.addAll(m_cArChilds);
	    		}
	    		else
	    		{
	    			m_cArChilds.set(0, getFilmStrips(CAT_FAVORITES));
	    			m_cArChildsOrig.set(0, m_cArChilds.get(0));
	    		}
	    		Map<String, Object> map = new HashMap<String, Object>();
				int nCount=0;
				/*if(m_bViewSearch)//режим поиска
					nCount=m_cArChildsSearch.get(0).size();
				else*/
					nCount=m_cArChilds.get(0).size();
				String strText=String.format("%s (%d)", m_strArCat.get(0), nCount);
				map.put("cat", strText);
				m_cArGroups.set(0,map);
				
	    		MyExpandableAdapter cAdapter=(MyExpandableAdapter)m_cListView.getExpandableListAdapter();
	    		cAdapter.notifyDataSetChanged();
	    	}
    	}
    	catch(Exception ex){}
    }
    
    @Override
	public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
    	ExpandableListView.ExpandableListContextMenuInfo cExMenuInfo=(ExpandableListView.ExpandableListContextMenuInfo)menuInfo;
    	if(ExpandableListView.PACKED_POSITION_TYPE_CHILD==ExpandableListView.getPackedPositionType(cExMenuInfo.packedPosition))
    	{
    		int nGroup=ExpandableListView.getPackedPositionGroup(cExMenuInfo.packedPosition);
    		int nChild=ExpandableListView.getPackedPositionChild(cExMenuInfo.packedPosition);
    		final String strID=(String)m_cArChilds.get(nGroup).get(nChild).get("ID");
    		if(m_strArIDFavorites.contains(strID))//Уже есть в избранных
    		{
    			menu.add(R.string.DelFromFavorites).setOnMenuItemClickListener(new OnMenuItemClickListener()
    			{
					public boolean onMenuItemClick(MenuItem item) 
					{//удаляем из избранных
						m_strArIDFavorites.remove(strID);
						SharedPreferences.Editor cPrefs=getSharedPreferences("common",Context.MODE_PRIVATE).edit();
						StringBuilder cBuilder=new StringBuilder();    		
			    		Iterator<String> cIter=m_strArIDFavorites.iterator();
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
			    		cPrefs.putString("Favorites", cBuilder.toString());
			    		cPrefs.apply();
			    		UpdateFavoritesList();
						return false;
					}
    				
    			});
    		}
    		else
    		{
    			menu.add(R.string.AddToFavorites).setOnMenuItemClickListener(new OnMenuItemClickListener()
    			{
					public boolean onMenuItemClick(MenuItem item) 
					{//добавляем в избранное
						m_strArIDFavorites.add(strID);
						SharedPreferences.Editor cPrefs=getSharedPreferences("common",Context.MODE_PRIVATE).edit();
						StringBuilder cBuilder=new StringBuilder();    		
			    		Iterator<String> cIter=m_strArIDFavorites.iterator();
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
			    		cPrefs.putString("Favorites", cBuilder.toString());
			    		cPrefs.commit();
			    		UpdateFavoritesList();
						return false;
					}    				
    			});
    		}
    	}
	}
    
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) 
	{
		try
		{
			String strID=(String)m_cArChilds.get(groupPosition).get(childPosition).get("ID");
			if(strID!=null)
			{
				Intent cIntent=new Intent(this,ImageActivity.class);
				cIntent.putExtra("ID", strID);
				startActivity(cIntent);
			}		
		}
		catch(Exception ex){}
		return false;
	}
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
    	menu.clear();
    	menu.add(0, MENU_SORTADDINGDATE, 0, R.string.SortAddingDate).setCheckable(true).setChecked(CacheManager.m_nSortType==3);    	
    	menu.add(0, MENU_SORTTITLE, 0, R.string.SortTitle).setCheckable(true).setChecked(CacheManager.m_nSortType==0);
    	menu.add(0, MENU_SORTYEAR, 0, R.string.SortYear).setCheckable(true).setChecked(CacheManager.m_nSortType==1);
    	menu.add(0, MENU_SORTSTUDIO, 0, R.string.SortStudio).setCheckable(true).setChecked(CacheManager.m_nSortType==2);
    	menu.add(0, MENU_SETTINGS, 0, R.string.MenuSettings);
    	menu.add(0, MENU_EXIT, 0, R.string.MenuExit); 	  
 	   	return true;
    }
    
    public boolean onOptionsItemSelected(MenuItem item) 
    {
		try {
			switch (item.getItemId()) {
				case MENU_SETTINGS:
					Intent cOptIntent = new Intent();
					cOptIntent.setClass(this, SettingsActivity.class);
					startActivity(cOptIntent);
					return true;
				case MENU_EXIT:
					finish();
					return true;
				case MENU_SORTTITLE:
					CacheManager.m_nSortType = 0;
					for (int n = 0; n < m_cArChildsOrig.size(); n++) {
						SortFilms(m_cArChildsOrig.get(n));
					}
					for (int n = 0; n < m_cArChilds.size(); n++) {
						SortFilms(m_cArChilds.get(n));
					}
				{
					MyExpandableAdapter cAdapter = (MyExpandableAdapter) m_cListView.getExpandableListAdapter();
					if (cAdapter != null) cAdapter.notifyDataSetChanged();
				}
				break;
				case MENU_SORTYEAR:
					CacheManager.m_nSortType = 1;
					for (int n = 0; n < m_cArChildsOrig.size(); n++) {
						SortFilms(m_cArChildsOrig.get(n));
					}
					for (int n = 0; n < m_cArChilds.size(); n++) {
						SortFilms(m_cArChilds.get(n));
					}
				{
					MyExpandableAdapter cAdapter = (MyExpandableAdapter) m_cListView.getExpandableListAdapter();
					if (cAdapter != null) cAdapter.notifyDataSetChanged();
				}
				break;
				case MENU_SORTSTUDIO:
					CacheManager.m_nSortType = 2;
					for (int n = 0; n < m_cArChildsOrig.size(); n++) {
						SortFilms(m_cArChildsOrig.get(n));
					}
					for (int n = 0; n < m_cArChilds.size(); n++) {
						SortFilms(m_cArChilds.get(n));
					}
				{
					MyExpandableAdapter cAdapter = (MyExpandableAdapter) m_cListView.getExpandableListAdapter();
					if (cAdapter != null) cAdapter.notifyDataSetChanged();
				}
				break;
				case MENU_SORTADDINGDATE:
					CacheManager.m_nSortType = 3;
					for (int n = 0; n < m_cArChildsOrig.size(); n++) {
						SortFilms(m_cArChildsOrig.get(n));
					}
					for (int n = 0; n < m_cArChilds.size(); n++) {
						SortFilms(m_cArChilds.get(n));
					}
				{
					MyExpandableAdapter cAdapter = (MyExpandableAdapter) m_cListView.getExpandableListAdapter();
					if (cAdapter != null) cAdapter.notifyDataSetChanged();
				}
				break;
			}
		}
		catch(Exception ex){}
    	return false;
    }
    
    @Override
    public boolean onKeyDown (int keyCode, KeyEvent event)
    {    	
    	if(keyCode==KeyEvent.KEYCODE_BACK)
    	{
    		try
    		{
	    		boolean bExit=true;
	    		for(int n=0;n<m_cArChilds.size();n++)
	    		{
	    			if(m_cListView.isGroupExpanded(n))
	    			{
	    				bExit=false;
	    				m_cListView.collapseGroup(n);
	    			}
	    		}
	    		if(bExit)
	    		{
	    			finish();
	    			return false;
	    		}
	    		else
	    			return true;
    		}
    		catch(Exception ex){}
    	}
    	return false;
    }
    
    private void SortFilms(List<Map<String, ?>> cArFilms)
    {
    	switch(CacheManager.m_nSortType)
    	{
    	case 1://по году
    		Collections.sort(cArFilms,new Comparator<Map<String, ?>>()
			{//сортируем по алфавиту
				public int compare(Map<String, ?> lhs, Map<String, ?> rhs) 
				{
					String str1=lhs.get("Year").toString();
					String str2=rhs.get("Year").toString();
					if(str1.length()==0&&str2.length()>0)						
						return 1;
					if(str1.length()>0&&str2.length()==0)
						return -1;
					int nRes=str1.compareTo(str2);
					if(nRes!=0)
						return nRes;
					str1=lhs.get("Title").toString().replace('ё','е').replace('Ё','Е');
					str2=rhs.get("Title").toString().replace('ё','е').replace('Ё','Е');
					return str1.compareTo(str2);					
				}				
			});
	    	break;
    	case 2://по студии
    		Collections.sort(cArFilms,new Comparator<Map<String, ?>>()
			{//сортируем по алфавиту
				public int compare(Map<String, ?> lhs, Map<String, ?> rhs) 
				{
					String str1=lhs.get("Studio").toString().replace('ё','е').replace('Ё','Е');
					String str2=rhs.get("Studio").toString().replace('ё','е').replace('Ё','Е');
					int nRes=str1.compareTo(str2);
					if(nRes!=0)
						return nRes;
					str1=lhs.get("Year").toString();
					str2=rhs.get("Year").toString();
					if(str1.length()==0&&str2.length()>0)						
						return 1;
					if(str1.length()>0&&str2.length()==0)
						return -1;
					nRes=str1.compareTo(str2);
					if(nRes!=0)
						return nRes;
					str1=lhs.get("Title").toString().replace('ё','е').replace('Ё','Е');
					str2=rhs.get("Title").toString().replace('ё','е').replace('Ё','Е');
					return str1.compareTo(str2);					
				}				
			});
	    	break;
    	case 3://по дате добавления
    		Collections.sort(cArFilms,new Comparator<Map<String, ?>>()
			{//сортируем по алфавиту
				public int compare(Map<String, ?> lhs, Map<String, ?> rhs) 
				{
					Long lDate1=(Long) lhs.get("Newsdate");
					Long lDate2=(Long) rhs.get("Newsdate");
					int nRes=lDate1.compareTo(lDate2);
					if(nRes==0)
					{
						String str1=lhs.get("Title").toString().replace('ё','е').replace('Ё','Е');
						String str2=rhs.get("Title").toString().replace('ё','е').replace('Ё','Е');
						return str1.compareTo(str2);
					}
					else
						return -nRes;
				}				
			});
	    	break;
	    default:
		    Collections.sort(cArFilms,new Comparator<Map<String, ?>>()
			{//сортируем по алфавиту
				public int compare(Map<String, ?> lhs, Map<String, ?> rhs) 
				{
					String str1=lhs.get("Title").toString().replace('ё','е').replace('Ё','Е');
					String str2=rhs.get("Title").toString().replace('ё','е').replace('Ё','Е');
					int nRes=str1.compareTo(str2);
					if(nRes!=0)
						return nRes;
					str1=lhs.get("Year").toString();
					str2=rhs.get("Year").toString();
					if(str1.length()==0&&str2.length()>0)						
						return 1;
					if(str1.length()>0&&str2.length()==0)
						return -1;
					return str1.compareTo(str2);					
				}				
			});
		    break;
    	}
    }
    
    //private List<Map<String, ?>> getFilmStrips(String strCat)
    private List<Map<String, ?>> getFilmStrips(int nCat)
	{
    	List<Map<String, ?>> cArFilms=new ArrayList<Map<String, ?>>();
    	String strCat="";
    	Time cTime;
    	long lTime=0;
    	switch(nCat)
    	{
    	case CAT_FAVORITES://избранное
    		for(int n=0;n<m_cArFilms.size();n++)
			{
				if(m_strArIDFavorites==null)
					break;
				OneFilmStrip cStrip=m_cArFilms.get(n);
				int nID=TryParseInt(cStrip.m_strID);//Integer.valueOf(cStrip.m_strID);
				m_nLastID=Math.max(nID, m_nLastID);
				if(!m_strArIDFavorites.contains(cStrip.m_strID)||nID<0||m_nLastID<0)
					continue;
				Map<String, Object> map = new HashMap<String, Object>();
		       	map.put("Title", cStrip.m_strTitle);
		       	map.put("Year", cStrip.m_strYear);
		       	map.put("ID", cStrip.m_strID);
		       	map.put("Object", cStrip);
		       	map.put("Studio", cStrip.m_strStudio);
		    	map.put("Newsdate", cStrip.m_lNewsDate);
		    	map.put("Adding", cStrip.m_strAdding);
		       	cArFilms.add(0,map);
			}
    		break;
    	case CAT_NEW://новые
    		cTime=new Time();
    		cTime.setToNow();
    		lTime=cTime.toMillis(false)-24*7*3600L*1000L;//текущее время, минус неделя
    		for(int n=0;n<m_cArFilms.size();n++)
			{
				OneFilmStrip cStrip=m_cArFilms.get(n);
				/*int nID=Integer.valueOf(cStrip.m_strID);
				m_nLastID=Math.max(nID, m_nLastID);
				if(nID<=CacheManager.LAST_DOWNLOADED_FILMSTRIPID)
					continue;*/
				if(cStrip.m_lNewsDate>0&&cStrip.m_lNewsDate<lTime)//старый
					continue;
				Map<String, Object> map = new HashMap<String, Object>();
		       	map.put("Title", cStrip.m_strTitle);
		       	map.put("Year", cStrip.m_strYear);
		       	map.put("ID", cStrip.m_strID);
		       	map.put("Studio", cStrip.m_strStudio);
		       	map.put("Object", cStrip);
		       	map.put("Newsdate", cStrip.m_lNewsDate);
		       	map.put("Adding", cStrip.m_strAdding);
		       	cArFilms.add(0,map);
			}
    		break;
    	default:
    		if(nCat>=0&&nCat<m_strArCat.size())
    			strCat=m_strArCat.get(nCat);
			for(int n=0;n<m_cArFilms.size();n++)
			{
				OneFilmStrip cStrip=m_cArFilms.get(n);
				int nID=TryParseInt(cStrip.m_strID);//Integer.valueOf(cStrip.m_strID);
				m_nLastID=Math.max(nID, m_nLastID);
				if(strCat.length()>0&&cStrip.m_strArCat.indexOf(strCat)==-1)//категория не подходит, пропускаем
					continue;
				Map<String, Object> map = new HashMap<String, Object>();
		       	map.put("Title", cStrip.m_strTitle);
		       	map.put("Year", cStrip.m_strYear);
		       	map.put("ID", cStrip.m_strID);
		       	map.put("Studio", cStrip.m_strStudio);
		       	map.put("Object", cStrip);
		       	map.put("Newsdate", cStrip.m_lNewsDate);
		       	map.put("Adding", cStrip.m_strAdding);
		       	cArFilms.add(0,map);
			}
			break;
    	}
		//сортировка по имени
		SortFilms(cArFilms);
		return cArFilms;
	}
	
	private class DownLoadThread extends Thread
	{
		private volatile boolean m_bStop=false;
		
		public void setStop()
		{
			m_bStop=true;
		}
		
		public void run()
		{
			MakeFilmStripsCollect();
			m_cArChilds=getChilds();
	        final MyExpandableAdapter cAdapter=new MyExpandableAdapter(FilmstripsActivity.this,getGroups(),
	            	android.R.layout.simple_expandable_list_item_1,new String[]{"cat"}, 
	            		new int[]{android.R.id.text1},m_cArChilds,R.layout.list_item,
	            			new String[]{"Title", "Studio", "Year", "Adding"},new int[]{R.id.textTitle, R.id.textStudio, R.id.textYear, R.id.textAdding});
	        runOnUiThread(new Runnable()
	        {
				public void run() 
				{
					m_cListView.setAdapter(cAdapter);	
			        setProgressBarIndeterminateVisibility(false);
				}	        	
	        });	        	        
		}
				
		private void MakeFilmStripsCollect()
	    {
			//String strXMLAddress="http://diafilmy.su/dia-listgz.php";
			String strXMLAddress="https://diafilmy.su/dia-list-androidgz.php";
			/*try
			{
				String strPName=getPackageName();
				if(strPName.indexOf("Pro")>=0)//проверяем на про версию
					strXMLAddress="http://diafilmy.su/dia-listfgz.php";
			}
			catch(Exception ex){}*/
			
			String strFile=CacheManager.CopyToCache(strXMLAddress,true);
			if(strFile==null)//не прочитан с обновлением кэша, берем старый
                strFile=CacheManager.CopyToCache(strXMLAddress,false);
			MyXMLElement cRoot=null;
			if(strFile!=null&&strFile.length()>0)
				cRoot=MyXMLElement.ReadFromFile(strFile);
			else {
                cRoot = MyXMLElement.ReadFromHTTP(strXMLAddress);
            }
			CacheManager.AdjustCacheSize();
	    	m_cArFilms.clear();
	    	m_strArCat.clear();
	    	if(cRoot==null) {//не закачан список, видимо, нет интернета
				return;
			}
	    	cRoot=cRoot.getChildAt(0);
	    	if(cRoot==null)	    	
	    		return;
	    	Time cTodayTime=new Time();
            cTodayTime.setToNow();
            long lMillisToday=cTodayTime.toMillis(false);
	    	for(int n=0;n<cRoot.getChildCount();n++)
	    	{
	    		MyXMLElement cElement=cRoot.getChildAt(n);
	    		OneFilmStrip cStrip=new OneFilmStrip();    		
	    		for(int m=0;m<cElement.getChildCount();m++)
	    		{
	    			MyXMLElement cChild=cElement.getChildAt(m);
	    			String strName=cChild.getName();
	    			if(strName.equalsIgnoreCase("id"))
	    				cStrip.m_strID=cChild.getContent();
	    			if(strName.equalsIgnoreCase("cat"))
	    			{
	    				String strCat=cChild.getContent().trim();
	    				if(!strCat.equalsIgnoreCase("чёрно-белые")&&!strCat.equalsIgnoreCase("черно-белые"))
	    				{
		    				if(m_strArCat.indexOf(strCat)==-1)
		    					m_strArCat.add(strCat);
		    				cStrip.m_strArCat.add(strCat);
	    				}
	    			}
	    			if(strName.equalsIgnoreCase("title"))
	    				cStrip.m_strTitle=cChild.getContent();
	    			if(strName.equalsIgnoreCase("year"))
	    				cStrip.m_strYear=cChild.getContent();
	    			if(strName.equalsIgnoreCase("url"))
	    				cStrip.m_strURL=cChild.getContent();
	    			if(strName.equalsIgnoreCase("studio"))
	    				cStrip.m_strStudio=cChild.getContent();
	    			if(strName.equalsIgnoreCase("img"))
	    				cStrip.m_strImageURL = cChild.getContent();
					if(strName.equalsIgnoreCase("newsdate"))
	    			{
	    				Time cTime=new Time();
	    				String strDate=cChild.getContent();
	    				int nPos=strDate.indexOf(' ');
	    				if(nPos!=-1)
	    					strDate=strDate.substring(0,nPos);
    					strDate=strDate.replace(' ', 'T');
    					//strDate=strDate.replace("-", "").replace(":", "").replace("/", "").replace(".", "").replace("\\", "");
    					strDate=strDate.replaceAll("[^0-9Т]", "");
	    				cTime.parse(strDate);
	    				cStrip.m_lNewsDate=cTime.toMillis(false);
	    				int nDayAddingSpend=2;//сколько дней прошло после добавления, 0 - сегодня, 1 - вчера
	    			    if((lMillisToday-cStrip.m_lNewsDate)<2*86400000L)//с даты добавления прошло не более 2 суток
                        {
                            if(cTime.month==cTodayTime.month)//все просто, дата в том же месяце
                                nDayAddingSpend=cTodayTime.monthDay-cTime.monthDay;
                            else
                            {//дата в прошлом месяце
                                nDayAddingSpend=1;
                            }
                        }
                        switch (nDayAddingSpend)
                        {
                            case 0:
                                cStrip.m_strAdding=getResources().getString(R.string.Today);
                                break;
                            case 1:
                                cStrip.m_strAdding=getResources().getString(R.string.Yesterday);
                                break;
                            default:
                                cStrip.m_strAdding=cTime.format("%d.%m.%Y");
                        }
	    			}
	    		}
	    		m_cArFilms.add(cStrip);
	    	}
	    	//Collections.sort(m_strArCat);
	    	SortCollection();
	    	//m_strArCat.insertElementAt("Новые",0);
	    	m_strArCat.insertElementAt("Новые",0);
	    	m_strArCat.insertElementAt("Все",0);
	    	m_strArCat.insertElementAt("Избранное",0);	    	
	    }
		
		private void SortCollection()
		{
			Vector<String>strArNew=new Vector<String>();
			SortCollection(strArNew,m_strArCat, "Сказки");
			SortCollection(strArNew,m_strArCat, "Мультфильмы");
			SortCollection(strArNew,m_strArCat, "Стихи и басни");
			SortCollection(strArNew,m_strArCat, "Повести и рассказы");
			SortCollection(strArNew,m_strArCat, "Озвученные диафильмы");
			SortCollection(strArNew,m_strArCat, "Образовательные и учебные");
			SortCollection(strArNew,m_strArCat, "Исторические и документальные");
			SortCollection(strArNew,m_strArCat, "Другие языки");
			Collections.sort(m_strArCat);//остальные отсортируем по алфавиту и добавим
			strArNew.addAll(m_strArCat);
			m_strArCat=strArNew;
		}
		
		private void SortCollection(Vector<String>strArNew, Vector<String>strArOld, String strCat)
		{//функция принудительного сортирования коллекции по слову
			int nIndex=strArOld.indexOf(strCat);
			if(nIndex>=0)
				strArNew.add(strArOld.remove(nIndex));
		}
		
		private List<Map<String, ?>> getGroups()
		{
			m_cArGroups = new ArrayList<Map<String, ?>>();
			for(int n=0;n<m_strArCat.size();n++)
			{
				if(m_bStop)
					break;
				Map<String, Object> map = new HashMap<String, Object>();
				int nCount=0;
				/*if(m_bViewSearch)//режим поиска
					nCount=m_cArChildsSearch.get(n).size();
				else*/
					nCount=m_cArChilds.get(n).size();
				String strText=String.format(Locale.US,"%s (%d)", m_strArCat.get(n), nCount);
				map.put("cat", strText);
				m_cArGroups.add(map);
			}        
			return m_cArGroups;
		}
	    
	    private List<List<Map<String, ?>>> getChilds()
		{
			m_cArChilds=new ArrayList<List<Map<String, ?>>>();
			for(int n=0;n<m_strArCat.size();n++)
			{
				if(m_bStop)
					break;		
				if(n==0)//категория избранное
				{
					m_cArChilds.add(getFilmStrips(CAT_FAVORITES));
				}
				else
				{
					if(n==1)//категория все
						m_cArChilds.add(getFilmStrips(CAT_ALL));
					else
					{
						if(n==2)//категория новые
							m_cArChilds.add(getFilmStrips(CAT_NEW));
						else
							m_cArChilds.add(getFilmStrips(n));
					}
				}
			}
			return m_cArChilds;
		}
		
	}
	
	private class MyExpandableAdapter extends SimpleExpandableListAdapter 
	{
		private UpdateFrameThread m_cFrameThread=null;		
		
		public MyExpandableAdapter(Context context,
				List<? extends Map<String, ?>> groupData, int groupLayout,
				String[] groupFrom, int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData,
				int childLayout, String[] childFrom, int[] childTo) 
		{
			super(context, groupData, groupLayout, groupFrom, groupTo, childData,
					childLayout, childFrom, childTo);
			m_cArChildsOrig.addAll(m_cArChilds);
			m_cFrameThread=new UpdateFrameThread();
			m_cFrameThread.start();
		}
		
		protected void finalize ()
		{
			m_cFrameThread.SetStop();
		}
		
		public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
		{
			if(getChildrenCount(groupPosition)==0)//пустая группа, скрываем ее
			{
				View cView=new View(FilmstripsActivity.this);
				cView.setVisibility(View.GONE);
				return cView;
			}			
			convertView=super.newGroupView(isExpanded, parent);
			return super.getGroupView(groupPosition, isExpanded, convertView, parent);
		}
				
		public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
		{
			View cResView=convertView;
			try {
				if (groupPosition < 0 || groupPosition >= m_strArCat.size())
					return cResView;
				if (childPosition < 0 || childPosition >= m_cArChilds.get(groupPosition).size())
					return cResView;
			}
			catch (Exception ex)
			{
				return cResView;
			}
			cResView=super.getChildView(groupPosition, childPosition, isLastChild, convertView, parent);
			ImageView cView=(ImageView)cResView.findViewById(R.id.image1);
			ImageView cViewStar=(ImageView)cResView.findViewById(R.id.imageStar);
			LinearLayout cLinearYear=(LinearLayout)cResView.findViewById(R.id.linearYear);
			LinearLayout cLinearAdding=(LinearLayout)cResView.findViewById(R.id.linearAdding);
			if(cView!=null)
			{
				try
				{
					OneFilmStrip cChild=(OneFilmStrip)m_cArChilds.get(groupPosition).get(childPosition).get("Object");
					if(cChild.m_cBmpIcon==null)
					{
						m_cFrameThread.AddFilm(cChild);
						cView.setImageBitmap(null);						
					}
					else
						cView.setImageBitmap(cChild.m_cBmpIcon);
					if(m_strArIDFavorites.contains(cChild.m_strID)&&groupPosition>0)//есть в избранном и не нулевая группа(не Избранное), открываем звезду
						cViewStar.setVisibility(View.VISIBLE);
					else
						cViewStar.setVisibility(View.GONE);
					if(cChild.m_strYear.length()==0)
						cLinearYear.setVisibility(View.GONE);
					else
						cLinearYear.setVisibility(View.VISIBLE);
					if(cChild.m_strAdding.length()==0)
						cLinearAdding.setVisibility(View.GONE);
					else
						cLinearAdding.setVisibility(View.VISIBLE);
					if(m_strSearch.length()>2)
					{//идет поиск, подсветим результат
						TextView cTitleView=(TextView)cResView.findViewById(R.id.textTitle);
						String strText=cTitleView.getText().toString().toLowerCase();
						SpannableString strSpan=new SpannableString(cTitleView.getText());
						int nStart=strText.indexOf(m_strSearch);
						int nEnd=nStart+m_strSearch.length();
						strSpan.setSpan(new BackgroundColorSpan(Color.DKGRAY), nStart, nEnd, 0);
						cTitleView.setText(strSpan);
					}
					TextView cStudio=(TextView)cResView.findViewById(R.id.textStudio);
					cStudio.setText(getResources().getText(R.string.Studio)+cChild.m_strStudio);
				}
				catch(Exception ex)
				{
					ex.printStackTrace();
				}
			}			
			return cResView;
		}
		
		private class UpdateFrameThread extends Thread
		{
			private Vector<OneFilmStrip>m_strArFilms=new Vector<OneFilmStrip>();
			private volatile boolean m_bStop=false;
			
			private Runnable m_cUpdateRunnable=new Runnable()
			{
				public void run() 
				{
					notifyDataSetChanged();
				}				
			};
			
			public synchronized void AddFilm(OneFilmStrip cStrip)
			{
				if(m_strArFilms.indexOf(cStrip)==-1)
					m_strArFilms.add(0,cStrip);
			}
			
			public synchronized void SetStop()
			{
				m_bStop=true;
			}
			
			@Override
			public void run()
			{
				while(!m_bStop)
				{
					try
					{
						Thread.sleep(500);
					}
					catch(Exception ex){ ex.printStackTrace(); }
					while(m_strArFilms.size()>0&&!m_bStop)
					{
						OneFilmStrip cStrip=m_strArFilms.get(0);
						{//попробуем через шорткат
						}
						cStrip.LoadIcon();
						m_strArFilms.remove(cStrip);
						runOnUiThread(m_cUpdateRunnable);
					}				
				}
			}
		}		
	}

	public void afterTextChanged(Editable arg0) 
	{
		if(m_cArGroups==null||m_cArChildsOrig==null||m_cArGroups.size()==0||m_cArGroups.size()!=m_cArChildsOrig.size())
			return;
		m_strSearch=m_cEditTextSearch.getText().toString().trim().toLowerCase();
		StopSearch();
		if(m_strSearch.length()>2)
		{
			Log.e(TAG, "Searching");
			m_cSearchTask=new SearchTask();
			m_cSearchTask.execute();
		}
		else
		{//восстанавливаем поиск
			Log.e(TAG, "Restore original");
			//m_cArChilds.clear();
			try
			{
				for(int n=0;n<m_cArChildsOrig.size();n++)
				{
					if(n>=m_cArChilds.size())
						m_cArChilds.add(m_cArChildsOrig.get(n));
					else
						m_cArChilds.set(n,m_cArChildsOrig.get(n));
				}
			}
			catch(Exception ex){}
		}
		UpdateList();
	}

	public void beforeTextChanged(CharSequence s, int start, int count,	int after) 
	{
		
	}

	public void onTextChanged(CharSequence s, int start, int before, int count) 
	{
		
	}
	
	private void StopSearch()
	{
		if(m_cSearchTask!=null)
		{
			try
			{
				m_cSearchTask.cancel(true);
			}
			catch(Exception ex){}
			setProgressBarIndeterminateVisibility(false);
		}
	}

	private void UpdateList()
	{
		try
		{
			for(int n=0;n<m_cArGroups.size();n++)
			{
				try {
					Map<String, Object> map = (Map<String, Object>) m_cArGroups.get(n);
					int nCount = m_cArChilds.get(n).size();
					String strText = String.format("%s (%d)", m_strArCat.get(n), nCount);
					map.put("cat", strText);
				}
				catch (Exception ex){
					ex.printStackTrace();
				}
			}        
			
			MyExpandableAdapter cAdapter=(MyExpandableAdapter)m_cListView.getExpandableListAdapter();
			cAdapter.notifyDataSetChanged();
		}
		catch(Exception ex){}
	}
	
    private class SearchTask extends AsyncTask<Void, Void, Void> 
	{
    	private Runnable m_cUpdateRun=new Runnable()
    	{
			public void run() 
			{
				UpdateList();
			}    		
    	};
 	
		protected void onPreExecute() 
		{
			setProgressBarIndeterminateVisibility(true);
		}
		      // automatically done on worker thread (separate from UI thread)
		@Override
		protected Void doInBackground(Void... params)
		{			
			try
			{
				int nCount=0;
				for(int n=0;n<m_cArGroups.size();n++)
				{
					List<Map<String,?>>cArGroupChild=new ArrayList<Map<String,?>>();
					if(m_cArChilds.size()<n)
						m_cArChilds.add(cArGroupChild);
					else
						m_cArChilds.set(n,cArGroupChild);
				}
				for(int m=0;m<m_cArChildsOrig.size();m++)
				{
					List<Map<String,?>>cArGroupList=m_cArChildsOrig.get(m);
					List<Map<String,?>>cArGroupChild=m_cArChilds.get(m);
					for(int n=0;n<cArGroupList.size();n++)
					{
						if(isCancelled())
						{
							runOnUiThread(m_cUpdateRun);
							return null;//отменили поиск
						}
						Map<String,?>cMap=cArGroupList.get(n);
						String strTitle=cMap.get("Title").toString().toLowerCase();
						if(strTitle.contains(m_strSearch))
						{
							Log.e(TAG,strTitle);
							cArGroupChild.add(cMap);
						}
						nCount++;
						if(nCount%10==0)
							runOnUiThread(m_cUpdateRun);
					}
				}
			}
			catch(Exception ex){}
			return null;
		}
		      
		      // can use UI thread here
		protected void onPostExecute(final Void unused)
		{	
			runOnUiThread(m_cUpdateRun);
			setProgressBarIndeterminateVisibility(false);
			m_cSearchTask=null;			
		}
	}

	public void onScroll(AbsListView view, int firstVisibleItem,	int visibleItemCount, int totalItemCount) 
	{
		
	}

	public void onScrollStateChanged(AbsListView view, int scrollState) 
	{
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(m_cEditTextSearch.getWindowToken(),  InputMethodManager.HIDE_NOT_ALWAYS);
	}
	
	private int TryParseInt(String strValue)
	{
		try
		{
			return Integer.parseInt(strValue);
		}
		catch(Exception ex)
		{
			return -9999;
		}
	}
}