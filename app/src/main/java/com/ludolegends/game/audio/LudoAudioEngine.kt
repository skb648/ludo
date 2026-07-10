package com.ludolegends.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.ludolegends.game.R
import com.ludolegends.game.vm.SoundCue

class LudoAudioEngine(private val context:Context):AutoCloseable {
    private val pool=SoundPool.Builder().setMaxStreams(5).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).build()).build()
    private val sounds=mapOf(SoundCue.DICE to pool.load(context,R.raw.dice_roll,1),SoundCue.HOP to pool.load(context,R.raw.token_hop,1),SoundCue.CAPTURE to pool.load(context,R.raw.token_kill,1),SoundCue.VICTORY to pool.load(context,R.raw.victory,1))
    private val music=ExoPlayer.Builder(context).build().apply { setMediaItem(MediaItem.fromUri("android.resource://${context.packageName}/${R.raw.ambient_theme}"));repeatMode=ExoPlayer.REPEAT_MODE_ONE;prepare() }
    fun play(cue:SoundCue,volume:Float=.8f,haptics:Boolean=true){sounds[cue]?.let{pool.play(it,volume,volume,1,0,1f)};if(haptics){val v=context.getSystemService(Vibrator::class.java);if(Build.VERSION.SDK_INT>=26)v?.vibrate(VibrationEffect.createOneShot(if(cue==SoundCue.CAPTURE)80 else 25,VibrationEffect.DEFAULT_AMPLITUDE))else @Suppress("DEPRECATION") v?.vibrate(30)}}
    fun startMusic(volume:Float){music.volume=volume;music.playWhenReady=true}
    fun setMusicVolume(volume:Float){music.volume=volume}
    override fun close(){pool.release();music.release()}
}
