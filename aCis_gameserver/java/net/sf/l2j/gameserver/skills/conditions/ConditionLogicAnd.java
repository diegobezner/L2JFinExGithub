package net.sf.l2j.gameserver.skills.conditions;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.skills.Env;

/**
 * The Class ConditionLogicAnd.
 *
 * @author mkizub
 */
public class ConditionLogicAnd extends Condition {

	private static Condition[] _emptyConditions = new Condition[0];
	public Condition[] conditions = _emptyConditions;

	/**
	 * Instantiates a new condition logic and.
	 */
	public ConditionLogicAnd() {
		super();
	}

	/**
	 * Adds the.
	 *
	 * @param condition the condition
	 */
	public void add(Condition condition) {
		if (condition == null) {
			return;
		}
		if (getListener() != null) {
			condition.setListener(this);
		}
		final int len = conditions.length;
		final Condition[] tmp = new Condition[len + 1];
		System.arraycopy(conditions, 0, tmp, 0, len);
		tmp[len] = condition;
		conditions = tmp;
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
			for (Condition c : conditions) {
				c.setListener(this);
			}
		} else {
			for (Condition c : conditions) {
				c.setListener(null);
			}
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
		for (Condition c : conditions) {
			if (!c.test(env)) {
				return false;
			}
		}
		return true;
	}
}
