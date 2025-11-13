package com.example.aware

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aware.utils.RecentNotificationAdapter

class ClearedNotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecentNotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_cleared_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recentlyClearedRecycleView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = RecentNotificationAdapter(mutableListOf())
        recyclerView.adapter = adapter

        val repository = (activity as DashboardActivity).repository2

        repository.recentNotifications.observe(viewLifecycleOwner) { newList ->
            adapter.updateList(newList)
        }
    }
}
