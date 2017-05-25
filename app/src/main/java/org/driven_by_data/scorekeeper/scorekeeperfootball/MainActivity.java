package org.driven_by_data.scorekeeper.scorekeeperfootball;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    long HALF_TIME_LENGTH = 2700000;

    Button button_start;
    Button button_reset;
    Button button_goal_team1;
    Button button_goal_team2;

    Button edit_team1;
    Button edit_team2;
    TextView header_team1;
    TextView header_team2;

    List<Integer> scores = new ArrayList<>(2);

    TextView score_team1_display;
    TextView score_team2_display;

    ListView score_list_team1;
    ListView score_list_team2;

    TextView timerDisplay;
    CountDownTimer gameTimer;
    long timeCache;

    int halftime;
    boolean hasGameEnded;

    ArrayAdapter<String> goal_adapter_team1;
    ArrayAdapter<String> goal_adapter_team2;
    ArrayList<String> goals_team1;
    ArrayList<String> goals_team2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Typeface iconFont = FontManager.getTypeface(getApplicationContext(), FontManager.FONTAWESOME);
        FontManager.markAsIconContainer(findViewById(R.id.edit_team1), iconFont);
        FontManager.markAsIconContainer(findViewById(R.id.edit_team2), iconFont);

        button_start = (Button) findViewById(R.id.button_start);
        button_start.setTag(0);
        button_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getTag().equals(0)) {
                    startGame();
                } else if (v.getTag().equals(1)) {
                    pauseGame();
                } else {
                    resumeGame();
                }
            }
        });

        button_reset = (Button) findViewById(R.id.button_reset);
        button_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createResetConfirmationMessage();
            }
        });

        button_goal_team1 = (Button) findViewById(R.id.button_goal_team1);
        button_goal_team1.setTag(0);
        button_goal_team1.setEnabled(false);
        button_goal_team1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGoalDialog(v);
            }
        });

        button_goal_team2 = (Button) findViewById(R.id.button_goal_team2);
        button_goal_team2.setTag(1);
        button_goal_team2.setEnabled(false);
        button_goal_team2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createGoalDialog(v);
            }
        });

        timerDisplay = (TextView) findViewById(R.id.timer);
        initializeTimer();

        score_team1_display = (TextView) findViewById(R.id.score_team1);
        score_team2_display = (TextView) findViewById(R.id.score_team2);
        initializeScores();

        score_list_team1 = (ListView) findViewById(R.id.goal_list_team1);
        score_list_team2 = (ListView) findViewById(R.id.goal_list_team2);
        initializeScoreList();

        edit_team1 = (Button) findViewById(R.id.edit_team1);
        edit_team1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRenameDialog(0);
            }
        });

        edit_team2 = (Button) findViewById(R.id.edit_team2);
        edit_team2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createRenameDialog(1);
            }
        });

        header_team1 = (TextView) findViewById(R.id.header_team1);
        header_team2 = (TextView) findViewById(R.id.header_team2);

        hasGameEnded = true;
    }

    private void createTimer() {
        gameTimer = new CountDownTimer(timeCache, 1000) {

            public void onTick(long millisUntilFinished) {
                int secs = (int) (2 * HALF_TIME_LENGTH -
                        ((1 - halftime) * HALF_TIME_LENGTH + millisUntilFinished)) / 1000;
                int mins = secs / 60;
                secs = secs % 60;
                timerDisplay.setText(String.format("%02d", mins) + ":"
                        + String.format("%02d", secs));
                timeCache = millisUntilFinished;
            }

            public void onFinish() {
                if (halftime == 0) {
                    createHalfTimeMessage();
                } else {
                    endGame();
                }
            }
        };
    }

    private void initializeTimer() {
        timerDisplay.setText(R.string.timer);
        timeCache = HALF_TIME_LENGTH;
        halftime = 0;
        createTimer();
    }

    private void initializeScores() {
        score_team1_display.setText("0");
        score_team2_display.setText("0");

        Integer[] s = new Integer[2];
        Arrays.fill(s, 0);
        scores = Arrays.asList(s);
    }

    private void initializeScoreList() {
        goals_team1 = new ArrayList<String>();
        goals_team2 = new ArrayList<String>();

        goal_adapter_team1 = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                goals_team1);

        goal_adapter_team2 = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                goals_team2);

        score_list_team1.setAdapter(goal_adapter_team1);
        score_list_team2.setAdapter(goal_adapter_team2);
    }

    private void startGame() {
        if (hasGameEnded) {
            hasGameEnded = false;
        }
        initializeTimer();
        gameTimer.start();
        switchButtonState();
    }

    private void pauseGame() {
        gameTimer.cancel();
        switchButtonState();
    }

    private void resumeGame() {
        createTimer();
        gameTimer.start();
        switchButtonState();
    }

    private void endGame() {
        button_start.setTag(4);
        hasGameEnded = true;
        switchButtonState();

        createEndGameMessage();
    }

    private void resetGame() {
        hasGameEnded = true;

        gameTimer.cancel();
        button_start.setTag(5);
        switchButtonState();

        initializeTimer();
        initializeScores();
        changeTeamName(getString(R.string.team1_default), 0);
        changeTeamName(getString(R.string.team2_default), 1);
    }

    private void changeTeamName(String name, Integer team_id) {
        if (team_id == 0) {
            header_team1.setText(name);
        } else if (team_id == 1) {
            header_team2.setText(name);
        }
    }

    private void createRenameDialog(final Integer team_id) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = createTextEditDialog(R.string.team_rename_dialog_header,
                R.string.team_rename_edit_label, R.string.team_rename_edit_default);
        dialogBuilder.setView(dialogView);

        final EditText team_name = (EditText) dialogView.findViewById(R.id.dialog_textedit);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                changeTeamName(team_name.getText().toString(), team_id);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void createEndGameMessage() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.endgame_message);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void createHalfTimeMessage() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.halftime_message);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                halftime = 1;
                timeCache = HALF_TIME_LENGTH;
                resumeGame();
            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void createResetConfirmationMessage() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setMessage(R.string.reset_message);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                resetGame();
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void setScores(Integer team_id) {
        scores.set(team_id, scores.get(team_id) + 1);
        score_team1_display.setText(scores.get(0).toString());
        score_team2_display.setText(scores.get(1).toString());
    }

    private void addScoreListEntry(Integer team_id, String scorer, String min) {
        String content = String.format("%s %s'", scorer, min);

        if (team_id == 0) {
            goals_team1.add(content);
            goal_adapter_team1.notifyDataSetChanged();
        } else {
            goals_team2.add(content);
            goal_adapter_team2.notifyDataSetChanged();
        }
    }

    private View createTextEditDialog(int header_text,
                                      int message_text,
                                      int edit_default) {
        LayoutInflater inflater = this.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.textedit_dialog, null);

        final TextView header = (TextView) dialogView.findViewById(R.id.dialog_header);
        header.setText(header_text);

        final TextView message = (TextView) dialogView.findViewById(R.id.dialog_message);
        message.setText(message_text);

        final EditText default_text = (EditText) dialogView.findViewById(R.id.dialog_textedit);
        default_text.setText(edit_default);

        return dialogView;
    }

    private void createGoalDialog(View v) {
        final int team_id = Integer.valueOf(v.getTag().toString());

        final String minute = timerDisplay.getText().toString().substring(0, 2);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View dialogView = createTextEditDialog(R.string.goal_dialog_header,
                R.string.goal_dialog_edit_label, R.string.goal_dialog_edit_default);
        dialogBuilder.setView(dialogView);

        final EditText Scorer = (EditText) dialogView.findViewById(R.id.dialog_textedit);

        dialogBuilder.setView(dialogView);
        dialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                setScores(team_id);
                addScoreListEntry(team_id, Scorer.getText().toString(), minute);
            }
        });
        dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        });
        AlertDialog b = dialogBuilder.create();
        b.show();
    }

    private void switchButtonState() {
        button_goal_team1.setEnabled(!hasGameEnded);
        button_goal_team2.setEnabled(!hasGameEnded);

        if (button_start.getTag().equals(0) | button_start.getTag().equals(2)) {
            button_start.setText(R.string.button_pause);
            button_start.setTag(1);
        } else if (button_start.getTag().equals(1)) {
            button_start.setText(R.string.button_resume);
            button_start.setTag(2);
        } else if (button_start.getTag().equals(4)) {
            button_start.setText(R.string.button_start);
            button_start.setEnabled(false);
            button_start.setTag(0);
        } else {
            button_start.setText(R.string.button_start);
            button_start.setEnabled(true);
            button_start.setTag(0);
        }
    }

}
