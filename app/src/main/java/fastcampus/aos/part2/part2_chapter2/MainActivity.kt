package fastcampus.aos.part2.part2_chapter2

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import fastcampus.aos.part2.part2_chapter2.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    companion object {
        const val REQUEST_RECORD_AUDIO_CODE = 200
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.playButton.setOnClickListener {

        }

        binding.recordButton.setOnClickListener {
            checkPermission()
        }

        binding.stopButton.setOnClickListener {

        }
    }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED -> {
                // 권한 허가 후 동작 추가
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.RECORD_AUDIO
            ) -> {
                // 권한 획득 이유 설명
                showPermissionRationalDialog()
            }

            else -> {
                requestPermissions(
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
            // TODO 녹음 작업 시작
        } else {
            // 시스템 권한 체크
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.RECORD_AUDIO)) {
                showPermissionRationalDialog()
            } else {
                showPermissionSettingDialog()
            }
        }
    }

    private fun showPermissionRationalDialog() {
        AlertDialog.Builder(this)
            .setMessage("녹음 권한을 켜주셔야 앱을 정상적으로 사용할 수 있습니다.")
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
            .setMessage("녹음 권한을 켜주셔야 앱을 정상적으로 사용할 수 있습니다.\n앱 설정 화면으로 진입하셔서 권한을 켜주세요.")
            .setPositiveButton("권한 변경하러 가기") { _, _ ->
                navigateToAppSetting()
            }.setNegativeButton("취소") { dialog, _ -> dialog.cancel() }
            .show()
    }

    private fun navigateToAppSetting() {
        startActivity(
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
        )
    }
}