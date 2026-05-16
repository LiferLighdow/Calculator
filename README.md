# 📱 Calculator - All-in-One Android Tool

[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.1.0-blue.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-5.0+-green.svg)](https://developer.android.com/)

A comprehensive, all-in-one Android calculator application built with Kotlin. This app goes beyond simple arithmetic, providing a suite of specialized tools for geometry, unit conversion, health, finance, and more.

[**⬇️ Download Latest APK**](https://github.com/LiferLighdow/Calculator/releases/latest)

---

## 🚀 Features

### 🔢 General Calculator
*   **Basic Operations:** Addition, subtraction, multiplication, and division.
*   **Scientific functions:** Trigonometry (Sin, Cos, Tan), Hyperbolic functions, and Analysis.
*   **Constants:** Access to common mathematical constants and number theory tools.

### 📐 Geometry Calculator
*   Calculate **Area, Perimeter, Volume, and Surface Area** for various shapes:
    *   **2D Shapes:** Circle, Square, Triangle, Rectangle, Sector, Regular Polygon.
    *   **3D Shapes:** Sphere, Cylinder, Pyramid, Prism, Cone.
*   **Visual Preview:** Real-time geometry preview and coordinate system visualization using custom views (`GeometryPreviewView`, `CoordinateSystemView`).

### 🔄 Unit Converter
*   **Common:** Length, Area, Weight, Volume, Temperature, Time.
*   **Science/Engineering:** Angle, Speed, Pressure, Force, Energy, Optics.
*   **Digital:** Data Storage (B, KB, MB, GB, etc.).
*   **Finance:** **Real-time Currency Converter** with live exchange rate updates via OkHttp.
*   **Electrical:** Various electric units.

### 🛠️ Special Calculators
*   **Health:** BMI (Body Mass Index) and BMR (Basal Metabolic Rate).
*   **Finance:** Discount and Loan/Mortgage calculators.
*   **Lifestyle:** Tip calculator (with bill splitting), Fuel cost/efficiency, and Unit Price comparison.
*   **Mathematics:** 
    *   **Sigma (Σ):** Summation calculator with custom expressions.
    *   **Combinatorics:** Permutations (P), Combinations (C), and Repetition Combinations (H).
    *   **Base Converter:** Decimal, Binary, and Hexadecimal.
    *   **Number Info:** Quick insights into number properties.
*   **Utility:** World Clock for timezone conversions.

### 📝 Math Notepad
*   Manage your calculations with a built-in notebook system to save and organize math notes.

---

## 🛠️ Technical Stack

*   **Language:** [Kotlin](https://kotlinlang.org/)
*   **UI Framework:** XML with [ConstraintLayout](https://developer.android.com/training/constraint-layout) and [Material Components](https://material.io/develop/android).
*   **Networking:** [OkHttp](https://square.github.io/okhttp/) for fetching real-time currency rates.
*   **Concurrency:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html).
*   **Security:** [Conscrypt](https://github.com/google/conscrypt) for modern TLS 1.2+ support on Android 5.0.
*   **Compatibility:** Android 5.0 (API 21) and above.

---

## 🏗️ Project Structure

*   `:app` - Main Android application module.
    *   `com.liferlighdow.calculator` - Base package.
    *   `GeometryPreviewView.kt` / `CoordinateSystemView.kt` - Custom drawing components.
    *   `MainActivity.kt` - Main entry point and basic calculator.
    *   `SpecialCalculatorsActivity.kt` - Host for specialized calculation tools.

---

## ⚙️ Build Requirements

1.  **Android Studio:** Ladybug (2024.2.1) or newer recommended.
2.  **JDK:** 11 or higher.
3.  **Android SDK:** Compile SDK 36.

### How to Build
```bash
# Clone the repository
git clone https://github.com/LiferLighdow/Calculator.git

# Navigate to directory
cd Calculator

# Build the APK
./gradlew assembleDebug
```

---

## 📄 License

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

Developed with ❤️ by [LiferLighdow](https://github.com/LiferLighdow)
