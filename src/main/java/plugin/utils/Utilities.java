package plugin.utils;
import arc.Events;
import arc.func.Cons;
import arc.func.Func;
import arc.struct.Seq;
import arc.util.Reflect;
import arc.util.Threads;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Groups;
import mindustry.gen.Player;
import mindustry.maps.Map;
import mindustry.server.ServerControl;
import rhino.Context;
import rhino.NativeJavaObject;
import rhino.Scriptable;
import rhino.Undefined;

import java.util.ArrayList;

import static plugin.commands.MainCommands.votedPlayer;
import static plugin.commands.MainCommands.votes;

public class Utilities {
    private static final Scriptable scope = Reflect.get(Vars.mods.getScripts(), "scope");
    // finds player using their name (without colors)
    public static Player findPlayerByName(String name){
        return Groups.player.find(t-> t.plainName().contains(name));
    }
    public static <T> T notNullElse(T value, T value2){
        return value != null ? value : value2;
    }
    public static void voteCanceled(){
        Call.sendMessage("[red]Vote has been canceled!");
        votes.set(0);
        votedPlayer.clear();
    }
    public static void voteSuccess(Map map){
        Call.sendMessage("[green]Vote success! Changing map!");
        Vars.maps.setNextMapOverride(map);
        Events.fire(new EventType.GameOverEvent(Team.derelict));
        votes.set(0);
        votedPlayer.clear();
    }
    public static Seq<Map> getMaps(){
        return Vars.maps.customMaps().copy();
    }
    public static <T> String stringify(ArrayList<T> arr, Func<T, String> stringer) {
        if (arr == null || arr.isEmpty()) return "";
        StringBuilder out = new StringBuilder();
        for (T elem: arr) {
            out.append(stringer.get(elem));
        }
        return out.toString();
    }
    public static void runJs(String code, Cons<String> result) {
        Threads.thread(() -> {
            Object out;
            try {
                Object resp = Context.enter().evaluateString(scope, code, "console.js", 1);
                if (resp instanceof NativeJavaObject o) {
                    out = o.unwrap();
                } else if (resp instanceof Undefined) {
                    out = "undefined";
                } else {
                    out = String.valueOf(resp).trim();
                }
            } catch (Exception e) {
                out = e.getClass().getSimpleName() + ": " + (e.getMessage() != null ? e.getMessage() : " ");
            }
            Context.exit();
            result.get(out != null ? out.toString(): "null");
        });
    }
}

