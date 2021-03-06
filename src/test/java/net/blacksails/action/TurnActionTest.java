package net.blacksails.action;

import static org.junit.Assert.assertEquals;
import net.blacksails.domain.Coord;
import net.blacksails.domain.Direction;
import net.blacksails.domain.GameBoard;
import net.blacksails.domain.Orientation;
import net.blacksails.domain.Ship;

import org.junit.Test;

public class TurnActionTest {
	@Test
	public void testTurnActionLeft() {
		Action action = new TurnAction(Direction.LEFT);
		GameBoard board = new GameBoard(10);
		Coord coord = new Coord(0, 0);
		Ship ship = new Ship(coord, Orientation.SOUTH);
		// -------------------------------------------------------
		action.act(board, ship, coord);
		// -------------------------------------------------------
		assertEquals(ship.getOrientation(), Orientation.EAST);
	}
	
	@Test
	public void testMoveActionRight() {
		Action action = new TurnAction(Direction.RIGHT);
		GameBoard board = new GameBoard(10);
		Coord coord = new Coord(0, 0);
		Ship ship = new Ship(coord, Orientation.WEST);
		// -------------------------------------------------------
		action.act(board, ship, coord);
		// -------------------------------------------------------
		assertEquals(ship.getOrientation(), Orientation.NORTH);
	}
	
	@Test(expected = RuntimeException.class)
	public void testTurnSunkenShipThrowsException() {
		Action action = new TurnAction(Direction.LEFT);
		GameBoard board = new GameBoard(10);
		Coord coord = new Coord(0, 0);
		Ship ship = new Ship(coord, Orientation.SOUTH);
		ship.sink();
		// -------------------------------------------------------
		action.act(board, ship, coord);
		// -------------------------------------------------------
	}
	
	@Test(expected = RuntimeException.class)
	public void testTurnNoShipThrowsException() {
		Action action = new TurnAction(Direction.RIGHT);
		GameBoard board = new GameBoard(10);
		Coord coord = new Coord(0, 0);
		// -------------------------------------------------------
		action.act(board, null, coord);
		// -------------------------------------------------------
	}
}
