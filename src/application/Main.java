package application;
	
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;


public class Main extends Application {
	
	ExecutorService executorService;
	ServerSocket serverSocket;
	List<Client> connections = new ArrayList<Client>();
	
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
		txtDisplay.appendText(text+"\n");
	}
	public void startServer() {
		executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		try {
			serverSocket = new ServerSocket();
			serverSocket.bind(new InetSocketAddress(5001));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			if(!serverSocket.isClosed()) {
				stopServer();
			}
			return;
		}
		
		Runnable runnable = new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Platform.runLater(()->{
					displayText("[서버 시작]");
					btnStartStop.setText("stop");
				});
				
				while(true) {
					try {
						Socket socket = serverSocket.accept();
						String message = "[연결수락:"+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName()+"]";
						Platform.runLater(()->{displayText(message);});
						
						Client client = new Client(socket);
						connections.add(client);
					}catch(Exception e) {
						if(!serverSocket.isClosed()) stopServer();
						break;
					}
				}
			}
			
		};
		executorService.submit(runnable);
	}

	public void stopServer() {
		try {
			Iterator<Client> iterator = connections.iterator();
			while(iterator.hasNext()) {
				Client client = iterator.next();
				client.socket.close();
				iterator.remove();
			}
			if(serverSocket != null && !serverSocket.isClosed()) {
				serverSocket.close();
			}
			if(executorService != null && !executorService.isShutdown()) {
				executorService.shutdown();
			}
			
			Platform.runLater(()->{
				displayText("[서버 멈춤]");
				btnStartStop.setText("start");
			});
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
	public static void main(String[] args) {
		launch(args);
	}
	
	
	
	
	class Client {
		Socket socket;
		
		public Client(Socket socket) {
			this.socket = socket;
			receive();
		}
		
		void receive() {
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						while(true) {
							byte[] byteArr = new byte[100];
							InputStream inputStream = socket.getInputStream();
							
							int readByteCount = inputStream.read(byteArr);
							
							if(readByteCount == -1) {throw new IOException();}
							
							String message = "[요청 처리 :"+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName()+"]";
							Platform.runLater(()->{displayText(message);});
							
							String data = new String(byteArr, 0, readByteCount,"UTF-8");
							
							for(Client client : connections) {
								client.send(data);
							}							
						}
					} catch (Exception e) {
						// TODO: handle exception
						try {
							connections.remove(Client.this);
							String message = "[클라이언트 통신안됨: "+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName()+"]";
							Platform.runLater(()->{displayText(message);});
						} catch (Exception e2) {
							// TODO: handle exception
						}
					}
				}
			};
			executorService.submit(runnable);
		}
		void send(String data) {
			Runnable runnable = new Runnable() {
				
				@Override
				public void run() {
					// TODO Auto-generated method stub
					try {
						byte[] dataToByte = data.getBytes();
						OutputStream os = socket.getOutputStream();
						os.write(dataToByte);
						os.flush();
					} catch (Exception e) {
						// TODO: handle exception
						try {
							String message = "[클라이언트 통신 안됨 :"+socket.getRemoteSocketAddress()+": "+Thread.currentThread().getName()+"]";
							Platform.runLater(()->{displayText(message);});
							connections.remove(Client.this);
							socket.close();
						} catch (Exception e2) {
							// TODO: handle exception
						}
					}
				}
			};
			executorService.submit(runnable);
		}
	}
}
