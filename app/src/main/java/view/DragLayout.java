package view;

import android.animation.FloatEvaluator;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.support.v4.widget.ViewDragHelper.Callback;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.nineoldandroids.view.ViewHelper;

import util.EventUtil;


public class DragLayout extends FrameLayout implements CloseableLayout {
    private ViewDragHelper viewDragHelper;

    private View leftView;
    private View mainView;
    private int leftWidth;

    // 定义回调的三个步骤
    // 1： 定义接口
    // 2： 提供设置接口对象的方法
    // 3：在合适的时机调用传入对象上相应的方法
    public enum State {
        CLOSE, DRAGING, OPEN;
    }

    public interface OnDragStateChangedListener {
        // 拖拽状态发生变化
        void onStateChanged(State newState);

        // 拖拽的百分比
        void onDraging(float percent);
    }

    OnDragStateChangedListener odscl;

    public void setOnDragStateChangedListener(OnDragStateChangedListener odscl) {
        this.odscl = odscl;
    }

    public DragLayout(Context context) {
        this(context, null);
    }

    public DragLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    // ViewDragHelper的使用步骤：
    // A：定义ViewDragHelper对象
    // B:在拦截触摸事件的onInterceptTouchEvent方法中调用ViewDragHelper的方法shouldInterceptTouchEvent
    // C:在处理触摸事件的onTouchEvent方法中调用ViewDragHelper的processTouchEvent方法
    // D：对Callback进行详细的设置

    private void init() {
        viewDragHelper = ViewDragHelper.create(this, callback);
        floatEvaluator = new FloatEvaluator();
        criticalVel = (int) (160 * getResources().getDisplayMetrics().density);
    }

