# Travel App

Travel App is an Android application for planning trips, creating travel routes, booking hotels and flights, and synchronizing data with the cloud.

Travel App allows users to:

- create travel routes;
- search places;
- view users rewiews;
- book hotels and flights;
- save data offline;
- synchronize with Firebase.

## Features

- Google Sign-In
- Offline mode
- Route planning
- Hotel search
- Flight search
- Share and view users reviews
- View route on interactive map
- Support for two application interface languages (English, Ukrainian)
- Cloud synchronization

## Technologies

- Kotlin
- Jetpack Compose
- Room
- Firebase Authentication
- Cloud Firestore
- Google Maps API
- MVVM pattern

## Required API keys

Create `local.properties` for them

- For search places and view route on interactive map MAPS_API_KEY (Places API, Direction API) - Google Cloud Console
- For search flights AVIATIONSTACK_KEY - Aviationstack
- For search hotels RAPIDAPI_KEY - Xotelo
- For weather forecast on visit day WEATHER_API_KEY - WeatherAPI
