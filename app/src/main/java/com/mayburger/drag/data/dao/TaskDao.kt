package com.mayburger.drag.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mayburger.drag.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE language LIKE :language AND state IS :state")
    fun getTasks(language:String, state:String): LiveData<List<Task>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putTasks(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putTasks(book:List<Task>)

    @Update
    suspend fun updateTask(task:Task)

    @Delete
    suspend fun deleteTask(task:Task)

}