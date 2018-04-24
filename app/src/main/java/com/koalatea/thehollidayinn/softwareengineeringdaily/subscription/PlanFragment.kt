package com.koalatea.thehollidayinn.softwareengineeringdaily.subscription

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.koalatea.thehollidayinn.softwareengineeringdaily.R

class PlanFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_plan, container, false)

        val monthlyPlan: Button = rootView.findViewById(R.id.monthlyButton)
        monthlyPlan.setOnClickListener({ selectMonthlyPlan() })

        val yearlyPlan: Button = rootView.findViewById(R.id.yearlyButton)
        yearlyPlan.setOnClickListener({ selectYearlyPlan() })

        return rootView
    }

    private fun selectMonthlyPlan () {
        mListener?.onPlanSelected("monthly");
    }

    private fun selectYearlyPlan () {
        mListener?.onPlanSelected("yearly");
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement PlanFragment.OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        fun onPlanSelected(type: String)
    }

    companion object {
        fun newInstance(): PlanFragment {
            val fragment = PlanFragment()
            return fragment
        }
    }
}// Required empty public constructor
