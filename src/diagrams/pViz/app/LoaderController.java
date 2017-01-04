package diagrams.pViz.app;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;

public class LoaderController implements IController, Initializable {

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

	}

	@Override
	public void getInfo(DataFormat fmt, String a, String colname, MouseEvent event) {
	}
	
	
	
	@FXML private void newGeneList()	{ 	App.doNewGeneList(null);	}
	@FXML private void newPathwayList()	{ 	App.browsePathways(null);	}
	@FXML private void newReferenceList(){ 	App.openReferenceList();	}
	@FXML private void enableLogging(){ 		}

}
