package com.example.forgeyourstrength

import android.content.Context
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "exercise_series")
data class ExerciseSeriesDB(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var weight: String,
    var repetitionsCount: String,
    var duration: String,
    var distance: String = "",
    var secondDistanceData: String = "",
    var seriesIds: List<Long>
)

@Entity(
    tableName = "exercise_entity",
    foreignKeys = [ForeignKey(
        entity = ExerciseSeriesDB::class,
        parentColumns = ["id"],
        childColumns = ["seriesId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ExerciseEntityDB(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var name: String,
    var isWeighted: Boolean,
    var weight: String,
    var isSeries: Boolean,
    var seriesCount: String,
    var isRepetitive: Boolean,
    var repetitionsCount: String,
    var isTimed: Boolean,
    var duration: String,
    @TypeConverters(Converters::class)
    var seriesIds: List<Int> // Changed to seriesIds
)

@Dao
interface ExerciseSeriesDao {
    @Insert
    fun insert(exerciseSeriesDB: ExerciseSeriesDB): Long

    @Query("SELECT * FROM exercise_series")
    fun getAll(): List<ExerciseSeriesDB>
}

@Dao
interface ExerciseEntityDao {
    @Insert
    fun insert(exerciseEntityDB: ExerciseEntityDB)

    @Query("SELECT * FROM exercise_entity")
    fun getAll(): List<ExerciseEntityDB>
}

@Database(entities = [ExerciseSeriesDB::class, ExerciseEntityDB::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exerciseSeriesDao(): ExerciseSeriesDao
    abstract fun exerciseEntityDao(): ExerciseEntityDao

    companion object {
        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "exercise_database"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

class Converters {
    @TypeConverter
    fun fromString(value: String): List<Int> {
        val listType = object : TypeToken<List<Int>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<Int>): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}
