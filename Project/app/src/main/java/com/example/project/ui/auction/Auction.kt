package com.example.project.ui.auction

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.project.databinding.FragmentAuctionBinding

class Auction : Fragment() {
    private lateinit var binding: FragmentAuctionBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuctionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val category = arguments?.getString(ARG_CATEGORY) ?: "Electronics"
        val items = AuctionData.getItemsForCategory(category)

        val adapter = AuctionAdapter(items)
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = adapter

        adapter.setOnItemClickListener { item ->
            val intent = Intent(requireContext(), Auctiondetail::class.java).apply {
                putExtra("auction_item", item)
            }
            startActivity(intent)
        }
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        fun newInstance(category: String): Auction {
            return Auction().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY, category)
                }
            }
        }
    }
}