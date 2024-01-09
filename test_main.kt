package com.example.forgeyourstrength

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.forgeyourstrength.ui.theme.ForgeYourStrengthTheme
import androidx.navigation.NavController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import java.util.Locale
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel


data class ExerciseSeries(
    var weight: String,
    var repetitionsCount: String,
    var duration: String
)

data class Exercise(
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

// LanguageViewModel.kt
class LanguageViewModel : ViewModel() {
    private val _language = mutableStateOf("English")
    val language: State<String> = _language

    // Define strings based on the selected language
    val strings = mapOf(
        "startTraining" to if (language.value == "Polski") "Rozpocznij trening" else "Start Training",
        // ... other strings
    )

    fun setLanguage(newLanguage: String) {
        _language.value = newLanguage
        // Update strings when language changes
        // ...
    }
}

// Provide this ViewModel to your composables
val LocalLanguageViewModel = compositionLocalOf<LanguageViewModel> { error("No LanguageViewModel provided") }

// MainActivity.kt
class MainActivity : ComponentActivity() {
    private lateinit var sharedPref: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPref = getPreferences(Context.MODE_PRIVATE)
        val defaultLanguage = sharedPref.getString("language", "en") ?: "en"
        val currentLanguage = getCurrentLanguage()

        // Check if the default language is not equal to the current language
        // If not equal, then set the default language
        if (defaultLanguage != currentLanguage) {
            setLanguage(defaultLanguage)
        }

        setContent {
            ForgeYourStrengthTheme {
                // Remember the exercises list state
                val exercisesListState = remember { mutableStateOf(listOf<Exercise>()) }
                // Obtain an instance of LanguageViewModel
                val languageViewModel = viewModel<LanguageViewModel>().apply {
                    setLanguage(defaultLanguage)
                }
                // Invoke AppNavigation composable function
                AppNavigation(exercisesListState, languageViewModel, this@MainActivity::restartActivity)
            }
        }
    }

    private fun setLanguage(language: String) {
        // Set the new default locale
        val locale = if (language == "Polski") Locale("pl", "PL") else Locale("en", "US")
        Locale.setDefault(locale)

        // Update the configuration with the new locale
        val config = resources.configuration.apply {
            setLocale(locale)
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                @Suppress("DEPRECATION")
                resources.updateConfiguration(this, resources.displayMetrics)
            }
        }

        // Apply the configuration
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            createConfigurationContext(config)
        }

        // Save the new language preference
        with(sharedPref.edit()) {
            putString("language", language)
            apply()
        }

        // Restart the activity to apply the new language preference
        restartActivity()
    }

    private fun getCurrentLanguage(): String {
        // Retrieve the current language from the configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            resources.configuration.locales[0].language
        } else {
            @Suppress("DEPRECATION")
            resources.configuration.locale.language
        }
    }

    private fun restartActivity() {
        // Create an intent to restart the MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
        // Start the intent
        startActivity(intent)
        // Finish the current instance of the activity
        finish()
    }
}


@Composable
fun AppNavigation(
    exercisesListState: MutableState<List<Exercise>>,
    languageViewModel: LanguageViewModel,
    restartActivity: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(navController, languageViewModel)
        }
        composable("training") {
            DetailScreen(navController, languageViewModel.strings["startTraining"] ?: "Start Training")
        }
        composable("createPlan") {
            DetailScreen(navController, languageViewModel.strings["createPlan"] ?: "Create Training Plan")
        }
        composable("addAssumptions") {
            DetailScreen(navController, languageViewModel.strings["addAssumptions"] ?: "Add Assumptions")
        }
        composable("removeExercise") {
            DetailScreen(navController, languageViewModel.strings["removeExercise"] ?: "Remove Exercise")
        }
        composable("editExercise") {
            DetailScreen(navController, languageViewModel.strings["editExercise"] ?: "Edit Exercise")
        }
        composable("editExercises") {
            EditExercisesScreen(navController)
        }
        composable("exerciseAdded") {
            ExerciseAddedScreen(navController)
        }
        composable("newExercise") {
            NewExerciseScreen(navController, exercisesListState)
        }
        composable("settings") {
            SettingsScreen(navController)
        }
        composable("language") {
            LanguageScreen(navController, languageViewModel, restartActivity)
        }
    }
}


