package com.akmanaev.filmstrip;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.akmanaev.filmstrip.EditTextDlg.OnTextEnteredListener;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class SelectFolderDlg extends Dialog implements OnClickListener,OnItemClickListener, Comparator<File>, OnTextEnteredListener
{
	private ListView m_cLVFolders;
	public interface OnFolderChangedListener 
    {
        void folderChanged(String strNewFolder);
    }
	private OnFolderChangedListener m_cListener=null;
	private String m_strFolder="";
	
	public SelectFolderDlg(Context context,OnFolderChangedListener cListener, String strFolder)
	{
		super(context);		
		m_cListener=cListener;
		m_strFolder=strFolder;
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        setTitle(R.string.CacheFolder);
        setContentView(R.layout.folder_picker);
        m_cLVFolders=(ListView)findViewById(R.id.listViewFolder);
        m_cLVFolders.setOnItemClickListener(this);
        FillFolderList(m_strFolder);
        findViewById(R.id.buttonCreate).setOnClickListener(this);
        findViewById(R.id.ButtonApply).setOnClickListener(this);
        findViewById(R.id.ButtonCancel).setOnClickListener(this);
	}

	public void onClick(View arg0) 
	{
		switch(arg0.getId())
		{
		case R.id.buttonCreate:			
			CreateNewFolder();
			break;
		case R.id.ButtonApply:
			m_cListener.folderChanged(m_strFolder);
			dismiss();
			break;
		case R.id.ButtonCancel:
			dismiss();
			break;
		}
	}
	
	private void FillFolderList(String strFolder)
	{
		try
		{
			Log.e("SelectFolder","Folder to fill="+strFolder);
			File cFile=new File(strFolder);
			File[] cArEntries=cFile.listFiles();
			Arrays.sort(cArEntries,this);
			List<Map<String,?>> cArItems=new Vector<Map<String,?>>();
			HashMap<String,String> cMap=new HashMap<String,String>();
			String strRootFolder="/";
			int nPos=strFolder.lastIndexOf("/");
			if(nPos>0)
			{//берем папку на уровень выше
				strRootFolder=strFolder.substring(0,nPos);
			}
			Log.e("SelectFolder","Root folder="+strRootFolder);
			cMap.put("Name", "..");
			cMap.put("Path", strRootFolder);
			cArItems.add(cMap);
			for(int n=0;n<cArEntries.length;n++)
			{				
				File cEntry=cArEntries[n];
				if(!cEntry.isDirectory())
					continue;
				cMap=new HashMap<String,String>();
				cMap.put("Name", cEntry.getName());
				cMap.put("Path", cEntry.getAbsolutePath());
				cArItems.add(cMap);
			}		 
			SimpleAdapter cAdapter=new SimpleAdapter(getContext(),cArItems,R.layout.folder_item,new String[] {"Name","Path"}, 
				new int[]{R.id.textFolderName,R.id.textFolderPath});
			m_cLVFolders.setAdapter(cAdapter);
			m_strFolder=strFolder;
			setTitle(getContext().getResources().getString(R.string.SelectedFolder)+" "+strFolder);			
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) 
	{
		TextView cTVFolder=(TextView)arg1.findViewById(R.id.textFolderPath);
		String strFolder=cTVFolder.getText().toString();
		FillFolderList(strFolder);
	}

	public int compare(File arg0, File arg1) 
	{
		return arg0.getName().compareToIgnoreCase(arg1.getName());
	}
	
	private void CreateNewFolder()
	{
		EditTextDlg cDlg=new EditTextDlg(getContext(),this,"");
		cDlg.show();		
	}

	public void TextEntered(String strText) 
	{
		strText=strText.trim();
		if(strText.length()==0)
			return;
		String strFolder=m_strFolder;
		if(strFolder.length()>0&&strFolder.charAt(strFolder.length()-1)!='/')
			strFolder+="/";
		strFolder+=strText;
		File cFile=new File(strFolder);
		if(cFile.mkdirs())
			FillFolderList(strFolder);		
	}
}
