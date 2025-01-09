package com.ibv.transactions.dashBoard.fragment

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.ibv.transactions.R
import com.ibv.transactions.base.DateHelper
import com.ibv.transactions.base.ErrorDialogHelper
import com.ibv.transactions.base.SharedPref
import com.ibv.transactions.dashBoard.fragment.adapter.TransactionAdapter
import com.ibv.transactions.dashBoard.fragment.model.TransactionBean
import com.ibv.transactions.dashBoard.fragment.transactionVM.TransactionVM
import com.ibv.transactions.data.util.ApiState
import com.ibv.transactions.databinding.FragmentTransactionListBinding
import com.ibv.transactions.others.Loader
import com.ibv.transactions.userAuth.UserActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class TransactionListFragment : Fragment() {

    private val transactionVM: TransactionVM by viewModels()
    private var _binding: FragmentTransactionListBinding? = null
    private val binding get() = _binding!!
    lateinit var loader: Loader
    private lateinit var adapter: TransactionAdapter
    private val transactionList = mutableListOf<TransactionBean>() // Original data
    private val filteredList = mutableListOf<TransactionBean>()    // Filtered data

    private var isExpanded = false
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionListBinding.inflate(inflater, container, false)
        loader = Loader(requireContext())

        // Initialize RecyclerView
        binding.rcvTransactionHistory.layoutManager = LinearLayoutManager(requireContext())
        adapter = TransactionAdapter(filteredList)
        binding.rcvTransactionHistory.adapter = adapter

        // Observe data
        observeTransactionData()

        // Fetch transactions
        transactionVM.getTransaction()

        setViewClickListeners()

        return binding.root
    }

    private fun setViewClickListeners() {

        val startColor = ContextCompat.getColor(requireContext(), R.color.white)
        val endColor = ContextCompat.getColor(requireContext(), R.color.white)

        binding.ivLogOut.setOnClickListener {
            confirmationDialog()
        }

        binding.etFromDate.setOnClickListener {
            DateHelper.showPastDatePickerDialog(requireContext(), binding.etFromDate)
        }

        binding.etToDate.setOnClickListener {
            DateHelper.showPastDatePickerDialog(requireContext(), binding.etToDate)
        }

        // Filter toggle button
        binding.ivFilter.setOnClickListener {
            if (isExpanded) {
                collapseWithColor(binding.llFilter, 500, endColor, startColor)
                resetFilters()
            } else {
                expandWithColor(binding.llFilter, 500, startColor, endColor)
            }
            isExpanded = !isExpanded
        }

        binding.swipeRefresh.setOnRefreshListener {
            transactionVM.getTransaction()
            binding.swipeRefresh.isRefreshing = false
        }

        filterTransactions(null, null, null)

        binding.searchText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val searchQuery = binding.searchText.text.toString().trim()
                val fromDate = binding.etFromDate.text.toString().trim()
                val toDate = binding.etToDate.text.toString().trim()
                filterTransactionsBasedOnCriteria(fromDate, toDate, searchQuery)
            }

            override fun afterTextChanged(s: Editable?) {}

            private fun filterTransactionsBasedOnCriteria(fromDate: String, toDate: String, searchQuery: String) {
                when {
                    fromDate.isEmpty() && toDate.isEmpty() -> filterTransactions(null, null, searchQuery)
                    fromDate.isEmpty() -> filterTransactions(null, toDate, searchQuery)
                    toDate.isEmpty() -> filterTransactions(fromDate, null, searchQuery)
                    else -> filterTransactions(fromDate, toDate, searchQuery)
                }
            }
        })

        binding.btnSearch.setOnClickListener {
            if (binding.etFromDate.text.toString().trim().isNotEmpty() && binding.etToDate.text.toString().trim().isNotEmpty()){
                filterTransactions(
                    binding.etFromDate.text.toString().trim(),
                    binding.etToDate.text.toString().trim(),
                    binding.searchText.text.toString().trim()
                )
            }else{
                ErrorDialogHelper().ErrorMessage(requireActivity(),"Please select start & end date".toString(),"Filter")
            }
        }
    }

    private fun resetFilters() {
        binding.searchText.setText("")
        binding.etToDate.text = null
        binding.etFromDate.text = null
        binding.searchText.hint = getString(R.string.common_search_hint)
        filterTransactions(null, null, null)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observeTransactionData() {
        lifecycleScope.launchWhenStarted {
            // Observe apiStateFlow for network data
            transactionVM.apiStateFlow.collect { state ->
                when (state) {
                    is ApiState.Success<List<TransactionBean>> -> {
                        loader.dismiss()
                        transactionList.clear()
                        transactionList.addAll(state.data)
                        filteredList.clear()
                        filteredList.addAll(transactionList)
                        adapter.notifyDataSetChanged()
                    }

                    is ApiState.Failure -> showErrorState(state.error)

                    is ApiState.Loading -> {
                        loader.show()
                        binding.swipeRefresh.visibility = View.VISIBLE
                        binding.noDataFound.visibility = View.GONE
                    }

                    else -> Unit
                }
            }
        }



        // Observe roomStateFlow for local Room data
        lifecycleScope.launchWhenStarted {
            transactionVM.roomStateFlow.collect { transactions ->
                if (transactions.isNotEmpty()) {
                    // Convert Room data (Transaction) to TransactionBean
                    val convertedTransactions = transactions.map { roomTransaction ->
                        TransactionBean(
                            id = roomTransaction.id,
                            date = roomTransaction.date,
                            category = roomTransaction.category,
                            amount = roomTransaction.amount,
                            description = roomTransaction.description
                        )
                    }

                    transactionList.clear()
                    transactionList.addAll(convertedTransactions)
                    filteredList.clear()
                    filteredList.addAll(transactionList)
                    adapter.notifyDataSetChanged()
                }
            }
        }



    }

    private fun showErrorState(error: Throwable) {
        ErrorDialogHelper().ErrorMessage(requireActivity(), error.message.toString(), "Transaction Error")
        loader.dismiss()
    }

    private fun filterTransactions(startDate: String?, endDate: String?, category: String?) {
        try {
            val start = startDate?.let { dateFormat.parse(it) }
            val end = endDate?.let { dateFormat.parse(it) }

            // Validate date range
            if (start != null && end != null && start.after(end)) {
                Log.e("FilterError", "Start date cannot be after end date")
                return
            }

            filteredList.clear()
            val filteredTransactions = transactionList.filter { transaction ->
                val transactionDate = dateFormat.parse(transaction.date)
                val isInDateRange = if (start != null && end != null) {
                    transactionDate != null && transactionDate in start..end
                } else {
                    true
                }

                val isInCategory = if (!category.isNullOrEmpty()) {
                    transaction.category.startsWith(category, ignoreCase = true)
                } else {
                    true
                }

                isInDateRange && isInCategory
            }

            filteredList.addAll(filteredTransactions)

            if (filteredTransactions.isEmpty()) {
                Log.e("FilterError", "No matching transactions found for the given criteria.")
                binding.swipeRefresh.visibility = View.GONE
                binding.noDataFound.visibility = View.VISIBLE
            } else {
                binding.swipeRefresh.visibility = View.VISIBLE
                binding.noDataFound.visibility = View.GONE
            }

            adapter.notifyDataSetChanged()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("FilterError", "Invalid date format or filtering error: ${e.message}")
        }
    }

    fun expandWithColor(view: View, duration: Int, startColor: Int, endColor: Int) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val targetHeight = view.measuredHeight
        view.visibility = View.VISIBLE

        val heightAnimator = ValueAnimator.ofInt(0, targetHeight)
        heightAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            view.layoutParams.height = animatedValue
            view.requestLayout()
        }

        val colorAnimator = ValueAnimator.ofArgb(startColor, endColor)
        colorAnimator.addUpdateListener { animation ->
            val animatedColor = animation.animatedValue as Int
            view.setBackgroundColor(animatedColor)
        }

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(heightAnimator, colorAnimator)
        animatorSet.duration = duration.toLong()
        animatorSet.interpolator = DecelerateInterpolator()

        animatorSet.start()
    }

    fun collapseWithColor(view: View, duration: Int, startColor: Int, endColor: Int) {
        val initialHeight = view.height

        val heightAnimator = ValueAnimator.ofInt(initialHeight, 0)
        heightAnimator.addUpdateListener { animation ->
            val animatedValue = animation.animatedValue as Int
            view.layoutParams.height = animatedValue
            view.requestLayout()
        }

        val colorAnimator = ValueAnimator.ofArgb(startColor, endColor)
        colorAnimator.addUpdateListener { animation ->
            val animatedColor = animation.animatedValue as Int
            view.setBackgroundColor(animatedColor)
        }

        heightAnimator.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator) {}
            override fun onAnimationEnd(p0: Animator) {
                view.visibility = View.GONE
            }
            override fun onAnimationCancel(p0: Animator) {}
            override fun onAnimationRepeat(p0: Animator) {}
        })

        val animatorSet = AnimatorSet()
        animatorSet.playTogether(heightAnimator, colorAnimator)
        animatorSet.duration = duration.toLong()
        animatorSet.interpolator = DecelerateInterpolator()

        animatorSet.start()
    }

    fun confirmationDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.confirm_logout_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnPositive = dialog.findViewById<TextView>(R.id.btn_Yes)
        val btnNegative = dialog.findViewById<TextView>(R.id.btn_No)

        val loader = Loader(requireContext())

        btnPositive.setOnClickListener {
            dialog.dismiss()
            loader.show()

            lifecycleScope.launch {
                delay(2000L)
                SharedPref.getInstance().removeToken()
                loader.dismiss()
                val intent = Intent(requireContext(), UserActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }

        btnNegative.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
