package com.longkd.simplemediarecord

import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.longkd.simplemediarecord.databinding.ActivityMainBinding
import com.longkd.simplemediarecord.util.RecPermissionManager
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

    private fun observeData() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    UiState.Idle -> {
                        binding.ivPlay.setImageResource(R.drawable.ic_play)
                        binding.ivStop.visibility = View.INVISIBLE
                        binding.tvTimer.visibility = View.INVISIBLE
                        binding.clOutput.visibility = View.VISIBLE
                    }

                    UiState.Recording -> {
                        binding.ivPlay.setImageResource(R.drawable.ic_pause)
                        binding.ivStop.visibility = View.VISIBLE
                        binding.tvTimer.visibility = View.VISIBLE
                        binding.clOutput.visibility = View.INVISIBLE
                    }

                    UiState.Paused -> {
                        binding.ivPlay.setImageResource(R.drawable.ic_play)
                        binding.ivStop.visibility = View.VISIBLE
                        binding.tvTimer.visibility = View.VISIBLE
                        binding.clOutput.visibility = View.INVISIBLE
                    }
                }
            }

            viewModel.timerText.collect {
                binding.tvTimer.text = it
            }
        }
    }


    private fun toggleRecPause() {
        viewModel.onPauseOrResumeRecording()
    }

    private fun setupView() {
        binding.ivPlay.setOnClickListener {
            if (viewModel.getCurrentState() is UiState.Idle) {
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

    }
}