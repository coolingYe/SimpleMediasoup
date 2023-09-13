package com.example.simplemediasoup

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.WindowInsets
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.simplemediasoup.adapter.CenterGridLayoutManager
import com.example.simplemediasoup.adapter.RoomAdapter
import com.example.simplemediasoup.databinding.ActivityRoomBinding
import com.example.simplemediasoup.utils.Log
import com.example.simplemediasoup.utils.RoomInfoDialog
import com.example.simplemediasoup.utils.Utils
import pub.devrel.easypermissions.EasyPermissions

class RoomActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, RoomContract.View {

    private lateinit var binding: ActivityRoomBinding
    private lateinit var roomStore: RoomStore
    private val roomAdapter = RoomAdapter()

    private var displayName: String ?= null
    private var cameraEnable: Boolean = true

    private val mInteractor by lazy {
        RoomInteractor(roomStore)
    }

    private val mPresenter by lazy {
        RoomPresenter(this, mInteractor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        roomStore = ViewModelProvider(this)[RoomStore::class.java]
        initListener()
        initObserve()
        initData()
    }


    private fun initData() {
        displayName = intent.getStringExtra("displayName") ?: Utils.getRandomString(8)
        cameraEnable = intent.getBooleanExtra("cameraEnable", true)
        checkPermissions()
    }

    private fun initListener() {

        binding.btnMeetingTest.setOnClickListener {

        }

        binding.ivMeetingSwitchCamera.setOnClickListener {
            mPresenter.switchCamera()
        }

        binding.tvMeetingTitle.setOnClickListener {
            val roomInfoDialog = RoomInfoDialog(this)
            roomInfoDialog.show()
        }

        binding.btnMeetingEnd.setOnClickListener {
            finish()
        }

        binding.tvMeetingAudio.setOnClickListener {
            val audioPro = roomStore.producers.value?.filter("audio")?.mProducer
            audioPro?.let {
                mPresenter.setTextImageTopDrawable(R.drawable.ic_mic_off, binding.tvMeetingAudio)
                binding.tvMeetingAudio.text = getString(R.string.talk)
                mPresenter.disableMicrophone()

            } ?: run {
                mPresenter.setTextImageTopDrawable(R.drawable.ic_mic_on, binding.tvMeetingAudio)
                binding.tvMeetingAudio.text = getString(R.string.mute)
                mPresenter.enableMicrophone()
            }
        }

        binding.tvMeetingVideo.setOnClickListener {
            val videoPro = roomStore.producers.value?.filter("video")?.mProducer
            videoPro?.let {
                mPresenter.setTextImageTopDrawable(R.drawable.ic_webcam_off, binding.tvMeetingVideo)
                binding.tvMeetingVideo.text = getString(R.string.open_camera)
                mPresenter.disableCamera()
            } ?: run {
                mPresenter.setTextImageTopDrawable(
                    R.drawable.ic_webcam_on,
                    binding.tvMeetingVideo
                )
                binding.tvMeetingVideo.text = getString(R.string.stop_camera)
                mPresenter.enableCamera()
            }
        }

        binding.tvChat.setOnClickListener {
            supportFragmentManager.beginTransaction().replace(R.id.fl_fragment, ChatFragment())
                .addToBackStack(null).commit()
        }

        binding.clMeetingParticipants.setOnClickListener {
            //roomStore.peers
            supportFragmentManager.beginTransaction().replace(R.id.fl_fragment, ParticipantFragment()).addToBackStack(null).commit()
        }
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initObserve() {
        roomStore.roomInfo.observe(this) {

        }

        roomStore.self.observe(this) {

        }

        roomStore.peers.observe(this) {
            if (it.getAllPeer().isEmpty()) return@observe
            roomAdapter.updateList(it.getAllPeer())
            binding.tvMeetingParticipantsValue.text = it.getAllPeer().size.toString()
        }

        roomStore.localVideoTrack.observe(this) {
            if (it == null) {
                roomAdapter.notifyDataSetChanged()
                return@observe
            }
            roomAdapter.setLocalVideoTrack(it)
        }

        roomStore.producers.observe(this) { producers ->
            if (producers == null) return@observe
//            val microphonePro =  producers.filter("audio")?.mProducer
//            val cameraPro =  producers.filter("video")?.mProducer
//            microphonePro?.let { producer ->
//                when {
//                    producer.isPaused -> {
//                        binding.tvMeetingAudio.isEnabled = false
//                        mPresenter.setTextImageTopDrawable(R.drawable.ic_mic_off, binding.tvMeetingAudio)
//                    }
//                    producer.isClosed -> {
//                        binding.tvMeetingAudio.isEnabled = producer.isClosed
//                    }
//                }
//            }
//
//            cameraPro?.let { producer ->
//                when {
//                    producer.isPaused -> {
//                        binding.tvMeetingVideo.isEnabled = false
//                        mPresenter.setTextImageTopDrawable(R.drawable.ic_mic_off, binding.tvMeetingAudio)
//                    }
//                    producer.isClosed -> {
//                        binding.tvMeetingVideo.isEnabled = producer.isClosed
//                    }
//                }
//            }
        }

        roomStore.consumers.observe(this) {
            if (it == null) return@observe
            roomAdapter.setConsumers(it)
        }

        roomStore.notify.observe(this) {
            if (it == null) return@observe
            Toast.makeText(this@RoomActivity, it.title + it.text, Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA,
        )
        if (!EasyPermissions.hasPermissions(this, *permissions)) {
            // Permission is not granted
            Log.d("checkCameraPermissions", "No Camera Permissions")
            EasyPermissions.requestPermissions(this, "Please provide permissions", 1, *permissions)
        } else {
            mPresenter.createRTC(displayName = displayName!!, cameraEnable = cameraEnable)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(ContentValues.TAG, "Permission request successful")
        mPresenter.createRTC(displayName = displayName!!, cameraEnable = cameraEnable)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(ContentValues.TAG, "Permission request failed")
    }

    override fun getContext(): Context = this@RoomActivity

    override fun initViews() {
        with(binding.rvMeetingVideo) {
            layoutManager = CenterGridLayoutManager(this@RoomActivity, 3)
        }
        roomAdapter.setHasStableIds(true)
        binding.rvMeetingVideo.adapter = roomAdapter
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) hideSystemUI()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun hideSystemUI() {
        window.decorView.windowInsetsController?.hide(WindowInsets.Type.navigationBars())
    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.close()
    }
}