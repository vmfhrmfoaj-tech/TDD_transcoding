package vmfhrmfoaj.study;

import java.io.File;
import java.util.List;

public interface CreatedFileSender {

	void send(List<File> multimediaFiles, File thumbnail, Long jobId);

}
