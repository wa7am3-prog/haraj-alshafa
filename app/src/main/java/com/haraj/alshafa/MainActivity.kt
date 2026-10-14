package com.haraj.alshafa

import android.app.Activity
import android.os.Bundle
import android.widget.TextView
import android.view.Gravity

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val textView = TextView(this)
        textView.text = "حراج الشفا - شغال 100% \nالنسخة التجريبية"
        textView.textSize = 22f
        textView.gravity = Gravity.CENTER
        textView.setPadding(50, 300, 50, 50)
        setContentView(textView)
    }
}
