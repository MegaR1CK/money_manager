package com.hfad.moneymanager.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.adapters.ChecksAdapter
import com.hfad.moneymanager.adapters.DebtAdapter
import com.hfad.moneymanager.models.DebtModel
import kotlinx.android.synthetic.main.fragment_checks.*
import kotlinx.android.synthetic.main.fragment_checks.view.*

class ChecksFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_checks, container, false)

        val setChecksRecycler = {
            view.recycler_checks.layoutManager = LinearLayoutManager(activity)
            view.recycler_checks.adapter = App.userData?.checks?.let { ChecksAdapter(it) }
        }

        val setDebtsRecycler = {
            view.recycler_debts.layoutManager = LinearLayoutManager(activity)
            view.recycler_debts.adapter = App.userData?.debts?.let { DebtAdapter(it) }
            debts_sum.text = String.format(getString(R.string.debt_amount,
                    App.userData?.debts?.filter { it.type == DebtModel.DebtType.toMe }?.sumOf { it.amount },
                    App.userData?.debts?.filter { it.type == DebtModel.DebtType.fromMe }?.sumOf { it.amount }))
        }

        if (App.userData?.checks == null)
            App.userData?.getChecks(setChecksRecycler)
        else setChecksRecycler.invoke()

        if (App.userData?.debts == null)
            App.userData?.getDebts(setDebtsRecycler)
        else setDebtsRecycler.invoke()

        return view
    }
}