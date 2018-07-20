package com.zhengdianfang.miracledemo


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.zhengdianfang.miracleframework.BaseFragment
import com.zhengdianfang.miracleframework.adapter.base.BaseQuickAdapter
import com.zhengdianfang.miracleframework.adapter.base.BaseViewHolder
import kotlinx.android.synthetic.main.fragment_list.*

/**
 * A simple [Fragment] subclass.
 *
 */
class ListFragment : BaseFragment() {
    companion object {
        fun newInstance() = ListFragment()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val list = arrayListOf<String>()
        for(index in 1..50) {
           list.add(index.toString())
        }
        val listAdapter = ListAdapter(list)
        listAdapter.setEnableLoadMore(true)
        listAdapter.isUpFetchEnable = true
        listAdapter.setUpFetchListener {
            Toast.makeText(context, "refresh", Toast.LENGTH_SHORT).show();
        }
        listAdapter.setRequestLoadMoreListener({
            Toast.makeText(context, "load more", Toast.LENGTH_SHORT).show();
        }, recyclerView)
        recyclerView.adapter = listAdapter

    }

    internal class ListAdapter(data: MutableList<String>?) : BaseQuickAdapter<String, BaseViewHolder>(R.layout.fragment_list_item, data) {
        override fun convert(helper: BaseViewHolder?, item: String?) {
           if (helper?.itemView!! is TextView) {
               (helper?.itemView as TextView).text = item ?: ""
           }
        }

    }
}
