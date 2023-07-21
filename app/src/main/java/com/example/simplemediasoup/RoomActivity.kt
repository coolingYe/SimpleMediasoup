package com.example.simplemediasoup

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowInsets
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simplemediasoup.adapter.RoomAdapter
import com.example.simplemediasoup.databinding.ActivityRoomBinding
import com.example.simplemediasoup.utils.Log
import pub.devrel.easypermissions.EasyPermissions

class RoomActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks, RoomContract.View {

    private lateinit var binding: ActivityRoomBinding
    private lateinit var roomStore: RoomStore
    private val roomAdapter = RoomAdapter()
    var handler: Handler = Handler(Looper.getMainLooper())

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
        checkPermissions()
    }

    private fun initListener() {
        binding.ivMeetingSwitchCamera.setOnClickListener {
            mPresenter.switchCamera()
        }

        binding.tvMeetingTitle.setOnClickListener {
            //show meeting info
        }

        binding.btnMeetingEnd.setOnClickListener {
            finish()
        }
    }

    private fun initObserve() {
        roomStore.roomInfo.observe(this) {

        }

        roomStore.me.observe(this) {

        }

        roomStore.peers.observe(this) {
            if (it.getAllPeer().isEmpty()) return@observe
            roomAdapter.updateList(it.getAllPeer())
        }

        roomStore.localVideoTrack.observe(this) {
            if (it == null) return@observe
            roomAdapter.setLocalVideoTrack(it)
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
            mPresenter.createRTC()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Log.d(ContentValues.TAG, "Permission request successful")
        mPresenter.createRTC()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        Log.d(ContentValues.TAG, "Permission request failed")
    }

    override fun getContext(): Context = this@RoomActivity

    override fun initViews() {
        with(binding.rvMeetingVideo) {
            layoutManager = LinearLayoutManager(this@RoomActivity, RecyclerView.VERTICAL, false)
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
        window.decorView.windowInsetsController?.hide(WindowInsets.Type.statusBars())

    }

    override fun onDestroy() {
        super.onDestroy()
        mPresenter.close()
    }
}