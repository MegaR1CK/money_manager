package com.hfad.moneymanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.adapters.TransactionsAdapter
import kotlinx.android.synthetic.main.fragment_transactions.view.*

class TransactionsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_transactions, container, false)

        val setTransactionsRecycler = {
            view.recycler_transactions?.layoutManager = LinearLayoutManager(activity)
            view.recycler_transactions?.adapter =
                App.userData?.transactions?.let{ TransactionsAdapter(it) }
            view.swipe_refresh_transactions.isRefreshing = false
        }

        setTransactionsRecycler.invoke()

        activity?.let { ContextCompat.getColor(it, R.color.dark_blue) }
            ?.let { view.swipe_refresh_transactions.setColorSchemeColors(it) }
        view.swipe_refresh_transactions.setOnRefreshListener {
            activity?.let { App.userData?.initUserData(it, setTransactionsRecycler) }
        }

        return view
    }
}