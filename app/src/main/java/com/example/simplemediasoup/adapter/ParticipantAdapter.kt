package com.example.simplemediasoup.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.simplemediasoup.R
import com.example.simplemediasoup.model.Consumers
import com.example.simplemediasoup.model.Peer

class ParticipantAdapter(
    private var mDataList: List<Peer> = emptyList()
) : RecyclerView.Adapter<ParticipantAdapter.ParticipantViewHolder>() {

    private var mConsumers: Consumers? = null

    @SuppressLint("NotifyDataSetChanged")
    fun setConsumers(mConsumers: Consumers) {
        this.mConsumers = mConsumers
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(mDataList: List<Peer>) {
        this.mDataList = mDataList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_participant_layout, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mDataList.size
    }

    override fun getItemId(position: Int): Long {
        return mDataList[position].hashCode().toLong()
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        holder.tvParticipantName.text = mDataList[position].displayName
        if (position == 0) {
            holder.tvParticipantType.text = holder.tvParticipantType.text.toString().replace("*","我")
        } else {
            holder.tvParticipantType.isVisible = false
            mConsumers?.let { consumers ->
                mDataList[position].consumers?.forEach { consumerId ->
                    val targetConsumer = consumers.getConsumer(consumerId)?.mConsumer
                    if (targetConsumer?.kind?.contains("video") == true) {
                        holder.ivParticipantCamera.isVisible = true
                    }

                    if (targetConsumer?.kind?.contains("audio") == true) {
                        holder.ivParticipantMicrophone.isVisible = true
                    }
                }
            }
        }
    }

    inner class ParticipantViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvParticipantName: TextView = itemView.findViewById(R.id.tv_participant_name)
        val tvParticipantType: TextView = itemView.findViewById(R.id.tv_participant_type)
        val ivParticipantCamera: ImageView = itemView.findViewById(R.id.iv_participant_camera)
        val ivParticipantMicrophone: ImageView = itemView.findViewById(R.id.iv_participant_microphone)
    }

}