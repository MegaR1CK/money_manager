package com.hfad.moneymanager.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.hfad.moneymanager.App
import com.hfad.moneymanager.R
import com.hfad.moneymanager.activities.EditCheckActivity
import com.hfad.moneymanager.activities.EditDebtActivity
import com.hfad.moneymanager.activities.CheckActivity
import com.hfad.moneymanager.adapters.ChecksAdapter
import com.hfad.moneymanager.adapters.DebtAdapter
import com.hfad.moneymanager.models.Debt
import kotlinx.android.synthetic.main.fragment_checks.*
import kotlinx.android.synthetic.main.fragment_checks.view.*

class ChecksFragment : Fragment() {


    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_checks, container, false)

        view.btn_add_check.setOnClickListener {
            startActivity(Intent(activity, EditCheckActivity::class.java))
        }

        view.btn_add_debt.setOnClickListener {
            startActivity(Intent(activity, EditDebtActivity::class.java))
        }

        return view
    }

    override fun onStart() {
        super.onStart()
        scroll_checks_frag.scrollTo(0,0)
        val setChecksRecycler = {
            if (App.userData?.checks?.isNotEmpty() == true) {
                recycler_checks.layoutManager = LinearLayoutManager(activity)
                val adapter = ChecksAdapter(
                    App.userData?.checks ?: listOf(),
                    App.userData?.transactions ?: listOf())
                adapter.onCheckClickListener = object : ChecksAdapter.CheckClickListener {
                    override fun onCheckClick(position: Int) {
                        startActivity(Intent(activity, CheckActivity::class.java)
                            .putExtra(CheckActivity.CHECK_POS, position))
                    }
                }
                recycler_checks.adapter = adapter
                swipe_refresh_checks.isRefreshing = false
            }
            else no_checks_title.visibility = View.VISIBLE
        }

        val setDebtsRecycler = {
            if (App.userData?.debts?.isNotEmpty() == true) {
                recycler_debts.layoutManager = LinearLayoutManager(activity)
                recycler_debts.adapter = App.userData?.debts?.let { DebtAdapter(it) }
                debts_sum.text = String.format(getString(R.string.debt_amount),
                    App.userData?.debts
                        ?.filter { it.type == Debt.DebtType.toMe }
                        ?.sumOf { it.amount },
                    App.userData?.debts
                        ?.filter { it.type == Debt.DebtType.fromMe }
                        ?.sumOf { it.amount })
            }
            else no_debts_title.visibility = View.VISIBLE
        }
        if (App.userData?.checks == null || App.userData?.debts == null ||
            App.userData?.transactions == null || App.userData?.categories == null)
                activity?.let { App.userData?.initUserData(it, setChecksRecycler, setDebtsRecycler) }
        else {
            setChecksRecycler.invoke()
            setDebtsRecycler.invoke()
        }

        activity?.let { ContextCompat.getColor(it, R.color.dark_blue) }
            ?.let { swipe_refresh_checks.setColorSchemeColors(it) }
        swipe_refresh_checks.setOnRefreshListener {
            activity?.let { App.userData?.initUserData(it, setChecksRecycler, setDebtsRecycler) }
        }
    }
}