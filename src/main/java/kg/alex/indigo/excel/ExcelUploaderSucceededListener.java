package kg.alex.indigo.excel;

import com.vaadin.ui.Upload;

import java.util.List;

public interface ExcelUploaderSucceededListener<T> {

    public void succeededListener(Upload.SucceededEvent event, List<T> items);

}
