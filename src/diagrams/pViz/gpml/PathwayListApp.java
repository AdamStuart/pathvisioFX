package diagrams.pViz.gpml;

import java.net.URL;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class PathwayListApp extends Application {

    public static void main(final String[] args) {     Application.launch(args);   }
    
    @Override public  void init() 
	{
	    instance = this;
	}

    @Override public  void start(Stage stage) throws Exception 
    {
    	theStage = stage;	//  keep a pointer to the stage used to position global dialogs, or set window title
    	doNew(theStage);	//  put this into a method so we can do it from the File menu
    }
	
	 static public PathwayListApp getInstance()	{ return instance;	}
	 static private PathwayListApp instance;
	 private Stage theStage;
	 public Stage getStage() 			{ return theStage;  }

	public void doNew(Stage stage)
	{
		if (stage == null)
			stage = new Stage();
		try 
		{
		    stage.setTitle(" Pathway Browser");
			FXMLLoader fxmlLoader = new FXMLLoader();
			String fullname = "PathwayList.fxml";
		    URL url = getClass().getResource(fullname);		// this gets the fxml file from the same directory as this class
		    if (url == null)
		    {
		    	System.err.println("Bad path to the FXML file");
		    	return;
		    }
		    fxmlLoader.setLocation(url);
		    BorderPane appPane =  fxmlLoader.load();
		    stage.setScene(new Scene(appPane, 500, 800));
		    stage.show();
		}
		catch (Exception e) { e.printStackTrace();}
		
	}

}
