package com.koalatea.thehollidayinn.softwareengineeringdaily.subscription

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

import com.koalatea.thehollidayinn.softwareengineeringdaily.R
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.APIInterface
import com.koalatea.thehollidayinn.softwareengineeringdaily.data.remote.ApiUtils

class PaymentFragment : Fragment() {

    private var mListener: OnFragmentInteractionListener? = null
    private var type: String = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val rootView = inflater.inflate(R.layout.fragment_payment, container, false)

        val button: Button = rootView.findViewById(R.id.paymentButton)
        button.setOnClickListener({ pay() })

        return rootView
    }

    private fun pay() {
        val service: APIInterface = ApiUtils.getKibbleService(activity)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new DisposableObserver<List<Post>>() {
//                    @Override
//                    public void onComplete() {
//                        skeletonScreen.hide();
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        Log.v(TAG, e.toString());
//                    }
//
//                    @Override
//                    public void onNext(List<Post> posts) {
//                        podcastListViewModel.setPostList(posts);
//                        swipeRefreshLayout.setRefreshing(false);
//                    }
//                });
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            mListener = context
        } else {
            throw RuntimeException(context!!.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        mListener = null
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction()
    }

    companion object {
        // TODO: Rename and change types and number of parameters
        fun newInstance(type: String): PaymentFragment {
            val fragment = PaymentFragment()
            fragment.type = type;
            return fragment
        }
    }
}// Required empty public constructor
