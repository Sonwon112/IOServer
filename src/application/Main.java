package application;
	
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {
	TextArea txtDisplay;
	Button btnStartStop;
	
	
	@Override
	public void start(Stage primaryStage) {
		BorderPane root = new BorderPane();
		root.setPrefSize(500, 300);
		
		txtDisplay = new TextArea();
		txtDisplay.setEditable(false);
		BorderPane.setMargin(txtDisplay, new Insets(0,0,2,0));
		root.setCenter(txtDisplay);
		
		btnStartStop = new Button();
		btnStartStop.setPrefHeight(30);
		btnStartStop.setMaxWidth(Double.MAX_VALUE);
		btnStartStop.setText("start");
		btnStartStop.setOnAction(e->{
			if(btnStartStop.getText().equals("start")){
				startServer();
			}else if(btnStartStop.getText().equals("stop")){
				stopServer();
			}
		});
		
		root.setBottom(btnStartStop);
		
		Scene scene = new Scene(root);
		scene.getStylesheets().add(getClass().getResource("application.css").toString());
		primaryStage.setScene(scene);
		primaryStage.setTitle("Server");
		primaryStage.setOnCloseRequest(e->stopServer());
		primaryStage.show();
	}
	
	public void displayText(String text) {
		txtDisplay.appendText(text);
	}
	
	public void startServer() {
		
	}
	
	public void stopServer() {
		
	}
	public static void main(String[] args) {
		launch(args);
	}
}
