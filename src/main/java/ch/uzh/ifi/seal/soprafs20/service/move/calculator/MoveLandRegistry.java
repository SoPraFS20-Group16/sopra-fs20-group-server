package ch.uzh.ifi.seal.soprafs20.service.move.calculator;

import ch.uzh.ifi.seal.soprafs20.constant.ErrorMsg;
import ch.uzh.ifi.seal.soprafs20.entity.Game;
import ch.uzh.ifi.seal.soprafs20.entity.game.Board;
import ch.uzh.ifi.seal.soprafs20.entity.game.Player;
import ch.uzh.ifi.seal.soprafs20.entity.game.Tile;
import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.Building;
import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.City;
import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.Road;
import ch.uzh.ifi.seal.soprafs20.entity.game.buildings.Settlement;
import ch.uzh.ifi.seal.soprafs20.entity.game.coordinate.Coordinate;
import ch.uzh.ifi.seal.soprafs20.entity.moves.BuildMove;
import ch.uzh.ifi.seal.soprafs20.entity.moves.development.RoadProgressMove;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;

/**
 * this helper method has the task similar to a land registry
 * - it can get already created buildings from players
 * - it can validate new building coordinates
 */
public class MoveLandRegistry {

    private MoveLandRegistry() {
        throw new IllegalStateException(ErrorMsg.INIT_MSG);
    }

    // -- get building(s) from player --

    static List<Road> getRoadsOfPlayer(Player player, Board board) {

        List<Road> roads = new ArrayList<>();

        for (Road road : board.getRoads()) {
            if (road.getUserId().equals(player.getUserId())) {
                roads.add(road);
            }
        }
        return roads;
    }

    static List<Settlement> getSettlementsOfPlayer(Player player, Board board) {

        List<Settlement> settlements = new ArrayList<>();

        for (Settlement settlement : board.getSettlements()) {
            if (settlement.getUserId().equals(player.getUserId())) {
                settlements.add(settlement);
            }
        }
        return settlements;
    }

    static List<City> getCitiesOfPlayer(Player player, Board board) {

        List<City> cities = new ArrayList<>();

        for (City city : board.getCities()) {
            if (city.getUserId().equals(player.getUserId())) {
                cities.add(city);
            }
        }
        return cities;
    }

    // -- get building(s) from tile --

    private static void getSettlementsFromTile(List<Building> buildings, Board board, Tile tile) {
        for (Settlement settlement : board.getSettlements()) {
            if (tile.getCoordinates().contains(settlement.getCoordinate())) {
                buildings.add(settlement);
            }
        }
    }

    private static void getCitiesFromTile(List<Building> buildings, Board board, Tile tile) {
        for (City city : board.getCities()) {
            if (tile.getCoordinates().contains(city.getCoordinate())) {
                buildings.add(city);
            }
        }
    }

    public static List<Building> getBuildingsFromTileWithRobber(Game game) {

        List<Building> buildings = new ArrayList<>();

        Board board = game.getBoard();

        for (Tile tile : board.getTiles()) {

            if (tile.hasRobber()) {

                getSettlementsFromTile(buildings, board, tile);

                getCitiesFromTile(buildings, board, tile);
            }
        }
        return buildings;
    }

    // -- valid-building-coordinate helper methods --

    static boolean isValidBuildingCoordinate(Board board, Coordinate coordinate) {

        if (board.hasBuildingWithCoordinate(coordinate)) {
            return false;
        }
        for (Coordinate neighbor : coordinate.getNeighbors()) {
            if (board.hasBuildingWithCoordinate(neighbor)) {
                return false;
            }
        }
        return true;
    }

    static List<Coordinate> getRoadCoordinates(List<Road> roads) {

        List<Coordinate> roadCoordinates = new ArrayList<>();

        for (Road road : roads) {
            roadCoordinates.add(road.getCoordinate1());
            roadCoordinates.add(road.getCoordinate2());
        }

        return roadCoordinates;
    }

    static List<Coordinate> getRoadEndPoints(Player player, Board board) {

        List<Coordinate> validEndPoints = new ArrayList<>();

        List<Road> roads = getRoadsOfPlayer(player, board);

        // check if road is between buildings / other roads
        for (Road road : roads) {




            if ()

            Coordinate coordinate1 = road.getCoordinate1();
            Coordinate coordinate2 = road.getCoordinate2();

            Coordinate neighbour = null;
            Coordinate secondNeighbour = null;

            for (Coordinate candidate: coordinate1.getNeighbors()) {
                if (!candidate.equals(coordinate2)) {
                    neighbour = candidate;
                    break;
                }
            }

            for (Coordinate candidate: coordinate1.getNeighbors()) {
                if (!candidate.equals(coordinate1) && !candidate.equals(neighbour)) {
                    secondNeighbour = candidate;
                    break;
                }
            }

            if (board.hasRoadWithCoordinates(coordinate1, neighbour)) {
                // check if road belongs to player
                continue;
            }

            if (secondNeighbour != null &&
                    board.hasRoadWithCoordinates(coordinate1, secondNeighbour)) {
                // check if road belongs to player
                continue;
            }

            validEndPoints.add(coordinate1);
        }

        return validEndPoints;
    }

