package vmfhrmfoaj.study;

public class TranscodingServiceImpl implements TranscodingService {
	
	private JobRepository jobRepository;

	
	private Transcoder transcoder;
	
	private ThumbnailExtractor thumbnailExtractor;
	
	private CreatedFileSender createdFileSender;
	
	private JobResultNotifier jobResultNotifier;

	public TranscodingServiceImpl(JobRepository jobRepository, Transcoder transcoder,
			ThumbnailExtractor thumbnailExtractor, CreatedFileSender createdFileSender,
			JobResultNotifier jobResultNotifier) {
		super();
		this.jobRepository = jobRepository;
		this.transcoder = transcoder;
		this.thumbnailExtractor = thumbnailExtractor;
		this.createdFileSender = createdFileSender;
		this.jobResultNotifier = jobResultNotifier;
	}
	
	@Override
	public void transcode(Long jobId) {

		Job job = jobRepository.findById(jobId);
		
		job.transcode(transcoder, thumbnailExtractor, createdFileSender, jobResultNotifier);
	}
}
