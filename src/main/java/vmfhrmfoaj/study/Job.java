package vmfhrmfoaj.study;

import java.io.File;
import java.util.List;

public class Job {

	public static enum State{
		MEDIASOURCECOPYING,
		COMPLETED,
		TRANSCODING, 
		THUMBNAILEXTRACTING, 
		CREATEDFILESEND, 
		JOBRESULTNOTIFY
	}
	
	private Long id;
	private MediaSourceFile mediaSourceFile;
	private DestinationStorage destinationStorage;
	private State state;
	private Throwable occerredException;

	public Job(Long jobId, MediaSourceFile mediaSourceFile, DestinationStorage destinationStorage) {
		this.id = jobId;
		this.mediaSourceFile = mediaSourceFile;
		this.destinationStorage = destinationStorage;
	}

	public void transcode(Transcoder transcoder,
			ThumbnailExtractor thumbnailExtractor, CreatedFileSender createdFileSender,
			JobResultNotifier jobResultNotifier) {

		try {
			// 미디어 원본으로부터 파일을 로컬에 복사한다.
			changeJobState(Job.State.MEDIASOURCECOPYING);
			File multimediaFile = copyMultimediaSourceToLocal();
			
			// 로컬에 복사된 파일을 변환처리한다.
			changeJobState(Job.State.TRANSCODING);
			List<File> multimediaFiles = transcode(multimediaFile, transcoder);
			
			// 로컬에 복사된 파일로부터 이미지를 추출한다.
			changeJobState(Job.State.THUMBNAILEXTRACTING);
			File thumbnail = extractThumbnail(multimediaFile, thumbnailExtractor);
			
			// 변환된 결과 파일과 썸네일 이미지를 목적지에 저장
			changeJobState(Job.State.CREATEDFILESEND);
			sendCreatedFilesToDestination(multimediaFiles, thumbnail, createdFileSender);
			
			// 결과를 통보
			changeJobState(Job.State.JOBRESULTNOTIFY);
			notifyJob(jobResultNotifier);
			
			changeJobState(Job.State.COMPLETED);
		} catch (RuntimeException e) {
			exceptionOccurred(e);
			throw e;
		}
	}

	public Object isLastState() {
		return state;
	}

	public boolean isSuccess() {
		return state == State.COMPLETED;
	}

	public boolean isWaiting() {
		return state == null;
	}

	public boolean isFinished() {
		return isSuccess() || isExceptionOccurred();
	}

	public Object getOccerredException() {
		return occerredException;
	};

	private File copyMultimediaSourceToLocal() {
		return mediaSourceFile.getSourceFile();
	}

	private List<File> transcode(File multimediaFile, Transcoder transcoder) {
		return transcoder.transcode(multimediaFile, id);
	}

	private File extractThumbnail(File multimediaFile, ThumbnailExtractor thumbnailExtractor) {
		return thumbnailExtractor.extractThumnail(multimediaFile, id);
	}

	private void sendCreatedFilesToDestination(List<File> multimediaFiles, File thumbnail, CreatedFileSender createdFileSender) {
		createdFileSender.send(multimediaFiles, thumbnail, id);
	}

	private void notifyJob(JobResultNotifier jobResultNotifier) {
		jobResultNotifier.notifyJob(id);
	}

	private void changeJobState(State newState) {
		this.state = newState;
	}

	private boolean isExceptionOccurred() {
		return (occerredException != null);
	}
	
	private void exceptionOccurred(RuntimeException e) {
		this.occerredException = e;
	}
}
