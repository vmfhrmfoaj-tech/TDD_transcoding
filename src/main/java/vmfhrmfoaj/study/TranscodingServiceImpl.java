package vmfhrmfoaj.study;

import java.io.File;
import java.util.List;

public class TranscodingServiceImpl implements TranscodingService {

	private MediaSourceCopier mediaSourceCopier;
	
	private Transcoder transcoder;
	
	private ThumbnailExtractor thumbnailExtractor;
	
	private CreatedFileSender createdFileSender;
	
	private JobResultNotifier jobResultNotifier;
	
	public TranscodingServiceImpl(MediaSourceCopier mediaSourceCopier, Transcoder transcoder,
			ThumbnailExtractor thumbnailExtractor, CreatedFileSender createdFileSender,
			JobResultNotifier jobResultNotifier) {
		super();
		this.mediaSourceCopier = mediaSourceCopier;
		this.transcoder = transcoder;
		this.thumbnailExtractor = thumbnailExtractor;
		this.createdFileSender = createdFileSender;
		this.jobResultNotifier = jobResultNotifier;
	}
	
	@Override
	public void transcode(Long jobId) {

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
