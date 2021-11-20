package com.mayburger.drag

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
import androidx.lifecycle.lifecycleScope
import com.mayburger.drag.data.Prefs


@AndroidEntryPoint
class TaskFragment : Fragment() {

    lateinit var binding: FragmentTaskBinding
    @Inject
    lateinit var database: PersistenceDatabase
    @Inject
    lateinit var taskAdapter: TaskAdapter

    companion object{
        const val ARG_TITLE = "arg_title"
        const val ARG_STATE = "arg_state"
        fun newInstance(title:String, state:String):Fragment{
            return TaskFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_TITLE, title)
                    putString(ARG_STATE, state)
                }
            }
        }
    }

    lateinit var title:String
    lateinit var state:String

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.title.text = title
        binding.recycler.apply{
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(requireActivity())
        }
        database.taskDao().getTasks("bahasa", state).observe(viewLifecycleOwner){
            taskAdapter.submitList(it)
        }
        taskAdapter.setListener(object:TaskAdapter.Callback{
            override fun onSelectedItem(task: Task, v: View) {
            }
        })

        binding.dropArea.setOnDragListener { v, event ->
            when (event.action) {
                DragEvent.ACTION_DROP -> {
                    lifecycleScope.launchWhenCreated {
                        database.taskDao().updateTask(Prefs.draggingTask.apply { state = this@TaskFragment.state })
                    }
                    binding.recycler.setBackgroundColor(0)
                }
                DragEvent.ACTION_DRAG_ENTERED->{
                    binding.recycler.setBackgroundColor(resources.getColor(R.color.neutral_300,null))
                }
                DragEvent.ACTION_DRAG_EXITED->{
                    binding.recycler.setBackgroundColor(0)
                }
            }
            true
        }
    }
}