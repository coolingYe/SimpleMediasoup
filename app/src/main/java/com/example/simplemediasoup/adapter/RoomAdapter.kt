package com.example.simplemediasoup.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simplemediasoup.R
import com.example.simplemediasoup.model.Peer

class RoomAdapter(private var peerData:List<Peer> = emptyList()) : RecyclerView.Adapter<RoomAdapter.ContactHolder>() {

    class ContactHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val displayName: TextView = itemView?.findViewById(R.id.tv_consume_name)!!
        val player: org.webrtc.SurfaceViewRenderer = itemView?.findViewById(R.id.player)!!
        val frame: View = itemView?.findViewById(R.id.talking)!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        return ContactHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_player_layout, parent, false))
    }

    override fun getItemCount() = peerData.size

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.displayName.text = peerData[position].displayName
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(data: List<Peer>) {
        this.peerData = data
        notifyDataSetChanged()
    }
}