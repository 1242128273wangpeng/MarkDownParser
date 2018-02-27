package com.wangpeng.markparseapplication

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.RelativeLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var content: RelativeLayout = findViewById<RelativeLayout>(R.id.content);
        var mdlayout: LinearLayout = ParserUtils.parseMD(this@MainActivity, "text.md");
        content.addView(mdlayout)
    }
}
