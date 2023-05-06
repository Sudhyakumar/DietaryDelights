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

    HBox topHeader;
    TextField foodUser;
    ComboBox<String> dietUser;
    ComboBox<String> countryUser;
    Button search;

    Label status;

    VBox resultBox;
    HBox topHBox;

    VBox recipe1;
    ImageView imageView1;
    Label titleLabel1;
    Label urlLabel1;
    Label priceLabel1;


    VBox recipe2;
    ImageView imageView2;
    Label titleLabel2;
    Label urlLabel2;
    Label priceLabel2;

    HBox bottomHBox;
    VBox recipe3;
    ImageView imageView3;
    Label titleLabel3;
    Label urlLabel3;
    Label priceLabel3;

    VBox recipe4;
    ImageView imageView4;
    Label titleLabel4;
    Label urlLabel4;
    Label priceLabel4;

    String recipeImage;
    String recipeTitle;
    String recipeLink;
    double priceConverted;
    String currencyCode;

    /**
     * Constructs an {@code ApiApp} object. This default (i.e., no argument)
     * constructor is executed in Step 2 of the JavaFX Application Life-Cycle.
     */
    public ApiApp() {
        this.stage = null;
        this.scene = null;
        this.root = new VBox();

        this.topHeader = new HBox();
        Label title = new Label("Personalized Recipe Finder");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        this.status = new Label("Type a food you would like a recipe for such as Pasta. You must also select a diet. You can also choose a currency to find out the cost of the ingredients for each recipe in different currencies. Click \"Search\" to load the top 4 recipe results...");
        foodUser = new TextField();
        foodUser.setPromptText("Enter Food: ");
        dietUser = new ComboBox<>();
        dietUser.setPromptText("Select Diet: ");
        dietUser.getItems().addAll("Gluten Free", "Ketogenic", "Vegetarian", "Lacto-Vegetarian",
        "Ovo-Vegetarian", "Vegan", "Pescatarian","Paleo", "Primal");
        countryUser = new ComboBox<>();
        countryUser.setPromptText("Select Country for Currency Exchange");
        countryUser.getItems().addAll("United States Dollar(USD)", "Australian Dollar(AUD)",
        "Brazilian Real(BRL)", "British Pound Sterling(GBP)", "Canadian Dollar(CAD)",
        "Chinese Yuan(CNY)", "Euro(EUR)", "Indian Rupee(INR)", "Japanese Yen(JPY)",
        "Mexican Peso(MXN)", "Russia Ruble(RUB)");

        search = new Button("Search");
        //status = new Label();
        this.resultBox = new VBox();
        this.topHBox = new HBox();

        this.recipe1 = new VBox();
        this.imageView1 = new ImageView();
        this.titleLabel1 = new Label();
        this.urlLabel1 = new Label();
        this.priceLabel1 = new Label();;

        this.recipe2 = new VBox();
        this.imageView2 = new ImageView();
        this.titleLabel2 = new Label();
        this.urlLabel2 = new Label();
        this.priceLabel2 = new Label();
        this.bottomHBox = new HBox();

        this.recipe3 = new VBox();
        this.imageView3 = new ImageView();
        this.titleLabel3 = new Label();
        this.urlLabel3 = new Label();
        this.priceLabel3 = new Label();

        this.recipe4 = new VBox();
        this.imageView4 = new ImageView();
        this.titleLabel4 = new Label();
        this.urlLabel4 = new Label();
        this.priceLabel4 = new Label();

    } // ApiApp


    /**{@inheritDoc}*/
    @Override
    public void init() {
        this.topHeader.getChildren().addAll(foodUser, dietUser, countryUser, search);
        this.recipe1.getChildren().addAll(imageView1, titleLabel1, urlLabel1, priceLabel1);
        this.recipe2.getChildren().addAll(imageView2, titleLabel2, urlLabel2, priceLabel2);
        this.topHBox.getChildren().addAll(recipe1, recipe2);
        this.recipe3.getChildren().addAll(imageView3, titleLabel3, urlLabel3, priceLabel3);
        this.recipe4.getChildren().addAll(imageView4, titleLabel4, urlLabel4, priceLabel4);
        this.bottomHBox.getChildren().addAll(recipe3, recipe4);
        this.resultBox.getChildren().addAll(topHBox, bottomHBox);
        //this.textFlow.getChildren().add(new Text("Click \"Search\" to load the recipe results..."));
        this.root.getChildren().addAll(topHeader, status, this.resultBox);
    } // init

    /** {@inheritDoc} */
    @Override
    public void start(Stage stage) {
        this.stage = stage;

        search.setOnAction(event -> searchResults());
        //exchangeRateAPITest();
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
        Runnable loadTask = () -> {
            search.setDisable(true);
            startLoading();
        //status.setText("Loading...");
        try {
            String spoonacular_API = "https://api.spoonacular.com/recipes/complexSearch?query=";
            String food = URLEncoder.encode(foodUser.getText(), StandardCharsets.UTF_8);
            String diet = URLEncoder.encode(dietUser.getValue(), StandardCharsets.UTF_8);

            String urlBuilder = new String();
            urlBuilder += food;

            if (!diet.isEmpty()) {
             urlBuilder+="&diet=";
              urlBuilder+=diet;
            }

            urlBuilder+="&number=4&apiKey=";
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

            ComplexSearchResponse complexSearchResponse = GSON
                .fromJson(jsonString, ComplexSearchResponse.class);
            if(complexSearchResponse.results.length < 4) {
                throw new IllegalArgumentException("Less than 4 recipes were found. Please try another search.");
            }
            //printRecipeInfoResponse(complexSearchResponse);

            for (int i = 0; i < complexSearchResponse.results.length; i++) {
                ComplexSearchResult result = complexSearchResponse.results[i];

                String recipeURI = "https://api.spoonacular.com/recipes/" + result.id + "/information?includeNutrition=false&apiKey=" + spoonacularApiKey;
                HttpRequest requestRecipeInfo = HttpRequest.newBuilder()
                    .uri(URI.create(recipeURI))
                    .build();
                HttpResponse<String> responseRecipeInfo = HTTP_CLIENT
                    .send(requestRecipeInfo, BodyHandlers.ofString());
                if(responseRecipeInfo.statusCode() != 200) {
                    throw new IOException(responseRecipeInfo.toString());
                }
                String jsonString2 = responseRecipeInfo.body();

                RecipeInfoResponse recipeInfoResponse = GSON
                    .fromJson(jsonString2, RecipeInfoResponse.class);
                currencyCode = countryToCode(countryUser.getValue());
                String exchange_url_str = "https://api.exchangerate.host/convert?from=USD&to=" + currencyCode +"&amount=" + recipeInfoResponse.pricePerServing ;

                HttpRequest exchangeRequest = HttpRequest.newBuilder()
                    .uri(URI.create(exchange_url_str))
                    .build();

                HttpResponse<String> exchange_Response = HTTP_CLIENT.send(exchangeRequest, BodyHandlers.ofString());

                if(response.statusCode() != 200) {
                    throw new IOException(exchange_Response.toString());
                }
                String exchangeJsonString = exchange_Response.body();

                ExchangeResponse exchangeResponse = GSON.fromJson(exchangeJsonString, ExchangeResponse.class);

                recipeImage = recipeInfoResponse.image;
                recipeTitle = recipeInfoResponse.title;
                recipeLink = recipeInfoResponse.sourceUrl;
                priceConverted = Math.round(exchangeResponse.result * Math.pow(10, 2))/ Math.pow(10,2);

                setRecipes(i, recipeImage, recipeTitle, recipeLink, priceConverted);

                System.out.printf(" - id = %s\n", recipeInfoResponse.id);
                System.out.printf(" - title = %s\n", recipeInfoResponse.title);
                System.out.printf(" - image = %s\n", recipeInfoResponse.image);
                System.out.printf(" - imageType = %s\n", recipeInfoResponse.imageType);
                System.out.printf(" - sourceUrl = %s\n", recipeInfoResponse.sourceUrl);
                System.out.printf(" - pricePerServing = %s\n", recipeInfoResponse.pricePerServing);
                endLoading();
            }

        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            errorLoading();
            alertError(e);
            System.err.println(e);
            e.printStackTrace();
        }
        };
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
        //search.setDisable(false);
        //status.setText("Done!");
    }

    private void setRecipes(int i, String image, String title, String link, double price) {
        Runnable task = () -> {
        if(i == 0) {
                    this.recipe1.setSpacing(10);
                    this.recipe1.setAlignment(Pos.CENTER);

                    // Create and add ImageView
                    imageView1.setImage(new Image(image, 200, 200, true, true));

                    // Create and add title label
                    titleLabel1.setText(title);
                    titleLabel1.setStyle("-fx-font-size: 12pt; -fx-font-weight: bold;");

                    // Create and add URL label
                    urlLabel1.setText(link);
                    urlLabel1.setStyle("-fx-font-size: 8pt;");

                    // Create and add price label
                    priceLabel1.setText(price + " " + currencyCode);
                    priceLabel1.setStyle("-fx-font-size: 8pt; -fx-font-weight: bold;");

                } else if (i == 1) {
                    this.recipe2.setSpacing(10);
                    this.recipe2.setAlignment(Pos.CENTER);

                    // Create and add ImageView
                    imageView2.setImage(new Image(image, 200, 200, true, true));

                    // Create and add title label
                    titleLabel2.setText(title);
                    titleLabel2.setStyle("-fx-font-size: 12pt; -fx-font-weight: bold;");

                    // Create and add URL label
                    urlLabel2.setText(link);
                    urlLabel2.setStyle("-fx-font-size: 8pt;");

                    // Create and add price label
                    priceLabel2.setText(price + " " + currencyCode);
                    priceLabel2.setStyle("-fx-font-size: 8pt; -fx-font-weight: bold;");
                } else if (i == 2) {
                    this.recipe3.setSpacing(10);
                    this.recipe3.setAlignment(Pos.CENTER);

                    // Create and add ImageView
                    imageView3.setImage(new Image(image, 200, 200, true, true));

                    // Create and add title label
                    titleLabel3.setText(title);
                    titleLabel3.setStyle("-fx-font-size: 12pt; -fx-font-weight: bold;");

                    // Create and add URL label
                    urlLabel3.setText(link);
                    urlLabel3.setStyle("-fx-font-size: 8pt;");

                    // Create and add price label
                    priceLabel3.setText(price + " " + currencyCode);
                    priceLabel3.setStyle("-fx-font-size: 8pt; -fx-font-weight: bold;");

                } else if(i == 3) {
                    this.recipe4.setSpacing(10);
                    this.recipe4.setAlignment(Pos.CENTER);

                    // Create and add ImageView
                    imageView4.setImage(new Image(image, 200, 200, true, true));

                    // Create and add title label
                    titleLabel4.setText(title);
                    titleLabel4.setStyle("-fx-font-size: 12pt; -fx-font-weight: bold;");

                    // Create and add URL label
                    urlLabel4.setText(link);
                    urlLabel4.setStyle("-fx-font-size: 8pt;");

                    // Create and add price label
                    priceLabel4.setText(price +  " " + currencyCode);
                    priceLabel4.setStyle("-fx-font-size: 8pt; -fx-font-weight: bold;");
                }
        };
        Platform.runLater(task);
    }


    private void startLoading() {
        Runnable task = () -> {
            search.setDisable(true);
            status.setText("Loading...");
        };
        Platform.runLater(task);
    }

    private void endLoading() {
        Runnable task = () -> {
            search.setDisable(false);
            status.setText("Done!");
        };
        Platform.runLater(task);
    }

    private void errorLoading() {
        Runnable task = () -> {
            search.setDisable(false);
            status.setText("Last attempt to get recipes failed...");
        };
        Platform.runLater(task);
    }
    public static void alertError(Throwable cause) {
        TextArea text = new TextArea("Exception: " + cause.toString());
        Runnable task = () -> {
            text.setEditable(false);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.getDialogPane().setContent(text);
            alert.setResizable(true);
        };
        Platform.runLater(task);
    }

    private String countryToCode(String currency){
        if(currency == "United States Dollar(USD)") {
            return "USD";
        } else if(currency == "Australian Dollar(AUD)") {
            return "AUD";
        } else if(currency == "Brazilian Real(BRL)") {
            return "BRL";
        } else if (currency == "British Pound Sterling(GBP)") {
            return "GBP";
        } else if(currency == "Canadian Dollar(CAD)") {
            return "CAD";
        } else if(currency == "Chinese Yuan(CNY)") {
            return "CNY";
        } else if(currency == "Euro(EUR)") {
            return "EUR";
        } else if(currency == "Indian Rupee(INR)") {
            return "INR";
        } else if(currency == "Japanese Yen(JPY)") {
            return "JPY";
        } else if(currency == "Mexican Peso(MXN)") {
            return "MXN";
        } else if(currency == "Russia Ruble(RUB)") {
            return "RUB";
        } else {
            return "USD";
        }
    }

    private void exchangeRateAPITest(){
        try {
            String url_str = "https://api.exchangerate.host/convert?from=USD&to=EUR&amount=1200";

        URL url = new URL(url_str);
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(url_str))
            .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, BodyHandlers.ofString());

        if(response.statusCode() != 200) {
            throw new IOException(response.toString());
        }
        String jsonString = response.body();

        ExchangeResponse exchangeResponse = GSON.fromJson(jsonString, ExchangeResponse.class);

        System.out.println(exchangeResponse.result);
        } catch(IOException | InterruptedException e) {
            System.err.println(e);
            e.printStackTrace();

        }
    }

    private void printRecipeInfoResponse(ComplexSearchResponse complexSearchResponse)    {
        try {
        System.out.println();
        System.out.println("********** PRETTY JSON STRING: **********");
        System.out.println(GSON.toJson(complexSearchResponse));
        System.out.println();
        System.out.println("********** PARSED RESULTS: **********");
        System.out.printf("number = %s\n", complexSearchResponse.number);
        for (int i = 0; i < complexSearchResponse.results.length; i++) {

            System.out.printf("complexSearchResponse.results[%d]:\n", i);
            ComplexSearchResult result = complexSearchResponse.results[i];
            //System.out.printf(" - id = %s\n", result.id);
            //System.out.printf(" - title = %s\n", result.title);
            //System.out.printf(" - image = %s\n", result.image);

            String recipeURI = "https://api.spoonacular.com/recipes/" + result.id + "/information?includeNutrition=false&apiKey=" + spoonacularApiKey;
            HttpRequest requestRecipeInfo = HttpRequest.newBuilder()
                .uri(URI.create(recipeURI))
                .build();
            HttpResponse<String> responseRecipeInfo = HTTP_CLIENT
                .send(requestRecipeInfo, BodyHandlers.ofString());
            if(responseRecipeInfo.statusCode() != 200) {
                throw new IOException(responseRecipeInfo.toString());
            }
            String jsonString2 = responseRecipeInfo.body();

            RecipeInfoResponse recipeInfoResponse = GSON
                .fromJson(jsonString2, RecipeInfoResponse.class);
            System.out.printf(" - id = %s\n", recipeInfoResponse.id);
            System.out.printf(" - title = %s\n", recipeInfoResponse.title);
            System.out.printf(" - image = %s\n", recipeInfoResponse.image);
            System.out.printf(" - imageType = %s\n", recipeInfoResponse.imageType);
            System.out.printf(" - sourceUrl = %s\n", recipeInfoResponse.sourceUrl);
            System.out.printf(" - pricePerServing = %s\n", recipeInfoResponse.pricePerServing);

        } // for
        } catch (IOException | InterruptedException | IllegalArgumentException e) {
            System.err.println(e);
            e.printStackTrace();
        }
    } // parseItunesResponse
} // ApiApp
