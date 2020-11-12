package com.sunfb.slidemenulayout.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;

import com.sunfb.slidemenulayout.R;

public class SlideMenuLayout extends HorizontalScrollView {
    private int mMenuRightMargin;
    private View mMenuView, mContentView;
    private boolean mMenuIsOpen;
    private boolean isIntercept;
    private GestureDetector gestureDetector;

    public SlideMenuLayout(Context context) {
        this(context, null);
    }

    public SlideMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.SlideMenuLayout);
        float rightMargin = array.getDimension(R.styleable.SlideMenuLayout_menuRightMargin, dp2px(50));
        mMenuRightMargin = (int) (getScreenWidth() - rightMargin);
        gestureDetector=new GestureDetector(context,simpleOnGestureListener);
        array.recycle();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        //指定宽高 内容页面 的宽度为屏幕的宽度 菜单页面的宽度为 屏幕的宽度 减去 设定宽度
        //获取到的是LinearLayout
        ViewGroup container = (ViewGroup) getChildAt(0);
        int childCount = container.getChildCount();
        //限制用户只能加入两个布局 如果不是就抛出异常告诉用户
        if (childCount != 2) {
            throw new RuntimeException("有且只能添加两个布局");
        }
        //第一步 设置宽高
        //获取菜单 布局
        mMenuView = container.getChildAt(0);
        ViewGroup.LayoutParams menuLayoutParams = mMenuView.getLayoutParams();
        menuLayoutParams.width = mMenuRightMargin;
        //7.0以上的手机必须设置这个
        mMenuView.setLayoutParams(menuLayoutParams);
        mContentView = container.getChildAt(1);
        ViewGroup.LayoutParams contentLayoutParams = mContentView.getLayoutParams();
        contentLayoutParams.width = getScreenWidth();
        mContentView.setLayoutParams(contentLayoutParams);
        //默认是关闭的 但是设置没有用 ? 为什么呢 因为这句话是在 onLayout之前执行的 所以要放到 onLayout方法中
        //closeMenu();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        isIntercept = false;
        if (mMenuIsOpen) {
            //TODO getX()与ev.getX()
            //触摸点 大于 菜单宽度
            float currentX = ev.getX();
            if (currentX > mMenuRightMargin) {
                //关闭菜单
                closeMenu();
                // 子view 不需要响应任何的触摸事件 也就是拦截子view的触摸事件 但是会响应自己的onTouch事件
                //所以要拦截自己的事件
                isIntercept = true;
                return true;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }


    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (isIntercept) {
            return true;
        }
        //设置滑动到手势上
        //在这里有一个问题 在下面的 up 事件中 也对菜单进行了打开关闭操作 所以某些时候会重复 需要做判断
        //快速滑动触发了 就不要再执行下面的方案了
        if(gestureDetector.onTouchEvent(ev)){
            return true;
        }
        if (MotionEvent.ACTION_UP == ev.getAction()) {
            //只需要监听抬起状态 根据滚动距离判断
            int scrollDistance = getScrollX();
            Log.e("TAG", "currentScrollX:  " + scrollDistance);
            if (scrollDistance <= mMenuRightMargin / 2) {
                openMenu();
            } else {
                closeMenu();
            }
            //这里返回是为了保证了 super不执行 否则 smoothScrollTo() 没有效果
            return true;
        }
        return super.onTouchEvent(ev);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.e("TAG","left="+l+" top="+t+" old-left="+oldl+" old-top="+oldt);
        //算一个比例
        float scale = 1f * l / mMenuRightMargin;
        float rightScale = 0.7f + 0.3f * scale;
        mContentView.setPivotX(0);
        mContentView.setPivotY(mContentView.getHeight() / 2);
        mContentView.setScaleX(rightScale);
        mContentView.setScaleY(rightScale);
        float leftScale = 0.7f + 0.3f * (1 - scale);
        mMenuView.setScaleX(leftScale);
        mMenuView.setPivotY(leftScale);
        float alphaScale = 0.5f + 0.5f * (1 - scale);
        mMenuView.setAlpha(alphaScale);
        //最后一个 酷狗 音乐 地下的 退出 按钮一开始 是在右边 被覆盖的
        // 通过平移实现
        mMenuView.setTranslationX(0.2f*l);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //默认关闭菜单 显示主页
        closeMenu();

    }
    GestureDetector.SimpleOnGestureListener simpleOnGestureListener =new GestureDetector.SimpleOnGestureListener(){
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(mMenuIsOpen){
                if(velocityX<0){
                    closeMenu();
                    return true;
                }
            }else {
                if(velocityX>0){
                    openMenu();
                    return true;
                }
            }
            return super.onFling(e1, e2, velocityX, velocityY);

        }
    };

    private int dp2px(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int px2dp(int px) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    public int getScreenWidth() {
        return getResources().getDisplayMetrics().widthPixels;
    }

    public int getScreenHeight() {
        return getResources().getDisplayMetrics().heightPixels;
    }

    public void openMenu() {
        mMenuIsOpen = true;
        smoothScrollTo(0, 0);
    }

    public void closeMenu() {
        mMenuIsOpen = false;
        smoothScrollTo(mMenuView.getWidth(), 0);
    }

}
