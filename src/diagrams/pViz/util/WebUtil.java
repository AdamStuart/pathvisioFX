package diagrams.pViz.util;

import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

public class WebUtil {

	public static void showURL(String jupy) {
		try 
		{
			Stage browser = new Stage();
		    WebView webview = new WebView();
		    browser.setScene(new Scene(webview, 800, 1200));
			final WebEngine webEngine = webview.getEngine();
			webEngine.getLoadWorker().stateProperty().addListener((x,y,newState) -> {
			     if (newState == Worker.State.SUCCEEDED) 
			         browser.setTitle(webEngine.getLocation());   }  );
			 webEngine.load(jupy);
			 browser.show();
		}
		catch(Exception e) { e.printStackTrace();	}
	
	}

}
