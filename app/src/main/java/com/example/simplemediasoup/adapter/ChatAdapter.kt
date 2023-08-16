package com.example.simplemediasoup.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simplemediasoup.R
import com.example.simplemediasoup.model.Chat

class ChatAdapter(private var chatData: ArrayList<Chat> = ArrayList()) :
    RecyclerView.Adapter<ChatAdapter.ContactHolder>() {

    private val TYPE_LEFT = 0
    private val TYPE_RIGHT = 1

    private lateinit var mRecyclerView: RecyclerView

    class ContactHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val name: TextView = itemView?.findViewById(R.id.tv_chat_name)!!
        val content: TextView = itemView?.findViewById(R.id.tv_chat_content)!!
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.mRecyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        return if (viewType == TYPE_LEFT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_char_left_layout, parent, false)
            ContactHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_char_right_layout, parent, false)
            ContactHolder(view)
        }
    }

    override fun getItemCount() = chatData.size

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.name.text = chatData[position].name
        holder.content.text = chatData[position].content
    }

    override fun getItemViewType(position: Int): Int {
        return chatData[position].type
    }

    override fun getItemId(position: Int): Long {
        return chatData[position].hashCode().toLong()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(chatData: ArrayList<Chat>) {
        this.chatData = chatData
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun addChatMessage(chat: Chat) {
        chatData.add(chat)
        notifyDataSetChanged()
    }
}