package net.sf.l2j.gameserver.skills.conditions;

import org.slf4j.LoggerFactory;

import net.sf.l2j.gameserver.skills.Env;

/**
 * The Class ConditionTargetActiveSkillId.
 */
public class ConditionTargetActiveSkillId extends Condition {

	private final int _skillId;

	/**
	 * Instantiates a new condition target active skill id.
	 *
	 * @param skillId the skill id
	 */
	public ConditionTargetActiveSkillId(int skillId) {
		_skillId = skillId;
	}

	@Override
	public boolean testImpl(Env env) {
		return env.getTarget().getSkill(_skillId) != null;
	}
}