            for (Coordinate neighbour: coordinate1.getNeighbors()) {


                if (neighbour.equals(coordinate2)) {
                    continue;
                }

                if (board.hasRoadWithCoordinates(coordinate1, neighbour)) {

                    if (board.getRoadWithCoordinates(coordinate1, neighbour).getUserId()
                            .equals(player.getUserId())) {
                        break;

                    } else if (board.hasRoadWithCoordinates(coordinate1)) {


                    }
                }
            }
        }


        List<Coordinate> coordinates = getRoadCoordinates(roads);

        int currentSize = coordinates.size();
        for (int i = 0; i < currentSize; i++) {
            Coordinate currentCoordinate = coordinates.get(i);
            int count = 0;
            for (Coordinate coordinate : coordinates) {
                if (currentCoordinate.equals(coordinate)) {
                    count++;
                }
                if (count > 1) {
                    break;
                }
            }
            coordinates.removeIf(o -> o.equals(currentCoordinate));
            currentSize = coordinates.size();
        }

        return coordinates;
    }

    static void calculateRoadBuildingMovesConnectingToBuilding(Game game, List<BuildMove> possibleMoves, Player player, Board board) {

        List<Settlement> settlements = getSettlementsOfPlayer(player, board);
        for (Settlement settlement : settlements) {
            Coordinate coordinate = settlement.getCoordinate();
            for (Coordinate neighbor : coordinate.getNeighbors()) {
                if (!board.hasRoadWithCoordinates(coordinate, neighbor)) {
                    BuildMove move = MoveCreator.createRoadMove(game, player, coordinate, neighbor);
                    possibleMoves.add(move);
                }
            }
        }

        List<City> cities = getCitiesOfPlayer(player, board);
        for (City city : cities) {
            Coordinate coordinate = city.getCoordinate();
            for (Coordinate neighbor : coordinate.getNeighbors()) {
                if (!board.hasRoadWithCoordinates(coordinate, neighbor)) {
                    BuildMove move = MoveCreator.createRoadMove(game, player, coordinate, neighbor);
                    possibleMoves.add(move);
                }
            }
        }
    }

    static void calculateRoadProgressMovesConnectingToBuilding(Game game, List<RoadProgressMove> possibleMoves,
                                                               Player player, Board board, int previousRoadProgressMoves) {

        List<Settlement> settlements = getSettlementsOfPlayer(player, board);
        for (Settlement settlement : settlements) {
            Coordinate coordinate = settlement.getCoordinate();
            for (Coordinate neighbor : coordinate.getNeighbors()) {
                if (!board.hasRoadWithCoordinates(coordinate, neighbor)) {
                    RoadProgressMove move = MoveCreator.createRoadProgressMove(game, player, coordinate,
                            neighbor, previousRoadProgressMoves);
                    possibleMoves.add(move);
                }
            }
        }

        List<City> cities = getCitiesOfPlayer(player, board);
        for (City city : cities) {
            Coordinate coordinate = city.getCoordinate();
            for (Coordinate neighbor : coordinate.getNeighbors()) {
                if (!board.hasRoadWithCoordinates(coordinate, neighbor)) {
                    RoadProgressMove move = MoveCreator.createRoadProgressMove(game, player, coordinate,
                            neighbor, previousRoadProgressMoves);
                    possibleMoves.add(move);
                }
            }
        }
    }

    static void calculateRoadBuildingMovesConnectingToRoad(Game game, List<BuildMove> possibleMoves, Player player, Board board) {

        // get all road end points
        List<Coordinate> roadEndPoints = getRoadEndPoints(player, board);

        // if there are open road end points, then calculate building coordinates
        if (!roadEndPoints.isEmpty()) {
            for (Coordinate coordinate : roadEndPoints) {
                for (Coordinate neighbor : coordinate.getNeighbors())
                    if (!board.hasRoadWithCoordinates(coordinate, neighbor)) {
                        BuildMove move = MoveCreator.createRoadMove(game, player, coordinate, neighbor);
                        possibleMoves.add(move);
                    }
            }
        }
    }

}
