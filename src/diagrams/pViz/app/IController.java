package diagrams.pViz.app;

import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseEvent;
import model.bio.Species;

public interface IController {
	public void getInfo(DataFormat fmt, String a, String colname, MouseEvent event);
}
