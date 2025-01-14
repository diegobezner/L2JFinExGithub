package net.sf.finex.events;

import org.slf4j.LoggerFactory;

/**
 * EventSubscribtion.java
 *
 * @author zcxv
 * @date 23.03.2018
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class SingleEventSubscription<T> extends AbstractEventSubscription<T> {

	@Override
	void execute(T object) {
		Object input = object;
		for (int i = 0; i < pipe.size() && input != null; i++) {
			final IEventPipe pipe = this.pipe.get(i);
			input = pipe.process(input);
		}
	}

}
