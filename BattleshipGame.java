import java.lang.classfile.ClassFile;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.util.Duration;

// Observer Pattern: Board observers
interface BoardObserver {
    void update();
}

// Strategy Pattern: Ship Placement
interface ShipPlacementStrategy {
    void placeShips(Board board);
}

// Concrete Strategy: Random Placement
class RandomPlacement implements ShipPlacementStrategy {
    @Override
    public void placeShips(Board board) {
        Random random = new Random();
        Ship ship = new Ship();
        for (int i = 0; i < 5; i++) { 
            boolean shipInBounds = false; //checks to make sure the ship is in bounds
            boolean shipNotCombined = false; //checks to make sure the ship is not combined with another ship
            while (!shipInBounds || !shipNotCombined) {
                shipInBounds = false;
                shipNotCombined = true; 
                ship = new Ship(random.nextInt(10), random.nextInt(10), i, random.nextInt(2)==1);
                if (ship.getVertical() && 10 - ship.getY() >= ship.getSize()) {
                    shipInBounds = true;
                }
                else if(!ship.getVertical() && 10 - ship.getX() >= ship.getSize()) {
                    shipInBounds = true;
                }
                for (Ship checkShip : board.ships) {
                    if (checkShip.checkIfCombined(ship, board)) {
                        shipNotCombined = false;
                        System.out.print("hihello");
                        break;
                    }
                }
            }
            for (Ship shipCheck : board.ships) {
                
            System.out.print(shipCheck.getTiles());
            System.err.println(i);
            System.out.println("\n");
            }
        board.addShip(ship);
        }
    }
}

// Strategy Pattern: Computer Attack Strategy
interface AttackStrategy {
    int[] getAttackCoordinates(Board board);
}

// Easy AI: Random Attacks
class EasyAttackStrategy implements AttackStrategy {
    private final Random random = new Random();

    @Override
    public int[] getAttackCoordinates(Board board) {
        return new int[] { random.nextInt(10), random.nextInt(10) };
    }
}

class Tile{ 
    private int x, y;
    private boolean ship = false;
    private boolean hit = false;

    public Tile(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean getShip() {
        return ship;
    }

    public boolean getHit() {
        return hit;
    }

    public void setShip(boolean set) {
        ship = set;
    }

    public void setHit(boolean set) {
        hit = set;
    }
}

// Game Board (Observable)
class Board {
    public final List<Ship> ships = new ArrayList<>();
    private final List<int[]> attacks = new ArrayList<>();
    private final List<BoardObserver> observers = new ArrayList<>();
    private final List<Tile> tiles = new ArrayList<>();
    private final ShipPlacementStrategy placementStrategy;

    public Board(ShipPlacementStrategy strategy) {
        this.placementStrategy = strategy;
        createTiles();
        placementStrategy.placeShips(this);
    }

    public void addShip(Ship ship) {
        ships.add(ship);
        notifyObservers();
        addShipToTiles(ship);
    }

    public void addShipToTiles(Ship ship){
        List<Tile> shipTiles = new ArrayList<>();
        for (int i = 0; i < ship.getSize(); i++) {
            if (ship.getVertical()) {
                findTile(ship.getX(), ship.getY() + i).setShip(true);
                shipTiles.add(findTile(ship.getX(), ship.getY() + i));
            }
            else {
                findTile(ship.getX() + i, ship.getY()).setShip(true);
                shipTiles.add(findTile(ship.getX() + i, ship.getY()));
            }
        }
        ship.setTiles(shipTiles);
    }

    private void createTiles() {
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                tiles.add(new Tile(i, j));
            }
        }
    }

    public Tile findTile(int x, int y) {
        for (Tile tile : tiles) {
            if (tile.getX() == x && tile.getY() == y) {
                return tile;
            }
        }
        return null;
    }

    public boolean attack(int x, int y) {
        attacks.add(new int[] { x, y });
        for (Ship ship : ships) {
            if (ship.getTiles().contains(findTile(x, y))) {
                ship.shipAttacked();
                if (ship.checkIfDestroyed()){
                    ships.remove(ship);
                }
                notifyObservers();
                return true;
            }
        }
        notifyObservers();
        return false;
    }

    public List<int[]> getAttacks() {
        return attacks;
    }

    public void addObserver(BoardObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers() {
        for (BoardObserver observer : observers) {
            observer.update();
        }
    }

    private boolean checkIfLost() {
        if (ships.isEmpty()){ 
            return true;
        }
        return false;
    }
}

// Ship Class
class Ship {
    private List<Tile> tiles = new ArrayList<>();
    private int x, y;
    private int type, size;
    private int attackCount = 0;
    private boolean vertical;

    public Ship(int x, int y, int type, boolean vertical) {
        this.x = x;
        this.y = y;
        this.type = type;
        this.vertical = vertical;
        sizeByType();
    }

