package com.example.project

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [Auction.newInstance] factory method to
 * create an instance of this fragment.
 */
class Auction : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_auction, container, false)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
        val category = arguments?.getString(ARG_CATEGORY) ?: "Electronics"
        val items = AuctionData.getItemsForCategory(category)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        recyclerView.adapter = AuctionAdapter(items)

        return view
    }

    companion object {
        private const val ARG_CATEGORY = "category"

        // Factory method to create a new instance with a category
        fun newInstance(category: String): Auction {
            return Auction().apply {
                arguments = Bundle().apply {
                    putString(ARG_CATEGORY, category)
                }
            }
        }
    }
}