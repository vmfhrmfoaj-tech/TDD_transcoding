package vmfhrmfoaj.study;

import java.io.File;
import java.util.List;

import vmfhrmfoaj.study.Job.State;

public class TranscodingServiceImpl implements TranscodingService {

	private MediaSourceCopier mediaSourceCopier;
	
	private Transcoder transcoder;
	
	private ThumbnailExtractor thumbnailExtractor;
	
	private CreatedFileSender createdFileSender;
	
	private JobResultNotifier jobResultNotifier;

	private JobStateChanger jobStateChanger;

	private JobExceptionHander exceptionHandler;
	
	public TranscodingServiceImpl(MediaSourceCopier mediaSourceCopier, Transcoder transcoder,
			ThumbnailExtractor thumbnailExtractor, CreatedFileSender createdFileSender,
			JobResultNotifier jobResultNotifier, JobStateChanger jobStateChanger,
			JobExceptionHander exceptionHandler) {
		super();
		this.mediaSourceCopier = mediaSourceCopier;
		this.transcoder = transcoder;
		this.thumbnailExtractor = thumbnailExtractor;
		this.createdFileSender = createdFileSender;
		this.jobResultNotifier = jobResultNotifier;
		this.jobStateChanger = jobStateChanger;
		this.exceptionHandler = exceptionHandler;
	}
	
	@Override
	public void transcode(Long jobId) {

		// 미디어 원본으로부터 파일을 로컬에 복사한다.
		changeJobState(jobId, Job.State.MEDIASOURCECOPYING);
		File multimediaFile = copyMultimediaSourceToLocal(jobId);
		
		// 로컬에 복사된 파일을 변환처리한다.
		changeJobState(jobId, Job.State.TRANSCODING);
		List<File> multimediaFiles = transcode(multimediaFile, jobId);
		
		// 로컬에 복사된 파일로부터 이미지를 추출한다.
		changeJobState(jobId, Job.State.THUMBNAILEXTRACTING);
		File thumbnail = extractThumbnail(multimediaFile, jobId);
		
		// 변환된 결과 파일과 썸네일 이미지를 목적지에 저장
		changeJobState(jobId, Job.State.CREATEDFILESEND);
		sendCreatedFilesToDestination(multimediaFiles, thumbnail, jobId);
		
		// 결과를 통보
		notifyJob(jobId);
		changeJobState(jobId, Job.State.COMPLETED);
	}

	private void changeJobState(Long jobId, State state) {
		jobStateChanger.changeJobState(jobId, state);
	}

	private void notifyJob(Long jobId) {
		jobResultNotifier.notifyJob(jobId);
	}

	private void sendCreatedFilesToDestination(List<File> multimediaFiles, File thumbnail, Long jobId) {
		try {
			createdFileSender.send(multimediaFiles, thumbnail, jobId);
		} catch (RuntimeException e) {
			exceptionHandler.notifyJobException(jobId, e);
			throw e;
		}
	}

	private File extractThumbnail(File multimediaFile, Long jobId) {
		try {
			return thumbnailExtractor.extractThumnail(multimediaFile, jobId);
		} catch (RuntimeException e) {
			exceptionHandler.notifyJobException(jobId, e);
			throw e;
		}
	}

	private List<File> transcode(File multimediaFile, Long jobId) {
		try {
			return transcoder.transcode(multimediaFile, jobId);
		} catch (RuntimeException e) {
			exceptionHandler.notifyJobException(jobId, e);
			throw e;
		}
	}

	private File copyMultimediaSourceToLocal(Long jobId) {
		try {
			return mediaSourceCopier.copy(jobId);
		} catch (RuntimeException e) {
			exceptionHandler.notifyJobException(jobId, e);
			throw e;
		}
	}
}
