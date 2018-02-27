package com.wangpeng.markparseapplication

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.Typeface
import android.text.TextUtils
import android.util.Log
import android.util.Log.i
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.wangpeng.markparseapplication.R.id.content
import com.wangpeng.markparseapplication.R.id.end
import java.io.*
import java.util.*
import kotlin.collections.ArrayList
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.text.style.URLSpan
import android.widget.Toast
import android.support.v4.content.ContextCompat.startActivity
import android.content.Intent
import android.net.Uri
import java.util.regex.Pattern


/**
 * Created by wangpeng on 2018/2/27.
 */
class ParserUtils {
    companion object {
        val H3: String = "###"
        val H2: String = "##"
        val H1: String = "#"
        val BOLD: String = "**"
        val ITALIC: String = "*"
        var IMAGE: String = "![图片]"
        var HREF: String = "[链接]"
        var NO_ORDER_LIST: String = "-"
        // 按行存储 markdown 文件

        fun parseMD(context: Context, fileName: String): LinearLayout {
            var mdList: List<String> = readAssetsFile(context, fileName) as List<String>;
            var mLinearLayout: LinearLayout = LinearLayout(context);
            var mlayoutParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
            var mBlankParams: LinearLayout.LayoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 36)
            var mContentParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
            mlayoutParams.leftMargin = 10
            mlayoutParams.rightMargin = 10
            mLinearLayout.layoutParams = mlayoutParams
            mLinearLayout.orientation = LinearLayout.VERTICAL

            for ((index, item) in mdList.withIndex()) {
                var lineend: Int = item.lastIndexOf("\\n");
                if (item.equals("")) {
                    var blank: TextView = TextView(context)
                    blank.setBackgroundColor(Color.WHITE)
                    mLinearLayout.addView(blank, mBlankParams)
                } else if (item.startsWith(H3)) {
                    var t3: TextView = TextView(context)
                    t3.setTextColor(Color.DKGRAY)
                    t3.setTextSize(42.0f)
                    mLinearLayout.addView(t3, mContentParams)
                    var content: String = item.substring(H3.length, lineend).trim()
                    t3.setText(content)
                } else if (item.startsWith(H2)) {
                    var t2: TextView = TextView(context)
                    t2.setTextColor(Color.DKGRAY)
                    t2.setTextSize(32.0f)
                    var content: String = item.substring(H2.length, lineend).trim()
                    t2.setText(content)
                    mLinearLayout.addView(t2, mContentParams)
                } else if (item.startsWith(H1)) {
                    var t1: TextView = TextView(context)
                    t1.setTextColor(Color.DKGRAY)
                    t1.setTextSize(22.0f)
                    var content: String = item.substring(H1.length, lineend).trim()
                    t1.setText(content)
                    mLinearLayout.addView(t1, mContentParams)
                } else if (item.startsWith(BOLD)) {
                    var boldText: TextView = TextView(context)
                    boldText.setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
                    var conentend: Int = item.lastIndexOf(BOLD);
                    var content: String = item.substring(BOLD.length, conentend).trim()
                    boldText.setText(content)
                    boldText.setTextColor(Color.DKGRAY)
                    mLinearLayout.addView(boldText, mContentParams)
                } else if (item.startsWith(ITALIC)) {
                    var italicText: TextView = TextView(context)
                    italicText.setTypeface(Typeface.MONOSPACE, Typeface.ITALIC)
                    var conentend: Int = item.lastIndexOf(ITALIC);
                    var content: String = item.substring(ITALIC.length, conentend).trim()
                    italicText.setText(content)
                    italicText.setTextColor(Color.DKGRAY)
                    mLinearLayout.addView(italicText, mContentParams)
                } else if (item.startsWith("-")) {
                    var noOrderItem: TextView = TextView(context);
                    var content: String = item.substring(NO_ORDER_LIST.length, lineend).trim()
                    noOrderItem.setText("●  " + content)
                    noOrderItem.setTextColor(Color.DKGRAY)
                    mLinearLayout.addView(noOrderItem, mContentParams)
                } else if (isNoOrderList(item.substring(0, 2))) {
                    var orderItem: TextView = TextView(context);
                    var content: String = item.substring(0, lineend).trim()
                    val noOrderItemContent = SpannableString(content)
                    noOrderItemContent.setSpan(ForegroundColorSpan(Color.BLACK), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                    orderItem.setText(noOrderItemContent)
                    mLinearLayout.addView(orderItem, mContentParams)
                } else if (item.startsWith(IMAGE)) {
                    var mImageView: ImageView = ImageView(context)
                    mImageView.layoutParams = mContentParams
                    var content: String = item.substring(IMAGE.length, lineend).trim()
                    var urlStart: Int = content.indexOf("(")
                    var urlEnd: Int = content.indexOf(")")
                    var urlImg: String = content.substring(urlStart + 1, urlEnd);
                    Glide.with(context).load(urlImg).into(mImageView)
                    mLinearLayout.addView(mImageView, mContentParams)
                    Log.i("parser", "![图片] content:" + content)
                } else if (item.startsWith(HREF)) {
                    var linkText: TextView = TextView(context)
                    linkText.setTextColor(Color.DKGRAY)
                    var content: String = item.substring(HREF.length, lineend).trim()
                    var urlStart: Int = content.indexOf("(")
                    var urlEnd: Int = content.indexOf(")")
                    var urlLink: String = content.substring(urlStart + 1, urlEnd);
                    var contentLink: String = item.substring(item.indexOf(")") + 1, lineend);
                    val ss = SpannableString(contentLink)
                    ss.setSpan(URLSpan(contentLink), 0, contentLink.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ss.setSpan(ForegroundColorSpan(Color.RED), 0, contentLink.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    linkText.setText(ss)
                    linkText.setOnClickListener { v ->
                        val uri = Uri.parse(urlLink)
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        context.startActivity(intent)
                    }
                    mLinearLayout.addView(linkText, mContentParams)
                } else {
                    if (lineend != 0) {
                        var common: TextView = TextView(context)
                        common.setTextColor(Color.DKGRAY)
                        var content: String = item.substring(0, lineend).trim()
                        common.setText(content)
                        mLinearLayout.addView(common, mBlankParams)
                    }
                }
            }
            return mLinearLayout;
        }

        fun readAssetsFile(context: Context, fileName: String): ArrayList<String> {
            val mdList = ArrayList<String>()
            var sb: StringBuffer = StringBuffer();
            var filein: InputStream = context.assets.open(fileName) as InputStream
            var br: BufferedReader = BufferedReader(InputStreamReader(filein) as Reader?)
            br.forEachLine { lineContent ->
                if (lineContent.equals("\\n")) {
                    mdList.add("")
                } else {
                    mdList.add(lineContent)
                }
            }
            Log.i("parser", sb.toString())
            return mdList
        }

        fun isNoOrderList(text: String): Boolean {
            val p = Pattern.compile("\\d{1}\\.", Pattern.CASE_INSENSITIVE)
            val matcher = p.matcher(text)
            return matcher.find()
        }
    }
}