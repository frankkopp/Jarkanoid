/**
MIT License

Copyright (c) 2018 Frank Kopp

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package fko.breakout.model;

import javafx.scene.paint.Color;

/**
 * Brick
 * <p>
 * A <code>Brick</code> has a certain <code>BrickType</code> and a <code>BrickPower</code> which can be
 * <code>BrickPower.NONE</code>. It counts the hits it has receives by the ball. If the hit count is
 * equal the number of allowed hits for this BrickType a "killed" flag is set which can be queried via 
 * <code>isKilled()</code>.
 * <p>
 * 04.01.2018
 * @author Frank Kopp
 */
public class Brick {

	private final BrickType brickType;
	private final BrickPowerType powerType;

	private int hitCount = 0;

	private boolean isInvincible = false;
	private boolean isKilled = false;

	/**
	 * @param brickType
	 */
	public Brick(BrickType brickType, BrickPowerType powerType) {
		this.brickType = brickType;
		this.powerType = powerType;
		if (brickType.equals(BrickType.GOLD)) isInvincible = true;
	}

	/**
	 * @return remaining number of hits until killed. If 0 the brick has been killed.
	 */
	public int increaseHitCount() {
		hitCount++;
		if (getRemainingHits() == 0 && !isInvincible) isKilled = true;
		return getRemainingHits();
	}

	/**
	 * @return  number of hits the brick has already received.
	 */
	public int getHitCount() {
		return hitCount;
	}

	/**
	 * @return remaining number of hits until killed. If 0 the brick has been killed.
	 */
	public int getRemainingHits() {
		return isInvincible ? brickType.hits : brickType.hits - hitCount;
	}

	/**
	 * @return true if the brick has been killed.
	 */
	public boolean isKilled() {
		return isInvincible ? false : isKilled;
	}

	/**
	 * @return the type
	 */
	public BrickType getType() {
		return brickType;
	}

	/**
	 * @return the powerType
	 */
	public BrickPowerType getPowerType() {
		return powerType;
	}

	/**
	 * @return points for killing this brick
	 */
	public int getPoints() {
		return brickType.points;
	}

	/** 
	 * @return Color of this brick
	 */
	public Color getColor() {
		return brickType.color;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("Brick [brickType=%s, powerType=%s, hitCount=%s, isInvincible=%s, isKilled=%s]", brickType,
				powerType, hitCount, isInvincible, isKilled);
	}
	
	public String toShortString() {
		return String.format("%2.2s%2.2s", brickType.sign,powerType.name());
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((brickType == null) ? 0 : brickType.hashCode());
		result = prime * result + hitCount;
		result = prime * result + (isInvincible ? 1231 : 1237);
		result = prime * result + (isKilled ? 1231 : 1237);
		result = prime * result + ((powerType == null) ? 0 : powerType.hashCode());
		return result;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Brick))
			return false;
		Brick other = (Brick) obj;
		if (brickType != other.brickType)
			return false;
		if (hitCount != other.hitCount)
			return false;
		if (isInvincible != other.isInvincible)
			return false;
		if (isKilled != other.isKilled)
			return false;
		if (powerType != other.powerType)
			return false;
		return true;
	}

	/**
	 * @see java.lang.Object#clone()
	 */
	@Override
	protected Brick clone() {
		Brick copy = new Brick(this.brickType, this.powerType);
		copy.hitCount = this.hitCount;
		copy.isInvincible = this.isInvincible;
		copy.isKilled = this.isKilled;
		return copy;
	}
	
}
