package com.bitfire.uracer.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.bitfire.uracer.ScalingStrategy;
import com.bitfire.uracer.URacer;
import com.bitfire.uracer.configuration.Config;
import com.bitfire.uracer.game.actors.Car;
import com.bitfire.uracer.game.actors.Car.Aspect;
import com.bitfire.uracer.game.actors.CarModel;
import com.bitfire.uracer.game.logic.GameLogic;
import com.bitfire.uracer.game.logic.replaying.Replay;
import com.bitfire.uracer.game.rendering.GameRenderer;
import com.bitfire.uracer.game.world.GameWorld;
import com.bitfire.uracer.postprocessing.PostProcessor;
import com.bitfire.uracer.postprocessing.effects.Bloom;
import com.bitfire.uracer.postprocessing.effects.Vignette;
import com.bitfire.uracer.postprocessing.effects.Zoom;
import com.bitfire.uracer.task.TaskManager;

public class Game implements Disposable {

	// world
	public GameWorld gameWorld = null;

	// config
	public GameplaySettings gameplaySettings = null;

	// debug
	private DebugHelper debug = null;

	// logic
	private GameLogic gameLogic = null;

	// rendering
	private GameRenderer gameRenderer = null;
	private boolean canPostProcess = false;

	// post processing
	private Bloom bloom = null;
	private Zoom zoom = null;
	private Vignette vignette = null;

	public Game( String levelName, ScalingStrategy scalingStrategy, GameDifficulty difficulty ) {
		gameplaySettings = new GameplaySettings( difficulty );

		gameWorld = new GameWorld( scalingStrategy, levelName, false );
		Gdx.app.log( "Game", "Game world ready" );

		// handles rendering
		gameRenderer = new GameRenderer( gameWorld, scalingStrategy, Config.PostProcessing.Enabled );
		canPostProcess = gameRenderer.hasPostProcessor();
		Gdx.app.log( "Game", "GameRenderer ready" );

		// post-processing
		if( canPostProcess ) {
			configurePostProcessing( gameRenderer.getPostProcessor(), gameWorld );
			Gdx.app.log( "Game", "Post-processing configured" );
		}

		// handles game rules and mechanics, it's all about game data
		gameLogic = new GameLogic( gameWorld, gameRenderer, gameplaySettings, scalingStrategy/* , carAspect, carModel */);
		Gdx.app.log( "Game", "GameLogic created" );

		// initialize the debug helper
		if( Config.Debug.UseDebugHelper ) {
			debug = new DebugHelper( gameRenderer.getWorldRenderer(), gameWorld.getBox2DWorld(), gameRenderer.getPostProcessor() );
			Gdx.app.log( "Game", "Debug helper initialized" );
		}
	}

	@Override
	public void dispose() {
		if( Config.Debug.UseDebugHelper ) {
			debug.dispose();
		}

		gameRenderer.dispose();
		gameLogic.dispose();
	}

	public void setPlayer( CarModel model, Aspect aspect ) {
		gameLogic.setPlayer( model, aspect );
		if( Config.Debug.UseDebugHelper ) {
			DebugHelper.setPlayer( gameLogic.getPlayer() );
		}
	}

	public void setLocalReplay( Replay replay ) {
		gameLogic.setBestLocalReplay( replay );
	}

	private void configurePostProcessing( PostProcessor processor, GameWorld world ) {

		processor.setEnabled( true );

		bloom = new Bloom( Config.PostProcessing.RttFboWidth, Config.PostProcessing.RttFboHeight );

		// Bloom.Settings bs = new Bloom.Settings( "arrogance-1 / rtt=0.25 / @1920x1050", BlurType.Gaussian5x5b, 1, 1,
		// 0.25f, 1f, 0.1f, 0.8f, 1.4f );
		// Bloom.Settings bs = new Bloom.Settings( "arrogance-2 / rtt=0.25 / @1920x1050", BlurType.Gaussian5x5b, 1, 1,
		// 0.35f, 1f, 0.1f, 1.4f, 0.75f );

		float threshold = ((world.isNightMode() && !Config.Graphics.DumbNightMode) ? 0.2f : 0.45f);
		Bloom.Settings bloomSettings = new Bloom.Settings( "subtle", Config.PostProcessing.BlurType, 1, 1.5f, threshold, 1f, 0.5f, 1f, 1.5f );
		bloom.setSettings( bloomSettings );

		zoom = new Zoom( Config.PostProcessing.ZoomQuality );
		zoom.setStrength( 0 );
		processor.addEffect( zoom );
		processor.addEffect( bloom );

		if( Config.PostProcessing.EnableVignetting ) {
			vignette = new Vignette();
			vignette.setCoords( 0.75f, 0.4f );
			processor.addEffect( vignette );
		}
	}

	// FIXME, this is logic and it shouldn't be here
	private void updatePostProcessingEffects() {
		float factor = 1 - (URacer.timeMultiplier - GameLogic.TimeMultiplierMin) / (Config.Physics.PhysicsTimeMultiplier - GameLogic.TimeMultiplierMin);
		Car playerCar = gameLogic.getPlayer();

		if( zoom != null && playerCar != null ) {
			zoom.setOrigin( GameRenderer.ScreenUtils.worldPxToScreen( playerCar.state().position ) );
			zoom.setStrength( -0.1f * factor );
		}

		if( bloom != null && zoom != null ) {
			bloom.setBaseSaturation( 0.5f - 0.5f * factor );
			// bloom.setBloomSaturation( 1.5f - factor * 0.85f ); // TODO when charged
			bloom.setBloomSaturation( 1.5f - factor * 1.5f );	// TODO when completely discharged
			bloom.setBloomIntesity( 1f + factor * 1.75f );

			// vignette.setY( (1 - factor) * 0.74f + factor * 0.4f );
			// vignette.setIntensity( 1f );

			vignette.setIntensity( factor );
		}
	}

	public void tick() {
		TaskManager.dispatchTick();
		gameLogic.onAcquireInput();
	}

	public boolean tickCompleted() {
		gameLogic.onSubstepCompleted();

		if( gameLogic.doQuit ) {
			return true;
		}

		return false;
	}

	public void render() {
		// trigger the event and let's subscribers interpolate and update their state()
		gameRenderer.beforeRender( URacer.getTemporalAliasing() );
		gameLogic.onBeforeRender();

		if( canPostProcess ) {
			updatePostProcessingEffects();
		}

		gameRenderer.render();
	}

	public void debugUpdate() {
		debug.update();
		gameRenderer.debugRender();
	}

	public void pause() {
	}

	public void resume() {
		gameRenderer.rebind();
	}
}