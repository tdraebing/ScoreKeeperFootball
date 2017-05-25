package org.driven_by_data.scorekeeper.scorekeeperfootball;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tdraebing on 5/23/2017.
 */

public class Game implements Parcelable {
    List<Integer> scores = new ArrayList<>(2);
    int halfTime;
    boolean hasGameEnded;
    ArrayList<String> goals_team1;
    ArrayList<String> goals_team2;

    public Game(){

    }

    public int describeContents(){
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        Parcelable[] history = new Parcelable[GAME_HISTORY.size()];
        for (int i = 0; i < GAME_HISTORY.size(); i++)
            history[i] = GAME_HISTORY.valueAt(i);

        dest.writeInt(ROUNDS);
        dest.writeInt(CURRENTROUND);
        dest.writeParcelableArray(history, flags);
        dest.writeParcelable(SCORE, flags);
        dest.writeByte((byte) (isEnded ? 1 : 0));
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public Game createFromParcel(Parcel in) {
            return new Game(in);
        }

        public Game[] newArray(int size) {
            return new Game[size];
        }
    };
}
