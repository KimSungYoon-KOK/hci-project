package com.android.hciproject.testAmplify

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.amazonaws.amplify.generated.graphql.ListPetsQuery
import com.android.hciproject.R


class MyAdapter()
    :RecyclerView.Adapter<MyAdapter.ViewHolder>() {

    lateinit var context: Context
    private var mData: List<ListPetsQuery.Item> = ArrayList()


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.recyclerview_row, parent, false)
        return ViewHolder(view)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var txt_name: TextView = itemView.findViewById(R.id.txt_name)
        private var txt_description: TextView = itemView.findViewById(R.id.txt_description)
        fun bindData(item: ListPetsQuery.Item) {
            txt_name.text = item.name()
            txt_description.text = item.description()
        }
    }

    // binds the data to the TextView in each row
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(mData[position])
    }

    override fun getItemCount(): Int {
        return mData.size
    }

    fun setItems(items: List<ListPetsQuery.Item>) {
        mData = items
    }





}