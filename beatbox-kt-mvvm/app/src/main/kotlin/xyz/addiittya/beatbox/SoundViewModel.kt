package xyz.addiittya.beatbox

import android.databinding.BaseObservable
import android.databinding.Bindable

class SoundViewModel(private val mBeatBox: BeatBox) : BaseObservable() {
    private var mSound: Sound? = null

//    val title: String
//        @Bindable
//        get() = mSound!!.name

    @Bindable
    fun getTitle(): String {
        return mSound!!.name
    }

    fun setSound(sound: Sound) {
        mSound = sound
        notifyChange()
    }

    fun onButtonClicked() {
        mBeatBox.play(mSound!!)
    }


}
