package com.koalatea.thehollidayinn.softwareengineeringdaily.subscription

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.koalatea.thehollidayinn.softwareengineeringdaily.R
import android.content.DialogInterface
import android.widget.Toast
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface
import io.reactivex.CompletableObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_plan_info.*

class PlanInfoFragment : Fragment() {
    private var mListener: OnFragmentInteractionListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView: View = inflater.inflate(R.layout.fragment_plan_info, container, false)

        val cancelButton: Button = rootView.findViewById(R.id.cancelButton)
        cancelButton.setOnClickListener({ confirmCancel() })

        return rootView
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context?.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    private fun confirmCancel () {
        cancelButton.isEnabled = false

        AlertDialog.Builder(this.activity)
            .setTitle("Cancel Subscription")
            .setMessage("Do you really want to cancel?")
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes, DialogInterface.OnClickListener {
                dialog, _ ->
                run {
                    cancelSubscription()
                }
            })
            .setNegativeButton(android.R.string.no, null).show()
    }

    private fun cancelSubscription () {
        val service: APIInterface = SEDApp.component.kibblService()
        service.cancelSubscription()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object: CompletableObserver {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onComplete() {
                        Toast.makeText(SEDApp.component.context(),
                                "Success!",
                                Toast.LENGTH_LONG
                        ).show()
                        cancelButton.isEnabled = true
                        mListener?.canceledSubscription()
                    }

                    override fun onError(e: Throwable ) {
                        Toast.makeText(SEDApp.component.context(),
                                e.message,
                                Toast.LENGTH_LONG
                        ).show()
                        cancelButton.isEnabled = true
                    }
                })
    }

    interface OnFragmentInteractionListener {
        fun canceledSubscription()
    }

    companion object {
        fun newInstance(): PlanInfoFragment {
            val fragment = PlanInfoFragment()
            return fragment
        }
    }
}// Required empty public constructor
