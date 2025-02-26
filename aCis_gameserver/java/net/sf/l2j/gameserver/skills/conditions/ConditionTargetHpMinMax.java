package net.sf.l2j.gameserver.skills.conditions;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.skills.Env;

/**
 * Used for Trap skills.
 *
 * @author Tryskell
 */
public class ConditionTargetHpMinMax extends Condition {

	private final int _minHp, _maxHp;

	public ConditionTargetHpMinMax(int minHp, int maxHp) {
		_minHp = minHp;
		_maxHp = maxHp;
	}

	@Override
	public boolean testImpl(Env env) {
		if (env.getTarget() == null) {
			return false;
		}

		int _currentHp = (int) env.getTarget().getCurrentHp() * 100 / env.getTarget().getMaxHp();
		return _currentHp >= _minHp && _currentHp <= _maxHp;
	}
}
