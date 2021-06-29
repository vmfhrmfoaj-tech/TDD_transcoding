package vmfhrmfoaj.study;

public class Job {

	public static enum State{
		MEDIASOURCECOPYING,
		COMPLETED,
		TRANSCODING, 
		THUMBNAILEXTRACTING, 
		CREATEDFILESEND
	}
	
	private State state;
	private Throwable occerredException;

	public Object isLastState() {
		return state;
	}

	public void changeState(State newState) {
		this.state = newState;
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

	private boolean isExceptionOccurred() {
		return (occerredException != null);
	}

	public Object getOccerredException() {
		return occerredException;
	};
	
	public void exceptionOccurred(RuntimeException e) {
		this.occerredException = e;
	}

}
