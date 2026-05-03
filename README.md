# Calculator

A comprehensive, all-in-one Android calculator application built with Kotlin. This app goes beyond simple arithmetic, providing a suite of specialized tools for geometry, unit conversion, health, finance, and more.

## 🚀 Features

### 🔢 General Calculator
*   Basic arithmetic operations.
*   Advanced mathematical functions (Trigonometry, Hyperbolic functions, Analysis).
*   Constants and Number Theory tools.

### 📐 Geometry Calculator
*   Calculate **Area, Perimeter, Volume, and Surface Area** for various shapes:
    *   2D: Circle, Square, Triangle, Rectangle, Sector, Regular Polygon.
    *   3D: Sphere, Cylinder, Pyramid, Prism, Cone.
*   **Visual Preview:** Real-time geometry preview and coordinate system visualization.

### 🔄 Unit Converter
*   Comprehensive conversion across multiple categories:
    *   **Common:** Length, Area, Weight, Volume, Temperature, Time.
    *   **Science/Engineering:** Angle, Speed, Pressure, Force, Energy, Optics.
    *   **Digital:** Data Storage.
    *   **Finance:** **Real-time Currency Converter** with live exchange rate updates via OkHttp.
    *   **Electrical:** Electric units.

### 🛠️ Special Calculators
*   **Health:** BMI (Body Mass Index) and BMR (Basal Metabolic Rate) calculator.
*   **Finance:** Discount and Loan/Mortgage calculators.
*   **Lifestyle:** Tip calculator with bill splitting, Fuel cost/efficiency calculator, and Unit Price comparison.
*   **Mathematics:** 
    *   **Sigma (Σ):** Summation calculator with custom expressions.
    *   **Combinatorics:** Permutations (P), Combinations (C), and Repetition Combinations (H).
    *   **Base Converter:** Convert between Decimal, Binary, and Hexadecimal.
    *   **Number Info:** Quick insights into number properties.
*   **Utility:** World Clock for timezone conversions.

### 📝 Math Notepad
*   Manage your calculations with a built-in notebook system to save and organize math notes.

## 🛠️ Technical Stack

*   **Language:** [Kotlin](https://kotlinlang.org/)
*   **UI Framework:** XML with [ConstraintLayout](https://developer.android.com/training/constraint-layout) and [Material Components](https://material.io/develop/android).
*   **Networking:** [OkHttp](https://square.github.io/okhttp/) for fetching real-time currency rates.
*   **Concurrency:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html).
*   **Security:** [Conscrypt](https://github.com/google/conscrypt) for modern TLS 1.2+ support on older Android versions.
*   **Compatibility:** Android 5.0 (API 21) and above.

## 🏗️ Project Structure

*   `:app` - Main Android application module.
    *   `com.liferlighdow.calculator` - Base package.
    *   `GeometryPreviewView.kt` / `CoordinateSystemView.kt` - Custom drawing views.
    *   `MainActivity.kt` - Main entry point and basic calculator.
    *   `SpecialCalculatorsActivity.kt` - Host for specialized calculation tools.

## ⚙️ Build Requirements

*   Android Studio Ladybug (2024.2.1) or newer.
*   JDK 11.
*   Android SDK 36 (Compile SDK).

---
Developed by liferlighdow
