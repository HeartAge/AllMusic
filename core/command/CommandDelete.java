package coloryr.allmusic.core.command;

import coloryr.allmusic.core.AllMusic;
import coloryr.allmusic.core.music.play.PlayMusic;
import coloryr.allmusic.core.utils.Function;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDelete extends ACommand {
    @Override
    public void ex(Object sender, String name, String[] args) {
        if (args.length != 2) {
            AllMusic.side.sendMessage(sender, AllMusic.getMessage().command.error);
            return;
        }
        if (!args[1].isEmpty() && Function.isInteger(args[1])) {
            int music = Integer.parseInt(args[1]);
            if (music == 0) {
                AllMusic.side.sendMessage(sender, "§d[AllMusic3]§2请输入有效的序列ID");
                return;
            }
            if (music > PlayMusic.getListSize()) {
                AllMusic.side.sendMessage(sender, "§d[AllMusic3]§2序列号过大");
                return;
            }
            PlayMusic.remove(music - 1);
            AllMusic.side.sendMessage(sender, "§d[AllMusic3]§2已删除序列" + music);
        } else {
            AllMusic.side.sendMessage(sender, "§d[AllMusic3]§2请输入有效的序列ID");
        }
    }

    @Override
    public List<String> tab(String name, String[] args, int index) {
        if (args.length == 1 || (args.length == 2 && args[1].isEmpty())) {
            List<String> list = new ArrayList<>();
            for (int a = 0; a < PlayMusic.getListSize(); a++) {
                list.add(String.valueOf(a + 1));
            }
            return list;
        }

        return Collections.emptyList();
    }
}