    // 在onInterceptTouchEvent方法中返回true了，就会把触摸事件交给自己的onTouchEvent处理
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.w("一 onInterceptTouchEvent", EventUtil.getMotionEventAction(ev));
        return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d("1 onTouchEvent", EventUtil.getMotionEventAction(event));
        viewDragHelper.processTouchEvent(event);
        return true;
    }

    private Callback callback = new Callback() {

        // !!尝试捕获view
        // 参数child，表示触摸点所对应的view
        // 参数pointerId，跟多点触摸有关
        // 返回值：表示child具有被拖动的可能性,具体能否移动，还需要继续判断
        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            Log.w("二 tryCaptureView", " ");
            // 明确mainView和leftView具有被拖拽的可能性
            if (child == mainView) {
                return true;
            } else if (child == leftView) {
                return true;
            }
            return false;
        }

        // 如果tryCaptureView返回true了，就会被调用
        // capturedChild 被捕获的view
        // activePointerId ，跟多点触摸有关
        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            Log.w("三 onViewCaptured", " ");
        }

        // 返回view的拖拽水平范围，一般返回正数即可，如果不重写默认返回0，可能导致bug
        @Override
        public int getViewHorizontalDragRange(View child) {
            Log.d("? getViewHorizontalDragRange", " ");
            return 1;
        }

        // 返回view的拖拽垂直范围，一般返回正数即可，如果不重写默认返回0，可能导致bug
        @Override
        public int getViewVerticalDragRange(View child) {
            Log.d("? getViewVerticalDragRange", " ");
            return 1;
        }

        // !! 在移动之前被调用
        // 返回值决定了child的移动位置
        // child将要被移动的view
        // dx 是与上次触摸事件相比，此次触摸事件的x轴移动距离
        // left 是经过ViewDragHelper处理的值，= child.getLeft() 当前view的left边的位置 + dx;
        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            Log.d("2 clampViewPositionHorizontal",
                    String.format("left %s, dx %s", left, dx));
            // 如果不想让leftView被水平方向移动？
            // if(child == leftView){
            // return leftView.getLeft();
            // }
            // leftview的移动范围在 - leftView的width和 0之间
            if (child == leftView) {
                // if (left > 0) {
                // return 0;
                // } else if (left < -leftWidth) {
                // return -leftWidth;
                // } else {
                // return left;
                // }

            } else if (child == mainView) {
                if (left < 0) {
                    return 0;
                } else if (left > leftWidth) {
                    return leftWidth;
                } else {
                    return left;
                }
            }
            return left - dx / 2;// left =child.getLeft() + dx
        }

        // !! 在移动之前被调用
        // 返回值决定了child的移动位置
        // child将要被移动的view
        // dy 是与上次触摸事件相比，此次触摸事件的y轴移动距离
        // top 是经过ViewDragHelper处理的值，= child.getTop() 当前view的top边的位置 + dy;
        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            Log.d("2 clampViewPositionVertical",
                    String.format("top %s, dy %s", top, dy));
            return child.getTop();
        }

        // ！！当ViewDragHelper处理了clampViewPositionVertical、clampViewPositionHorizontal的返回值后（移动了）
        // 想要发生什么额外的变化
        // !! 实际上在这个方法之前，在clampViewPositionVertical\Horizontal 之后，会调用
        // offsetLeftAndRight 和offsetTopAndBottom去移动view的位置
        // changedView 被移动了的view
        // left 是移动后changedView的left边的位置
        // top 是移动后changedView的top边的位置
        // dx 移动前后，left边的变化 变化后的 - 变化前的
        // dy 移动前后，top边的变化 变化后的 - 变化前的
        @Override
        public void onViewPositionChanged(View changedView, int left, int top,
                                          int dx, int dy) {
            Log.d("3 onViewPositionChanged", String.format(
                    "left %s, top %s,dx %s, dy %s", left, top, dx, dy));
            if (changedView == leftView) {
                // 把leftView移动回原来的位置
                leftView.offsetLeftAndRight(-dx);
                // mainView.offsetLeftAndRight(dx);

                // 检测如果mainView的位置，发现已经到完全打开的时候，就不再调用mainView.offsetLeftAndRight(dx);
                int tmpMainLeft = mainView.getLeft() + dx;
                if (tmpMainLeft > leftWidth) {
                    // 如果要到达的位置超过了leftWidth，就最多移动到leftWidth
                    mainView.offsetLeftAndRight(leftWidth - mainView.getLeft());
                } else if (tmpMainLeft < 0) {
                    // 如果要到达的位置小于了0，就最多移动到0
                    mainView.offsetLeftAndRight(0 - -mainView.getLeft());
                } else {
                    mainView.offsetLeftAndRight(dx);
                }

                // ViewHelper.setTranslationX(leftView,
                // leftView.getTranslationX() + dx);
                // 与上面等价
                // mainView.layout(mainView.getLeft() + dx, mainView.getTop(),
                // mainView.getRight() +dx, mainView.getBottom());
            } else if (changedView == mainView) {
                // leftView.offsetLeftAndRight(dx);
            }

            // 处理动画：
            // 把动画相关的变量都转化为1个： mainView的left
            // 再转化成一个百分比；所有的情况都跟百分比有关（菜单关闭时为0，完全打开时为1）
            // ViewHelper.setTranslationX(leftView,-leftWidth +
            // mainView.getLeft() );

            changeState(State.DRAGING);

            float percent = 1.0f * mainView.getLeft() / leftWidth;
            if (odscl != null) {
                odscl.onDraging(percent);
            }
            dispatchAnimation(percent);

            // leftView.setTranslationX(100);
            // // nineoldandrois中有ViewHelper可帮我们完成属性动画
            // // 与上面的方法等价
            // ViewHelper.setTranslationX(leftView, 100);
            // 在api10 及以前 offsetLeftAndRight 和offsetTopAndBottom 没有进行重绘操作
            // Build.VERSION.SDK_INT 当前运行应用程序的机器的api版本
            // Build.VERSION_CODES 中存放有所有的版本号
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
                invalidate();
            }

        }

        // 拖拽的状态发生了变化
        // 状态值一共有3种 ： STATE_IDLE 空闲 STATE_DRAGGING 手指拖拽 STATE_SETTLING
        // 自动化（松手到自动归位的状态）
        @Override
        public void onViewDragStateChanged(int state) {
            Log.w("四 六 onViewDragStateChanged", EventUtil.getDragState(state));
            if (state == ViewDragHelper.STATE_IDLE) {
                // 判断mainview的left位置，决定新的状态
                if (mainView.getLeft() == 0) {
                    // 关闭
                    changeState(State.CLOSE);
                } else if (mainView.getLeft() == leftWidth) {
                    // 打开状态
                    changeState(State.OPEN);
                } else {
                    // 拖拽 状态
                    changeState(State.DRAGING);
                }

            }

        }

        // 当松手时会被调用
        // releasedChild 释放的view
        // xvel 水平方向的速度，单位是 像素/秒
        // yvel 垂直方向的速度，单位是 像素/秒
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            Log.w("五 onViewReleased",
                    String.format("xvel %s,yvel %s", xvel, yvel));
            if (xvel > criticalVel) { // 快速从左向右滑动，代表打开的手势
                open();
            } else if (xvel < -criticalVel) {// 快速从右向左滑动，代表关闭的手势
                close();
            } else if (mainView.getLeft() < leftWidth / 2) { // 应该关闭
                close();
            } else if (mainView.getLeft() >= leftWidth / 2) { // 应该打开
                open();
            }

        }

        // 如下方法与边缘触摸有关
        @Override
        public void onEdgeTouched(int edgeFlags, int pointerId) {
        }

        @Override
        public boolean onEdgeLock(int edgeFlags) {
            return super.onEdgeLock(edgeFlags);
        }

        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            super.onEdgeDragStarted(edgeFlags, pointerId);
        }

        // 去捕获view的时候会调用
        @Override
        public int getOrderedChildIndex(int index) {
            return super.getOrderedChildIndex(index);
        }

    };
    State currentState = State.CLOSE;

    private void changeState(State newState) {
        if (newState == currentState) {
            return;
        }
        if (odscl != null) {
            odscl.onStateChanged(newState);
        }
        Log.d("changeState", "" + newState);
        currentState = newState;
    }

    @Override
    public void close() {
        // 让viewDragHelper去判断mainView的left 和 top能否平滑地移动到参数做制定的位置
        // 如果mainView的当前位置与指定位置有差异，就会返回true
        if (viewDragHelper.smoothSlideViewTo(mainView, 0, 0)) {
            // ViewCompat关于view的兼容包，有些api是新的，旧版本用不了
            // postInvalidateOnAnimation 下一次需要绘制的时候（1000 / 60）重绘
            // 重绘的目标是当前对象，并不是mainView，因为mainView的摆放是由它的父控件决定的
            ViewCompat.postInvalidateOnAnimation(this);

        }
    }

    public void open() {
        if (viewDragHelper.smoothSlideViewTo(mainView, leftWidth, 0)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    @Override
    public void computeScroll() {
        // continueSettling 这个方法会根据scroller判断是否需要继续移动，并移动，
        // 传的参数是true，因为文档要求
        if (viewDragHelper.continueSettling(true)) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    private FloatEvaluator floatEvaluator;

    private int criticalVel;

    private void dispatchAnimation(float percent) {
        // 就需要使用估值器
        // 经过分析
        // 主面板有1个动画： 缩放 从1 --> 0.8

        // 第二个参数应该传绝对位置
        ViewHelper.setPivotX(mainView, 0);
        ViewHelper.setPivotY(mainView, mainView.getHeight() / 2);
        // 透明度 0-->1
        ViewHelper.setAlpha(leftView, floatEvaluator.evaluate(percent, 0, 1));
    }

    // 当完成布局的加载，可获取自view了，实际上，这个方法发生在activity的oncreate方法中
    protected void onFinishInflate() {
        leftView = getChildAt(0);
        mainView = getChildAt(1);

        leftView.post(new Runnable() {

            @Override
            public void run() {
                leftWidth = leftView.getWidth();
                Log.d("post", "getWidth" + leftView.getWidth());
            }
        });

        // 不管用
        // Log.d("onFinishInflate", "getWidth"+leftView.getWidth());
        // leftView.measure(0, 0);
        // Log.d("onFinishInflate",
        // "getMeasuredWidth"+leftView.getMeasuredWidth());

    }

    @Override
    public boolean isClosed() {
        return mainView.getLeft() == 0;
    }


    // 在DragLayout大小变化的时候会被调用
    // protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    // super.onSizeChanged(w, h, oldw, oldh);
    // Log.d("onSizeChanged", "getWidth" + leftView.getWidth());
    // Log.d("onSizeChanged",
    // "getMeasuredWidth" + leftView.getMeasuredWidth());
    // }

}
