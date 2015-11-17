
public class DeadLock {

	/**
	 * States to go through to reproduce the deadlock precondition.
	 */
	private enum PreconditionState {
		NO_LOCKS, THREAD1_LOCKS_RESOURCE1, THREAD1_LOCKS_RESOURCE1__AND__THREAD2_LOCKS_RESOURCE2
	}

	private transient PreconditionState _preconditionState = PreconditionState.NO_LOCKS;
	private final String _resource1 = "RESOURCE1";
	private final String _resource2 = "RESOURCE2";

	private final Thread _thread1 = new Thread("Thread1") {
		public void run() {
			if (_preconditionState != PreconditionState.NO_LOCKS) {
				logBadStateError(this.getName(), _preconditionState);
				return;
			}
			synchronized (_resource1) {
				_preconditionState = PreconditionState.THREAD1_LOCKS_RESOURCE1;
				while (_preconditionState != PreconditionState.THREAD1_LOCKS_RESOURCE1__AND__THREAD2_LOCKS_RESOURCE2) {
					logBadStateError(this.getName(), _preconditionState);
				}
				synchronized (_resource2) {
					logBothResourcesAquired(this.getName());
				}
			}
		}

	};

	private final Thread _thread2 = new Thread("Thread2") {
		public void run() {
			while (_preconditionState != PreconditionState.THREAD1_LOCKS_RESOURCE1) {
				logBadStateError(this.getName(), _preconditionState);
			}
			synchronized (_resource2) {
				_preconditionState = PreconditionState.THREAD1_LOCKS_RESOURCE1__AND__THREAD2_LOCKS_RESOURCE2;
				synchronized (_resource1) {
					logBothResourcesAquired(this.getName());
				}
			}
		}
	};

	private static void logBadStateError(final String threadName, final PreconditionState state) {
		System.err.println(threadName + " bad state: " + state);
	}

	private static void logBothResourcesAquired(final String threadName) {
		System.err.println(threadName + " has aquired both resources");
	}

	public static void main(final String arg[]) {
		final DeadLock deadLock = new DeadLock();
		deadLock._thread2.start();
		deadLock._thread1.start();
	}
}
