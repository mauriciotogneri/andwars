package com.mauriciotogneri.andwars.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;

import com.mauriciotogneri.andwars.R;
import com.mauriciotogneri.andwars.objects.Game.GameMode;
import com.mauriciotogneri.andwars.objects.Map;
import com.mauriciotogneri.andwars.ui.renders.MapRenderer;
import com.mauriciotogneri.andwars.ui.renders.MapRenderer.MapListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainMenuActivity extends Activity implements MapListener
{
    private int mapIndex = 0;
    private final List<Map> maps = new ArrayList<>();
    private MapRenderer mapRenderer;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setTitle("  " + getString(R.string.app_name));

        this.maps.addAll(getMaps());

        FrameLayout mapContainer = findViewById(R.id.map_container);

        this.mapRenderer = new MapRenderer(this, this);
        mapContainer.addView(this.mapRenderer, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        ImageButton mapPrevious = findViewById(R.id.map_previous);
        mapPrevious.setOnClickListener(v -> previousMap());

        ImageButton mapNext = findViewById(R.id.map_next);
        mapNext.setOnClickListener(v -> nextMap());

        ImageButton startGameComputer = findViewById(R.id.mainmenu_button_start_game_vs_computer);
        startGameComputer.setOnClickListener(view -> startGame(GameMode.VS_COMPUTER));

        ImageButton startGameHuman = findViewById(R.id.mainmenu_button_start_game_vs_human);
        startGameHuman.setOnClickListener(view -> startGame(GameMode.VS_HUMAN));

        updateMap();
    }

    private void previousMap()
    {
        moveIndex(-1);
        updateMap();
    }

    private void nextMap()
    {
        moveIndex(1);
        updateMap();
    }

    private void moveIndex(int value)
    {
        this.mapIndex += value;

        if (this.mapIndex < 0)
        {
            this.mapIndex = this.maps.size() - 1;
        }
        else if (this.mapIndex >= this.maps.size())
        {
            this.mapIndex = 0;
        }
    }

    private Map getSelectedMap()
    {
        return this.maps.get(this.mapIndex);
    }

    private void updateMap()
    {
        Map map = getSelectedMap();

        TextView mapName = findViewById(R.id.map_name);
        mapName.setText(map.toString());

        updateMap(map);
    }

    private List<Map> getMaps()
    {
        List<Map> result = new ArrayList<>();

        try
        {
            List<String> mapPaths = readIndex(getAssets());

            for (String mapPath : mapPaths)
            {
                Map map = new Map(mapPath, this);
                result.add(map);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return result;
    }

    private void updateMap(Map map)
    {
        if (map != null)
        {
            this.mapRenderer.update(map);
        }
    }

    private void startGame(GameMode mode)
    {
        Map map = getSelectedMap();

        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra(GameActivity.PARAMETER_GAME_MODE, mode);
        intent.putExtra(GameActivity.PARAMETER_MAP_NAME, map.getPath());

        startActivity(intent);
    }

    private void showHelp()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_help_main_menu);
        builder.setIcon(android.R.drawable.ic_menu_help);
        builder.setCancelable(true);

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout = inflater.inflate(R.layout.dialog_help_main_menu, null);
        builder.setView(layout);

        builder.setPositiveButton(R.string.button_accept, null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private List<String> readIndex(AssetManager assetManager) throws IOException
    {
        List<String> result = new ArrayList<>();

        BufferedReader bufferedReader = null;

        try
        {
            bufferedReader = new BufferedReader(new InputStreamReader(assetManager.open("asset.index")));
            String line;

            while ((line = bufferedReader.readLine()) != null)
            {
                result.add(line.replace("\\", "/"));
            }
        }
        finally
        {
            if (bufferedReader != null)
            {
                bufferedReader.close();
            }
        }

        return result;
    }

    @Override
    protected void onResume()
    {
        final Map map = getSelectedMap();

        Handler handler = new Handler();
        handler.post(() -> {
            if (map != null)
            {
                updateMap(map);
            }
        });

        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.mainmenu_help:
                showHelp();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onCreateMap()
    {
        updateMap();
    }
}