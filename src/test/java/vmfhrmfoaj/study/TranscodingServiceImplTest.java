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
import static org.mockito.Mockito.doAnswer;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

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
	private JobStateChanger jobStateChanger;

	@Mock
	private JobRepository jobRepository;
	
	@Mock
	private JobExceptionHander jobExceptionHander;
	
	private TranscodingService transcodingService;
	
	private Job mockJob = new Job();

	File mockMultimediaFile = mock(File.class);
	
	List<File> mockMultimediaFiles = new ArrayList<File>();
	
	File mockThumnailFile = mock(File.class);
	
	@Before
	public void setup() {
		transcodingService = new TranscodingServiceImpl(mediaSourceCopier, transcoder, thumbnailExtractor, createdFileSender, jobResultNotifier, jobStateChanger, jobExceptionHander);
		
		doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				
				State newState = (State)invocation.getArguments()[1];
				mockJob.changeState(newState);
				return null;
			}
			
		}).when(jobStateChanger).changeJobState(anyLong(),  any(Job.State.class));;
		
		doAnswer(new Answer<Object>() {

			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				
				RuntimeException ex = (RuntimeException)invocation.getArguments()[1];
				mockJob.exceptionOccurred(ex);
				return null;
			}
			
		}).when(jobExceptionHander).notifyJobException(anyLong(), any(RuntimeException.class));
		
		when(jobRepository.findById(jobId)).thenReturn(mockJob);
		
		when(mediaSourceCopier.copy(jobId)).thenReturn(mockMultimediaFile);
		
		when(transcoder.transcode(mockMultimediaFile, jobId)).thenReturn(mockMultimediaFiles);
		
		when(thumbnailExtractor.extractThumnail(mockMultimediaFile, jobId)).thenReturn(mockThumnailFile);
	}

	@Test
	public void transcodeSuccessfully() {
		
		Job job = jobRepository.findById(jobId);
		assertTrue(job.isWaiting());
		
		transcodingService.transcode(jobId);
		assertTrue(job.isFinished());
		assertTrue(job.isSuccess());
		assertEquals(Job.State.COMPLETED, job.isLastState());
		assertNull(job.getOccerredException());
		
		verify(mediaSourceCopier, only()).copy(jobId);
		verify(transcoder, only()).transcode(mockMultimediaFile, jobId);
		verify(thumbnailExtractor, only()).extractThumnail(mockMultimediaFile, jobId);
		verify(createdFileSender, only()).send(mockMultimediaFiles, mockThumnailFile, jobId);
		verify(jobResultNotifier, only()).notifyJob(jobId);
	}
	
	@Test
	public void transcodeFailBecauseExceptionOccuredAtMediaSourceCopier() {
		
		RuntimeException mockException = new RuntimeException();
		when(mediaSourceCopier.copy(jobId)).thenThrow(mockException);
		
		try {
			transcodingService.transcode(jobId);
			fail("발생해야함");
		} catch (Exception e) {
			assertSame(mockException, e);
		}
		
		Job job = jobRepository.findById(jobId);
		assertTrue(job.isFinished());
		assertFalse(job.isSuccess());
		assertEquals(Job.State.MEDIASOURCECOPYING, job.isLastState());
		assertNotNull(job.getOccerredException());
		
		verify(mediaSourceCopier, only()).copy(jobId);
		verify(transcoder, never()).transcode(any(File.class), anyLong());
		verify(thumbnailExtractor, never()).extractThumnail(any(File.class), anyLong());
		verify(createdFileSender, never()).send(anyListOf(File.class), any(File.class), anyLong());
		verify(jobResultNotifier, never()).notifyJob(anyLong());
	}
	
	@Test
	public void transcodeFailBecauseExceptionOccuredAtTranscoder() {
		
		RuntimeException mockException = new RuntimeException();
		when(transcoder.transcode(mockMultimediaFile, jobId)).thenThrow(mockException);
		
		try {
			transcodingService.transcode(jobId);
			fail("발생해야함");
		} catch (Exception e) {
			assertSame(mockException, e);
		}
		
		Job job = jobRepository.findById(jobId);
		assertTrue(job.isFinished());
		assertFalse(job.isSuccess());
		assertEquals(Job.State.TRANSCODING, job.isLastState());
		assertNotNull(job.getOccerredException());
		
		verify(mediaSourceCopier, only()).copy(jobId);
		verify(transcoder, only()).transcode(mockMultimediaFile, jobId);
		verify(thumbnailExtractor, never()).extractThumnail(any(File.class), anyLong());
		verify(createdFileSender, never()).send(anyListOf(File.class), any(File.class), anyLong());
		verify(jobResultNotifier, never()).notifyJob(anyLong());
	}
	
	@Test
	public void transcodeFailBecauseExceptionOccuredAtThumbnailExtract() {
		
		RuntimeException mockException = new RuntimeException();
		when(thumbnailExtractor.extractThumnail(mockMultimediaFile, jobId)).thenThrow(mockException);
		
		try {
			transcodingService.transcode(jobId);
			fail("발생해야함");
		} catch (Exception e) {
			assertSame(mockException, e);
		}
		
		Job job = jobRepository.findById(jobId);
		assertTrue(job.isFinished());
		assertFalse(job.isSuccess());
		assertEquals(Job.State.THUMBNAILEXTRACTING, job.isLastState());
		assertNotNull(job.getOccerredException());
		
		verify(mediaSourceCopier, only()).copy(jobId);
		verify(transcoder, only()).transcode(mockMultimediaFile, jobId);
		verify(thumbnailExtractor, only()).extractThumnail(mockMultimediaFile, jobId);
		verify(createdFileSender, never()).send(anyListOf(File.class), any(File.class), anyLong());
		verify(jobResultNotifier, never()).notifyJob(anyLong());
	}
	
	@Test
	public void transcodeFailBecauseExceptionOccuredAtCreatedFileSender() {
		
		RuntimeException mockException = new RuntimeException();
		Mockito.doThrow(mockException).when(createdFileSender).send(mockMultimediaFiles, mockThumnailFile, jobId);
		
		try {
			transcodingService.transcode(jobId);
			fail("발생해야함");
		} catch (Exception e) {
			assertSame(mockException, e);
		}
		
		Job job = jobRepository.findById(jobId);
		assertTrue(job.isFinished());
		assertFalse(job.isSuccess());
		assertEquals(Job.State.CREATEDFILESEND, job.isLastState());
		assertNotNull(job.getOccerredException());
		
		verify(mediaSourceCopier, only()).copy(jobId);
		verify(transcoder, only()).transcode(mockMultimediaFile, jobId);
		verify(thumbnailExtractor, only()).extractThumnail(mockMultimediaFile, jobId);
		verify(createdFileSender, only()).send(mockMultimediaFiles, mockThumnailFile, jobId);
		verify(jobResultNotifier, never()).notifyJob(anyLong());
	}
	
	@Test
	public void transcodeFailBecauseExceptionOccuredAtjobResultNotifier() {
		
		RuntimeException mockException = new RuntimeException();
		Mockito.doThrow(mockException).when(jobResultNotifier).notifyJob(jobId);
		
		try {
			transcodingService.transcode(jobId);
			fail("발생해야함");
		} catch (Exception e) {
			assertSame(mockException, e);
		}
		
		Job job = jobRepository.findById(jobId);
		assertTrue(job.isFinished());
		assertFalse(job.isSuccess());
		assertEquals(Job.State.JOBRESULTNOTIFY, job.isLastState());
		assertNotNull(job.getOccerredException());
		
		verify(mediaSourceCopier, only()).copy(jobId);
		verify(transcoder, only()).transcode(mockMultimediaFile, jobId);
		verify(thumbnailExtractor, only()).extractThumnail(mockMultimediaFile, jobId);
		verify(createdFileSender, only()).send(mockMultimediaFiles, mockThumnailFile, jobId);
		verify(jobResultNotifier, only()).notifyJob(jobId);
	}
}
