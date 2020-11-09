package com.hebreuyannis.pocksmsscheduler.model

import android.content.Context
import androidx.room.*

@Dao
interface TransactionDatabaseDao {

    @Insert
    fun insert(transactionLog: TransactionLogEntity)

    @Query("SELECT * FROM transaction_Log WHERE id_transaction = :transactionId")
    fun getTransactionById(transactionId: String): TransactionLogEntity?

    @Query("UPDATE transaction_Log SET date_update = :dateUpdate, status = :status WHERE id = :id")
    fun updateTransactionLog(id: Long, dateUpdate: Long, status: Int): Int

    @Query("SELECT * FROM transaction_Log")
    fun getAllTransactions(): List<TransactionLogEntity>

}

@Database(
    entities = [TransactionLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TransactionDatabase : RoomDatabase() {
    abstract val transactionDatabaseDao: TransactionDatabaseDao

    companion object {

        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        fun getInstance(context: Context): TransactionDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        TransactionDatabase::class.java,
                        "transaction_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }

        }
    }
}
