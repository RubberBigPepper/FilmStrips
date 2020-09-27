package com.akmanaev.filmstrip;

import android.os.Environment;
import android.os.StatFs;
import android.text.format.Time;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.Comparator;

//import org.apache.http.util.ByteArrayBuffer;

public class CacheManager
{//????? ?????? ? ????? ?????????
    private static String m_strFolder=Environment.getExternalStorageDirectory().getAbsolutePath()+"/filmstrips/";//????? ??? ??????
    private static long m_lMaxSize=50*1048576;//50 ???????? ???
    protected static int m_nSwype=0;//??? ?????

    public static boolean USE_VOLUME=false;
    public static boolean USE_CURSOR=true;
    public static boolean USE_PAGE=true;
    public static boolean STRETCH_TO_FULLSCREEN=false;//???? ?????????? ?? ???? ?????
    public static int LAST_DOWNLOADED_FILMSTRIPID=-1;//?????? ?????????? ???????????? ?????????
    public static int m_nLastSeenId=-1;//????????? ????????????? ????????
    public static int m_nLastSeenPage=0;// ????????? ???????? ? ????????? ????????????? ?????????
    public static int m_nSortType=3;//??? ??????????, 0 - ?? ?????, 1 - ?? ????, ????? ?? ?????, 2- ?? ??????, ????? ?? ?????, 3 - ?? ????
    public static boolean m_bBlackBackground=false;//true ???? ??????? ?????? ???
    public static boolean AUTO_LIST=false;//если автопролистывание задействовано

    public static String getFolder()
    {
        return m_strFolder;
    }

    public static void setFolder(String strFolder)
    {
        m_strFolder=strFolder;
        if(m_strFolder.length()>0&&m_strFolder.charAt(m_strFolder.length()-1)!='/')
            m_strFolder+="/";
        CreateFolder();
    }

    public static long getMaxCacheSize()
    {
        return m_lMaxSize;
    }

    public static void setMaxCacheSize(long lMaxSize)
    {
        m_lMaxSize=lMaxSize;
    }

