package diagrams.draw;

import java.net.URL;

//import edu.stanford.nlp.util.ArrayUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

//---------------------------------------------------------------------------------------
	/*
	 * The Application in JavaFX doesn't do much except load a FXML file 
	 * and put it in a window.  Put your model setup code in init, and the steps
	 * to make a new window in start
	 */
	public class App extends Application
	{
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

	 static public App getInstance()	{ return instance;	}
	 static private App instance;
	 private Stage theStage;
	 public Stage getStage() 			{ return theStage;  }

	//---------------------------------------------------------------------------------------
	public void doNew(Stage stage)
	{
		if (stage == null)
			stage = new Stage();
		try 
		{
		    stage.setTitle("A PathVisio Mockup");
			FXMLLoader fxmlLoader = new FXMLLoader();
			String fullname = "Visio.fxml";
		    URL url = getClass().getResource(fullname);		// this gets the fxml file from the same directory as this class
		    if (url == null)
		    {
		    	System.err.println("Bad path to the FXML file");
		    	return;
		    }
		    fxmlLoader.setLocation(url);
		    BorderPane appPane =  fxmlLoader.load();
		    stage.setScene(new Scene(appPane, 1000, 800));
		    stage.show();
		}
		catch (Exception e) { e.printStackTrace();}
		
	}
 //---------------------------------------------------------------------------------------
	/*
	 * Tool lists all of the types of nodes we can put on our canvas
	 * 
	 * Actual instances are created in ShapeFactory and NodeFactory
	 */
}
