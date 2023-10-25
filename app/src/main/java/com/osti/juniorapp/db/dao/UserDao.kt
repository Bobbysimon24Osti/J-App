package com.osti.juniorapp.db.dao

import androidx.room.*
import com.osti.juniorapp.db.tables.UserTable

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun creaUser(params : UserTable)

    @Query("select * from users where server_id = :id")
    fun getUser (id:String) :UserTable?

    @Query("UPDATE users set name = :name, type = :type, perm_timbrature = :perm_timbrature, perm_workflow = :perm_workflow, badge = :badge, idDipendente = :idDipendente, nascondi_timbrature = :nascondiTimbrature, livello_manager = :livelloMan where server_id = :server_id")
    fun setUser (name:String, type:String, perm_timbrature:String, perm_workflow: String, badge:Int, idDipendente:Long, server_id:String, nascondiTimbrature:String, livelloMan:String)
}
