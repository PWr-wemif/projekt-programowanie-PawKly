package com.example.forgeyourstrength

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ExerciseViewModel(private val repository: ExerciseRepository) : ViewModel() {

    // Funkcja do dodawania nowego ćwiczenia
    fun addExercise(exercise: ExerciseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            // Najpierw wstawiamy serię ćwiczeń do bazy danych, aby uzyskać ich ID
            val seriesIds = exercise.series.map { series ->
                // Upewniamy się, że metoda insertExerciseSeries zwraca Long
                repository.insertExerciseSeries(convertExerciseSeriesToDB(series))
            }

            // Konwersja ExerciseEntity na ExerciseEntityDB przed wstawieniem do bazy danych
            val exerciseDB = convertExerciseEntityToDB(exercise, seriesIds)
            repository.insertExercise(exerciseDB)
        }
    }

    // Metoda do konwersji ExerciseEntity na ExerciseEntityDB
    private fun convertExerciseEntityToDB(exercise: ExerciseEntity, seriesIds: List<Long>): ExerciseEntityDB {
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
            seriesIds = seriesIds
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
