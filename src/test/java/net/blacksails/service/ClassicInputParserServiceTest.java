package net.blacksails.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.blacksails.action.Action;
import net.blacksails.action.MoveAction;
import net.blacksails.action.ShootAction;
import net.blacksails.action.TurnAction;
import net.blacksails.domain.Coord;
import net.blacksails.domain.Direction;
import net.blacksails.domain.GameModel;
import net.blacksails.domain.Orientation;
import net.blacksails.domain.Ship;

import org.junit.Test;

public class ClassicInputParserServiceTest {
	ClassicInputParserService parser = new ClassicInputParserService();
	
	@Test
	public void testParsesBoardSize() throws IOException {
		ClassicInputParserService parserSpy = spy(parser);
		BufferedReader reader = mock(BufferedReader.class);
		InputStream inputStream = mock(InputStream.class);
		doReturn(reader).when(parserSpy).getReader(inputStream);
		when(reader.readLine()).thenReturn("10").thenReturn(null);
		// -------------------------------------------------------
		GameModel model = parserSpy.parseInput(inputStream);
		// -------------------------------------------------------
		assertEquals(model.getInitialBoard().getSize(), 10);
	}
	
	@Test(expected = RuntimeException.class)
	public void testInvalidBoardSizeThrowsException() throws IOException {
		// TODO: This should be less brittle and test a more specific exception is thrown
		ClassicInputParserService parserSpy = spy(parser);
		BufferedReader reader = mock(BufferedReader.class);
		InputStream inputStream = mock(InputStream.class);
		doReturn(reader).when(parserSpy).getReader(inputStream);
		when(reader.readLine()).thenReturn("-5").thenReturn(null);
		// -------------------------------------------------------
		parserSpy.parseInput(inputStream);
		// -------------------------------------------------------
	}
	
	@Test
	public void testParsesShips() throws IOException {
		ClassicInputParserService parserSpy = spy(parser);
		BufferedReader reader = mock(BufferedReader.class);
		InputStream inputStream = mock(InputStream.class);
		doReturn(reader).when(parserSpy).getReader(inputStream);
		when(reader.readLine()).thenReturn("10").thenReturn("(1, 2, N) (9, 4, E)").thenReturn(null);
		// -------------------------------------------------------
		GameModel model = parserSpy.parseInput(inputStream);
		// -------------------------------------------------------
		assertEquals(model.getInitialBoard().size(), 2);
		
		int i = 0;
		for(List<Ship> ships : model.getInitialBoard().values()) {
			if(i == 0) {
				assertEquals(ships.get(0).getCoord().x, 1);
				assertEquals(ships.get(0).getCoord().y, 2);
				assertEquals(ships.get(0).getOrientation(), Orientation.NORTH);
			} else {
				assertEquals(ships.get(0).getCoord().x, 9);
				assertEquals(ships.get(0).getCoord().y, 4);
				assertEquals(ships.get(0).getOrientation(), Orientation.EAST);
			}
			i++;
		}
	}
	
	@Test(expected = RuntimeException.class)
	public void testShipUnknownOrientationThrowsException() throws IOException {
		// TODO: This should be less brittle and test a more specific exception is thrown
		ClassicInputParserService parserSpy = spy(parser);
		BufferedReader reader = mock(BufferedReader.class);
		InputStream inputStream = mock(InputStream.class);
		doReturn(reader).when(parserSpy).getReader(inputStream);
		when(reader.readLine()).thenReturn("10").thenReturn("(1, 2, H)").thenReturn(null);
		// -------------------------------------------------------
		parserSpy.parseInput(inputStream);
		// -------------------------------------------------------
	}
	
	@Test(expected = RuntimeException.class)
	public void testShipCoordBeyondBoardSizeThrowsException() throws IOException {
		// TODO: This should be less brittle and test a more specific exception is thrown
		ClassicInputParserService parserSpy = spy(parser);
		BufferedReader reader = mock(BufferedReader.class);
		InputStream inputStream = mock(InputStream.class);
		doReturn(reader).when(parserSpy).getReader(inputStream);
		when(reader.readLine()).thenReturn("5").thenReturn("(6, 5, E)").thenReturn(null);
		// -------------------------------------------------------
		parserSpy.parseInput(inputStream);
		// -------------------------------------------------------
	}
	
