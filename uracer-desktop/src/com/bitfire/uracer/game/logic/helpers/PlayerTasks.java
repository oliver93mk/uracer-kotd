package com.bitfire.uracer.game.logic.helpers;

import com.bitfire.uracer.ScalingStrategy;
import com.bitfire.uracer.configuration.Config;
import com.bitfire.uracer.game.logic.GameTasksManager;
import com.bitfire.uracer.game.logic.LapInfo;
import com.bitfire.uracer.game.logic.hud.debug.HudDebug;
import com.bitfire.uracer.game.logic.hud.elements.HudLapInfo;
import com.bitfire.uracer.game.logic.hud.elements.HudPlayerDriftInfo;
import com.bitfire.uracer.game.logic.sounds.effects.PlayerDriftSoundEffect;
import com.bitfire.uracer.game.logic.sounds.effects.PlayerImpactSoundEffect;
import com.bitfire.uracer.game.logic.trackeffects.effects.PlayerSkidMarks;
import com.bitfire.uracer.game.player.PlayerCar;

/** Manages the creation and destruction of the player-bound game tasks. */
public class PlayerTasks {

	private GameTasksManager manager = null;
	private ScalingStrategy scalingStrategy = null;

	/**
	 * keeps track of the concrete player tasks
	 * (note that they are all publicly accessible for performance reasons)
	 */

	public HudPlayerDriftInfo hudPlayerDriftInfo = null;
	public HudLapInfo hudLapInfo = null;
	public HudDebug hudDebug = null;
	public PlayerSkidMarks playerSkidMarks = null;
	public PlayerDriftSoundEffect playerDriftSoundFx = null;
	public PlayerImpactSoundEffect playerImpactSoundFx = null;

	public PlayerTasks( GameTasksManager gameTaskManager, ScalingStrategy strategy ) {
		manager = gameTaskManager;
		scalingStrategy = strategy;
	}

	public void dispose() {

	}

	public void createTasks( PlayerCar player, LapInfo lapInfo ) {
		// sounds
		playerDriftSoundFx = new PlayerDriftSoundEffect( player );
		playerImpactSoundFx = new PlayerImpactSoundEffect( player );

		// track effects
		playerSkidMarks = new PlayerSkidMarks( player );

		// hud, player's drift information
		hudPlayerDriftInfo = new HudPlayerDriftInfo( scalingStrategy, player );

		// hud, player's lap info
		hudLapInfo = new HudLapInfo( scalingStrategy, lapInfo );

		manager.sound.add( playerDriftSoundFx );
		manager.sound.add( playerImpactSoundFx );
		manager.effects.add( playerSkidMarks );
		manager.hud.add( hudPlayerDriftInfo );
		manager.hud.add( hudLapInfo );

		// hud-style debug information for various data (player's drift state, number of skid marks particles, ..)
		if( Config.Debug.RenderHudDebugInfo ) {
			hudDebug = new HudDebug( player, player.driftState, playerSkidMarks /* can be null */);
			manager.hud.add( hudDebug );
		}
	}

	public void destroyTasks() {
		if( playerDriftSoundFx != null ) {
			manager.sound.remove( playerDriftSoundFx );
			playerDriftSoundFx = null;
		}

		if( playerImpactSoundFx != null ) {
			manager.sound.remove( playerImpactSoundFx );
			playerImpactSoundFx = null;
		}

		if( playerSkidMarks != null ) {
			manager.effects.remove( playerSkidMarks );
			playerSkidMarks = null;
		}

		if( hudPlayerDriftInfo != null ) {
			manager.hud.remove( hudPlayerDriftInfo );
			hudPlayerDriftInfo = null;
		}

		if( hudLapInfo != null ) {
			manager.hud.remove( hudLapInfo );
			hudLapInfo = null;
		}

		if( hudDebug != null ) {
			manager.hud.remove( hudDebug );
			hudDebug = null;
		}
	}
}
