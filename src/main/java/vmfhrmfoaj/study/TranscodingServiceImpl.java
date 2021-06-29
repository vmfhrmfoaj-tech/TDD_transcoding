package vmfhrmfoaj.study;

public class TranscodingServiceImpl implements TranscodingService {
	
	private JobRepository jobRepository;

	
	private Transcoder transcoder;
	
	private ThumbnailExtractor thumbnailExtractor;
	
	private JobResultNotifier jobResultNotifier;

	public TranscodingServiceImpl(JobRepository jobRepository, Transcoder transcoder,
			ThumbnailExtractor thumbnailExtractor,
			JobResultNotifier jobResultNotifier) {
		super();
		this.jobRepository = jobRepository;
		this.transcoder = transcoder;
		this.thumbnailExtractor = thumbnailExtractor;
		this.jobResultNotifier = jobResultNotifier;
	}
	
	@Override
	public void transcode(Long jobId) {

		Job job = jobRepository.findById(jobId);
		
		job.transcode(transcoder, thumbnailExtractor, jobResultNotifier);
	}
}
