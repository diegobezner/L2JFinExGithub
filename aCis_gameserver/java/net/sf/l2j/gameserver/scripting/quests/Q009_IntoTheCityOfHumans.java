package net.sf.l2j.gameserver.scripting.quests;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.base.ClassRace;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;

public class Q009_IntoTheCityOfHumans extends Quest {

	private static final String qn = "Q009_IntoTheCityOfHumans";

	// NPCs
	public final int PETUKAI = 30583;
	public final int TANAPI = 30571;
	public final int TAMIL = 30576;

	// Rewards
	// public final int SOE_GIRAN = 7126;
	public Q009_IntoTheCityOfHumans() {
		super(9, "Into the City of Humans");

		addStartNpc(PETUKAI);
		addTalkId(PETUKAI, TANAPI, TAMIL);
	}

	@Override
	public String onAdvEvent(String event, Npc npc, Player player) {
		String htmltext = event;
		QuestState st = player.getQuestState(qn);
		if (st == null) {
			return htmltext;
		}

		if (event.equalsIgnoreCase("30583-01.htm")) {
			st.setState(STATE_STARTED);
			st.set("cond", "1");
			st.playSound(QuestState.SOUND_ACCEPT);
		} else if (event.equalsIgnoreCase("30571-01.htm")) {
			st.set("cond", "2");
			st.playSound(QuestState.SOUND_MIDDLE);
		} else if (event.equalsIgnoreCase("30576-01.htm")) {
			st.giveDataExpSp();
			st.giveDataReward();
			st.playSound(QuestState.SOUND_FINISH);
			st.exitQuest(false);
		}

		return htmltext;
	}

	@Override
	public String onTalk(Npc npc, Player player) {
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null) {
			return htmltext;
		}

		switch (st.getState()) {
			case STATE_CREATED:
				if (player.getLevel() >= 3 && player.getRace() == ClassRace.ORC) {
					htmltext = "30583-00.htm";
				} else {
					htmltext = "30583-00a.htm";
				}
				break;

			case STATE_STARTED:
				int cond = st.getInt("cond");
				switch (npc.getNpcId()) {
					case PETUKAI:
						if (cond == 1) {
							htmltext = "30583-01a.htm";
						}
						break;

					case TANAPI:
						if (cond == 1) {
							htmltext = "30571-00.htm";
						} else if (cond == 2) {
							htmltext = "30571-01a.htm";
						}
						break;

					case TAMIL:
						if (cond == 2) {
							htmltext = "30576-00.htm";
						}
						break;
				}
				break;

			case STATE_COMPLETED:
				htmltext = getAlreadyCompletedMsg();
				break;
		}

		return htmltext;
	}
}
