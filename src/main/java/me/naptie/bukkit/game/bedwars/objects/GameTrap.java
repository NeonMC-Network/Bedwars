package me.naptie.bukkit.game.bedwars.objects;

import me.naptie.bukkit.game.bedwars.messages.Messages;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class GameTrap {

	private GameTeam team;
	private Type type;
	private List<PotionEffectType> effects;
	private int duration;
	private int amplifier;

	public GameTrap(GameTeam team, Type type) {
		this.team = team;
		this.team.addTrap(this);
		this.type = type;
	}

	public GameTrap(GameTeam team, List<PotionEffectType> effects, int duration, int amplifier) {
		this.team = team;
		this.team.addTrap(this);
		this.type = Type.EFFECT;
		this.effects = effects;
		this.duration = duration;
		this.amplifier = amplifier;
	}

	public void activate(GamePlayer enemy) {
		this.team.getTraps().remove(this);
		if (this.type == Type.ALERT) {
			for (GamePlayer player : this.team.getMembers()) {
				player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1, 1);
				player.sendTitle(Messages.getMessage(player, "TRAP_TRIGGERED"), "\"color\":\"red\",\"bold\":true", Messages.getMessage(player, "ENTERED_YOUR_BASE").replace("%player%", enemy.getPlayer().getName()), "\"color\":\"yellow\"", 5, 40, 10);
				player.sendMessage(Messages.getMessage(player, "TRAP_SET_OFF"));
			}
		}
		if (this.type == Type.EFFECT) {
			for (PotionEffectType effect : this.effects) {
				enemy.getPlayer().addPotionEffect(new PotionEffect(effect, duration, amplifier));
			}
		}
	}

	public Type getType() {
		return type;
	}

	public List<PotionEffectType> getEffects() {
		return effects;
	}

	public int getDuration() {
		return duration;
	}

	public int getAmplifier() {
		return amplifier;
	}

	public GameTeam getTeam() {
		return team;
	}

	public enum Type {
		ALERT, EFFECT
	}
}
