package com.example.aware

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.aware.database.NotificationEntity
import com.example.aware.utils.NotificationAdapter
import com.example.aware.utils.NotificationItemAnimator
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActiveNotificationsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private var hasVibrated = false
    private var deletedNotification: NotificationEntity? = null
    private var deletedPosition: Int = -1
    private var isHandlingDelete = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_active_notifications, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.mainRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        
        adapter = NotificationAdapter(mutableListOf())
        recyclerView.adapter = adapter
        recyclerView.itemAnimator = NotificationItemAnimator()

        val repository = (activity as DashboardActivity).repository

        repository.notifications.observe(viewLifecycleOwner) { newList ->
            if (!isHandlingDelete) {
                adapter.updateList(newList)
            }
        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView)
    }

    private val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            if (position == RecyclerView.NO_POSITION) return

            val repository = (activity as DashboardActivity).repository
            val notificationList = repository.notifications.value ?: return
            if (position >= notificationList.size) return

            deletedNotification = notificationList[position]
            deletedPosition = position

            isHandlingDelete = true
            adapter.removeItem(position)

            lifecycleScope.launch(Dispatchers.IO) {
                repository.removeNotification(deletedNotification!!)
            }

            val snackbar = Snackbar.make(recyclerView, "Notification deleted", Snackbar.LENGTH_LONG)
                .setAction("UNDO") {
                    lifecycleScope.launch {
                        withContext(Dispatchers.IO) {
                            deletedNotification?.let {
                                repository.addNotification(it)
                            }
                        }

                        withContext(Dispatchers.Main) {
                            deletedNotification?.let {
                                adapter.insertItem(it, deletedPosition)
                                recyclerView.smoothScrollToPosition(deletedPosition)
                            }
                        }
                    }
                }
                .addCallback(object : Snackbar.Callback() {
                    override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                        isHandlingDelete = false
                        deletedNotification = null
                        deletedPosition = -1
                    }
                })

            snackbar.show()
        }

        override fun onChildDraw(
            c: Canvas,
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            dX: Float,
            dY: Float,
            actionState: Int,
            isCurrentlyActive: Boolean
        ) {
            val itemView = viewHolder.itemView
            val context = recyclerView.context
            val deleteIcon = ContextCompat.getDrawable(context, R.drawable.baseline_delete_outline_24)
            val paint = Paint()
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            if (dX < 0) {
                val swipeThreshold = itemView.width / 3
                val alpha = (255 * (minOf(kotlin.math.abs(dX), swipeThreshold.toFloat()) / swipeThreshold)).toInt()
                paint.color = Color.argb(minOf(alpha, 180), 255, 59, 48)

                val backgroundRect = RectF(
                    itemView.right + dX, itemView.top.toFloat(),
                    itemView.right.toFloat(), itemView.bottom.toFloat()
                )
                c.drawRect(backgroundRect, paint)

                deleteIcon?.let {
                    val iconSize = it.intrinsicHeight
                    val iconMargin = (itemView.height - iconSize) / 2
                    val iconLeft = itemView.right - iconSize - iconMargin
                    val iconTop = itemView.top + iconMargin
                    val iconRight = itemView.right - iconMargin
                    val iconBottom = iconTop + iconSize

                    if (kotlin.math.abs(dX) > iconSize * 2) {
                        it.alpha = minOf(alpha, 255)
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        it.draw(c)
                    }
                }

                if (!hasVibrated && kotlin.math.abs(dX) > itemView.width / 3) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.EFFECT_TICK))
                    } else {
                        vibrator.vibrate(50)
                    }
                    hasVibrated = true
                }
            }

            if (dX == 0f) {
                hasVibrated = false
            }

            itemView.scaleX = 1.0f
            itemView.scaleY = 1.0f

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    override fun onResume() {
        super.onResume()
        isHandlingDelete = false
    }
}
