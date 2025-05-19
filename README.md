# Historical_Artifact_Catalog_Project
# JavaFX Desktop Application

This is a Java desktop application developed using **JavaFX** and managed with **Maven**. The application is packaged as a `.exe` file for easy execution on Windows systems.

---

## 📦 Application Features

- Developed with JavaFX for a modern, interactive GUI
- Uses Maven for dependency management and project structure
- Reads and writes JSON data 
- Ready-to-run `ArtifactCatalog.exe` file included
- Clean and modular codebase
- Easily extendable and maintainable structure

---
🔎 Notes for usage:

Examle Json Format:

{
  "artifacts": [
    {
      "artifactId": 1,                    // Unique ID of the artifact
      "artifactName": "String",           // Name of the artifact
      "category": "String",               // (Optional) Category or type, e.g., "Statue"
      "civilization": "String",           // The civilization it belongs to
      "discoveryLocation": "String",      // Place where it was discovered
      "composition": "String",            // Materials used
      "discoveryDate": "YYYY-MM-DD",      // Date it was discovered (ISO 8601 format)
      "currentPlace": "String",           // Where it is currently displayed/stored
      "width": Number,                    // Width in centimeters
      "length": Number,                   // Length in centimeters
      "height": Number,                   // Height in centimeters
      "weight": Number,                   // Weight in kilograms
      "tags": ["String", "..."]           // List of descriptive tags
    }
  ]
}

Field	Description:

category	 		:Optional — some artifacts may not have this. Handle it as nullable or optional.
discoveryDate	                :Use standard date format (e.g., "1799-07-15"). If you're using Jackson or Gson in Java, apply appropriate formatting.
tags	                        :This is an array (list) of strings, typically used for search/filter features.
weight, width, length, height	:Represent measurements; specify units (cm and kg) in the UI or documentation.


## 💻 System Requirements

| Component        | Required Version         |
|------------------|--------------------------|
| Java Development Kit (JDK) | 17 or higher   |
| Maven            | **3.8.6** or higher      |
| JavaFX           | **21**            	      |


> Note: Java does **not need to be installed** separately. The EXE includes a bundled runtime (if jpackage or a similar method was used).

---

## 🚀 How to Run

### 🖥️ Option 1: Use the `ArtifactCatalog.exe` File
Simply double-click the provided `ArtifactCatalog.exe` file to launch the application.

No installation required. This file is self-contained.

---

### ⚙️ Option 2: Build & Run Manually (Developers)

If you'd like to run or modify the project from source:

1. Install **Java JDK 21** and **Maven**
2. Open terminal and navigate to the project root
3. Run:

mvn clean install
mvn javafx:run


🛠️ Installer Information (Inno Setup)

An official Windows installer (.exe) has been created using Inno Setup for easier deployment of the application. The installer:

-Automatically copies the ArtifactCatalog.exe to the selected installation directory

-Creates necessary folders and adds optional Start Menu/Desktop shortcuts

-Includes a bundled Java Runtime (no separate installation required)

-Offers a clean and user-friendly setup process

📦 Installation Steps


1.Run the ArtifactCatalog_Installer.exe file

2.Follow the setup wizard instructions

3.(Optional) A desktop shortcut will be created after installation

4.Launch the application via the shortcut or from the installation folder


🔧 Notes

*Built with Inno Setup 6

*The installer includes the ArtifactCatalog.exe generated via jpackage

*Default installation directory is: C:\Program Files\ArtifactCatalog (modifiable during setup)

*No need to install Java separately — the runtime is bundled with the application


 
