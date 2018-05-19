package com.koalatea.thehollidayinn.softwareengineeringdaily.subscription

import android.content.Intent
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import com.koalatea.thehollidayinn.softwareengineeringdaily.MainActivity
import com.koalatea.thehollidayinn.softwareengineeringdaily.R
import com.koalatea.thehollidayinn.softwareengineeringdaily.app.SEDApp
import com.koalatea.thehollidayinn.softwareengineeringdaily.repositories.UserRepository

import kotlinx.android.synthetic.main.activity_subscription.*

class SubscriptionActivity : AppCompatActivity(),
        PaymentFragment.OnFragmentInteractionListener,
        PlanFragment.OnFragmentInteractionListener,
        PlanInfoFragment.OnFragmentInteractionListener {

    private lateinit var userRepository: UserRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscription)
        setSupportActionBar(toolbar)


        userRepository = SEDApp.component.userRepository()
        if (userRepository.hasPremium) {
            showInfoFragment()
            return
        }

        showPlanOptions()
    }

    private fun showInfoFragment () {
        // @TODO: save instance?
        val planFragment = PlanInfoFragment.newInstance()
        this.supportFragmentManager
                .beginTransaction()
                .replace(R.id.fragment_container, planFragment)
                .commit()
    }

    private fun showFragement(fragment: Fragment) {
        this.supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    private fun showPlanOptions () {
        // @TODO: save instance?
        showFragement(PlanFragment.newInstance())
    }

    private fun showPayment (type: String) {
        // @TODO: save instance?
        showFragement(PaymentFragment.newInstance(type))
    }

    override fun paymentSuccess() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onPlanSelected(type: String) {
        showPayment(type);
    }

    override fun canceledSubscription() {
        userRepository.hasPremium = false
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}
