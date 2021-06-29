package vmfhrmfoaj.study;

import java.io.File;
import java.util.List;

public interface DestinationStorage {

	void store(List<File> multimediaFiles, File thumbnail, Long id);

}
