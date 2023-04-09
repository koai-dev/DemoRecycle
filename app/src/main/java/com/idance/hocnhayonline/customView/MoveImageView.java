package com.idance.hocnhayonline.customView;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;

import androidx.appcompat.widget.AppCompatImageView;

public class MoveImageView extends AppCompatImageView {

    private int start_x;
    private int start_y;
    long exitTi;
    private int width;
    private int screenHeight;
    private int statusHeight;
    private int navigationHeight;

    public MoveImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public MoveImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MoveImageView(Context context) {
        super(context);
        init();
    }

    private void init() {

        width = ScreenUtils.getScreenWidth(getContext());

        screenHeight = ScreenUtils.getScreenHeight(getContext());//获得屏幕高度
        statusHeight = ScreenUtils.getStatusHeight(getContext());//获得状态栏的高度
        navigationHeight = ScreenUtils.getVirtualBarHeigh(getContext());//获取虚拟功能键高度

    }

    public void setWidthHeight(int height) {
        screenHeight = height;
    }

    public interface ClickImageViewCallBack {
        void onClickSideEnd();
    }

    private ClickImageViewCallBack onClickCallBack;

    public void setCallBackClick(ClickImageViewCallBack clickImageViewCallBack) {
        this.onClickCallBack = clickImageViewCallBack;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {

            case MotionEvent.ACTION_DOWN:

                start_x = (int) event.getX();
                start_y = (int) event.getY();
                exitTi = System.currentTimeMillis();//记录按下时间
                float sideEndX = getWidth() / 5 * 4;
                float sideTopY = getHeight() / 5;
                if (event.getX() > sideEndX && event.getY() < sideTopY) {
                    if (onClickCallBack != null) {
                        onClickCallBack.onClickSideEnd();
                    }
                }
                break;

            case MotionEvent.ACTION_MOVE:

                int loast_x = (int) event.getX();
                int loast_y = (int) event.getY();

                int px = loast_x - start_x;
                int py = loast_y - start_y;


                float x = getX() + px;
                float y = getY() + py;


                //检测是否到达边缘 左上右下
                //x = x < 0 ? 0 : x > width - getWidth() ? width - getWidth() : x;//暂时去掉x方向的判断

                if (y < 0) {//顶部界限判断，可以根据需求加上标题栏等高度
                    y = 0;
                }

                //底部界限判断，可以根据需求减去底部tap切换栏高度
                if (y > screenHeight - getHeight()) {
                    y = screenHeight - getHeight();
                }


                Log.e("position:", "dX:" + x + " dY:" + y);

                setX(x);
                setY(y);

                //this.x = loast_x;
                //this.y = loast_y;

                break;
            case MotionEvent.ACTION_UP:

                int rawX = (int) event.getRawX();

                if (rawX >= width / 2) {
                    animate().setInterpolator(new DecelerateInterpolator())
                            .setDuration(500)
                            .xBy(width - getWidth() - getX())
                            .start();
                } else {
                    ObjectAnimator oa = ObjectAnimator.ofFloat(this, "x", getX(), 0);
                    oa.setInterpolator(new DecelerateInterpolator());
                    oa.setDuration(500);
                    oa.start();
                }

                if ((System.currentTimeMillis() - exitTi) > 200) {// 系统时间和记录的退出时间差大于2秒
                    //只触发滑动事件
                    return true;
                }

                break;
        }

        //触发点击事件
        return super.onTouchEvent(event);
    }

}
