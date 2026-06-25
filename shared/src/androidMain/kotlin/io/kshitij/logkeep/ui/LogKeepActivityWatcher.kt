package io.kshitij.logkeep.ui

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import java.util.WeakHashMap

internal object LogKeepActivityWatcher : Application.ActivityLifecycleCallbacks {

    private val buttons = WeakHashMap<Activity, View>()

    override fun onActivityResumed(activity: Activity) {
        if (activity is LogKeepActivity) return
        val btn = createFloatingButton(activity)
        (activity.window.decorView as ViewGroup).addView(btn)
        buttons[activity] = btn
    }

    override fun onActivityPaused(activity: Activity) {
        buttons.remove(activity)?.let {
            (activity.window.decorView as ViewGroup).removeView(it)
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit
    override fun onActivityStarted(activity: Activity) = Unit
    override fun onActivityStopped(activity: Activity) = Unit
    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
    override fun onActivityDestroyed(activity: Activity) = Unit
}

private fun createFloatingButton(context: Context): View {
    val btn = TextView(context).apply {
        text = "Logs"
        setBackgroundColor(Color.BLACK)
        setTextColor(Color.WHITE)
        setPadding(24, 16, 24, 16)
        setOnClickListener {
            it.context.startActivity(Intent(it.context, LogKeepActivity::class.java))
        }
    }
    btn.layoutParams = FrameLayout.LayoutParams(
        FrameLayout.LayoutParams.WRAP_CONTENT,
        FrameLayout.LayoutParams.WRAP_CONTENT
    ).apply {
        gravity = Gravity.BOTTOM or Gravity.END
        bottomMargin = 64
        rightMargin = 32
    }
    return btn
}
