public class DeadLock {

	private enum StateMachine {
		START, THREAD1_HOLDS_RESOURCE1, THREAD2_HOLDS_RESOURCE2, THREAD1_REQUESTS_RESOURCE2, THREAD2_REQUESTS_RESOURCE1 {
			/**
			 * Last state has no next.
			 */
			@Override
			public StateMachine next() {
				return null;
			};
		};

		/**
		 * @return the next state of the state machine
		 */
		protected StateMachine next() {
			return values()[ordinal() + 1];
		}
	}

	private StateMachine stateMachine = StateMachine.START;
	private final String resource1 = "RESOURCE1";
	private final String resource2 = "RESOURCE2";

	private final Thread thread1 = new Thread("Thread1") {
		public void run() {
			if (stateMachine != StateMachine.START) {
				System.err.println(this.getName() + " bad state: " + stateMachine);
				return;
			}
			synchronized (resource1) {
				stateMachine = stateMachine.next();
				while (stateMachine != StateMachine.THREAD2_HOLDS_RESOURCE2) {
					System.err.println(this.getName() + " bad state: " + stateMachine);
				}
				synchronized (resource2) {
					System.out.println(resource1 + resource2);
				}
			}
		}
	};

	private final Thread thread2 = new Thread("Thread2") {
		public void run() {
			while (stateMachine != StateMachine.THREAD1_HOLDS_RESOURCE1) {
				System.err.println(this.getName() + " bad state: " + stateMachine);
			}
			synchronized (resource2) {
				stateMachine = stateMachine.next();
				synchronized (resource1) {
					System.out.println(resource2 + resource1);
				}
			}
		}
	};

	public static void main(final String arg[]) {
		final DeadLock mdl = new DeadLock();
		mdl.thread2.start();
		mdl.thread1.start();

	}
}
