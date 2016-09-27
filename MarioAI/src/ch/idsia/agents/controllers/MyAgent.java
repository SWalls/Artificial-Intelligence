
package ch.idsia.agents.controllers;

import ch.idsia.agents.Agent;
import ch.idsia.benchmark.mario.engine.sprites.Mario;
import ch.idsia.benchmark.mario.environments.Environment;


public class MyAgent extends BasicMarioAIAgent implements Agent
{

	public static final int ANYWHERE = 0;
	public static final int UP = 1;
	public static final int DOWN = 1 << 1;
	public static final int LEFT = 1 << 2;
	public static final int RIGHT = 1 << 3;

	public MyAgent()
	{
		super("MyAgent");
		reset();
	}
	
	public int closestEnemyDistance(int dir) {
		int xMin = 0;
		int xMax = 19;
		int yMin = 0;
		int yMax = 19;
		if((dir & UP) == 1) { // UP
			yMax = 9;
		}
		if(((dir & DOWN) >> 1) == 1) { // DOWN
			yMin = 10;
		}
		if(((dir & LEFT) >> 2) == 1) { // LEFT
			xMax = 9;
		}
		if(((dir & RIGHT) >> 3) == 1) { // RIGHT
			xMin = 10;
		}
		int closest = -1;
		for(int x=xMin; x<xMax; x++) {
			for(int y=yMin; y<yMax; y++) {
				int dist = (int)Math.round(Math.sqrt(((x-9)*(x-9)) + ((y-9)*(y-9))));
				if(hasEnemy(y,x) && (closest == -1 || dist < closest)) {
					closest = dist;
				}
			}
		}
		return closest;
	}

	public int closestGapDistance() {
		int count = 0;
		int lastBrickX = 0;
		int lastEmptyX = 0;
		int dist = -1;
		for(int y=10; y<19; y++) {
			count = 0;
			lastBrickX = -1;
			lastEmptyX = 0;
			for(int x=10; x<19; x++) {
				if(!isEmpty(y,x) && !hasEnemy(y,x)) {
					lastBrickX = x;
				} else if(isEmpty(y,x) && lastBrickX > -1) {
					boolean allTheWayDown = true;
					for(int w=y+1; w<19; w++) {
						if(!isEmpty(w,x)) {
							allTheWayDown = false;
							break;
						}
					}
					if(allTheWayDown) {
						if(lastEmptyX == x-1) {
							count++;
							if(count == 3) {
								int pt = x-3;
								dist = (int)Math.abs(pt-9);
								return dist;
							}
						} else {
							count = 1;
						}
						lastEmptyX = x;
					}
				}
			}
		}
		return dist;
	}

	// Does (row, col) contain an enemy?   
	public boolean hasEnemy(int row, int col) {
		return enemies[row][col] != 0;
	}

	// Is (row, col) empty?   
	public boolean isEmpty(int row, int col) {
		return (levelScene[row][col] == 0);
	}


	// Display Mario's view of the world
	public void printObservation() {
		System.out.println("**********OBSERVATIONS**************");
		for (int i = 0; i < mergedObservation.length; i++) {
			for (int j = 0; j < mergedObservation[0].length; j++) {
				if (i == mergedObservation.length / 2 && j == mergedObservation.length / 2) {
					System.out.print("M ");
				}
				else if (hasEnemy(i, j)) {
					System.out.print("E ");
				}
				else if (!isEmpty(i, j)) {
					System.out.print("B ");
				}
				else {
					System.out.print(" ");
				}
			}
			System.out.println();
		}
		System.out.println("************************");
	}

	boolean hasJumped = false;
	boolean hasLanded = false;
	boolean hasInitiatedJumpOverGap = false;
	int groundCounter = 0;
	int obstacleCounter = 0;
	int leftButtonCounter = 0;

	// Actually perform an action by setting a slot in the action array to be true
	public boolean[] getAction()
	{
		int distFromGap = closestGapDistance();
		if(distFromGap > -1 && distFromGap <= 1 && !hasInitiatedJumpOverGap) {
			System.out.println("GAP " + distFromGap + " blocks to the right!");
			// if we detect a gap coming up soon...
			if(!isMarioOnGround) {
				// stop moving right until we hit the ground
				if(leftButtonCounter < 15) {
					action[Mario.KEY_LEFT] = true;
					leftButtonCounter++;
				} else {
					action[Mario.KEY_LEFT] = false;
				}
				action[Mario.KEY_RIGHT] = false;
				action[Mario.KEY_JUMP] = false;
			} else if(isMarioAbleToJump) {
				// once we hit the ground, jump and go right!
				System.out.println("Initiated jump!");
				leftButtonCounter = 0;
				hasJumped = false;
				hasLanded = false;
				groundCounter = 0;
				hasInitiatedJumpOverGap = true;
				action[Mario.KEY_LEFT] = false;
				action[Mario.KEY_RIGHT] = true;
				action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = true;
			} else {
				action[Mario.KEY_RIGHT] = false;
				action[Mario.KEY_LEFT] = true;
				action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = false;
			}
		} else if(hasInitiatedJumpOverGap && !hasLanded) {
			// if we're still jumping over the gap...
			// keep jumping and moving right
			action[Mario.KEY_LEFT] = false;
			action[Mario.KEY_RIGHT] = true;
			action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = true;
			if(!isMarioOnGround) {
				groundCounter = 0;
				hasJumped = true;
			} else if(hasJumped) {
				if(groundCounter < 15) {
					groundCounter++;
				} else {
					// yay! we made the jump!
					System.out.println("Landed!");
					hasJumped = false;
					hasLanded = true;
					hasInitiatedJumpOverGap = false;
					groundCounter = 0;
				}
			}
		} else {
			// otherwise, if no gap...
			int upDistFromEnemy = closestEnemyDistance(UP);
			int leftDistFromEnemy = closestEnemyDistance(LEFT);
			int rightDistFromEnemy = closestEnemyDistance(RIGHT);
			// move away from enemies
			if(rightDistFromEnemy == -1 || rightDistFromEnemy > 2) {
				action[Mario.KEY_RIGHT] = true;
				action[Mario.KEY_LEFT] = false;
			} else if(leftDistFromEnemy == -1 || leftDistFromEnemy > 2) {
				action[Mario.KEY_RIGHT] = false;
				action[Mario.KEY_LEFT] = true;
			} else {
				action[Mario.KEY_RIGHT] = false;
				action[Mario.KEY_LEFT] = false;
			}
			int distFromEnemy = closestEnemyDistance(ANYWHERE);
			if(!isEmpty(9,10) || !isEmpty(10,10) || !isEmpty(11,10)) {
				obstacleCounter++;
			} else {
				obstacleCounter = 0;
			}
			if((upDistFromEnemy == -1 || upDistFromEnemy > 4) || obstacleCounter > 35) {
				// jump if there are nearby enemies
				action[Mario.KEY_SPEED] = action[Mario.KEY_JUMP] = isMarioAbleToJump || !isMarioOnGround;
			}
		}
        // printObservation();
		return action;
	}

	// Do the processing necessary to make decisions in getAction
	public void integrateObservation(Environment environment)
	{
		super.integrateObservation(environment);
    	levelScene = environment.getLevelSceneObservationZ(2);
	}

	// Clear out old actions by creating a new action array
	public void reset()
	{
		action = new boolean[Environment.numberOfKeys];
	}
}
