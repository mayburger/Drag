package com.mayburger.drag.ui

import android.icu.number.IntegerWidth
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.mayburger.drag.adapter.TaskAdapter
import com.mayburger.drag.data.PersistenceDatabase
import com.mayburger.drag.databinding.FragmentTaskBinding
import com.mayburger.drag.model.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_task.*
import javax.inject.Inject

import android.view.DragEvent
import android.widget.TextView
import com.mayburger.drag.R
import com.mayburger.drag.data.Prefs
import com.mayburger.drag.ui.bsd.StateComposerBSD
import com.mayburger.drag.utils.ViewUtils.dpToPx
import com.mayburger.drag.utils.ViewUtils.hide
import com.mayburger.drag.utils.ViewUtils.show
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


@AndroidEntryPoint
class TaskFragment : Fragment() {

    lateinit var binding: FragmentTaskBinding

    @Inject
    lateinit var database: PersistenceDatabase

    @Inject
    lateinit var taskAdapter: TaskAdapter

    companion object {
        const val ARG_TITLE = "arg_title"
        const val ARG_STATE = "arg_state"
        fun newInstance(title: String, state: String): Fragment {
            return TaskFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_STATE, state)
                }
            }
        }
    }

    lateinit var title: String
    lateinit var state: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE).toString()
            state = it.getString(ARG_STATE).toString()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskBinding.inflate(LayoutInflater.from(requireActivity()))
        return binding.root
    }

    val tasks = ArrayList<Task>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = title
        binding.recycler.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }
        database.taskDao().getTasks(state).observe(viewLifecycleOwner) {
            tasks.clear()
            tasks.addAll(it)
            taskAdapter.setItems(tasks)
        }
        taskAdapter.setListener(object : TaskAdapter.Callback {
            override fun onSelectedItem(task: Task, v: View) {
            }

            override fun onDragEntered(position: Int) {
                taskAdapter.setItems(tasks, position)
            }

            override fun onDropped() {
                onDrop()
            }
        })

        binding.root.setOnDragListener { v, event ->
            if (state != Int.MIN_VALUE.toString()){
                when (event.action) {
                    DragEvent.ACTION_DROP -> {
                        onDrop()
                    }
                    DragEvent.ACTION_DRAG_ENTERED -> {
                        if (taskAdapter.data.isEmpty() || taskAdapter.data.filter { it.id == -1 }.isEmpty()) {
                            taskAdapter.setItemsLast(tasks)
                        }
                    }
                    DragEvent.ACTION_DRAG_EXITED -> {
                        taskAdapter.setItems(tasks)
                    }
                    DragEvent.ACTION_DRAG_ENDED -> {
                        taskAdapter.setItems(tasks)
                    }
                }
            }
            true
        }

        initializeNewList()
    }

    fun initializeNewList() {
        if (state == Integer.MIN_VALUE.toString()) {
            binding.title.setTextColor(resources.getColor(R.color.purple_500, null))
            binding.title.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
            binding.title.setPadding(0, dpToPx(16), 0, 0)
            binding.recyclerCard.setOnClickListener {
                StateComposerBSD().show(childFragmentManager, "")
            }
        }
    }

    fun onDrop() {
        val newData = taskAdapter.data.mapIndexed { index, task ->
            if (task.id == -1) {
                Prefs.draggingTask.apply {
                    state = this@TaskFragment.state
                    order = index
                }
            } else {
                task.apply {
                    order = index
                }
            }
        }
        CoroutineScope(IO).launch {
            database.taskDao().updateTask(Prefs.draggingTask.apply {
                state = this@TaskFragment.state
                order = if (taskAdapter.data.isEmpty()) 0 else taskAdapter.data.maxOf {
                    it.order ?: 0
                } + 1
            })
            database.taskDao().updateTask(newData.toCollection(arrayListOf()))
        }
    }
}
