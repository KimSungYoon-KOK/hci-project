package com.android.hciproject.testAmplify

import android.R
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class MyAdapter {
    private var mData: List<ListPetsQuery.Item> = ArrayList()
    private var mInflater: LayoutInflater? = null


    // data is passed into the constructor
    fun MyAdapter(context: Context?) {
        mInflater = LayoutInflater.from(context)
    }

    // inflates the row layout from xml when needed
    fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder? {
        val view: View = mInflater.inflate(R.layout.recyclerview_row, parent, false)
        return ViewHolder(view)
    }

    // binds the data to the TextView in each row
    fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(mData[position])
    }

    // total number of rows
    fun getItemCount(): Int {
        return mData.size
    }

    // resets the list with a new set of data
    fun setItems(items: List<ListPetsQuery.Item>) {
        mData = items
    }

    // stores and recycles views as they are scrolled off screen
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var txt_name: TextView
        var txt_description: TextView
        fun bindData(item: ListPetsQuery.Item) {
            txt_name.setText(item.name())
            txt_description.setText(item.description())
        }

        init {
            txt_name = itemView.findViewById(R.id.txt_name)
            txt_description = itemView.findViewById(R.id.txt_description)
        }
    }
}