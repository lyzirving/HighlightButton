package com.liuyuzhou.highlightanimationbutton;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by lyzirving
 *
 * @params:
 * highlight_button_img_id : standing for the button's view , this parma must be set .
 * highlight_button_height_self_adaption : view's width must be set exact by user, and height adapt to the image's size ratio.
 * highlight_button_width_self_adaption : view's height must be set exact by user, and width adapt to the image's size ratio.
 * highlight_button_max_animation_ratio : determin the max range of the animation . This value must be bigger than 1.
 * highlight_button_outline_color : determin the color of the outline.
 *
 * @requirements:
 * 1  user must set its minSdk >=16
 * 2  HighlightButton uses ValueAnimator , so user can pull in library nineoldandroids to be compatible for the version before Android 11
 *
 */

public class HighlightButton extends View {

    public static final int ANIMATION_MAX_STEPS=100;
    public static final int ANIMATION_MIN_STEPS=1;
    public static final float MIN_ANIMATION_RATIO=1;
    public static final int MIN_OUTLINE_ALPHA=0;
    //255为完全可见
    public static final int MAX_OUTLINE_ALPHA=200;

    private Drawable mOriginalBackground;
    private String mParentClassName;
    private int mViewWidth,mViewHeight;
    private float mImageSizeRatio;
    private ViewGroup mParent;
    private Canvas mContainerCanvas;

    //获取的自定义属性
    private int mImgId;
    private boolean mWidthSelfAdaption;
    private boolean mHeightSelfAdaption;
    private float mMaxAnimationRatio;
    private int mOutlineColor;

    private Bitmap mImg;
    private Bitmap mOutline;
    private Paint mPaint;
    private boolean mDrawOutline;

    //用于获取轮廓的obj
    private Paint mContentPainter;
    private BitmapDrawable mSrc;
    private Bitmap mContainer;

    private ValueAnimator mAnimator;

    public HighlightButton(Context context, AttributeSet attrs)  {
        this(context, attrs, 0);
    }
    public HighlightButton(Context context) {
        this(context, null);
    }
    public HighlightButton(Context context, AttributeSet attrs, int defStyle) {
        super(context,attrs,defStyle);
        init(context,attrs,defStyle);
    }

    protected void adaptToViewSize(int width,int height){
        int imgWidth=mImg.getWidth();
        float imgScaleRatio=(imgWidth*mMaxAnimationRatio)/width;
        if (imgScaleRatio<0.5 && imgScaleRatio>0){
            Matrix m=new Matrix();
            m.postScale(1/imgScaleRatio,1/imgScaleRatio);
            mImg=Bitmap.createBitmap(mImg,0,0,mImg.getWidth(),mImg.getHeight(),m,false);
            mOutline=Bitmap.createBitmap(mOutline,0,0,mOutline.getWidth(),mOutline.getHeight(),m,false);
            mContainer=Bitmap.createBitmap(mContainer,0,0,mContainer.getWidth(),mContainer.getHeight(),m,false);
        }
    }

    protected void calcImageSizeRatio(){
        if (mSrc!=null){
            mImageSizeRatio=(mSrc.getIntrinsicWidth()*1f)/(mSrc.getIntrinsicHeight()*1f);
        }
    }

