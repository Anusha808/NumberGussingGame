
package com.guessinggame;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.effect.Glow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Sphere;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NumberGuessingGame extends Application {

    private final Random random = new Random();
    private final List<Circle> stars = new ArrayList<>();
    private final List<Double> starSpeeds = new ArrayList<>();
    private final List<Node> movingObjects = new ArrayList<>();

    private int secretNumber;
    private int attempts;
    private int maxAttempts;
    private int minNumber = 1;
    private int maxNumber = 100;
    private int round = 1;
    private int wins = 0;

    private Label feedbackLabel;
    private Label attemptsLabel;
    private Label scoreLabel;
    private Label rangeLabel;
    private TextField guessField;
    private Button guessButton;
    private Button playAgainButton;
    private ComboBox<String> difficultyBox;

    private Group objectGroup;
    private Pane starPane;
    private AnimationTimer animationTimer;

    private double objectOffsetX = 0;
    private double objectOffsetY = 0;
    private double targetOffsetX = 0;
    private double targetOffsetY = 0;

    @Override
    public void start(Stage stage) {
        BorderPane root = new BorderPane();

        starPane = createStarfield();

        SubScene subScene = create3DScene();
        subScene.setMouseTransparent(true);

        VBox gamePanel = createGamePanel();

        StackPane centerPane = new StackPane(starPane, subScene, gamePanel);
        root.setCenter(centerPane);

        Scene scene = new Scene(root, 1000, 700);

        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                targetOffsetX -= 35;
            } else if (event.getCode() == KeyCode.RIGHT) {
                targetOffsetX += 35;
            } else if (event.getCode() == KeyCode.UP) {
                targetOffsetY -= 35;
            } else if (event.getCode() == KeyCode.DOWN) {
                targetOffsetY += 35;
            }
        });

        centerPane.setOnMouseMoved(event -> {
            double width = centerPane.getWidth();
            double height = centerPane.getHeight();

            if (width > 0 && height > 0) {
                targetOffsetX = (event.getX() - width / 2) * 0.12;
                targetOffsetY = (event.getY() - height / 2) * 0.12;
            }
        });

        stage.setTitle("3D Number Guessing Game");
        stage.setScene(scene);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.setMaximized(true);
        stage.show();

        starPane.prefWidthProperty().bind(centerPane.widthProperty());
        starPane.prefHeightProperty().bind(centerPane.heightProperty());

        subScene.widthProperty().bind(centerPane.widthProperty());
        subScene.heightProperty().bind(centerPane.heightProperty());

        startAnimation();
        startNewRound();
    }

    private Pane createStarfield() {
        Pane pane = new Pane();
        pane.setStyle("-fx-background-color: #020611;");
        pane.setMinSize(0, 0);
        pane.setPrefSize(1000, 700);

        pane.widthProperty().addListener((obs, oldWidth, newWidth) ->
                populateStars(pane, newWidth.doubleValue(), pane.getHeight()));

        pane.heightProperty().addListener((obs, oldHeight, newHeight) ->
                populateStars(pane, pane.getWidth(), newHeight.doubleValue()));

        return pane;
    }

    private void populateStars(Pane pane, double width, double height) {
        if (width <= 0 || height <= 0) {
            return;
        }

        pane.getChildren().clear();
        stars.clear();
        starSpeeds.clear();

        int starCount = Math.max(300, (int) (width * height / 1800));

        for (int i = 0; i < starCount; i++) {
            double radius = random.nextDouble() * 1.5 + 0.4;

            Circle star = new Circle(radius);
            star.setFill(Color.rgb(
                    190 + random.nextInt(66),
                    200 + random.nextInt(56),
                    220 + random.nextInt(36)
            ));
            star.setOpacity(0.35 + random.nextDouble() * 0.65);
            star.setCenterX(random.nextDouble() * width);
            star.setCenterY(random.nextDouble() * height);

            pane.getChildren().add(star);
            stars.add(star);
            starSpeeds.add(0.15 + random.nextDouble() * 0.65);
        }

        for (int i = 0; i < 25; i++) {
            Circle star = new Circle(random.nextDouble() * 2 + 1.2);
            star.setFill(Color.LIGHTCYAN);
            star.setOpacity(0.8);
            star.setEffect(new Glow(0.7));
            star.setCenterX(random.nextDouble() * width);
            star.setCenterY(random.nextDouble() * height);

            pane.getChildren().add(star);
            stars.add(star);
            starSpeeds.add(0.2 + random.nextDouble() * 0.8);
        }
    }

    private SubScene create3DScene() {
        objectGroup = new Group();

        PhongMaterial blueMaterial = new PhongMaterial(Color.DODGERBLUE);
        blueMaterial.setSpecularColor(Color.LIGHTCYAN);
        blueMaterial.setSpecularPower(70);

        PhongMaterial cyanMaterial = new PhongMaterial(Color.DEEPSKYBLUE);
        cyanMaterial.setSpecularColor(Color.WHITE);
        cyanMaterial.setSpecularPower(80);

        // Small blue balls
        for (int i = 0; i < 7; i++) {
            Sphere ball = new Sphere(9 + random.nextInt(5));
            ball.setMaterial(blueMaterial);
            ball.setTranslateX(-300 + i * 45);
            ball.setTranslateY(-190 + (i % 2) * 45);
            ball.setTranslateZ(100);

            objectGroup.getChildren().add(ball);
            movingObjects.add(ball);
        }

        // Small blue cubes
        for (int i = 0; i < 7; i++) {
            Box smallCube = new Box(20, 20, 20);
            smallCube.setMaterial(cyanMaterial);
            smallCube.setTranslateX(30 + i * 45);
            smallCube.setTranslateY(170 - (i % 2) * 45);
            smallCube.setTranslateZ(100);

            objectGroup.getChildren().add(smallCube);
            movingObjects.add(smallCube);
        }

        AmbientLight ambientLight = new AmbientLight(
                Color.color(0.65, 0.65, 0.75)
        );

        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateX(-200);
        pointLight.setTranslateY(-200);
        pointLight.setTranslateZ(-300);

        Group world = new Group(objectGroup, ambientLight, pointLight);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(2000);
        camera.setTranslateZ(-900);

        SubScene subScene = new SubScene(
                world,
                1000,
                700,
                true,
                SceneAntialiasing.BALANCED
        );

        subScene.setFill(Color.TRANSPARENT);
        subScene.setCamera(camera);

        return subScene;
    }

    private VBox createGamePanel() {
        VBox panel = new VBox(18);
        panel.setAlignment(Pos.CENTER);
        panel.setPadding(new Insets(30));
        panel.setMaxWidth(470);
        panel.setMaxHeight(570);

        panel.setStyle(
                "-fx-background-color: rgba(5, 15, 35, 0.88);" +
                "-fx-background-radius: 24;" +
                "-fx-border-color: #45caff;" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 24;"
        );

        Label title = new Label("NUMBER GUESSING");
        title.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 28px;" +
                "-fx-font-weight: bold;"
        );

        Label subtitle = new Label("Guess the secret number!");
        subtitle.setStyle(
                "-fx-text-fill: #9bb7d8;" +
                "-fx-font-size: 15px;"
        );

        difficultyBox = new ComboBox<>();
        difficultyBox.getItems().addAll("Easy", "Medium", "Hard");
        difficultyBox.setValue("Medium");
        difficultyBox.setMaxWidth(220);
        difficultyBox.setStyle("-fx-font-size: 14px;");
        difficultyBox.setOnAction(e -> startNewRound());

        rangeLabel = new Label();
        rangeLabel.setStyle(
                "-fx-text-fill: #8ddcff;" +
                "-fx-font-size: 16px;"
        );

        guessField = new TextField();
        guessField.setPromptText("Enter your guess");
        guessField.setMaxWidth(250);
        guessField.setAlignment(Pos.CENTER);
        guessField.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 12;"
        );

        // Digits only, including pasted input
        guessField.setTextFormatter(new TextFormatter<String>(change -> {
            String newText = change.getControlNewText();
            return newText.matches("\\d*") ? change : null;
        }));

        guessField.setOnAction(e -> checkGuess());

        guessButton = new Button("GUESS");
        guessButton.setDefaultButton(true);
        guessButton.setStyle(
                "-fx-background-color: #1687ff;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 16px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 12 35;"
        );
        guessButton.setOnAction(e -> checkGuess());

        feedbackLabel = new Label("Enter a number to begin.");
        feedbackLabel.setWrapText(true);
        feedbackLabel.setMinHeight(35);
        feedbackLabel.setAlignment(Pos.CENTER);
        feedbackLabel.setStyle(
                "-fx-text-fill: white;" +
                "-fx-font-size: 18px;" +
                "-fx-font-weight: bold;"
        );

        attemptsLabel = new Label();
        attemptsLabel.setStyle(
                "-fx-text-fill: #c0d5ed;" +
                "-fx-font-size: 15px;"
        );

        scoreLabel = new Label();
        scoreLabel.setStyle(
                "-fx-text-fill: #c0d5ed;" +
                "-fx-font-size: 15px;"
        );

        playAgainButton = new Button("PLAY AGAIN");
        playAgainButton.setStyle(
                "-fx-background-color: #16a085;" +
                "-fx-text-fill: white;" +
                "-fx-font-size: 15px;" +
                "-fx-font-weight: bold;" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 11 30;"
        );
        playAgainButton.setOnAction(e -> {
            round++;
            startNewRound();
        });

        panel.getChildren().addAll(
                title,
                subtitle,
                difficultyBox,
                rangeLabel,
                guessField,
                guessButton,
                feedbackLabel,
                attemptsLabel,
                scoreLabel,
                playAgainButton
        );

        return panel;
    }

    private void startNewRound() {
        String difficulty = difficultyBox.getValue();

        switch (difficulty) {
            case "Easy":
                minNumber = 1;
                maxNumber = 50;
                maxAttempts = 10;
                break;

            case "Hard":
                minNumber = 1;
                maxNumber = 200;
                maxAttempts = 5;
                break;

            default:
                minNumber = 1;
                maxNumber = 100;
                maxAttempts = 7;
                break;
        }

        secretNumber = random.nextInt(maxNumber - minNumber + 1)
                + minNumber;

        attempts = 0;

        rangeLabel.setText(
                "Range: " + minNumber + " – " + maxNumber
        );

        feedbackLabel.setText("Make your first guess!");
        feedbackLabel.setTextFill(Color.WHITE);

        guessField.clear();
        guessField.setDisable(false);
        guessButton.setDisable(false);
        playAgainButton.setDisable(false);

        updateCounters();
        guessField.requestFocus();
    }

    private void checkGuess() {
        if (guessButton.isDisabled()) {
            return;
        }

        String input = guessField.getText().trim();

        if (input.isEmpty()) {
            feedbackLabel.setText("Please enter a number.");
            feedbackLabel.setTextFill(Color.ORANGE);
            return;
        }

        int guess;

        try {
            guess = Integer.parseInt(input);
        } catch (NumberFormatException ex) {
            feedbackLabel.setText("Enter a valid whole number.");
            feedbackLabel.setTextFill(Color.ORANGE);
            guessField.clear();
            return;
        }

        if (guess < minNumber || guess > maxNumber) {
            feedbackLabel.setText(
                    "Enter a number from " + minNumber +
                    " to " + maxNumber + "."
            );
            feedbackLabel.setTextFill(Color.ORANGE);
            guessField.clear();
            return;
        }

        attempts++;
        updateCounters();

        if (guess == secretNumber) {
            wins++;
            feedbackLabel.setText("CORRECT! You guessed it!");
            feedbackLabel.setTextFill(Color.LIMEGREEN);
            endRound();
            return;
        }

        if (guess < secretNumber) {
            feedbackLabel.setText("TOO LOW");
            feedbackLabel.setTextFill(Color.ORANGE);
        } else {
            feedbackLabel.setText("TOO HIGH");
            feedbackLabel.setTextFill(Color.ORANGE);
        }

        guessField.clear();

        if (attempts >= maxAttempts) {
            feedbackLabel.setText(
                    "GAME OVER! The number was " + secretNumber + "."
            );
            feedbackLabel.setTextFill(Color.TOMATO);
            endRound();
        }
    }

    private void endRound() {
        guessField.setDisable(true);
        guessButton.setDisable(true);
        updateCounters();
    }

    private void updateCounters() {
        attemptsLabel.setText(
                "Attempts: " + attempts + " / " + maxAttempts
        );

        scoreLabel.setText(
                "Round: " + round + "     Wins: " + wins
        );
    }

    private void startAnimation() {
        animationTimer = new AnimationTimer() {
            private double angle = 0;

            @Override
            public void handle(long now) {
                angle += 0.5;

                // Smoothly follow mouse position
                objectOffsetX += (targetOffsetX - objectOffsetX) * 0.08;
                objectOffsetY += (targetOffsetY - objectOffsetY) * 0.08;

                objectGroup.setTranslateX(objectOffsetX);
                objectGroup.setTranslateY(objectOffsetY);

                for (int i = 0; i < movingObjects.size(); i++) {
                    Node object = movingObjects.get(i);
                    object.setRotate(angle * (i % 2 == 0 ? 1 : -1));
                }

                for (int i = 0; i < stars.size(); i++) {
                    Circle star = stars.get(i);
                    double speed = starSpeeds.get(i);

                    star.setCenterY(star.getCenterY() + speed);

                    if (star.getCenterY() > starPane.getHeight()) {
                        star.setCenterY(0);
                        star.setCenterX(
                                random.nextDouble() * starPane.getWidth()
                        );
                    }

                    star.setOpacity(
                            0.4 + 0.6 * Math.abs(
                                    Math.sin(angle / 20 + i)
                            )
                    );
                }
            }
        };

        animationTimer.start();
    }

    @Override
    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
