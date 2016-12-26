package diagrams.pViz.tables;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TableColumn;

public class ColumnRecord {
	StringProperty id = new SimpleStringProperty();
	StringProperty type = new SimpleStringProperty();
	StringProperty name = new SimpleStringProperty();
	DoubleProperty colWidth = new SimpleDoubleProperty();
	BooleanProperty visible = new SimpleBooleanProperty();
	BooleanProperty editable = new SimpleBooleanProperty();
	DoubleProperty score = new SimpleDoubleProperty(0);
	StringProperty formula = new SimpleStringProperty();


	
	public ColumnRecord(TableColumn<?, ?> col) {
		name.set(col.getText());
		visible.set(col.isVisible());
		editable.set(col.isEditable());
		colWidth.set(col.getPrefWidth());
	}
	
	
	public StringProperty  idProperty()  { return id;}
	public String getId()  { return id.get();}
	public void setId(String s)  { id.set(s);}

	public StringProperty  typeProperty()  { return type;}
	public String getType()  { return  type.get();		}
	public void setType(String s)  { type.set(s);}

	public StringProperty  nameProperty()  { return name;}
	public String getName()  { return name.get();}
	public void setName(String s)  { name.set(s);}

	public DoubleProperty  colWidthProperty()  { return colWidth;}
	public Double getColWidth()  { return colWidth.get();}
	public void setColWidth(Double s)  { colWidth.set(s);}

	public BooleanProperty  visibleProperty()  { return visible;}
	public Boolean getVisible()  { return visible.get();}
	public void setVisible(Boolean s)  { visible.set(s);}
	
	public BooleanProperty  editableProperty()  { return editable;}
	public Boolean getEditable()  { return editable.get();}
	public void setEditable(Boolean s)  { editable.set(s);}
	
	public DoubleProperty  scoreProperty()  { return score;}
	public Double getScore()  { return score.get();}
	public void setScore(Double s)  { score.set(s);}

	public StringProperty  formulaProperty()  { return formula;}
	public String getFormula()  { return formula.get();}
	public void setFormula(String s)  { formula.set(s);}

	public String toString()	{		return id.get() + ": " + name.get() + (visible.get() ? " visible" : " not visible")
			 + (editable.get() ? " editable" : " not editable");	}
}
