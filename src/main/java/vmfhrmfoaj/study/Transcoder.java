package vmfhrmfoaj.study;

import java.io.File;
import java.util.List;

public interface Transcoder {

	List<File> transcode(File multimediaFile, Long jobId);

}
