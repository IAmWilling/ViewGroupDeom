package com.zhy.viewgroupdeom

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class MyAdapter(val strings: List<String>,val context: Context) : BaseAdapter() {

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        return LayoutInflater.from(context).inflate(R.layout.item, p2, false).apply {
            findViewById<TextView>(R.id.textview).text = strings[p0]
        }
    }



    override fun getItem(p0: Int): Any {
        return strings[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0 as Long;
    }


    override fun getCount(): Int {
        return strings.size
    }
}