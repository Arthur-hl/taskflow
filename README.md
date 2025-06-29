# TaskFlow - Trello-like Android App

TaskFlow is an Android application designed to manage tasks in a Trello-like interface. It allows users to create and manage boards, lists, and cards, with full support for offline functionality and synchronization with a remote API.

## Features
- **User Authentication:** Login and registration functionality integrated with the API at `https://yrkqw2-5000.csb.app/`.
- **Task Management:** Create, view, and manage boards, lists within boards, and cards within lists.
- **Offline Support:** Continue using the app without an internet connection. Changes made offline are synced to the server when connectivity is restored.
- **JWT Authentication:** Secure API calls with JSON Web Tokens for all endpoints beyond authentication.

## Installation
1. Clone this repository or download the source code.
2. Open the project in Android Studio.
3. Build and run the app on an emulator or physical device with Android API level 24 or higher.

## Usage
For detailed instructions on how to use the app, refer to the [User Manual](./docs/user_manual.md).

## Dependencies
- **Volley:** For network requests to the API.
- **Room Persistence Library:** For local database storage and offline support.
- **WorkManager:** For background synchronization of data with the API.

## License
This project is licensed under the MIT License - see the LICENSE file for details.
