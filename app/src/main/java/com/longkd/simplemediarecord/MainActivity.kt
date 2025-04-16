package com.longkd.simplemediarecord

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.longkd.simplemediarecord.databinding.ActivityMainBinding
import com.longkd.simplemediarecord.util.RecPermissionManager
import com.longkd.simplemediarecord.util.calculateProgressPercent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    @Inject
    lateinit var permissionManager: RecPermissionManager

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainActivityViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupView()
        observeData()
    }

    @SuppressLint("SetTextI18n")
    private fun observeData() {
        lifecycleScope.launch {
            launch {
                viewModel.recorderUiState.collect { state ->
                    when (state) {
                        RecorderUiState.Idle -> {
                            binding.ivPlay.setImageResource(R.drawable.ic_play)
                            binding.ivStop.visibility = View.INVISIBLE
                            binding.tvTimer.visibility = View.INVISIBLE
                            binding.clOutput.visibility = View.VISIBLE
                        }

                        RecorderUiState.Recording -> {
                            binding.ivPlay.setImageResource(R.drawable.ic_pause)
                            binding.ivStop.visibility = View.VISIBLE
                            binding.tvTimer.visibility = View.VISIBLE
                            binding.clOutput.visibility = View.INVISIBLE
                        }

                        RecorderUiState.Paused -> {
                            binding.ivPlay.setImageResource(R.drawable.ic_play)
                            binding.ivStop.visibility = View.VISIBLE
                            binding.tvTimer.visibility = View.VISIBLE
                            binding.clOutput.visibility = View.INVISIBLE
                        }
                    }
                }
            }

            launch {
                viewModel.timerText.collect {
                    binding.tvTimer.text = it
                }
            }

            launch {
                viewModel.playbackUiState.collect {
                    if (it.error != null) Toast.makeText(
                        this@MainActivity,
                        it.error,
                        Toast.LENGTH_LONG
                    ).show()

                    binding.run {
                        if (!it.isReady) {
                            clOutput.visibility = View.INVISIBLE
                        } else clOutput.visibility = View.VISIBLE
                        tvCurrentTime.text = it.currentPosition
                        tvTotalTime.text = it.totalDuration
                        tvDeviceStatus.text = "Playing through: ${it.device}"
                        btnPlayPause.setImageResource(if (it.isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
                        binding.seekBar.progress =
                            calculateProgressPercent(it.currentPosition, it.totalDuration)
                    }
                }
            }

        }
    }

    private fun toggleRecPause() {
        viewModel.onPauseOrResumeRecording()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupView() {
        binding.seekBar.setOnTouchListener { _, _ -> true }
        binding.ivPlay.setOnClickListener {
            if (viewModel.getCurrentState() is RecorderUiState.Idle) {
                lifecycleScope.launch {
                    val permissionGranted = permissionManager
                        .checkOrRequestRecordingPermission(this@MainActivity)

                    if (!permissionGranted) {
                        Toast.makeText(this@MainActivity, "Denied", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.onStartRecording()
                    }
                }
            } else {
                toggleRecPause()
            }
        }

        binding.ivStop.setOnClickListener {
            viewModel.onStopRecording()
        }

        binding.btnPlayPause.setOnClickListener {
            viewModel.playPauseAudio()
        }
    }
}