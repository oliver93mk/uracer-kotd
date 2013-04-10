
package com.bitfire.uracer.game.logic.gametasks.hud.elements.player;

import aurelienribon.tweenengine.Timeline;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.equations.Linear;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.bitfire.uracer.configuration.Config;
import com.bitfire.uracer.game.logic.gametasks.hud.Positionable;
import com.bitfire.uracer.game.tween.GameTweener;
import com.bitfire.uracer.resources.Art;
import com.bitfire.uracer.utils.AMath;
import com.bitfire.uracer.utils.BoxedFloat;
import com.bitfire.uracer.utils.BoxedFloatAccessor;

public class WrongWay extends Positionable {
	private float w, h, x, y, offX, offY;
	private Sprite sign;
	private BoxedFloat bfAlpha;
	private boolean isShown;

	public WrongWay () {
		sign = new Sprite(Art.wrongWay);
		float scale = 0.4f;
		w = /* Convert.scaledPixels */(Art.wrongWay.getWidth()) * scale;
		h = /* Convert.scaledPixels */(Art.wrongWay.getHeight()) * scale;
		offX = w / 2;
		offY = h / 2;

		sign.setSize(w, h);
		sign.setOrigin(offX, offY);
		sign.flip(false, true);
		bfAlpha = new BoxedFloat(0);
		isShown = false;
	}

	// private TweenCallback fadeOutFinished = new TweenCallback() {
	// @Override
	// public void onEvent (int type, BaseTween<?> source) {
	// switch (type) {
	// case COMPLETE:
	// isShown = false;
	// }
	// }
	// };

	@Override
	public float getWidth () {
		return w * scale;
	}

	@Override
	public float getHeight () {
		return h * scale;
	}

	public void fadeIn () {
		fadeIn(Config.Graphics.DefaultFadeMilliseconds);
	}

	public void fadeOut () {
		fadeOut(Config.Graphics.DefaultFadeMilliseconds);
	}

	public void fadeIn (int millisecs) {
		if (!isShown) {
			isShown = true;
			Timeline seq = Timeline.createSequence();
			seq.push(Tween.to(bfAlpha, BoxedFloatAccessor.VALUE, millisecs).target(1f).ease(Linear.INOUT));
			GameTweener.start(seq);
		}
	}

	public void fadeOut (int millisecs) {
		if (isShown) {
			isShown = false;
			Timeline seq = Timeline.createSequence();
			seq.push(Tween.to(bfAlpha, BoxedFloatAccessor.VALUE, millisecs).target(0f).ease(Linear.INOUT));
			GameTweener.start(seq);
		}
	}

	public void render (SpriteBatch batch, float cameraZoom) {
		if (!AMath.isZero(bfAlpha.value)) {

			// float scale = 0.5f;
			// float timeFactor = URacer.Game.getTimeModFactor() * 0.3f;
			// float s = 0.55f + timeFactor * 0.5f;
			// float scl = cameraZoom * s;
			//
			// s = 0.8f + timeFactor;
			// scl = cameraZoom * scale * s;

			float px = position.x - offX;
			float py = position.y - offY;

			sign.setPosition(px, py);

			// HACK ALERT!
			// bounds.set(w * scl, h * scl);
			// halfBounds.set(bounds.x, bounds.y);

			sign.draw(batch, bfAlpha.value);
		}
	}
}