    protected void getOutline(){
        if (mContentPainter==null){
            mContentPainter=new Paint();
        }
        mContentPainter.setColor(mOutlineColor);
        mContentPainter.setAlpha(MAX_OUTLINE_ALPHA);
        //获取有img产生的background，并将背景重设为用户设置的background
        //这么做的意义主要是为了获得mSrc对象
        mSrc=(BitmapDrawable) getBackground();
        setBackground(mOriginalBackground);

        mContainer=Bitmap.createBitmap(mSrc.getIntrinsicWidth(),mSrc.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        mContainerCanvas=new Canvas(mContainer);
        //在container上绘制只有alpha通道的img
        mContainerCanvas.drawBitmap(mImg.extractAlpha(),0,0,mContentPainter);
        Matrix m=new Matrix();
        m.postScale(1.1f,1.1f);
        //获得img的轮廓outline，并将它放大（比原图大一点）
        mOutline=Bitmap.createBitmap(mContainer,0,0,mContainer.getWidth(),mContainer.getHeight(),m,false);
    }

    protected String getParentClassName(){
        mParent=(ViewGroup) getParent();
        String name=mParent.getClass().getName();
        int ind=name.lastIndexOf(".");
        return name.substring(ind+1);
    }

    protected void init(Context context, AttributeSet attrs, int defStyle){
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.HighlightButtonStyle, defStyle, 0);
        mImgId=a.getResourceId(R.styleable.HighlightButtonStyle_highlight_button_img_id,-1);
        mHeightSelfAdaption=a.getBoolean(R.styleable.HighlightButtonStyle_highlight_button_height_self_adaption,false);
        mWidthSelfAdaption=a.getBoolean(R.styleable.HighlightButtonStyle_highlight_button_width_self_adaption,false);
        mMaxAnimationRatio=a.getDimension(R.styleable.HighlightButtonStyle_highlight_button_max_animation_ratio,1.3f);
        mOutlineColor=a.getColor(R.styleable.HighlightButtonStyle_highlight_button_outline_color,Color.parseColor("#787878"));
        a.recycle();

        if (mImgId<0){
            throw new RuntimeException("custom:highlight_button_img_id shouldn't be empty.");
        }
        if (mMaxAnimationRatio<=1){
            throw new RuntimeException("custom:highlight_button must be set bigger than 1.");
        }

        mImg= BitmapFactory.decodeResource(getResources(),mImgId);
        mDrawOutline=false;
        mOriginalBackground=getBackground();
        setBackgroundResource(mImgId);
        //获得轮廓，并放在mOuline对象中
        getOutline();
        calcImageSizeRatio();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mParentClassName=getParentClassName();

        if (mHeightSelfAdaption && mWidthSelfAdaption){
            throw new RuntimeException("only one attr between custom:highlight_button_fit_width and custom:hightlight_button_fit_height can be set true.");
        }
        // 默认为高度自适应
        if (!mHeightSelfAdaption && !mWidthSelfAdaption){
            mHeightSelfAdaption=true;
        }

        RelativeLayout.LayoutParams rlp=null;
        LinearLayout.LayoutParams llp=null;
        FrameLayout.LayoutParams flp=null;

        //本View只对三个父布局有效
        //设置view的大小自适应img的宽高比例
        switch (mParentClassName){
            case "RelativeLayout":
                rlp=(RelativeLayout.LayoutParams) getLayoutParams();
                resetLayoutParams(rlp);
                setLayoutParams(rlp);
                break;
            case "LinearLayout":
                llp=(LinearLayout.LayoutParams) getLayoutParams();
                resetLayoutParams(llp);
                setLayoutParams(llp);
                break;
            case "FrameLayout":
                flp=(FrameLayout.LayoutParams) getLayoutParams();
                resetLayoutParams(flp);
                setLayoutParams(flp);
                break;
            default:
                throw new RuntimeException("HighlightButton can only be the child of RelativeLayout,LinearLayout and FrameLayout.");
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width=getMeasuredWidth();
        int height=getMeasuredHeight();

        if (mPaint==null){
            mPaint=new Paint();
        }

        //如果img相对于view太小，将img适当的放大
        adaptToViewSize(width,height);

        //控制是否绘制轮廓
        if (mDrawOutline){
            if (mOutline!=null){
                float outlineLeft=(width-mOutline.getWidth())/2;
                float outlineTop=(height-mOutline.getHeight())/2;
                canvas.drawBitmap(mOutline,outlineLeft,outlineTop,mPaint);
            }
        }

        if (mImg!=null){
            float imgLeft=(width-mImg.getWidth())/2;
            float imgTop=(height-mImg.getHeight())/2;
            canvas.drawBitmap(mImg,imgLeft,imgTop,mPaint);
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width=MeasureSpec.getSize(widthMeasureSpec);
        int widthMode=MeasureSpec.getMode(widthMeasureSpec);
        int height=MeasureSpec.getSize(heightMeasureSpec);
        int heightMode=MeasureSpec.getMode(heightMeasureSpec);

        ViewGroup.MarginLayoutParams lp=(ViewGroup.MarginLayoutParams) getLayoutParams();
        int wrapWidth=0,wrapHeight=0;

        wrapWidth=lp.leftMargin+lp.rightMargin+getPaddingLeft()+getPaddingRight()+mOutline.getWidth();
        wrapHeight=lp.topMargin+lp.bottomMargin+getPaddingTop()+getPaddingBottom()+mOutline.getHeight();

        setMeasuredDimension( widthMode==MeasureSpec.EXACTLY ? width:wrapWidth ,
                heightMode==MeasureSpec.EXACTLY ? height : wrapHeight );
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                if (!mDrawOutline){
                    mDrawOutline=true;
                    if (mAnimator==null){
                        mAnimator=ValueAnimator.ofInt(ANIMATION_MIN_STEPS,ANIMATION_MAX_STEPS);
                        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int currentVal=(Integer) animation.getAnimatedValue();
                                //轮廓逐渐变大
                                float curOutlineRatio=MIN_ANIMATION_RATIO+(mMaxAnimationRatio-MIN_ANIMATION_RATIO)*animation.getAnimatedFraction();
                                setOutlineRatio(curOutlineRatio,currentVal);
                            }
                        });
                    }
                    mAnimator.setDuration(1500).start();
                }else if (mDrawOutline){
                    mDrawOutline=false;
                    mAnimator.end();
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    protected void resetLayoutParams(ViewGroup.LayoutParams lp){
        // 当用户选择高度自适应时
        if (mHeightSelfAdaption){
            mViewWidth=lp.width;
            if (mViewWidth== ViewGroup.LayoutParams.MATCH_PARENT){
                mViewWidth=mParent.getWidth();
            }else if (mViewWidth==ViewGroup.LayoutParams.WRAP_CONTENT){
                mViewWidth=mSrc.getIntrinsicWidth();
            }
            mViewHeight=(int)(mViewWidth/mImageSizeRatio);
        }else if (mWidthSelfAdaption){
            //当用户选择宽度自适应时
            mViewHeight=lp.height;
            if (mViewHeight== ViewGroup.LayoutParams.MATCH_PARENT){
                mViewHeight=mParent.getHeight();
            }else if (mViewHeight==ViewGroup.LayoutParams.WRAP_CONTENT){
                mViewHeight=mSrc.getIntrinsicHeight();
            }
            mViewWidth=(int)(mViewHeight*mImageSizeRatio);
        }
        lp.width=mViewWidth;
        lp.height=mViewHeight;
    }

    //实现动画的关键
    public void setOutlineRatio(float ratio , int process){
        //计算透明度
        //当前透明度逐渐变小
        int mCurAlpha=(int) (MAX_OUTLINE_ALPHA-(process*1f/100f)*(MAX_OUTLINE_ALPHA-MIN_OUTLINE_ALPHA));
        //要重新创建bitmap对象以适应新的透明度
        mContainer=Bitmap.createBitmap(mImg.getWidth(),mImg.getHeight(), Bitmap.Config.ARGB_8888);
        mContainerCanvas=new Canvas(mContainer);
        mContentPainter.setAlpha(mCurAlpha);
        mContainerCanvas.drawBitmap(mImg.extractAlpha(),0,0,mContentPainter);

        //计算轮廓大小
        Matrix m=new Matrix();
        m.postScale(ratio,ratio);
        mOutline=Bitmap.createBitmap(mContainer,0,0,mContainer.getWidth(),mContainer.getHeight(),m,false);
        if (ratio==mMaxAnimationRatio){
            mDrawOutline=false;
        }
        invalidate();
    }

}
