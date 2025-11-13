# 🥗 Nutrition Planner Pro

A modern JavaFX application for tracking daily nutrition with MySQL database integration.

## Features
- 🔐 User authentication (Login/Signup)
- 🍽️ Track meals (Breakfast, Lunch, Dinner, Snacks)
- 📊 Nutrition analytics with charts
- 🎯 Goal-based calorie recommendations
- 💾 MySQL database for data persistence
- 📈 Daily insights and recommendations

## Technologies
- **Frontend:** JavaFX 23
- **Backend:** Java 21
- **Database:** MySQL 8.0
- **JDBC:** MySQL Connector

## Requirements
- Java 21 or higher
- JavaFX 23 SDK
- MySQL 8.0
- MySQL Connector JAR

## Database Setup
```sql
CREATE DATABASE javadb;
USE javadb;

CREATE TABLE users (
    user_id INT AUTO_INCREMENT PRIMARY KEY,
    user_name VARCHAR(100) NOT NULL,
    password VARCHAR(100) NOT NULL,
    weight DOUBLE NOT NULL,
    height DOUBLE NOT NULL,
    goal VARCHAR(50) NOT NULL,
    target_calories DOUBLE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE food_items (
    food_id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    meal_type VARCHAR(50) NOT NULL,
    food_name VARCHAR(100) NOT NULL,
    calories DOUBLE NOT NULL,
    protein DOUBLE NOT NULL,
    carbs DOUBLE NOT NULL,
    fats DOUBLE NOT NULL,
    meal_date DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);
```

## How to Run
1. Update database credentials in `NUTRITIONPLANNER.java`
2. Compile: `javac --module-path "path/to/javafx/lib" --add-modules javafx.controls NUTRITIONPLANNER.java`
3. Run: `java --module-path "path/to/javafx/lib" --add-modules javafx.controls NUTRITIONPLANNER`

## Screenshots
(Add screenshots here)

## Author
Your Name

## License
MIT License
```

---

## **Step 5: Create .gitignore File**

Create a file named `.gitignore` in your project folder:
```
# Compiled class files
*.class

# Log files
*.log

# Package Files
*.jar
*.war
*.nar
*.ear
*.zip
*.tar.gz
*.rar

# Virtual machine crash logs
hs_err_pid*

# IDE specific files
.idea/
*.iml
.vscode/
.settings/
.project
.classpath

# OS specific files
.DS_Store
Thumbs.db

# Build directories
out/
target/
build/
bin/

# Sensitive information (don't commit passwords!)
**/Database.java
config.properties
