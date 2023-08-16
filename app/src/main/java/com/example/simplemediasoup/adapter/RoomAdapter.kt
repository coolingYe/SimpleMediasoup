package com.example.simplemediasoup.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.simplemediasoup.R
import com.example.simplemediasoup.model.Consumers
import com.example.simplemediasoup.model.Peer
import com.example.simplemediasoup.rtc.PeerConnectionUtils
import org.webrtc.VideoTrack

class RoomAdapter(private var peerData: List<Peer> = emptyList()) :
    RecyclerView.Adapter<RoomAdapter.ContactHolder>() {

    private var mLocalVideoTrack: VideoTrack? = null
    private var mConsumers: Consumers? = null
    private lateinit var mRecyclerView: RecyclerView

    class ContactHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {
        val displayName: TextView = itemView?.findViewById(R.id.tv_consume_name)!!
        val player: org.webrtc.SurfaceViewRenderer = itemView?.findViewById(R.id.player)!!
        val frame: View = itemView?.findViewById(R.id.talking)!!
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.mRecyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_player_layout, parent, false)
        view.layoutParams.height = mRecyclerView.width / 3
        return ContactHolder(view)
    }

    override fun getItemCount() = peerData.size

    override fun onBindViewHolder(holder: ContactHolder, position: Int) {
        holder.displayName.text = peerData[position].displayName
        if (position == 0) {
            mLocalVideoTrack?.let {
                if (it.enabled()) {
                    if (holder.player.isVisible.not()) {
                        holder.player.isVisible = true
                        holder.player.init(PeerConnectionUtils.getEglContext(), null)
                    }
                    it.addSink(holder.player)
                } else holder.player.isVisible = false
            }
        } else {
            mConsumers?.let { consumer ->
                peerData[position].consumers?.forEach { consumerId ->
                    val targetConsumer = consumer.getConsumer(consumerId)?.mConsumer
                    if (targetConsumer?.kind?.contains("video") == true) {
                        if (holder.player.isVisible.not()) {
                            holder.player.isVisible = true
                            holder.player.init(PeerConnectionUtils.getEglContext(), null)
                        }
                        (targetConsumer.track as VideoTrack).addSink(holder.player)
                    }
                }
            }
        }
    }

    override fun getItemId(position: Int): Long {
        return peerData[position].hashCode().toLong()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(data: List<Peer>) {
        this.peerData = data
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setLocalVideoTrack(videoTrack: VideoTrack) {
        this.mLocalVideoTrack = videoTrack
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setConsumers(consumers: Consumers) {
        this.mConsumers = consumers
        notifyDataSetChanged()
    }
}