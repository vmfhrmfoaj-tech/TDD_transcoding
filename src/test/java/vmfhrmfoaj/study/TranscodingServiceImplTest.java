package vmfhrmfoaj.study;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TranscodingServiceImplTest {

	@Mock
	private MediaSourceCopier mediaSourceCopier;
	
	@Mock
	private Transcoder transcoder;
	
	@Mock
	private ThumbnailExtractor thumbnailExtractor;
	
	@Mock
	private CreatedFileSender createdFileSender;
	
	@Mock
	private JobResultNotifier jobResultNotifier;
	
	private TranscodingService transcodingService;
	
	@Before
	public void setup() {
		transcodingService = new TranscodingServiceImpl(mediaSourceCopier, transcoder, thumbnailExtractor, createdFileSender, jobResultNotifier);
	}

	@Test
	public void transcodeSuccessfully() {
		Long jobId = new Long(1);
		
		File mockMultimediaFile = mock(File.class);
		when(mediaSourceCopier.copy(jobId)).thenReturn(mockMultimediaFile);
		
		List<File> mockMultimediaFiles = new ArrayList<File>();
		when(transcoder.transcode(mockMultimediaFile, jobId)).thenReturn(mockMultimediaFiles);
		
		File mockThumnailFile = mock(File.class);
		when(thumbnailExtractor.extractThumnail(mockMultimediaFile, jobId)).thenReturn(mockThumnailFile);
		
		transcodingService.transcode(jobId);
		
		verify(mediaSourceCopier, only()).copy(jobId);
		verify(transcoder, only()).transcode(mockMultimediaFile, jobId);
		verify(thumbnailExtractor, only()).extractThumnail(mockMultimediaFile, jobId);
		verify(createdFileSender, only()).send(mockMultimediaFiles, mockThumnailFile, jobId);
		verify(jobResultNotifier, only()).notifyJob(jobId);
	}
}
