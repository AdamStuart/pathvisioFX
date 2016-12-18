package diagrams.pViz.tables;

import diagrams.pViz.app.Controller;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

public class DraggableTableRow<Record> extends TableRow<Record> {
	private TableView<Record> table;
	private TableRow<Record> thisRow;
	private Controller controller;
	public DraggableTableRow(TableView<Record> inTable, DataFormat mimeType, Controller cntrlr)
	{
		table = inTable;
		controller = cntrlr;
		setOnDragDetected(event -> {
        if (! isEmpty()) {
            Integer index = getIndex();
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            db.setDragView(snapshot(null, null));
            ClipboardContent cc = new ClipboardContent();
            cc.put(mimeType, index);
            db.setContent(cc);
            event.consume();
            thisRow = this;
        }
    });

    setOnDragEntered(event -> {
        Dragboard db = event.getDragboard();
        if (db.hasContent(mimeType)) {
            if (getIndex() != ((Integer)db.getContent(mimeType)).intValue()) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
                thisRow = thisRow;
//              if (thisRow != null) 
//             	   thisRow.setOpacity(0.3);
            }
        }
    });

    setOnDragExited(event -> {
        if (event.getGestureSource() != thisRow &&
                event.getDragboard().hasString()) {
//           if (thisRow != null) 
//        	   thisRow.setOpacity(1);
           thisRow = null;
        }
    });

    setOnDragOver(event -> {
        Dragboard db = event.getDragboard();
        if (db.hasContent(mimeType)) {
            if (getIndex() != ((Integer)db.getContent(mimeType)).intValue()) {
                event.acceptTransferModes(TransferMode.MOVE);
                event.consume();
            }
        }
    });

    setOnMouseClicked(event -> {
    	if (event.getClickCount() == 2)
        {
            int idx = getIndex();
            controller.getInfo(mimeType, idx);
          event.consume();
        }
    });

    setOnDragDropped(event -> {
        Dragboard db = event.getDragboard();
        if (db.hasContent(mimeType)) {
            int draggedIndex = (Integer) db.getContent(mimeType);
            Record draggedNode = table.getItems().remove(draggedIndex);

            int  dropIndex = (isEmpty()) ? table.getItems().size() : getIndex();
            table.getItems().add(dropIndex, draggedNode);

            event.setDropCompleted(true);
            table.getSelectionModel().select(dropIndex);
            event.consume();
//            if (thisRow != null) 
//         	   thisRow.setOpacity(1);
            thisRow = null;
        }
    });

	}

}
