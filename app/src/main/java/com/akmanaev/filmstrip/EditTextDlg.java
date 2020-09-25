package com.akmanaev.filmstrip;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

public class EditTextDlg extends Dialog implements OnClickListener
{
	public interface OnTextEnteredListener 
    {
        void TextEntered(String strText);
    }
	private OnTextEnteredListener m_cListener=null;
	private String m_strTitle="";
	private EditText m_cEditText;
	
	public EditTextDlg(Context context,OnTextEnteredListener cListener,String strTitle) 
	{
		super(context);
		m_cListener=cListener;
		m_strTitle=strTitle;
	}
	
	@Override
    protected void onCreate(Bundle savedInstanceState) 
	{
        super.onCreate(savedInstanceState);

        setTitle(m_strTitle);
        setContentView(R.layout.edit_text_dlg);
        m_cEditText=(EditText)findViewById(R.id.editText1);
        findViewById(R.id.ButtonApply).setOnClickListener(this);
        findViewById(R.id.ButtonCancel).setOnClickListener(this);
	}

	public void onClick(View v) 
	{
		switch(v.getId())
		{
		case R.id.ButtonApply:
			if(m_cListener!=null)
				m_cListener.TextEntered(m_cEditText.getText().toString());
			dismiss();
			break;
		case R.id.ButtonCancel:
			dismiss();
			break;
		}
	}

}
