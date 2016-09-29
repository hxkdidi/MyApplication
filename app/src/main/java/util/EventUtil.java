package util;

import android.support.v4.widget.ViewDragHelper;
import android.view.MotionEvent;

public class EventUtil {
	public static String getMotionEventAction(MotionEvent me) {
		//@formatter:off
		int action = me.getAction();
		return 
		  action == MotionEvent.ACTION_DOWN ? "DOWN"
		: action == MotionEvent.ACTION_MOVE ? "MOVE"
		: action == MotionEvent.ACTION_UP ? "UP"
		: action == MotionEvent.ACTION_CANCEL ? "ACTION_CANCEL"
		: action == MotionEvent.ACTION_HOVER_ENTER ? "HOVER_ENTER"
		: action == MotionEvent.ACTION_HOVER_MOVE ? "HOVER_MOVE"
		: action == MotionEvent.ACTION_HOVER_EXIT ? "HOVER_EXIT"
		: action == MotionEvent.ACTION_MASK ? "ACTION_MASK"
		: action == MotionEvent.ACTION_OUTSIDE ? "OUTSIDE"
		: action == MotionEvent.ACTION_SCROLL ? "SCROLL "
		: "OTHER";
		//@formatter:on
	}
	public static String getDragState(int state) {
		//@formatter:off
		return state == ViewDragHelper.STATE_DRAGGING ? "DRAGGING" 
		:  state == ViewDragHelper.STATE_IDLE ? "IDLE" 
		:  state == ViewDragHelper.STATE_SETTLING ? "SETTLING" : "WRONG";
		//@formatter:on
	}
}
