package xyz.addiittya.beatbox

import android.content.Intent
import android.databinding.BindingAdapter
import android.graphics.Typeface
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.TextView

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this@SplashActivity, BeatBoxActivity::class.java))
        finish()
    }

    companion object {
        @JvmStatic @BindingAdapter("bind:font")
        fun setFont(textView: TextView, fontName: String) {
            textView.typeface = Typeface.createFromAsset(textView.context.assets, "fonts/" + fontName)
        }
    }

}
