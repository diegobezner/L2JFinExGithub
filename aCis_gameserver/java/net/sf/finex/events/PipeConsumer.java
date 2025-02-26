package net.sf.finex.events;

import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

import lombok.RequiredArgsConstructor;

/**
 * EventAction.java
 *
 * @author zcxv
 * @date 23.03.2018
 */
@RequiredArgsConstructor
class PipeConsumer<Input> implements IEventPipe<Input, Input> {

	private final Consumer<Input> consumer;

	@Override
	public Input process(Input object) {
		consumer.accept(object);
		return object;
	}

}
