package net.sf.l2j.gameserver.skills.conditions;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.skills.Env;

/**
 * The Class ConditionLogicNot.
 *
 * @author mkizub
 */
public class ConditionLogicNot extends Condition {

	private final Condition _condition;

	/**
	 * Instantiates a new condition logic not.
	 *
	 * @param condition the condition
	 */
	public ConditionLogicNot(Condition condition) {
		_condition = condition;
		if (getListener() != null) {
			_condition.setListener(this);
		}
	}

	/**
	 * Sets the listener.
	 *
	 * @param listener the new listener
	 * @see
	 * net.sf.l2j.gameserver.skills.conditions.Condition#setListener(net.sf.l2j.gameserver.skills.conditions.ConditionListener)
	 */
	@Override
	void setListener(ConditionListener listener) {
		if (listener != null) {
			_condition.setListener(this);
		} else {
			_condition.setListener(null);
		}
		super.setListener(listener);
	}

	/**
	 * Test impl.
	 *
	 * @param env the env
	 * @return true, if successful
	 * @see
	 * net.sf.l2j.gameserver.skills.conditions.Condition#testImpl(net.sf.l2j.gameserver.skills.Env)
	 */
	@Override
	public boolean testImpl(Env env) {
		return !_condition.test(env);
	}
}
