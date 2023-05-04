package cs1302.api;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import java.io.IOException;
import java.net.URL;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * REPLACE WITH NON-SHOUTING DESCRIPTION OF YOUR APP.
 */
public class ApiApp extends Application {
    private String spoonacularApiKey = "7ac0583f3e084044af190ff46a4bb3fd";
    Stage stage;
    Scene scene;
    VBox root;

    HBox topHBox;
    TextField foodUser;
    ComboBox<String> dietUser;

    Button search;

    Label status;

    HBox resultBox;
    TextArea result;
    ScrollPane textPane;
    TextFlow textFlow;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        this.stage = null;
        this.scene = null;
        this.root = new VBox();

        this.topHBox = new HBox();
        Label title = new Label("Personalized Recipe Finder");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        foodUser = new TextField();
        foodUser.setPromptText("Enter Food: ");
        dietUser = new ComboBox<>();
        dietUser.setPromptText("Select Diet: ");
        dietUser.getItems().addAll("Gluten Free", "Ketogenic", "Vegetarian", "Lacto-Vegetarian",
        "Ovo-Vegetarian", "Vegan", "Pescatarian","Paleo", "Primal");

        search = new Button("Search");
        status = new Label();
        this.resultBox = new HBox();
        this.textPane = new ScrollPane();
        this.textFlow = new TextFlow();
        result = new TextArea();
        result.setEditable(false);
    } // ApiApp


    /**{@inheritDoc}*/
    @Override
    public void init() {
        this.topHBox.getChildren().addAll(foodUser, dietUser, search);
        //this.resultBox.getChildren().addAll(result);
        this.textFlow.getChildren().add(new Text("Click \"Search\" to load the recipe results..."));
        this.textFlow.setMaxWidth(630);
        this.textPane.setPrefHeight(480);
        this.textPane.setContent(this.textFlow);
        this.root.getChildren().addAll(topHBox, this.textPane);
    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        // demonstrate how to load local asset using "file:resources/"
        //Image bannerImage = new Image("file:resources/readme-banner.png");
        //ImageView banner = new ImageView(bannerImage);
        //banner.setPreserveRatio(true);
        //banner.setFitWidth(640);

        // some labels to display information

        //Gson gson = new Gson();

        search.setOnAction(event -> searchResults());

        // setup scene
        scene = new Scene(root,640,480);

        // setup stage
        stage.setTitle("ApiApp!");
        stage.setScene(scene);
        stage.setOnCloseRequest(event -> Platform.exit());
        stage.sizeToScene();
        stage.show();
    } // start

    private static HttpClient HTTP_CLIENT = HttpClient.newBuilder()
        .version(HttpClient.Version.HTTP_2)           // uses HTTP protocol version 2 where possible
        .followRedirects(HttpClient.Redirect.NORMAL)  // always redirects, except from HTTPS to HTTP
        .build();                                     // builds and returns an HttpClient

    private static Gson GSON = new GsonBuilder()
        .setPrettyPrinting()
        .create();

    private void searchResults() {
        try {
            String spoonacular_API = "https://api.spoonacular.com/recipes/complexSearch?query=";
            String food = URLEncoder.encode(foodUser.getText(), StandardCharsets.UTF_8);
            String diet = URLEncoder.encode(dietUser.getValue(), StandardCharsets.UTF_8);
            //String cuisine = URLEncoder.encode(cuisineUser.getValue(), StandardCharsets.UTF_8);
            //String intolerance = URLEncoder.encode(intoleranceUser.getValue(), StandardCharsets.UTF_8);

            String urlBuilder = new String();
            urlBuilder += food;
            //if (!cuisine.isEmpty()) {
            // urlBuilder+="&cuisine=";
            //  urlBuilder+=cuisine;
            //}
            if (!diet.isEmpty()) {
             urlBuilder+="&diet=";
              urlBuilder+=diet;
            }
            //if (!intolerance.isEmpty()) {
            //  urlBuilder+="&intolerances=";
            //  urlBuilder+=intolerance;
            //}
            urlBuilder+="&apiKey=";
            urlBuilder+=spoonacularApiKey;
            String uri = spoonacular_API + urlBuilder;

            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .build();
            HttpResponse<String> response = HTTP_CLIENT
                .send(request, BodyHandlers.ofString());
            if(response.statusCode() != 200) {
                throw new IOException(response.toString());
            }

            String jsonString = response.body();
            System.out.println("********** RAW JSON STRING: **********");
            System.out.println(jsonString.trim());


            SpoonacularResponse spoonacularResponse = GSON
                .fromJson(jsonString, SpoonacularResponse.class);
            printSpoonacularResponse(spoonacularResponse);

            // parse the JSON-formatted string using GSON
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            System.err.println(e);
            e.printStackTrace();
        }
    }

    private void printSpoonacularResponse(SpoonacularResponse spoonacularResponse)    {
        try {
        System.out.println();
        System.out.println("********** PRETTY JSON STRING: **********");
        System.out.println(GSON.toJson(spoonacularResponse));
        System.out.println();
        System.out.println("********** PARSED RESULTS: **********");
        System.out.printf("number = %s\n", spoonacularResponse.number);
        for (int i = 0; i < spoonacularResponse.results.length; i++) {

            System.out.printf("spoonacularResponse.results[%d]:\n", i);
            SpoonacularResult result = spoonacularResponse.results[i];
            System.out.printf(" - id = %s\n", result.id);
            System.out.printf(" - title = %s\n", result.title);
            System.out.printf(" - image = %s\n", result.image);

            String recipeURI = "https://api.spoonacular.com/recipes/" + result.id + "/information?includeNutrition=false&apiKey=" + spoonacularApiKey;
            HttpRequest request2 = HttpRequest.newBuilder()
                .uri(URI.create(recipeURI))
                .build();
            HttpResponse<String> response2 = HTTP_CLIENT
                .send(request2, BodyHandlers.ofString());
            if(response2.statusCode() != 200) {
                throw new IOException(response2.toString());
            }
            String jsonString2 = response2.body();

            SpoonacularResponse spoonacularResponse2 = GSON
                .fromJson(jsonString2, SpoonacularResponse.class);

            for (int j = 0; j < spoonacularResponse2.results.length; j++)             {
                SpoonacularResult result2 = spoonacularResponse2.results[j];
                System.out.printf(" - sourceUrl = %s\n", result2.sourceUrl);
            }

        } // for
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            System.err.println(e);
            e.printStackTrace();
        }
    } // parseItunesResponse
} // ApiApp
