package com.hfad.moneymanager.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.hfad.moneymanager.R
import com.hfad.moneymanager.adapters.ChecksAdapter
import kotlinx.android.synthetic.main.fragment_checks.view.*

class ChecksFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_checks, container, false)

        view.recycler_checks.layoutManager = LinearLayoutManager(activity)
        //view.recycler_checks.adapter = ChecksAdapter()

        return view
    }
}