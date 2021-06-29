package vmfhrmfoaj.study;

import vmfhrmfoaj.study.Job.State;

public interface JobStateChanger {

	void changeJobState(Long jobId, State state);

}
