import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ResumeGeneratorApp extends Application {

    private static final String DB_URL = "jdbc:oracle:thin:@localhost:1522:xe";
    private static final String DB_USER = "harshini";
    private static final String DB_PASSWORD = "harshini";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Label titleLabel = new Label("Resume Generator");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name *");

        TextField emailField = new TextField();
        emailField.setPromptText("Email *");

        TextField phoneField = new TextField();
        phoneField.setPromptText("Phone Number *");

        TextArea addressField = new TextArea();
        addressField.setPromptText("Address *");
        addressField.setPrefRowCount(2);

        TextArea educationField = new TextArea();
        educationField.setPromptText("Education *");
        educationField.setPrefRowCount(2);

        TextArea experienceField = new TextArea();
        experienceField.setPromptText("Experience *");
        experienceField.setPrefRowCount(2);

        TextArea skillsField = new TextArea();
        skillsField.setPromptText("Skills *");
        skillsField.setPrefRowCount(2);

        TextArea projectsField = new TextArea();
        projectsField.setPromptText("Projects *");
        projectsField.setPrefRowCount(2);

        TextArea hobbiesField = new TextArea();
        hobbiesField.setPromptText("Hobbies *");
        hobbiesField.setPrefRowCount(2);

        TextArea extraField = new TextArea();
        extraField.setPromptText("Extra Curricular Activities *");
        extraField.setPrefRowCount(2);

        Button submitButton = new Button("Save Resume");
        Label statusLabel = new Label();

        submitButton.setOnAction(e -> {
            String name = nameField.getText();
            String email = emailField.getText();
            String phone = phoneField.getText();
            String address = addressField.getText();
            String education = educationField.getText();
            String experience = experienceField.getText();
            String skills = skillsField.getText();
            String projects = projectsField.getText();
            String hobbies = hobbiesField.getText();
            String extra = extraField.getText();

            if (name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || education.isEmpty() ||
                    experience.isEmpty() || skills.isEmpty() || projects.isEmpty() || hobbies.isEmpty() || extra.isEmpty()) {
                statusLabel.setText("All fields marked with * are required!");
                return;
            }

            if (!isValidEmail(email)) {
                statusLabel.setText("Invalid email format!");
                return;
            }

            if (!isValidPhoneNumber(phone)) {
                statusLabel.setText("Invalid phone number! It must be 10 digits.");
                return;
            }

            String insertSQL = "INSERT INTO resume_data (name, email, phone, address, education, experience, skills, projects, hobbies, extra_curricular) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try {
                Class.forName("oracle.jdbc.driver.OracleDriver");
                Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                PreparedStatement pstmt = conn.prepareStatement(insertSQL);
                pstmt.setString(1, name);
                pstmt.setString(2, email);
                pstmt.setString(3, phone);
                pstmt.setString(4, address);
                pstmt.setString(5, education);
                pstmt.setString(6, experience);
                pstmt.setString(7, skills);
                pstmt.setString(8, projects);
                pstmt.setString(9, hobbies);
                pstmt.setString(10, extra);
                pstmt.executeUpdate();
                conn.close();

                statusLabel.setText("Resume saved successfully!");

                // === Resume Display ===
                VBox resumeBox = new VBox(10);
                resumeBox.setPadding(new Insets(20));
                resumeBox.setStyle("-fx-background-color: white;");

                Label resumeName = new Label(name);
                resumeName.setFont(Font.font("Arial", FontWeight.BOLD, 24));

                HBox contactBox = new HBox(10);
                contactBox.setPadding(new Insets(5));

                ImageView emailIcon = new ImageView(new Image("https://img.icons8.com/ios-filled/20/000000/email.png"));
                ImageView phoneIcon = new ImageView(new Image("https://img.icons8.com/ios-filled/20/000000/phone.png"));

                Label emailLabel = new Label(" " + email);
                Label phoneLabel = new Label(" " + phone);
                emailLabel.setFont(Font.font("Arial", 13));
                phoneLabel.setFont(Font.font("Arial", 13));

                contactBox.getChildren().addAll(emailIcon, emailLabel, phoneIcon, phoneLabel);

                Label addressLabel = new Label("📍 Address: " + address);
                addressLabel.setFont(Font.font("Arial", 13));

                resumeBox.getChildren().addAll(
                        resumeName,
                        contactBox,
                        addressLabel,
                        new Separator(),
                        createSection("🎓 Education", education),
                        createSection("💼 Experience", experience),
                        createSection("🛠 Skills", skills),
                        createSection("📂 Projects", projects),
                        createSection("🎯 Hobbies", hobbies),
                        createSection("🏅 Extra Curricular Activities", extra)
                );

                ScrollPane scrollPane = new ScrollPane(resumeBox);
                scrollPane.setFitToWidth(true);

                Dialog<Void> resumeDialog = new Dialog<>();
                resumeDialog.setTitle("Formatted Resume");
                resumeDialog.getDialogPane().setContent(scrollPane);
                resumeDialog.getDialogPane().setPrefWidth(600);
                resumeDialog.getDialogPane().setPrefHeight(700);
                resumeDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                resumeDialog.showAndWait();

                // === Job Suggestion Pop-up ===
                String jobSuggestion = generateJobSuggestions(skills + " " + experience + " " + education);

                Label jobTitle = new Label("🧭 Suggested Jobs Based on Your Resume");
                jobTitle.setFont(Font.font("Arial", FontWeight.BOLD, 18));

                Label suggestionLabel = new Label(jobSuggestion);
                suggestionLabel.setWrapText(true);
                suggestionLabel.setFont(Font.font("Arial", 13));

                VBox suggestionBox = new VBox(10, jobTitle, new Separator(), suggestionLabel);
                suggestionBox.setPadding(new Insets(20));

                Dialog<Void> jobDialog = new Dialog<>();
                jobDialog.setTitle("Job Suggestions");
                jobDialog.getDialogPane().setContent(suggestionBox);
                jobDialog.getDialogPane().setPrefWidth(500);
                jobDialog.getDialogPane().setPrefHeight(300);
                jobDialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
                jobDialog.showAndWait();

                clearFields(nameField, emailField, phoneField, addressField,
                        educationField, experienceField, skillsField, projectsField,
                        hobbiesField, extraField);

            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("Error: " + ex.getMessage());
            }
        });

        VBox root = new VBox(10,
                titleLabel,
                nameField, emailField, phoneField,
                addressField, educationField, experienceField, skillsField,
                projectsField, hobbiesField, extraField,
                submitButton, statusLabel
        );
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f2f2f2;");

        Scene scene = new Scene(root, 500, 750);
        primaryStage.setTitle("Resume Generator");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createSection(String title, String content) {
        Label sectionTitle = new Label(title);
        sectionTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        sectionTitle.setStyle("-fx-text-fill: #003366;");

        Label sectionContent = new Label(content);
        sectionContent.setWrapText(true);
        sectionContent.setFont(Font.font("Arial", 13));

        VBox sectionBox = new VBox(5);
        sectionBox.getChildren().addAll(sectionTitle, sectionContent, new Separator());
        sectionBox.setPadding(new Insets(5, 0, 5, 0));
        return sectionBox;
    }

    private void clearFields(TextInputControl... fields) {
        for (TextInputControl field : fields) {
            field.clear();
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private boolean isValidPhoneNumber(String phone) {
        return phone.matches("\\d{10}");
    }

    // Job suggestion logic
    private String generateJobSuggestions(String text) {
        text = text.toLowerCase();
        StringBuilder suggestions = new StringBuilder();

        if (text.contains("java") || text.contains("spring")) suggestions.append("💼 Java Developer\n");
        if (text.contains("python") || text.contains("machine learning") || text.contains("ai")) suggestions.append("🤖 AI/ML Engineer\n");
        if (text.contains("html") || text.contains("css") || text.contains("javascript")) suggestions.append("🌐 Frontend Developer\n");
        if (text.contains("sql") || text.contains("oracle") || text.contains("database")) suggestions.append("🗄 Database Administrator (DBA)\n");
        if (text.contains("android") || text.contains("mobile")) suggestions.append("📱 Android App Developer\n");
        if (text.contains("c++") || text.contains("c language")) suggestions.append("👨‍💻 Embedded Systems Engineer\n");
        if (text.contains("networking") || text.contains("cyber")) suggestions.append("🔐 Network Security Analyst\n");
        if (text.contains("data analysis") || text.contains("excel") || text.contains("power bi")) suggestions.append("📊 Data Analyst\n");
        if (text.contains("cloud") || text.contains("aws") || text.contains("azure") || text.contains("gcp")) suggestions.append("☁️ Cloud Engineer\n");
        if (text.contains("devops") || text.contains("docker") || text.contains("kubernetes")) suggestions.append("🛠 DevOps Engineer\n");
        if (text.contains("project management") || text.contains("scrum") || text.contains("agile")) suggestions.append("📋 Project Manager\n");
        if (text.contains("ui/ux") || text.contains("figma") || text.contains("adobe xd")) suggestions.append("🎨 UI/UX Designer\n");
        if (text.contains("testing") || text.contains("qa") || text.contains("selenium")) suggestions.append("🧪 QA/Test Engineer\n");
        if (text.contains("blockchain") || text.contains("web3")) suggestions.append("⛓ Blockchain Developer\n");
        if (text.contains("salesforce") || text.contains("crm")) suggestions.append("🔧 Salesforce Developer\n");
        if (text.contains("game development") || text.contains("unity") || text.contains("unreal")) suggestions.append("🎮 Game Developer\n");
        if (text.contains("react") || text.contains("angular") || text.contains("vue")) suggestions.append("🖥 Frontend Framework Developer\n");
        if (text.contains("node.js") || text.contains("express")) suggestions.append("🧩 Backend Developer (Node.js)\n");
        if (text.contains("php") || text.contains("laravel")) suggestions.append("🐘 PHP Developer\n");
        if (text.contains("english literature") || text.contains("creative writing")) suggestions.append("📚 Content Writer / Editor\n");
        if (text.contains("tamil literature") || text.contains("tamil language")) suggestions.append("📝 Tamil Content Creator / Translator\n");
        if (text.contains("journalism") || text.contains("reporting") || text.contains("mass communication")) suggestions.append("📰 Journalist / Media Professional\n");
        if (text.contains("teaching") || text.contains("education") || text.contains("tutor")) suggestions.append("👩‍🏫 Teacher / Academic Instructor\n");
        if (text.contains("translation") || text.contains("translator") || text.contains("language expert")) suggestions.append("🌍 Language Translator / Linguist\n");
        if (text.contains("public relations") || text.contains("communication skills")) suggestions.append("📢 Public Relations Specialist\n");
        if (text.contains("history") || text.contains("culture") || text.contains("heritage")) suggestions.append("🏛 Cultural Researcher / Historian\n");
        if (text.contains("psychology") || text.contains("counseling")) suggestions.append("🧠 Psychologist / Counselor\n");




        if (suggestions.length() == 0)
            suggestions.append("🤔 No specific suggestions found. Try adding more skills.");

        return suggestions.toString();
    }
}
