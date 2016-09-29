package com.dev.map.myapplication;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nineoldandroids.view.ViewHelper;

import util.LeftMenu;
import view.DragLayout;
import view.OpenNoTouchLinearLayout;


public class MainActivity extends Activity {

    private ListView leftLv;
    private ListView mainLv;
    private OpenNoTouchLinearLayout mainLayout;
    private static final String TAG = "MainActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dragLayout = (DragLayout) findViewById(R.id.dragLayout);
        dragLayout.setOnDragStateChangedListener(odscl);
        initListView();
        mainLayout = (OpenNoTouchLinearLayout) findViewById(R.id.mainLayout);
        mainLayout.setCloseableLayout(dragLayout);
    }

    private DragLayout.OnDragStateChangedListener odscl = new DragLayout.OnDragStateChangedListener() {
        @Override
        public void onStateChanged(DragLayout.State newState) {
            if (newState == DragLayout.State.OPEN) {
                Log.i(TAG,"open");
            } else if (newState == DragLayout.State.CLOSE) {
                Log.i(TAG,"close");
            }
        }

        @Override
        public void onDraging(float percent) {
            // A 主界面透明度的变化 从1 到0
            // 使用属性动画，并使用估值器帮我们计算当前percent对应的透明度的值
            ViewHelper.setAlpha(mainLv, 1 - percent); //floatEvaluator.evaluate(percent, 1, 0)
            // 先获取背景
            Drawable background = dragLayout.getBackground();
            //setColorFilter方法，会按照指定模式（第二个参数），覆盖指定的颜色（第一个参数）到background
            background.setColorFilter(evaluateArgb(percent, Color.BLACK, Color.TRANSPARENT), PorterDuff.Mode.SRC_OVER);
        }
    };
    private DragLayout dragLayout;

    private void initListView() {
        mainLv = (ListView) findViewById(R.id.mainLv);
        mainLv.setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                LeftMenu.NAMES));
        leftLv = (ListView) findViewById(R.id.leftLv);
        leftLv.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1,
                LeftMenu.Strings) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView,
                        parent);
                tv.setTextColor(Color.WHITE);
                return tv;
            }
        });
    }

    public int evaluateArgb(float fraction, Object startValue, Object endValue) {
        int startInt = (Integer) startValue;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = (Integer) endValue;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        return (int) ((startA + (int) (fraction * (endA - startA))) << 24) |
                (int) ((startR + (int) (fraction * (endR - startR))) << 16) |
                (int) ((startG + (int) (fraction * (endG - startG))) << 8) |
                (int) ((startB + (int) (fraction * (endB - startB))));
    }

}
