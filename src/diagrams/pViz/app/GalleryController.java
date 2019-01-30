package diagrams.pViz.app;

import java.net.URL;
import java.util.ResourceBundle;

import icon.FontAwesomeIcons;
import icon.GlyphIcon;
import icon.GlyphsDude;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import model.IController;

public class GalleryController implements Initializable {
	@FXML private ChoiceBox<String> speciesChoices;
	@FXML private Button search;
	@FXML private TextField searchBox;
	static String lastSearch = "cell cycle";
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO
		String[] organisms = { "Any", "Homo sapiens", "Mus musculus", "Rattus norvegicus", "Canis familiarus", "Box taurus", "Pan troglodytes", "Gallus gallus" };
		speciesChoices.getItems().addAll(organisms);
		speciesChoices.getSelectionModel().select(1);
		
		String tooltip = "A local, case-insensitive search within this pathway."; 		// TODO
		search.setGraphic(GlyphsDude.createIcon(FontAwesomeIcons.SEARCH, GlyphIcon.DEFAULT_ICON_SIZE));
		search.setText("");
		search.setTooltip(new Tooltip(tooltip));
		searchBox.setTooltip(new Tooltip(tooltip));
		searchBox.setText(lastSearch);
		searchBox.addEventHandler(KeyEvent.KEY_PRESSED, e -> {  if (e.getCode() == KeyCode.ENTER) doSearch();	} );
		searchBox.selectAll();


	}
	private String state;
	public void setState(String s) { 	state = s; }
	public String getState() 	{ 	return state; 	}
	@FXML public void doSearch()	
	{}
	
}
