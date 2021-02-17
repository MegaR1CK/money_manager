package com.hfad.moneymanager.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.activities.AddCheckActivity
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

        view.btn_add_check.setOnClickListener {
            startActivity(Intent(activity, AddCheckActivity::class.java))
        }

        return view
    }

    override fun onStart() {
        super.onStart()

        val setChecksRecycler = {
            if (App.userData?.checks?.isNotEmpty() == true) {
                recycler_checks.layoutManager = LinearLayoutManager(activity)
                recycler_checks.adapter = App.userData?.checks?.let { ChecksAdapter(it) }
            }
            else no_checks_title.visibility = View.VISIBLE
        }

        val setDebtsRecycler = {
            if (App.userData?.debts?.isNotEmpty() == true) {
                recycler_debts.layoutManager = LinearLayoutManager(activity)
                recycler_debts.adapter = App.userData?.debts?.let { DebtAdapter(it) }
                debts_sum.text = String.format(getString(R.string.debt_amount),
                    App.userData?.debts
                        ?.filter { it.type == DebtModel.DebtType.toMe }
                        ?.sumOf { it.amount },
                    App.userData?.debts
                        ?.filter { it.type == DebtModel.DebtType.fromMe }
                        ?.sumOf { it.amount })
            }
            else no_debts_title.visibility = View.VISIBLE
        }

        if (App.userData?.checks == null)
            activity?.let { App.userData?.getChecks(it, setChecksRecycler) }
        else setChecksRecycler.invoke()

        if (App.userData?.debts == null)
            activity?.let { App.userData?.getDebts(it, setDebtsRecycler) }
        else setDebtsRecycler.invoke()
    }
}