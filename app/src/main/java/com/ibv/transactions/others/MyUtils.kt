package com.ibv.transactions.others;

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.text.Html
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.URLUtil
import android.widget.ImageView
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.google.gson.JsonObject
import com.ibv.transactions.base.BaseActivity

import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.sql.Time
import java.text.DecimalFormat
import java.text.Format
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class MyUtils {

    companion object {

        fun isValidPassword(password: String?): Boolean {
            if (password != null && password.length < 7) {
                return false
            }
            val pattern: Pattern
            val matcher: Matcher
            val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
            pattern = Pattern.compile(PASSWORD_PATTERN)
            matcher = pattern.matcher(password)
            return matcher.matches()
        }

        fun viewGone(view: View?) {
            if (view != null) {
                view.visibility = View.GONE

            }
        }

        fun viewVisible(view: View?) {
            if (view != null && (view.visibility == View.INVISIBLE || view.visibility == View.GONE)) {
                view.visibility = View.VISIBLE

            }
        }

        fun isEmptyString(value: String?): Boolean {
            return TextUtils.isEmpty(value) || TextUtils.isEmpty(value?.trim())
        }



        fun contains(value: String?, match: String?): Boolean {
            return !isEmptyString(value) && !isEmptyString(match) && value!!.contains(match!!)
        }


    }


}
