package net.sf.l2j.gameserver.skills.effects;

import net.sf.l2j.gameserver.skills.Effect;
import net.sf.l2j.gameserver.skills.EffectTemplate;
import net.sf.l2j.gameserver.skills.Env;
import net.sf.l2j.gameserver.skills.L2Effect;
import net.sf.l2j.gameserver.templates.skills.EEffectFlag;
import net.sf.l2j.gameserver.templates.skills.L2EffectType;

/**
 * @author mkizub
 */
@Effect("Sleep")
public class EffectSleep extends L2Effect {

	public EffectSleep(Env env, EffectTemplate template) {
		super(env, template);
	}

	@Override
	public L2EffectType getEffectType() {
		return L2EffectType.SLEEP;
	}

	@Override
	public boolean onStart() {
		getEffected().startSleeping();
		return true;
	}

	@Override
	public void onExit() {
		getEffected().stopSleeping(false);
	}

	@Override
	public boolean onActionTime() {
		return false;
	}

	@Override
	public boolean onSameEffect(L2Effect effect) {
		return false;
	}

	@Override
	public int getEffectFlags() {
		return EEffectFlag.SLEEP.getMask();
	}
}
