package com.osti.juniorapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.osti.juniorapp.db.dao.AngelDao
import com.osti.juniorapp.db.dao.DipendentiDao
import com.osti.juniorapp.db.dao.GiustificheDao
import com.osti.juniorapp.db.dao.GiustificheRecordDao
import com.osti.juniorapp.db.dao.JuniorConfigDao
import com.osti.juniorapp.db.dao.LogDao
import com.osti.juniorapp.db.dao.NomiFileDao
import com.osti.juniorapp.db.dao.NotificheDao
import com.osti.juniorapp.db.dao.ParametriDao
import com.osti.juniorapp.db.dao.TimbrDao
import com.osti.juniorapp.db.dao.UserDao
import com.osti.juniorapp.db.tables.AngelTable
import com.osti.juniorapp.db.tables.DipendentiTable
import com.osti.juniorapp.db.tables.GiustificheRecord
import com.osti.juniorapp.db.tables.GiustificheTable
import com.osti.juniorapp.db.tables.JuniorConfigTable
import com.osti.juniorapp.db.tables.LogTable
import com.osti.juniorapp.db.tables.NomiFileTable
import com.osti.juniorapp.db.tables.NotificheTable
import com.osti.juniorapp.db.tables.ParametriTable
import com.osti.juniorapp.db.tables.TimbrTable
import com.osti.juniorapp.db.tables.UserTable
import com.osti.juniorapp.preferences.JuniorShredPreferences
import com.osti.juniorapp.utils.Utils.DB_NAME
import com.osti.juniorapp.utils.Utils.DB_VERSION

@Database(
    version = DB_VERSION,
    entities = [ParametriTable::class,
                UserTable::class,
                TimbrTable::class,
                DipendentiTable::class,
                JuniorConfigTable::class,
                AngelTable::class,
                GiustificheTable::class,
                LogTable::class,
                GiustificheRecord::class,
                NomiFileTable::class,
                NotificheTable::class],
        exportSchema = false
)
abstract class JuniorDatabase : RoomDatabase() {

    abstract fun mParametriDao() : ParametriDao
    abstract fun mUsersDao() : UserDao
    abstract fun mTimbrDao() : TimbrDao
    abstract fun mDipDao() : DipendentiDao
    abstract fun mConfigDao() : JuniorConfigDao
    abstract fun mAngelDao() : AngelDao
    abstract fun mGiustificheDao() : GiustificheDao
    abstract fun mLogDao() : LogDao
    abstract fun mGiustificheRecordDao() : GiustificheRecordDao
    abstract fun mNomiFileDao() : NomiFileDao
    abstract fun mNotificheDao() : NotificheDao

    companion object {
        //Per migrare sostituire O con la vecchia versione e N con la nuovaù
        //                                     -->Versione vecchia
        //                                     |  -->Verrsione nuova
        //                                     |  |
        val MIGRATION_O_N = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE `Fruit` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, " +
                            "PRIMARY KEY(`id`))"
                )
            }
        }

        @Volatile
        private var instance: JuniorDatabase? = null
        fun getInstance(context: Context): JuniorDatabase {
            var oldVersion = JuniorShredPreferences.getDBversion(context)

            //DA TOGLIERE DOPO AGGIORNAMENTO
            if(oldVersion == -1){
                oldVersion = 1
            }

            if(oldVersion != DB_VERSION){
                return instance ?: synchronized(this) {
                    instance ?: buildDatabaseWithMigration(context, oldVersion).also { instance = it }
                }
            }
            else {
                return instance ?: synchronized(this) {
                    instance ?: buildDatabase(context).also { instance = it }
                }
            }
        }

        private fun buildDatabase(context: Context): JuniorDatabase {
            return Room.databaseBuilder(context, JuniorDatabase::class.java, DB_NAME).build()
        }

        private fun buildDatabaseWithMigration(context: Context, oldVersion:Int?): JuniorDatabase {
            var oldDbVersion = oldVersion
            val newDb =  Room.databaseBuilder(context, JuniorDatabase::class.java, DB_NAME)
            when(oldDbVersion){
                1 ->{
                    newDb.addMigrations(MIGRATION_1_2)
                    newDb.addMigrations(MIGRATION_2_3)
                    newDb.addMigrations(MIGRATION_3_4)
                    newDb.addMigrations(MIGRATION_4_5)
                }
                2 ->{
                    newDb.addMigrations(MIGRATION_2_3)
                    newDb.addMigrations(MIGRATION_3_4)
                    newDb.addMigrations(MIGRATION_4_5)
                }
                3 ->{
                    newDb.addMigrations(MIGRATION_3_4)
                    newDb.addMigrations(MIGRATION_4_5)
                }
                4 ->{
                    newDb.addMigrations(MIGRATION_4_5)
                }
            }
            return newDb.build()
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE " +
                            "`nomi_file` (" +
                            "`fil_id` INTEGER NOT NULL, " +
                            "`fil_nome` TEXT, " +
                            "`fil_estensione` TEXT, " +
                            "`fil_nome_visualizzato` TEXT, " +
                            "`fil_mimetype` TEXT, " +
                            "`fil_dataora_upload` TEXT, " +
                            "`fil_valido_fino_al` TEXT NOT NULL, " +
                            "`fil_note` TEXT, " +
                            "`fld_dataora_visto_prima` TEXT NOT NULL, " +
                            "`fld_dataora_risposta` TEXT NOT NULL, " +
                            "`ute_nome` TEXT NOT NULL, " +
                            "`fil_tipo` TEXT NOT NULL, " +
                            "`fld_risposta` TEXT NOT NULL, " +
                            "`fil_nome_url` TEXT NOT NULL, " +
                            "`file_tipo` TEXT NOT NULL, " +
                            "`file_risposta` TEXT NOT NULL, " +
                            "PRIMARY KEY(`fil_id`))"
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE users ADD COLUMN `perm_cartellino` TEXT NOT NULL DEFAULT '0'"
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `notifiche` (" +
                            "`n_ute_id_creata` INTEGER, " +
                            "`n_ute_id_destinatario` INTEGER, " +
                            "`n_dataora_creata` TEXT NOT NULL, " +
                            "`n_dataora_inviata_app` TEXT NOT NULL, " +
                            "`n_dataora_letta_app` TEXT NOT NULL, " +
                            "`n_oggetto` TEXT NOT NULL, " +
                            "`n_messaggio` TEXT NOT NULL, " +
                            "`n_tipo_notifica` TEXT NOT NULL, " +
                            "`n_id_record_notifica` INTEGER NOT NULL, " +
                            "`n_id` INTEGER NOT NULL, PRIMARY KEY(`n_id`))"
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE users ADD `nascondi_timbrature` TEXT NOT NULL DEFAULT '0'"
                )
            }
        }


    }
}