@Composable
fun MainScreen(navController: NavController, languageViewModel: LanguageViewModel) {
    val currentLanguage = languageViewModel.language.value

    // Assuming you have defined these strings somewhere in your resources
    val startTrainingText = if (currentLanguage == "Polski") "Rozpocznij trening" else "Start Training"
    val createPlanText = if (currentLanguage == "Polski") "Utwórz plan treningowy" else "Create Training Plan"
    val editExercisesText = if (currentLanguage == "Polski") "Edytuj ćwiczenia" else "Edit Exercises"
    val addAssumptionsText = if (currentLanguage == "Polski") "Dodaj założenia" else "Add Assumptions"
    val settingsText = if (currentLanguage == "Polski") "Ustawienia" else "Settings"

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
            Text(startTrainingText)
        }
        Button(
            onClick = { navController.navigate("createPlan") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(createPlanText)
        }
        Button(
            onClick = { navController.navigate("editExercises") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(editExercisesText)
        }
        Button(
            onClick = { navController.navigate("addAssumptions") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(addAssumptionsText)
        }
        Button(
            onClick = { navController.navigate("settings") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text(settingsText)
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
fun NewExerciseScreen(navController: NavController, exercisesListState: MutableState<List<Exercise>>) {
    var exerciseName by remember { mutableStateOf("") }
    var isWeighted by remember { mutableStateOf(false) }
    var weight by remember { mutableStateOf("") }
    var isSeries by remember { mutableStateOf(false) }
    var seriesCount by remember { mutableStateOf("") }
    var isRepetitive by remember { mutableStateOf(false) }
    var repetitionsCount by remember { mutableStateOf("") }
    var isTimed by remember { mutableStateOf(false) }
    var duration by remember { mutableStateOf("") }

    val seriesDetails = remember { mutableStateListOf<ExerciseSeries>() }

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
                isTimed = isTimed
            )
        }

        Button(onClick = {
            val newExercise = Exercise(
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
            exercisesListState.value = exercisesListState.value + newExercise
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
fun SeriesDetailInput(
    seriesDetail: ExerciseSeries,
    seriesNumber: Int,
    onSeriesDetailChanged: (ExerciseSeries) -> Unit,
    isWeighted: Boolean,
    isRepetitive: Boolean,
    isTimed: Boolean
) {
    Column {
        Text("Seria $seriesNumber")
        if (isWeighted) {
            TextField(
                value = seriesDetail.weight,
                onValueChange = { updatedWeight ->
                    onSeriesDetailChanged(seriesDetail.copy(weight = updatedWeight))
                },
                label = { Text("Ciężar") }
            )
        }
        if (isRepetitive) {
            TextField(
                value = seriesDetail.repetitionsCount,
                onValueChange = { updatedRepetitions ->
                    onSeriesDetailChanged(seriesDetail.copy(repetitionsCount = updatedRepetitions))
                },
                label = { Text("Ilość powtórzeń") }
            )
        }
        if (isTimed) {
            TextField(
                value = seriesDetail.duration,
                onValueChange = { updatedDuration ->
                    onSeriesDetailChanged(seriesDetail.copy(duration = updatedDuration))
                },
                label = { Text("Czas trwania (hh:mm:ss)") }
            )
        }
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
            Text("Tryb")
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
fun LanguageScreen(navController: NavController, languageViewModel: LanguageViewModel, restartActivity: () -> Unit) {
    val context = LocalContext.current // Get the current local context
    val currentLanguage = languageViewModel.language.value

    var isPolish by remember { mutableStateOf(currentLanguage == "Polski") }
    var isEnglish by remember { mutableStateOf(currentLanguage == "English") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isPolish) "Wybierz język" else "Select language",
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
                        languageViewModel.setLanguage("Polski")
                        updateLanguagePreferenceAndRestart("Polski", context, restartActivity)
                    }
                }
            )
            Text(if (isPolish) "Polski" else "Polish")
        }

        // English Checkbox
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isEnglish,
                onCheckedChange = { selected ->
                    if (selected) {
                        isEnglish = true
                        isPolish = false
                        languageViewModel.setLanguage("English")
                        updateLanguagePreferenceAndRestart("English", context, restartActivity)
                    }
                }
            )
            Text(if (isEnglish) "Angielski" else "English")
        }

        // Back Button
        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = if (isPolish) "Powrót" else "Back")
        }
    }
}

// Helper function to update the shared preferences and restart the activity
private fun updateLanguagePreferenceAndRestart(language: String, context: Context, restartActivity: () -> Unit) {
    val sharedPref = context.getSharedPreferences(
        "com.example.forgeyourstrength.PREFERENCE_FILE_KEY",
        Context.MODE_PRIVATE
    )
    with(sharedPref.edit()) {
        putString("language", language)
        apply()
    }
    // Restart activity to apply language change
    restartActivity()
}


// Extension function on Activity to restart the current activity
private fun Activity.restartActivity() {
    val intent = Intent(this, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    }
    startActivity(intent)
    finish()
}


@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ForgeYourStrengthTheme {
        // Provide a dummy MutableState object for the preview
        val dummyState = remember { mutableStateOf(listOf<Exercise>()) }
        val dummyViewModel = LanguageViewModel()
        AppNavigation(exercisesListState = dummyState, languageViewModel = dummyViewModel) {
            // Dummy restart activity function for preview
        }
    }
}
