package fastcampus.aos.part2.part2_chapter2

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import fastcampus.aos.part2.part2_chapter2.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity(), OnTimerTickListener {

    // 릴리즈 -> 녹음 중 -> 릴리즈
    // 릴리즈 -> 재생 -> 릴리즈
    private enum class State {
        RELEASE, RECORDING, PLAYING
    }

    private lateinit var timer: Timer

    private lateinit var binding: ActivityMainBinding
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    private var fileName: String = ""
    private var state: State = State.RELEASE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"
        timer = Timer(this)

        binding.playButton.setOnClickListener {
            when (state) {
                State.RELEASE -> {
                    onPlay(true)
                }

                else -> {}
            }
        }

        binding.recordButton.setOnClickListener {
            when (state) {
                State.RELEASE -> {
                    checkPermission()
                }

                State.RECORDING -> {
                    onRecord(false)
                }

                State.PLAYING -> {

                }
            }
        }

        binding.stopButton.setOnClickListener {
            when (state) {
                State.PLAYING -> {
                    onPlay(false)
                }

                else -> {}
            }
        }

        initViews()
    }

    private fun initViews() {
        binding.playButton.apply {
            isEnabled = false
            alpha = 0.3f
        }
        binding.stopButton.apply {
            isEnabled = false
            alpha = 0.3f
        }
    }


    /** Permission */

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                onRecord(true)
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                showPermissionRationalDialog()
            }

            else -> {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val audioRecordPermissionGranted =
            requestCode == REQUEST_RECORD_AUDIO_CODE && grantResults.firstOrNull() == PackageManager.PERMISSION_GRANTED

        if (audioRecordPermissionGranted) {
            onRecord(true)
        } else {
            if (
                ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.RECORD_AUDIO
                )
            ) {
                showPermissionRationalDialog()
            } else {
                showPermissionSettingDialog()
            }
        }
    }

    /** Click Action */

    private fun onRecord(isRecord: Boolean) = if (isRecord) startRecording() else stopRecording()

    private fun startRecording() {
        state = State.RECORDING
        recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            setOutputFile(fileName)
            setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)

            try {
                prepare()
            } catch (e: IOException) {
                Log.e("onRecord", "prepare() failed")
                Log.e("onRecord", "cause: ${e.cause}, message: ${e.message}")
            }

            start()
        }

        binding.waveFormView.clearData()
        timer.start()

        changeRecordUI()
    }

    private fun stopRecording() {
        state = State.RELEASE
        recorder?.apply {
            stop()
            release()
        }
        recorder = null

        timer.stop()

        changeRecordUI()
    }

    private fun changeRecordUI() {
        binding.recordButton.apply {
            setImageDrawable(
                ContextCompat.getDrawable(
                    applicationContext,
                    if (state == State.RELEASE)
                        R.drawable.baseline_fiber_manual_record_24
                    else
                        R.drawable.baseline_pause_24
                )
            )

            imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(
                    applicationContext,
                    if (state == State.RELEASE)
                        R.color.red
                    else
                        R.color.black
                )
            )
        }

        binding.playButton.apply {
            isEnabled = state == State.RELEASE
            alpha = if (state == State.RELEASE) 1.0f else 0.3f
        }

        binding.stopButton.apply {
            isEnabled = state == State.RELEASE
            alpha = if (state == State.RELEASE) 1.0f else 0.3f
        }
    }

    private fun onPlay(isPlay: Boolean) = if (isPlay) startPlaying() else stopPlaying()

    private fun startPlaying() {
        state = State.PLAYING

        player = MediaPlayer().apply {
            try {
                setDataSource(fileName)
                prepare()
            } catch (e: IOException) {
                Log.e("onPlay", "prepare() failed")
                Log.e("onPlay", "cause: ${e.cause}, message: ${e.message}")
            }
            start()
        }

        binding.waveFormView.clearWave()
        timer.start()

        player?.setOnCompletionListener {
            stopPlaying()
        }

        changePlayUI()
    }

    private fun stopPlaying() {
        state = State.RELEASE

        player?.release()
        player = null

        timer.stop()

        changePlayUI()
    }

    private fun changePlayUI() {
        binding.recordButton.apply {
            isEnabled = state == State.RELEASE
            alpha = if (state == State.RELEASE) 1.0f else 0.3f
        }
    }

    /** Dialog */

    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.permission_rational_message))
            .setPositiveButton("권한 허용하기") { _, _ ->
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    REQUEST_RECORD_AUDIO_CODE
                )
            }.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun showPermissionSettingDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.permission_setting_message))
            .setPositiveButton("권한 변경하러 가기") { _, _ ->
                navigateToAppSetting()
            }.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
            .show()
    }

    /** Move */

    private fun navigateToAppSetting() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        )
    }

    /** listener */

    @SuppressLint("DefaultLocale")
    override fun onTick(duration: Long) {
        val millSecond = (duration % 1000) / 10
        val second = (duration / 1000) % 60
        val minute = (duration / 1000) / 60

        binding.timerTextView.text = String.format("%02d:%02d.%02d", minute, second, millSecond)

        if (state == State.PLAYING) {
            binding.waveFormView.replayAmplitude()
        } else if (state == State.RECORDING) {
            binding.waveFormView.addAmplitude(recorder?.maxAmplitude?.toFloat() ?: 0f)
        }
    }

    companion object {
        const val REQUEST_RECORD_AUDIO_CODE = 200
    }
}