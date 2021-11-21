package com.mayburger.drag.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mayburger.drag.model.Task

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE language LIKE :language AND state IS :state ORDER BY `order`")
    fun getTasks(language:String, state:String): LiveData<List<Task>>
    @Query("SELECT * FROM tasks WHERE language LIKE :language AND state IS :state ORDER BY `order`")
    fun getTasksSuspended(language:String, state:String): List<Task>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putTasksSuspended(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun putTasks(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun putTasksSuspended(book:List<Task>)

    @Update
    suspend fun updateTask(task:Task)
    @Update
    suspend fun updateTask(task:ArrayList<Task>)

    @Delete
    suspend fun deleteTask(task:Task)

}