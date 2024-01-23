package com.example.forgeyourstrength

import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.forgeyourstrength.ui.theme.ForgeYourStrengthTheme
import java.util.*


data class ExerciseSeries(
    var weight: String,
    var repetitionsCount: String,
    var duration: String,
    var distance: String = "", // Add this
    var secondDistanceData: String = "" // And this
)

class ExerciseEntity(
    var name: String,
    var isWeighted: Boolean,
    var weight: String, // General weight if not done in series
    var isSeries: Boolean,
    var seriesCount: String, // Number of series
    var isRepetitive: Boolean,
    var repetitionsCount: String, // General number of repetitions if not done in series
    var isTimed: Boolean,
    var duration: String, // General duration if not done in series
    var series: List<ExerciseSeries> // Details for each series
)

class YourApplication : Application() {
    // Usuwamy leniwe inicjalizacje, ponieważ mogą one prowadzić do błędów w przypadku odwołania przed ich zainicjalizowaniem
    val database: AppDatabase = AppDatabase.getDatabase(this)
    val repository: ExerciseRepository = ExerciseRepository(database.exerciseDao())
}

class ExerciseViewModelFactory(private val repository: ExerciseRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExerciseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ExerciseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ExerciseRepository(private val exerciseDao: ExerciseEntityDao, private val exerciseSeriesDao: ExerciseSeriesDao) {
    // Przykładowe metody dostępu do bazy danych
    fun getAllExercises() = exerciseDao.getAll()
    fun insertExercise(entity: ExerciseEntityDB) = exerciseDao.insert(entity)
    // Inne metody...
}

class MainActivity : ComponentActivity() {
    private lateinit var sharedPref: SharedPreferences

    // Zaktualizowano, aby korzystać z konkretnego dostawcy fabryki ViewModel
    private val exerciseViewModel: ExerciseViewModel by viewModels {
        ExerciseViewModelFactory((applicationContext as YourApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPref = getPreferences(Context.MODE_PRIVATE)
        val defaultLanguage = sharedPref.getString("language", "en") ?: "en"
        if (defaultLanguage != getCurrentLanguage()) {
            setLanguage(defaultLanguage)
        }

        setContent {
            ForgeYourStrengthTheme {
                val exercisesListState = remember { mutableStateOf(listOf<ExerciseEntity>()) }
                AppNavigation(
                    exercisesListState = exercisesListState,
                    exerciseViewModel = exerciseViewModel, // Pass it here
                    onLanguageChange = { language ->
                        setLanguage(language)
                        restartActivity()
                    }
                )
            }
        }
    }

    private fun setLanguage(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)

        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createConfigurationContext(config)
        } else {
            resources.updateConfiguration(config, resources.displayMetrics)
        }

        with(sharedPref.edit()) {
            putString("language", language)
            apply()
        }
        // Call to restart the activity is removed from here; it should be called after this method
    }

    private fun getCurrentLanguage(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales.get(0).language
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale.language
        }
    }

    private fun restartActivity() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        startActivity(intent)
        finish()
    }
}

