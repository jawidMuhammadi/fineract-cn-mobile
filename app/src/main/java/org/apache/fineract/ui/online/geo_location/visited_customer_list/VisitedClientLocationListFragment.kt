package org.apache.fineract.ui.online.geo_location.visited_customer_list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import org.apache.fineract.R
import org.apache.fineract.ui.base.FineractBaseActivity
import org.apache.fineract.ui.base.FineractBaseFragment
import javax.inject.Inject

class VisitedClientLocationListFragment : FineractBaseFragment() {

    private lateinit var rootView: View
    private lateinit var viewModel: VisitedClientLocationViewModel

    @Inject
    lateinit var factory: VisitedClientLocationViewModelFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        rootView = inflater.inflate(R.layout.fragment_visted_customers_list, container, false)
        (activity as FineractBaseActivity).activityComponent.inject(this)
        viewModel = ViewModelProviders.of(this, factory).get(VisitedClientLocationViewModel::class.java)

        return rootView
    }

    companion object {
        @JvmStatic
        fun newInstance() =
                VisitedClientLocationListFragment().apply {
                    arguments = Bundle().apply {
                    }
                }
    }
}