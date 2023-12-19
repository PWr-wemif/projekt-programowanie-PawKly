package com.example.forgeyourstrength

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignments
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.forgeyourstrength.ui.theme.ForgeYourStrengthTheme
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ForgeYourStrengthTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Użyj State, aby śledzić, który przycisk został kliknięty
                    var isTrainingPlansClicked by remember { mutableStateOf(false) }
                    var isExerciseDatabaseClicked by remember { mutableStateOf(false) }
                    var isStartTrainingClicked by remember { mutableStateOf(false) }
                    var isAddConditionsClicked by remember { mutableStateOf(false) }

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Greeting("Android")
                        ClickableButton(onClick = { isTrainingPlansClicked = true }, text = "Plany treningowe")
                        ClickableButton(onClick = { isExerciseDatabaseClicked = true }, text = "Baza ćwiczeń")
                        ClickableButton(onClick = { isStartTrainingClicked = true }, text = "Rozpocznij trening")
                        ClickableButton(onClick = { isAddConditionsClicked = true }, text = "Dodaj warunki treningowe")

                        // Wyświetl ekran w zależności od klikniętego przycisku
                        if (isTrainingPlansClicked) {
                            // Tutaj dodaj kod, który ma się wykonać po kliknięciu w "Plany treningowe"
                            // Na razie wyświetlamy tylko komunikat w logach
                            println("Przejście do planów treningowych")
                        } else if (isExerciseDatabaseClicked) {
                            // Tutaj dodaj kod, który ma się wykonać po kliknięciu w "Baza ćwiczeń"
                            println("Przejście do bazy ćwiczeń")
                        } else if (isStartTrainingClicked) {
                            // Tutaj dodaj kod, który ma się wykonać po kliknięciu w "Rozpocznij trening"
                            println("Rozpoczęcie treningu")
                        } else if (isAddConditionsClicked) {
                            // Tutaj dodaj kod, który ma się wykonać po kliknięciu w "Dodaj warunki treningowe"
                            println("Dodawanie warunków treningowych")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ClickableButton(onClick: () -> Unit, text: String) {
    Button(
        onClick = {
            println("Udało się: $text")
            onClick.invoke() // Wywołaj dostarczoną funkcję po kliknięciu guzika
        },
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text)
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ForgeYourStrengthTheme {
        Greeting("Android")
    }
}