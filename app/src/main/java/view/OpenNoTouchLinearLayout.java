package view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.widget.LinearLayout;

import util.EventUtil;


// 在DragLayout打开的时候，让此布局拦截触摸事件，不往下传递
// 在DragLayout打开的时候，点击此布局，关闭DragLayout
public class OpenNoTouchLinearLayout extends LinearLayout {
	CloseableLayout closeableLayout;

	public OpenNoTouchLinearLayout(Context context, AttributeSet attrs,
			int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init();
	}

	public OpenNoTouchLinearLayout(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public OpenNoTouchLinearLayout(Context context) {
		this(context, null);
	}

	private void init() {
		gestureDetector = new GestureDetector(getContext(), listener);

	}

	private OnGestureListener listener = new GestureDetector.SimpleOnGestureListener() {
		// 单击手指抬起的时候gestureDetector会调用listener的方法
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Log.d("onSingleTapUp", " ");
			// 表示命中手势了
			return true;
		}

	};
	private GestureDetector gestureDetector;

	public void setCloseableLayout(CloseableLayout closeableLayout) {
		this.closeableLayout = closeableLayout;
	}

	// 拦截事件
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (closeableLayout != null && !closeableLayout.isClosed()) {
			// 把触摸事件转移动自己的onTouchEvent中，不再向下传递
			return true;
		} else {
			return super.onInterceptTouchEvent(ev);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.e("onTouchEvent", EventUtil.getMotionEventAction(event));

		if (closeableLayout != null && !closeableLayout.isClosed()) {
			// 不断地把触摸事件交给gestureDetector处理，它发现listener有方法返回true了，就会返回true
			if (gestureDetector.onTouchEvent(event)) {
				closeableLayout.close();
			}
			return true;
		} else {
			return super.onTouchEvent(event);
		}

	}

}
