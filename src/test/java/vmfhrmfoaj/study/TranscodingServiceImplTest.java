package vmfhrmfoaj.study;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import vmfhrmfoaj.study.Job.State;

@RunWith(MockitoJUnitRunner.class)
public class TranscodingServiceImplTest {
	
	private Long jobId = new Long(1);

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
	
	@Mock
	private JobRepository jobRepository;
	
	private TranscodingService transcodingService;
	
	private Job mockJob = new Job(jobId);

	File mockMultimediaFile = mock(File.class);
	
	List<File> mockMultimediaFiles = new ArrayList<File>();
	
	File mockThumnailFile = mock(File.class);
	
	RuntimeException mockException = new RuntimeException();
	
	@Before
	public void setup() {
		transcodingService = new TranscodingServiceImpl(jobRepository, mediaSourceCopier, transcoder, thumbnailExtractor, createdFileSender, jobResultNotifier);
		
		when(jobRepository.findById(jobId)).thenReturn(mockJob);
		
		when(mediaSourceCopier.copy(jobId)).thenReturn(mockMultimediaFile);
		
		when(transcoder.transcode(mockMultimediaFile, jobId)).thenReturn(mockMultimediaFiles);
		
		when(thumbnailExtractor.extractThumnail(mockMultimediaFile, jobId)).thenReturn(mockThumnailFile);
	}

	@Test
	public void transcodeSuccessfully() {
		
		assertJobIsWaitingState();
		
		transcodingService.transcode(jobId);
		
		Job job = jobRepository.findById(jobId);
		assertTrue(job.isFinished());
		assertTrue(job.isSuccess());
		assertEquals(Job.State.COMPLETED, job.isLastState());
		assertNull(job.getOccerredException());
		
		VerifyOption verifyOption = new VerifyOption();
        verifyCollaboration(verifyOption);
	}

	@Test
	public void transcodeFailBecauseExceptionOccuredAtMediaSourceCopier() {
		
		when(mediaSourceCopier.copy(jobId)).thenThrow(mockException);
		
		assertJobIsWaitingState();
		
		executeFailingTranscodeAndAssertFail(Job.State.MEDIASOURCECOPYING);
		
		VerifyOption verifyOption = new VerifyOption();
        verifyOption.transcoderNever = true;
        verifyOption.thumbnailExtractorNever = true;
        verifyOption.createdFileSenderNever = true;
        verifyOption.jobResultNotifierNever = true;
        verifyCollaboration(verifyOption);
	}
	
	@Test
	public void transcodeFailBecauseExceptionOccuredAtTranscoder() {
		
		when(transcoder.transcode(mockMultimediaFile, jobId)).thenThrow(mockException);
		
		assertJobIsWaitingState();
		
		executeFailingTranscodeAndAssertFail(Job.State.TRANSCODING);
		
		VerifyOption verifyOption = new VerifyOption();
        verifyOption.thumbnailExtractorNever = true;
        verifyOption.createdFileSenderNever = true;
        verifyOption.jobResultNotifierNever = true;
        verifyCollaboration(verifyOption);
	}
	
	@Test
	public void transcodeFailBecauseExceptionOccuredAtThumbnailExtract() {
		
		when(thumbnailExtractor.extractThumnail(mockMultimediaFile, jobId)).thenThrow(mockException);
		
		assertJobIsWaitingState();
		
		executeFailingTranscodeAndAssertFail(Job.State.THUMBNAILEXTRACTING);
		
		VerifyOption verifyOption = new VerifyOption();
        verifyOption.createdFileSenderNever = true;
        verifyOption.jobResultNotifierNever = true;
        verifyCollaboration(verifyOption);
	}
	
	@Test
	public void transcodeFailBecauseExceptionOccuredAtCreatedFileSender() {
		
		Mockito.doThrow(mockException).when(createdFileSender).send(mockMultimediaFiles, mockThumnailFile, jobId);
		
		assertJobIsWaitingState();
		
		executeFailingTranscodeAndAssertFail(Job.State.CREATEDFILESEND);
		
		VerifyOption verifyOption = new VerifyOption();
        verifyOption.jobResultNotifierNever = true;
        verifyCollaboration(verifyOption);
	}
	
	@Test
	public void transcodeFailBecauseExceptionOccuredAtjobResultNotifier() {
		
		Mockito.doThrow(mockException).when(jobResultNotifier).notifyJob(jobId);
		
		assertJobIsWaitingState();
		
		executeFailingTranscodeAndAssertFail(Job.State.JOBRESULTNOTIFY);
		
		VerifyOption verifyOption = new VerifyOption();
        verifyCollaboration(verifyOption);
	}
	
	private void assertJobIsWaitingState() {
		
		Job job = jobRepository.findById(jobId);
		assertTrue(job.isWaiting());
	}

	private void executeFailingTranscodeAndAssertFail(State state) {

		try {
			transcodingService.transcode(jobId);
			fail("발생해야함");
		} catch (Exception e) {
			assertSame(mockException, e);
		}
		
		Job job = jobRepository.findById(jobId);
		assertTrue(job.isFinished());
		assertFalse(job.isSuccess());
		assertEquals(state, job.isLastState());
		assertNotNull(job.getOccerredException());
	}
	
	private void verifyCollaboration(VerifyOption opt) {

		verify(mediaSourceCopier, only()).copy(jobId);
		
		if( opt.transcoderNever ) {
			verify(transcoder, never()).transcode(any(File.class), anyLong());			
		}else {
			verify(transcoder, only()).transcode(mockMultimediaFile, jobId);			
		}
		
		if( opt.thumbnailExtractorNever) {
			verify(thumbnailExtractor, never()).extractThumnail(any(File.class), anyLong());
		}else {
			verify(thumbnailExtractor, only()).extractThumnail(mockMultimediaFile, jobId);
		}
		
		if( opt.createdFileSenderNever) {
			verify(createdFileSender, never()).send(anyListOf(File.class), any(File.class), anyLong());
		}else {
			verify(createdFileSender, only()).send(mockMultimediaFiles, mockThumnailFile, jobId);
		}
		
		if( opt.jobResultNotifierNever) {
			verify(jobResultNotifier, never()).notifyJob(anyLong());
		}else {
			verify(jobResultNotifier, only()).notifyJob(jobId);
		}
		
	}

	
	class VerifyOption{

		public boolean jobResultNotifierNever = false;
		public boolean createdFileSenderNever = false;
		public boolean thumbnailExtractorNever = false;
		public boolean transcoderNever = false;
		
	}
}
