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

class MainActivity : ComponentActivity() {
    private val exercisesListState = mutableStateOf(listOf<Exercise>())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ForgeYourStrengthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(exercisesListState)
                }
            }
        }
    }
}

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

@Composable
fun AppNavigation(exercisesListState: MutableState<List<Exercise>>) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen(navController) }
        composable("training") { DetailScreen(navController, "Rozpocznij trening") }
        composable("createPlan") { DetailScreen(navController, "Utwórz plan treningowy") }
        composable("editExercises") { EditExercisesScreen(navController) }
        composable("addAssumptions") { DetailScreen(navController, "Dodaj założenia planu treningowego") }
        composable("removeExercise") { DetailScreen(navController, "Usuń ćwiczenie") }
        composable("editExercise") { DetailScreen(navController, "Edytuj ćwiczenie") }
        composable("exerciseAdded") { ExerciseAddedScreen(navController) }
        composable("newExercise") { NewExerciseScreen(navController, exercisesListState) }
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
fun SeriesDetailInput(seriesDetail: ExerciseSeries, seriesNumber: Int, onSeriesDetailChanged: (ExerciseSeries) -> Unit) {
    var weight by remember { mutableStateOf(seriesDetail.weight) }
    var repetitionsCount by remember { mutableStateOf(seriesDetail.repetitionsCount) }
    var duration by remember { mutableStateOf(seriesDetail.duration) }

    Column {
        Text("Seria $seriesNumber")
        TextField(
            value = weight,
            onValueChange = {
                weight = it
                onSeriesDetailChanged(seriesDetail.copy(weight = it))
            },
            label = { Text("Ciężar") }
        )
        TextField(
            value = repetitionsCount,
            onValueChange = {
                repetitionsCount = it
                onSeriesDetailChanged(seriesDetail.copy(repetitionsCount = it))
            },
            label = { Text("Ilość powtórzeń") }
        )
        TextField(
            value = duration,
            onValueChange = {
                duration = it
                onSeriesDetailChanged(seriesDetail.copy(duration = it))
            },
            label = { Text("Czas trwania (hh:mm:ss)") }
        )
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

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    ForgeYourStrengthTheme {
        // Provide a dummy MutableState object for the preview
        AppNavigation(mutableStateOf(listOf()))
    }
}
