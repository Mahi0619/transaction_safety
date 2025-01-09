# Transaction Safety App


## Project Overview


This project implements a **transaction management system** using the **MVVM architecture** in Android. It contains features like **user login**, **fetching transaction details**, and **biometric authentication**. The app communicates with a remote server via **Retrofit** and manages data using **ViewModels**, **LiveData**, and **Kotlin Coroutines** for asynchronous tasks.


---


## Architecture


The application follows the **MVVM (Model-View-ViewModel)** design pattern:


- **Model**: Represents the data and the business logic. In this project, this is handled by **LoginBean**, **LoginRequest**, and **Transaction Details**.
- **View**: Represents the UI. In this case, it includes activities like **UserActivity** where the UI components interact with the ViewModel.
- **ViewModel**: Manages UI-related data in a lifecycle-conscious way and communicates with the model layer. It holds the state and handles all business logic (e.g., login and transaction fetching).


---


## Key Features


### User Login:


- The app supports both **username/password login** and **biometric authentication**.
- Upon successful login, the user is directed to the **DashboardActivity**.
- The login process is performed through a **ViewModel** that fetches data from an API.


### Transaction Details:


- The user can view a list of transactions fetched from a remote server.
- The transactions are displayed in a list format on the main screen.
- Stored transactions data into Room DataBase.


### Biometric Authentication:


- If supported, the app can authenticate users via biometric methods (fingerprint, face recognition) using **BiometricPrompt**.


---


## Technologies Used


- **Kotlin**: The programming language used for development.
- **MVVM architecture**: Used to separate concerns between UI, business logic, and data management.
- **Retrofit**: For making network calls and handling responses from the API.
- **Dagger Hilt**: For dependency injection.
- **Kotlin Coroutines**: For handling background operations asynchronously.
- **Room**: Used for local data persistence (if needed).
- **JUnit/Mockito**: For writing unit and UI tests.
- **Biometric Authentication**: Using **BiometricPrompt** for secure login.


---


## Setup Instructions


1. Clone the repository:
    ```bash
    git clone https://github.com/Mahi0619/transaction_safety.git
    ```


2. Open the project in **Android Studio**.


3. Set up your **API base URL** and other configurations in the `build.gradle` file.


4. Sync the project with Gradle files.


---


## Build Configuration


The app uses **Build Flavors** for different environments:


- **releaseFlavor**: For production, with different API URLs and versioning.
- **Debug**: For development purposes, without URL overrides.


Signatures for signing the release build are specified in the `signingConfigs` block.


---


## Dependencies


The project uses the following major dependencies:


- **Dagger Hilt** for dependency injection.
- **Retrofit** and **Gson** for network calls and JSON parsing.
- **Biometric Prompt** for biometric authentication.
- **Kotlin Coroutines** for handling background operations.
- **JUnit** and **Mockito** for unit testing.
- **Room** for local storage (if applicable).
- **Lottie** for animations.


---


## Testing


### Unit Tests


Unit tests are written using **JUnit** and **Mockito**. Tests are written to cover the following scenarios:


- **User login**.
- **Transaction details fetching(Local & Remotely**.
- **Error handling** during API calls.


### UI Tests


UI tests are written using **Espresso** for verifying the UI components and user interactions.


### Mocking APIs


Mocked responses are used for testing various API states such as **Success**, **Failure**, and **Loading** states.


---


## Conclusion


This project demonstrates how to build an Android app using **MVVM**, **Retrofit**, and **Biometric Authentication**. The app is structured with a focus on testability, maintainability, and scalability. Dependency injection via **Hilt** helps in decoupling the components and managing dependencies effectively.


---


## License


This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.


