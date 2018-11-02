package xyz.addiittya.beatbox

import android.media.AudioManager

class BeatBox(context: android.content.Context) {

    private val mAssets: android.content.res.AssetManager = context.assets
    private val mSounds = java.util.ArrayList<Sound>()
    private val mSoundPool: android.media.SoundPool
    private var mSpeed: Float = 0.toFloat()
    private val TAG = "Beatbox"
    private val SOUNDS_FOLDER = "sample_sounds"
    private val MAX_SOUNDS = 5

    init {
        //This old constructor is deprecated, but needed for compatibility
        mSoundPool = android.media.SoundPool(MAX_SOUNDS, AudioManager.STREAM_MUSIC, 0)
        mSpeed = 1.0f
        loadSounds()
    }

    fun setSpeed(speed: Float) {
        mSpeed = (speed + 20) / 100
    }

    fun play(sound: Sound) {
        val soundId = sound.soundId ?: return
        mSoundPool.play(soundId, //sound id
                1.0f,            //vol left
                1.0f,            //vol right
                1,               //priority
                0,               //loop? yes or no
                mSpeed)          //playback rate
    }

    fun release() {
        mSoundPool.release()
    }

    private fun loadSounds() {
        val soundNames: Array<String>
        try {
            soundNames = mAssets.list(SOUNDS_FOLDER)
            android.util.Log.i(TAG, "Found " + soundNames.size + " sounds.")
        } catch (ioe: java.io.IOException) {
            android.util.Log.e(TAG, "Could not list assets", ioe)
            return
        }

        for (filename in soundNames) {
            try {
                val assetPath = SOUNDS_FOLDER + "/" + filename
                val sound = Sound(assetPath)
                load(sound)
                mSounds.add(sound)
            } catch (ioe: java.io.IOException) {
                android.util.Log.e(TAG, "Could not load sound " + filename, ioe)
            }

        }
    }

    @Throws(java.io.IOException::class)
    private fun load(sound: Sound) {
        val afd = mAssets.openFd(sound.assetPath)
        val soundId = mSoundPool.load(afd, 1)
        sound.soundId = soundId
    }

    val sounds: List<Sound>
        get() = mSounds
}
