package com.akmanaev.filmstrip;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Spinner;
import android.widget.TextView;

/*import com.vungle.publisher.AdConfig;
import com.vungle.publisher.VunglePub;*/

public class SettingsActivity extends Activity implements OnClickListener, OnCheckedChangeListener
{
/*
	final VunglePub vunglePub = VunglePub.getInstance();
    final AdConfig globalAdConfig = vunglePub.getGlobalAdConfig();
*/

	private TextView m_cTextViewFolder;
	private Spinner m_cComboCacheSize;
	private Spinner m_cComboSwype;
	private CheckBox m_cCheckBoxVolume;
	private CheckBox m_cCheckBoxCursor;
	private CheckBox m_cCheckBoxPage;
	private CheckBox m_cCheckBoxFullScreen;
	private CheckBox m_cCheckBoxBlackBackground;
	private CheckBox m_cCheckAutoListing;
	private Button m_cBtnShowAd;
	private Handler m_cHandler=new Handler();
	private Runnable m_cRunUpdateBtnAd=new Runnable()
    {
        public void run()
        {
            m_cHandler.removeCallbacks(m_cRunUpdateBtnAd);
            //if (FilmstripsActivity.VungleInit&&vunglePub.isAdPlayable(FilmstripsActivity.DEFAULT_PLACEMENT_ID))
            if (FilmstripsActivity.mInterstitialAd.isLoaded())
            {
                m_cBtnShowAd.setVisibility(View.VISIBLE);
            }
            else
            {
                m_cBtnShowAd.setVisibility(View.GONE);
            }
            m_cHandler.postDelayed(m_cRunUpdateBtnAd,1000l);
        }
    };

   	@Override
	protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
       
        m_cComboCacheSize=(Spinner)findViewById(R.id.SpinnerCacheSize);
        switch((int)CacheManager.getMaxCacheSize())
        {
        case 10*1048576:
        	m_cComboCacheSize.setSelection(0);
        	break;
        case 25*1048576:
        	m_cComboCacheSize.setSelection(1);
        	break;
        case 50*1048576:
        	m_cComboCacheSize.setSelection(2);
        	break;
        case 100*1048576:
        	m_cComboCacheSize.setSelection(3);
        	break;
        case 250*1048576:
        	m_cComboCacheSize.setSelection(4);
        	break;
        case 500*1048576:
        	m_cComboCacheSize.setSelection(5);
        	break;
        default:
        	m_cComboCacheSize.setSelection(6);
        }
        m_cComboSwype=(Spinner)findViewById(R.id.SpinnerSwypeFrames);
        m_cComboSwype.setSelection(CacheManager.m_nSwype);
        m_cTextViewFolder=(TextView)findViewById(R.id.TextViewFolder);
        m_cTextViewFolder.setText(CacheManager.getFolder());
        m_cCheckBoxFullScreen=(CheckBox)findViewById(R.id.checkBoxFullScreen);
        m_cCheckBoxVolume=(CheckBox)findViewById(R.id.checkBoxVolume);
        m_cCheckBoxCursor=(CheckBox)findViewById(R.id.checkBoxCursor);
        m_cCheckBoxPage=(CheckBox)findViewById(R.id.checkBoxPageUpDn);
        m_cCheckBoxBlackBackground=(CheckBox)findViewById(R.id.checkBoxBlackBackground);
		m_cCheckAutoListing=(CheckBox)findViewById(R.id.checkBoxAutoListing);
        m_cBtnShowAd=(Button)findViewById(R.id.ButtonDonation);

        m_cCheckBoxBlackBackground.setChecked(CacheManager.m_bBlackBackground);
        m_cCheckBoxFullScreen.setChecked(CacheManager.STRETCH_TO_FULLSCREEN);
        m_cCheckBoxVolume.setChecked(CacheManager.USE_VOLUME);
        m_cCheckBoxCursor.setChecked(CacheManager.USE_CURSOR);
        m_cCheckBoxPage.setChecked(CacheManager.USE_PAGE);
		m_cCheckAutoListing.setChecked(CacheManager.AUTO_LIST);
        
