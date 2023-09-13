package com.example.simplemediasoup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.simplemediasoup.adapter.ParticipantAdapter
import com.example.simplemediasoup.databinding.FragmentParticipantBinding

class ParticipantFragment : Fragment() {

    private lateinit var binding: FragmentParticipantBinding
    private lateinit var participantAdapter: ParticipantAdapter
    private lateinit var mRoomStore: RoomStore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentParticipantBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRoomStore = ViewModelProvider(requireActivity())[RoomStore::class.java]

        val viewHeader =  view.findViewById<View>(R.id.view_header)
        val tvHeaderTitle = viewHeader.findViewById<TextView>(R.id.tv_header_title)
        tvHeaderTitle.text = getString(R.string.participants)
        val tvHeaderLeft = viewHeader.findViewById<TextView>(R.id.tv_header_left)
        tvHeaderLeft.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        binding.rvParticipant.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        participantAdapter = ParticipantAdapter()
        binding.rvParticipant.adapter = participantAdapter

        mRoomStore.getPeers()?.let {
            participantAdapter.updateList(it)
        }

        initObserver()
    }

    private fun initObserver() {
        mRoomStore.consumers.observe(viewLifecycleOwner) {
            if (it == null) return@observe
            participantAdapter.setConsumers(it)
        }

        mRoomStore.peers.observe(viewLifecycleOwner) {
            if (it.getAllPeer().isEmpty()) return@observe
            participantAdapter.updateList(it.getAllPeer())
        }
    }
}