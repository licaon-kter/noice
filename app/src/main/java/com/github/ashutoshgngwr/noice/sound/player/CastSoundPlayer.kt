package com.github.ashutoshgngwr.noice.sound.player

import com.github.ashutoshgngwr.noice.sound.PlaybackManager
import com.github.ashutoshgngwr.noice.sound.Sound
import com.google.android.gms.cast.framework.CastSession
import com.google.gson.GsonBuilder
import com.google.gson.annotations.Expose

/**
 * [CastSoundPlayer] implements [SoundPlayer] that sends the control events
 * to the cast receiver application.
 */
class CastSoundPlayer(
  private val session: CastSession,
  private val namespace: String,
  sound: Sound
) : SoundPlayer() {

  @Expose
  @Suppress("unused")
  val soundKey = sound.key

  @Expose
  @Suppress("unused")
  val isLooping = sound.isLoopable

  @Expose
  var volume: Float = 0.0f
    private set

  @Expose
  var state = PlaybackManager.State.STOPPED

  private val gson = GsonBuilder()
    .excludeFieldsWithoutExposeAnnotation()
    .create()

  override fun setVolume(volume: Float) {
    if (this.volume == volume) {
      return
    }

    this.volume = volume

    // since volume update will only take effect during the PLAYING state, it would be
    // redundant to send updates for others. Once the player comes back to PLAYING state
    // the volume will be updated along with state update.
    if (state == PlaybackManager.State.PLAYING) {
      notifyChanges()
    }
  }

  override fun play() {
    this.state = PlaybackManager.State.PLAYING
    notifyChanges()
  }

  override fun pause() {
    this.state = PlaybackManager.State.PAUSED
    notifyChanges()
  }

  override fun stop() {
    this.state = PlaybackManager.State.STOPPED
    notifyChanges()
  }

  private fun notifyChanges() {
    session.sendMessage(namespace, gson.toJson(this))
  }
}
