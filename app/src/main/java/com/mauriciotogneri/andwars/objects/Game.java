package com.mauriciotogneri.andwars.objects;

import android.graphics.Color;

import com.mauriciotogneri.andwars.R;
import com.mauriciotogneri.andwars.objects.players.Player;
import com.mauriciotogneri.andwars.states.Initialization;
import com.mauriciotogneri.andwars.states.TurnManager;
import com.mauriciotogneri.andwars.ui.renders.GameRenderer;

import java.util.ArrayList;
import java.util.List;

public class Game
{
    private final GameMode mode;
    private final Map map;
    private final List<Player> players;
    private GameRenderer gameRenderer;
    private TurnManager turnManager;

    private boolean started = false;
    private boolean finished = false;
    private boolean screenLocked = true;

    private final List<OnCellSelected> onCellSelectedListeners = new ArrayList<>();

    public static final int NUMBER_INITIAL_CELLS = 5;
    public static final int NUMBER_MOVES_PER_PLAYER = 2;

    public enum GameMode
    {
        VS_COMPUTER, VS_HUMAN
    }

    private enum GameAction
    {
        START, RESTART, CLOSE
    }

    public enum GameResult
    {
        BLUE(R.string.dialog_game_finished_blue), RED(R.string.dialog_game_finished_red), TIE(R.string.dialog_game_finished_tie);

        private final int textId;

        GameResult(int textId)
        {
            this.textId = textId;
        }

        public int getTextId()
        {
            return this.textId;
        }
    }

    public Game(GameMode mode, Map map, List<Player> players)
    {
        this.mode = mode;
        this.map = map;
        this.players = players;

        for (Player player : players)
        {
            player.initialize(this);
        }
    }

    public void setGameRenderer(GameRenderer gameRenderer)
    {
        this.gameRenderer = gameRenderer;
        updateMap();
    }

    public void start()
    {
        this.started = true;
        this.finished = false;

        updateMap();

        Initialization initialization = new Initialization(this, this.players);
        initialization.start();
    }

    public void restart()
    {
        this.finished = false;

        lockScreen();
        restartPlayers();
        this.map.restart();
        updateMap();

        Initialization initialization = new Initialization(this, this.players);
        initialization.start();
    }

    private void restartPlayers()
    {
        for (Player player : this.players)
        {
            player.restart();
        }
    }

    public boolean isStarted()
    {
        return this.started;
    }

    private synchronized boolean isScreenLocked()
    {
        return this.screenLocked;
    }

    public synchronized void lockScreen()
    {
        this.screenLocked = true;
        this.gameRenderer.lockButtons();
    }

    public synchronized void unlockScreen(boolean unlockButtons)
    {
        this.screenLocked = false;

        if (unlockButtons)
        {
            this.gameRenderer.unlockButtons();
        }
    }

    public Map getMap()
    {
        return this.map;
    }

    public void updateMap()
    {
        this.gameRenderer.update(this.map);
    }

    public void updateMap(Move move)
    {
        this.gameRenderer.update(this.map, move);
    }

    public void updateTurnNumber(int turn)
    {
        this.gameRenderer.updateTurnNumber(turn);
    }

    public void onClick(int x, int y)
    {
        if (!isScreenLocked())
        {
            List<Cell> cells = this.map.getCells();

            for (Cell cell : cells)
            {
                if ((cell.x == x) && (cell.y == y))
                {
                    for (OnCellSelected listener : this.onCellSelectedListeners)
                    {
                        listener.onCellSelected(cell);
                    }
                    break;
                }
            }
        }
    }

    public void addOnCellSelectedListener(OnCellSelected listener)
    {
        this.onCellSelectedListeners.add(listener);
    }

    public void gameInitialized()
    {
        this.turnManager = new TurnManager(this, this.players);
        this.turnManager.start();
    }

    public void updateUnits()
    {
        this.map.updateUnits(this);
        updateMap();
    }

    public boolean isFinished()
    {
        return this.finished;
    }

    public void gameFinished(Player winner)
    {
        this.finished = true;

        if (winner.isBlue())
        {
            this.gameRenderer.showEndMessage(GameResult.BLUE, winner.borderColor);
        }
        else
        {
            this.gameRenderer.showEndMessage(GameResult.RED, winner.borderColor);
        }
    }

    public void gameTie()
    {
        this.finished = true;

        this.gameRenderer.showEndMessage(GameResult.TIE, Color.BLACK);
    }

    public void passTurn()
    {
        if ((!isScreenLocked()) && (this.turnManager != null))
        {
            this.turnManager.passTurn();
        }
    }

    public interface OnCellSelected
    {
        void onCellSelected(Cell cell);
    }
}