package vmfhrmfoaj.study;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class TranscodingServiceTest {

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

	@Test
	public void transcodeSuccessfully() {
		Long jobId = new Long(1);
		
		File mockMultimediaFile = mock(File.class);
		when(mediaSourceCopier.copy(jobId)).thenReturn(mockMultimediaFile);
		
		List<File> mockMultimediaFiles = new ArrayList<File>();
		when(transcoder.transcode(mockMultimediaFile, jobId)).thenReturn(mockMultimediaFiles);
		
		File mockThumnailFile = mock(File.class);
		when(thumbnailExtractor.extractThumnail(mockMultimediaFile, jobId)).thenReturn(mockThumnailFile);
		
		transcode(jobId);
		
		verify(mediaSourceCopier, only()).copy(jobId);
		verify(transcoder, only()).transcode(mockMultimediaFile, jobId);
		verify(thumbnailExtractor, only()).extractThumnail(mockMultimediaFile, jobId);
		verify(createdFileSender, only()).send(mockMultimediaFiles, mockThumnailFile, jobId);
		verify(jobResultNotifier, only()).notifyJob(jobId);
	}

	private void transcode(Long jobId) {

		// 미디어 원본으로부터 파일을 로컬에 복사한다.
		File multimediaFile = copyMultimediaSourceToLocal(jobId);
		
		// 로컬에 복사된 파일을 변환처리한다.
		List<File> multimediaFiles = transcode(multimediaFile, jobId);
		
		// 로컬에 복사된 파일로부터 이미지를 추출한다.
		File thumbnail = extractThumbnail(multimediaFile, jobId);
		
		// 변환된 결과 파일과 썸네일 이미지를 목적지에 저장
		sendCreatedFilesToDestination(multimediaFiles, thumbnail, jobId);
		
		// 결과를 통보
		notifyJob(jobId);
	}

	private void notifyJob(Long jobId) {
		jobResultNotifier.notifyJob(jobId);
	}

	private void sendCreatedFilesToDestination(List<File> multimediaFiles, File thumbnail, Long jobId) {
		createdFileSender.send(multimediaFiles, thumbnail, jobId);
	}

	private File extractThumbnail(File multimediaFile, Long jobId) {
		return thumbnailExtractor.extractThumnail(multimediaFile, jobId);
	}

	private List<File> transcode(File multimediaFile, Long jobId) {
		return transcoder.transcode(multimediaFile, jobId);
	}

	private File copyMultimediaSourceToLocal(Long jobId) {
		return mediaSourceCopier.copy(jobId);
	}
}
