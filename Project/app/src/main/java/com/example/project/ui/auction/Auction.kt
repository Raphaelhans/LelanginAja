package com.example.project.ui.auction

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.project.UserViewModel
import com.example.project.database.dataclass.Products
import com.example.project.databinding.FragmentAuctionBinding
import kotlinx.coroutines.launch

class Auction : Fragment() {
    private lateinit var binding: FragmentAuctionBinding
    val viewModel: UserViewModel by activityViewModels()
    lateinit var adapter: AuctionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAuctionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categoryId = arguments?.getString(ARG_CATEGORY) ?: return

        adapter = AuctionAdapter()
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            val items = viewModel.loadItemsForCategory(categoryId)
            Log.d("Auction", "Items: $items")
            adapter.submitList(items)
        }

        adapter.onItemClickListener = { item ->
            val auctionItem = AuctionItem(
                id = item.items_id,
                name = item.name,
                category = item.category_id,
                currentBid = item.start_bid.toDouble(),
                imageResId = item.image_url,
                sellerId = item.seller_id
            )

            val intent = Intent(requireContext(), Auctiondetail::class.java).apply {
                putExtra("auction_item", auctionItem)
                putExtra("current_userId", viewModel.getCurrentUserId())

                if (item is Products) {
                    putExtra("product", item)
                }
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
