package main.uj.wmii.pwj.battleships;

public interface BattleshipGenerator {

    String generateMap();

    static BattleshipGenerator defaultInstance() {
        return new Creator();
    }

}