@Composable
fun AppNavigation(
    exercisesListState: MutableState<List<ExerciseEntity>>,
    exerciseViewModel: ExerciseViewModel = viewModel(), // This provides a default ViewModel instance
    onLanguageChange: (String) -> Unit = {}
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("training") { DetailScreen(navController, "Rozpocznij trening") }
        composable("createPlan") { DetailScreen(navController, "Utwórz plan treningowy") }
        composable("addAssumptions") { DetailScreen(navController, "Dodaj założenia planu treningowego") }
        composable("removeExercise") { DetailScreen(navController, "Usuń ćwiczenie") }
        composable("editExercise") { DetailScreen(navController, "Edytuj ćwiczenie") }
        composable("editExercises") { EditExercisesScreen(navController) }
        composable("exerciseAdded") { ExerciseAddedScreen(navController) }
        composable("newExercise") { NewExerciseScreen(navController, exercisesListState, exerciseViewModel) }
        composable("settings") { SettingsScreen(navController) }
        composable("language") {
            LanguageScreen(navController, onLanguageChange)
        }
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            "ForgeYourStrength",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(
            onClick = { navController.navigate("training") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Rozpocznij trening")
        }
        Button(
            onClick = { navController.navigate("createPlan") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Utwórz plan treningowy")
        }
        Button(
            onClick = { navController.navigate("editExercises") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Edytuj swoje ćwiczenia")
        }
        Button(
            onClick = { navController.navigate("addAssumptions") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Dodaj założenia planu treningowego")
        }
        Button(
            onClick = { navController.navigate("settings") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Ustawienia")
        }
    }
}

@Composable
fun EditExercisesScreen(navController: NavController) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            "Edycja ćwiczeń",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .padding(bottom = 24.dp)
                .align(Alignment.CenterHorizontally)
        )
        Button(
            onClick = { navController.navigate("newExercise") }, // Change this line
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Dodaj własne ćwiczenie")
        }
        Button(
            onClick = { navController.navigate("removeExercise") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Usuń ćwiczenie")
        }
        Button(
            onClick = { navController.navigate("editExercise") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
        ) {
            Text("Edytuj ćwiczenie")
        }
    }
}

@Composable
fun SeriesDetailInput(seriesDetail: ExerciseSeries,
                      seriesNumber: Int,
                      onSeriesDetailChanged: (ExerciseSeries) -> Unit,
                      isWeighted: Boolean,
                      isRepetitive: Boolean,
                      isTimed: Boolean,
                      isDistanceBased: Boolean,
                      distance: String,
                      secondDistanceData: String,
                      onDistanceChanged: (String) -> Unit,
                      onSecondDistanceDataChanged: (String) -> Unit
) {
    var weight by remember { mutableStateOf(seriesDetail.weight) }
    var repetitionsCount by remember { mutableStateOf(seriesDetail.repetitionsCount) }
    var hours by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    var seconds by remember { mutableStateOf("") }

    Column {
        Text("Seria $seriesNumber")
        if (isWeighted) {
            TextField(
                value = weight,
                onValueChange = { updatedWeight ->
                    onSeriesDetailChanged(seriesDetail.copy(weight = updatedWeight))
                },
                label = { Text("Ciężar") }
            )
        }
        if (isRepetitive) {
            TextField(
                value = repetitionsCount,
                onValueChange = { updatedRepetitions ->
                    onSeriesDetailChanged(seriesDetail.copy(repetitionsCount = updatedRepetitions))
                },
                label = { Text("Ilość powtórzeń") }
            )
        }

        // Funkcja do aktualizacji duration
        fun updateDuration() {
            val updatedDuration = "$hours:$minutes:$seconds"
            onSeriesDetailChanged(seriesDetail.copy(duration = updatedDuration))
        }

        if (isTimed) {
            Row {
                TextField(
                    value = hours,
                    onValueChange = {
                        hours = it
                        updateDuration()
                    },
                    label = { Text("Godziny") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = minutes,
                    onValueChange = {
                        minutes = it
                        updateDuration()
                    },
                    label = { Text("Minuty") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = seconds,
                    onValueChange = {
                        seconds = it
                        updateDuration()
                    },
                    label = { Text("Sekundy") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (isDistanceBased) {
            Row{
                TextField(
                    value = seriesDetail.distance,
                    onValueChange = { updatedDistance ->
                        onSeriesDetailChanged(seriesDetail.copy(distance = updatedDistance))
                    },
                    label = { Text("Jednostka 1") },
                    modifier = Modifier.weight(1f)
                )
                TextField(
                    value = seriesDetail.secondDistanceData,
                    onValueChange = { updatedSecondDistanceData ->
                        onSeriesDetailChanged(seriesDetail.copy(secondDistanceData = updatedSecondDistanceData))
                    },
                    label = { Text("Jednostka 2") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun NewExerciseScreen(
    navController: NavController,
    exercisesListState: MutableState<List<ExerciseEntity>>,
    exerciseViewModel: ExerciseViewModel
) {
    var exerciseName by remember { mutableStateOf("") }
    var isWeighted by remember { mutableStateOf(false) }
    var weight by remember { mutableStateOf("") }
    var isSeries by remember { mutableStateOf(false) }
    var seriesCount by remember { mutableStateOf("") }
    var isRepetitive by remember { mutableStateOf(false) }
    var repetitionsCount by remember { mutableStateOf("") }
    var isTimed by remember { mutableStateOf(false) }
    var duration by remember { mutableStateOf("") }
    var isDistanceBased by remember { mutableStateOf(false) }
    var distance by remember { mutableStateOf("") }
    var secondDistanceData by remember { mutableStateOf("") }

    val seriesDetails = remember { mutableStateListOf<ExerciseSeries>().also {
        if (isSeries) {
            val count = seriesCount.toIntOrNull() ?: 0
            for (i in 0 until count) {
                it.add(ExerciseSeries("", "", "", "", ""))
            }
        }
    } }

    // React to changes in seriesCount
    LaunchedEffect(key1 = seriesCount) {
        if (isSeries) {
            seriesDetails.clear()
            val count = seriesCount.toIntOrNull() ?: 0
            for (i in 0 until count) {
                seriesDetails.add(ExerciseSeries("", "", ""))
            }
        }
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())) {

        OutlinedTextField(
            value = exerciseName,
            onValueChange = { exerciseName = it },
            label = { Text("Nazwa ćwiczenia") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Series Checkbox
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isSeries,
                onCheckedChange = {
                    isSeries = it
                    if (!it) seriesDetails.clear() // Clear series details if not series-based
                }
            )
            Text("Wykonywane seriami")
        }

        // Weighted Checkbox
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isWeighted,
                onCheckedChange = { isWeighted = it }
            )
            Text("Ćwiczenie z ciężarem")
        }

        // Repetitive Checkbox
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isRepetitive,
                onCheckedChange = { isRepetitive = it }
            )
            Text("Wykonywane powtórzeniami")
        }

        // Timed Checkbox
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isTimed,
                onCheckedChange = { isTimed = it }
            )
            Text("Wykonywane czasowo")
        }

        // Distance Checkbox
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isDistanceBased,
                onCheckedChange = {
                    isDistanceBased = it
                    if (!it) {
                        // Reset the distance data if 'Wykonywane dystansowo' is unchecked
                        distance = ""
                        secondDistanceData = ""
                    }
                }
            )
            Text("Wykonywane dystansowo")
        }


        // Series count field
        if (isSeries) {
            OutlinedTextField(
                value = seriesCount,
                onValueChange = { seriesCount = it },
                label = { Text("Liczba serii") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Series details
        seriesDetails.forEachIndexed { index, seriesDetail ->
            SeriesDetailInput(
                seriesDetail = seriesDetail,
                seriesNumber = index + 1,
                onSeriesDetailChanged = { updatedSeriesDetail ->
                    seriesDetails[index] = updatedSeriesDetail
                },
                isWeighted = isWeighted,
                isRepetitive = isRepetitive,
                isTimed = isTimed,
                isDistanceBased = isDistanceBased,
                distance = seriesDetail.distance,
                secondDistanceData = seriesDetail.secondDistanceData,
                onDistanceChanged = { updatedDistance ->
                    // Handle the distance change
                    seriesDetails[index] = seriesDetails[index].copy(distance = updatedDistance)
                },
                onSecondDistanceDataChanged = { updatedSecondDistanceData ->
                    // Handle the second distance data change
                    seriesDetails[index] = seriesDetails[index].copy(secondDistanceData = updatedSecondDistanceData)
                }
            )
        }

        Button(onClick = {
            val newExerciseEntity = ExerciseEntity(
                name = exerciseName,
                isWeighted = isWeighted,
                weight = if (isWeighted && !isSeries) weight else "",
                isSeries = isSeries,
                seriesCount = if (isSeries) seriesCount else "",
                isRepetitive = isRepetitive,
                repetitionsCount = if (isRepetitive && !isSeries) repetitionsCount else "",
                isTimed = isTimed,
                duration = if (isTimed && !isSeries) duration else "",
                series = seriesDetails.toList()
            )
            exerciseViewModel.addExercise(newExerciseEntity)
            navController.navigate("exerciseAdded")
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Zapisz ćwiczenie")
        }
    }
}


@Composable
fun ExerciseAddedScreen(navController: NavController) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text("Pomyślnie dodano ćwiczenie", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = { navController.navigate("editExercises") }) {
            Text("Powrót")
        }
    }
}

@Composable
fun DetailScreen(navController: NavController, buttonName: String) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Text("Udało Ci się nacisnąć przycisk $buttonName", style = MaterialTheme.typography.headlineMedium)
    }
}


@Composable
fun SettingsScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Ustawienia",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Theme Mode Button
        Button(
            onClick = { /* TODO: Implement theme switching logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Motyw")
        }

        // Units Button
        Button(
            onClick = { /* TODO: Implement unit switching logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Jednostki")
        }

        // Language Button
        Button(
            onClick = { navController.navigate("language") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Język")
        }
    }
}

@Composable
fun LanguageScreen(navController: NavController, onLanguageChange: (String) -> Unit) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences(
        "com.example.forgeyourstrength.PREFERENCE_FILE_KEY",
        Context.MODE_PRIVATE
    )

    // State variables to hold the checkbox status
    var isPolish by remember { mutableStateOf(sharedPref.getString("language", "English") == "Polski") }
    var isEnglish by remember { mutableStateOf(sharedPref.getString("language", "English") == "English") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if(isPolish) "Wybierz język" else "Select language",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Polish Checkbox
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isPolish,
                onCheckedChange = { selected ->
                    if (selected) {
                        isPolish = true
                        isEnglish = false
                        sharedPref.edit().putString("language", "Polski").apply()
                        onLanguageChange("Polski")
                        // This is where you could trigger a recomposition
                    }
                }
            )
            Text(text = if(isPolish) "Polski" else "Polish")
        }

        // English Checkbox
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isEnglish,
                onCheckedChange = { selected ->
                    if (selected) {
                        isEnglish = true
                        isPolish = false
                        sharedPref.edit().putString("language", "English").apply()
                        onLanguageChange("English")
                        // This is where you could trigger a recomposition
                    }
                }
            )
            Text(text = if(isPolish) "Angielski" else "English")
        }

        // Back Button
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = if(isPolish) "Powrót" else "Back")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ForgeYourStrengthTheme {
        // Provide a dummy MutableState object for the preview
        val dummyState = remember { mutableStateOf(listOf<ExerciseEntity>()) }
        AppNavigation(exercisesListState = dummyState)
    }
}
