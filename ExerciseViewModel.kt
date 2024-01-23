package com.example.forgeyourstrength

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseViewModel(private val exerciseEntityDao: ExerciseEntityDao, private val exerciseSeriesDao: ExerciseSeriesDao) : ViewModel() {

    // Funkcja do dodawania nowego ćwiczenia
    fun addExercise(exercise: ExerciseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Assuming exerciseSeriesDao.insert returns a Long and never null.
            val seriesIdsLong = exercise.series.map { series ->
                exerciseSeriesDao.insert(convertExerciseSeriesToDB(series))
            }

            // Converting Long list to Int list assuming all values are within Int range.
            val seriesIds = seriesIdsLong.map { it.toInt() }

            // Make sure that seriesIds is the correct type expected by convertExerciseEntityToDB
            val exerciseDB = convertExerciseEntityToDB(exercise, seriesIds)
            exerciseEntityDao.insert(exerciseDB)
        }
    }

    // Metoda do konwersji ExerciseEntity na ExerciseEntityDB
    private fun convertExerciseEntityToDB(exercise: ExerciseEntity, seriesIds: List<Int>): ExerciseEntityDB {
        // Należy również upewnić się, że klasa ExerciseEntityDB jest zdefiniowana z listą seriesIds typu List<Int>
        return ExerciseEntityDB(
            name = exercise.name,
            isWeighted = exercise.isWeighted,
            weight = exercise.weight,
            isSeries = exercise.isSeries,
            seriesCount = exercise.seriesCount,
            isRepetitive = exercise.isRepetitive,
            repetitionsCount = exercise.repetitionsCount,
            isTimed = exercise.isTimed,
            duration = exercise.duration,
            seriesIds = seriesIds // Lista ID serii typu Int
        )
    }

    // Metoda do konwersji ExerciseSeries na ExerciseSeriesDB
    private fun convertExerciseSeriesToDB(series: ExerciseSeries): ExerciseSeriesDB {
        return ExerciseSeriesDB(
            weight = series.weight,
            repetitionsCount = series.repetitionsCount,
            duration = series.duration,
            distance = series.distance,
            secondDistanceData = series.secondDistanceData
        )
    }

    // Tutaj możesz dodać inne funkcje do aktualizowania, usuwania itd.
}
