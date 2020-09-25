package com.akmanaev.common;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class ZoomView extends View 
{
	private static final int WIDTH = 10;
	
    private static final int REMAIN = Color.argb(128,49, 49, 49);
    private static final int PROCEED = Color.argb(80,22, 237, 72);
    private static final int CURRENT = Color.argb(255,255, 222,10);
    
    private int mHeight;
    private int mWidth=WIDTH;

    private int mMax=100;
    private int mProceedColor=PROCEED;
    private int mRemainColor=REMAIN;
    private int mCurrentColor=CURRENT;
    private int mCurrent=0;
    
    private Paint m_cPaint = new Paint();
    private LinearGradient m_cGradientProceed=new LinearGradient (0,0, 1, 1, new int[]{0,0}, null, Shader.TileMode.REPEAT);
    private LinearGradient m_cGradientRemain=new LinearGradient (0,0, 1, 1, new int[]{0,0}, null, Shader.TileMode.REPEAT);
    private LinearGradient m_cGradientCurrent=new LinearGradient (0,0, 1, 1, new int[]{0,0}, null, Shader.TileMode.REPEAT);
    
    private boolean []m_bArProgress=new boolean[100];
    
    public int getProceedColor()
    {
    	return mProceedColor;
    }
    
    public synchronized void setProceedColor(int nColor)
    {
    	mProceedColor=nColor;
    	UpdateGradient();
    }
    
    public int getRemainColor()
    {
    	return mRemainColor;
    }
    
    public synchronized void setRemainColor(int nColor)
    {
    	mRemainColor=nColor;
    	UpdateGradient();
    }
    
	public ZoomView(Context context) 
	{
		super(context);
		m_cPaint.setStyle(Style.FILL);
	}

	public ZoomView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
		m_cPaint.setStyle(Style.FILL);
	}

	public ZoomView(Context context, AttributeSet attrs, int defStyle) 
	{
		super(context, attrs, defStyle);
		m_cPaint.setStyle(Style.FILL);
	}

	public synchronized int getCurrent()
	{
		return mCurrent;
	}
	
    public synchronized void setCurrent(int nCurrent)
    {
    	mCurrent=nCurrent;
    	refreshProgress();
    }

    public synchronized void setProgress(int progress)
    {
    	setProgress(progress,false);
    }
    
    private synchronized void setProgress(int progress, boolean bFromUser)
    {
    	if (progress < 0)
        {
            progress = 0;
        }

        if (progress >= mMax)
        {
            progress = mMax-1;
        }

        m_bArProgress[progress]=true;
        refreshProgress();
    }
    
    public synchronized void setMax(int max)
    {
        this.mMax = max;
        m_bArProgress=new boolean[max];
    }
    
    public synchronized int getMax()
    {
        return this.mMax;
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);
        
        for(int n=0;n<mMax;n++)
        {
        	float currentPosTop = mHeight-((float) (n+1) / mMax) * (float) this.mHeight;
        	float currentPosBottom = mHeight-((float) n / mMax) * (float) this.mHeight;
        	RectF rcCur=new RectF(0,currentPosTop,mWidth,currentPosBottom);
        	if(m_bArProgress[n])
        		m_cPaint.setShader(m_cGradientProceed);
        	else
        		m_cPaint.setShader(m_cGradientRemain);
        	canvas.drawRect(rcCur, m_cPaint);
        }
        
        float currentPosTop = mHeight-((float) mCurrent / mMax) * (float) this.mHeight;
        float currentPosBottom = mHeight-((float) (mCurrent-1) / mMax) * (float) this.mHeight;
        RectF rcCur=new RectF(0,currentPosTop,mWidth,currentPosBottom);
        m_cPaint.setShader(m_cGradientCurrent);
        //canvas.restore();
        canvas.drawRect(rcCur, m_cPaint);
    }
    
    @Override
    protected void onMeasure(int widthSpecId, int heightSpecId)
    {
        this.mHeight = View.MeasureSpec.getSize(heightSpecId);
        this.mWidth = View.MeasureSpec.getSize(widthSpecId); 
        UpdateGradient();
        setMeasuredDimension(this.mWidth, this.mHeight);
    }
    
    private synchronized void refreshProgress()
    {
    	post(new Runnable()
    	{
			public void run() 
			{
				invalidate();
			}    		
    	});        
    }
   
    private void UpdateGradient()
    {
    	int clrMinProceed=Color.argb(Color.alpha(mProceedColor)/4,Color.red(mProceedColor), Color.green(mProceedColor), Color.blue(mProceedColor));
    	int clrMinRemain=Color.argb(Color.alpha(mRemainColor)/4,Color.red(mRemainColor), Color.green(mRemainColor), Color.blue(mRemainColor));
    	if(mWidth<mHeight)
    	{//вертикальный 
    		m_cGradientProceed=new LinearGradient (0,0, mWidth, 0, new int[]{clrMinProceed,mProceedColor,clrMinProceed}, null, Shader.TileMode.REPEAT);    		
    		m_cGradientRemain=new LinearGradient (0,0, mWidth, 0, new int[]{clrMinRemain,mRemainColor,clrMinRemain}, null, Shader.TileMode.REPEAT);
    		m_cGradientCurrent=new LinearGradient (0,0, mWidth, 0, new int[]{clrMinRemain,mCurrentColor,clrMinRemain}, null, Shader.TileMode.REPEAT);
    	}
    	else
    	{//горизонтальное расположение
    		m_cGradientProceed=new LinearGradient (0,0, 0, mHeight, new int[]{clrMinProceed,mProceedColor,clrMinProceed}, null, Shader.TileMode.REPEAT);    		
    		m_cGradientRemain=new LinearGradient (0,0, 0, mHeight, new int[]{clrMinRemain,mRemainColor,clrMinRemain}, null, Shader.TileMode.REPEAT);		
    		m_cGradientCurrent=new LinearGradient (0,0, 0, mHeight, new int[]{clrMinRemain,mCurrentColor,clrMinRemain}, null, Shader.TileMode.REPEAT);
    	}
    }
}
