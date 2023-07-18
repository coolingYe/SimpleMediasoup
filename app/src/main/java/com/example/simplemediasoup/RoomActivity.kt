package com.example.simplemediasoup

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simplemediasoup.adapter.RoomAdapter
import com.example.simplemediasoup.databinding.ActivityRoomBinding

class RoomActivity : AppCompatActivity(), RoomContract.View {

    private lateinit var binding: ActivityRoomBinding
    private lateinit var viewModel: RoomStore
    private val roomAdapter = RoomAdapter()

    private val mInteractor by lazy {
        RoomInteractor(viewModel)
    }

    private val mPresenter by lazy {
        RoomPresenter(this, mInteractor)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)
        viewModel = ViewModelProvider(this)[RoomStore::class.java]
        initListener()
        initObserve()
        initData()
    }


    private fun initData() {
        mPresenter.createRTC()
    }

    private fun initListener() {
        binding.ivMeetingSwitchCamera.setOnClickListener {
            //switch camera
        }

        binding.tvMeetingTitle.setOnClickListener {
            //show meeting info
        }

        binding.btnMeetingEnd.setOnClickListener {
            //end
        }
    }

    private fun initObserve() {
        viewModel.roomInfo.observe(this) {

        }

        viewModel.me.observe(this) {

        }

        viewModel.peers.observe(this) {
            if (it.getAllPeer().isEmpty()) return@observe
            roomAdapter.updateList(it.getAllPeer())
        }
    }

    override fun getContext(): Context = this@RoomActivity

    override fun initViews() {
        with(binding.rvMeetingVideo) {
            layoutManager = LinearLayoutManager(this@RoomActivity, RecyclerView.VERTICAL, false)
        }
        binding.rvMeetingVideo.adapter = roomAdapter
    }
}