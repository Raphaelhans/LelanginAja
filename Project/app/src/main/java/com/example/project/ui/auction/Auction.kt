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
import com.bumptech.glide.Glide
import com.example.project.R
import com.example.project.UserViewModel
import com.example.project.database.dataclass.Products
import com.example.project.databinding.FragmentAuctionBinding
import kotlinx.coroutines.launch

class Auction : Fragment() {
    private lateinit var binding: FragmentAuctionBinding
    val viewModel: UserViewModel by activityViewModels()
    lateinit var adapter: AuctionAdapter

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

        val categoryId = arguments?.getString(ARG_CATEGORY) ?: return
        val email = arguments?.getString(ARG_EMAIL) ?: return
        viewModel.getCurrUser(email)

        adapter = AuctionAdapter()
        binding.recyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch {
            val items = viewModel.loadItemsForCategory(categoryId)
            Log.d("Auction", "Items: $items")
            adapter.submitList(items)
        }

        viewModel.searchBrg.observe(viewLifecycleOwner) { query ->
            lifecycleScope.launch {
                val items = viewModel.loadItemsForCategory(categoryId)
                val filtered = items.filter {
                    it.name.contains(query, ignoreCase = true)
                }
                adapter.submitList(filtered)
            }
        }

        viewModel.currUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                adapter.onItemClickListener = { item ->
                    val intent = Intent(requireContext(), Auctiondetail::class.java).apply {
                        putExtra("produk_id", item.items_id)
                        putExtra("seller_id", item.seller_id.toString())
                        putExtra("user_id", user.user_id.toString())
                        putExtra("email", user.email)
                        putExtra("auction_item", item.items_id)
                    }
                    startActivity(intent)

                }
            }
            adapter.onItemClickListener = { item ->
                val intent = Intent(requireContext(), Auctiondetail::class.java).apply {
                    putExtra("items_id", item.items_id)
                    putExtra("email", user?.email)
                    putExtra("user_id", user?.user_id.toString())
                    putExtra("seller_id", item.seller_id.toString())
                    putExtra("current_userId", user?.user_id.toString())

                    if (item is Products) {
                        putExtra("product", item)
                    }
                }
                startActivity(intent)
            }
        }
    }
    companion object {
        private const val ARG_CATEGORY = "category"
        private const val ARG_EMAIL = "email"

        fun newInstance(category: String, email: String): Auction {
            return Auction().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY, category)
                    putString(ARG_EMAIL, email)
                }
            }
        }
    }
}
