package com.akmanaev.filmstrip;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

public class MyXMLElement 
{//элемент XML файла
	private Vector<MyXMLElement> m_cArChilds=new Vector<MyXMLElement>();//дочерние элементы
	private MyXMLElement m_cParent=null;//родитель элемента, если NULL - то это главный элемент
	private String m_strName="";//название элемента
	private String m_strContent="";//содержимое элемента
	
	public MyXMLElement(String strName, MyXMLElement cParent)
	{
		m_cParent=cParent;
		m_strName=strName;
		if(m_cParent!=null)
			m_cParent.AddChild(this);
	}
	
	public int getChildCount()
	{
		return m_cArChilds.size();
	}
	
	public MyXMLElement getChildAt(int nIndex)
	{
		if(nIndex<0||nIndex>=m_cArChilds.size())
			return null;
		return m_cArChilds.get(nIndex);
	}
	
	private void AddChild(MyXMLElement cChild)
	{
		m_cArChilds.add(cChild);
	}
	
	public MyXMLElement getParent()
	{
		return m_cParent;
	}
	
	public MyXMLElement getRoot()
	{
		if(m_cParent==null)
			return this;
		return m_cParent.getRoot();
	}
	
	public String getName()
	{
		return m_strName;
	}
	
	public String getContent()
	{
		return m_strContent;
	}
	
	public void setContent(String strText)
	{
		m_strContent=strText;
	}
	
	public static MyXMLElement ReadFromHTTP(String strURL)
	{
		try
		{
			URL url = new URL(strURL);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setDoOutput(true);
			urlConnection.connect();
		    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
		    MyXMLElement cElement=parse(new InputStreamReader(in));
		    in.close();
		    urlConnection.disconnect();
		    return cElement;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	public static MyXMLElement ReadFromFile(String strFile)
	{
		try
		{
			FileReader cReader=new FileReader(strFile);
			MyXMLElement cElement=parse(cReader);
			cReader.close();
		    return cElement;
		}
		catch(Exception ex)
		{
			
		}
		return null;
	}
	
	 public static MyXMLElement parse(Reader cReader)
	 {
		 MyXMLElement cElement=null;
		 try
		 {
		     XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
		     factory.setNamespaceAware(true);
		     XmlPullParser xpp = factory.newPullParser();
		
		     xpp.setInput(cReader);
		     int eventType = xpp.getEventType();
		    
		     while (eventType != XmlPullParser.END_DOCUMENT) 
		     {
		    	 switch(eventType)
		    	 {
		    	 case XmlPullParser.START_DOCUMENT: 
			    	 {//Начало документа, создаем корневой элемент
			    		 cElement=new MyXMLElement("",null); 
			    		 //System.out.println("Start document");
			    		 break;
			    	 } 
		    	 case XmlPullParser.START_TAG: 
		    		 {//нашли новый элемент-заводим все под него
		    			 cElement=new MyXMLElement(xpp.getName(),cElement);
		    			 //System.out.println("Start tag "+xpp.getName());
		    			 break;
		    		 } 
		    	 case XmlPullParser.END_TAG: 
			    	 {
	    				 cElement=cElement.getParent();//возвращаемся к предыдущему элементу в дереве
			    		 //System.out.println("End tag "+xpp.getName());
	    				 break;
			    	 } 
		    	 case XmlPullParser.TEXT: 
					 {//нашли содержимое, заносим его
						 cElement.setContent(xpp.getText());
						//System.out.println("Text "+xpp.getText());
						 break;
					 }
		    	 }
		    	 eventType = xpp.next();
		     }
		     return cElement.getRoot();
		 }
		 catch(Exception ex)
		 {
			 ex.printStackTrace();
		 }
		 return null;
	 }
}