    private static void CreateFolder()
    {
        File cFile=new File(m_strFolder);
        boolean bRes=cFile.mkdirs();
        Log.e("Filmscope", "Create folder result = "+bRes);
        try
        {
            FileOutputStream cStream=new FileOutputStream(m_strFolder+".nomedia");
            cStream.close();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
    }

    private static String MakeFileName(String strURL)
    {
        String strFile=strURL.toLowerCase();
        strFile=strFile.replace("/","_").replace("https:__", m_strFolder);
        strFile=strFile.replace("?","_");
        strFile=strFile.replace("&","_");
        strFile=strFile.replace("=","_");
        return strFile;
    }

    public static String CopyToCache(String strURL, boolean bRefresh)
    {//??????????? ????? ? ???
        InputStream in =null;
        HttpURLConnection urlConnection = null;
        String strFile=null;
        Log.e("Filmscope","Try to read"+strURL);
        try
        {
            strFile=MakeFileName(strURL);
            CreateFolder();
            File cFile=new File(strFile);
            if(cFile.exists()&&cFile.length()>0)
            {//???? ????
                if(!bRefresh)//????????? ?? ?????-?????? ?? ???? ???
                {
                    Time cTime=new Time();
                    cTime.setToNow();
                    cFile.setLastModified(cTime.toMillis(true));
                    Log.e("Filmscope","Read from cache "+strFile);
                    return strFile;
                }
            }
            else
            {//??? ?????
                Log.e("Filmscope","File doesnot exists "+strFile);
                strFile=null;
            }
            URL url = new URL(strURL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            Log.e("Filmscope","connect "+strURL);
            in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] bArBuffer=new byte[Math.max(1000,in.available())];
            ByteArrayOutputStream cArray=new ByteArrayOutputStream();
            while(true)
            {
                int nRead=in.read(bArBuffer);
                //Log.e("filmstrips",String.format("Read %d bytes",nRead));
                if(nRead<=0)
                    break;
                cArray.write(bArBuffer, 0, nRead);
            }
			/*ByteArrayBuffer cArray=new ByteArrayBuffer(bArBuffer.length);
		    while(true)
		    {
		    	int nRead=in.read(bArBuffer);
		    	//Log.e("filmstrips",String.format("Read %d bytes",nRead));		    	
		    	if(nRead<=0)
		    		break;
		    	cArray.append(bArBuffer, 0, nRead);
		    }*/
            in.close();
            urlConnection.disconnect();
            Log.e("Filmscope","Readed file success "+strURL+ " readed bytes "+String.valueOf(cArray.size()));
            FileOutputStream cWriter=new FileOutputStream(cFile);
            cWriter.write(cArray.toByteArray());
            cWriter.close();
            strFile=cFile.getAbsolutePath();
        }
        catch(Exception ex)
        {
        }
        return strFile;
    }

    private static byte[] ReadFileFromCache(String strFile)
    {//?????? ?? ????
        try
        {
            CreateFolder();
            File cFile=new File(strFile);
            if(!cFile.exists())
                return null;
            FileInputStream cReader=new FileInputStream(cFile);
            byte[] byArData=new byte[(int)cFile.length()];
            cReader.read(byArData);
            cReader.close();
            Time cTime=new Time();
            cTime.setToNow();
            cFile.setLastModified(cTime.toMillis(true));
            return byArData;
        }
        catch(Exception ex){}
        return null;
    }

    public static byte[] ReadFile(String strURL, boolean bRefresh)
    {
        byte[] byArData=null;
        try
        {//?????? ????????? ? ????
            String strFile=MakeFileName(strURL);
            byArData=ReadFileFromCache(strFile);
            if(byArData!=null&&byArData.length>0&&!bRefresh)
                return byArData;
            Log.e("Filmscope","Read file "+strURL);
            byArData=null;
            //???? ??? ? ????-?????? ????? ????
            InputStream in =null;
            HttpURLConnection urlConnection = null;
            URL url = new URL(strURL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(5000);
            urlConnection.setDoOutput(true);
            urlConnection.connect();
            Log.e("Filmscope","connect "+strURL);
            in = new BufferedInputStream(urlConnection.getInputStream());
            byte[] bArBuffer=new byte[Math.max(1000,in.available())];
            ByteArrayOutputStream cArray=new ByteArrayOutputStream();
            while(true)
            {
                int nRead=in.read(bArBuffer);
                //Log.e("filmstrips",String.format("Read %d bytes",nRead));
                if(nRead<=0)
                    break;
                cArray.write(bArBuffer, 0, nRead);
            }
			/*
		    ByteArrayBuffer cArray=new ByteArrayBuffer(bArBuffer.length);
		    while(true)
		    {
		    	int nRead=in.read(bArBuffer);
		    	if(nRead<=0)
		    		break;
		    	cArray.append(bArBuffer, 0, nRead);
		    }*/
            in.close();
            urlConnection.disconnect();
            Log.e("Filmscope","Readed file success "+strURL+ " readed bytes "+String.valueOf(cArray.size()));
            CreateFolder();
            File cFile=new File(strFile);
            FileOutputStream cWriter=new FileOutputStream(cFile);
            byArData=cArray.toByteArray();
            cWriter.write(byArData);
            cWriter.close();
        }
        catch(OutOfMemoryError e1)
        {
            e1.printStackTrace();
        }
        catch(Exception ex)
        {
            ex.printStackTrace();
        }
        return byArData;
    }

    public static void AdjustCacheSize()
    {//?????????? ??????? ???? ? ???????
        if(m_lMaxSize<=0)
            return;//?????????????? ??????
        try
        {
            String strFolder=m_strFolder;
            File cDir=new File(strFolder);
            File[] cArFiles=cDir.listFiles();
            Arrays.sort(cArFiles, new Comparator<File>()
                    {
                        public int compare(File lhs, File rhs)
                        {
                            if(lhs.lastModified()<rhs.lastModified())
                                return 1;
                            if(lhs.lastModified()>rhs.lastModified())
                                return -1;
                            return 0;
                        }
                    }
            );
            long lAllSize=0;
            for(File cOneFile: cArFiles)
            {
                lAllSize+=cOneFile.length();
            }
            int nDelIndex=0;
            StatFs cStatFS=new StatFs(m_strFolder);
            long lBlockSize=cStatFS.getBlockSize();
            long lAvailableBlock=cStatFS.getAvailableBlocks();
            long lAvailableSize=lAvailableBlock*lBlockSize;
            long lMinAvailableSize=m_lMaxSize/10;
            while((lAllSize>=m_lMaxSize||lAvailableSize<lMinAvailableSize)&&nDelIndex<cArFiles.length-1)
            {
                try
                {
                    cArFiles[nDelIndex].delete();
                    cArFiles[nDelIndex]=null;//???? ??????
                }
                catch(Exception ex){}
                nDelIndex++;
                lAllSize=0;
                for(File cOneFile: cArFiles)
                {
                    if(cOneFile!=null)
                        lAllSize+=cOneFile.length();
                }
                lBlockSize=cStatFS.getBlockSize();
                lAvailableBlock=cStatFS.getAvailableBlocks();
                lAvailableSize=lAvailableBlock*lBlockSize;
            }
        }
        catch(Exception ex)
        {

        }
    }

    public static float getFilmLoads(int nID)//сколько фильма закачано (от 0 до 1) в кэш
    {
        return 0.0f;
    }

    public static void clearCache(){//очистка всего кеша
        try {
            String strFolder = m_strFolder;
            File cDir = new File(strFolder);
            File[] cArFiles = cDir.listFiles();
            for (File file : cArFiles) {
                file.delete();
            }
        }
        catch (Exception ex){ ex.printStackTrace();}
    }
}
