package vmfhrmfoaj.study;

public interface JobExceptionHander {

	void notifyJobException(Long jobId, Exception e);

}
