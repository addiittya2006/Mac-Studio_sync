package xyz.addiittya.beatbox


import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar

import xyz.addiittya.beatbox.databinding.FragmentBeatBoxBinding
import xyz.addiittya.beatbox.databinding.ListItemSoundBinding

class BeatBoxFragment : Fragment() {
    private var mBeatBox: BeatBox? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        mBeatBox = BeatBox(activity)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val binding = DataBindingUtil
                .inflate<FragmentBeatBoxBinding>(inflater!!, R.layout.fragment_beat_box, container, false)


        binding.recyclerView.layoutManager = GridLayoutManager(activity, 3)
        binding.recyclerView.adapter = SoundAdapter(mBeatBox!!.sounds)

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                mBeatBox!!.setSpeed(progress.toFloat())
                binding.seekBarTextview.text = getString(R.string.playback_label) + (progress + 20).toString() + "%"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        mBeatBox!!.release()
    }


    private inner class SoundHolder constructor(private val mBinding: ListItemSoundBinding) : RecyclerView.ViewHolder(mBinding.root) {

        init {
            mBinding.viewModel = SoundViewModel(mBeatBox!!)
        }

        fun bind(sound: Sound) {
            mBinding.viewModel.setSound(sound)
            mBinding.executePendingBindings()
        }
    }

    private inner class SoundAdapter(private val mSounds: List<Sound>) : RecyclerView.Adapter<SoundHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundHolder {
            val inflater = LayoutInflater.from(activity)
            val binding = DataBindingUtil
                    .inflate<ListItemSoundBinding>(inflater, R.layout.list_item_sound, parent, false)
            return SoundHolder(binding)
        }

        override fun onBindViewHolder(holder: SoundHolder, position: Int, payloads: List<Any>?) {
            val sound = mSounds[position]
            holder.bind(sound)
        }

        override fun onBindViewHolder(holder: SoundHolder, position: Int) {

        }

        override fun getItemCount(): Int {
            return mSounds.size
        }
    }

    companion object {

        fun newInstance(): BeatBoxFragment {
            return BeatBoxFragment()
        }
    }

}
