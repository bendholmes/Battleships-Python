package net.blacksails.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.blacksails.action.MoveAction;
import net.blacksails.action.ShootAction;
import net.blacksails.action.TurnAction;
import net.blacksails.domain.Coord;
import net.blacksails.domain.Direction;
import net.blacksails.domain.GameBoard;
import net.blacksails.domain.GameModel;
import net.blacksails.domain.Operation;
import net.blacksails.domain.Orientation;
import net.blacksails.domain.Ship;

/**
 * Parses the input according to the spec.
 * 
 * TODO: This is pretty messy and should be refactored. Let's assume this is a production MVP??
 */
public class ClassicInputParserService implements InputParserService {
	private static final int BOARD_SIZE_LINE = 0;
	private static final int SHIPS_LINE = 1;
	
	private static final Pattern SHIP_PATTERN = Pattern.compile("(\\(\\d+, \\d+, \\w\\))\\s*");
	private static final Pattern SHIP_VALUES_PATTERN = Pattern.compile("\\((\\d+), (\\d+), (\\w)\\)");
	private static final Pattern OPERATION_PATTERN = Pattern.compile("(\\(\\d+, \\d+\\))\\s*(\\w+)*");
	private static final Pattern OPERATION_COORD_PATTERN = Pattern.compile("\\((\\d+), (\\d+)\\)");
	
	private static final int OPERATION_SHOOT_GROUP_COUNT = 1;
	private static final int OPERATION_OTHER_GROUP_COUNT = 2;
	
	private static final char ACTION_MOVE_TOKEN = 'M';
	private static final char ACTION_TURN_LEFT_TOKEN = 'L';
	private static final char ACTION_TURN_RIGHT_TOKEN = 'R';
	
	@Override
	public GameModel parseInput(InputStream inputStream) {
	    BufferedReader reader = getReader(inputStream);
    	String line;
    	int lineNumber = 0;
    	GameBoard initialBoard = null;
    	GameModel gameModel = null;
	    
	    try {
	    	while ((line = reader.readLine()) != null) {
	    		switch(lineNumber) {
	    			case BOARD_SIZE_LINE:
	    				initialBoard = createGameBoard(line);
	    				gameModel = new GameModel(initialBoard);
	    				break;
	    			case SHIPS_LINE:
	    				loadShips(initialBoard, line);
	    				break;
	    			default:
	    				loadOperation(gameModel, line);
	    		}
	    		
	    		lineNumber++;
	    	}
	    } catch (IOException e) {
	    	throw new RuntimeException(e);
	    }
	    
	    return gameModel;
	}
	
	public BufferedReader getReader(InputStream inputStream) {
		return new BufferedReader(new InputStreamReader(inputStream));
	}
	
	private GameBoard createGameBoard(String sizeLine) {
		int boardSize = Integer.parseInt(sizeLine);
		
		if(boardSize < 1) {
			throw new RuntimeException("Board size '" + boardSize + "' is invalid, must be > 0.");
		}
		return new GameBoard(boardSize);
	}
	
	private void loadShips(GameBoard board, String ships) {
		Matcher shipMatcher = SHIP_PATTERN.matcher(ships);
		while(shipMatcher.find()) {
			Matcher shipValuesMatcher = SHIP_VALUES_PATTERN.matcher(shipMatcher.group(1));
			if(shipValuesMatcher.matches()) {
				Coord coord = parseCoord(board, shipValuesMatcher.group(1), shipValuesMatcher.group(2));
				if(board.get(coord) != null) {
					throw new RuntimeException("Two ships found starting in the same location (" + coord + ").");
				}
				List<Ship> shipSet = new ArrayList<Ship>();
				shipSet.add(parseShip(coord, shipValuesMatcher.group(3)));
				board.put(coord, shipSet);
			}
		}
	}
	
	private Coord parseCoord(GameBoard board, String coordX, String coordY) {
		int x = Integer.valueOf(coordX);
		int y = Integer.valueOf(coordY);
		
		Coord coord = new Coord(x, y);
		coord.isValid(board);
		return coord;
	}
	
	private Ship parseShip(Coord coord, String orientationString) {
		return new Ship(coord, getOrientationFromString(orientationString));
	}
	
	private Orientation getOrientationFromString(String orientationString) {
		Orientation orientation = Orientation.LOOKUP.get(orientationString);
		if(orientation == null) {
			throw new RuntimeException("Could not determine orientation from string '" + orientationString + "'.");
		}
		return orientation;
	}
	
	/**
	 * Parses out an individual operation.
	 * 
	 * TODO: This needs refactoring, so much coupling...
	 */
	private void loadOperation(GameModel gameModel, String operationString) {
		Matcher operationMatcher = OPERATION_PATTERN.matcher(operationString);
		if(operationMatcher.matches()) {
			Coord coord;
			Matcher operationCoordMatcher = OPERATION_COORD_PATTERN.matcher(operationMatcher.group(1));
			if(operationCoordMatcher.matches()) {
				coord = parseCoord(gameModel.getInitialBoard(), operationCoordMatcher.group(1), operationCoordMatcher.group(2));
			} else {
				throw new RuntimeException("Could not determine coord from operation '" + operationMatcher.group(1) + "'.");
			}
			
			Operation operation = new Operation(coord);
			String actionList = operationMatcher.group(2);
			
			if(actionList == null) {
				operation.add(new ShootAction());
				gameModel.add(operation);
			} else {
				for(char actionToken : actionList.toCharArray()) {
					switch(actionToken) {
						case ACTION_MOVE_TOKEN:
							operation.add(new MoveAction());
							break;
						case ACTION_TURN_LEFT_TOKEN:
							operation.add(new TurnAction(Direction.LEFT));
							break;
						case ACTION_TURN_RIGHT_TOKEN:
							operation.add(new TurnAction(Direction.RIGHT));
							break;
						default:
							throw new RuntimeException("Could not determine action from token '" + actionToken + "'.");
					}
				}
				gameModel.add(operation);
			}
		} else {
			throw new RuntimeException("Could not parse from operation '" + operationString + "'.");
		}
	}
}
