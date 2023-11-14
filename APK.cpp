#include <iostream>
#include <fstream>
#include <vector>
#include <string>

using namespace std;

struct Exercise {
    string name;
    int sets;
    int reps;
    double weight;
};

void savePlanToFile(const vector<Exercise>& plan, const string& fileName) {
    ofstream file(fileName);
    if (file.is_open()) {
        for (const Exercise& exercise : plan) {
            file << exercise.name << "," << exercise.sets << "," << exercise.reps << "," << exercise.weight << endl;
        }
        file.close();
        cout << "Plan treningowy został zapisany do pliku." << endl;
    } else {
        cerr << "Nie można otworzyć pliku do zapisu." << endl;
    }
}

vector<Exercise> loadPlanFromFile(const string& fileName) {
    vector<Exercise> plan;
    ifstream file(fileName);
    if (file.is_open()) {
        string line;
        while (getline(file, line)) {
            Exercise exercise;
            size_t pos = 0;
            string token;
            int count = 0;
            while ((pos = line.find(',')) != string::npos) {
                token = line.substr(0, pos);
                switch (count) {
                    case 0:
                        exercise.name = token;
                        break;
                    case 1:
                        exercise.sets = stoi(token);
                        break;
                    case 2:
                        exercise.reps = stoi(token);
                        break;
                    case 3:
                        exercise.weight = stod(token);
                        break;
                }
                line.erase(0, pos + 1);
                count++;
            }
            plan.push_back(exercise);
        }
        file.close();
        cout << "Plan treningowy został wczytany z pliku." << endl;
    } else {
        cerr << "Nie można otworzyć pliku do odczytu." << endl;
    }
    return plan;
}

int main() {
    vector<Exercise> trainingPlan;

    while (true) {
        cout << "1. Dodaj ćwiczenie do planu" << endl;
        cout << "2. Zapisz plan treningowy do pliku" << endl;
        cout << "3. Wczytaj plan treningowy z pliku" << endl;
        cout << "4. Wyjście" << endl;

        int choice;
        cin >> choice;

        if (choice == 1) {
            Exercise exercise;
            cout << "Nazwa ćwiczenia: ";
            cin.ignore();
            getline(cin, exercise.name);
            cout << "Liczba serii: ";
            cin >> exercise.sets;
            cout << "Liczba powtórzeń: ";
            cin >> exercise.reps;
            cout << "Ciężar (kg): ";
            cin >> exercise.weight;
            trainingPlan.push_back(exercise);
        } else if (choice == 2) {
            savePlanToFile(trainingPlan, "training_plan.txt");
        } else if (choice == 3) {
            trainingPlan = loadPlanFromFile("training_plan.txt");
        } else if (choice == 4) {
            break;
        }
    }

    return 0;
}
