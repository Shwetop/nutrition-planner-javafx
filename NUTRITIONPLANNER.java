import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class NUTRITIONPLANNER extends Application {
    
    private String userName;
    private double weight;
    private double height;
    private String goal;
    private double targetCalories;
    private Map<String, List<FoodItem>> dailyMeals = new HashMap<>();
    private int currentUserId = -1;
    
    // Color scheme
    private static final String PRIMARY_COLOR = "#6366f1";
    private static final String SECONDARY_COLOR = "#8b5cf6";
    private static final String SUCCESS_COLOR = "#10b981";
    private static final String WARNING_COLOR = "#f59e0b";
    private static final String DANGER_COLOR = "#ef4444";
    private static final String BACKGROUND_COLOR = "#f8fafc";
    
    @Override
    public void start(Stage primaryStage) {
        initializeMeals();
        primaryStage.setTitle("Nutrition Planner Pro");
        primaryStage.setScene(createLoginScene(primaryStage));
        primaryStage.show();
    }
    
    private void initializeMeals() {
        dailyMeals.put("Breakfast", new ArrayList<>());
        dailyMeals.put("Lunch", new ArrayList<>());
        dailyMeals.put("Dinner", new ArrayList<>());
        dailyMeals.put("Snacks", new ArrayList<>());
    }
    
    public static class Database {
        private static final String DB_DRIVER = "com.mysql.cj.jdbc.Driver";
        private static final String DB_URL = "jdbc:mysql://localhost:3306/javadb";
        private static final String DB_USER = "root";
        private static final String DB_PASSWORD = "shwet123";

        public static Connection getConnection() throws SQLException {
            try {
                Class.forName(DB_DRIVER);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        }
    }
    
    // NEW: Login Scene
    private Scene createLoginScene(Stage stage) {
        StackPane root = new StackPane();
        
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#667eea")),
            new Stop(1, Color.web("#764ba2"))
        );
        
        Rectangle bg = new Rectangle(600, 700);
        bg.setFill(gradient);
        
        VBox card = new VBox(25);
        card.setMaxWidth(450);
        card.setPadding(new Insets(40));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 10);");
        
        Circle icon = new Circle(40);
        icon.setFill(Color.web(PRIMARY_COLOR));
        Label iconText = new Label("🥗");
        iconText.setFont(Font.font(40));
        StackPane iconPane = new StackPane(icon, iconText);
        
        Label title = new Label("Welcome Back!");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setStyle("-fx-text-fill: #1e293b;");
        
        Label subtitle = new Label("Login to continue your health journey");
        subtitle.setFont(Font.font(14));
        subtitle.setStyle("-fx-text-fill: #64748b;");
        
        VBox form = new VBox(15);
        form.setAlignment(Pos.CENTER);
        
        TextField userIdField = createStyledTextField("👤 User ID");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("🔒 Password");
        passwordField.setStyle(
            "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 10; " +
            "-fx-border-color: #e2e8f0; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-color: #f8fafc;"
        );
        passwordField.setPrefWidth(350);
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: " + DANGER_COLOR + "; -fx-font-weight: bold;");
        
        Button loginBtn = createGradientButton("Login →");
        loginBtn.setPrefWidth(350);
        
        loginBtn.setOnAction(e -> {
            String userId = userIdField.getText();
            String password = passwordField.getText();
            
            if (userId.isEmpty() || password.isEmpty()) {
                errorLabel.setText("⚠️ Please fill all fields!");
                return;
            }
            
            try (Connection conn = Database.getConnection()) {
                String sql = "SELECT user_id, user_name, weight, height, goal, target_calories FROM users WHERE user_id = ? AND password = ?";
                java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, Integer.parseInt(userId));
                pstmt.setString(2, password);
                java.sql.ResultSet rs = pstmt.executeQuery();
                
                if (rs.next()) {
                    currentUserId = rs.getInt("user_id");
                    userName = rs.getString("user_name");
                    weight = rs.getDouble("weight");
                    height = rs.getDouble("height");
                    goal = rs.getString("goal");
                    targetCalories = rs.getDouble("target_calories");
                    
                    initializeMeals();
                    loadMealsFromDatabase();
                    
                    stage.setScene(createMainScene(stage));
                } else {
                    errorLabel.setText("⚠️ Invalid User ID or Password!");
                }
            } catch (NumberFormatException ex) {
                errorLabel.setText("⚠️ User ID must be a number!");
            } catch (SQLException ex) {
                errorLabel.setText("⚠️ Database error: " + ex.getMessage());
                ex.printStackTrace();
            }
        });
        
        Label signupLabel = new Label("Don't have an account?");
        signupLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 13px;");
        
        Button signupBtn = new Button("Sign Up");
        signupBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY_COLOR + "; -fx-font-weight: bold; -fx-cursor: hand;");
        signupBtn.setOnAction(e -> stage.setScene(createSignUpScene(stage)));
        
        HBox signupBox = new HBox(5, signupLabel, signupBtn);
        signupBox.setAlignment(Pos.CENTER);
        
        form.getChildren().addAll(userIdField, passwordField);
        card.getChildren().addAll(iconPane, title, subtitle, form, errorLabel, loginBtn, signupBox);
        root.getChildren().addAll(bg, card);
        
        return new Scene(root, 600, 700);
    }
    
    // NEW: Sign Up Scene
    private Scene createSignUpScene(Stage stage) {
        StackPane root = new StackPane();
        
        LinearGradient gradient = new LinearGradient(
            0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#667eea")),
            new Stop(1, Color.web("#764ba2"))
        );
        
        Rectangle bg = new Rectangle(600, 700);
        bg.setFill(gradient);
        
        VBox card = new VBox(20);
        card.setMaxWidth(450);
        card.setPadding(new Insets(30));
        card.setAlignment(Pos.CENTER);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 10);");
        
        Circle icon = new Circle(35);
        icon.setFill(Color.web(PRIMARY_COLOR));
        Label iconText = new Label("🥗");
        iconText.setFont(Font.font(35));
        StackPane iconPane = new StackPane(icon, iconText);
        
        Label title = new Label("Create Account");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setStyle("-fx-text-fill: #1e293b;");
        
        Label subtitle = new Label("Start your health journey today");
        subtitle.setFont(Font.font(13));
        subtitle.setStyle("-fx-text-fill: #64748b;");
        
        VBox form = new VBox(12);
        form.setAlignment(Pos.CENTER);
        
        TextField nameField = createStyledTextField("👤 Full Name");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("🔒 Password");
        passwordField.setStyle(
            "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 10; " +
            "-fx-border-color: #e2e8f0; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-color: #f8fafc;"
        );
        passwordField.setPrefWidth(350);
        
        TextField weightField = createStyledTextField("⚖️ Weight (kg)");
        TextField heightField = createStyledTextField("📏 Height (cm)");
        
        ComboBox<String> goalCombo = new ComboBox<>();
        goalCombo.getItems().addAll("🔥 Weight Loss", "💪 Weight Gain", "⚖️ Maintain Weight", "🏋️ Muscle Building");
        goalCombo.setPromptText("🎯 Select Your Goal");
        goalCombo.setStyle("-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 10;");
        goalCombo.setPrefWidth(350);
        
        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: " + DANGER_COLOR + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        
        Label successLabel = new Label();
        successLabel.setStyle("-fx-text-fill: " + SUCCESS_COLOR + "; -fx-font-weight: bold; -fx-font-size: 12px;");
        
        Button signupBtn = createGradientButton("Create Account →");
        signupBtn.setPrefWidth(350);
        
        signupBtn.setOnAction(e -> {
            try {
                String name = nameField.getText();
                String password = passwordField.getText();
                double wt = Double.parseDouble(weightField.getText());
                double ht = Double.parseDouble(heightField.getText());
                String selectedGoal = goalCombo.getValue();
                
                if (name.isEmpty() || password.isEmpty() || selectedGoal == null) {
                    errorLabel.setText("⚠️ Please fill all fields!");
                    successLabel.setText("");
                    return;
                }
                
                String userGoal = selectedGoal.substring(2).trim();
                
                // Calculate target calories
                double bmr = 10 * wt + 6.25 * ht - 5 * 30 + 5;
                double tdee = bmr * 1.55;
                double targetCal;
                
                switch (userGoal) {
                    case "Weight Loss": targetCal = tdee - 500; break;
                    case "Weight Gain": targetCal = tdee + 500; break;
                    case "Muscle Building": targetCal = tdee + 300; break;
                    default: targetCal = tdee;
                }
                
                try (Connection conn = Database.getConnection()) {
                    String sql = "INSERT INTO users (user_name, password, weight, height, goal, target_calories) VALUES (?, ?, ?, ?, ?, ?)";
                    java.sql.PreparedStatement pstmt = conn.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS);
                    pstmt.setString(1, name);
                    pstmt.setString(2, password);
                    pstmt.setDouble(3, wt);
                    pstmt.setDouble(4, ht);
                    pstmt.setString(5, userGoal);
                    pstmt.setDouble(6, targetCal);
                    pstmt.executeUpdate();
                    
                    java.sql.ResultSet rs = pstmt.getGeneratedKeys();
                    if (rs.next()) {
                        int newUserId = rs.getInt(1);
                        errorLabel.setText("");
                        successLabel.setText("✅ Account created! Your User ID is: " + newUserId);
                        
                        // Clear fields
                        nameField.clear();
                        passwordField.clear();
                        weightField.clear();
                        heightField.clear();
                        goalCombo.setValue(null);
                    }
                } catch (SQLException ex) {
                    errorLabel.setText("⚠️ Database error: " + ex.getMessage());
                    successLabel.setText("");
                    ex.printStackTrace();
                }
                
            } catch (NumberFormatException ex) {
                errorLabel.setText("⚠️ Please enter valid numbers!");
                successLabel.setText("");
            }
        });
        
        Label loginLabel = new Label("Already have an account?");
        loginLabel.setStyle("-fx-text-fill: #64748b; -fx-font-size: 12px;");
        
        Button loginBtn = new Button("Login");
        loginBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: " + PRIMARY_COLOR + "; -fx-font-weight: bold; -fx-cursor: hand;");
        loginBtn.setOnAction(e -> stage.setScene(createLoginScene(stage)));
        
        HBox loginBox = new HBox(5, loginLabel, loginBtn);
        loginBox.setAlignment(Pos.CENTER);
        
        form.getChildren().addAll(nameField, passwordField, weightField, heightField, goalCombo);
        card.getChildren().addAll(iconPane, title, subtitle, form, errorLabel, successLabel, signupBtn, loginBox);
        root.getChildren().addAll(bg, card);
        
        return new Scene(root, 600, 750);
    }
    
    private TextField createStyledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setStyle(
            "-fx-font-size: 14px; " +
            "-fx-padding: 12; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-width: 2; " +
            "-fx-border-radius: 10; " +
            "-fx-background-color: #f8fafc;"
        );
        field.setPrefWidth(350);
        field.setOnMouseEntered(e -> field.setStyle(
            "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 10; " +
            "-fx-border-color: " + PRIMARY_COLOR + "; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-color: white;"
        ));
        field.setOnMouseExited(e -> {
            if (!field.isFocused()) {
                field.setStyle(
                    "-fx-font-size: 14px; -fx-padding: 12; -fx-background-radius: 10; " +
                    "-fx-border-color: #e2e8f0; -fx-border-width: 2; -fx-border-radius: 10; -fx-background-color: #f8fafc;"
                );
            }
        });
        return field;
    }
    
    private Button createGradientButton(String text) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: linear-gradient(to right, " + PRIMARY_COLOR + ", " + SECONDARY_COLOR + "); " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 16px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 15 30; " +
            "-fx-background-radius: 12; " +
            "-fx-cursor: hand;"
        );
        
        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.web(PRIMARY_COLOR, 0.4));
        shadow.setRadius(15);
        
        btn.setOnMouseEntered(e -> {
            btn.setEffect(shadow);
            btn.setScaleX(1.05);
            btn.setScaleY(1.05);
        });
        btn.setOnMouseExited(e -> {
            btn.setEffect(null);
            btn.setScaleX(1.0);
            btn.setScaleY(1.0);
        });
        
        return btn;
    }
    
    private Button createSecondaryButton(String text, String color) {
        Button btn = new Button(text);
        btn.setStyle(
            "-fx-background-color: " + color + "; " +
            "-fx-text-fill: white; " +
            "-fx-font-size: 14px; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 10 20; " +
            "-fx-background-radius: 10; " +
            "-fx-cursor: hand;"
        );
        
        btn.setOnMouseEntered(e -> {
            btn.setStyle(
                "-fx-background-color: derive(" + color + ", -10%); " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand; " +
                "-fx-scale-x: 1.05; " +
                "-fx-scale-y: 1.05;"
            );
        });
        btn.setOnMouseExited(e -> {
            btn.setStyle(
                "-fx-background-color: " + color + "; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 10 20; " +
                "-fx-background-radius: 10; " +
                "-fx-cursor: hand;"
            );
        });
        
        return btn;
    }
    
    private void calculateTargetCalories() {
        double bmr = 10 * weight + 6.25 * height - 5 * 30 + 5;
        double tdee = bmr * 1.55;
        
        switch (goal) {
            case "Weight Loss":
                targetCalories = tdee - 500;
                break;
            case "Weight Gain":
                targetCalories = tdee + 500;
                break;
            case "Muscle Building":
                targetCalories = tdee + 300;
                break;
            default:
                targetCalories = tdee;
        }
    }
    
    private Scene createMainScene(Stage stage) {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        VBox header = createModernHeader(stage);
        
        VBox center = new VBox(20);
        center.setPadding(new Insets(25));
        
        HBox statsCards = createStatsCards();
        TabPane mealTabs = createStyledTabPane(stage);
        VBox summaryCard = createModernSummaryCard();
        
        HBox actionButtons = new HBox(15);
        actionButtons.setAlignment(Pos.CENTER);
        
        Button viewChartsBtn = createGradientButton("📊 View Analytics");
        Button resetBtn = createSecondaryButton("🔄 Reset Day", DANGER_COLOR);
        
        resetBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Reset Day");
            alert.setHeaderText("Clear all meals?");
            alert.setContentText("This will remove all food entries for today.");
            
            if (alert.showAndWait().get() == ButtonType.OK) {
                try (Connection conn = Database.getConnection()) {
                    String sql = "DELETE FROM food_items WHERE user_id = ? AND meal_date = CURDATE()";
                    java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
                    pstmt.setInt(1, currentUserId);
                    pstmt.executeUpdate();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
                
                initializeMeals();
                stage.setScene(createMainScene(stage));
            }
        });
        
        viewChartsBtn.setOnAction(e -> stage.setScene(createChartsScene(stage)));
        
        actionButtons.getChildren().addAll(viewChartsBtn, resetBtn);
        
        center.getChildren().addAll(statsCards, mealTabs, summaryCard, actionButtons);
        
        ScrollPane scrollPane = new ScrollPane(center);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + BACKGROUND_COLOR + "; -fx-background-color: transparent;");
        
        layout.setTop(header);
        layout.setCenter(scrollPane);
        
        return new Scene(layout, 900, 700);
    }
    
    private VBox createModernHeader(Stage stage) {
        VBox header = new VBox(10);
        header.setPadding(new Insets(25));
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, " + PRIMARY_COLOR + ", " + SECONDARY_COLOR + "); " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 10, 0, 0, 5);"
        );
        
        HBox headerTop = new HBox();
        headerTop.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(headerTop, Priority.ALWAYS);
        
        VBox userInfo = new VBox(5);
        Label welcomeLabel = new Label("Hello, " + userName + "! 👋");
        welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        welcomeLabel.setStyle("-fx-text-fill: white;");
        
        Label goalLabel = new Label("🎯 " + goal + " | Daily Target: " + String.format("%.0f", targetCalories) + " cal | ID: " + currentUserId);
        goalLabel.setFont(Font.font(14));
        goalLabel.setStyle("-fx-text-fill: rgba(255,255,255,0.9);");
        
        userInfo.getChildren().addAll(welcomeLabel, goalLabel);
        
        Button logoutBtn = new Button("Logout");
        logoutBtn.setStyle(
            "-fx-background-color: rgba(255,255,255,0.2); " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-padding: 8 20; " +
            "-fx-background-radius: 8; " +
            "-fx-cursor: hand;"
        );
        logoutBtn.setOnAction(e -> {
            currentUserId = -1;
            initializeMeals();
            stage.setScene(createLoginScene(stage));
        });
        
        headerTop.getChildren().addAll(userInfo, logoutBtn);
        header.getChildren().add(headerTop);
        
        return header;
    }
    
    private HBox createStatsCards() {
        HBox container = new HBox(15);
        container.setAlignment(Pos.CENTER);
        
        NutritionSummary summary = calculateTotalNutrition();
        
        container.getChildren().addAll(
            createStatCard("🔥 Calories", String.format("%.0f", summary.calories), String.format("/ %.0f", targetCalories), PRIMARY_COLOR),
            createStatCard("🥩 Protein", String.format("%.1f g", summary.protein), "", "#ec4899"),
            createStatCard("🍞 Carbs", String.format("%.1f g", summary.carbs), "", WARNING_COLOR),
            createStatCard("🥑 Fats", String.format("%.1f g", summary.fats), "", "#8b5cf6")
        );
        
        return container;
    }
    
    private VBox createStatCard(String title, String value, String subtitle, String color) {
        VBox card = new VBox(5);
        card.setPadding(new Insets(20));
        card.setAlignment(Pos.CENTER);
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 15; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 5);"
        );
        card.setPrefWidth(200);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        
        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        valueLabel.setStyle("-fx-text-fill: " + color + ";");
        
        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #94a3b8;");
        
        card.getChildren().addAll(titleLabel, valueLabel, subLabel);
        return card;
    }
    
    private TabPane createStyledTabPane(Stage stage) {
        TabPane tabPane = new TabPane();
        tabPane.setStyle("-fx-background-color: transparent;");
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        String[] emojis = {"🌅", "🌞", "🌙", "🍿"};
        int i = 0;
        for (String mealType : dailyMeals.keySet()) {
            Tab tab = new Tab(emojis[i++] + " " + mealType);
            tab.setContent(createMealPane(mealType, stage));
            tab.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
            tabPane.getTabs().add(tab);
        }
        
        return tabPane;
    }
    
    private VBox createMealPane(String mealType, Stage stage) {
        VBox pane = new VBox(15);
        pane.setPadding(new Insets(20));
        pane.setStyle("-fx-background-color: white; -fx-background-radius: 15;");
        
        ListView<String> foodList = new ListView<>();
        foodList.setPrefHeight(180);
        foodList.setStyle(
            "-fx-background-color: " + BACKGROUND_COLOR + "; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-radius: 10;"
        );
        updateFoodList(foodList, mealType);
        
        GridPane addGrid = new GridPane();
        addGrid.setHgap(10);
        addGrid.setVgap(10);
        addGrid.setAlignment(Pos.CENTER);
        
        TextField foodNameField = createCompactTextField("Food name");
        TextField caloriesField = createCompactTextField("Cal");
        TextField proteinField = createCompactTextField("Protein");
        TextField carbsField = createCompactTextField("Carbs");
        TextField fatsField = createCompactTextField("Fats");
        
        addGrid.add(foodNameField, 0, 0, 2, 1);
        addGrid.add(caloriesField, 0, 1);
        addGrid.add(proteinField, 1, 1);
        addGrid.add(carbsField, 0, 2);
        addGrid.add(fatsField, 1, 2);
        
        Button addBtn = createSecondaryButton("➕ Add Food", SUCCESS_COLOR);
        Button removeBtn = createSecondaryButton("🗑️ Remove", DANGER_COLOR);
        
        addBtn.setOnAction(e -> {
            try {
                String name = foodNameField.getText();
                double cal = Double.parseDouble(caloriesField.getText());
                double pro = Double.parseDouble(proteinField.getText());
                double carb = Double.parseDouble(carbsField.getText());
                double fat = Double.parseDouble(fatsField.getText());
                
                FoodItem item = new FoodItem(name, cal, pro, carb, fat);
                dailyMeals.get(mealType).add(item);
                
                saveFoodToDatabase(mealType, item);
                
                foodNameField.clear();
                caloriesField.clear();
                proteinField.clear();
                carbsField.clear();
                fatsField.clear();
                
                stage.setScene(createMainScene(stage));
            } catch (NumberFormatException ex) {
                showErrorAlert("Invalid input! Please enter valid numbers.");
            }
        });
        
        removeBtn.setOnAction(e -> {
            int selected = foodList.getSelectionModel().getSelectedIndex();
            if (selected >= 0) {
                FoodItem item = dailyMeals.get(mealType).get(selected);
                
                deleteFoodFromDatabase(mealType, item);
                
                dailyMeals.get(mealType).remove(selected);
                stage.setScene(createMainScene(stage));
            }
        });
        
        HBox buttonBox = new HBox(10, addBtn, removeBtn);
        buttonBox.setAlignment(Pos.CENTER);
        
        pane.getChildren().addAll(
            new Label("📋 Foods Added:"),
            foodList,
            new Label("➕ Add New Food:"),
            addGrid,
            buttonBox
        );
        
        return pane;
    }
    
    private TextField createCompactTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefWidth(150);
        field.setStyle(
            "-fx-font-size: 13px; " +
            "-fx-padding: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-radius: 8;"
        );
        return field;
    }
    
    private void updateFoodList(ListView<String> listView, String mealType) {
        listView.getItems().clear();
        for (FoodItem item : dailyMeals.get(mealType)) {
            listView.getItems().add(item.toString());
        }
    }
    
    private VBox createModernSummaryCard() {
        VBox card = new VBox(15);
        card.setPadding(new Insets(25));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);"
        );
        
        Label title = new Label("💡 Daily Insights & Recommendations");
        title.setFont(Font.font("System", FontWeight.BOLD, 18));
        title.setStyle("-fx-text-fill: #1e293b;");
        
        NutritionSummary summary = calculateTotalNutrition();
        
        VBox progressSection = new VBox(8);
        Label progressLabel = new Label(String.format("Calorie Progress: %.0f / %.0f cal", summary.calories, targetCalories));
        progressLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #475569;");
        
        ProgressBar progressBar = new ProgressBar(summary.calories / targetCalories);
        progressBar.setPrefWidth(400);
        progressBar.setStyle(
            "-fx-accent: " + (summary.calories <= targetCalories ? SUCCESS_COLOR : WARNING_COLOR) + ";"
        );
        
        progressSection.getChildren().addAll(progressLabel, progressBar);
        
        TextArea suggestions = new TextArea(generateDietSuggestions(summary));
        suggestions.setWrapText(true);
        suggestions.setEditable(false);
        suggestions.setPrefRowCount(4);
        suggestions.setStyle(
            "-fx-background-color: " + BACKGROUND_COLOR + "; " +
            "-fx-background-radius: 10; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-radius: 10; " +
            "-fx-font-size: 13px;"
        );
        
        card.getChildren().addAll(title, progressSection, suggestions);
        return card;
    }
    
    private NutritionSummary calculateTotalNutrition() {
        NutritionSummary summary = new NutritionSummary();
        for (List<FoodItem> meals : dailyMeals.values()) {
            for (FoodItem item : meals) {
                summary.calories += item.calories;
                summary.protein += item.protein;
                summary.carbs += item.carbs;
                summary.fats += item.fats;
            }
        }
        return summary;
    }
    
    private String generateDietSuggestions(NutritionSummary summary) {
        StringBuilder suggestions = new StringBuilder();
        
        double calorieDiff = summary.calories - targetCalories;
        
        if (Math.abs(calorieDiff) < 100) {
            suggestions.append("✅ Excellent! Your calorie intake is right on target!\n\n");
        } else if (calorieDiff > 0) {
            suggestions.append("⚠️ You're ").append(String.format("%.0f", calorieDiff))
                      .append(" calories over your target. Consider smaller portions or more vegetables.\n\n");
        } else {
            suggestions.append("⬇️ You're ").append(String.format("%.0f", -calorieDiff))
                      .append(" calories under your target. Add nutrient-dense snacks like nuts or fruits.\n\n");
        }
        
        double proteinTarget = weight * 1.0;
        if (summary.protein < proteinTarget * 0.8) {
            suggestions.append("🥩 Boost protein: Add chicken, fish, eggs, or plant-based proteins.\n");
        } else if (summary.protein >= proteinTarget) {
            suggestions.append("✅ Great protein intake for muscle maintenance!\n");
        }
        
        if (goal.equals("Muscle Building") && summary.protein < weight * 1.6) {
            suggestions.append("💪 For muscle building, aim for 1.6-2.2g protein per kg body weight.\n");
        }
        
        double totalCal = summary.protein * 4 + summary.carbs * 4 + summary.fats * 9;
        if (totalCal > 0) {
            double carbPercent = (summary.carbs * 4 / totalCal) * 100;
            if (goal.equals("Weight Loss") && carbPercent > 45) {
                suggestions.append("🥗 Consider swapping refined carbs for more vegetables and lean proteins.\n");
            }
        }
        
        return suggestions.toString();
    }
    
    private Scene createChartsScene(Stage stage) {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: " + BACKGROUND_COLOR + ";");
        
        VBox header = new VBox(10);
        header.setPadding(new Insets(25));
        header.setStyle(
            "-fx-background-color: linear-gradient(to right, " + PRIMARY_COLOR + ", " + SECONDARY_COLOR + ");"
        );
        
        Label title = new Label("📊 Nutrition Analytics");
        title.setFont(Font.font("System", FontWeight.BOLD, 26));
        title.setStyle("-fx-text-fill: white;");
        
        Label subtitle = new Label("Visual breakdown of your daily intake");
        subtitle.setStyle("-fx-text-fill: rgba(255,255,255,0.8); -fx-font-size: 14px;");
        
        header.getChildren().addAll(title, subtitle);
        
        VBox chartsContainer = new VBox(30);
        chartsContainer.setPadding(new Insets(30));
        chartsContainer.setAlignment(Pos.CENTER);
        
        NutritionSummary summary = calculateTotalNutrition();
        
        PieChart pieChart = new PieChart();
        pieChart.setTitle("🥧 Macronutrient Distribution");
        pieChart.setStyle("-fx-font-size: 14px;");
        pieChart.getData().addAll(
            new PieChart.Data("Protein " + String.format("(%.0fg)", summary.protein), summary.protein * 4),
            new PieChart.Data("Carbs " + String.format("(%.0fg)", summary.carbs), summary.carbs * 4),
            new PieChart.Data("Fats " + String.format("(%.0fg)", summary.fats), summary.fats * 9)
        );
        pieChart.setPrefHeight(350);
        
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        BarChart<String, Number> barChart = new BarChart<>(xAxis, yAxis);
        barChart.setTitle("📈 Calories by Meal");
        barChart.setStyle("-fx-font-size: 14px;");
        xAxis.setLabel("Meal Type");
        yAxis.setLabel("Calories");
        barChart.setLegendVisible(false);
        
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, List<FoodItem>> entry : dailyMeals.entrySet()) {
            double mealCal = entry.getValue().stream().mapToDouble(f -> f.calories).sum();
            series.getData().add(new XYChart.Data<>(entry.getKey(), mealCal));
        }
        barChart.getData().add(series);
        barChart.setPrefHeight(350);
        
        VBox pieCard = createChartCard(pieChart);
        VBox barCard = createChartCard(barChart);
        
        Button backBtn = createGradientButton("← Back to Dashboard");
        backBtn.setOnAction(e -> stage.setScene(createMainScene(stage)));
        
        chartsContainer.getChildren().addAll(pieCard, barCard, backBtn);
        
        ScrollPane scrollPane = new ScrollPane(chartsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: " + BACKGROUND_COLOR + "; -fx-background-color: transparent;");
        
        layout.setTop(header);
        layout.setCenter(scrollPane);
        
        return new Scene(layout, 900, 700);
    }
    
    private VBox createChartCard(javafx.scene.Node chart) {
        VBox card = new VBox(chart);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: white; " +
            "-fx-background-radius: 20; " +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 15, 0, 0, 5);"
        );
        return card;
    }
    
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void loadMealsFromDatabase() {
        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT meal_type, food_name, calories, protein, carbs, fats " +
                         "FROM food_items WHERE user_id = ? AND meal_date = CURDATE()";
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            java.sql.ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                String mealType = rs.getString("meal_type");
                FoodItem item = new FoodItem(
                    rs.getString("food_name"),
                    rs.getDouble("calories"),
                    rs.getDouble("protein"),
                    rs.getDouble("carbs"),
                    rs.getDouble("fats")
                );
                dailyMeals.get(mealType).add(item);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    private void saveFoodToDatabase(String mealType, FoodItem item) {
        try (Connection conn = Database.getConnection()) {
            String sql = "INSERT INTO food_items (user_id, meal_type, food_name, calories, protein, carbs, fats, meal_date) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, CURDATE())";
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, mealType);
            pstmt.setString(3, item.name);
            pstmt.setDouble(4, item.calories);
            pstmt.setDouble(5, item.protein);
            pstmt.setDouble(6, item.carbs);
            pstmt.setDouble(7, item.fats);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorAlert("Failed to save food to database: " + ex.getMessage());
        }
    }
    
    private void deleteFoodFromDatabase(String mealType, FoodItem item) {
        try (Connection conn = Database.getConnection()) {
            String sql = "DELETE FROM food_items WHERE user_id = ? AND meal_type = ? AND food_name = ? " +
                         "AND meal_date = CURDATE() LIMIT 1";
            java.sql.PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, currentUserId);
            pstmt.setString(2, mealType);
            pstmt.setString(3, item.name);
            pstmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    static class FoodItem {
        String name;
        double calories;
        double protein;
        double carbs;
        double fats;
        
        FoodItem(String name, double calories, double protein, double carbs, double fats) {
            this.name = name;
            this.calories = calories;
            this.protein = protein;
            this.carbs = carbs;
            this.fats = fats;
        }
        
        @Override
        public String toString() {
            return String.format("%s • %.0f cal (P: %.1fg, C: %.1fg, F: %.1fg)", 
                               name, calories, protein, carbs, fats);
        }
    }
    
    static class NutritionSummary {
        double calories = 0;
        double protein = 0;
        double carbs = 0;
        double fats = 0;
    }
    
    
    public static void main(String[] args) {
        launch(args);
    }
}