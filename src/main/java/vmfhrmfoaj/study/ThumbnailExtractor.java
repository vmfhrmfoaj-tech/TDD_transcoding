package vmfhrmfoaj.study;

import java.io.File;

public interface ThumbnailExtractor {

	File extractThumnail(File multimediaFile, Long jobId);

}
