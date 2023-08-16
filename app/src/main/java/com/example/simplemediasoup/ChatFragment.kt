package com.example.simplemediasoup

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.simplemediasoup.adapter.ChatAdapter
import com.example.simplemediasoup.databinding.FragmentChatBinding
import com.example.simplemediasoup.model.Chat

class ChatFragment : Fragment() {

    private lateinit var binding: FragmentChatBinding
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var roomStore: RoomStore

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roomStore = ViewModelProvider(requireActivity())[RoomStore::class.java]
        initViews()
        initListener()
    }

    private fun initViews() {
        chatAdapter = ChatAdapter(roomStore.chatInfo.value!!)
        with(binding.rvChat) {
            layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
            adapter = chatAdapter
        }
    }

    private fun initListener() {
        roomStore.chatInfo.observe(requireActivity()) {
            if (it.size == 0) return@observe
            chatAdapter.updateList(it)
            binding.rvChat.smoothScrollToPosition(chatAdapter.itemCount - 1)
        }

        binding.headerView.tvHeaderLeft.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.ivChatSend.setOnClickListener {
            val message = binding.edChat.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(requireContext(), "content is empty", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            roomStore.self.value?.let {
                val sendData = Chat(it.mId, it.mDisplayName, message, 1)
                val chatList = roomStore.chatInfo.value?.apply {
                    this.add(sendData)
                }
                roomStore.chatInfo.postValue(chatList)
            }
            roomStore.roomClient?.sendChatMessage(message)
            binding.edChat.setText("")
        }
    }
}