	@Test
	public void testParsesShootOperation() throws IOException {
		ClassicInputParserService parserSpy = spy(parser);
		BufferedReader reader = mock(BufferedReader.class);
		InputStream inputStream = mock(InputStream.class);
		doReturn(reader).when(parserSpy).getReader(inputStream);
		when(reader.readLine()).thenReturn("10").thenReturn("(1, 2, N)").thenReturn("(1, 2)").thenReturn(null);
		// -------------------------------------------------------
		GameModel model = parserSpy.parseInput(inputStream);
		// -------------------------------------------------------
		assertEquals(model.size(), 1);
		assertEquals(model.get(0).getCoord(), new Coord(1, 2));
		assertEquals(model.get(0).size(), 1);
		assertTrue(model.get(0).get(0) instanceof ShootAction);
	}
	
	@Test
	public void testParsesMoveOperation() throws IOException {
		ClassicInputParserService parserSpy = spy(parser);
		BufferedReader reader = mock(BufferedReader.class);
		InputStream inputStream = mock(InputStream.class);
		doReturn(reader).when(parserSpy).getReader(inputStream);
		when(reader.readLine()).thenReturn("10").thenReturn("(3, 4, S)").thenReturn("(3, 4) MRL").thenReturn(null);
		// -------------------------------------------------------
		GameModel model = parserSpy.parseInput(inputStream);
		// -------------------------------------------------------
		assertEquals(model.size(), 1);
		assertEquals(model.get(0).getCoord(), new Coord(3, 4));
		assertEquals(model.get(0).size(), 3);
		assertTrue(model.get(0).get(0) instanceof MoveAction);
		assertTrue(model.get(0).get(1) instanceof TurnAction);
		assertEquals(((TurnAction)model.get(0).get(1)).getDirection(), Direction.RIGHT);
		assertTrue(model.get(0).get(2) instanceof TurnAction);
		assertEquals(((TurnAction)model.get(0).get(2)).getDirection(), Direction.LEFT);
	}
	
	@Test
	public void testNoOperationContainsShootAndMove() throws IOException {
		ClassicInputParserService parserSpy = spy(parser);
		BufferedReader reader = mock(BufferedReader.class);
		InputStream inputStream = mock(InputStream.class);
		doReturn(reader).when(parserSpy).getReader(inputStream);
		when(reader.readLine()).thenReturn("10").thenReturn("(3, 4, S)").thenReturn("(3, 4) MRL")
			.thenReturn("(8, 8)").thenReturn("(2, 4) LRMLR").thenReturn(null);
		// -------------------------------------------------------
		GameModel model = parserSpy.parseInput(inputStream);
		// -------------------------------------------------------
		assertEquals(model.size(), 3);
		assertEquals(model.get(0).size(), 3);
		for(Action action : model.get(0)) {
			assertFalse(action instanceof ShootAction);
		}
		assertEquals(model.get(1).size(), 1);
		assertTrue(model.get(1).get(0) instanceof ShootAction);
		assertEquals(model.get(2).size(), 5);
		for(Action action : model.get(2)) {
			assertFalse(action instanceof ShootAction);
		}	
	}
	
	@Test(expected = RuntimeException.class)
	public void testInvalidOperationCoordinatesThrowsException() throws IOException {
		ClassicInputParserService parserSpy = spy(parser);
		BufferedReader reader = mock(BufferedReader.class);
		InputStream inputStream = mock(InputStream.class);
		doReturn(reader).when(parserSpy).getReader(inputStream);
		when(reader.readLine()).thenReturn("10").thenReturn("(3, 4, S)").thenReturn("(99, 102) MRL").thenReturn(null);
		// -------------------------------------------------------
		GameModel model = parserSpy.parseInput(inputStream);
		// -------------------------------------------------------
	}
	
	@Test(expected = RuntimeException.class)
	public void testMalformedOperationThrowsException() throws IOException {
		ClassicInputParserService parserSpy = spy(parser);
		BufferedReader reader = mock(BufferedReader.class);
		InputStream inputStream = mock(InputStream.class);
		doReturn(reader).when(parserSpy).getReader(inputStream);
		when(reader.readLine()).thenReturn("10").thenReturn("(3, 4, S)").thenReturn("(1, 1) BLABLA").thenReturn(null);
		// -------------------------------------------------------
		GameModel model = parserSpy.parseInput(inputStream);
		// -------------------------------------------------------
	}
}
