package diagrams.pViz.tables;

import diagrams.pViz.app.IController;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;


public class DraggableTableRow<IRecord> extends TableRow<IRecord> {
	private TableView<IRecord> table;
	private TableRow<IRecord> thisRow;
	private IController controller;
	public DraggableTableRow(TableView<IRecord> inTable, DataFormat mimeType, IController cntrlr)
	{
		table = inTable;
		controller = cntrlr;
		setOnDragDetected(event -> {
        if (! isEmpty()) {
            Integer index = getIndex();
            IRecord r = table.getItems().get(index);
            String id = "";
            if (r != null)
            	id = r.toString();
            Dragboard db = startDragAndDrop(TransferMode.MOVE);
            db.setDragView(snapshot(null, null));
            ClipboardContent cc = new ClipboardContent();
            cc.put(mimeType, index);
            cc.put(DataFormat.PLAIN_TEXT, id);
            db.setContent(cc);
            event.consume();
            thisRow = (TableRow<IRecord>) this;
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
            IRecord r = table.getItems().get(idx);		// TODO -- need interface to get Id
            String colName = getColumnId(event.getX());
            if (controller != null) 
            	controller.getInfo(mimeType, "" + idx, colName, event);	//r.getId()
          event.consume();
        }
    });

    setOnDragDropped(event -> {
        Dragboard db = event.getDragboard();
        if (db.hasContent(mimeType)) {
            int draggedIndex = (Integer) db.getContent(mimeType);
            IRecord draggedNode = table.getItems().remove(draggedIndex);

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
	public  String getColumnId(double x)
    {
    	for (TableColumn col : table.getColumns())
    	{
    		double width = col.getWidth();
    		if (width > x) return col.getId();
    		x -= width;
    	}
     	return "";
    }

}
