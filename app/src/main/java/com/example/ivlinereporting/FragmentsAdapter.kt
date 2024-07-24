package com.example.ivlinereporting

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentsAdapter(private val fragments: ArrayList<Fragment>, activity: FragmentActivity):FragmentStateAdapter(activity) {
    override fun getItemCount():Int{
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }
}