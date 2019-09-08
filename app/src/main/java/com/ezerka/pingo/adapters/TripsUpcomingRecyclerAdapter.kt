package com.ezerka.pingo.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ezerka.pingo.R
import com.ezerka.pingo.models.AddressData


class TripsUpcomingRecyclerAdapter
    (val context: Context, private val mAddressList: ArrayList<AddressData>) :
    RecyclerView.Adapter<TripsUpcomingRecyclerAdapter.ViewHolder>() {

    private var layoutInflater: LayoutInflater = LayoutInflater.from(context)


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = layoutInflater.inflate(R.layout.item_trips_history_list, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return mAddressList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(mAddressList[position])
    }

    class ViewHolder(itemView: View?) : RecyclerView.ViewHolder(itemView!!) {

        fun bindItems(addresses: AddressData) {
            val mTripsPickupAddress =
                itemView.findViewById<TextView?>(R.id.id_Text_Trips_Pickup_Address)
            val mTripsDestAddress =
                itemView.findViewById<TextView?>(R.id.id_Text_Trips_Dest_Address)

            mTripsPickupAddress!!.text = addresses.pickupAddress
            mTripsDestAddress!!.text = addresses.destAddress

        }

    }
}