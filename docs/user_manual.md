# TaskFlow User Manual

Welcome to TaskFlow, a Trello-like Android app designed for task management. This manual provides step-by-step instructions on how to use the app to manage your tasks effectively.

## Table of Contents
- [Getting Started](#getting-started)
- [Login and Registration](#login-and-registration)
- [Managing Boards](#managing-boards)
- [Managing Lists](#managing-lists)
- [Managing Cards](#managing-cards)
- [Offline Functionality](#offline-functionality)
- [Troubleshooting](#troubleshooting)

## Getting Started
1. **Install the App:** Download and install TaskFlow from the provided APK or build it from source using Android Studio.
2. **Launch the App:** Open TaskFlow on your Android device. You'll be greeted with the login screen if you're not already logged in.

## Login and Registration
- **Login:**
  1. Enter your username and password on the login screen.
  2. Tap the "Login" button to access your dashboard.
  3. If you don't have an account, tap on "Register" to create one.
- **Registration:**
  1. On the registration screen, fill in the required fields (username, email, password).
  2. Tap the "Register" button to create your account and return to the login screen.

**Note:** Login requires an internet connection. Once logged in, you can use the app offline.

## Managing Boards
Boards are the top-level containers for your tasks.
- **Viewing Boards:** After logging in, you'll see a list of your boards on the dashboard.
- **Creating a Board:**
  1. Tap the "Add Board" button on the dashboard.
  2. Enter a title for the new board in the dialog.
  3. Tap "Add" to create the board.
- **Opening a Board:** Tap on a board to view its lists.

## Managing Lists
Lists organize tasks within a board.
- **Viewing Lists:** Inside a board, you'll see a horizontal list of lists.
- **Creating a List:**
  1. Tap the "Add List" button within a board.
  2. Enter a title for the new list in the dialog.
  3. Tap "Add" to create the list.
- **Opening a List:** Tap on a list to view its cards.

## Managing Cards
Cards represent individual tasks or items within a list.
- **Viewing Cards:** Inside a list, you'll see a vertical list of cards.
- **Creating a Card:**
  1. Tap the "Add Card" button within a list.
  2. Enter a title and optional description for the new card in the dialog.
  3. Tap "Add" to create the card.
- **Viewing Card Details:** Tap on a card to see more details (currently shows a toast message with the card title).

## Offline Functionality
TaskFlow supports offline usage for logged-in users:
- **Working Offline:** If you're offline, the app will load data from the local database. An "Offline" indicator will appear at the top of the screen.
- **Making Changes Offline:** You can create boards, lists, and cards while offline. These changes are saved locally and queued for synchronization.
- **Synchronization:** When an internet connection is restored, the app automatically syncs your offline changes to the server in the background. Data from the server is also updated in your local database when online.

## Troubleshooting
- **Login Issues:** Ensure you have an internet connection for the initial login. If you've forgotten your password, contact support (feature to be implemented).
- **Sync Issues:** If changes made offline aren't syncing, check your internet connection. The app will attempt to sync automatically when online.
- **Data Not Showing:** If boards, lists, or cards are not displaying, try refreshing by navigating back and forth between screens. If the issue persists, ensure you're logged in.

For further assistance, please contact the developer or refer to the GitHub repository for updates and issues.

---
*Last Updated: [Insert Date Here]*
