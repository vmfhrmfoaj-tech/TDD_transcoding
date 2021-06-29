package vmfhrmfoaj.study;

public class TranscodingServiceImpl implements TranscodingService {
	
	private JobRepository jobRepository;

	private MediaSourceCopier mediaSourceCopier;
	
	private Transcoder transcoder;
	
	private ThumbnailExtractor thumbnailExtractor;
	
	private CreatedFileSender createdFileSender;
	
	private JobResultNotifier jobResultNotifier;

	private JobStateChanger jobStateChanger;

	private JobExceptionHander exceptionHandler;
	
	public TranscodingServiceImpl(JobRepository jobRepository, MediaSourceCopier mediaSourceCopier, Transcoder transcoder,
			ThumbnailExtractor thumbnailExtractor, CreatedFileSender createdFileSender,
			JobResultNotifier jobResultNotifier, JobStateChanger jobStateChanger,
			JobExceptionHander exceptionHandler) {
		super();
		this.jobRepository = jobRepository;
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

		Job job = jobRepository.findById(jobId);
		
		job.transcode(mediaSourceCopier, transcoder, thumbnailExtractor, createdFileSender, jobResultNotifier, jobStateChanger, exceptionHandler);
	}
}