        m_cCheckBoxBlackBackground.setOnCheckedChangeListener(this);
        findViewById(R.id.LinearLayoutFolder).setOnClickListener(this);
        findViewById(R.id.ButtonDonation).setOnClickListener(this);
        findViewById(R.id.imageButtonFacebook).setOnClickListener(this);
        findViewById(R.id.imageButtonTwitter).setOnClickListener(this);
        findViewById(R.id.imageButtonVkontakte).setOnClickListener(this);
        findViewById(R.id.imageButtonInstagram).setOnClickListener(this);
//        findViewById(R.id.buttonScanRestore).setOnClickListener(this);
		TextView textView = (TextView) findViewById(R.id.textViewLink);
		Linkify.addLinks(textView, Linkify.WEB_URLS);
        UpdateBackground();
        m_cBtnShowAd.setVisibility(View.GONE);
        m_cHandler.postDelayed(m_cRunUpdateBtnAd,100l);
	}

	@Override
	protected void onPause() {
		super.onPause();
		//vunglePub.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		//vunglePub.onResume();
	}

	@Override
    protected void onDestroy()
    {
        super.onDestroy();
		if(isFinishing())
		{//сохраняем настройки
            m_cHandler.removeCallbacks(m_cRunUpdateBtnAd);
			CacheManager.setFolder(m_cTextViewFolder.getText().toString());
			switch(m_cComboCacheSize.getSelectedItemPosition())
			{
			case 0:
				CacheManager.setMaxCacheSize(10*1048576);
				break;
			case 1:
				CacheManager.setMaxCacheSize(25*1048576);
				break;
			case 2:
				CacheManager.setMaxCacheSize(50*1048576);
				break;
			case 3:
				CacheManager.setMaxCacheSize(100*1048576);
				break;
			case 4:
				CacheManager.setMaxCacheSize(250*1048576);
				break;
			case 5:
				CacheManager.setMaxCacheSize(500*1048576);
				break;
			default:
				CacheManager.setMaxCacheSize(0);
			}
			CacheManager.m_nSwype=m_cComboSwype.getSelectedItemPosition();
			CacheManager.USE_VOLUME=m_cCheckBoxVolume.isChecked();
			CacheManager.USE_CURSOR=m_cCheckBoxCursor.isChecked();
			CacheManager.USE_PAGE=m_cCheckBoxPage.isChecked();
			CacheManager.STRETCH_TO_FULLSCREEN=m_cCheckBoxFullScreen.isChecked();
			CacheManager.m_bBlackBackground=m_cCheckBoxBlackBackground.isChecked();
			CacheManager.AUTO_LIST=m_cCheckAutoListing.isChecked();
		}
    }

	public void onClick(View arg0) 
	{
		switch(arg0.getId())
		{
		case R.id.LinearLayoutFolder:
			SelectFolderDlg();
			break;//выбор папки
		case R.id.ButtonDonation:
			Log.e(FilmstripsActivity.TAG,"Show AD");
			/*if (FilmstripsActivity.VungleInit&&vunglePub.isAdPlayable(FilmstripsActivity.DEFAULT_PLACEMENT_ID))
			{
				Log.e(FilmstripsActivity.TAG,"Play AD");
				vunglePub.playAd(FilmstripsActivity.DEFAULT_PLACEMENT_ID, globalAdConfig);
			}*/
			if(FilmstripsActivity.mInterstitialAd.isLoaded())
                FilmstripsActivity.mInterstitialAd.show();
			break;
		case R.id.imageButtonFacebook:
			ShowBrowser("https://www.facebook.com/diafilmy.su");  
			break;
		case R.id.imageButtonTwitter:
			ShowBrowser("https://twitter.com/diafilmy");  
			break;
		case R.id.imageButtonVkontakte:
			ShowBrowser("http://vk.com/diafilm1");  
			break;
		case R.id.imageButtonInstagram:
			ShowBrowser("https://www.instagram.com/diafilmy.su");  
			break;
		}
	}
	
	private void ShowBrowser(String strURL)
	{
		Intent i = new Intent(Intent.ACTION_VIEW);
		Uri u = Uri.parse(strURL);  
		i.setData(u);  
		try 
		{  
			startActivity(i);  
		} 
		catch (Exception e) 
		{  
		}
	}
	
	private void SelectFolderDlg()
	{
		SelectFolderDlg cDlg=new SelectFolderDlg(this, new SelectFolderDlg.OnFolderChangedListener() 
		{			
			public void folderChanged(String strNewFolder) 
			{
				CacheManager.setFolder(strNewFolder);
				m_cTextViewFolder.setText(CacheManager.getFolder());
			}
		}
		,CacheManager.getFolder());
		cDlg.show();
	}
	
	@SuppressLint("NewApi")
	private void UpdateBackground()
	{
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
	}

	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) 
	{
		switch(buttonView.getId())
		{
		case R.id.checkBoxBlackBackground:
			CacheManager.m_bBlackBackground=m_cCheckBoxBlackBackground.isChecked();
			UpdateBackground();
			break;
		}
	}
}