    public Ship(){

    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getType() {
        return type;
    }

    public int getSize() {
        return size;
    }

    public int getAttackCount() {
        return attackCount;
    }

    public boolean getVertical() {
        return vertical;
    }

    public List<Tile> getTiles(){
        return tiles;
    }

    public void setTiles(List<Tile> set) {
        tiles = set;
    }

    public boolean checkIfCombined(Ship ship, Board board) {
        for (int i = 0; i < ship.size; i++) {
            if (ship.vertical) {
                if (tiles.contains(board.findTile(ship.x, ship.y + i))) {
                    return true;
                }
            }
            else {
                if (tiles.contains(board.findTile(ship.x + i, ship.y))) {
                    return true;
                }
            }
        }
        return false;
    }

    public void shipAttacked() {
        attackCount++;
    }

    public boolean checkIfDestroyed() {
        if (attackCount == size) {
            return true;
        }
        return false;
    }

    // This function calculates the size of the ship by its type
    // 0 = destroyer, 1 = submarine, 2 = cruiser, 3 = battleship, 4 = carrier
    // if the type is invalid, the size will be -1
    private void sizeByType() {
        switch (type) {
            case 0:
                size = 2;
                break;
            case 1:
                size = 3;
                break;
            case 2:
                size = 3;
                break;
            case 3:
                size = 4;
                break;
            case 4:
                size = 5;
                break;
            default:
                size = -1;
        }
    }
}

// JavaFX UI
public class BattleshipGame extends Application implements BoardObserver {
    private static final int TILE_SIZE = 50;
    private static final int GRID_SIZE = 10;
    private final Board playerBoard = new Board(new RandomPlacement());
    private final Board enemyBoard = new Board(new RandomPlacement());
    private final Canvas playerCanvas = new Canvas(GRID_SIZE * TILE_SIZE, GRID_SIZE * TILE_SIZE);
    private final Canvas enemyCanvas = new Canvas(GRID_SIZE * TILE_SIZE, GRID_SIZE * TILE_SIZE);
    private final Image shipImage = new Image("ship_placeholder.png");
    private final AttackStrategy attackStrategy = new EasyAttackStrategy();

    @Override
    public void start(Stage stage) {
        Pane root = new Pane(playerCanvas, enemyCanvas);
        enemyCanvas.setLayoutX(GRID_SIZE * TILE_SIZE + 20);
        Scene scene = new Scene(root);
        playerBoard.addObserver(this);
        enemyBoard.addObserver(this);

        enemyCanvas.setOnMouseClicked(this::handlePlayerAttack);
        update();

        stage.setScene(scene);
        stage.setTitle("Battleships");
        stage.show();
    }

    private void handlePlayerAttack(MouseEvent event) {
        int x = (int) event.getX() / TILE_SIZE;
        int y = (int) event.getY() / TILE_SIZE;
        if (!enemyBoard.attack(x, y)) {
            enemyTurn();
        }
    }

    private void enemyTurn() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            int[] attackCoords = attackStrategy.getAttackCoordinates(playerBoard);
            playerBoard.attack(attackCoords[0], attackCoords[1]);
        }));
        timeline.setCycleCount(1);
        timeline.play();
    }

    @Override
    public void update() {
        GraphicsContext gcPlayer = playerCanvas.getGraphicsContext2D();
        GraphicsContext gcEnemy = enemyCanvas.getGraphicsContext2D();

        gcPlayer.clearRect(0, 0, playerCanvas.getWidth(), playerCanvas.getHeight());
        gcEnemy.clearRect(0, 0, enemyCanvas.getWidth(), enemyCanvas.getHeight());

        // Draw grid
        gcPlayer.setStroke(Color.BLACK);
        gcEnemy.setStroke(Color.BLACK);
        for (int i = 0; i <= GRID_SIZE; i++) {
            gcPlayer.strokeLine(i * TILE_SIZE, 0, i * TILE_SIZE, GRID_SIZE * TILE_SIZE);
            gcPlayer.strokeLine(0, i * TILE_SIZE, GRID_SIZE * TILE_SIZE, i * TILE_SIZE);
            gcEnemy.strokeLine(i * TILE_SIZE, 0, i * TILE_SIZE, GRID_SIZE * TILE_SIZE);
            gcEnemy.strokeLine(0, i * TILE_SIZE, GRID_SIZE * TILE_SIZE, i * TILE_SIZE);
        }

        // Draw ships on player board
        for (Ship ship : playerBoard.ships) {
            for (int i = 0; i < ship.getSize(); i++) {
                if (ship.getVertical()) {
                    gcPlayer.drawImage(shipImage, (ship.getX()) * TILE_SIZE, (ship.getY() + i) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
                else {
                    gcPlayer.drawImage(shipImage, (ship.getX() + i) * TILE_SIZE, (ship.getY()) * TILE_SIZE, TILE_SIZE, TILE_SIZE);
                }
            }
            System.out.println(ship.getSize());
        }

        // Draw attack attempts
        gcPlayer.setFill(Color.RED);
        gcEnemy.setFill(Color.BLUE);
        for (int[] attack : playerBoard.getAttacks()) {
            gcPlayer.fillOval(attack[0] * TILE_SIZE + TILE_SIZE / 4, attack[1] * TILE_SIZE + TILE_SIZE / 4,
                    TILE_SIZE / 2, TILE_SIZE / 2);
        }
        for (int[] attack : enemyBoard.getAttacks()) {
            gcEnemy.fillOval(attack[0] * TILE_SIZE + TILE_SIZE / 4, attack[1] * TILE_SIZE + TILE_SIZE / 4,
                    TILE_SIZE / 2, TILE_SIZE / 2);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}