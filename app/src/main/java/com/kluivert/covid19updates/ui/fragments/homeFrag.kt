package com.kluivert.covid19updates.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.kluivert.covid19updates.CountriesArrayAdapter
import com.kluivert.covid19updates.R
import com.kluivert.covid19updates.Resource
import com.kluivert.covid19updates.formatNumber
import com.kluivert.covid19updates.model.CasesData
import com.kluivert.covid19updates.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_home.confirmedCases
import kotlinx.android.synthetic.main.fragment_home.dataLayout
import kotlinx.android.synthetic.main.fragment_home.deaths
import kotlinx.android.synthetic.main.fragment_home.errorLayout
import kotlinx.android.synthetic.main.fragment_home.lastUpdated
import kotlinx.android.synthetic.main.fragment_home.progressBar
import kotlinx.android.synthetic.main.fragment_home.recoveredCases
import kotlinx.android.synthetic.main.fragment_home.retry
import kotlinx.android.synthetic.main.fragment_home.spinner
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*
import kotlin.collections.ArrayList


class homeFrag : Fragment(), AdapterView.OnItemSelectedListener  {

    private val viewModel: MainViewModel by activityViewModels()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_home, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        dataLayout.visibility = View.GONE
        errorLayout.visibility = View.GONE
        spinner.visibility = View.INVISIBLE
        progressBar.visibility = View.VISIBLE
        relativeLayout.visibility = View.GONE

        spinner.onItemSelectedListener = this

        viewModel.getCountriesData().observe(viewLifecycleOwner, Observer {
            when(it) {
                is Resource.Loading -> {
                    progressBar.visibility = View.VISIBLE
                    dataLayout.visibility = View.GONE
                    relativeLayout.visibility = View.GONE
                    errorLayout.visibility = View.GONE
                }

                is Resource.Success -> {
                    val adapter = CountriesArrayAdapter(
                        requireContext(),
                        ArrayList(it.data)
                    )

                    spinner.adapter = adapter
                    spinner.visibility = View.VISIBLE
                }

                is Resource.Error -> {
                    progressBar.visibility = View.GONE
                    dataLayout.visibility = View.GONE
                    relativeLayout.visibility = View.GONE
                    errorLayout.visibility = View.VISIBLE
                    retry.setOnClickListener {
                        viewModel.fetchAll()
                    }
                }
            }
        })
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val resource =
            if(position == 0) viewModel.getAllLiveData().value
            else viewModel.getCountriesData().value

        when (resource) {
            is Resource.Loading -> {
                progressBar.visibility = View.VISIBLE
                dataLayout.visibility = View.GONE
                errorLayout.visibility = View.GONE
                relativeLayout.visibility = View.GONE
            }

            is Resource.Success -> {
                progressBar.visibility = View.GONE
                errorLayout.visibility = View.GONE

                val item = when (resource.data) {
                    is List<*> -> {
                        resource.data[position - 1] as CasesData
                    }
                    is CasesData -> {
                        resource.data
                    }
                    else -> return
                }

                confirmedCases.text = formatNumber(item.cases) +" cases"
                activeCases.text = formatNumber(item.active)
                recoveredCases.text = formatNumber(item.recovered)
                deaths.text = formatNumber(item.deaths)

                confirmedCasesToday.text = getString(R.string.todayTemplate, formatNumber(item.todayCases))
                deathsToday.text = getString(R.string.todayTemplate, formatNumber(item.todayDeaths))

                lastUpdated.text = getString(R.string.lastUpdated, Date(item.updated))

                dataLayout.visibility = View.VISIBLE
                relativeLayout.visibility = View.VISIBLE
            }

            is Resource.Error -> {
                relativeLayout.visibility = View.GONE
                progressBar.visibility = View.GONE
                dataLayout.visibility = View.GONE
                errorLayout.visibility = View.VISIBLE
                retry.setOnClickListener {
                    viewModel.fetchAll()
                }
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
}