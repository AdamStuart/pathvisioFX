package diagrams.pViz.gpml;

import diagrams.pViz.app.GPMLRecord;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.input.DataFormat;

public class CommentRecord implements GPMLRecord {

	StringProperty source = new SimpleStringProperty();
	StringProperty text = new SimpleStringProperty();

	public StringProperty  sourceProperty()  { return source;}
	public String getSource()  { return source.get();}
	public void setSource(String s)  { source.set(s);}

	public StringProperty  textProperty()  { return text;}
	public String getText()  { return text.get();}
	public void setText(String s)  { text.set(s);}

	public CommentRecord(String src, String txt)
	{
		source.set(src);
		text.set(txt);
	}
	
	@Override
	public void getInfo(DataFormat mimetype, String a, String b) {
		
		System.out.println("CommentRecord.getInfo: " + a + " " + b);

	}
	public String toGPML() {
		return "<Comment Source=\"" + getSource() + "\">" + getText() + "</Comment>\n";
	}

}
